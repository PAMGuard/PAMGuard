package tethys.detection;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamController.PamControlledUnit;
import PamController.PamguardVersionInfo;
import PamModel.PamPluginInterface;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelector;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import metadata.deployment.DeploymentData;
import nilus.AlgorithmType;
import nilus.AlgorithmType.SupportSoftware;
import nilus.DataSourceType;
import nilus.Deployment;
import nilus.Detection;
import nilus.DetectionEffort;
import nilus.DetectionEffortKind;
import nilus.DetectionGroup;
import nilus.Detections;
import nilus.Helper;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.deployment.DeploymentHandler;
import tethys.TethysStateObserver;
import tethys.TethysTimeFuncs;
import tethys.dbxml.DBXMLConnect;
import tethys.detection.DetectionGranularity.GRANULARITY;
import tethys.niluswraps.PDeployment;
import tethys.niluswraps.PDetections;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.TethysDataPoint;
import tethys.pamdata.TethysDataProvider;
import tethys.swing.export.ExportWorkerCard;

public class DetectionsHandler {


	private TethysControl tethysControl;
	
	public int uniqueDetectionsId=1;
	public int uniqueDetectionId;

	private volatile boolean activeExport;

	private ExportWorker exportWorker;
	
	public DetectionsHandler(TethysControl tethysControl) {
		super();
		this.tethysControl = tethysControl;
	}
	
	
	/**
	 * Get a list of Detections documents associated with a particular data stream for 
	 * this data set (not the entire project). 
	 * @param dataBlock
	 */
	public StreamDetectionsSummary getStreamDetections(PamDataBlock dataBlock) {
		ArrayList<PDeployment> deployments = tethysControl.getDeploymentHandler().getMatchedDeployments();
		return getStreamDetections(dataBlock, deployments);
	}

