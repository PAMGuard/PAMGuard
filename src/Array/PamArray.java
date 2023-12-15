/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package Array;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

import Acquisition.AcquisitionControl;
import pamMaths.PamVector;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StreamerDataIterator;
import GPS.GpsData;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;
import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamView.PamSymbol;
import PamguardMVC.ChannelIterator;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Contains information on the hydrophone array. Individual hydrophones
 *         are not given channel numbers - channel numbers are the position of
 *         the hydrophone in the arraylist.
 *         <p>
 *         This class should eventually be extended so that Hydrophones
 *         positions can be updated dynamically and functions added to give
 *         distances / angles between them, interpolate the hydrophones position
 *         based on the ships track, etc.
 * @see Array.Hydrophone
 * @see Array.PamArray
 */
public class PamArray implements Serializable, Cloneable, ManagedParameters {

	// lose these - the definition of what constitutes an array is getting 
	// much more abstract !
	//	static private final int ARRAY_TYPE_TOWED = 0;
	//	static private final int ARRAY_TYPE_STATIC = 1;

	//	public static final int HEADING_TYPE_NONE = 0;
	//	public static final int HEADING_TYPE_TRUE = 1;
	//	public static final int HEADING_TYPE_MAGNETIC = 2;
	//	public static final int HEADING_TYPE_SHIP = 3;

	public static final int ORIGIN_USE_LATEST = 0;
	public static final int ORIGIN_INTERPOLATE = 1;
	public static final int ORIGIN_USE_PRECEEDING = 2;

	//	public static final int TILT_TYPE_NONE = 0;
	//	public static final int TILT_TYPE_UPDOWN = 1;

	public static final long serialVersionUID = 1;

	private ArrayList<Streamer> streamers = new ArrayList<Streamer>();

	private ArrayList<Hydrophone> hydrophoneArray = new ArrayList<Hydrophone>();

	private double speedOfSound = 1500;

	private double speedOfSoundError = 0;

	private String arrayName;

	private String arrayFile;

	//	private int originInterpolation = ORIGIN_USE_LATEST;
	private int originInterpolation = ORIGIN_USE_PRECEEDING;

	private int hydrophoneInterpolation = ORIGIN_USE_LATEST;

	private transient SnapshotGeometry lastSnapshotGeometry;
	
	private transient long lastSnapshotGeomTime;

	//	private LatLong fixedLatLong = new LatLong();

	//	private int tiltType = TILT_TYPE_NONE;
	//	
	//	private int headingType = HEADING_TYPE_NONE;
	//	
	//	private int arrayType = ARRAY_TYPE_TOWED;

	/**
	 * Array shape - point, line, plane, etc. 
	 */
	private int arrayShape = -1;

	@Deprecated
	private Class arrayLocatorClass; // still used when loading up old configurations !

	//	private Class<HydrophoneOriginMethod> hydrophoneOrigin;

	//	private int arrayLocator;

	//	transient private PamDataBlock<GpsDataUnit> fixedPointReferenceBlock;
	//	
	//	transient private GpsDataUnit fixedPointDataUnit;

	private MasterLocator masterLocator; // want to serialise this
	//	private static PamArray singleInstance;

	public PamArray(String arrayName, Class hydrophoneLocatorClass) {
		super();
		this.arrayName = arrayName;
		this.arrayLocatorClass = hydrophoneLocatorClass;
		masterLocator = new MasterLocator(this);
		sortHydrophoneLocator();
	}

	protected void sortHydrophoneLocator() {
		/*  
		 * old hydrophones will not have an arrayLocatorName 
		 *  in which case this first attempt will fail and it will
		 *  be necessary to use the old flags. 
		 */
		//		setHydrophoneLocator(arrayLocatorClass);
		//		if (hydrophoneLocator == null) {
		//			if (arrayType == ARRAY_TYPE_STATIC) {
		//				setHydrophoneLocator(StaticHydrophoneLocator.class);
		//			}
		//			switch (arrayLocator) {
		//			case 0:
		//				setHydrophoneLocator(StraightHydrophoneLocator.class);
		////				return new StraightHydrophoneLocator(pamArray);
		//			case 1:
		//				setHydrophoneLocator(ThreadingHydrophoneLocator.class);
		////				return new ThreadingHydrophoneLocator(pamArray);
		//			case 2:
		//				setHydrophoneLocator(NetworkHydrophoneLocator.class);
		////				return new NetworkHydrophoneLocator(pamArray);
		//			}
		//		}
		if (masterLocator == null || masterLocator.getClass() != MasterLocator.class) {
			masterLocator = new MasterLocator(this);
		}
		masterLocator.setupLocators(this);
	}

	public void notifyModelChanged(int changeType, boolean initComplete) {
		if (masterLocator != null) {
			masterLocator.notifyModelChanged(changeType, initComplete);
		}
		synchronized (streamers) {
			for (Streamer aStreamer:streamers) {
				aStreamer.notifyModelChanged(changeType, initComplete);
			}
		}
	}

	/**
	 * Get a snapshot geometry for the entire array for a given time. 
	 * will reuse the last value if at all possible. 
	 * @param timeMillis time in millis.  
	 * @return Snapshot geometry
	 */
	public SnapshotGeometry getSnapshotGeometry(long timeMillis) {
		if (lastSnapshotGeometry != null && timeMillis == lastSnapshotGeomTime) {
			return lastSnapshotGeometry;
		}
		lastSnapshotGeometry = ArrayManager.getArrayManager().getSnapshotGeometry(PamUtils.PamUtils.makeChannelMap(hydrophoneArray.size()), timeMillis);
		lastSnapshotGeomTime = timeMillis;
		return lastSnapshotGeometry;
	}

	public int addHydrophone(Hydrophone hydrophone) {
		synchronized (hydrophoneArray) {
			hydrophoneArray.add(hydrophone.getID(), hydrophone);
			checkHydrophoneIds();
			return hydrophoneArray.indexOf(hydrophone);
		}
	}

	public boolean removeHydrophone(Hydrophone hydrophone) {
		synchronized(hydrophoneArray) {
			if (hydrophoneArray.remove(hydrophone)){
				checkHydrophoneIds();
				return true;
			}
		}
		return false;
	}

	public int updateHydrophone(int oldIndex, Hydrophone hydrophone) {
		//hydrophoneArray.add(hydrophone);
		// remove it and put it back again
		synchronized (hydrophoneArray) {
			hydrophoneArray.remove(oldIndex);
			return addHydrophone(hydrophone);
		}
	}


