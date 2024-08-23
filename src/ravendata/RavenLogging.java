package ravendata;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import PamDetection.AcousticSQLLogging;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

public class RavenLogging extends AcousticSQLLogging {

	private RavenDataBlock ravenDataBlock;
	private RavenControl ravenControl;
	private ArrayList<RavenColumnInfo> usedExtraColumns;
	private PamTableItem[] extraItems;
	
	public RavenLogging(RavenControl ravenControl, RavenDataBlock pamDataBlock) {
		super(pamDataBlock, ravenControl.getUnitName());
		this.ravenControl = ravenControl;
		this.ravenDataBlock = pamDataBlock;
	}

	@Override
	protected RavenDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int chanMap, long duration,
			double[] f) {
		RavenDataUnit ravenDataUnit = new RavenDataUnit(timeMilliseconds, chanMap, duration, f[0], f[1]);

		if (extraItems == null || usedExtraColumns == null) {
			return ravenDataUnit;
		}
		HashMap<String, String> extraData = new HashMap<>();
		for (int i = 0; i < usedExtraColumns.size(); i++) {
			String data = extraItems[i].getDeblankedStringValue();
			if (data != null && data.length() > 0) {
				extraData.put(usedExtraColumns.get(i).name, data);
			}
		}
		ravenDataUnit.setExtraData(extraData);
		
		return ravenDataUnit;
	}

	public void addExtraColumns(ArrayList<RavenColumnInfo> extraColumns) {
		this.usedExtraColumns = new ArrayList<RavenColumnInfo>();
		PamTableDefinition baseTable = getBaseTableDefinition();
		for (int i = 0; i < extraColumns.size(); i++) {
			if (extraColumns.get(i).sqlType == null) {
				continue;
			}
			usedExtraColumns.add(extraColumns.get(i));
		}
		extraItems = new PamTableItem[usedExtraColumns.size()];
		for (int i = 0; i < usedExtraColumns.size(); i++) {
			RavenColumnInfo col = usedExtraColumns.get(i);
			extraItems[i] = new PamTableItem(col.name, Types.CHAR, Math.max(col.maxStrLength,30));
			baseTable.addTableItem(extraItems[i]);
		}
		setTableDefinition(baseTable);
		DBControlUnit dbc = DBControlUnit.findDatabaseControl();
		if (dbc != null) {
			dbc.getDbProcess().checkTable(baseTable);
		}
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);
		// and do the extras
		RavenDataUnit ravenDataUnit = (RavenDataUnit) pamDataUnit;
		HashMap<String, String> extraData = ravenDataUnit.getExtraData();
		if (extraData == null || extraItems == null || usedExtraColumns == null) {
			return;
		}
		for (int i = 0; i < usedExtraColumns.size(); i++) {
			String data = extraData.get(usedExtraColumns.get(i).name);
			extraItems[i].setValue(data);
		}
	}


}
