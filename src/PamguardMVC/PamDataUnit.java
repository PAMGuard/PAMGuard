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
package PamguardMVC;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JPopupMenu;

import pamMaths.PamVector;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import binaryFileStorage.DataUnitFileInformation;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.HydrophoneLocator;
import Array.PamArray;
import Array.SnapshotGeometry;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamUtils.FrequencyFormat;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamSort;
import PamUtils.PamUtils;
import PamUtils.time.CalendarControl;
import PamguardMVC.datamenus.DataMenuParent;
import PamguardMVC.superdet.SubdetectionInfo;
import PamguardMVC.superdet.SuperDetection;

/**
 * @author Doug Gillespie
 *         <p>
 *         Class for units of PAM data.
 *         <p>
 *         PamDataUnit's are held in ArrayLists within PamDataBlocks.
 *         <p>
 *         When a PamDataUnit is added to a PamDataBlock any PamProcesses that
 *         subscribe to that PamDataBlock receive a notification and can retrieve the
 *         PamDataUnits from the block.
 *        <p>
 *        Any data derived from acoustic data should subclass from AcousticDataUnit
 *        <p> Types T is for subdetections, U is for super detections
 *        2021 note: T and U are no longer used since most types of super detection are 
 *        capable of holding many types of data unit and in any case, the Type of sub detection is held 
 *        in a subclass of PamDataUnit called SuperDetection and can be configured for that only. the Super Detection
 *        type U is now also always of a type derived from SuperDetection, though I daren't change it in the type 
 *        of PamDataUnit since doing so would break any plugins. So can probably live without useing them, but leave them 
 *        there unless planning on revising all plugin modules. 
 * 
 * @see PamguardMVC.PamDataBlock
 * @see AcousticDataUnit
 */
@SuppressWarnings("rawtypes")
abstract public class PamDataUnit<T extends PamDataUnit, U extends PamDataUnit> implements Comparable<PamDataUnit> {


	private DataUnitBaseData basicData;
	
	/**
	 * Absolute block index, needed for searches once 
	 * NPDU's start getting deleted off the front of the storage
	 */
	protected int absBlockIndex;
	
	/**
	 * Reference to parent data block
	 */
	private PamDataBlock<T> parentDataBlock;
	
//	public GpsDataUnit gpsDataUnit;


	
	/**
	 * Counter which increases if the data are altered and re-sent around PAMGUARD
	 */
	private int updateCount = 0;
	
	/**
	 * time of the last update
	 */
	private long lastUpdateTime = 0;
	
	/**
	 * Index of last entry into database - what will happen if the data
	 * are written into > 1 column ?
	 */
	private int databaseIndex;
	
	/**
	 * Index of any database unit that this updated. 
	 */
	private int databaseUpdateOf;
	
	/**
	 * Information about the binary file the data unit is stored in
	 * so that it can be resaved. 
	 */
	private DataUnitFileInformation dataUnitFileInformation;
	
	/**
	 * List of data annotations added to this data unit 
	 */
	private List<DataAnnotation> dataAnnotations = null;

	/**
	 * Localisation information
	 */
	protected AbstractLocalisation localisation = null;
	
	//	T subDetection; 
	//private Vector<T> subDetections;

	//	U superDetection;
	private Vector<SuperDetection> superDetections;

	private Object superDetectionSyncronisation = new Object();
	
	
	/**
	 * Force the next getAmplitude to recalculate the amplitude. 
	 */
	private boolean forceAmpRecalc = false;

	/**
	 * Flag to say that this is data under development. If this is set true
	 * then when the data are added to the datablock, they will not get saved
	 * to the binary store. They will get saved on the first update AFTER the 
	 * embryonic flag is set false. 
	 */
	private boolean embryonic = false;


	/**
	 * Old constructor using only time - everything has time !
	 * @param timeMilliseconds Time in standard milliseconds. 
	 */
	public PamDataUnit(long timeMilliseconds) {
		super();
		basicData = new DataUnitBaseData(timeMilliseconds, 0);
		lastUpdateTime = timeMilliseconds;
		this.parentDataBlock = null;
	}