	/**
	 * Get an individual hydrophone. 
	 * Programmers are advised NOT to access the hydrophone data directly, but
	 * rather to get coordinates via the array hydrophone locator. for this reason 
	 * this function has been protected. If you really MUST get hydrophone
	 * data directly, use getHiddenHydrophone
	 * @param iPhone hydrophone number
	 * @return Hydrophone
	 */
	protected Hydrophone getHydrophone(int iPhone) {
		synchronized (hydrophoneArray) {
			if (iPhone < 0 || iPhone >= hydrophoneArray.size()) {
				return null;
			}
			return hydrophoneArray.get(iPhone);
		}
	}

	protected Hydrophone getHydrophone(int iPhone, long timeMilliseconds) {
//		System.out.println("PAMArray: Get hydrophone coords: " + PamCalendar.formatDateTime(timeMilliseconds) + " iPhone: " + iPhone);
		

		if (hydrophoneInterpolation == ORIGIN_USE_LATEST) {
			return getHydrophone(iPhone);
		}

		//		ChannelIterator<HydrophoneDataUnit> channelIterator = ArrayManager.getArrayManager().getHydrophoneDataBlock().getChannelIterator(1<<iPhone, PamDataBlock.ITERATOR_END);
		//		HydrophoneDataUnit hdu = channelIterator.getPreceding(timeMilliseconds, true);

		//FIXME - for some reason the above lines were always returning the first hydrophone in the datablock ^
		HydrophoneDataUnit hdu = ArrayManager.getArrayManager().getHydrophoneDataBlock().getClosestHydrophone(timeMilliseconds, iPhone); 
		
//		System.out.println("PAMArray: hdu: " + hdu + " " + (hdu==null? null: PamCalendar.formatDateTime(hdu.getTimeMilliseconds()) + " Z" + hdu.getHydrophone().getdZ()));

		if (hdu != null) {
//						System.out.println("PAMArray: found unit: " + hdu.getTimeMilliseconds());
//						long firstTime = ArrayManager.getArrayManager().getHydrophoneDataBlock().getFirstUnit().getTimeMilliseconds();
//						long lastTime = ArrayManager.getArrayManager().getHydrophoneDataBlock().getLastUnit().getTimeMilliseconds(); 
//						System.out.println("PAMArray: found unit: " + firstTime + " " + lastTime + " no: units: " + ArrayManager.getArrayManager().getHydrophoneDataBlock().getUnitsCount()); 
			// TODO should maybe do something here to average out two hydrophones if interpolation option is selected. 
			return hdu.getHydrophone();
		}
		//		else Debug.println("PAMArray: found unit: null");

		// if it get's here, it didn't find a data unit, so just return the 
		// hydrophone from the main array
		return getHydrophone(iPhone);
	}


	/**
	 * public version of getHydrophone(). you probably shouldn't be calling this 
	 * unless it's from within an array locator looking for nominal position data. 
	 * @param channel
	 * @return
	 */
	public Hydrophone getHiddenHydrophone(int channel) {
		return getHydrophone(channel);
	}

	public void clearArray() {
		hydrophoneArray.clear();
	}

	public ArrayList<Hydrophone> getHydrophoneArray() {
		return hydrophoneArray;
	}

	/**
	 * Gets the size of the hydrophone array which is implicitly
	 * the hydrophone count.
	 *  
	 * @return The values reflecting the size of the hydrophone array
	 * with 0 being zero or a null object reference.
	 */
	public int getHydrophoneCount() {
		if (hydrophoneArray == null) return 0;
		return hydrophoneArray.size();
	}

