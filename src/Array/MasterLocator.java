package Array;

import java.io.Serializable;
import java.lang.reflect.Field;

import GPS.GpsData;
import PamController.PamControllerInterface;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Master PAMGuard array localiser. There is now one of these and 
 * of no other type within each PamArray and this will call out 
 * to different Localisers dependent on which channel is being used. 
 * @author Doug Gillespie
 *
 */
public class MasterLocator extends HydrophoneLocator implements Serializable, Cloneable, ManagedParameters {

	private static final long serialVersionUID = 1L;

	private PamArray pamArray;

	private int[] phoneToStreamerLUT;

	/**
	 * Individual locators will not be serialised. 
	 */
//	transient private HydrophoneLocator[] streamerLocators;

//	private ArrayList<LocatorSettings> locatorSettingsList; /// we want to serialise this !

	public MasterLocator(PamArray pamArray) {
		super(pamArray, null);
		this.pamArray = pamArray;
	}

	private int phoneToStreamer(int iPhone) {
		if (phoneToStreamerLUT == null) {
			setupLocators(pamArray);
		}
		if (iPhone >= phoneToStreamerLUT.length) {
			return 0;
		}
		return phoneToStreamerLUT[iPhone];
	}

	protected void setupLocators(PamArray currentArray) {
		pamArray = currentArray;
		int nStreamers = pamArray.getNumStreamers();
		int nPhones = pamArray.getHydrophoneCount();
		phoneToStreamerLUT = new int[nPhones];
		for (int i = 0; i < nPhones; i++) {
			phoneToStreamerLUT[i] = pamArray.getHydrophone(i).getStreamerId();
		}
		for (int i = 0; i < nStreamers; i++) {
			Streamer streamer = pamArray.getStreamer(i);
			streamer.setupLocator(pamArray);
		}
//		if (nStreamers > 0) {
//			HydrophoneLocator firstLocator = pamArray.getStreamer(0).getHydrophoneLocator();
//			LatLong refLatLong = firstLocator.getReferenceLatLong(PamCalendar.getTimeInMillis());
////			if (refLatLong != null) {
////				MasterReferencePoint.setRefLatLong(refLatLong, firstLocator.getName());
////			}
//		}
	}

	/**
	 * Work out which locator to use for a specific hydrophone
	 * @param iPhone hydrophone index
	 * @return
	 */
	protected HydrophoneLocator locatorForHydrophone(int iPhone) {
		return locatorForStreamer(phoneToStreamer(iPhone));
	}

	protected HydrophoneLocator locatorForStreamer(int iStreamer) {
		Streamer streamer = pamArray.getStreamer(iStreamer);
		if (streamer != null) {
			return streamer.getHydrophoneLocator();
		}
		return null;
	}

//	protected void setHydrophoneLocator(int iStreamer, HydrophoneLocator locator) {
//		Streamer streamer = pamArray.getStreamer(iStreamer);
//		if (streamer != null) {
//			streamer.setHydrophoneLocator(locator);
//		}
//	}


	/**
	 * Work out if two hydrophones are using the same locator or not. 
	 * @param phone1
	 * @param phone2
	 * @return true if it's the same locator
	 */
	private boolean sameLocator(int phone1, int phone2) {
		HydrophoneLocator l1, l2;
		l1 = locatorForHydrophone(phone1);
		l2 = locatorForHydrophone(phone2);
		return (l1 == l2);
	}

	@Override
	protected GpsData getStreamerGpsPos(long timeMilliseconds) {
		/*
		 * Just do the first for now - could consider an average, or additional 
		 * options such as setting a master streamer.
		 */
		Streamer streamer = pamArray.getStreamer(0);
		if (streamer == null) {
			return null;
		}
		return streamer.getHydrophoneLocator().getStreamerGpsPos(timeMilliseconds);
	}

	@Override
	public GpsData getPhoneLatLong(long timeMilliseconds, int phoneNo) {
		return locatorForHydrophone(phoneNo).getPhoneLatLong(timeMilliseconds, phoneNo);
	}

//	@Override
//	public double getPhoneHeight(long timeMilliseconds, int phoneNo) {
//		return locatorForHydrophone(phoneNo).getPhoneHeight(timeMilliseconds, phoneNo);
//	}
//
//	@Override
//	public double getPhoneTilt(long timeMilliseconds, int phoneNo) {
//		return locatorForHydrophone(phoneNo).getPhoneTilt(timeMilliseconds, phoneNo);
//	}
//
//	@Override
//	public double getPairAngle(long timeMilliseconds, int phone1, int phone2,
//			int angleType) {
//		if (sameLocator(phone1, phone2)) {
//			return locatorForHydrophone(phone1).getPairAngle(timeMilliseconds, phone1, phone2, angleType);
//		}
//		else {
//			/**
//			 * Really needs a much more sophisticated calculation now !
//			 * Get the lat long of each hydrophone then 
//			 */
//			LatLong ll1 = locatorForHydrophone(phone1).getPhoneLatLong(timeMilliseconds, phone1);
//			LatLong ll2 = locatorForHydrophone(phone1).getPhoneLatLong(timeMilliseconds, phone2);
//			if (ll1 == null || ll2 == null) {
//				return 0;
//			}
//			return ll1.bearingTo(ll2);
//		}
//	}
//
//	@Override
//	public double getPairSeparation(long timeMilliseconds, int phone1, int phone2) {
//		if (sameLocator(phone1, phone2)) {
//			return locatorForHydrophone(phone1).getPairSeparation(timeMilliseconds, phone1, phone2);
//		}
//		else {
//			/**
//			 * Really needs a much more sophisticated calculation now !
//			 * Get the lat long of each hydrophone then 
//			 */
//			LatLong ll1 = locatorForHydrophone(phone1).getPhoneLatLong(timeMilliseconds, phone1);
//			LatLong ll2 = locatorForHydrophone(phone1).getPhoneLatLong(timeMilliseconds, phone2);
//			if (ll1 == null || ll2 == null) {
//				return 0;
//			}
//			return ll1.distanceToMetres(ll2);
//		}
//	}

