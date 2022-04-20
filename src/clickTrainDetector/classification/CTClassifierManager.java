package clickTrainDetector.classification;

import java.util.ArrayList;

import PamUtils.PamCalendar;
import PamguardMVC.debug.Debug;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.bearingClassifier.BearingClassification;
import clickTrainDetector.classification.bearingClassifier.BearingClassifier;
import clickTrainDetector.classification.simplechi2classifier.Chi2CTClassification;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.standardClassifier.StandardClassification;
import clickTrainDetector.classification.standardClassifier.StandardClassifier;
import clickTrainDetector.classification.templateClassifier.TemplateClassification;
import clickTrainDetector.classification.templateClassifier.CTTemplateClassifier;

/**
 * The classification manager for the click train detector. 
 * <p>
 * This handles the various types of classifiers.
 * 
 * @author Jamie Macaulay 
 */
public class CTClassifierManager {

	/**
	 * Pre classifier which is used to junk click train initially rather than classification 
	 */
	private Chi2ThresholdClassifier  preClassifier;

	/**
	 * List of all the current click train classifiers which add annotations to data data unit
	 * but do not result in click train being deleted if they do not pass classification. 
	 */
	private ArrayList<CTClassifier> cTClassifiers = new ArrayList<CTClassifier>();

	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	public CTClassifierManager(ClickTrainControl clickTrainControl) {
		this.clickTrainControl = clickTrainControl; 
		this.preClassifier = new Chi2ThresholdClassifier(clickTrainControl, -1); 
	}

	@Deprecated
	public String getClassifierName(CTClassifierType classifierType) {
		return classifierType.toString(); 
//		switch (classifierType) {
//		case CHI2THRESHOLD:
//			return "X\u00b2 threshold classifier";
//		case TEMPLATECLASSIFIER:
//			return "Spectral Template";
//		case BEARINGCLASSIFIER:
//			return "Bearing Classifier";
//		case STANDARDCLASSIFIER:
//			return "Click Train Classifier";
//			/////****ADD NEW CLASSIFIERS HERE****/////
//		default:
//			return ""; 
//		}
	}

	/**
	 * Get the number of classifier types. 
	 * @return the number of classifier types. 
	 */
	public int getNClassifierTypes() {
		return CTClassifierType.values().length;
	}


	/**
	 * CT classifier
	 * 
	 * @return the CT classifier 
	 */
	public CTClassifier createClassifier(CTClassifierType classifierType) {
		if (classifierType==null) return null; 
		switch (classifierType) {
		case CHI2THRESHOLD:
			return new Chi2ThresholdClassifier(clickTrainControl, 1); 
		case TEMPLATECLASSIFIER:
			return new CTTemplateClassifier(clickTrainControl, 1);
		case BEARINGCLASSIFIER:
			return new BearingClassifier(clickTrainControl, -1); 
		case STANDARDCLASSIFIER:
			return new StandardClassifier(clickTrainControl, -1); 
			/////****ADD NEW CLASSIFIERS HERE****/////
		default:
			return new Chi2ThresholdClassifier(clickTrainControl, 1); 
		}
	}


	/**
	 * Get a <i>classification</i> from a string;
	 * @return the classification. 
	 */
	public CTClassification jsonString2Classification(String jsonstring) {

		SimpleClassifierJSONLogging simpleJSON = new SimpleClassifierJSONLogging(); 
		SimpleCTClassification  classification = (SimpleCTClassification) simpleJSON.createClassification(jsonstring);
		
//		if (classification==null) {
//			Debug.err.println("ClassificationManager:  Classification null: " + jsonstring);
//			jsonstring=jsonstring+"}"; 
//			 classification = (SimpleCTClassification) simpleJSON.createClassification(jsonstring);
//		}
//		else if (classification.getClassifierType()==null) {
//			Debug.err.println("ClassificationManager:  Classification type is null: " + jsonstring);
//			jsonstring=jsonstring+"}"; 
//			 classification = (SimpleCTClassification) simpleJSON.createClassification(jsonstring);
//		}
		
		switch (classification.getClassifierType()) {
		case CHI2THRESHOLD:
			return new Chi2CTClassification(jsonstring); 
		case TEMPLATECLASSIFIER:
			return new TemplateClassification(jsonstring); 
		case BEARINGCLASSIFIER:
			return new BearingClassification(jsonstring); 
		case STANDARDCLASSIFIER:
			return new StandardClassification(jsonstring); 
			/////****ADD NEW CLASSIFICATIONTYPES HERE****/////
		default:
			return null; 
		}
	}


