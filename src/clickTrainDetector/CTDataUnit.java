package clickTrainDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import PamUtils.PamArrayUtils;
import PamUtils.avrgwaveform.AverageWaveform;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;

/**
 * 
 * Data unit for a click train which holds a series of data units grouped into a click train. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class CTDataUnit extends CTDetectionGroupDataUnit implements RawDataHolder, AcousticDataUnit 	{

	/**
	 * Chi^2 value for the click train. 
	 */
	public Double chi2 = null;

	/**
	 * The classifications for the click train. This is a list of classifiers 
	 * which have PASSED. i.e. which have an speciesID>0. Only the first classification gets
	 * loaded in from the database. 
	 */
	public ArrayList<CTClassification> ctClassifications = new ArrayList<CTClassification>(); 

	/**
	 * The classification index. This is the correct classification for the data unit. If -1 then the data unit
	 * has not been classified. 
	 */
	private int clssfdIndex = -1; 

	/**
	 * The average waveform. 
	 */
	public AverageWaveform averageWaveform = null;

	/**
	 * The current inter-detection interval info.
	 */
	private IDIInfo currentIDIInfo = null; 

	/**
	 * Flag used to indicate the click train should be deleted. Used in classification processes. 
	 */
	private boolean junkTrain = false;
	
	/**
	 * Any extra information from the click train detection algorithm. 
	 */
	private CTAlgorithmInfo ctAlgorithmInfo;

	/**
	 * Raw data transforms; 
	 */
	private RawDataTransforms rawDataTransforms; 

	/**
	 * The default FFT length for the average templates. 
	 * Made static because of the super constructor adding clicks in function addDetectionList
	 */
	public static int defaultFFTLen = 2048; 

	public CTDataUnit(long timeMilliseconds) {
		super(timeMilliseconds, null);	
		//very strange things were happening when I tried to override the addDetection List here...
		//The average waveform was being calculated but then returning null at the end of the constructor...
		//no idea why or how. Quick fix is to ensure data units are added after instantiation of the class. 

		if (currentIDIInfo==null) currentIDIInfo = new IDIInfo(); 
		
		this.rawDataTransforms = new RawDataTransforms(this); 

	}

	@SuppressWarnings("unchecked")
	@Override
	public int addSubDetections(List<PamDataUnit> list) {

//		Debug.out.println("CTDataUnit: Add data list: " +
//				list);
		
		//have to to do this because addDetectionList is called from super constructor. 
		checkAverageWaveformInfo();
		
		if (list==null) return -1; 

		currentIDIInfo.lastNumber=-1; //forces an update on the IDI manager next time it's called.
		
		//Do not calc here to save processing time calculating new IDI values every time and array is added. 
		PamDataUnit dataUnit; 

		for (int i =0; i<list.size(); i++) {
			dataUnit = list.get(i); 
			for (int j=0; j<dataUnit.getSuperDetectionsCount(); j++) {
				//if the data unit was part of a temporary data unit then remove it!
				if (dataUnit.getSuperDetection(j) instanceof TempCTDataUnit) {
					dataUnit.removeSuperDetection(dataUnit.getSuperDetection(j));
				}
			}
		}

		int result = super.addSubDetections(list); 
		
		this.calcMinMaxAng();

		return result; 
	}
	
	
	@Override
	public int addSubDetection(PamDataUnit subDetection) {		
		
		//Note: when data is loaded in viewer mode from database, then this is called instead of addDetectionList();
				
		//update the average waveform. 
		addToAverageWaveform(subDetection); 
		
		return super.addSubDetection(subDetection);
	}
	
	public void checkAverageWaveformInfo() {
		//have to to do this because addDetectionList is called from super constructor. 
		if (averageWaveform==null || this.getSubDetections() ==null || this.getSubDetections().size()<1) averageWaveform = new AverageWaveform();
		if (currentIDIInfo==null  || this.getSubDetections() ==null || this.getSubDetections().size()<1) currentIDIInfo = new IDIInfo(); 
	}

	/**
	 * Adds to the average waveform is the data unit contains raw info. 
	 * @param dataUnit - the data unit to add.
	 */
	public void addToAverageWaveform(PamDataUnit dataUnit) {
		// add to the average waveform.
		if (dataUnit instanceof RawDataHolder) {
			averageWaveform.addWaveform(((RawDataHolder) dataUnit).getWaveData()[0],
					dataUnit.getParentDataBlock().getSampleRate(), defaultFFTLen, false);
//			Debug.println("CTDataUnit: Add data to raw data holder: " +
//					averageWaveform.getAverageWaveform());
		}
	}

	@Override
	public void removeAllSubDetections() {
//		Debug.out.println("Remove all sub detections: "); 
		super.removeAllSubDetections();
		if (averageWaveform!=null) {
			averageWaveform.clearAvrgData();
			currentIDIInfo= new IDIInfo(); 
		}	
	}

	/**
	 * Get the chi^2 value for the click train. Can be null if 
	 * the click train algorithm does not calculate it. 
	 * @return the chi2 algorithm. 
	 */
	public Double getCTChi2() {
		return chi2; 
	}

	/**
	 * Set the chi^2 value for the click train. 
	 * @param chi^2 the chi^2 value to set.
	 */
	public void setCTChi2(Double chi2) {
		this.chi2 = chi2;
	}


	/**
	 * Get the click train's classification. 
	 * @return the ctClassification
	 */
	public ArrayList<CTClassification> getCtClassifications() {
		return ctClassifications;
	}


	public AverageWaveform averageWaveform() {
		return averageWaveform;
	}

	/**
	 * Get an average waveform for the data unit. 
	 * @return the average waveform 
	 */
	public double[] getAverageWaveform() {
		return this.averageWaveform.getAverageWaveform();
	}

	/**
	 * Get an average spectrum for the data unit. 
	 * @return the average spectrum 
	 */
	public double[] getAverageSpectra() {
		if (averageWaveform==null) return null; 
		return this.averageWaveform.getAverageSpectra();
	}

	/**
	 * Adds a classification for the click train. 
	 * @param ctClassification the ctClassification to set
	 */
	public void addCtClassification(CTClassification ctClassification) {
		this.ctClassifications.add(ctClassification);
	}

	/**
	 * Clear all classification results from the data unit
	 */
	public void clearClassifiers() {
		this.ctClassifications.clear();
	}

	/**
	 * Get the IDI info for the data 
	 * @return the IDIInfo for the click train. 
	 */
	public IDIInfo getIDIInfo() {
		if (currentIDIInfo.lastNumber != this.getSubDetectionsCount()) {
			//Bit of a HACK but 
			currentIDIInfo.calcTimeSeriesData(this.getSubDetections());
		}
		return this.currentIDIInfo; 
	}

	/**
	 * Force an update of the IDI calculation. This should be used if data unit information
	 * changes. The IDI is automaticaly updated when new data units are added to the CTData unit. 
	 */
	public void forceIDIUpdater() {
		currentIDIInfo.lastNumber=-1; 
	}

	/**
	 * Check whether a click train should be junked
	 * @return true to junk train
	 */
	public boolean isJunkTrain() {
		return junkTrain;
	}

	/**
	 * Set whther a click train should be junked. 
	 * @param junkTrain - true to junk train. 
	 */
	public void setJunkTrain(boolean junkTrain) {
		this.junkTrain = junkTrain;
	}

	@Override
	public String getSummaryString() {
		String summaryString = super.getSummaryString(); 

		summaryString+="Number detections: " + this.getSubDetectionsCount() + "<p>";

		summaryString+="Median IDI: " 	+ String.format("%.3f",this.getIDIInfo().medianIDI)+" ";
		summaryString+="Mean IDI: " 	+ String.format("%.3f",this.getIDIInfo().meanIDI)+" ";
		summaryString+="Std IDI: " 		+ String.format("%.3f",this.getIDIInfo().stdIDI)+"<p>";
		
		summaryString+="Total X\u00b2: " 		+ String.format("%.1f",this.getCTChi2())+"<p>";
		
//		Debug.out.println("ctAlgorithmInfo: " + ctAlgorithmInfo); 
		if (ctAlgorithmInfo!=null) {
			summaryString+=ctAlgorithmInfo.getInfoString(); 
		}

		if (this.getCtClassifications().size()>0) {
			summaryString+=String.format("<p>"
					+ "Click Train Classifications: %d Index %d", getCtClassifications().size(), this.getClassificationIndex()); 
			for (int i=0; i<this.getCtClassifications().size(); i++) {
				summaryString+="<p>	" + this.getCtClassifications().get(i).getSummaryString();
				summaryString+=" Classified: " + (this.getCtClassifications().get(i).getSpeciesID()>CTClassifier.NOSPECIES); 
			}
		}

		return  summaryString; 
	}


	/**
	 * The index of the classification (this is to set one classification as the
	 * "master" classifications). This is usually just for convenience, the index
	 * could be calculated by finding the first non zero species code in the
	 * classification list.
	 * 
	 * @param i - the classification index.
	 */
	public void setClassificationIndex(int i) {
		this.clssfdIndex=i; 
	}

	/**
	 * Get the the classification index (this is to set one classification as the
	 * "master" classifications). <0 indicates that no classification was passed.
	 * 
	 * @param i - the classification index.
	 */
	public int getClassificationIndex() {
		return clssfdIndex;
	}

	/**
	 * Set the average waveform. 
	 * @param averageWaveform2 - the average waveform to set. 
	 */
	public void setAverageWaveform(AverageWaveform averageWaveform2) {
		this.averageWaveform=averageWaveform2; 
	}

	@Override
	public double[][] getWaveData() {
		//format average wave data. 
		return new double[][] {this.averageWaveform.getAverageWaveform()};
	}

	/**
	 * Set the click train algorithm info class. 
	 * @param ctAlgorithmInfo
	 */
	public void setCTAlgorithmInfo(CTAlgorithmInfo ctAlgorithmInfo) {
		this.ctAlgorithmInfo=ctAlgorithmInfo; 
	}
	
	/**
	 * Set the click train algorithm info class. This provides extra information from 
	 * the click, train algorithm used.  
	 * @param ctAlgorithmInfo - the click train algorithm info. 
	 */
	public CTAlgorithmInfo getCTAlgorithmInfo() {
		return ctAlgorithmInfo; 
	}

	@Override
	public RawDataTransforms getDataTransforms() {
		return rawDataTransforms;
	}

	/**
	 * Set the current IDI info. 
	 * @param idiInfo - the IDI info. 
	 */
	public void setIDIInfo(IDIInfo idiInfo) {
		this.currentIDIInfo=idiInfo; 
	}
}