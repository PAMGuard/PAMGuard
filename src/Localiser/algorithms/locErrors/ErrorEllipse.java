package Localiser.algorithms.locErrors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;


/**
 * Class for calculating errors from localisation data. An error ellipse
 * describes N dimensional error based on a scatter of points or chi2 surface.
 * The dimensions and rotation of the ellipse describe the distribution of
 * error. Note that although an ellipse will often be a satisfactory description
 * of an error surface, in some cases it will not adequately represent errors.
 * e.g. a linear array produces a doughnut shaped error surface which would not
 * be described well by an ellipse.
 * 
 * @author Jamie Macaulay
 *
 */
public class ErrorEllipse {
	
	/**
	 * Slice through the ellipse in the XY plane 
	 */
	public static final int PLANE_XY=0; 
	
	/**
	 * Slice through the ellipse in the ZY plane
	 */
	public static final int PLANE_ZY=1; 

	
	/**
	 * Slice through the ellipse in the ZX plane
	 */
	public static final int PLANE_ZX=2; 
	
	/**
	 *Project the ellipse ontpo the XY plane- the extremities of the ellipse are kept
	 */
	public static final int PLANE_XY_PROJ=3; 
	
	/**
	 *Project the ellipse ontpo the Yz plane- the extremities of the ellipse are kept
	 */
	public static final int PLANE_ZY_PROJ=4; 
	
	/**
	 *Project the ellipse ontpo the ZX plane- the extremities of the ellipse are kept
	 */
	public static final int PLANE_ZX_PROJ=5; 
	
	/**
	 * A 2D projection of the ellipsoid in the XY plane. 
	 * Only usaed if this is a 3D ellipse (ellipsoid). 
	 */
	public ErrorEllipse errorEllipseXY; 
	
	/**
	 * A 2D projection of the ellipsoid in the ZY plane. 
	 * Only usaed if this is a 3D ellipse (ellipsoid). 
	 */
	public ErrorEllipse errorEllipseZY; 

	/**
	 * A 2D projection of the ellipsoid in the ZX plane. 
	 * Only usaed if this is a 3D ellipse (ellipsoid). 
	 */
	public ErrorEllipse errorEllipseZX; 


//	/**
//	 * For 95% confidence interval is 2.4477
//	 */
//	double chisquare_val=2.4477; 
	
	/**
	 * For SD confidence interval is 1;
	 */
	private double chisquare_val=1; 

	/**
	 * The dimensions of the ellipse/ellipsoid. This is generally a,b for 2D and a, b, c for 3D 
	 * the ellipse/ellipsoid is described by x^2/a + y^2/b+ z^2/c =1. c is -1 if a 2D ellipse. 
	 */
	private double[] ellipseDim; 

	/**
	 * The angle of the ellipsoid in RADIANS. Angles are euler angles and in order heading pitch and roll.  
	 */
	private double[] angles; 


	/**
	 * Generate an error ellipse from a set of points. The dimensions of the error ellipse is by
	 * default the 95% confidence interval of the points. The location of the ellipse is the mean of the points. 
	 * @param points a scatter of points. Can be 2D or 3 set of points. 
	 */
	ErrorEllipse(double[][] points){
		calcErrorEllipse(points);
	}
	
	
	/**
	 * Construct an error ellipse from 3 eigenvectors. 
	 * @param eigenvectors 3 eigenvectors describing the magnitude and direction of the error. 
	 */
	ErrorEllipse(ArrayList<Vector3D> eigenvectors){
		
		this.angles=calcAngles(eigenvectors.get(0).toArray(), eigenvectors.get(1).toArray(), 3);
	
		double[] dim=new double[3];
		dim[0]=eigenvectors.get(0).distance(new Vector3D(0,0,0)); 
		dim[1]=eigenvectors.get(1).distance(new Vector3D(0,0,0)); 
		dim[2]=eigenvectors.get(2).distance(new Vector3D(0,0,0)); 
		
		this.ellipseDim=dim; 

//		double[][] eigenVecdbl= new double[3][3];
//		for (int i=0; i<eigenVecdbl.length; i++){
//			eigenVecdbl[i][0]=eigenvectors.get(i).getX();
//			eigenVecdbl[i][1]=eigenvectors.get(i).getY();
//			eigenVecdbl[i][2]=eigenvectors.get(i).getZ();
//		}
//		
//		Array2DRowRealMatrix eigenVec=new Array2DRowRealMatrix(eigenVecdbl); 
//		
//		double[][] eigenValdbl= new double[3][3];
//		for (int i=0; i<eigenValdbl.length; i++){
//			//this is just because the eigenvectors are compensated to get chi2 value in calcErrorEllipse. This keeps
//			//the values of a b and c the szame as the magnitude of the vectors. 
//			eigenValdbl[i][i]=Math.pow(eigenvectors.get(i).distance(new Vector3D(0,0,0))/chisquare_val,2);
//			//eigenValdbl[i][i]=eigenvectors.get(i).distance(new Vector3D(0,0,0));
//		}
//		
//		Array2DRowRealMatrix eigenVal=new Array2DRowRealMatrix(eigenValdbl); 
//
//		calcErrorEllipse(eigenVal, eigenVec);
		
	}
	
