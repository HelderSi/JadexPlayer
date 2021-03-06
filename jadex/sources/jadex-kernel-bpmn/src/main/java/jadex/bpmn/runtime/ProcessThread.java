package jadex.bpmn.runtime;

import jadex.bpmn.model.MActivity;
import jadex.bpmn.model.MBpmnModel;
import jadex.bpmn.model.MLane;
import jadex.bpmn.model.MParameter;
import jadex.bpmn.model.MSequenceEdge;
import jadex.bpmn.model.MSubProcess;
import jadex.commons.IFilter;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.javaparser.IParsedExpression;
import jadex.javaparser.IValueFetcher;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *  Representation of a single control flow in a BPMN process instance,
 *  i.e. an instance of a sequence flow.
 */
public class ProcessThread	implements ITaskContext
{
	//-------- constants --------
	
//	/** Waiting constant for time. */
//	public static String WAITING_FOR_TIME = "waiting_for_time";
//	
//	/** Waiting constant for message. */
//	public static String WAITING_FOR_MESSAGE = "waiting_for_message";
//	
//	/** Waiting constant for condition. */
//	public static String WAITING_FOR_CONDITION = "waiting_for_condition";
//	
//	/** Waiting constant for join. */
//	public static String WAITING_FOR_JOIN = "waiting_for_join";
//
//	/** Waiting constant for task. */
//	public static String WAITING_FOR_TASK = "waiting_for_task";
//
//	/** Waiting constant for subprocess. */
//	public static String WAITING_FOR_SUBPROCESS = "waiting_for_subprocess";
//	
//	/** Waiting constant for multi. */
//	public static String WAITING_FOR_MULTI = "waiting_for_multi";
	
	//-------- attributes --------
	
	/** The thread id. */
	protected String id;
	
	/** The next activity. */
	protected MActivity	activity;
	
	/** The last edge (if any). */
	protected MSequenceEdge	edge;
		
	/** The data of the current or last activity. */
	protected Map data;
	
	/** The thread context. */
	protected ThreadContext	context;
	
	/** The Bpmn instance. */
	protected BpmnInterpreter instance;
	
	/** The exception that has just occurred in the process (if any). */
	protected Exception	exception;
	
	/** Is the process in a waiting state. */
//	protected String	waiting;
	protected boolean waiting;
	
	/** The wait info. */
	protected Object waitinfo;
	
	/** The wait filter. */
	protected IFilter waitfilter;
	
	/** The static id counter. */
	protected static int idcnt;
	
	//-------- constructors --------

	/**
	 *  Create a new process instance.
	 *  @param activity	The current activity.
	 */
	public ProcessThread(MActivity activity, ThreadContext context, BpmnInterpreter instance)
	{
		synchronized(ProcessThread.class)
		{
			this.id = "ProcessThread_"+idcnt++;
		}
		this.activity	= activity;
		this.context	= context;
		this.instance = instance;
	}
	
	//-------- methods --------
	
	/**
	 *  Get the id.
	 *  @return The id.
	 */
	public String getId()
	{
		return this.id;
	}	
	
	/**
	 *  Get the activity.
	 *  @return The activity.
	 */
	public MActivity getActivity()
	{
		return this.activity;
	}

	/**
	 *  Set the next activity. Sets the last edge to null.
	 *  Should only be used, when no edge available (e.g. start events or event handlers of subprocesses).
	 *  @return The activity.
	 */
	public void	setActivity(MActivity activity)
	{
		this.edge	= null;
		this.activity	= activity;
	}
	
	/**
	 *  Get the last edge (if any).
	 *  @return The edge.
	 */
	public MSequenceEdge getLastEdge()
	{
		return this.edge;
	}

	/**
	 *  Set the last edge. Also sets the next activity.
	 *  @param edge	The edge.
	 */
	public void setLastEdge(MSequenceEdge edge)
	{
		setActivity(edge!=null ? (MActivity)edge.getTarget() : null);
		this.edge	= edge;
	}
	
	/**
	 *  Is the process in a waiting state (i.e. blocked)? 
	 *  @return The waiting flag.
	 */
	public boolean	isWaiting()
	{
//		return this.waiting!=null;
		return waiting;
	}
	
	/**
	 *  Set the waiting state.
	 */
	public void setWaiting(boolean waiting)
	{
//		System.out.println("Set waiting thread: "+getId()+" "+waiting);
		this.waiting = waiting;
	}
	
	/**
	 *  Set to non waiting.
	 *  @return The waiting flag.
	 */
	public void	setNonWaiting()
	{
		this.waiting = false;
		this.waitinfo = null;
		this.waitfilter = null;
//		System.out.println("Thread: "+getId()+" "+waiting);
	}
	
