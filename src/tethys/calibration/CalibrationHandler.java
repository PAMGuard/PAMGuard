package tethys.calibration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Document;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import PamController.PamController;
import PamController.soundMedium.GlobalMedium;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamUtils.PamCalendar;
import dbxml.Queries;
import PamController.soundMedium.GlobalMediumManager;
import nilus.Calibration;
import nilus.Calibration.FrequencyResponse;
import nilus.Calibration.QualityAssurance;
import nilus.Helper;
import nilus.MetadataInfo;
import nilus.QualityValueBasic;
import nilus.ResponsibleParty;
import tethys.Collection;
import tethys.DocumentInfo;
import tethys.DocumentNilusObject;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysStateObserver;
import tethys.TethysTimeFuncs;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.TethysException;
import tethys.niluswraps.NilusUnpacker;
import tethys.pamdata.AutoTethysProvider;

public class CalibrationHandler implements TethysStateObserver {

	private TethysControl tethysControl;
	
	private ArrayList<DocumentNilusObject<Calibration>> calibrationDataBlock;
	
	/**
	 * @param tethysControl
	 */
	public CalibrationHandler(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
		calibrationDataBlock = new ArrayList();
		tethysControl.addStateObserver(this);
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch (tethysState.stateType) {
		case EXPORTING:
			break;
		case NEWPAMGUARDSELECTION:
		case NEWPROJECTSELECTION:
		case TRANSFERDATA:
		case UPDATEMETADATA:
		case UPDATESERVER:
			updateDocumentsList();
		default:
			break;
		
		}
	}

