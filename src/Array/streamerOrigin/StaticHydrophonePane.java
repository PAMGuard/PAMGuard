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
		
		latLongPane = new LatLongPane("Hello");
		this.setCenter(latLongPane.getContentNode()); 
		
	}


	public void setParams() {
		GpsDataUnit dataUnit = getStaticOriginSettings().getStaticPosition();
		if (dataUnit == null) {
			return;
		}
		GpsData gpsData = dataUnit.getGpsData();
		if (gpsData == null) {
			return;
		}
		else {
			setLatLong(gpsData);
		}
		
	}
	
	private StaticOriginSettings getStaticOriginSettings() {
		return ((StaticOriginSettings) staticOriginMethod.getOriginSettings()); 
	}
	

	public boolean getParams() {
		boolean ok =  getStaticOriginSettings()!= null && getStaticOriginSettings() .getStaticPosition() != null;
		return ok;
	}

	/**
	 * Just set the lat long without resetting the heading. 
	 * @param latLong
	 */
	private void setLatLong(LatLong latLong) {		
		latLongPane.setParams(latLong);
	}

}
