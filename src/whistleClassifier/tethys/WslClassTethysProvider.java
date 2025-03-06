package whistleClassifier.tethys;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.GranularityEnumType;
import nilus.Helper;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;
import whistleClassifier.WhistleClassificationDataBlock;
import whistleClassifier.WhistleClassificationDataUnit;
import whistleClassifier.WhistleClassifierControl;

public class WslClassTethysProvider extends AutoTethysProvider {

	private WhistleClassifierControl wslClassifiercontrol;
	
	private Helper helper;

	public WslClassTethysProvider(TethysControl tethysControl, WhistleClassifierControl wslClassifiercontrol, WhistleClassificationDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
		this.wslClassifiercontrol = wslClassifiercontrol;
		try {
			helper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		setAddFrequencyInfo(true);
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection det = super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		WhistleClassificationDataUnit wcDataUnit = (WhistleClassificationDataUnit) dataUnit;
		/**
		 * Add best score and fragment count
		 */
		double[] probs = wcDataUnit.getSpeciesProbabilities();
		double maxProb = 0;
		for (int i = 0; i < probs.length; i++) {
			maxProb = Math.max(probs[i], maxProb);
		}
		maxProb = roundSignificantFigures(maxProb, 2);
		det.getParameters().setScore(maxProb);
		String frags = String.format("%d", wcDataUnit.getNFragments());
		try {
			helper.AddAnyElement(det.getParameters().getUserDefined().getAny(), "NumFragments", frags);
		} catch (JAXBException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return det;
	}

	@Override
	public GranularityEnumType[] getAllowedGranularities() {
		GranularityEnumType[] allowed = {GranularityEnumType.GROUPED};
		return allowed;
	}

	@Override
	public boolean canExportLocalisations(GranularityEnumType granularityType) {
		return false;
	}

}
