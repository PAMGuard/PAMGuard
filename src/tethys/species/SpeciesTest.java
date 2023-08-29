package tethys.species;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import PamController.settings.output.xml.PamguardXMLWriter;
import dbxml.JerseyClient;
import dbxml.Queries;
import tethys.dbxml.DBQueryResult;
import tethys.dbxml.TethysQueryException;

public class SpeciesTest {

	String uri = "http://localhost:9779";
	public static void main(String[] args) {
		
		SpeciesTest st = new SpeciesTest();
		st.runJson();
		
//		int spermWhale = 180488;
//		st.getCodeInfo(spermWhale);
//		st.runXQuery();

	}
	private  void getCodeInfo(int itisCode) {
		System.out.println("Running getCodeInfo()");
		String jQBase = "{\"return\":[\"ranks/rank\"],\"select\":[{\"op\":\"=\",\"operands\":[\"ranks/rank/tsn\",\"SPECIESTSN\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String jQ = jQBase.replace("SPECIESTSN", String.format("%d", itisCode));
		

		DBQueryResult result = null;
		String queryResult = null;
		String schemaPlan = null;
		JerseyClient jerseyClient = new JerseyClient(uri);
		long t1 = System.nanoTime();
		try {
			queryResult = jerseyClient.queryJSON(jQ, 0);
//			schemaPlan = jerseyClient.queryJSON(jQ, 1);
		} catch (Exception e1) {
			e1.printStackTrace();
			
		}
		long t2 = System.nanoTime();
		System.out.printf("Query time was %3.1fms\n" , (double) (t2-t1)/1e6);
		System.out.println(queryResult);
		
//		TethysITISResult itisResult = new TethysITISResult(queryResult);
	}
	/*
	 * 
<ranks xmlns="http://tethys.sdsu.edu/schema/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://tethys.sdsu.edu/schema/1.0 tethys.xsd">
  <rank>
    <tsn>-10</tsn>
    <completename>Other phenomena</completename>
    <vernacular>
      <name language="English">Other</name>
      <name language="French">Autre</name>
      <name language="Spanish">Otro</name>
    </vernacular>
  </rank>
  <rank>
    <tsn>555654</tsn>
    <completename>Delphinus capensis</completename>
    <vernacular>
      <name language="English">Long-beaked Common Dolphin</name>
    </vernacular>
  </rank>
	 */
	
	private void runXQuery() {
		System.out.println("Running runXQuery()");
		String queryBase = "count(collection(\"Detections\")/Detections[Id=\"ReplaceDocumentId\"]/OnEffort/Detection)";
//		String xQ = "collection(\"ITIS_ranks\")/ty:ranks/ty:rank[dbxml:contains(ty:completename, \"Physeter\")]";
		
//		String xQ = "<Result xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> {\r\n"
//				+ "  for $Deployment0 in collection(\"Deployments\")/Deployment[Project = \"BM\"]\r\n"
//				+ "  return\r\n"
//				+ "    <Deployment>{\r\n"
//				+ "      $Deployment0/Id\r\n"
//				+ "    }</Deployment>\r\n"
//				+ "} </Result>";

		String xQ = "<Result xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> {\r\n"
				+ "  for $rank0 in collection(\"ITIS_ranks\")/rank[tsn = \"180488\"]\r\n"
				+ "  return\r\n"
				+ "    <rank>{\r\n"
				+ "      $rank0/completename\r\n"
				+ "    }</rank>\r\n"
				+ "} </Result>";
		

		JerseyClient jerseyClient = new JerseyClient(uri);
		Queries queries = new Queries(jerseyClient);
		
		String queryResult = null;
		try {
			 queryResult = queries.QueryTethys(xQ);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//API to obtain DOM Document instance
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
			System.out.println(queryResult);
		}
//		PAMGuardXMLPreview xmlPreview = new PAMGuardXMLPreview(null, "returned", qResult.queryResult)
		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
		String fDoc = pamXMLWriter.getAsString(doc, true);
		System.out.println(fDoc);
//		System.out.println(queryResult);
		
	}
	private void runJson() {
//		String jQ = "{\"return\":[\"Deployment\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Deployment/Project\",\"DCLDE2022\"],\"optype\":\"binary\"}],\"enclose\":1}";
//		String jQ = "{\"return\":[\"ranks/rank\"],\"select\":[{\"op\":\"=\",\"operands\":[\"ranks/rank/tsn\",\"180488\"],\"optype\":\"binary\"}],\"enclose\":1}";
//		String jQ = "{\"return\":[\"ranks/rank\"],\"select\":[{\"op\":\"=\",\"operands\":[\"ranks/rank/tsn\",\"180488\"],\"optype\":\"binary\"}],\"enclose\":1}";
//		String jQ = "{\"return\":[\"ranks/rank\"],\"select\":[{\"op\":\"=\",\"operands\":[\"ranks/rank/completename\",\"Physeter macrocephalus\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String jQ = "{\"return\":[\"ranks/rank\"],\"select\":[{\"op\":\"dbxml:contains\",\"operands\":[\"ranks/rank/completename\",\"Sperm\"],\"optype\":\"function\"}],\"enclose\":1}";

		System.out.println(jQ);

		DBQueryResult result = null;
		String queryResult = null;
		String schemaPlan = null;
		JerseyClient jerseyClient = new JerseyClient(uri);
		long t1 = System.nanoTime();
		try {
			queryResult = jerseyClient.queryJSON(jQ, 0);
//			schemaPlan = jerseyClient.queryJSON(jQ, 1);
		} catch (Exception e1) {
			System.out.println("epic fail");
			e1.printStackTrace();
		}
		long t2 = System.nanoTime();
		System.out.printf("Query time was %3.1fms\n" , (double) (t2-t1)/1e6);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		//API to obtain DOM Document instance
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			//Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();

			//Parse the content to Document object
			doc = builder.parse(new InputSource(new StringReader(queryResult)));
		} catch (Exception e) {
//			e.printStackTrace();
			System.out.println(queryResult);
		}
//		PAMGuardXMLPreview xmlPreview = new PAMGuardXMLPreview(null, "returned", qResult.queryResult)
		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
		String fDoc = pamXMLWriter.getAsString(doc, true);
		System.out.println(fDoc);
//		System.out.println(queryResult);
		
	}

}
