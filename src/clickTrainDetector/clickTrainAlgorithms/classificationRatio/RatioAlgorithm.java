package clickTrainDetector.clickTrainAlgorithms.classificationRatio;

import clickTrainDetector.ClickTrainControl;

/**
 * Algorithm which simply looks at the ration of one click type to another. If the ratio is above a predefined 
 * value for a specified time window then the click train is saved.
 * @author Jamie Macaulay
 *
 */
public class RatioAlgorithm extends RatioClickTrainAlgorithm {

	public RatioAlgorithm(ClickTrainControl clickTrainControl, int channelMap) {
		super(clickTrainControl);
	}

}
