package clickTrainDetector.classification.simplechi2classifier;

import PamUtils.PamCalendar;
import PamguardMVC.debug.Debug;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import clickTrainDetector.layout.classification.simplechi2classifier.SimpleCTClassifierGraphics;

/**
 * Simple classifier which has a chi2 threshold
 * 
 * @author Jamie Macaulay 
 *
 */
public class Chi2ThresholdClassifier implements CTClassifier {

	/**
	 * The chi2 threshold to set. This is the chi2 value divided by the number of clicks in the train 
	 */
	public Chi2ThresholdParams clssfrParams = new Chi2ThresholdParams();

	/**
	 * The classifier graphics
	 */
	public CTClassifierGraphics ctClassifierGraphics;

	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;


	public Chi2ThresholdClassifier(ClickTrainControl clickTrainControl, int defaultSpeciesID) {
		this.clickTrainControl=clickTrainControl; 
		clssfrParams.speciesFlag=defaultSpeciesID; 
	}

	
	public Chi2ThresholdClassifier(int defaultSpeciesID) {
		clssfrParams.speciesFlag=defaultSpeciesID; 
	}

	@Override
	public Chi2CTClassification classifyClickTrain(CTDataUnit clickTrain) {

		Debug.out.println("Classify click train: " + clssfrParams.chi2Threshold + " chi2: " + 
		clickTrain.getCTChi2().doubleValue() + " No clks: " + clickTrain.getSubDetectionsCount() + "  "
		+ PamCalendar.formatDateTime2(clickTrain.getTimeMilliseconds(), false));
				
		if (clickTrain.getDurationInMilliseconds()<clssfrParams.minTime*1000.) {
//			Debug.out.println("Failed on duration: " + clickTrain.getListDurationInMillis() + " min time: " + clssfrParams.minTime*1000.); 
			return new Chi2CTClassification(CTClassifier.NOSPECIES); //no classification
		}
		
		if (clickTrain.getSubDetectionsCount()<clssfrParams.minClicks) {
//			Debug.out.println("Failed on min clicks: "); 
			return new Chi2CTClassification(CTClassifier.NOSPECIES); //no classification
		}
		
		if ((clickTrain.getCTChi2().doubleValue())>clssfrParams.chi2Threshold && clssfrParams.chi2Threshold!=0) {
//			Debug.out.println("Failed on chi2Threshold"); 
			return new Chi2CTClassification(CTClassifier.NOSPECIES); //no classification
		}
		
		return new Chi2CTClassification(clssfrParams.speciesFlag);
	}

	@Override
	public String getName() {
		return "X\u00b2 threshold classifier";
	}

	@Override
	public CTClassifierGraphics getCTClassifierGraphics() {
		if (ctClassifierGraphics == null) {
			ctClassifierGraphics = new SimpleCTClassifierGraphics(this); 
		}
		return ctClassifierGraphics;
	}

//	//The click train classifier params. 
//
//	@Override
//	public String getUnitName() {
//		return getName() + "_" + this.clickTrainControl.getUnitName();
//	}
//
//	@Override
//	public String getUnitType() {
//		return getName() ;
//	}
//
//	@Override
//	public Serializable getSettingsReference() {
//		return clssfrParams;
//	}
//
//	@Override
//	public long getSettingsVersion() {
//		return  Chi2ThresholdParams.serialVersionUID;
//	}
//
//	@Override
//	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
//		try {
//			this.clssfrParams = (Chi2ThresholdParams) pamControlledUnitSettings.getSettings();
//			return true;
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			return true; 
//		}
//	}

	/**
	 * Set the classifier parameters. 
	 * @param clssfrParams - the classifier parameters to set. 
	 */
	public void setParams(Chi2ThresholdParams clssfrParams) {
		//System.out.println("HELLO CLASSIFIER PARAMS: " + clssfrParams.chi2Threahold );
		this.clssfrParams=clssfrParams;

	}

	/**
	 * Get the simple classifier parameters. 
	 * @return the classifier parameters. 
	 */
	public Chi2ThresholdParams getParams() {
		return clssfrParams;
	}

	@Override
	public void setParams(CTClassifierParams ctClassifierParams) {
		// TODO Auto-generated method stub
		this.clssfrParams=(Chi2ThresholdParams) ctClassifierParams;
	}

	@Override
	public int getSpeciesID() {
		return this.clssfrParams.speciesFlag;
	}


}
