package loggerForms.loggeraudio;

import java.util.LinkedList;
import java.util.List;

public class PlatformAudio {

	private String platform;
	private LoggerAudioProcess loggerAudioProcess;
	private List<double[]> audioQueue;
	private int maxQueueBytes = 8000*2;

	public PlatformAudio(LoggerAudioProcess loggerAudioProcess, String platform) {
		this.loggerAudioProcess = loggerAudioProcess;
		this.platform = platform;
		audioQueue = new LinkedList<>();
	}
	
	public void addAudioData(double[] data) {
		synchronized (audioQueue) {
			audioQueue.add(data);
		}
		trimQueue();
	}
	
	private void trimQueue() {
		synchronized (audioQueue) {
			while (getQueueSize() > maxQueueBytes) {
				audioQueue.remove(0);
			}		
		}
	}

	public int getQueueSize() {
		int sz = 0;
		synchronized (audioQueue) {
			for (double[] aData : audioQueue) {
				sz += aData.length;
			}
		}
		return sz;
	}
	
	public double[] getBlock() {
		synchronized (audioQueue) {
			if (audioQueue.size() > 0) {
				return audioQueue.remove(0);
			}
		}
		return null;
	}

}
