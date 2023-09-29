package tethys.calibration;

import java.util.List;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import PamController.PamController;
import PamController.soundMedium.GlobalMedium;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamController.soundMedium.GlobalMediumManager;
import nilus.Calibration;
import nilus.Calibration.FrequencyResponse;
import nilus.Calibration.QualityAssurance;
import nilus.Helper;
import nilus.MetadataInfo;
import nilus.QualityValueBasic;
import nilus.ResponsibleParty;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysStateObserver;
import tethys.TethysTimeFuncs;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.TethysException;
import tethys.niluswraps.PDeployment;
import tethys.pamdata.AutoTethysProvider;

public class CalibrationHandler implements TethysStateObserver {

	private TethysControl tethysControl;
	
	/**
	 * @param tethysControl
	 */
	public CalibrationHandler(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
		tethysControl.addStateObserver(this);
	}

	@Override
	public void updateState(TethysState tethysState) {
		// TODO Auto-generated method stub
		
	}

	public int exportAllCalibrations() {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int nPhone = array.getHydrophoneCount();
		DBXMLConnect dbxml = tethysControl.getDbxmlConnect();
		int nExport = 0;
		for (int i = 0; i < nPhone; i++) {
//			String docName = getHydrophoneId(i);
			Calibration calDoc = createCalibrationDocument(i);
			boolean ok = false;
			try {
				ok = dbxml.postAndLog(calDoc);
			} catch (TethysException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				tethysControl.showException(e);
				ok = false;
			}
			if (ok) {
				nExport++;
			}
		}
		return nExport;
	}
	
	/**
	 * Create a calibration document for a single hydrophone channel. 
	 * @param pDeployment deployment, for cross referencing. 
	 * @param channelIndex channel id. One document per channel for a multi hydrophone array. 
	 * @return Calibration document. 
	 */
	public Calibration createCalibrationDocument(int channelIndex) {
		AcquisitionControl daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.unitType);
		return createCalibrationDocument(daqControl, channelIndex);
	}
	
	/**
	 * Get an id based on the instrument identifiers and channel number. 
	 * @param channelIndex
	 * @return id string - instrument type + instrument id + channel 
	 */
	public String getHydrophoneId(int channelIndex) {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (array == null) {
			return null;
		}
		String id = String.format("%s_%s_ch%02d", array.getInstrumentType(), array.getInstrumentId(), channelIndex);
		id = id.replace(" ", "_");
		return id;
	}

	/**
	 * Create a calibration document for a single hydrophone channel. 
	 * @param pDeployment deployment, for cross referencing. 
	 * @param soundAcquisition Daq information - needed to get the ADC calibration information. 
	 * @param channelIndex channel id. One document per channel for a multi hydrophone array. 
	 * @return Calibration document. 
	 */
	public Calibration createCalibrationDocument(AcquisitionControl soundAcquisition, int channelIndex) {
		/**
		 * Calibrations document id and cross referencing to Deploymnet documents:
		 * Identifier of instrument, preamplifier, or hydrophone.
		 * Corresponds to elements in Deployment: 
		 * 			Deployment/Instrument/Id,
		 * 			Deployment/Sensors/Audio/HydrophoneId, 
		 * 			Deployment/Sensors/Audio[i]/PreampId.
		 * As instruments may be calibrated multiple times, it is not an error for duplicate Id values to appear.  
		 * It is recommended that the three different types of identifiers (instrument, hydrophone, preamp) be distinct, 
		 * but the Type element may be used to distinguish them if they are not.
		 */
		
		/*
		 *  very remote possibility that DAQ doesn't exist. What to do in this case ? It's also possible that some configurations may 
		 *  have to have >1 DAQ's ?  
		 */
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (array == null) {
			return null;
		}
		if (channelIndex < 0 || channelIndex >= array.getHydrophoneCount()) {
			return null;
		}
//		ArrayManager.getArrayManager().get
//		hydrophones = array.
		Hydrophone hydrophone = array.getHydrophoneArray().get(channelIndex);
		double hSens = hydrophone.getSensitivity();
		double preampGain = hydrophone.getPreampGain();
		
		GlobalMediumManager mediumManager = PamController.getInstance().getGlobalMediumManager();
		SoundMedium currentMedium = mediumManager.getCurrentMedium();
		double dbRef = GlobalMedium.getdBreference(currentMedium); // probably in Pa, so multiply by 1e6.  
		
		/**
		 * The calibration id can be a bit tricky, it will need to be cross referenced from the 
		 * Deployment document, and it is likely that a deployment document will have to reference several 
		 * calibration documents for different channels. 
		 * Make the name from the Array name (new), the array Instrument Id (unique to the array)
		 * and the channel number. These will then all have to go into the Deployment document in 
		 * the list of audio devices, cross referenced as the SensorId field. 
		 * 
		 */
		
		Calibration calibration = new Calibration();

		try {
			Helper.createRequiredElements(calibration);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		String id = getHydrophoneId(channelIndex);
//		 id = String.format("%d", channelIndex);
		calibration.setId(id);
		calibration.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(System.currentTimeMillis()));
		calibration.setType(GlobalMedium.getRecieverString(currentMedium, false, false));
		calibration.setIntensityReferenceUPa(AutoTethysProvider.roundSignificantFigures(dbRef*1e6,3));
		String sensRef = GlobalMedium.getdBRefString(currentMedium);
		// it doesn't like this since it has a unicode character. Leave it or change the micro to 'u'
//		calibration.setSensitivityReference(sensRef);
		calibration.setSensitivityDBV(hSens+preampGain);
		if (soundAcquisition != null) {
			AcquisitionProcess daqProcess = soundAcquisition.getAcquisitionProcess();
			double fullScale = daqProcess.rawAmplitude2dB(1, channelIndex, false);
			calibration.setSensitivityDBFS(fullScale);
		}
		FrequencyResponse frs = calibration.getFrequencyResponse();
		List<Double> hz = frs.getHz();
		List<Double> db = frs.getDB();
		hz.add(Double.valueOf(0));
		db.add(Double.valueOf(hSens+preampGain));
		
		MetadataInfo metaInf = calibration.getMetadataInfo();
		metaInf.setDate(TethysTimeFuncs.xmlGregCalFromMillis(System.currentTimeMillis()));
		metaInf.setUpdateFrequency("as-needed");
		ResponsibleParty contact = metaInf.getContact();
		contact.setIndividualName("Unknown");
		contact.setOrganizationName("unknown");
		
		QualityAssurance qa = calibration.getQualityAssurance();
		qa.setQuality(QualityValueBasic.VALID);
		qa.setComment("Unknown calibration");
		
		
		return calibration;
	}
}
