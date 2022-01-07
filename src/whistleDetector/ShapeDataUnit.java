package whistleDetector;

import whistlesAndMoans.AbstractWhistleDataUnit;

/**
 * Example class to go with WslPeak to show how WslPeaks, WslShapes and
 * WslEvents all go together. 
 * @author Doug
 *
 */
public class ShapeDataUnit extends AbstractWhistleDataUnit {


	private WhistleShape whistleShape; 
	
	public WhistleShape getWhistleShape() {
		return whistleShape;
	}

	public void setWhistleShape(WhistleShape whistleShape) {
		this.whistleShape = whistleShape;
	}

	public ShapeDataUnit(long timeMilliseconds, int channelBitmap, long startSample, int duration,
			WhistleShape whistleShape) {
		super(timeMilliseconds,  channelBitmap, startSample, 
				duration);
		this.whistleShape = whistleShape;

	}
	
	private double[] freqData;
	@Override
	public double[] getFreqsHz() {
		WhistleDataBlock db = (WhistleDataBlock) getParentDataBlock();
		int L = getSliceCount();
		if (freqData == null || freqData.length != L) {
			freqData = new double[L];
			for (int i = 0; i < L; i++) {
				freqData[i] = db.binsToHz(whistleShape.GetPeak(i).PeakFreq);
			}
		}
		
		return freqData;
	}

	@Override
	public int getSliceCount() {
		return whistleShape.getSliceCount();
	}

	private double[] timeData;
	@Override
	public double[] getTimesInSeconds() {
		WhistleDataBlock db = (WhistleDataBlock) getParentDataBlock();
		int L = getSliceCount();
		if (timeData == null || timeData.length != L) {
			timeData = new double[L];
			for (int i = 0; i < L; i++) {
				timeData[i] = db.binsToSeconds(whistleShape.GetPeak(i).sliceNumber);
			}
		}
		return timeData;
	}

	@Override
	public double getAmplitudeDB() {
		return whistleShape.getDBAmplitude();
	}

	@Override
	public double getCalculatedAmlitudeDB() {
		return whistleShape.getDBAmplitude();
	}

}
