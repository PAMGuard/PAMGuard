package simulatedAcquisition.movement;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class GridMovementParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public static final String[] dimNames = {"X", "Y", "Z"};
	/**
	 * Ranges of grid search in z, y and z;
	 */
	int[][] distRangeMetres = {{-20, 20}, {-20, 20}, {0, 10}};
		
	/**
	 * Step size in metres across grid in three dimensions. 
	 */
	double[] distStepsMetres = {10.0, 10.0, 10.0};
	
	/**
	 * Number of different directions to point in at each step.
	 */
	int directionsPerPoint = 4;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected GridMovementParams clone() {
		try {
			return (GridMovementParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("distRangeMetres");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return distRangeMetres;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("distStepsMetres");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return distStepsMetres;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("directionsPerPoint");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return directionsPerPoint;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
