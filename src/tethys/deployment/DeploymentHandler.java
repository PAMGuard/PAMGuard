package tethys.deployment;

import java.awt.Window;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.beanutils.converters.BigIntegerConverter;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.DaqStatusDataUnit;
import Acquisition.DaqSystem;
import Acquisition.FolderInputSystem;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.HydrophoneLocator;
import Array.PamArray;
import Array.Streamer;
import Array.ThreadingHydrophoneLocator;
import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamSensor;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import binaryFileStorage.BinaryStore;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import generalDatabase.DBControlUnit;
import metadata.MetaDataContol;
import metadata.PamguardMetaData;
import nilus.AcousticDataQAType;
import nilus.AcousticDataQAType.Quality;
import nilus.AcousticDataQAType.Quality.FrequencyRange;
import nilus.Audio;
import nilus.ChannelInfo;
import nilus.ChannelInfo.DutyCycle;
import nilus.ChannelInfo.DutyCycle.Regimen.RecordingDurationS;
import nilus.ChannelInfo.DutyCycle.Regimen.RecordingIntervalS;
import nilus.ChannelInfo.Sampling;
import nilus.ChannelInfo.Sampling.Regimen;
import nilus.Deployment;
import nilus.Deployment.Data;
import nilus.Deployment.Data.Tracks;
import nilus.Deployment.Data.Tracks.Track;
import nilus.Deployment.Data.Tracks.Track.Point;
import nilus.Deployment.Data.Tracks.Track.Point.BearingDegN;
import nilus.Deployment.Instrument;
import nilus.Deployment.SamplingDetails;
import nilus.Deployment.Sensors;
import nilus.DeploymentRecoveryDetails;
import nilus.DescriptionType;
import nilus.GeometryTypeM;
import nilus.Helper;
import nilus.MetadataInfo;
import nilus.UnknownSensor;
import pamMaths.PamVector;
import pamMaths.STD;
import tethys.Collection;
import tethys.CollectionHandler;
import tethys.TethysControl;
import tethys.TethysLocationFuncs;
import tethys.TethysState;
import tethys.TethysStateObserver;
import tethys.TethysTimeFuncs;
import tethys.calibration.CalibrationHandler;
import tethys.TethysState.StateType;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.TethysException;
import tethys.deployment.swing.DeploymentWizard;
import tethys.deployment.swing.EffortProblemDialog;
import tethys.deployment.swing.RecordingGapDialog;
import tethys.niluswraps.PDeployment;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;
import tethys.reporter.TethysReporter;
import tethys.swing.DeploymentTableObserver;

/**
 * Functions to gather data for the deployment document from all around PAMGuard.
 * There should be just one of these, available from TethysControl and it will try 
 * to sensible handle when and how it updates it's list of PAMGuard and Tethys information
 * <br> Any part of PAMGuard wanting information on Deployments should come here. 
 * @author dg50
 *
 */
public class DeploymentHandler extends CollectionHandler implements TethysStateObserver, DeploymentTableObserver {
	
//	private TethysControl tethysControl;
	
	private EffortFunctions effortFunctions;

	private DeploymentOverview deploymentOverview;
	
	private ArrayList<PDeployment> projectDeployments;

	private Helper nilusHelper;
	
	private DeploymentExportOpts deploymentExportOptions = new DeploymentExportOpts();

	public static final String helpPoint = "utilities.tethys.docs.deployments";

