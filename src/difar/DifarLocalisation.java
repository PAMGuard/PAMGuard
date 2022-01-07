package difar;

import pamMaths.PamVector;
import Array.ArrayManager;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;

/**
 * Localisations from a single sonobuoy are bearings originating from the 
 * sonobuoy.
 * If more than one sonobuoy detects the same sound, triangulation of the
 * bearings can yeild a latitude and longitude.
 */
public class DifarLocalisation extends AbstractLocalisation {

	private DifarDataUnit difarDataUnit;
	
	/**For DIFAR buoys, 95% of bearings should be within +/- 10 
	 * degrees of the mean bearing, and if the compass is 
	 * calibrated, then mean bearing should be pretty close to 
	 * the actual true bearing. See IWC Paper: SC-65b-SH08
	 */
	private double angleErr[] = {Math.toRadians(10.)};
	
	public DifarLocalisation(DifarDataUnit difarDataUnit, int locContents,
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
	public LocContents getLocContents() {
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

		Double ang = difarDataUnit.getTrueAngle();
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
		Double ang = difarDataUnit.getTrueAngle();
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
