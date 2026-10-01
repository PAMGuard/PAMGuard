package difar;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.StringJoiner;

import GPS.GpsData;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Database storage of DIFAR information. May as well write out the buoy lat and long. 
 * These aren't needed for Viewer operation, but will help with any further offline analysis
 * of the data that get's done. The only really interesting things in the data are the angle and 
 * the species selection. 
 * @author Doug Gillespie
 *
 */
public class DifarSqlLogging extends SQLLogging {

	private DifarControl difarControl;
	private DifarDataBlock difarDataBlock;
	private PamTableItem channel, clipLength, hydrophoneLatitude, hydrophoneLongitude, relativeAngle, trueAngle;
	private PamTableItem difarFrequency, buoyHeading, species;
	private PamTableItem latitude, longitude, xError, yError, matchedUnits; 
	private PamTableItem difarGain, sigAmplitude;
	private PamTableItem triggerName, trackedGroup;
	private PamTableItem buoyName, deploymentUID;

	/** Buoy names are typed by the operator; this is generous. */
	private static final int BUOY_NAME_LENGTH = 80;

	protected DifarSqlLogging(DifarControl difarControl, DifarDataBlock difarDataBlock) {
		super(difarDataBlock);
		this.difarControl = difarControl;
		this.difarDataBlock = difarDataBlock;
		PamTableDefinition tableDef = new PamTableDefinition(difarControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(channel = new PamTableItem("Channel", Types.INTEGER)); 
		tableDef.addTableItem(clipLength = new PamTableItem("ClipLength", Types.DOUBLE)); 
		tableDef.addTableItem(hydrophoneLatitude = new PamTableItem("BuoyLatitude", Types.DOUBLE)); 
		tableDef.addTableItem(hydrophoneLongitude = new PamTableItem("BuoyLongitude", Types.DOUBLE)); 
		tableDef.addTableItem(triggerName = new PamTableItem("TriggerName", Types.CHAR, 80)); 
		tableDef.addTableItem(buoyHeading = new PamTableItem("BuoyHeading", Types.DOUBLE)); 
		tableDef.addTableItem(relativeAngle = new PamTableItem("DIFARBearing", Types.DOUBLE)); 
		tableDef.addTableItem(trueAngle = new PamTableItem("TrueBearing", Types.DOUBLE)); 
		tableDef.addTableItem(difarFrequency = new PamTableItem("DifarFrequency", Types.DOUBLE)); 
		tableDef.addTableItem(sigAmplitude = new PamTableItem("SignalAmplitude", Types.DOUBLE)); 
		tableDef.addTableItem(difarGain = new PamTableItem("DifarGain", Types.DOUBLE)); 
		tableDef.addTableItem(species = new PamTableItem("Species", Types.CHAR, 20)); 
		tableDef.addTableItem(latitude = new PamTableItem("Latitude", Types.DOUBLE)); 
		tableDef.addTableItem(longitude = new PamTableItem("Longitude", Types.DOUBLE)); 
		tableDef.addTableItem(xError = new PamTableItem("XError", Types.DOUBLE));
		tableDef.addTableItem(yError = new PamTableItem("YError", Types.DOUBLE));
		tableDef.addTableItem(matchedUnits = new PamTableItem("MatchedAngles", Types.CHAR, 80));
		tableDef.addTableItem(trackedGroup = new PamTableItem("TrackedGroup", Types.CHAR, 80));
		tableDef.addTableItem(buoyName = new PamTableItem("BuoyName", Types.CHAR, BUOY_NAME_LENGTH));
		tableDef.addTableItem(deploymentUID = new PamTableItem("DeploymentUID", Types.BIGINT));
		
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		DifarDataUnit difarDataUnit = (DifarDataUnit) pamDataUnit;
		int chan = PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap());
		channel.setValue(chan);
		clipLength.setValue((double) (difarDataUnit.getSampleDuration() / difarControl.getDifarProcess().getSampleRate()));
		triggerName.setValue(difarDataUnit.triggerName);
		GpsData origin = difarDataUnit.getOriginLatLong(false);
		if (origin != null) {
			hydrophoneLatitude.setValue(origin.getLatitude());
			hydrophoneLongitude.setValue(origin.getLongitude());
			buoyHeading.setValue(origin.getTrueHeading());
		}
		else {
			hydrophoneLatitude.setValue(null);
			hydrophoneLongitude.setValue(null);
			buoyHeading.setValue(null);
		}
		relativeAngle.setValue(difarDataUnit.getSelectedAngle());
		if (origin != null && origin.getTrueHeading() != null) {
			trueAngle.setValue(difarDataUnit.getSelectedAngle() + origin.getTrueHeading());
		}
		else {
			trueAngle.setValue(null);
		}
		difarGain.setValue(difarDataUnit.getDifarGain());
		sigAmplitude.setValue(difarDataUnit.getAmplitudeDB());
		
		difarFrequency.setValue(difarDataUnit.getSelectedFrequency());
		species.setValue(difarDataUnit.getSpeciesCode());
		/*
		 * Crossings are stored in their own tables. These columns stay only so
		 * that old databases still open, and are empty in new rows.
		 */
		latitude.setValue(null);
		longitude.setValue(null);
		xError.setValue(null);
		yError.setValue(null);
		matchedUnits.setValue(null);
		SonobuoyRecord buoy = difarDataUnit.getBuoyRecord();
		buoyName.setValue(buoy == null ? null : buoy.getName());
		deploymentUID.setValue(buoy == null ? null : buoy.getUid());
		if (difarDataUnit.getTrackedGroup() == null){
			trackedGroup.setValue(null);
		}
		else {
			trackedGroup.setValue(difarDataUnit.getTrackedGroup());
		}
		
	}

