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


	public DLGroupDetection(long timeMilliseconds, int channelBitmap, long startSample, double duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

	@Override
	public double[] getFrequency() {
		if ((super.getFrequency()==null || super.getFrequency()[0]  == super.getFrequency()[1]) && this.getSubDetectionsCount()>0) {

			double minFreq =  Double.POSITIVE_INFINITY;
			double maxFreq =  Double.NEGATIVE_INFINITY;

			double[] freq;
			
			
			for (int j=0; j<getSubDetectionsCount(); j++) {


				freq = getSubDetection(j).getFrequency();
				if (freq[0]<minFreq) {
					minFreq = freq[0];
				}
				if (freq[1]>maxFreq) {
					maxFreq = freq[1];
				}

			}
			this.setFrequency(new double[] {minFreq, maxFreq});
		}
		return super.getFrequency();
	}

	@Override
	public int getSubDetectionsCount() {
		
		for (int j=0; j<getSubDetectionsCount(); j++) {
			if (getSubDetection(j) != null) return 0;
		}
		return super.getSubDetectionsCount();
	}

}
