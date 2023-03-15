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
import PamguardMVC.PamDataBlock;
import metadata.MetaDataContol;
import metadata.deployment.DeploymentData;
import nilus.Audio;
import nilus.ChannelInfo;
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
import tethys.TethysLocationFuncs;
import tethys.TethysTimeFuncs;

/**
 * Functions to gather data for the deployment document from all around PAMGuard.
 * @author dg50
 *
 */
public class DeploymentHandler {


	/**
	 * Get an overview of all the deployments.
	 * @return
	 */
	public DeploymentOverview createOverview() {
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
				if (gap < 3 || gap < prevDur/50) {
					// ignoring up to 3s gap or a sample error < 2%.Dunno if this is sensible or not.
					prevPeriod.setRecordStop(nextPeriod.getRecordStop());
					iterator.remove();
					nextPeriod = prevPeriod;
				}
			}
			prevPeriod = nextPeriod;
		}
		System.out.printf("Data have %d distinct files, but only %d distinct recording periods\n", nPeriods, tempPeriods.size());
		DutyCycleInfo dutyCycleinfo = assessDutyCycle(tempPeriods);
		DeploymentOverview deploymentOverview = new DeploymentOverview(false, tempPeriods);
		return deploymentOverview;
		// find the number of times it started and stopped ....
//		System.out.printf("Input map of sound data indicates data from %s to %s with %d starts and %d stops over %d files\n",
//				PamCalendar.formatDateTime(dataStart), PamCalendar.formatDateTime(dataEnd), nStart, nStop, nFile+1);
		// now work out where there are genuine gaps and make up a revised list of recording periods.


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
			return null;
		}
		double[] ons = new double[n-1]; // ignore the last one since it may be artificially shortened which is OK
		double[] gaps = new double[n-1];
		for (int i = 0; i < n; i++) {
			ons[i] = tempPeriods.get(i).getDuration();
		}
		return null;
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

	//in each channel
	public ArrayList<DeploymentRecoveryPair> getDeployments() {

		DeploymentOverview recordingOverview = createOverview();

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
		long dataStart = Long.MAX_VALUE;
		long dataEnd = Long.MIN_VALUE;
		if (allStatusData != null && allStatusData.size() > 0) {
			// find the number of times it started and stopped ....
			int nStart = 0, nStop = 0, nFile=0;
			for (DaqStatusDataUnit daqStatus : allStatusData) {
				switch (daqStatus.getStatus()) {
				case "Start":
					nStart++;
					dataStart = Math.min(dataStart, daqStatus.getTimeMilliseconds());
					break;
				case "Stop":
					nStop++;
					dataEnd = Math.max(dataEnd, daqStatus.getEndTimeInMilliseconds());
					break;
				case "NextFile":
					nFile++;
					break;
				}
			}

//			System.out.printf("Input map of sound data indicates data from %s to %s with %d starts and %d stops over %d files\n",
//					PamCalendar.formatDateTime(dataStart), PamCalendar.formatDateTime(dataEnd), nStart, nStop, nFile+1);

		}

//		// and we find the datamap within that ...
//		OfflineDataMap daqMap = daqInfoDataBlock.getOfflineDataMap(DBControlUnit.findDatabaseControl());
//		if (daqMap != null) {
//			// iterate through it.
//			long dataStart = daqMap.getFirstDataTime();
//			long dataEnd = daqMap.getLastDataTime();
//			List<OfflineDataMapPoint> mapPoints = daqMap.getMapPoints();
//			System.out.printf("Input map of sound data indicates data from %s to %s with %d individual files\n",
//					PamCalendar.formatDateTime(dataStart), PamCalendar.formatDateTime(dataEnd), mapPoints.size());
//			/*
//			 *  clearly in the first database I've been looking at of Tinas data, this is NOT getting sensible start and
//			 *  end times. Print them out to see what's going on.
//			 */
////			for ()
//		}
		DeploymentRecoveryPair pair = new DeploymentRecoveryPair();
		DeploymentRecoveryDetails deployment = new DeploymentRecoveryDetails();
		DeploymentRecoveryDetails recovery = new DeploymentRecoveryDetails();
		pair.deploymentDetails = deployment;
		pair.recoveryDetails = recovery;

		deployment.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataStart));
		deployment.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataStart));
		recovery.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataEnd));
		recovery.setAudioTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataEnd));

		ArrayList<DeploymentRecoveryPair> drPairs = new ArrayList<>();
		drPairs.add(pair);
		return drPairs;

	}

	public Deployment createDeploymentDocument(int i, DeploymentRecoveryPair drd) {
		Deployment deployment = new Deployment();
		String id = String.format("%d", i);
		deployment.setId(id);
		deployment.setDeploymentId(i);
		deployment.setDeploymentDetails(drd.deploymentDetails);
		deployment.setRecoveryDetails(drd.recoveryDetails);

		TethysLocationFuncs.getTrackAndPositionData(deployment);

		getProjectData(deployment);

		getSamplingDetails(deployment);

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
		PamControlledUnit aUnit = PamController.getInstance().findControlledUnit(MetaDataContol.class, null);
		if (aUnit instanceof MetaDataContol == false) {
			return false;
		}
		MetaDataContol metaControl = (MetaDataContol) aUnit;
		DeploymentData deploymentData = metaControl.getDeploymentData();
		deployment.setProject(deploymentData.getProject());
		deployment.setDeploymentAlias(deploymentData.getDeploymentAlias());
		deployment.setSite(deploymentData.getSite());
		deployment.setCruise(deploymentData.getCruise());
		deployment.setPlatform(deploymentData.getPlatform());
		deployment.setRegion(deploymentData.getRegion());
		Instrument instrument = new Instrument();
		instrument.setType(deploymentData.getInstrumentType());
		instrument.setInstrumentId(deploymentData.getInstrumentId());
		// get the geometry type from the array manager. 
		String geomType = getGeometryType();
		instrument.setGeometryType(geomType);
		deployment.setInstrument(instrument);
		return true;
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
	 */
	private boolean getSamplingDetails(Deployment deployment) {
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
