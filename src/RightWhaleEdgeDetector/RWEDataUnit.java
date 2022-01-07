package RightWhaleEdgeDetector;

import PamguardMVC.DataUnitBaseData;
import whistlesAndMoans.AbstractWhistleDataUnit;

public class RWEDataUnit extends AbstractWhistleDataUnit {

	public RWESound rweSound;
	
	public RWEDataUnit(long timeMilliseconds, int channelBitmap,
			long startSample, long duration,  RWESound rweSound) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.rweSound = rweSound;
		// TODO Auto-generated constructor stub
	}

	public RWEDataUnit(DataUnitBaseData basicData,  RWESound rweSound) {
		super(basicData);
		this.rweSound = rweSound;
	}

	double[] freqsHz;
	@Override
	public double[] getFreqsHz() {
		if (freqsHz == null) {
			freqsHz = new double[rweSound.sliceCount];
		}
		return null;
	}

	@Override
	public int getSliceCount() {
		return rweSound.sliceCount;
	}

	@Override
	public double[] getTimesInSeconds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSummaryString() {
		String string = super.getSummaryString();
		string += String.format("RW Edge Score: %d, %s", rweSound.soundType, rweSound.getTypeString());
		return string;
	}

}
