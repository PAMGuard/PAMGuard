package Array.swing.sidepanel;

import javax.swing.JComponent;

import GPS.GpsData;

/**
 * Array display panel showing tilt, roll, etc. in numeric and graphical format.  
 * @author dg50
 *
 */
abstract public class ArrayDisplay {

	public ArrayDisplay() {
		// TODO Auto-generated constructor stub
	}
	
	abstract public JComponent getComponent();
	
	/**
	 * Update the array data. 
	 */
	abstract public void updateData(int streamerMap, long timeMilliseconds, GpsData gpsData);
	
	/**
	 * Update the array layout, may require the addition 
	 * of extra controls for new sub arrays, etc. 
	 * <br> can display multiple streamers on one plot or make multiple plots. 
	 * @param streamerMap bitmap of streamers to display. 
	 */
	abstract public void updateLayout(int streamerMap);

}