	/**
	 * Create an error ellipse. This can be a 3D or 2D ellipse. For a 2D ellipse ellipseDim[2] should be -1. 
	 * @param ellipseDim - the dimensions of the ellipse. 
	 * @param angles - the rotation of the ellipse, heading pitch and roll in RADIANS. 
	 */
	ErrorEllipse(double[] ellipseDim, double[] angles){
		this.ellipseDim=ellipseDim;		
		this.angles=angles;
	}


	/**
	 * Calculate the error for a set of points. 
	 * @param points
	 */
	public void calcErrorEllipse(double[][] points){
		if (points==null){
			return; 
		}

		Covariance cov= new Covariance(points, false); 
		
//		System.out.println("Coveriance"); 
//		printMatrix(cov.getCovarianceMatrix());
		
		//calculate the eigenvectors and eigenvalues 
		EigenDecomposition eign=new EigenDecomposition(cov.getCovarianceMatrix());
		RealMatrix eigenVal = eign.getD();
		RealMatrix eigenVec = eign.getV();
		
//		System.out.println("Eigenvalues"); 
//		printMatrix(eigenVal); 
//	
//		System.out.println("Eigenvectors"); 
//		printMatrix(eigenVec);

		calcErrorEllipse(eigenVal, eigenVec);
		
		//because the ellipse angles are degrees from normal position which is sitting along the x
		// axis we need to rotate by 90 degrees to get in terms of the y axis. 
		//angles[0]=PamUtils.PamUtils.constrainedAngleR(angles[0]+(3*Math.PI)/4, Math.PI);
		//angles[0]=-angles[0]; 
	}
	
	/**
	 * Calculate the error ellipse from eigenvalues and eigenvectors. 
	 * @param eigenVal - matrix the eigenvalues - this is the size of the ellipse
	 * @param eigenVec - matrix eigenvectors- the direction of the ellipse. Not necassarily in order. 
	 */
	private void calcErrorEllipse(RealMatrix eigenVal, RealMatrix eigenVec){
		
		//find the largest eigenvector and eigenvalue and sort in order 
		final double[] maxValArray=new double[eigenVal.getColumnDimension()];
		for (int i=0; i<eigenVal.getColumnDimension(); i++){

			//find the largest eigenvalue and it's index in the matrix. 
			double maxEigenVal=Double.MIN_VALUE;

			for (int j=0; j<eigenVal.getRowDimension(); j++){
				double val=eigenVal.getEntry(j, i);
				if (val>maxEigenVal){
					maxEigenVal=val; 
					//maxInd=i; 
				}
			}
			maxValArray[i]=maxEigenVal;
		}
		
		//System.out.println("maxValArray" + maxValArray[0] +" " +maxValArray[1]+" "+maxValArray[2]); 

		//create list of indexes()
		final Integer[] idx = new Integer[maxValArray.length];
		for (int i=0; i<maxValArray.length; i++){
			idx[i]=i; 
		}
		
		//sort array from largest to smallest. 
		//would have been nicer to have done this using the streams API in Java 8 but PG
		//not quite ready for 8 yet. 
		Arrays.sort(idx, new Comparator<Integer>() {
			@Override public int compare(final Integer o1, final Integer o2) {
				return Double.compare(maxValArray[o2], maxValArray[o1]);
			}
		});
		
		//System.out.println("idx " + idx[0] +" " +idx[1]+" "+idx[2]); 

		//now have indexes of largest to smallest eigenvalues. These will make up the error ellipse. 
		double[] first_eignevector=eigenVec.getColumn(idx[0]); //need for direction
		double[] second_eigenvector=eigenVec.getColumn(idx[1]); //need for roll


		/**Size of errors**/

		//work out the shape of the ellipse. This is generally a,b for 2D and a, b, c for 3D 
		// the ellipse/ellipsoid is described by x^2/a + y^2/b+ z^2/c =1
		ellipseDim=new double[maxValArray.length];
		for (int i=0; i<ellipseDim.length; i++){
			ellipseDim[i]=chisquare_val*Math.sqrt(maxValArray[idx[i]]);
		}				
				
		/**Direction of errors**/

		//here's where it gets a bit more complex. For a 2D ellipse only one rotation is needed. 
		//For a 3D ellipse a full set of angles are required. So best to describe with a vector 
		//i.e. a Quaternion as need 4 elements to describe roll. 

		this.angles=calcAngles(first_eignevector, second_eigenvector, maxValArray.length); 
		
	}
	