	/**
	 * Constructor using the original parameters that have now been moved to DataUnitBaseData
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param startSample
	 * @param duration (number of samples, not milliseconds)
	 */
	public PamDataUnit(long timeMilliseconds,
			int channelBitmap, long startSample, long durationSamples) {
		this(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setStartSample(startSample);
		setSampleDuration(durationSamples);
	}

		/**
	 * Constructor using the new (Oct 2016) {@linkplain #PamguardMVC.DataUnitBaseData} DataUnitBaseData class which 
	 * speeds up and simplifies construction when reading data units
	 * from streams (Files, Network sockets, etc). 
	 * @param basicData 
	 */
	public PamDataUnit(DataUnitBaseData basicData) {
		this.basicData = basicData;
		lastUpdateTime = getTimeMilliseconds();
		this.parentDataBlock = null;
	}

	/**
	 * Set the data units unique identifier
	 * @param uid unique identifier
	 */
	public void setUID(long uid) {
		basicData.setUID(uid);
	}
	
	/**
	 * Get the data units unique identifier
	 * @return unique identifier
	 */
	public long getUID() {
		return basicData.getUID();
	}

	public void setAbsBlockIndex(int absBlockIndex) {
		this.absBlockIndex = absBlockIndex;
	}


	/**
	 * Set the datablock for a data unit. 
	 * @param parentDataBlock
	 */
	public void setParentDataBlock(PamDataBlock parentDataBlock) {
		this.parentDataBlock = parentDataBlock;
	}


	/**
	 * Set the millisecond time of the data unit using the 
	 * standard Java time system of milliseconds since midnight, January 1, 1970 UTC
	 * @param timeMilliseconds time in milliseconds. 
	 */
	public void setTimeMilliseconds(long timeMilliseconds) {
		basicData.setTimeMilliseconds(timeMilliseconds);
	}

	/**
	 * 
	 * @return The millisecond time of the data unit using the 
	 * standard Java time system of milliseconds since midnight, January 1, 1970 UTC
	 */
	public long getTimeMilliseconds() {
		return basicData.getTimeMilliseconds();
	}
	
	/**
	 * Returns the start sample stored in the basic data unit
	 * 
	 * @return
	 */
	public Long getStartSample() {
		return basicData.getStartSample();
	}
	
	/**
	 * Sets the start sample stored in the basic data unit
	 * @param startSample
	 */
	public void setStartSample(Long startSample) {
		basicData.setStartSample(startSample);
	}
	
	/**
	 * @return  the start time of the data unit in seconds relative to the start of the run.
	 */
	public double getSeconds() {
		return getStartSample() / getParentDataBlock().getSampleRate();
	}

	/**
	 * Gets the data unit duration, in samples
	 * 
	 * @return
	 */
	public Long getSampleDuration() {
		return basicData.getSampleDuration();
	}
	
	/**
	 * Returns the data unit duration as an int value.  If the duration is
	 * null, a -1 gets returned.  This is helpful for calls that use the
	 * duration as a way of initializing the size of an ArrayList
	 * @return
	 */
	public int getSampleDurationAsInt() {
		if (basicData.getSampleDuration()==null) {
			return -1;
		} else {
			return basicData.getSampleDuration().intValue();
		}
	}
	
	/**
	 * Returns the data unit duration in milliseconds, as a Float value.  
	 * 
	 * @return null if nothing available - see if this causes problems consider returning a float value of -1. 
	 */
	public Double getDurationInMilliseconds() {
		if (this.basicData.getMillisecondDuration() != null) {
			return basicData.getMillisecondDuration();
		}
		else if (getParentDataBlock() == null) {
			return null;
		}
		else if (getSampleDuration() != null && getParentDataBlock().getSampleRate() > 0.) {
			return (double) (getSampleDuration() / getParentDataBlock().getSampleRate() * 1000.);		
		}
		else {
			return null;
		}
	}
	
	public long getEndTimeInMilliseconds() {
		Double dur = getDurationInMilliseconds();
		long t = getTimeMilliseconds();
		if (dur != null) {
			t += (long) (double) dur;
		}
		return t;
	}
	
	/**
	 * Takes the passed duration, in milliseconds, and converts to a sample duration
	 * 
	 * @param durationMs
	 */
	public void setDurationInMilliseconds(double durationMs) {
		basicData.setMillisecondDuration(durationMs);
//		basicData.setSampleDuration((long) (durationMs/1000.*getParentDataBlock().getSampleRate()));
	}
	
	/**
	 * Sets the data unit duration, in samples
	 * @param duration
	 */
	public void setSampleDuration(Long duration) {
		basicData.setSampleDuration(duration);
	}
	
	/**
	 * get the last sample number in the data unit
	 * @return
	 */
	public long getLastSample() {
		return getStartSample() + getSampleDuration() - 1;
	}

	/**
	 * returns the time overlap of another unit on this unit - so if the other unit is 
	 * longer in time and completely covers this unit, the overlap is 1.0, if the 
	 * other unit is shorter or not aligned, then the overlap will be < 1.
	 * @param o Other PamDataUnit
	 * @return fractional overlap in time
	 */
	public double getTimeOverlap(PamDataUnit o) {
		if (o.getLastSample() < getStartSample()) {
			return 0;
		}
		else if (o.getStartSample() > getLastSample()) {
			return 0;
		}
		long oStart = Math.max(o.getStartSample(), getStartSample());
		long oEnd = Math.min(o.getLastSample(), getLastSample());
		return (double) (oEnd - oStart) / getSampleDuration();
	}
	
	
//	/**
//	 * Set the data unit's time in nanoseconds. Referenced to the same start
//	 * as the Java millisecond time (1/1/1970). 
//	 * @param timeNanoSeconds Time in nanoseconds. 
//	 */
//	public void setTimeNanoseconds(long timeNanoSeconds) {
//		timeMilliseconds = timeNanoSeconds / 1000000;
//	}
	
	/**
	 * Get the data unit's time in nanoseconds. Normally this is referenced to the same start
	 * as the Java millisecond time (1/1/1970) however there are times (in Viewer mode) 
	 * when the absolute time may be lost and it will only be useful as a relative measure. <p> 
	 * This should therefore only be used for calculations of time differences
	 * between data units and care should be taken to avoid overflows. Ideally
	 * you should subtract two nanosecond times as integers before converting to a
	 * floating point number. i.e. <br>
	 * double timeDiffSecs = (double) (nanoTime2 - nanoTime1) / 1.e9;<br>
	 * may not give the same result as <br>
	 * double timeDiffSecs = nanoTime2/1.e9 - nanoTime1/1.e9;
	 * @return time in nanoseconds. 
	 */
	public long getTimeNanoseconds() {
		return parentDataBlock.getNanoTimeCalculator().getNanoTime(this);
//		return timeMilliseconds * 1000000;
	}


	public int getAbsBlockIndex() {
		return absBlockIndex;
	}

	public PamDataBlock getParentDataBlock() {
		return parentDataBlock;
	}

	public void updateDataUnit(long updateTime) {
		updateCount++;
		this.lastUpdateTime = updateTime;
	}

	/**
	 * Do a clear of update count after a database save.
	 */
	public void clearUpdateCount() {
		updateCount = 0;
	}
	
	/**
	 * @return the number of times the data unit has been updated. 
	 */
	public int getUpdateCount() {
		return updateCount;
	}


	/**
	 * 
	 * @return Bitmap of software channels used in this data unit
	 */
	public int getChannelBitmap() {
		return basicData.channelBitmap;
	}


	/**
	 * Set the channel bitmap (software channels)
	 * @param channelBitmap
	 */
	public void setChannelBitmap(int channelBitmap) {
		basicData.channelBitmap = channelBitmap;
	}
	
	/**
	 * Get a sequence position within a datablock having multiple channels
	 * of data, multiple beams, etc. 
	 * @return integer position within the sequence. 
	 */
	public int getSequenceBitmap() {
		if (basicData.getSequenceBitmap() != null) {
			return basicData.getSequenceBitmap();
		}
		else {
			return getChannelBitmap();
		}
	}
	
	/**
	 * Set the sequence bitmap - the order of the data within 
	 * a data stream for beam formed data where the channelBitmap
	 * no longer contains a single unique channel. 
	 * @param sequenceBitmap
	 */
	public void setSequenceBitmap(Integer sequenceBitmap) {
		basicData.setSequenceBitmap(sequenceBitmap);
	}

	/**
	 * Get a sequence position within a datablock having multiple channels
	 * of data, multiple beams, etc. The difference between this method and
	 * getSequenceBitmap is that this will return a null if there is no sequence
	 * number, whereas the other method returns the channel map in that situation
	 * @return Integer position within the sequence, or null if there is no sequence 
	 */
	public Integer getSequenceBitmapObject() {
			return basicData.getSequenceBitmap();
	}
	
	
	/**
	 * This method will sort out this PamDataUnit's channel map and sequence map, depending on
	 * the source that it's getting it's information from.  It should typically be
	 * called when created, especially if the channel/sequence information is a subset of the
	 * source data block (such as when selected in a GroupSourcePanel object).  <br>
	 * There are 3 passed parameters: the source channel map, the source sequence map, and the
	 * subset map that this data unit should reference.  The local subset map may be the same as the
	 * source channel/sequence map, or it may only be a few of the source's channels or sequences. <br>  
	 * If the source has <em>no</em> sequence map (sourceSeqMap==null) then it's a normal FFT
	 * data block, and this PamDataUnit should be storing the local subset map in it's channelMap field and
	 * keeping it's sequenceMap field = null.<br>
	 * If the source <em>does</em> have a sequence map (i.e. the source is the output of a Beamformer),
	 * then this PamDataUnit should be storing the local subset map in it's sequenceMap field and storing
	 * the source channel map in it's channelMap field. 
	 * @param sourceChanMap
	 * @param sourceSeqMap
	 * @param localSubsetMap
	 */
	public void sortOutputMaps(int sourceChanMap, Integer sourceSeqMap, int localSubsetMap) {
		if (sourceSeqMap==null) {
			basicData.setSequenceBitmap(null);
			basicData.setChannelBitmap(localSubsetMap);
		} else {
			basicData.setSequenceBitmap(localSubsetMap);
			basicData.setChannelBitmap(sourceChanMap);
		}
	}
	
	

	/**
	 * Sets the original constructor parameters
	 * 
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param startSample
	 * @param duration
	 */
	public void setInfo(long timeMilliseconds,
			int channelBitmap, long startSample, long duration) {
		setTimeMilliseconds(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setStartSample(startSample);
		setSampleDuration(duration);
	}
	
/**
	 * 
	 * @return Bitmap of hydrophones used in this data unit which may 
	 * be different from the software channel mapping for some configurations. 
	 */
	public int getHydrophoneBitmap() {
		if (basicData.channelBitmap == 0) {
			return 0;
		}
		// will need to find a source process and get similar information as used in 
		// PamDataBlock.gethydrophoneMap.
		if (parentDataBlock == null) {
			return basicData.channelBitmap;
		}
		PamProcess parentProcess = parentDataBlock.parentProcess;
		if (parentProcess == null) {
			return basicData.channelBitmap;
		}
		PamProcess sourceProcess = parentProcess.getSourceProcess();
		if (sourceProcess == null) {
			return basicData.channelBitmap;
		}
		if (AcquisitionProcess.class.isAssignableFrom(sourceProcess.getClass())) {
			AcquisitionControl daqControl = ((AcquisitionProcess) sourceProcess).getAcquisitionControl();
			return daqControl.ChannelsToHydrophones(basicData.channelBitmap);
		}
		else {
			return basicData.channelBitmap;
		}
	}
	

	// return the reference position for this detection. 
	// Only ever calculate this one, unless recaluclate 
	// flag is set to true. 
//	protected GpsData oLL = null;
//	private double[] pairAngles = null;
//	private Double hydrophoneHeading = null;
	private SnapshotGeometry snapshotGeometry;
	
	/**
	 * Get the latlong of the mean hydrophone position at the time of 
	 * this detection. If the data unit has a channel bitmap of zero, then 
	 * get the GPS position of the vessel at that time. 
	 * @param recalculate
	 * @return Lat long of detection origin (usually the position of the reference hydrophone at time of detection)
	 */
	public GpsData getOriginLatLong(boolean recalculate) {
		if (recalculate) {
			snapshotGeometry = calcSnapshotGeometry();
		}
		if (getSnapshotGeometry() == null) {
			return null;
		}
		return snapshotGeometry.getCentreGPS();
//		if (oLL == null || recalculate) {
//			calcOandAngles();
//		}
//		return oLL;
	}
	
//	public void setOriginLatLong(GpsData oll) {
//		this.oLL = oll;
//	}


	/**
	 * Return the angle between pairs of hydrophones. For a n channel detection, 
	 * n-1 pair angles are calculated, each being the bearing, realtive to north, from the 
	 * pair+1th hydrophone TO the 0th hydrophone (i.e. for a 2 channel array, there is 
	 * one pair calculated and it's from channel 1 to channel 0, which is the most
	 * useful. If other inter-pair angles are required, then it should easy to
	 * calculate them from these values.   
	 * @param pair
	 * @param recalculate
	 * @return angle clockwise from North in degrees. 
	 */
//	public double getPairAngle(int pair, boolean recalculate) {
//		if (getSnapshotGeometry() == null) {
//			return Double.NaN;
//		}
//		
//		if (pairAngles == null || recalculate) {
//			calcOandAngles();
//		}
//		if (pairAngles.length > pair) {
//			return pairAngles[pair];
//		}
//		return Double.NaN;
//	}
	
	/**
	 * Get the hydrophone heading for the first hydrophone included in the 
	 * detection. this is now the reference heading from the snapshot geometry for the data unit.
	 * @param reacalculate force recalculation
	 * @return hydrophone heading
	 */
	public double getHydrophoneHeading(boolean recalculate) {
		if (getSnapshotGeometry() == null) {
			return Double.NaN;
		}
//		if (hydrophoneHeading == null || recalculate) {
//			calcHeadingandOrigin();
//		}
//		if (hydrophoneHeading != null) {
//			return hydrophoneHeading;
//		}
//		return Double.NaN;
		if (snapshotGeometry == null) {
			return Double.NaN;
		}
		GpsData refGps = snapshotGeometry.getReferenceGPS();
		if (refGps == null) {
			return Double.NaN;
		}
		
		return refGps.getHeading();
	}
	
//	/**
//	 * Do the actual calculation of hydrophone heading and 
//	 * it's position at the time of the detection. 
//	 */
//	public void calcHeadingandOrigin() {
//		// TODO Auto-generated method stub
//		int nPhones = PamUtils.getNumChannels(basicData.channelBitmap);
//		GpsData gpsData;
//		if (nPhones == 0) {
//			oLL = gpsData = getGpsPosition();
//			if (gpsData != null) {
//				hydrophoneHeading = gpsData.getHeading();
//			}
//			return;
//		}
//		ArrayManager arrayManager = ArrayManager.getArrayManager();
//		PamArray array = arrayManager.getCurrentArray();
//		HydrophoneLocator hydrophoneLocator = array.getHydrophoneLocator();
//		// turn channel numbers into hydrophone numbes.
//		// to do this we need the parameters from the acquisition process !
//		// if this is not available, then assume 1:1 mapping and get on with it.
//		AcquisitionProcess daqProcess;
//		AcquisitionParameters daqParams = null;
//		try {
//			daqProcess = (AcquisitionProcess) this.getParentDataBlock().getParentProcess().getSourceProcess();
//			daqParams = daqProcess.getAcquisitionControl().acquisitionParameters;
//		}
//		catch (Exception ex) {
//			daqProcess = null;
//			daqParams = null;
//		}
//		
//		int phone = PamUtils.getNthChannel(0, basicData.channelBitmap);
//		if (daqParams != null) {
//			phone = daqParams.getHydrophone(phone);
//		}
//		if (phone < 0) return;
//		// seems like we've found a hydrophone ...
//		LatLong phoneLatLong = hydrophoneLocator.getPhoneLatLong(getTimeMilliseconds(), phone);
//		hydrophoneHeading = hydrophoneLocator.getArrayHeading(getTimeMilliseconds(), phone);
//		
//	}

	public synchronized void clearOandAngles() {
		snapshotGeometry = null;
//		oLL = null;
//		hydrophoneHeading = null;
//		pairAngles = null;
	}
		
//	/**
//	 * Calculate the origin and angles of the array at the moment
//	 * of detection. 
//	 */
//	public void calcOandAngles() {
//		int nPhones = PamUtils.getNumChannels(basicData.channelBitmap);
//		if (nPhones == 0) {
//			oLL = getGpsPosition();
//			return;
//		}
//		
//		ArrayManager arrayManager = ArrayManager.getArrayManager();
//		PamArray array = arrayManager.getCurrentArray();
//		HydrophoneLocator hydrophoneLocator = array.getHydrophoneLocator();
//		// turn channel numbers into hydrophone numbers.
//		// to do this we need the parameters from the acquisition process !
//		// if this is not available, then assume 1:1 mapping and get on with it.
//		AcquisitionProcess daqProcess;
//		AcquisitionParameters daqParams = null;
//		try {
//			daqProcess = (AcquisitionProcess) this.getParentDataBlock().getParentProcess().getSourceProcess();
//			daqParams = daqProcess.getAcquisitionControl().acquisitionParameters;
//		}
//		catch (Exception ex) {
//			daqProcess = null;
//			daqParams = null;
//		}
//		int nChan = PamUtils.getNumChannels(basicData.channelBitmap);
//		pairAngles = new double[nChan - 1];
//		int phone;
//		double totalLat = 0, totalLong = 0, totalHeight = 0, totalHeading=0, totalPitch=0, totalRoll=0;
//		GpsData phoneLatLong = null;
//		LatLong firstLatLong = null;
//		int firstPhone;
//		for (int i = 0; i < nChan; i++) {
//			phone = PamUtils.getNthChannel(i, basicData.channelBitmap);
//			if (daqParams != null) {
//				phone = daqParams.getHydrophone(phone);
//			}
//			if (phone < 0) {
//				continue;
//			}
//			// seems like we've found a hydrophone ...
//			phoneLatLong = hydrophoneLocator.getPhoneLatLong(getTimeMilliseconds(), phone);
//			if (phoneLatLong == null) {
////				System.out.println("Can't find phone lat long for time " + 
////						PamCalendar.formatDateTime(getTimeMilliseconds()));
////				phoneLatLong = hydrophoneLocator.getPhoneLatLong(getTimeMilliseconds(), phone);
//				return;
//			}
//			if (i == 0) {
//				firstPhone = phone;
//				firstLatLong = phoneLatLong;
//			}
//			else{
//				
//				pairAngles[i-1] = phoneLatLong.bearingTo(firstLatLong);
////				pairAngles[i-1] = hydrophoneLocator.getPairAngle(getTimeMilliseconds(), phone, 
////						firstPhone, HydrophoneLocator.ANGLE_RE_NORTH);
//			}
//			// this code is NOT GOOD since it is averaging angles and will give mean of 359 and 1 is 180 !
//			totalLat += phoneLatLong.getLatitude();
//			totalLong += phoneLatLong.getLongitude();
//			totalHeight += phoneLatLong.getHeight();
//			totalHeading += phoneLatLong.getHeading();
//			totalPitch += phoneLatLong.getPitch();
//			totalRoll += phoneLatLong.getRoll();
//		}
//		if (nPhones == 0) {
//			// return the ship GPS for that time.
//			oLL = getGpsPosition();
//		}
//		else if (nPhones == 1) {
//			oLL = phoneLatLong;
//		}
//		else {
//			oLL = new GpsData(totalLat / nPhones, totalLong / nPhones, totalHeight / nPhones, totalHeading/nPhones, totalPitch/nPhones, totalRoll/nPhones, getTimeMilliseconds());
//		}
//	}
	/**
	 * Used when no hydrophone information is specified to get the nearest ships GPS position.
	 * @return GPS data closest to the time of the detection 
	 */
	protected GpsData getGpsPosition() {
		PamDataBlock<GpsDataUnit> gpsDataBlock = PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
		if (gpsDataBlock == null) return null;
		GpsDataUnit gpsDataUnit =  gpsDataBlock.getPreceedingUnit(getTimeMilliseconds());
		if (gpsDataUnit == null) { // get the first one anyway - it may be close enough !
			gpsDataUnit = gpsDataBlock.getFirstUnit();
		}
		if (gpsDataUnit == null) {
			return null;
		}
		return gpsDataUnit.getGpsData();
	}


	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	/**
	 * Get the last time that anything happened to this data unit - either 
	 * created or updated. 
	 * @return The greater of getLastUpdateTime() and getTimeMilliseconds()
	 */
	public long getLastChangeTime() {
		return Math.max(lastUpdateTime, basicData.getTimeMilliseconds());
	}
	

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}


	public int getDatabaseIndex() {
		return databaseIndex;
	}


	public void setDatabaseIndex(int databaseIndex) {
		this.databaseIndex = databaseIndex;
	}


	public int getDatabaseUpdateOf() {
		return databaseUpdateOf;
	}


	public void setDatabaseUpdateOf(int databaseUpdateOf) {
		this.databaseUpdateOf = databaseUpdateOf;
	}


	public DataUnitFileInformation getDataUnitFileInformation() {
		return dataUnitFileInformation;
	}


	public void setDataUnitFileInformation(
			DataUnitFileInformation dataUnitFileInformation) {
		this.dataUnitFileInformation = dataUnitFileInformation;
	}

	@Override
	public int compareTo(PamDataUnit o) {

		/**
		 * Can't just to minus since long might wrap when compared to 
		 * Integer. 
		 * So use the compareTo embedded in the Long class
		 * 30/11/2016 MO: added additional comparison of start sample, in case
		 * the data units have the same millisecond times
		 * If start sample doesn't work, use channel number, then UID. 
		 * One of those should work !
		 */
		int ans1 = Long.valueOf(getTimeMilliseconds()).compareTo(o.getTimeMilliseconds());
		if (ans1 == 0) {
			// nest as much as possible to minimise if statements if first go on millis worked
			if (basicData.getStartSample()!=null && o.getStartSample()!=null) {
				ans1 = basicData.getStartSample().compareTo(o.getStartSample());
			}
			if (ans1 == 0) {
				ans1 = this.getChannelBitmap() - o.getChannelBitmap();
				if (ans1 == 0) {
					/*
					 *  it would be pretty amazing if this were ever called. would
					 *  require two non acoustic objects in the same millisecond.
					 */					
					ans1 = Long.valueOf(this.getUID()).compareTo(o.getUID());
				}
			}
		}
		return ans1;
	}
	
	
	//localisation capability
	
	/**
	 * @return Returns the localisation.
	 */
	public AbstractLocalisation getLocalisation() {
		return localisation;
	}
	
	/**
	 * @param localisation The localisation to set.
	 */
	public void setLocalisation(AbstractLocalisation localisation) {
		this.localisation = localisation;
//		updateCount++;
	}
	
	/**
	 * Return an html formatted summary string
	 * describing the detection which can be 
	 * used in tooltips anywhere in PAMGuard. 
	 * @return summary string 
	 */
	public String getSummaryString() {
		String str = "<html>";
		str += "UID: " + getUID() + "<p>";
		if (parentDataBlock != null) {
			str += "<i>" + parentDataBlock.getLongDataName() + "</i><p>";
		}
//		str += PamCalendar.formatDateTime(timeMilliseconds) + "<p>";
		str += String.format("%s %s %s<p>", PamCalendar.formatDate(basicData.getTimeMilliseconds(), true),
				PamCalendar.formatTime(basicData.getTimeMilliseconds(), 3, true),
				CalendarControl.getInstance().getTZCode(true));
		if (CalendarControl.getInstance().isUTC() == false) {
			str += String.format("%s %s %s<p>", PamCalendar.formatDate(basicData.getTimeMilliseconds(), false),
					PamCalendar.formatTime(basicData.getTimeMilliseconds(), 3, false),
					"UTC");
		}
		if (basicData.channelBitmap > 0) {
			str += "Channels: " + PamUtils.getChannelList(basicData.channelBitmap) + "<p>";
		}
		if (databaseIndex > 0) {
			str += "Database Index : " + databaseIndex + "<p>";
		}
		Double duration = getDurationInMilliseconds();
		if (duration != null) {
			str += String.format("Duration %5.3fs<p>", duration / 1000.);
		}
		if (localisation != null) {
//			double[] angles = localisation.getAngles();
//			double bearingRef = localisation.getBearingReference();
			PamVector[]	worldVecs =	localisation.getWorldVectors();
			if (worldVecs != null && worldVecs.length > 0) {
				double angle = 90.-Math.toDegrees(Math.atan2(worldVecs[0].getElement(1), worldVecs[0].getElement(0)));
				angle = PamUtils.constrainedAngle(angle, 180);
				str += String.format("Angle %3.1f\u00B0", angle);
				if (worldVecs.length >= 2) {
					double angle2 = 90.-Math.toDegrees(Math.atan2(worldVecs[1].getElement(1), worldVecs[1].getElement(0)));
					angle2 = PamUtils.constrainedAngle(angle2, 180);
					if (Math.abs(angle2-angle) > 0.1) {
						str += String.format(" (or %3.1f\u00B0)", angle2);
					}
				}
				str += " re. Array<p>";
			}
			worldVecs = localisation.getRealWorldVectors();
			if (worldVecs != null && worldVecs.length > 0) {
				double angle = 90.-Math.toDegrees(Math.atan2(worldVecs[0].getElement(1), worldVecs[0].getElement(0)));
				angle = PamUtils.constrainedAngle(angle);
				str += String.format("Bearing %3.1f\u00B0", angle);
				if (worldVecs.length >= 2) {
					double angle2 = 90.-Math.toDegrees(Math.atan2(worldVecs[1].getElement(1), worldVecs[1].getElement(0)));
					angle2 = PamUtils.constrainedAngle(angle2);
					if (Math.abs(angle2-angle) > 0.1) {
						str += String.format(" (or %3.1f\u00B0)", angle2);
					}
				}
				str += " re. North<p>";
			}
		}

		String annotString = getAnnotationsSummaryString();
		if (annotString != null) {
			str += annotString;
		}
//		int nAttotations = getNumDataAnnotations();
//		for (int i = 0; i < nAttotations; i++) {
//			DataAnnotation an = getDataAnnotation(i);
//			DataAnnotationType ant = an.getDataAnnotationType();
//			String anName = ant.getAnnotationName();
//			String anString = an.toString();
//			if (anString == null) {
//				continue;
//			}
//			if (anString.contains("<html>")) {
//				anString = anString.replace("<html>", "");
//			}
//			if (anString.contains("</html>")) {
//				anString = anString.replace("</html>", "");
//			}
//			str += anName + ": " + anString + "<br>";
//		}

		
		// add frequency and amplitude information
		double[] frequency = this.getFrequency();
		if (frequency != null) {
			boolean allzeros = true;
			for (int i = 0; i < frequency.length; i++) {
				if (frequency[i] > 0) {
					allzeros = false;
				}
			}
			if (!allzeros) {
				str += "Frequency: " + FrequencyFormat.formatFrequencyRange(this.getFrequency(), true) + "<br>";
			}
		}
		if (getAmplitudeDB() != 0) {
			str += String.format("Amplitude: %3.1fdB<br>", getAmplitudeDB());
		}
		if (getSignalSPL() != null) {
			str += String.format("SPL: %3.1fdBre1uPa<br>",linAmplitudeToDB(getSignalSPL()));
		}
		if (getNoiseBackground() != null) {
			str += String.format("NSE: %3.1fdBre1uPa",linAmplitudeToDB(getNoiseBackground()));
			if (getSignalSPL() != null) {
				str += String.format("; SNR: %3.1fdB<br>", 20*Math.log10(getSignalSPL()/getNoiseBackground()));
			}
			else {
				str += "<br>";
			}
		}
		
		if (superDetections != null) {
			for (PamDataUnit sd:superDetections) {
				String sdString = sd.getSummaryString();
				if (sdString == null) {
					continue;
				}
				if (sdString.startsWith("<html>")) {
					sdString = sdString.substring(6);
				}
				sdString = "<b>Super detection</b> " + sdString;
				Object sdBlock = sd.getParentDataBlock();
				if (sdBlock != null) {
					str += "Grouped in " + sd.getParentDataBlock().getDataName() + "<br>";
				}
				str += sdString;
			}
		}

		return str;
	}
	
	/**
	 * Get string information for the annotations. Kept separate so 
	 * it can be called in overridden version of getSummaryString()
	 * @return
	 */
	public String getAnnotationsSummaryString() {
		int nAnnotations = getNumDataAnnotations();
		if (nAnnotations == 0) {
			return null;
		}
		String str = "";
		for (int i = 0; i < nAnnotations; i++) {
			DataAnnotation an = getDataAnnotation(i);
			DataAnnotationType ant = an.getDataAnnotationType();
			String anName = ant.getAnnotationName();
			String anString = an.toString();
			if (anString == null) {
				continue;
			}
			if (anString.contains("<html>")) {
				anString = anString.replace("<html>", "");
			}
			if (anString.contains("</html>")) {
				anString = anString.replace("</html>", "");
			}
			str += anName + ": " + anString + "<br>";
		}
		return str.length() > 0 ? str : null;
	}
	
	/**
	 * Some functions to do with data annotations
	 */
	/**
	 * Adds a data annotation to the data unit. 
	 * Removes any existing annotation of the same type and name. 
	 * @param dataAnnotation data annotation
	 */
	public void addDataAnnotation(DataAnnotation dataAnnotation) {
		if (dataAnnotations == null) {
			dataAnnotations = new ArrayList<DataAnnotation>();
		}
		else {
			DataAnnotation existingAnnotation = findDataAnnotation(dataAnnotation.getClass(),
					dataAnnotation.getDataAnnotationType().getAnnotationName());
			if (existingAnnotation != null) {
				dataAnnotations.remove(existingAnnotation);
			}
		}
		dataAnnotations.add(dataAnnotation);
		if (getParentDataBlock() != null) {
			getParentDataBlock().updatePamData(this, PamCalendar.getTimeInMillis());
		}
		else {
			setLastUpdateTime(PamCalendar.getTimeInMillis());
			updateCount++;
		}
		if (dataUnitFileInformation !=null) {
			dataUnitFileInformation.setNeedsUpdate(true);
		}
	}
	
	/**
	 * 
	 * @return the number of data annotations
	 */
	public int getNumDataAnnotations() {
		if (dataAnnotations == null) {
			return 0;
		}
		return dataAnnotations.size();
	}
	
	/**
	 * Get a data annotation. <p>No array size checking 
	 * so call getNumDataAnnotations first !
	 * @param index index of data annotation
	 * @return a data annotation. 
	 */
	public DataAnnotation getDataAnnotation(int index) {
		return dataAnnotations.get(index);
	}
	
	/**
	 * Find a data annotation that can cast to a certain type. 
	 * @param annotationClass class to search for
	 * @return data annotation or null
	 */
	public DataAnnotation findDataAnnotation(Class annotationClass) {
		if (dataAnnotations == null) {
			return null;
		}
		for (DataAnnotation an:dataAnnotations) {
			//Debug.out.println("PamDataUnit: Data Unit: " + this + " Annotation type: " + an); 
			if (annotationClass.isAssignableFrom(an.getClass())) {
				return an;
			}
		}
		return null;
	}
	
	/**
	 * Find a data annotation that can cast to a certain type, and that has a specific name. 
	 * @param annotationClass class to search for
	 * @return data annotation or null
	 */
	public DataAnnotation findDataAnnotation(Class annotationClass, String annotationName) {
		if (dataAnnotations == null) {
			return null;
		}
		for (DataAnnotation an:dataAnnotations) {
			if (annotationClass.isAssignableFrom(an.getClass())) {
				if (an.getDataAnnotationType().getAnnotationName().equals(annotationName)){
					return an;
				}
			}
		}
		return null;
	}
	
	/**
	 * Remove a data annotation
	 * @param index index of data annotation
	 * @return Reference to data annotation or null if it wasn't in the list
	 */
	public DataAnnotation removeDataAnnotation(int index) {
		if (dataAnnotations == null) {
			return null;
		}
		return dataAnnotations.remove(index);
	}
	
	/**
	 * Remove a data annotation
	 * @param annotation reference to data annotation to remove. 
	 * @return true if the annotation was included in the list. 
	 */
	public boolean removeDataAnnotation(DataAnnotation annotation) {
		if (dataAnnotations == null) {
			return false;
		}
		return dataAnnotations.remove(annotation);
	}

	/**
	 * @return the basicData
	 */
	public DataUnitBaseData getBasicData() {
		return basicData;
	}
	
	/**
	 * Add in this same capability for target motion analysis here.
	 */

	

	/**
	 * Sets a unique super detection. i.e. if the
	 * data unit already had a super detection of the 
	 * same class, then this data unit is removed from that
	 * pre-existing superdetection. 
	 * @param superDetection
	 */
	public int setUniqueSuperDetection(SuperDetection superDetection) {
		synchronized (superDetectionSyncronisation) {
			if (superDetections != null) {
				ListIterator<SuperDetection> superList = superDetections.listIterator();
				SuperDetection aSuper;
				while (superList.hasNext()) {
					aSuper = superList.next();
					if (aSuper == superDetection) {
						return 0; // they are the same !
					}
					if (aSuper.getClass() == superDetection.getClass()) {
						superList.remove();
						aSuper.removeSubDetection(this);
					}
				}
			}
			addSuperDetection(superDetection);
			return 1;
		}
	}

	public void addSuperDetection(SuperDetection superDetection) {
		/*
		 * Having a couple of problems with this in instances where data are scrolled, 
		 * but don't actually reload because another scroller is holding a wider range
		 * of data, however the superdets did reload - so are ending up with multiple 
		 * superdets being added to the same subdet. 
		 * So iterate through and remove any existing superdets that have the same UID and datablock
		 */
		synchronized (superDetectionSyncronisation) {
			if (superDetections == null) {
				superDetections = new Vector<SuperDetection>();
			}
			ListIterator<SuperDetection> iter = superDetections.listIterator();
			while (iter.hasNext()) {
				SuperDetection superDet = iter.next();
				if (superDet.getParentDataBlock() == superDetection.getParentDataBlock() && 
						superDet.getUID() == superDetection.getUID()) {
					iter.remove();
				}
			}
			if (superDetections.contains(superDetection) == false) {
				superDetections.add(superDetection);
			}
			if (parentDataBlock != null && PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
				/**
				 * for some reason, this was feeding around when loading viewer data and stopping if from linking
				 * data offline after the addition of the first sub detection. So have left for other modes, but 
				 * stopped this update in viewer mode. should load faster too.  
				 */
				// some subdet are (and need to be) added before they are in their 
				// own datablock for logging cross reference purposes
				parentDataBlock.updatePamData((T) this, System.currentTimeMillis());
			}
			//			superDetection.addSubDetection(this);
		}
	}
	
	public int getSuperDetectionsCount() {
		if (superDetections == null) return 0;
		return superDetections.size();
	}
	
	/**
	 * Find a super detection which is of or is assignable to a particular class type
	 * 
	 * @param superClass class or dub class of super detection
	 * @param includeSubClasses flag to say search for sub classes 
	 * @return super detection or null
	 */
	public SuperDetection getSuperDetection(Class superClass, boolean includeSubClasses) {
		if (includeSubClasses == false) {
			return getSuperDetection(superClass);
		}
		synchronized (superDetectionSyncronisation) {
			if (superDetections == null) return null;
			SuperDetection superDet;
			for (int i = 0; i < superDetections.size(); i++) {
				superDet = superDetections.get(i);
				if (superClass.isAssignableFrom(superDet.getClass())) {
					return superDet;
				}
			}
		}		
		return null;
	}
	
	/**
	 * Find a super detection of a given class. 
	 * @param superClass class of super detection
	 * @return found data unit or null
	 */
	public SuperDetection getSuperDetection(Class superClass) {
		synchronized (superDetectionSyncronisation) {
			if (superDetections == null) return null;
			SuperDetection superDet;
			for (int i = 0; i < superDetections.size(); i++) {
				superDet = superDetections.get(i);
				if (superDet.getClass() == superClass) {
					return superDet;
				}
			}
		}		
		return null;
	}
	
	/**
	 * find a super detection form the parent data block of the super detection. 
	 * @param superDataBlock data block of super detection
	 * @return data unit from that block, or null.
	 */
	public SuperDetection getSuperDetection(PamDataBlock superDataBlock) {
		synchronized (superDetectionSyncronisation) {
			if (superDetections == null) return null;
			SuperDetection superDet;
			for (int i = 0; i < superDetections.size(); i++) {
				superDet = superDetections.get(i);
				if (superDet.getParentDataBlock() == superDataBlock) {
					return superDet;
				}
			}
		}		
		return null;
	}

	/**
	 * find a super detection form the parent data block of the super detection. 
	 * @param superDataBlock data block of super detection
	 * @param allowSuperSuper Allow iteration through mutilple super detection layers
	 * @return data unit from that block, or null.
	 */
	public SuperDetection getSuperDetection(PamDataBlock superDataBlock, boolean allowSuperSuper) {
		synchronized (superDetectionSyncronisation) {
			if (superDetections == null) return null;
			SuperDetection superDet;
			for (int i = 0; i < superDetections.size(); i++) {
				superDet = superDetections.get(i);
				if (superDet.getParentDataBlock() == superDataBlock) {
					return superDet;
				}
				if (allowSuperSuper) {
					SuperDetection supersuper = superDet.getSuperDetection(superDataBlock, allowSuperSuper);
					if (supersuper != null) {
						return supersuper;
					}
				}
			}
		}		
		return null;
	}

	public SuperDetection getSuperDetection(int ind) {
		synchronized (superDetectionSyncronisation) {
			if (superDetections == null || superDetections.size()<=ind) return null;
			return superDetections.get(ind);
		}
	}


	public void removeSuperDetection(SuperDetection superDetection) {
		if (superDetections == null) {
			return;
		}
		synchronized (superDetectionSyncronisation) {
			if (superDetections.remove(superDetection)) {
				//System.out.println("Remove super detection: " + superDetection + " scount: " + this.getSuperDetectionsCount());
				parentDataBlock.updatePamData((T) this, System.currentTimeMillis());
			}
		}
	}

	public Object getSuperDetectionSyncronisation() {
		return superDetectionSyncronisation;
	}
	
	/**
	 * gets the frequency limits from the DataUnitBaseData object
	 * @return
	 */
	public double[] getFrequency() {
		return basicData.getFrequency();
	}
	
	/**
	 * Set the frequency limits in the DataUnitBaseData object using
	 * a double[] array.
	 * @param freq
	 */
	public void setFrequency(double[] freq) {
		basicData.setFrequency(freq);
	}
	
	/**
	 * Signal excess is the maximum of the ratio of the detection 
	 * statistic in dB. 
	 * @return the signalExcess
	 */
	public float getSignalExcess() {
		return basicData.getSignalExcess();
	}

	/**
	 * Signal excess is the maximum of the ratio of the detection 
	 * statistic in dB. 
	 * @param signalExcess the signalExcess to set
	 */
	public void setSignalExcess(float signalExcess) {
		basicData.setSignalExcess(signalExcess);
	}
	
	public void setNoiseBackground(Float noiseBackground) {
		basicData.setNoiseBackground(noiseBackground);
	}
	
	public Float getNoiseBackground() {
		return basicData.getNoiseBackground();
	}

	public void setSignalSPL(Float signalSPL) {
		basicData.setSignalSPL(signalSPL);
	}
	
	public Float getSignalSPL() {
		return basicData.getSignalSPL();
	}
	
	/**
	 * gets the array of time delays, measured in seconds
	 * @return
	 */
	public double[] getTimeDelaysSeconds() {
		return basicData.getTimeDelaysSeconds();
	}
	
	/**
	 * Sets the time delays, in seconds
	 * @param td
	 */
	public void setTimeDelaysSeconds(double[] td) {
		basicData.setTimeDelaysSeconds(td);
	}
	
	/**
	 * returns the frequency overlap of another unit on this unit - so if the other unit is 
	 * longer in time and completely covers this unit, the overlap is 1.0, if the 
	 * other unit is shorter or not aligned, then the overlap will be < 1.
	 * @param o Other AcousticDataUnit
	 * @return fractional overlap in time
	 */
	public double getFrequencyOverlap(PamDataUnit<?, ?> o) {
		if (getFrequency() == null || getFrequency().length < 2) {
			return 0;
		}
		else if (o.getFrequency() == null || o.getFrequency().length < 2) {
			return 0;
		}
		if (o.getFrequency()[1] < getFrequency()[0]) {
			return 0;
		}
		else if (o.getFrequency()[0] > getFrequency()[1]) {
			return 0;
		}
		double oStart = Math.max(o.getFrequency()[0], getFrequency()[0]);
		double oEnd = Math.max(o.getFrequency()[1], getFrequency()[1]);
		return  (oEnd - oStart) / (getFrequency()[1] - getFrequency()[0]);
	}

	// *******************************************************************
	// Amplitude calculation methods, moved from AcousticDataUnit
	// *******************************************************************
	
	/**
	 * @return Returns the measuredAmplitude.
	 */
	public double getMeasuredAmplitude() {
		return basicData.getMeasuredAmplitude();
	}
	
	/**
	 * @return Returns the measuredAmplitudeType.
	 */
	public int getMeasuredAmplitudeType() {
		return basicData.getMeasuredAmplitudeType();
	}
	
	/**
	 * 
	 * @param measuredAmplitude
	 */
	public void setMeasuredAmplitude(double measuredAmplitude) {
		basicData.setMeasuredAmplitude(measuredAmplitude);
	}
	
	/**
	 * 
	 * @param measuredAmplitudeType
	 */
	public void setMeasuredAmplitudeType(int measuredAmplitudeType) {
		basicData.setMeasuredAmplitudeType(measuredAmplitudeType);
	}

	/**
	 * Sets both the measured amplitude, as well as the type of measurement
	 * (as defined by the DataUnitBaseData constants)
	 * 
	 * @param measuredAmplitude The measuredAmplitude to set.
	 * @param measuredAmplitudeType The type of amplitude measurement
	 */
	public void setMeasuredAmpAndType(double measuredAmplitude, int measuredAmplitudeType) {
		basicData.setMeasuredAmplitude(measuredAmplitude);
		basicData.setMeasuredAmplitudeType(measuredAmplitudeType);
	}

	/**
	 * Returns the calculated amplitude in dB, stored in the base data
	 * 
	 * @return
	 */
	public double getCalculatedAmlitudeDB() {
		return basicData.getCalculatedAmlitudeDB();                                                          
	}

	/**
	 * sets the calculated amplitude in dB, stored in the base data
	 * @param calculatedAmlitudeDB
	 */
	public void setCalculatedAmlitudeDB(double calculatedAmlitudeDB) {
		basicData.setCalculatedAmlitudeDB(calculatedAmlitudeDB);
	}

	/**
	 * Amplifies the measured amplitude stored in the base unit by the gain (in dB).  Make
	 * sure that the passed gain value is consistent with the measuredAmplitudeType
	 * parameter
	 * 
	 * @param gaindB the gain factor, in dB
	 */
	public void amplifyMeasuredAmplitudeByDB(double gaindB) {
		switch (getMeasuredAmplitudeType()) {
		case DataUnitBaseData.AMPLITUDE_SCALE_DBREMPA:
//		case DataUnitBaseData.AMPLITUDE_SCALE_SPECTRUM:
			setMeasuredAmplitude(getMeasuredAmplitude() + gaindB);
			break;
		case DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD:
			setMeasuredAmplitude(getMeasuredAmplitude() * Math.pow(10, gaindB/20));
		}
	}
	
	/**
	 * Amplifies the measured amplitude stored in the base unit by the linear gain.  Make
	 * sure that the passed gain value is consistent with the measuredAmplitudeType
	 * parameter
	 * 
	 * @param gain the linear gain factor
	 */
	public void amplifyMeasuredAmplitudeByLinear(double gain) {
		gain = Math.abs(gain);
		switch (getMeasuredAmplitudeType()) {
		case DataUnitBaseData.AMPLITUDE_SCALE_DBREMPA:
//		case DataUnitBaseData.AMPLITUDE_SCALE_SPECTRUM:
			setMeasuredAmplitude(getMeasuredAmplitude() + 20 * Math.log10(gain));
			break;
		case DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD:
			setMeasuredAmplitude(getMeasuredAmplitude()*gain);
		}
	}

	/**
	 * Get the calculated amplitude, in dB. If it hasn't been calculated yet, do
	 * that first and then return the value
	 * 
	 * @return the amplitude in dB with reference unit dictated by the
	 *         hydrophone/microphone sensitivity value units.
	 */
	public double getAmplitudeDB() {
				
		/*
		 * try to be a bit swish here and see if the amplitude calculation has already
		 * been done, redoing it if necessary.
		 */
		if (!Double.isNaN(getCalculatedAmlitudeDB()) && !isForceAmpRecalc()) {
			return getCalculatedAmlitudeDB();
		} 
		else {
			setCalculatedAmlitudeDB(calculateAmplitudeDB());
			setForceAmpRecalc(false);
			return getCalculatedAmlitudeDB();
		}
	}

	/**
	 * Calculate the amplitude in dB and return the value
	 * 
	 * @return
	 */
	private double calculateAmplitudeDB() {
		switch (getMeasuredAmplitudeType()) {
		case DataUnitBaseData.AMPLITUDE_SCALE_DBREMPA:
			return getMeasuredAmplitude();
		case DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD:
			return linAmplitudeToDB(getMeasuredAmplitude()); // some calculation !
//		case DataUnitBaseData.AMPLITUDE_SCALE_SPECTRUM:
//			return 1; // some other calculation				
		}
		return getCalculatedAmlitudeDB();
	}
	
	/**
	 * calculate the amplitude in dB from the measured linear amplitude
	 * @param linamp
	 * @return
	 */
	public double linAmplitudeToDB(double linamp) {
		/*
		 * Need to find the acquisition process for these data
		 */
		if (getParentDataBlock() == null) {
			return Double.NaN;
		}
		PamProcess daqProcess = getParentDataBlock().getSourceProcess();
		if (daqProcess == null) {
			return 0;
		}
		if (AcquisitionProcess.class.isAssignableFrom(daqProcess.getClass())) {
			AcquisitionProcess ap = (AcquisitionProcess) daqProcess;
			return ap.rawAmplitude2dB(linamp, PamUtils.getLowestChannel(getChannelBitmap()), false);
		}
		return 0;
	}
	

	/**
	 * Check whether force amplitude calculation is set. This will force an amplitude 
	 * recalculation on calling getAmpliudeDB. After a new amplitude is calculated this 
	 * is automatically reset to false.
	 * @return true if force amplitude is set. 
	 */
	public boolean isForceAmpRecalc() {
		return forceAmpRecalc;
	}
	/**
	 * Set whether force amplitude calculation is set. This will force an amplitude 
	 * recalculation on calling getAmpliudeDB. After a new amplitude is calculated this 
	 * is automatically reset to false.
	 * @param forceAmpRecalc - true if force amplitude is set. 
	 */
	public void setForceAmpRecalc(boolean forceAmpRecalc) {
		this.forceAmpRecalc = forceAmpRecalc;
	}



	public int getSuperId(Class<OfflineEventDataUnit> superClass) {
		PamDataUnit superDet = getSuperDetection(superClass);
		if (superDet == null) {
			return 0;
		}
		else {
			return superDet.getDatabaseIndex();
		}
	}

	/**
	 * Free as much data as possible. this gets called when processing offline
	 * data and during datagramming to free any data that's not needed in memory
	 * such as derived spectral data (e.g. from a click waveform) which can 
	 * be recalculated if necessary. 
	 */
	public void freeData() {		
	}

	/**
	 * Get a standard popup menu with options for this data unit. PAsses the call through to
	 * the datablock, using a function that can handle multiple data units, but for convenience, 
	 * it's often easier to start here. Override at will, but best to put as much functionality 
	 * as possible into PamDataBlock.getDataUnitPopupMenu(..) and PamDataBlock.getDataUnitMenuItems(...).
	 * <p> the standard menu item list contains annotation options (if available) so good to start with that and
	 * add to it 
	 * @param menuParent 
	 * @param mousePosition
	 * @return popup menu or null (mostly null!)
	 */
	public JPopupMenu getDataUnitPopupMenu(DataMenuParent menuParent, Point mousePosition) {
		if (getParentDataBlock() == null) {
			return null;		
		}
		else {
			return getParentDataBlock().getDataUnitPopupMenu(menuParent, mousePosition, this);
		}
	}

	/**
	 * Get a snapshot of the array geometry at the time of this detection for the 
	 * channels it uses. 
	 * @return
	 */
	public synchronized SnapshotGeometry getSnapshotGeometry() {
		if (snapshotGeometry == null) {
			snapshotGeometry = calcSnapshotGeometry();
		}
		return snapshotGeometry;
	}
	
	public SnapshotGeometry calcSnapshotGeometry() {
		return ArrayManager.getArrayManager().getSnapshotGeometry(getHydrophoneBitmap(), getTimeMilliseconds());
	}

	/**
	 * @param snapshotGeometry the snapshotGeometry to set
	 */
	public void setSnapshotGeometry(SnapshotGeometry snapshotGeometry) {
		this.snapshotGeometry = snapshotGeometry;
	}

	/**
	 * Get a colour id. this can be pretty much anything and will 
	 * be scaled, looped, to fit in the range of whale id colours. 
	 * Mostly used for superdetection display, but can be used by 
	 * anything. 
	 * @return any integer.
	 */
	public int getColourIndex() {
		/*
		 * This can go wrong when UID > 2^31 since the colour chooser takes 
		 * a mod WRT number of whale colours and it doesn't like negative numbers. 
		 * So need to keep the value going in positive. 
		 */
		long uid = getUID();
		uid &= 0x7FFFFFFF; // avoid anything in top bit of an int32 or higher
		return (int) uid;
	}

	/**
	 * @return the embryonic
	 */
	public boolean isEmbryonic() {
		return embryonic;
	}

	/**
	 * @param embryonic the embryonic to set
	 */
	public void setEmbryonic(boolean embryonic) {
		this.embryonic = embryonic;
	}
}
