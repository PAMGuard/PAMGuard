package Localiser.detectionGroupLocaliser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pamMaths.PamVector;
import GPS.GpsData;
import Localiser.algorithms.locErrors.LocaliserError;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

/**
 * Used to store localisation results if a group of PamDetections (rather than one) is used. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class GroupLocalisation extends AbstractLocalisation {

	/*
	 * Need to synch the list in case it tries to redraw just as an item is being added. 
	 */
	private List<GroupLocResult> groupLocalisationResults =  Collections.synchronizedList(new ArrayList<GroupLocResult>());
	
//	/**
//	 * optionally keep a reference to any previous localisation objects for 
//	 * a particular data unit, e.g. if this is output from the crossed bearing 
//	 * localiser, which annotates existing data units, it may be best to 
//	 * keep the old bearing only information. 
//	 */
//	private AbstractLocalisation parentLocalisation;

	public GroupLocalisation(PamDataUnit pamDataUnit, GroupLocResult targetMotionResult) {
		super(pamDataUnit, 0, 0);
		
		if (targetMotionResult != null) {
			setTargetMotionResult( targetMotionResult);  
		}
		
	}
	
	/**
	 * Set a target motion result.
	 * @param targetMotionResult - the target motion result
	 */
	public void setTargetMotionResult(GroupLocResult targetMotionResult) {
		groupLocalisationResults.add(targetMotionResult);
		setReferenceHydrophones(targetMotionResult.getReferenceHydrophones());
		if (targetMotionResult.getLatLong() != null) {
			addLocContents(LocContents.HAS_LATLONG);
		}
		if (targetMotionResult.getPerpendicularDistance() != null) {
			addLocContents(LocContents.HAS_RANGE);
		}
	}


	@Override
	public LatLong getLatLong(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return null;
		}
		LatLong ll = groupLocalisationResults.get(iSide).getLatLong();
		if (ll != null) {
			return ll;
		}
		return super.getLatLong(iSide);
	}

	@Override
	public double getPerpendiculaError(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return Double.NaN;
		}
		return groupLocalisationResults.get(iSide).getPerpendicularDistanceError();
	}
	


	/**
	 * Add a target motion result and return the number of results now in the localisation
	 * @param groupLocResult New tm result
	 * @return number of results in localisaion. 
	 */
	public int addGroupLocaResult(GroupLocResult groupLocResult) {
		groupLocalisationResults.add(groupLocResult);
		return groupLocalisationResults.size();
	}
	
	/**
	 * @return the targetMotionResult
	 */
	public GroupLocResult getGroupLocaResult(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return null;
		}
		return groupLocalisationResults.get(iSide);
	}
	
	/**
	 * Get all group localisation results. 
	 * @return the targetMotionResult
	 */
	public GroupLocResult[] getGroupLocResults() {
		if (groupLocalisationResults.size()<1) return null; 
		GroupLocResult[] groupLocResult=new GroupLocResult[groupLocalisationResults.size()]; 
		return groupLocalisationResults.toArray(groupLocResult);
	}

	@Override
	public GpsData getOriginLatLong() {
		if ( groupLocalisationResults.size() <1) {
			return null;
		}
		GroupLocResult targetMotionResult = groupLocalisationResults.get(0);
		if (targetMotionResult.getBeamLatLong() != null) {
			return new GpsData(targetMotionResult.getBeamLatLong());
		}
		return super.getOriginLatLong();
	}

	@Override
	public String toString() {
		if (groupLocalisationResults == null || groupLocalisationResults.size() == 0) {
			return "null Target Motion Localisation";
		}
		String str = "";
		for (int i = 0; i < groupLocalisationResults.size(); i++) {
			if (i > 0) {
				str += "; ";
			}
			str +=  groupLocalisationResults.get(i).toString();
		}
		return str;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getRange(int)
	 */
	@Override
	public double getRange(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return Double.NaN;
		}
		GroupLocResult tmr = groupLocalisationResults.get(iSide);
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

		if (groupLocalisationResults.size() == 0) {
			return null;
		}
		double[] angles = new double[1];
		GroupLocResult tmr = groupLocalisationResults.get(0);
		if (tmr == null) {
			angles[0] = Double.NaN;
			return angles;
		}
		/*
		 *  DG 25/7/16. needs the 90- to make this work I think for volumentric arrays. .
		 *  In current version there is somethign wrong here - works for some but not other array
		 *  configurations.  
		 */
		PamVector firstBearing = tmr.getFirstBearing();
		if (firstBearing == null) {
			return null;
		}
		angles[0] = Math.PI/2.-PamVector.vectorToSurfaceBearing(firstBearing);
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
		return (groupLocalisationResults.size() > 1);
	}

	
	
	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getRangeError(int)
	 */
	@Override
	public double getRangeError(int iSide) {
		if (groupLocalisationResults == null) {
			return Double.NaN;
		}
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return Double.NaN;
		}
		GroupLocResult res = groupLocalisationResults.get(iSide);
		if (res == null) {
			return Double.NaN;
		}
		Double err = res.getPerpendicularDistanceError();
		if (err == null) {
			return Double.NaN;
		}
		return err;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getParallelError(int)
	 */
	@Override
	public double getParallelError(int iSide) {
		//TODO- fix
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return Double.NaN;
		}
		return groupLocalisationResults.get(iSide).getParallelError();
	}
	
	@Override
	public PamVector getErrorDirection(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return null;
		}
		return groupLocalisationResults.get(iSide).getErrorVector();
	}
	
	@Override
	public double getHeight(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return Double.NaN;
		}
		return groupLocalisationResults.get(iSide).getLatLong().getHeight();
	}
	
	@Override
	public double getHeightError(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return Double.NaN;
		}
		/*
		 * Does this has to get called time and time again or can we 
		 * remember it once it's been called once ? Could be slowing 
		 * down the map. 
		 */
		return groupLocalisationResults.get(iSide).getLocError().getError(LocaliserError.zdirVec);
	}
	
	@Override
	public LocaliserError getLocError(int iSide) {
		if (iSide < 0 || iSide >= groupLocalisationResults.size()) {
			return null;
		}
		return groupLocalisationResults.get(iSide).getLocError(); 
	}
	
	@Override
	public int getAmbiguityCount() {
		return groupLocalisationResults.size(); 
	}


	/**
	 * Clear previous localisation results. 
	 */
	public void clearLocResults() {
		groupLocalisationResults.clear();
	}

	/**
	 * Sort the localisation results in oder based on AIC and Chi2. 
	 */
	public void sortLocResults() {
		if (groupLocalisationResults == null) {
			return;
		}
		Collections.sort(groupLocalisationResults);
	}

}
