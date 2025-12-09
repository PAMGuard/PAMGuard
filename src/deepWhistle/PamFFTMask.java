package deepWhistle;

import java.util.List;

import fftManager.FFTDataUnit;

/**
 * Interface for FFT masking algorithms.
 */
public interface PamFFTMask {

	/**
	 * Initialize the mask. Called once before processing starts.
	 * 
	 * @return true if initialization was successful, false otherwise
	 */
	public boolean initMask();

	/**
	 * Apply the mask to a batch of FFTDataUnit objects. The mask performs some operation
	 * on the FFT data within each FFTDataUnit in the batch and returns the modified data units.
	 * 
	 * @param batch List of FFTDataUnit objects to process
	 * @return List of FFTDataUnit objects after applying the mask
	 */
	public List<FFTDataUnit> applyMask(List<FFTDataUnit> batch);
 
}
