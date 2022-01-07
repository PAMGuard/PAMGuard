package rawDeepLearningClassifier.logging;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDataUnit;

/**
 * Saves the classified DL data units to a database. 
 *
 * 
 * @author Jamie Macaulay
 *
 */
public class DLLogging  extends SQLLogging { 
	
	
	/**
	 * The PAM items for saving.
	 */
	private PamTableItem analysisTime, predicition;
	
	/**
	 * Reference to the DLControl. 
	 */
	private DLControl dlControl;



	public DLLogging(DLControl dlControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock); 
		this.dlControl=dlControl; 
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		DLDataUnit dgdu = (DLDataUnit) pamDataUnit;
		analysisTime.setValue(dgdu.getPredicitionResult().getAnalysisTime());
		predicition.setValue(dgdu.getPredicitionResult().getPrediction());
	}
	

	/**
	 * Create the basic table definition for the group detection. 
	 * @return basic table - annotations will be added shortly !
	 */
	public PamTableDefinition createBaseTable() {
		PamTableDefinition tableDef = new PamTableDefinition(dlControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(analysisTime 	= new PamTableItem("Analysis_Time", Types.DOUBLE));
		tableDef.addTableItem(predicition 	= new PamTableItem("Predicition", Types.DOUBLE));
		return tableDef;
	}
	
	//TODO viewer mode. 

}