	/**
	 * Calculate the heading pitch and roll of the ellipse from two vectors. 
	 * @param first_eignevector - vector of largest radii. 
	 * @param second_eigenvector - vecotr of second largest radii
	 * @param dimesnion of ellipse, 2 or 3. 
	 */
	private double[] calcAngles(double[] first_eignevector, double[] second_eigenvector, int maxValArray){
		//bit messy here at the moment. 
		double heading=0; 
		double pitch=0; 
		double roll=0; 
		if (maxValArray==3){
			heading = Math.atan2(first_eignevector[1], first_eignevector[0]);
			heading = Math.PI/2. - heading;//hmmmm....conversion...
			pitch 	= Math.atan2(Math.sqrt((Math.pow(first_eignevector[1],2) +Math.pow(first_eignevector[0],2))), first_eignevector[2])-Math.PI/2;
			roll 	= Math.atan2(Math.sqrt((Math.pow(second_eigenvector[1],2) +Math.pow(second_eigenvector[0],2))), second_eigenvector[2])-Math.PI/2;
		}
		else if (maxValArray==2){
			heading = Math.atan2(first_eignevector[1], first_eignevector[0]);
			//heading = Math.atan2(maxValArray[idx[1]], maxValArray[idx[0]]);
		}

		double[] angles={heading, pitch, roll};
		return angles; 
	}
	
	/**
	 * Get the magnitude of the error in a particular direction. 
	 * @param unitVec - a unit vector (Note: must be a UNIT vector)
	 * @return the magnitude of the error in the direction of the unit vector. 
	 */
	public double getErrorMagnitude(double[] unitVec){
		
			//work out if 2D or 3D ellipse
			boolean threeD=true; 
			if (ellipseDim[2]==-1) threeD=false; 
			
			Rotation rotation;
			Vector3D newPoint; 
			if (threeD){ 
				rotation = new Rotation(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR, -getAngles()[2],
					-getAngles()[1], -getAngles()[0]);
				newPoint=new Vector3D(unitVec[0], unitVec[1], unitVec[2]);
			}
			else {
				//if 2D need to only rotate by heading and ignore and Z component. 
				rotation = new Rotation(RotationOrder.XYZ, RotationConvention.VECTOR_OPERATOR, 0,
					0, -getAngles()[0]);
				newPoint=new Vector3D(unitVec[0], unitVec[1],0);
			}

			newPoint=rotation.applyTo(newPoint);

			//figure out magnitude of vector
			double t=Math.sqrt(1/((Math.pow(newPoint.getX(),2)/Math.pow(ellipseDim[0],2) + Math.pow(newPoint.getY(),2)/Math.pow(ellipseDim[1],2)+ Math.pow(newPoint.getZ(),2)/Math.pow(ellipseDim[2],2))));

			return t; 
	}
	
