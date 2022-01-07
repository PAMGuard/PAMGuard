package beamformer.localiser.plot;

import java.util.List;

import PamUtils.PamUtils;
import beamformer.BeamAlgorithmParams;
import beamformer.continuous.BeamOGramDataUnit;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.BeamLocaliserData;
import javafx.application.Platform;

public class MiniBeamOGram extends BeamDataDisplayF {

	public MiniBeamOGram(BeamFormLocaliserControl bfLocaliserControl, int channelMap) {
		super(bfLocaliserControl, channelMap, "BeamOGram", "Head Angle");
		setPaintPeakLine(true);
		getSimplePlot().getPlotPane().getxAxisBottom().setLabel("Head Angle");
	}

	@Override
	public void updateF(BeamLocaliserData beamLocData)
	{
		List<BeamOGramDataUnit> beamOData = beamLocData.getCollatedBeamOGram();
		double[][] aveData = BeamOGramDataUnit.averageFrequencyAngle1Data(beamOData);
		if (aveData == null) {
			getSimplePlot().setData(null, null, null);
			return;
		}
		double[][] scaledData = beamOData.get(0).dataToDB(aveData);
		
		double[] dataFreqRange = {0, beamLocData.getFFTDataBlock().getSampleRate()/2};
		// peak line plot (do first so it updates on setData)
		double[] lineData = make1DAverageData(aveData);
		if (lineData != null) {
			double[] scaledLine = new double[lineData.length];
			double min = 10.*Math.log10(lineData[0]);
			double max = min;
			for (int i = 0; i < lineData.length; i++) {
				scaledLine[i] = 10.*Math.log10(lineData[i]);
				min= Math.min(min, scaledLine[i]);
				max= Math.max(max, scaledLine[i]);
			}
			for (int i = 0; i < scaledLine.length; i++) {
				scaledLine[i] -= max;
			}
			min = PamUtils.roundNumber(min-max-2., 10.);
			min = Math.min(-5, min);
			max = 0;
			setLinePlotData(scaledLine, min, 0);
		}

		BeamAlgorithmParams groupParams = findAlgorithmParams(beamLocData.getChannelBitmap());
		int[] angles = groupParams.getBeamOGramAngles();
		double[] xRange = {angles[0], angles[1]};
		getSimplePlot().setData(scaledData, xRange, dataFreqRange);
	}

	private double[] make1DAverageData(double[][] data) {
		if (data == null || data.length == 0 || data[0].length == 0) {
			return null;
		}
		int ni = data.length;
		double[] data1D = new double[ni];
		for (int i = 0; i < ni; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data1D[i] += data[i][j];
			}
			data1D[i] /= data[i].length;
		}
		return data1D;
	}


}
