package dataPlotsFX.scrollingPlot2D;

import PamguardMVC.DataUnit2D;
import PamguardMVC.DataUnitBaseData;

public class DummyDataUnit2D extends DataUnit2D {

	public DummyDataUnit2D(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
	}

	public DummyDataUnit2D(DataUnitBaseData basicData) {
		super(basicData);
	}

	public DummyDataUnit2D(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	@Override
	public double[] getMagnitudeData() {
		return null;
	}

}
