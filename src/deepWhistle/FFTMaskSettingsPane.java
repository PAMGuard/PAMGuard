package deepWhistle;

import fftManager.FFTDataBlock;
import javafx.scene.layout.Pane;

/**
 * Base class for the settings pane of a single {@link PamFFTMask}. Each mask
 * type provides its own implementation which holds the mask specific controls.
 * The pane is shown within the main {@link MaskedFFTSettingsPane} whenever the
 * corresponding mask is selected by the user.
 *
 * @author Jamie Macaulay
 */
public abstract class FFTMaskSettingsPane {

	/**
	 * Get the JavaFX node holding the mask specific controls.
	 *
	 * @return the content node for the pane.
	 */
	public abstract Pane getContentNode();

	/**
	 * Set the mask specific controls from the parameters.
	 *
	 * @param params - the current parameters.
	 */
	public abstract void setParams(MaskedFFTParamters params);

	/**
	 * Write the mask specific control values into the parameters.
	 *
	 * @param params - the parameters to update.
	 */
	public abstract void getParams(MaskedFFTParamters params);

	/**
	 * Notify the pane of the currently selected FFT data source. Masks can use
	 * this to check that the source is configured correctly for the model (e.g.
	 * the correct FFT length and hop) and warn / offer to fix it. The default
	 * implementation does nothing.
	 *
	 * @param fftSource - the selected FFT data source, or null if none.
	 */
	public void setFFTSource(FFTDataBlock fftSource) {
		// default: no-op
	}

}