	/**
	 * Get the intersection point on the surface of the ellipsoid for a vector 
	 * @param intersectionVector - the intersection vector. 
	 * @return the point of intersection. 
	 */
	public double[][] getIntersection(double[] intersectionVector){
		
		//needs to be a unit vector. 
		double magnitude=Math.sqrt(Math.pow(intersectionVector[0], 2)+Math.pow(intersectionVector[1], 2)+Math.pow(intersectionVector[2], 2));
		
		//unit vector. 
		double[] unitVec= {intersectionVector[0]/magnitude, intersectionVector[1]/magnitude, intersectionVector[2]/magnitude};
		
		//calculate the magnitude of the vector. 
		double t= getErrorMagnitude(unitVec);
		 
		//the magnitude multiplied by the vector gives the intersection points. 
		double[][] intersectionPoint = {{unitVec[0]*t, unitVec[1]*t, unitVec[2]*t},
										{-unitVec[0]*t, -unitVec[1]*t, -unitVec[2]*t}};
		
		return intersectionPoint;
		
	}
	
	/**
	 * Get the ellipse projected onto a 3D plane. 
	 * @return an array. array [0] is the first radii. array[1] is the second radii. array[2] is the rotation relative to the plane in RADIANS. 
	 */
	public double[] getErrorEllipse2D(int planeType){
	
		double[] data = new double[3]; 
		switch (planeType){
		case PLANE_XY: case PLANE_XY_PROJ:
			if (ellipseDim[2]==-1){
				//this is 2D ellipse so just add angles
				//System.out.println("Herrow: "+ellipseDim[0]+" "+ ellipseDim[1]);
				data[0]=ellipseDim[0];
				data[1]=ellipseDim[1];
				data[2]=angles[0];
			}
			else {
				if(errorEllipseXY==null){
					//here we calculate and save the projection- once saved the projection is never
					//recalculated. 
					errorEllipseXY = calc2DEllipse(planeType); 
					if (errorEllipseXY == null) return null;
				}
				
				data[0]=errorEllipseXY.getEllipseDim()[0];
				data[1]=errorEllipseXY.getEllipseDim()[1];
				data[2]=errorEllipseXY.getAngles()[0];
				//System.out.println("ErrorEllipse: data: "+data[0]+ " "+data[1]+" "+data[2] +  " Ellipse largest vector: "+getEllipseDim()[0]);
			}
		
			break;
		case PLANE_ZY:
			//TODO
			break;
		case PLANE_ZX:
			//TODO
			break; 
			default:
			break;
		
		}
		
		return data;
		
	}
	
	private int nAngles=100; 
	
	
	/**
	 * Calculate a 2D projection of the 3D ellipse. 
	 * @param planeType
	 * @return
	 */
	private ErrorEllipse calc2DEllipse(int planeType){
		
		switch (planeType){
		case PLANE_XY_PROJ:		case PLANE_ZY_PROJ:		case PLANE_ZX_PROJ:
			return calc2DEllipseProj(planeType); 
			
		case PLANE_XY:		case PLANE_ZY:		case PLANE_ZX:
			return calc2DEllipseSlice(planeType); 
		default:
			return calc2DEllipseProj(planeType); 
		}
		
	}
	
