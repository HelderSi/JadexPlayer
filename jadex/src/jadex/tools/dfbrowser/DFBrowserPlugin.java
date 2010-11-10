package jadex.tools.dfbrowser;

import jadex.adapter.fipa.AMSAgentDescription;
import jadex.adapter.fipa.AgentDescription;
import jadex.adapter.fipa.SearchConstraints;
import jadex.adapter.fipa.ServiceDescription;
import jadex.runtime.IGoal;
import jadex.tools.common.AgentTreeTable;
import jadex.tools.common.GuiProperties;
import jadex.tools.common.jtreetable.DefaultTreeTableNode;
import jadex.tools.common.plugin.IAgentListListener;
import jadex.tools.jcc.AbstractJCCPlugin;
import jadex.util.SGUI;
import jadex.util.SUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

/**
 *  DFBrowserPlugin
 */
public class DFBrowserPlugin extends AbstractJCCPlugin implements IAgentListListener
{
	//-------- constants --------
	
	/** The image icons. */
	protected static final UIDefaults icons = new UIDefaults(new Object[]
	{
		"dfbrowser", SGUI.makeIcon(DFBrowserPlugin.class, "/jadex/tools/common/images/new_dfbrowser.png"), 
		"dfbrowser_sel", SGUI.makeIcon(DFBrowserPlugin.class, "/jadex/tools/common/images/new_dfbrowser_sel.png"), 
		"remove_agent", SGUI.makeIcon(DFBrowserPlugin.class, "/jadex/tools/common/images/new_remove_service.png"),
		"starter", SGUI.makeIcon(DFBrowserPlugin.class, "/jadex/tools/common/images/new_starter.png"),
		"refresh", SGUI.makeIcon(DFBrowserPlugin.class, "/jadex/tools/common/images/new_refresh_anim00.png")
	});

	/** Don't refresh. */
	protected static final long REFRESH0 = 0;
	
	/** Refresh every second */
	protected static final long REFRESH1 = 1000;

	/** Refresh every 5 seconds */
	protected static final long REFRESH5 = 5000;

	/** Refresh every 30 seconds */
	protected static final long REFRESH30 = 30000;
	
	//-------- attributes --------
	
	/** The agent table (showing platform DFs). */
	protected AgentTreeTable df_agents;

	/** The agent table. */
	protected DFAgentTable agent_table;

	/** The service table. */
	protected DFServiceTable service_table;

	/** The service panel. */
	protected ServiceDescriptionPanel service_panel;

	/** How long should the refresh process wait */
	protected long sleep = REFRESH5;

	/** The thread that refreshes the plugin views */
	protected volatile Thread refresh_thread;

	/** The first split pane. */
	protected JSplitPane split1;

	/** The second split pane. */
	protected JSplitPane split2;

	/** The third split pane. */
	protected JSplitPane split3;
	
	/** Refresh setting . */
	protected JRadioButtonMenuItem refresh0;
	
	/** Refresh setting . */
	protected JRadioButtonMenuItem refresh1;

	/** Refresh setting . */
	protected JRadioButtonMenuItem refresh5;

	/** Refresh setting . */
	protected JRadioButtonMenuItem refresh30;

	/** The old agent descriptions. */
	protected AgentDescription[] old_ads;

	//-------- methods --------
	
	/**
	 * @return "DF Browser"
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#getName()
	 */
	public String getName()
	{
		return "DF Browser";
	}

	/**
	 * @return the icon of DFBrowser
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#getToolIcon()
	 */
	public Icon getToolIcon(boolean selected)
	{
		return selected? icons.getIcon("dfbrowser_sel"): icons.getIcon("dfbrowser");
	}

	/**
	 *  Create tool bar.
	 *  @return The tool bar.
	 */
	public JComponent[] createToolBar()
	{
		JButton b;

		b = new JButton(REMOVE_AGENT);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);

