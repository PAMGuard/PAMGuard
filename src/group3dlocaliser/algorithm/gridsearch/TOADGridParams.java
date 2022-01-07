package group3dlocaliser.algorithm.gridsearch;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import group3dlocaliser.grids.SphericalGrid;
import pamMaths.PamVector;

public class TOADGridParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private String gridType = SphericalGrid.gridName;
	
	/**
	 * Grid centre relative to array centre (more important for spherical / cylindrical grids)
	 */
	private PamVector gridCentre = new PamVector(0,0,-12.2);   
	

	@Override
	protected TOADGridParams clone()  {
		try {
			return (TOADGridParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * @return the gridCentre
	 */
	public PamVector getGridCentre() {
		return gridCentre;
	}


	/**
	 * @param gridCentre the gridCentre to set
	 */
	public void setGridCentre(PamVector gridCentre) {
		this.gridCentre = gridCentre;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("gridType");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return gridType;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


}
