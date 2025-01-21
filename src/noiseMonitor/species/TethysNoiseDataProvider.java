package noiseMonitor.species;

import java.util.List;

import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.Detection.Parameters;
import nilus.DetectionEffortKind;
import nilus.GranularityEnumType;
import nilus.Helper;
import noiseMonitor.NoiseDataBlock;
import noiseMonitor.NoiseDataUnit;
import tethys.TethysControl;
import tethys.niluswraps.PDeployment;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;
import tethys.swing.export.ExportWizardCard;
import tethys.swing.export.GranularityCard;

public class TethysNoiseDataProvider extends AutoTethysProvider {

	private NoiseDataBlock noiseDataBlock;

	public TethysNoiseDataProvider(TethysControl tethysControl, NoiseDataBlock noiseDataBlock) {
		super(tethysControl, noiseDataBlock);
		this.noiseDataBlock = noiseDataBlock;
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
		NoiseDataUnit noiseDataUnit = (NoiseDataUnit) dataUnit;
		/*
		 * Now all the noise measurements, noting there may be several types. 
		 */
		int statTypes = noiseDataBlock.getStatisticTypes();
		int nTypes = PamUtils.getNumChannels(statTypes);
		Parameters params = detection.getParameters();
		List<Double> measurements = params.getFrequencyMeasurementsDB();
		double[][] noiseData = noiseDataUnit.getNoiseBandData();
		int meanIndex = -1;
		for (int i = 0; i < nTypes; i++) {
			int type = PamUtils.getNthChannel(i, statTypes);
			String name = noiseDataBlock.getMeasureName(type);
			if (1<<type == NoiseDataBlock.NOISE_MEAN) {
				meanIndex = i;
			}
		}
		if (meanIndex < 0) {
			meanIndex = 0;
		}
		int nMeasures = noiseData.length;
//		int meanIndex = PamUtils.getChannelPos(NoiseDataBlock.NOISE_MEAN, statTypes);
//		double[] meanData = noiseData[meanIndex];
		for (int i = 0; i < nMeasures; i++) {
			measurements.add(roundDecimalPlaces(noiseData[i][meanIndex],1));
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			kind.setParameters(params);
		}
		List<Double> fMeasures = params.getFrequencyMeasurementsHz();
		double[] loEdges = noiseDataBlock.getBandLoEdges();
		double[] hiEdges = noiseDataBlock.getBandHiEdges();
		// put lot mean into the array
		for (int i = 0; i < loEdges.length; i++) {
			fMeasures.add(roundSignificantFigures(Math.sqrt(loEdges[i]*hiEdges[i]), 4));
		}
	}

	@Override
	public boolean wantExportDialogCard(ExportWizardCard wizPanel) {
		if (wizPanel.getClass() == GranularityCard.class) {
			/*
			 *  but do also need to be certain to set the granularity to call
			 *  and the output type to detection ! 
			 */
			StreamExportParams params = getTethysControl().getTethysExportParams().getStreamParams(getTethysControl(), noiseDataBlock);
			params.exportDetections = true;
			params.exportLocalisations = false;
			return false;
		}
		return super.wantExportDialogCard(wizPanel);
	}

}
