package fftManager;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;

/**
 * Sam T: Added to support a plugin that is using FFT Process without the FFT Control (June 2026).
 */
public abstract class PamNotFFTControl extends PamControlledUnit {
	
	public PamNotFFTControl(String unitType, String unitName) {
		super(unitType, unitName);
	}

	public PamNotFFTControl(PamConfiguration pamConfiguration, String string, String unitName) {
		super(pamConfiguration, string, unitName);
	}

	public abstract FFTParameters getFFTParameters();
}
