package detectionPlotFX.plots;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import depthReadout.DepthParameters;
import fftFilter.FFTFilterParams;

public class WaveformPlotParams implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * View a filtered waveform. 
	 */
	public boolean showFilteredWaveform = false; 
	
	/**
	 * Parameters for waveform filter. 
	 */
	public FFTFilterParams waveformFilterParams = new FFTFilterParams();
	
	/**
	 * Seperate waveform plots
	 */
	public boolean showSperateWaveform=true;

	/**
	 * Show the waveform plot
	 */
	public boolean waveShowEnvelope=false;

	/**
	 * Show a fixed x scale. 
	 */
	public boolean waveFixedXScale=false;

	/**
	 * The length of the x scale. 
	 */
	public double maxLength=2048;

	/**
	 * Invert the waveform
	 */
	public boolean invert=false;
		
	
	@Override
	public WaveformPlotParams clone() {
		try {
			return (WaveformPlotParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
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
