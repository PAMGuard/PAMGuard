package simulatedAcquisition.movement;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class CircularMovementParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public int angleStep = 1;
	
	public int directionsPerPoint = 10;
	
	public int[] rangeRange = {1000, 1000};
	
	public int rangeStep = 1000;
	
	public int[] depthRange = {0, 0};
	
	public int depthStep = 100;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected CircularMovementParams clone() {
		try {
			return (CircularMovementParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getAngleStep() {
		return angleStep;
	}

	public void setAngleStep(int angleStep) {
		this.angleStep = angleStep;
	}

	public int getDirectionsPerPoint() {
		return directionsPerPoint;
	}

	public void setDirectionsPerPoint(int directionsPerPoint) {
		this.directionsPerPoint = directionsPerPoint;
	}

	public int[] getRangeRange() {
		return rangeRange;
	}

	public void setRangeRange(int[] rangeRange) {
		this.rangeRange = rangeRange;
	}

	public int getRangeStep() {
		return rangeStep;
	}

	public void setRangeStep(int rangeStep) {
		this.rangeStep = rangeStep;
	}

	public int[] getDepthRange() {
		return depthRange;
	}

	public void setDepthRange(int[] depthRange) {
		this.depthRange = depthRange;
	}

	public int getDepthStep() {
		return depthStep;
	}

	public void setDepthStep(int depthStep) {
		this.depthStep = depthStep;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
