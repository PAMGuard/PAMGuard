package clickTrainDetector.logging;

import java.sql.Types;
import PamController.PamViewParameters;
import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamUtils.avrgwaveform.AverageWaveform;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;
import clickTrainDetector.IDIInfo;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.localisation.CTLocalisation;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import pamMaths.PamInterp;


/**
 * Functions for saving a click train. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClickTrainDetLogging extends SuperDetLogging {


	/**
	 * The delimiter used for the average spectrum in the database. 
	 */
	private static final String AVERAGE_SPECTRUM_DELIMITTER = ","; 

	/**
	 * The delimiter for classifiers. 
	 */
	private final static String JSON_DELIMITER =  "--";
	
	/**
	 * The default maximum specturm length
	 */
	private final static int DEFAULT_SPECTRUM_LEN = 256; 


	/**
	 * Reference to the click train data block. 
	 */
	private ClickTrainDataBlock<CTDataUnit> clickTrainDataBlock;

	/**
	 * Detection group control. 
	 */
	private ClickTrainControl clickTrainControl;

	/**
	 * The pam table items for saving.
	 */
	private PamTableItem endTime, dataCount, chi2, medianIDI, meanIDI, stdIDI,
	algorithmInfo, avrg_Spectrum, speciesFlag, avrg_Spectrum_max;

	/**
	 * Classifiers 
	 */
	private PamTableItem classifiers;




	public ClickTrainDetLogging(ClickTrainControl clickTrainControl, ClickTrainDataBlock<CTDataUnit> pamDataBlock) {
		super(pamDataBlock, false);
		this.clickTrainControl = clickTrainControl;
		this.clickTrainDataBlock = pamDataBlock;
		setTableDefinition(createBaseTable());
	}

	/**
	 * Create the basic table definition for the group detection. 
	 * @return basic table - annotations will be added shortly !
	 */
	public PamTableDefinition createBaseTable() {
		PamTableDefinition tableDef = new PamTableDefinition(clickTrainControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(endTime 		= new PamTableItem("EndTime", Types.TIMESTAMP));
		tableDef.addTableItem(dataCount 	= new PamTableItem("DataCount", Types.INTEGER));
		tableDef.addTableItem(chi2 			= new PamTableItem("Chi2", Types.DOUBLE));
		tableDef.addTableItem(medianIDI 	= new PamTableItem("median_IDI_sec", Types.DOUBLE));
		tableDef.addTableItem(meanIDI 		= new PamTableItem("mean_IDI_sec", Types.DOUBLE));
		tableDef.addTableItem(stdIDI	 	= new PamTableItem("std_IDI_sec", Types.DOUBLE));
		//		tableDef.addTableItem(stdIDI	 	= new PamTableItem("clssfrs_passed", Types.CHAR)); //TODO- classifiers.
		//need to add a length because even though this is never loaded it is still read and creates a -1 index out of bounds exception
		//256 is meaningless length....just seemed like a good number. 
		tableDef.addTableItem(algorithmInfo = new PamTableItem("algorithm_info", Types.CHAR, 256)); 

		//average spectrum
		tableDef.addTableItem(avrg_Spectrum_max = new PamTableItem("avrg_spectrum_max", Types.DOUBLE)); 
		tableDef.addTableItem(avrg_Spectrum = new PamTableItem("avrg_spectrum", Types.CHAR, 8*DEFAULT_SPECTRUM_LEN)); 
		tableDef.addTableItem(classifiers = new PamTableItem("classifiers", Types.CHAR, 8128));

		//a species flag, this is entirely for user convenience and is NOT read back - the species flag 
		//is read from the JSON strings when reloading the data unit. If they end being different something has gone 
		//a little wrong.
		tableDef.addTableItem(speciesFlag = new PamTableItem("speciesFlag", Types.INTEGER));
		
		setEventEndTimeItem(endTime);

		return tableDef;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		CTDataUnit ctDataUnit = (CTDataUnit) pamDataUnit;
		if (ctDataUnit.getSubDetectionsCount()>0) {
			endTime.setValue(sqlTypes.getTimeStamp(ctDataUnit.getEndTimeInMilliseconds()));
			dataCount.setValue(ctDataUnit.getSubDetectionsCount());
			chi2.setValue(ctDataUnit.getCTChi2());


			//the IDI measurements. 
			medianIDI.setValue(ctDataUnit.getIDIInfo().medianIDI);
			meanIDI.setValue(ctDataUnit.getIDIInfo().meanIDI);
			stdIDI.setValue(ctDataUnit.getIDIInfo().stdIDI);


			//			Debug.out.println("ClickTrainDetLogging: " + dgdu.getCTAlgorithmInfo());
			//			Debug.out.println("ClickTrainDetLogging: " + dgdu.getCTAlgorithmInfo().getCTAlgorithmLogging());

			//save the algorithm info to the database
			algorithmInfo.setValue(ctDataUnit.getCTAlgorithmInfo().getCTAlgorithmLogging().getJsonString(ctDataUnit.getCTAlgorithmInfo()));

			String classificationData =  getClassifierData(ctDataUnit); 

			//			Debug.out.println("Classifier Save: " + classificationData);

			if (classificationData.length()>0) {
				classifiers.setLength(classificationData.length());
				classifiers.setValue(classificationData);
			}

			if (ctDataUnit.getClassificationIndex()>=0) {
				speciesFlag.setValue(ctDataUnit.getCtClassifications().
						get(ctDataUnit.getClassificationIndex()).getSpeciesID());
			}
			else {
				//OK, so using no species here because it's PG convention (0 for no species) and this 
				//never used apart from being a convenience column for user. Programmatically
				//it should be CTClassifier.PRECLASSIFIER
				speciesFlag.setValue(CTClassifier.NOSPECIES);
			}

			//set the speciesflag 

			/**
			 * In viewer mode the click train can be loaded but not have any average spectrum...
			 */
			//			Debug.out.println("SAVING A NEW CLICK TRAIN"); 
			//			Debug.out.println("The number of sub detections is: " + pamDataUnit.getSubDetectionsCount() + "   " +PamCalendar.formatDateTime(pamDataUnit.getTimeMilliseconds()));
			//			Debug.out.println("Median ICI: " +dgdu.getIDIInfo().medianIDI); 
			//			Debug.out.println("Avrg spectrum size: " +dgdu.getAverageSpectra().length); 
			//			Debug.out.println("Chi2: " +dgdu.getCTChi2()); 

			/** Interpolate the spectrum if needed**/
			double[] averageSpectra  = ctDataUnit.getAverageSpectra(); 
			
			if (averageSpectra.length>DEFAULT_SPECTRUM_LEN) {
				//downsample the spectrum
				double[] freqBinsXd  = PamArrayUtils.list2ArrayD(PamUtils.linspace(0, ctDataUnit.getParentDataBlock().getSampleRate()/2, averageSpectra.length)); 
				double[] freqBinsXid  = PamArrayUtils.list2ArrayD(PamUtils.linspace(0, ctDataUnit.getParentDataBlock().getSampleRate()/2, DEFAULT_SPECTRUM_LEN)); 
				averageSpectra = PamInterp.interp1(freqBinsXd, averageSpectra, freqBinsXid, 0); 
			}
			
			//System.out.println("Get average spectrum: " + averageSpectra.length); 
			
			if (averageSpectra!=null) {
				double waveformDivisor = PamArrayUtils.max(averageSpectra); 
				//			if (dgdu.getAverageSpectra()==null) return; 
				String spectrumData =spectrumString(averageSpectra, waveformDivisor); 

				//			Debug.out.println("Hello average count: " + dgdu.averageWaveform.getAverageCount()); 
				//			PamArrayUtils.printArray(dgdu.averageWaveform.getAverageSpectra()); 
				//			System.out.println("Average spectrum string: " +spectrumData); 

				// the average spectrum
				avrg_Spectrum.setLength(spectrumData.length());
				avrg_Spectrum.setValue(spectrumData);
				avrg_Spectrum_max.setValue(waveformDivisor);

			}
		}
	}

	/**
	 * Get the classifier string. 
	 * @param ctDataUnit - the CTDataUnit.
	 * @return the JSON string. 
	 */
	String getClassifierData(CTDataUnit ctDataUnit) {
		String jsonString = ""; 
		for (int i=0; i<ctDataUnit.getCtClassifications().size(); i++) {
			jsonString += ctDataUnit.getCtClassifications().get(i).getJSONLogging().
					getJsonString(ctDataUnit.getCtClassifications().get(i)) + JSON_DELIMITER;
		}
		return jsonString; 
	}

	/**
	 * Create a spectrum string for the database. 
	 * @param spectrum - the spectrum. 
	 * @param - waveformDivisor - the value to divide the waveform by for more manageable string numbers.
	 * @return string representation for normalised spectrum
	 */
	private String spectrumString(double[] spectrum, double waveformDivisor) {
		double[] spectrumNorm = PamArrayUtils.normalise(spectrum); 

		if (spectrumNorm == null) {
			return "No spectrum";
		}

		String spectrumString = "";
		//double maxSpectrum = PamArrayUtils.max(spectrum);
		for (int i=0; i<spectrumNorm.length; i++) {
			spectrumString+= String.format("%.5f", spectrum[i]/waveformDivisor) +  AVERAGE_SPECTRUM_DELIMITTER; 
		}
		//	
		//		System.out.println("SpectrumString"); 
		//		System.out.println(spectrumString); 
		return spectrumString;
	}

	/**
	 * Parse the average spectrum from a data base string.
	 * @param avrgSpectrumStr - the average spectrum to aprse. 
	 * @return the average sepctrum as a double array. 
	 */
	private double[] parseAvrgeSpectrum(String avrgSpectrumStr, double waveformDivisor) {
		if (avrgSpectrumStr==null) return null;
		String[] values = avrgSpectrumStr.split(AVERAGE_SPECTRUM_DELIMITTER); 
		double[] averageSpectra = new double[values.length];

		for (int i=0; i<values.length; i++) {
			averageSpectra[i] = Double.valueOf(values[i])*waveformDivisor; 
		}

		return averageSpectra;
	}



	/**
	 * Reference to the click train control. 
	 * @return the detectionGroupControl. 
	 */
	public ClickTrainControl getClickTrainControl() {
		return clickTrainControl;
	}

	@Override
	protected CTDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {

		CTDataUnit dataUnit = new CTDataUnit(timeMilliseconds);

		//Debug.out.println("CTDataUnit: " + PamCalendar.formatDateTime(timeMilliseconds)); 


		Long endUTC = SQLTypes.millisFromTimeStamp(endTime.getValue());

		int nData = dataCount.getIntegerValue();
		if (nData == 0) {
			nData = checkSubTableCount(databaseIndex);
		}
		if (endUTC != null) {
			dataUnit.setDurationInMilliseconds(endUTC-timeMilliseconds);
		}

		dataUnit.setCTAlgorithmInfo(this.clickTrainControl.getCTAlgorithmInfoManager().getAlgorithmInfo(algorithmInfo.getStringValue())); 

		//set the classifications. 
		String classifiersData = classifiers.getStringValue();
		
		//FIXME
		//System.out.println(classifiersData);

		if (classifiersData!=null && classifiersData.length()>0) {
			String[] classifiersDatas = classifiersData.split(JSON_DELIMITER);
			for (int i=0; i<classifiersDatas.length; i++) {
				/**
				 * There is something weird in the way the strings ar ebeing loaded which sometimes 
				 * gets rid of the "}" at the end of the string. 
				 */
				if (classifiersDatas[i].substring(classifiersDatas[i].length() - 1)!="}") {
					classifiersDatas[i] = classifiersDatas[i] + "}";
				}

				try {
					CTClassification classification = this.clickTrainControl.getClassifierManager().jsonString2Classification(classifiersDatas[i]);
					if (classification!=null) {
						dataUnit.addCtClassification(classification);
					}
				}
				catch (Exception e) {
					Debug.err.println("ClickTrainDetLogging: BAD JSON CLASSIFIER STRING: " + classifiersDatas[i]);
					Debug.err.println(classifiersData);
					e.printStackTrace();
				}
			}
		}

		//		Debug.out.println("----------------"); 
		boolean hasBeenClssfd=false;
		for (int i=0; i<dataUnit.getCtClassifications().size(); i++) {
			//set the species flag but only if this is the first time the ct data unit has been classified. 
			//			Debug.out.println("SpeciesID: " + dataUnit.getCtClassifications().get(i).getSpeciesID()); 
			if (dataUnit.getCtClassifications().get(i).getSpeciesID()>0 && !hasBeenClssfd) {
				dataUnit.setClassificationIndex(i); //set the classification index. 
				hasBeenClssfd = true; 
			}
		}
		//		Debug.out.println("Index: " + dataUnit.getClassificationIndex() + " UID: " + databaseIndex); 
		//		Debug.out.println("----------------"); 

		String avrgSpectrumStr = avrg_Spectrum.getStringValue();
		Double avrgSpectrumDivisor = avrg_Spectrum_max.getDoubleValue(); 

		//		Debug.out.println("Classifier Load: chi2: " + chi2.getDoubleValue() + "  " + PamCalendar.formatDateTime(dataUnit.getTimeMilliseconds()) + "  " +classifiers.getStringValue()); 

		//max waveform was added alter so in older databases might be null. In this case just keep normalised spectrum. Meh. 
		if (avrgSpectrumDivisor==null || avrgSpectrumDivisor==0) {
			avrgSpectrumDivisor = 1.; 
		}

		//set the average spectrum 
		AverageWaveform averageWaveform = new AverageWaveform(); 
		averageWaveform.setAverageSpectra(parseAvrgeSpectrum(avrgSpectrumStr, avrgSpectrumDivisor)); 
		averageWaveform.setSampleRate(clickTrainControl.getClickTrainProcess().getSampleRate()); //important to set the samplerate!

		dataUnit.setAverageWaveform(averageWaveform);
		
		
		//important to set the IDI info because, although it is calculated when sub detections are added, when reprocessing offline,
		//sub detections are not loaded and thus the IDI is not calculated properly. 
		IDIInfo idiInfo = new IDIInfo(medianIDI.getDoubleValue(), meanIDI.getDoubleValue(), stdIDI.getDoubleValue(), nData); 
		dataUnit.setIDIInfo(idiInfo); 

		//note the average waveform and IDI info is created when the sub detections are added in PAMGuard. 
		//		dataUnit.setnSubDetections(nData);
		dataUnit.setCTChi2(chi2.getDoubleValue());
		dataUnit.setLocalisation(new CTLocalisation(dataUnit, null,  this.clickTrainControl.getClickTrainParams().ctLocParams));
		return dataUnit;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#getViewerLoadClause(generalDatabase.SQLTypes, PamController.PamViewParameters)
	 */
	@Override
	public String getViewerLoadClause(SQLTypes sqlTypes, PamViewParameters pvp) {
		//				int loadOption = detectionGroupControl.getDetectionGroupSettings().getOfflineShowOption();
		//				if (loadOption == DisplayOptionsHandler.SHOW_ALL) {
		//					return getViewerEverythingClause(sqlTypes, pvp);
		//				}
		//				else {
		//					return getViewerOverlapClause(sqlTypes, pvp, endTime.getName());
		//				}
		//		Debug.out.println("CLICK DATA BLOCK: " + super.getViewerLoadClause( sqlTypes,  pvp)); 
		return super.getViewerLoadClause(sqlTypes, pvp);
	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		//		int dbInd = dataUnit.getDatabaseIndex();
		//		if (dbInd != 0) {
		//			System.out.println("Relogging data unit that looks like it's already logged");
		//		}
		//		else {
		//			System.out.println("Logging first time data unit that looks like it's already logged");
		//		}
		boolean res = super.logData(con, dataUnit, superDetection);
		//		Debug.out.printf("Log Localisation UID %d db index change %d to %d\n", dataUnit.getUID(), dbInd, dataUnit.getDatabaseIndex());
		return res;
	}

	@Override
	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		// TODO Auto-generated method stub
		//		int dbInd = dataUnit.getDatabaseIndex();
		boolean res = super.reLogData(con, dataUnit, superDetection);
		//		Debug.out.printf("Re-Log Localisation UID %d db index change %d to %d\n", dataUnit.getUID(), dbInd, dataUnit.getDatabaseIndex());
		return res;
	}


}
