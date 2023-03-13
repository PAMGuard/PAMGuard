package tethys;

import GPS.GpsData;
import nilus.Deployment;

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
	}

}
