package qa.operations;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.lookupTables.LookUpTables;

public class QAOpsLogging extends SQLLogging{

	private QAOpsDataBlock opsDataBlock;
	
	private PamTableItem code, text;
	

	protected QAOpsLogging(QAOpsDataBlock opsDataBlock) {
		super(opsDataBlock);
		this.opsDataBlock = opsDataBlock;
		PamTableDefinition tableDef = new PamTableDefinition("SIDE_Operations");
		tableDef.addTableItem(code = new PamTableItem("Code", Types.CHAR, LookUpTables.CODE_LENGTH));
		tableDef.addTableItem(text = new PamTableItem("Text", Types.CHAR, LookUpTables.TEXT_LENGTH));
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		QAOpsDataUnit opsDataUnit = (QAOpsDataUnit) pamDataUnit;
		code.setValue(opsDataUnit.getOpsStatusCode());
		text.setValue(opsDataUnit.getOpsStatusName());
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(generalDatabase.SQLTypes, long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		String opsCode = code.getDeblankedStringValue();
		String opsTxt = text.getDeblankedStringValue();
		return new QAOpsDataUnit(timeMilliseconds, opsCode, opsTxt);
	}


}
