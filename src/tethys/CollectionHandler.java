package tethys;

import java.util.List;

import PamController.PamControlledUnit;
import PamController.PamguardVersionInfo;
import PamModel.PamPluginInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import nilus.Deployment;
import nilus.DetectionEffort;
import nilus.DetectionEffortKind;
import tethys.niluswraps.PDeployment;
import tethys.output.StreamExportParams;
import tethys.pamdata.TethysDataProvider;

abstract public class CollectionHandler {

	private Collection collection;
	
	protected TethysControl tethysControl;

	/**
	 * @param tethysControl
	 * @param collection
	 */
	public CollectionHandler(TethysControl tethysControl, Collection collection) {
		this.tethysControl = tethysControl;
		this.collection = collection;
	}
	
	public String collectionName() {
		return collection.collectionName();
	}

	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return collection;
	}

	/**
	 * @return the tethysControl
	 */
	public TethysControl getTethysControl() {
		return tethysControl;
	}
	
	public abstract String getHelpPoint();
	
	/**
	 * Get the Detection Effort part of a Detections document
	 * @param pDeployment
	 * @param dataBlock
	 * @param exportParams
	 * @return
	 */
	public DetectionEffort getDetectorEffort(PDeployment pDeployment, PamDataBlock dataBlock, StreamExportParams exportParams) {
		DetectionEffort effort = new DetectionEffort();
		Deployment deployment = pDeployment.nilusObject;
		Long effortStart = pDeployment.getAudioStart();
		Long effortEnd = pDeployment.getAudioEnd();
		effort.setStart(TethysTimeFuncs.xmlGregCalFromMillis(effortStart));
		effort.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(effortEnd));
		//		effort.set // no setter for DetectionEffortKind
		List<DetectionEffortKind> effortKinds = effort.getKind();

		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		dataProvider.getEffortKinds(pDeployment, effortKinds, exportParams);


		return effort;
	}

	/**
	 * Method string for Detections Algorithm documents.
	 * @param dataBlock
	 * @return
	 */
	public String getMethodString(PamDataBlock dataBlock) {
		if (dataBlock == null) {
			return null;
		}
		PamProcess process = dataBlock.getParentProcess();
		return "PAMGuard " + process.getProcessName();

	}

	/**
	 * Software string for Detections Algorithm documents.
	 * @param dataBlock
	 * @return
	 */
	public String getSoftwareString(PamDataBlock dataBlock) {
		if (dataBlock == null) {
			return null;
		}
		return dataBlock.getLongDataName();
	}

	/**
	 * Software string for Detections Algorithm documents.
	 * @param dataBlock
	 * @return
	 */
	public String getVersionString(PamDataBlock dataBlock) {
		if (dataBlock == null) {
			return null;
		}
		PamProcess process = dataBlock.getParentProcess();
		PamControlledUnit pcu = process.getPamControlledUnit();
		PamPluginInterface plugin = pcu.getPlugin();
		if (plugin == null) {
			return PamguardVersionInfo.version;
		}
		else {
			return plugin.getVersion();
		}
	}

	/**
	 * 
	 * @param dataBlock
	 * @return default value is PAMGuard
	 */
	public String getSupportSoftware(PamDataBlock dataBlock) {
		return "PAMGuard";
	}

	/**
	 * 
	 * @param dataBlock
	 * @return PAMGuard version
	 */
	public String getSupportSoftwareVersion(PamDataBlock dataBlock) {
		//		should try to dig into the binary store and get the version from there.
		return PamguardVersionInfo.version;
	}
	
}
