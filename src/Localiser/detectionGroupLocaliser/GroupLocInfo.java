package Localiser.detectionGroupLocaliser;

import java.util.ArrayList;

import pamMaths.PamQuaternion;
import pamMaths.PamVector;
import GPS.GpsData;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * Provides information useful for localisation from groups of detections. 
 * <p>
 * Often multiple detections are required for localisation to be possible <i>e.g.</i> target motion localisation.
 *
 * Before localisation is attempted multiple calculations need to be performed, <i>e.g.</i>  converting all positions of the hydrophone array to x y z, 
 * calculating array rotation, creating a list of time delays. 
 * <p>
 * There are three main pieces of information supplied by GroupLocInfo. More maybe added in the future. 
 * <p>
 * <b>Hydrophone/Reciever Positions</b>
 * Hydrophones can be in a latitude and longitude and Cartesian co-ordinate frame depending on the localisation algorithm. GroupLocInfo
 * allows easy conversation between these two systems for geo-referencing results. <i>GroupLocInfo must have reciever positions implemented</i>
 * <p>
 * <b>Time Delays</b>
 * (Optional) Time delays are often the raw measurement used in localisation. A list of time delays for every detection is supplied using the 
 * indexM1 and indexM2 convention in AbstractLocalisation . Time delays can be used along with hydrophone positions to calculate locations.
 * Time delays are considered a more 'raw' measurement than bearings and thus are not sorted for ambiguity etc.  
 * <p>
 * <b>Bearings </b>
 * (Optional) Bearings can be considered a step up from time delays. Now dealing with a data which has already been partially localised.
 * To achieve maximum flexibility vectors can be returned which correspond to a particular ambiguity. 
 * Bearings may be calculated from time delays, beam forming, particle velocity sensors etc. It does necessarily follow that a groupLoc info 
 * with bearings also has time delay measurements. 
 * GroupLocInfo is used to calculate these values before they are passed to a more generic localisation algortihm. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface GroupLocInfo {
	
	/**********Detections***********/
	/**
	 * Returns the parent detection which contains sub detections which will be used in a group localisation. 
	 * @return a parent detection which should contain two or more sub detections. 
	 */
	public SuperDetection getParentDetection();
	
	/**
	 * The number of detections to be used in this target motion.
	 * @return the number of detections we have in total getCurrentDetection().size(); 
	 */
	public int getDetectionCount();
	
	/**********Time Delays***********/
	/**
	 * Returns the time delays for each detection. The number of time delays per detection is going to be based on the number of hydrophones in the array. 
	 * @return a list of time delays. Each ArrayList<Double> corresponds to the time delays calculated from a PamDetection. Time delays are related to channel numbers by the indexM1 and indexM2 functions
	 * in AbstractLocalisation. 
	 */
	public ArrayList<ArrayList<Double>> getTimeDelays();
	
	
	/**
	 * Returns the time delay errors for each detection. This should be based on the uncertainty in spacing between hydrophones for target motion analysis. 
	 * @return a list of time delay errors. Each ArrayList<Double> corresponds to the time delays calculated from a PamDetection. Time delays are related to channel numbers by the indexM1 and indexM2 functions
	 * in AbstractLocalisation. 
	 */
	public ArrayList<ArrayList<Double>> getTimeDelayErrors();

	
	/**
	 * The real world vectors for each detection. There may be an ambiguity in the vectors in which case each AbstractLocalisation will contain 
	 *  two world vectors per detections e.g. for a stereo array there are two bearings whilst for a 3D towed array there would usually be only 
	 *  one three dimensional bearing. A list of vectors corresponding to one ambiguity is returned here.
	 * @return a 2D array of world vectors corresponding to each ambiguity in the currentDetectionsGroup. 
	 * The size of the array is the same as the number of sub detections. A bearing may be null if not used. 
	 * If side> the no. ambiguities null is returned 
	 */
	public PamVector[] getWorldVectors(int side);

	
	/**
	 * The error for world vectors
	 * @return the error for world vectors 
	 ** The size of the array is the same as the number of sub detections. A bearing may be null if not used. 
	 * If side> the no. ambiguities null is returned 
	 * */
	public PamVector[] getWorldVectorErrors(int side);
	
	/**
	 * Get the number of ambiguities expected from bearings. Solving a loclaisation for different numbers of dimensions can introduce a different number of ambiguities. 
	 * @param - the number of dimensions.
	 * @return the number of ambiguities e.g. for stereo towed array will usually be 2. 
	 */
	public int getAmbiguityCount(int dim); 
	

	/**********Hydrophones***********/
	
	/**
	 * Returns the origin of the hydrophone array for each detection point- this is in meters relative to 0,0,0; 
	 * @return the vector of a PamDetections origin were (0,0,0) is the origin.
	 */
	public PamVector[] getOrigins();
	
	/**
	 * Returns the positions of the hydrophones for every detection, relative to (0,0,0) for every pamDetection.
	 * @return a list of hydrophone positions. Each <ArrayList<double[]> corresponds to the hydrophone positions for one PamDetection. 
	 */
	public ArrayList<ArrayList<double[]>> getHydrophonePos();
	

	/**
	 * Get the Euler angles for each detection. It assumed that the hydrophones within a single PamDetection (usually referred to as a group) will be from 
	 * rigid array, hence share the same rotation angles. 
	 * <p>
	 * <b>Note on Rotation Angles:</b> 
	 * We need to know the position and depth of the array but we also must know the rotation of the array. It would be nice to store the rotation as vector, however a unit
	 * vector cannot describe fully the rotation of the array. e.g say we have the unit vector (-0.71 0.0 0.71), this would describe a heading of 270 and pitch of 45 degrees. However 
	 * this unit vector tells us nothing about the roll (tilt) of the array. This is because we had to start off with a unit vector (0,1,0) to rotate to begin with. 
	 * We can transform co-ordinate systems all we want but a unit vector will only ever tell us two Euler angles. Hence we have to store euler angles as a PamQuaternion
	 * (a 4 element vector) not a PamVector (a 3 element vector). 
	 * <p>
	 * @see PamQuaternion
	 * @return An array of PamQuaternion angles representing each PamDetection. If a quaternion is null the array is assumed to have no rotation.  

	 */
	public PamQuaternion[] getRotationVectors();
	
	
	/***********Geo reference Information************/
	/**
	 * The GPS point which corresponds to (0,0,0) in the cartesian system.  
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
	 * Convert a point in your Cartesian Co-ordinate frame
	 * @param pt - a point 
	 * @param isRotated
	 * @return
	 */
	public LatLong metresToLatLong(PamVector pt);
	
	/**
	 * Get a bitmap of hydrophones channels used in the localisation. 
	 * @return a bitmap of hydrophone positions of all channels used. 
	 */
	 public int getReferenceHydrophones();
	 
	 /**
	  * Get a time for the detection (usually an average of all the individual detectiopns)
	  * @return the time in millis
	  */
	 public Long getTimeMillis();
	 
	 /**
	  * Get the beam time. This is the time at whihc the hydrophones are 
	  * closest to a localisation result 
	  * @return the beam time
	  */
	 public long getBeamTime(LatLong locLatLong);
	 
	/**
	 * Get the beam latitude and longitude. 
	 * @param locLatLong
	 * @return
	 */
	 public GpsData getBeamLatLong(LatLong locLatLong); 
	 
}
