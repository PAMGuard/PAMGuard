package difar.crossings;

import java.sql.Types;

import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;

/**
 * Logs DIFAR crossings, one row per crossing. The clips in each crossing are
 * logged in a subtable by {@link DifarCrossingSubLogging}.
 */
public class DifarCrossingLogging extends SuperDetLogging {

	/** Longest match choice name, with room to spare. */
	private static final int MATCH_CHOICE_LENGTH = 20;

	private PamTableItem endTime, clipCount, latitude, longitude, xError, yError, matchChoice;

	/**
	 * @param tableName the table, named after the DIFAR module.
	 * @param dataBlock the crossings.
	 */
	public DifarCrossingLogging(String tableName, DifarCrossingDataBlock dataBlock) {
		super(dataBlock);
		PamTableDefinition tableDef = new PamTableDefinition(tableName, UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(endTime = new PamTableItem("EndTime", Types.TIMESTAMP));
		tableDef.addTableItem(clipCount = new PamTableItem("ClipCount", Types.INTEGER));
		tableDef.addTableItem(latitude = new PamTableItem("Latitude", Types.DOUBLE));
		tableDef.addTableItem(longitude = new PamTableItem("Longitude", Types.DOUBLE));
		tableDef.addTableItem(xError = new PamTableItem("XError", Types.DOUBLE));
		tableDef.addTableItem(yError = new PamTableItem("YError", Types.DOUBLE));
		tableDef.addTableItem(matchChoice = new PamTableItem("MatchChoice", Types.CHAR, MATCH_CHOICE_LENGTH));
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		DifarCrossing crossing = (DifarCrossing) pamDataUnit;
		endTime.setValue(sqlTypes.getTimeStamp(crossing.getEndTimeInMilliseconds()));
		clipCount.setValue(crossing.getSubDetectionsCount());
		LatLong location = crossing.getLocation();
		latitude.setValue(location == null ? null : location.getLatitude());
		longitude.setValue(location == null ? null : location.getLongitude());
		xError.setValue(finiteOrNull(crossing.getXError()));
		yError.setValue(finiteOrNull(crossing.getYError()));
		matchChoice.setValue(crossing.getMatchChoice() == null ? null : crossing.getMatchChoice().name());
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		DifarCrossing crossing = new DifarCrossing(timeMilliseconds);
		Long endUTC = SQLTypes.millisFromTimeStamp(endTime.getValue());
		if (endUTC != null) {
			crossing.setDurationInMilliseconds(endUTC - timeMilliseconds);
		}
		LatLong location = null;
		if (latitude.getValue() != null && longitude.getValue() != null) {
			location = new LatLong(latitude.getDoubleValue(), longitude.getDoubleValue());
		}
		crossing.setResult(location, xError.getDoubleValue(), yError.getDoubleValue());
		crossing.setMatchChoice(parseChoice(matchChoice.getDeblankedStringValue()));
		return crossing;
	}

	private static Double finiteOrNull(double value) {
		return Double.isFinite(value) ? value : null;
	}

	private static DifarCrossing.MatchChoice parseChoice(String name) {
		if (name == null) {
			return null;
		}
		try {
			return DifarCrossing.MatchChoice.valueOf(name.trim());
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
}