	/**
	 *  Get the waiting type. 
	 *  @return The waiting type.
	 * /
	public String getWaitingState()
	{
		return this.waiting;
	}*/

	/**
	 *  Set the process waiting state (i.e. blocked). 
	 *  @param waiting	The waiting flag.
	 * /
	public void setWaitingState(String waiting)
	{
		this.waiting = waiting;
//		System.out.println("Thread: "+this+" "+waiting);
	}*/
	
	// todo: refactor/simplify the waiting stuff.
	
	/**
	 *  Set the process waiting info. 
	 *  @param waiting	The waiting info.
	 */
	public void setWaitInfo(Object waitinfo)
	{
		this.waitinfo = waitinfo;
	}
	
	/**
	 *  Get the waitinfo.
	 *  @return The waitinfo.
	 */
	public Object getWaitInfo()
	{
		return this.waitinfo;
	}
	
	/**
	 *  Get the wait filter.
	 *  @return The waitfilter.
	 */
	public IFilter getWaitFilter()
	{
		return this.waitfilter;
	}

	/**
	 *  Set the wait filter.
	 *  @param waitfilter The waitfilter to set.
	 */
	public void setWaitFilter(IFilter waitfilter)
	{
		this.waitfilter = waitfilter;
	}
	
	/**
	 *  Get the thread context
	 *  @return	The thread context.
	 */
	public ThreadContext getThreadContext()
	{
		return context;
	}

	/**
	 *  Create a copy of this thread (e.g. for a parallel split).
	 */
	public ProcessThread createCopy()
	{
		ProcessThread	ret	= new ProcessThread(activity, context, instance);
		ret.edge	= edge;
		ret.data	= data!=null? new HashMap(data): null;
		return ret;
	}
	
	/**
	 *  Test if a parameter has been set on activity.
	 *  @param name	The parameter name. 
	 *  @return	True if parameter is known.
	 */
	public boolean hasOwnParameterValue(String name)
	{
		return data!=null && data.containsKey(name);
	}
	
	//-------- ITaskContext --------
	
	/**
	 *  Test if a parameter has been set on activity.
	 *  @param name	The parameter name. 
	 *  @return	True if parameter is known.
	 */
	public boolean hasParameterValue(String name)
	{
		return hasOwnParameterValue(name) || getThreadContext().getInitiator()!=null 
			&& getThreadContext().getInitiator().hasParameterValue(name);
	}

	/**
	 *  Get the model element.
	 *  @return	The model of the task.
	 */
	public MActivity getModelElement()
	{
		return activity;
	}

	/**
	 *  Get the value of a parameter.
	 *  @param name	The parameter name. 
	 *  @return	The parameter value. 
	 */
	public Object getParameterValue(String name)
	{
		return hasOwnParameterValue(name)? data.get(name): 
			getThreadContext().getInitiator()!=null? 
			getThreadContext().getInitiator().getParameterValue(name): null;
	}

	/**
	 *  Set the value of a parameter.
	 *  @param name	The parameter name. 
	 *  @param value	The parameter value. 
	 */
	public void	setParameterValue(String name, Object value)
	{
		setParameterValue(name, null, value);
	}
	
	/**
	 *  Set the value of a parameter.
	 *  @param name	The parameter name. 
	 *  @param value	The parameter value. 
	 */
	public void	setParameterValue(String name, Object key, Object value)
	{
		if(!getActivity().hasParameter(name))
		{
			ProcessThread pt = getThreadContext().getInitiator();
			if(pt!=null)
			{
				pt.setParameterValue(name, key, value);
			}
		}
			
		if(data==null)
			data = new HashMap();
		
		if(key==null)
		{
			data.put(name, value);	
		}
		else
		{
			Object coll = data.get(name);
			if(coll instanceof List)
			{
				int index = key==null? -1: ((Number)key).intValue();
				if(index>=0)
					((List)coll).set(index, value);
				else
					((List)coll).add(value);
			}
			else if(coll!=null && coll.getClass().isArray())
			{
				int index = ((Number)key).intValue();
				Array.set(coll, index, value);
			}
			else if(coll instanceof Map)
			{
				((Map)coll).put(key, value);
			}
//			else
//			{
//				throw new RuntimeException("Unsupported collection type: "+coll);
//			}
		}
	}

	/**
	 *  Remove the value of a parameter.
	 *  @param name	The parameter name. 
	 */
	public void	removeParameterValue(String name)
	{
		assert activity!=null;
		if(data!=null)
		{
			data.remove(name);
		}
	}
	
