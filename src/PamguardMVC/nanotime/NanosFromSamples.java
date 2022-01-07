package PamguardMVC.nanotime;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;

/**
 * Calculate the nanosecond time from the sample number. Generally, this will be OK
 * but there are times when it could go wrong in Viewer mode when data are loaded over
 * a PAMguard stop / start. For time matching, this should not be a problem since this will 
 * not happen over a stop/start bounday in any case (hopefully not anyway). 
 * @author dg50
 *
 */
public class NanosFromSamples implements NanoTimeCalculator {
	
	@Override
	public long getNanoTime(PamDataUnit pamDataUnit) {
		double sr = pamDataUnit.getParentDataBlock().getSampleRate();
		// floating point resolution on 500kHz sample rate after 1 day is 7.6e-6 i.e. Matlab eps(500e3*3600*24) = 7.6294e-06
		// which means that you'd have one sample resolution after 360 years of sampling. 
		return PamCalendar.getSessionStartTime() * NANOSTOMILLS + (long) ((double) pamDataUnit.getStartSample() / sr * NANOSTOSECONDS_F);
	}

}
