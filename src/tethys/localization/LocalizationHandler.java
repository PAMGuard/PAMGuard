package tethys.localization;

import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import nilus.Localize;
import tethys.Collection;
import tethys.CollectionHandler;
import tethys.TethysControl;
import tethys.detection.StreamDetectionsSummary;
import tethys.niluswraps.NilusDataWrapper;
import tethys.niluswraps.PDeployment;

/**
 * Handler for localizations. This was originally going to mirror the DetectionsHandler, but became 
 * a bit redundant when it became clear that the localization export would have to happen in parralel
 * with the detections export, so all happens in DetectionsHandler. The DetectionsHandler will use
 * a LocalizationBuilder class to create each document, but will call through the an interface
 * in each Localiser algorithm (LocalizationCreator) to do the real work or sorting out coordinate
 * frames and creating the actual detections. 
 * @author dg50
 *
 */
public class LocalizationHandler extends CollectionHandler {

	private int uniqueLocalisationsId = 1;

	public LocalizationHandler(TethysControl tethysControl) {
		super(tethysControl, Collection.Localizations);
	}

	//	public LocalizationType getLoc() {
	//		LocalizationType lt = new LocalizationType();
	//		CylindricalCoordinateType cct = new CylindricalCoordinateType();
	////		cct.set
	//		CoordinateReferenceSystem cr;
	//		return null;
	//	}

	/**
	 * Get a list of Localization documents associated with a particular data block for all deployments
	 * documents. Group them by abstract or something
	 * @param dataBlock
	 * @return
	 */
	public StreamDetectionsSummary<NilusDataWrapper<PLocalization>> getStreamLocalizations(PamDataBlock dataBlock) {
		ArrayList<PDeployment> deployments = tethysControl.getDeploymentHandler().getMatchedDeployments();
		return getStreamLocalizations(dataBlock, deployments);
	}

	/**
	 * Get a list of Localization documents associated with a particular data block for the list of deployments
	 * documents. Group them by abstract or something
	 * @param dataBlock
	 * @param deployments can be null for all deployments.
	 * @return
	 */
	public StreamDetectionsSummary<NilusDataWrapper<PLocalization>> getStreamLocalizations(PamDataBlock dataBlock, ArrayList<PDeployment> deployments) {
		// get the basic data for each document including it's Description.

		ArrayList<PLocalization> localizeDocs = new ArrayList<>();
		for (PDeployment aDep : deployments) {
			ArrayList<String> someNames = tethysControl.getDbxmlQueries().getLocalizationDocuments(dataBlock, aDep.getDocumentId());
			if (someNames == null) {
				continue;
			}
			//			// no have a list of all the Detections documents of interest for this datablock.
			for (String aDoc : someNames) {
				Localize localize = tethysControl.getDbxmlQueries().getLocalizationDocInfo(aDoc);
				int count = tethysControl.getDbxmlQueries().countLocalizations2(aDoc);
				PLocalization pLocalize = new PLocalization(localize, dataBlock, aDep, count);
				localizeDocs.add(pLocalize);
				//				PDetections pDetections = new PDetections(detections, dataBlock, aDep, count);
				//				detectionsDocs.add(pDetections);
			}
		}
		return new StreamDetectionsSummary(localizeDocs);
	}

	@Override
	public String getHelpPoint() {
		return null;
	}

	public String getLocalisationdocId(String prefix) {
		/*
		 * Check the document name isn't already used and increment id as necessary.
		 */
		String fullId;
		while (true) {
			fullId = String.format("%s_%d", prefix, uniqueLocalisationsId++);
			if (!tethysControl.getDbxmlQueries().documentExists(Collection.Localizations.toString(), fullId)) {
				break;
			}
		}
		return fullId;
	}

}
