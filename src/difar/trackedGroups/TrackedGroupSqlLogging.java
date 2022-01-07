package difar.trackedGroups;

import java.sql.Types;
import java.util.ListIterator;

import pamScrollSystem.ViewLoadObserver;
import difar.DifarControl;
import PamController.PamViewParameters;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
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
 * @author Brian Miller
 *
 */
public class TrackedGroupSqlLogging extends SQLLogging {

	private DifarControl difarControl;
	private TrackedGroupDataBlock difarDataBlock;
	private PamTableItem groupName, channel, buoyStartTime, buoyName; 
	private PamTableItem meanBearing, bearingSTD;
	private PamTableItem firstBearing, firstDetectionTime,  lastBearing, lastDetectionTime;
	private PamTableItem groupLatitude, groupLongitude;
	private PamTableItem latitude, longitude;
	private PamTableItem numBearings;

	protected TrackedGroupSqlLogging(DifarControl difarControl, TrackedGroupDataBlock difarDataBlock) {
		super(difarDataBlock);
		this.difarControl = difarControl;
		this.difarDataBlock = difarDataBlock;
		super.setUpdatePolicy(UPDATE_POLICY_OVERWRITE);
		PamTableDefinition tableDef = new PamTableDefinition(difarControl.getUnitName() + "Tracked Groups", UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(groupName = new PamTableItem("GroupName", Types.CHAR, 80));
		tableDef.addTableItem(buoyName = new PamTableItem("BuoyName", Types.CHAR, 80));
		tableDef.addTableItem(latitude = new PamTableItem("BuoyLatitude", Types.DOUBLE)); 
		tableDef.addTableItem(longitude = new PamTableItem("BuoyLongitude", Types.DOUBLE)); 
		tableDef.addTableItem(channel = new PamTableItem("Channel", Types.INTEGER)); 		 
		tableDef.addTableItem(buoyStartTime = new PamTableItem("BuoyTime", Types.TIMESTAMP)); 
		tableDef.addTableItem(firstBearing = new PamTableItem("FirstBearing", Types.DOUBLE));
		tableDef.addTableItem(firstDetectionTime = new PamTableItem("FirstDetectionTime", Types.TIMESTAMP)); 
		tableDef.addTableItem(lastBearing = new PamTableItem("LastBearing", Types.DOUBLE)); 
		tableDef.addTableItem(lastDetectionTime = new PamTableItem("LastDetectionTime", Types.TIMESTAMP)); 
		tableDef.addTableItem(numBearings = new PamTableItem("NumBearings", Types.INTEGER));
		tableDef.addTableItem(meanBearing = new PamTableItem("MeanBearing", Types.DOUBLE)); 
		tableDef.addTableItem(bearingSTD = new PamTableItem("MeanBearingStandardDev", Types.DOUBLE));
		tableDef.addTableItem(groupLatitude = new PamTableItem("GroupLatitude", Types.DOUBLE)); 
		tableDef.addTableItem(groupLongitude = new PamTableItem("GroupLongitude", Types.DOUBLE)); 
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		TrackedGroupDataUnit groupDataUnit = (TrackedGroupDataUnit) pamDataUnit;
		groupName.setValue(groupDataUnit.getGroupName());
		buoyName.setValue(groupDataUnit.getBuoyName());

		int chan = PamUtils.getSingleChannel(groupDataUnit.getChannelBitmap());
		channel.setValue(chan);
		
		LatLong oll = groupDataUnit.getOriginLatLong(false);
		latitude.setValue(oll.getLatitude());
		longitude.setValue(oll.getLongitude());
		buoyStartTime.setValue(sqlTypes.getTimeStamp(groupDataUnit.getBuoyStartTime()));
		firstBearing.setValue(groupDataUnit.getFirstBearing());
		firstDetectionTime.setValue(sqlTypes.getTimeStamp(groupDataUnit.getFirstDetectionTime()));
		lastBearing.setValue(groupDataUnit.getMostRecentBearing());
		lastDetectionTime.setValue(sqlTypes.getTimeStamp(groupDataUnit.getMostRecentDetectionTime()));
		numBearings.setValue(groupDataUnit.getNumBearings());
		meanBearing.setValue(groupDataUnit.getMeanBearing());
		bearingSTD.setValue(groupDataUnit.getBearingSTD());
		if (groupDataUnit.getDifarCrossing()!=null)
			if (groupDataUnit.getDifarCrossing().getCrossLocation() != null) {
				groupLatitude.setValue(groupDataUnit.getDifarCrossing().getCrossLocation().getLatitude());
				groupLongitude.setValue(groupDataUnit.getDifarCrossing().getCrossLocation().getLongitude());
			}

	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		
		String groupName = this.groupName.getDeblankedStringValue();
		int channelBitmap = 1<<channel.getIntegerValue();
		double meanBearing = this.meanBearing.getDoubleValue();
		double bearingSTD = this.bearingSTD.getDoubleValue();
		double firstBearing = this.firstBearing.getDoubleValue();
		double lastBearing = this.lastBearing.getDoubleValue();
		long lastDetectionTime = sqlTypes.millisFromTimeStamp(this.lastDetectionTime.getValue());
		
		int n = numBearings.getIntegerValue();
		
		TrackedGroupDataUnit tgdu = new TrackedGroupDataUnit(timeMilliseconds, channelBitmap,
				groupName, firstBearing, lastBearing, lastDetectionTime, 
				meanBearing, bearingSTD, n); 
		return tgdu;
		
	}
	
	
	
	/**
	 * Automatically fills table data columns that can be done automatically
	 * (counters, primary keys and columns cross referenced to data in other
	 * tables). The abstract function setTableData is then called to fill in the
	 * other columns with detector specific data.
	 * 
	 * @param pamDataUnit
	 */
	protected void updateData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		PamTableDefinition tableDef = getTableDefinition();
		PamTableItem tableItem;

		tableDef.getIndexItem().setValue(pamDataUnit.getDatabaseIndex());
		/*
		 * All tables have a timestamp near the front of the table. And all data
		 * units have a time in milliseconds, so always fill this in !
		 */
		tableDef.getTimeStampItem().setValue(
				sqlTypes.getTimeStamp(pamDataUnit.getTimeMilliseconds()));

		tableDef.getTimeStampMillis().setValue((int) (pamDataUnit.getTimeMilliseconds()%1000));

		tableDef.getLocalTimeItem().setValue(sqlTypes.getLocalTimeStamp(pamDataUnit.getTimeMilliseconds()));
		
		tableDef.getPCTimeItem().setValue(sqlTypes.getTimeStamp(System.currentTimeMillis()));
		
		if (tableDef.getUpdateReference() != null) {
			tableDef.getUpdateReference().setValue(pamDataUnit.getDatabaseIndex());
		}

		for (int i = 0; i < tableDef.getTableItemCount(); i++) {

			tableItem = tableDef.getTableItem(i);
			if (tableItem.getCrossReferenceItem() != null) {
				tableItem.setValue(tableItem.getCrossReferenceItem().getValue());
			}
		}

		setTableData(sqlTypes, pamDataUnit);
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		PamConnection connection = dbControl.getConnection();
		reLogData(connection, pamDataUnit);
	
	}
	
	/**
	 * After loading the tracked groups, triangulate the positions of
	 * all the loaded groups
	 */
	@Override
	public boolean loadViewData(PamConnection con,
			PamViewParameters pamViewParameters, ViewLoadObserver loadObserver) {
		if (super.loadViewData(con, pamViewParameters, loadObserver)){
			ListIterator<PamDataUnit> listIterator = difarDataBlock.getListIterator(PamDataBlock.ITERATOR_END);

			while (listIterator.hasPrevious()) {
				TrackedGroupDataUnit unit = (TrackedGroupDataUnit) listIterator.previous();

				// Don't even consider the DefaultGroup, since this group will
				//  be a hodgepodge of miscellaneous bearings
				if (unit.getGroupName().equals(difarControl.getDifarParameters().DefaultGroup)) continue;

				difarControl.getTrackedGroupProcess().getDifarRangeInfo(unit);
			}

			return true;
		}
		return false;
	}


}
