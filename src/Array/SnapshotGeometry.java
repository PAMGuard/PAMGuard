package Array;

import java.util.Arrays;

import GPS.GpsData;
import PamUtils.LatLong;
import pamMaths.PamVector;

/**
 * Holder of array geometry for a group of data units and an 
 * instant in time. This class has primarily been developed to 
 * support the group 3D localiser functions. 
 * @author dg50
 *
 */
public class SnapshotGeometry {

	private long timeMilliseconds;
	
	private GpsData referenceGPS;
	
	private PamVector[] geometry;
	
	private PamVector geometricCentre;
	
	private int[] hydrophoneList;

	private PamArray currentArray;

	private int[] streamerList;

	private PamVector[] streamerErrors;

	private PamVector[] hydrophoneErrors;

	private Integer shape;
	
	private PamVector[] arrayAxes;
	

	/**
	 * 
	 * @param currentArray  Hydrophone array
	 * @param timeMilliseconds time this snap shot was created
	 * @param channelList list of data channels
	 * @param hydrophoneList list of hydrophones
	 * @param referenceGPS reference position and orientation
	 * @param geometricCentre geometric centre (average values of geometry)
	 * @param geometry array geometry in metres relative to referenceGPS. 
	 */
	public SnapshotGeometry(PamArray currentArray, long timeMilliseconds, int[] streamerList, int[] hydrophoneList, GpsData referenceGPS,
			PamVector geometricCentre, PamVector[] geometry, PamVector[] streamerErrors, PamVector[] hydrophoneErrors) {
		super();
		this.currentArray = currentArray;
		this.timeMilliseconds = timeMilliseconds;
		this.streamerList = streamerList;
		this.hydrophoneList = hydrophoneList;
		this.referenceGPS = referenceGPS;
		this.geometricCentre = geometricCentre;
		this.geometry = geometry;
		this.streamerErrors = streamerErrors;
		this.hydrophoneErrors = hydrophoneErrors;
	}
	
	public int getShape() {
		if (shape == null) {
			shape = workoutShape();
		}
		return shape;
	}
	

	/**
	 * Work out the array shape based on current geometry. 
	 * @return
	 */
	private Integer workoutShape() {
		int nPhones = geometry.length;
		/*
		 * If it's a point, all hydrophones have the 
		 * same location
		 * 
		 * If it's a line, then the dot product between vectors from 
		 * hydrophone 0 to all other hydrophones is always 1 or -1 or 
		 * the cross products are always close to 0. 
		 * 
		 * If it's a plane, then cross products of all pair vectors all point
		 * in the same or opposite directions. 
		 * 
		 * Otherwise it's a volume.
		 * 
		 */
		if (nPhones == 0) {
			return ArrayManager.ARRAY_TYPE_NONE;
		}
		if (nPhones == 1) {
			return ArrayManager.ARRAY_TYPE_POINT;
		}
		PamVector[] diffVectors = getDifferenceVectors(geometry);
		if (diffVectors == null || diffVectors.length== 0) {
			return ArrayManager.ARRAY_TYPE_POINT;
		}
		// here it must be at least at line !
		PamVector[] crossedVectors1 = getCrossedVectors(diffVectors);
		if (crossedVectors1 == null || crossedVectors1.length == 0) {
			arrayAxes = new PamVector[1];
			arrayAxes[0] = diffVectors[0].getUnitVector(); 
			return ArrayManager.ARRAY_TYPE_LINE;
		}
		/*
		 *  now cross the crosses and if there is still dimension there,
		 *  it's a volume. 
		 */
		PamVector[] crossedVectors2 = getCrossedVectors(crossedVectors1);
		if (crossedVectors2 == null || crossedVectors2.length == 0) {
			arrayAxes = new PamVector[2];
			arrayAxes[0] = diffVectors[0].getUnitVector(); 
			// first crossed vector is perpendicular to the plane
			PamVector perpVector = crossedVectors1[0].getUnitVector();
			// so cross that with the vector in the plane to get a second vector in the plane. 
			arrayAxes[1] = perpVector.vecProd(arrayAxes[0]);
			return ArrayManager.ARRAY_TYPE_PLANE;
		}
		else {
			arrayAxes = PamVector.cartesianAxes;
			return ArrayManager.ARRAY_TYPE_VOLUME;
		}
	}

	private PamVector[] getCrossedVectors(PamVector[] vectors) {
		int nOk = 0;
		int n = vectors.length;
		if (n<=1) {
			return null;
		}
		PamVector[] newVecs = new PamVector[vectors.length-1];
		for (int i = 1; i < n; i++) {
			PamVector newVec = vectors[i].vecProd(vectors[i-1]);
			double meanSize = Math.sqrt(vectors[i].normSquared()+vectors[i-1].normSquared());
			if (newVec.norm() / meanSize > 1e-2) {
				// only keep it if the vec prod is > 1/100th of the mean vector size. 
				newVecs[nOk++] = newVec; 
			}
		}
		if (nOk < newVecs.length) {
			newVecs = Arrays.copyOf(newVecs, nOk);
		}
		return newVecs;
	}

