package rawDeepLearningClassifier.logging;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import rawDeepLearningClassifier.DLControl;


/**
 * Logging for deep learning detection groups. 
 */
public class DLGroupDetectionLogging extends SuperDetLogging {

	private DLControl dlControl;

	public DLGroupDetectionLogging(DLControl dlControl, SuperDetDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.dlControl=dlControl;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		
	}

	public DLControl getDLControl() {
		return dlControl;
	}

}
