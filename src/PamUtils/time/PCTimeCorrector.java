package PamUtils.time;

import java.awt.Window;

/**
 * Interface for anything that is capable of making estimates of PC time corrections
 * e.g. from NMEA and NTP server data. 
 * @author Doug
 *
 */
public interface PCTimeCorrector {
	
	/**
	 * 
	 * @return Name of the time corrector 
	 */
	public String getName();
	
	/**
	 * 
	 * @return Name of the source for logging (e.g. NMEA string or NTP server name)
	 */
	public String getSource();
	
	/**
	 * show a dialog to configure the system
	 * @param frame
	 * @return OK if dialog was OK'd 
	 */
	public boolean showDialog(Window frame);
	
	/**
	 * Likely update interval - can return 0 for very frequency updates (e.g. NMEA data)
	 * @return update interval in seconds
	 */
	public int getUpdateInterval();
	
	/**
	 * Stop the system
	 */
	public void stop();
	
	/**
	 * Start the system
	 * @return true if it seemed to get started OK. 
	 */
	public boolean start();
	
	/**
	 * A status flag ? 
	 * @return
	 */
	public int getStatus();	

}