	/**
	 * Make a set of vectors which are the differences between the input 
	 * vectors. throw out any which are less than 1mm in length.
	 * @param vectors input vectors
	 * @return difference between input vectors. 
	 */
	private PamVector[] getDifferenceVectors(PamVector[] vectors) {
		int nOk = 0;
		int n = vectors.length;
		if (n<=1) {
			return null;
		}
		PamVector[] newVecs = new PamVector[vectors.length-1];
		PamVector lastNotNull = null;
		for (int i = 0; i < n; i++) {
			if (vectors[i] == null) {
				continue;
			}
			if (lastNotNull != null) {
				PamVector newVec = vectors[i].sub(lastNotNull);
				if (newVec.norm() > 1e-3) {
					newVecs[nOk++] = newVec; 
				}
			}
			lastNotNull = vectors[i];
		}
		if (nOk < newVecs.length) {
			newVecs = Arrays.copyOf(newVecs, nOk);
		}
		return newVecs;
	}
	
	/**
	 * Get the abs position of a specified hydrophone. 
	 * @param iPhone
	 * @return
	 */
	public LatLong getAbsPhoneLatLong(int iPhone) {
		if (iPhone >= geometry.length) {
			return null;
		}
		return referenceGPS.addDistanceMeters(geometry[iPhone]);
	}

	/**
	 * Get the array axes. For a linear array this is a single
	 * vector. 
	 * @return
	 */
	public PamVector[] getArrayAxes() {
		return arrayAxes;
	}
	
	/**
	 * Get the array axes as two angles, clockwise from North 
	 * and elevation. 
	 * @return array angles (1 element vector)
	 */
	public double[] getArrayAngles() {
		if (arrayAxes == null || arrayAxes.length == 0) {
			return null;
		}
		double[] angs = {arrayAxes[0].getHeading(), arrayAxes[0].getPitch()};
		return angs;
	}

	/**
	 * @return the timeMilliseconds
	 */
	public long getTimeMilliseconds() {
		return timeMilliseconds;
	}

	/**
	 * @param timeMilliseconds the timeMilliseconds to set
	 */
	public void setTimeMilliseconds(long timeMilliseconds) {
		this.timeMilliseconds = timeMilliseconds;
	}

	/**
	 * @return the referenceGPS
	 */
	public GpsData getReferenceGPS() {
		return referenceGPS;
	}

	/**
	 * @param referenceGPS the referenceGPS to set
	 */
	public void setReferenceGPS(GpsData referenceGPS) {
		this.referenceGPS = referenceGPS;
	}
	
	/**
	 * Get the central GPS position for used hydrophones. 
	 * @return central position of used hydrophones. 
	 */
	public GpsData getCentreGPS() {
		if (referenceGPS == null) {
			return null;
		}
		return referenceGPS.addDistanceMeters(getGeometricCentre());
	}

	/**
	 * @return the geometry in metres relative to the referenceGPS
	 */
	public PamVector[] getGeometry() {
		return geometry;
	}
	
	/**
	 * Get the maximum separation between any two hydrophones. 
	 * @return max separation between any pair of hydrophones
	 */
	public double getMaxSeparation() {
		if (geometry == null || geometry.length < 2) {
			return 0.;
		}
		double maxSep = 0;
		for (int i = 0; i < geometry.length; i++) {
			PamVector g1 = geometry[i];
			if (g1 == null) {
				continue;
			}
			for (int j = i+1; j < geometry.length; j++) {
				PamVector g2 = geometry[j];
				if (g2 == null) {
					continue;
				}
				double l = g1.sub(g2).norm();
				maxSep = Math.max(maxSep, l);
			}
		}
		return maxSep;
	}

	/**
	 * Get the distance between a hydrophone pair. 
	 * @param h1
	 * @param h2
	 * @return
	 */
	public double getPairDistance(int h1, int h2) {
		if (h1 >= geometry.length || h2 >= geometry.length) {
			return 0;
		}
		PamVector v1 = geometry[h1];
		PamVector v2 = geometry[h2];
		if (v1 == null || v2 == null) {
			return 0;
		}
		return v1.sub(v2).norm();
	}
	/**
	 * @param geometry the geometry in metres relative to the referenceGPS
	 */
	public void setGeometry(PamVector[] geometry) {
		this.geometry = geometry;
	}

	/**
	 * @return the hydrophoneList
	 */
	public int[] getHydrophoneList() {
		return hydrophoneList;
	}

	/**
	 * @param hydrophoneList the hydrophoneList to set
	 */
	public void setHydrophoneList(int[] hydrophoneList) {
		this.hydrophoneList = hydrophoneList;
	}

	/**
	 * @return the geometricCentre
	 */
	public PamVector getGeometricCentre() {
		return geometricCentre;
	}

	/**
	 * @return the currentArray
	 */
	public PamArray getCurrentArray() {
		return currentArray;
	}

	/**
	 * @return the streamerList
	 */
	public int[] getStreamerList() {
		return streamerList;
	}

	/**
	 * @return the streamerErrors
	 */
	public PamVector[] getStreamerErrors() {
		return streamerErrors;
	}

	/**
	 * @return the hydrophoneErrors
	 */
	public PamVector[] getHydrophoneErrors() {
		return hydrophoneErrors;
	}

}
