package NMEA;

import PamguardMVC.PamDataUnit;

public class NMEADataUnit extends PamDataUnit {

	private StringBuffer charData;
	
	private String stringId;

	/**
	 * Save the system time when the NMEA data unit is created. this is needed 
	 * if the NMEA data are used for PC clock corrections since the main 
	 * timeMilliseconds that passes to the superclass may already have been corrected.  
	 */
	private long systemTime;
	

	public NMEADataUnit(long timeMilliseconds, StringBuffer charData) {
		super(timeMilliseconds);
		this.charData = charData;
		systemTime = System.currentTimeMillis();
		stringId = NMEADataBlock.getSubString(charData, 0);
	}

	/**
	 * Get teh full NMEA character string. 
	 * @return
	 */
	public StringBuffer getCharData() {
		return charData;
	}

	/**
	 * Set teh full NMEA character string.
	 * @param charData
	 */
	public void setCharData(StringBuffer charData) {
		this.charData = charData;
		stringId = NMEADataBlock.getSubString(charData, 0);
	}


	/**
	 * @return the stringId
	 */
	public String getStringId() {
		return stringId;
	}

	/**
	 * This is the true system time from the PC clock from the moment the
	 * data unit was created. 
	 * @return the systemTime
	 */
	public long getSystemTime() {
		return systemTime;
	}

}
