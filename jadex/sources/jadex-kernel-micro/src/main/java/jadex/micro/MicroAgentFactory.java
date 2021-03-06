package jadex.micro;

import jadex.bridge.IArgument;
import jadex.bridge.IComponentAdapterFactory;
import jadex.bridge.IComponentDescription;
import jadex.bridge.IComponentFactory;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IModelInfo;
import jadex.bridge.IErrorReport;
import jadex.bridge.IModelValueProvider;
import jadex.bridge.ModelInfo;
import jadex.commons.ByteClassLoader;
import jadex.commons.Future;
import jadex.commons.SGUI;
import jadex.commons.SReflect;
import jadex.commons.service.BasicService;
import jadex.commons.service.IServiceProvider;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.UIDefaults;

/**
 *  Factory for creating micro agents.
 */
public class MicroAgentFactory extends BasicService implements IComponentFactory
{
	//-------- constants --------
	
	/** The micro agent file type. */
	public static final String	FILETYPE_MICROAGENT	= "Micro Agent";
	
	/** The image icons. */
	protected static final UIDefaults icons = new UIDefaults(new Object[]
	{
		"micro_agent",	SGUI.makeIcon(MicroAgentFactory.class, "/jadex/micro/images/micro_agent.png"),
	});

	//-------- attributes --------
	
	/** The platform. */
	protected IServiceProvider provider;
	
	/** The properties. */
	protected Map properties;
	
	//-------- constructors --------
	
	/**
	 *  Create a new agent factory.
	 */
	public MicroAgentFactory(IServiceProvider provider, Map properties)
	{
		super(provider.getId(), IComponentFactory.class, null);

		this.provider = provider;
		this.properties = properties;
	}
	
	//-------- IAgentFactory interface --------
	
	/**
	 *  Load a  model.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return The loaded model.
	 */
	public IModelInfo loadModel(InputStream in, String[] imports, ClassLoader classloader)
	{
		IModelInfo ret = null;
		
		ByteClassLoader cl = new ByteClassLoader(classloader);
		BufferedInputStream bin = new BufferedInputStream(in);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try
		{
			int d;
			while((d = bin.read())!=-1)
				bos.write(d);
		
			Class cma = cl.loadClass(null, bos.toByteArray(), true);
			String model = cma.getName().replace('.', '/');
			
			ret = loadModel(model, cma, cl);
		}
		catch(Exception e)
		{
		}
		
		try
		{
			bin.close();
		}
		catch(IOException e)
		{
		}
		try
		{
			bos.close();
		}
		catch(IOException e)
		{
		}
		
		return ret;
	}
	
	/**
	 *  Load a  model.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return The loaded model.
	 */
	public IModelInfo loadModel(String model, String[] imports, ClassLoader classloader)
	{
//		System.out.println("loading micro: "+model);
		String clname = model;
		
		// Hack! for extracting clear classname
		if(clname.endsWith(".class"))
			clname = model.substring(0, model.indexOf(".class"));
		clname = clname.replace('\\', '.');
		clname = clname.replace('/', '.');
		
		Class cma = getMicroAgentClass(clname, imports, classloader);
		
		return loadModel(model, cma, classloader);
	}
	
	/**
	 *  Load the model.
	 */
	protected IModelInfo loadModel(String model, Class cma, ClassLoader classloader)
	{
		// Try to read meta information from class.
		MicroAgentMetaInfo metainfo = null;
		try
		{
			Method m = cma.getMethod("getMetaInfo", new Class[0]);
			if(m!=null)
				metainfo = (MicroAgentMetaInfo)m.invoke(null, new Object[0]);
		}
		catch(Exception e)
		{
//			e.printStackTrace();
		}
		String name = SReflect.getUnqualifiedClassName(cma);
		if(name.endsWith("Agent"))
			name = name.substring(0, name.lastIndexOf("Agent"));
		String packagename = cma.getPackage()!=null? cma.getPackage().getName(): null;
		String description = metainfo!=null && metainfo.getDescription()!=null? metainfo.getDescription(): null;
		IErrorReport report = null;
		String[] configurations = metainfo!=null? metainfo.getConfigurations(): null;
		IArgument[] arguments = metainfo!=null? metainfo.getArguments(): null;
		IArgument[] results = metainfo!=null? metainfo.getResults(): null;
		Map properties = metainfo!=null && metainfo.getProperties()!=null? new HashMap(metainfo.getProperties()): new HashMap();
		Class[] required = metainfo!=null? metainfo.getRequiredServices(): null;
		Class[] provided = metainfo!=null? metainfo.getProvidedServices(): null;
		IModelValueProvider master = metainfo!=null? metainfo.getMaster(): null;
		IModelValueProvider daemon= metainfo!=null? metainfo.getDaemon(): null;
		IModelValueProvider autosd = metainfo!=null? metainfo.getAutoShutdown(): null;
		
		// Add debugger breakpoints
		List names = new ArrayList();
		for(int i=0; metainfo!=null && i<metainfo.getBreakpoints().length; i++)
			names.add(metainfo.getBreakpoints()[i]);
		properties.put("debugger.breakpoints", names);
		
		// Exclude getServiceProvider() from remote external access interface
//		addExcludedMethods(properties, new String[]{"getServiceProvider"});
		
		IModelInfo ret = new ModelInfo(name, packagename, description, report, 
			configurations, arguments, results, true, model, properties, classloader, required, provided,
			master, daemon, autosd);
		
		return ret;
	}
	
