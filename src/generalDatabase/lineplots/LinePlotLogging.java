package generalDatabase.lineplots;

import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class LinePlotLogging extends SQLLogging {

	private LinePlotControl linePlotControl;
	private List<EnhancedTableItem> dataCols;

	protected LinePlotLogging(LinePlotControl linePlotControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.linePlotControl = linePlotControl;
		PamTableDefinition tableDef = new PamTableDefinition(linePlotControl.getUnitName(), UPDATE_POLICY_WRITENEW);
		dataCols = linePlotControl.getColumnItems();
		for (EnhancedTableItem ti:dataCols) {
			tableDef.addTableItem(ti);
		}
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// never called since this only ever reads data in viewer !
		LinePlotDataUnit linePlotDataUnit = (LinePlotDataUnit) pamDataUnit;
		Object[] data = linePlotDataUnit.getData();
		for (int i = 0; i < data.length; i++) {
			dataCols.get(i).setValue(data[i]);
		}
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		Object[] data = new Object[dataCols.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = dataCols.get(i).getValue();
		}
		LinePlotDataUnit lpdu = new LinePlotDataUnit(linePlotControl, timeMilliseconds, data);
		return lpdu;
	}

}
