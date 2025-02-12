package rawDeepLearningClassifier.logging;

import java.sql.Types;
import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDataUnit;
import rawDeepLearningClassifier.dlClassification.DLGroupDetection;
import rawDeepLearningClassifier.dlClassification.PredictionResult;

/**
 * Saves the classified DL data units to a database. 
 * TODO
 * 
 * @author Jamie Macaulay
 *
 */
public class DLResultLogging  extends SQLLogging { 
	
	
	/**
	 * The PAM items for saving.
	 */
	private PamTableItem analysisTime, predicition, startSample, typeTableItem;
	
	/**
	 * Reference to the DLControl. 
	 */
	private DLControl dlControl;



	public DLResultLogging(DLControl dlControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock); 
		this.dlControl=dlControl; 
		setTableDefinition(createBaseTable());
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		DLDataUnit dgdu = (DLDataUnit) pamDataUnit;
		startSample.setValue(pamDataUnit.getStartSample());
		analysisTime.setValue(dgdu.getPredicitionResult().getAnalysisTime());
		typeTableItem.setValue(ModelResultBinaryFactory.getType(dgdu.getPredicitionResult()));

		ArrayList<PredictionResult> predictionResult = new ArrayList<PredictionResult>(); 
		predictionResult.add(dgdu.getPredicitionResult());
		
		predicition.setValue(
				DLAnnotationSQL.prediction2String(predictionResult));
		
		
		
		
	}
	
	/**
	 * Create the basic table definition for the group detection. 
	 * @return basic table - annotations will be added shortly !
	 */
	public PamTableDefinition createBaseTable() {
		PamTableDefinition tableDef = new PamTableDefinition(dlControl.getUnitName()+"_Predictions", UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(analysisTime 	= new PamTableItem("Processing_time", Types.DOUBLE));
		tableDef.addTableItem(startSample 	= new PamTableItem("Start_sample", Types.LONGNVARCHAR));
		tableDef.addTableItem(predicition 	= new PamTableItem("Predicition", Types.CHAR, 8128));
		tableDef.addTableItem(typeTableItem = new PamTableItem("Prediction_Type", Types.INTEGER, "The type flag for the predictions"));

		return tableDef;
	}
		
	
	@Override
	protected DLDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
	
		int chan = this.getTableDefinition().getChannelBitmap().getIntegerValue();
		
		long startsampleL = startSample.getLongValue();
		
		String predictionsString = (String) predicition.getValue();
		Integer predicitonType = (Integer) typeTableItem.getValue();
		
		if (predictionsString!=null) {

			ArrayList<PredictionResult> modelResults =  DLAnnotationSQL.string2Predictions( predictionsString,  predicitonType);
			DLDataUnit dlDataUnit = new DLDataUnit(timeMilliseconds,  chan, startsampleL, dlControl.getDLParams().rawSampleSize, modelResults.get(0)); 
			
			return dlDataUnit; 

		}		
		
		return null;
	}


}
