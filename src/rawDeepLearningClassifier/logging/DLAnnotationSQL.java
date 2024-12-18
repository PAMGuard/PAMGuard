package rawDeepLearningClassifier.logging;

import java.sql.Types;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;
import rawDeepLearningClassifier.dlClassification.PredictionResult;


/**
 * Log deep learning annotation to the database
 * 
 * @author Jamie Macaulay
 */
public class DLAnnotationSQL implements SQLLoggingAddon   {
	
	DLAnnotationType dlAnnotationType;
	

	private static final String PREDICTIONS_JSON_KEY = "predictions";


	private static final String CLASSIDS_JSON_KEY = "class_id"; 

	/**
	 * Holds predictions
	 */
	private PamTableItem predictionsItem;


	private PamTableItem typeTableItem;

	public DLAnnotationSQL(DLAnnotationType dlAnnotationType) {
		this.dlAnnotationType = dlAnnotationType;		
		predictionsItem = new PamTableItem("Prediction", Types.CHAR, 8128);
		typeTableItem = new PamTableItem("Prediction_Type", Types.INTEGER, "The type flag for the predictions");
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(predictionsItem);
		pamTableDefinition.addTableItem(typeTableItem);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		
//		System.out.println("SAVE SQL DL Annotation data: " + pamDataUnit); 
		DLAnnotation dlAnnotation = (DLAnnotation) pamDataUnit.findDataAnnotation(DLAnnotation.class);
		if (dlAnnotation == null) {
			predictionsItem.setValue(null);
		}
		else {
			predictionsItem.setValue(prediction2String(dlAnnotation.getModelResults()));
			typeTableItem.setValue(ModelResultBinaryFactory.getType(dlAnnotation.getModelResults().get(0)));

		}
		return false;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {

		String predictionsString = (String) predictionsItem.getValue();
		Integer predicitonType = (Integer) typeTableItem.getValue();

		if (predictionsString!=null) {

			ArrayList<PredictionResult> modelResults =  string2Predictions( predictionsString,  predicitonType);
			
			DLAnnotation annotation = new DLAnnotation(dlAnnotationType, modelResults);

			pamDataUnit.addDataAnnotation(annotation);

			return true;
		}		
		return false;
	}

	@Override
	public String getName() {
		return dlAnnotationType.getAnnotationName();
	}
	
	/**
	 * Create a list of predictions from a JSON string stored in a database table. 
	 * @param predictionsString - JSON string
	 * @param predicitonType - the prediction object type e.g.ModelResultBinaryFactory.GENERIC
	 * @return a list of prediction objects. 
	 */
	private ArrayList<PredictionResult> string2Predictions(String predictionsString, int predicitonType) {
		ArrayList<PredictionResult> modelResults = new ArrayList<PredictionResult>(); 

	    JSONObject jsonObject = new JSONObject(predictionsString);
	    
	    JSONArray classIDArray = jsonObject.getJSONArray(CLASSIDS_JSON_KEY);
	    short[] classIDs = new short[classIDArray.length()];
	    for (int i=0; i<classIDArray.length(); i++) {
	    	classIDs[i] = (short) classIDArray.getInt(i);
	    }
	    
	    JSONArray array = jsonObject.getJSONArray(PREDICTIONS_JSON_KEY);
	    float[][] predicitons = new float[array.length()][];
	    
	    PredictionResult predicitonResult;
    	float[] predictions;
	    for (int i=0; i<predicitons.length; i++) {
	    	
	    	predictions= new float[array.getJSONArray(i).length()];
	    	
	    	for(int j=0; j< array.getJSONArray(i).length(); j++) {
	    		predictions[j] =  array.getJSONArray(i).getBigDecimal(j).floatValue();
	    	}
	    	
	    	predicitonResult= ModelResultBinaryFactory.makePredictionResult(predicitonType, predictions, classIDs,  true); 
	    	modelResults.add(predicitonResult); 
	    }
	    
	    
	    
	    return modelResults;
	}

	
	/**
	 * Create a JSON compatible matrix from a string. 
	 * @param predictions - the spectrum. 
	 * @return string representation for the predictions in JSON format
	 */
	private String prediction2String(ArrayList<PredictionResult> arrayList) {

		if (arrayList == null) {
			return "null";
		}

		JSONObject jsonObject = new JSONObject();

		JSONArray matrixArray = new JSONArray();

		for (PredictionResult result : arrayList) {
			JSONArray rowArray = new JSONArray();
			for (float element : result.getPrediction()) {
				rowArray.put(element);
			}
			matrixArray.put(rowArray);
		}
		
		
		//we assume the class IDs are the same for each chunk of predicitons so don't
		//to make this a matrix
		JSONArray classIDArray = new JSONArray();
		short[] classID  = arrayList.get(0).getClassNames();
		for (short aClassID : classID) {
			classIDArray.put(aClassID); 
		}

		jsonObject.put(PREDICTIONS_JSON_KEY, matrixArray);
		jsonObject.put(CLASSIDS_JSON_KEY, classIDArray);

		//	
		//		System.out.println("SpectrumString"); 
		//		System.out.println(spectrumString); 
		return jsonObject.toString();
	}

}
