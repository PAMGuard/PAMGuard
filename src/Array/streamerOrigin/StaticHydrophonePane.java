package Array.streamerOrigin;

import GPS.GpsData;
import GPS.GpsDataUnit;
import PamUtils.LatLong;
import javafx.scene.control.Label;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.utilityPanes.LatLongPane;

/**
 * JavaFX settings pane for a static hydrophones. 
 */
public class StaticHydrophonePane extends PamBorderPane  {
	
	/**
	 * Reference to static origin mwthod. 
	 */
	private StaticOriginMethod staticOriginMethod;
	
	LatLongPane latLongPane;

	public StaticHydrophonePane(StaticOriginMethod staticOriginMethod) {
		
		this.staticOriginMethod=staticOriginMethod; 
		
		latLongPane = new LatLongPane("Static streamer position");
		this.setCenter(latLongPane.getContentNode()); 
		
	}


	public void setParams() {
		GpsDataUnit dataUnit = getStaticOriginSettings().getStaticPosition();
		if (dataUnit == null) {
			setLatLong(null);
		}
		else {
			GpsData gpsData = dataUnit.getGpsData();
			setLatLong(gpsData);
		}
		
//		if (gpsData == null) {
//			return;
//		}
//		else {
//			setLatLong(gpsData);
//		}
		
	}
	
	private StaticOriginSettings getStaticOriginSettings() {
		return ((StaticOriginSettings) staticOriginMethod.getOriginSettings()); 
	}
	

	public boolean getParams() {
		
		LatLong latLong = latLongPane.getParams(null);
		
		if (latLong==null) {
			System.err.println("StaticHydrophonePane: latitude and longitude is null"); 
			return false;
		}
		
		if (getStaticOriginSettings()==null) {
			System.err.println("StaticHydrophonePane: static origin is null"); 
			return false;
		}
		
		//set
		getStaticOriginSettings().setStaticPosition(staticOriginMethod.getStreamer(), new GpsData(latLong));	
//		
//		boolean ok =  getStaticOriginSettings()!= null && getStaticOriginSettings() .getStaticPosition() != null;
//		
//		System.out.println("StaticHydrophonePane: Get params from static origin 1 : " + getStaticOriginSettings()); 
//		
//		System.out.println("StaticHydrophonePane: Get params from static origin 2: " + getStaticOriginSettings() .getStaticPosition()); 

		return true;
	}

	/**
	 * Just set the lat long without resetting the heading. 
	 * @param latLong
	 */
	private void setLatLong(LatLong latLong) {		
		if (latLong==null) {
			//create a default latitude and longitude - Rockall (why not).
			LatLong latLongdefault = new LatLong(); 
			latLongdefault.setLatitude(57.595833333333);
			latLongdefault.setLongitude(-13.686944444444);
			latLongPane.setParams(latLongdefault);
		}
		else latLongPane.setParams(latLong);
	}

}
