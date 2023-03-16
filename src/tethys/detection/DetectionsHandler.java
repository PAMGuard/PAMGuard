package tethys.detection;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import nilus.DataSourceType;
import nilus.Deployment;
import nilus.Detection;
import nilus.DetectionEffort;
import nilus.DetectionEffortKind;
import nilus.DetectionGroup;
import nilus.Detections;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.detection.DetectionGranularity.GRANULARITY;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.TethysDataProvider;

public class DetectionsHandler {


	private TethysControl tethysControl;
	
	public int uniqueDetectionsId;
	public int uniqueDetectionId;
	
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

		return false;
	

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
		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider();
		Detections detections = new Detections();
		detections.setId(String.format("%d", uniqueDetectionsId++));
		detections.setDescription(dataProvider.getDescription(deployment, tethysExportParams));
		DataSourceType dataSource = new DataSourceType();
		dataSource.setDeploymentId(deployment.getId());
//		dataSource.setEnsembleId(""); ToDo
		detections.setDataSource(dataSource);
		detections.setAlgorithm(dataProvider.getAlgorithm());
		detections.setUserId("Unknown user");
		detections.setEffort(getDetectorEffort(deployment, startTimeMillis, endTimeMillis));
		DetectionGroup detectionGroup = new DetectionGroup();
		detections.setOnEffort(detectionGroup);
		List<Detection> detectionList = detectionGroup.getDetection();
		for (int i = 0; i < data.size(); i++) {
			PamDataUnit dataUnit = data.get(i);
			Detection detection = dataProvider.createDetection(dataUnit, tethysExportParams, streamExportParams);			
			if (detection != null) {
				detectionList.add(detection);
			}
		}
		System.out.printf("Exporting %d %s detections for time period %s to %s\n", detectionList.size(), dataBlock.getDataName(), 
				detections.getEffort().getStart().toString(), detections.getEffort().getEnd().toString());
		/*
		 * We should now have a fully populated Detections object, so write it to the database
		 * using functions in DBXMLConnect 
		 */
		ArrayList<Detections> detectionDocuments = new ArrayList();
		detectionDocuments.add(detections);
		
		tethysControl.getDbxmlConnect().postToTethys(detectionDocuments); // call whatever you need to call in here to write the Detections. 
		
		
		return true;
		
	}

//	private boolean exportByTimeChunk(PamDataBlock aDataBlock, Deployment deployment, long granularityIntervalSeconds,
//			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	private DetectionEffort getDetectorEffort(Deployment deployment, long effortStart, long effortEnd) {
		DetectionEffort effort = new DetectionEffort();
		effort.setStart(TethysTimeFuncs.xmlGregCalFromMillis(effortStart));
		effort.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(effortEnd));
//		effort.set // no setter for DetectionEffortKind
		List<DetectionEffortKind> effortKinds = effort.getKind();
		DetectionEffortKind kind = new DetectionEffortKind();
		try {
			nilus.Helper.createRequiredElements(kind);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		kind.getSpeciesId().setValue(BigInteger.valueOf(180537));
		kind.getGranularity().setValue(nilus.GranularityEnumType.CALL);
		
		effortKinds.add(kind);		
				 
		return effort;
	}

}
