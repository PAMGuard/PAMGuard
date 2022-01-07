package targetMotionModule;

import GPS.GpsData;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;

public class TargetMotionLocalisation extends AbstractLocalisation {
	
	private TargetMotionResult targetMotionResult;

	public TargetMotionLocalisation(TargetMotionResult pamDataUnit) {
		super(pamDataUnit, LocContents.HAS_LATLONG | LocContents.HAS_RANGE, 
				pamDataUnit.getReferenceHydrophones());
		this.targetMotionResult = pamDataUnit;
	}

	@Override
	public LatLong getLatLong(int iSide) {
		return targetMotionResult.getLatLong();
	}

	@Override
	public double getPerpendiculaError(int iSide) {
		return targetMotionResult.getPerpendicularDistanceError();
	}

	/**
	 * @return the targetMotionResult
	 */
	public TargetMotionResult getTargetMotionResult() {
		return targetMotionResult;
	}

	@Override
	public GpsData getOriginLatLong() {
		if (targetMotionResult.getBeamLatLong() != null) {
			return targetMotionResult.getBeamLatLong();
		}
		return super.getOriginLatLong();
	}

	@Override
	public String toString() {
		if (targetMotionResult == null) {
			return "null Target Motion Localisation";
		}
		return targetMotionResult.toString();
	}
	
	

}
