package Array.streamerOrigin;

import java.sql.Types;

import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import pamMaths.PamVector;
import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Array.Streamer;
import Array.StreamerDataUnit;
import GPS.GpsData;
import PamUtils.PamUtils;

public class StaticOriginSystem extends HydrophoneOriginSystem {

	// can't change this since it get's stored and is used to recreate
	// the objects from settings. 
	protected static String systemName = "Static moorings and buoys";
	
	protected static String systemNewName = "Fixed location (moorings and buoys)";
	
	private PamTableItem latItem, longItem;
	
	@Override
	public String getName() {
		return systemName;
	}
	

	@Override
	public HydrophoneOriginMethod createMethod(PamArray pamArray,
			Streamer streamer) {
		return new StaticOriginMethod(pamArray, streamer);
	}

	@Override
	public Class getMethodClass() {
		return StaticOriginMethod.class;
	}

	/* (non-Javadoc)
	 * @see Array.streamerOrigin.HydrophoneOriginSystem#createSQLLoggingFields(generalDatabase.PamTableDefinition)
	 */
	@Override
	public int createSQLLoggingFields(PamTableDefinition tableDefinition) {
		latItem = tableDefinition.findTableItem("Latitude");
		if (latItem == null) {
			tableDefinition.addTableItem(latItem = new PamTableItem("Latitude", Types.DOUBLE));
		}
		longItem = tableDefinition.findTableItem("Longitude");
		if (longItem == null) {
			tableDefinition.addTableItem(longItem = new PamTableItem("Longitude", Types.DOUBLE));
		}
		return 2;
	}

	/* (non-Javadoc)
	 * @see Array.streamerOrigin.HydrophoneOriginSystem#fillSQLLoggingFields(Array.StreamerDataUnit)
	 */
	@Override
	public boolean fillSQLLoggingFields(StreamerDataUnit streamerDataUnit) {
		Streamer streamer = streamerDataUnit.getStreamerData();
		try {
			StaticOriginSettings os = (StaticOriginSettings) streamer.getHydrophoneOrigin().getOriginSettings();
			GpsData ll = os.getStaticPosition().getGpsData();
			latItem.setValue(ll.getLatitude());
			longItem.setValue(ll.getLongitude());
			return true;
		}
		catch (ClassCastException e) {
			System.out.println("Invalid static origin settings type for logging");
			System.out.println(e.getMessage());
			return false;
		}
		catch (NullPointerException e) {
			System.out.println("Null static origin settings type for logging");
			System.out.println(e.getMessage());
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see Array.streamerOrigin.HydrophoneOriginSystem#extractSQLLoggingFields(Array.StreamerDataUnit)
	 */
	@Override
	public boolean extractSQLLoggingFields(StreamerDataUnit streamerDataUnit) {
		Streamer streamer = streamerDataUnit.getStreamerData();
		try {
			StaticOriginSettings os = (StaticOriginSettings) streamer.getHydrophoneOrigin().getOriginSettings();
			GpsData ll = os.getStaticPosition().getGpsData();
			if (latItem.getValue() != null) {
				ll.setLatitude(latItem.getDoubleValue());
			}
			if (longItem.getValue() != null) {
				ll.setLongitude(longItem.getDoubleValue());
			}
			Streamer streamerData = streamerDataUnit.getStreamerData();
			PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
			int[] streamerList = {streamerData.getStreamerIndex()};
			int[] hydrophoneList = PamUtils.getChannelArray(streamerData.getStreamerHydrophones());
			GpsData referenceGPS = ll;
			PamVector geometricCentre = new PamVector();
			PamVector[] geometry = new PamVector[hydrophoneList.length];
			PamVector[] hydErrors = new PamVector[hydrophoneList.length];
			PamVector[] streamerErrors = new PamVector[1];
			streamerErrors[0] = new PamVector(streamerData.getCoordinateError());
			for (int i = 0; i < hydrophoneList.length; i++) {
				geometry[i] = currentArray.getHydrophoneVector(hydrophoneList[i], streamerDataUnit.getTimeMilliseconds());
				hydErrors[i] = new PamVector(currentArray.getHydrophoneCoordinateErrors(hydrophoneList[i], streamerDataUnit.getTimeMilliseconds()));
			}
			
			SnapshotGeometry geom = new SnapshotGeometry(currentArray, streamerDataUnit.getTimeMilliseconds(), streamerList, hydrophoneList, referenceGPS, geometricCentre, geometry, streamerErrors, hydErrors);
//			streamerDataUnit.setOriginLatLong(new GpsData);
			return true;
		}
		catch (ClassCastException e) {
			System.out.println("Invalid static origin settings type for logging");
			System.out.println(e.getMessage());
			return false;
		}
		catch (NullPointerException e) {
			System.out.println("Null static origin settings type for logging");
			System.out.println(e.getMessage());
			return false;
		}
	}


	@Override
	public String toString() {
		/**
		 * Gets used in the streamer dialog. Wanted a different name but
		 * can't change getName or wont deserialise correctly/
		 */
		return systemNewName;
	}
}
