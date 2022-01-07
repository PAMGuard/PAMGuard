package PamUtils.time;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControl;
import generalDatabase.DbSpecial;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class GlobalTimeLogging extends DbSpecial {
	
	private PamTableItem systemTime, correctedTime, correction, systemName, source, updating;
	private GlobalTimeManager globalTimeManager;

	protected GlobalTimeLogging(DBControl dbControl, GlobalTimeManager globalTimeManager) {
		super(dbControl);
		this.globalTimeManager = globalTimeManager;
		PamTableDefinition tableDef = new PamTableDefinition("Global Time", UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(systemTime = new PamTableItem("System Time", Types.TIMESTAMP));
		tableDef.addTableItem(correctedTime = new PamTableItem("Corrected Time", Types.TIMESTAMP));
		tableDef.addTableItem(correction = new PamTableItem("Correction", Types.INTEGER));
		tableDef.addTableItem(systemName = new PamTableItem("Time System", Types.CHAR, 30));
		tableDef.addTableItem(source = new PamTableItem("Time Source", Types.CHAR, 30));
		tableDef.addTableItem(updating = new PamTableItem("Update UTC", Types.BIT));
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		GlobalTimeDataUnit gtdu = (GlobalTimeDataUnit) pamDataUnit;
		TimeCorrection tc = gtdu.getTimeCorrection();
		systemTime.setValue(sqlTypes.getTimeStamp(tc.getSystemTime()));
		correctedTime.setValue(sqlTypes.getTimeStamp(tc.getCorrectedTime()));
		correction.setValue((int) tc.getCorrection());
		systemName.setValue(gtdu.getTimeCorrector().getName());
		source.setValue(tc.getSource());
		updating.setValue(globalTimeManager.getGlobalTimeParameters().isUpdateUTC());
	}

	@Override
	public void pamStart(PamConnection con) {
		
	}

	@Override
	public void pamStop(PamConnection con) {
	}

}
