package group3dlocaliser.grids;

/**
 * Parameter control for all 3D grids. Can we use the same 
 * param set for all ? 
 * @author dg50
 *
 */
public class Grid3DParams {

	private double[][] dimensionRange = new double[3][];
	
	private int[] dimensionPoints = new int[3];
	
	private String gridName;

	/**
	 * @return the dimensionRange
	 */
	public double[] getDimensionRange(int iDimension) {
		return dimensionRange[iDimension];
	}

	/**
	 * @param dimensionRange the dimensionRange to set
	 */
	public void setDimensionRange(int iDimension, double[] dimensionRange) {
		this.dimensionRange[iDimension] = dimensionRange;
	}

	/**
	 * @return the dimensionPoints
	 */
	public int getDimensionPoints(int iDimension) {
		return dimensionPoints[iDimension];
	}

	/**
	 * @param dimensionPoints the dimensionPoints to set
	 */
	public void setDimensionPoints(int iDimension, int dimensionPoints) {
		this.dimensionPoints[iDimension] = dimensionPoints;
	}

	/**
	 * @return the gridName
	 */
	public String getGridName() {
		return gridName;
	}

	/**
	 * @param gridName the gridName to set
	 */
	public void setGridName(String gridName) {
		this.gridName = gridName;
	}

}
