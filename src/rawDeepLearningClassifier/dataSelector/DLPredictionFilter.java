package rawDeepLearningClassifier.dataSelector;


import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.logging.DLAnnotation;

/**
 * A data filter which filters data by the maximum prediction value
 * for different classes. 
 * 
 * @author Jamie Macaulay
 */
public class DLPredictionFilter implements DLDataFilter {

	/**
	 * Reference to the DLControl
	 */
	private DLControl dlcontrol;
	
	/**
	 * The filter parameters
	 */
	private DLPredictionFilterParams filterParams = new DLPredictionFilterParams();

	private DLPredictonPane dlPredictonPane;

	private DLPredictionPanel dlPredictonPanel; 

	public DLPredictionFilter(DLControl dlcontrol) {
		this.dlcontrol = dlcontrol; 
		checkParamsClass() ;
	}
	
	
	@Override
	public int scoreDLData(PamDataUnit dataUnit) {

		
		DLAnnotation annotation = (DLAnnotation) dataUnit. findDataAnnotation(DLAnnotation.class) ;

		if (annotation==null) return -1;
		
		//iterate through all results and check that at least one class passes data selection. 
		float[] results;
		int maxClassIndex = -1; 
		int maxPred = -1; 

		//get the maximum prediction index which passes the minimum threshold
		for (PredictionResult modelResult: annotation.getModelResults()) {
			results = modelResult.getPrediction();
			for (int j=0; j<results.length; j++) {
				if (filterParams.classSelect[j] && results[j]>filterParams.minClassPredicton[j]) {
					if (results[j]> maxPred) maxClassIndex = j;
				}
			}
		}
		
		return maxClassIndex;
	}

	@Override
	public DLPredictionFilterParams getParams() {
		 checkParamsClass() ;
		return filterParams;
	}

	@Override
	public void setParams(DataSelectParams params) {
		this.filterParams = ((DLPredictionFilterParams) params).clone();
		 checkParamsClass();
		
	}
	
	private void checkParamsClass() {
		if (filterParams.classSelect==null ||  dlcontrol.getNumClasses()!=filterParams.classSelect.length) {
			filterParams.classSelect = new boolean[dlcontrol.getNumClasses()];
			filterParams.minClassPredicton = new double[dlcontrol.getNumClasses()];
			
			//set default so data selector does not always prevent all classes from showing. 
			for (int i=0; i<filterParams.classSelect.length; i++) {
				filterParams.classSelect[i]=true;
				filterParams.minClassPredicton[i]=0.4;
			}

		}
	}


	@Override
	public DynamicSettingsPane getSettingsPane() {
		if (dlPredictonPane ==null) {
			dlPredictonPane = new DLPredictonPane(this);
		}
		return dlPredictonPane;
	}
	
	@Override
	public PamDialogPanel getSettingsPanel() {
		if (dlPredictonPanel ==null) {
			dlPredictonPanel = new DLPredictionPanel(this);
		}
		return dlPredictonPanel;
	}


	public DLControl getDLControl() {
		return this.dlcontrol;
	}


//	/**
//	 * Get the index of the highest prediciton value a list of results. 
//	 * @param predictions - index of the highest prediction within a matrix of predicitons. 
//	 * @return an index of the hghest predictions. 
//	 */
//	public static int[] getBestClass(List<PredictionResult> predictions) {
//				
//		float[][] results = new float[predictions.size()][]; 
//		
//		//A detection might have multiple prediction results, i.e. predictions are a matrix. Need 
//		//to iterate through all the predictions and then work out whihc is the maximum. That index is then then]
//		//class colour. 
//		int i=0;
//		for (PredictionResult modelResult: predictions) {
//			results[i] = modelResult.getPrediction();
//			i++;
//		}
//		
//		int[] indexBest = PamArrayUtils.maxPos(results); 
//		
//		return indexBest;
//	}

}
