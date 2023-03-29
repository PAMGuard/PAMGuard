package tethys.dbxml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import java.nio.file.Files;
import java.nio.file.Path;

import dbxml.JerseyClient;
import dbxml.Queries;
import dbxml.uploader.Importer;
import nilus.Deployment;
import nilus.MarshalXML;
import tethys.TethysControl;
import tethys.output.TethysExportParams;
import tethys.output.StreamExportParams;
import PamguardMVC.PamDataBlock;

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
		if (currentSiteURL.equalsIgnoreCase(params.getFullServerName()) == false) {
			return false;
		}
		return true;
	}
	
	/**
	 * Get the client. The client will only be recreated if the url changes
	 * @return Jersy client
	 */
	public synchronized  JerseyClient getJerseyClient() {
		if (checkClient() == false) {
			openConnections();
		}
		return jerseyClient;
	}
	
	/**
	 * Get the Queries object. This will only be recreated if the client changes. 
	 * @return
	 */
	public synchronized  Queries getTethysQueries() {
		if (checkClient() == false) {
			openConnections();
		}
		return queries;
	}
	
	/**
	 * Update a document within Tethys. We're assuming that a
	 * document with the same name in the same collection already
	 * exists. If it doesn't / has a different name, then use
	 * the removedocument function
	 * @param nilusDocument
	 * @return
	 */
	public String updateDocument(Object nilusDocument) {
		deleteDocument(nilusDocument);
		return postToTethys(nilusDocument);		
	}
	
	/**
	 * Delete a nilus document from the database. The only field which 
	 * needs to be populated here is the Id. The code also uses the object
	 * class to identify the correct collection. 
	 * @param nilusDocument
	 * @return
	 */
	public boolean deleteDocument(Object nilusDocument) {

		Class objClass = nilusDocument.getClass();
		String collection = getTethysCollection(objClass.getName());
		String docId = getDocumentId(nilusDocument);
		String result = null;
		try {
			result = jerseyClient.removeDocument(collection, docId );
			/**
			 * Return from a sucessful delete is something like
			 * 
				deployment = getTethysControl().getDeploymentHandler().createDeploymentDocument(freeId++, recordPeriod);
				<DELETE>
  <ITEM> ['ECoastNARW0'] </ITEM>
</DELETE>
			 */
		}
		catch (Exception e) {
			System.out.printf("Error deleting %s %s: %s\n", collection, docId, e.getMessage());	
		}
		return result == null;
	}
	
	/**
	 * take a nilus object loaded with PamGuard data and post it to the Tethys database
	 * 
	 * @param pamGuardObjs a nilus object loaded with PamGuard data
	 * @return error string, null string means there are no errors
	 */
	public String postToTethys(Object nilusObject) 
	{
		Class objClass = nilusObject.getClass();
		String collection = getTethysCollection(objClass.getName());
		TethysExportParams params = new TethysExportParams();
		String fileError = null;
		String tempName = getTempFileName(nilusObject);
		tempName = tempDirectory.getAbsolutePath() + File.separator + tempName + ".xml";
		File tempFile = new File(tempName);
		try {
			MarshalXML marshal = new MarshalXML();
			marshal.createInstance(objClass);	
//				Path tempFile = Files.createTempFile("pamGuardToTethys", ".xml");
				marshal.marshal(nilusObject, tempFile.toString());
				fileError = Importer.ImportFiles(params.getFullServerName(), collection,
						new String[] { tempFile.toString() }, "", "", false);

//				System.out.println(fileError);
				
				tempFile.deleteOnExit();
		} catch(IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return fileError;
	}
	
	/**
	 * Get a temp folder to hold xml output. This will be the standard 
	 * temp folder + /PAMGuardTethys. Files will be left here until PAMGUard
	 * exits then should delete automatically  
	 */
	private void checkTempFolder() {
			String javaTmpDirs = System.getProperty("java.io.tmpdir") + File.separator + "PAMGuardTethys";
			
			File tempDir = new File(javaTmpDirs);
			if (tempDir.exists() == false) {
				tempDir.mkdirs();
			}
			if (tempDir.exists()) {
				tempDirectory = tempDir;
			};
			if (tempDirectory == null) {
				tempDirectory = new File(System.getProperty("java.io.tmpdir"));
			}
		
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


	/**
	 * get Tethys collection name from nilus collection objects
	 * @param className nilus object Class Name
	 * @return name of Tethys collection
	 */
	public String getTethysCollection(String className) {
		switch(className) {
			case "nilus.Deployment": 
				return "Deployments";				
			case "nilus.Detections": 
				return "Detections";
			case "nilus.Calibration": 
				return "Calibrations";				
			case "nilus.Ensemble": 
				return "Ensembles";			
			case "nilus.Localization": 
				return "Localizations";				
			case "nilus.SpeciesAbbreviation": 
				return "SpeciesAbbreviations";				
			case "nilus.SourceMap": 
				return "SourceMaps";				
			case "nilus.ITIS": 
				return "ITIS";							
			case "nilus.ranks": 
				return "ITIS_ranks";
			default: 
				return "";									
		}
	}
	
	/**
	 * Delete a Deploymnet and any contained Detections document. Doesn't work !
	 * @param deploymentId
	 * @return
	 */
	public boolean deleteDeployment(String deploymentId) {
		ArrayList<String> detDocNames = tethysControl.getDbxmlQueries().getDetectionsDocsIds(deploymentId);
		JerseyClient jerseyClient = getJerseyClient();
		Queries queries = null;
		String result;
//		for (int i = 0; i < detDocNames.size(); i++) {
//			try {
//				System.out.println("Delete " + detDocNames.get(i));
//				result = jerseyClient.removeDocument("Detections", detDocNames.get(i));
//			}
//			catch (Exception e) {
//				e.printStackTrace();
////				return false;
////				break;
//			}
//		}
		try {
			String doc = queries.getDocument("Deployments", deploymentId);
//			queries.
			result = jerseyClient.removeDocument("Deployments", deploymentId );
		}
		catch (Exception e) {
//			e.printStackTrace();
			return false;
		}
		return true;
	}


	public synchronized boolean openConnections() {
		TethysExportParams params = tethysControl.getTethysExportParams();
		currentSiteURL = params.getFullServerName();
		jerseyClient = new JerseyClient(currentSiteURL);
		queries = new Queries(jerseyClient);
		ServerStatus state = pingServer();
		return state.ok;
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
