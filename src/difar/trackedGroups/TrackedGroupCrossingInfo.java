package difar.trackedGroups;

import java.util.ArrayList;

import pamMaths.PamVector;
import targetMotionModule.TargetMotionResult;
import GPS.GpsData;
import PamUtils.LatLong;

/**
 * 
 * @author Doug Gillespie
 *
 */
public class TrackedGroupCrossingInfo {

	private LatLong crossLocation;
	private TrackedGroupDataUnit[] matchedUnits;
	private PamVector xyz;
	private Double[] errors;

	public TrackedGroupCrossingInfo(TrackedGroupDataUnit[] detectionList, TargetMotionResult difarCrossing) {
		this.matchedUnits = detectionList;
		this.xyz = difarCrossing.getLocalisationXYZ();
		this.errors = difarCrossing.getErrors();
		crossLocation = difarCrossing.getLatLong();
		
	}

	/**
	 * Constructor to use when readin gback in binary data. 
	 * @param matchedUnits
	 * @param latLong
	 */
	public TrackedGroupCrossingInfo(TrackedGroupDataUnit[] matchedUnits, LatLong latLong, Double[] errors) {
		this.matchedUnits = matchedUnits;
		this.crossLocation = latLong;
		this.errors = errors;
	}

	/**
	 * Constructor to use when readin gback in binary data. 
	 * @param matchedUnits
	 * @param latLong
	 */
	public TrackedGroupCrossingInfo(TrackedGroupDataUnit[] matchedUnits, LatLong latLong) {
		this.matchedUnits = matchedUnits;
		this.crossLocation = latLong;
	}
	
	public void setLocation(LatLong latLong) {
		this.crossLocation = latLong;
	}

	/**
	 * @return the crossLocation
	 */
	public LatLong getCrossLocation() {
		return crossLocation;
	}

	/**
	 * @return the matchedUnits
	 */
	public TrackedGroupDataUnit[] getMatchedUnits() {
		return matchedUnits;
	}

	/**
	 * @return the xyz
	 */
	public PamVector getXyz() {
		return xyz;
	}

	/**
	 * @return the errors
	 */
	public Double[] getErrors() {
		return errors;
	}


}
