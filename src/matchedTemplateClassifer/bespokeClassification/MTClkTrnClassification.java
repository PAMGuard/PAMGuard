package matchedTemplateClassifer.bespokeClassification;

import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.classification.SimpleCTClassification;

/**
 * A classification for a click train. 
 * @author Jamie Macaulay
 *
 */
public class MTClkTrnClassification extends SimpleCTClassification {

	public MTClkTrnClassification(int speciesID) {
		super(speciesID, CTClassifierType.MATCHEDCLICK);
		// TODO Auto-generated constructor stub
	}

}
