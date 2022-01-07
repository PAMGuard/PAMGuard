package difar.trackedGroups;

import java.util.ArrayList;
import java.util.Vector;

import difar.DifarDataUnit;
import Array.ArrayManager;
import Array.SnapshotGeometry;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamUtils.BearingMean;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * @author Brian Miller
 * Each data unit represents a group of whales tracked via the DIFAR module.
 * The data unit will contain summary information regarding the track, such as
 * first detection time, total number of detections average bearing, 
 * most recent bearing. 
 *
 */
@SuppressWarnings("rawtypes")
public class TrackedGroupDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements PamDetection {

	/**
	 * Name of the tracked group
	 */
	private String groupName;
	
	/**
	 * The sonobuoy channel (i.e. not the bitMap, but 0 for channel 0)
	 */
	private int channel;
	
	/**
	 * The time of deployment of the sonobuoy
	 */
	private long buoyStartTime;
	private String buoyName;	
	
	/**
	 * Each bearing has an angle and a time
	 * @author brian_mil
	 *
	 */
	private ArrayList<Double> bearings;
	private ArrayList<Long> bearingTimes;

	/**
	 * Class for computing circular statistics
	 */
	private BearingMean meanBearing;
	
	/**
	 * The mean of all the bearings to this group from this buoy
	 */
	private Double bearingMean;
	
	/**
	 * The standard deviation of all the bearings to this group from this buoy
	 */
	private Double bearingStd;
	
	
	/**
	 * Bearing of the very first bearing to this group on this buoy
	 */
	private Double firstBearing;
	
	/**
	 * Time of the very first detection of this group on this buoy
	 */
	private long firstDetectionTime;
	
	/**
	 * Bearing of the most recent detection of this group on this buoy
	 */
	private Double mostRecentBearing;
	
	/**
	 * Time of the most recent detection of this group on this buoy
	 */
	private long mostRecentDetectionTime = Long.MIN_VALUE;

	/**
	 * Number of detections for this group (on this buoy)
	 */
	private int numBearings;
	
	/**
	 * Triangulated position of group based on all bearings
	 */
	private TrackedGroupCrossingInfo crossingInfo;
	
	/**
	 * Localisation info almost identical to DifarLocalisation
	 */
	//	private TrackedGroupLocalisation localisation;
	
	/**
	 * Create a new trackedGroup data unit from an existing difarDataUnit
	 * @param difarDataUnit
	 */
	public TrackedGroupDataUnit(DifarDataUnit difarDataUnit){
		super(difarDataUnit.getClipStartMillis(), difarDataUnit.getChannelBitmap(), 
				difarDataUnit.getStartSample(), 0);
		
		channel = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
		setFirstDetectionTime(difarDataUnit.getTimeMilliseconds());
		setFirstBearing(difarDataUnit.getTrueAngle());
		mostRecentDetectionTime = getFirstDetectionTime();
		groupName = difarDataUnit.getTrackedGroup();
		bearings = new ArrayList<Double>();
		bearingTimes = new ArrayList<Long>();
		bearings.add(difarDataUnit.getTrueAngle());
		bearingTimes.add(difarDataUnit.getTimeMilliseconds());
		numBearings = 1;
		setLocalisation(new TrackedGroupLocalisation(this, LocContents.HAS_BEARING, getChannelBitmap()));
		updateGroupStats();
		setSonobuoy();
	}
	
	/**
	 * Create a new TrackedGroupDataUnit from the SQL database
	 * @param bearingSTD 
	 * @param difarDataUnit
	 */
	 
	public TrackedGroupDataUnit(long timeMilliseconds, int channelBitmap,
			String groupName, double firstBearing,	double lastBearing, long lastDetectionTime, 
			double meanBearing, double bearingSTD, int n){
		super(timeMilliseconds, channelBitmap, 0, 0);
		this.groupName = groupName;
		channel = PamUtils.getSingleChannel(channelBitmap);
		setFirstBearing(firstBearing);
		setFirstDetectionTime(timeMilliseconds);
		mostRecentDetectionTime = lastDetectionTime;
		bearings = new ArrayList<Double>();
		bearingTimes = new ArrayList<Long>();
		mostRecentBearing = lastBearing;
		bearingMean = meanBearing;
		bearingStd = bearingSTD;
		numBearings = n;
		setLocalisation(new TrackedGroupLocalisation(this, LocContents.HAS_BEARING, channelBitmap));
		setSonobuoy();
	}
	
	
	/**
	 * Set the origin from the streamer. Sonobuoys will always use
	 *  the streamerDataUnit that precedes the start time of the track
	 */
	private void setSonobuoy(){
		// get the geometry - check there is one for this buoy at this time 
		SnapshotGeometry geom = getSnapshotGeometry();
		// if one wasn't found, then try a few other things. 
		if (geom == null) {
			geom = ArrayManager.getArrayManager().getSnapshotGeometry(getChannelBitmap(), PamCalendar.getSessionStartTime());
		}
		if (geom == null) {
			geom = ArrayManager.getArrayManager().getSnapshotGeometry(0, PamCalendar.getSessionStartTime());
		}
		setSnapshotGeometry(geom);
		
//		PamCalendar.formatDateTime(getFirstDetectionTime());
//		StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getPreceedingUnit(getFirstDetectionTime(), getChannelBitmap());
//		if (sdu==null){
//			System.out.println("DIFAR Tracked Groups cannot finds streamer data unit (Sonobuoy deployment data)");
//			// so try to find a unit from the start of this time. 
//			sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getPreceedingUnit(PamCalendar.getSessionStartTime(), getChannelBitmap());
//		}
//		if (sdu == null) {
//			
//		}
//			buoyName = ArrayManager.getArrayManager().getCurrentArray().getStreamer(channel).getStreamerName();
//			buoyStartTime = PamCalendar.getSessionStartTime();
//			getSnapshotGeometry(); // should do it  since channel  / channel bitmap are all matched up correctly. it will end up at same place as was on next line. 
////			this.oLL = ArrayManager.getArrayManager().getCurrentArray().getStreamer(channel).getHydrophoneLocator().getPhoneLatLong(buoyStartTime, channel);
//		} else {
//			buoyName = sdu.getStreamerData().getStreamerName();
//			buoyStartTime = sdu.getTimeMilliseconds();
//			this.oLL = sdu.getOriginLatLong(false);
//		}
	}
	
