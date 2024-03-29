package tethys.species;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import PamController.settings.output.xml.PAMGuardXMLPreview;
import PamController.settings.output.xml.PamguardXMLWriter;
import dbxml.Queries;
import tethys.TethysControl;
import tethys.dbxml.DBQueryResult;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.DBXMLQueries;
import tethys.dbxml.TethysQueryException;

/**
 * Functions associated with pulling ITIS information from the databsae. 
 * @author dg50
 *
 */
public class ITISFunctions {

	private TethysControl tethysControl;

	public ITISFunctions(TethysControl tethysControl) {
		super();
		this.tethysControl = tethysControl;
	}
	
	public TethysITISResult getITISInformation(int itisCode) {
		/*
		 * hope to get back something like
		 * 
<?xml version="1.0" encoding="ISO-8859-1"?><Result xmlns="http://tethys.sdsu.edu/schema/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <rank>
        <tsn>180488</tsn>
        <taxon_unit>Species</taxon_unit>
        <completename>Physeter macrocephalus</completename>
        <vernacular>
            <name language="English">Sperm Whale</name>
            <name language="English">cachalot</name>
        </vernacular>
    </rank>
</Result>
		 */

		String jQ =  "{\"return\":[\"ranks/rank\"],\"select\":[{\"op\":\"=\",\"operands\":[\"ranks/rank/tsn\",\"SPECIESCODE\"],\"optype\":\"binary\"}],\"enclose\":1}";
		jQ = jQ.replace("SPECIESCODE", String.format("%d", itisCode));
		
		DBXMLQueries dbQueries = tethysControl.getDbxmlQueries();
		DBQueryResult qResult = null;
		try {
			qResult = dbQueries.executeQuery(jQ);
		} catch (TethysQueryException e) {
			e.printStackTrace();
			return null;
		}
		
		Document doc = dbQueries.convertStringToXMLDocument(qResult.queryResult);
		Element docEl = doc.getDocumentElement();
//		doc.
//		PAMGuardXMLPreview xmlPreview = new PAMGuardXMLPreview(null, "returned", qResult.queryResult)
		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
		String fDoc = pamXMLWriter.getAsString(doc, true);
//		System.out.println(fDoc);
		
		String tsn = dbQueries.getElementData(docEl, "tsn");
		if (tsn == null) {
			return null;
		}
		String taxunit = dbQueries.getElementData(docEl, "taxon_unit");
		String latin = dbQueries.getElementData(docEl, "completename");
		// try to find a vernacular.
		NodeList vEls = doc.getElementsByTagName("vernacular");
		String vernacular = null;
		if (vEls.getLength() > 0) {
			Node f = vEls.item(0);
			if (f instanceof Element) {
				vernacular = dbQueries.getElementData((Element) f, "name");
			}
		}
		
		return new TethysITISResult(itisCode, taxunit, latin, vernacular);
	}
	
	/**
	 * Search species codes. If the search term is a valid Integer number
	 * then it's assumed to be an ITIS code and the function should
	 * return a single map item. If it's non-integer, it's assumed to 
	 * be a common or latin name search
	 * @param searchTerm
	 * @return array list of possible matches. 
	 */
	public ArrayList<SpeciesMapItem> searchSpecies(String searchTerm) {
		Integer intVal = null;
		try {
			intVal = Integer.valueOf(searchTerm);
		}
		catch (NumberFormatException e) {
			intVal = null;
		}
		if (intVal != null) {
			return searchCodes(intVal);
		}
		else { // assume name search
			return searchNames(searchTerm);
		}
	}
	
	private ArrayList<SpeciesMapItem> searchCodes(Integer intCode) {
		ArrayList<SpeciesMapItem> mapItems = new ArrayList();
		TethysITISResult result = getITISInformation(intCode);
		if (result != null) {
			mapItems.add(new SpeciesMapItem(intCode, "", "", result.getLatin(), result.getVernacular()));
		}
		return mapItems;
	}

	/**
	 * Search common and latin names for partial matched of the search term
	 * and return an array list of all possible matches. 
	 * @param searchTerm
	 * @return
	 */
	public ArrayList<SpeciesMapItem> searchNames(String searchTerm) {
		ArrayList<SpeciesMapItem> items = new ArrayList<SpeciesMapItem>();		
		String xQ = "let $target := \"thespeciessearchterm\" \r\n"
				+ "return\r\n"
				+ "<Result xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> {\r\n"
				+ "\r\n"
				+ "  for $ranks0 in collection(\"ITIS_ranks\")/ranks/rank[\r\n"
				+ "          dbxml:contains(completename, $target) or \r\n"
				+ "                 vernacular[dbxml:contains(name,$target)]]\r\n"
				+ "return\r\n"
				+ "  <Record> {\r\n"
				+ "    $ranks0/tsn,\r\n"
				+ "            $ranks0/completename,\r\n"
				+ "            <vernacular>\r\n"
				+ "              {string-join($ranks0/vernacular/name, \", \")}\r\n"
				+ "            </vernacular>\r\n"
				+ "  } </Record>\r\n"
				+ "} </Result>\r\n"
				+ "";
		xQ = xQ.replace("thespeciessearchterm", searchTerm);
		DBXMLConnect dbXMLConnect = tethysControl.getDbxmlConnect();
		DBXMLQueries dbxmlQueries = tethysControl.getDbxmlQueries();
		Queries queries = dbXMLConnect.getTethysQueries();

		String queryResult = null;
		try {
			 queryResult = queries.QueryTethys(xQ);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return items;
		}
		

		DocumentBuilder builder = null;
		Document doc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			//Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();

			//Parse the content to Document object
			doc = builder.parse(new InputSource(new StringReader(queryResult)));
		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println(queryResult);
			return items;
		}
		// print for now, then comment this out.. 
//		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
//		String fDoc = pamXMLWriter.getAsString(doc, true);
//		System.out.println(fDoc);
		// now unpack the xml document. 
		NodeList els = doc.getElementsByTagName("Record");
		int n = els.getLength();
		for (int i = 0; i < n; i++) {
			Node aNode = els.item(i);
			if (aNode instanceof Element) {
				Element anEl = (Element) aNode;
				String tsn = dbxmlQueries.getElementData(anEl, "tsn");
				int nTSN = 0;
				try {
					nTSN = Integer.valueOf(tsn);
				}
				catch (NumberFormatException ex) {
					System.out.println("Invalid TSN read from Tethys: " + tsn);
					continue;
				}
				
				String completeName = dbxmlQueries.getElementData(anEl, "completename");
				String vernacular = dbxmlQueries.getElementData(anEl, "vernacular");
				SpeciesMapItem mapItem = new SpeciesMapItem(nTSN, "", "", completeName, vernacular);
				items.add(mapItem);
			}
			
		}
		return items;
	}
}
