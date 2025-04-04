package group3dlocaliser.localisation;

import GPS.GpsData;
import Localiser.detectionGroupLocaliser.LocalisationChi2;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamguardMVC.PamDataUnit;
import group3dlocaliser.algorithm.Chi2Data;
import pamMaths.PamVector;

public class LinearLocalisation extends AbstractLocalisation implements LocalisationChi2{

	private double[] angles;
	private Double range;
	private GpsData referencePosition;
	private Chi2Data chi2Dat;

	public LinearLocalisation(PamDataUnit pamDataUnit, int referenceHydrophones, PamVector[] arrayAxes, double bearing, Double range) {
		super(pamDataUnit, LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY, referenceHydrophones);
		setArrayAxis(arrayAxes);
		this.angles = new double[1];
		angles[0] = bearing;
		this.range = range;
		if (range != null) {
			this.getLocContents().addLocContent(LocContents.HAS_RANGE);
		}
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getAngles()
	 */
	@Override
	public double[] getAngles() {
		return angles;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getRange(int)
	 */
	@Override
	public double getRange(int iSide) {
		return range;
	}

	@Override
	public Double getChi2() {
		return chi2Dat.getChi2();
	}

	@Override
	public Integer getNDF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getAic() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AbstractLocalisation#getOriginLatLong()
	 */
	@Override
	public GpsData getOriginLatLong() {
		if (referencePosition != null) {
			return referencePosition;
		}
		else {
			return super.getOriginLatLong();
		}
	}

	/**
	 * @return the referencePosition
	 */
	public GpsData getReferencePosition() {
		return referencePosition;
	}

	/**
	 * @param referencePosition the referencePosition to set
	 */
	public void setReferencePosition(GpsData referencePosition) {
		this.referencePosition = referencePosition;
	}

	/**
	 * Set Chi2 data
	 * @param chi2dat
	 */
	public void setChi2(Chi2Data chi2dat) {
		this.chi2Dat = chi2dat;
	}
	
	
	
	

}
