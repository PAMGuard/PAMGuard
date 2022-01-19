package Array;

import java.lang.reflect.Field;
import java.util.ListIterator;
import pamMaths.PamVector;
import Array.sensors.StreamerSensorManager;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.OriginIterator;
import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import GPS.NavDataSynchronisation;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;

/**
 * 
 * A threading hydrophone is one that exactly follows the track of the ship.
 * It works by holding GPS information, then for any time, works out where the 
 * ship was, then works back to where the hydrophone would have been. 
 * <p>
 * Extends StrightHydrophoneLocator since that already handled accessing GPS
 * data.  
 * 
 * @author Doug Gillespie
 *
 */
public class ThreadingHydrophoneLocator extends StraightHydrophoneLocator implements ManagedParameters {

	private static final long serialVersionUID = 3244112516372812481L;

	private Object headingSynchObject = NavDataSynchronisation.getSynchobject();

	private double storedHeading;
	private long storedHeadingTime;
	private int storedHeadingPhone;


	public ThreadingHydrophoneLocator(PamArray pamArray, Streamer streamer) {
		super(pamArray, streamer);
	}


	@Override
	public String toString() {
		return ThreadingHydrophoneLocatorSystem.sysName;
	}

	@Override
	protected GpsData getStreamerGpsPos(long timeMilliseconds) {
		/**
		 * now doing entirely of GPS data.
		 * Though this function probably needs changing very soon, so that the public call to getStreamerLatLong 
		 * is wrapped in another function which can get the hprd data and force it onto the output of this function. 
		 * Can either wrap this function in something that does that or encourage a call to a standard additional 
		 * function that finds all the hpr data. 
		 */
		HydrophoneOriginMethod originMethod = streamer.getHydrophoneOrigin();

		double distBack = -streamer.getY();
		GpsData gpsUnit = null;
		GpsData prevGpsData=null, gpsData=null;

		synchronized (originMethod.getSynchronizationObject()) {
			OriginIterator gpsIt = originMethod.getGpsDataIterator(PamDataBlock.ITERATOR_END);
			
			if (gpsIt == null) {
				return new GpsData(); // return (0,0)
			}
			/**
			 * Now need to go back until we're close to the time of interest, or it all goes 
			 * badly wrong. 
			 */
			while (gpsIt.hasPrevious()) {
				gpsUnit = gpsIt.previous();
				gpsData = gpsUnit;
				if (gpsUnit.getTimeInMillis() < timeMilliseconds) {
					break;
				}
			}
			if (gpsData == null && gpsIt.hasPrevious()) {
				/*
				 *  I don't think this line can ever be called since gpsUnit must have been 
				 *  set in the while loop avoe. 
				 */			
				gpsUnit = gpsIt.previous();
			}
			if (gpsIt.hasPrevious() == false) {
				/**
				 * We're off the start of the available track, so can do nothing except use the stright model to 
				 * interpolate backwards. 
				 */
				return super.getStreamerGpsPos(timeMilliseconds);
			}

			gpsData = prevGpsData = gpsUnit;
			double ahead = (timeMilliseconds - gpsUnit.getTimeInMillis())*prevGpsData.getSpeedMetric()/1000.;
			distBack -= ahead;
			
			while (distBack > 0 && gpsIt.hasPrevious()) {
				gpsUnit = gpsIt.previous();
				gpsData = gpsUnit;
				if (prevGpsData == null) {
					prevGpsData = gpsData;
					continue;
				}
				double dist = gpsData.distanceToMetres(prevGpsData);
//				distBack += dist;
				distBack-=dist;
				if (distBack < 0) {
					break;
				}
//				if (dist > distBack) {
//					break;
//				}
				else {
					prevGpsData = gpsData;
				}
			}
		}
		if (gpsData == null) {
			return new GpsData();
		}
		gpsData = gpsData.clone();
		double gpsHead = gpsData.getHeading(false);
		LatLong streamerGps = gpsData.travelDistanceMeters(gpsHead, -distBack);
		if (streamer.getX() != 0) {
			streamerGps = streamerGps.travelDistanceMeters(gpsHead+90, streamer.getX());
		}
		gpsData.setLatitude(streamerGps.getLatitude());
		gpsData.setLongitude(streamerGps.getLongitude());
		gpsData.setHeight(streamer.getZ());

		return gpsData;
	}

