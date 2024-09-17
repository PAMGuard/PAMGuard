package PamDetection;

import Array.ArrayManager;
import GPS.GpsData;
import Jama.Matrix;
import Localiser.algorithms.locErrors.LocaliserError;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;

/**
 * Class for localisation information. 
 * <p> Each AbstractLocalisation should be uniquely linked
 * to a single PamDataUnit, since this class is abstract, where the actual data
 * are stored (in the data unit or in the localisation) is up to the programmer. 
 * <p>
 * This class has been through a number of iterations during 2007 and may well 
 * go through a few more before we are all happy with it. The main problem has
 * been whether to reference positions purely by a hydrophone number, a time, a distance 
 * and bearing, or simply by a LatLong. It's ended up as a mixture of both. 
 * <p>
 * Things are made worse by ambiguities which means that there may be one or more of each type 
 * of information. The key is to correctly set the types of information held within a particular
 * localisation so that other parts of Pamguard may query it and act accordingly. It is vital that the 
 * PamDataBlock which will hold the PamDataUnits associated with a localisation have their localisationContents
 * member correctly set to the full range of POSSIBLE localisation information types for the data units they
 * will contain. This is important for the database and the various displays, which will all query the 
 * PamDataBlocks before the contain any PamDataUnits in order to see which columns to 
 * put in database tables, whether or
 * not the data within a PamDataBlock may plot on a particular display, etc. 
 * 
 * @author Doug Gillespie
 * @see PamDataUnit
 * @see PamDataBlock
 *
 */
abstract public class AbstractLocalisation {
	
	static private final double[][] linearArrayGeometry = {{0, 1, 0}, {1, 0, 0}, {0, 0, 1}}; 

	/**
	 * reference to parent PamDetection object
	 */
	private PamDataUnit pamDataUnit;
	
	/**
	 * bitmap of flags saying what's in the localisation information
	 */
	private LocalisationInfo locContents= new LocContents(0); 
	
	/**
	 * Type of array, point, line, plane, volume, etc. 
	 */
	private int arrayType = ArrayManager.ARRAY_TYPE_NONE;
	
	/**
	 * Principle axis of the array geometry. 
	 */
	private PamVector[] arrayAxis = null;
	
//	/**
//	 * Flag to say we tried to find the array axis, but failed. 
//	 */
//	private boolean arrayAxisFindFailed = false;
	
	/**
	 * All localisation must be relative to at least one hydrophone. If more than one is specified, then 
	 * it's assumed that the data are relative to the mean position of all hydrophones. 
	 */
	private int referenceHydrophones;

	/**
	 * Constructor for a localisation. 
	 * @param pamDataUnit - the pam data units the localisation is associated with
	 * @param locContents - the localisation contents bitmap. 
	 * @param referenceHydrophones - the reference hydrophones.
	 */
	public AbstractLocalisation(PamDataUnit pamDataUnit, int locContents, int referenceHydrophones) {
		super();
		this.pamDataUnit = pamDataUnit;
		this.locContents.setLocContent(locContents);
		this.referenceHydrophones = referenceHydrophones;
	}
	
	/**
	 * Constructor for a localisation. 
	 * @param pamDataUnit - the pam data units the localisation is associated with.
	 * @param locContents - the localisation contents class. 
	 * @param referenceHydrophones - the reference hydrophones.
	 */
	public AbstractLocalisation(PamDataUnit pamDataUnit, LocContents locContents, int referenceHydrophones) {
		super();
		this.pamDataUnit = pamDataUnit;
		this.locContents=locContents; 
		this.referenceHydrophones = referenceHydrophones;
	}


	/**
	 * Constructor for a localisation. 
	 * @param pamDataUnit - the pam data units the localisation is associated with
	 * @param locContents - the localisation contents bitmap. 
	 * @param referenceHydrophones - the reference hydrophones.
	 * @param arrayType - the array type. 
	 * @param arrayAxis - the array axis. 
	 */
	public AbstractLocalisation(PamDataUnit pamDataUnit, int locContents, int referenceHydrophones,
			int arrayType, PamVector[] arrayAxis) {
		super();
		this.pamDataUnit = pamDataUnit;
		this.locContents.setLocContent(locContents);
		this.referenceHydrophones = referenceHydrophones;
	}
	