	public DeploymentHandler(TethysControl tethysControl) {
		
		super(tethysControl, Collection.Deployments);
		
		this.tethysControl = tethysControl;
		
		this.effortFunctions = new EffortFunctions(tethysControl);
		
		tethysControl.addStateObserver(this);		
		try {
			nilusHelper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	
		PamSettingManager.getInstance().registerSettings(new SettingsHandler());
	}
	
	/**
	 * Gather up all track information both from the GPS module (if it exists) and 
	 * the type of hydrophone array (or many!)
	 * @return
	 */
	public TrackInformation getTrackInformation() {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int nStreamers = array.getStreamerCount();
		HydrophoneLocator locator = null;
		for (int i = 0; i < nStreamers; i++) {
			Streamer aStreamer = array.getStreamer(i);
			locator = aStreamer.getHydrophoneLocator();
//			locator.getLocatorSettings().
		}
		// try to find a GPS datablock and see what's in it's datamap.
		OfflineDataMap gpsDataMap = null;
		GPSControl gpsControl = (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.gpsUnitType);
		if (gpsControl != null) {
			GPSDataBlock gpsDataBlock = gpsControl.getGpsDataBlock();
			gpsDataMap = gpsDataBlock.getPrimaryDataMap();
		}
		TrackInformation trackInformation = new TrackInformation(gpsDataMap, locator);
		return trackInformation;
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch (tethysState.stateType) {
		case NEWPROJECTSELECTION:
			updateProjectDeployments();
			break;
		case EXPORTRDATA:
		case DELETEDATA:
			updateProjectDeployments();
			break;
		case UPDATESERVER:
			updateProjectDeployments();
			break;
		default:
			break;
		}
	}

	/**
	 * Update the list of Tethys deployments
	 * @return true if OK
	 */
	public boolean updateProjectDeployments() {
		Deployment projData = tethysControl.getGlobalDeplopymentData();
		ArrayList<Deployment> tethysDocs = tethysControl.getDbxmlQueries().getProjectDeployments(projData.getProject(), getInstrumentId());
		if (tethysDocs == null) {
			return false;
		}
		projectDeployments = new ArrayList<>();
		for (Deployment deployment : tethysDocs) {
			projectDeployments.add(new PDeployment(deployment));
		}
		matchPamguard2Tethys(deploymentOverview, projectDeployments);
		tethysControl.sendStateUpdate(new TethysState(TethysState.StateType.NEWPAMGUARDSELECTION));
		return true;
	}
	
	/**
	 * Get a list of Tethys deployment docs. Note that this 
	 * doesn't update the list, but uses the one currently in memory
	 * so call updateTethysDeployments() first if necessary.
	 * @return list of (wrapped) nilus Deployment objects. 
	 */
	public ArrayList<PDeployment> getProjectDeployments() {
		if (projectDeployments == null) {
			updateProjectDeployments();
		}
		return projectDeployments;
	}
	
//	/**
//	 * Get an overview of all the deployments.
//	 * @return
//	 */
//	public DeploymentOverview createPamguardOverview() {
//		// first find an acquisition module.
//		PamControlledUnit aModule = PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
//		if (!(aModule instanceof AcquisitionControl)) {
//			// will return if it's null. Impossible for it to be the wrong type.
//			// but it's good practice to check anyway before casting.
//			return null;
//		}
//		// cast it to the right type.
//		AcquisitionControl daqControl = (AcquisitionControl) aModule;
//		AcquisitionParameters daqParams = daqControl.getAcquisitionParameters();
//		/**
//		 * The daqParams class has most of what we need about the set up in terms of sample rate,
//		 * number of channels, instrument type, ADC input range (part of calibration), etc.
//		 * It also has a hydrophone list, which maps the input channel numbers to the hydrophon numbers.
//		 * Realistically, this list is always 0,1,2,etc or it goes horribly wrong !
//		 */
//		// so write functions here to get information from the daqParams.
////		System.out.printf("Sample regime: %s input with rate %3.1fHz, %d channels, gain %3.1fdB, ADCp-p %3.1fV\n", daqParams.getDaqSystemType(),
////				daqParams.getSampleRate(), daqParams.getNChannels(), daqParams.preamplifier.getGain(), daqParams.voltsPeak2Peak);
//		/**
//		 * then there is the actual sampling. This is a bit harder to find. I thought it would be in the data map
//		 * but the datamap is a simple count of what's in the databasase which is not quite what we want.
//		 * we're going to have to query the database to get more detailed informatoin I think.
//		 * I'll do that here for now, but we may want to move this when we better organise the code.
//		 * It also seems that there are 'bad' dates in the database when it starts new files, which are the date
//		 * data were analysed at. So we really need to check the start and stop records only.
//		 */
//		PamDataBlock<DaqStatusDataUnit> daqInfoDataBlock = daqControl.getAcquisitionProcess().getDaqStatusDataBlock();
//		// just load everything. Probably OK for the acqusition, but will bring down
//		daqInfoDataBlock.loadViewerData(0, Long.MAX_VALUE, null);
//		ArrayList<DaqStatusDataUnit> allStatusData = daqInfoDataBlock.getDataCopy();
//		/**
//		 * Due to seird file overlaps we need to resort this by id if we can.
//		 * 
//		 */
//		Collections.sort(allStatusData, new Comparator<DaqStatusDataUnit>() {
//
//			@Override
//			public int compare(DaqStatusDataUnit o1, DaqStatusDataUnit o2) {
//				if (o1.getDatabaseIndex() == 0) {
//					return (int) (o1.getTimeMilliseconds()-o2.getTimeMilliseconds());
//				}
//				return o1.getDatabaseIndex()-o2.getDatabaseIndex();
//			}
//		});
//
//		ArrayList<RecordingPeriod> tempPeriods = null;
//
//		if (allStatusData == null || allStatusData.size() == 0) {
//			System.out.println("Data appear to have no logged recording periods. Try to extract from raw audio ...");
//			tempPeriods = extractTimesFromFiles(daqControl);
//		}
//		else {
//			tempPeriods = extractTimesFromStatus(allStatusData);
//		}
//		if (tempPeriods == null || tempPeriods.size() == 0) {
//			System.out.println("Data appear to have no logged recording periods available either from the database or the raw recordings.");
//			tempPeriods = extractTimesFromOutputMaps();
//		}
//		if (tempPeriods == null || tempPeriods.size() == 0) {
//			System.out.println("Data appear to have no logged recording periods available either from the database or the raw recordings.");
//			return null;
//		}
//
//		int nPeriods = tempPeriods.size();
////		int i = 0;
////		for (RecordingPeriod aP : tempPeriods) {
////			System.out.printf("Pre merge %d : %s to %s\n", i++, PamCalendar.formatDBDateTime(aP.getRecordStart()), 
////					PamCalendar.formatDBDateTime(aP.getRecordStop()));
////		}
//		// now go through those and merge into longer periods where there is no gap between files.
//		ListIterator<RecordingPeriod> iterator = tempPeriods.listIterator();
//		RecordingPeriod prevPeriod = null;
//		while (iterator.hasNext()) {
//			RecordingPeriod nextPeriod = iterator.next();
//			long nextDur = nextPeriod.getRecordStop()-nextPeriod.getRecordStart();
//			if (nextDur == 0) {
//				continue;
//			}
//			if (prevPeriod != null) {
//				long gap = nextPeriod.getRecordStart() - prevPeriod.getRecordStop();
//				long prevDur = prevPeriod.getRecordStop()-prevPeriod.getRecordStart();
//				if (gap < exportOptions.maxGapSeconds*1000) {
//					// ignoring up to 3s gap or a sample error < 2%.Dunno if this is sensible or not.
//					prevPeriod.setRecordStop(nextPeriod.getRecordStop());
//					iterator.remove();
//					nextPeriod = prevPeriod;
//				}
//			}
//			prevPeriod = nextPeriod;
//		}
//		// now remove ones which are too short even after merging. 
//		iterator = tempPeriods.listIterator();
//		while (iterator.hasNext()) {
//			RecordingPeriod nextPeriod = iterator.next();
//			long duration = nextPeriod.getDuration();
//			if (duration < exportOptions.minLengthSeconds*1000L) {
//				iterator.remove();
//			}
//		}
////		i = 0;
////		for (RecordingPeriod aP : tempPeriods) {
////			System.out.printf("Post merge %d : %s to %s\n", i++, PamCalendar.formatDBDateTime(aP.getRecordStart()), 
////					PamCalendar.formatDBDateTime(aP.getRecordStop()));
////		}
////		System.out.printf("Data have %d distinct files, but only %d distinct recording periods\n", nPeriods, tempPeriods.size());
//		DutyCycleInfo dutyCycleinfo = assessDutyCycle(tempPeriods);
//		// if it's duty cycles, then we only want a single entry. 
//		ArrayList<RecordingPeriod> deploymentPeriods;
//		if (dutyCycleinfo.isDutyCycled == false) {
//			deploymentPeriods = tempPeriods;
//		}
//		else {
//			deploymentPeriods = new ArrayList<>();
//			deploymentPeriods.add(new RecordingPeriod(tempPeriods.get(0).getRecordStart(), tempPeriods.get(tempPeriods.size()-1).getRecordStop()));
//		}
//		/*
//		 * do another sort of the deploymentPeriods. The start stops were in the order they went into the 
//		 * database in the hope that pairs were the right way round. Now check all data are/
//		 */
//		Collections.sort(deploymentPeriods, new Comparator<RecordingPeriod>() {
//			@Override
//			public int compare(RecordingPeriod o1, RecordingPeriod o2) {
//				return (int) (o1.getRecordStart()-o2.getRecordStart());
//			}
//		});
//		
//		DeploymentOverview deploymentOverview = new DeploymentOverview(dutyCycleinfo, deploymentPeriods);
//		matchPamguard2Tethys(deploymentOverview, projectDeployments);
//		this.deploymentOverview = deploymentOverview;
//		return deploymentOverview;
//		// find the number of times it started and stopped ....
////		System.out.printf("Input map of sound data indicates data from %s to %s with %d starts and %d stops over %d files\n",
////				PamCalendar.formatDateTime(dataStart), PamCalendar.formatDateTime(dataEnd), nStart, nStop, nFile+1);
//		// now work out where there are genuine gaps and make up a revised list of recording periods.
//
//
//	}
	
	public void showOptions(Window parent) {
		if (parent == null) {
			parent = tethysControl.getGuiFrame();
		}
		DeploymentExportOpts newOpts = RecordingGapDialog.showDiloag(parent, deploymentExportOptions);
		if (newOpts != null) {
			deploymentExportOptions = newOpts;
			createPamguardOverview();
		}
	}

	public void createPamguardOverview() {
		deploymentOverview = effortFunctions.makeRecordingOverview();
		
		checkDeploymentOverview(deploymentOverview);
		
		updateProjectDeployments();
		matchPamguard2Tethys(deploymentOverview, projectDeployments);
		
		tethysControl.sendStateUpdate(new TethysState(StateType.NEWPAMGUARDSELECTION));
	}

	/**
	 * Check the deployment overview for consistency.<br>
	 * Take the raw audio information and the binary information and check they are similar.
	 * if not, ask the user what to do. 
	 * @param deploymentOverview
	 */
	private void checkDeploymentOverview(DeploymentOverview overview) {
		RecordingList rawList = overview.getRawDataList();
		RecordingList binList = overview.getBinaryDataList();
		if (rawList == null || binList == null) {
			return; // nothing to do
		}
		double similarity = rawList.getSimilarity(binList);
		if (similarity > 0.95) {
			return;
		}
		/*
		 *  if we get here, it seems like the two lists are very different, so
		 *  show a dialog to ask the user what to do. 
		 */
		RecordingList selList = EffortProblemDialog.showDialog(tethysControl.getGuiFrame(), overview);
		if (selList != null) {
			tethysControl.getTethysExportParams().setEffortSourceName(selList.getSourceName());
		}
	}

	/**
	 * Export button pressed on GUI. Run wizard....
	 */
	public void exportDeployments() {
		Deployment deployment = MetaDataContol.getMetaDataControl().getMetaData().getDeployment();
		DeploymentExportOpts exportOptions = DeploymentWizard.showWizard(getTethysControl().getGuiFrame(), tethysControl, deployment, this.deploymentExportOptions);
		if (exportOptions != null) {
			this.deploymentExportOptions = exportOptions;
			deploymentOverview = getDeploymentOverview();
			RecordingList allPeriods = deploymentOverview.getMasterList(getTethysControl());
			exportDeployments(allPeriods);
		}
	}

	/**
	 * Export deployments docs. Playing with a couple of different ways of doing this. 
	 * @param allPeriods
	 */
	public void exportDeployments(RecordingList allPeriods) {
		TethysReporter.getTethysReporter().clear();
		if (deploymentExportOptions.separateDeployments) {
			exportSeparateDeployments(allPeriods);
		}
		else {
			exportOneDeploymnet(allPeriods);
		}
		TethysReporter.getTethysReporter().showReport(tethysControl.getGuiFrame(), true);
	}
	
	/**
	 * Make one big deployment document with all the recording periods in it. 
	 */
	private void exportOneDeploymnet(RecordingList recordingList) {
		// do the lot, whatever ...
		Float sampleRate = null;
		AcquisitionControl daq = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daq != null) {
			DaqSystem system = daq.findDaqSystem(null);
			AcquisitionParameters daqParams = daq.acquisitionParameters;
			sampleRate = daqParams.sampleRate;
		}
		
		int freeId = getTethysControl().getDeploymentHandler().getFirstFreeDeploymentId();
		RecordingPeriod onePeriod = new RecordingPeriod(recordingList.getStart(), 
				recordingList.getEnd());
		TethysExportParams exportParams = tethysControl.getTethysExportParams();
		String id = String.format("%s_%s", exportParams.getDatasetName(), "all");
		Deployment deployment = createDeploymentDocument(freeId, onePeriod, id);
		// fill in a few things from here
		Deployment globalMeta = getTethysControl().getGlobalDeplopymentData();
		deployment.setCruise(globalMeta.getCruise());
		deployment.setSite(globalMeta.getSite());
		ArrayList<RecordingPeriod> effortPeriods = recordingList.getEffortPeriods();
		if (recordingList.size() > 1) {
//			// now need to remove the sampling details - don't though, add invalid periods instead. 
//			SamplingDetails samplingDetails = deployment.getSamplingDetails();
//			samplingDetails.getChannel().clear();
//			for (int i = 0; i < selectedDeployments.size(); i++) {
//				addSamplingDetails(deployment, selectedDeployments.get(i));
//			}
			/*
			 * Instead, we're putting invalid periods into the QA section. 
			 */
			AcousticDataQAType qa = deployment.getQualityAssurance();
			if (qa == null) {
				deployment.setQualityAssurance(qa = new AcousticDataQAType());
			}
			List<Quality> qualityList = qa.getQuality();
			for (int i = 1; i < recordingList.size(); i++) {
				long end = effortPeriods.get(i-1).getRecordStop();
				long start = effortPeriods.get(i).getRecordStart();
				Quality q = new Quality();
				q.setStart(TethysTimeFuncs.xmlGregCalFromMillis(end));
				q.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(start));
				q.setCategory("unusable");
				if (sampleRate != null) {
					FrequencyRange f = q.getFrequencyRange();
					if (f == null) {
						q.setFrequencyRange(f = new FrequencyRange());
					}
					f.setLowHz(0);
					f.setHighHz(sampleRate/2);
				}
				q.setComment("No data (probably off or out of water)");
				qualityList.add(q);
			}
		}
		DBXMLConnect dbxmlConnect = getTethysControl().getDbxmlConnect();
		PDeployment exDeploymnet = onePeriod.getMatchedTethysDeployment();
		try {
			if (exDeploymnet != null) {
				deployment.setId(exDeploymnet.nilusObject.getId());
				dbxmlConnect.updateDocument(deployment);
			}
			else {
				dbxmlConnect.postAndLog(deployment);
			}
		}
		catch (TethysException e) {
			getTethysControl().showException(e);
		}
		getTethysControl().sendStateUpdate(new TethysState(StateType.UPDATESERVER, Collection.Deployments));
	}
	
	/**
	 * Make a separate deployment document for every recording period. 
	 */
	private void exportSeparateDeployments(RecordingList recordingList) {
		
		int freeId = getTethysControl().getDeploymentHandler().getFirstFreeDeploymentId();
		// fill in a few things from here
		Deployment globalMeta = getTethysControl().getGlobalDeplopymentData();
		TethysExportParams exportParams = tethysControl.getTethysExportParams();
		ArrayList<RecordingPeriod> effortPeriods = recordingList.getEffortPeriods();
		for (int i = 0; i < recordingList.size(); i++) {
			RecordingPeriod recordPeriod = effortPeriods.get(i);
			PDeployment exDeploymnet = recordPeriod.getMatchedTethysDeployment();
			Deployment deployment = null;
			String id = String.format("%s_%d", exportParams.getDatasetName(), i);
			if (exDeploymnet != null) {
				deployment = createDeploymentDocument(freeId, recordPeriod, id);
				deployment.setId(exDeploymnet.nilusObject.getId());
			}
			if (deployment == null) {
				deployment = createDeploymentDocument(freeId++, recordPeriod, id);
			}
			deployment.setCruise(globalMeta.getCruise());
			deployment.setSite(globalMeta.getSite());
			// also need to sort out track data here, etc.
			DBXMLConnect dbxmlConnect = getTethysControl().getDbxmlConnect();
			try {
				if (exDeploymnet != null) {
					dbxmlConnect.updateDocument(deployment);
				}
				else {
					dbxmlConnect.postAndLog(deployment);
				}
			}
			catch (TethysException e) {
				getTethysControl().showException(e);
			}
		}
		getTethysControl().sendStateUpdate(new TethysState(StateType.UPDATESERVER, Collection.Deployments));
	}

	

	public DeploymentOverview getDeploymentOverview() {
		return deploymentOverview;
	}

	/**
	 * Match what we think the PAMGuard deployment times are with Tethys Deployments read back 
	 * from the database. 
	 * @param deploymentOverview
	 * @param deployments
	 */
	private void matchPamguard2Tethys(DeploymentOverview deploymentOverview, ArrayList<PDeployment> deployments) {
		if (deployments == null || deploymentOverview == null) {
			return;
		}
		RecordingList recordingList = deploymentOverview.getMasterList(getTethysControl());
		ArrayList<RecordingPeriod> effortPeriods = recordingList.getEffortPeriods();
		for (RecordingPeriod aPeriod : effortPeriods) {
			PDeployment closestDeployment = findClosestDeployment(aPeriod, deployments);
			aPeriod.setMatchedTethysDeployment(closestDeployment);
			if (closestDeployment != null) {
				closestDeployment.setMatchedPAMGaurdPeriod(aPeriod);
			}
		}
	}

	/**
	 * find the Tethys deployment that most closely matches the PAMGuard recording period. 
	 * @param aPeriod
	 * @param deployments
	 * @return
	 */
	private PDeployment findClosestDeployment(RecordingPeriod aPeriod, ArrayList<PDeployment> deployments) {
		double overlap = -1;
		PDeployment bestDeployment = null;
		for (PDeployment aDeployment : deployments) {
			double newOverlap = getDeploymentOverlap(aDeployment, aPeriod);
			if (newOverlap > overlap) {
				bestDeployment = aDeployment;
				overlap = newOverlap;
			}
		}
		return bestDeployment;
	}

	/**
	 * Get the overlap in mills between a nilus Deployment and a PAMGuard recording period
	 * @param aDeployment nilus Deployment from Tethys
	 * @param aPeriod PAMGuard recording period
	 * @return overlap in milliseconds
	 */
	public long getDeploymentOverlap(PDeployment aDeployment, RecordingPeriod aPeriod) {
		long start = aPeriod.getRecordStart(); // recording period. 
		long stop = aPeriod.getRecordStop();
		Long depStart = aDeployment.getAudioStart();
		Long depStop = aDeployment.getAudioEnd();
		if (depStart == null || depStop == null) {
			return -1;
		}
		long overlap = (Math.min(stop, depStop)-Math.max(start, depStart));
		return overlap;
	}

	

	
	/**
	 * Get a list of Tethys Deployment docs that match the current PAMGuard data. Watch for repeats
	 * if a single deployment doc covers many perdiods. 
	 * @return
	 */
	public ArrayList<PDeployment> getMatchedDeployments() {
		ArrayList<PDeployment> matched = new ArrayList<>();
		if (deploymentOverview == null) {
			return matched;
		}
		ArrayList<RecordingPeriod> effortPeriods = deploymentOverview.getMasterList(getTethysControl()).getEffortPeriods();
		for (RecordingPeriod period : effortPeriods) {
			PDeployment deployment = period.getMatchedTethysDeployment();
			if (deployment != null) {
				if (matched.contains(deployment) == false) {
					matched.add(period.getMatchedTethysDeployment());
				}
			}
		}
		return matched;
	}

	/**
	 * Get a list of instruments from the current project deployments. 
	 * This may be a shorter list than the list of deployments. 
	 * @return
	 */
	public ArrayList<PInstrument> getProjectInstruments() {
		if (projectDeployments == null) {
			return null;
		}
		ArrayList<PInstrument> instruments = new ArrayList<>();
		for (PDeployment aDepl : projectDeployments) {
			Instrument intr = aDepl.nilusObject.getInstrument();
			if (intr == null) {
				continue;
			}
			PInstrument pInstr = new PInstrument(intr.getType(), intr.getInstrumentId());
			if (instruments.contains(pInstr) == false) {
				instruments.add(pInstr);
			}
		}
		return instruments;
	}
	//in each channel
