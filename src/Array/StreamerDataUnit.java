package Array;

import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorFieldType;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StaticOriginMethod;
import Array.streamerOrigin.StaticOriginSettings;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamguardMVC.PamDataUnit;

public class StreamerDataUnit extends PamDataUnit {

	private Streamer streamerData;
	
	/*
	 * Real GPS data gets added to cloned StreamerDataUnits
	 * when used by localisers. 
	 */
	private GpsData realGpsData;
	
	public StreamerDataUnit(long timeMilliseconds, Streamer streamer) {
		super(timeMilliseconds);
		this.setStreamerData(streamer.clone());
		setChannelBitmap(1<<streamer.getStreamerIndex());
	}

	/**
	 * @return the streamerData
	 */
	public Streamer getStreamerData() {
		return streamerData;
	}

	/**
	 * @param streamerData the streamerData to set
	 */
	public void setStreamerData(Streamer streamerData) {
		this.streamerData = streamerData;
	}

	/**
	 * Average two streamer data units. 
	 * @param sd1
	 * @param sd2
	 * @return
	 */
	public static StreamerDataUnit getAverage(StreamerDataUnit sd1,
			StreamerDataUnit sd2) {
		if (sd1 == null) {
			return sd2;
		}
		if (sd2 == null) {
			return sd1;
		}
		return new StreamerDataUnit((sd1.getTimeMilliseconds()+sd2.getTimeMilliseconds())/2, 
				Streamer.getAverage(sd1.getStreamerData(), sd2.getStreamerData()));
	}

	/**
	 * Make a combined streamer data unit that has both the streamer data
	 * and GPS data so that localisers have easy access to both types of information. 
	 * @param gpsDataUnit
	 * @return a new combined streamer data unit. 
	 */
	public StreamerDataUnit makeCombinedGpsUnit(GpsDataUnit gpsDataUnit) {
		if (gpsDataUnit == null) {
			return null;
		}
		StreamerDataUnit newUnit = new StreamerDataUnit(gpsDataUnit.getTimeMilliseconds(), getStreamerData());
		//set the depth as the depth of the streamer
		gpsDataUnit.getGpsData().setHeight(getStreamerData().getZ());
		newUnit.setGpsData(gpsDataUnit.getGpsData());
		return newUnit;
	}

	public void setGpsData(GpsData gpsData) {
		realGpsData = gpsData;
		
	}

	/**
	 * We need to get the GPS unit associated with this array, however, we only wish to add roll, pitch and heading to that unit if we have enabled the
	 * use of orientation data. Thus we must clone() the realGps data unit so we don't inadvertently overwrite true heading data or even pitch and roll data which has been
	 * saved with the GPS data unit. 
	 * @return cloned gps data unit with the heading, pitch and roll added if orientation is anabled for this streamer data unit. 
	 */
	public GpsData getGpsData() {
		if (streamerData == null) {
			return realGpsData;
		}
		OriginSettings os = streamerData.getOriginSettings();
		if (realGpsData == null && os instanceof StaticOriginSettings) {
			StaticOriginSettings sos = (StaticOriginSettings) os;
			GpsDataUnit realGpsDataUnit = sos.getStaticPosition();
			if (realGpsDataUnit != null) {
				realGpsData = realGpsDataUnit.getGpsData();
			}
		}
		/**
		 * If there is sensor data it will get added in later, but we may need to get 
		 * the HPR data which is in streamerData. but only need to do this if 
		 * the data are set to use the fixed values. Defaults are all zero. 
		 */
		if (realGpsData == null) {
			return null;
		}
		ArraySensorFieldType[] fieldTypes = ArraySensorFieldType.values();
		for (int i = 0; i < fieldTypes.length; i++) {
			ArrayParameterType paramType = streamerData.getOrientationTypes(fieldTypes[i]);
			switch (paramType) {
			case FIXED:
				realGpsData.setOrientationField(fieldTypes[i], streamerData.getSensorField(fieldTypes[i]));
				break;
			case DEFAULT:
				if (fieldTypes[i] != ArraySensorFieldType.HEADING) {
					realGpsData.setOrientationField(fieldTypes[i], 0.);
				}
				break;
			case SENSOR: // no need to so anything since this will gbe found and added later on
				break;
			}
		}
		
		return realGpsData;
//		GpsData gpsData = null;
//		boolean isStatic = false;
//		if (realGpsData != null) {
//			gpsData = realGpsData.clone();
//		}
//		else {
//			// otherwise have to pull all this out of the origin data !
//			HydrophoneOriginMethod ho = streamerData.getHydrophoneOrigin();
//			if (ho.getOriginSettings().originMethodClass == StaticOriginMethod.class) {
//				isStatic = true;
//				StaticOriginSettings sos = (StaticOriginSettings) ho.getOriginSettings();
//				gpsData = sos.getStaticPosition().getGpsData().clone();
//				// but need to override the heading, etc. with data from the streamer.
//				gpsData.setTrueHeading(streamerData.getHeading());
//				gpsData.setPitch(streamerData.getPitch());
//				gpsData.setRoll(streamerData.getRoll());
//			}
//		}
//		
//		// now combine the static streamer data with the gps data !
//		if (streamerData.isEnableOrientation() && gpsData!=null) {
//			if (isStatic || streamerData.getHeading() != null) {
//				gpsData.setTrueHeading(streamerData.getHeading());
//			}
//			if (isStatic || streamerData.getPitch() != null) {
//				gpsData.setPitch(streamerData.getPitch());
//			}
//			if (isStatic || streamerData.getRoll() != null) {
//				gpsData.setRoll(streamerData.getRoll());
//			}
//		}
//		if (gpsData != null) {
//			gpsData.setHeight(streamerData.getZ());
//		}
//		return gpsData;
	}
}
