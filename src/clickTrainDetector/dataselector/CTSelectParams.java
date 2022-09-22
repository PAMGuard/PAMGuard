package clickTrainDetector.dataselector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.dataSelector.DataSelectParams;
import clickDetector.dataSelector.ClickTrainSelectParameters;

/**
 * Parameters for click train data selection. 
 *
 * @author Jamie Macaulay
 *
 */
public class CTSelectParams extends DataSelectParams implements Serializable, Cloneable, ManagedParameters {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	
	/***Data selector stuff mainly for plotting on map***/
	
	/**
	 * Remove if has localisation
	 */
	public boolean needsLoc = false; 

	/**
	 * The minimum number of sub detections before a click train is plotted. 
	 */
	public int minSubDetections = 10; 
	
	/**
	 * True of the click train detector needs a classifcation No !!!!
	 * This 'needsClassification' variable was been used the wrong way around throughout and is screwing the classiifer. 
	 * I've therefore replaced it with the variable allowAll which is how this is actually used !
	 * Given that anything using this would have has screwed logic, I don't care if I break 
	 * a config or two ! DG 22/9/22
	 */
	public boolean allowAnyClassification = false; 
//	private boolean needsClassification = false; 
	
	/**
	 * In the data selector, if this is false it will only look at the best classifier, i.e. the
	 * one with the best classifier score. If set true it will check every species above min
	 * score even if it's not the first classification choice. 
	 */
	public boolean allowMultipleChoices = false;
	
	
	/**
	 * The classifier type(s) to select
	 */
	public int[] classifier = null; 


	/**
	 * The plotting works as follows: Data is plotted either after a minTime has passed and bearing change has exceeded 
	 * maxAngle change or the the time since last bearing exceeds maxTime. 
	 */

	/**
	 * The minimum time between bearing lines being drawn on the map in millis
	 */
	public long minTime = 5000; 
	
	/**
	 * The maximum time between bearing lines being drawn on the map in millis
	 */
	public long maxTime = 60*1000;

	/**
	 * The  angle change at which a new bearing line is drawn. Note that minTime 
	 * overrides the maxAngle in RADIANS
	 */
	public double maxAngleChange = Math.toRadians(5);
	
	
	
	@Override
	public ClickTrainSelectParameters clone() {
		try {
			return (ClickTrainSelectParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}


}