	/**
	 * Get the current classifiers. 
	 * @return the current classifier list. 
	 */
	public ArrayList<CTClassifier> getCurrentClassifiers() {
		return cTClassifiers;
	}


	/**
	 * Create a classifier. 
	 * @param clssfrIndex - the classifier index
	 * @return the classifier
	 */
	public CTClassifier createClassifier(int clssfrIndex) {
		return createClassifier(CTClassifierType.values()[clssfrIndex]);
	}

	/**
	 * Classify a CTData unit base on current classifiers,. 
	 * @param ctDataUnit - the data unit to classify
	 * @return - the same data unit with classification updated (not cloned)
	 */
	public CTDataUnit classify(CTDataUnit ctDataUnit) {
		//first check the pre-classifier
		Chi2CTClassification classification = this.preClassifier.classifyClickTrain(ctDataUnit); 
		
		System.out.println("Pre classifier: " + PamCalendar.formatDateTime(ctDataUnit.getTimeMilliseconds()) + " N. " + ctDataUnit.getSubDetectionsCount() + "UID first: " + ctDataUnit.getSubDetection(0).getUID() ); 

		if (classification.getSpeciesID()==CTClassifier.NOSPECIES) {
			System.out.println("No SPECIES: chi^2" + ctDataUnit.getCTChi2()); 
			ctDataUnit.setJunkTrain(true);
			//no need to do any more classification- the click train has been flagged for deletion.
			ctDataUnit.clearClassifiers();
			return ctDataUnit;
		}
		
		//if no classifiers enabled no need to go any further
		if (!clickTrainControl.getClickTrainParams().runClassifier) return ctDataUnit;

		return classifySpecies(ctDataUnit); 
	}
	
	/**
	 * Classify the species of the CTDataUnit., 
	 * @param ctDataUnit - the ctDataUnit
	 * @return ctDataUnit with classification info added. 
	 */
	public CTDataUnit classifySpecies(CTDataUnit ctDataUnit) {
		
		//classify the click train. 
		CTClassification ctclassification;
		boolean hasBeenClssfd = false;
		
		//make sure to clear old classifiers. 
		ctDataUnit.clearClassifiers();
		ctDataUnit.setClassificationIndex(-1);
		
		System.out.println("Classify species: Num classifier " +  this.cTClassifiers.size()); 

		for (int i=0; i<this.cTClassifiers.size(); i++) {
			
		
			System.out.println("Classifier: " + i); 

			//the first classifier 
			ctclassification = this.cTClassifiers.get(i).classifyClickTrain(ctDataUnit);
			
			System.out.println("Classifier complete: SPECIES: " + ctclassification.getSpeciesID()); 

			
//			Debug.out.println(i + " ClassifierManager: Classify a click train data unit: " + ctDataUnit  + " parent data block: " +
//					" " + ctDataUnit.getnSubDetections()); 
			ctDataUnit.addCtClassification(ctclassification);
			
			//set the species flag but only if this is the first time the ct data unit has been classified. 
			if (ctclassification.getSpeciesID()>CTClassifier.NOSPECIES && !hasBeenClssfd) {
				System.out.println("Set classiifcation index: " + i); 
				ctDataUnit.setClassificationIndex(i); //set the classification index. 
				hasBeenClssfd = true; 
			}
		}

		return ctDataUnit;
	}


	/**
	 * Setup classifiers based on the list of classifier params in the list.
	 */
	public void setupClassifiers() {
		//set params for pre-classifier
		this.preClassifier.setParams(clickTrainControl.getClickTrainParams().simpleCTClassifier); 

		cTClassifiers.clear();

		CTClassifierParams[] ctParams = clickTrainControl.getClickTrainParams().ctClassifierParams; 

		if (ctParams==null) return; 

		CTClassifier aClassifier; 
		for (int i=0; i<ctParams.length; i++) {
			aClassifier = createClassifier(ctParams[i].type); 
			if (aClassifier==null) {
				System.err.println("CTCLassifier manager: the classifier is null");
				continue; 
			}
			aClassifier.setParams(ctParams[i]); 
			cTClassifiers.add(aClassifier); 
		}

	}




}
