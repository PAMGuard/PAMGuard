package clickTrainDetector.classification.idiClassifier;

import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.IDIInfo;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import clickTrainDetector.layout.classification.idiClassifier.IDIClassifierGraphics;

/**
 * An inter-detection interval based classifier for click trains. 
 * 
 * @author Jamie Macaulay
 */
public class IDIClassifier implements CTClassifier {
	
	/**
	 * The IDI parameters. 
	 */
	private IDIClassifierParams idiParams;
	
	/**
	 * The bearing classifier graphics
	 */
	private IDIClassifierGraphics idiClassifierGraphics; 
	
	public IDIClassifier(ClickTrainControl clickTrainControl, int defaultSpeciesID) {
		idiParams = new IDIClassifierParams(); 
		idiParams.speciesFlag=defaultSpeciesID; 
		this.idiClassifierGraphics = new IDIClassifierGraphics(clickTrainControl, this); 
	}

	@Override
	public CTClassification classifyClickTrain(CTDataUnit clickTrain) {
		//check IDI classification is passed
		boolean passesIDI = checkIDIMeasurements(clickTrain); 
		
		return null;
	}

	@Override
	public String getName() {
		return "IDI classifier";
	}

	@Override
	public int getSpeciesID() {
		return idiParams.speciesFlag;
	}

	@Override
	public CTClassifierGraphics getCTClassifierGraphics() {
		return idiClassifierGraphics;
	}

	@Override
	public void setParams(CTClassifierParams ctClassifierParams) {
		this.idiParams=(IDIClassifierParams) ctClassifierParams;
	}
	
	/**
	 * Check IDI measurements for a click train
	 * @return true if all measurements are passed. 
	 */
	private boolean checkIDIMeasurements(CTDataUnit clickTrain) {
		IDIInfo idiInfo = clickTrain.getIDIInfo();
				
		if (idiParams.useMedianIDI && 
				(idiInfo.medianIDI<idiParams.minMedianIDI || idiInfo.medianIDI>idiParams.maxMedianIDI)) {
			return false; 
		}
		
		if (idiParams.useMeanIDI && 
				(idiInfo.meanIDI<idiParams.minMeanIDI || idiInfo.meanIDI>idiParams.maxMeanIDI)) {
			return false; 
		}
		
		if (idiParams.useStdIDI && 
				(idiInfo.stdIDI<idiParams.minStdIDI || idiInfo.stdIDI>idiParams.maxStdIDI)) {
			return false; 
		}
		
		return true; 
	}

	public IDIClassifierParams getParams() {
		return idiParams;
	}


	
	
	
	
}