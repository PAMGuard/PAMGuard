package PamUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

//import com.sun.org.apache.xml.internal.serialize.Method;
//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
//import com.sun.org.apache.xml.internal.serialize.OutputFormat.Defaults;

public class XMLUtils {


	/**
	 * Convert an xml document to a String.
	 * @param doc xml document
	 * @returnJava String
	 */
	public static synchronized String getStringFromDocument(Document doc)
	{
	    try
	    {
	    	// this Transofrmer stuff doesn't work any more. The OutputFormat
	    	// below seems OK though. 
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
//	    	TransformerFactory tf = TransformerFactory.newInstance();
//	    	Transformer transformer = tf.newTransformer();
//	    	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//	    	StringWriter writer = new StringWriter();
//	    	transformer.transform(new DOMSource(doc), new StreamResult(writer));
//	    	String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
//	    	return output;
	    	

//	        OutputFormat format    = new OutputFormat (doc);
//	        OutputFormat format = new OutputFormat();
//	        format.setMethod(Method.XML);
//	        // as a String
//	        StringWriter stringOut = new StringWriter ();    
//	        XMLSerializer serial   = new XMLSerializer (stringOut, format);
//	        serial.serialize(doc);
//	        return stringOut.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    } 
//	    catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    return null;
	} 
	
	/**
	 * Convert a string back into an XML document.  
	 * @param xmlString String
	 * @return XML Document, or null if there was an error
	 */
	public static synchronized Document getDocumentFromString(String xmlString) {
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xmlString.getBytes("utf-8"));
			Document xmlDocument = db.parse(is);
			return xmlDocument;
		} catch (Exception e) {
			System.err.println("Error parsing xml string " + xmlString);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get an integer attribute from an Element
	 * @param el XML Element
	 * @param name Attribute name
	 * @return null if not a number
	 */
	public static synchronized Integer getIntegerValue(Element el, String name) {
		String str = el.getAttribute(name);
		if (str == null) {
			return null;
		}
		try {
			return Integer.decode(str);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	/**
	 * Get a Double attribute from an Element
	 * @param el XML Element
	 * @param name Attribute name
	 * @return null if not a number
	 */
	public static synchronized Double getDoubleValue(Element el, String name) {
		String str = el.getAttribute(name);
		if (str == null) {
			return null;
		}
		try {
			return Double.valueOf(str);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Creates a blank Document object, or returns null if there was an error
	 * 
	 * @return
	 */
	public static Document createBlankDoc() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create an xml document from an xml file. 
	 * @param xmlFileName xml file name
	 * @return document
	 */
	public static Document createDocument(String xmlFileName) {
		if (xmlFileName == null) {
			return null;
		}
		return createDocument(new File(xmlFileName));
	}
	
	/**
	 * Create an xml document from an xml file. 
	 * @param xmlFile xml file
	 * @return document
	 */
	public static Document createDocument(File xmlFile) {
		if (xmlFile == null || xmlFile.exists() == false) {
			return null;
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			doc = dbFactory.newDocumentBuilder().parse(xmlFile);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return doc;
	}
}
