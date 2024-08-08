package rawDeepLearningClassifier.tethys;

import java.util.ArrayList;


import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.Detection.Parameters;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.logging.DLAnnotation;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class DLTethysDataProvider extends AutoTethysProvider {

	private DLControl dlControl;

	public DLTethysDataProvider(TethysControl tethysControl, DLControl dlControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
		this.dlControl = dlControl;
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection detection =  super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		if (detection == null) {
			return null;
		}
		DLDetection dlDetection = (DLDetection) dataUnit;
		
		// try to find the score which is burried in the annotation
		DLAnnotation annotation = (DLAnnotation) dlDetection.findDataAnnotation(DLAnnotation.class) ;
		if (annotation != null) {
			double bestScore = 0;
			ArrayList<PredictionResult> results = annotation.getModelResults();
			for (PredictionResult res : results) {
				float[] resres = res.getPrediction();
				if (resres != null)  for (int i = 0; i < resres.length; i++) {
					double aRes = resres[i];
					if (aRes > bestScore) {
						bestScore = aRes;
					}
				}
			}
			bestScore = roundSignificantFigures(bestScore, 4);
			detection.getParameters().setScore(bestScore);
		}
//		ds = getPamDataBlock().
		
	
//		result = 
		String annotSummary = dlDetection.getAnnotationsSummaryString();
		if (annotSummary != null) {
			Parameters parameters = detection.getParameters();
			addUserDefined(parameters, "Annotation", annotSummary);
		}
	
		return detection;
	}

	@Override
	public nilus.AlgorithmType.Parameters getAlgorithmParameters() {
		
		/**
		 * Add the model parameters to the main dlControl parameters so that they get 
		 * correctly serialized to the XML output
//		 */
//		RawDLParams dlParams = dlControl.getDLParams();
//		DLClassiferModel model = dlControl.getDLModel();
//		Serializable modelParams = null;
//		if (model != null) {
//			modelParams = model.getDLModelSettings();
//		}
//		dlParams.setModelParameters(modelParams);
		dlControl.checkModelParams();
		
		nilus.AlgorithmType.Parameters parameters = super.getAlgorithmParameters();
		
		return parameters;
	}
}
