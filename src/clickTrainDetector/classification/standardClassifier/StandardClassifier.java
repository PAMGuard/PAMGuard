package clickTrainDetector.classification.standardClassifier;

import java.util.ArrayList;

import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.bearingClassifier.BearingClassifier;
import clickTrainDetector.classification.bearingClassifier.BearingClassifierParams;
import clickTrainDetector.classification.idiClassifier.IDIClassification;
import clickTrainDetector.classification.idiClassifier.IDIClassifier;
import clickTrainDetector.classification.simplechi2classifier.Chi2CTClassification;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.templateClassifier.CTTemplateClassifier;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import clickTrainDetector.layout.classification.standardClassifier.StandardClassifierGraphics;

/**
 * Combines the IDI, CHI2, BEARING and TEMPLATE classifier into one. 

 * @author Jamie Macaulay
 *
 */
public class StandardClassifier implements CTClassifier {
	
	/**
	 * The bearing classifier parameters. 
	 */
	private StandardClassifierParams standardClssfrParams = new StandardClassifierParams();
	
	/**
	 * The graphics for changing settings. 
	 */
	private StandardClassifierGraphics standardClassifierGraphics; 
	
	/**
	 * List of the classifiers used. 
	 */
	private ArrayList<CTClassifier> classifiers;
	
	/**
	 * Click train control. 
	 */
	private ClickTrainControl clickTrainControl; 

	public StandardClassifier(ClickTrainControl clickTrainControl, int speciesID) {
		this.clickTrainControl = clickTrainControl; 
		standardClassifierGraphics = new StandardClassifierGraphics(clickTrainControl, this); 
		standardClssfrParams.speciesFlag=speciesID; 
		
		//load the settings
		createClassifiers(); 

	}
	
	/**
	 * Create the classifier. Each sub classifier (if enabled) must be passed for a positive classification. 
	 */
	private void createClassifiers() {
		classifiers = new ArrayList<CTClassifier>(); 
		
		classifiers.add(new Chi2ThresholdClassifier(clickTrainControl, standardClssfrParams.speciesFlag));
		
		classifiers.add(new IDIClassifier(clickTrainControl, standardClssfrParams.speciesFlag));
			
		classifiers.add(new CTTemplateClassifier(clickTrainControl, standardClssfrParams.speciesFlag));
		
		classifiers.add(new BearingClassifier(clickTrainControl, standardClssfrParams.speciesFlag));
		
		setClassifierParams();
	}
	
	/**
	 * Set the parameters for the individual classifiers
	 */
	private void setClassifierParams() {
		
		if (standardClssfrParams.ctClassifierParams ==null || standardClssfrParams.ctClassifierParams.length != classifiers.size()) {
			standardClssfrParams.ctClassifierParams = new CTClassifierParams[classifiers.size()];  
			standardClssfrParams.enable = new boolean[classifiers.size()]; 
			//enable all classifiers by default. 
			for (int i=0; i<classifiers.size(); i++) {
				standardClssfrParams.enable[i] = true; //default is false
			}
		}
		
		for (int i=0; i<classifiers.size(); i++) {
			if (standardClssfrParams.ctClassifierParams[i]==null) {
				//set default settings
				standardClssfrParams.ctClassifierParams[i]= classifiers.get(i).getParams(); 
			}
			else {
				//the standard classifier should have settings set. 
				classifiers.get(i).setParams(standardClssfrParams.ctClassifierParams[i]); 
			}
		}
	}

	@Override
	public CTClassification classifyClickTrain(CTDataUnit clickTrain) {
		
		
		//all classifiers have to pass.  
		
	
		return null;
	}

	@Override
	public String getName() {
		return "Standard Classifier";
	}

	@Override
	public int getSpeciesID() {
		return standardClssfrParams.speciesFlag;
	}

	@Override
	public CTClassifierGraphics getCTClassifierGraphics() {
		return standardClassifierGraphics;
	}

	@Override
	public void setParams(CTClassifierParams ctClassifierParams) {
		this.standardClssfrParams=(StandardClassifierParams) ctClassifierParams;
		setClassifierParams(); 
	}

	public StandardClassifierParams getParams() {
		return standardClssfrParams;
	}

	/**
	 * Get the classifiers the standard classifier uses. . 
	 * @return the classifiers. 
	 */
	public ArrayList<CTClassifier> getClassifiers() {
		return classifiers;
	}

}
