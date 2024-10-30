package rawDeepLearningClassifier.logging;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;


/**
 * Logs the the children of the deep learning detection group. 
 * 
 * @author Jamie Macaulay
 */
public class DLGroupSubLogging extends SQLLogging {

	private DLGroupDetectionLogging dlGroupLogging;

	public DLGroupSubLogging(DLGroupDetectionLogging dlGroupDetLogging, PamDataBlock pamDataBlock) {
		super(pamDataBlock);		
		this.dlGroupLogging = dlGroupDetLogging;
		setTableDefinition(new PamSubtableDefinition(dlGroupDetLogging.getDLControl().getUnitName()+"_Children"));
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub

	}



}
