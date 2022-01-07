package Array.streamerOrigin;

import Array.PamArray;
import Array.Streamer;

public class GPSOriginSystem extends HydrophoneOriginSystem {

	protected static String systemName = "Ship GPS Data";
	
	@Override
	public String getName() {
		return systemName;
	}

	@Override
	public HydrophoneOriginMethod createMethod(PamArray pamArray,
			Streamer streamer) {
		return new GPSOriginMethod(pamArray, streamer);
	}

	@Override
	public Class getMethodClass() {
		return GPSOriginMethod.class;
	}

}
