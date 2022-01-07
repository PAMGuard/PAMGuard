package depthReadout;

import java.awt.Frame;


/**
 * Hydrophone depth may be read out in a number of different ways and may 
 * arrive from a number of different sources. The main DepthProcess will manage
 * common functinaility and common data, but the actual work will be done
 * through this interface. 
 * @author Douglas Gillespie
 *
 */
public interface DepthSystem {

	static public final double DEPTH_OUT_OF_RANGE = Double.NEGATIVE_INFINITY;
	static public final double DEPTH_NO_DATA = Double.POSITIVE_INFINITY;
	/**
	 * DepthProcess should ask this system for data every so often
	 * if this return false, then the data are comign from somewhere beyond
	 * the immediate contron of Pamgaurd and we just wait for it to arrive. 
	 * @return true if the sensor needs to be polled.
	 */
	boolean shouldPoll();
	
	/**
	 * Get the depth (should be in meters)
	 * @param iSensor
	 * @return depth in meters. or DEPTH_OUT_OF_RANGE or DEPTH_NO_DATA
	 */
	double getDepth(int iSensor);
	
	/**
	 * Read all data from a sensor. These data should be stored locally in the 
	 * concrete class and will then be retreived with other get... functions. 
	 * @param iSensor number of the sensor
	 * @return true if the read went OK.
	 */
	boolean readSensor(int iSensor);

	/**
	 * Get the depth (should be in meters)
	 * @param iSensor
	 * @return depth in meters. or DEPTH_OUT_OF_RANGE or DEPTH_NO_DATA
	 */
	double getDepthRawData(int iSensor);
	
	/**
	 * Say whether ot not a particular sensor can be configured. 
	 * @return true if the sensor can be configured
	 */
	boolean canConfigure();
	
	/**
	 * Configure one of the sensors. 
	 * @param parentFrame owner frame for dialog
	 * @return true if configurations went Ok. 
	 */
	boolean configureSensor(Frame parentFrame);
}
