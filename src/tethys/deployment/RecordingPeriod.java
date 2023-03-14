package tethys.deployment;

public class RecordingPeriod {

	private long recordStart;
	
	private long recordStop;

	public RecordingPeriod(long recordStart, long recordStop) {
		super();
		this.recordStart = recordStart;
		this.recordStop = recordStop;
	}

	public long getRecordStart() {
		return recordStart;
	}

	public void setRecordStart(long recordStart) {
		this.recordStart = recordStart;
	}

	public long getRecordStop() {
		return recordStop;
	}

	public void setRecordStop(long recordStop) {
		this.recordStop = recordStop;
	}
	
	public long getDuration() {
		return recordStop-recordStart;
	}
	
	
}
