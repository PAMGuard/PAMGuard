package rawDeepLearningClassifier.logging;


import java.sql.Types;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.SuperDetection;
import cpod.CPODClassification;
import cpod.CPODClickTrainDataUnit;
import cpod.CPODUtils;
import cpod.CPODClassification.CPODSpeciesType;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLGroupDetection;


/**
 * Logging for deep learning detection groups. 
 */
public class DLGroupDetectionLogging extends SuperDetLogging {

	private DLControl dlControl;
	
	private PamTableItem duration, startsample;
	
	

	public DLGroupDetectionLogging(DLControl dlControl, SuperDetDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.dlControl=dlControl;
		setTableDefinition(createBaseTable());
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
//		System.out.println("Save deep learning group: " + ((SuperDetection) pamDataUnit).getSubDetectionsCount());
		duration.setValue(pamDataUnit.getDurationInMilliseconds());
		startsample.setValue(pamDataUnit.getStartSample());
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
		tableDef.addTableItem(duration 	= new PamTableItem("Duration_millis", Types.DOUBLE));
		tableDef.addTableItem(startsample 	= new PamTableItem("start_sample", Types.LONGNVARCHAR));

		return tableDef;
	}
	
	
	
	@Override
	protected DLGroupDetection createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {

	
		int chan = this.getTableDefinition().getChannelBitmap().getIntegerValue();
		
		double durationD = duration.getDoubleValue();
		long startsampleL = startsample.getLongValue();

		
		DLGroupDetection dlGroupDet = new DLGroupDetection(timeMilliseconds, chan, startsampleL, durationD);
		
		return dlGroupDet;
	}


}
