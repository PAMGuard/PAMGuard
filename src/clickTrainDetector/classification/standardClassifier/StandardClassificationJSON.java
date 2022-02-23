package clickTrainDetector.classification.standardClassifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.SimpleClassifierJSONLogging;
import clickTrainDetector.classification.bearingClassifier.BearingClassification;
import clickTrainDetector.classification.bearingClassifier.BearingClassifier;
import clickTrainDetector.classification.idiClassifier.IDIClassification;
import clickTrainDetector.classification.idiClassifier.IDIClassifier;
import clickTrainDetector.classification.simplechi2classifier.Chi2CTClassification;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.templateClassifier.CTTemplateClassifier;
import clickTrainDetector.classification.templateClassifier.TemplateClassification;


/**
 * Standard classification JSON logging
 * 
 * @author Jamie Macaulay 
 *
 */
public class StandardClassificationJSON extends SimpleClassifierJSONLogging {	
	
	private CTClassification[] standardClassifier;

	/**
	 * Create the JSON logging for standard classifier. 
	 * @param ctClassifications - the standard classifier.
	 */
	public StandardClassificationJSON(CTClassification[] ctClassifications) {
		this.standardClassifier=ctClassifications; 
	}
	
	public StandardClassificationJSON() {
		
	}
	
	

	@Override
	public void writeJSONData(JsonGenerator jg, CTClassification ctClassification) {
		for (int i=0; i<standardClassifier.length; i++) {
			((SimpleClassifierJSONLogging) standardClassifier[i].getJSONLogging()).writeJSONData(jg, standardClassifier[i]);
		}
	}
	
	
	@Override
	public CTClassification createClassification(String jsonString) {
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jsonString.getBytes()));
			//			JsonNode nv = jTree.findValue("NAME");


			JsonNode na = jTree.findValue("SPECIES");
			int speciesID;
			if (na != null ) {
				speciesID = na.asInt(); 
			}
			else {
				System.err.println("Cannot load standard classifier");
				return null; 
			}

			//System.out.println("Hello load classification: " + standardClassifier.length); 
			CTClassification[] ctClassification = new CTClassification[StandardClassifier.CLASSIFIER_TYPES.length]; 


			for (int i=0; i<ctClassification.length; i++) {
				CTClassification classification = null; 
				switch (StandardClassifier.CLASSIFIER_TYPES[i]) {
				case CHI2THRESHOLD:
					classification = new Chi2CTClassification(jsonString); 
					break;
				case IDICLASSIFIER:
					classification = new IDIClassification(jsonString); 
					break;
				case TEMPLATECLASSIFIER:
					classification = new TemplateClassification(jsonString); 
					break;
				case BEARINGCLASSIFIER:
					classification = new BearingClassification(jsonString);
					break;
				default:
					System.err.println("StandardClassification: classifier JSON not found");
					break; 
				}
				ctClassification[i] = classification; 
			}

			StandardClassification stClassification = 
					new StandardClassification(ctClassification, speciesID); 

			return stClassification; 

		} catch (IOException e) {
			System.err.println("Classification interpreting " + jsonString);
			e.printStackTrace();
			return null;
		}
	}

	


}
