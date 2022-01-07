package targetMotionOld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pamMaths.PamVector;
import GPS.GpsData;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 */
@Deprecated
public class TargetMotionLocalisation extends AbstractLocalisation {
	
	/*
	 * Need to synch the list in case it tries to redraw just as an item is being added. 
	 */
	private List<GroupLocResult> targetMotionResults =  Collections.synchronizedList(new ArrayList<GroupLocResult>());

	public TargetMotionLocalisation(PamDataUnit pamDataUnit, GroupLocResult targetMotionResult) {
		super(pamDataUnit, 0, 0);
		if (targetMotionResult != null) {
			targetMotionResults.add(targetMotionResult);
			setReferenceHydrophones(targetMotionResult.getReferenceHydrophones());
			
			if (targetMotionResult.getLatLong() != null) {
				addLocContents(LocContents.HAS_LATLONG);
			}
			if (targetMotionResult.getPerpendicularDistance() != null) {
				addLocContents(LocContents.HAS_RANGE);
			}
		}
	}

	@Override
	public LatLong getLatLong(int iSide) {
		if (iSide < 0 || iSide >= targetMotionResults.size()) {
			return null;
		}
		LatLong ll = targetMotionResults.get(iSide).getLatLong();
		if (ll != null) {
			return ll;
		}
		return super.getLatLong(iSide);
	}

	@Override
	public double getPerpendiculaError(int iSide) {
		if (iSide < 0 || iSide >= targetMotionResults.size()) {
			return Double.NaN;
		}
		return targetMotionResults.get(iSide).getPerpendicularDistanceError();
	}

	/**
	 * Add a target motion result and return the number of results now in the localisation
	 * @param targetMotionResult New tm result
	 * @return number of results in localisaion. 
	 */
	public int addTargetMotionResult(GroupLocResult targetMotionResult) {
		targetMotionResults.add(targetMotionResult);
		return targetMotionResults.size();
	}
	/**
	 * @return the targetMotionResult
	 */
	public GroupLocResult getTargetMotionResult(int iSide) {
		if (iSide < 0 || iSide >= targetMotionResults.size()) {
			return null;
		}
		return targetMotionResults.get(iSide);
	}

	@Override
	public GpsData getOriginLatLong() {
		if ( targetMotionResults.size() < 1) {
			return null;
		}
		GroupLocResult targetMotionResult = targetMotionResults.get(0);
		if (targetMotionResult.getBeamLatLong() != null) {
			return new GpsData(targetMotionResult.getBeamLatLong());
		}
		return super.getOriginLatLong();
	}

	@Override
	public String toString() {
		if (targetMotionResults == null || targetMotionResults.size() == 0) {
			return "null Target Motion Localisation";
		}
		String str = "";
		for (int i = 0; i < targetMotionResults.size(); i++) {
			if (i > 0) {
				str += "; ";
			}
			str +=  targetMotionResults.get(i).toString();
		}
		return str;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getRange(int)
	 */
	@Override
	public double getRange(int iSide) {
		if (iSide < 0 || iSide >= targetMotionResults.size()) {
			return Double.NaN;
		}
		GroupLocResult tmr = targetMotionResults.get(iSide);
		if (tmr == null) {
			return Double.NaN;
		}
		Double pd = tmr.getPerpendicularDistance();
		if (pd == null) {
			return Double.NaN;
		}
		else {
			return pd;
		}
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getAngles()
	 */
	@Override
	public double[] getAngles() {
		/**
		 * Shit ! This getAngles is only returning angles for one side - not two angles
		 * to represent ambiguity - it's meant to be colatitude and colongitude or something 
		 * like that, so returning two angles really screws it up into thinking the second is a 
		 * slant angle. 
		 * Really must get rid of these angles and stick to vectors !
		 */

		if (targetMotionResults.size() == 0) {
			return null;
		}
		double[] angles = new double[targetMotionResults.size()];
		
		GroupLocResult tmr = targetMotionResults.get(0);
		if (tmr == null) {
			angles[0] = Double.NaN;
			return angles;
		}
		angles[0] = PamVector.vectorToSurfaceBearing(tmr.getFirstBearing());
		
//		double[] angles = new double[targetMotionResults.size()];
//		GpsData o = getParentDetection().getOriginLatLong(false);
//		for (int i = 0; i < targetMotionResults.size(); i++) {
//			TargetMotionResult tmr = targetMotionResults.get(i);
//			LatLong p = tmr.getLatLong();
//			if (o == null || p == null){ 
//				if (tmr.getFirstBearing() != null) {
//					angles[i] = tmr.getFirstBearing();
//				}
//				else{
//					angles[i] = Double.NaN;
//				}
//			}
//			else {
//				angles[i] = Math.toRadians(o.bearingTo(p) - o.getTrueHeading());
//			}
//		}
		return angles;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#bearingAmbiguity()
	 */
	@Override
	public boolean bearingAmbiguity() {
		return (targetMotionResults.size() > 1);
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getRangeError(int)
	 */
	@Override
	public double getRangeError(int iSide) {
		if (iSide < 0 || iSide >= targetMotionResults.size()) {
			return Double.NaN;
		}
		return targetMotionResults.get(iSide).getPerpendicularDistanceError();
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getParallelError(int)
	 */
	@Override
	public double getParallelError(int iSide) {
		if (iSide < 0 || iSide >= targetMotionResults.size()) {
			return Double.NaN;
		}
		return targetMotionResults.get(iSide).getParallelError();
	}
	
	

}
