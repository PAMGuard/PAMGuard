package pamMaths;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import Jama.Matrix;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.PamCoordinate;
import PamUtils.PamUtils;

/**
 * @author Doug Gillespie
 * 
 * Vector calculations for spatial orientation. 
 * <br>All vectors have three elements for x,y,z.
 * <br>Package includes basic vector calculations, mostly
 * copied from Stephenson 1961
 *
 */
public class PamVector implements Serializable, Cloneable, PamCoordinate, ManagedParameters {

	private static final long serialVersionUID = 1L;
	
	protected double[] vector;

	/**
	 * True if the vector represents a surface rather than a pointing vector i.e. the vector points alon the surface of a cone. 
	 * The axis of the cone is by default [0,1,0]
	 */
	private boolean isCone = false;
	
	public static final PamVector xAxis = new PamVector(1,0,0);
	public static final PamVector yAxis = new PamVector(0,1,0);
	public static final PamVector zAxis = new PamVector(0,0,1);
	public static final PamVector[] cartesianAxes = {xAxis, yAxis, zAxis};
	
	public PamVector(double[] vector) {
		if (vector != null) {
			this.vector = vector;
		}
		else {
			this.vector = new double[3];
		}
	}
	
	public PamVector(double x, double y, double z) {
		vector = new double[3];
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
	}

	public PamVector(PamVector vector) {
		this.vector = Arrays.copyOf(vector.vector, 3);
	}

	public PamVector() {
		vector = new double[3];
	}

