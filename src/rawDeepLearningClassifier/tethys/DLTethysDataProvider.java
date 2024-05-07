package rawDeepLearningClassifier.tethys;

import java.io.Serializable;

import javax.xml.bind.JAXBException;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.Detection.Parameters;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.RawDLParams;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;
import tethys.pamdata.TethysParameterPacker;

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