	/**
	 * Get a list of Detections documents associated with a particular data block for the list of deployments
	 * documents. Group them by abstract or something 
	 * @param dataBlock
	 * @param deployments
	 * @return
	 */
	public StreamDetectionsSummary getStreamDetections(PamDataBlock dataBlock, ArrayList<PDeployment> deployments) {
		// get the basic data for each document including it's Description. 
		
		ArrayList<PDetections> detectionsDocs = new ArrayList<>();
		for (PDeployment aDep : deployments) {
			ArrayList<String> someNames = tethysControl.getDbxmlQueries().getDetectionsDocuments(dataBlock, aDep.deployment.getId());
			if (someNames == null) {
				continue;
			}
			// no have a list of all the Detections documents of interest for this datablock. 
			for (String aDoc : someNames) {
				Detections detections = tethysControl.getDbxmlQueries().getDetectionsDocInfo(aDoc);
				int count = tethysControl.getDbxmlQueries().countDetections2(aDoc);
				PDetections pDetections = new PDetections(detections, null, count);
				detectionsDocs.add(pDetections);
			}
		}
		return new StreamDetectionsSummary(detectionsDocs);
	}


//	/**
//	 * Here is where we export data for a specific data stream to Tethys.
//	 *
//	 * @param aDataBlock
//	 * @param aDeployment 
//	 * @param tethysExportParams
//	 * @param streamExportParams
//	 */
//	public boolean exportDetections(PamDataBlock aDataBlock, Deployment deployment, DetectionGranularity granularity, TethysExportParams tethysExportParams,
//			StreamExportParams streamExportParams) {
//		if (granularity == null || granularity.granularity == null) {
//			granularity = new DetectionGranularity(GRANULARITY.TIME, 3600);
//		}
//		switch (granularity.granularity) {
//		case BINARYFILE:
//			return exportByBinaryFile(aDataBlock, deployment, tethysExportParams, streamExportParams);
//		case NONE:
//			return exportEverything(aDataBlock, deployment, tethysExportParams, streamExportParams);
//		case TIME:
//			return exportByTimeChunk(aDataBlock, deployment, granularity.granularityIntervalSeconds, tethysExportParams, streamExportParams);
//		default:
//			break;
//		}
//
//		return false;
//	
//
//	}
//
//	private boolean exportByBinaryFile(PamDataBlock dataBlock, Deployment deployment,
//			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
//		long deploymentStart = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
//		long deploymentStop = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getAudioTimeStamp());
//		/*
//		 *  there should be a pretty good correspondence between the start of a binary file and the deploymentStart
//		 *  since they all derived from the same start clock. 
//		 */
//		OfflineDataMap dataMap = dataBlock.getPrimaryDataMap();
//		if (dataMap == null) {
//			return false;
//		}
//		List<OfflineDataMapPoint> mapPoints = dataMap.getMapPoints();
//		boolean ok = true;
//		for (OfflineDataMapPoint mapPoint : mapPoints) {
//			if (mapPoint.getEndTime() < deploymentStart) {
//				continue;
//			}
//			if (mapPoint.getStartTime() >= deploymentStop) {
//				continue;
//			}
//			ok &= loadAndExport(dataBlock, deployment, Math.max(deploymentStart, mapPoint.getStartTime()), 
//					Math.min(deploymentStop, mapPoint.getEndTime()), tethysExportParams, streamExportParams);
//		}
//		
//		
//		return ok;
//	}
//
//	private boolean exportEverything(PamDataBlock dataBlock, Deployment deployment,
//			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
//		long deploymentStart = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
//		long deploymentStop = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getAudioTimeStamp());
//		return loadAndExport(dataBlock, deployment, deploymentStart, deploymentStop, tethysExportParams, streamExportParams);
//	}
//
//	private boolean exportByTimeChunk(PamDataBlock dataBlock, Deployment deployment, long granularityIntervalSeconds,
//			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
//
//		long deploymentStart = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
//		long deploymentStop = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getAudioTimeStamp());
//		long chunkMillis = granularityIntervalSeconds*1000;
//		long exportStart = deploymentStart / chunkMillis;
//		exportStart *= chunkMillis;
//		boolean ok = true;
//		while (exportStart < deploymentStop) {
//			ok &= loadAndExport(dataBlock, deployment, Math.max(deploymentStart, exportStart), 
//					Math.min(deploymentStop, exportStart + chunkMillis), tethysExportParams, streamExportParams);
//			exportStart += chunkMillis;
//		}
//		
//		return ok;
//	}
//
///**
//	 * Load and export data for a given time period. This may be a complete deployment, it may be a short section. Do as told !
//	 * Hopefully data interval is small enough to hold all in memory - it needs to be if the document will fit in mempory, so should be OK
//	 * @param dataBlock
//	 * @param deployment
//	 * @param max
//	 * @param min
//	 * @param tethysExportParams
//	 * @param streamExportParams
//	 */
//	private boolean loadAndExport(PamDataBlock dataBlock, Deployment deployment, long startTimeMillis, long endTimeMillis,
//			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
//		// load the data
//		dataBlock.loadViewerData(startTimeMillis, endTimeMillis, null);
//		DataSelector dataSelector = dataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
//		/*
//		 *  for easier synching, get a copy of the data and also apply the data selector right away so that
//		 *  we've a list of exactly the right data.  
//		 */
//		ArrayList<PamDataUnit> data = dataBlock.getDataCopy(startTimeMillis, endTimeMillis, true, dataSelector);
//		/*
//		 * Here, make Detection object and add the DetectionEffort data. 
//		 */
//		DeploymentData globalDeplData = tethysControl.getGlobalDeplopymentData();
//		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider();
//		Detections detections = new Detections();
////		String prefix = getDetectionsDocIdPrefix(globalDeplData.getProject(), dataBlock);
//		String prefix = deployment.getId();
//		detections.setId(String.format("%s_%d", prefix, uniqueDetectionsId++));
//		detections.setDescription(dataProvider.getDescription(deployment, tethysExportParams));
//		DataSourceType dataSource = new DataSourceType();
//		dataSource.setDeploymentId(deployment.getId());
////		dataSource.setEnsembleId(""); ToDo
//		detections.setDataSource(dataSource);
//		detections.setAlgorithm(dataProvider.getAlgorithm());
//		detections.setUserId("Unknown user");
//		detections.setEffort(getDetectorEffort(deployment, startTimeMillis, endTimeMillis));
//		DetectionGroup detectionGroup = new DetectionGroup();
//		detections.setOnEffort(detectionGroup);
//		List<Detection> detectionList = detectionGroup.getDetection();
//		for (int i = 0; i < data.size(); i++) {
//			PamDataUnit dataUnit = data.get(i);
//			Detection detection = dataProvider.createDetection(dataUnit, tethysExportParams, streamExportParams);			
//			if (detection != null) {
//				detectionList.add(detection);
//			}
//		}
//		System.out.printf("Exporting %d %s detections for time period %s to %s\n", detectionList.size(), dataBlock.getDataName(), 
//				detections.getEffort().getStart().toString(), detections.getEffort().getEnd().toString());
//		/*
//		 * We should now have a fully populated Detections object, so write it to the database
//		 * using functions in DBXMLConnect 
//		 */
//		ArrayList<Detections> detectionDocuments = new ArrayList();
//		detectionDocuments.add(detections);
//		
////		tethysControl.getDbxmlConnect().postToTethys(detectionDocuments); // call whatever you need to call in here to write the Detections. 
//		
//		
//		return true;
//		
//	}

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
	
