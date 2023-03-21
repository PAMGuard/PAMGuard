package tethys.deployment;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.JAXBException;

import org.apache.commons.beanutils.converters.BigIntegerConverter;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.DaqStatusDataUnit;
import Acquisition.DaqSystem;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.HydrophoneLocator;
import Array.PamArray;
import Array.Streamer;
import Array.ThreadingHydrophoneLocator;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import SoundRecorder.RecordingInfo;
import javafx.scene.chart.PieChart.Data;
import metadata.MetaDataContol;
import metadata.deployment.DeploymentData;
import nilus.Audio;
import nilus.ChannelInfo;
import nilus.ChannelInfo.DutyCycle;
import nilus.ChannelInfo.DutyCycle.Regimen.RecordingDurationS;
import nilus.ChannelInfo.DutyCycle.Regimen.RecordingIntervalS;
import nilus.ChannelInfo.Sampling;
import nilus.ChannelInfo.Sampling.Regimen;
import nilus.Deployment;
import nilus.Deployment.Instrument;
import nilus.Deployment.SamplingDetails;
import nilus.Deployment.Sensors;
import nilus.DeploymentRecoveryDetails;
import nilus.GeometryTypeM;
import nilus.Helper;
import pamMaths.PamVector;
import pamMaths.STD;
import tethys.TethysControl;
import tethys.TethysLocationFuncs;
import tethys.TethysState;
import tethys.TethysStateObserver;
import tethys.TethysTimeFuncs;
import tethys.niluswraps.PDeployment;

/**
 * Functions to gather data for the deployment document from all around PAMGuard.
 * There should be just one of these, available from TethysControl and it will try 
 * to sensible handle when and how it updates it's list of PAMGuard and Tethys information
 * <br> Any part of PAMGuard wanting information on Deployments should come here. 
 * @author dg50
 *
 */
public class DeploymentHandler implements TethysStateObserver {
	
	private TethysControl tethysControl;
	
	private DeploymentOverview deploymentOverview;
	
	private ArrayList<PDeployment> projectDeployments;