	/**
	 * 
	 * @return the type of hydrophone sub array used, eg point, planar, line, volumetric.
	 * this can tell us the type of information likely to be available in terms of bearings, etc.
	 * <p> Added DG 6 January 2010 
	 */
	public int getSubArrayType() {
		return arrayType;
	}
	
	/**
	 * Set the type of array (or sub array) used for this particular localisation
	 * This may not be the entire array if detection was only on some channels. 
	 * @param arrayType
	 */
	public void setSubArrayType(int arrayType) {
		this.arrayType = arrayType;
	}
	
	
	/**
	 * 
	 * @return one, two or three orthogonal orientation vectors which tell us the nominal direction the 
	 * array is pointing in. For a simple towed array, this is likely to be (0,1,0), i.e. aligned
	 * with the positive y axis. For a planar array, two vectors should be returned and for 
	 * a volumetric array, three, although the third will just be the cross product of the first two.  
	 */
	public PamVector[] getArrayOrientationVectors() {
		return arrayAxis;
	}
	
	/**
	 * Set the array axis - one, two or three axes defining the principle 
	 * orientations of the array used for the localisation. The angles
	 * in the localisation will be relative to these axes. 
	 * @param arrayAxis array axis vectors. 
	 */
	public void setArrayAxis(PamVector[] arrayAxis) {
		this.arrayAxis = arrayAxis;
	}
		
	/**
	 * Returns angles projected onto the surface of a plane defined by the 
	 * first two (or first in the case of a linear array) array axis directions. <p>
	 * In an ideal world, this plane will be aligned with the sea surface, in which case it 
	 * will be relatively easy to convert these angles to a surface angle. However, the choice 
	 * of array primary and secondary axes may vary a lot (depending on hydrophone order) and there
	 * is no implicit guarantee that these angles will be relative to the real x and y axis of the array. 
	 * <p>For a linear array
	 * this will just be +/- the theta angle, or the getBearing(0) angle and
	 * there will be two of them, reflecting lr ambiguity. 
	 * <br>For a planar array, the
	 * angle will probably be a single angle, but will be projected onto the 
	 * plane of the array. 
	 * <br>For a volumetric array, it will be a projection of the  
	 * two angles. 
	 * <p>To use these data though, it will also be necessary to know the
	 * orientation of the principle axes of the array. For a linear array, this will 
	 * just be the line of the array. For a planar and volumetric array, these may depend
	 * on the type of localiser used. The array axes should be available in arrayAxes.
	 * <p>The planar angles will be correct relative to the two primary array axis directions. 
	 * 
	 * @return array of possible angles. 
	 */
	public double[] getPlanarAngles() {
		double[] newAngles = getAngles();
		double[] planarAngles;
		if (newAngles == null) {
			if (hasLocContent(LocContents.HAS_AMBIGUITY)) {
				planarAngles = new double[2];
				planarAngles[0] = getBearing(0);
				planarAngles[1] = getBearing(1);
			}
			else {
				planarAngles = new double[1];
				planarAngles[0] = getBearing(0);
			}
		}
		else {
			if (newAngles.length == 2) {
				planarAngles = new double[1];
				planarAngles[0] = Math.atan2(Math.sin(newAngles[0])*Math.cos(newAngles[1]), Math.cos(newAngles[0]));
			}
			else {
				planarAngles = new double[2];
				planarAngles[0] = newAngles[0];		
				planarAngles[1] = -planarAngles[0];
			}
		}
		return planarAngles;
	}
	
