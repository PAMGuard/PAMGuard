package Array.streamerOrigin;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import GPS.NavDataSynchronisation;
import PamUtils.PamCalendar;


public class GPSOriginMethod extends HydrophoneOriginMethod {

	public GPSOriginMethod(PamArray pamArray, Streamer streamer) {
		super(pamArray, streamer);
		prepare();
	}

	private GPSOriginSettings gpsOriginSettings = new GPSOriginSettings();
	
	private GPSDataBlock gpsDataBlock;
	
	@Override
	public String getName() {
		return GPSOriginSystem.systemName;
	}

	@Override
	public OriginSettings getOriginSettings() {
		return gpsOriginSettings;
	}

	@Override
	public void setOriginSettings(OriginSettings originSettings) {
		gpsOriginSettings = (GPSOriginSettings) originSettings;
	}

	@Override
	public boolean prepare() {
		gpsDataBlock = ArrayManager.getArrayManager().getGPSDataBlock();
		return gpsDataBlock != null;
	}

	@Override
	public StreamerDataUnit getLastStreamerData() {
		if (gpsDataBlock == null) {
			return null;
		}
		GpsDataUnit gpsDataUnit = gpsDataBlock.getLastUnit();
		// also get the last streamer data unit. 
		StreamerDataBlock streamerDataBlock = ArrayManager.getArrayManager().getStreamerDatabBlock();
		StreamerDataUnit streamerDataUnit = streamerDataBlock.getLastUnit(1<<streamer.getStreamerIndex());
		if (streamerDataUnit == null) {
			streamerDataUnit = new StreamerDataUnit(PamCalendar.getTimeInMillis(), streamer);
		}
		return streamerDataUnit.makeCombinedGpsUnit(gpsDataUnit);		
	}

//	@Override
//	public GpsDataUnit getGpsData(long timeMillis) {
//		if (gpsDataBlock == null) {
//			return null;
//		}
//		return gpsDataBlock.getClosestUnitMillis(timeMillis);
//	}

	@Override
	public OriginIterator getGpsDataIterator(int whereFrom) {
		return new GPSOriginIterator(streamer, whereFrom, gpsDataBlock);
	}

	@Override
	public Object getSynchronizationObject() {
//		if (gpsDataBlock != null) {
//			return gpsDataBlock;
//		}
//		return new Object();
		return NavDataSynchronisation.getSynchobject();
	}

	/* (non-Javadoc)
	 * @see Array.streamerOrigin.HydrophoneOriginMethod#getAllowedInterpolationMethods()
	 */
	@Override
	public int getAllowedInterpolationMethods() {
		return (1<<PamArray.ORIGIN_INTERPOLATE) | (1<<PamArray.ORIGIN_USE_PRECEEDING);
	}

}
