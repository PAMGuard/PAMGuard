package tethys.dbxml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import PamController.settings.output.xml.PamguardXMLWriter;
import PamguardMVC.PamDataBlock;
import dbxml.JerseyClient;
import dbxml.Queries;
import nilus.Deployment;
import nilus.Deployment.Instrument;
import nilus.DeploymentRecoveryDetails;
import nilus.DescriptionType;
import nilus.Detections;
import nilus.Helper;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.output.TethysExportParams;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Some standard queries we're going to want to make from various
 * parts of the system as the user interacts with the GUI.
 * @author dg50
 *
 */
public class DBXMLQueries {

	private TethysControl tethysControl;
	private DBXMLConnect dbXMLConnect;
	
	private PamWarning queryWarning;

	public DBXMLQueries(TethysControl tethysControl, DBXMLConnect dbXMLConnect) {
		super();
		this.tethysControl = tethysControl;
		this.dbXMLConnect = dbXMLConnect;
		queryWarning = new PamWarning("Tethys Query", null, 0);
	}

	/**
	 * Execute a DBXML query. Returns an object which included the time
	 * taken to execute the query and either a returned Document or an Exception.
	 * Or will return null if the server is not connected
	 * @param jsonQueryString
	 * @return query result
	 * @throws TethysQueryException 
	 */
	private DBQueryResult executeQuery(String jsonQueryString) throws TethysQueryException {
		long t1 = System.currentTimeMillis();
		DBQueryResult result = null;
		TethysQueryException tException = null;
		try {
			result = executeQueryT(jsonQueryString);
		}
		catch (TethysQueryException e) {
			tException = e;
		}
		if (result == null) {
			// try pinging the server and throw an exception if it's not alive.
			ServerStatus serverStatus = tethysControl.getDbxmlConnect().pingServer();
			if (serverStatus.ok) {
				queryWarning.setWarnignLevel(2);
				queryWarning.setWarningMessage("null return from Tethys json query");
				queryWarning.setWarningTip(jsonQueryString);
				queryWarning.setEndOfLife(Long.MAX_VALUE);
				WarningSystem.getWarningSystem().addWarning(queryWarning);
			}
			else {
				queryWarning.setWarnignLevel(2);
				if (serverStatus.error != null) {
					queryWarning.setWarningMessage(serverStatus.error.getMessage());
				}
				else {
					queryWarning.setWarningMessage("Unknown Tethys server error");
				}
				queryWarning.setWarningTip(jsonQueryString);
				queryWarning.setEndOfLife(Long.MAX_VALUE);
				WarningSystem.getWarningSystem().addWarning(queryWarning);
				return null;
			}
		}
		long t2 = System.currentTimeMillis();

		if (tException != null) {
			// display query warning then throw the exception anyway
			queryWarning.setWarnignLevel(2);
			queryWarning.setWarningMessage("Error running Tethys json query");
			queryWarning.setWarningTip(jsonQueryString);
			queryWarning.setEndOfLife(Long.MAX_VALUE);
			WarningSystem.getWarningSystem().addWarning(queryWarning);
			throw tException;
		}
		else {
			// clear warning, but say how long query took still
			queryWarning.setWarnignLevel(0);
			queryWarning.setWarningMessage(String.format("Tethys json query executed in %4.2f seconds", (double) (t2-t1)/1000.));
			queryWarning.setWarningTip(jsonQueryString);
			WarningSystem.getWarningSystem().addWarning(queryWarning);
			queryWarning.setEndOfLife(t2+10000);
			return result;
		}
		
	}

	private DBQueryResult executeQueryT(String jsonQueryString) throws TethysQueryException {
		long t1 = System.currentTimeMillis();

		DBXMLConnect dbxmlConnect = tethysControl.getDbxmlConnect();
		ServerStatus serverStatus = dbxmlConnect.pingServer();
		if (!serverStatus.ok) {
			return null;
		}

		String queryResult = null;
		String schemaPlan = null;
		TethysExportParams params = tethysControl.getTethysExportParams();

		try {
			JerseyClient jerseyClient = dbxmlConnect.getJerseyClient();
//			Queries queries = new Queries(jerseyClient);

			queryResult = jerseyClient.queryJSON(jsonQueryString, 0);
			schemaPlan = jerseyClient.queryJSON(jsonQueryString, 1);

		}
		catch (Exception e) {
			throw new TethysQueryException("Error running JSON query", jsonQueryString);

		}
		return new DBQueryResult(System.currentTimeMillis()-t1, queryResult, schemaPlan);
	}

