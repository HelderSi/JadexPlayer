package jadex.micro.examples.mandelbrot;

import jadex.bridge.IComponentManagementService;
import jadex.bridge.IExternalAccess;
import jadex.commons.IFuture;
import jadex.commons.concurrent.SwingDefaultResultListener;
import jadex.commons.service.IServiceProvider;
import jadex.commons.service.SServiceProvider;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *  Panel for displaying calculated results.
 */
public class DisplayPanel extends JComponent
{
	//-------- constants --------
	
	/** The help text. */
	public static final String	HELPTEXT	=
		"Use mouse to navigate:\n" +
		"[wheel] zoom in/out\n" +
		"[left button] choose and click into area\n" +
		"[rigth button] drag to move, click for original area.\n";
	
	//-------- attributes --------
	
	/** The service provider. */
	protected IServiceProvider	provider;
	
	/** The colors for drawing. */
	protected Color[]	colors;
	
	/** The latest area data used for determining original coordinates of painted regions. */
	protected AreaData	data;
	
	/** The current image derived from the results. */
	protected Image	image;
	
	/** The current selection start point (if any). */
	protected Point	point;
	
	/** The current selection range (if any). */
	protected Rectangle	range;
	
	/** Flag indicating that a calculation is in progress. */
	protected boolean	calculating;
	
	/** Progress data objects, available only when calculating (progress data -> percent finished). */
	protected Map	progressdata;
	
	/** Progress update timer. */
	protected Timer	progressupdate;
	
	/** Start point for dragging (if any). */
	protected Point	startdrag;
	
	/** End point for dragging (if any). */
	protected Point	enddrag;

	//-------- constructors --------
	
	/**
	 *  Create a new display panel.
	 */
	public DisplayPanel(final IServiceProvider provider)
	{
		this.provider	= provider;
		setColorScheme(new Color[]{new Color(50, 100, 0), Color.red});
		calcDefaultImage();
		
		// Dragging with right mouse button.
		MouseAdapter draghandler = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(!calculating && e.getButton()==MouseEvent.BUTTON3 && e.getClickCount()==1 && image!=null)
				{
					startdrag = new Point(e.getX(), e.getY());
					range	= null;
					point	= null;
				}
				else
				{
					startdrag	= null;
				}
			}
			
			public void mouseDragged(MouseEvent e)
			{
				if(startdrag!=null)
				{
					enddrag = new Point(e.getX(), e.getY());
					repaint();
				}
			}
			