	@Override
	public PamVector clone() {
		try {
			PamVector v = (PamVector) super.clone();
			v.vector = Arrays.copyOf(this.vector, 3);
			return v;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @return The magnitude squared of the vector
	 */
	public double normSquared() {
		double a = 0;
		for (int dim = 0; dim < 3; dim++) {
			a += Math.pow(vector[dim],2);
		}
		return a;
	}
	
	/**
	 * Get the squared magnitude in a limited number of dimensions. 
	 * @param nDim number of dimensions
	 * @return The magnitude squared of the vector
	 */
	public double normSquared(int nDim) {
		double a = 0;
		for (int dim = 0; dim < nDim; dim++) {
			a += Math.pow(vector[dim],2);
		}
		return a;
	}
	
	/**
	 * Create a unit vector based on a heading (degrees from North)
	 * and a slant angle 90 up up, -90 = down
	 * @param heading heading from North in degrees
	 * @param slantAngle slant angle from the horizontal. 
	 * @return Unit vector. 
	 */
	public static PamVector fromHeadAndSlant(double heading, double slantAngle) {
		double z = Math.sin(Math.toRadians(slantAngle));
		double r = Math.cos(Math.toRadians(slantAngle));
		double x = r*Math.sin(Math.toRadians(heading));
		double y = r*Math.cos(Math.toRadians(heading));
		return new PamVector(x, y, z);
	}
	
	/**
	 * Create a unit vector based on a heading (radians)
	 * and a slant angle Pi/2 up to -Pi/2 = down
	 * @param heading heading from North in radians
	 * @param slantAngle slant angle from the horizontal. 
	 * @return Unit vector. 
	 */
	public static PamVector fromHeadAndSlantR(double heading, double slantAngle) {
		double z = Math.sin(slantAngle);
		double r = Math.cos(slantAngle);
		double x = r*Math.sin(heading);
		double y = r*Math.cos(heading);
		return new PamVector(x, y, z);
	}
	
	/**
	 * Opposite of the fromHeadAndSlantR() function. returning an angle
	 * pair for the vector. First angle is heading, second is slant angle
	 * @return angles in radians.
	 */
	public double[] toHeadAndSlantR() {
		PamVector v = this.getUnitVector();
		double[] angles = new double[2];
		angles[1] = Math.asin(v.getElement(2));
		double ca2 = Math.cos(angles[1]);
		if (ca2 != 0) {
			angles[0] = Math.asin(v.getElement(0)/ca2);
			/*
			 * Otherwise leave angles[0] at 0 since it has no meaning when the 
			 * slant angle is either straight up or down. 
			 */
		}
		angles[0] =  PamUtils.constrainedAngleR(angles[0], Math.PI);
		return angles;
	}
	
	
	
	/**
	 * 
	 * @param vec other vector to compare. 
	 * @return true if the vectors are equal in all dimensions
	 */
	public boolean equals(PamVector vec) {
		for (int dim = 0; dim < 3; dim++) {
			if (vector[dim] != vec.vector[dim]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Subtract another vector from this vector and return the result in a
	 * new Vector
	 * @param vec vector to subtract
	 * @return new vector
	 */
	public PamVector sub(PamVector vec) {
		PamVector newVec = this.clone();
		int nDim = this.getNumCoordinates();
		for (int dim = 0; dim < nDim; dim++) {
			newVec.vector[dim]-=vec.vector[dim];
		}
		return newVec;
	}
	
	/**
	 * Add another vector to this vector and return the result in a
	 * new Vector
	 * @param vec vector to add
	 * @return new vector
	 */
	public PamVector add(PamVector vec) {
		PamVector newVec = this.clone();
		int nDim = this.getNumCoordinates();
		for (int dim = 0; dim < nDim; dim++) {
			newVec.vector[dim]+=vec.vector[dim];
		}
		return newVec;
	}
	
	/**
	 * Add any number of vectors together. 
	 * @param pamVectors list of vectors
	 * @return sum of all vectors. 
	 */
	public static PamVector add(PamVector... pamVectors) {
		PamVector vec = new PamVector();
		for (int i = 0; i < pamVectors.length; i++) {
			for (int d = 0; d < 3; d++) {
				vec.vector[d] += pamVectors[i].vector[d];
			}
		}
		return vec;
	}
	
	/**
	 * Add any number of vectors together in quadrature. 
	 * @param pamVectors list of vectors
	 * @return sum of all vectors. 
	 */
	public static PamVector addQuadrature(PamVector... pamVectors) {
		PamVector vec = new PamVector();
		for (int i = 0; i < pamVectors.length; i++) {
			for (int d = 0; d < 3; d++) {
				vec.vector[d] += Math.pow(pamVectors[i].vector[d], 2);
			}
		}
		for (int d = 0; d < 3; d++) {
			vec.vector[d] = Math.sqrt(vec.vector[d]);
		}
		return vec;
	}
	
	/**
	 * convert a series of vectors into a Matrix
	 * @param pamVectors array of vectors
	 * @return Jama Matrix
	 */
	public static Matrix arrayToMatrix(PamVector[] pamVectors) {
		int nRow = pamVectors.length;
		int nCol = 3;
		double[][] data = new double[nRow][];
		for (int i = 0; i < nRow; i++) {
			if (pamVectors[i] != null) {
				data[i] = pamVectors[i].vector;
			}
		}
		return new Matrix(data);
	}
	
	/**
	 * Rotate the vector by a matrix. 
	 * @param rotationMatrix rotation matrix
	 * @return rotated vector. 
	 */
	public PamVector rotate(Matrix rotationMatrix) {
		// create a single row matrix
		Matrix thisMat = new Matrix(vector, 1);
		Matrix rotated = thisMat.times(rotationMatrix);
		return new PamVector(rotated.getRowPackedCopy());
	}
	
	/**
	 * Rotate a vector anti-clockwise by an angle. 
	 * <br> the third dimension of the matrix remains untouched. 
	 * The first two dimensions will rotate anti clockwise by the 
	 * given angle. 
	 * @param rotationAngle rotation angle in radians. 
	 * @return new rotated vector. 
	 */
	public PamVector rotate(double rotationAngle) {
		double[] vd = getVector();
		double[] newV = new double[3];
		double sin = Math.sin(rotationAngle);
		double cos = Math.cos(rotationAngle);
		newV[0] = vd[0] * cos - vd[1] * sin;
		newV[1] = vd[0] * sin + vd[1] * cos;
		newV[2] = vd[2];
		return new PamVector(newV);
	}
	
	/**
	 * Rotate a vector using another vector. 
	 * This is basically a surface rotation in the x,y plane
	 * using the same functionality as the rotate(double rotationAngle)
	 * function, but the input are already in vector form
	 * @param rotationVector
	 * @return
	 */
	public PamVector rotate(PamVector rotationVector) {
		double[] vd = getVector();
		double[] newV = new double[3];
		double sin = rotationVector.getElement(1);
		double cos = rotationVector.getElement(0);
		newV[0] = vd[0] * cos - vd[1] * sin;
		newV[1] = vd[0] * sin + vd[1] * cos;
		newV[2] = vd[2];
		return new PamVector(newV);
	}

	/**
	 * Multiply a vector by a scalar
	 * @param scalar scalar value
	 * @return new vector
	 */
	public PamVector times(double scalar) {
		PamVector newVec = new PamVector(this);
		for (int dim = 0; dim < 3; dim++) {
			newVec.vector[dim] *= scalar;
		}
		return newVec;
	}
	
	/**
	 * Get the unit vector, or if the vector is zero, return 
	 * the zero vector (0,0,0). 
	 * @return the unit vector
	 */
	public PamVector getUnitVector() {
		double sz = norm();
		if (sz == 0) {
			return clone();
		}
		return times(1./sz);
	}
	
	/**
	 * Calculate the angle between two vectors
	 * @param vec other vector
	 * @return angle in radians between 0 and pi
	 */
	public double angle(PamVector vec) {
		PamVector v1, v2;
		v1 = getUnitVector();
		v2 = vec.getUnitVector();
		return Math.acos(v1.dotProd(v2));
	}
	
	/**
	 * Calculates the smallest angle between two vectors (0 <= angle < pi/2);
	 * @param vec other vector
	 * @return angle between two vectors (0 < angle < pi/2) 
	 */
	public double absAngle(PamVector vec) {
		double ang = angle(vec);
		if (ang > Math.PI/2) {
			ang = Math.PI-ang;
		}
		return ang;
	}
	
	/**
	 * 
	 * @param vec other vector
	 * @return square of the distance between two vectors
	 */
	public double distSquared(PamVector vec) {
		double d = 0;
		for (int dim = 0; dim < 3; dim++) {
			d += Math.pow(vec.vector[dim]-this.vector[dim], 2);
		}
		return d;
	}
	
	/**
	 * 
	 * @param vec other vector
	 * @return the distance between two vectors
	 */
	public double dist(PamVector vec) {
		return Math.sqrt(distSquared(vec));
	}
	
	/**
	 * 
	 * @return the magnitude of the vector
	 */
	public double norm() {
		return Math.sqrt(normSquared());
	}
	
	/**
	 * Get the magnitude of the vector in a limited
	 * number of dimensions
	 * @param nDim number of dimensions
	 * @return magnitude of those dimensions only. 
	 */
	public double norm(int nDim) {
		return Math.sqrt(normSquared(nDim));
	}
	
	/**
	 * 
	 * @param vec a PamVector
	 * @return the dot product of this and vec
	 */
	public double dotProd(PamVector vec) {
		double a = 0;
		for (int dim = 0; dim < 3; dim++) {
			a += vector[dim]*vec.vector[dim];
		}
		return a;
	}
	
	/**
	 * A bit like a dot product, but the three components are added
	 * in quadrature. This is used to estimate errors along a particular
	 * vector component.
	 * @param vec a Pam Vector
	 * @return sum of components in each direction, added in quadrature. 
	 */
	public double sumComponentsSquared(PamVector vec) {
		double a = 0;
		for (int dim = 0; dim < 3; dim++) {
			a += Math.pow(vector[dim]*vec.vector[dim],2);
		}
		return Math.sqrt(a);
	}
	
	/**
	 * Vector or cross product of two vectors. 
	 * @param vec
	 * @return vector product of this and vec.
	 */
	public PamVector vecProd(PamVector vec) {
		PamVector newVec = new PamVector();
		/**
		 * for each dimension, take a determinant which 
		 * is made from the 'other' directions. 
		 */
		newVec.vector[0] = this.vector[1]*vec.vector[2]-this.vector[2]*vec.vector[1];
		newVec.vector[1] = -this.vector[0]*vec.vector[2]+this.vector[2]*vec.vector[0];
		newVec.vector[2] = this.vector[0]*vec.vector[1]-this.vector[1]*vec.vector[0];
		return newVec;
	}
	
	/** 
	 * Calculate the triple product of this with v1 and v2
	 * @param v1 other vector
	 * @param v2 other vector
	 * @return triple produce this.(v1^v2);
	 */
	public double tripleDotProduct(PamVector v1, PamVector v2) {
		PamVector v = v1.vecProd(v2);
		return this.dotProd(v);
	}
	
	/**
	 * Tests to see whether two vectors are parallel
	 * @param vec other vector
	 * @return true if parallel to within 1/1000 radian. 
	 */
	public boolean isParallel(PamVector vec) {
		double ang = angle(vec);
		return (ang < 0.001 || Math.PI-ang < 0.001);
	}
	
	/**
	 * Test to see whether two vectors lie in a perfect line
	 * @param vec other vector
	 * @return true if they line up perfectly
	 */
	public boolean isInLine(PamVector vec) {
		// first check they aren't points. 
		double sz = Math.max(this.norm(), vec.norm());
		if (sz == 0) {
			return true;
		}
		// first check they are parallel
		if (isParallel(vec) == false) {
			return false;
		}
		/*
		 *  then construct a new vector being the difference of the two
		 *  and check that this is also parallel
		 *  I don' tkno wwhy this is done and it's failing if it ends up that 
		 *  two vectors are the same size, as happens in a long 
		 *  linearly spaced array. Adding an extra line to check 
		 *  the difference isn't close to zero. 
		 */
		PamVector v = vec.sub(this);
		if (v.norm() / sz < .000001) {
			return true;
		}
		return v.isParallel(vec);
	}

	/**
	 * 
	 * @return the magnitude of the xy components of the vector - generally 
	 * the magnitude across the sea surface if the vector is in normal coordinates. 
	 */
	public double xyDistance() {
		return Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]);
	}
	
	public double[] getVector() {
		return vector;
	}

	public void setVector(double[] vector) {
		this.vector = vector;
	}

	/**
	 * Get a single vector element
	 * @param iElement element index
	 * @return value
	 */
	public double getElement(int iElement) {
		return vector[iElement];
	}

	/**
	 * Set a single vector element
	 * @param iElement element index
	 * @param val element value
	 */
	public void setElement(int iElement, double val) {
		this.vector[iElement] = val;
	}

	public static PamVector getXAxis() {
		return xAxis;
	}

	public static PamVector getYAxis() {
		return yAxis;
	}

	public static PamVector getZAxis() {
		return zAxis;
	}

	/**
	 * 
	 * @param iAxis
	 * @return a unit vector along a Cartesian axis (x,y,z)
	 */
	public static PamVector getCartesianAxes(int iAxis) {
		return cartesianAxes[iAxis];
	}

	/**
	 * Create a set of vector pairs, which are vectors between all 
	 * the vectors in the input argument
	 * @param vectors array of vectors
	 * @return array of vectors between all the input vectors 
	 * (creates n*(n-1)/2 new vectors)
	 */
	public static PamVector[] getVectorPairs(PamVector[] vectors) {
		int nVecs = vectors.length;
		int nPairs = nVecs * (nVecs-1) / 2;
		PamVector[] vectorPairs = new PamVector[nPairs];
		int iVec = 0;
		for (int i = 0; i < nVecs; i++) {
			for (int j = i+1; j < nVecs; j++) {
				vectorPairs[iVec++] = vectors[j].sub(vectors[i]);
			}
		}
		return vectorPairs;
	}
	/**
	 * 
	 * @return the axis to which the vector is closest. 
	 */
	public int getPrincipleAxis() {
		int ax = 0;
		double minAng = absAngle(xAxis);
		double ang;
		for (int i = 1; i < 3; i++) {
			ang = absAngle(cartesianAxes[i]);
			if (ang < minAng) {
				minAng = ang;
				ax = i;
			}
		}
		return ax;
	}
	
	/**
	 * Get a vector that's perpendicular to the current one, in the xy plane if at all possible. 
	 * @return
	 */
	public PamVector getPerpendicularVector() {
		if (vector[2] == 0) 
			return this.rotate(Math.PI/2);
		if (vector[0] == 0) {
			return xAxis.clone();
		}
		if (vector[1] == 0) {
			return yAxis.clone();
		}
		if (vector[2] == 0) {
			return zAxis.clone();
		}
		// otherwise generate a perpendicular vector in the xy plane!
		PamVector v = new PamVector(getCoordinate(0), getCoordinate(1), 0).rotate(Math.PI/2);
		return v;
	}

	@Override
	public String toString() {
		return String.format("(%2g, %2g, %2g) mag = %2g", vector[0], vector[1], vector[2], norm());
	}
	
	/**
	 * Convert an array of Cartesian vectors into bearings from North. Only the x and y parts of the 
	 * vector are used in this and to be a pain, the bearing is left in radians !
	 * @param vectors Cartesian vector (need not be a unit vector) 
	 * @return clockwise bearings from north in radians  
	 */
	public static double[] vectorsToSurfaceBearings(PamVector[] vectors) {
		double[] bearings = new double[vectors.length];
		for (int i = 0; i < vectors.length; i++) {
			bearings[i] = vectorToSurfaceBearing(vectors[i]);
		}
		return bearings;
	}
	
	/**
	 * Convert a Cartesian vector into a bearing from North. Only the x and y parts of the 
	 * vector are used in this and to be a pain, the bearing is left in radians !
	 * @param vector Cartesian vector (need not be a unit vector) 
	 * @return clockwise bearing from north in radians  
	 */
	public static double vectorToSurfaceBearing(PamVector vector) {
		
	  double bearing = Math.atan2(vector.getElement(1), vector.getElement(0));
	  bearing = Math.PI/2. - bearing;
	  return bearing;
	}

	/**
	 * Normalise a vector so it has a magnitude of 1.
	 * if the vector has magnitude 0, it is left with this magnitude
	 * and 0 is returned. 
	 * @return 0 if the vector had zero magnitude, a otherwise. 
	 */
	public double normalise() {
		double mag = norm();
		if (mag == 0) {
			return 0.;
		}
		for (int i = 0; i < 3; i++) {
			vector[i] /= mag;
		}
		return 1.;
	}
	
	/**
	 * Make all elements within the vector positive. This can be used to simplify symmetric problems. 
	 * @return The vector but with all elements made positive. 
	 */
	public PamVector absElements(){
		double[] vd = getVector();
		double[] newV = new double[3];
		for (int dim = 0; dim < 3; dim++) {
			newV[dim]=Math.abs(vd[dim]);
		}
		return new PamVector(newV);
	}
	
	
	/**
	 * Get a matrix which rotates a vector around the x axis. 
	 * @param angle- angle to rotate around the x axis in RADIANS. 
	 * @return x axis rotation matrix.
	 */
	public static Matrix getRotMatrixX(double angle){
		double[][] Rx={{1, 0, 0}, {0, Math.cos(angle), -Math.sin(angle)}, {0, Math.sin(angle), Math.cos(angle)}};
		return new Matrix(Rx, 3, 3);
	}
	
	/**
	 * Get a matrix which rotates a vector around the y axis. 
	 * @param angle- angle to rotate around the y axis in RADIANS. 
	 * @return y axis rotation matrix.
	 */
	public static Matrix getRotMatrixY(double angle){
		double[][] Ry={{Math.cos(angle), 0, Math.sin(angle)}, {0, 1, 0}, {-Math.sin(angle), 0,  Math.cos(angle)}};
		return new Matrix(Ry, 3, 3);
	}
	
	/**
	 * Get a matrix which rotates a vector around the z axis. 
	 * @param angle- angle to rotate around the z axis in RADIANS. 
	 * @return z axis rotation matrix.
	 */
	public static Matrix getRotMatrixZ(double angle){
		double[][] Rz={{Math.cos(angle), Math.sin(angle), 0}, {-Math.sin(angle), Math.cos(angle), 0}, {0, 0, 1}};
		return new Matrix(Rz, 3, 3);
	}
	
	
	/**
	 * Rotates a vector by a Quaternion . 
	 * @param PamQuaternion - orientation
	 * @return vector rotated by the quaternion. 
	 */
	public static PamVector rotateVector(PamVector vector, PamQuaternion orientation){
		
		double[][] vectorM={{vector.getElement(0)},{vector.getElement(1)},{vector.getElement(2)}};
		Matrix rotVector= new Matrix(vectorM, 3, 1);
		double[][] vectorMResult=orientation.toRotation().times(rotVector).getArray();
		
		PamVector relElement=new PamVector(vectorMResult[0][0], vectorMResult[1][0], vectorMResult[2][0]);
		relElement.isCone=vector.isCone();

		return relElement;
		
	}
	
	/**
	 * Rotates a vector about an origin point
	 * @param vector - the vector to rotate
	 * @param origin - the origin
	 * @param orientation - the quaternion to rotate the vector by
	 * @return the rotated vector. 
	 */
	public static PamVector rotateVector(PamVector vector, double[] origin, PamQuaternion orientation){
		
		double[][] vectorM={{vector.getElement(0)-origin[0]},{vector.getElement(1)-origin[1]},{vector.getElement(2)-origin[2]}};
		
		
		Matrix rotVector= new Matrix(vectorM, 3, 1);
		double[][] vectorMResult=orientation.toRotation().times(rotVector).getArray();
		
		
		PamVector relElement=new PamVector(vectorMResult[0][0]+origin[0], vectorMResult[1][0]+origin[1], vectorMResult[2][0]+origin[2]);
		relElement.isCone=vector.isCone();
		
		return relElement;
		
	}

	
	/**
	 * Rotates a vector by the Euler angles. 
	 * @param heading- heading in RADIANS. Here we use 0 to 360 for heading.
	 * @param pitch- pitch in RADIANS. Use the standard PAMGUARD convention 90=-g; 0=0 -90=g (g =direction of gravitational field)
	 * @return vector rotated by the Euler angles. 
	 */
	public static PamVector rotateVector(PamVector vector, double heading, double pitch){
		double[] angles={heading, pitch};
		return rotateVector( vector, angles);
	}

	/**
	 * Rotates a vector by the Euler angles. 
	 * @param angle - the Euler angles. angle[0] is heading in RADIANS. Here we use 0 to 360 for heading angle[1] 
	 * pitch in RADIANS. Use the standard PAMGUARD convention 90=-g; 0=0 -90=g (g =direction of gravitational field)
	 * angle[2] is tilt in RADIANS. Use the standard PAMGUARD convention.
	 * @return vector rotated by the Euler angles. 
	 */
	public static PamVector rotateVector(PamVector vector, double[] angle){
		
		Matrix rMat;
		
		//first convert the vector to a matrix 
		double[][] vectorM={{vector.getElement(0)},{vector.getElement(1)},{vector.getElement(2)}};
		Matrix rotVector= new Matrix(vectorM, 3, 1);
		
		//pitch roates around x/
		//the rotation matrix for rotation around the x axis. 
		if (angle[1]!=0){
			rMat = getRotMatrixX(angle[1]);
		    rotVector = rMat.times(rotVector);
		}
		
//		//tilt rotates around y/
//		//the rotation matrix for rotation around the x axis. 
//		if (angle[2]!=0){
//			rMat = getRotMatrixY(angle[2]);
//			rotVector = rMat.times(rotVector);
//		}
		
		//heading rotates around z/
		//the rotation matrix for rotation around the x axis. 
		if (angle[0]!=0){
			rMat = getRotMatrixZ(angle[0]);
			rotVector = rMat.times(rotVector);
		}
		
		double[][] results=rotVector.getArray();
		double[] rotVectorResult=new double[3];
		
		for (int j=0; j<results.length; j++){
			for (int i=0; i<results[j].length; i++){
//			System.out.println(String.format("%.2f", results[j][i]));
			rotVectorResult[j]=results[j][i];	
			}
		}
		
		PamVector relVector=new PamVector(rotVectorResult);
		relVector.isCone=vector.isCone();

		return relVector;
		
	}

	/**
	 * Check whether the vector represents a surface, rather than a pointing vector. PamVectors may represent the surface of a cone rather than a 3D vector. 
	 * @return true if the PamVector represents a surface. 
	 */
	public boolean isCone() {
		return isCone;
	}
	
	/**
	 * Set whether the vector represents a surface, rather than a pointing vector. PamVectors may represent the surface of a cone rather than a 3D vector. 
	 * If this is the case then isCone is true.
	 * @param isCone - true if the PamVector represents a surface. 
	 */
	public void setCone(boolean isCone){
		this.isCone=isCone; 
	}

	@Override
	public double getCoordinate(int iCoordinate) {
		return vector[iCoordinate];
	}

	/* (non-Javadoc)
	 * @see PamUtils.PamCoordinate#setCoordinate(int, double)
	 */
	@Override
	public void setCoordinate(int iCoordinate, double value) {
		vector[iCoordinate] = value;
	}

	@Override
	public int getNumCoordinates() {
		return vector.length;
	}

	/**
	 * Calculate the mean position of a set of vectors. 
	 * @param vectors
	 * @return the vector mean
	 */
	public static PamVector mean(PamVector[] vectors) {
		if (vectors == null) {
			return null;
		}
		// first gte the maximum number of dimensions:
		int nDim = 0;
		for (int i = 0; i < vectors.length; i++) {
			nDim = Math.max(nDim, vectors[i].getNumCoordinates());
		}
		double[] vecData = new double[nDim];
		for (int i = 0; i < vectors.length; i++) {
			PamVector v = vectors[i];
			for (int d = 0; d < v.getNumCoordinates(); d++) {
				vecData[d] += (v.vector[d] / vectors.length);
			}
		}
		return new PamVector(vecData);		
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this, Modifier.FINAL | Modifier.STATIC);
	}
	
	/**
	 * 
	 * @return The heading of a vector, angles clockwise from N in radians. 
	 */
	public double getHeading() {
		return Math.PI/2.-Math.atan2(vector[1], vector[0]);
	}
	
	/**
	 * 
	 * @return The pitch or slant angle of a vector. 
	 */
	public double getPitch() {
		double x = Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2));
		return Math.atan2(vector[2],x);
	}
	
