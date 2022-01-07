package beamformer.localiser.plot;

import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.BeamLocaliserData;

public abstract class BeamDataDisplayF extends BeamDataDisplay {

	public BeamDataDisplayF(BeamFormLocaliserControl bfLocControl, int channelMap, String plotName, String xAxisLabel) {
		super(bfLocControl, channelMap, plotName, xAxisLabel, "Frequency");
		// TODO Auto-generated constructor stub
	}

	@Override
	public final void update(BeamLocaliserData beamLocData) {
		setFrequencyScale(beamLocData.getFrequency());
		updateF(beamLocData);
	}
	
	/**
	 * Does what the update function did, but after the axis have been set. 
	 * @param beamLocData
	 */
	public abstract void updateF(BeamLocaliserData beamLocData);

	public void setFrequencyScale(double[] freqRange) {
		double f2 = PamUtils.roundNumberUpP(freqRange[1], 2);
		FrequencyFormat freqFormat = FrequencyFormat.getFrequencyFormat(freqRange[1]);
		double fScale = 1./freqFormat.getScale();
		String label = String.format("Frequency [%s]", freqFormat.getUnitText());
		getSimplePlot().setLeftAxisRange(freqRange[0], freqRange[1], fScale, label);
	}


}
