package Localiser.algorithms.locErrors;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import Localiser.algorithms.genericLocaliser.MinimisationFunction;
import Localiser.algorithms.genericLocaliser.simplex.Simplex.Chi2;
import PamUtils.PamUtils;
import pamMaths.PamVector;

/**
 * Errors are generated from the curvature of a Chi2 surface. Saves the error as an ellipse. Thus, once saved
 * some information will be lost.
 * 
 * @author Jamie Macaulay 
 *
 */
public class LikilihoodError extends EllipticalError {

	/*
	 * The chi2 surface. This might be null but if around then use it!
	 */
	private MinimisationFunction chi2;
	
	/**
	 * The solution on the surface for the error estimate. 
	 */
	private double[] point; 
	
	/**
	 * The number of points around a sphere to search for the largest eigenvector for the error ellipse. 
	 */
	private int nSpherePoints=512; 


	public LikilihoodError(MinimisationFunction chi2, double[] point){
		super();
		this.chi2=chi2;
		this.point=point; 
		super.setErrorEllipse(chi2SurftoErrorEllipse(point, 3)); 
	}
	
	
	public LikilihoodError(MinimisationFunction chi2, double[] point, int nDim){
		super();
		this.chi2=chi2;
		this.point=point; 
		super.setErrorEllipse(chi2SurftoErrorEllipse(point, nDim)); 
	}
	
	
	
	/**
	 * Calculate the error ellipse from a chi2surface. 
	 * @param chi2 - the chi2 surface; 
	 * @param point - the point on the surface to search around for error. 
	 * @param nDim - the number of dimensions. 2 or 3. 
	 * @return the approximate 3D ellipse which represents the error. 
	 */
	private ErrorEllipse chi2SurftoErrorEllipse(double[] point, int nDim){
		ErrorEllipse errorEllipse; 
		if (nDim==2){
			errorEllipse = chi2SurfToErrorEllipse2D( point);
		}
		else if (nDim==3){
			errorEllipse = chi2SurfToErrorEllipse3D (point);
			if (errorEllipse==null) {
				//maight have failed due to a 3D model trying to fit a 2D problem and therefore 
				//heading off to infinity...
				errorEllipse = chi2SurfToErrorEllipse2D( point);
			}
		}
		else{
			System.err.println("Likilihood Error: Error ellipse only implmented for 2 or 3 dimensions"); 
			return null; 
		}
		
		return errorEllipse; 
	}
	
	
	/**
	 * Calculate the error ellipse for a 2D chi2 surface. 
	 * @param chi2 - the chi2 surface; 
	 * @param point - the point on the surface to search around for error. 
	 * @return the approximate 3D ellipse which represents the error. 
	 */
	private ErrorEllipse chi2SurfToErrorEllipse2D(double[] point2) {
		
		//create a set of numbers around a sphere; 
		double angleBin=(2*Math.PI)/nSpherePoints;

		double curvatureError; 
		double max=Double.MIN_VALUE;
		int ind=-1; 
		for (int i=0; i<nSpherePoints; i++){
			//now because the sphere has a radius of 1, all the points are already unit vectors. 
			curvatureError=getLLCurvature(point, PamVector.fromHeadAndSlant(Math.toDegrees(angleBin*i), 0));
			if (curvatureError>max){
				max=curvatureError;
				ind=i; //record index of max value; 
			}
		}
		
		double[] dim={max, getLLCurvature(point, PamVector.fromHeadAndSlant(Math.toDegrees(angleBin*ind)+90, 0)),-1 };
		double[] angles={angleBin*ind, 0,0};
		
		for (int i=0; i<dim.length; i++){
			if (Double.isInfinite(dim[i]) || Double.isNaN(dim[i])){
//				System.err.println("Likilihood Error: An infinite or NaN value has been returned");
				return null;
			}
		}
		
//		System.out.println("LikilihoodError: dim: "+dim[0] +" "+dim[1]+" "+dim[2]);

		//so for 2D problme that is us! 
		return new ErrorEllipse(dim, angles);
	}


