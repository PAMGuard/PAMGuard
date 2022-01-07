package difar.trackedGroups;

import pamMaths.PamVector;
import Array.ArrayManager;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamUtils.LatLong;

/**
 * Hold localisation information for Difar TrackedGroups. 
 * Very similar to DifarLocalisation, so should probably consider creating
 * a generic DifarLocalisation and subclassing regular Difar bearings and
 * TrackedGroups from the generic.
 * @author brian_mil
 *
 */
public class TrackedGroupLocalisation extends AbstractLocalisation {

	private TrackedGroupDataUnit difarDataUnit;
	
	private double angleErr[] = {Math.toRadians(5.)};
	
	public TrackedGroupLocalisation(TrackedGroupDataUnit difarDataUnit, int locContents,
			int referenceHydrophones) {
		super(difarDataUnit, locContents, referenceHydrophones);
		this.difarDataUnit = difarDataUnit;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getSubArrayType()
	 */
	@Override
	public int getSubArrayType() {
		return ArrayManager.ARRAY_TYPE_POINT;
	}


	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getLocContents()
	 */
	@Override
	public LocalisationInfo getLocContents() {
		int cont = LocContents.HAS_BEARING;
		if (difarDataUnit.getDifarCrossing() != null) {
			cont |= LocContents.HAS_LATLONG;
		}
		return new LocContents(cont);
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#bearingAmbiguity()
	 */
	@Override
	public boolean bearingAmbiguity() {
		return false;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getWorldVectors()
	 */
	@Override
	public PamVector[] getWorldVectors() {

		Double ang = difarDataUnit.getMeanBearing();
		if (ang == null) {
			return null;
		}
		else {
			PamVector[] worldVectors = new PamVector[1];
			double radians = Math.toRadians(90-ang);
			worldVectors[0] = new PamVector(Math.cos(radians), Math.sin(radians), 0);
			return worldVectors;
		}
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getAngles()
	 */
	@Override
	public double[] getAngles() {		
		Double ang = difarDataUnit.getMeanBearing();
		if (ang == null) {
			return null;
		}
		double angles[] = new double[1];
		angles[0] = Math.toRadians(ang);
		return angles;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getAngleErrors()
	 */
	@Override
	public double[] getAngleErrors() {
		return angleErr;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getAngleErrors()
	 */
	public void setAngleErrors(double[] angleErr) {
		this.angleErr = angleErr;
	}
	
	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getLatLong(int)
	 */
	@Override
	public LatLong getLatLong(int iSide) {
		if (difarDataUnit.getDifarCrossing() == null) return null;
		return difarDataUnit.getDifarCrossing().getCrossLocation();
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getNumLatLong()
	 */
	@Override
	public int getNumLatLong() {
		return (difarDataUnit.getDifarCrossing() == null ? 0 : 1);
	}

}