	//	 public GpsData getPhoneLatLongOld(long timeMilliseconds, int phoneNo) {
	//		/*
	//		 * in the array basic settings, x is the distance along the beam and y
	//		 * is the distance ahead of the vessel - so need to use the vessel heading
	//		 * to get from the gps position to the positions of individual hydrophones
	//		 * IF no GPS data are available, then reference to 0,0.
	//		 * Available methods are currently GPS and static
	//		 */
	//		HydrophoneOriginMethod originMethod = streamer.getHydrophoneOrigin();
	//		
	////		System.out.println("");
	////		System.out.println(String.format("Phone %d position at %s", phoneNo, PamCalendar.formatTime(timeMilliseconds)));
	//
	//		GpsData gpsData = null, g = null;
	//		StreamerDataUnit streamerDataUnit = null;
	//		double distanceToTravel = 0;
	//
	//		synchronized (headingSynchObject) {
	//			storedHeadingTime = 0;
	//			storedHeadingPhone = -1;
	//			storedHeading = 0;
	//		}
	//		
	//		PamVector sVec;
	//		PamVector hVec = null;
	//		if (originMethod == null || originMethod.getLastStreamerData() == null) {
	//			return new GpsData();
	//		}
	//			
	//		synchronized (originMethod.getSynchronizationObject()) {
	//
	//			// also synch of streamer data block
	//			ListIterator<StreamerDataUnit> streamerIterator = originMethod.getGpsDataIterator(PamDataBlock.ITERATOR_END);
	//			// work back until we find the closest gps unit.
	//			// do all this here, since we want to hold on to the iterator to carry
	//			// on working backwards.
	//			if (streamerIterator.hasPrevious()) {
	//				streamerDataUnit = streamerIterator.previous();
	//			}
	//			StreamerDataUnit prevUnit;
	//			while(streamerIterator.hasPrevious()) {
	//				prevUnit = streamerIterator.previous();
	//				if (prevUnit == null) {
	//					break;
	//				}
	//				if (timeMilliseconds > prevUnit.getTimeMilliseconds()) {
	//					if (streamerDataUnit.getTimeMilliseconds() - timeMilliseconds < 
	//							timeMilliseconds - prevUnit.getTimeMilliseconds()) {
	//						streamerIterator.next();
	//					}
	//					else {
	//						streamerDataUnit = prevUnit;
	//					}
	//					break;
	//				}
	//				streamerDataUnit = prevUnit;
	//			}
	//
	//			if (streamerDataUnit != null) {
	//				gpsData = streamerDataUnit.getGpsData();
	////				System.out.println(String.format("CLosest unit at %s (%s) pos %s", PamCalendar.formatTime(streamerDataUnit.getTimeMilliseconds()), 
	////						PamCalendar.formatTime(gpsData.getTimeInMillis()), gpsData.toFullString()));
	//			}
	//			if (gpsData == null) {
	//				gpsData = new GpsData();
	//			}
	//
	////			if (streamerDataUnit.getDatabaseIndex() == 4440) {
	////				System.out.println("Stop");
	////			}
	////			System.out.println(String.format("Th' locator t=%s/%s phone=%d, stream Id %d/%d, streamer Pos=%s", 
	////					PamCalendar.formatDateTime(timeMilliseconds),PamCalendar.formatDateTime(streamerDataUnit.getTimeMilliseconds()), 
	////					phoneNo, streamerDataUnit.getChannelBitmap(), streamerDataUnit.getStreamerData().getStreamerIndex(), 
	////					streamerDataUnit.getStreamerData().getCoordinateVector().toString()));
	////			if (streamerDataUnit == null) {
	////			System.out.println("GPS Orientation: Heading: " +gpsData.getHeading()+" Pitch: "+gpsData.getPitch()+" Roll: "+gpsData.getRoll()+" Height: "+ gpsData.getHeight());
	////			System.out.println("Streamer Orientation: Heading: " +streamerDataUnit.getStreamerData().getHeading()+" Pitch: "+streamerDataUnit.getStreamerData().getPitch()+" Roll: "+streamerDataUnit.getStreamerData().getRoll() + " Height: " +streamerDataUnit.getStreamerData().getZ()) ;
	////			}
	//			
	//			/*
	//			 * That's told us where the ship was, now need to work back and 
	//			 * find out where the actual hydrophone was at that time
	//			 * The easiest way to do this is based on vessel speed and hope
	//			 * it didn't change too much, but a better way, used here, is to 
	//			 * tot up distances between gps's, which have been conveniently
	//			 * stored in the GpsData. 
	//			 */
	//			
	//			if (phoneNo >= 0) {
	//				hVec = pamArray.getHydrophoneVector(phoneNo,timeMilliseconds);
	//			}
	//			sVec = streamer.getCoordinateVector();
	//			
	//
	//			if (sVec.getElement(1) > 0 || streamerDataUnit == null) {
	//				// the hydrophone is in front of the boat, so assume a rigid system. 
	//				LatLong latLong = gpsData.travelDistanceMeters(getArrayHeading(gpsData), 
	//						sVec.getElement(1));
	//				latLong = latLong.travelDistanceMeters(getArrayHeading(gpsData)+90, 
	//						sVec.getElement(0));
	//
	//				gpsData = gpsData.clone();
	//				gpsData.setLatitude(latLong.getLatitude());
	//				gpsData.setLongitude(latLong.getLongitude());
	//				gpsData.setHeight(sVec.getElement(2) );
	//				return gpsData;
	//			}
	//
	////			double  = getPhoneHeight(timeMilliseconds, phoneNo);
	//			/**
	//			 * Making some changes to the threading locator April 2019 to get correct locations
	//			 * for arrays with heading sensors. these sensors are in with the hydrophones
	//			 * themselves, so the heading sensor information should not be used to move anything until
	//			 * it get's to the point of adding the actual phone locations to the overall array position.  
	//			 */
	//
	//			// start by seeing how far beyond the last gps position we're likely
	//			// to have travelled...
	//			double beyond = (timeMilliseconds - gpsData.getTimeInMillis()) / 1000. * gpsData.getSpeedMetric();
	//			distanceToTravel = -sVec.getElement(1) - beyond;
	//			
	////			//now we need to compensate for depth. Rememember that we are itarating along the x y plane, however if we assume the array is attached at (0,0,0) then if z!=0
	////			//distanceToTravel is not the distance on the xy plane but the distance in 3D. Easy to calculate the xy distance, we just use Pythagoras theorem.  
	////			distanceToTravel=Math.sqrt(Math.pow(distanceToTravel,2)-Math.pow(hVec.getElement(2),2));
	//			
	//			//now work back through gps units until we're at the GPS unit in front of the one we want
	//			StreamerDataUnit gu = streamerDataUnit;
	//			g = streamerDataUnit.getGpsData();
	//			double heightFromLast=0; 
	//			GpsData prevGps = null;
	//			while (streamerIterator.hasPrevious() && gu != null) {
	//				g = gu.getGpsData();
	//				if (g == null) {
	//					System.out.println("Null gps data for streamer in ThreadingHydrophoneLocator.getPhoneLatLong");
	//					break;
	//				}
	//				double distanceFromLast = 0;
	//				if (prevGps != null) {
	//					distanceFromLast = prevGps.distanceToMetres(g);
	//				}
	////				//TODO- height from last.
	////				double distanceFromLast=Math.sqrt(Math.pow(g.getDistanceFromLast(),2)+Math.pow(heightFromLast, 2));
	//				if (distanceToTravel > distanceFromLast) {
	//					prevGps = g;
	//					distanceToTravel -= distanceFromLast;
	//					gu = streamerIterator.previous();
	//				}
	//				else {
	//					if (prevGps != null) {
	//						g = prevGps; // move back to the next one, so we should exit loop with the gps AFTER the position. 
	//					}
	//					break;
	//				}
	//			}
	//		}
	//		if (g == null) {
	//			return new GpsData();
	//		}
	//		// distance to travel should now be a small positive number.
	//		// need to travel backwards a bit more, so need sign change in next function call ...
	//		/*
	//		 * g is the GPS data at the streamer. 
	//		 * gpsData is the GPS Data at the vessel. 
	//		 * both should have their corrected heading based on the streamer information. 
	//		 * 
	//		 */
	//		double gHead = getArrayHeading(g); // gps heading at the streamer
	//		double gpsHead = getArrayHeading(gpsData); // hopefully GPS heading using sensor data. 
	////		System.out.printf("Head at vessel %3.1f, Head at streamer %.1f\n", gpsHead, gHead);
	//		/*
	//		 * The final position of the STREAMER extrapolates along the heading of the
	//		 * array at the array end, i.e. using gHead. 
	//		 */
	//		LatLong latLong = g.travelDistanceMeters(gHead, -distanceToTravel);
	//		if (sVec.getElement(0) != 0) {
	//			// and offset with any x coordinate of the streamer. 
	//			latLong = latLong.travelDistanceMeters(gpsHead+90, sVec.getElement(0));
	//		}
	//		latLong.setHeight(sVec.getElement(2));
	//		/*
	//		 * for the individual hydrophones, we then need to use the streamer heading
	//		 * which may be different, if based on sensor readings. 
	//		 */
	//		if (hVec != null) {
	//			if (hVec.getElement(0) != 0) {
	//				latLong = latLong.travelDistanceMeters(gpsHead+90, hVec.getElement(0));
	//			}
	//			if (hVec.getElement(1) != 0) {
	//				latLong = latLong.travelDistanceMeters(gpsHead, hVec.getElement(1));
	//			}
	//			if (hVec.getElement(2) != 0) {
	//				latLong.setHeight(latLong.getHeight()+hVec.getElement(2));
	//			}
	//		}
	//		
	//		synchronized (headingSynchObject) {
	//			storedHeadingTime = timeMilliseconds;
	//			storedHeadingPhone = phoneNo;
	//			storedHeading = g.getHeading();
	//		}
	//
	//		GpsData finalData = g.clone();
	//		finalData.setLatitude(latLong.getLatitude());
	//		finalData.setLongitude(latLong.getLongitude());
	//		finalData.setHeight(sVec.getElement(2));
	//		/**
	//		 * the million $$ question, is do we want gHead or gpsHead here, which is returned to the snapshot geom ? 
	//		 * Really, we should have three different headings to deal with.
	//		 * 1. The GPS heading of the vessel at this time
	//		 * 2. The GPs heading of the closest point to the streamer
	//		 * 3. The streamer heading at the time we think we're at. What a bloody mess !
	//		 * When NOT using sensors, gHead works. 
	//		 * When using sensors, gpsHead works. 
	//		 */
	////		boolean useOrient = false;
	////		if (streamer != null) {
	////			useOrient = streamer.isEnableOrientation();
	////		}
	////		if (useOrient) {
	////			finalData.setTrueHeading(gpsHead); // head from current time 
	////			finalData.setPitch(gpsData.getPitchD());
	////			finalData.setRoll(gpsData.getRollD());
	////		}
	////		else {
	//			finalData.setTrueHeading(gHead); // head from gps data at hydrophone. 
	////		}
	//		
	//		return finalData;
	//	}
	//	
	//	
	//	@Override
	//	public double getArrayHeading(long timeMilliseconds, int phoneNo) {
	//		synchronized (headingSynchObject) {
	//			if (storedHeadingTime != timeMilliseconds || storedHeadingPhone != phoneNo) {
	//				GpsData gData = getPhoneLatLong(timeMilliseconds, phoneNo);
	//				return gData.getHeading();
	////				storedHeading = super.getArrayHeading(timeMilliseconds, phoneNo);
	////				storedHeadingTime = timeMilliseconds;
	////				storedHeadingPhone = phoneNo;
	//			}
	//			return storedHeading;
	//		}
	//	}
	//
	//	/*
	//	 * Needs to be a bit more sophisticated too
	//	 * in order to cope with the threading angle
	//	 *  (non-Javadoc)
	//	 * @see Array.HydrophoneLocator#getPairAngle(long, int, int, int)
	//	 */
	//	@Override
	//	public double getPairAngle(long timeMilliseconds, int phone1, int phone2, int angleType) {
	//		/*
	//		 * One way would be to get the latlong of each hydrophone and then recaluclate 
	//		 * the bearing. 
	//		 * What I'm going to do is take the mean y position of the two phones 
	//		 * and get the hydrophone heading at that position.
	//		 */
	//
	//		// first get the angle relative to the array - this is dead easy. 
	//		double angle = super.getPairAngle(timeMilliseconds, phone1, phone2, HydrophoneLocator.ANGLE_RE_ARRAY);
	//
	//
	//		// all we need to do for ANGLE_RE_ARRAY option, so just return
	//		if (angleType == HydrophoneLocator.ANGLE_RE_ARRAY) return angle;
	//		StreamerDataUnit arrayGpsUnit;
	//		GpsData arrayGpsData, shipGpsData;
	//
	//
	//		HydrophoneOriginMethod originMethod = streamer.getHydrophoneOrigin();
	//		if (originMethod.getLastStreamerData() == null) {
	//			return angle;
	//		}
	//		synchronized (originMethod) {
	//			// now we need the location of the ship. 
	//			OriginIterator streamerIterator = originMethod.getGpsDataIterator(PamDataBlock.ITERATOR_END);
	//			StreamerDataUnit shipGpsDataUnit = null;
	//			
	//			while (streamerIterator.hasPrevious()) {
	//				shipGpsDataUnit = streamerIterator.previous();
	//				if (shipGpsDataUnit.getTimeMilliseconds() < timeMilliseconds) {
	//					break;
	//				}
	//			}
	//			if (shipGpsDataUnit == null) return angle;
	//			shipGpsData = shipGpsDataUnit.getGpsData();
	//
	//			Hydrophone h1 = pamArray.getHydrophone(phone1);
	//			if (h1 == null) return angle;
	//			Hydrophone h2 = pamArray.getHydrophone(phone1);
	//			if (h2 == null) return angle;
	//			//		DepthDataUnit ddu = findDepthDataUnit(timeMilliseconds);
	//
	//			double meanY = (h1.getY() + h2.getY()) / 2.;
	//			if (meanY > 0) {
	//				// it's in front of the ship so use the rigid locator
	//				if (angleType == HydrophoneLocator.ANGLE_RE_SHIP) return angle;
	//				else return angle + shipGpsData.getCourseOverGround();
	//			}
	//			// otherwise work back to closest ...
	//			double beyond = (timeMilliseconds - shipGpsData.getTimeInMillis()) / 1000. * shipGpsData.getSpeedMetric();
	//			double distanceToTravel = -meanY - beyond;
	//			//now work back through gps units until we're at the GPS unit in front of the one we want
	//			arrayGpsUnit = shipGpsDataUnit;
	//			arrayGpsData = shipGpsDataUnit.getGpsData();
	//			while (streamerIterator.hasPrevious()) {
	//				//		while (arrayGpsUnit != null) {
	//				arrayGpsData = arrayGpsUnit.getGpsData();
	//				if (distanceToTravel > arrayGpsData.getDistanceFromLast()) {
	//					distanceToTravel -= arrayGpsData.getDistanceFromLast();
	//					arrayGpsUnit = streamerIterator.previous();
	//				}
	//				else {
	//					break;
	//				}
	//			}
	//		}
	//
	//		if (angleType == HydrophoneLocator.ANGLE_RE_SHIP) {
	//			// add in the difference between where the ship is heading and where the array is heading
	//			return angle + arrayGpsData.getCourseOverGround() - shipGpsData.getCourseOverGround();
	//		}
	//		else {
	//			// return angle relative to North. 
	//			return angle + arrayGpsData.getCourseOverGround();
	//		}
	//
	//	}
	//