	/**
	 * Add a new bearing to this group. Check if its the most recent and
	 * update accordingly. Also, recompute the mean bearing.
	 * 
	 * @param difarDataUnit
	 */
	public void addData(DifarDataUnit difarDataUnit){
		bearings.add(difarDataUnit.getTrueAngle());
		bearingTimes.add(difarDataUnit.getTimeMilliseconds());
		numBearings = bearings.size();
		updateMostRecentBearing(difarDataUnit);
		updateGroupStats();
		updateDataUnit(difarDataUnit.getTimeMilliseconds());
	}
	
	public void updateMostRecentBearing(DifarDataUnit difarDataUnit){
		if (mostRecentDetectionTime < difarDataUnit.getTimeMilliseconds()){
			mostRecentDetectionTime = difarDataUnit.getTimeMilliseconds();
			mostRecentBearing = difarDataUnit.getTrueAngle();
		}
	}

	/**
	 * Compute the circular mean and standard deviation
	 * of all the bearings added to this group
	 * @return
	 */
	private void updateGroupStats(){
		int n = getNumBearings();
		double[] bearings = new double[n];
		for (int i = 0; i < n; i++){
			bearings[i] = (double) this.bearings.get(i);
		}
		meanBearing = new BearingMean(bearings);
		bearingMean = meanBearing.getBearingMean();
		bearingStd = meanBearing.getBearingSTD2();
		
		/*
		 * If there are only a few bearings, then use 10 degrees 
		 * as the standard deviation. For DIFAR buoys, 95% of bearings 
		 * should be within +/- 10 degrees of the actual bearing, and  
		 * if the compass is calibrated, then mean bearing should be 
		 * pretty close to the actual bearing.
		 */
		if (n < 3){ 
			bearingStd = 10D;
		}
		
		double angleErr[] = {Math.toRadians(2 * bearingStd)};
		((TrackedGroupLocalisation) this.getLocalisation()).setAngleErrors(angleErr);
	}
	
	@Override
	public String getSummaryString() {
		String str = "<html>";
		str += String.format("%s (%d detections on buoy %s)", groupName, getNumBearings(), getBuoyName());
		str += String.format("\n<br>Mean bearing: %3.0f�, Std: %3.0f�",  getMeanBearing(), getBearingSTD());
		str += String.format("\n<br>\tFirst detected: %s, (%3.0f�)", PamCalendar.formatDateTime(getFirstDetectionTime()), firstBearing);
		str += String.format("\n<br>\tLast detected: %s, (%3.0f�)", PamCalendar.formatDateTime(getMostRecentDetectionTime()), mostRecentBearing);
		str += "</html>";
		return str;
	}

	public String getGroupName(){
		return groupName;
	}
	
	public String getBuoyName(){
		return buoyName;
	}

	double getFirstBearing(){
		return firstBearing;
	}
	
	void setFirstBearing(Double angle) {
		firstBearing = angle;
	}

	public long getFirstDetectionTime() {
		return firstDetectionTime;
	}
	public void setFirstDetectionTime(long timeMillis) {
		this.firstDetectionTime = timeMillis;
	}
	
	/**
	 * The time of the most recent bearing to this group
	 * @return
	 */
	public long getMostRecentDetectionTime(){
		return mostRecentDetectionTime;
	}
	
	public double getMostRecentBearing() {
		if (mostRecentBearing == null) {
			mostRecentBearing = -999D;
		}
		return mostRecentBearing;
	}

	/**
	 * The circular mean of all of the bearings for this group
	 * @return
	 */
	public double getMeanBearing(){
		return bearingMean;
	}
	
	public double getBearingSTD() {
		return bearingStd;
	}

	public long getBuoyStartTime() {
		return buoyStartTime;
	}


	/**
	 * @return The total number of bearings for this group on this sonobuoy
	 */
	public int getNumBearings(){
		return numBearings;
	}

	public TrackedGroupCrossingInfo getDifarCrossing() {
		return crossingInfo;
	}

	public void setCrossing(TrackedGroupCrossingInfo crossInfo) {
		crossingInfo = crossInfo;
	}


}