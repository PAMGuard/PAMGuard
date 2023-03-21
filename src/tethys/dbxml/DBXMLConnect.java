package tethys.dbxml;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import java.nio.file.Files;
import java.nio.file.Path;

import dbxml.JerseyClient;
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

	public DBXMLConnect(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
	}
	
	
	/**
	 * take list of nilus objects loaded with PamGuard data and post them to the Tethys database
	 * all objects must be of the same nilus object
	 * TethysExportParams obj used from UI inputs  
	 * 
	 * @param pamGuardObjs all nilus objects loaded with PamGuard data
	 * @return error string, null string means there are no errors
	 */
	public String postToTethys(List<?> pamGuardObjs) 
	{
		Class objClass = pamGuardObjs.get(0).getClass();
		String collection = getTethysCollection(objClass.getName());
		PamDataBlock defaultPamBlock = null;
		TethysExportParams params = new TethysExportParams();
		String fileError = null;
		try {
			MarshalXML marshal = new MarshalXML();
			marshal.createInstance(objClass);			
			for (Object obj : pamGuardObjs ) 
			{				
				Path tempFile = Files.createTempFile("pamGuardToTethys", ".xml");
				marshal.marshal(obj, tempFile.toString());
				fileError = Importer.ImportFiles(params.getFullServerName(), collection,
						new String[] { tempFile.toString() }, "", "", false);

//				System.out.println(fileError);
				
				tempFile.toFile().deleteOnExit();
			}
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
		JerseyClient jerseyClient = null;
		try {
			jerseyClient = new JerseyClient(tethysControl.getTethysExportParams().getFullServerName());
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		String result;
		for (int i = 0; i < detDocNames.size(); i++) {
			try {
				System.out.println("Delete " + detDocNames.get(i));
				result = jerseyClient.removeDocument("Detections", detDocNames.get(i));
			}
			catch (Exception e) {
				e.printStackTrace();
//				return false;
//				break;
			}
		}
		try {
			result = jerseyClient.removeDocument("Deployments", deploymentId);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public boolean openDatabase() {
		
		return true;
	}
	
	public void closeDatabase() {
		
	}

	/**
	 * Get the server state via a ping ? 
	 * @return Server state ? 
	 */
	public ServerStatus pingServer() {
		JerseyClient jerseyClient = new JerseyClient(tethysControl.getTethysExportParams().getFullServerName());
		boolean ok = false;
		try {
			ok = jerseyClient.ping();
		}
		catch (Exception ex) {
			return new ServerStatus(false, ex);
		}
		return new ServerStatus(ok, null);
	}
	
	// add whatever calls are necessary ... 

}
