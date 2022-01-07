package simulatedAcquisition.movement;

import java.awt.Window;

import simulatedAcquisition.SimObject;
import simulatedAcquisition.SimObjectDataUnit;
import simulatedAcquisition.SimSoundDataUnit;

/**
 * Model of movement of a simulated object. 
 * @author doug
 *
 */
public abstract class MovementModel {

	private SimObject simObject;
	
	public MovementModel(SimObject simObject) {
		super();
		this.simObject = simObject;
	}
	
	/**
	 * Prepare to start moving. Called once at 
	 * run start. 
	 * @param timeMilliseconds current time in milliseconds
	 * @param simObjectDataUnit Some data unit to modify. 
	 * @return true if step taken OK. False if track is completed, in which case 
	 * acquisition will stop. 
	 */
	public abstract boolean start(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit);
	
	/**
	 * Take a step. Update the position, heading, etc. directly 
	 * in the SimSoundDataUnit. 
	 * @param timeMilliseconds current time in milliseconds
	 * @param simObjectDataUnit Some data unit to modify. 
	 * @return true if step taken OK. False if track is completed, in which case 
	 * acquisition will stop. 
	 */
	public abstract boolean takeStep(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit);
	
	/**
	 *  
	 * @return Name for the model.
	 */
	public abstract String getName();
	
	/**
	 * Has configurable options. 
	 * @return true of it has a dialog
	 */
	public boolean hasOptions() {
		return false;
	}
	
	/**
	 * Show options dialog. All options to be handled and stored
	 * internally by the MovementModel. 
	 * @param window
	 * @param simObject
	 * @return
	 */
	public boolean showOptions(Window window, SimObject simObject) {
		return false;
	}

	/**
	 * @return the simObject
	 */
	public SimObject getSimObject() {
		return simObject;
	}
}
