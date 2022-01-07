package Localiser.algorithms.locErrors;

import Localiser.algorithms.locErrors.json.SimpleErrorJsonConverter;
import pamMaths.PamVector;

/**
 * Simple error class. Stores a perpendicular, parallel and depth error and an angle for the perpindicular error. 
 * <br>
 * The perpendicular error is perpendicular to a track line if one exists. Otherwise it points in the y axis direction. 
 * Parallel error is parallel to a track line or if no track line exists it is in the x direction. 
 * The direction of the depth error remains the same no matter the track line. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SimpleError implements LocaliserError {
	
	SimpleLocErrorDraw simpleErrorDraw;  
	
	private static SimpleErrorJsonConverter simpleErrorJsonConverter = new SimpleErrorJsonConverter();
	
	private Double perpindiuclarError; 
	
	private Double parallelError; 
	
	private Double zError; 
	
	/**
	 * The perpendicular  angle. Null if the perpendicular error is just the y error. 
	 */
	private PamVector perpVector;

	/**
	 * Stored perpendicular direction in xy. Stored to prevent lots of calculations from vector. 
	 */
	private Double perpAngle; 


	/**
	 * /**
	 * Create a simple error. 
	 * @param xyzError - an array of the x/perpindicular y/parallel and z error in meters {x y z}
	 */
	public SimpleError(Double xyzError[]) {
		simpleErrorDraw=new SimpleLocErrorDraw(this); 
		perpindiuclarError=xyzError[0]; 
		parallelError=xyzError[1]; 
		zError=xyzError[2]; 
	}
	
	/**
	 * Create a simple error. 
	 * @param xyzError - an array of the x/perpindicular y/parallel and z error in meters {x y z}
	 * @param angle the angle of the perpindicular error in RADIANS. 
	 */
	public SimpleError(Double xyzError[], Double angle) {
		simpleErrorDraw=new SimpleLocErrorDraw(this); 
		perpindiuclarError=xyzError[0]; 
		parallelError=xyzError[1]; 
		zError=xyzError[2]; 
		this.perpAngle=angle; 
	}
	
	/**
	 * Create a simple error. 
	 * @param x - the x/perpendicualar error in meters
	 * @param y - the y/parallel error in meters
	 * @param z - the z error in meters
	 * @param angle - the angle of the perpendicular error in RADIANS
	 */
	public SimpleError(Double x, Double y, Double z, Double angle) {
		simpleErrorDraw=new SimpleLocErrorDraw(this); 
		perpindiuclarError=x; 
		parallelError=y; 
		zError=z; 
		this.perpAngle=angle; 
	}


	/**
	 * Get a simple error. This is slightly more difficult for simple errors as there is a lack of reference info. An elliptical fit cannot be used
	 * bacause a perpindicular and parallel error cannot describe an error ellipse of unknown orientation. Therefore only three 
	 * inputs are possible here LocaliserError.xdirVec,  LocaliserError.ydirVec and LocaliserError.zdirVec. Otherwise NaN will be returned. 
	 * @param errorDirection - the direction of the error. 
	 */
	@Override
	public double getError(PamVector errorDirection) {
		
		if (perpAngle==null) perpAngle=0.0; 
			
			
		if (perpVector==null) perpVector=PamVector.fromHeadAndSlant(Math.toDegrees(perpAngle), 0);
		//if the erro vector is null then only accept y, x vectors. 
		if (perpVector==null){
			if (errorDirection.equals(LocaliserError.xdirVec)) {
				return perpindiuclarError; 
			}
			else if (errorDirection.equals(LocaliserError.ydirVec)){
				return parallelError; 
			}
		}
		
		//if there is an error vector then only accept vectors the same as or perpendicular to that error. 
		else {
			if (errorDirection.equals(this.perpVector)) {
				return perpindiuclarError; 
			}
			else if (errorDirection.equals(this.perpVector.rotate(Math.PI/2))){
				return parallelError; 
			}
		}
		
		if (errorDirection.equals(LocaliserError.zdirVec)){
			return zError; 
		}
		
		return Double.NaN;

	}

	/* (non-Javadoc)
	 * @see Localiser.algorithms.locErrors.LocaliserError#getJSONErrorString()
	 */
	@Override
	public String getJsonErrorString() {
		// TODO Auto-generated method stub
		return simpleErrorJsonConverter.getJsonString(this);
	}

	@Override
	public LocErrorGraphics getErrorDraw() {
		return simpleErrorDraw;
	}
	
	@Override
	public PamVector getErrorDirection(){
		return LocaliserError.ydirVec;
	}

	@Override
	public String getStringResult() {
		String perpErr = formatErrorValue(perpindiuclarError);
		String parErr = formatErrorValue(parallelError);
		return String.format("Perp %sm, Parallel %sm", perpErr, parErr);
	}

	public String formatErrorValue(Double err) {
		if (err == null) {
			return "???";
		}
		double eVal = err;
		if (err > 100) {
			return String.format("%d", (int) eVal);
		}
		else if (err > 1) {
			return String.format("%3.1f", eVal);
		}
		else {
			return String.format("%3.2f", eVal);
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getStringResult();
	}

	public Double getPerpError() {
		return this.perpindiuclarError;
	}

	public Double getParallelError() {
		return this.parallelError;
	}

	public void setPerpVector(PamVector errorVector) {
		this.perpVector=errorVector; 
		this.perpAngle=PamVector.vectorToSurfaceBearing(perpVector);
	}
	
	public void setPerpAngle(double errorAngle) {
		this.perpAngle=errorAngle;
		perpVector=null;//set the vector to null
	}

	
	public Double getPerpAngle() {
		return perpAngle;
	}

	public Double getDepthError() {
		return this.zError;
	}

	@Override
	public double getErrorMagnitude() {
		double err = 0;
		if (perpindiuclarError != null) {
			err += Math.pow(perpindiuclarError, 2);
		}
		if (parallelError != null) {
			err += Math.pow(parallelError, 2);
		}
		if (zError != null) {
			err += Math.pow(zError, 2);
		}
		return Math.sqrt(err);
	}


}
