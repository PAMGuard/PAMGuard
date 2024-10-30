package rawDeepLearningClassifier.dlClassification;

import java.util.List;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/*8
 * A deep learning detection which is derived from a group of data units. 
 */
public class DLGroupDetection  extends DetectionGroupDataUnit implements PamDetection {

	public DLGroupDetection(long timeMilliseconds, List<PamDataUnit> list) {
		super(timeMilliseconds, list);
		// TODO Auto-generated constructor stub
	}


}
