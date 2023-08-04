package group3dlocaliser.algorithm.toadmcmc;

import Array.SnapshotGeometry;
import Localiser.algorithms.locErrors.EllipseLocErrorDraw;
import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.LocErrorGraphics;
import PamUtils.PamArrayUtils;

/**
 * Elliptical error for MCMC.
 *  
 * @author Jamie Macaulay
 *
 */
public class MCMCEllipticalError extends EllipticalError {
	
		/**
	 * Class for drawing the error.
	 */
	private EllipseLocErrorDraw ellipseLocErrorDraw = new MCMCErrorDraw(this);
	
	
		private float[][] points;


		private double[] meanLoc; 


	public double[] getMeanLoc() {
			return meanLoc;
		}



	public MCMCEllipticalError(double[][] points, double[] meanLoc) {
		super(points);
		this.points = PamArrayUtils.double2Float(points);
		this.meanLoc = meanLoc;
	}



	public float[][] getPoints() {
		return points;
	}



	public void setPoints(float[][] points) {
		this.points = points;
	}



	@Override
	public LocErrorGraphics getErrorDraw() {
		return ellipseLocErrorDraw;
	}
}