	/**
	 *  Get the name of all parameters.
	 *  @return The parameter names.
	 */
	public String[] getParameterNames()
	{
		return data!=null? (String[])data.keySet().toArray(new String[data.size()]): SUtil.EMPTY_STRING_ARRAY;
	}
	
	/**
	 *  Get the value of a property.
	 *  @param name	The property name. 
	 *  @return	The property value. 
	 */
	public Object getPropertyValue(String name)
	{
		return getPropertyValue(name, activity);
	}
	
	/**
	 *  Hack: method is necessary because thread.activity is not always 
	 *  the activity to execute in case of multiple event.
	 *  Get the value of a property.
	 *  @param name	The property name. 
	 *  @return	The property value. 
	 */
	public Object getPropertyValue(String name, MActivity activity)
	{
		assert activity!=null;
		Object ret	= activity.getPropertyValue(name);
		if(ret instanceof IParsedExpression)
		{
			try
			{
//				System.out.println("here: "+ret);
				ret = ((IParsedExpression)ret).getValue(new ProcessThreadValueFetcher(this, true, instance.getValueFetcher()));
			}
			catch(RuntimeException e)
			{
				throw new RuntimeException("Error parsing property: "+instance+", "+this+", "+name+", "+ret, e);
			}
		}
		return ret;
	}
	
	/**
	 *  Test, if a property is declared.
	 *  @param name	The property name.
	 *  @return True, if the property is declared.
	 */
	public boolean	hasPropertyValue(String name)
	{
		return activity.hasPropertyValue(name);
	}
	
//	/**
//	 *  Check if the value of a result is set.
//	 *  @param name	The result name. 
//	 *  @return	True, if the result is set to some value. 
//	 */
//	public boolean hasResultValue(String name)
//	{
//		return instance.hasResultValue(name);
//	}
//
//	/**
//	 *  Get the value of a result.
//	 *  @param name	The result name. 
//	 *  @return	The result value. 
//	 */
//	public Object getResultValue(String name)
//	{
//		return instance.getResultValue(name);
//	}
//	
//	/**
//	 *  Set the value of a result.
//	 *  @param name	The result name. 
//	 *  @param value The result value. 
//	 */
//	public void	setResultValue(String name, Object value)
//	{
//		instance.setResultValue(name, value);
//	}
	
	/**
	 *  Get the exception (if any).
	 *  @return	The exception (if any).
	 */
	public Exception	getException()
	{
		return exception;
	}

	/**
	 *  Set the exception.
	 *  @param exception	The exception.
	 */
	public void	setException(Exception exception)
	{
		this.exception	= exception;
	}
	
	/**
	 *  Get the instance.
	 *  @return The instance.
	 */
	public BpmnInterpreter getInstance()
	{
		return this.instance;
	}
	
	/**
	 *  Get the data.
	 *  @return The data.
	 */
	public Map getData()
	{
		return this.data;
	}

	/**
	 *  Test if the thread belongs to the given pool and/or lane.
	 *  @param pool	The pool to be executed or null for any.
	 *  @param lane	The lane to be executed or null for any. Nested lanes may be addressed by dot-notation, e.g. 'OuterLane.InnerLane'.
	 *  @return True, when the thread belongs to the given pool and/or lane. Also returns true when both pool and lane are null.
	 */
	public boolean belongsTo(String pool, String lane)
	{
		boolean	ret	= pool==null  || pool.equals(getActivity().getPool().getName());
		
		// Test lane
		if(ret && lane!=null)
		{
			List	lanes	= new ArrayList();
			MLane	mlane	= getActivity().getLane();
			while(mlane!=null)
			{
				lanes.add(mlane);
				mlane	= mlane.getLane();
			}
			
			StringTokenizer	stok	= new StringTokenizer(lane, ".");
			while(ret && stok.hasMoreTokens())
			{
				ret	= !lanes.isEmpty() && ((MLane)lanes.remove(lanes.size()-1)).getName().equals(stok.nextToken());
			}
		}
		
		return ret;
	}