//	public ArrayList<DeploymentRecoveryPair> getDeployments() {
//
//		DeploymentOverview recordingOverview = this.deploymentOverview;
//
//		// first find an acquisition module.
//		PamControlledUnit aModule = PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
//		if (!(aModule instanceof AcquisitionControl)) {
//			// will return if it's null. Impossible for it to be the wrong type.
//			// but it's good practice to check anyway before casting.
//			return null;
//		}
//		// cast it to the right type.
//		AcquisitionControl daqControl = (AcquisitionControl) aModule;
//		AcquisitionParameters daqParams = daqControl.getAcquisitionParameters();
//		/**
//		 * The daqParams class has most of what we need about the set up in terms of sample rate,
//		 * number of channels, instrument type, ADC input range (part of calibration), etc.
//		 * It also has a hydrophone list, which maps the input channel numbers to the hydrophon numbers.
//		 * Realistically, this list is always 0,1,2,etc or it goes horribly wrong !
//		 */
//		// so write functions here to get information from the daqParams.
////		System.out.printf("Sample regime: %s input with rate %3.1fHz, %d channels, gain %3.1fdB, ADCp-p %3.1fV\n", daqParams.getDaqSystemType(),
////				daqParams.getSampleRate(), daqParams.getNChannels(), daqParams.preamplifier.getGain(), daqParams.voltsPeak2Peak);
//		/**
//		 * then there is the actual sampling. This is a bit harder to find. I thought it would be in the data map
//		 * but the datamap is a simple count of what's in the databasase which is not quite what we want.
//		 * we're going to have to query the database to get more detailed informatoin I think.
//		 * I'll do that here for now, but we may want to move this when we better organise the code.
//		 * It also seems that there are 'bad' dates in the database when it starts new files, which are the date
//		 * data were analysed at. So we really need to check the start and stop records only.
//		 */
//		PamDataBlock<DaqStatusDataUnit> daqInfoDataBlock = daqControl.getAcquisitionProcess().getDaqStatusDataBlock();
//		// just load everything. Probably OK for the acqusition, but will bring down
//		daqInfoDataBlock.loadViewerData(0, Long.MAX_VALUE, null);
//		ArrayList<DaqStatusDataUnit> allStatusData = daqInfoDataBlock.getDataCopy();
//		long dataStart = Long.MAX_VALUE;
//		long dataEnd = Long.MIN_VALUE;
//		if (allStatusData != null && allStatusData.size() > 0) {
//			// find the number of times it started and stopped ....
//			int nStart = 0, nStop = 0, nFile=0;
//			for (DaqStatusDataUnit daqStatus : allStatusData) {
//				switch (daqStatus.getStatus()) {
//				case "Start":
//					nStart++;
//					dataStart = Math.min(dataStart, daqStatus.getTimeMilliseconds());
//					break;
//				case "Stop":
//					nStop++;
//					dataEnd = Math.max(dataEnd, daqStatus.getEndTimeInMilliseconds());
//					break;
//				case "NextFile":
//					nFile++;
//					break;
//				}
//			}
//
////			System.out.printf("Input map of sound data indicates data from %s to %s with %d starts and %d stops over %d files\n",
////					PamCalendar.formatDateTime(dataStart), PamCalendar.formatDateTime(dataEnd), nStart, nStop, nFile+1);
//
//		}
//
////		// and we find the datamap within that ...
////		OfflineDataMap daqMap = daqInfoDataBlock.getOfflineDataMap(DBControlUnit.findDatabaseControl());
////		if (daqMap != null) {
////			// iterate through it.
////			long dataStart = daqMap.getFirstDataTime();
////			long dataEnd = daqMap.getLastDataTime();
////			List<OfflineDataMapPoint> mapPoints = daqMap.getMapPoints();
////			System.out.printf("Input map of sound data indicates data from %s to %s with %d individual files\n",
////					PamCalendar.formatDateTime(dataStart), PamCalendar.formatDateTime(dataEnd), mapPoints.size());
////			/*
////			 *  clearly in the first database I've been looking at of Tinas data, this is NOT getting sensible start and
////			 *  end times. Print them out to see what's going on.
////			 */
//////			for ()
////		}
//		DeploymentRecoveryPair pair = new DeploymentRecoveryPair();
//		DeploymentRecoveryDetails deployment = new DeploymentRecoveryDetails();
//		DeploymentRecoveryDetails recovery = new DeploymentRecoveryDetails();
//		pair.deploymentDetails = deployment;
//		pair.recoveryDetails = recovery;
//
//		deployment.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataStart));
//		deployment.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataStart));
//		recovery.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataEnd));
//		recovery.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataEnd));
//
//		ArrayList<DeploymentRecoveryPair> drPairs = new ArrayList<>();
//		drPairs.add(pair);
//		return drPairs;
//
//	}
	
	/**
	 * Get the first free deploymendId. This will get appended to 
	 * the ProjectName to make and id for each Deployment document
	 * @return
	 */
	public int getFirstFreeDeploymentId() {
		/**
		 * This is an integer used for the DeploymentId. Note that the String Id (currentl9) is just the Project name 
		 * appended with this number. 
		 */
		int firstFree = 0;
		if (projectDeployments != null) {
			for (PDeployment dep : projectDeployments) {
				firstFree = Math.max(firstFree, dep.nilusObject.getDeploymentId()+1);
			}
		}
		return firstFree;
	}

	public Deployment createDeploymentDocument(int i, RecordingPeriod recordingPeriod, String deploymentId) {
		Deployment deployment = new Deployment();
		try {
			nilus.Helper.createRequiredElements(deployment);
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
		PamguardMetaData pamguardMetaData = MetaDataContol.getMetaDataControl().getMetaData();
		Deployment templateDeployment = pamguardMetaData.getDeployment();
		
//		Deployment globalDeplData = tethysControl.getGlobalDeplopymentData();
		deployment.setId(deploymentId);
		deployment.setDeploymentId(i);

		DeploymentRecoveryDetails deploymentDetails = deployment.getDeploymentDetails();
		if (deploymentDetails == null) {
			deploymentDetails = new DeploymentRecoveryDetails();
		}
		DeploymentRecoveryDetails recoveryDetails = deployment.getRecoveryDetails();
		if (recoveryDetails == null) {
			recoveryDetails = new DeploymentRecoveryDetails();
		}

		deploymentDetails.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStart()));
		recoveryDetails.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStop()));
		// handle situation where deployment and recovery times are not the same as the audio times. 
		if (pamguardMetaData.useAudioForDeploymentTimes == false) {
			if (templateDeployment.getDeploymentDetails().getAudioTimeStamp() != null) {
				deploymentDetails.setTimeStamp(templateDeployment.getDeploymentDetails().getAudioTimeStamp());
			}
			if (templateDeployment.getRecoveryDetails().getAudioTimeStamp() != null) {
				recoveryDetails.setTimeStamp(templateDeployment.getRecoveryDetails().getAudioTimeStamp());
			}
		}
		
		deploymentDetails.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStart()));
		recoveryDetails.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStop()));
		
		deployment.setDeploymentDetails(deploymentDetails);
		deployment.setRecoveryDetails(recoveryDetails);

		getProjectData(deployment);

		TethysLocationFuncs.getTrackAndPositionData(deployment);	
		
		getTrackDetails(deployment);	
		
		/**
		 * Get some of the meta data from the centralised source. 
		 */
		MetadataInfo metaData = templateDeployment.getMetadataInfo();
		metaData.setDate(TethysTimeFuncs.xmlGregCalFromMillis(System.currentTimeMillis()));
		metaData.setUpdateFrequency("as-needed");
		deployment.setMetadataInfo(metaData);
		
		deployment.setDescription(templateDeployment.getDescription());
