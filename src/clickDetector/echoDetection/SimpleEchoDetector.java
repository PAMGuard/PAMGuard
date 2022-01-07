package clickDetector.echoDetection;

import java.util.Hashtable;

import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector.ChannelGroupDetector;

public class SimpleEchoDetector implements EchoDetector {
	
	private SimpleEchoDetectionSystem simpleEchoDetectionSystem;
	
	private int channelBitmap;
	
	private ClickControl clickControl;
	
	private ChannelGroupDetector channelGroupDetector;
	
	private ClickDetection prevDetection;
	
	private double sampleRate;
		
	/**
	 * @param simpleEchoDetectionSystem 
	 * @param clickControl
	 * @param channelGroupDetector
	 * @param channelBitmap
	 */
	public SimpleEchoDetector(SimpleEchoDetectionSystem simpleEchoDetectionSystem, ClickControl clickControl,
			ChannelGroupDetector channelGroupDetector, int channelBitmap) {
		super();
		this.simpleEchoDetectionSystem = simpleEchoDetectionSystem;
		this.clickControl = clickControl;
		this.channelGroupDetector = channelGroupDetector;
		this.channelBitmap = channelBitmap;
	}

	@Override
	public void initialise() {
		sampleRate = clickControl.getClickDetector().getSampleRate();
	}

	@Override
	public boolean isEcho(ClickDetection clickDetection) {
		if (prevDetection == null) {
			prevDetection = clickDetection;
			return false;
		}
		if (sampleRate == 0) {
			initialise();
		}
		double delay  = (clickDetection.getStartSample() - prevDetection.getStartSample()) / sampleRate;
		boolean isEcho = (delay >= 0 && delay <= simpleEchoDetectionSystem.simpleEchoParams.maxIntervalSeconds);
		if (!isEcho) {
			prevDetection = clickDetection;
		}
		return isEcho;
	}

}