	public ArrayList<String> getProjectNames() {

		String projectQuery = "{\"return\":[\"Deployment/Project\"],\"select\":[],\"enclose\":1}";

		DBQueryResult result;
		try {
			result = executeQuery(projectQuery);
		} catch (TethysQueryException e) {
			tethysControl.showException(e);
			return null;
		}

		if (result == null || result.queryResult == null) {
			return null;
		}

//		System.out.println("Project query execution time millis = " + result.queryTimeMillis);

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
		Document doc = convertStringToXMLDocument(result.queryResult);
		if (doc == null) {
			return null;
		}
		NodeList returns = doc.getElementsByTagName("Project");
		//		System.out.println("N projects = " + returns.getLength());
		int n = returns.getLength();
		for (int i = 0; i < n; i++) {
			Node aNode = returns.item(i);
			String projName = aNode.getTextContent();
			if (projName != null) {
				if (!projectNames.contains(projName)) {
					projectNames.add(projName);
				}
			}
//		}
//			if (aNode instanceof Element) {
//				Node depEl = ((Element) aNode).getFirstChild();
//				if (depEl == null) {
//					continue;
//				}
//				if (depEl instanceof Element) {
//					Element projEl = (Element) ((Element) depEl).getFirstChild();
//					String projName = projEl.getTextContent();
//					if (projName != null) {
//						if (!projectNames.contains(projName)) {
//							projectNames.add(projName);
//						}
//					}
//				}
//			}
		}

		Collections.sort(projectNames);

		return projectNames;
	}

