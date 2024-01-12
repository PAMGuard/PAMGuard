package tethys.dbxml;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DBQueryResult {

	public long queryTimeMillis;

	public String queryResult;

	public String schemaPlan;

	public Exception queryException;

	public DBQueryResult(long queryTimeMillis, String queryResult, String schemaPlan) {
		super();
		this.queryTimeMillis = queryTimeMillis;
		this.queryResult = queryResult;
		this.schemaPlan = schemaPlan;
	}

	public DBQueryResult(long queryTimeMillis, Exception queryException) {
		super();
		this.queryTimeMillis = queryTimeMillis;
		this.queryException = queryException;
	}

	/**
	 * Get the result as an XML document.
	 * @return XML document
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getDocument() throws ParserConfigurationException, SAXException, IOException {
		if (queryResult == null) {
			return null;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		//API to obtain DOM Document instance
		DocumentBuilder builder = null;

		//Create DocumentBuilder with default configuration
		builder = factory.newDocumentBuilder();

		//Parse the content to Document object
		Document doc = builder.parse(new InputSource(new StringReader(queryResult)));
		return doc;
	}

}
