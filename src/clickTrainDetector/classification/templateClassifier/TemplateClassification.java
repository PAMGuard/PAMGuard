package clickTrainDetector.classification.templateClassifier;

import PamUtils.PamArrayUtils;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.classification.ClassifierJSONLogging;

/**
 * Classification result for the template classifier. 
 * 
 * @author Jamie Macaulay
 *
 */
public class TemplateClassification implements CTClassification {
	
	/**
	 * The species ID. 
	 */
	private int speciesID;
	
	/**
	 * The correlation value for the average spectrum and waveform. 
	 */
	private double correlationVal;
	
	/**
	 * JSON logging for the template classifier. 
	 */
	private TemplateClassifierJSONLogging templateClassifierJSONLogging;

	/**
	 * The template classification. 
	 */
	public TemplateClassification(int speciesID, double correlationVal) {
		this.speciesID= speciesID; 
		this.correlationVal =correlationVal; 
		templateClassifierJSONLogging = new TemplateClassifierJSONLogging(); 
	}
	
	/**
	 * Create the classification from a JSON string
	 * @param clickTrainControl - the click train control. 
	 * @param jsonstring - the json string. 
	 */
	public TemplateClassification(String jsonstring) {
		templateClassifierJSONLogging = new TemplateClassifierJSONLogging(); 
		CTClassification classification  = templateClassifierJSONLogging.createClassification(jsonstring); 
		this.speciesID=classification.getSpeciesID(); 
		this.correlationVal=((TemplateClassification) classification).getCorrelationValue();
	}

	@Override
	public int getSpeciesID() {
		return speciesID;
	}
	
	/**
	 * The correlation value.
	 * @return the correlation value. 
	 */
	public double getCorrelationValue() {
		return correlationVal;
	}

	@Override
	public String getSummaryString() {
		return String.format("Template X\u00b2 Clssfr: Species ID: %d Corr. Value: %.2f " , this.getSpeciesID(), this.correlationVal);
	}

	@Override
	public CTClassifierType getClassifierType() {
		return CTClassifierType.TEMPLATECLASSIFIER;
	}

	@Override
	public ClassifierJSONLogging getJSONLogging() {
		return templateClassifierJSONLogging;
	}
	
	public static void main(String[] args) {
		double[] arr = {0,0,1,1,0,0,0,0,0,0};
		double[] normarr = PamArrayUtils.normalise(arr); 
		PamArrayUtils.printArray(normarr);
	}

}