	/**
	 * Calculate the error ellipse for a 3D chi2 surface. 
	 * @param chi2 - the chi2 surface; 
	 * @param point - the point on the surface to search around for error. 
	 * @return the approximate 3D ellipse which represents the error. Returns null if it is not possible to calculate an error. 
	 */
	private ErrorEllipse chi2SurfToErrorEllipse3D(double[] point){
		//first, find the largest error. 

		//create a set of numbers around a sphere; 
		double[][] spherePoints= PamUtils.getSpherePoints(nSpherePoints, 1);

		double curvatureError; 
		double max=-Double.MAX_VALUE;
		int ind=-1; 
		for (int i=0; i<spherePoints.length; i++){
			//now because the sphere has a radius of 1, all the points are already unit vectors. 
			curvatureError=getLLCurvature(point, new PamVector(spherePoints[i]));
			//System.out.printf("Curve error %d = %3.2f\n", i, curvatureError);
			if (curvatureError>max){
				max=curvatureError;
				ind=i; //record index of max value; 
			}
		}
		
		/**
		 * Curvature can be -Infinity of a 3D algorithm has tried to solve a 2D problem- need to used the 2D likilihood calculation
		 * so return null. 
		 */
		if (ind==-1) return null; 
		
		//so the vector with maximum error is the largest eigenvector of the ellipsoid i.e. the first radius. 
		//Now the other two eigenvectors are in an orthogonal plane to this vector....need to search this plane for the second eigenvector. 
		Vector3D firstEigenvector=new Vector3D(spherePoints[ind]); 
		Plane plane = new Plane(firstEigenvector, 0.1);
		
		double binSize=0.5*Math.sqrt(nSpherePoints); //want to keep this efficient; 
		double maxPlane=-Double.MAX_VALUE;
		int indPlane=0; 
		int n=0; 
		for (double i=0; i<Math.PI; i=i+binSize){
	
			//now create a 2D vector 
			Vector2D vector2D=new Vector2D(Math.cos(i), Math.sin(i)); 
			Vector3D location3D=plane.getPointAt(vector2D, 0);
			
			// get the max error in this plane. 
			curvatureError=getLLCurvature(point, new PamVector(location3D.toArray()));
			if (curvatureError>maxPlane){
				maxPlane=curvatureError;
				indPlane=n; //record index of max value; 
			}
			n++;
		}
		
		/**
		 * Curvature can be -Infinity of a 3D algorithm has tried to solve a 2D problem- need to used the 2D likilihood calculation
		 * so return null. 
		 */
		if (indPlane==-1) return null; 
		
		//now we have all information for the roll, and all three vectors. 
		Vector3D secondVector=new Vector3D(spherePoints[indPlane]); 

		//the third eigenvector
		Vector3D thirdVector=Vector3D.crossProduct(firstEigenvector, secondVector);
		
//		System.out.println(" First vector: "+firstEigenvector.toString()+ " ind "+ind);
//		System.out.println("Angle: " +Math.toDegrees(PamVector.vectorToSurfaceBearing(new PamVector(firstEigenvector.toArray())))); 
//		System.out.println(" Second vector: "+secondVector.toString());
//		System.out.println(" Third vector: "+thirdVector.toString());
//		System.out.println(" Magnitude: "+max + " " +maxPlane+ " "+getLLCurvature( chi2,  point, new PamVector(thirdVector.toArray())));
		
		
		//we have three eigenvectors. Now we can generate an ellipse from these three vectors. Remember eigenvectors are unit vectors so must 
		//multiply by error value. 
		ArrayList<Vector3D> vectors=new ArrayList<Vector3D>(); 
		vectors.add(firstEigenvector.scalarMultiply(max)); //have already calculated error in previous steps. 
		vectors.add(secondVector.scalarMultiply(maxPlane)); //have already calculated error in previous steps. 
		vectors.add(thirdVector.scalarMultiply(getLLCurvature( point, new PamVector(thirdVector.toArray()))));
		
		//create an error ellipse.
		ErrorEllipse errorEllipse=new ErrorEllipse(vectors); 

		return errorEllipse;
		
	}
	
	
	@Override
	public double getError(PamVector errorDirection) {
		//if the chi2 surface is available then use that to get error. Otherwise return errror form the ellipse. 
//		if (chi2!=null) return getLLCurvature( chi2,  point,errorDirection);
//		else return super.getError(errorDirection);
		return super.getError(errorDirection);
	}
	