	/**
	 * Get a unit vector pointing towards this localisation 
	 * in the coordinate frame of the array geometry. 
	 * 
	 * @return a unit vector in the coordinate frame of the array geometry. <br>
	 * If you want the projection of this vector onto the plane, simply set the third
	 * element to zero. If you do that, do not normalise in case the direction is 
	 * perpendicular to the plane, in which case you'll get a divide by zero.
	 */
	public PamVector getPlanarVector() {
		double[] newAngles = getAngles();
//		System.out.printf("Angle in degrees is %3.1f\n", newAngles[0]*180./Math.PI);
//		newAngles = newAngles.clone();
//		newAngles[0] = Math.PI/2-newAngles[0];
		PamVector planeVec;// = new PamVector();
		if (newAngles == null) {
			double bearing = getBearing(0);
//			planeVec.setElement(0, Math.cos(bearing));
//			planeVec.setElement(1, Math.sin(bearing));
			planeVec = PamVector.fromHeadAndSlantR(Math.PI/2.-bearing, 0);
			planeVec.setCone(true);
		}
		else {
			if (newAngles.length == 1) {
//				planeVec.setElement(0, Math.cos(newAngles[0]));
//				planeVec.setElement(1, Math.sin(newAngles[0]));
				planeVec = PamVector.fromHeadAndSlantR(Math.PI/2.-newAngles[0], 0);
				planeVec.setCone(true);
			}
			else {
//				planeVec.setElement(0, Math.cos(newAngles[0]));
//				double sin0 = Math.sin(newAngles[0]);
//				planeVec.setElement(1, sin0*Math.cos(newAngles[1]));
//				// seem to need to bodge the sign of z for some reason. Annoying !
////				double z = Math.abs(sin0*Math.sin(newAngles[1]))*Math.signum(newAngles[1]);
//				double z = Math.abs(sin0)*Math.sin(newAngles[1]);
//				planeVec.setElement(2, z);
////				System.out.println("3d pointing vec : " + planeVec.toString());
				planeVec = PamVector.fromHeadAndSlantR(Math.PI/2.-newAngles[0], newAngles[1]);
//				System.out.println(planeVec.toString() + " - " + vec2.toString());
//				planeVec = vec2;
			}
		}
		return planeVec;
	}
	
	/**
	 * Get vectors pointing at this localisation in a real world coordinate frame. 
	 * <br>N.B. Real world in this instance means relative to the xyz coordinate
	 * frame of the hydrophone array. To get real real world vectors relative to the 
	 * planet, the vectors will need to be further rotated by the course of 
	 * the vessel/array and the pitch, roll of the hydrophone array
	 * @return vectors pointing at this localisation in a real world coordinate frame.
	 * For a volumetric array, there should be a single vector since the geometry gives an
	 * unambiguous result. For a planar array we will get two vectors, one either side of
	 * the plane.    
	 */
	public PamVector[] getWorldVectors() {
		PamVector singleVec = getPlanarVector();
		Matrix pointer = new Matrix(3, 1);
		for (int i = 0; i < 3; i++) {
			pointer.set(i, 0, singleVec.getElement(i));
		}
		Matrix rotatedPointer;
		Matrix coordMatrix;
		Matrix invCoordMatrix;
		//TODO-delete
//		System.out.println("Abstract Localisation: ARRAY_TYPE: "+ArrayManager.getArrayTypeString(arrayType));
		if (arrayType == ArrayManager.ARRAY_TYPE_VOLUME) {
			PamVector[] vecs = new PamVector[1];
			///TODO- flipZ?   Why do we flip Z ? 
			coordMatrix = getCoordinateMatrix(false);
			invCoordMatrix = coordMatrix.inverse();
			rotatedPointer = invCoordMatrix.times(pointer);
			vecs[0] = new PamVector(rotatedPointer.getColumnPackedCopy());
			return vecs;
		}
		else if (arrayType == ArrayManager.ARRAY_TYPE_PLANE) {
			PamVector[] vecs = new PamVector[2];
			coordMatrix = getCoordinateMatrix(false);
			invCoordMatrix = coordMatrix.inverse();
			rotatedPointer = invCoordMatrix.times(pointer);
			vecs[0] = new PamVector(rotatedPointer.getColumnPackedCopy());
			coordMatrix = getCoordinateMatrix(true);
			invCoordMatrix = coordMatrix.inverse();
			rotatedPointer = invCoordMatrix.times(pointer);
			vecs[1] = new PamVector(rotatedPointer.getColumnPackedCopy());
			return vecs;
		}
		else {
			PamVector[] vecs = new PamVector[2];
			coordMatrix = getCoordinateMatrix(false);
			invCoordMatrix = coordMatrix.inverse();
			rotatedPointer = invCoordMatrix.times(pointer);
			vecs[0] = new PamVector(rotatedPointer.getColumnPackedCopy());
			vecs[0].setCone(true); // the pamvector is a surface, not a vector
			pointer.set(1,0,-pointer.get(1,0));
			rotatedPointer = invCoordMatrix.times(pointer);
			vecs[1] = new PamVector(rotatedPointer.getColumnPackedCopy());
			vecs[1].setCone(true); // the pamvector is a surface, not a vector

//			vecs[0] = singleVec;
//			
//			vecs[1] = singleVec.clone();
//			vecs[1].setElement(1, -vecs[1].getElement(1));
			return vecs;
		}
	}
	
