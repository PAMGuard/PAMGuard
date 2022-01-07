package videoRangePanel.importTideData;

import java.sql.Types;

import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class TideSQLLogging extends SQLLogging {

	
	private TideTabelDefinition table;
	

	public TideSQLLogging(PamDataBlock<?> pamDataBlock) {
		super(pamDataBlock);		
//		table=new SLTableDefinition("StaticLocaliserData");
		table=new TideTabelDefinition("Tide_Height_Data");
		setTableDefinition(table);
		
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		TideDataUnit tideDataUnit=(TideDataUnit) pamDataUnit;
		
		table.findTableItem("locTime")		.setValue(tideDataUnit.getTimeMilliseconds());
		table.findTableItem("level_m")		.setValue(tideDataUnit.getLevel());
		table.findTableItem("speed_m_s")	.setValue(tideDataUnit.getSpeed());
		table.findTableItem("angle_rad")	.setValue(tideDataUnit.getAngle());
		if (tideDataUnit.getLocation()!=null){
			table.findTableItem("latitude")		.setValue(tideDataUnit.getLocation().getLatitude());
			table.findTableItem("longitude")	.setValue(tideDataUnit.getLocation().getLongitude());
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long dataTime, int iD) {
		
		TideDataUnit tideData = new TideDataUnit(); 
		
		Object ts = table.getTimeStampItem().getValue();
//		System.out.println("The time stamp is " + ts);
		long t = sqlTypes.millisFromTimeStamp(ts);
//		System.out.println("The time stamp 2  is " + PamCalendar.formatDateTime(t));
		tideData.setTimeMilliseconds(t);
		
		
		tideData.setLevel(table.getTideLevel().getDoubleValue());
		tideData.setAngle(table.getTideAngle().getDoubleValue());
		tideData.setSpeed(table.getTideSpeed().getDoubleValue());

		if (table.getLatitude()!=null){
			LatLong latLong= new LatLong(); 
			latLong.setLatitude(table.getLatitude().getDoubleValue());
			latLong.setLongitude(table.getLongitude().getDoubleValue());
			tideData.setLocation(latLong);

		}
		((PamDataBlock<TideDataUnit>) getPamDataBlock()).addPamData(tideData);
		
		return tideData;
		
	}
	
	
	class TideTabelDefinition extends PamTableDefinition{
		
		//SAVABLE FIELDS
		PamTableItem tideTime			=new PamTableItem("locTime", Types.DOUBLE);
		PamTableItem tideLevel			=new PamTableItem("level_m", Types.DOUBLE);
		PamTableItem tideSpeed			=new PamTableItem("speed_m_s", Types.DOUBLE);
		PamTableItem tideAngle			=new PamTableItem("angle_rad", Types.DOUBLE);
		PamTableItem latitude			=new PamTableItem("latitude", Types.DOUBLE);
		PamTableItem longitude			=new PamTableItem("longitude", Types.DOUBLE);
		
		
		public TideTabelDefinition(String tableName) {
			super(tableName,SQLLogging.UPDATE_POLICY_OVERWRITE);
			addTableItem(tideTime);
			addTableItem(tideLevel);
			addTableItem(tideSpeed);
			addTableItem(tideAngle);
			addTableItem(latitude);
			addTableItem(longitude);
		}


		//SAVABLE FIELDS
		public PamTableItem getTideTime() {
			return tideTime;
		}

		public void setTideTime(PamTableItem tideTime) {
			this.tideTime = tideTime;
		}

		public PamTableItem getTideLevel() {
			return tideLevel;
		}

		public void setTideLevel(PamTableItem tideLevel) {
			this.tideLevel = tideLevel;
		}

		public PamTableItem getTideSpeed() {
			return tideSpeed;
		}

		public void setTideSpeed(PamTableItem tideSpeed) {
			this.tideSpeed = tideSpeed;
		}

		public PamTableItem getTideAngle() {
			return tideAngle;
		}

		public void setTideAngle(PamTableItem tideAngle) {
			this.tideAngle = tideAngle;
		}

		public PamTableItem getLatitude() {
			return latitude;
		}

		public void setLatitude(PamTableItem latitude) {
			this.latitude = latitude;
		}

		public PamTableItem getLongitude() {
			return longitude;
		}

		public void setLongitude(PamTableItem longitude) {
			this.longitude = longitude;
		}


	}
	
}
	
	