	/**
	 * The log likilihood. 
	 * @param location - the point on the chi2 surface to fin dlog likilhood for.  
	 * @return the log likilihood. 
	 */
	public double logLikelihood(double[] location){
		return -chi2.value(location) / 2;
	}

	
	/**
	 * Get chi2 error for any direction
	 * @param chi2 - the chi2 surface
	 * @param point - the minima of the surface i.e. the soluton
	 * @param errorVector - the direction in which to calculate the error. 
	 * @return the error magnitude in the direction. This is the average value of the error in the 
	 * direction specified and in the opposite direction. Curvature is expressed as 1 standard deviation
	 * error
	 */
	private double getLLCurvature(double[] point, PamVector errorVector) {
	
		double dis = 10; //the jump along the chi2 surface. 
		double err = 0;
		int nE = 0;
		
		PamVector locVector=new PamVector(point);
	
		//double[] vector={dis*Math.cos(direction), dis*Math.sin(direction), 0}; 
		//PamVector pointVector=new PamVector(errorVector); 
		
		PamVector newloc1=locVector.add(errorVector);
		PamVector newloc2=locVector.sub(errorVector);
		try {
			while (true) {
				double ll1, ll2, ll3;
				double[] pos2 = Arrays.copyOf(point, point.length);
				ll2 = logLikelihood(pos2);
				ll1 = logLikelihood(Arrays.copyOf(newloc1.getVector(), pos2.length));
				ll3 = logLikelihood(Arrays.copyOf(newloc2.getVector(), pos2.length));
				double q = (ll1/ll2 + ll3/ll2 - 2) / (dis*dis);
				err = 1./Math.sqrt(q);
				double change = dis/err;
				if (++nE > 8 || (change > 0.95 && change < 1.05)) {
					break;
				}
				dis = err;
				
				errorVector.normalise();
	
				newloc1=locVector.add(errorVector.times(dis));
				newloc2=locVector.sub(errorVector.times(dis));
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return err;
	}

	
	/**
	 * Get the curvature of a log likelihood surface along any dimension.
	 * @param chi2 Chi2 surface
	 * @param point starting point
	 * @param dim dimension to move in
	 * @return curvature, expressed as 1 SD error
	 */
	@Deprecated 
	private double getLLCurvature(Chi2 chi2, double[] point, int dim) {

		double dis = 5; //the jump along the chi2 surface. 
		double err = 0;
		int nE = 0;
		try {
			while (true) {
				double ll1, ll2, ll3;
				double[] pos2 = Arrays.copyOf(point, point.length);
				ll2 = chi2.logLikelihood(pos2);
				pos2[dim] -= dis;
				ll1 = chi2.logLikelihood(pos2);
				pos2 = Arrays.copyOf(point, point.length);
				pos2[dim] += dis;
				ll3 = chi2.logLikelihood(pos2);
				double q = (ll1 + ll3 - 2*ll2) / (dis*dis);
				err = 1./Math.sqrt(-q);
				double change = dis/err;
				if (++nE > 4 || (change > 0.95 && change < 1.05)) {
					break;
				}
				dis = err;
			}
		} catch (FunctionEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return err;
	}

}
