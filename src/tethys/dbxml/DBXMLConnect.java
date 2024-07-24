package tethys.dbxml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBException;

import dbxml.JerseyClient;
import dbxml.Queries;
import dbxml.uploader.Importer;
import nilus.MarshalXML;
import tethys.Collection;
import tethys.TethysControl;
import tethys.database.TethysActions;
import tethys.database.TethysLogger;
import tethys.niluswraps.NilusChecker;
import tethys.output.TethysExportParams;
import tethys.reporter.TethysReport;
import tethys.reporter.TethysReporter;

/**
 * Class containing functions for managing the database connection. Opening, closing,
 * writing, keeping track of performance, etc.
 * @author Doug Gillespie, Katie O'Laughlin
 *
 */
public class DBXMLConnect {

	private TethysControl tethysControl;
	private File tempDirectory;

	private JerseyClient jerseyClient;

	private Queries queries;

	private String currentSiteURL;

//	public static String[] collections = {"Deployments", "Detections", "Localizations", "Calibrations", "SpeciesAbbreviations"};

	public DBXMLConnect(TethysControl tethysControl) {
		this.tethysControl = tethysControl;

		checkTempFolder();
	}

	/**
	 * Check the jersey client and the queries. Need to recreate
	 * if the url has changed.
	 * @return
	 */
	private boolean checkClient() {
		if (jerseyClient == null || queries == null || currentSiteURL == null) {
			return false;
		}
		TethysExportParams params = tethysControl.getTethysExportParams();
		if (!currentSiteURL.equalsIgnoreCase(params.getFullServerName())) {
			return false;
		}
		return true;
	}


	/**
	 * Get the client. The client will only be recreated if the url changes
	 * @return Jersy client
	 */
	public synchronized  JerseyClient getJerseyClient() {
		if (!checkClient()) {
			openConnections();
		}
		return jerseyClient;
	}

	/**
	 * Get the Queries object. This will only be recreated if the client changes.
	 * @return
	 */
	public synchronized  Queries getTethysQueries() {
		if (!checkClient()) {
			openConnections();
		}
		return queries;
	}

	/**
	 * Convert a nilus Object into a file
	 * @param nilusObject nilus object
	 * @param file file (should not exist)
	 * @return file (will be the same as input file)
	 * @throws TethysException
	 */
	public File createXMLDocument(Object nilusObject, File file) throws TethysException {
		Class objClass = nilusObject.getClass();
		try {
			MarshalXML marshal = new MarshalXML();
			marshal.createInstance(objClass);
			marshal.marshal(nilusObject, file.toString());
		} catch(IllegalArgumentException e) {
			throw new TethysException("IllegalArgumentException posting to Tethys: " + e.getMessage(), null);
		} catch (IOException e) {
			throw new TethysException("IOException posting to Tethys: " + e.getMessage(), null);
		} catch (JAXBException e) {
			throw new TethysException("JAXBException posting to Tethys: " + e.getMessage(), null);
		}
		return file;
	}

	/**
	 * Create a temporary nilus file.
	 * @param nilusObject
	 * @return
	 * @throws TethysException
	 */
	public File createTempXMLDocument(Object nilusObject) throws TethysException {
		String tempName = getTempFileName(nilusObject);
		tempName = tempDirectory.getAbsolutePath() + File.separator + tempName + ".xml";
		File tempFile = new File(tempName);
		File retFile = createXMLDocument(nilusObject, tempFile);
		retFile.deleteOnExit();
		return retFile;
	}


	public boolean postAndLog(Object nilusObject) throws TethysException
	{
		return postAndLog(nilusObject, null);
	}

	/**
	 * I don't think this should ever be used since everything goes a bit pear
	 * shaped if the documentName isn't the same as the Id. However, for Calibration
	 * documents this is no longer the case, since a Calibration can have multiple
	 * entries on different dates, so allow it !
	 * @param nilusObject
	 * @param documentName
	 * @return
	 * @throws TethysException
	 */
	public boolean postAndLog(Object nilusObject, String documentName) throws TethysException
	{
//		boolean ok = NilusChecker.warnEmptyFields(tethysControl.getGuiFrame(), nilusObject);

		TethysException e = null;
		boolean success = false;
		try {
			success = postToTethys(nilusObject, documentName);
		}
		catch (TethysException ex) {
			e = ex;
		}
		TethysLogger logger = TethysLogger.getTethysLogger(tethysControl);
		Class objClass = nilusObject.getClass();
		Collection collection = Collection.fromClass(objClass);
		String documentId = getDocumentId(nilusObject);
		logger.logAction(collection.collectionName(), documentId, TethysActions.ADDDOCUMENT, success, "");
		if (e != null) {
			throw (e);
		}
		return success;
	}

