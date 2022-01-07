package detectionPlotFX.plots;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class SpectrumPlotParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	static public final transient int CHANNELS_SINGLE = 0;
	static public final transient int CHANNELS_MEANS = 1;

	public boolean logScale = false;

	public double logRange = 30;

	public int plotSmoothing = 5;

	public boolean smoothPlot = false;

	public int channelChoice = CHANNELS_SINGLE;

	public boolean showEventInfo=true;

	public boolean plotCepstrum = false;

	@Override
	protected SpectrumPlotParams clone()  {
		try {
			return (SpectrumPlotParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}

