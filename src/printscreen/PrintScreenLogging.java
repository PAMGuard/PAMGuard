package printscreen;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class PrintScreenLogging extends SQLLogging {
	
	private PamTableItem fileName, frameNumber;
	private PrintScreenControl printScreenControl;

	public PrintScreenLogging(PrintScreenControl printScreenControl) {
		super(printScreenControl.getPrintScreenDataBlock());
		this.printScreenControl = printScreenControl;
		setTableDefinition(createBaseTable());
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		PrintScreenDataUnit psdu = (PrintScreenDataUnit) pamDataUnit;
		frameNumber.setValue(psdu.getFrameNumber());
		fileName.setValue(psdu.getImageFile());
	}

	public PamTableDefinition createBaseTable() {
		PamTableDefinition tableDef = new PamTableDefinition(printScreenControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(frameNumber = new PamTableItem("Frame", Types.INTEGER));
		tableDef.addTableItem(fileName = new PamTableItem("File", Types.CHAR, 50));
		return tableDef;
	}

}
