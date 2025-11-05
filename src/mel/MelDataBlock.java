package mel;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamProcess;
import dataPlotsFX.data.DataTypeInfo;
import fftManager.FFTDataBlock;

public class MelDataBlock extends FFTDataBlock {

	private MelProcess melProcess;
	private MelControl melControl;
	DataTypeInfo dataTypeInfo;

	public MelDataBlock(MelControl melControl, MelProcess parentProcess, int channelMap, int hop, int length) {
		super("Mel Spectrum", parentProcess, channelMap, hop, length);
		this.melControl = melControl;
		this.melProcess = parentProcess;
		dataTypeInfo = new DataTypeInfo(ParameterType.FREQUENCY, ParameterUnits.HZ);
		setLogScale(true);
	}

	@Override
	public int getHopSamples() {
		FFTDataBlock fftBlock = melProcess.getInputFFTData();
		if (fftBlock == null) {
			return 1;
		}
		return fftBlock.getHopSamples();
	}

	@Override
	public int getDataWidth(int sequenceNumber) {
		return melControl.getMelParameters().nMel;
	}

	@Override
	public double getMinDataValue() {
		return melControl.getMelParameters().minFrequency;
	}

	@Override
	public double getMaxDataValue() {
		return melControl.getMelParameters().maxFrequency;
	}

	@Override
	public DataTypeInfo getScaleInfo() {
		return dataTypeInfo;
	}




}
