package simulatedAcquisition.movement;

import java.util.ArrayList;

import simulatedAcquisition.SimObject;
import simulatedAcquisition.SimObjectDataUnit;

/**
 * Manage a list of movement models for the simulator. 
 * @author Doug Gillespie
 *
 */
public class MovementModels {

	private ArrayList<MovementModel> models;
	
	private SimObject simObject;

	public MovementModels(SimObject simObject) {
		this.simObject = simObject;
		models = new ArrayList<MovementModel>();
		models.add(new StraightLineMovement(simObject));
		models.add(new GridMovement(simObject));
		models.add(new CircularMovement(simObject));
	}
	
	public int getNumModels() {
		return models.size();
	}
	
	public MovementModel getModel(int i) {
		return models.get(i);
	}

	/**
	 * @return the simObject
	 */
	public SimObject getSimObject() {
		return simObject;
	}
}
