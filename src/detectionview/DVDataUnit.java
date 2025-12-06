package detectionview;

import clipgenerator.ClipDataUnit;

public class DVDataUnit extends ClipDataUnit {

	public DVDataUnit(long timeMilliseconds, long triggerMilliseconds, long startSample, int durationSamples,
			int channelMap, String fileName, String triggerName, double[][] rawData, float sourceSampleRate) {
		super(timeMilliseconds, triggerMilliseconds, startSample, durationSamples, channelMap, fileName, triggerName,
				rawData, sourceSampleRate);
		// TODO Auto-generated constructor stub
	}

}