	public String getSupportSoftware(PamDataBlock dataBlock) {
		return "PAMGuard";
	}
	
	public String getSupportSoftwareVersion(PamDataBlock dataBlock) {
//		should try to dig into the binary store and get the version from there. 
		return PamguardVersionInfo.version;
	}
//	/**
//	 * Get a prefix for a id for a Detections document. This is just the project name 
//	 * and the datablock name. Something may need to be added to allow for multiple
//	 * analysis going into one database. 
//	 * @param project
//	 * @param dataBlock
//	 * @return Detections document prefix. 
//	 */
//	public static final String getDetectionsDocIdPrefix(String project, PamDataBlock dataBlock) {
//		return project + "_" + dataBlock.getDataName();
//	}

	/**
	 * Detections will be exported in a separate worker thread since export may take some time and 
	 * the user should be given ample opportunity to cancel it. 
	 * @param pamDataBlock 
	 * @param streamExportParams
	 * @param exportWorkerCard
	 */
	public void startExportThread(PamDataBlock pamDataBlock, StreamExportParams streamExportParams, DetectionExportObserver exportObserver) {
		tethysControl.getTethysExportParams().setStreamParams(pamDataBlock, streamExportParams);
		activeExport = true;
		exportWorker = new ExportWorker(pamDataBlock, streamExportParams, exportObserver);
		exportWorker.execute();
	}
	
	public void cancelExport() {
		activeExport = false;
	}
	