	/**
	 * take a nilus object loaded with PamGuard data and post it to the Tethys database
	 *
	 * @param pamGuardObjs a nilus object loaded with PamGuard data
	 * @return error string, null string means there are no errors
	 * @throws TethysException
	 */
	private boolean postToTethys(Object nilusObject, String documentName) throws TethysException
	{
		Class objClass = nilusObject.getClass();
		Collection collection = Collection.fromClass(nilusObject.getClass());
		TethysExportParams params = new TethysExportParams();
		String importReturn = null;
		if (documentName == null) {
			documentName = getTempFileName(nilusObject);
		}
		documentName = tempDirectory.getAbsolutePath() + File.separator + documentName + ".xml";
		File tempFile = new File(documentName);
		String bodgeName = documentName;//"C:\\Users\\dg50\\AppData\\Local\\Temp\\PAMGuardTethys\\Meygen2022_10a.xml";
		try {
			MarshalXML marshal = new MarshalXML();
			marshal.createInstance(objClass);
			marshal.marshal(nilusObject, tempFile.toString());
			// above lines have made a file. Are now going to gzip it before sending to Tethys
			File zipFile = null;
//			try {
//				zipFile = zipOutputFile(tempFile);
//			}
//			catch (FileNotFoundException e1){
//				System.out.println(e1.getMessage());
//			}
//			catch (IOException e2) {
//				System.out.println(e2.getMessage());
//			}
			String finalName;
			if (zipFile == null) {
				finalName = bodgeName;
			}
			else {
				finalName = zipFile.toString();
			}


			//				tempFile = stripXMLHeader(tempFile);
			importReturn = Importer.ImportFiles(params.getFullServerName(), collection.collectionName(),
					new String[] { finalName }, "", "", false);


			tempFile.deleteOnExit();
			if (zipFile != null) {
				zipFile.deleteOnExit();
			}
		} catch(IllegalArgumentException e) {
			throw new TethysException("IllegalArgumentException posting to Tethys: " + e.getMessage(), null);
		} catch (IOException e) {
			throw new TethysException("IOException posting to Tethys: " + e.getMessage(), null);
		} catch (JAXBException e) {
			throw new TethysException("JAXBException posting to Tethys: " + e.getMessage(), null);
		}

		/*
		 * The returned string consists of the file name, then an XML report.
		 * Quite hard to see much common structure in this, so just look for
		 * two words, <Success> and <Error>
		 */
		boolean error = importReturn.contains("<Error");
		boolean success = importReturn.contains("<Success>");
		String name = tempFile.getName();
		TethysReporter.getTethysReporter().addReport(new TethysReport(success, collection, name, name));
//		error = !success; might be a better options.
		if (error) {
			throw new TethysException("Error posting to Tethys", importReturn);
		}
		return success;
	}

	/**
	 * Zip an xml file (or any file) into a gz file with a new end
	 * @param xmlFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private File zipOutputFile(File xmlFile) throws FileNotFoundException, IOException {
		String zipName = xmlFile.toString() + "-temp-.gz";
		File zipFile = new File(zipName);
		GZIPOutputStream opStream = new GZIPOutputStream(new FileOutputStream(zipFile));
		InputStream fis = new FileInputStream(xmlFile);
		int chunkSize = 100*1024;
		byte[] buffer = new byte[chunkSize];
//		ZipEntry zipEntry = new ZipEntry(xmlFile.getName());
		int bytesRead;
		while ((bytesRead = fis.read(buffer)) >= 0) {
			opStream.write(buffer, 0, bytesRead);
		}

		opStream.close();

		fis.close();


		return zipFile;
	}

	/**
	 * Update a document within Tethys. We're assuming that a
	 * document with the same name in the same collection already
	 * exists. If it doesn't / has a different name, then use
	 * the removedocument function
	 * @param nilusDocument
	 * @return
	 * @throws TethysException
	 */
	public boolean updateDocument(Object nilusDocument) throws TethysException {
		deleteDocument(nilusDocument);
		return postToTethys(nilusDocument, null);
	}

