package rawDeepLearningClassifier.dlClassification;

import java.util.List;

import PamDetection.PamDetection;
import PamView.GeneralProjector;
import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;

/*8
 * A deep learning detection which is derived from a group of data units. 
 */
public class DLGroupDetection extends SegmenterDetectionGroup implements PamDetection {

	public DLGroupDetection(long timeMilliseconds, int channelBitmap, long startSample, double duration, List<PamDataUnit> list) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.addSubDetections(list);
	}



}
