package tethys.detection;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import PamController.PamControlledUnit;
import PamController.PamSettings;
import PamController.settings.output.xml.PamguardXMLWriter;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import nilus.DataSourceType;
import nilus.Deployment;
import nilus.DetectionEffort;
import nilus.DetectionEffortKind;
import nilus.Detections;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.detection.DetectionGranularity.GRANULARITY;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.TethysDataProvider;
import tethys.pamdata.TethysSchema;

public class DetectionsHandler {


	private TethysControl tethysControl;
	
	public DetectionsHandler(TethysControl tethysControl) {
		super();
		this.tethysControl = tethysControl;
	}

	/**
	 * Here is where we export data for a specific data stream to Tethys.
	 *
	 * @param aDataBlock
	 * @param aDeployment 
	 * @param tethysExportParams
	 * @param streamExportParams
	 */
	public boolean exportDetections(PamDataBlock aDataBlock, Deployment deployment, DetectionGranularity granularity, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		if (granularity == null || granularity.granularity == null) {
			granularity = new DetectionGranularity(GRANULARITY.TIME, 3600);
		}
		switch (granularity.granularity) {
		case BINARYFILE:
			return exportByBinaryFile(aDataBlock, deployment, tethysExportParams, streamExportParams);
		case NONE:
			return exportEverything(aDataBlock, deployment, tethysExportParams, streamExportParams);
		case TIME:
			return exportByTimeChunk(aDataBlock, deployment, granularity.granularityIntervalSeconds, tethysExportParams, streamExportParams);
		default:
			break;
		
		}
		/**
		 * This will probably need to be passed additional parameters and may also want
		 * to return something other than void in order to build a bigger Tethys
		 * document.
		 */
		/*
		 * first we'll probably want a reference to the module containing the data. in
		 * principle this can't get null, since the datablock was found be searching in
		 * the other direction.
		 */
		PamControlledUnit pamControlledUnit = aDataBlock.getParentProcess().getPamControlledUnit();

		TethysDataProvider dataProvider = aDataBlock.getTethysDataProvider();

		Detections detections = new Detections();
		detections.setId(deployment.getId());
		detections.setDescription(dataProvider.getDescription(deployment, tethysExportParams));
		DataSourceType dataSource = new DataSourceType();
		dataSource.setDeploymentId(deployment.getId());
//		dataSource.setEnsembleId(""); ToDo
		detections.setDataSource(dataSource);
		detections.setAlgorithm(dataProvider.getAlgorithm());
		
		detections.setUserId("Unknown user");
		detections.setEffort(getDetectorEffort(deployment));
		
		return true;
	

	}

	private boolean exportByBinaryFile(PamDataBlock dataBlock, Deployment deployment,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		long deploymentStart = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
		long deploymentStop = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getAudioTimeStamp());
		/*
		 *  there should be a pretty good correspondence between the start of a binary file and the deploymentStart
		 *  since they all derived from the same start clock. 
		 */
		OfflineDataMap dataMap = dataBlock.getPrimaryDataMap();
		if (dataMap == null) {
			return false;
		}
		List<OfflineDataMapPoint> mapPoints = dataMap.getMapPoints();
		boolean ok = true;
		for (OfflineDataMapPoint mapPoint : mapPoints) {
			if (mapPoint.getEndTime() < deploymentStart) {
				continue;
			}
			if (mapPoint.getStartTime() >= deploymentStop) {
				continue;
			}
			ok &= loadAndExport(dataBlock, deployment, Math.max(deploymentStart, mapPoint.getStartTime()), 
					Math.min(deploymentStop, mapPoint.getEndTime()), tethysExportParams, streamExportParams);
		}
		
		
		return ok;
	}

	private boolean exportEverything(PamDataBlock dataBlock, Deployment deployment,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		long deploymentStart = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
		long deploymentStop = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getAudioTimeStamp());
		return loadAndExport(dataBlock, deployment, deploymentStart, deploymentStop, tethysExportParams, streamExportParams);
	}

	private boolean exportByTimeChunk(PamDataBlock dataBlock, Deployment deployment, long granularityIntervalSeconds,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {

		long deploymentStart = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
		long deploymentStop = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getAudioTimeStamp());
		long chunkMillis = granularityIntervalSeconds*1000;
		long exportStart = deploymentStart / chunkMillis;
		exportStart *= chunkMillis;
		boolean ok = true;
		while (exportStart < deploymentStop) {
			ok &= loadAndExport(dataBlock, deployment, Math.max(deploymentStart, exportStart), 
					Math.min(deploymentStop, exportStart + chunkMillis), tethysExportParams, streamExportParams);
			exportStart += chunkMillis;
		}
		
		return ok;
	}

/**
	 * Load and export data for a given time period. This may be a complete deployment, it may be a short section. Do as told !
	 * Hopefully data interval is small enough to hold all in memory - it needs to be if the document will fit in mempory, so should be OK
	 * @param dataBlock
	 * @param deployment
	 * @param max
	 * @param min
	 * @param tethysExportParams
	 * @param streamExportParams
	 */
	private boolean loadAndExport(PamDataBlock dataBlock, Deployment deployment, long startTimeMillis, long endTimeMillis,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		// load the data
		dataBlock.loadViewerData(startTimeMillis, endTimeMillis, null);
		DataSelector dataSelector = dataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
		/*
		 *  for easier synching, get a copy of the data and also apply the data selector right away so that
		 *  we've a list of exactly the right data.  
		 */
		ArrayList<PamDataUnit> data = dataBlock.getDataCopy(startTimeMillis, endTimeMillis, true, dataSelector);
		/*
		 * Here, make Detection object and add the DetectionEffort data. 
		 */
		for (int i = 0; i < data.size(); i++) {
			PamDataUnit dataUnit = data.get(i);
			// add many Detecion objects
		}
		
//		write to database
		
		
		
		return true;
		
	}

//	private boolean exportByTimeChunk(PamDataBlock aDataBlock, Deployment deployment, long granularityIntervalSeconds,
//			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	private DetectionEffort getDetectorEffort(Deployment deployment) {
		DetectionEffort effort = new DetectionEffort();
		effort.setStart(deployment.getDeploymentDetails().getAudioTimeStamp());
		effort.setEnd(deployment.getRecoveryDetails().getAudioTimeStamp());
//		effort.set // no setter for DetectionEffortKind
		List<DetectionEffortKind> effortKinds = effort.getKind();
		return effort;
	}

}
