package group3dlocaliser.grids;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class SphericalGridParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Angle step in degrees
	 */
	private double angleStep = 15;
	
	private double[] rangeRange = {1., 50.};
	
	private int nRanges = 20;
	
	private boolean logRangeScale = true;
	
	@Override
	protected SphericalGridParams clone() {
		try {
			return (SphericalGridParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the angleStep
	 */
	public double getAngleStep() {
		return angleStep;
	}

	/**
	 * @param angleStep the angleStep to set
	 */
	public void setAngleStep(double angleStep) {
		this.angleStep = angleStep;
	}

	/**
	 * @return the rangeRange
	 */
	public double[] getRangeRange() {
		return rangeRange;
	}

	/**
	 * @param rangeRange the rangeRange to set
	 */
	public void setRangeRange(double[] rangeRange) {
		this.rangeRange = rangeRange;
	}

	/**
	 * @return the logRangeScale
	 */
	public boolean isLogRangeScale() {
		return logRangeScale;
	}

	/**
	 * @param logRangeScale the logRangeScale to set
	 */
	public void setLogRangeScale(boolean logRangeScale) {
		this.logRangeScale = logRangeScale;
	}

	/**
	 * @return the nRanges
	 */
	public int getnRanges() {
		return nRanges;
	}

	/**
	 * @param nRanges the nRanges to set
	 */
	public void setnRanges(int nRanges) {
		this.nRanges = nRanges;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}


}
