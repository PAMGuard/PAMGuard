package Array.streamerOrigin;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataUnit;
import GPS.GpsData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.debug.Debug;


/**
 * Methods for getting streamer origins. This is now separated out from the HydrophoneLocator
 * interface - though that is also used and will in fact call back into the appropriate HydrophoneOriginMethod
 * to work out where the hell it is.This is to incorporate functionality for remote buoys, hydrophones on other
 * ships (from which we get AIS data), etc. The main thing that a HydrophoneOriginMethod has to do is to be 
 * able to provide an iterator to a list of data units which contain position heading and other relevant data
 * that the locators themselves can work through to get the array positions at a given time.  
 * @author Doug Gillespie
 *
 */
public abstract class HydrophoneOriginMethod implements Cloneable {

	protected Streamer streamer;

	protected PamArray pamArray;

	abstract public String getName();

	public HydrophoneOriginMethod(PamArray pamArray, Streamer streamer) {
		super();
		this.pamArray = pamArray;
		this.streamer = streamer;
	}

	@Override
	public String toString() {
		return getName();
	}


	/**
	 * Get a dialog panel component to include in the other settings configuration 
	 * dialogs. 
	 * @return Component to enter into a larger dialog
	 */
	public OriginDialogComponent getDialogComponent() {
		return null;
	}

	abstract public OriginSettings getOriginSettings();

	abstract public void setOriginSettings(OriginSettings originSettings);

	/**
	 * prepare the method - called from the controllers notify model changed or
	 * whenever anything else happens that might require some preparatory action
	 */
	abstract public boolean prepare();

	/**
	 * Get the latest streamer data unit. 
	 * <p>Origin methods MUST return something for this, even if they
	 * don't have any data units, they should just make one up from the 
	 * stored streamer data. 
	 * @return the latest streamer data unit. 
	 */
	abstract public StreamerDataUnit getLastStreamerData();

	/**
	 * Get the gps data closest to a particular time. <p>
	 * Note that some methods, might always opt for the preceeding, some 
	 * may take the closest. 
	 * @param timeMillis time in milliseconds
	 * @return closest gps data
	 */
	public GpsData getStreamerData(long timeMillis) {
		/*
		 *  First, check what type of origin information is required, if it's just 
		 *  the latest, then that's easy - return the latest. Otherwise, 
		 *  first look in the datablock to see if there is anything there,
		 *  and use that if at all possible, then if that fails, return the 
		 *  current position.  
		 */
		Object synchObj = getSynchronizationObject();
		//		Debug.out.println("Synch on " + synchObj.toString());
		/**
		 * This needs to synch on the above synchObject, which is almost definitely
		 * the GPS block (for towed data) and will also need to synch 
		 * Changes on 31/5/19 should mean that GPS and streamer data are synched together. 
		 */
		// synch off the streamer block as well as the other one (which will probably be GPS)
		synchronized (synchObj) {
			int originInterpolation = getPamArray().getOriginInterpolation();
			if (originInterpolation == PamArray.ORIGIN_USE_LATEST) {
				return toGpsData(getLastStreamerData());
			}

			OriginIterator originIterator = getGpsDataIterator(PamDataBlock.ITERATOR_END);

			GpsData preceedingUnit = originIterator.movePreceeding(timeMillis);
			if (originInterpolation == PamArray.ORIGIN_USE_PRECEEDING && preceedingUnit != null) {
				return preceedingUnit;
			}
			GpsData nextUnit = null;
			if (originIterator.hasNext()) {
				nextUnit = originIterator.next();
			}

			return GpsData.getAverage(preceedingUnit, nextUnit);
		} 
	}
	
	/**
	 * Get the gps data out of streamer data, but return null if the streamer data is null
	 * @param streamerDataUnit
	 * @return
	 */
	protected GpsData toGpsData(StreamerDataUnit streamerDataUnit) {
		return streamerDataUnit == null ? null : streamerDataUnit.getGpsData();
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected HydrophoneOriginMethod clone()  {
		try {
			return (HydrophoneOriginMethod) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get an iterator for stepping through gps data, but only selecting 
	 * from a particular streamer if more than one streamer / buoy is available. 
	 * @param wherefrom from end, start, etc. 
	 * @return an iterator, possibly containing special functionality to deal with data from specific streamers. 
	 */
	abstract public OriginIterator getGpsDataIterator(int wherefrom);

	abstract public Object getSynchronizationObject();

	/**
	 * @return the streamer
	 */
	protected Streamer getStreamer() {
		return streamer;
	}

	/**
	 * @return the pamArray
	 */
	protected PamArray getPamArray() {
		return pamArray;
	}

	/**
	 * Get allowable interpolation methods for this type of origin
	 * @return a bitmap of the:
	 * <br>PamArry.ORIGIN_USE_LATEST = 0;
	 * <br>PamArry.ORIGIN_INTERPOLATE = 1;
	 * <br>PamArry.ORIGIN_USE_PRECEEDING = 2;
	 */
	public int getAllowedInterpolationMethods() {
		return 0xFF;
	}

}
