package seismicVeto;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

public class VetoDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements PamDetection {

	public VetoDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);

	}
}
