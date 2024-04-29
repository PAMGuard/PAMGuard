package binaryFileStorage;

import javafx.scene.control.ProgressIndicator;
import pamViewFX.pamTask.PamTaskUpdate;

public class BinaryMapMakeProgress extends PamTaskUpdate {

	static public final int STATUS_IDLE = 0;
	static public final int STATUS_COUNTING_FILES = 1;
	static public final int STATUS_ANALYSING_FILES = 2;
	static public final int STATUS_SORTING = 3;
	static public final int STATUS_SERIALIZING = 4;
	static public final int STATUS_DESERIALIZING = 5;
	
	private int status;
	
	private String streamName;
	
	private int totalStreams;

	private int currentStream;

	public BinaryMapMakeProgress(int status, String streamName, int totalStreams,
			int currentStream) {
		super();
		this.status = status;
		this.streamName = streamName;
		this.totalStreams = totalStreams;
		this.currentStream = currentStream;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the streamName
	 */
	public String getStreamName() {
		return streamName;
	}

	/**
	 * @param streamName the streamName to set
	 */
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	/**
	 * @return the totalStreams
	 */
	public int getTotalStreams() {
		return totalStreams;
	}

	/**
	 * @param totalStreams the totalStreams to set
	 */
	public void setTotalStreams(int totalStreams) {
		this.totalStreams = totalStreams;
	}

	/**
	 * @return the currentStream
	 */
	public int getCurrentStream() {
		return currentStream;
	}

	/**
	 * @param currentStream the currentStream to set
	 */
	public void setCurrentStream(int currentStream) {
		this.currentStream = currentStream;
	}
	
	@Override
	public double getProgress(){
//		System.out.println("BinaryProgress: " + currentStream + " of: " + totalStreams + " status: " + getStatus()); 
		if (totalStreams==0) return 1.;
		double progress = 0;
		switch (getStatus()){
		case STATUS_ANALYSING_FILES:
			if (totalStreams==0) progress=-1;
			else progress=((double) currentStream)/totalStreams;
			break;
		default:
			progress=ProgressIndicator.INDETERMINATE_PROGRESS;
			break;
		}
		return progress;
	}
	
	

	@Override
	public String getName() {
		return "Binary Data Map";
	}
	
	@Override
	public String getProgressString() {
		return this.streamName;
	}
}
