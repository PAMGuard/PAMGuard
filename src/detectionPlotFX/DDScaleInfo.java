package detectionPlotFX;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Holds information on the scale (e.g. min max values) for the x axis and y axis. 
 * @author Jamie Macaulay
 *
 */
public class DDScaleInfo {
	
	/**
	 * The minimum x axis property
	 */
	public DoubleProperty minXVal=new SimpleDoubleProperty(0); 
	
	/**
	 * The maximum x axis property
	 */
	public DoubleProperty maxXVal=new SimpleDoubleProperty(1); 

	/**
	 * The minimum y value property
	 */
	public DoubleProperty minYVal=new SimpleDoubleProperty(0); 

	/**
	 * The maximum y value property.
	 */
	public DoubleProperty maxYVal=new SimpleDoubleProperty(1);
	
	/**
	 * The minimum z value property
	 */
	public DoubleProperty minZVal=new SimpleDoubleProperty(0); 

	/**
	 * The maximum z value property.
	 */
	public DoubleProperty maxZVal=new SimpleDoubleProperty(1);

	/**
	 * The number of dimensions- either 2 or 3. 
	 */
	private int dim=2; 


	/**
	 * Constructor for DDCaleInfo. 
	 * @param minX - the minimum value of the x axis.
	 * @param maxX - the maximum value of the x axis
	 * @param minY - the minimum value of the y axis
	 * @param maxY - the maximum value of the y axis.
	 */
	public DDScaleInfo(double minX, double maxX, double minY, double maxY) {
		minXVal.setValue(minX);
		maxXVal.setValue(maxX);
		minYVal.setValue(minY);
		maxYVal.setValue(maxY);
		dim=2; 
	}


	/**
	 * Constructor for DDCaleInfo- used for 3D graphs e.g. Wigner plots or 3D phase plots.  
	 * @param minX - the minimum value of the x axis.
	 * @param maxX - the maximum value of the x axis
	 * @param minY - the minimum value of the y axis
	 * @param maxY - the maximum value of the y axis.
	 */
	public DDScaleInfo(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
		minXVal.setValue(minX);
		maxXVal.setValue(maxX);
		minYVal.setValue(minY);
		maxYVal.setValue(maxY);
		minZVal.setValue(minZ);
		maxZVal.setValue(maxZ);
		dim=3; 
	}

	/**
	 * The number of dimensions the graph shows. 
	 * @return the number of dimension, either 2 or 3. 
	 */
	public int getDim() {
		return dim;
	}



	/**
	 * The number of dimensions the graph shows. 
	 * @param dim the number of dimension, either 2 or 3. 
	 */
	public void setDim(int dim) {
		if (dim!=2 || dim!=3){
			System.err.println("DDScaleInfo: The number of dimensions must be 2 or 3");
			return;
		}
		this.dim = dim;
	}


}
