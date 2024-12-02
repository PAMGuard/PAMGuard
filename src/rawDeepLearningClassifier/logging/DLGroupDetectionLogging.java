package rawDeepLearningClassifier.logging;


import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.SuperDetection;
import generalDatabase.PamTableDefinition;
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
		setTableDefinition(createBaseTable());
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
//		System.out.println("Save deep learning group: " + ((SuperDetection) pamDataUnit).getSubDetectionsCount());
	}

	public DLControl getDLControl() {
		return dlControl;
	}
	
	
	/**
	 * Create the basic table definition for the group detection. 
	 * @return basic table - annotations will be added shortly !
	 */
	public PamTableDefinition createBaseTable() {
		PamTableDefinition tableDef = new PamTableDefinition(dlControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		return tableDef;
	}

}
