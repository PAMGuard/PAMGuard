package detectiongrouplocaliser.logging;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class DetectionGroupSubLogging extends SQLLogging {

	private DetectionGroupLogging detectionGroupLogging;

	public DetectionGroupSubLogging(DetectionGroupLogging detectionGroupLogging, PamDataBlock pamDataBlock) {
		super(pamDataBlock);		
		this.detectionGroupLogging = detectionGroupLogging;
		setTableDefinition(new PamSubtableDefinition(detectionGroupLogging.getDetectionGroupControl().getUnitName()+"_Children"));

	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub

	}

}
