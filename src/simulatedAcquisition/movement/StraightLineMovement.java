package simulatedAcquisition.movement;


import PamUtils.LatLong;
import pamMaths.PamVector;
import simulatedAcquisition.SimObject;
import simulatedAcquisition.SimObjectDataUnit;

/**
 * Simplest movement model - is the one that's in there at the 
 * moment, i.e. stationary or moving in a straight line. 
 * @author Doug Gillespie
 *
 */
public class StraightLineMovement extends MovementModel {

	public StraightLineMovement(SimObject simObject) {
		super(simObject);
	}

	private long lastUpdateTime;
	
	@Override
	public boolean start(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit) {
		lastUpdateTime = timeMilliseconds;
		return true;
	}
	
	@Override
	public boolean takeStep(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit) {
		long updateInterval = timeMilliseconds - lastUpdateTime;
		if (updateInterval == 0) {
			return true;
		}
		lastUpdateTime = timeMilliseconds;
		SimObject simObject = simObjectDataUnit.getSimObject();
		double dist = (double) updateInterval / 1000. * simObject.speed;
		PamVector currentHeading = simObjectDataUnit.getCurrentHeading().getUnitVector();
		LatLong currentPosition = simObjectDataUnit.getCurrentPosition();
		double currentHeight = simObjectDataUnit.getCurrentHeight();
		currentPosition = currentPosition.addDistanceMeters(dist*currentHeading.getElement(0), dist*currentHeading.getElement(1));
		currentHeight += dist*currentHeading.getElement(2);
		simObjectDataUnit.setCurrentPosition(timeMilliseconds, currentPosition, currentHeight, currentHeading);
		
		return true;
	}

	@Override
	public String getName() {
		return "Straight";
	}

}
