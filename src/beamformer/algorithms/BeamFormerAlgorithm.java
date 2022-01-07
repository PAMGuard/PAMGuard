package beamformer.algorithms;

import fftManager.FFTDataUnit;

public interface BeamFormerAlgorithm {
	
	/**
	 * Process a block of time aligned FFT Data blocks. These
	 * Should match the channel map for the algorithm, but will 
	 * be blocked together even if there are gaps in the algorithms channel map
	 * @param fftDataUnits array of FFT data units. 
	 */
	public void process(FFTDataUnit[] fftDataUnits);
	
	/**
	 * Prepare the algorithm. Gets called just before it starts. It's here
	 * that steering vectors, etc. should get calculated. 
	 */
	public void prepare();
	
	/**
	 * Get the number of output beams
	 * @return number of output beams
	 */
	public int getNumBeams();
	
	/**
	 * Get the number of angles used in the beamogram calculation.  Note that at the moment all
	 * beamograms must use the same angles in order be able to add their data to the common PamDataBlock.
	 * The BasicFreqDomParams object defines the beamogram angles as from 0 deg to 180 deg in 2 degree steps
	 * (91 angles total).  This should be matched in all other algorithms capable of creating beamograms, until
	 * changes can be made to accomodate different PamDataUnit sizes in the same PamDataBlock.
	 * 
	 * @return the number of angles used in the beamogram calculation 
	 */
	public int getNumBeamogramAngles();
	
	/**
	 * Get information about a specific beam. I've currently no idea 
	 * whatsoever what information we're going to include here !
	 * @param iBeam
	 * @return
	 */
	public BeamInformation getBeamInformation(int iBeam);

	/**
	 * Boolean indicating whether a beamOgram should be created (true) or
	 * not (false)
	 * @return
	 */
	boolean thereIsABeamogram();
	
	/**
	 * Tell the algorithm to keep frequency information in 
	 * beamOGram output.
	 * @param keep 
	 */
	public void setKeepFrequencyInformation(boolean keep);
	
	/**
	 * Set a frequency bin range for the BeamOGram analysis. <br>
	 * Beam forming will take place between binFrom to binTo. <br>
	 * (Not inclusive - so binTo can equal fftLength/2 
	 * process loop is for (int i = binFrom; i < bnTo; i++)). 
	 * @param binFrom first bin to process
	 * @param binTo last bin to process 
	 */
	public void setFrequencyBinRange(int binFrom, int binTo);
}