	/**
	 * Get vectors to the detection in real world (relative to earths surface)
	 * coordinates. 
	 * <br> This is the output of getWorldVectors rotated by the reference bearing from 
	 * the GPS data. 
	 * @return real world vectors. 
	 */
	public PamVector[] getRealWorldVectors() {
		
		PamVector[] v = getWorldVectors();
		if (v == null) {
			return null;
		}
		
		//rotate the vector by euler angles. 
		GpsData origin = getOriginLatLong();
		if (origin == null) {
			return v;
		}
		
		PamQuaternion direction=origin.getQuaternion();
		for (int i = 0; i < v.length; i++) {
			v[i]=PamVector.rotateVector(v[i], direction);
			if (getSubArrayType() == ArrayManager.ARRAY_TYPE_LINE) {
				v[i].setCone(true);
			}
		}
		
		return v;
	}
	
	/**
	 * Get the coordinate matrix which describes the principle coordinates of the array. 
	 * Ultimately, the matrix must be three orthogonal vectors. Generally only two will 
	 * have been defined thus far, and the third is easily calculated. 
	 * <br>
	 * For some old geometries, or for linear arrays, there will be no data, so in this
	 * case, set the first axis in lie with the array, the second in the horizontal, at right
	 * angles to the first and the third as the orthogonal to both the others. 
	 * @param flipZ
	 * @return
	 */
	private Matrix getCoordinateMatrix(boolean flipZ) {
		if (arrayAxis == null || arrayAxis.length < 1) {
			return new Matrix(linearArrayGeometry);
		}
		else if (arrayAxis.length == 1) {
			return linearCoordinateMatrix(flipZ);
		}
		
//		System.out.println("AbstractLocalisation: Array Axis: "+ arrayAxis[0].toString() + "    " + arrayAxis[1].toString());

		double[][] m = new double[3][];
		m[0] = arrayAxis[0].getVector();
		m[1] = arrayAxis[1].getVector();
		if (flipZ) {
			m[2] = arrayAxis[1].vecProd(arrayAxis[0]).getVector();
		}
		else {
			m[2] = arrayAxis[0].vecProd(arrayAxis[1]).getVector();
		}
		return new Matrix(m);
	}
	
	/**
	 * Special case for linear arrays. In principle, these can be at any orientation, 
	 * but will most probably be in line with one of the principle axis. If there is ANY horizontal
	 * component at all, then set the second component perpendicular to that but also 
	 * in the horizontal plane. If the array is perfectly vertical, then set the second component
	 * to be (1,0,0)
	 * @param flipZ
	 * @return
	 */
	private Matrix linearCoordinateMatrix(boolean flipZ) {
		double[][] m = new double[3][];
		m[0] = arrayAxis[0].getVector();
		PamVector m1 = null;
		if (m[0][0] == 0 && m[0][1] == 0) {
			m1 = PamVector.getXAxis();
		}
		else {
			m1 = PamVector.getZAxis().vecProd(arrayAxis[0]);
		}
		m[1] = m1.getVector();
		if (flipZ) {
			m[2] = m1.vecProd(arrayAxis[0]).getVector();
		}
		else {
			m[2] = arrayAxis[0].vecProd(m1).getVector();
		}

		return new Matrix(m);
	}