	/**
	 * 
	 * @param iStreamer streamer Id
	 * @return number of hydrophone elements associated with that streamer. 
	 */
	public int getStreamerHydrophoneCount(int iStreamer) {
		int n = 0;
		for (int i = 0; i < hydrophoneArray.size(); i++) {
			if (hydrophoneArray.get(i).getStreamerId() == iStreamer) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Ensures that hydrophone numbering in in sequential order
	 */
	public void checkHydrophoneIds() {
		if (hydrophoneArray == null) return;
		for (int i = 0; i < hydrophoneArray.size(); i++) {
			hydrophoneArray.get(i).setID(i);
		}
	}

	/**
	 * 
	 * @param a1 phone 1
	 * @param a2 phone 2
	 * @return separation between two hydrophones in metres
	 */
	public double getSeparation(int a1, int a2, long timeMillis) {		

		PamVector v1 = getAbsHydrophoneVector(a1,timeMillis);
		PamVector v2 = getAbsHydrophoneVector(a2,timeMillis);


		if (v1 == null || v2 == null) return 0;
		return v1.dist(v2);
	}


	/**
	 * Get the true location vector for  a hydrophone element 
	 * which is the phone vector + the array vector. 
	 * @param iPhone hydrophone number
	 * @return absolute location. 
	 */
	public PamVector getAbsHydrophoneVector(int iPhone, long timeMillis) {
		// TODO needs rewriting to use the times to get the correct hydrophone and streamer data units !

		//find the correct hydrophone.
		Hydrophone hydrophone = getHydrophone(iPhone, timeMillis);
		if (hydrophone == null) {
			return null;
		}
		PamVector v = new PamVector(hydrophone.getCoordinates());

		//find the correct streamer.
		Streamer streamer = getStreamerData(hydrophone.getStreamerId(),timeMillis);
		if (streamer != null) {
			v = v.add(streamer.getCoordinateVector());
		}

		return v;
	}

	/**
	 * Get the hydrophone coordinates for a given time. These are just the hydrophone
	 * coordinates, and do NOT include the array coordinates. 
	 * @param iPhone
	 * @param timeMillis
	 * @return
	 */
	public PamVector getHydrophoneVector(int iPhone, long timeMillis) {
		Hydrophone h = getHydrophone(iPhone, timeMillis);
		if (h == null) {
			return null;
		}
		return new PamVector(h.getCoordinates());		
	}



	public void updateStreamer(int iStreamer, Streamer newStreamer) {
		synchronized (streamers) {
			if (iStreamer < streamers.size()){
				streamers.remove(iStreamer);
			}
			streamers.add(iStreamer, newStreamer);
			checkStreamerIndexes();
		}
	}

	/**
	 * Update a streamer with information from elsewhere in PAMGuard - currently called from the 
	 * network receiver when new buoy gps data arrive. <p>
	 * Any parameters that are null will not be updated. 
	 * @param iStreamer  Streamer index
	 * @param name Streamer name
	 * @param coord Streamer coordinates
	 * @param coordError streamer coordinate errors
	 * @param originSettings origin settings (what the streamer is referenced to)
	 * @param locatorSettings locator settings (not yet used).
	 */
	public void newStreamerFromUpdate(long timeMilliseconds, int iStreamer, String name, double[] coord, double[] coordError, 
			OriginSettings originSettings, LocatorSettings locatorSettings) {
		Streamer streamer = getStreamer(iStreamer).clone();
		if (name != null) {
			streamer.setStreamerName(name);
		}
		if (coord != null) {
			streamer.setCoordinates(coord);
		}
		if (coordError != null) {
			streamer.setCoordinateErrors(coordError);
		}
		if (originSettings != null) {
			streamer.setOriginSettings(originSettings);
		}
		if (locatorSettings != null) {
			streamer.setLocatorSettings(locatorSettings);
		}
		newStreamer(timeMilliseconds, streamer);
	}

	/**
	 * 
	 * Update a streamer and create a new StreamerDataUnit based on an existing
	 * streamer from elsewhere in PAMGuard - currently called from the DIFAR &
	 * network receiver modules.
	 * <p>
	 * 
	 * This will not only update the streamer, but will also create a new streamer
	 * data unit and all that is associated with it.
	 * 
	 * @param timeMilliseconds
	 * @param newStreamerData
	 *            - the new streamer & data unit will be based on this
	 */
	public void newStreamer(long timeMilliseconds, Streamer newStreamerData) {
		newStreamerData.setupLocator(this);
		updateStreamer(newStreamerData.getStreamerIndex(), newStreamerData);
		/*
		 * Now make a new updated data unit.  
		 */
		StreamerDataUnit sdu = new StreamerDataUnit(timeMilliseconds, newStreamerData);
		ArrayManager.getArrayManager().getStreamerDatabBlock().addPamData(sdu);
	}

	/**
	 * 
	 * Update a streamer and update an existing StreamerDataUnit 
	 * with information from elsewhere in PAMGuard. 
	 * 
	 * @param timeMilliseconds
	 * @param newStreamerData
	 */
	public void updateStreamerAndDataUnit(long timeMilliseconds, Streamer newStreamerData) {
		newStreamerData.setupLocator(this);
		int index = newStreamerData.getStreamerIndex();
		updateStreamer(index, newStreamerData);

		// Now make a new updated data unit.
		StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock()
				.getPreceedingUnit(timeMilliseconds, 1<<index);
		sdu.setStreamerData(newStreamerData);
		ArrayManager.getArrayManager().getStreamerDatabBlock().updatePamData(sdu, System.currentTimeMillis());
	}

	/**
	 * Get the separation of two hydrophones - hopefully, channelMap
	 * will contain just two set bits !
	 * @param channelMap
	 * @return the separation between two hydrophones in milliseconds
	 */
	public double getSeparationInSeconds(int channelMap, long timeMillis) {
		int[] channels = new int[2];
		int found = 0;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & channelMap) > 0) {
				channels[found] = i;
				if (++found >= 2) break;
			}
		}
		return getSeparationInSeconds(channels[0], channels[1], timeMillis);
	}

	/**
	 * 
	 * @param a1 phone 1
	 * @param a2 phone 2
	 * @return separation of two hydrophones in seconds. 
	 */
	public double getSeparationInSeconds(int a1, int a2, long timeMillis) {
		return getSeparation(a1, a2, timeMillis) / speedOfSound;
	}
	/**
	 * Get the max phone separation in metres for all hydrophones in the array.  
	 * @return max phone separation in metres. 
	 */
	public double getMaxPhoneSeparation(long timeMillis){
		return getMaxPhoneSeparation(0xFFFFFFFF, timeMillis);
	}

	/**
	 * Get the max phone separation in metres. 
	 * <p> this will not currently work if hydrophones are in different clusters
	 * @param phoneMap bit map of used hydrophones. 
	 * @return max phone separation in metres. 
	 */
	public double getMaxPhoneSeparation(int phoneMap, long timeMillis) {
		double maxSep = 0, sep;
		int nH = getHydrophoneCount();
		for (int i = 0; i < nH; i++) {
			if ((1<<i & phoneMap) == 0) continue;
			for (int j = i+1; j < nH; j++) {
				if ((1<<j & phoneMap) == 0) continue;
				sep = getSeparation(i, j, timeMillis);
				maxSep = Math.max(sep, maxSep);
			}
		}

		return maxSep;
	}

	/**
	 * Get the error in the separation between two hydrophones along
	 * the axis joining the two phones
	 * @param a1 phone 1
	 * @param a2 phone 1
	 * @return error in m. 
	 */
	public double getSeparationError(int a1, int a2, long timeMillis) {
		PamVector v1 = getAbsHydrophoneVector(a1,timeMillis);
		PamVector v2 = getAbsHydrophoneVector(a2,timeMillis);
		if (v1 == null || v2 == null) {
			return 0;
		}
		else {
			return getSeparationError(a1, a2, v1.sub(v2),timeMillis);
		}
	}

	/**
	 * Get the separation error in a given direction
	 * @param a1 phone 1
	 * @param a2 phone 2 
	 * @param direction direction of interest. 
	 * @return error in that direction
	 */
	public double getSeparationError(int a1, int a2, PamVector direction,long timeMillis) {
		PamVector errorVector = getSeparationErrorVector(a1, a2, timeMillis);
		direction = direction.getUnitVector();
		return Math.abs(errorVector.dotProd(direction));
	}

	public double getWobbleRadians(int a1, int a2 ,long timeMillis) {
		double perpError = getPerpendicularError(a1, a2, timeMillis);
		double separation = getSeparation(a1, a2, timeMillis);
		double wobble = Math.abs(Math.atan2(perpError, separation));
		return wobble;
	}

	/**
	 * Get the error in the plane perpendicular to the line separating 
	 * two hydrophones. 
	 * @param a1 hydrophone 1
	 * @param a2 hydrophone 2
	 * @return magnitude of error perpendicular to the line between them. 
	 */
	public double getPerpendicularError(int a1, int a2, long timeMillis) {
		PamVector v1 = getAbsHydrophoneVector(a1,timeMillis);
		PamVector v2 = getAbsHydrophoneVector(a2,timeMillis);
		if (v1 == null || v2 == null) {
			return 0;
		}
		else {
			return getPerpendicularError(a1, a2, v1.sub(v2));
		}
	}

	/**
	 * 
	 * @param a1
	 * @param a2
	 * @param direction
	 * @return
	 */
	public double getPerpendicularError(int a1, int a2, PamVector direction) {
		if (direction.normalise() == 0) {
			return 0;
		}

		Hydrophone h = getHydrophone(a1);
		if (h == null) {
			return 0;
		}
		PamVector v1 = h.getErrorVector();
		h = getHydrophone(a2);
		if (h == null) {
			return 0;
		}
		PamVector v2 = h.getErrorVector();
		PamVector errVec = PamVector.addQuadrature(v1, v2);
		errVec = errVec.vecProd(direction);
		double error = errVec.norm();

		return error;
	}

	/**
	 * Gets the error of the separation between two hydrophones as a vector. 
	 * <p>
	 * If the hydrophones are in the same streamer, then the error is the error
	 * on the coordinate of each phone added in quadrature, and the streamer errors
	 * are ignored. 
	 * <p>
	 * If the hydrophones are in different streamers, then the streamer errors are
	 * also incorporated. 
	 * @param a1 phone number 1
	 * @param a2 phone number 2
	 * @param timeMillis the current time in milliseconds
	 * @return vector representing components of error in each direction. 
	 */
	public PamVector getSeparationErrorVector(int a1, int a2, long timeMillis) {
		Hydrophone h1 = getHydrophone(a1, timeMillis);
		Hydrophone h2 = getHydrophone(a2, timeMillis);
		PamVector e1 = new PamVector(h1.getCoordinateErrors());
		PamVector e2 = new PamVector(h2.getCoordinateErrors());
		if (h1.getStreamerId() == h2.getStreamerId()) {
			return PamVector.addQuadrature(e1, e2);
		}
		PamVector e3 = getStreamerData(h1.getStreamerId(), timeMillis).getErrorVector();
		PamVector e4 = getStreamerData(h2.getStreamerId(), timeMillis).getErrorVector();
		return PamVector.addQuadrature(e1, e2, e3, e4);
	}


	/**
	 * 
	 * @param a1 phone number 1
	 * @param a2 phone number 2
	 * @param timeMillis
	 * @return
	 */
	public double getTimeDelayError(int a1, int a2, long timeMillis) {

		PamVector sepError= getSeparationErrorVector( a1,  a2,  timeMillis); 

		double totSqrd=0;

		for (int i=0; i<3; i++){
			totSqrd+=Math.pow(sepError.getVector()[i],2);
		}

		double tdError=(Math.sqrt(totSqrd))/speedOfSound; 

		/**
		 * This was wrong up to 22/1/2019. 
		 * The additional timing error due to the sos being incorrect should be a multiple of the 
		 * absolute distance between the hydrophones, NOT the uncertainty in that distance. 
		 * Old code line ...
		double soundError=Math.abs(((Math.sqrt(totSqrd))/(speedOfSound+ speedOfSoundError))-tdError);
		 */
		// new lines ...
		double hSep = getSeparation(a1, a2, timeMillis);
		// this is mathematically equivalent to the line for se2 for speedofSoundErr << speedOfSound
		double soundError = hSep/speedOfSound*(speedOfSoundError/speedOfSound);
		//		double se2 = hSep*Math.abs((1/(speedOfSound+speedOfSoundError)-1./speedOfSound));

		return Math.sqrt(Math.pow(tdError,2)+Math.pow(soundError,2));
	}

	/**
	 * Create a simple linear array. 
	 * @param arrayName name
	 * @param y0 y position of first hydrophone
	 * @param depth depth
	 * @param separation separation between each element
	 * @param nElements number of eleemnts
	 * @param sensitivity hydrophone sensitivity
	 * @param gain preamp gain
	 * @param bandwidth preamp bandwidth
	 * @return new array 
	 */
	public static PamArray createSimpleArray(String arrayName, double y0, double height, double separation,
			int nElements, double sensitivity, double gain, double[] bandwidth) {
		PamArray newArray = new PamArray(arrayName, ThreadingHydrophoneLocator.class);
		for (int i = 0; i < nElements; i++) {
			newArray.addHydrophone(new Hydrophone(i, 0, y0 - separation * i, height, "Unknown",
					sensitivity, bandwidth, gain));
		}

		return newArray;
	}

	/**
	 * 
	 * @return Speed of sound in m/s
	 */
	public double getSpeedOfSound() {
		return speedOfSound;
	}

	/**
	 * 
	 * @param speedOfSound speed of sound in m/s
	 */
	public void setSpeedOfSound(double speedOfSound) {
		this.speedOfSound = speedOfSound;
	}
	public double getSpeedOfSoundError() {
		return speedOfSoundError;
	}

	public void setSpeedOfSoundError(double speedOfSoundError) {
		this.speedOfSoundError = speedOfSoundError;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return arrayName;
	}

	public String getArrayFileName() {
		if (arrayFile != null) return arrayFile;
		return arrayName + "." + ArrayManager.getArrayFileType();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PamArray clone() {
		try {
			PamArray pa = (PamArray) super.clone();
			if (pa.arrayFile != null) {
				pa.arrayFile = new String(pa.arrayFile);
			}
			if (pa.arrayName != null) {
				pa.arrayName = new String(pa.arrayName);
			}
			ArrayList<Hydrophone> hl = new ArrayList<Hydrophone>();
			for (int i = 0; i < pa.hydrophoneArray.size(); i++) {
				hl.add(pa.hydrophoneArray.get(i).clone());
			}
			pa.hydrophoneArray = hl;
			if (pa.masterLocator == null) {
				pa.masterLocator = new MasterLocator(pa);
			}
			// now check that the streamers have had their indexes set.
			pa.checkStreamerIndexes();
			if (pa.masterLocator == null) {
				pa.masterLocator = new MasterLocator(pa);
			}
			pa.sortHydrophoneLocator();
			return pa;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
			return null;
		}
	}

	private void checkStreamerIndexes() {
		int n = getNumStreamers();
		int[] newIndexes = new int[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < n; i++) {
			newIndexes[getStreamer(i).getStreamerIndex()] = i;
			getStreamer(i).setStreamerIndex(i);
		}
		Hydrophone h;
		n = getHydrophoneCount();
		for (int i = 0; i < n; i++) {
			h = getHydrophone(i);
			int oldInd = h.getStreamerId();
			if (oldInd >= 0 && oldInd < PamConstants.MAX_CHANNELS) {
				h.setStreamerId(newIndexes[oldInd]);
			}
		}
		masterLocator.setupLocators(this);
	}

	/**
	 * @return Returns the arrayFile.
	 */
	public String getArrayFile() {
		return arrayFile;
	}
	/**
	 * @param arrayFile The arrayFile to set.
	 */
	public void setArrayFile(String arrayFile) {
		this.arrayFile = arrayFile;
	}

	//	/**
	//	 * @return Returns the fixedLatLong.
	//	 */
	//	public LatLong getFixedLatLong() {
	//		return fixedLatLong;
	//	}
	//	/**
	//	 * @param fixedLatLong The fixedLatLong to set.
	//	 */
	//	public void setFixedLatLong(LatLong fixedLatLong) {
	//		this.fixedLatLong = fixedLatLong.clone();
	////		setStaticMasterReference();
	//	}

	//	private void setStaticMasterReference() {
	//		MasterReferencePoint.setRefLatLong(fixedLatLong, "Static Array Loc");		
	//	}
	/**
	 * @return Returns the arrayName.
	 */
	public String getArrayName() {
		return arrayName;
	}
	/**
	 * @param arrayName The arrayName to set.
	 */
	public void setArrayName(String arrayName) {
		this.arrayName = arrayName;
	}
	//	/**
	//	 * @return Returns the arrayType. Either ARRAY_TYPE_TOWED or ARRAY_TYPE_STATIC
	//	 */
	//	public int getArrayType() {
	//		return arrayType;
	//	}
	//	/**
	//	 * @param arrayType Set the array type - Either ARRAY_TYPE_TOWED or ARRAY_TYPE_STATIC.
	//	 */
	//	public void setArrayType(int arrayType) {
	//		this.arrayType = arrayType;
	//		switch (arrayType) {
	//		case ARRAY_TYPE_STATIC:
	//			hydrophoneLocator = new StaticHydrophoneLocator(this);
	//			setStaticMasterReference();
	//			break;
	//		case ARRAY_TYPE_TOWED:
	//			hydrophoneLocator = HydrophoneLocators.getInstance().get(arrayLocator, this);
	//			break;
	//		}
	//	}
	//	/**
	//	 * @return Returns the headingType.
	//	 */
	//	public int getHeadingType() {
	//		return headingType;
	//	}
	//
	//	/**
	//	 * @param headingType The headingType to set.
	//	 */
	//	public void setHeadingType(int headingType) {
	//		this.headingType = headingType;
	//	}

	//	/**
	//	 * @return Returns the tiltType.
	//	 */
	//	public int getTiltType() {
	//		return tiltType;
	//	}
	//
	//	/**
	//	 * @param tiltType The tiltType to set.
	//	 */
	//	public void setTiltType(int tiltType) {
	//		this.tiltType = tiltType;
	//	}
	/**
	 * @return Returns the fixedPointReferenceBlock.
	 */
	//	public PamDataBlock<GpsDataUnit> getFixedPointReferenceBlock() {
	//		if (fixedPointReferenceBlock == null || fixedPointReferenceBlock.getUnitsCount() == 0) {
	////			setupFixedPointReferenceBlock();
	//		}
	//		return fixedPointReferenceBlock;
	//	}
	//	private void setupFixedPointReferenceBlock() {
	//		if (fixedPointReferenceBlock != null & arrayType == ARRAY_TYPE_TOWED) {
	//			fixedPointReferenceBlock = null;
	//			fixedPointDataUnit = null;
	//			return;
	//		}
	//		if (fixedPointReferenceBlock == null) {
	//			fixedPointReferenceBlock = new PamDataBlock<GpsDataUnit>(GpsDataUnit.class, "Fixed Data", null, 0);
	//			fixedPointReferenceBlock.setLinkGpsData(false);
	//			fixedPointReferenceBlock.setNaturalLifetime(Integer.MAX_VALUE);
	//		}
	//		fixedPointReferenceBlock.clearAll();
	//		GpsData positionData = new GpsData();
	////		if (fixedLatLong != null) {
	////			positionData.setLatitude(fixedLatLong.getLatitude());
	////			positionData.setLongitude(fixedLatLong.getLongitude());
	//////			setStaticMasterReference();
	////		}
	//		fixedPointDataUnit = new GpsDataUnit(PamCalendar.getTimeInMillis(), positionData);
	//		fixedPointReferenceBlock.addPamData(fixedPointDataUnit);
	//	}

	//	public boolean setHydrophoneLocator(Class hydrophoneLocatorClass) {
	//		HydrophoneLocatorSystem hs = HydrophoneLocators.getInstance().getLocatorSystem(hydrophoneLocatorClass);
	//		if (hs == null) {
	//			return false;
	//		}
	//		HydrophoneLocator hl = hs.getLocator(this);
	//		setHydrophoneLocator(hl);
	//		return hydrophoneLocator != null;

	//	}

	//	public void setHydrophoneLocator(HydrophoneLocator hydrophoneLocator) {
	//		
	//		if (hydrophoneLocator == null) {
	//			hydrophoneLocator = new StraightHydrophoneLocator(this);
	//		}
	//		this.hydrophoneLocator = hydrophoneLocator;
	//		arrayLocatorClass = hydrophoneLocator.getClass();
	//	}


	public HydrophoneLocator getHydrophoneLocator() {
		return masterLocator;
	}

	/**
	 * 
	 * @return the number of streamers in the system.
	 */
	public int getNumStreamers() {
		checkDefStreamer();
		return streamers.size();
	}

	/**
	 * Get the index of a specified streamer. 
	 * @param aStreamer
	 * @return index of streamer or -1. 
	 */
	public int indexOfStreamer(Streamer aStreamer) {
		synchronized (streamers) {
			return streamers.indexOf(aStreamer);
		}
	}

	private void checkDefStreamer() {
		if (streamers == null) {
			streamers = new ArrayList<Streamer>();
		}
		synchronized (streamers) {
			if (streamers.size() == 0) {
				streamers.add(new Streamer(streamers.size(), 0, 0, 0, 0.1, 0.1, 0.1));
			}
		}
	}

	/**
	 * Get the streamer at a specific index. 
	 * @param streamerIndex
	 * @return reference to a streamer. 
	 */
	public Streamer getStreamer(int streamerIndex) {
		checkDefStreamer();
		if (streamerIndex < streamers.size()) {
			return streamers.get(streamerIndex);
		}
		return null;
	}

	/**
	 * Get the number of different streamers (not the number of streamer data units)
	 * @return number of unique streamers. 
	 */
	public int getStreamerCount() {
		if (streamers==null) return 0; 
		return streamers.size();
	}

	//	/**
	//	 * Get the streamer at a specififed time. This function searches through the streamer datablock and finds the streamer closest to timeMillis which has a specified streamer index. .
	//	 * @param streamerIndex - the streamer index you wish to find
	//	 * @param timeMilliseconds - the time in millis. 
	//	 * @return the streamer with index streamerIndex closest to timeMilliseconds
	//	 */
	//	public Streamer getStreamer(int streamerIndex, long timeMilliseconds) {
	//		
	//		Streamer streamer=getStreamer(streamerIndex);
	//		
	//		StreamerDataUnit streamerDataUnit = getStreamerDataUnit( streamerIndex,  timeMilliseconds);
	//		
	//		if (streamerDataUnit==null) return streamer;
	//		else return streamerDataUnit.getStreamerData();
	//
	//	}
	//	
	//	public StreamerDataUnit getStreamerDataUnit(int streamerIndex, long timeMilliseconds) {
	//		
	//		Streamer streamer=getStreamer(streamerIndex);
	//		
	//		HydrophoneOriginMethod originMethod = streamer.getHydrophoneOrigin();
	//		StreamerDataUnit streamerDataUnit=originMethod.getStreamerData(timeMilliseconds);
	//
	//		return streamerDataUnit;
	//		
	//	}


	//	/**
	//	 * Find a streamer with a given buoy Id. 
	//	 * @param buoyId integer buoy id
	//	 * @return streamer or null if none found. 
	//	 */
	//	public Streamer findBuoyStreamer(int buoyId) {
	//		for (Streamer s:streamers) {
	//			if (s.getBuoyId1() != null && s.getBuoyId1() == buoyId) {
	//				return s;
	//			}
	//		}
	//		return null;
	//	}

	public Streamer findStreamer(String streamerName) {
		if (streamerName == null) {
			return null;
		}
		for (Streamer s:streamers) {
			if (streamerName.equals(s.getStreamerName())) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Clone a streamer, doing a hard clone of all hydrophones, localiser
	 * stuff, etc. 
	 * @return the new streamer reference (which will be added already to the array)
	 */
	public Streamer cloneStreamer() {
		Streamer aStreamer = streamers.get(0);
		if (aStreamer == null) {
			return null;
		}
		return cloneStreamer(aStreamer);
	}

	/**
	 * Clone a streamer, doing a hard clone of all hydrophones, localiser
	 * stuff, etc. 
	 * @param streamer to clone. 
	 * @return the new streamer reference (which will be added already to the array)
	 */
	public Streamer cloneStreamer(Streamer aStreamer) {
		/*
		 * streamer clone already does a hard clone of most of what we need. 
		 */
		Streamer newStreamer = aStreamer.clone();

		addStreamer(newStreamer);
		checkStreamerIndexes();

		/*
		 * Now copy all hydrophones associated with aStreamer. 
		 */
		int n = getHydrophoneCount();
		Hydrophone h, newH;
		for (int i = 0; i < n; i++) {
			h = getHydrophone(i);
			if (h.getStreamerId() != aStreamer.getStreamerIndex()) {
				continue;
			}
			newH = h.clone();
			newH.setStreamerId(newStreamer.getStreamerIndex());
			addHydrophone(newH);
		}


		return newStreamer;
	}

	/**
	 * Get the limits of the dimensions of the entire array. 
	 * 
	 * @return a 3 by 2 double array of the min and max of x,y,z.
	 */
	public double[][] getDimensionLimits() {
		if (hydrophoneArray.size() == 0) {
			return null;
		}
		Hydrophone h = hydrophoneArray.get(0);
		Streamer s = getStreamer(h.getStreamerId());
		double[][] lims = new double[3][2];
		for (int c = 0; c < 3; c++) {
			for (int i = 0; i < 2; i++) {
				lims[c][i] = h.getCoordinate(c);
				if (s != null) {
					lims[c][i] += s.getCoordinate(c);
				}
			}
		}
		double[] hCoordinate;
		double[] sCoordinate;
		for (int iH = 1; iH < hydrophoneArray.size(); iH++) {
			h = hydrophoneArray.get(iH);
			hCoordinate = Arrays.copyOf(h.getCoordinates(),3);
			s = getStreamer(h.getStreamerId());
			if (s != null) {
				sCoordinate = s.getCoordinates();
				for (int i = 0; i < 3; i++) {
					hCoordinate[i] += sCoordinate[i];
				}
			}
			for (int c = 0; c < 3; c++) {
				lims[c][0] = Math.min(lims[c][0], hCoordinate[c]);
				lims[c][1] = Math.max(lims[c][1], hCoordinate[c]);	
			}
		}
		return lims;
	}

	/**
	 * Get a bitmap of hydrophones used with a particular streamer. 
	 * @param streamer Hydrophone streamer. 
	 * @return bitmap of used hydrophones. 
	 */
	public int getPhonesForStreamer(Streamer streamer) {
		int streamerIndex = streamers.indexOf(streamer);
		return getPhonesForStreamer(streamerIndex);
	}

	/**
	 * Get a bitmap of hydrophones used with a particular streamer. 
	 * @param streamerId Hydrophone streamer index. 
	 * @return bitmap of used hydrophones.
	 */
	public int getPhonesForStreamer(int streamerId) {
		int phones = 0;
		int ih = 0;
		for (Hydrophone h:hydrophoneArray) {
			if (h.getStreamerId() == streamerId) {
				phones |= 1<<ih;
			}
			ih++;
		}
		return phones;
	}

	/**
	 * Get the streamer id for a particular hydrophone
	 * @param phoneId hydrophone index
	 * @return stramer index.
	 */
	public int getStreamerForPhone(int phoneId) {
		if (phoneId < 0 || phoneId >= hydrophoneArray.size()) {
			//			System.out.printf("Invalid hydrophone id: %d. Range is 0 to %d. Assigning to default streamer\n", phoneId, hydrophoneArray.size()-1);
			return 0;
		}
		Hydrophone h = hydrophoneArray.get(phoneId);
		return h.getStreamerId();
	}

	/**
	 * Get data for a specific hydrophone at a specific time.
	 * This is the raw hydrophone data, which may later get modified by 
	 * a hydroponelocaliser.  
	 * @param hydrophoneIndex
	 * @param timeMilliseconds
	 * @return
	 */
	public Hydrophone getHydroponeData(int hydrophoneIndex, long timeMilliseconds) {
		if (hydrophoneIndex < 0 || hydrophoneIndex >= hydrophoneArray.size()) {
			return null;
		}
		Hydrophone hydrophone = hydrophoneArray.get(hydrophoneIndex);
		int interpolation = getHydrophoneInterpolation();
		if (interpolation == PamArray.ORIGIN_USE_LATEST) {
			return hydrophone;
		}
		HydrophoneDataBlock hydDataBlock = ArrayManager.getArrayManager().getHydrophoneDataBlock();
		HydrophoneDataUnit prevUnit = null, nextUnit = null;
		synchronized (hydDataBlock.getSynchLock()) {
			ChannelIterator<HydrophoneDataUnit> hydIter = hydDataBlock.getChannelIterator(1<<hydrophoneIndex, PamDataBlock.ITERATOR_END);
			while (hydIter.hasPrevious()) {
				prevUnit = hydIter.previous();
				if (prevUnit.getTimeMillis() < timeMilliseconds) {
					break;
				}
			}
			if (prevUnit == null) {
				return hydrophone;
			}
			if (interpolation == PamArray.ORIGIN_USE_PRECEEDING) {
				return prevUnit.getHydrophone();
			}
			if (hydIter.hasNext()) {
				nextUnit = hydIter.next();
			}
		}
		if (nextUnit == null) {
			return prevUnit.getHydrophone();
		}
		double w1 = nextUnit.getTimeMilliseconds()-timeMilliseconds;
		double w2 = timeMilliseconds-prevUnit.getTimeMilliseconds();
		double wTot = w1+w2;
		w1 /= wTot;
		w2 /= wTot;
		return Hydrophone.weightedAverage(prevUnit.getHydrophone(), nextUnit.getHydrophone(), w1, w2); 
	}

	/**
	 * Get the streamer information for the given time. Should always be able to 
	 * return something. May be an existing object or may be able to interpret between 
	 * them. 
	 * Note that this is the basic streamer data and is NOT iterated through the origin systems or anything. 
	 * @param timeMilliseconds
	 * @return
	 */
	public Streamer getStreamerData(int streamerIndex, long timeMilliseconds) {
		if (streamerIndex < 0 || streamerIndex >= streamers.size()) {
			return null;
		}
		Streamer streamer = streamers.get(streamerIndex);
		int interpolation = getHydrophoneInterpolation();
		if (interpolation == PamArray.ORIGIN_USE_LATEST) {
			return streamer;
		}
		// otherwise we'll need to interpolate. 
		StreamerDataBlock streamerDataBlock = ArrayManager.getArrayManager().getStreamerDatabBlock();
		StreamerDataUnit preceeding = null, nextUnit = null;
		synchronized (streamerDataBlock.getSynchLock()) {
			ChannelIterator<StreamerDataUnit> streamerIterator = streamerDataBlock.getChannelIterator(1<<streamerIndex, PamDataBlock.ITERATOR_END);
			// move to the one before our time. 
			preceeding = null;
			while (streamerIterator.hasPrevious()) {
				preceeding = streamerIterator.previous();
				if (preceeding.getTimeMilliseconds() <= timeMilliseconds) {
					break;
				}
			}
			if (preceeding == null) {
				return streamer;
			}
			if (interpolation == PamArray.ORIGIN_USE_PRECEEDING) {
				return preceeding.getStreamerData();
			}
			// otherwise, we also want the next one ...
			if (streamerIterator.hasNext() == false) {
				return preceeding.getStreamerData();
			}
			nextUnit = streamerIterator.next();
		}
		// get some hashed up interpolation of the two.
		if (nextUnit == null) {
			return preceeding.getStreamerData();
		}
		double w1 = nextUnit.getTimeMilliseconds()-timeMilliseconds;
		double w2 = timeMilliseconds-preceeding.getTimeMilliseconds();
		double wTot = w1+w2;
		if (wTot == 0 || Double.isFinite(wTot) == false) {
			w1 = w2 = 0.5;
		}
		else {
			w1 /= wTot;
			w2 /= wTot;
		}
		return Streamer.weightedAverage(preceeding.getStreamerData(), nextUnit.getStreamerData(), w1, w2); 
	}


	/**
	 * Add a streamer and return the streamer id which will be one less than the number of streamers. 
	 * @param streamer
	 * @return
	 */
	public int addStreamer(Streamer streamer) {
		synchronized (streamers) {
			streamers.add(streamer);
			checkStreamerIndexes();
			return streamer.getStreamerIndex();
		}
	}


	public void removeStreamer(int row) {
		synchronized (streamers) {
			streamers.remove(row);
			checkStreamerIndexes();
		}
	}

	/**
	 * 
	 * @return the array shape, this is either point, line, plane or volumetric
	 */
	public int getArrayShape() {
		if (arrayShape < 0) {
			arrayShape = ArrayManager.getArrayManager().getArrayShape(this);
		}
		return arrayShape;
	}

	/**
	 * 
	 * @param arrayShape set the array shape - point, line, plane or volumetric. 
	 */
	public void setArrayShape(int arrayShape) {
		this.arrayShape = arrayShape;
	}

	/**
	 * Return the type of array this is (e.g. "Point Array", "Line Array", etc)
	 * 
	 * @return
	 */
	public String getArrayTypeString() {
		return ArrayManager.getArrayTypeString(this.getArrayShape());
	}

	/**
	 * @return the arrayLocator class. This is NOT the master
	 * locator, but the default class for the array which will 
	 * get used by the sub locators running behind the master locator. 
	 */
	public Class getArrayLocatorClass() {
		return arrayLocatorClass;
	}

	/**
	 * Get the hydrophone sensitivity and gain added together. 
	 * @param hydrophoneChannel hydrophone hardware number
	 * @return Sum of hydrophone sensitivity and gain. 
	 */
	public double getHydrophoneSensitivityAndGain(int hydrophoneChannel) {
		Hydrophone h = getHydrophone(hydrophoneChannel);
		if (h == null) return 0;
		return h.getSensitivity() + h.getPreampGain();
	}

	/**
	 * Get the clip level - this is the level at which a signal will clip using the specified
	 * DAQ system. The level is  in dB re 1uPa if in water or dB re 20uPa if in air.
	 * @param hydrophoneChannel - the  hydrophone/microphone hardware number
	 * @return the clip level. 
	 */
	public double getClipLevel(int hydrophoneChannel) {
		Hydrophone h = getHydrophone(hydrophoneChannel);
		if (h == null) return 0;
		// if the answer is -Infinity or Infinity or NaN, just set it to 0
	
		ArrayList<AcquisitionControl> acquisitionControllers = Acquisition.AcquisitionControl.getControllers();
		
		int [] chans; 
		AcquisitionControl theControl = null; 
		for (AcquisitionControl acquisitionContorl: acquisitionControllers) {
			 chans = acquisitionContorl.getAcquisitionParameters().getHardwareChannelList(); 
			 if (PamArrayUtils.contains(chans, hydrophoneChannel)){
				 theControl = acquisitionContorl; 
				 break;
			 }
		}
		
		double dB ;
		if (theControl == null) {
			System.err.println("Warning: PamArray.getClipLevel: unable to find peak to peak voltage in Acquisition moudles."); 
			dB =  - (h.getSensitivity() + h.getPreampGain()); 
		}
		else {
			dB  = 20*Math.log10(theControl.getPeak2PeakVoltage(hydrophoneChannel))  -  (h.getSensitivity() + h.getPreampGain());
		}
		
		if (!Double.isFinite(dB)) {
			dB = 0;
		}
		
		return dB;
	}


	/**
	 * Get the hydrophone coordinates as a three element (x,y,z) vector
	 * referenced to the streamer position 
	 * <p> Hydrophone locators which want to vary location as a function of 
	 * time should override this function and use the timeMilliseconds 
	 * parameter to get the true location of the hydrophones in their own way. 
	 * @param iHydrophone hydrophone number
	 * @param timeMilliseconds time of location
	 * @return three element position vector. 
	 */
	public double[] getHydrophoneCoordinates(int iHydrophone, long timeMilliseconds) {
		Hydrophone h = getHydrophone(iHydrophone, timeMilliseconds);
		if (h == null) {
			return null;
		}
		return h.getCoordinates();
	}

	/**
	 * Get the hydrophone coordinate errors as a three element (x,y,z) vector. 
	 * <p> Hydrophone locators which want to vary this error as a function of 
	 * time should override this function and use the timeMilliseconds 
	 * parameter to get the true errors in their own way. 
	 * @param iHydrophone hydrophone number
	 * @param timeMilliseconds time of location
	 * @return three element error vector. 
	 */
	public double[] getHydrophoneCoordinateErrors(int iHydrophone, long timeMilliseconds) {
		Hydrophone h = getHydrophone(iHydrophone, timeMilliseconds);
		if (h == null) {
			return null;
		}
		return h.getCoordinateErrors();
	}

	/**
	 * Get a hydrophone coordinate 
	 * referenced to the nominal array position (GPS for towed systems, a fixed point, etc.)
	 * <p> Hydrophone locators which want to vary location as a function of 
	 * time should override this function and use the timeMilliseconds 
	 * parameter to get the true location of the hydrophones in their own way. 
	 * @param iHydrophone hydrophone number
	 * @param iCoordinate coordinate number (0,1,2 for x,y,z);
	 * @param timeMilliseconds time of location
	 * @return three element position vector. 
	 */
	public double getHydrophoneCoordinate(int iHydrophone, int iCoordinate,
			long timeMilliseconds) {
		return getHydrophoneCoordinates(iHydrophone, timeMilliseconds)[iCoordinate];
	}

	/**
	 * Get a hydrophone coordinate error 
	 * <p> Hydrophone locators which want to vary this error as a function of 
	 * time should override this function and use the timeMilliseconds 
	 * parameter to get the true errors in their own way. 
	 * @param iHydrophone hydrophone number
	 * @param iCoordinate coordinate number (0,1,2 for x,y,z);
	 * @param timeMilliseconds time of location
	 * @return three element error vector. 
	 */
	public double getHydrophoneCoordinateError(int iHydrophone,
			int iCoordinate, long timeMilliseconds) {
		return getHydrophoneCoordinateErrors(iHydrophone, timeMilliseconds)[iCoordinate];
	}

	/**
	 * Get a hydrophone x coordinate for a given time
	 * @param iHydrophone hydrophone number
	 * @param timeMilliseconds time in milliseconds
	 * @return hydrophone x coordinate. 
	 */
	public double getHydrophoneX(int iHydrophone, long timeMilliseconds) {
		return getHydrophoneCoordinate(iHydrophone, 0, timeMilliseconds);
	}

	/**
	 * Get a hydrophone y coordinate for a given time
	 * @param iHydrophone hydrophone number
	 * @param timeMilliseconds time in milliseconds
	 * @return hydrophone y coordinate. 
	 */
	public double getHydrophoneY(int iHydrophone, long timeMilliseconds) {
		return getHydrophoneCoordinate(iHydrophone, 1, timeMilliseconds);
	}

	/**
	 * Get a hydrophone z coordinate for a given time
	 * @param iHydrophone hydrophone number
	 * @param timeMilliseconds time in milliseconds
	 * @return hydrophone z coordinate. 
	 */
	public double getHydrophoneZ(int iHydrophone, long timeMilliseconds) {
		return getHydrophoneCoordinate(iHydrophone, 2, timeMilliseconds);
	}

	/**
	 * Get the preferred symbol for a given hydrophone. 
	 * @param iHydrophone hydrophone number
	 * @return Symbol to plot
	 */
	public PamSymbol getHydrophoneSymbol(int iHydrophone) {
		PamSymbol symbol = getHydrophone(iHydrophone).getSymbol();
		return symbol;
	}

	/**
	 * Called just before the array is serialised into Pam settings
	 * so it can get data out of the localisers, etc. 
	 */
	public void prepareToSerialize() {
		masterLocator.getLocatorSettings();
	}

	/**
	 * called just after the array settings have been loaded so that 
	 * data can be loaded back into the Hydrophone localisers.  
	 */
	public void arrayDeserialized() {
		masterLocator.setupLocators(this);
		masterLocator.setLocatorSettings(null);
	}

	/**
	 * @return the masterLocator
	 */
	protected MasterLocator getMasterLocator() {
		return masterLocator;
	}

	/**
	 * @return the originInterpolation
	 */
	public int getOriginInterpolation() {
		return originInterpolation;
	}

	/**
	 * @param originInterpolation the originInterpolation to set
	 */
	public void setOriginInterpolation(int originInterpolation) {
		this.originInterpolation = originInterpolation;
	}

	/**
	 * @return the hydrophoneInterpolation
	 */
	public int getHydrophoneInterpolation() {
		return hydrophoneInterpolation;
	}

	/**
	 * @param hydrophoneInterpolation the hydrophoneInterpolation to set
	 */
	public void setHydrophoneInterpolation(int hydrophoneInterpolation) {
		this.hydrophoneInterpolation = hydrophoneInterpolation;
	}

	/**
	 * Set the hydrophone preamplifier gain
	 * @param iPhone hydrophone index
	 * @param phoneGain preamplifier gain
	 * @return true if set successfully
	 */
	public boolean setHydrophoneGain(int iPhone, double phoneGain) {
		if (iPhone >= hydrophoneArray.size()) {
			return false;
		}
		hydrophoneArray.get(iPhone).setPreampGain(phoneGain);
		return true;
	}

	/**
	 * Set the hydrophone bandwidth
	 * @param iPhone hydrophone index
	 * @param phoneGain preamplifier bandwidth
	 * @return true if set successfully
	 */
	public boolean setHydrophoneFilter(int iPhone, double phoneFilter) {
		if (iPhone >= hydrophoneArray.size()) {
			return false;
		}
		double[] bw = hydrophoneArray.get(iPhone).getBandwidth();
		if (bw == null || bw.length != 2) {
			bw = new double[2];
		}
		bw[0] = phoneFilter;
		hydrophoneArray.get(iPhone).setBandwidth(bw);
		return true;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("streamers");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return streamers;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	/**
	 * Set all sensitivities on all hydrophones to a new value
	 * @param defaultRecieverSens - the new sensitivity to set in dB re 1V/uPa (units may differ depending on medium)
	 */
	public void setDefaultSensitivity(double defaultRecieverSens) {
		for (Hydrophone aHydrophone: hydrophoneArray) {
			aHydrophone.setSensitivity(defaultRecieverSens);
		}

	}
}
