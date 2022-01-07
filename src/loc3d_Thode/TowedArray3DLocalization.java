/**
 * 
 */
package loc3d_Thode;

import Array.ArrayManager;
import Array.HydrophoneLocator;
import Array.PamArray;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

/**
 * @author Aaron Thode
 *
 */


public class TowedArray3DLocalization extends AbstractLocalisation {

	/**
	 * @param pamDataUnit
	 * @param locContents
	 * @param referenceHydrophones
	 */
	
	/*
	 * Am hoping that you guys who have more experience of the types
	 * of localisation information available may be able to compile this
	 * list. 
	 */
	
	private TowedArray3DDataUnit towedArray3DDataUnit;

	public TowedArray3DLocalization(TowedArray3DDataUnit towedArray3DDataUnit, int locContents, int referenceHydrophones) {
		super(towedArray3DDataUnit, locContents, referenceHydrophones);
		this.towedArray3DDataUnit = towedArray3DDataUnit;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean bearingAmbiguity() {
		return towedArray3DDataUnit.isHasAngleAmbiguity();
	}

	@Override
	public double getBearing(int iBearing) {

		double angles[] = towedArray3DDataUnit.getAngle();
		if (angles != null && angles.length > iBearing && towedArray3DDataUnit.isHasAngles()) {
			//return bearing associated with ship
			//AARON
			return angles[iBearing];
		}
		return Double.NaN;
		
	}
	
	public double getTilt(int iTilt) {
		double tilts[] = towedArray3DDataUnit.getTilts();
		if (tilts != null && tilts.length > iTilt) {
			return tilts[iTilt];
		}
		return Double.NaN;
	}
	
	public double getHeading(int iHeading) {
		double headings[] = towedArray3DDataUnit.getHeadings();
		if (headings != null && headings.length > iHeading) {
			return headings[iHeading];
		}
		return Double.NaN;
	}

//	@Override
//	public double getBearingError() {
//		// TODO Auto-generated method stub
//		return super.getBearingError();
//	}

	@Override
	public double getBearingReference() {

		// get angle of first hydrophone pair for now
		setReferenceHydrophones(3);
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		int p1 = PamUtils.getNthChannel(0, this.getReferenceHydrophones());
//		int p2 = PamUtils.getNthChannel(1, this.getReferenceHydrophones());
		int p1 = 0;
		int p2 = 1;
		return getParentDetection().getHydrophoneHeading(false);
//		return array.getHydrophoneLocator().
//		getPairAngle(this.getParentDetection().getTimeMilliseconds(), p2, p1, HydrophoneLocator.ANGLE_RE_NORTH) * Math.PI / 180.;

	}

	@Override
	public LatLong getLatLong(int iSide)
	{
		if (towedArray3DDataUnit.HasLatLong() == false) return null;
		return towedArray3DDataUnit.getLatlong(iSide);
	}
	
	@Override
	public double getDepth() {
		if (towedArray3DDataUnit.isHasDepth() == false) return Double.NaN;
		return towedArray3DDataUnit.getDepth();
	}

//	@Override
//	public double getDepthError() {
//		// TODO Auto-generated method stub
//		return super.getDepthError();
//	}

	@Override
	public double getHeight(int iSide) {
		if (towedArray3DDataUnit.isHasDepth() == false) return Double.NaN;
		return towedArray3DDataUnit.getDepth();
	}

	@Override
	public LocalisationInfo getLocContents() {
		// TODO Auto-generated method stub
		return super.getLocContents();
	}

	@Override
	public PamDataUnit getParentDetection() {
		// TODO Auto-generated method stub
		return super.getParentDetection();
	}

	@Override
	public double getRange(int iRange) {
		double ranges[] = towedArray3DDataUnit.getRanges();
		if (ranges != null && ranges.length > iRange && towedArray3DDataUnit.isHasRanges()) {
			//Return range associated with forwardmost hydrophone-AARON
			return ranges[iRange];
		}
		return Double.NaN;
	}

//	@Override
//	public double getRangeError(int iRangeError) {
//		// TODO Auto-generated method stub
//		return super.getRangeError(0);
//	}

	@Override
	public int getReferenceHydrophones() {
		// TODO Auto-generated method stub
		//AARON change to refer to forwardmost hydrophone in array
		return super.getReferenceHydrophones();
		//return 1;
	}

}
