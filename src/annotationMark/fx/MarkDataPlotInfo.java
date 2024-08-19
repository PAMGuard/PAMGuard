package annotationMark.fx;

import PamDetection.AbstractLocalisation;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.generic.GenericDataPlotInfo;
import dataPlotsFX.layout.TDGraphFX;

public class MarkDataPlotInfo extends GenericDataPlotInfo {

	public MarkDataPlotInfo(MarkPlotProviderFX tdDataProvider, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		// TODO Auto-generated constructor stub
	}

	public Double getBearingValue(PamDataUnit pamDataUnit) {
		AbstractLocalisation locData = pamDataUnit.getLocalisation();
		if (locData == null) {
			return null;
		}
		double[] angles = locData.getAngles();
		if (angles != null) {
			return Math.toDegrees(angles[0]);
		}
		
		return null;
	}
}

