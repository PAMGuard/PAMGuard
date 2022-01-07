package PamguardMVC;

import java.util.List;

import fftManager.FFTDataUnit;

/**
 * <p>Interface to indicate that a data unit can provide it's own FFT data.
 * There may be one or more blocks per channel, so data are returned in 
 * a List, which is hopefully n times the number of channels in getChannelBitMap.</p>
 * <p><strong>When data units implement this interface, the parent data block should implement
 * the {@link FFTDataHolderBlock} interface.</strong></p> 
 * @author Doug Gillespie
 *
 */
public interface FFTDataHolder {

	/**
	 * Return a list of FFT data units from the data unit. 
	 * @param fftLength Length of FFT to use. If this is null, a default
	 * from the data source will be used, otherwise the source should recalculate if possible. 
	 * If it's not possible to recalculate, then return what's available and the calling 
	 * process must decide whether it can use the data or not.  
	 * @return list of FFT units, interleaved by channel. 
	 */
	public List<FFTDataUnit> getFFTDataUnits(Integer fftLength);
}
