package beamformer.localiser.plot;

import java.util.List;

import beamformer.BeamAlgorithmParams;
import beamformer.continuous.BeamOGramDataUnit;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.BeamLocaliserData;

public class MiniBeamOGram2D extends BeamDataDisplay {

	public MiniBeamOGram2D(BeamFormLocaliserControl bfLocControl, int channelMap) {
		super(bfLocControl, channelMap, "BeamOGram2D", "Head Angle", "Slant angle");
		setPaintPeakLine(false);
	}

	@Override
	public void update(BeamLocaliserData beamLocData) {
		List<BeamOGramDataUnit> beamOData = beamLocData.getCollatedBeamOGram();
		double[][] aveData = BeamOGramDataUnit.averageAngleAngleData(beamOData);
		if (aveData == null) {
			getSimplePlot().setData(null, null, null);
			return;
		}
		double[][] scaledData = beamOData.get(0).dataToDB(aveData);
		int channelMap = beamLocData.getChannelBitmap();
		BeamAlgorithmParams groupParams = findAlgorithmParams(channelMap);
		int[] angles = groupParams.getBeamOGramAngles();
		int[] slantAngles = groupParams.getBeamOGramSlants();
		if (angles == null || slantAngles == null) return;
		
		
		double[] xRange = {angles[0], angles[1]};
		double[] yRange = {slantAngles[0], slantAngles[1]};
		getSimplePlot().setPaintPeakPos(true);
		getSimplePlot().setData(scaledData, xRange, yRange);
//		getSimplePlot().setXAxisRange(xRange[0], xRange[1], 1, "Head Angle");
//		getSimplePlot().setYAxisRange(yRange[0], yRange[1], 1, "Slant Angle");

	}


}
