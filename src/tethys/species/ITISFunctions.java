package tethys.species;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import PamController.settings.output.xml.PAMGuardXMLPreview;
import PamController.settings.output.xml.PamguardXMLWriter;
import tethys.TethysControl;
import tethys.dbxml.DBQueryResult;
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
		System.out.println(fDoc);
		
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
}
