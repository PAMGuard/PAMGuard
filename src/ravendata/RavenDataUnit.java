package ravendata;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class RavenDataUnit extends PamDataUnit implements AcousticDataUnit, PamDetection {

	public RavenDataUnit(long timeMilliseconds, int channelMap, long durationMillis, double f1, double f2) {
		super(timeMilliseconds);
		setChannelBitmap(channelMap);
		setDurationInMilliseconds(durationMillis);
		double[] freq = {f1, f2};
		setFrequency(freq);
	}



}