	public DeploymentHandler(TethysControl tethysControl) {
		super();
		this.tethysControl = tethysControl;
		tethysControl.addStateObserver(this);
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch (tethysState.stateType) {
		case NEWPROJECTSELECTION:
			updateProjectDeployments();
			break;
		case TRANSFERDATA:
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
		DeploymentData projData = tethysControl.getGlobalDeplopymentData();
		ArrayList<Deployment> tethysDocs = tethysControl.getDbxmlQueries().getProjectDeployments(projData.getProject());
		if (tethysDocs == null) {
			return false;
		}
		projectDeployments = new ArrayList<>();
		for (Deployment deployment : tethysDocs) {
			projectDeployments.add(new PDeployment(deployment));
		}
		matchPamguard2Tethys(deploymentOverview, projectDeployments);
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
	
	/**
	 * Get an overview of all the deployments.
	 * @return
	 */
	public DeploymentOverview createPamguardOverview() {
		// first find an acquisition module.
		PamControlledUnit aModule = PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (!(aModule instanceof AcquisitionControl)) {
			// will return if it's null. Impossible for it to be the wrong type.
			// but it's good practice to check anyway before casting.
			return null;
		}
		// cast it to the right type.
		AcquisitionControl daqControl = (AcquisitionControl) aModule;
		AcquisitionParameters daqParams = daqControl.getAcquisitionParameters();
		/**
		 * The daqParams class has most of what we need about the set up in terms of sample rate,
		 * number of channels, instrument type, ADC input range (part of calibration), etc.
		 * It also has a hydrophone list, which maps the input channel numbers to the hydrophon numbers.
		 * Realistically, this list is always 0,1,2,etc or it goes horribly wrong !
		 */
		// so write functions here to get information from the daqParams.
//		System.out.printf("Sample regime: %s input with rate %3.1fHz, %d channels, gain %3.1fdB, ADCp-p %3.1fV\n", daqParams.getDaqSystemType(),
//				daqParams.getSampleRate(), daqParams.getNChannels(), daqParams.preamplifier.getGain(), daqParams.voltsPeak2Peak);
		/**
		 * then there is the actual sampling. This is a bit harder to find. I thought it would be in the data map
		 * but the datamap is a simple count of what's in the databasase which is not quite what we want.
		 * we're going to have to query the database to get more detailed informatoin I think.
		 * I'll do that here for now, but we may want to move this when we better organise the code.
		 * It also seems that there are 'bad' dates in the database when it starts new files, which are the date
		 * data were analysed at. So we really need to check the start and stop records only.
		 */
		PamDataBlock<DaqStatusDataUnit> daqInfoDataBlock = daqControl.getAcquisitionProcess().getDaqStatusDataBlock();
		// just load everything. Probably OK for the acqusition, but will bring down
		daqInfoDataBlock.loadViewerData(0, Long.MAX_VALUE, null);
		ArrayList<DaqStatusDataUnit> allStatusData = daqInfoDataBlock.getDataCopy();

		ArrayList<RecordingPeriod> tempPeriods = null;

		if (allStatusData == null || allStatusData.size() == 0) {
			System.out.println("Data appear to have no logged recording periods. Try to extract from raw audio ...");
			tempPeriods = extractTimesFromFiles(daqControl);
		}
		else {
			tempPeriods = extractTimesFromStatus(allStatusData);
		}
		if (tempPeriods == null || tempPeriods.size() == 0) {
			System.out.println("Data appear to have no logged recording periods available either from the database or the raw recordings.");
		}
		int nPeriods = tempPeriods.size();
		// now go through those and merge into longer periods where there is no gap between files.
		ListIterator<RecordingPeriod> iterator = tempPeriods.listIterator();
		RecordingPeriod prevPeriod = null;
		while (iterator.hasNext()) {
			RecordingPeriod nextPeriod = iterator.next();
			long nextDur = nextPeriod.getRecordStop()-nextPeriod.getRecordStart();
			if (nextDur == 0) {
				continue;
			}
			if (prevPeriod != null) {
				long gap = nextPeriod.getRecordStart() - prevPeriod.getRecordStop();
				long prevDur = prevPeriod.getRecordStop()-prevPeriod.getRecordStart();
				if (gap < 3000 || gap < prevDur/50) {
					// ignoring up to 3s gap or a sample error < 2%.Dunno if this is sensible or not.
					prevPeriod.setRecordStop(nextPeriod.getRecordStop());
					iterator.remove();
					nextPeriod = prevPeriod;
				}
			}
			prevPeriod = nextPeriod;
		}
//		System.out.printf("Data have %d distinct files, but only %d distinct recording periods\n", nPeriods, tempPeriods.size());
		DutyCycleInfo dutyCycleinfo = assessDutyCycle(tempPeriods);
		// if it's duty cycles, then we only want a single entry. 
		ArrayList<RecordingPeriod> deploymentPeriods;
		if (dutyCycleinfo.isDutyCycled == false) {
			deploymentPeriods = tempPeriods;
		}
		else {
			deploymentPeriods = new ArrayList<>();
			deploymentPeriods.add(new RecordingPeriod(tempPeriods.get(0).getRecordStart(), tempPeriods.get(tempPeriods.size()-1).getRecordStop()));
		}
		DeploymentOverview deploymentOverview = new DeploymentOverview(dutyCycleinfo, deploymentPeriods);
		matchPamguard2Tethys(deploymentOverview, projectDeployments);
		this.deploymentOverview = deploymentOverview;
		return deploymentOverview;
		// find the number of times it started and stopped ....
//		System.out.printf("Input map of sound data indicates data from %s to %s with %d starts and %d stops over %d files\n",
//				PamCalendar.formatDateTime(dataStart), PamCalendar.formatDateTime(dataEnd), nStart, nStop, nFile+1);
		// now work out where there are genuine gaps and make up a revised list of recording periods.


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
		ArrayList<RecordingPeriod> recordingPeriods = deploymentOverview.getRecordingPeriods();
		for (RecordingPeriod aPeriod : recordingPeriods) {
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
		long start = aPeriod.getRecordStart();
		long stop = aPeriod.getRecordStop();
		long depStart = aDeployment.getAudioStart();
		long depStop = aDeployment.getAudioEnd();
		long overlap = (Math.min(stop, depStop)-Math.max(start, depStart));
		return overlap;
	}

	/**
	 * Work out whether or not the data are evenly duty cycled by testing the
	 * distributions of on and off times.
	 * @param tempPeriods
	 * @return
	 */
	private DutyCycleInfo assessDutyCycle(ArrayList<RecordingPeriod> tempPeriods) {
		int n = tempPeriods.size();
		if (n < 2) {
			return new DutyCycleInfo(false, 0,0,n);
		}
		double[] ons = new double[n-1]; // ignore the last one since it may be artificially shortened which is OK
		double[] gaps = new double[n-1];
		for (int i = 0; i < n-1; i++) {
			ons[i] = tempPeriods.get(i).getDuration()/1000.;
			gaps[i] = (tempPeriods.get(i+1).getRecordStart()-tempPeriods.get(i).getRecordStop())/1000.;
		}
		// now look at how consistent those values are
		STD std = new STD();
		double onsMean = std.getMean(ons);
		double onsSTD = std.getSTD(ons);
		double gapsMean = std.getMean(gaps);
		double gapsSTD = std.getSTD(gaps);
		boolean dutyCycle = onsSTD/onsMean < .05 && gapsSTD/gapsMean < 0.05;
		DutyCycleInfo cycleInfo = new DutyCycleInfo(dutyCycle, onsMean, gapsMean, tempPeriods.size());
		return cycleInfo;
	}


	private ArrayList<RecordingPeriod> extractTimesFromStatus(ArrayList<DaqStatusDataUnit> allStatusData) {
		ArrayList<RecordingPeriod> tempPeriods = new ArrayList<>();
		long dataStart = Long.MAX_VALUE;
		long dataEnd = Long.MIN_VALUE;
		Long lastStart = null;
		int nStart = 0;
		int nStop = 0;
		int nFile = 0;
		for (DaqStatusDataUnit daqStatus : allStatusData) {
			switch (daqStatus.getStatus()) {
			case "Start":
				nStart++;
				dataStart = Math.min(dataStart, daqStatus.getTimeMilliseconds());
				lastStart = daqStatus.getTimeMilliseconds();
				break;
			case "Stop":
				nStop++;
				dataEnd = Math.max(dataEnd, daqStatus.getEndTimeInMilliseconds());
				long lastEnd = daqStatus.getEndTimeInMilliseconds();
				if (lastStart != null) {
					tempPeriods.add(new RecordingPeriod(lastStart, lastEnd));
				}
				lastStart = null;
				break;
			case "NextFile":
				nFile++;
				break;
			}
		}
		return tempPeriods;
	}

	private ArrayList<RecordingPeriod> extractTimesFromFiles(AcquisitionControl daqControl) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get a list of Tethys Deployment docs that match the current PAMGuard data. 
	 * @return
	 */
	public ArrayList<PDeployment> getMatchedDeployments() {
		ArrayList<PDeployment> matched = new ArrayList<>();
		if (deploymentOverview == null) {
			return matched;
		}
		for (RecordingPeriod period : deploymentOverview.getRecordingPeriods()) {
			if (period.getMatchedTethysDeployment() != null) {
				matched.add(period.getMatchedTethysDeployment());
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
			Instrument intr = aDepl.deployment.getInstrument();
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
				firstFree = Math.max(firstFree, dep.deployment.getDeploymentId()+1);
			}
		}
		return firstFree;
	}

	public Deployment createDeploymentDocument(int i, RecordingPeriod recordingPeriod) {
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
		DeploymentData globalDeplData = tethysControl.getGlobalDeplopymentData();
		String id = String.format("%s%d", globalDeplData.getProject(), i);
		deployment.setId(id);
		deployment.setDeploymentId(i);

		DeploymentRecoveryDetails deploymentDetails = new DeploymentRecoveryDetails();
		DeploymentRecoveryDetails recoveryDetails = new DeploymentRecoveryDetails();
		deploymentDetails.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStart()));
		deploymentDetails.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStart()));
		recoveryDetails.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStop()));
		recoveryDetails.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(recordingPeriod.getRecordStop()));
		
		deployment.setDeploymentDetails(deploymentDetails);
		deployment.setRecoveryDetails(recoveryDetails);

		TethysLocationFuncs.getTrackAndPositionData(deployment);			

		getProjectData(deployment);

		getSamplingDetails(deployment, recordingPeriod);

		getSensorDetails(deployment);

		/**
		 * Stuff that may need to be put into the UI:
		 * Audio: can easily get current loc of raw and binary data, but may need to override these. I think
		 * this may be for the export UI ?
		 * Tracks: trackline information. General problem in PAMGUard.
		 */



		return deployment;
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
		DeploymentData deploymentData = tethysControl.getGlobalDeplopymentData();
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

		DeploymentData globalDeplData = tethysControl.getGlobalDeplopymentData();
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
		Helper nilusHelper = null;
		try {
			nilusHelper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		for (Hydrophone aPhone : phones) {
			PamVector hydLocs = array.getAbsHydrophoneVector(iPhone, timeMillis);
			Audio audio = new Audio();
			audio.setNumber(BigInteger.valueOf(iPhone));
			audio.setSensorId(String.format("Hydrophone %d", iPhone)); // shold replace with serial number if it exists.
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
	private boolean getSamplingDetails(Deployment deployment, RecordingPeriod recordingPeriod) {
		SamplingDetails samplingDetails = new SamplingDetails();
		// this is basically going to be a list of almost identical channel information
		// currently just for the first acquisition. May extend to more.
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
			channelInfo.setStart(deployment.getDeploymentDetails().getAudioTimeStamp());
			channelInfo.setEnd(deployment.getRecoveryDetails().getAudioTimeStamp());

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
			regimen.setTimeStamp(deployment.getDeploymentDetails().getAudioTimeStamp());
			regimen.setSampleRateKHz(fs/1000.);
			if (system != null) {
				regimen.setSampleBits(system.getSampleBits());
			}
			regimens.add(regimen);
			
			DutyCycleInfo dutyCycleInf = deploymentOverview.getDutyCycleInfo();
			boolean isDS = dutyCycleInf != null && dutyCycleInf.isDutyCycled;
			if (isDS) {
				DutyCycle dutyCycle = new DutyCycle();
				List<nilus.ChannelInfo.DutyCycle.Regimen> reg = dutyCycle.getRegimen();
				nilus.ChannelInfo.DutyCycle.Regimen dsr = new nilus.ChannelInfo.DutyCycle.Regimen();
				reg.add(dsr);
				RecordingDurationS ssss = new RecordingDurationS();
				ssss.setValue(dutyCycleInf.meanOnTimeS);
				dsr.setRecordingDurationS(ssss);
				RecordingIntervalS ris = new RecordingIntervalS();
				ris.setValue(dutyCycleInf.meanOnTimeS + dutyCycleInf.meanGapS);
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
		deployment.setSamplingDetails(samplingDetails);
		return true;
	}

}
