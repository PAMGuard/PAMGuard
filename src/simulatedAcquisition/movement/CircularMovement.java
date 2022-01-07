package simulatedAcquisition.movement;

import java.awt.Window;
import java.io.Serializable;

import Array.ArrayManager;
import GPS.GpsData;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.LatLong;
import PamguardMVC.debug.Debug;
import pamMaths.PamVector;
import simulatedAcquisition.SimObject;
import simulatedAcquisition.SimObjectDataUnit;

public class CircularMovement extends MovementModel implements PamSettings {

	private CircularMovementParams circularMovementParams = new CircularMovementParams();

	int[] nSteps = new int[4];
	
	int[] iStep = new int[4];

	private int totalSteps;

	private int totalDone;

	private GpsData arrayLatLong;
	
	public CircularMovement(SimObject simObject) {
		super(simObject);
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public boolean start(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit) {
		nSteps[2] = 360 / circularMovementParams.angleStep + 1;
		nSteps[1] = Math.round((circularMovementParams.depthRange[1]-circularMovementParams.depthRange[0]) / circularMovementParams.depthStep)+1;
		nSteps[1] = Math.max(nSteps[1], 1);
		nSteps[0] = Math.round((circularMovementParams.rangeRange[1]-circularMovementParams.rangeRange[0]) / circularMovementParams.rangeStep)+1;
		nSteps[0] = Math.max(nSteps[0], 1);
		nSteps[3] = circularMovementParams.directionsPerPoint;
		iStep[0] = iStep[1] = iStep[2] = 0;
		iStep[3] = -1;

		totalSteps = 1;
		for (int i = 0; i < 4; i++) {
			totalSteps *= nSteps[i];
		}
		totalDone = 0;
		/*
		 * Get the reference position from the array ...
		 * 
		 */
		try {
			arrayLatLong = ArrayManager.getArrayManager().getCurrentArray().getStreamer(0).getHydrophoneOrigin().getLastStreamerData().getGpsData();
		}
		catch (Exception e) {
			arrayLatLong = new GpsData();
		}
		
		return true;
	}

	@Override
	public boolean takeStep(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit) {
		int dim = 3;
		boolean move = true;
		for (int i = 3; i >= 0 && move; i--) {
			if (iStep[i] < nSteps[i] -1) {
				/* 
				 * if we can move in this dimension without 
				 * hitting the end, then that's all we have
				 * to do. 
				 */
				iStep[i]++;
				break;
			}
			else if (i == 0) {
				return false;
			}
			else {
				/*
				 * Otherwise, set this one to zero and let it 
				 * get on to the next one. 
				 */
				iStep[i] = 0;
			}
		}
		
		double angle = Math.toRadians(circularMovementParams.angleStep * iStep[2]);
		double range = circularMovementParams.rangeStep * iStep[0] + circularMovementParams.rangeRange[0];
		double depth = circularMovementParams.depthStep * iStep[1] + circularMovementParams.depthRange[0];
//		System.out.printf("Move sound to range %3.1fm, angle %3.1fm, depth %3.1fm for gen %d\n", range, angle*180/Math.PI,depth,iStep[3]);
		LatLong currentPos = arrayLatLong.addDistanceMeters(range*Math.sin(angle), range*Math.cos(angle));
		PamVector heading = getHeading(iStep[3]);
//		Debug.out.printf("Set r to %d and angle to %3.1f\n", (int)range, angle*180/Math.PI) ;
		simObjectDataUnit.setCurrentPosition(timeMilliseconds, currentPos, arrayLatLong.getHeight()-depth, heading);
		
		return true;
	}
	
	private PamVector getHeading(int i) {
		double ang = Math.random() * Math.PI * 2;		
		return new PamVector(Math.sin(ang), Math.cos(ang), 0);
	}

	@Override
	public String getName() {
		return "Circular Movement";
	}
	@Override
	public String getUnitName() {
		return getSimObject().name;
	}

	@Override
	public String getUnitType() {
		return "SimObject Grid Settings";
	}

	@Override
	public Serializable getSettingsReference() {
		return circularMovementParams;
	}

	@Override
	public long getSettingsVersion() {
		return CircularMovementParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		circularMovementParams = ((CircularMovementParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public boolean hasOptions() {
		return true;
	}

	@Override
	public boolean showOptions(Window window, SimObject simObject) {
		CircularMovementParams newParams = CircularMovementDialog.showDialog(window, circularMovementParams);
		if (newParams != null) {
			circularMovementParams = newParams;
			return true;
		}
		return false;
	}


}
