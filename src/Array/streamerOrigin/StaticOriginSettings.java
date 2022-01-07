package Array.streamerOrigin;

import java.lang.reflect.Field;

import Array.Streamer;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.PamCalendar;

public class StaticOriginSettings extends OriginSettings implements ManagedParameters {

	transient private GpsDataUnit staticDataUnit;
	
	private GpsData staticGpsData;
	
	public StaticOriginSettings() {
		super(StaticOriginMethod.class);
	}

	/**
	 * Get the main position and orientation of a static hydrophone cluster.
	 * @return the staticPosition
	 */
	public GpsDataUnit getStaticPosition() {
		if (staticDataUnit == null && staticGpsData != null) {
			staticDataUnit = new GpsDataUnit(staticGpsData.getTimeInMillis(), staticGpsData);
		}
		return staticDataUnit;
	}

	/**
	 * Set the main position and orientation of a static hydrophone cluster.
	 * @param streamer 
	 * @param staticPosition the staticPosition to set
	 */
	public void setStaticPosition(Streamer streamer, GpsData staticPosition) {
		staticDataUnit = new GpsDataUnit(PamCalendar.getTimeInMillis(), staticGpsData = staticPosition);
		if (streamer != null) {
			staticGpsData.setTrueHeading(streamer.getHeading());
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public StaticOriginSettings clone() {
		StaticOriginSettings newSettings = (StaticOriginSettings) super.clone();
		if (newSettings.staticGpsData == null) {
			newSettings.staticGpsData = new GpsData();
		}
		else {
			newSettings.staticGpsData = staticGpsData.clone();
		}
		newSettings.staticDataUnit = new GpsDataUnit(PamCalendar.getTimeInMillis(), newSettings.staticGpsData);
		
		return newSettings;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("staticGpsData");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return staticGpsData;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