	/**
	 * Get some basic (not all) data for deployments associated with a project. Note that
	 * this may include deployments which are NOT part of the current dataset. That requires
	 * a search on Instrument as well.
	 * @param projectName
	 * @return
	 */
	public ArrayList<nilus.Deployment> getProjectDeployments(String projectName) {
		if (projectName == null) {
			return null;
		}
		String qBase = "{\"return\":[\"Deployment\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Deployment/Project\",\"%s\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String qStr = String.format(qBase, projectName);

		DBQueryResult result = null;
		try {
			result = executeQuery(qStr);
		} catch (TethysQueryException e1) {
			tethysControl.showException(e1);
		}
		if (result == null)  {
			return null;
		}
//		System.out.println("Deployment query execution time millis = " + result.queryTimeMillis);

		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();

		Document doc = convertStringToXMLDocument(result.queryResult);
		if (doc == null) {
			return null;
		}

//		System.out.println(pamXMLWriter.getAsString(doc));

		ArrayList<Deployment> deployments = new ArrayList<>();

		NodeList returns = doc.getElementsByTagName("Deployment");
//		if (returns.getLength() == 0) {
//			// try REsult instead !
//			returns = doc.getElementsByTagName("Result");
//		}
		//		System.out.println("N projects = " + returns.getLength());
		int n = returns.getLength();

//		Queries queries = new Queries(null)
		for (int i = 0; i < n; i++) {
			Node aNode = returns.item(i);
			if (aNode instanceof Element) {
				Element returnedEl = (Element) aNode;

				String Id = getElementData(returnedEl, "Id");
				String project = getElementData(returnedEl, "Project");
				String DeploymentId = getElementData(returnedEl, "DeploymentId");
				String instrType = getElementData(returnedEl, "Instrument.Type");
				String instrId = getElementData(returnedEl, "Instrument.InstrumentId");
				String geometry = getElementData(returnedEl, "Instrument.GeometryType");
				String audioStart = getElementData(returnedEl, "DeploymentDetails.AudioTimeStamp");
				String audioEnd = getElementData(returnedEl, "RecoveryDetails.AudioTimeStamp");
				String region = getElementData(returnedEl, "Region");
				Deployment deployment = new Deployment();
				try {
					Helper.createRequiredElements(deployment);
				} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
					e.printStackTrace();
				}
				deployment.setId(Id);
				deployment.setProject(projectName);
				deployment.setDeploymentId(Integer.valueOf(DeploymentId));
				XMLGregorianCalendar gcStart = TethysTimeFuncs.fromGregorianXML(audioStart);
				XMLGregorianCalendar gcEnd = TethysTimeFuncs.fromGregorianXML(audioEnd);
//				System.out.printf("Converted %s to %s\n", audioStart,
//						PamCalendar.formatDBDateTime(TethysTimeFuncs.millisFromGregorianXML(gcStart), true));
				deployment.getDeploymentDetails().setAudioTimeStamp(gcStart);
				if (deployment.getRecoveryDetails() == null) {
					deployment.setRecoveryDetails(new DeploymentRecoveryDetails());
				}
				deployment.getRecoveryDetails().setAudioTimeStamp(gcEnd);
				if (instrType != null || instrId != null) {
					Instrument instrument = new Instrument();
					instrument.setType(instrType);
					instrument.setInstrumentId(instrId);
					instrument.setGeometryType(geometry);
					deployment.setInstrument(instrument);
				}
				deployment.setRegion(region);
				deployments.add(deployment);
			}
		}
		return deployments;
	}

	/**
	 * Get a list of Detections documents which associate with a datablock and a deploymentId.
	 * @param dataBlock
	 * @param deploymentId
	 * @return
	 */
	public ArrayList<String> getDetectionsDocuments(PamDataBlock dataBlock, String deploymentId) {
		/**
		 * first query for Detections documents associated with this deployment and datablock.
		 * updated May 23
		 */
		String queryNoDepl = "{\"species\":{\"query\":{\"op\":\"lib:completename2tsn\",\"optype\":\"function\",\"operands\":[\"%s\"]},\"return\":{\"op\":\"lib:tsn2completename\",\"optype\":\"function\",\"operands\":[\"%s\"]}},\"return\":[\"Detections/Id\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/Algorithm/Software\",\"LongDataName\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String queryWithDepl = "{\"species\":{\"query\":{\"op\":\"lib:completename2tsn\",\"optype\":\"function\",\"operands\":[\"%s\"]},\"return\":{\"op\":\"lib:tsn2completename\",\"optype\":\"function\",\"operands\":[\"%s\"]}},\"return\":[\"Detections/Id\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/DataSource/DeploymentId\",\"TheDeploymentId\"],\"optype\":\"binary\"},{\"op\":\"=\",\"operands\":[\"Detections/Algorithm/Software\",\"LongDataName\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String query;
		if (deploymentId == null) {
			query = queryNoDepl;
		}
		else {
			query = queryWithDepl.replace("TheDeploymentId", deploymentId);
		}
		query = query.replace("LongDataName", dataBlock.getLongDataName());
		DBQueryResult queryResult = null;
		try {
			queryResult = executeQuery(query);
		} catch (TethysQueryException e1) {
			tethysControl.showException(e1);
			return null;
		}
		if (queryResult ==null) {
			return null;
		}
		Document doc;
		try {
			doc = queryResult.getDocument();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
		if (doc == null) {
			return null;
		}
		ArrayList<String> detectionsNames = new ArrayList();
		int count = 0;
		NodeList returns = doc.getElementsByTagName("Detections");
//		if (returns.getLength() == 0) {
//			returns = doc.getElementsByTagName("Result");
//		}
		for (int i = 0; i < returns.getLength(); i++) {
			Node aNode = returns.item(i);
			String docName = aNode.getTextContent();
			detectionsNames.add(docName);
		}
		return detectionsNames;
	}

	/**
		 * Get the names of all detection documents for a given deployment for all data streams.
		 * @param deploymentId
		 * @return
		 */
		public ArrayList<String> getDetectionsDocuments(String deploymentId) {
			String queryBase = "{\"species\":{\"query\":{\"op\":\"lib:abbrev2tsn\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]},\"return\":{\"op\":\"lib:tsn2abbrev\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]}},\"return\":[\"Detections/Id\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/DataSource/DeploymentId\",\"SomeDeploymentId\"],\"optype\":\"binary\"}],\"enclose\":1}";
			String queryStr = queryBase.replace("SomeDeploymentId", deploymentId);
			DBQueryResult queryResult = null;
			try {
				queryResult = executeQuery(queryStr);
			} catch (TethysQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (queryResult == null || queryResult.queryException != null) {
				return null;
			}

	//		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();

			Document doc = convertStringToXMLDocument(queryResult.queryResult);
			if (doc == null) {
				return null;
			}

			ArrayList<String> detectionDocs = new ArrayList<>();

			NodeList returns = doc.getElementsByTagName("Return");
			if (returns.getLength() == 0) {
				returns = doc.getElementsByTagName("Result");
			}
			for (int i = 0; i < returns.getLength(); i++) {
				Node aNode = returns.item(i);
				detectionDocs.add(aNode.getTextContent());
			}
			return detectionDocs;
		}

	public int countData(PamDataBlock dataBlock, String deploymentId) {
//		/**
//		 * first query for Detections documents associated with this deployment and datablock.
//		 */
//		String queryNoDepl = "{\"species\":{\"query\":{\"op\":\"lib:abbrev2tsn\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]},\"return\":{\"op\":\"lib:tsn2abbrev\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]}},\"return\":[\"Detections/Id\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/Algorithm/Software\",\"LongDataName\"],\"optype\":\"binary\"}],\"enclose\":1}";
//		String queryWithDepl = "{\"species\":{\"query\":{\"op\":\"lib:abbrev2tsn\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]},\"return\":{\"op\":\"lib:tsn2abbrev\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]}},\"return\":[\"Detections/Id\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/Algorithm/Software\",\"LongDataName\"],\"optype\":\"binary\"},{\"op\":\"=\",\"operands\":[\"Detections/DataSource/DeploymentId\",\"TheDeploymentId\"],\"optype\":\"binary\"}],\"enclose\":1}";
//		String query;
//		if (deploymentId == null) {
//			query = queryNoDepl;
//		}
//		else {
//			query = queryWithDepl.replace("TheDeploymentId", deploymentId);
//		}
//		query = query.replace("LongDataName", dataBlock.getLongDataName());
//		DBQueryResult queryResult = executeQuery(query);
//		if (queryResult ==null) {
//			return 0;
//		}
//		Document doc;
//		try {
//			doc = queryResult.getDocument();
//		} catch (ParserConfigurationException | SAXException | IOException e) {
//			e.printStackTrace();
//			return 0;
//		}
//
//		int count = 0;
//		NodeList returns = doc.getElementsByTagName("Return");
		ArrayList<String> documentNames = getDetectionsDocuments(dataBlock, deploymentId);
		if (documentNames == null) {
			return 0;
		}
		int count = 0;
		for (String docName : documentNames) {
//			System.out.println(aNode.getTextContent());
			int count2 = countDetections2(docName);
			count += count2; //countDetecionsData(docName);

		}
		return count;
	}

	public String getDocument(String collection, String documentId) {
//		String queryBase = "return:(collection(\"replaceCollectionName\")/Detections[Id=\"ReplaceDocumentId\"])";
//		queryBase = queryBase.replace("replaceCollectionName", collection);
//		queryBase = queryBase.replace("ReplaceDocumentId", documentId);
//
//		String result = null;
//		try {
//			Queries queries = dbXMLConnect.getTethysQueries();
//			result = queries.QueryTethys(queryBase);
////			System.out.println(result);
//		}
//		catch (Exception e) {
//			System.out.println("Error executing " + queryBase);
////			e.printStackTrace();
//			return null;
//		}
//		return result;

		Queries queries = dbXMLConnect.getTethysQueries();
		String result = null;
		try {
			result = queries.getDocument(collection, documentId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

//		String queryBase = "{\"return\":[\"Deployment/Project\"],\"select\":[],\"enclose\":1}";
	}
	
	/**
	 * Count on effort detections in a Detections document
	 * @param docName
	 * @return
	 */
	public int countDetections2(String docName) {
		TethysExportParams params = tethysControl.getTethysExportParams();
		String queryBase = "count(collection(\"Detections\")/Detections[Id=\"ReplaceDocumentId\"]/OnEffort/Detection)";
		String query = queryBase.replace("ReplaceDocumentId", docName);

		String result = null;
		try {
			Queries queries = dbXMLConnect.getTethysQueries();
			result = queries.QueryTethys(query);
//			System.out.println(result);
		}
		catch (Exception e) {
			System.out.println("Error executing " + query);
//			e.printStackTrace();
			return -1;
		}
		int count = 0;
		try {
		 count = Integer.valueOf(result);
		}
		catch (NumberFormatException e) {
			System.out.println("Unable to interpret count data " + result);
			return 0;
		}
		return count;
	}

//	/**
//	 * Get a count of the detections in a detections document.
//	 * Only looking in onEffort so far.
//	 * @param deploymentId
//	 * @param detectionDocId
//	 * @param dataBlock
//	 * @return
//	 */
//	public int getDetectionsDetectionCount(String deploymentId, String detectionDocId, PamDataBlock dataBlock) {
//		String queryBase = "{\"species\":{\"query\":{\"op\":\"lib:abbrev2tsn\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]},\"return\":{\"op\":\"lib:tsn2abbrev\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]}},\"return\":[\"Detections/OnEffort/Detection/Start\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/Id\",\"SomeDetectionsId\"],\"optype\":\"binary\"},{\"op\":\"=\",\"operands\":[\"Detections/DataSource/DeploymentId\",\"SomeDeploymentId\"],\"optype\":\"binary\"}],\"enclose\":1}";
//		String queryStr = queryBase.replace("SomeDetectionsId", detectionDocId);
//		queryStr = queryStr.replace("SomeDeploymentId", deploymentId);
//		DBQueryResult queryResult = executeQuery(queryStr);
//		if (queryResult == null || queryResult.queryException != null) {
//			return 0;
//		}
////		System.out.println("Detections query time ms = " + queryResult.queryTimeMillis);
//
//		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
//
//		Document doc = convertStringToXMLDocument(queryResult.queryResult);
//		if (doc == null) {
//			return 0;
//		}
//
////		System.out.println(pamXMLWriter.getAsString(doc));
//
////		ArrayList<String> detectionDocs = new ArrayList<>();
//
//		NodeList returns = doc.getElementsByTagName("Start");
//		int n = returns.getLength();
//		return n;
//	}

//	/**
//	 * This is the quickest way of counting data in a project, but it will load the start
//	 * times for every detection in a project at once, so might use a lot of memory. Also
//	 * it wll probably get data for all deployments in a project, which may not be what we want.
//	 * @param projectName
//	 * @param dataPrefixes
//	 * @return
//	 */
//	public int[] countDataForProject(String projectName, String[] dataPrefixes) {
//		int[] n = new int[dataPrefixes.length];
//		ArrayList<PDeployment> matchedDeployments = tethysControl.getDeploymentHandler().getMatchedDeployments();
////		ArrayList<nilus.Deployment> deployments = getProjectDeployments(projectName);
//		if (matchedDeployments == null) {
//			return null;
//		}
//		for (PDeployment aDeployment : matchedDeployments) {
////			ArrayList<String> detectionsIds = getDetectionsDocsIds(aDeployment.getId());
////			for (String detId : detectionsIds) {
////				n += getDetectionsDetectionCount(aDeployment.getId(), detId, dataBlock);
////			}
//			int[] newN =  countDataForDeployment(projectName, aDeployment.deployment.getId(), dataPrefixes);
//			for (int i = 0; i < n.length; i++) {
//				n[i] += newN[i];
//			}
//		}
//		return n;
//	}

	/**
	 * Count data within a deployment document which is associated with a set of datablocks
	 * Since the detections all come back in one query, it's easier to count all datablocks at once so
	 * that it can all happen off a single query.
	 * @param id
	 * @param dataBlockPrefixes
	 * @return
	 */
	private int[] countDataForDeployment(String projectId, String deploymentId, String[] dataPrefixes) {
		String queryBase = "{\"species\":{\"query\":{\"op\":\"lib:abbrev2tsn\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]},\"return\":{\"op\":\"lib:tsn2abbrev\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]}},\"return\":[\"Detections/Id\",\"Detections/OnEffort/Detection/Start\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/DataSource/DeploymentId\",\"ReplaceDeploymentIdString\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String queryString = queryBase.replace("ReplaceDeploymentIdString", deploymentId);
		DBQueryResult result;
		try {
			result = executeQuery(queryString);
		} catch (TethysQueryException e) {
			tethysControl.showException(e);
			return null;
		}
		if (result == null || result.queryResult == null) {
			return null;
		}
		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();

		Document doc = convertStringToXMLDocument(result.queryResult);
		if (doc == null) {
			return null;
		}

//		System.out.println(pamXMLWriter.getAsString(doc));

		NodeList detsDocs = doc.getElementsByTagName("Detections");
		int[] blockCounts = new int[dataPrefixes.length];

//		String detDocPrefix = projectId + "_" + dataBlock.getDataName();

//		int totalCalls = 0;
		int detCount = 0;
		int dataIndex;
		for (int i = 0; i < detsDocs.getLength(); i++) {
			Node detNode = detsDocs.item(i);

			NodeList childNodes = detNode.getChildNodes();
			detCount = childNodes.getLength()-1;
			dataIndex = -1;
			for (int n = 0; n < childNodes.getLength(); n++) {
				Node aNode = childNodes.item(n);
				if (aNode instanceof Element) {
					Element el = (Element) aNode;
					String nodeName = el.getNodeName();
					if (nodeName.equals("Id")) {
						String id = el.getTextContent();
						for (int j = 0; j < dataPrefixes.length; j++) {
							if (id != null && id.startsWith(dataPrefixes[j])) {
								dataIndex = j;
							}
						}
//						if (id != null && id.startsWith(detDocPrefix) == false) {
//							detCount = 0;
//							break;
//						}
					}
				}
			}
			if (dataIndex >= 0) {
				blockCounts[dataIndex] += detCount;
			}
//			System.out.printf("%d Added %d for new total %d\n",i, detCount, totalCalls);
		}

		return blockCounts;
	}

	private String getElementData(Element root, String elName) {
		String[] tree = elName.split("\\.");
		for (String element : tree) {
			NodeList nodeList = root.getElementsByTagName(element);
			// should only be one node for what we're unpacking.
			if (nodeList == null || nodeList.getLength() == 0) {
				return null;
			}
			Node firstNode = nodeList.item(0);
			if (firstNode instanceof Element) {
				root = (Element) firstNode;
			}
		}
		return root.getTextContent();
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

	/**
	 * Get the basic information about a Detections document. This is basically everything apart from
	 * the actual detections themselves.
	 * @param aDoc
	 * @return
	 */
	public Detections getDetectionsDocInfo(String detectionsDocName) {
		String oldqueryBase = "{\"species\":{\"query\":{\"op\":\"lib:abbrev2tsn\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]},\"return\":{\"op\":\"lib:tsn2abbrev\",\"optype\":\"function\",\"operands\":[\"%s\",\"SIO.SWAL.v1\"]}},\"return\":[\"Detections/Id\",\"Detections/Description\",\"Detections/DataSource\",\"Detections/Algorithm\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/Id\",\"DetectionsDocName\"],\"optype\":\"binary\"}],\"enclose\":1}";
		// updated May 23
		String queryBase = "{\"species\":{\"query\":{\"op\":\"lib:completename2tsn\",\"optype\":\"function\",\"operands\":[\"%s\"]},\"return\":{\"op\":\"lib:tsn2completename\",\"optype\":\"function\",\"operands\":[\"%s\"]}},\"return\":[\"Detections/Id\",\"Detections/Description\",\"Detections/DataSource\",\"Detections/Algorithm\",\"Detections/QualityAssurance\",\"Detections/UserId\",\"Detections/MetadataInfo\"],\"select\":[{\"op\":\"=\",\"operands\":[\"Detections/Id\",\"DetectionsDocName\"],\"optype\":\"binary\"}],\"enclose\":1}";
		String query = queryBase.replace("DetectionsDocName", detectionsDocName);
		DBQueryResult queryResult;
		try {
			queryResult = executeQuery(query);
		} catch (TethysQueryException e) {
			tethysControl.showException(e);
			return null;
		}
		Document doc;
		try {
			doc = queryResult.getDocument();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
//		System.out.println(queryResult.queryResult);

		Detections detections = new Detections();
		try {
			Helper.createRequiredElements(detections);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}

		NodeList returns = doc.getElementsByTagName("Result");
		//		System.out.println("N projects = " + returns.getLength());
		int n = returns.getLength();
		if (n == 0) {
			return null;
		}
		Element result = (Element) returns.item(0);

		DescriptionType description = detections.getDescription();
		if (description == null) {
			description = new DescriptionType();
			detections.setDescription(description);
		}
		detections.setId(getElementData(result, "Id"));
		description.setAbstract(getElementData(result, "Description.Abstract"));
		description.setMethod(getElementData(result, "Description.Method"));
		description.setObjectives(getElementData(result, "Description.Objectives"));



		// TODO Auto-generated method stub
		return detections;
	}

}
