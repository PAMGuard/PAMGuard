package clickTrainDetector.logging;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;

/**
 * Logs the the children of the click train detection. 
 * @author Jamie Macaulay
 *
 */
public class ClickTrainDetSubLogging extends SQLLogging {

	private ClickTrainDetLogging clickTrainLogging;

	public ClickTrainDetSubLogging(ClickTrainDetLogging clickTrainLogging, PamDataBlock pamDataBlock) {
		super(pamDataBlock);		
		this.clickTrainLogging = clickTrainLogging;
		setTableDefinition(new PamSubtableDefinition(clickTrainLogging.getClickTrainControl().getUnitName()+"_Children"));
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub

	}

}
