package RightWhaleEdgeDetector;

import PamguardMVC.DataUnitBaseData;
import whistlesAndMoans.AbstractWhistleDataUnit;

public class RWEDataUnit extends AbstractWhistleDataUnit {

	public RWESound rweSound;
	private RWEProcess rweProcess;
	
	public RWEDataUnit(RWEProcess rweProcess, long timeMilliseconds, int channelBitmap,
			long startSample, long duration,  RWESound rweSound) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.rweSound = rweSound;
		this.rweProcess = rweProcess;
		// TODO Auto-generated constructor stub
	}

	public RWEDataUnit(RWEProcess rweProcess, DataUnitBaseData basicData,  RWESound rweSound) {
		super(basicData);
		this.rweSound = rweSound;
		this.rweProcess = rweProcess;
	}

	@Override
	public double[] getFreqsHz() {
		double[] f = new double[rweSound.sliceCount];
		RWEDataBlock rweDataBlock = rweProcess.getRweDataBlock();
		double binToHz = rweDataBlock.getSampleRate() / rweDataBlock.getFftLength();
		for (int i = 0; i < f.length; i++) {
			f[i] = (double) rweSound.peakFreq[i] * binToHz;
		}
		return f;
	}

	@Override
	public int getSliceCount() {
		return rweSound.sliceCount;
	}

	@Override
	public double[] getTimesInSeconds() {
		if (rweSound == null) {
			return null;
		}
		double[] t = new double[rweSound.sliceCount];
		RWEDataBlock rweDataBlock = rweProcess.getRweDataBlock();
		double binToT = rweDataBlock.getFftHop() / rweDataBlock.getSampleRate();
		for (int i = 0; i < t.length; i++) {
			t[i] = (double) rweSound.sliceList[i] * binToT;
		}
		return t;
	}

	@Override
	public String getSummaryString() {
		String string = super.getSummaryString();
		string += String.format("RW Edge Score: %d, %s", rweSound.soundType, rweSound.getTypeString());
		return string;
	}

}
