package jadex.gpmn;

import jadex.commons.IFilter;
import jadex.commons.ResourceInfo;
import jadex.commons.SUtil;
import jadex.gpmn.model2.MActivationEdge;
import jadex.gpmn.model2.MActivationPlan;
import jadex.gpmn.model2.MBpmnPlan;
import jadex.gpmn.model2.MContext;
import jadex.gpmn.model2.MContextElement;
import jadex.gpmn.model2.MGoal;
import jadex.gpmn.model2.MGpmnModel;
import jadex.gpmn.model2.MPlanEdge;
import jadex.gpmn.model2.MSubprocess;
import jadex.xml.AccessInfo;
import jadex.xml.AttributeInfo;
import jadex.xml.MappingInfo;
import jadex.xml.ObjectInfo;
import jadex.xml.SubobjectInfo;
import jadex.xml.TypeInfo;
import jadex.xml.XMLInfo;
import jadex.xml.bean.BeanObjectReaderHandler;
import jadex.xml.reader.Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 *  Reader for loading Gpmn XML models into a Java representation states.
 */
public class GpmnXMLReader2
{
	/**
	 *  Main for testing.
	 */
	public static void main(String[] args) throws Exception
	{
		File file = new File("/home/jander/test.gpmn");
		InputStream is = new FileInputStream(file);
		ResourceInfo rinfo = new ResourceInfo(file.getAbsolutePath(), is, 0);
		MGpmnModel model = read(rinfo, Thread.currentThread().getContextClassLoader());
		System.out.println(SUtil.arrayToString(model.getActivationEdges().toArray()));
		System.out.println(((MActivationPlan)model.getActivationPlans().values().toArray()[0]).getMode());
	}
	
	//-------- attributes --------
	
	/** The singleton reader instance. */
	protected static Reader	reader;
	
	//-------- methods --------
	
	// Initialize reader instance.
	static
	{
		reader = new Reader(new BeanObjectReaderHandler(getXMLMapping()));
	}
	
	/**
	 *  Read properties from xml.
	 *  @param info	The resource info.
	 *  @param classloader The classloader.
 	 */
	protected static MGpmnModel read(ResourceInfo rinfo, ClassLoader classloader) throws Exception
	{
		MGpmnModel ret = (MGpmnModel)reader.read(rinfo.getInputStream(), classloader, null);
		ret.setFilename(rinfo.getFilename());
		ret.setLastModified(rinfo.getLastModified());
		ret.setClassloader(classloader);
		String name = new File(rinfo.getFilename()).getName();
		name = name.substring(0, name.length()-5);
		ret.setName(name);
		ret.initModelInfo();
		
		rinfo.getInputStream().close();
		return ret;
	}
	
	/**
	 *  Read properties from xml.
	 *  @param input The input stream.
	 *  @param classloader The classloader.
	 * 	@param context The context.
 	 */
	public static MGpmnModel read(String filename, final ClassLoader classloader, final Object context) throws Exception
	{
		ResourceInfo rinfo = SUtil.getResourceInfo0(filename, classloader);
		if(rinfo==null)
			throw new RuntimeException("Could not find resource: "+filename);
		MGpmnModel ret = (MGpmnModel)reader.read(rinfo.getInputStream(), classloader, context);
		
		ret.setLastModified(rinfo.getLastModified());
		ret.setFilename(rinfo.getFilename());
		ret.setClassloader(classloader);
		
		String name = new File(rinfo.getFilename()).getName();
		name = name.substring(0, name.length()-5);
		ret.setName(name);
		ret.initModelInfo();
		
		rinfo.getInputStream().close();
		
		return ret;
	}
	
	
	