	/* (non-Javadoc)
	 * @see Array.StraightHydrophoneLocator#getName()
	 */
	@Override
	public String getName() {
		return ThreadingHydrophoneLocatorSystem.sysName;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("storedHeading");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return storedHeading;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("storedHeadingPhone");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return storedHeadingPhone;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("storedHeadingTime");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return storedHeadingTime;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}



	//	@Override
	//	synchronized public LatLong getPhoneLatLong(long timeMilliseconds, int phoneNo) {
	//		/*
	//		 * in the array basic settings, x is the distance along the beam and y
	//		 * is the distance ahead of the vessel - so need to use the vessel heading
	//		 * to get from the gps position to the positions of individual hydrophones
	//		 * IF no GPS data are available, then reference to 0,0.
	//		 */
	//		GpsData gpsData = null, g = null;
	//		GpsDataUnit gpsDataUnit = null;
	//		double distanceToTravel = 0;
	//
	//		synchronized (headingSynchObject) {
	//			storedHeadingTime = 0;
	//			storedHeadingPhone = -1;
	//			storedHeading = 0;
	//		}
	//		
	//		PamVector hVec;
	//		if (gpsDataBlock == null) {
	//			return new LatLong(0,0);
	//		}
	//		synchronized (gpsDataBlock) {
	//			ListIterator<GpsDataUnit> gpsIterator = gpsDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
	//			// work back until we find the closest gps unit.
	//			// do all this here, since we want to hold on to the iterator to carry
	//			// on working backwards.
	//			gpsDataUnit = gpsDataBlock.getPreceedingUnit(gpsIterator, timeMilliseconds);
	//
	//			if (gpsDataUnit != null) {
	//				gpsData = gpsDataUnit.getGpsData();
	//			}
	//			if (gpsData == null) {
	//				gpsData = new GpsData();
	//			}
	//			/*
	//			 * That's told us where the ship was, now need to work back and 
	//			 * find out where the actual hydrophone was at that time
	//			 * The easiest way to do this is based on vessel speed and hope
	//			 * it didn't change too much, but a better way, used here, is to 
	//			 * tot up distances between gps's, which have been conveniently
	//			 * stored in the GpsData. 
	//			 */
	//
	//			hVec=new PamVector(getHydrophoneCoordinates(phoneNo,timeMilliseconds));
	//			Streamer s = pamArray.getStreamer(pamArray.getHydrophone(phoneNo).getID());
	//			
	//			if (s != null && s.getCoordinateVector() != null) {
	//				hVec = hVec.add(s.getCoordinateVector());
	//			}
	//			
	//			if (hVec == null) {
	//				return null;
	//			}
	////			h = pamArray.getHydrophone(phoneNo);
	////			if (h == null) return null;
	//
	//			if (hVec.getElement(1) > 0 || gpsDataUnit == null) {
	//				// the hydrophone is in front of the boat, so assume a rigid system. 
	//				Double heading =getArrayHeading(gpsData);
	//				if (heading==null) heading=0.0;
	//				LatLong latLong = gpsData.travelDistanceMeters(heading, 
	//						hVec.getElement(1));
	//				latLong = latLong.travelDistanceMeters(heading+90, 
	//						hVec.getElement(0));
	//
	//				return latLong;
	//			}
	//
	////			double  = getPhoneHeight(timeMilliseconds, phoneNo);
	//
	//			// start by seeing how far beyond the last gps position we're likely
	//			// to have travelled...
	//			double beyond = (timeMilliseconds - gpsData.getTimeInMillis()) / 1000. * gpsData.getSpeedMetric();
	//			distanceToTravel = -hVec.getElement(1) - beyond;
	//			//now work back through gps units until we're at the GPS unit in front of the one we want
	//			GpsDataUnit gu = gpsDataUnit;
	//			g = gpsDataUnit.getGpsData();
	//			while (gpsIterator.hasPrevious()) {
	//				g = gu.getGpsData();
	//				if (distanceToTravel > g.getDistanceFromLast()) {
	//					distanceToTravel -= g.getDistanceFromLast();
	//					gu = gpsIterator.previous();
	//				}
	//				else {
	//					break;
	//				}
	//			}
	//		}
	//		// distance to travel should now be a small positive number.
	//		// need to travel backwards a bit more, so need sign change in next function call ...
	//
	//		LatLong latLong = g.travelDistanceMeters(getArrayHeading(g), -distanceToTravel);
	//		latLong = latLong.travelDistanceMeters(getArrayHeading(g)+90, hVec.getElement(0));
	//		latLong.setHeight(hVec.getElement(2));
	//		
	//		synchronized (headingSynchObject) {
	//			storedHeadingTime = timeMilliseconds;
	//			storedHeadingPhone = phoneNo;
	//			storedHeading = getArrayHeading(g);
	//		}
	//
	//		return latLong;
	//	}
	//	
	//	
	//	@Override
	//	public double getArrayHeading(long timeMilliseconds, int phoneNo) {
	//		synchronized (headingSynchObject) {
	//			if (storedHeadingTime != timeMilliseconds || storedHeadingPhone != phoneNo) {
	//				getPhoneLatLong(timeMilliseconds, phoneNo);
	//			}
	//			//System.out.println("storedHeading"+storedHeading);
	//			return storedHeading;
	//		}
	//	}
	//
	//	/*
	//	 * Needs to be a bit more sophisticated too
	//	 * in order to cope with the threading angle
	//	 *  (non-Javadoc)
	//	 * @see Array.HydrophoneLocator#getPairAngle(long, int, int, int)
	//	 */
	//	@Override
	//	public double getPairAngle(long timeMilliseconds, int phone1, int phone2, int angleType) {
	//		/*
	//		 * One way would be to get the latlong of each hydrophone and then recaluclate 
	//		 * the bearing. 
	//		 * What I'm going to do is take the mean y position of the two phones 
	//		 * and get the hydrophone heading at that position.
	//		 */
	//
	//		// first get the angle relative to the array - this is dead easy. 
	//		double angle = super.getPairAngle(timeMilliseconds, phone1, phone2, HydrophoneLocator.ANGLE_RE_ARRAY);
	//
	//
	//		// all we need to do for ANGLE_RE_ARRAY option, so just return
	//		if (angleType == HydrophoneLocator.ANGLE_RE_ARRAY) return angle;
	//		GpsDataUnit arrayGpsUnit;
	//		GpsData arrayGpsData, shipGpsData;
	//		if (gpsDataBlock == null) {
	//			return angle;
	//		}
	//		synchronized (gpsDataBlock) {
	//			// now we need the location of the ship. 
	//			ListIterator<GpsDataUnit> gpsIterator = gpsDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
	//			GpsDataUnit shipGpsDataUnit = gpsDataBlock.getPreceedingUnit(timeMilliseconds);
	//			if (shipGpsDataUnit == null) return angle;
	//			shipGpsData = shipGpsDataUnit.getGpsData();
	//
	//			Hydrophone h1 = pamArray.getHydrophone(phone1);
	//			if (h1 == null) return angle;
	//			Hydrophone h2 = pamArray.getHydrophone(phone1);
	//			if (h2 == null) return angle;
	//			//		DepthDataUnit ddu = findDepthDataUnit(timeMilliseconds);
	//
	//			double meanY = (h1.getY() + h2.getY()) / 2.;
	//			if (meanY > 0) {
	//				// it's in front of the ship so use the rigid locator
	//				if (angleType == HydrophoneLocator.ANGLE_RE_SHIP) return angle;
	//				else return angle + getArrayHeading(shipGpsData);
	//			}
	//			// otherwise work back to closest ...
	//			double beyond = (timeMilliseconds - shipGpsData.getTimeInMillis()) / 1000. * shipGpsData.getSpeedMetric();
	//			double distanceToTravel = -meanY - beyond;
	//			//now work back through gps units until we're at the GPS unit in front of the one we want
	//			arrayGpsUnit = shipGpsDataUnit;
	//			arrayGpsData = shipGpsDataUnit.getGpsData();
	//			while (gpsIterator.hasPrevious()) {
	//				//		while (arrayGpsUnit != null) {
	//				arrayGpsData = arrayGpsUnit.getGpsData();
	//				if (distanceToTravel > arrayGpsData.getDistanceFromLast()) {
	//					distanceToTravel -= arrayGpsData.getDistanceFromLast();
	//					arrayGpsUnit = gpsIterator.previous();
	//				}
	//				else {
	//					break;
	//				}
	//			}
	//		}
	//
	//		if (angleType == HydrophoneLocator.ANGLE_RE_SHIP) {
	//			// add in the difference between where the ship is heading and where the array is heading
	//			return angle + getArrayHeading(arrayGpsData) - getArrayHeading(shipGpsData);
	//		}
	//		else {
	//			// return angle relative to North. 
	//			return angle + getArrayHeading(arrayGpsData);
	//		}
	//
	//	}
}
