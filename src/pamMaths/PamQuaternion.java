package pamMaths;

import java.io.Serializable;

import Jama.Matrix;

/**
 * Basically a quaternion is a vector which can encode three dimensional
 * orientation information (e.g. heading, pitch and roll). Means we can get rid
 * of Euler angles if we want to. This particular qauternion class assumes that
 * the heading rotates around the z-axis, pitch rotates around the x-axis and
 * roll rotates around the y-axis.
 * <p>
 * Wikipedia Description 15/11/2013
 * <p>
 * Unit quaternions, also known as versors, provide a convenient mathematical
 * notation for representing orientations and rotations of objects in three
 * dimensions. Compared to Euler angles they are simpler to compose and avoid
 * the problem of gimbal lock. Compared to rotation matrices they are more
 * numerically stable and may be more efficient. Quaternions have found their
 * way into applications in computer graphics, computer vision, robotics,
 * navigation, molecular dynamics, flight dynamics,[1] and orbital mechanics of
 * satellites
 * <p>
 * Code is heavily based on code from Prasanna Velagapudi. Copyright (c) 2008,
 * Prasanna Velagapudi <pkv@cs.cmu.edu>. Permission to use, modify and
 * distribute.
 * https://github.com/psigen/robotutils/blob/master/src/main/java/robotutils/Quaternion.java
 * <p>
 * Note this code is also available in the apache common 3+ library. PAMGUARD
 * currently implements the Apache 2.2 library so have made unique class.
 * 
 * @author Prasanna Velagapudi. Modified by Jamie Macaulay to use Jama and
 *         support the PAMGUARD vector and orientation conventions.
 * 
 */

public class PamQuaternion implements Cloneable, Serializable {

	/**
	 * Determines if a de-serialized object is compatible with this class.
	 *
	 * Maintainers must change this value if and only if the new version of this
	 * class is not compatible with old versions. See Sun docs for
	 * <a href=http://java.sun.com/products/jdk/1.1/docs/guide
	 * /serialization/spec/version.doc.html> details. </a>
	 */
	public static final long serialVersionUID = 1L;

	/**
	 * This defines the north pole singularity cutoff when converting from
	 * quaternions to Euler angles.
	 */
	public static final double SINGULARITY_NORTH_POLE = 0.49999;

	/**
	 * This defines the south pole singularity cutoff when converting from
	 * quaternions to Euler angles.
	 */
	public static final double SINGULARITY_SOUTH_POLE = -0.49999;

	/**
	 * 4D quaternion coordinates.
	 */
	private final double w, x, y, z;