	/**
	 *  Get the XML mapping.
	 */
	public static Set getXMLMapping()
	{
		Set types = new HashSet();
		
		String uri = "http://jadex.sourceforge.net/gpmn";
		
		IFilter activationplanfilter = new IFilter()
		{
			public boolean filter(Object obj)
			{
				boolean ret = false;
				if (obj != null)
					ret = "gpmn:ActivationPlan".equals(((Map) obj).get("type"));
				return ret;
			}
		};
		
		IFilter bpmnplanfilter = new IFilter()
		{
			public boolean filter(Object obj)
			{
				boolean ret = false;
				if (obj != null)
					ret = "gpmn:BpmnPlan".equals(((Map) obj).get("type"));
				return ret;
			}
		};
		
		String schemaloc = "http://www.w3.org/2001/XMLSchema-instance";
		
		types.add(new TypeInfo(new XMLInfo(new QName[]{new QName(uri, "GpmnDiagram")}), new ObjectInfo(MGpmnModel.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo(new QName(schemaloc, "schemaLocation"), null, AccessInfo.IGNORE_READWRITE)),
			new AttributeInfo(new AccessInfo("version", null, AccessInfo.IGNORE_READWRITE)),
			},
			new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("goal")),
			new SubobjectInfo(new XMLInfo("plan", activationplanfilter), new AccessInfo("activationPlan")),
			new SubobjectInfo(new XMLInfo("plan", bpmnplanfilter), new AccessInfo("bpmnPlan")),
			new SubobjectInfo(new AccessInfo("subProcess", "subprocess")),
			new SubobjectInfo(new AccessInfo("activationEdge")),
			new SubobjectInfo(new AccessInfo("planEdge")),
			new SubobjectInfo(new AccessInfo("artifacts", "artifact")),
		})));
		
		types.add(new TypeInfo(new XMLInfo("context"), 
				new ObjectInfo(MContext.class),
				new MappingInfo(null, new AttributeInfo[]{
				new AttributeInfo(new AccessInfo("id", null, AccessInfo.IGNORE_READWRITE))},
				new SubobjectInfo[]{
				new SubobjectInfo(new AccessInfo("element", "contextElement")),
				})));
		
		types.add(new TypeInfo(new XMLInfo("element"), new ObjectInfo(MContextElement.class),
				new MappingInfo(null, new AttributeInfo[]{
				new AttributeInfo(new AccessInfo("id", null, AccessInfo.IGNORE_READWRITE))},
				new SubobjectInfo[]{
				new SubobjectInfo(new AccessInfo("value"))
				})));
		
		types.add(new TypeInfo(new XMLInfo("goal"), 
			new ObjectInfo(MGoal.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("id", "Id")),
			new AttributeInfo(new AccessInfo("exclude", "excludeMode"))},
			new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("creationcondition", "creationCondition")),
			new SubobjectInfo(new AccessInfo("contextcondition", "contextCondition")),
			new SubobjectInfo(new AccessInfo("dropcondition", "dropCondition")),
			new SubobjectInfo(new AccessInfo("targetcondition", "targetCondition")),
			new SubobjectInfo(new AccessInfo("maintaincondition", "maintainCondition")),
			})));
		
		types.add(new TypeInfo(new XMLInfo("plan", activationplanfilter),
			new ObjectInfo(MActivationPlan.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("id", "Id")),
			new AttributeInfo(new AccessInfo(new QName(schemaloc, "type"), null, AccessInfo.IGNORE_READWRITE))},
			new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("precondition", "preCondition")),
			new SubobjectInfo(new AccessInfo("contextcondition", "contextCondition")),
			})));
		
		types.add(new TypeInfo(new XMLInfo("plan", bpmnplanfilter),
			new ObjectInfo(MBpmnPlan.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("id", "Id")),
			new AttributeInfo(new AccessInfo(new QName(schemaloc, "type"), null, AccessInfo.IGNORE_READWRITE))},
			new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("precondition", "preCondition")),
			new SubobjectInfo(new AccessInfo("contextcondition", "contextCondition")),
			})));
		
		types.add(new TypeInfo(new XMLInfo("subProcess"),
			new ObjectInfo(MSubprocess.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("id", "Id")),
			new AttributeInfo(new AccessInfo("processref", "processReference"))}
			)));
		
		types.add(new TypeInfo(new XMLInfo("activationEdge"), new ObjectInfo(MActivationEdge.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("id", "Id")),
			new AttributeInfo(new AccessInfo("source", "sourceId")),
			new AttributeInfo(new AccessInfo("target", "targetId"))}, null)));
		
		types.add(new TypeInfo(new XMLInfo("planEdge"), new ObjectInfo(MPlanEdge.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("id", "Id")),
			new AttributeInfo(new AccessInfo("source", "sourceId")),
			new AttributeInfo(new AccessInfo("target", "targetId"))}, null)));
		
		return types;
	}
}