	/**
	 * Convert <i>orthogonal<i> vectors to heading pitch and roll. 
	 * @param vectors
	 * @return array of heading pith and roll in radians
	 */
	static public double[] getHeadingPitchRoll(PamVector[] vectors) {
		if (vectors.length < 2) {
			return null;
		}
		double[] hpr = new double[3];
		hpr[0] = vectors[0].getHeading();
		hpr[1] = vectors[0].getPitch();
		hpr[2] = vectors[1].getPitch();
		return hpr;
	}
	
	/**
	 * Get minimum heading pitch and roll information, throwing away any 
	 * data which are zeros. Primarily used for saving to file where we 
	 * normally won't need to write p and r if they are zero
	 * @param vectors orthogonal vectors describing array
	 * @return angles in radians (angle clockwise from N, elevation and roll)
	 */
	static public double[] getMinimalHeadingPitchRoll(PamVector[] vectors) {
		if (vectors == null || vectors.length < 1) {
			return null;
		}
		double[] angs = new double[3];
		angs[0] = vectors[0].getHeading();
		angs[1] = vectors[1].getPitch();
		if (vectors.length >= 2) {
			angs[2] = vectors[1].getPitch();
		}
		int nz = 1; // non zeros count. Always include heading. 
		if (Math.abs(angs[1]) > 1e-5) {
			nz = 2;
		}
		if (Math.abs(angs[2]) > 1e-5) {
			nz = 3;
		}
		if (nz < 3) {
			angs = Arrays.copyOf(angs, nz);
		}
		return angs;
	}
	
//	static public PamVector[] fromHeadingPitchRoll(double[] angles) {
//		
//	}
//	static public PamVector[] fromHeadingPitchRoll(double head, double pitch, double roll) {
//		
//	}
	
}
