package tethys.detection;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamguardVersionInfo;
import PamModel.PamPluginInterface;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.SuperDetDataBlock.ViewerLoadPolicy;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import nilus.AlgorithmType;
import nilus.AlgorithmType.SupportSoftware;
import nilus.DataSourceType;
import nilus.Deployment;
import nilus.Detection;
import nilus.DetectionEffort;
import nilus.DetectionEffortKind;
import nilus.DetectionGroup;
import nilus.Detections;
import nilus.GranularityEnumType;
import nilus.Helper;
import tethys.Collection;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.TethysException;
import tethys.deployment.DeploymentHandler;
import tethys.niluswraps.PDeployment;
import tethys.niluswraps.PDetections;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

/**
 * Functions for handling output of Detections documents. 
 * Works closely with a TethysDataProvider and DataBlockSpeciesManager
 * to generate Detections elements for an xml doc to export to Tethys. 
 * @author dg50
 *
 */
public class DetectionsHandler {

	private TethysControl tethysControl;

	public int uniqueDetectionsId=1;
	public int uniqueDetectionId;

	private volatile boolean activeExport;

	private ExportWorker exportWorker;

	/**
	 * 
	 * @param tethysControl
	 */
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
				PDetections pDetections = new PDetections(detections, dataBlock, aDep, count);
				detectionsDocs.add(pDetections);
			}
		}
		return new StreamDetectionsSummary(detectionsDocs);
	}

	/**
	 * Get the Detection Effort part of a Detections document
	 * @param pDeployment
	 * @param dataBlock
	 * @param exportParams
	 * @return
	 */
	private DetectionEffort getDetectorEffort(PDeployment pDeployment, PamDataBlock dataBlock, StreamExportParams exportParams) {
		DetectionEffort effort = new DetectionEffort();
		Deployment deployment = pDeployment.deployment;
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

	/**
	 * Detections will be exported in a separate worker thread since export may take some time and
	 * the user should be given ample opportunity to cancel it.
	 * @param pamDataBlock
	 * @param streamExportParams
	 * @param exportWorkerCard
	 */
	public void startExportThread(PamDataBlock pamDataBlock, StreamExportParams streamExportParams, DetectionExportObserver exportObserver) {

		checkGranularity(pamDataBlock, streamExportParams);
		tethysControl.getTethysExportParams().setStreamParams(pamDataBlock, streamExportParams);
		activeExport = true;
		exportWorker = new ExportWorker(pamDataBlock, streamExportParams, exportObserver);
		exportWorker.execute();
	}

	/**
	 * Fudge because some outputs don't show the granularity card, but need to 
	 * make sure that it's set to the correct only option ..
	 * @param pamDataBlock
	 * @param streamExportParams
	 */
	private void checkGranularity(PamDataBlock pamDataBlock, StreamExportParams streamExportParams) {
		if (streamExportParams == null) {
			return;
		}
		TethysDataProvider tethysProvider = pamDataBlock.getTethysDataProvider(tethysControl);
		if (tethysProvider == null) return;
		GranularityEnumType[] allowed = tethysProvider.getAllowedGranularities();
		if (allowed == null || allowed.length == 0) {
			return;
		}
		for (int i = 0; i < allowed.length; i++) {
			if (allowed[i] == streamExportParams.granularity) {
				return; // matches allowed value, so OK
			}
		}
		/*
		 *  if we get here, it's all wrong, so set to the first allowed value
		 *  which will be the only one if the card wasn't shown
		 */
		streamExportParams.granularity = allowed[0];
	}

	/**
	 * send a cancel command to export thread if it's running
	 */
	public void cancelExport() {
		activeExport = false;
	}
	
	/**
	 * Round a bin start so that it's aligned correctly with
	 * day starts. 
	 * @param binStart in milliseconds
	 * @param binInterval in milliseconds
	 * @return rounded time. 
	 */
	public static long roundDownBinStart(long binStart, long binInterval) {
		binStart/=binInterval;
		return binStart*binInterval;
	}

	/**
	 * Export detections in all deployments for this PAMGuard dataset.
	 * @param dataBlock
	 * @param streamExportParams
	 * @param exportObserver
	 * @return
	 */
	private int countDetections(PamDataBlock dataBlock, StreamExportParams streamExportParams, DetectionExportObserver exportObserver) {
		/*
		 * This is currently called for the entire dataset, but we will need to loop over specific Deployment documents
		 * and export the content of each separately.
		 */
		TethysExportParams exportParams = tethysControl.getTethysExportParams();
		DeploymentHandler depHandler = tethysControl.getDeploymentHandler();
		ArrayList<PDeployment> deployments = depHandler.getMatchedDeployments();
//		Detections currentDetections = null;
		OfflineDataMap dataMap = dataBlock.getPrimaryDataMap();
		DataSelector dataSelector = dataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
		int totalCount = dataMap.getDataCount();
		int skipCount = 0;
		int exportCount = 0;
		long lastUnitTime = 0;
		DetectionExportProgress prog;
		ViewerLoadPolicy viewerLoadPolicy = ViewerLoadPolicy.LOAD_UTCNORMAL;
		if (dataBlock instanceof SuperDetDataBlock) {
			SuperDetDataBlock superDataBlock = (SuperDetDataBlock) dataBlock;
			viewerLoadPolicy = superDataBlock.getViewerLoadPolicy();
		}
		if (viewerLoadPolicy == null) {
			viewerLoadPolicy = ViewerLoadPolicy.LOAD_UTCNORMAL;
		}
		GranularityHandler granularityHandler = GranularityHandler.getHandler(streamExportParams.granularity, tethysControl, dataBlock, exportParams, streamExportParams);
		for (PDeployment deployment : deployments) {
			int documentCount = 0;
			prog = new DetectionExportProgress(deployment, null,
					lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_COUNTING);
			exportObserver.update(prog);
			granularityHandler.prepare(deployment.getAudioStart());
			// export everything in that deployment.
			// need to loop through all map points in this interval.
			List<OfflineDataMapPoint> mapPoints = dataMap.getMapPoints();
			
			for (OfflineDataMapPoint mapPoint : mapPoints) {
				if (!activeExport) {
					prog = new DetectionExportProgress(deployment, null,
							lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_CANCELED);
					exportObserver.update(prog);
				}

				if (mapPoint.getEndTime() < deployment.getAudioStart()) {
					continue;
				}
				if (mapPoint.getStartTime() >= deployment.getAudioEnd()) {
					break;
				}
				dataBlock.loadViewerData(mapPoint.getStartTime(), mapPoint.getEndTime(), null);
				ArrayList<PamDataUnit> dataCopy = dataBlock.getDataCopy(deployment.getAudioStart(), deployment.getAudioEnd(), true, dataSelector);
//				System.out.printf("%d loaded from %s to %s %d kept\n", dataBlock.getUnitsCount(), PamCalendar.formatDateTime(mapPoint.getStartTime()),
//						PamCalendar.formatDateTime(mapPoint.getEndTime()), dataCopy.size());
				skipCount += dataBlock.getUnitsCount() - dataCopy.size();
				for (PamDataUnit dataUnit : dataCopy) {
					/*
					 * Here is where we need to handle the different granularities.
					 */
					Detection dets[] = granularityHandler.addDataUnit(dataUnit);
					if (dets != null) {
						exportCount+=dets.length;
						documentCount+=dets.length;
						
						if (exportCount % 100 == 0) {
							prog = new DetectionExportProgress(deployment, null,
									lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_COUNTING);
							exportObserver.update(prog);
						}
					}
//					Detection det = dataProvider.createDetection(dataUnit, exportParams, streamExportParams);
//					exportCount++;
//					documentCount++;
//					onEffort.getDetection().add(det);
					lastUnitTime = dataUnit.getTimeMilliseconds();
				}

				prog = new DetectionExportProgress(deployment, null,
						lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_COUNTING);
				exportObserver.update(prog);
				
				if (viewerLoadPolicy == ViewerLoadPolicy.LOAD_ALWAYS_EVERYTHING) {
					break;
				}

			}
			Detection dets[] = granularityHandler.cleanup(deployment.getAudioEnd());
			if (dets != null) {
				exportCount += dets.length;
			}
			


		}

		return exportCount;
	}/**
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
		DataSelector dataSelector = dataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
		int totalCount = dataMap.getDataCount();
		int skipCount = 0;
		int exportCount = 0;
		long lastUnitTime = 0;
		DetectionExportProgress prog;
		ViewerLoadPolicy viewerLoadPolicy = ViewerLoadPolicy.LOAD_UTCNORMAL;
		if (dataBlock instanceof SuperDetDataBlock) {
			SuperDetDataBlock superDataBlock = (SuperDetDataBlock) dataBlock;
			viewerLoadPolicy = superDataBlock.getViewerLoadPolicy();
		}
		if (viewerLoadPolicy == null) {
			viewerLoadPolicy = ViewerLoadPolicy.LOAD_UTCNORMAL;
		}
		GranularityHandler granularityHandler = GranularityHandler.getHandler(streamExportParams.granularity, tethysControl, dataBlock, exportParams, streamExportParams);
		for (PDeployment deployment : deployments) {
			int documentCount = 0;
			prog = new DetectionExportProgress(deployment, null,
					lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_COUNTING);
			exportObserver.update(prog);
			granularityHandler.prepare(deployment.getAudioStart());
			// export everything in that deployment.
			// need to loop through all map points in this interval.
			List<OfflineDataMapPoint> mapPoints = dataMap.getMapPoints();
			for (OfflineDataMapPoint mapPoint : mapPoints) {
				if (!activeExport) {
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
					/*
					 * Here is where we need to handle the different granularities.
					 */
					Detection dets[] = granularityHandler.addDataUnit(dataUnit);
					if (dets != null) {
						for (int dd = 0; dd < dets.length; dd++) {
							exportCount++;
							documentCount++;
							onEffort.getDetection().add(dets[dd]);
						}
					}
					if (exportCount % 100 == 0) {
						prog = new DetectionExportProgress(deployment, null,
								lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_GATHERING);
						exportObserver.update(prog);
					}
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
					try {
						dbxmlConnect.postAndLog(currentDetections);
					} catch (TethysException e) {
						tethysControl.showException(e);
					}
					currentDetections = null;
				}

				if (viewerLoadPolicy == ViewerLoadPolicy.LOAD_ALWAYS_EVERYTHING) {
					break;
				}
			}
			

			if (currentDetections != null) {
				Detection dets[] = granularityHandler.cleanup(deployment.getAudioEnd());
				if (dets != null) {
					for (int dd = 0; dd < dets.length; dd++) {
						exportCount++;
						documentCount++;
						currentDetections.getOnEffort().getDetection().add(dets[dd]);
					}
				}
				prog = new DetectionExportProgress(deployment, currentDetections,
						lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_WRITING);
				closeDetectionsDocument(currentDetections, deployment.getAudioEnd());
				try {
					dbxmlConnect.postAndLog(currentDetections);
				} catch (TethysException e) {
					tethysControl.showException(e);
				}
				currentDetections = null;
			}
		}

		prog = new DetectionExportProgress(null, null,
				lastUnitTime, totalCount, exportCount, skipCount, DetectionExportProgress.STATE_COMPLETE);
		exportObserver.update(prog);
		return DetectionExportProgress.STATE_COMPLETE;
	}
	
	/**
	 * Start a new detections document for the deployment and datablock. <br>
	 * Add all the standard information to the top of the Document
	 * @param deployment
	 * @param dataBlock
	 * @param exportParams
	 * @return new Detections document
	 */
	private Detections startDetectionsDocument(PDeployment deployment, PamDataBlock dataBlock,
			StreamExportParams exportParams) {
		Detections detections = new Detections();
		try {
			Helper.createRequiredElements(detections);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
			return null;
		}
		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider(tethysControl);

		String prefix = deployment.deployment.getId() + "_" + dataProvider.getDetectionsName();
		String fullId = "";
		/*
		 * Check the document name isn't already used and increment id as necessary.
		 */
		while (true) {
			fullId = String.format("%s_%d", prefix, uniqueDetectionsId++);
			if (!tethysControl.getDbxmlQueries().documentExists(Collection.Detections.toString(), fullId)) {
				break;
			}
		}
		detections.setId(fullId);
//		detections.setDescription(dataProvider.getDescription(deployment, tethysExportParams));
		detections.setDescription(exportParams.getNilusDetectionDescription());
		DataSourceType dataSource = new DataSourceType();
		dataSource.setDeploymentId(deployment.deployment.getId());
//		dataSource.setEnsembleId(""); ToDo
		detections.setDataSource(dataSource);
		AlgorithmType algorithm = detections.getAlgorithm();

		if (dataProvider != null) {
			algorithm = dataProvider.getAlgorithm();
//			detections.setAlgorithm(algorithm);
		}
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
		detections.setEffort(getDetectorEffort(deployment, dataBlock, exportParams));

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

	/**
	 * Worker thread for exporting detections. 
	 * Currently, it counts them first, then checks the user wants to export 
	 * This requires going through the data twice, but may be sensible to avoid
	 * people outputting stupidly large documents. 
	 * @author dg50
	 *
	 */
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
			Integer ans = null;
			try {
				int count = countDetections(dataBlock, exportParams, exportObserver);
				String msg = String.format("Do you want to go ahead and output %d %s detections to Tethys?", 
						count, exportParams.granularity);
				int doit = WarnOnce.showWarning("Tethys Detections Export", msg, WarnOnce.OK_CANCEL_OPTION);
				if (doit == WarnOnce.OK_OPTION) {
					ans = exportDetections(dataBlock, exportParams, this);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return ans;
		}

		@Override
		protected void done() {
//			this.
			DetectionExportProgress prog = new DetectionExportProgress(null, null, 0, 0, 0, 0, DetectionExportProgress.STATE_COMPLETE);
			tethysControl.exportedDetections(dataBlock);
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
