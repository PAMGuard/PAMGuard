package noiseOneBand.offline;

import java.sql.Types;

import noiseOneBand.OneBandControl;
import noiseOneBand.OneBandLogging;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class OfflineOneBandLogging extends OneBandLogging {

	PamTableItem intervalItem;
	PamTableItem numberItem;
	
	protected OfflineOneBandLogging(OneBandControl oneBandControl,
			PamDataBlock pamDataBlock) {
		super(oneBandControl, pamDataBlock);
		
		getTableDefinition().addTableItem(intervalItem = new PamTableItem("Interval", Types.INTEGER));
		getTableDefinition().addTableItem(numberItem = new PamTableItem("DataCount", Types.INTEGER));
		String newName = oneBandControl.getUnitName()+"_Summary";
		getTableDefinition().setTableName(newName);
	}


	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);
		OfflineOneBandDataUnit du = (OfflineOneBandDataUnit) pamDataUnit;
		intervalItem.setValue(new Integer(du.getInterval()));
		numberItem.setValue(new Integer(du.getnDatas()));
		
	}

}
