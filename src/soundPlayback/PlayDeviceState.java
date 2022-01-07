package soundPlayback;

/**
 * Class to hold basic information about the state of a playback device. 
 * @author dg50
 *
 */
public class PlayDeviceState {
	
	private boolean started;
	
	private long dataPlayed;
	
	private long dataDumped;

	public PlayDeviceState() {
	}
	
	public void reset() {
		dataPlayed = dataDumped = 0;
	}
	
	public void addDataPlayed(long dataPlayed) {
		this.dataPlayed += dataPlayed;
	}
	
	public void addDataDumpled(long dataDumped) {
		this.dataDumped += dataDumped;
	}

	@Override
	public String toString() {
		if (dataDumped == 0) {
			return getStateString();
		}
		else {
			return String.format("%s: Dumped %3.3f%%", getStateString(), getPercentDumped());
		}
	}

	private double getPercentDumped() {
		return (double) dataDumped / (double) (dataDumped + dataPlayed) * 100.;
	}

	private String getStateString() {
		return started ? "Active" : "Idle";
	}
	
	/**
	 * @return the started
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * @param started the started to set
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * @return the dataPlayed
	 */
	public long getDataPlayed() {
		return dataPlayed;
	}

	/**
	 * @param dataPlayed the dataPlayed to set
	 */
	public void setDataPlayed(long dataPlayed) {
		this.dataPlayed = dataPlayed;
	}

	/**
	 * @return the dataDumped
	 */
	public long getDataDumped() {
		return dataDumped;
	}

	/**
	 * @param dataDumped the dataDumped to set
	 */
	public void setDataDumped(long dataDumped) {
		this.dataDumped = dataDumped;
	}
	
	

}
