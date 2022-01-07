/**
 * 
 */
package difar;

import Array.ArrayManager;
import Array.HydrophoneLocator;
import GPS.GPSDataBlock;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamguardMVC.PamDataUnit;

/**
 * used to store and process information about each buoy.
 * <br>
 * will pick data when times match its requirements. (eg 20 clips 10 seconds long, spaced by 60 seconds)
 * <br>
 * additional clips may be added to the calibration or the clibration be overwritten to update or something...??  
 * @author gw
 *
 */
public class BuoyCalibration {
	
	/**
	 */
	DifarControl difarControl;
	
	/**
	 */
	long timeOfStart;
	/**
	 */
	LatLong latLong;
	
	/**
	 */
	int channelMap;
	
	
	/**
	 */
	public BuoyCalibration(DifarControl difarControl, int channelMap) {
		
		this.difarControl=difarControl;
		this.timeOfStart=PamCalendar.getTimeInMillis();
//		this.latLong=GPS
		this.channelMap = channelMap;
	}
	
	/**
	 */
	public BuoyCalibration(DifarControl difarControl, long timeOfStart, LatLong latLong, int channelMap) {
		
		this.difarControl=difarControl;
		this.timeOfStart=timeOfStart;	//
		this.latLong=latLong;			//
		this.channelMap = channelMap;
		
	}
	
	/**
	 * check time of each new data unit from raw data source(~0.1s) to see if it if needed for any of the buoy calibration sequences
	 * @param pamDataUnit
	 */
	
	void newDataUnit(PamDataUnit pamDataUnit){
		
		//time is bofore 
		if (pamDataUnit.getTimeMilliseconds() >= timeOfStart+difarControl.getDifarParameters().vesselClipLength){
//			difarControl.getDifarProcess().
		}
		
	}
	
	void updateLocationData(HydrophoneLocator difarLocator){
		
	}
	
	
	
	
}