	/**
	 * Bring the columns that come from the buoy up to date in a clip's row,
	 * after the buoy changed: its position, heading, true bearing, name and
	 * deployment UID. The row is found by UID, so this works for clips loaded
	 * from binary files in the viewer, which have no database index. Nothing
	 * else in the row changes, and the clip's binary object is not touched.
	 * @param connection the database connection.
	 * @param clip a saved clip.
	 * @return the number of rows updated: 1, or 0 if the clip has no row.
	 */
	public synchronized int updateBuoyColumns(PamConnection connection, DifarDataUnit clip) {
		SQLTypes sqlTypes = connection.getSqlTypes();
		setTableData(sqlTypes, clip);
		PamTableItem[] items = {hydrophoneLatitude, hydrophoneLongitude, buoyHeading, trueAngle, buoyName, deploymentUID};
		StringJoiner set = new StringJoiner(", ");
		for (PamTableItem item : items) {
			set.add(sqlTypes.formatColumnName(item) + " = ?");
		}
		String sql = String.format("UPDATE %s SET %s WHERE UID = ?",
				sqlTypes.formatTableName(getTableDefinition().getTableName()), set);
		try (PreparedStatement statement = connection.getConnection().prepareStatement(sql)) {
			int i = 1;
			for (PamTableItem item : items) {
				if (item.getValue() == null) {
					statement.setNull(i++, item.getSqlType());
				}
				else {
					statement.setObject(i++, item.getValue(), item.getSqlType());
				}
			}
			statement.setLong(i, clip.getUID());
			return statement.executeUpdate();
		}
		catch (SQLException e) {
			System.out.println("DIFAR: could not update the buoy columns of clip UID " + clip.getUID() + ": " + e.getMessage());
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		return null;
		// use the binary store if you want to read difar units back into memory
//		int chan = channel.getIntegerValue();
//		double clipLength = this.clipLength.getDoubleValue();
//		String trigName = triggerName.getDeblankedStringValue();
//		Double hLat = (Double) hydrophoneLatitude.getValue();
//		Double hLong = (Double) hydrophoneLongitude.getValue();
//		double relAngle = relativeAngle.getDoubleValue();
//		Double trueAngle = (Double) this.trueAngle.getDoubleValue();
//		double difarFreq = difarFrequency.getDoubleValue();
//		String spCode = species.getDeblankedStringValue();
//		Double lat = (Double) latitude.getValue();
//		Double lon = (Double) longitude.getValue();
//		String matches = matchedUnits.getDeblankedStringValue();
//		
//		long samples = (long) (clipLength * difarControl.getDifarProcess().getSampleRate());
//		DifarDataUnit difarDataUnit = new DifarDataUnit(timeMilliseconds, timeMilliseconds, 0, samples, 1<<chan, 
//				null, trigName, null, timeMilliseconds, null, frequencyRange, sourceSampleRate, displaySampleRate)
	}

}
