package tethys;

import Array.ArrayManager;
import Array.HydrophoneLocator;
import Array.PamArray;
import Array.Streamer;
import GPS.GPSControl;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import nilus.Deployment;
import nilus.DeploymentRecoveryDetails;

/**
 * Function(s) to get location information for Tethys in the required format. 
 * @author dg50
 *
 */
public class TethysLocationFuncs {


	/**
	 * Get everything we need for a deployment document including the track #
	 * and the deployment / recovery information. Basically this means we 
	 * have to load the GPS data, then potentially filter it. Slight risk this 
	 * may all be too much for memory, but give it a go by loading GPS data for 
	 * the deployment times. 
	 * @param deployment
	 */
	public static void getTrackAndPositionData(Deployment deployment) {
		long start = TethysTimeFuncs.millisFromGregorianXML(deployment.getDeploymentDetails().getAudioTimeStamp());
		long end = TethysTimeFuncs.millisFromGregorianXML(deployment.getRecoveryDetails().getAudioTimeStamp());
		/*
		 * Need to load data for GPS, Hydrophones and Streamers datablocks for this time period. Can then use
		 * the snapshot geomentry classes to do the rest from the array manager ?
		 */
		boolean ok = true;
		ok &= addPositionData(deployment.getDeploymentDetails());
		ok &= addPositionData(deployment.getRecoveryDetails());
		
	}
	
	/**
	 * Add position data to DeploymentRecoveryDetails. 
	 * @param drd
	 * @return
	 */
	public static boolean addPositionData(DeploymentRecoveryDetails drd) {
		long timeMillis = TethysTimeFuncs.millisFromGregorianXML(drd.getAudioTimeStamp()); 
		LatLong pos = getLatLongData(timeMillis);
		if (pos == null) {
			return false;
		}
		drd.setLongitude(PamUtils.constrainedAngle(pos.getLongitude(), 360));
		drd.setLatitude(pos.getLatitude());
		drd.setElevationInstrumentM(pos.getHeight());
		drd.setDepthInstrumentM(-pos.getHeight());
		return true;
	}
	
	public static LatLong getLatLongData(long timeMillis) {
		// check the array time. 
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		Streamer aStreamer = array.getStreamer(0);
		GPSControl gpsControl = GPSControl.getGpsControl();
		PamConnection con = DBControlUnit.findConnection();
		if (gpsControl != null) {
//			check GPS data are loaded for times around this. 
			GpsDataUnit gpsData = (GpsDataUnit) gpsControl.getGpsDataBlock().getLogging().findClosestDataPoint(con, timeMillis);
			if (gpsData != null) {
				return gpsData.getGpsData();
			}
		}
		HydrophoneLocator hydrophoneLocator = aStreamer.getHydrophoneLocator();
		if (hydrophoneLocator == null) {
			return null;
		}
		return hydrophoneLocator.getStreamerLatLong(timeMillis);
	}

}
