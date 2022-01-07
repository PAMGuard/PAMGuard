package simulatedAcquisition.movement;

import java.awt.Window;
import java.io.Serializable;

import pamMaths.PamVector;
import Array.ArrayManager;
import GPS.GpsData;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.LatLong;
import simulatedAcquisition.SimObject;
import simulatedAcquisition.SimObjectDataUnit;
import simulatedAcquisition.SimSoundDataUnit;

public class GridMovement extends MovementModel implements PamSettings {

	private GridMovementParams gridMovementParams = new GridMovementParams();
	
	/**
	 * Four dimensions of stems since this includes the heading. 
	 */
	int[] nSteps = new int[4];
	int[] iStep = new int[4];

	private int totalDone;

	private int totalSteps;

	private GpsData arrayLatLong;

//	private SimObjectDataUnit simObjectDataUnit;
		
	public GridMovement(SimObject simObject) {
		super(simObject);
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public boolean start(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit) {
		for (int i = 0; i < 3; i++) {
			nSteps[i] = (int) Math.round((gridMovementParams.distRangeMetres[i][1]-gridMovementParams.distRangeMetres[i][0])/
					gridMovementParams.distStepsMetres[i]) + 1;
			iStep[i] = 0;
		}
		nSteps[3] = gridMovementParams.directionsPerPoint;
		iStep[3] = -1; // need this to initialise step taking.
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
		
		return false;
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
		double[] pos = new double[3];
		for (int i = 0; i < 3; i++) {
			pos[i] = gridMovementParams.distRangeMetres[i][0] + iStep[i] * gridMovementParams.distStepsMetres[i];
		}
		double ang = 360.*iStep[3]/gridMovementParams.directionsPerPoint;
		totalDone++;
//		System.out.println(String.format("Step %d of %d %d,%d,%d,%d %3.1f,%3.1f%3.1f, angle %3.1f",
//				totalDone, totalSteps, iStep[0], iStep[1], iStep[2], iStep[3], pos[0], pos[1], pos[2], ang));
		LatLong currentPos = arrayLatLong.addDistanceMeters(pos[0], pos[1]);
		PamVector heading = getHeading(iStep[3]);
		simObjectDataUnit.setCurrentPosition(timeMilliseconds, currentPos, arrayLatLong.getHeight()+pos[2], heading);
		return true;//totalDone < totalSteps*2;
	}

	private PamVector getHeading(int i) {
		double ang = Math.random() * Math.PI * 2;		
		return new PamVector(Math.sin(ang), Math.cos(ang), 0);
	}

	@Override
	public String getName() {
		return "Grid Movement";
	}

	@Override
	public boolean hasOptions() {
		return true;
	}

	@Override
	public boolean showOptions(Window window, SimObject simObject) {
		GridMovementParams newParams = GridMovementDialog.showDialog(window, gridMovementParams);
		if (newParams != null) {
			gridMovementParams = newParams;
			return true;
		}
		return false;
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
		return gridMovementParams;
	}

	@Override
	public long getSettingsVersion() {
		return GridMovementParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		gridMovementParams = ((GridMovementParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

}
