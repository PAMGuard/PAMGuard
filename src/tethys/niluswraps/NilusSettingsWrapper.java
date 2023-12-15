package tethys.niluswraps;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import nilus.MarshalXML;

/**
 * Class to wrap up a nilus object so that it can be saved using Java serialization. 
 * This is basically saving the xml as a String, since all nilus objects are NOT
 * serializable, but should make it (relatively) straight forward to save nilus
 * objects into PAMGuard settings files. 
 * @author dg50
 *
 */
public class NilusSettingsWrapper<T extends Object> implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	private String xmlString;
	
	private transient T nilusObject;
	
	/**
	 * Create a nilus object. Have to pass the class type in as 
	 * an argument, since Class isn't serializable, so can't be stored
	 * with the object - which contains nothing but a String, which can be safely serialised.
	 * @param nilusClass
	 * @return nilus object. 
	 */
	public T getNilusObject(Class nilusClass) {
		if (nilusObject == null) {
			nilusObject = unpackNilusObject(nilusClass);
		}
		return nilusObject;
	}
	
	private T unpackNilusObject(Class nilusClass) {
		Document doc = getDocument();
		if (doc == null) {
			return null;
		}
		/**
		 * Try to turn the string into a document. 
		 */
		NilusUnpacker unpacker = new NilusUnpacker();
		T unpacked = (T) unpacker.unpackDocument(doc, nilusClass);
		
		return unpacked;
	}
	
	/**
	 * Set the nilus object. This marshals the nilus object 
	 * into xml and saves the data as an intetnal xml string which
	 * can be safely serialized. 
	 * @param nilusObject nilus object.
	 * @return true if it was marshalled OK. 
	 */
	public boolean setNilusObject(T nilusObject) {
		// use the marshaller to create a Tethys type document, then 
		// get it as a string. 
		this.nilusObject = nilusObject;
		// and convert immediately to the XML string. 
		return packNilusObject(nilusObject);
	}
	/**
	 * Set the nilus object. This marshals the nilus object 
	 * into xml and saves the data as an intetnal xml string which
	 * can be safely serialized. 
	 * @param nilusObject nilus object.
	 * @return true if it was marshalled OK. 
	 */
	private boolean packNilusObject(T nilusObject) {
		// use the marshaller to create a Tethys type document, then 
		// get it as a string. 
		// and convert immediately to XML string. 
		if (nilusObject == null) {
			xmlString = null;
			return true;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter printWriter = new PrintWriter(bos);
		MarshalXML marshaler;
		try {
			marshaler = new MarshalXML();
			marshaler.marshal(nilusObject, bos);
		} catch (JAXBException e) {
			e.printStackTrace();
			return false;
		}
		xmlString = bos.toString();
		return true;
	}
	
	/**
	 * Repack the object.i.e. write the xml text string.
	 * May want to do this before serializing or cloning. 
	 * @return
	 */
	public boolean repackNilusObject() {
		return packNilusObject(nilusObject);
	}
	
	
	/**
	 * Get a document from the internal xml String representation. 
	 * @return xml document
	 */
	public Document getDocument() {
		if(xmlString == null) {
			return null;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//API to obtain DOM Document instance
		DocumentBuilder builder = null;
		try {
			//Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();
			//Parse the content to Document object
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the xmlString
	 */
	public String getXmlString() {
		return xmlString;
	}

	/**
	 * Repack the nilus object. Call this just before settings are
	 * saved to ensure everything is up to date since this probably won't
	 * have happened if changes were made within existing nilus objects 
	 * and setNilusObject was never called. 
	 */
	public void reSerialise() {
		packNilusObject(nilusObject);
	}

	@Override
	public NilusSettingsWrapper<T> clone() {
		/**
		 * Clone the underlying data, then force it to re-read the string into a new object. 
		 */
		this.repackNilusObject();
		NilusSettingsWrapper<T> clone = null;
		try {
			clone = (NilusSettingsWrapper<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		if (nilusObject != null) {
			clone.nilusObject = clone.unpackNilusObject(nilusObject.getClass());
		}
		return clone;
	}

}
