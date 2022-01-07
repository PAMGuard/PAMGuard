package networkTransfer.receive;

import java.sql.Connection;
import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class BuoyStatusLogging extends SQLLogging {

	private NetworkReceiver networkReceiver;
	
	PamTableItem buoyId1, buoyId2, streamer, latitude, longitude;

	public BuoyStatusLogging(NetworkReceiver networkReceiver, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.networkReceiver = networkReceiver;
		PamTableDefinition tableDef = new PamTableDefinition(networkReceiver.getUnitName(), SQLLogging.UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(buoyId1 = new PamTableItem("BuoyId1", Types.INTEGER));
		tableDef.addTableItem(buoyId2 = new PamTableItem("BuoyId2", Types.INTEGER));
		tableDef.addTableItem(streamer = new PamTableItem("Streamer", Types.INTEGER));
		tableDef.addTableItem(latitude = new PamTableItem("Latitude", Types.REAL));
		tableDef.addTableItem(longitude = new PamTableItem("Longitude", Types.REAL));
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		BuoyStatusDataUnit bsdu = (BuoyStatusDataUnit) pamDataUnit;
		buoyId1.setValue(bsdu.getBuoyId1());
		buoyId2.setValue(bsdu.getBuoyId2());
		if (bsdu.getHydrophoneStreamer() != null) {
			streamer.setValue(bsdu.getHydrophoneStreamer().getStreamerIndex());
		}
		else {
			streamer.setValue(null);
		}
		if (bsdu.getGpsData() != null) {
			latitude.setValue(double2Float(bsdu.getGpsData().getLatitude()));
			longitude.setValue(double2Float(bsdu.getGpsData().getLongitude()));
		}
		else {
			latitude.setValue(null);
			longitude.setValue(null);
		}
//		battVolts.setValue(double2Float(bsdu.getBattVolts()));
//		chargeCurrent.setValue(double2Float(bsdu.getChargeCurrent()));
//		loadCurrent.setValue(double2Float(bsdu.getLoadCurrent()));

	}

	long lastLogTime = 0;
	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#logData(java.sql.Connection, PamguardMVC.PamDataUnit)
	 */
	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		/**
		 * Don't log buoy data more than once per minute. 
		 */
		if (dataUnit.getLastUpdateTime() - lastLogTime < 60000) {
			return true;
		}
		boolean ans =  super.logData(con, dataUnit);
		if (ans) {
			lastLogTime = dataUnit.getLastUpdateTime();
		}
		return ans;
	}

}
