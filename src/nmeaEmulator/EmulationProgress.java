package nmeaEmulator;

public class EmulationProgress {
	
	public static final int STATUS_IDLE = 0; 
	public static final int STATUS_RUNNING = 1; 
	public static final int STATUS_MESSAGE = 2; 
	
	public EmulationProgress() {
		super();
		status = STATUS_IDLE;
	}

	public EmulationProgress(String message) {
		super();
		this.message = message;
		status = STATUS_MESSAGE;
	}

	public EmulationProgress(long currTime, long dataTime, int percentProgress) {
		super();
		this.currTime = currTime;
		this.dataTime = dataTime;
		this.percentProgress = percentProgress;
		status = STATUS_RUNNING;
	}

	public long currTime;
	
	public long dataTime;
	
	public int percentProgress;
	
	public int status;
	
	public String message;
}
