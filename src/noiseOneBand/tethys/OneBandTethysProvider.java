package noiseOneBand.tethys;

import java.util.List;

import Filters.FilterParams;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.DetectionEffortKind;
import nilus.GranularityEnumType;
import nilus.Helper;
import nilus.Detection.Parameters;
import nilus.Detection.Parameters.UserDefined;
import noiseOneBand.OneBandControl;
import noiseOneBand.OneBandDataBlock;
import noiseOneBand.OneBandDataUnit;
import noiseOneBand.OneBandParameters;
import tethys.TethysControl;
import tethys.niluswraps.PDeployment;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;
import tethys.swing.export.ExportWizardCard;
import tethys.swing.export.GranularityCard;

public class OneBandTethysProvider extends AutoTethysProvider {

	private OneBandDataBlock oneBandDataBlock;
	private OneBandControl oneBandControl;

	public OneBandTethysProvider(TethysControl tethysControl, OneBandControl oneBandControl, OneBandDataBlock oneBandDataBlock) {
		super(tethysControl, oneBandDataBlock);
		this.oneBandControl = oneBandControl;
		this.oneBandDataBlock = oneBandDataBlock;
	}
	
	@Override
	public GranularityEnumType[] getAllowedGranularities() {
		GranularityEnumType[] allowed = {GranularityEnumType.CALL};
		return allowed;
	}
	
	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection detection = super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		OneBandDataUnit noiseDataUnit = (OneBandDataUnit) dataUnit;
		
		Parameters params = detection.getParameters();
		List<Double> measurements = params.getFrequencyMeasurementsDB();
		
		measurements.add(roundDecimalPlaces(noiseDataUnit.getRms(), 1));

		/*
		 * Add the band limits as additional parameters
		 */
		OneBandParameters obParams = oneBandControl.getParameters();
		FilterParams fp = obParams.getFilterParams();
		double lo = fp.highPassFreq;
		double hi = fp.lowPassFreq;
		params.setMinFreqHz(lo);
		params.setMaxFreqHz(hi);
		params.setDurationS((double) obParams.measurementInterval);
		/**
		 * Add the p-p, 0-p and SEL as UserDefined's. 
		 */
		addUserDefined(params, "peak-peak", String.format("%3.1f", noiseDataUnit.getPeakPeak()));
		addUserDefined(params, "zero-peak", String.format("%3.1f", noiseDataUnit.getZeroPeak()));
		Double sel = noiseDataUnit.getIntegratedSEL();
		if (sel != null) {
			addUserDefined(params, "SEL", String.format("%3.1f", sel));
			addUserDefined(params, "SEL-time", String.format("%d", noiseDataUnit.getSelIntegationTime()));
		}
		
		return detection;
	}
	
	@Override
	public void getEffortKinds(PDeployment pDeployment, List<DetectionEffortKind> effortKinds,
			StreamExportParams exportParams) {
		super.getEffortKinds(pDeployment, effortKinds, exportParams);
		DetectionEffortKind kind = effortKinds.get(0);
		nilus.DetectionEffortKind.Parameters params = kind.getParameters();
		if (params == null) {
			params = new nilus.DetectionEffortKind.Parameters();
			try {
				Helper.createRequiredElements(params);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			kind.setParameters(params);
		}
		OneBandParameters obParams = oneBandControl.getParameters();
		FilterParams fp = obParams.getFilterParams();
		double lo = fp.highPassFreq;
		double hi = fp.lowPassFreq;
		double mid = Math.sqrt(lo*hi);
		List<Double> fMeasures = params.getFrequencyMeasurementsHz();
		fMeasures.add(roundSignificantFigures(mid, 4));
	}
	@Override
	public boolean wantExportDialogCard(ExportWizardCard wizPanel) {
		if (wizPanel.getClass() == GranularityCard.class) {
			/*
			 *  but do also need to be certain to set the granularity to call
			 *  and the output type to detection ! 
			 */
			StreamExportParams params = getTethysControl().getTethysExportParams().getStreamParams(getTethysControl(), oneBandDataBlock);
			params.exportDetections = true;
			params.exportLocalisations = false;
			return false;
		}
		return super.wantExportDialogCard(wizPanel);
	}

	@Override
	public boolean canExportLocalisations(GranularityEnumType granularityType) {
		/* 
		 * will ensure that this forces it to set export of detections to true
		 * since the granularity panel will not be shown for this data type. 
		 */
		return false;
	}
}