	/**
	 * Construct a new quaternion.
	 * 
	 * @param w
	 *            the w-coordinate of the object
	 * @param x
	 *            the x-coordinate of the object
	 * @param y
	 *            the y-coordinate of the object
	 * @param z
	 *            the z-coordinate of the object
	 */
	public PamQuaternion(double w, double x, double y, double z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Wrap a quaternion in vector form.
	 * 
	 * @param q a quaternion vector in {w, x, y, z} form.
	 */
	public PamQuaternion(double[] q) {
		if (q.length != 4) {
			throw new IllegalArgumentException("Quaternion vector must be 4D.");
		}

		this.w = q[0];
		this.x = q[1];
		this.y = q[2];
		this.z = q[3];
	}

	/**
	 * Create a quartenion from euler angles.
	 * @param heading -0-360 degrees. Input in RADIANS
	 * @param pitch - 90->90 degrees. Input in RADIANS
	 * @param roll -0->180 and 0->-180. Input in RADIANS

	 */
	public PamQuaternion(double heading, double pitch, double roll) {

		double[] q = fromEulerAngles(heading, pitch, roll);

		this.w = q[0];
		this.x = q[1];
		this.y = q[2];
		this.z = q[3];

	}

	/**
	 * Access the w-component (scalar) of the quaternion.
	 * 
	 * @return the w-component of the quaternion.
	 */
	public double getW() {
		return w;
	}

	/**
	 * Access the x-component ("i") of the quaternion.
	 * 
	 * @return the x-component of the quaternion.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Access the y-component ("j") of the quaternion.
	 * 
	 * @return the y-component of the quaternion.
	 */
	public double getY() {
		return y;
	}

	/**
	 * Access the z-component ("k") of the quaternion.
	 * 
	 * @return the z-component of the quaternion.
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Access the components of the quaternion.
	 * 
	 * @return the components of the quaternion in {w, x, y, z} form.
	 */
	public double[] getArray() {
		return new double[] { w, x, y, z };
	}

	public static PamQuaternion fromRotation(Matrix m) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Converts quaternion to (3x3) rotation matrix.
	 *
	 * @return a 2D 3x3 rotation matrix representing the quaternion.
	 */
	public Matrix toRotation() {
		double[][] m = new double[3][3];

		// Compute necessary components
		double xx = x * x;
		double xy = x * y;
		double xz = x * z;
		double xw = x * w;
		double yy = y * y;
		double yz = y * z;
		double yw = y * w;
		double zz = z * z;
		double zw = z * w;

		// Compute rotation tranformation
		// Compute rotation tranformation
		m[0][0] = 1 - 2 * (yy + zz);
		m[0][1] = 2 * (xy - zw);
		m[0][2] = 2 * (xz + yw);
		m[1][0] = 2 * (xy + zw);
		m[1][1] = 1 - 2 * (xx + zz);
		m[1][2] = 2 * (yz - xw);
		m[2][0] = 2 * (xz - yw);
		m[2][1] = 2 * (yz + xw);
		m[2][2] = 1 - 2 * (xx + yy);

		// Put into Jama format
		return new Matrix(m, 3, 3);
	}

	public static PamQuaternion fromTransform(Matrix t) {
		double[] q = new double[4];
		double[][] m = t.getArray();

		// Recover the magnitudes
		q[0] = Math.sqrt(Math.max(0, 1 + m[0][0] + m[1][1] + m[2][2])) / 2;
		q[1] = Math.sqrt(Math.max(0, 1 + m[0][0] - m[1][1] - m[2][2])) / 2;
		q[2] = Math.sqrt(Math.max(0, 1 - m[0][0] + m[1][1] - m[2][2])) / 2;
		q[3] = Math.sqrt(Math.max(0, 1 - m[0][0] - m[1][1] + m[2][2])) / 2;

		// Recover sign information
		q[1] *= Math.signum(m[2][1] - m[1][2]);
		q[2] *= Math.signum(m[0][2] - m[2][0]);
		q[3] *= Math.signum(m[1][0] - m[0][1]);

		return new PamQuaternion(q);
	}

	public Matrix toTransform() {
		double[][] m = new double[4][4];

		// Compute necessary components
		double xx = x * x;
		double xy = x * y;
		double xz = x * z;
		double xw = x * w;
		double yy = y * y;
		double yz = y * z;
		double yw = y * w;
		double zz = z * z;
		double zw = z * w;

		// Compute rotation tranformation
		m[0][0] = 1 - 2 * (yy + zz);
		m[0][1] = 2 * (xy - zw);
		m[0][2] = 2 * (xz + yw);
		m[1][0] = 2 * (xy + zw);
		m[1][1] = 1 - 2 * (xx + zz);
		m[1][2] = 2 * (yz - xw);
		m[2][0] = 2 * (xz - yw);
		m[2][1] = 2 * (yz + xw);
		m[2][2] = 1 - 2 * (xx + yy);
		m[0][3] = m[1][3] = m[2][3] = m[3][0] = m[3][1] = m[3][2] = 0;
		m[3][3] = 1;

		// Put into Jama format
		return new Matrix(m, 4, 4);
	}

	/**
	 * Create a quaternion from euler angles.
	 * 
	 * @param heading 0-360 degrees. Input in RADIANS
	 * @param pitch 90->90 degrees. Input in RADIANS
	 * @param roll 0->180 and 0->-180.
	 * @return
	 */
	public static double[] fromEulerAngles(double heading, double pitch, double roll) {
		double q[] = new double[4];

		// convert heading to correct format.
		heading = 2 * Math.PI - heading;

		// Apply Euler angle transformations
		double c1 = Math.cos(pitch / 2.0);
		double s1 = Math.sin(pitch / 2.0);
		double c2 = Math.cos(roll / 2.0);
		double s2 = Math.sin(roll / 2.0);
		double c3 = Math.cos(heading / 2.0);
		double s3 = Math.sin(heading / 2.0);

		// Compute quaternion from components
		q[0] = c1 * c2 * c3 + s1 * s2 * s3;
		q[1] = s1 * c2 * c3 - c1 * s2 * s3;
		q[2] = c1 * s2 * c3 + s1 * c2 * s3;
		q[3] = c1 * c2 * s3 - s1 * s2 * c3;
		return q;
	}

	public static double[] fromEulerAnglesb(double heading, double pitch, double roll) {
		double q[] = new double[4];

		// convert heading to correct format.
		heading = 2 * Math.PI - heading;

		// Apply Euler angle transformations
		double c3 = Math.cos(pitch / 2.0);
		double s3 = Math.sin(pitch / 2.0);
		double c2 = Math.cos(roll / 2.0);
		double s2 = Math.sin(roll / 2.0);
		double c1 = Math.cos(heading / 2.0);
		double s1 = Math.sin(heading / 2.0);

		// Compute quaternion from components
		q[0] = c3 * c2 * c1 + s3 * s2 * s1;
		q[1] = s3 * c2 * c1 - c3 * s2 * s1;
		q[2] = c3 * s2 * c1 + s3 * c2 * s1;
		q[3] = c3 * c2 * s1 - s3 * s2 * c1;
		return q;
	}

	/**
	 * Returns the roll component of the quaternion if it is represented as
	 * standard roll-pitch-yaw Euler angles.
	 * 
	 * @return the roll (x-axis rotation) of the robot.
	 */
	public double toPitch() {
		// // This is a test for singularities
		double test = w * y - z * x;

		// Special case for north pole
		if (test > SINGULARITY_NORTH_POLE)
			return 0;

		// Special case for south pole
		if (test < SINGULARITY_SOUTH_POLE)
			return 0;

		return Math.atan2(2 * x * w + 2 * y * z, 1 - 2 * x * x - 2 * y * y);
	}

	/**
	 * Returns the pitch component of the quaternion if it is represented as
	 * standard roll-pitch-yaw Euler angles.
	 * 
	 * @return the pitch (y-axis rotation) of the robot.
	 */
	public double toRoll() {
		// // This is a test for singularities
		double test = w * y - z * x;

		// Special case for north pole
		if (test > SINGULARITY_NORTH_POLE)
			return Math.PI / 2;

		// Special case for south pole
		if (test < SINGULARITY_SOUTH_POLE)
			return -Math.PI / 2;

		return Math.asin(2 * test);
	}

	/**
	 * Returns the yaw component of the quaternion if it is represented as
	 * standard roll-pitch-yaw Euler angles.
	 * 
	 * @return the yaw (z-axis rotation) in RADIANS
	 */
	public double toHeading() {
		// // This is a test for singularities
		double test = w * y - z * x;
		double heading;
		// Special case for north pole
		if (test > SINGULARITY_NORTH_POLE)
			heading = 2 * Math.atan2(x, w);

		// Special case for south pole
		if (test < SINGULARITY_SOUTH_POLE)
			heading = -2 * Math.atan2(x, w);

		heading = Math.atan2(2 * w * z + 2 * x * y, 1 - 2 * y * y - 2 * z * z);

		if (heading < 0)
			heading = -heading;
		else
			heading = 2 * Math.PI - heading;

		return heading;
	}

	/**
	 * Returns the components of the quaternion if it is represented as standard
	 * roll-pitch-yaw Euler angles.
	 * 
	 * @return an array of the form {roll, pitch, yaw}.
	 */
	public double[] toEulerAngles() {

		return new double[] { toHeading(), toPitch(), toRoll() };
	}

	@Override
	public PamQuaternion clone() {
		return new PamQuaternion(w, x, y, z);
	}

	@Override
	public String toString() {
		return "Q[" + w + "," + x + "," + y + "," + z + "]";
	}
}