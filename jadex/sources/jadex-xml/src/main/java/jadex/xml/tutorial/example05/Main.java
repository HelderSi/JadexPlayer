package jadex.xml.tutorial.example05;

import jadex.commons.SUtil;
import jadex.xml.AccessInfo;
import jadex.xml.MappingInfo;
import jadex.xml.ObjectInfo;
import jadex.xml.SubobjectInfo;
import jadex.xml.TypeInfo;
import jadex.xml.XMLInfo;
import jadex.xml.bean.BeanObjectReaderHandler;
import jadex.xml.bean.BeanObjectWriterHandler;
import jadex.xml.reader.Reader;
import jadex.xml.tutorial.example04.Invoice;
import jadex.xml.writer.Writer;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 *  Main class to execute tutorial lesson.
 */
public class Main
{
	/**
	 *  Main method for using the xml reader/writer.
	 */
	public static void main(String[] args) throws Exception
	{
		// Reading a simple xml in a Java class with public bean getter/setters.
		// Using a subobject info for mapping an xml tag to a differently named Java property.
		// In this example the invoice xml was changed to contain some container tags
		// which are automatically skipped for reading.
		
		// Create minimal type infos for types that need to be mapped
		Set typeinfos = new HashSet();
		typeinfos.add(new TypeInfo(new XMLInfo("invoice"), new ObjectInfo(Invoice.class),
			new MappingInfo(null, new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("product-key", "key"))		
			})));
		
		// Create an xml reader with standard bean object reader and the
		// custom typeinfos
		Reader xmlreader = new Reader(new BeanObjectReaderHandler(typeinfos));
		InputStream is = SUtil.getResource("jadex/xml/tutorial/example05/data.xml", null);
		Object object = xmlreader.read(is, null, null);
		is.close();
		
		typeinfos = new HashSet();
		typeinfos.add(new TypeInfo(new XMLInfo("invoice/someuglycontainertag"), new ObjectInfo(Invoice.class),
			new MappingInfo(null, new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("product-key", "key")),
			new SubobjectInfo(new XMLInfo("someuglyothercontainertag/quantity"), new AccessInfo("quantity"))
			})));
		
		// Write the xml to the output file.
		Writer xmlwriter = new Writer(new BeanObjectWriterHandler(typeinfos, false, true), false);
		String xml = Writer.objectToXML(xmlwriter, object, null);
//		OutputStream os = new FileOutputStream("out.xml");
//		xmlwriter.write(object, os, null, null);
//		os.close();
		
		// And print out the result.
		System.out.println("Read object: "+object);
		System.out.println("Wrote xml: "+xml);
	}
}
