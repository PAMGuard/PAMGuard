package tethys.dbxml;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import PamController.settings.output.xml.PamguardXMLWriter;
import dbxml.JerseyClient;
import tethys.output.TethysExportParams;

public class DMXMLQueryTest {

	public static void main(String[] args) {
		new DMXMLQueryTest().runTest();
	}

	private void runTest() {
		TethysExportParams params = new TethysExportParams();

		JerseyClient jerseyClient = new JerseyClient(params.getFullServerName());

//		String testJson = "{\"return\":[\"Deployment/Project\",\"Deployment/DeploymentId\",\"Deployment/Site\",\"Deployment/DeploymentDetails/AudioTimeStamp\",\"Deployment/RecoveryDetails/AudioTimeStamp\"],\"select\":[],\"enclose\":1}";
//		String testJson = "{\"return\":[\"Deployment/Project\",\"Deployment/Region\",\"Deployment/DeploymentDetails/AudioTimeStamp\",\"Deployment/RecoveryDetails/AudioTimeStamp\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Deployment/DeploymentId\",\"2\"],\"optype\":\"binary\"}],\"enclose\":1}";
		//String testJson = "{\"return\":[\"Deployment/Project\",\"Deployment/Region\",\"Deployment/DeploymentDetails/AudioTimeStamp\",\"Deployment/RecoveryDetails/AudioTimeStamp\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Deployment/DeploymentId\",\"2\"],\"optype\":\"binary\"},{\"op\":\"=\",\"operands\":[\"Deployment/Project\",\"DCLDE2022\"],\"optype\":\"binary\"}],\"enclose\":1}";
//		String testJson = "{\"return\":[\"Deployment/Project\",\"Deployment/Region\",\"Deployment/DeploymentDetails/AudioTimeStamp\",\"Deployment/RecoveryDetails/AudioTimeStamp\",\"Deployment/DeploymentId\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Deployment/DeploymentId\",\"2\"],\"optype\":\"binary\"},{\"op\":\"=\",\"operands\":[\"Deployment/Project\",\"DCLDE2022\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String testJson = "{\"return\":[\"Deployment/Project\"],\"select\":[],\"enclose\":1}";
		// web browse to http://localhost:9779/Client

		String testResult = jerseyClient.queryJSON(testJson);

		Document doc = convertStringToXMLDocument(testResult);

		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
		String formettedXML = pamXMLWriter.getAsString(doc, true);

		System.out.println(testResult);
		System.out.println(formettedXML);
//		try {
//			Transformer serializer = SAXTransformerFactory.newInstance()
//					.newTransformer();
//			Source source = new StreamSource(testResult);
//			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//			StreamResult res = new StreamResult(bytes);
//			serializer.transform(source, res);
//			System.out.println(bytes.toString());
//		} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
//			e.printStackTrace();
//		}
//		//		System.err.println(testResult);
//		catch (TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

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
