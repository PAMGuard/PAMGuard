package cepstrum;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamProcess;
import dataPlotsFX.data.DataTypeInfo;
import fftManager.FFTDataBlock;

public class CepstrumDataBlock extends FFTDataBlock {
	
	private static DataTypeInfo scaleInfo = new DataTypeInfo(ParameterType.TIME, ParameterUnits.SECONDS);

	public CepstrumDataBlock(String dataName, PamProcess parentProcess, int channelMap, int fftHop, int fftLength) {
		super(dataName, parentProcess, channelMap, fftHop, fftLength);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getMaxDataValue() {
		/*
		 * The y-axis units are time (interpreted as ICI for a burst pulse), and should 
		 * range from 0s to FFTLen / (2 * SampleRate), so 0 to .010666s for a 48kHz SR with a 1024 FFT length.
		 */
		return getFftLength() / (2*getSampleRate());
	}

	@Override
	public DataTypeInfo getScaleInfo() {
		return scaleInfo;
	}

	/**
	 * Override the normal FFTDataBlock getDataGain method here and just return 1.  If we use the FFTDataBlock
	 * version without running the normal FFTDataBlock initialization routines, we will always get 0 gain.
	 */
	@Override
	public double getDataGain(int iChan) {
		return 1;
	}

}
