package jadex.xml.writer;

import jadex.xml.IContext;
import jadex.xml.TypeInfo;

import javax.xml.namespace.QName;


/**
 *  Interface for an object writer handler.
 *  Has the task to generate write information for an object.
 */
public interface IObjectWriterHandler
{
	/**
	 *  Get the most specific mapping info.
	 *  @param tag The tag.
	 *  @param fullpath The full path.
	 *  @return The most specific mapping info.
	 */
	public TypeInfo getTypeInfo(Object object, QName[] fullpath, IContext context);
	
	/**
	 *  Get the tag name for an object.
	 */
	public QName getTagName(Object object, IContext context);
		
	/**
	 *  Get the tag with namespace.
	 */
	public QName getTagWithPrefix(QName tag, IContext context);
	
	/**
	 *  Get all subobjects of an object.
	 *  @param object The object.
	 *  @param typeinfo The Typeinfo.
	 */
	public WriteObjectInfo getObjectWriteInfo(Object object, TypeInfo typeinfo, IContext context) throws Exception;
}
