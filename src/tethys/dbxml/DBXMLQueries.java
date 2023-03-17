package tethys.dbxml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import dbxml.JerseyClient;
import tethys.TethysControl;
import tethys.output.TethysExportParams;

/**
 * Some standard queries we're going to want to make from various 
 * parts of the system as the user interracts with the GUI.
 * @author dg50
 *
 */
public class DBXMLQueries {

	private TethysControl tethysControl;

	public DBXMLQueries(TethysControl tethysControl) {
		super();
		this.tethysControl = tethysControl;
	}

	public ArrayList<String> getProjectNames() {
		DBXMLConnect dbxmlConnect = tethysControl.getDbxmlConnect();
		ServerStatus serverStatus = dbxmlConnect.pingServer();
		if (serverStatus.ok == false) {
			return null;
		}
		Document doc = null;

		TethysExportParams params = tethysControl.getTethysExportParams();

		try {
		JerseyClient jerseyClient = new JerseyClient(params.getFullServerName());

		String testJson = "{\"return\":[\"Deployment/Project\"],\"select\":[],\"enclose\":1}";
		// web browse to http://localhost:9779/Client

		String testResult = jerseyClient.queryJSON(testJson);

		doc = convertStringToXMLDocument(testResult);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (doc == null) {
			return null;
		}

		ArrayList<String> projectNames = new ArrayList<>();
		// iterate through the document and make a list of names, then make them unique. 
		/* looking for elements like this:
		 * 
		 * check out the jaxb unmarshaller ...
    <Return>
        <Deployment>
            <Project>LJ</Project>
        </Deployment>
    </Return>
		 */
		NodeList returns = doc.getElementsByTagName("Return");
//		System.out.println("N projects = " + returns.getLength());
		int n = returns.getLength();
		for (int i = 0; i < n; i++) {
			Node aNode = returns.item(i);
			if (aNode instanceof Element) {
				Node depEl = ((Element) aNode).getFirstChild();
				if (depEl == null) {
					continue;
				}
				if (depEl instanceof Element) {
					Element projEl = (Element) ((Element) depEl).getFirstChild();
					String projName = projEl.getTextContent();
					if (projName != null) {
						if (projectNames.contains(projName) == false) {
							projectNames.add(projName);
						}
					}
				}
			}
		}
		
		Collections.sort(projectNames);

		return projectNames;
	}

	private Document convertStringToXMLDocument(String xmlString) {
		//Parser that produces DOM object trees from XML content
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

}
