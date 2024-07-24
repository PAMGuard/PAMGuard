package PamguardMVC;

/**
 * Exception thrown by PamRawDataBlock when raw data are requested. 
 * @author Doug Gillespie
 *
 */
public class RawDataUnavailableException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public static final int DATA_ALREADY_DISCARDED = 1;
	public static final int DATA_NOT_ARRIVED = 2;
	public static final int INVALID_CHANNEL_LIST = 3;
	public static final int NEGATIVE_DURATION = 4;
	public static final int NO_DATA = 5;
	
	
	private PamRawDataBlock rawDataBlock;

	private long startSample;

	private int duration;

	private long availableStart;

	private long availableEnd;
	/**
	 * @return the dataCause
	 */
	public int getDataCause() {
		return dataCause;
	}

	/**
	 * @param rawDataBlock
	 * @param duration 
	 * @param startSample 
	 * @param cause
	 */
	public RawDataUnavailableException(PamRawDataBlock rawDataBlock, int dataCause, long availStart, long availEnd, long startSample, int duration) {
		super();
		this.rawDataBlock = rawDataBlock;
		this.dataCause = dataCause;
		this.availableStart = availStart;
		this.availableEnd = availEnd;
		this.startSample = startSample;
		this.duration = duration;
	}


	/**
	 * Cause of the exception - no data or invalid channel list. 
	 */
	private int dataCause;

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		switch (dataCause) {
		case DATA_ALREADY_DISCARDED:
			return String.format("Samples %d length %d requested from %s have already been discarded. %d to %d available", startSample, duration, 
					rawDataBlock.getDataName(), availableStart, availableEnd);
		case DATA_NOT_ARRIVED:
			return String.format("Samples %d length %d  requested from %s have not yet arrived", 
					startSample, duration, rawDataBlock.getDataName());
		case INVALID_CHANNEL_LIST:
			return String.format("Samples %d length %d  requested from %s do not contain the reqeusted channels", 
					startSample, duration, rawDataBlock.getDataName());
		case NEGATIVE_DURATION:
			return String.format("Negative data duration request for %d samples" , duration);
		case NO_DATA:
			return "No raw data available";
		}
		return super.getMessage();
	}

	/**
	 * @return the availableStart
	 */
	public long getAvailableStart() {
		return availableStart;
	}

	/**
	 * @return the availableEnd
	 */
	public long getAvailableEnd() {
		return availableEnd;
	}
	

}
