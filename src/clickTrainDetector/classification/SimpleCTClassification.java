package clickTrainDetector.classification;

/**
 * A holder class which is convenient for sub classing. 
 * @author Jamie Macaulay
 *
 */
public class SimpleCTClassification implements CTClassification {
	
	private CTClassifierType classifierType; 
	
	private int speciesID;

	private SimpleClassifierJSONLogging jsonLogging; 
	
	public SimpleCTClassification(int speciesID, CTClassifierType classifierType) {
		this.classifierType=classifierType; 
		this.speciesID=speciesID;
		this.jsonLogging = new SimpleClassifierJSONLogging(); 
	}

	@Override
	public CTClassifierType getClassifierType() {
		return classifierType;
	}

	@Override
	public int getSpeciesID() {
		return speciesID;
	}

	@Override
	public String getSummaryString() {
		return "SimpleClssf: SpeciesID: " + speciesID + " ClassifierType: " + classifierType;
	}

	@Override
	public ClassifierJSONLogging getJSONLogging() {
		return jsonLogging;
	}

}
