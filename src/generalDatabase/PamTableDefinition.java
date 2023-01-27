package generalDatabase;

import java.sql.Connection;
import java.sql.Types;
import PamController.PamViewParameters;
import PamUtils.PamCalendar;

/**
 * 
 * Defines a database table for use with the Pamguard database
 * The Pamguard database can query this table definition to 
 * see what columns should be present and to create appropriate 
 * tables. Also used to prepare Sql statements for writing and
 * reading back data.
 * 
 * I did a bit of redifining what columns are used for on 4 Oct, 2012. 
 * PCLocalTime was a UTC time from the PC of the time analysis took place. 
 * When running in real time, this would be the same as the data in the UTC column
 * (give or take the odd second for data to get through the system). I've now defined
 * this to be the UTC time + the timezone offset. I've added another column PCTime which
 * is the system clock from the PC at the time data were stored - which will be different 
 * to UTC if the sound card is sampling too slow or too fast or if you're reprocessing 
 * archived data from file. 
 * 
 * @author Doug Gillespie
 *@see generalDatabase.PamTableItem
 *@see pamDatabase.DatabaseProcess
 *
 */
public class PamTableDefinition extends EmptyTableDefinition implements Cloneable {

	public static final String utcColName = "UTC";
	
	private PamTableItem timeStampItem, timeStampMillis;

	private PamTableItem localTime, pcTime;

	private PamTableItem updateReference;
	
	private PamTableItem uid;
	
	private PamTableItem channelBitmap;
	
	private PamTableItem sequenceBitmap;

	public PamTableDefinition(String tableName) {
		this(tableName, SQLLogging.UPDATE_POLICY_OVERWRITE);
	}
	
	/**
	 * 
	 * @param tableName Name of the database table.
	 * Spaces in the database name will automatically
	 * be replaced by the _ character.
	 */
	public PamTableDefinition(String tableName, int updatePolicy) {
		super(tableName, updatePolicy);
		pamTableItems.add(uid = new PamTableItem("UID", Types.BIGINT, "Unique Identifier"));
		pamTableItems.add(timeStampItem = new PamTableItem(utcColName, Types.TIMESTAMP, "Timestamp UTC"));
		pamTableItems.add(timeStampMillis = new PamTableItem("UTCMilliseconds", Types.SMALLINT, "Time milliseconds (for databases which do not support millis)"));
		pamTableItems.add(localTime = new PamTableItem("PCLocalTime", Types.TIMESTAMP, "Local time on PC"));
		pamTableItems.add(pcTime = new PamTableItem("PCTime", Types.TIMESTAMP, "Time data written, UTC. Same as UTC for real time data, current time for offline file analysis"));
		pamTableItems.add(channelBitmap = new PamTableItem("ChannelBitmap", Types.INTEGER, "Bitmap of input channels used"));
		pamTableItems.add(sequenceBitmap = new PamTableItem("SequenceBitmap", Types.INTEGER, "Bitmap of beam or channel outputs used"));
		if (updatePolicy == SQLLogging.UPDATE_POLICY_WRITENEW) {
			addTableItem(updateReference = new PamTableItem("UpdateOf", Types.INTEGER, "Reference to previous value"));
		}
	}


	private String preparedSelectString;
	@Override
	public String getSQLSelectString(SQLTypes sqlTypes) {
		if (preparedSelectString == null){ 
			preparedSelectString = getSQLSelectString(sqlTypes, null);
		}
		return preparedSelectString;
	}
	/**
	 * gets an sql select string for the table that selects all fields.
	 * Note that some databases don't support the " around a column name, so this
	 * has been omitted, making it impossible to use fields with spaces.  
	 * @return SQL Select string
	 */
	public String getSQLSelectString(SQLTypes sqlTypes, PamViewParameters pamViewParameters) {

		PamTableItem tableItem;

		String sqlString = super.getSQLSelectString(sqlTypes);

		// query between set times
		if (pamViewParameters != null) {
//			String b = getBetweenString(sqlTypes, pamViewParameters);
			String b = pamViewParameters.getSelectClause(sqlTypes);
			if (b != null) {
				sqlString += b;
			}
		}

		// now order by any primary keys...
		int pkCount = 0;
		for (int i = 0; i < getTableItemCount(); i++) {
			tableItem = getTableItem(i);
			if (tableItem.isPrimaryKey() || tableItem.isCounter()) {
				if (pkCount == 0) {
					sqlString += " ORDER BY ";
				}
				else {
					sqlString += ", ";
				}
				sqlString += tableItem.getName();
				pkCount++;
			}

		}
		return sqlString;
	}
	
	/**
	 * Get the where clause for Viewer SQL statements. 
	 * @param pvp
	 * @return clause string
	 */
	private String getBetweenString(SQLTypes sqlTypes, PamViewParameters pvp) {
		if (pvp.analEndTime == 0 && pvp.analStartTime == 0 && 
				pvp.viewStartTime == 0 && pvp.viewEndTime == 0) {
			return "";
		}

		String str = null;
		String wrappedTableName = getTableName();
		if (pvp.viewStartTime != 0 && pvp.viewEndTime != 0) {
			str = String.format(" WHERE %s.UTC BETWEEN %s AND %s", wrappedTableName, 
					sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewStartTime()),
					sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewEndTime()));
		}
		else if (pvp.viewStartTime != 0) {
			str = String.format(" WHERE %s.UTC > %s ", wrappedTableName, 
					sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewStartTime()));
		}
		else if (pvp.viewEndTime != 0) {
			str = String.format(" WHERE %s.UTC < %s ", wrappedTableName, 
					sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewEndTime()));
		}
		if (pvp.useAnalysisTime) {
			if (str != null) {
				str += " AND ";
			}
			else {
				str = " WHERE ";
			}

			if (pvp.analStartTime != 0 && pvp.analEndTime != 0) {
				str += String.format(" (LocalTime BETWEEN %s AND %s OR LocalTime BETWEEN %s AND %s)", 
						sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedAnalStartTime()),
						sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedAnalEndTime()),
						sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewStartTime()),
						sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedViewEndTime()));
			}
			else if (pvp.analStartTime != 0) {
				str += String.format(" LocalTime > %s ", 
						sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedAnalStartTime()));
			}
			else if (pvp.analEndTime != 0) {
				str += String.format(" LocalTime < %s ", 
						sqlTypes.formatDBDateTimeQueryString(pvp.getRoundedAnalEndTime()));
			}
		}

		return str;
	}

	public PamTableItem getTimeStampItem() {
		return timeStampItem;
	}

	public PamTableItem getTimeStampMillis() {
		return timeStampMillis;
	}
	
	public PamTableItem getPCTimeItem() {
		return pcTime;
	}
	
	public PamTableItem getLocalTimeItem() {
		return localTime;
	}

	/**
	 * @return the uid table item
	 */
	public PamTableItem getUidItem() {
		return uid;
	}
	
	public PamTableItem getUpdateReference() {
		return updateReference;
	}

	public void setUpdateReference(PamTableItem updateReference) {
		this.updateReference = updateReference;
	}
	
	public PamTableItem getChannelBitmap() {
		return channelBitmap;
	}
	
	public void setChannelBitmap(PamTableItem channelBitmap) {
		this.channelBitmap = channelBitmap;
	}
	
	public PamTableItem getSequenceBitmap() {
		return sequenceBitmap;
	}
	
	public void setSequenceBitmap(PamTableItem sequenceBitmap) {
		this.sequenceBitmap = sequenceBitmap;
	}

	@Override
	protected PamTableDefinition clone() {
		return (PamTableDefinition) super.clone();
	}

}