			public void mouseReleased(MouseEvent e)
			{
				if(startdrag!=null && enddrag!=null)
				{
//					System.out.println("dragged: "+startdrag+" "+enddrag);
					dragImage();
				}
			}
		};
		addMouseMotionListener(draghandler);
		addMouseListener(draghandler);
				
		// Zooming with mouse wheel.
		addMouseWheelListener(new MouseAdapter()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if(!calculating)
				{
					int sa = e.getScrollAmount();
					double	dir = Math.signum(e.getWheelRotation());
					double	percent	= 10*sa;
					double	factor;
					if(dir>0)
					{
						factor	= (100+percent)/100;
					}
					else
					{
						factor	= 100/(100+percent);
					}
					zoomImage(e.getX(), e.getY(), factor);
				}
			}
		});
		
		// Selecting range and default area.
		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(SwingUtilities.isRightMouseButton(e))
				{
					calcDefaultImage();
				}
				
				else if(!calculating && range!=null)
				{
					if(e.getX()>=range.x && e.getX()<=range.x+range.width
						&& e.getY()>=range.y && e.getY()<=range.y+range.height)
					{
						zoomIntoRange();
					}
				}
			}
			
			public void mousePressed(MouseEvent e)
			{
				if(!calculating)
				{
					if(SwingUtilities.isRightMouseButton(e))
					{
						DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					else
					{
						point	= e.getPoint();
						DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
				}
			}
			
			public void mouseReleased(MouseEvent e)
			{
				if(!calculating)
				{
					DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		addMouseMotionListener(new MouseAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				if(!calculating && range!=null)
				{
					if(e.getX()>=range.x && e.getX()<=range.x+range.width
						&& e.getY()>=range.y && e.getY()<=range.y+range.height)
					{
						DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					else
					{
						DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));						
					}
				}
			}
			
			public void mouseDragged(MouseEvent e)
			{
				if(!calculating && point!=null)
				{
					range	= new Rectangle(
						point.x<e.getX() ? point.x : e.getX(),
						point.y<e.getY() ? point.y : e.getY(),
						Math.abs(point.x-e.getX()), Math.abs(point.y-e.getY()));
					
					repaint();
				}
			}
		});
		
		// ESC stops dragging / range selection
		setFocusable(true);
		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
				{
					range	= null;
					point	= null;
					startdrag	= null;
					enddrag	= null;
					DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));						
					repaint();
				}
			}
		});
	}
	
	//-------- methods --------
	
	/**
	 *  Set new results.
	 */
	public void	setResults(final AreaData data)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int[][]	results	= data.getData();
				DisplayPanel.this.data	= data;
				DisplayPanel.this.image	= createImage(results.length, results[0].length);
				Graphics	g	= image.getGraphics();
				for(int x=0; x<results.length; x++)
				{
					for(int y=0; y<results[x].length; y++)
					{
						Color	c;
						if(results[x][y]==-1)
						{
							c	= Color.black;
						}
						else
						{
							c	= colors[results[x][y]%colors.length];
						}
						g.setColor(c);
						g.drawLine(x, y, x, y);
					}
				}
				
				point	= null;
				range	= null;
				progressdata	= null;
				if(progressupdate!=null)
				{
					progressupdate.stop();
					progressupdate	= null;
				}
				calculating	= false;
				DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				getParent().invalidate();
				getParent().doLayout();
				getParent().repaint();
			}
		});
	}
	
	/**
	 *  Display intermediate calculation results.
	 */
	public void addProgress(final ProgressData progress)
	{		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(progressdata==null)
					progressdata	= new HashMap();
				
				Integer	percent	= (Integer)progressdata.remove(progress);
				if(percent==null || progress.isFinished())
				{
					percent	= new Integer(progress.isFinished() ? 100 : 0);
				}
				progressdata.put(progress, percent);
				repaint();
				
				if(progressupdate==null)
				{
					progressupdate	= new Timer(200, new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							SServiceProvider.getService(provider, IComponentManagementService.class)
								.addResultListener(new SwingDefaultResultListener(DisplayPanel.this)
							{
								public void customResultAvailable(Object source, Object result)
								{
									IComponentManagementService	cms	= (IComponentManagementService)result;
									if(progressdata!=null)
									{
										for(Iterator it=progressdata.keySet().iterator(); it.hasNext(); )
										{
											final ProgressData	progress	= (ProgressData)it.next();
											cms.getExternalAccess(progress.getProviderId())
												.addResultListener(new SwingDefaultResultListener(DisplayPanel.this)
											{
												public void customResultAvailable(Object source, Object result)
												{
													IExternalAccess	ea	= (IExternalAccess)result;
													SServiceProvider.getService(ea.getServiceProvider(), IProgressService.class)
														.addResultListener(new SwingDefaultResultListener(DisplayPanel.this)
													{
														public void customResultAvailable(Object source, Object result)
														{
															IProgressService	ps	= (IProgressService)result;
															if(ps!=null)
															{
																ps.getProgress(progress.getTaskId())
																	.addResultListener(new SwingDefaultResultListener(DisplayPanel.this)
																{
																	public void customResultAvailable(Object source, Object result)
																	{
																		if(progressdata!=null && progressdata.containsKey(progress))
																		{
																			Integer	current	= (Integer)result;
																			Integer	percent	= (Integer)progressdata.get(progress);
																			if(current.intValue()>percent.intValue())
																			{
																				progressdata.put(progress, current);
																				repaint();
																			}
																		}
																	}
	
																	public void customExceptionOccurred(Object source, Exception exception)
																	{
																		// ignore
																	}
																});
															}
														}
	
														public void customExceptionOccurred(Object source, Exception exception)
														{
															// ignore
														}
													});
												}
	
												public void customExceptionOccurred(Object source, Exception exception)
												{
													// ignore
												}
											});
										}
									}
								}

								public void customExceptionOccurred(Object source, Exception exception)
								{
									// ignore
								}
							});
						}
					});
					progressupdate.start();
				}
			}
		});
	}
	
	//-------- JPanel methods --------
	
	/**
	 *  Paint the results.
	 */
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		// Draw image.
		if(image!=null)
		{
			Rectangle bounds = getInnerBounds(true);
			int	ix	= 0;
			int iy	= 0;
			int	iwidth	= image.getWidth(this);
			int iheight	= image.getHeight(this);
			Rectangle drawarea = scaleToFit(bounds, iwidth, iheight);

			// Zoom into original image while calculating
			if(calculating && range!=null)
			{
				ix	= (range.x-drawarea.x-bounds.x)*iwidth/drawarea.width;
				iy	= (range.y-drawarea.y-bounds.y)*iheight/drawarea.height;
				iwidth	= range.width*iwidth/drawarea.width;
				iheight	= range.height*iheight/drawarea.height;
				
				// Scale again to fit new image size.
				drawarea = scaleToFit(bounds, iwidth, iheight);
				
				g.drawImage(image, bounds.x+drawarea.x, bounds.y+drawarea.y,
					bounds.x+drawarea.x+drawarea.width, bounds.y+drawarea.y+drawarea.height,
					ix, iy, ix+iwidth, iy+iheight, this);
			}
			
			// Offset and clip image and show border while dragging.
			else if(startdrag!=null && enddrag!=null)
			{
				// Draw original image in background
				g.drawImage(image, bounds.x+drawarea.x, bounds.y+drawarea.y,
					bounds.x+drawarea.x+drawarea.width, bounds.y+drawarea.y+drawarea.height,
					ix, iy, ix+iwidth, iy+iheight, this);
				g.setColor(new Color(32,32,32,160));
				g.fillRect(bounds.x+drawarea.x, bounds.y+drawarea.y, drawarea.width, drawarea.height);

				// Draw offsetted image in foreground
				Shape	clip	= g.getClip();
				g.setClip(bounds.x+drawarea.x, bounds.y+drawarea.y, drawarea.width, drawarea.height);
				int	xoff	= enddrag.x-startdrag.x;
				int	yoff	= enddrag.y-startdrag.y;
				g.drawImage(image, bounds.x+drawarea.x+xoff, bounds.y+drawarea.y+yoff,
					bounds.x+drawarea.x+xoff+drawarea.width, bounds.y+drawarea.y+yoff+drawarea.height,
					ix, iy, ix+iwidth, iy+iheight, this);
				g.setClip(clip);
			}
			else
			{
				g.drawImage(image, bounds.x+drawarea.x, bounds.y+drawarea.y,
					bounds.x+drawarea.x+drawarea.width, bounds.y+drawarea.y+drawarea.height,
					ix, iy, ix+iwidth, iy+iheight, this);
			}
			
			// Draw progress boxes.
			if(progressdata!=null)
			{
				JProgressBar	bar	= new JProgressBar(0, 100);
				bar.setStringPainted(true);
				Dimension	barsize	= bar.getPreferredSize();
				for(Iterator it=progressdata.keySet().iterator(); it.hasNext(); )
				{
					ProgressData	progress	= (ProgressData)it.next();
					
					double xf = drawarea.getWidth()/progress.getImageWidth();
					double yf = drawarea.getHeight()/progress.getImageHeight();
					int corx = (int)(progress.getArea().x*xf);
					int cory = (int)(progress.getArea().y*yf);
					int corw = (int)(progress.getArea().width*xf);
					int corh = (int)(progress.getArea().height*yf);
					
					if(!progress.isFinished())
					{
						g.setColor(new Color(32,32,32,160));
						g.fillRect(bounds.x+drawarea.x+corx+1, bounds.y+drawarea.y+cory+1, corw-1, corh-1);							
					}
					g.setColor(Color.white);
					g.drawRect(bounds.x+drawarea.x+corx, bounds.y+drawarea.y+cory, corw, corh);
					
					// Print provider name.
					if(progress.getProviderId()!=null)
					{
						String	name	= progress.getProviderId().toString();
						String	provider	= "";
						int index	=	name.indexOf('@');
						if(index!=-1)
						{
							provider	= name.substring(index+1);
							name	= name.substring(0, index);
						}
						provider	= progress.getTaskId().toString();
						
						FontMetrics	fm	= g.getFontMetrics();
						Rectangle2D	sb1	= fm.getStringBounds(name, g);
						Rectangle2D	sb2	= fm.getStringBounds(provider, g);
						int width	= (int)Math.max(sb1.getWidth(), sb2.getWidth());
						int	height	= fm.getHeight()*2 + barsize.height + 2;
						if(width<corw-4 && height<corh-4)
						{
							// Draw provider id.
							int	x	= bounds.x+drawarea.x+corx + (corw-width)/2;
							int	y	= bounds.y+drawarea.y+cory + (corh-height)/2 + fm.getLeading()/2;
							g.drawString(name, x, y + fm.getAscent());
							g.drawString(provider, x, y + fm.getAscent() + fm.getHeight());
							
							// Draw progress bar.
							if(!progress.isFinished())
							{
								bar.setStringPainted(true);
								bar.setValue(((Number)progressdata.get(progress)).intValue());
								width	= Math.min(corw-10, barsize.width);
								x	= bounds.x+drawarea.x+corx + (corw-width)/2;
								y	= y + fm.getHeight()*2 + 2;
								bar.setBounds(0, 0, width, barsize.height);
								Graphics	g2	= g.create();
								g2.translate(x, y);
								bar.paint(g2);
							}
						}
						else if(!progress.isFinished() && corw>8 && corh>8)
						{
							bar.setStringPainted(false);
							int	x	= bounds.x+drawarea.x+corx + 2;
							int	y	= bounds.y+drawarea.y+cory + Math.max((corh-barsize.height)/2, 2);
							bar.setValue(((Number)progressdata.get(progress)).intValue());
							bar.setBounds(0, 0, corw-4, Math.min(barsize.height, corh-4));
							Graphics	g2	= g.create();
							g2.translate(x, y);
							bar.paint(g2);
						}
					}
				}
			}
		}
		
		// Draw range area.
		if(!calculating && range!=null)
		{
			Rectangle bounds = getInnerBounds(false);
			double	rratio	= (double)range.width/range.height;
			double	bratio	= (double)bounds.width/bounds.height;
			
			// Draw left and right boxes to show unused space
			if(rratio<bratio)
			{
				int	drawwidth	= range.height*bounds.width/bounds.height;
				int offset = (range.width-drawwidth)/2;
				g.setColor(new Color(128,128,128,64));
				g.fillRect(range.x+offset, range.y, -offset, range.height+1);
				g.fillRect(range.x+range.width, range.y, -offset, range.height+1);
			}
			// Draw upper and lower boxes to show unused space
			else if(rratio>bratio)
			{
				int	drawheight	= range.width*bounds.height/bounds.width;
				int offset = (range.height-drawheight)/2;
				g.setColor(new Color(128,128,128,64));
				g.fillRect(range.x, range.y+offset, range.width+1, -offset);
				g.fillRect(range.x, range.y+range.height, range.width+1, -offset);
			}
		
			g.setColor(Color.white);
			g.drawRect(range.x, range.y, range.width, range.height);
		}
	}
	
	/**
	 *  Calculate draw area for image.
	 */
	protected Rectangle scaleToFit(Rectangle bounds, int iwidth, int iheight)
	{
		double	iratio	= (double)iwidth/iheight;
		double	bratio	= (double)bounds.width/bounds.height;
		Rectangle	drawarea	= new Rectangle(0, 0, bounds.width, bounds.height);
		
		// Scale to fit height
		if(iratio<bratio)
		{
			 double	hratio	= (double)bounds.height/iheight;
			 drawarea.width	= (int)(iwidth*hratio);
			 drawarea.x	= (bounds.width-drawarea.width)/2;
		}
		// Scale to fit width
		else if(iratio>bratio)
		{
			 double	wratio	= (double)bounds.width/iwidth;
			 drawarea.height	= (int)(iheight*wratio);
			 drawarea.y	= (bounds.height-drawarea.height)/2;
		}
		return drawarea;
	}

	/**
	 *  Get the bounds with respect to insets (if any).
	 *  @param scrollarea	True when inner bounds of scroll area instead of visible window space should be considered.
	 */
	protected Rectangle getInnerBounds(boolean scrollarea)
	{
		Rectangle	bounds	= getBounds();

		if(!scrollarea && getParent() instanceof JViewport)
		{
			// Get bounds of outer scroll panel
			Rectangle	pbounds	= getParent().getParent().getBounds();
			if(bounds.width>pbounds.width || bounds.height>pbounds.height)
			{
				bounds	= pbounds;
			}
		}
		
		Insets	insets	= getInsets();
		if(insets!=null)
		{
			bounds.x	= insets.left;
			bounds.y	= insets.top;
			bounds.width	-= insets.left + insets.right;
			bounds.height	-= insets.top + insets.bottom;
		}
		else
		{
			bounds.x	= 0;
			bounds.y	= 0;
		}
		return bounds;
	}
	
	/**
	 *  Get the desired size of the panel.
	 */
	public Dimension getMinimumSize()
	{
		Insets	ins	= getInsets();
		Dimension	ret	= new Dimension(ins!=null ? ins.left+ins.right : 0, ins!=null ? ins.top+ins.bottom : 0);
		if(image!=null)
		{
			ret.width	+= image.getWidth(this);
			ret.height	+= image.getHeight(this);
		}
		return ret;
	}
	
	/**
	 *  Get the desired size of the panel.
	 */
	public Dimension getPreferredSize()
	{
		return getMinimumSize();
	}

	/**
	 *  Set the color scheme.
	 */
	public void	setColorScheme(Color[] scheme)
	{
		if(scheme==null || scheme.length==0)
		{
			colors	= new Color[]{Color.white};
		}
		else if(scheme.length==1)
		{
			colors	= scheme;
		}
		else
		{
			colors	= new Color[scheme.length*16];
			for(int i=0; i<colors.length; i++)
			{
				int index	= i/16;
				Color	start	= scheme[index];
				Color	end	= index+1<scheme.length ? scheme[index+1] : scheme[0];
				colors[i]	= new Color(
					(int)(start.getRed()+(double)(i%16)/16*(end.getRed()-start.getRed())),
					(int)(start.getGreen()+(double)(i%16)/16*(end.getGreen()-start.getGreen())),
					(int)(start.getBlue()+(double)(i%16)/16*(end.getBlue()-start.getBlue())));
			}
		}
		
		if(data!=null)
		{
			setResults(data);
		}
	}
	
	//-------- recalculation methods --------
	
	/**
	 *  Drag the image according to current drag settings.
	 */
	protected void dragImage()
	{
		Rectangle	bounds	= getInnerBounds(true);
		Rectangle	drawarea	= scaleToFit(bounds, image.getWidth(DisplayPanel.this), image.getHeight(DisplayPanel.this));
		int xdiff = startdrag.x-enddrag.x;
		int ydiff = startdrag.y-enddrag.y;
		double xp = ((double)xdiff)/drawarea.width;
		double yp = ((double)ydiff)/drawarea.height;
		
		double xm = (data.getXEnd()-data.getXStart())*xp;
		double ym = (data.getYEnd()-data.getYStart())*yp;
		final double xs = data.getXStart()+xm;
		final double xe = data.getXEnd()+xm;
		final double ys = data.getYStart()+ym;
		final double ye = data.getYEnd()+ym;
		
		startdrag = null;
		enddrag = null;
		range	= new Rectangle(bounds.x+drawarea.x+xdiff, bounds.y+drawarea.y+ydiff, drawarea.width, drawarea.height);

		calcArea(xs, xe, ys, ye, data.getSizeX(), data.getSizeY());
	}

	/**
	 *  Zoom into the given location by the given factor.
	 */
	protected void zoomImage(int x, int y, double factor)
	{
		final Rectangle	bounds	= getInnerBounds(true);
		
		int	iwidth	= image.getWidth(DisplayPanel.this);
		int iheight	= image.getHeight(DisplayPanel.this);
		final Rectangle drawarea = scaleToFit(bounds, iwidth, iheight);
		int mx = Math.min(bounds.x+drawarea.x+drawarea.width, Math.max(bounds.x+drawarea.x, x));
		int my = Math.min(bounds.y+drawarea.y+drawarea.height, Math.max(bounds.y+drawarea.y, y));
		double xrel = ((double)mx-(bounds.x+drawarea.x))/drawarea.width;
		double yrel = ((double)my-(bounds.y+drawarea.y))/drawarea.height;

		double wold = data.getXEnd()-data.getXStart();
		double hold = data.getYEnd()-data.getYStart();
		double wnew = wold*factor;
		double hnew = hold*factor;
		double wd = wold-wnew;
		double hd = hold-hnew;
		
		final double xs = data.getXStart()+wd*xrel;
		final double xe = xs+wnew;
		final double ys = data.getYStart()+hd*yrel;
		final double ye = ys+hnew;
		
		// Set range for drawing preview of zoom area.
		double	xdiff	= drawarea.width - drawarea.width*factor;
		double	ydiff	= drawarea.height - drawarea.height*factor;
		range	= new Rectangle(bounds.x+drawarea.x+(int)Math.round(xdiff*xrel), bounds.y+drawarea.y+(int)Math.round(ydiff*yrel),
			(int)Math.round(drawarea.width*factor), (int)Math.round(drawarea.height*factor));
		
//		zoomIntoRange();
		calcArea(xs, xe, ys, ye, data.getSizeX(), data.getSizeY());
	}

	/**
	 *  Set display coordinates to default values.
	 */
	protected void calcDefaultImage()
	{
		final Rectangle	bounds	= getInnerBounds(false);
		double	rratio	= 1;
		double	bratio	= (double)bounds.width/bounds.height;
		// Calculate pixel width/height of area.
		if(rratio<bratio)
		{
			int	width	= (int)(bounds.height*rratio);
			bounds.width	= width;
		}
		else if(rratio>bratio)
		{
			int	height	= (int)(bounds.width/rratio);
			bounds.height	= height;
		}
		else if(bounds.width==0 || bounds.height==0)
		{
			bounds.width	= 100;
			bounds.height	= 100;
		}
		
		// Clear image for painting only background.
		DisplayPanel.this.image	= createImage(bounds.width, bounds.height);
		
		calcArea(-2, 1, -1.5, 1.5, bounds.width, bounds.height);
	}
	
	/**
	 *  Zoom into the selected range.
	 */
	protected void zoomIntoRange()
	{
		// Calculate bounds relative to original image.
		Rectangle	bounds	= getInnerBounds(true);
		Rectangle	drawarea	= scaleToFit(bounds, image.getWidth(DisplayPanel.this), image.getHeight(DisplayPanel.this));
		final double	x	= (double)(range.x-bounds.x-drawarea.x)/drawarea.width;
		final double	y	= (double)(range.y-bounds.y-drawarea.y)/drawarea.height;
		final double	x2	= x + (double)range.width/drawarea.width;
		final double	y2	= y + (double)range.height/drawarea.height;
		
		// Original bounds
		final double	ox	= data.getXStart();
		final double	oy	= data.getYStart();
		final double	owidth	= data.getXEnd()-data.getXStart();
		final double	oheight	= data.getYEnd()-data.getYStart();
		
		// Calculate pixel width/height of visible area.
		bounds	= getInnerBounds(false);
		double	rratio	= (double)range.width/range.height;
		double	bratio	= (double)bounds.width/bounds.height;
		if(rratio<bratio)
		{
			bounds.width	= (int)(bounds.height*rratio);
		}
		else if(rratio>bratio)
		{
			bounds.height	= (int)(bounds.width/rratio);
		}
		final Rectangle	area	= bounds;

		calcArea(ox+owidth*x, ox+owidth*x2, oy+oheight*y, oy+oheight*y2, area.width, area.height);
	}

	/**
	 *  Calculate the given area.
	 */
	protected void calcArea(double x1, double x2, double y1, double y2, int sizex, int sizey)
	{
		final AreaData ad	= new AreaData(x1, x2, y1, y2, sizex, sizey,
			data!=null?data.getMax():256, data!=null?data.getParallel():10, data!=null?data.getTaskSize():300);
		
		DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		calculating	= true;
		repaint();
		
		SServiceProvider.getService(provider, IGenerateService.class)
			.addResultListener(new SwingDefaultResultListener()
		{
			public void customResultAvailable(Object source, Object result)
			{
				IGenerateService	gs	= (IGenerateService)result;
				IFuture	fut	= gs.generateArea(ad);
				fut.addResultListener(new SwingDefaultResultListener(DisplayPanel.this)
				{
					public void customResultAvailable(Object source, Object result)
					{
						DisplayPanel.this.setResults((AreaData)result);
					}
					public void customExceptionOccurred(Object source, Exception exception)
					{
						calculating	= false;
						DisplayPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						super.customExceptionOccurred(source, exception);
					}
				});
			}
		});
	}
}
