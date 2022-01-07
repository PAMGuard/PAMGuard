package SoundRecorder;

import PamUtils.PamUtils;

public class RecordingInfo {
	
	public String fileName;

	public float sampleRate;
	
	public int channelBitMap;
	
	public int nBits;
	
	public long startTimeMillis;
	
	public long endTimeMillis;
	
	public long startSample;
	
	public String trigger;

	public RecordingInfo(String fileName, float sampleRate, int channelBitMap, 
			int nBits, long startTimeMillis, long startSample, String trigger) {
		this.fileName = fileName;
		this.sampleRate = sampleRate;
		this.channelBitMap = channelBitMap;
		this.nBits = nBits;
		this.startTimeMillis = startTimeMillis;
		endTimeMillis = startTimeMillis;
		this.startSample = startSample;
		this.trigger = trigger;
	}

	@Override
	public String toString() {
		String str =  String.format("Recording %s; %d Hz; Channels ", fileName, (int) sampleRate);
		for (int i = 0; i <= PamUtils.getHighestChannel(channelBitMap); i++) {
			if ((1<<i & channelBitMap) > 0) {
				str += i;
				if (i < PamUtils.getHighestChannel(channelBitMap)) {
					str += ",";
				}
			}
		}
		str += String.format("; duration %ds", (endTimeMillis-startTimeMillis)/1000);
			
		return str;
	}
	
	
}
