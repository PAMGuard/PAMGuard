package clickTrainDetector.clickTrainAlgorithms.classificationRatio;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import clickTrainDetector.layout.CTDetectorGraphics;

/**
 * 
 * Click train algorithm which is very simple. It looks at the ratio of unclassified clicks to classified 
 * clicks. If the ratio reaches a certain threshold then a all classified clicks within the time window are 
 * added to a click train. 
 * <p>
 * The ratio algorithm is not particularly sophisticated, however, it is useful for finding areas in the data which 
 * might have high numbers of true positive detections whilst ignoring sections of data that have high numbers of 
 * false positives 
 * 
 * @author Jamie Macaulay 
 *
 */
public class RatioClickTrainAlgorithm implements ClickTrainAlgorithm {

	public RatioClickTrainAlgorithm(ClickTrainControl clickTrainControl) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Ratio Detector";
	}

	@Override
	public void newDataUnit(PamDataUnit<?, ?> dataUnit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CTDetectorGraphics getClickTrainGraphics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(int i, Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CTAlgorithmInfoLogging getCTAlgorithmInfoLogging() {
		// TODO Auto-generated method stub
		return null;
	}

}
