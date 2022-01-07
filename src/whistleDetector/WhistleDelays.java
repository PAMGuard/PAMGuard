package whistleDetector;

import PamguardMVC.PamDataBlock;

/**
 * Class containing functions to calulate the delay between 
 * a detected whistle on one channel and the signal on 
 * an adjacent channel. 
 * <p>
 * Existing FFT (spectrgram) data is used during the calculation.
 * <p> The correlation function is calculated for each time partition 
 * along the length of the whistle and these correlations summed
 * to overcome the problem of aliassing. 
 * <p> As the correlations are caculated, the spectrum is band
 * limited in each time partition to only include acoustic
 * energy close to the whistle contour. In this way, it is
 * possible to independently calculate bearings to simultaneously
 * occurring overlapping whistles. 
 * @author Doug Gillespie
 *
 */
public class WhistleDelays {
	
	static final public int INVALID_DELAY = Integer.MIN_VALUE;
	static final public int NO_CHANNEL_DATA = Integer.MIN_VALUE+1;

	/**
	 * Calculate the delay.<p>
	 * The whistle knows which channel it was detected on. Tell
	 * this functin which channel to calculate the dealy to. If
	 * FFT data for that channel does not exist, then return NO_CHANNEL_DATA
	 * @param fftDataBlock PamDataBlock of spectrgram data
	 * @param whistle Whistle contour from the whistle detector
	 * @param delayChannel channel to measure the delay to
	 * @return delay in samples
	 */
	static public int getDelay(PamDataBlock fftDataBlock, WhistleShape whistle, int delayChannel) {
		
		/*
		 * First check that the other channel actually exists. 
		 */
		
		
		
		return INVALID_DELAY;
	}
}
