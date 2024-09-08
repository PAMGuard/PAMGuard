package videoRangePanel;
import Array.ArrayManager;
import GPS.GpsDataUnit;
import PamUtils.LatLong;

/**
 * When taking photos users may have been at one location, on a boat, or at multiple different static locations. There are multiple ways PAMGUARD could provide position data, a user may upload a GPS track, or maybe a user wants to write a list of co-ordinates that can be selected for different photographs. 
 * The photos could even be geo-tagged. We need a manager for this which brings all these elements together and allows users to easily select exactly how they want to define their position. 
 * @author Jamie Macaulay
 *
 */
public class LocationManager {
	
	/**
	 * Flag to indicate no GPS data
	 */
	public final static int NO_GPS_DATA_FOUND=0;

	/**
	 * Goes through the array manager and determines a location of the 'boat'. This is the automatic method and will select GPS position from datablock or array manager input if static. 
	 */
	public final static int ARRAY_MANAGER=1;
	/**
	 * Look for a geo-tag in the current photo
	 */
	public final static int PHOTO_TAG=2;
	/**
	 * Gets GPS from a table or other source of manually inputted data. 
	 */
	public final static int MANUAL_INPUT=3;
	
	/**
	 *Get the GPS location from a landmark group. If doing this we assume that all the origin latlongs of the landmarks are the same. Hence just find the origin of the first landmark.   
	 */
	public final static int LANDMARK_GROUP=4;
	
	
	private int lastSearch=0;
	
	/**
	 * All current methods which can be selected by the user. Note: some vrMethods will override all other methods to determine location, e.g. the landmark method. These methods are not included in this array. 
	 */
	private final static int[] allMethods={ARRAY_MANAGER,PHOTO_TAG,MANUAL_INPUT};
	/**
	 * The manager searches for a location using multiple techniques in order of priority. If more than one available method of extracting a gps locationiis available then the method which is first in the list (has the highest priority) is used.
	 */
	private int[] currentMethods={PHOTO_TAG,MANUAL_INPUT,ARRAY_MANAGER};
		
	private VRControl vrControl;

	public LocationManager(VRControl vrControl){
		this.vrControl=vrControl; 
	}
	
	/**
	 * Get the priority of the location type. Returns -1 if location type is not included in list. 
	 * @param the location method
	 * @return the priority of this method. 0 means this method is tried first, 1 tried seconds etc. 
	 */
	public int getPriority(int type){
		for (int i=0; i<currentMethods.length; i++){
			if (currentMethods[i]==type) return i;
		}
		return -1;
	}
	
	/**
	 * Get the current array of methods for determining a location. The order the array indicates the priority methods should be attempted. 
	 * @return an array of the current methods selected by the user in order of priority. 
	 */
	public int[] getCurrentMethods(){
		return currentMethods;
	}
	
	/**
	 * Get the Location based on the set priority for methods. 
	 * @param timeMillis- time of GPS location
	 * @return latlong at timeMillis
	 */
	public LatLong getLocation(long timeMillis){
		LatLong latLong=null; 
		for (int i=0; i<currentMethods.length; i++){
			latLong=getGPSPoint(timeMillis, currentMethods[i]);
			if (latLong!=null){
				lastSearch=currentMethods[i];
				return latLong;
			}
		}
		lastSearch=NO_GPS_DATA_FOUND;
		return latLong;
	}
	
	public String getTypeString(int type){
		switch (type){
		case NO_GPS_DATA_FOUND:
			return "No GPS data";
		case ARRAY_MANAGER:
			return "GPS Datablock";
		case PHOTO_TAG:
			return "Image Geo-Tag";
		case MANUAL_INPUT:
			return  "GPS Table";
		case LANDMARK_GROUP:
			return "LandMark GPS";
		}
		return "No GPS data";
	}
	
	@Deprecated
	//TODO- hydrophone locator to be revamped soon-this function will need changed. 
	public LatLong searchGPSDataBlock(long timeMillis){
//		System.out.println("VideoRangePanel: LocationManager: Searching GPS datablock: "+ArrayManager.getArrayManager().getGPSDataBlock());
		GpsDataUnit gpsDataunit;
		ArrayManager.getArrayManager();
		if (ArrayManager.getGPSDataBlock()!=null){
			ArrayManager.getArrayManager();
			gpsDataunit=ArrayManager.getGPSDataBlock().getClosestUnitMillis(timeMillis);
			if (gpsDataunit!=null){
				return gpsDataunit.getGpsData();
			}
		}
		return null;
	}
	
	/**
	 * Returns the current GPS point. Note that the GPS can be null 
	 * @return
	 */
	private LatLong getGPSPoint(long timeMillis, int currentMethod){
		
		LatLong latLong=null; 
		switch (currentMethod){
		case ARRAY_MANAGER:
			// if using in relatime then get the most updated GPS point
			//TODO-will likely need changed soon with introduction of new latlong
			latLong=searchGPSDataBlock(timeMillis);
//			if (latLong==null) ArrayManager.getArrayManager().getCurrentArray().getFixedLatLong();
			try {
				if (latLong==null){
					latLong = ArrayManager.getArrayManager().getCurrentArray().getStreamer(0).getHydrophoneOrigin().getLastStreamerData().getGpsData();
				}
			}
			catch (Exception e) {
				System.out.println("Video Range location manager: Problem with hydrophone origin: " + e.getMessage());
			}
		break;
		case PHOTO_TAG:
			if (vrControl.getCurrentImage()==null) break;
			latLong=vrControl.getCurrentImage().getGeoTag();
		break;
		case MANUAL_INPUT:
			if (vrControl.getVRParams().getManualGPSDatas()==null) break;
			if (vrControl.getVRParams().getManualGPSDatas().size()==0) break;
			latLong=vrControl.getVRParams().getManualGPSDatas().get(vrControl.getVRParams().getCurrentManualGPSIndex()).getPosition();
		break;
		case LANDMARK_GROUP:
			if (vrControl.getVRParams().getLandMarkDatas()==null) break;
		break;
		}
		
		return latLong;
	}

	public int getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(int lastSearch) {
		this.lastSearch = lastSearch;
	}

	/**
	 * An array of all the methods
	 * @return
	 */
	public static int[] getAllMethods() {
		return allMethods;
	}

	public void setCurrentMethods(int[] currentMethods) {
		this.currentMethods=currentMethods;
		
	}


}
