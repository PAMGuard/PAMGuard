package tethys.calibration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import Array.Preamplifier;
import PamController.PamController;
import PamController.soundMedium.GlobalMedium;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import dbxml.Queries;
import PamController.soundMedium.GlobalMediumManager;
import nilus.AlgorithmType.Parameters;
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
import tethys.calibration.swing.CalibrationsExportWizard;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.TethysException;
import tethys.niluswraps.NilusChecker;
import tethys.niluswraps.NilusSettingsWrapper;
import tethys.niluswraps.NilusUnpacker;
import tethys.pamdata.AutoTethysProvider;
import tethys.reporter.TethysReporter;

public class CalibrationHandler implements TethysStateObserver {

	private TethysControl tethysControl;
	
	private ArrayList<DocumentNilusObject<Calibration>> calibrationsList;
	
	public static final String[] updateOptions = {"as-needed", "unplanned", "yearly"};
	
	public static final String[] calibrationMethods = {"Reference hydrophone", "Manufacturers specification", "Piston phone", "Other calibrated source", "Unknown"};
	
	public static final String[] qaTypes = {"unverified", "valid", "invalid"};
	
	private Helper nilusHelper;
	/**
	 * @param tethysControl
	 */
	public CalibrationHandler(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
		calibrationsList = new ArrayList();
		tethysControl.addStateObserver(this);		try {
			nilusHelper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch (tethysState.stateType) {
		case EXPORTING:
			break;
		case NEWPAMGUARDSELECTION:
		case NEWPROJECTSELECTION:
		case EXPORTRDATA:
		case DELETEDATA:
		case UPDATEMETADATA:
		case UPDATESERVER:
			if (isWantedState(tethysState)) {
				updateDocumentsList();
			}
		default:
			break;
		
		}
	}
	
	/**
	 * Is it a state notification we want to respond to 
	 * @param state
	 * @return true if worth it. 
	 */
	protected boolean isWantedState(TethysState state) {
		if (state.collection == null) {
			return true;
		}
		switch (state.collection) {
		case OTHER:
		case Calibrations:
			return true;
		}
		return false;
	}

	/**
	 * Update the list of documents associated with the selected instrument.  
	 */
	private void updateDocumentsList() {
		
		calibrationsList.clear();
		
		ArrayList<DocumentInfo> docsList = getArrayCalibrations();
		// now immediately read the calibrations in again. 
		if (docsList == null) {
			return;
		}
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
				calibrationsList.add(calDataUnit);
//				System.out.println(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int exportAllCalibrations() {
		
		Calibration sampleCal = new Calibration();
		try {
			Helper.createRequiredElements(sampleCal);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e1) {
			e1.printStackTrace();
		}
		sampleCal = CalibrationsExportWizard.showWizard(tethysControl.getGuiFrame(), sampleCal);
		if (sampleCal == null) {
			return 0;
		}
		
		NilusSettingsWrapper<Calibration> wrappedSample = new NilusSettingsWrapper<Calibration>();
		wrappedSample.setNilusObject(sampleCal);
		
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int nPhone = array.getHydrophoneCount();
		DBXMLConnect dbxml = tethysControl.getDbxmlConnect();
		int nExport = 0;
		boolean overwrite = false;
		boolean exists;
		TethysReporter.getTethysReporter().clear();
		for (int i = 0; i < nPhone; i++) {
//			String docName = getHydrophoneId(i);
			NilusSettingsWrapper<Calibration> clonedWrap = wrappedSample.clone();
			sampleCal = clonedWrap.getNilusObject(Calibration.class);
			Calibration calDoc = createCalibrationDocument(i);
			if (sampleCal != null) {
				calDoc.setMetadataInfo(sampleCal.getMetadataInfo());				
				calDoc.setProcess(sampleCal.getProcess());
				calDoc.setQualityAssurance(sampleCal.getQualityAssurance());
				if (NilusChecker.isEmpty(sampleCal.getResponsibleParty()) == false) {
					calDoc.setResponsibleParty(sampleCal.getResponsibleParty());
				}
				calDoc.setTimeStamp(sampleCal.getTimeStamp());
			}
			// check the contact info in the metadata. 
			// can't so because it's required. 
//			MetadataInfo metaData = calDoc.getMetadataInfo();
//			if (metaData != null) {
//				if (NilusChecker.isEmpty(metaData.getContact())) {
//					metaData.setContact(null);
//				}
//			}
			
			
			addParameterDetails(calDoc, i);
			
			String calDocName = createDocumentName(calDoc, i);
			exists = calDocumentExists(calDocName);
			if (exists && overwrite == false) {
				String msg = String.format("Calibration document %s already exists. Do you want to overwrite it and other documents from this date?", calDocName);
				int ans = WarnOnce.showWarning("Calibration Export", msg, WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.OK_OPTION) {
					overwrite = true;
				}
				else {
					return nExport;
				}
			}
			boolean ok = false;
			if (exists == true && overwrite == false) {
				continue;
			}
			try {
				if (exists) {
					ok = dbxml.removeDocument(Collection.Calibrations, calDocName);
				}
				ok = dbxml.postAndLog(calDoc, calDocName);
			} catch (TethysException e) {
				e.printStackTrace();
				tethysControl.showException(e);
				ok = false;
				break;
			}
			if (ok) {
				nExport++;
			}
		}
		tethysControl.sendStateUpdate(new TethysState(TethysState.StateType.EXPORTRDATA, Collection.Calibrations));
		TethysReporter.getTethysReporter().showReport(true);
		return nExport;
	}
	
	/**
	 * Add the separate pamguard parameters to the document which are used
	 * to make up the overall calibration. 
	 * @param calDoc
	 * @param i hydrophone number
	 */
	private void addParameterDetails(Calibration calDoc, int i) {
		Parameters params = calDoc.getProcess().getParameters();
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		AcquisitionControl daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.unitType);
		AcquisitionParameters daqParams = daqControl.getAcquisitionParameters();
		Hydrophone phone = array.getHydrophoneArray().get(i);
		try {
			nilusHelper.AddAnyElement(params.getAny(), "HydrophoneType", phone.getType());
			nilusHelper.AddAnyElement(params.getAny(), "Sensitivity", String.format("%3.1f", phone.getSensitivity()));
			nilusHelper.AddAnyElement(params.getAny(), "PreampGain", String.format("%3.1f", phone.getPreampGain()));
			nilusHelper.AddAnyElement(params.getAny(), "ADCp-p", String.format("%3.2fV", daqParams.getVoltsPeak2Peak()));
			Preamplifier preamp = daqParams.preamplifier;
			if (preamp != null) {
				nilusHelper.AddAnyElement(params.getAny(), "ADCAmplifier", String.format("%3.2fdB", preamp.getGain()));
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
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
	 * Get a name for the document, which is  a bit like the id within
	 * the document, but also contain a yymmdd data string. 
	 * @param calDoc
	 * @param i channel
	 * @return document name
	 */
	private String createDocumentName(Calibration calDoc, int iChan) {
		long docDate = System.currentTimeMillis();
		XMLGregorianCalendar date = calDoc.getMetadataInfo().getDate();
		if (date != null) {
			docDate = TethysTimeFuncs.millisFromGregorianXML(date);
		}
		String dateStr = formatDate(docDate);
		String name = String.format("%s_%s_ch%d", createCalibrationDocumentRoot(), dateStr, iChan);
		return name;
	}

	/**
	 * Get a start of name for a calibration document. This will be used in the document name
	 * with a date and a channel, and the document Id just of the root and the channel. 
	 * @return root string for document names and document id's. 
	 */
	public String createCalibrationDocumentRoot() {
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
		
		if (NilusChecker.isEmpty(calibration.getResponsibleParty())) {
			calibration.setResponsibleParty(null);
		}
		
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
		if (NilusChecker.isEmpty(metaInf.getContact())) {
			metaInf.setContact(null);
		}
		if (NilusChecker.isEmpty(metaInf)) {
			calibration.setMetadataInfo(null);
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
	 * See if a document already exists. This should only occur if you 
	 * try to export the same document twice with the same calibration date. 
	 * @param documentName
	 * @return true if a document already exists. 
	 */
	public boolean calDocumentExists(String documentName) {
		if (calibrationsList == null) {
			return false;
		}
		for (int i = 0; i < calibrationsList.size(); i++) {
			if (calibrationsList.get(i).getDocumentName().equalsIgnoreCase(documentName)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return if we have at least one document for every channel. 
	 * @return true if all cal documents exist. 
	 */
	public boolean haveAllChannelCalibrations() {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int nPhone = array.getHydrophoneCount();
		for (int i = 0; i < nPhone; i++) {
			if (haveChannelCalibration(i) == false) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Find whether we have a document for this instrument and channel. 
	 * @param iChan
	 * @return true if we have an appropriate doc. 
	 */
	public boolean haveChannelCalibration(int iChan) {
		if (calibrationsList == null) {
			return false;
		}
		String seachPattern = makeChannelNamePart(iChan);
		for (int i = 0; i < calibrationsList.size(); i++) {
			String docName = calibrationsList.get(i).getDocumentName();
			if (docName.endsWith(seachPattern)) {
				return true;
			}
		}
		
		return false;
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
		String id = String.format("%s_%s", createCalibrationDocumentRoot(), makeChannelNamePart(channelIndex));
		id = id.replace(" ", "_");
		return id;
	}
	
	/**
	 * Make the final part of the document name / id which is the channel number. 
	 * @param channelIndex channel index
	 * @return string in the form ch%02d (e.g. ch03) 
	 */
	public String makeChannelNamePart(int channelIndex) {
		return String.format("ch%02d", channelIndex);
	}

	/**
	 * @return the calibrationDataBlock
	 */
	public ArrayList<DocumentNilusObject<Calibration>> getCalibrationDataList() {
		return calibrationsList;
	}
	
	/**
	 * Make a list of document names associated with this instrument. 
	 * @return list of calibration documents using this instrument, based on the start of the document name. 
	 */
	private ArrayList<DocumentInfo> getArrayCalibrations() {
		ArrayList<DocumentInfo> allCals = null;
		try {
			allCals = tethysControl.getDbxmlQueries().getCollectionDocumentList(Collection.Calibrations);
		}
		catch (Exception e) {
			
		}
		if (allCals == null) {
			return null;
		}
		String prefix = createCalibrationDocumentRoot(); // find doc names that have that root. 
		ArrayList<DocumentInfo> theseCals = new ArrayList<>();
		for (DocumentInfo aDoc : allCals) {
			if (aDoc.getDocumentName().startsWith(prefix)) {
				theseCals.add(aDoc);
			}
		}
		return theseCals;
	}
}