	@Override
	public void notifyModelChanged(int changeType, boolean initComplete) {
//		if (streamerLocators == null) {
//			return;
//		}
//		for (int i = 0; i < streamerLocators.length; i++) {
//			streamerLocators[i].notifyModelChanged(changeType);
//		}
		if (initComplete) switch (changeType) {
		case PamControllerInterface.HYDROPHONE_ARRAY_CHANGED:
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			setupLocators(ArrayManager.getArrayManager().getCurrentArray());
		}
	}

//	@Override
//	public double[] getHydrophoneCoordinates(int iHydrophone, long timeMilliseconds) {
//		try {
//			return locatorForHydrophone(iHydrophone).getHydrophoneCoordinates(iHydrophone, timeMilliseconds);
//		}
//		catch (NullPointerException e) {
//			e.printStackTrace();
//			return locatorForHydrophone(iHydrophone).getHydrophoneCoordinates(iHydrophone, timeMilliseconds);
//		}
//	}
//
//	@Override
//	public double[] getHydrophoneCoordinateErrors(int iHydrophone,
//			long timeMilliseconds) {
//		return locatorForHydrophone(iHydrophone).getHydrophoneCoordinateErrors(iHydrophone, timeMilliseconds);
//	}
//
//	@Override
//	public Double getArrayHeading(GpsData gpsData) {
//		Streamer streamer = pamArray.getStreamer(0);
//		if (streamer == null) {
//			return null;
//		}
//		return streamer.getHydrophoneLocator().getArrayHeading(gpsData);
//	}
//
//	@Override
//	public double getArrayHeading(long timeMilliseconds, int phoneNo) {
//		return locatorForHydrophone(phoneNo).getArrayHeading(timeMilliseconds, phoneNo);
//	}

	@Override
	public boolean isStatic() {
		/**
		 * Will return true only if all locators are static. 
		 */
		int n = pamArray.getNumStreamers();
		boolean isStat = true;
		for (int i = 0; i < n; i++) {
			Streamer streamer = pamArray.getStreamer(i);
			if (!streamer.getHydrophoneLocator().isStatic()) {
				isStat = false;
			}
		}
		return isStat;
	}

	@Override
	public boolean isChangeable() {
		/**
		 * Will return true if ANY locators are changeable. 
		 */
		boolean isChang = false;
		int n = pamArray.getNumStreamers();
		for (int i = 0; i < n; i++) {
			Streamer streamer = pamArray.getStreamer(i);
			if (streamer.getHydrophoneLocator().isChangeable()) {
				isChang = true;
			}
		}
		return isChang;
	}

	@Override
	public LocatorSettings getLocatorSettings() {
//		ArrayList<LocatorSettings> locatorSettingsList = new ArrayList<LocatorSettings>();
		int n = pamArray.getNumStreamers();
		for (int i = 0; i < n; i++) {
			Streamer streamer = pamArray.getStreamer(i);
			streamer.getLocatorSettings();
		}
		return null;
	}


	/**
	 * This is used a bit differently in the master locator
	 * since it's passed null, but will have an array list of 
	 * settings from when it was deserialized which it must pass on 
	 * to the other locators.   
	 */
	@Override
	public boolean setLocatorSettings(LocatorSettings unusedSettings) {
		// probably don't need to do anything here since the locator settings
		// are serialised into the streamers. Could however do a quick check to ensure that
		// all the streamer locators have been set up correctly.
		int n = pamArray.getNumStreamers();
		for (int i = 0; i < n; i++) {
			Streamer streamer = pamArray.getStreamer(i);
			streamer.setupLocator(pamArray);
		}
		return true;

		
//		if (this.locatorSettingsList == null) {
//			return false;
//		}
//		if (streamerLocators == null) {
//			return false;
//		}
//		int n = Math.min(locatorSettingsList.size(), streamerLocators.length);
//		for (int i = 0; i < n; i++) {
//			streamerLocators[i].setInitialLocatorSettings(locatorSettingsList.get(i));
//		}
	}

	/* (non-Javadoc)
	 * @see Array.HydrophoneLocator#getName()
	 */
	@Override
	public String getName() {
		return "Master Locator";
	}

	@Override
	public LocatorSettings getDefaultSettings() {
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("pamArray");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return pamArray;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("phoneToStreamerLUT");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return phoneToStreamerLUT;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


}
