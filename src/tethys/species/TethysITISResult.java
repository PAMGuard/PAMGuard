package tethys.species;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Class to hold and unpack a XML string returned from the ITIS_ranks document
 * in a Tethys database. 
 * @author dg50
 *
 */
public class TethysITISResult {

	/**
	 * Construct a ITIS object from XML data
	 * @param xmlData
	 */
	public TethysITISResult(String xmlData) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		//API to obtain DOM Document instance
		DocumentBuilder builder = null;

//		//Create DocumentBuilder with default configuration
//		builder = factory.newDocumentBuilder();
//
//		//Parse the content to Document object
//		Document doc = builder.parse(new InputSource(new StringReader(xmlData)));
		
	}
}