	/**
	 * Delete a nilus document from the database. The only field which
	 * needs to be populated here is the Id. The code also uses the object
	 * class to identify the correct collection.
	 * @param nilusDocument
	 * @return
	 * @throws TethysException
	 */
	public boolean deleteDocument(Object nilusDocument) throws TethysException {

		Class objClass = nilusDocument.getClass();
		Collection collection = Collection.fromClass(objClass);
		String docId = getDocumentId(nilusDocument);
		String result = null;
		try {
			result = jerseyClient.removeDocument(collection.collectionName(), docId );
			/**
			 * Return from a sucessful delete is something like
			 *
				deployment = getTethysControl().getDeploymentHandler().createDeploymentDocument(freeId++, recordPeriod);
				<DELETE>
  <ITEM> ['ECoastNARW0'] </ITEM>
</DELETE>
An error will throw an exception.
			 */
		}
		catch (Exception e) {
//			System.out.printf("Error deleting %s %s: %s\n", collection, docId, e.getMessage());
			String msg = String.format("Error deleting %s:%s", collection, docId);
			throw new TethysException(msg, e.getLocalizedMessage());
		}
		//		forceFlush();
		return true;
	}

	/**
	 * Delete a Deploymnet and any contained Detections document. Doesn't work !
	 * @param deploymentId
	 * @return
	 * @throws TethysException
	 */
	public boolean deleteDeployment(String deploymentId) throws TethysException {
		ArrayList<String> detDocNames = tethysControl.getDbxmlQueries().getDetectionsDocuments(deploymentId);
		JerseyClient jerseyClient = getJerseyClient();
		Queries queries = null;
		String result = null;
		try {
			result = jerseyClient.removeDocument("Deployments", deploymentId );
		}
		catch (Exception e) {
			throw new TethysException("Error deleting deployment document " + deploymentId, e.getMessage());
		}
		return true;
	}

	/**
	 * Remove a document based on a collection name and a cdocument Id.
	 * @param collection collection name.
	 * @param documentName document name (not the internal Document Id)
	 * @return
	 * @throws TethysException
	 */
	public boolean removeDocument(Collection collection, String documentName) throws TethysException {
		return removeDocument(collection.collectionName(), documentName);
	}

	/**
	 * Remove a document based on a collection name and a document namw.
	 * @param collectionName collection name.
	 * @param documentName document name (not the internal Document Id)
	 * @return
	 * @throws TethysException
	 */
	public boolean removeDocument(String collectionName, String documentName) throws TethysException {
		try {
			Object result = jerseyClient.removeDocument(collectionName, documentName );
			/**
			 * Return from a sucessful delete is something like
			 *
				deployment = getTethysControl().getDeploymentHandler().createDeploymentDocument(freeId++, recordPeriod);
				<DELETE>
  					<ITEM> ['ECoastNARW0'] </ITEM>
				</DELETE>
			An error will throw an exception.
			 */
		}
		catch (Exception e) {
			String msg = String.format("Error deleting %s:%s", collectionName, documentName);
			throw new TethysException(msg, e.getLocalizedMessage());
		}
		return true;
	}

	/**
	 *  check the return string from importFiles and if it's an
	 *  error, throw an exception. Otherwise do nothing.
	 * @param fileError
	 */
	private void checkReturnString(String fileError) {
		/**
		 * Example good string is
		 *
C:\Users\dg50\AppData\Local\Temp\PAMGuardTethys\20080311_2DSimplex_0.xml: 7360 bytes
<?xml version="1.0" encoding="iso-8859-1"?>
<Import>
  <Document name="20080311_2DSimplex_0">added</Document>
  <SummaryReport>
    <Success>
      <Document>20080311_2DSimplex_0</Document>
    </Success>
  </SummaryReport>
</Import>

Example error (file not existing)
C:\Users\dg50\AppData\Local\Temp\PAMGuardTethys\20080311_2DSimplex_0.xmlnot: 0 bytes
<Error>
  <File>
    <Name>C:\Users\dg50\AppData\Local\Temp\PAMGuardTethys\20080311_2DSimplex_0.xmlnot</Name>
    <Cause> does not exist </Cause>
  </File>
</Error>



		 */


	}

