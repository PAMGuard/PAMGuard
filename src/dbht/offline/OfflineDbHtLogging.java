package dbht.offline;

import java.sql.Types;

import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dbht.DbHtControl;
import dbht.DbHtLogging;

public class OfflineDbHtLogging extends DbHtLogging {

	PamTableItem intervalItem;
	PamTableItem numberItem;
	
	protected OfflineDbHtLogging(DbHtControl dbHtControl,
			PamDataBlock pamDataBlock) {
		super(dbHtControl, pamDataBlock);
		
		getTableDefinition().addTableItem(intervalItem = new PamTableItem("Interval", Types.INTEGER));
		getTableDefinition().addTableItem(numberItem = new PamTableItem("DataCount", Types.INTEGER));
		String newName = dbHtControl.getUnitName()+"_Summary";
		getTableDefinition().setTableName(newName);
	}


	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);
		OfflineDbHtDataUnit du = (OfflineDbHtDataUnit) pamDataUnit;
		intervalItem.setValue(new Integer(du.getInterval()));
		numberItem.setValue(new Integer(du.getnDatas()));
		
	}

}
