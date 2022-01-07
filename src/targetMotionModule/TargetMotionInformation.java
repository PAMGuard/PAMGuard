package targetMotionModule;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import GPS.GpsData;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;


/**
 * The control panel will return a list of pamDetections. 
 * From these pamDetections we need bearings, GPS Co-Ordinates and other bits and pieces to convert between lat long and some world co-ordinate system. 
 * Any control panel will return this interface, which will supply the correct information to the localisers, map and other panels which need this info.   
 * 
 * @author Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public interface TargetMotionInformation {
	
	/**********Detections***********/
	/**
	 * Returns the pamDetections which will be used for this target motion localisation. 
	 * @return a list fo the current detections to be localised with target motion algorithms
	 */
	public ArrayList<PamDataUnit> getCurrentDetections();
	
	/**
	 * The number of detections to be used in this target motion.
	 * @return the number of detections we have in total getCurrentDetection().size(); 
	 */
	public int getNDetections();
	
	/**********Time Delays***********/
	/**
	 * Returns the time delays for each detection. The number of time delays per detection is going to be based on the number of hydrophones in the array. 
	 * @return a list of time delays. Each ArrayList<Double> corresponds to the time delays calculated from a PamDtection,. Time delays are related to channel numbers by the indexM1 and indexM2 functions
	 * in AbstractLocalisation. 
	 */
	public ArrayList<ArrayList<Double>> getTimeDelays();
	
	
	/**
	 * Returns the time delay errors for each detection. This should be based on the uncertainty in spacing between hydrophones for target motion analysis. 
	 * @return a list of time delay errors. Each ArrayList<Double> corresponds to the time delays calculated from a PamDtection,. Time delays are related to channel numbers by the indexM1 and indexM2 functions
	 * in AbstractLocalisation. 
	 */
	public ArrayList<ArrayList<Double>> getTimeDelayErrors();
	
	
	/**********Bearing Info***********/
	/**
	 * Returns the origin of the hydrophone array for each detection point- this is in meters relative to 0,0,0; 
	 * @return the vector of a PamDetections origin were (0,0,0) is the origin.
	 */
	public PamVector[] getOrigins();
	
	/**
	 * The real world vectors for each detection. There may be an ambiguity in the vectors in which case we would have two world vectors per detections e.g. for a stereo array there are two bearings whilst for a 3D towed array there would usually be only one three dimensional bearing. 
	 * @return a 2D array of world vectors corresponding to each pamDetection in the currentDetectionsGroup. 
	 */
	public PamVector[][]  getWorldVectors();
	
	/**
	 * Get the Euler angles for each detection. We are assumming that any pamDetection used for the target motion localiser will be from a group which is within a rigid array. Hence
	 * the hydrophones within that array will all have the same euler angles. 
	 * <p>
	 * Note on Euler Angles:
	 * <p>
	 * We need to know the position and depth of the array but we also must know the rotation of the array. It would be nice to store the rotation as vector, however a unit
	 * vector cannot describe fully the rotation of the array. e.g say we have the unit vector (-0.71 0.0 0.71), this would describe a heading of 270 and pitch of 45 degrees. However 
	 * this unit vector tells us nothing about the roll (tilt) of the array. This is because we had to start off with a unit vector (0,1,0) to rotate to begin with. 
	 * We can transform co-ordinate systems all we want but a unit vector will only ever tell us two Euler angles. Hence we have to store euler angles as a PamQuaternion not a PamVector. 
	 * @see PamQuaternion
	 * @return PamQuaternion angles for each PamDetection. 

	 */
	public PamQuaternion[] getEulerAngles();
	

	/**********Hydrophones***********/
	/**
	 * Returns the positions of the hydrophones, relative to (0,0,0) for every pamDetection
	 * @return a list of hydrophone positions. Each <ArrayList<Point3f> corresponds to the hydrophone positions for one PamDetection. 
	 */
	public ArrayList<ArrayList<Point3f>> getHydrophonePos();
	
	/***********GPS************/
	/**
	 * The GPS point which corresponds to (0,0,0).  
	 * @return the lat long which has been used for the (0,0,0) reference. 
	 */
	public LatLong getGPSReference();

	/**
	 *
	 * @param ll- the GPS Co-Ordinate
	 * @param rotate
	 * @return
	 */
	public PamVector latLongToMetres(LatLong ll);
	
	/**
	 *Convert a point in your cartesian co-ordinate frame
	 * @param pt
	 * @param isRotated
	 * @return
	 */
	public LatLong metresToLatLong(PamVector pt);
	
	/**
	 * 
	 * @return
	 */
	 public int getReferenceHydrophones();
	 
	 /**
	  * Get a time for the detection (usually an average of all the individual detectiopns)
	  * @return
	  */
	 public Long getTimeMillis();
	 
	 /**
	  * Calculate the beam latitude and longitude. The beam lat long is basically the point on the GPS path which is closest to the 
	  * localised position of the animal 
	  * @param localised- localisation result to find beam lat long for. 
	  * @return lat long
	  */
	 public GpsData getBeamLatLong(LatLong localised);
	 
		
	 /**
	  * Calculate the beam time. This is not the time that the vessel was at the beam lat long but the time the streamer passed that point. Note that if there are 
	  * multiple streamers passing the point  the streamer lengths are averaged and used as our beam lat long. We use the model for each streamer to to work out when it passed the 
	  * beam lat long point. 
	  * @param beamPos- the beam lat long. 
	  * @return the time at which the hydrophone array passed the beam lat long point. 
	 */
	 public long getBeamTime(GpsData beamPos);

}
