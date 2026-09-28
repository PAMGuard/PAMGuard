package clickTrainDetector;

import java.util.ArrayList;
import java.util.List;

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
 * <p>
 * The average waveform and IDI calculations are inherited from
 * {@link PamguardMVC.superdet.AcousticDetectionGroup}.
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

	public CTDataUnit(long timeMilliseconds) {
		super(timeMilliseconds, null);	
		//very strange things were happening when I tried to override the addDetection List here...
		//The average waveform was being calculated but then returning null at the end of the constructor...
		//no idea why or how. Quick fix is to ensure data units are added after instantiation of the class. 

		setCalculateAverages(true);
		checkAverageWaveformInfo();

		this.rawDataTransforms = new RawDataTransforms(this); 

	}

	@SuppressWarnings("unchecked")
	@Override
	public int addSubDetections(List<PamDataUnit> list) {

		//have to to do this because addDetectionList is called from super constructor. 
		checkAverageWaveformInfo();
		
		if (list==null) return -1; 

		forceIDIUpdater(); //forces an update on the IDI manager next time it's called.
		
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
		
		summaryString+="Total X²: " 		+ String.format("%.1f",this.getCTChi2())+"<p>";
		
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

	@Override
	public double[][] getWaveData() {
		//format average wave data. 
		if (averageWaveform == null) return null;
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

}
