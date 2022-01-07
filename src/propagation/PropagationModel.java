package propagation;

import pamMaths.PamVector;
import PamUtils.LatLong;

/**
 * Really simple propagation models. Basically just spreading models
 * since they have no knowledge of frequency dependent attenuation. 
 * If you need frequency dependency, then also add an AttenutationModel.
 * @author Doug Gillespie
 *
 */
public interface PropagationModel {
	/**
	 * Set the locations of hydrophone and the source
	 * @param hydrophoneLatLong
	 * @param sourceLatLong
	 * @param speedOfSound 
	 * though the dialog may show a positive number for depth !
	 * @return true if model ran OK
	 */
	public boolean setLocations(LatLong hydrophoneLatLong, 
			LatLong sourceLatLong, double speedOfSound);
	
	/**
	 * Get the number of propagation paths that will be returned
	 * @return number of paths
	 */
	public int getNumPaths();
	
	/**
	 * Get the time delays for each path
	 * @return delays in seconds
	 */
	public double[] getDelays();
	
	/**
	 * Get a list of vectors pointing from the sound source
	 * to the receiver (or it's apparent location for 
	 * surface echos). 
	 * @return Vector pointing from source to receiver.  
	 */
	public PamVector[] getPointingVectors();
	
	/**
	 * Get the gains for each path
	 * <p>These are the inverse of attenuation
	 * and are a scale factors NOT in dB so that
	 * surface reflections can be given a negative number
	 * @return path gains. 
	 */
	public double[] getGains();
	
	/**
	 * 
	 * @return name
	 */
	public String getName();
	
}
