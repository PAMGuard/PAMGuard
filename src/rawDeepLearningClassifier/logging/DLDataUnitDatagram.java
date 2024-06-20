package rawDeepLearningClassifier.logging;

import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDataUnit;

/**
 * Datagram showing the raw model outputs form the classifier. 
 * @author Jamie Macaulay 
 *
 */
public class DLDataUnitDatagram implements DatagramProvider {

	private DLControl dlControl;

	private DatagramScaleInformation scaleInfo;

	public DLDataUnitDatagram(DLControl dlContorl) {
		this.dlControl=dlContorl; 
		scaleInfo = new DatagramScaleInformation(Double.NaN, Double.NaN, "Probability", false, DatagramScaleInformation.PLOT_3D);
	}

	@Override
	public int getNumDataGramPoints() {
		return dlControl.getDLClassifyProcess().getNumClasses(); 
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		DLDataUnit dlDataUnit = (DLDataUnit) dataUnit;
		
		if (dataGramLine==null || dataGramLine.length==0) return 0;
		
		if (dlDataUnit.getPredicitionResult().getPrediction()!=null) {
			for (int i=0; i<dlDataUnit.getPredicitionResult().getPrediction().length; i++) {
				dataGramLine[i] += (float) dlDataUnit.getPredicitionResult().getPrediction()[i]; 
			}
		}

		return 1;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		return scaleInfo;
	}

}
