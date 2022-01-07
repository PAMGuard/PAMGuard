package group3dlocaliser.grids;

import PamController.PamSettings;
import pamMaths.PamVector;

/**
 * 3D grid for localisation. These will come in many shapes and sizes, 
 * Rectangular, spherical, cylindrical, etc. 
 * @author dg50
 *
 */
public abstract class Grid3D implements PamSettings {
	
	private String settingsName;

	/**
	 * @param grid3dParams
	 */
	public Grid3D(String settingsName) {
		this.settingsName = settingsName;
	}

	/**
	 * 
	 * @return A descriptive name for the grid. 
	 */
	abstract public String getGridName();
	
	/**
	 * Get the total number of points in the grid. 
	 * @return number of grid points
	 */
	abstract public int getTotalPoints();
	
	/**
	 * Reset the grid so that the next call to getNextPoint will 
	 * return the first grid point again. 
	 */
	abstract public void resetGrid();
	
	/**
	 * Get the next point in the grid. Will return null when 
	 * it's complete. 
	 * @return next grid point
	 */
	abstract public PamVector getNextPoint();

	@Override
	public String getUnitName() {
		return settingsName;
	}

	@Override
	public String getUnitType() {
		return "Grid:"+getGridName();
	}
}
