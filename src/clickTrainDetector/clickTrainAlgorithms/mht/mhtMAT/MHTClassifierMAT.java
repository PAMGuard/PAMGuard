package clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT;

import java.util.ArrayList;

import clickTrainDetector.CTDataUnit;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.bearingClassifier.BearingClassifier;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.templateClassifier.CTTemplateClassifier;
import clickTrainDetector.classification.templateClassifier.TemplateClassification;

/**
 * MHT classifiers for the Java / MATLAB interface.
 * 
 * @author Jamie Macaulay
 *
 */
public class MHTClassifierMAT {
	
	/**
	 * List of all the current click train classifiers which add annotations to data data unit
	 * but do not result in click train being deleted if they do not pass classification. 
	 */
	private ArrayList<CTClassifier> cTClassifiers = new ArrayList<CTClassifier>();
	
	
	public MHTClassifierMAT() {
		
	}
	
	/**
	 * Add a classifier based on an input settings class and type string
	 * @param type - classifier type. Option are "Template", "Simple" and "Bearing"
	 * @param clssfrsettings
	 */
	public void addClassifier(String type, CTClassifierParams clssfrsettings) {
		CTClassifier classifier = null; 
		switch (type) {
		case "Template":
			classifier = new CTTemplateClassifier(clssfrsettings.speciesFlag); 
			classifier.setParams(clssfrsettings);
			break;
		case "Simple":
			classifier = new Chi2ThresholdClassifier(clssfrsettings.speciesFlag); 
			classifier.setParams(clssfrsettings);
			break;
		case "Bearing":
			classifier = new BearingClassifier(clssfrsettings.speciesFlag); 
			classifier.setParams(clssfrsettings);
			break;
		}
		cTClassifiers.add(classifier); 
	}
	
	
	/**
	 * Add classification info to a CTData unit
	 * @param dataUnit - the data unit to classify 
	 */
	public void classify(CTDataUnit dataUnit) {
		CTClassification classification; 
		boolean hasBeenClssfd = false; 
		for (int i = 0; i<cTClassifiers.size(); i++) {
			classification  = cTClassifiers.get(i).classifyClickTrain(dataUnit); 
			dataUnit.addCtClassification(classification);
			
//			System.out.println("Classification " + classification.getSpeciesID() + " speciesID " + cTClassifiers.get(i).getSpeciesID()); 

			if (classification.getSpeciesID()>CTClassifier.NOSPECIES && !hasBeenClssfd) {
//				System.out.println("Set classiifcation index: " + i); 
				dataUnit.setClassificationIndex(i); //set the classification index. 
				hasBeenClssfd = true; 
			}
		}
	
	}
	
	

}