	/**
	 * 
	 * @return Angles to detection in radians. The number of angles will be 0, 1 or 2. 	
	 * <p>
	 * *****************  CHANGES FROM AUGUST 2017 ******************* <br>
	 * Now oriented the 'globe' differently, so that the first angle can be between -Pi and +Pi or 
	 * between 0 and 2Pi. The second angle is the elevation angle which is either up (+ve angles)
	 * or down (-ve angles) having a range of -Pi/2 to +Pi/2. This change will only affect 
	 * data from volumetric arrays when the animal is at significant depth. Changes are being made
	 * to data from click and whistle detectors which used the older system to maintain compatibility. 
	 * <p> For a point array, null should be returned. 
	 * <p>For a line array
	 * a single angle is returned which is the angle relative to the first
	 * orientation vector with 0 being in line with the orientation vector 
	 * (can be thought of as colatitude). 
	 * <p>For a planar or volumetric array, two angles should be returned, the first
	 * being the colongitude, the second being the latitude. Imagine a globe, with North vertical and 
	 * 0 longitude pointing along the main axis of the array geometry.  
	 */
	public double[] getAngles() {
		return null;
	}
	
	
	/**
	 * 
	 * @return Angle errors to detection in radians. The number of angles will be 0, 1 or 2. 
	 * <p> For a point array, null should be returned. 
	 * <p>For a line array
	 * a single error is returned which is the angle relative to the first
	 * orientation vector with 0 being in line with the orientation vector 
	 * (can be thought of as colatitude). 
	 * <p>For a planar or volumetric array, two errors should be returned, the first
	 * being the error on colatitude, the second being the longitude which will be between 0 and pi 
	 * for a planar array and either -pi to pi or 0 to 2pi for a volumetric array. 
	 */
	public double[] getAngleErrors() {
		return null;
	}
	
	
	/**
	 * The PAMGuard convention on the order of hydrophone pairs to calculate time delays between. In the case of a simple stereo array 
	 * this will simply be an array {{0,1}}. For a more complex array e.g. 4 channels the function would return {{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}}. 
	 * @param the number of receivers/hydrophones in the array. 
	 * @return an array with channel numbers showing which receivers/hydrophones to calculate time delays between. 
	 */
	public static int[][] getTimeDelayChIndex(int numberOfHydrophones){		
		int[][] index=new int[PamUtils.calcTDNumber(numberOfHydrophones)][2];
		int n=0;
		for (int i=0; i<numberOfHydrophones; i++){
			for (int j=0; j<numberOfHydrophones-(i+1);j++){
				index[n][1]=j+i+1;
				index[n][0]=i;	
				n++;
			}
		}
		return index;
	}
	
	/**
	 * Time delays in seconds. 
	 * <br>These are calculated between every hydrophone pair in a group. To get the pairs use IndexM1() and IndexM2() functions. For example for 
	 * four hydrophones in an array;
	 * <br>IndexM1=0 0 0 1 1 2 
	 * <br>IndexM2=1 2 3 2 3 3
	 * <br>So the first time delay will be between hydrophones 0 and 1, the second time delay between hydrophones 0 and 2 and so on.
	 * Time delay convention. The time delay is positive if it hits the indexM1 hydrophone BEFORE hitting the indexM2 hydrophone.
	 * @return array of time delays in seconds. 
	 */
	public double[] getTimeDelays(){
		return null;
	}
	
	/**
	 * Time delay errors
	 * @return
	 */
	public double[] getTimeDelayErrors(){
		return null;
	}


	/**
	 * @return a set of flags specifying which data are available within this localisation object. 
	 */
	public LocalisationInfo getLocContents() {
		return locContents;
	}

	/**
	 * 
	 * @param locContents a set of flags specifying which data are available within this localisation object.
	 */
	public void setLocContents(LocalisationInfo locContents) {
		this.locContents = locContents;
	}
	