	/**
	 *  Update parameters based on edge inscriptions and initial values.
	 *  @param instance	The calling BPMN instance.
	 */
	protected  void updateParametersBeforeStep(BpmnInterpreter instance)
	{
//		System.out.println("before: "+getActivity());

		if(MBpmnModel.TASK.equals(getActivity().getActivityType())
			|| getActivity() instanceof MSubProcess)
		{
			// Handle parameter passing in edge inscriptions.
			Map	passedparams = null;
			Set	indexparams	= null;
			if(getLastEdge()!=null && getLastEdge().getParameterMappings()!=null)
			{
				Map mappings = getLastEdge().getParameterMappings();
				if(mappings!=null)
				{
					IValueFetcher fetcher = new ProcessThreadValueFetcher(this, false, instance.getValueFetcher());
					for(Iterator it=mappings.keySet().iterator(); it.hasNext(); )
					{
						boolean	found	= false;
						String	name	= (String)it.next();
						IParsedExpression exp = (IParsedExpression)((Object[])mappings.get(name))[0];
						IParsedExpression iexp = (IParsedExpression)((Object[])mappings.get(name))[1];
						Object value;
						Object index;
						try
						{
							value	= exp.getValue(fetcher);
						}
						catch(RuntimeException e)
						{
							throw new RuntimeException("Error parsing parameter value: "+instance+", "+this+", "+name+", "+exp, e);
						}
						try
						{
							index	= iexp!=null ? iexp.getValue(fetcher) : null;
						}
						catch(RuntimeException e)
						{
							throw new RuntimeException("Error parsing parameter index: "+instance+", "+this+", "+name+", "+iexp, e);
						}
												
						if(getActivity().hasParameter(name))
						{
							if(passedparams==null)
								passedparams	= new HashMap();
							
							if(iexp!=null)
							{
								if(!passedparams.containsKey(name))
								{
									passedparams.put(name, new ArrayList());
									if(indexparams==null)
										indexparams	= new HashSet();
									
									indexparams.add(name);
								}
								((List)passedparams.get(name)).add(new Object[]{index, value});
							}
							else
							{
								passedparams.put(name, value);
							}
							found	= true;
						}
						
						if(!found && hasParameterValue(name))
						{
							setParameterValue(name, index, value);
							found	= true;
						}
						
						if(!found && instance.hasContextVariable(name))
						{
							if(iexp!=null)
							{
								Object	array	= instance.getContextVariable(name);
								Array.set(array, ((Number)index).intValue(), value);
							}
							else
							{
								instance.setContextVariable(name, value);
							}
							found	= true;
						}
						else if(!found)
						{
							throw new RuntimeException("Unknown parameter or context variable: "+name+", "+this);
						}
					}
				}
			}
						
			// todo: parameter direction / class
			
			Set before = data!=null? new HashSet(data.keySet()): Collections.EMPTY_SET;
			IValueFetcher fetcher = new ProcessThreadValueFetcher(this, true, instance.getValueFetcher());
			Map params = getActivity().getParameters();
			if(params!=null)
			{
				for(Iterator it=params.values().iterator(); it.hasNext(); )
				{
					MParameter param = (MParameter)it.next();
					if(passedparams!=null && passedparams.containsKey(param.getName()))
					{
						if(indexparams!=null && indexparams.contains(param.getName()))
						{
							Object	array	= getParameterValue(param.getName());
							List	values	= (List)passedparams.get(param.getName());
							for(int i=0; i<values.size(); i++)
							{
								Object[]	entry	= (Object[])values.get(i);
								Array.set(array, ((Number)entry[0]).intValue(), entry[1]);
							}
						}
						else
						{
							setParameterValue(param.getName(), passedparams.get(param.getName()));
							before.remove(param.getName());
						}
					}
					else
					{
						try
						{
							setParameterValue(param.getName(), param.getInitialValue()==null? null: param.getInitialValue().getValue(fetcher));
							before.remove(param.getName());
						}
						catch(RuntimeException e)
						{
							throw new RuntimeException("Error parsing parameter value: "+instance+", "+this+", "+param.getName()+", "+param.getInitialValue(), e);
						}
					}
				}
			}
			
			// Remove old data (all values that have not been renewed).
			for(Iterator it=before.iterator(); it.hasNext(); )
			{
				data.remove(it.next());
			}
		}
	}
	
	/**
	 *  Remove in parameters after step.
	 *  @param instance	The calling BPMN instance.
	 */
	public  void updateParametersAfterStep(MActivity activity, BpmnInterpreter instance)
	{
		// Remove all in paramters
//		System.out.println("after: "+act);
		if(MBpmnModel.TASK.equals(activity.getActivityType()) || activity instanceof MSubProcess)
		{
			List params = activity.getParameters(new String[]{MParameter.DIRECTION_IN});
			for(int i=0; i<params.size(); i++)
			{
				MParameter inp = (MParameter)params.get(i);
				removeParameterValue(inp.getName());
//				System.out.println("Removed thread param value: "+inp.getName());
			}
		}
	}
	
	/**
	 *  Create a string representation of this process thread.
	 *  @return A string representation of this process thread.
	 */
	public String	toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(id=");
		buf.append(id);
		buf.append("(activity=");
		buf.append(activity);
		buf.append(", data=");
		buf.append(data);
		buf.append(", waiting=");
		buf.append(waiting);
		buf.append(")");
		return buf.toString();
	}
}
