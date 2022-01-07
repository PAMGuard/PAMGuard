package beamformer.localiser.plot;

import java.util.List;
import java.util.Random;

import PamguardMVC.DataBlock2D;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.BeamLocaliserData;
import fftManager.FFTDataUnit;

public class MiniSpectrogram extends BeamDataDisplayF {

	public MiniSpectrogram(BeamFormLocaliserControl bfLocaliserControl, int channelMap) {
		super(bfLocaliserControl, channelMap, "Spectrogram", "Time");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void updateF(BeamLocaliserData beamLocData) {

		List<FFTDataUnit> specData = beamLocData.getCollatedFFTData();
		if (specData == null) {
			return;
		}
		DataBlock2D fftBlock = beamLocData.getFFTDataBlock();
		int nT = specData.size();
		int nF = fftBlock.getDataWidth(0);
		double[][] data = new double[nT][];
		int iT = 0;
		for (FFTDataUnit fftUnit:specData) {
			data[iT++] = fftUnit.getMagnitudeData();
		}
		double[] tRange = {0., nT*fftBlock.getHopSamples()/fftBlock.getSampleRate()};
		double[] fRange = {0., fftBlock.getSampleRate()/2.};
		double timeScale = tRange[1] < 1.1 ? 1000. : 1;
		getSimplePlot().setBottomAxisRange(0, tRange[1], timeScale, getXAxisLabel(timeScale));
		getSimplePlot().setData(data, tRange, fRange);
	}
	
	public String getXAxisLabel(double axisLabelScale) {
		if (axisLabelScale == 1000.) {
			return "Time [ms]";
		}
		return "Time [s]";
	}

}