	/**
	 *  Test if a model can be loaded by the factory.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return True, if model can be loaded.
	 */
	public boolean isLoadable(String model, String[] imports, ClassLoader classloader)
	{
		boolean ret = model.toLowerCase().endsWith("agent.class");
//		if(model.toLowerCase().endsWith("Agent.class"))
//		{
//			ILibraryService libservice = (ILibraryService)platform.getService(ILibraryService.class);
//			String clname = model.substring(0, model.indexOf(".class"));
//			Class cma = SReflect.findClass0(clname, null, libservice.getClassLoader());
//			ret = cma!=null && cma.isAssignableFrom(IMicroAgent.class);
//			System.out.println(clname+" "+cma+" "+ret);
//		}
		return ret;
	}
	
	/**
	 *  Test if a model is startable (e.g. an component).
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return True, if startable (and loadable).
	 */
	public boolean isStartable(String model, String[] imports, ClassLoader classloader)
	{
		return isLoadable(model, imports, classloader);
	}

	/**
	 *  Get the names of ADF file types supported by this factory.
	 */
	public String[] getComponentTypes()
	{
		return new String[]{FILETYPE_MICROAGENT};
	}

	/**
	 *  Get a default icon for a file type.
	 */
	public Icon getComponentTypeIcon(String type)
	{
		return type.equals(FILETYPE_MICROAGENT) ? icons.getIcon("micro_agent") : null;
	}

	/**
	 *  Get the component type of a model.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 */
	public String getComponentType(String model, String[] imports, ClassLoader classloader)
	{
		return model.toLowerCase().endsWith("agent.class") ? FILETYPE_MICROAGENT: null;
	}
	
	/**
	 * Create a component instance.
	 * @param adapter The component adapter.
	 * @param model The component model.
	 * @param config The name of the configuration (or null for default configuration) 
	 * @param arguments The arguments for the agent as name/value pairs.
	 * @param parent The parent component (if any).
	 * @return An instance of a component.
	 */
	public Object[] createComponentInstance(IComponentDescription desc, IComponentAdapterFactory factory, IModelInfo model, 
		String config, Map arguments, IExternalAccess parent, Future ret)
	{
		MicroAgentInterpreter mai = new MicroAgentInterpreter(desc, factory, model, getMicroAgentClass(model.getFullName()+"Agent", 
			null, model.getClassLoader()), arguments, config, parent, ret);
		return new Object[]{mai, mai.getAgentAdapter()};
	}
	
	/**
	 *  Get the element type.
	 *  @return The element type (e.g. an agent, application or process).
	 * /
	public String getElementType()
	{
		return IComponentFactory.ELEMENT_TYPE_AGENT;
	}*/
	
	/**
	 *  Get the properties.
	 *  Arbitrary properties that can e.g. be used to
	 *  define kernel-specific settings to configure tools.
	 *  @param type	The component type. 
	 *  @return The properties or null, if the component type is not supported by this factory.
	 */
	public Map	getProperties(String type)
	{
		return FILETYPE_MICROAGENT.equals(type)
		? properties: null;
	}
	
	/**
	 *  Start the service.
	 * /
	public synchronized IFuture	startService()
	{
		return new Future(null);
	}*/
	
	/**
	 *  Shutdown the service.
	 *  @param listener The listener.
	 * /
	public synchronized IFuture	shutdownService()
	{
		return new Future(null);
	}*/
	
	/**
	 *  Get the mirco agent class.
	 */
	// todo: make use of cache
	protected Class getMicroAgentClass(String clname, String[] imports, ClassLoader classloader)
	{
		Class ret = SReflect.findClass0(clname, imports, classloader);
//		System.out.println(clname+" "+cma+" "+ret);
		int idx;
		while(ret==null && (idx=clname.indexOf('.'))!=-1)
		{
			clname	= clname.substring(idx+1);
			ret = SReflect.findClass0(clname, imports, classloader);
//			System.out.println(clname+" "+cma+" "+ret);
		}
		if(ret==null)// || !cma.isAssignableFrom(IMicroAgent.class))
			throw new RuntimeException("No micro agent file: "+clname);
		return ret;
	}
	
	/**
	 *  Add excluded methods.
	 */
	public static void addExcludedMethods(Map props, String[] excludes)
	{
		Object ex = props.get("remote_excluded");
		if(ex!=null)
		{
			List newex = new ArrayList();
			for(Iterator it=SReflect.getIterator(ex); it.hasNext(); )
			{
				newex.add(it.next());
			}
			for(int i=0; i<excludes.length; i++)
			{
				newex.add(excludes[i]);
			}
		}
		else
		{
			props.put("remote_excluded", excludes);
		}
	}
}
