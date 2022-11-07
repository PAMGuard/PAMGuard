package clickTrainDetector.classification.simplechi2classifier;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.classification.ClassifierJSONLogging;
import clickTrainDetector.classification.SimpleClassifierJSONLogging;

/**
 * 
 * Class to hold basic classification data for a click train. This usually 
 * acts as the Default simple classification. Most other classifications
 * should sublcass this. 
 * 
 * @author Jamie Macaulay
 *
 */
public class Chi2CTClassification implements CTClassification {
	
	/**
	 * The species code 
	 */
	private int speciesCode = -1;
	
	
	private SimpleClassifierJSONLogging simpleClassifierJSONLogging;


	public Chi2CTClassification(int speciesCode) {
		this.speciesCode = speciesCode; 
		simpleClassifierJSONLogging=new SimpleClassifierJSONLogging(); 
	}


	/**
	 * Create the classification from a JSON string
	 * @param clickTrainControl - the click train control. 
	 * @param jsonstring - the json string. 
	 */
	public Chi2CTClassification(String jsonstring) {
		simpleClassifierJSONLogging=new SimpleClassifierJSONLogging(); 
		CTClassification classification  = simpleClassifierJSONLogging.createClassification(jsonstring); 
		this.speciesCode=classification.getSpeciesID(); 
	}

	@Override
	public int getSpeciesID() {
		return speciesCode;
	}

	@Override
	public String getSummaryString() {
		return "X\u00b2 Clssfr: Species ID: " + this.getSpeciesID();
	}
	
	@Override
	public CTClassifierType getClassifierType() {
		return CTClassifierType.CHI2THRESHOLD;
	}
	
	@Override
	public ClassifierJSONLogging getJSONLogging() {
		return simpleClassifierJSONLogging;
	}
	
	

}
