package tethys.dbxml;

import java.util.ArrayList;
import java.util.HashMap;

import nilus.Deployment;
import tethys.Collection;
import tethys.DocumentInfo;
import tethys.TethysControl;
import tethys.niluswraps.PDeployment;

/**
 * Map of all documents, with links or labels of Documents associated with this project. 
 * @author dg50
 *
 */
public class DocumentMap {

	private HashMap<Collection, ArrayList<DocumentInfo>> allDocuments;
	private TethysControl tethysControl;
	private DBXMLQueries dbXmlQ;
	
	private enum MatchType {Tethys, PAMGuard}; 
	
	public DocumentMap(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
		allDocuments = new HashMap<>();
		dbXmlQ = tethysControl.getDbxmlQueries();
	}
	
	public synchronized void clearMap() {
		allDocuments.clear();
	}
	
	public synchronized boolean createMap() {
		
		if (tethysControl.isServerOk() == false) {
			return false;
		}
		long t0, t1, t2, t3, t4;
		t0 = System.currentTimeMillis();
		clearMap(); // perhaps not needed ?
		DBXMLQueries dbxmlQ = tethysControl.getDbxmlQueries();
		Collection[] collections = Collection.mainList();
		for (int i = 0; i < collections.length; i++) {
			ArrayList<DocumentInfo> set = dbxmlQ.getCollectionDocumentList(collections[i]);
			allDocuments.put(collections[i], set);
		}
		t1 = System.currentTimeMillis();
		// now link stuff up as much as possible
		// first find project deployments
		Deployment projData = tethysControl.getGlobalDeplopymentData();
		if (projData == null) {
			return false;
		}
		/**
		 * List of deployments associated with this Tethys Project name
		 */
		ArrayList<Deployment> projDeps = dbxmlQ.getProjectDeployments(projData.getProject());
		
		/**
		 * List of Deployments linked to this PAMGuard dataset.  
		 */
		ArrayList<PDeployment> dataDeployments = tethysControl.getDeploymentHandler().getProjectDeployments();

		t2 = System.currentTimeMillis();
		
		// then for each of those find associated calibration, Detection, and Localisation documents. 
		for (Deployment aDep : projDeps) {
			matchDeployments(aDep, MatchType.Tethys);
		}
		
		for (PDeployment pDep : dataDeployments) {
			matchDeployments(pDep.getNilusObject(), MatchType.PAMGuard);
		}

		t3 = System.currentTimeMillis();
		
		// now take the deployments that have either match and search for their other documents.
		ArrayList<DocumentInfo> deps = allDocuments.get(Collection.Deployments);
		if (deps == null) {
			return true;
		}
		// horrible many to many cross link. 
		for (DocumentInfo documentInfo : deps) { 
			if (documentInfo.isThisTethysProject() == false) {
				continue;
			}
			matchOthers(documentInfo);
		}
		t4 = System.currentTimeMillis();
		
		// play with this again when we're running on a server - could get painful!
//		System.out.printf("All docs map making time: %d, %d, %d, %d total %dms\n", t1-t0, t2-t1, t3-t2, t4-t3, t4-t0);
		
		return true;
	}

	private void matchDocuments(Deployment aDep, MatchType matchType) {
		matchDeployments(aDep, matchType);
	}

	private void matchDeployments(Deployment aDep, MatchType matchType) {
		/**
		 * Match deployments to this project, this doesn't require re-querying
		 * the database since the infos are in memory. 
		 */
		ArrayList<DocumentInfo> deps = allDocuments.get(Collection.Deployments);
		if (deps == null) {
			return;
		}
		// horrible many to many cross link. 
		for (DocumentInfo documentInfo : deps) {
			if (documentInfo.getDocumentId().equals(aDep.getId())) {
				// it's a match
				switch (matchType) {
				case PAMGuard:
					documentInfo.setThisPAMGuardDataSet(true);
					break;
				case Tethys:
					documentInfo.setThisTethysProject(true);
					break;
				default:
					break;
				}
			}
		}
	}

	private void matchOthers(DocumentInfo deploymentInfo) {
		/*
		 *  Deploymnets and localisations have a DeploymentId that matches back to this Id. But that's not held
		 *  within the documentInfo, so need to query tethys for this deployment 
		 */
		matchCollection(Collection.Detections, deploymentInfo);
		matchCollection(Collection.Localizations, deploymentInfo);
		matchCalibrations(deploymentInfo);
	
	}

	/**
	 * Work out which calibrations documents match this. 
	 * @param deploymentInfo
	 */
	private void matchCalibrations(DocumentInfo deploymentInfo) {
		// to do this, we need to get the sensor id out of the deployment document, then query for Cal documents that
		// have that sensor Id. 
		ArrayList<DocumentInfo> allCals = allDocuments.get(Collection.Calibrations);
		ArrayList<String> calDocs = dbXmlQ.getDeploymnetCalibrations(deploymentInfo.getDocumentId());
//		System.out.println(calDocs);
		// find the right doc
		for (String aCal : calDocs) {
			DocumentInfo foundInfo = findInfo(allCals, aCal);
			if (foundInfo == null) {
				System.out.println("Unable to find calibration document Id " + aCal);
			}
			else {
				foundInfo.setThisPAMGuardDataSet(deploymentInfo.isThisPAMGuardDataSet());
				foundInfo.setThisTethysProject(deploymentInfo.isThisTethysProject());
			}
		}
		
	}

	private void matchCollection(Collection collection, DocumentInfo deploymentInfo) {
		ArrayList<DocumentInfo> allDocs = allDocuments.get(collection);
		if (allDocs == null || allDocs.size() == 0) {
			return;
		}
		if (deploymentInfo.getDocumentId().startsWith("Shiant")) {
//			System.out.println("Shiant isles");
		}
		ArrayList<String> deplDocs = dbXmlQ.getDeploymentDocuments(collection, deploymentInfo.getDocumentId());
		// depldocs should be a subset of alldocs, so match them up and copy some info over 
		if (deplDocs == null) {
			return;
		}
		for (String aDocId : deplDocs) {
			DocumentInfo foundInfo = findInfo(allDocs, aDocId);
			if (foundInfo != null) {
				foundInfo.setThisPAMGuardDataSet(deploymentInfo.isThisPAMGuardDataSet());
				foundInfo.setThisTethysProject(deploymentInfo.isThisTethysProject());
			}
			else {
				System.out.printf("Unable to find %s document with Id %s\n", collection.toString(), aDocId);
			}
		}
	}
	
	/**
	 * find a matching document info
	 * @return
	 */
	private DocumentInfo findInfo(ArrayList<DocumentInfo> allInfos, String docId) {
		for (DocumentInfo aInfo : allInfos) {
			if (aInfo.getDocumentId().equals(docId)) {
				return aInfo;
			}
		}
		return null;
	}

	public ArrayList<DocumentInfo> getCollection(Collection collection) {
		if (allDocuments.isEmpty()) {
			createMap();
		}
		ArrayList<DocumentInfo> data = allDocuments.get(collection);
		
		return data;
	}

}