	/**
	 * Seems we have to get rid of the line <?xml version="1.0" encoding="UTF-8"?>
	 * which is being put there by the marshaller ?
	 * @param tempFile
	 */
	private File stripXMLHeader(File tempFile) {
		// TODO Auto-generated method stub

		File tempTemp = new File(tempFile.getAbsolutePath().replace(".temp.xml", ".xml"));
		try {
			BufferedReader reader = new BufferedReader(new FileReader(tempFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempTemp));
			String line = reader.readLine();
			while (line != null) {
				// see if the line has any unicode in it
				int len = line.length();
				byte[] bytes = line.getBytes();
				if (len == bytes.length) {
					System.out.println(line);
				}

				if (line.startsWith("<?xml version=")) {

				}
				else {
					writer.write(line + "\r\n");
				}
				line = reader.readLine();
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return tempFile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return tempFile;
		}
		boolean deleted = tempFile.delete();
		if (deleted) {
			tempTemp.renameTo(tempFile);
			return tempFile;
		}
		else {
			return tempTemp;
		}

	}

	//	/*
	//	 * force a fluch by sending a dummy document to th eimporter which will rail, but ...
	//	 */
	//	private void forceFlush() {
	//		TethysExportParams params = new TethysExportParams();
	//		String fileError = null;
	//		try {
	//		fileError = Importer.ImportFiles(params.getFullServerName(), "NoCollection",
	//				new String[] { "ThereIsNoFileE" }, "", "", false);
	//		}
	//		catch (Exception e) {
	//
	//		}
	////		System.out.println(fileError);
	//
	//	}

	/**
	 * Get a temp folder to hold xml output. This will be the standard
	 * temp folder + /PAMGuardTethys. Files will be left here until PAMGUard
	 * exits then should delete automatically
	 */
	public File checkTempFolder() {
		String javaTmpDirs = System.getProperty("java.io.tmpdir") + File.separator + "PAMGuardTethys";

		File tempDir = new File(javaTmpDirs);
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		if (tempDir.exists()) {
			tempDirectory = tempDir;
		}
		if (tempDirectory == null) {
			tempDirectory = new File(System.getProperty("java.io.tmpdir"));
		}
		return tempDirectory;
	}

	/**
	 * Get a document Id string. All Document objects should have a getId() function
	 * however they do not have a type hierarchy, so it can't be accessed directly.
	 * instead go via the class.getDeclaredMethod function and it should be possible to find
	 * it.
	 * @param nilusObject
	 * @return document Id for any type of document, or null if the document doesn't have a getID function
	 */
	private String getDocumentId(Object nilusObject) {
		String tempName = null;
		Class nilusClass = nilusObject.getClass();
		Method getId;
		try {
			getId = nilusClass.getDeclaredMethod("getId", null);
			Object[] inputs = new Object[0];
			Object res = getId.invoke(nilusObject, inputs);
			if (res instanceof String) {
				tempName = (String) res;
				return tempName;
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return tempName;
	}

	/**
	 * needs to be based on the document id,
	 * @param nilusObject
	 * @return
	 */
	private String getTempFileName(Object nilusObject) {

		String docId = getDocumentId(nilusObject);
		if (docId == null || docId.length() == 0) {
			docId = "PamguardTethys";
		}
		return docId;
	}




	public synchronized boolean openConnections() {
		TethysExportParams params = tethysControl.getTethysExportParams();
		currentSiteURL = params.getFullServerName();
		jerseyClient = new JerseyClient(currentSiteURL);
		queries = new Queries(jerseyClient);
		ServerStatus state = pingServer();

		setCache(false);

		return state.ok;
	}


	private void setCache(boolean cacheOn) {
		// from Marie. 4/4/2022: Basically it is a PUT to http://localhost:9979/Tethys/cache/off  (or on).
		TethysExportParams params = tethysControl.getTethysExportParams();

		String cmd = String.format("curl -X PUT -data \"\" %s/Tethys/cache/%s", params.getFullServerName(), cacheOn ? "on" : "off");
		System.out.println(cmd);
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		CUrl curl = new CUrl(cmd);

	}

	public synchronized void closeConnections() {
		jerseyClient = null;
		queries = null;
		currentSiteURL = null;
	}

	/**
	 * Get the server state via a ping ?
	 * @return Server state ?
	 */
	public ServerStatus pingServer() {

		boolean ok = false;
		try {
			ok = getJerseyClient().ping();
		}
		catch (Exception ex) {
			return new ServerStatus(false, ex);
		}
		return new ServerStatus(ok, null);
	}


	// add whatever calls are necessary ...

}
