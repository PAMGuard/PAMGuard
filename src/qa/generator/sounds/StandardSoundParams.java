package qa.generator.sounds;

public class StandardSoundParams {

	private double durationS;
	
	private double startFrequency;
	
	private double endFrequency;
		
	/**
	 * 
	 * @param durationS
	 * @param startFrequency
	 * @param endFrequency
	 */
	public StandardSoundParams(double durationS, double startFrequency, double endFrequency) {
		super();
		this.durationS = durationS;
		this.startFrequency = startFrequency;
		this.endFrequency = endFrequency;
	}
	/**
	 * @return the durationS
	 */
	public double getDurationS() {
		return durationS;
	}
	/**
	 * @param durationS the durationS to set
	 */
	public void setDurationS(double durationS) {
		this.durationS = durationS;
	}
	/**
	 * @return the startFrequency
	 */
	public double getStartFrequency() {
		return startFrequency;
	}
	/**
	 * @param startFrequency the startFrequency to set
	 */
	public void setStartFrequency(double startFrequency) {
		this.startFrequency = startFrequency;
	}
	/**
	 * @return the endFrequency
	 */
	public double getEndFrequency() {
		return endFrequency;
	}
	/**
	 * @param endFrequency the endFrequency to set
	 */
	public void setEndFrequency(double endFrequency) {
		this.endFrequency = endFrequency;
	}
//	/**
//	 * @return the amplitudeDB
//	 */
//	public double getAmplitudeDB() {
//		return amplitudeDB;
//	}
//	/**
//	 * @param amplitudeDB the amplitudeDB to set
//	 */
//	public void setAmplitudeDB(double amplitudeDB) {
//		this.amplitudeDB = amplitudeDB;
//	}

}