//		DescriptionType description = deployment.getDescription();
//		if (description == null ) {
//			description = new DescriptionType();
//			deployment.setDescription(description);
//			description.setAbstract("No abstract");
//			description.setMethod("no methods");
//			description.setObjectives("No objectives");
//		}
//		description.set

		addSamplingDetails(deployment, recordingPeriod);

		getSensorDetails(deployment);
		
		getSensors(deployment);

		/**
		 * Stuff that may need to be put into the UI:
		 * Audio: can easily get current loc of raw and binary data, but may need to override these. I think
		 * this may be for the export UI ?
		 * Tracks: trackline information. General problem in PAMGUard.
		 */
		getDataDetails(deployment);


		return deployment;
	}

	/**
	 * Add the track to the deployment, if there is one (i.e. not for 
	 * a fixed sensor). 
	 * @param deployment
	 */
	private void getTrackDetails(Deployment deployment) {
		TrackInformation trackInfo = getTrackInformation();
		if (trackInfo.haveGPSTrack() == false) {
			return;
		}
		GPSDataBlock gpsDataBlock = (GPSDataBlock) trackInfo.getGpsDataMap().getParentDataBlock();
		if (gpsDataBlock == null) {
			return;
		}
		/*
		 *  should have some track information. Do a load from the
		 *  database for the whole deployment. this may be the entire GPS record, but
		 *  we should be able to cope with that.  
		 */
		long trackStart = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getTimeStamp());
		long trackEnd = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getTimeStamp());
		long dataWin =(long)  (Math.max(1./trackInfo.getGPSDataRate(), deploymentExportOptions.trackPointInterval));
		
		// get the tracks object. 
		Tracks tracks = deployment.getData().getTracks();
		if (tracks == null) {
			tracks = new Tracks();
			deployment.getData().setTracks(tracks);
		}
		tracks.setSpeedUnit("kn");
		List<Track> trackList = tracks.getTrack(); // lists are usually there. 
		
		Track aTrack = new Track();
		trackList.add(aTrack);
		List<Point> points = aTrack.getPoint();
		
		gpsDataBlock.loadViewerData(trackStart-dataWin, trackEnd+dataWin, null);
		long lastPointTime = 0;
		ListIterator<GpsDataUnit> it = gpsDataBlock.getListIterator(0);
		while (it.hasNext()) {
			GpsDataUnit gpsDataUnit = it.next();
			if (gpsDataUnit.getTimeMilliseconds()-lastPointTime < deploymentExportOptions.trackPointInterval*1000) {
				continue;
			}
			GpsData gpsData = gpsDataUnit.getGpsData();
			Point gpsPoint = new Point();
			gpsPoint.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(gpsDataUnit.getTimeMilliseconds()));
			gpsPoint.setLatitude(gpsData.getLatitude());
			gpsPoint.setLongitude(PamUtils.constrainedAngle(gpsData.getLongitude()));
			BearingDegN bdn = gpsPoint.getBearingDegN();
			if (bdn == null) {
				bdn = new BearingDegN();
				gpsPoint.setBearingDegN(bdn);
			}
			bdn.setValue(AutoTethysProvider.roundDecimalPlaces(PamUtils.constrainedAngle(gpsData.getHeading()),1));
			gpsPoint.setSpeed(AutoTethysProvider.roundDecimalPlaces(gpsData.getSpeed(),2));
			
			points.add(gpsPoint);
			lastPointTime = gpsDataUnit.getTimeMilliseconds();
		}
	}

	public String getBinaryDataURI() {
		BinaryStore binStore = BinaryStore.findBinaryStoreControl();
		if (binStore != null) {
			return binStore.getBinaryStoreSettings().getStoreLocation();
		}
		return null;
	}
	
	public String getDatabaseURI() {
		DBControlUnit databaseControl = DBControlUnit.findDatabaseControl();
		if (databaseControl != null) {
			return databaseControl.getLongDatabaseName();
		}
		return null;
	}
	
	public String getRawDataURI() {
		try {
			PamControlledUnit daq = PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daq instanceof AcquisitionControl) {
			AcquisitionControl daqCtrl = (AcquisitionControl) daq;
			DaqSystem system = daqCtrl.findDaqSystem(null);// getAcquisitionProcess().getRunningSystem();
			if (system instanceof FolderInputSystem) {
				FolderInputSystem fip = (FolderInputSystem) system;
				return fip.getFolderInputParameters().recentFiles.get(0);
			}
		}
		}
		catch (Exception e) {
		}
		return "unknown";
	}
	
	private void getDataDetails(Deployment deployment) {
		Data data = deployment.getData();
		if (data == null) {
			data = new Data();
			deployment.setData(data);
		}
		nilus.Deployment.Data.Audio audio = data.getAudio();
		if (audio == null) {
			audio = new nilus.Deployment.Data.Audio();
			data.setAudio(audio);
		}
		audio.setURI(getRawDataURI());
		String processed = "Database:"+getDatabaseURI();
		String binary = getBinaryDataURI();
		if (binary != null) {
			binary += ";Binary:"+binary;
		}
		audio.setProcessed(processed);
		
	}
	
	/**
	 * Get sensor information. The Soundtrap CTD will count as a sensor. 
	 * Modules that are sensors will have to implement a PAMSensor interface
	 * @param deployment
	 */
	private void getSensors(Deployment deployment) {
		ArrayList<PamControlledUnit> sensorModules = PamController.getInstance().findControlledUnits(PamSensor.class, true);
		if (sensorModules == null || sensorModules.size() == 0) {
			return;
		}
		Sensors sensors = deployment.getSensors();
		if (sensors == null) {
			sensors = new Sensors();
			deployment.setSensors(sensors);
		}
		List<UnknownSensor> sensorList = sensors.getSensor();
		for (PamControlledUnit aUnit : sensorModules) {
			PamSensor pamSensor = (PamSensor) aUnit;
			UnknownSensor nilusSensor = new UnknownSensor();
			try {
				Helper.createRequiredElements(nilusSensor);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			nilusSensor.setName(pamSensor.getUnitName());
			nilusSensor.setType(pamSensor.getUnitType());
			nilusSensor.setNumber(BigInteger.ZERO);
			nilusSensor.setDescription(pamSensor.getSensorDescription());
			nilusSensor.setSensorId(pamSensor.getUnitType());
			
			sensorList.add(nilusSensor);			
		}
	}

	/**
	 * Add project Metadata to a Deploymnet document. This is currently being
	 * made available in the MetaDataControl module which should be added to PAMGuard
	 * as well as the Tethys output module.
	 * @param deployment
	 */
	private boolean getProjectData(Deployment deployment) {
//		PamControlledUnit aUnit = PamController.getInstance().findControlledUnit(MetaDataContol.class, null);
//		if (aUnit instanceof MetaDataContol == false || true) {
//			deployment.setProject("thisIsAProject");
//			deployment.setPlatform("Yay a platform");
//			Instrument instrument = new Instrument();
//			instrument.setType("machiney");
//			instrument.setInstrumentId("12345555");			
//			deployment.setInstrument(instrument);
//			return false;
//		}
//		
//		MetaDataContol metaControl = (MetaDataContol) aUnit;
		PamguardMetaData metaData = MetaDataContol.getMetaDataControl().getMetaData();
		Deployment deploymentData = tethysControl.getGlobalDeplopymentData();
		deployment.setProject(deploymentData.getProject());
		deployment.setDeploymentAlias(deploymentData.getDeploymentAlias());
		deployment.setSite(deploymentData.getSite());
		deployment.setCruise(deploymentData.getCruise());
		deployment.setPlatform(getPlatform());
		deployment.setRegion(deploymentData.getRegion());
		Instrument instrument = new Instrument();
		instrument.setType(getInstrumentType());
		instrument.setInstrumentId(getInstrumentId());
		// get the geometry type from the array manager. 
		String geomType = getGeometryType();
		instrument.setGeometryType(geomType);
		deployment.setInstrument(instrument);
		
		// overwrite the default deployment and recovery times if there is non null data
		XMLGregorianCalendar depTime = deploymentData.getDeploymentDetails().getTimeStamp();
		if (depTime != null) {
			deployment.getDeploymentDetails().setTimeStamp(depTime);
		}
		if (deploymentData.getRecoveryDetails() != null) {
			XMLGregorianCalendar recMillis = deploymentData.getRecoveryDetails().getTimeStamp();
			if (recMillis != null) {
				deployment.getRecoveryDetails().setTimeStamp(recMillis);
			}
			double recLat = deploymentData.getRecoveryDetails().getLatitude();
			double recLong = deploymentData.getRecoveryDetails().getLongitude();
			if (recLat != 0 & recLong != 0.) {
				deployment.getRecoveryDetails().setLatitude(recLat);
				deployment.getRecoveryDetails().setLongitude(PamUtils.constrainedAngle(recLong));
			}
		}
		
		return true;
	}

	/**
	 * Instrument identifier, e.g. serial number
	 * @return
	 */
	private String getInstrumentId() {
		return ArrayManager.getArrayManager().getCurrentArray().getInstrumentId();
	}
	
	/**
	 * Test to see if it's possible to export Deployment documents. This is basically a test of 
	 * various metadata fields that are required, such as instrument id's. 
	 * @return null if OK, or a string describing the first encountered error
	 */
	public String canExportDeployments() {

		Deployment globalDeplData = tethysControl.getGlobalDeplopymentData();
		if (globalDeplData.getProject() == null) {
			return "You must set a project name";
		}
		
		PInstrument arrayInstrument = getCurrentArrayInstrument();
		if (arrayInstrument == null) {
			return "No 'Instrument' set. Goto array manager";
		}
		return null;
	}
	
	/**
	 * Get the Instrument info for the current array.
	 * @return
	 */
	public PInstrument getCurrentArrayInstrument() {
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		String currType = currentArray.getInstrumentType();
		String currId = currentArray.getInstrumentId();
		PInstrument currentInstrument = null;
		if (currType != null || currId != null) {
			currentInstrument = new PInstrument(currType, currId);
		}
		return currentInstrument;
	}

	/**
	 * On what platform is the instrument deployed? (e.g. mooring, tag)
	 * @return
	 */
	private String getPlatform() {
		return getGeometryType();
	}
	/**
	 * Instrument type, e.g. HARP, EAR, Popup, DMON, Rock Hopper, etc.
	 * @return
	 */
	private String getInstrumentType() {
		return ArrayManager.getArrayManager().getCurrentArray().getInstrumentType();
	}
	
	/**
	 * Get a geometry type string for Tethys based on information in the array manager. 
	 * @return
	 */
	private String getGeometryType() {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int nStreamer = array.getStreamerCount();
		for (int i = 0; i < nStreamer; i++) {
			Streamer streamer = array.getStreamer(i);
			HydrophoneLocator locator = streamer.getHydrophoneLocator();
			if (locator == null) {
				continue;
			}
			if (locator instanceof ThreadingHydrophoneLocator) {
				return "cabled";
			}
			else {
				return "rigid";
			}
		}
		return "unknown";
	}

	private boolean getSensorDetails(Deployment deployment) {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		Sensors sensors = new Sensors();
		List<Audio> audioList = sensors.getAudio();
		ArrayList<Hydrophone> phones = array.getHydrophoneArray();
		int iPhone = 0;
		long timeMillis = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
		CalibrationHandler calibrationHandler = tethysControl.getCalibrationHandler();
		
		for (Hydrophone aPhone : phones) {
			PamVector hydLocs = array.getAbsHydrophoneVector(iPhone, timeMillis);
			Audio audio = new Audio();
			audio.setNumber(BigInteger.valueOf(iPhone));
			String id = calibrationHandler.getHydrophoneId(iPhone);
//			audio.setSensorId(String.format("Hydrophone %d", iPhone)); // should replace with serial number if it exists.
			audio.setSensorId(id);
			GeometryTypeM geom = new GeometryTypeM();
			geom.setXM(hydLocs.getCoordinate(0));
			geom.setYM(hydLocs.getCoordinate(1));
			geom.setZM(hydLocs.getCoordinate(2));
//			Geometry geom = new Geometry();
//			audio.setGeometry(geom);
////			nilusHelper.
//			List<Serializable> geomCont = geom.getContent();
//			for (int iCoord = 0; iCoord < 3; iCoord++) {
//				geom.getContent().add(Double.valueOf(hydLocs.getCoordinate(iCoord)));
//			}
//			try {
//				MarshalXML mXML = new MarshalXML();
//				mXML.marshal(geom);
//			} catch (JAXBException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			/**
			 * Need to be able to add the values from hydLocs to the geometry object, but can't.
			 */
			audioList.add(audio);
			iPhone++;
		}
//		try {
//			MarshalXML mXML = new MarshalXML();
//			mXML.marshal(sensors);
//		} catch (JAXBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		deployment.setSensors(sensors);
		return true;
	}

	/**
	 * Fill in the sampling details in a Deployment document.
	 * @param deployment
	 * @param recordingPeriod 
	 */
	private boolean addSamplingDetails(Deployment deployment, RecordingPeriod recordingPeriod) {
		
		SamplingDetails samplingDetails = deployment.getSamplingDetails();
		if (samplingDetails == null) {
			samplingDetails = new SamplingDetails();
			deployment.setSamplingDetails(samplingDetails);
		}
		// this is basically going to be a list of almost identical channel information
		// currently just for the first acquisition. May extend to more.
		// see if there is > 1 acquisition. May want to include many.
		ArrayList<PamControlledUnit> daqUnits = PamController.getInstance().findControlledUnits(AcquisitionControl.class);
		
		AcquisitionControl daq = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daq == null) {
			return false;
		}
		DaqSystem system = daq.findDaqSystem(null);
		AcquisitionParameters daqParams = daq.acquisitionParameters;
		int nChan = daqParams.nChannels;
		float fs = daqParams.sampleRate;
		int[] hydroMap = daqParams.getHydrophoneList();
		int[] inputMap = daqParams.getHardwareChannelList();
		double vp2p = daqParams.getVoltsPeak2Peak();

		List<ChannelInfo> channelInfos = samplingDetails.getChannel();
		for (int i = 0; i < nChan; i++) {
			ChannelInfo channelInfo = new ChannelInfo();
			channelInfo.setStart(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStart()));
			channelInfo.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStop()));

			BigIntegerConverter biCon = new BigIntegerConverter();
			BigInteger chanNum = BigInteger.valueOf(i);
			channelInfo.setChannelNumber(chanNum);
			if (hydroMap != null) {
				channelInfo.setSensorNumber(hydroMap[i]);
			}
			else {
				channelInfo.setSensorNumber(i);
			}
			/*
			 * Gain - may have to cycle through and see if this ever changes (or
			 * if was recorded that it changed which may not be the same!)
			 */
			ChannelInfo.Gain gain = new ChannelInfo.Gain();
			List<nilus.ChannelInfo.Gain.Regimen> gainList = gain.getRegimen();
			nilus.ChannelInfo.Gain.Regimen aGain = new nilus.ChannelInfo.Gain.Regimen();
			aGain.setGainDB(daqParams.getPreamplifier().getGain());
			channelInfo.setGain(gain);

			Sampling sampling = new Sampling();
			List<Regimen> regimens = sampling.getRegimen();
			Sampling.Regimen regimen = new Sampling.Regimen();
			regimen.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStart()));
			regimen.setSampleRateKHz(fs/1000.);
			if (system != null) {
				regimen.setSampleBits(system.getSampleBits());
			}
			regimens.add(regimen);
			
			RecordingList recordingList = deploymentOverview.getMasterList(getTethysControl());
			DutyCycleInfo dutyCycleInf = recordingList.assessDutyCycle();
			boolean isDS = dutyCycleInf != null && dutyCycleInf.isDutyCycled;
			if (isDS) {
				DutyCycle dutyCycle = new DutyCycle();
				List<nilus.ChannelInfo.DutyCycle.Regimen> reg = dutyCycle.getRegimen();
				nilus.ChannelInfo.DutyCycle.Regimen dsr = new nilus.ChannelInfo.DutyCycle.Regimen();
				reg.add(dsr);
				RecordingDurationS ssss = new RecordingDurationS();
				// round to a second ... or .1s if short duty cycle. 
				int dp = 1;
				if (dutyCycleInf.meanOnTimeS > 59) {
					dp = 0;
				}
				ssss.setValue(AutoTethysProvider.roundDecimalPlaces(dutyCycleInf.meanOnTimeS,dp));
				dsr.setRecordingDurationS(ssss);
				RecordingIntervalS ris = new RecordingIntervalS();
				ris.setValue(AutoTethysProvider.roundDecimalPlaces(dutyCycleInf.meanOnTimeS + dutyCycleInf.meanGapS,dp));
				dsr.setRecordingIntervalS(ris);
				dsr.setTimeStamp(deployment.getDeploymentDetails().getAudioTimeStamp());
				channelInfo.setDutyCycle(dutyCycle);
			}

			channelInfo.setSampling(sampling);

			channelInfos.add(channelInfo);

			/**
			 * Need something about duty cycling. this is probably something that will have to be added
			 * earlier to a wrapper around the Deployment class.
			 */
		}
		return true;
	}

	@Override
	public void selectionChanged() {
		// TODO Auto-generated method stub
		
	}
	
	private class SettingsHandler implements PamSettings {

		@Override
		public String getUnitName() {
			return tethysControl.getUnitName();
		}

		@Override
		public String getUnitType() {
			return "Tethys Deployment Handler";
		}

		@Override
		public Serializable getSettingsReference() {
			return deploymentExportOptions;
		}

		@Override
		public long getSettingsVersion() {
			return DeploymentExportOpts.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			deploymentExportOptions = (DeploymentExportOpts) pamControlledUnitSettings.getSettings();
			return true;
		}
		
	}

	/**
	 * @return the deploymentExportOptions
	 */
	public DeploymentExportOpts getDeploymentExportOptions() {
		return deploymentExportOptions;
	}

	@Override
	public String getHelpPoint() {
		return helpPoint;
	}

}