	/**
	 * Projects the ellipse onto a plane. The entire ellipse is projected onto a plane. This differs form calc2DEllipseSlice, as the entire
	 * ellipse is projected onto the plane rather than just a 'slice' of a single section. 
	 * @param planeType - the type of plane. PLANE_XY_PROJ, PLANE_ZY_PROJ, PLANE_ZX_PROJ
	 * @return the 2D projection of the ellipse onto a plane. 
	 */
	private ErrorEllipse calc2DEllipseProj(int planeType){

		int dim1=0;
		int dim2=1; 
		
		double[] angles=new double[3]; 
		double[] dim=new double[3]; 

		
		switch (planeType){
		case PLANE_XY_PROJ:
			dim1=0;
			dim2=1;
			
			//22 Aug 2023 - dim[1] was using sin instead of cos - for projecting onto a 2d plane cos 
			//is the correct trig function to use. 
			dim[0]=this.ellipseDim[0]*Math.cos(this.angles[1]); //the major axis on 2D
			dim[1]=this.ellipseDim[1]*Math.cos(this.angles[2]); //the minor Axis. 
			angles[0]=this.angles[0]; 
			
			break;
		case PLANE_ZY_PROJ:
			dim1=1;
			dim2=2;
			
			//TODO
			break;
		case PLANE_ZX_PROJ:
			
			dim1=0;
			dim2=2;
			
			//TODO
			
			break; 
		}
		
		ErrorEllipse errorEllipse2D=new ErrorEllipse(dim, angles); 

		return errorEllipse2D; 
	}
	
	
	/**
	 * Generate a projection of the ellipse into 2D. The projection is a defined plane slicing through the center of the ellipse
	 * @param planeType - the type of plane. PLANE_XY, PLANE_ZY, PLANE_ZX
	 * @return the 2D projection of the ellipse onto a plane. 
	 */
	private ErrorEllipse calc2DEllipseSlice(int planeType){

		int dim1=0;
		int dim2=1; 
		switch (planeType){
		case PLANE_XY:
			dim1=0;
			dim2=1;
			break;
		case PLANE_ZY:
			dim1=1;
			dim2=2;
			break;
		case PLANE_ZX:
			dim1=2;
			dim2=1;
			break; 
		}

		//list of unit vectors
		double[][] unitVectors=new double[nAngles][3];

		//range between 0 and 2pi
		double angle; 
		for (int i=0; i<nAngles; i++){
			angle=(Math.PI/nAngles)*i; 
			double[] unitVec=new double[3]; 
			unitVec[dim1]=Math.sin(angle); 
			unitVec[dim2]=Math.cos(angle); 
			unitVectors[i]=unitVec;
		}

		Double max=-Double.MAX_VALUE; 
		double magnitude; 
		int ind=-1; 
		for (int i=0; i<unitVectors.length; i++){
			magnitude=getErrorMagnitude(unitVectors[i]); 
			if (magnitude>max){
				max=magnitude; 
				ind=i;
			}
		}
		
		if (ind==-1){
			return null; 
		}
		
		//now have a 2D vector. 
		double ellipseAngle=Math.atan2(unitVectors[ind][dim1], unitVectors[ind][dim2]);
		ellipseAngle = Math.PI/2. - ellipseAngle; //PAMGuard convention for heading angle. 
		double[] angles=new double[3];
		angles[dim1]= ellipseAngle; 
		
		double[] dim=new double[3];
		dim[dim1]=max;
		double[] orthogUnitVec=new double[3];
		orthogUnitVec[dim1]=-unitVectors[ind][dim2];
		orthogUnitVec[dim2]=unitVectors[ind][dim1];
		dim[dim2]=getErrorMagnitude(orthogUnitVec); 

		ErrorEllipse errorEllipse2D=new ErrorEllipse(dim, angles); 

		return errorEllipse2D; 
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public double[] getEllipseDim() {
		return ellipseDim;
	}


	/**
	 * The euler angles.,
	 * @return
	 */
	public double[] getAngles() {
		return angles;
	}
	
	
	public void printMatrix(RealMatrix m){
	    try{
	        int rows = m.getRowDimension();
	        int columns = m.getColumnDimension();
	        String str = "|\t";

	        for(int i=0;i<rows;i++){
	            for(int j=0;j<columns;j++){
	                str += m.getEntry(i, j) + "\t";
	            }

	            System.out.println(str + "|");
	            str = "|\t";
	        }

	    }
	    catch(Exception e){
	    	System.out.println("Matrix is empty!!");
	    }
	}

	/**
	 * Convenient check to see if a 2D or 3D ellipse. 
	 * @return true if a 3D error ellipse. False if a 2D ellipse.  
	 */
	public boolean is3D() {
		for (int i=0; i<this.ellipseDim.length; i++){
			if (ellipseDim[i]<0) return false; 
		}
		return true;
	}
	
	/**
	 * Get the chi^2 value. This is used to determine how the error ellipse samples the distribution. 
	 * e.g. a value of 2.4477 means the ellipse represents the 95% confidence interval whilst a value of 1
	 * will mean the ellipse represents the standard deviation. 
	 * @return chisquare_val - the chi^2 value current used. 
	 */
	public double getChisquare_val() {
		return chisquare_val;
	}

	/**
	 * Set the chi^2 value. This is used to determine how the error ellipse samples the distribution. 
	 * e.g. a value of 2.4477 means the ellipse represents the 95% confidence interval whilst a value of 1
	 * will mean the ellipse represents the standard deviation. 
	 * @param chisquare_val - the chi^2 value to set. 
	 */
	public void setChisquare_val(double chisquare_val) {
		this.chisquare_val = chisquare_val;
	}


}