	/**
	 * Export detections in all deployments for this PAMGuard dataset. 
	 * @param dataBlock
	 * @param streamExportParams
	 * @param exportObserver
	 * @return
	 */
	private int exportDetections(PamDataBlock dataBlock, StreamExportParams streamExportParams, DetectionExportObserver exportObserver) {
		/*
		 * This is currently called for the entire dataset, but we will need to loop over specific Deployment documents 
		 * and export the content of each separately. 
		 */
		TethysExportParams exportParams = tethysControl.getTethysExportParams();
		DBXMLConnect dbxmlConnect = tethysControl.getDbxmlConnect();
		DeploymentHandler depHandler = tethysControl.getDeploymentHandler();
		ArrayList<PDeployment> deployments = depHandler.getMatchedDeployments();
		Detections currentDetections = null;
		OfflineDataMap dataMap = dataBlock.getPrimaryDataMap();
		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider();
		DataSelector dataSelector = dataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
		int totalCount = dataMap.getDataCount();
		int skipCount = 0;
		int exportCount = 0;
		long lastUnitTime = 0;
		DetectionExportProgress prog;
		for (PDeployment deployment : deployments) {
			int documentCount = 0;
			prog = new DetectionExportProgress(deployment, null, 
					lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_GATHERING);
			exportObserver.update(prog);
			// export everything in that deployment. 
			// need to loop through all map points in this interval. 
			List<OfflineDataMapPoint> mapPoints = dataMap.getMapPoints();
			for (OfflineDataMapPoint mapPoint : mapPoints) {
				if (activeExport == false) {
					prog = new DetectionExportProgress(deployment, currentDetections, 
							lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_CANCELED);
					exportObserver.update(prog);
				}
				
				if (currentDetections == null) {
					currentDetections = startDetectionsDocument(deployment, dataBlock, streamExportParams);
					currentDetections.getEffort().setStart(TethysTimeFuncs.xmlGregCalFromMillis(mapPoint.getStartTime()));
				}
				if (mapPoint.getEndTime() < deployment.getAudioStart()) {
					continue;
				}
				if (mapPoint.getStartTime() >= deployment.getAudioEnd()) {
					break;
				}
				dataBlock.loadViewerData(mapPoint.getStartTime(), mapPoint.getEndTime(), null);
				ArrayList<PamDataUnit> dataCopy = dataBlock.getDataCopy(deployment.getAudioStart(), deployment.getAudioEnd(), true, dataSelector);
				skipCount += dataBlock.getUnitsCount() - dataCopy.size();
				DetectionGroup onEffort = currentDetections.getOnEffort();
				for (PamDataUnit dataUnit : dataCopy) {
					Detection det = dataProvider.createDetection(dataUnit, exportParams, streamExportParams);
					exportCount++;
					documentCount++;
					onEffort.getDetection().add(det);
					lastUnitTime = dataUnit.getTimeMilliseconds();
				}
				
				prog = new DetectionExportProgress(deployment, currentDetections, 
						lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_GATHERING);
				exportObserver.update(prog);
				
				if (documentCount > 500000 && mapPoint != dataMap.getLastMapPoint()) {
					prog = new DetectionExportProgress(deployment, currentDetections, 
							lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_WRITING);
					exportObserver.update(prog);
					closeDetectionsDocument(currentDetections, mapPoint.getEndTime());
					dbxmlConnect.postToTethys(currentDetections);
					currentDetections = null;
				}
			}
			
			if (currentDetections != null) {
				prog = new DetectionExportProgress(deployment, currentDetections, 
						lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_WRITING);
				closeDetectionsDocument(currentDetections, deployment.getAudioEnd());
				dbxmlConnect.postToTethys(currentDetections);
				currentDetections = null;
			}
		}

		prog = new DetectionExportProgress(null, null, 
				lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_COMPLETE);
		exportObserver.update(prog);
		return DetectionExportProgress.STATE_COMPLETE;
	}
	private Detections startDetectionsDocument(PDeployment deployment, PamDataBlock dataBlock,
			StreamExportParams exportParams) {
		Detections detections = new Detections();
		try {
			Helper.createRequiredElements(detections);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
			return null;
		}

		String prefix = deployment.deployment.getId();
		detections.setId(String.format("%s_%d", prefix, uniqueDetectionsId++));
//		detections.setDescription(dataProvider.getDescription(deployment, tethysExportParams));
		detections.setDescription(exportParams.detectionDescription);
		DataSourceType dataSource = new DataSourceType();
		dataSource.setDeploymentId(deployment.deployment.getId());
//		dataSource.setEnsembleId(""); ToDo
		detections.setDataSource(dataSource);
		AlgorithmType algorithm = detections.getAlgorithm();
		algorithm.setMethod(getMethodString(dataBlock));
		algorithm.setSoftware(getSoftwareString(dataBlock));
		algorithm.setVersion(getVersionString(dataBlock));
		List<SupportSoftware> supSoft = algorithm.getSupportSoftware();
		SupportSoftware supportSoft = new SupportSoftware();
		supportSoft.setSoftware(getSupportSoftware(dataBlock));
		supportSoft.setVersion(getSupportSoftwareVersion(dataBlock));
		supSoft.add(supportSoft);
		detections.setAlgorithm(algorithm);
		detections.setUserId("Unknown user");
		detections.setEffort(getDetectorEffort(deployment.deployment, deployment.getAudioStart(), deployment.getAudioEnd()));
		
		return detections;
	}

	/**
	 * Close a detections document. This basically just means rewriting the end time and it's only
	 * important in the event that a document got too big and has to be restarted. 
	 * @param detections
	 * @param audioEnd
	 */
	private void closeDetectionsDocument(Detections detections, Long audioEnd) {
		detections.getEffort().setEnd(TethysTimeFuncs.xmlGregCalFromMillis(audioEnd));
	}

	private class ExportWorker extends SwingWorker<Integer, DetectionExportProgress> implements DetectionExportObserver {

		private PamDataBlock dataBlock;
		private StreamExportParams exportParams;
		private DetectionExportObserver exportObserver;
		
		public ExportWorker(PamDataBlock dataBlock, StreamExportParams exportParams,
				DetectionExportObserver exportObserver) {
			super();
			this.dataBlock = dataBlock;
			this.exportParams = exportParams;
			this.exportObserver = exportObserver;
		}
		
		public void publish(DetectionExportProgress exportProgress) {
			super.publish(exportProgress);
		}
		
		@Override
		protected Integer doInBackground() throws Exception {
			// eventually need to switch over the four granularity options here. 
			return exportDetections(dataBlock, exportParams, this);
		}

		@Override
		protected void done() {
//			this.
			DetectionExportProgress prog = new DetectionExportProgress(null, null, 0, 0, 0, 0, DetectionExportProgress.STATE_COMPLETE);
			exportObserver.update(prog);
		}

		@Override
		protected void process(List<DetectionExportProgress> chunks) {
			for (DetectionExportProgress prog : chunks) {
				exportObserver.update(prog);
			}
		}

		@Override
		public void update(DetectionExportProgress progress) {
			publish(progress);
		}
		
	}
}
