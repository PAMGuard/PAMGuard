package rawDeepLearningClassifier.tethys;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.Detection.Parameters;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class DLTethysDataProvider extends AutoTethysProvider {

	public DLTethysDataProvider(TethysControl tethysControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
		// TODO Auto-generated constructor stub
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
}