	/**
	 * 
	 * @param flagsToAdd localisation flags to add to existing flags. 
	 */
	public void addLocContents(int flagsToAdd) {
		locContents.addLocContent(flagsToAdd);
	}
	
	/**
	 * 
	 * @param flagsToRemove bitmap of localisation flags to remove. 
	 * @return new or remaining localisation content flags. 
	 */
	public int removeLocContents(int flagsToRemove) {
		return locContents.removeLocContent(flagsToRemove)
;
	}

	/**
	 * Check that the localisation has specific content. 
	 * @param requiredContent specified content
	 * @return true if specified content exists, false otherwise. 
	 */
	public boolean hasLocContent(int requiredContent) {
		return locContents.hasLocContent(requiredContent); 
	}
	/**
	 * 
	 * @return Parent detection containing this localisation information
	 */
	public PamDataUnit getParentDetection() {
		return pamDataUnit;
	}

	/**
	 * 
	 * @param parentDetection Parent detection containing this localisation information
	 */
	public void setParentDetection(@SuppressWarnings("rawtypes") PamDataUnit parentDetection) {
		this.pamDataUnit = parentDetection;
	} 
	
	/**
	 * 
	 * @return a bitmap of hydrophone numbers that form a reference position for this localisation
	 */
	public int getReferenceHydrophones() {
		return referenceHydrophones;
	}

	/**
	 * 
	 * @param referenceHydrophones a bitmap of hydrophone numbers that form a reference position for this localisation
	 */
	public void setReferenceHydrophones(int referenceHydrophones) {
		this.referenceHydrophones = referenceHydrophones;
	}
	
	/**
	 * Get the reference bearing in radians. This is relative to North, 
	 * moving in a clockwise direction as would other bearings.
	 * <br> now that the general code for localisation using vectors has been 
	 * sorted out so that the getWorldVectors now returns vectors which are 
	 * correct in the general xyz frame of the array geometry, all that is actually
	 * needed here now is the array heading at the time of the event, while in previous
	 * versions, this required the actual bearing between two hydrophones (which was 
	 * the same as the array heading for linear arrays which is why it all worked).
	 * @return Reference bearing in radians. 
	 */
	public double getBearingReference() {
		return Math.toRadians(pamDataUnit.getHydrophoneHeading(false));
//		return pamDataUnit.getPairAngle(0, false) * Math.PI / 180.;
	}
	
	/**
	 * Get the range for a specific side (where ambiguity exists)
	 * @param iSide
	 * @return range
	 */
	public double getRange(int iSide) {
		return Double.NaN;
	}

	
	/**
	 * Get height of the detection in meters. Depth is -height. 
	 * @return The height of the detection in meters.  
	 */
	public double getHeight(int iSide) {
		return Double.NaN;
	}

	
	/**
	 * 
	 * @param iSide
	 * @return The error on the bearing estimation in radians for the given side
	 */
	public double getBearingError(int iSide) {
		return Double.NaN;
	}
		
	/**
	 * 
	 * @return the latlong of the centre of the hydrophones associated with 
	 * the channels used in this detection. If no channels are set, then it returns
	 * the GPS location for the time of the detection.
	 */
	public GpsData getOriginLatLong() {
		return pamDataUnit.getOriginLatLong(false);
	}
	
	/**
	 * Return the latlong for a location. There may be more than one of them
	 * if there is side to side ambiguity.
	 * @param iSide 0, 1, 2, etc. 
	 * @return LatLong information
	 */
	public LatLong getLatLong(int iSide) {
		return null;
	}
	
	
	/**
	 * Get the number of localisation results present.
	 * <br>
	 * Note: There are two types of ambiguity, ambiguity in 'raw' localisation data e.g. 3D bearings
	 * from a planar array and ambiguity in localisation result e.g. multiple possible animal locations. 
	 * This function return the number of localisation results e.g. usually 2 for a towed stereo or planar array. 
	 * @param side - the ambiguity index. 
	 * @return the number of localisation results available. 
	 */
	public int getAmbiguityCount(){
		return 0; 
	}
	
	
	/**Localisation errors**/
	
	
	/**
	 * Get the loclaisation error. The error class allows users to specify an error in any direction. The error is plotted relative to 
	 * @param side - the ambiguity index. 
	 * @return
	 */
	public LocaliserError getLocError(int side){
		return null; 
	}
	
	
	/**
	 * Get the error perpendicular to the track line (in meters). Perpendicular error can be two things. 
	 * It can be the error perpendicular to a track line or it can be the y error from a static system
	 * @param iSide 0, 1, 2, etc.
	 * @return the error in metres. 
	 */
	public double getPerpendiculaError(int iSide) {
			return Double.NaN;
	}
	
