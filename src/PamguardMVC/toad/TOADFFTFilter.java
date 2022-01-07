package PamguardMVC.toad;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

/**
 * Class to tell a TOAD calculator how to filter data during a cross correlation 
 * measurement. 
 * @author dg50
 *
 */
public abstract class TOADFFTFilter<T extends PamDataUnit<?,?>> {

	abstract public int[] getUsedFFTBins(T dataUnit, int sampleInSound, float sampleRate, int fftLength);
	
}
