package clickDetector;

import PamUtils.complex.ComplexArray;
import clipgenerator.ClipSpectrogram;

/**
 * Creates and stores a spectrogram image for a wave file snippet. Usually used with small
 * snippets. 
 *
 * @author Jamie Macaulay
 *
 */
public class ClickSpectrogram extends ClipSpectrogram {


	/**
	 * Reference to the click detection. 
	 */
	private ClickDetection clickDetection; 


	/**
	 * A spectrogram for a click detection. 
	 * @param clickDetection
	 */
	ClickSpectrogram(ClickDetection clickDetection) {
		super(clickDetection); 
		this.fastFFT = null; //don't ever need to use this. 
		this.clickDetection=clickDetection; 
	}

	@Override
	public ComplexArray specTransform(double[] waveChunk) {
		//dunno why I do this. Probably because it's how it's done in the rest of the click detector. 
		return clickDetection.getClickDetector().fastFFT.rfft(waveChunk, getFFTSize());
	}


}
