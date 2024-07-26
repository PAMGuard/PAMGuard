package tethys.localization;

import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import nilus.CylindricalCoordinateType;
import nilus.LocalizationType;
import nilus.Localize;
import nilus.Localize.Effort.CoordinateReferenceSystem;
import tethys.Collection;
import tethys.CollectionHandler;
import tethys.TethysControl;
import tethys.detection.StreamDetectionsSummary;
import tethys.niluswraps.NilusDataWrapper;
import tethys.niluswraps.PDeployment;

public class LocalizationHandler extends CollectionHandler {

	public LocalizationHandler(TethysControl tethysControl) {
		super(tethysControl, Collection.Localizations);
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
		return null;
	}

}
