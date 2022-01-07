package seismicVeto;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class VetoLogging extends SQLLogging {

	private VetoController vetoController;
	
	private PamTableDefinition tableDef;
	
	private PamTableItem duration;

	public VetoLogging(PamDataBlock pamDataBlock, VetoController vetoController) {
		super(pamDataBlock);
		this.vetoController = vetoController;
		tableDef = new PamTableDefinition(pamDataBlock.getDataName(), SQLLogging.UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(duration = new PamTableItem("Duration", Types.DOUBLE));
		tableDef.setUseCheatIndexing(true);

		setTableDefinition(tableDef);
	}
//
//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDef;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		VetoDataUnit vetoDataUnit = (VetoDataUnit) pamDataUnit;
		duration.setValue(vetoDataUnit.getDurationInMilliseconds() / 1000.);
	}
	
	
}
