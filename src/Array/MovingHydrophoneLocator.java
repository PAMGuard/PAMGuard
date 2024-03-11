package Array;

import GPS.GpsData;
import GPS.GpsDataUnit;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;

abstract public class MovingHydrophoneLocator extends SimpleHydrophoneLocator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7448060427072317265L;

	public MovingHydrophoneLocator(PamArray pamArray, Streamer streamer) {
		super(pamArray, streamer);
	}

	@Override
	public boolean isChangeable() {
		return true;
	}


}