		return new JComponent[]{b};
	}
	
	/**
	 *  Create main panel.
	 *  @return The main panel.
	 */
	public JComponent createView()
	{
		df_agents = new AgentTreeTable();
		df_agents.setMinimumSize(new Dimension(0, 0));
		df_agents.getTreetable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		df_agents.getNodeType(AgentTreeTable.NODE_AGENT).addPopupAction(REFRESH_DF);

		service_panel = new ServiceDescriptionPanel();
		service_table = new DFServiceTable();
		JScrollPane stscroll = new JScrollPane(service_table);
		stscroll.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Registered Services"));
		
		agent_table = new DFAgentTable(this);
		JScrollPane atscroll = new JScrollPane(agent_table);
		atscroll.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Registered Agent Descriptions"));
		
		// Listeners
		jcc.addAgentListListener(this);
		
		df_agents.getTreetable().getSelectionModel().addListSelectionListener(
			new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				//System.out.println("valueChanged: "+df_agents.getTreetable().getSelectedRow());
				refresh();
			}
		});
		/*df_agents.getTreetable().addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2)
				{
					refresh();
				}
			}
		});*/
		agent_table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				//updateServices(old_ads);
				AgentDescription[] selagents = agent_table.getSelectedAgents();
				service_table.setAgentDescriptions(selagents);
			}
		});
		service_table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				updateDetailedService();
			}
		});
	
		JPanel main_panel = new JPanel(new BorderLayout());
		
		split3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split3.setDividerLocation(250);
		split3.add(stscroll);
		split3.add(service_panel);
		split3.setResizeWeight(1.0);
		split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split2.setDividerLocation(250);
		split2.add(atscroll);
		split2.add(split3);
		split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split1.setDividerLocation(200);
		split1.setOneTouchExpandable(true);
		split1.add(df_agents);
		split1.add(split2);
	
		main_panel.removeAll();
		main_panel.add(split1, BorderLayout.CENTER);
		main_panel.validate();

		// Hack! Double invokeLater ensures that selection code is executed after agentBorn
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				//SwingUtilities.invokeLater(new Runnable()
				//{
					//public void run()
					//{
						df_agents.adjustColumnWidths();
						
						DefaultTreeTableNode[] nodes = df_agents.getAllAgents();
						//System.out.println(SUtil.arrayToString(nodes));
						for(int i=0; i<nodes.length; i++)
						{
							AMSAgentDescription desc = (AMSAgentDescription)nodes[i].getUserObject();
							if(desc.getName().getName().startsWith("df@"))
							{
								TreePath path = new TreePath(nodes[i].getPath());
						        int row = df_agents.getTreetable().getTree().getRowForPath(path);
						        df_agents.getTreetable().getSelectionModel().setSelectionInterval(row, row);
						        //System.out.println("seleected: "+row);
						        break;
							}
						}
					//}
				//});
			}
		});
		
		GuiProperties.setupHelp(split1, "tools.dfbrowser");

		return main_panel;
	}
	
	/**
	 *  Create menu bar.
	 *  @return The menu bar.
	 */
	public JMenu[] createMenuBar()
	{
		ButtonGroup group = new ButtonGroup();
		
		JMenu menu = new JMenu("Refresh");
		GuiProperties.setupHelp(menu, "tools.dfbrowser");

		refresh0 = new JRadioButtonMenuItem(new AbstractAction("Never")
		{
			public void actionPerformed(ActionEvent e)
			{
				sleep = REFRESH0;
			}
		});
		refresh0.setSelected(sleep == REFRESH0);
		group.add(refresh0);
		menu.add(refresh0);
		
		refresh1 = new JRadioButtonMenuItem(new AbstractAction("1 s")
		{
			public void actionPerformed(ActionEvent e)
			{
				sleep = REFRESH1;
				startRefreshThread();
			}
		});
		refresh1.setSelected(sleep == REFRESH1);
		group.add(refresh1);
		menu.add(refresh1);

		refresh5 = new JRadioButtonMenuItem(new AbstractAction("5 s")
		{
			public void actionPerformed(ActionEvent e)
			{
				sleep = REFRESH5;
				startRefreshThread();
			}
		});
		refresh5.setSelected(sleep == REFRESH5);
		group.add(refresh5);
		menu.add(refresh5);

		refresh30 = new JRadioButtonMenuItem(new AbstractAction("30 s")
		{
			public void actionPerformed(ActionEvent e)
			{
				sleep = REFRESH30;
				startRefreshThread();
			}
		});
		refresh30.setSelected(sleep == REFRESH30);
		group.add(refresh30);
		menu.add(refresh30);

		//menu.addSeparator();

		return new JMenu[]{menu};
	}
	
	/**
	 *  Load the properties.
	 *  @param props
	 */
	public void setProperties(Properties props)
	{
		// try to load arguments from properties.
		try
		{
			split3.setDividerLocation(Integer.parseInt(props.getProperty("split3.location")));
		}
		catch(Exception e)
		{
		}
		try
		{
			split2.setDividerLocation(Integer.parseInt(props.getProperty("split2.location")));
		}
		catch(Exception e)
		{
		}
		try
		{
			split1.setDividerLocation(Integer.parseInt(props.getProperty("split1.location")));
		}
		catch(Exception e)
		{
		}
		//System.out.println("setProps: "+props.getProperty("refresh"));
		
		agent_table.setProperties(props);
		
		service_table.setProperties(props);
		
		try
		{
			this.sleep = Long.parseLong(props.getProperty("sleep"));
		}
		catch(Exception e)
		{
		}
		
		refresh0.setSelected(sleep == REFRESH0);
		refresh1.setSelected(sleep == REFRESH1);
		refresh5.setSelected(sleep == REFRESH5);
		refresh30.setSelected(sleep == REFRESH30);
		
		startRefreshThread();
	}

	/**
	 *  Save the properties.
	 *  @param props
	 */
	public void getProperties(Properties props)
	{
		props.put("split1.location", Integer.toString(split1.getDividerLocation()));
		props.put("split2.location", Integer.toString(split2.getDividerLocation()));
		props.put("split3.location", Integer.toString(split3.getDividerLocation()));
		if(refresh0.isSelected())
			props.put("sleep", ""+new Long(REFRESH0));
		else if(refresh1.isSelected())
			props.put("sleep", ""+new Long(REFRESH1));
		else if(refresh5.isSelected())
			props.put("sleep", ""+new Long(REFRESH5));
		else if(refresh30.isSelected())
			props.put("sleep", ""+new Long(REFRESH30));
		agent_table.getProperties(props);
		service_table.getProperties(props);
	}

	/**
	 *  Refresh the view.
	 */
	protected void refresh()
	{
		//System.out.println("refresh: "+getSelectedDF());
		AgentDescription[] ads = new AgentDescription[0];
		
		if(getSelectedDF() != null)
		{
			SearchConstraints constraints = new SearchConstraints();
			constraints.setMaxResults(-1);

			// Use a subgoal to search
			IGoal ft = getJCC().getAgent().createGoal("df_search");
			ft.getParameter("description").setValue(new AgentDescription());
			ft.getParameter("constraints").setValue(constraints);
			ft.getParameter("df").setValue(getSelectedDF().getName());

			try
			{
				getJCC().getAgent().dispatchTopLevelGoalAndWait(ft);
				ads = (AgentDescription[])ft.getParameterSet("result").getValues();
			}
			catch(Exception e)
			{
				//e.printStackTrace();
				String text = SUtil.wrapText("Could not refresh descriptions: "+e.getMessage());
				JOptionPane.showMessageDialog(SGUI.getWindowParent(getView()), text, "Refresh Problem", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		
		if(old_ads == null || !Arrays.equals(old_ads, ads))
		{
			agent_table.setAgentDescriptions(ads);
			updateServices(ads);
			updateDetailedService();
			old_ads = ads;
		}
	}
	
	/**
	 *  Update the services panel.
	 *  @param ads The agent descriptions.
	 */
	public void updateServices(AgentDescription[] ads)
	{
		AgentDescription[] selagents = agent_table.getSelectedAgents();
		if(selagents.length==0)
			service_table.setAgentDescriptions(ads);
	}
	
	/**
	 *  Update the detail view of services.
	 */
	public void updateDetailedService()
	{
		Object[] sdescs = service_table.getSelectedServices();
		service_panel.setService((AgentDescription)sdescs[1], 
			(ServiceDescription)sdescs[0]);
	}
	
	/**
	 * @param ad
	 */
	public void agentDied(final AMSAgentDescription ad)
	{
		// Update components on awt thread.
		/*SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{*/
				df_agents.removeAgent(ad);
				//refresh();
			/*}
		});*/
	}

	/**
	 * @param ad
	 */
	public void agentBorn(final AMSAgentDescription ad)
	{
		//System.out.println("Agent born: "+ad.getName()+" "+Thread.currentThread());
		
		// Update components on awt thread.
		/*SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{*/
				// todo: Megahack replace with sth. useful (agent need not be there,
				// todo: other agents could be the df and not be named df?)
				if(ad.getName().getName().startsWith("df@"))
				{
					df_agents.addAgent(ad);
					//refresh();
				}
			/*}
		});*/
	}
	
	/**
	 * @param ad
	 */
	public void agentChanged(final AMSAgentDescription ad)
	{
		// nop?
		// Update components on awt thread.
		/*SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				agents.addAgent(ad);
			}
		});*/
	}	

	/**
	 *  Get the selected df.
	 *  @return The selected DF.
	 */
	public AMSAgentDescription getSelectedDF()
	{
		AMSAgentDescription ret = null;
		if(df_agents.getTreetable().getTree().getSelectionPath()!=null)
		{
			DefaultTreeTableNode node = (DefaultTreeTableNode)df_agents.getTreetable().getTree()
				.getSelectionPath().getLastPathComponent();
			if(node != null && node.getUserObject() instanceof AMSAgentDescription)
				ret = (AMSAgentDescription)node.getUserObject();
		}
		return ret;
	}
	
	
	/**
	 * @return the help id of the perspective
	 * @see jadex.tools.jcc.AbstractJCCPlugin#getHelpID()
	 */
	public String getHelpID()
	{
		return "tools.dfbrowser";
	}

	/**
	 *  todo: Remove!
	 *  Refresh the descriptions.
	 */
	protected void startRefreshThread()
	{
		if(refresh_thread == null && !refresh0.isSelected())
		{
			//System.out.println(refresh_thread+" "+refresh0.isSelected());
			// todo: remove extra thread!
		
			refresh_thread = new Thread("DFBrowser refresh thread")
			{
				public void run()
				{
					//System.out.println("refresher started");
					try
					{
						while(!refresh0.isSelected() && refresh_thread == this)
						{
							//System.out.println("refreshing");
							refresh();
							sleep(DFBrowserPlugin.this.sleep);
						}
					}
					catch(Exception e)
					{
					}
					if(refresh_thread == this)
					{
						refresh_thread = null;
					}
					//System.out.println("refresher ended");
				}
			};
			refresh_thread.start();
		}
	}

	/**
	 * @see jadex.tools.jcc.AbstractJCCPlugin#shutdown()
	 */
	public void shutdown()
	{
		refresh_thread = null;
	}

	/**
	 * @param description
	 */
	protected void removeAgentRegistration(AgentDescription description)
	{
		try
		{
			IGoal deregister = getJCC().getAgent().createGoal("df_deregister");
			deregister.getParameter("description").setValue(description);
			deregister.getParameter("df").setValue(getSelectedDF().getName());
			getJCC().getAgent().dispatchTopLevelGoalAndWait(deregister, 100);
			refresh();
		}
		catch(Exception e)
		{
			// NOP
		}
	}
	
	/**
	 *  Removes agent from the df.
	 */
	final AbstractAction REMOVE_AGENT = new RemoveAgentAction();

	/**
	 *  The remove agent action. 
	 */
	protected class RemoveAgentAction extends AbstractAction
	{
		/**
		 * Create a new action.
		 */
		protected RemoveAgentAction()
		{
			super("Remove agent registration", icons.getIcon("remove_agent"));
		}

		/**
		 * @param e
		 */
		public void actionPerformed(ActionEvent e)
		{
			boolean rem = false;
			if(getSelectedDF() != null)
			{
				AgentDescription[] as = agent_table.getSelectedAgents();
				for(int i = 0; i < as.length; i++)
				{
					removeAgentRegistration(as[i]);
				}
				rem = as.length>0;
				//refresh();
			}
			if(!rem)
			{
				String text = SUtil.wrapText("No agent description selected for removal.");
				JOptionPane.showMessageDialog(SGUI.getWindowParent(getView()), text, 
					"Remove Problem", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	/**
	 *  Refresh df action.
	 */
	final AbstractAction REFRESH_DF = new RefreshDFAction();

	/**
	 *  The refresh DF action. 
	 */
	protected class RefreshDFAction extends AbstractAction
	{
		/**
		 * Create a new action.
		 */
		protected RefreshDFAction()
		{
			super("Refresh DF view", icons.getIcon("refresh"));
		}

		/**
		 * @param e
		 */
		public void actionPerformed(ActionEvent e)
		{
			refresh();
		}
	}
}