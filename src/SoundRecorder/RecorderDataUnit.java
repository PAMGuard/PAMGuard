package SoundRecorder;

import PamguardMVC.PamDataUnit;

public class RecorderDataUnit extends PamDataUnit {

	private RecordingInfo recordingInfo;

	public RecorderDataUnit(long timeMilliseconds, RecordingInfo recordingInfo) {
		super(timeMilliseconds);
		this.recordingInfo = recordingInfo;
	}

	public RecordingInfo getRecordingInfo() {
		return recordingInfo;
	}

	public void setRecordingInfo(RecordingInfo recordingInfo) {
		this.recordingInfo = recordingInfo;
	}
	
}