	private void updateDocumentsList() {
		ArrayList<DocumentInfo> docsList = getArrayCalibrations();
		// now immediately read the calibrations in again. 
		calibrationDataBlock.clear();;
		NilusUnpacker unpacker = new NilusUnpacker();
		for (DocumentInfo aDoc : docsList) {
			Queries queries = tethysControl.getDbxmlConnect().getTethysQueries();
			String result = null;
			Calibration calObj = null;
			try {
				result = queries.getDocument(Collection.Calibrations.toString(), aDoc.getDocumentName());
				if (result != null) {
					// create a document and convert it into a Nilus calibrations document. 
					Document doc = tethysControl.getDbxmlQueries().convertStringToXMLDocument(result);
					if (doc == null) {
						System.out.println("Unable to convert Calibration result to Document\n " + result);
						continue;
					}
					calObj = (Calibration) unpacker.unpackDocument(doc, Calibration.class);
					if (calObj == null) {
						System.out.println("Unable to convert Calibration document to nilus object\n " + result);
						continue;
					}
				}
				long t = System.currentTimeMillis();
				try {
					XMLGregorianCalendar gt = calObj.getMetadataInfo().getDate();
					if (gt != null) {
						t = TethysTimeFuncs.millisFromGregorianXML(gt);
					}
				}
				catch (Exception e) {
					
				}
				DocumentNilusObject<Calibration> calDataUnit = new DocumentNilusObject(Collection.Calibrations, aDoc.getDocumentName(), calObj.getId(), calObj);
				calibrationDataBlock.add(calDataUnit);
//				System.out.println(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int exportAllCalibrations() {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int nPhone = array.getHydrophoneCount();
		DBXMLConnect dbxml = tethysControl.getDbxmlConnect();
		int nExport = 0;
		for (int i = 0; i < nPhone; i++) {
//			String docName = getHydrophoneId(i);
			Calibration calDoc = createCalibrationDocument(i);
			String calDocName = getDocumentName(calDoc, i);
			boolean ok = false;
			try {
				ok = dbxml.postAndLog(calDoc, calDocName);
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
	 * Get a name for the document, which is  a bit like the id within
	 * the document, but also contain a yymmdd data string. 
	 * @param calDoc
	 * @param i channel
	 * @return document name
	 */
	private String getDocumentName(Calibration calDoc, int iChan) {
		long docDate = System.currentTimeMillis();
		XMLGregorianCalendar date = calDoc.getMetadataInfo().getDate();
		if (date != null) {
			docDate = TethysTimeFuncs.millisFromGregorianXML(date);
		}
		String dateStr = formatDate(docDate);
		String name = String.format("%s_%s_ch%d", getCalibrationDocumentRoot(), dateStr, iChan);
		return name;
	}
	/**
	 * Format the data in the dd MMMM yyyy format
	 * @param timeInMillis time in milliseconds
	 * @return formatted string. 
	 */
	public static String formatDate(long timeInMillis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(PamCalendar.defaultTimeZone);
		DateFormat df = new SimpleDateFormat("yyMMdd");
		df.setTimeZone(PamCalendar.defaultTimeZone);
		Date d = c.getTime();
		return df.format(d);
	}


	/**
	 * Get a start of name for a calibration document. This will be used in the document name
	 * with a date and a channel, and the document Id just of the root and the channel. 
	 * @return root string for document names and document id's. 
	 */
	public String getCalibrationDocumentRoot() {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (array == null) {
			return null;
		}
		String root = String.format("%s %s",  array.getInstrumentType(), array.getInstrumentId());
		root = root.replace(" ", "_");
		return root;
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
		double dbRef = GlobalMedium.getdBreference(currentMedium); // probably in Pa, so multiply by 1e6.  20 (air) or 0 (water)
		
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
//		calibration.setType(GlobalMedium.getRecieverString(currentMedium, false, false));
		calibration.setType("end-to-end");
		calibration.setIntensityReferenceUPa(AutoTethysProvider.roundSignificantFigures(dbRef*1e6,3));
//		String sensRef = GlobalMedium.getdBRefString(currentMedium);
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
		if (metaInf == null) {
			metaInf = new MetadataInfo();
			calibration.setMetadataInfo(metaInf);
		}
		metaInf.setDate(TethysTimeFuncs.xmlGregCalFromMillis(System.currentTimeMillis()));
		metaInf.setUpdateFrequency("as-needed");
		ResponsibleParty contact = metaInf.getContact();
		if (contact == null) {
			contact = new ResponsibleParty();
			metaInf.setContact(contact);
		}
		contact.setIndividualName("Unknown");
		contact.setOrganizationName("unknown");
		
		QualityAssurance qa = calibration.getQualityAssurance();
		if (qa == null) {
			qa = new QualityAssurance();
			calibration.setQualityAssurance(qa);
		}
		qa.setQuality(QualityValueBasic.VALID);
		qa.setComment("Unknown calibration");
		
		
		return calibration;
	}

	/**
	 * Get an id based on the instrument identifiers and channel number. 
	 * This is the internal id of the document, not the document name which 
	 * includes an additional date part in the name. 
	 * @param channelIndex
	 * @return id string - instrument type + instrument id + channel 
	 */
	public String getHydrophoneId(int channelIndex) {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (array == null) {
			return null;
		}
		String id = String.format("%s_ch%02d", getCalibrationDocumentRoot(), channelIndex);
		id = id.replace(" ", "_");
		return id;
	}

	/**
	 * @return the calibrationDataBlock
	 */
	public ArrayList<DocumentNilusObject<Calibration>> getCalibrationDataList() {
		return calibrationDataBlock;
	}
	
	private ArrayList<DocumentInfo> getArrayCalibrations() {
		ArrayList<DocumentInfo> allCals = tethysControl.getDbxmlQueries().getCollectionDocumentList(Collection.Calibrations);
		String prefix = getCalibrationDocumentRoot();
		// find doc names that have that root. 
		ArrayList<DocumentInfo> theseCals = new ArrayList<>();
		for (DocumentInfo aDoc : allCals) {
			if (aDoc.getDocumentName().startsWith(prefix)) {
				theseCals.add(aDoc);
			}
		}
		return theseCals;
	}
}
