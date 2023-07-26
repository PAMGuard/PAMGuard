package Localiser.algorithms.locErrors;

import Localiser.algorithms.locErrors.json.EllipseJsonConverter;
import pamMaths.PamVector;

/**
 * A 3D elliptical error. Elliptical error is made up of 3 radii and a quaternion/euler angles representing the heading pitch and roll of the ellipse. 
 * This a good approximation of error for most situations, however will not be a good approximation for non symmetric or cmore complex errors. 
 * @author Jamie Macaulay
 *
 */
public class EllipticalError implements LocaliserError {
	
	/**
	 * The error ellips.e 
	 */
	private ErrorEllipse errorEllipse;
	
	
	private static EllipseJsonConverter errorEllipseXMLData = new EllipseJsonConverter();
	
	/**
	 * Class for drawing the error.
	 */
	private EllipseLocErrorDraw ellipseLocErrorDraw; 

	/**
	 * Generate an error ellipse from a set of points. The dimensions of the error ellipse is by
	 * default the 95% confidence interval of the points. The location of the ellipse is the mean of the points. 
	 * @param points a scatter of points. Can be 2D or 3 set of points. 
	 */
	public EllipticalError(double[][] points){
		errorEllipse=new ErrorEllipse(points);
		ellipseLocErrorDraw= new EllipseLocErrorDraw(this);
	}
	
	/**
	 * Create an empty elliptical error. The error ellipse needs to be set explicitly. 
	 */
	protected EllipticalError(){
		ellipseLocErrorDraw= new EllipseLocErrorDraw(this);
	}

	/* 
	 * Create an error ellipse. This can be a 3D or 2D ellipse. For a 2D ellipse errors[2] should be -1. 
	 * @param errors - the size of the errors in 3 dimensions. x (longest radii of ellipse) , y (second longest radii of ellipse), z (third longest radii of ellipse). 
	 * @param angles - the rotation of the ellipse, heading pitch and roll in RADIANS. 
	 */
	public EllipticalError(double[] angles, double[] errors) {
		ellipseLocErrorDraw= new EllipseLocErrorDraw(this);
		errorEllipse = new ErrorEllipse(errors, angles);
	}

	
	
	@Override
	public double getError(PamVector errorDirection) {
		return errorEllipse.getErrorMagnitude(errorDirection.getUnitVector().getVector());
	}

	@Override
	public String getJsonErrorString() {
		return errorEllipseXMLData.getJsonString(this);
	}

	@Override
	public LocErrorGraphics getErrorDraw() {
		return ellipseLocErrorDraw;
	}

	public void setErrorEllipse(ErrorEllipse errorEllipse) {
		this.errorEllipse=errorEllipse; 
	}
	

	public ErrorEllipse getErrorEllipse( ) {
		return errorEllipse;
	}


	/**
	 * Get the 2D elliptical error.
	 * @param planeXy - the plane on whihc to find the ellipse for. 
	 * @return the elliptical error. 
	 */
	public double[] getErrorEllipse2D(int planeXy) {
		return errorEllipse.getErrorEllipse2D(planeXy);
	}

	@Override
	public PamVector getErrorDirection() {
		return LocaliserError.ydirVec;
	}
	

	/**
	 * 
	 * @return
	 */
	public double[] getEllipseDim() {
		return errorEllipse.getEllipseDim();
	}


	/**
	 * 
	 * @return
	 */
	public double[] getAngles() {
		return errorEllipse.getAngles();
	}

	@Override
	public String getStringResult() {
		String description=""; 
		if (!errorEllipse.is3D()){
			description+=String.format("<i>&ensp 2D Elliptical error: <br>&ensp Radii: ");
			for (int i=0; i<errorEllipse.getEllipseDim().length; i++){
				if (errorEllipse.getEllipseDim()[i]>=0) description+=String.format("%4.1fm " ,errorEllipse.getEllipseDim()[i]);
			}
			description+=String.format(" Angle: %4.1f&#176" ,Math.toDegrees(errorEllipse.getAngles()[0]));
		}
		else description+=String.format("<i>&ensp 3D Elliptical error: <br>&ensp Radii: %4.1fm %4.1fm %4.1fm <br> &ensp Orientation: heading %4.1f&#176 pitch %4.1f&#176 roll %4.1f&#176</i>",
				errorEllipse.getEllipseDim()[0], errorEllipse.getEllipseDim()[1], errorEllipse.getEllipseDim()[2],
				Math.toDegrees(errorEllipse.getAngles()[0]), Math.toDegrees(errorEllipse.getAngles()[1]), Math.toDegrees(errorEllipse.getAngles()[2])); 
		return description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getStringResult();
	}

	@Override
	public double getErrorMagnitude() {
		if (errorEllipse == null || errorEllipse.getEllipseDim() == null) {
			return 0;
		}
		double err = 0;
		double[] errVec = errorEllipse.getEllipseDim();
		for (int i = 0; i < errVec.length; i++) {
			err += Math.pow(errVec[i], 2);
		}
		return Math.sqrt(err);
	}
	
	

}