	/**
	 * Get the error parallel to the track line (in meters). Parallel error can be two things. 
	 * It can be the error parallel to a track line or it can be the x error from a static system
	 * @param iSide 0, 1, 2, etc.
	 * @return the error in metres. 
	 */
	public double getParallelError(int iSide) {
		return Double.NaN;
	}
	
	/**
	 * The error on the range. The range is A direct line from the origin to the localisation. 
	 * It includes depth. 
	 * @param side - the ambiguity index. 
	 * @return The error on the range estimation in meters. 
	 */
	public double getRangeError(int iSide) {
		return Double.NaN;
	}

	
	/**
	 * The error in the height. (height is -depth). 
	 * @param side - the ambiguity index. 
	 * @return The error on the depth estimation in meters. 
	 */
	public double getHeightError(int iSide) {
		return Double.NaN;
	}
	
	/**
	 * Get an angle that the errors are to be plotted relative to. This is the real world angle.
	 * So if plotted relative to the direction of a ship the angle will be the direction of the ship.
	 * If plotted  in y= north and x=east Cartesian then the error direction will be 0
	 * @param iSide
	 * @return Error direction (radians)
	 */
	public PamVector getErrorDirection(int iSide) {
		return null;
	}

	
	
	/**Deprecated functions**/
	
	/**
	 * 
	 * @return true if the bearing is subject to a left right 
	 * (or rotational) ambiguity about the reference bearing. 
	 */
	@Deprecated
	public boolean bearingAmbiguity() {
//		double[] newAngles = getAngles();
//		if (newAngles != null) {
//			return (newAngles.length == 1 ? false : true);
//		}
		return locContents.hasLocContent(LocContents.HAS_AMBIGUITY);
	}

	
	/**
	 * Gte the number of lat long results (replaced by getAmbiguityCount()); 
	 * @return The number of LatLongs (generally 0 to 2)
	 */
	@Deprecated
	public int getNumLatLong() {
		return getAmbiguityCount(); 
//		if (bearingAmbiguity()) {
//			return 2;
//		}
//		else {
//			return 1;
//		}
	}
	
	/**
	 * 
	 * @return The bearing to the localisation (in radians)
	 */
	@Deprecated 
	public double getBearing() {
		return getBearing(0);
	}

	/**
	 * Get the bearing in radians, relative to the bearing reference
	 * @param iSide which side is the bearing on 
	 * @return bearing in radians
	 * @see getBearingReference
	 */
	@Deprecated
	public double getBearing(int iSide) {
		return Double.NaN;
	}
	
	@Deprecated
	public double getDepth() {
		return getHeight(0); 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		int nLoc = Math.max(1,getAmbiguityCount());
		String str = "";
		for (int i = 0; i < nLoc; i++) {
			double[] angles = getAngles();
			if (i > 0 && str.length() > 0) {
				str += "; ";
			}
			LatLong latLong = getLatLong(i);
			if (latLong != null) {
				str += latLong.toString();
				str += String.format(", depth %3.1m", -latLong.getHeight());
				continue;
			}
			else if (angles != null) {
				if (angles.length == 1) {
					str += String.format("Angle %3.1f\u00B0", Math.toDegrees(angles[0]));
				}
				else if (angles.length == 2) {
					str += String.format("Angles %3.1f\u00B0/%3.1f\u00B0", Math.toDegrees(angles[0]), Math.toDegrees(angles[1]));
				}
			}
		}
		return str;
	}
	
	

}
