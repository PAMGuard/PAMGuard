package beamformer;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import beamformer.algorithms.basicFreqDomain.BasicFreqDomParams;

/*
 * Base class for all beam algorithms. 
 */
public abstract class BeamAlgorithmParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	/**
	 * The name of the algorithm using these parameters.  This should be the name returned by the
	 * getStaticProperties().getName() call to the algorithm provider object, in order to properly
	 * match up provider to parameters in BeamformerProcess.
	 */
	private String algorithmName;
	
	/**
	 * The group number these parameters are linked to
	 */
	protected int groupNumber;

	/**
	 * Channel map describing the channels (hydrophones) used in this group
	 */
	protected int channelMap;

	/**
	 * integer indicating how many individual beams are to be created.  Defaults to 0.
	 */
	protected int numBeams;
	
	/**
	 * an array containing the beam headings, in degrees, where 0 = straight ahead and 180 = behind.  The
	 * length of the array is numBeams.  The term headings (aka azimuth) is used to make the variable easily recognisable for the
	 * most common type of hydrophones array, the linear horizontal array.  A more generic term for this variable would
	 * be main angle, in the direction of the primary array axis
	 */
	protected int[] headings;
	
	/**
	 * an array containing the beam slant angles, in degrees, where 0 is horizontal and -90 = straight down.  The
	 * length of the array is numBeams.  The term slant angle is used to make the variable easily recognisable for the
	 * most common type of hydrophones array, the linear horizontal array.  A more generic term for this variable would
	 * be secondary angle, relative to the perpendicular to the array axis primary array axis
	 */
	protected int[] slants;

	/**
	 * A 2D array containing the frequency range to analyse.  The second array is of length 2 with index 0 = min freq and index 1 = max freq.
	 * The first array will have one index for each of the beams (i.e. [numBeams]).
	 * If this array is null, it will default to the full frequency range available from the fft source.  
	 */
	protected double[][] freqRange;
	
	/**
	 * integer indicating the number of beamograms to create.  Valid values are 0 (for no beamograms)
	 * or 1.  Defaults to 0.
	 */
	private int numBeamogram;
	
	/**
	 * A 3 element vector with the minimum azimuth angle (index 0), maximum azimuth angle (index 1), and step
	 * size (index 2) to use for the BeamOGram sweep.  Initialises to 0deg to 180deg, with a 2 degree step.  Note
	 * that the term azimuth is used for familiarity, and is appropriate for horizontal linear arrays.  A more
	 * generic term would be main angle, in the direction of the primary array axis
	 */
	protected int[] beamOGramAngles = new int[]{0, 180, 2};
	
	/**
	 * A 3 element vector with the minimum slant angle (index 0), maximum slant angle (index 1), and step
	 * size (index 2) to use for the BeamOGram sweep.  For a horizontal linear array, 0 deg is horizontal and -90 deg
	 * is straight down.  Initialises to zero slant (0 deg x 0 deg x 1 deg step - step size cannot be 0 or else it will
	 * cause div-by-0 error later). 
	 * Note that the term slant is used for familiarity, and is appropriate for horizontal linear arrays.  A more
	 * generic term would be secondary angle, relative to the perpendicular to the array axis primary array axis.
	 */
	protected int[] beamOGramSlants = new int[]{0, 0, 1};
	
	/**
	 * A 2D array containing the frequency range to analyse.  The array is of length 2 with index 0 = min freq and index 1 = max freq.
	 * If this array is null, it will default to the full frequency range available from the fft source.  
	 */
	private double[] beamOGramFreqRange;
	
	/**
	 * whether or not this instance of the algorithm can/should allow the user to create individual beams.
	 * Defaults to true.
	 */
	protected boolean canBeam = true;
	
	/**
	 * whether or not this instance of the algorithm can/should allow the user to create a beamogram.
	 * Defaults to true.
	 */
	protected boolean canBeamogram = true;
	
	/**
	 * Main constructor
	 * @param algorithmName The name of the algorithm using these parameters.  This should be the name returned by the
	 * getStaticProperties().getName() call to the algorithm provider object, in order to properly
	 * match up provider to parameters in BeamformerProcess.
	 * @param groupNumber the group number
	 * @param channelMap the channel map for the channels in this group
	 */
	public BeamAlgorithmParams(String algorithmName, int groupNumber, int channelMap) {
		this.algorithmName = algorithmName;
		this.groupNumber = groupNumber;
		this.channelMap = channelMap;
	}

	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @param algorithmName the algorithmName to set
	 */
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	/**
	 * @return the groupNumber
	 */
	public int getGroupNumber() {
		return groupNumber;
	}

	/**
	 * @param groupNumber the groupNumber to set
	 */
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the channelMap
	 */
	public int getChannelMap() {
		return channelMap;
	}

	/**
	 * @param channelMap the channelMap to set
	 */
	public void setChannelMap(int channelMap) {
		this.channelMap = channelMap;
	}
	
	/**
	 * @return the numBeams
	 */
	public int getNumBeams() {
		return numBeams;
	}

	/**
	 * @param numBeams the numBeams to set
	 */
	public void setNumBeams(int numBeams) {
		this.numBeams = numBeams;
	}

	/**
	 * @return the headings
	 */
	public int[] getHeadings() {
		return headings;
	}

	/**
	 * @param headings the headings to set
	 */
	public void setHeadings(int[] headings) {
		this.headings = headings;
	}

	/**
	 * @return the numBeamogram
	 */
	public int getNumBeamogram() {
		return numBeamogram;
	}

	/**
	 * @param numBeamogram the numBeamogram to set
	 */
	public void setNumBeamogram(int numBeamogram) {
		this.numBeamogram = numBeamogram;
	}

	/**
	 * @return the beamOGramAngles
	 */
	public int[] getBeamOGramAngles() {
		return beamOGramAngles;
	}

	/**
	 * @param beamOGramAngles the beamOGramAngles to set
	 */
	public void setBeamOGramAngles(int[] beamOGramAngles) {
		this.beamOGramAngles = beamOGramAngles;
	}

	/**
	 * @return the freqRange
	 */
	public double[][] getFreqRange() {
		return freqRange;
	}

	/**
	 * @param freqRange the freqRange to set
	 */
	public void setFreqRange(double[][] freqRange) {
		this.freqRange = freqRange;
	}

	/**
	 * Return the slant angles for the individual beams.  In some circumstances, such as for linear arrays,
	 * the slant angles will not have been defined.  If this is the case, create a new array of size numBeams
	 * containing 0's, and pass that back to the calling method.
	 * @return
	 */
	public int[] getSlants() {
		if (slants == null) {
			slants = new int[numBeams];
		}
		return slants;
	}

	/**
	 * Set the slant angles
	 * @param slants
	 */
	public void setSlants(int[] slants) {
		this.slants = slants;
	}

	/**
	 * 
	 * @return
	 */
	public int[] getBeamOGramSlants() {
		return beamOGramSlants;
	}

	/**
	 * 
	 * @param beamOGramSlants
	 */
	public void setBeamOGramSlants(int[] beamOGramSlants) {
		this.beamOGramSlants = beamOGramSlants;
	}

	/**
	 * Clone the fields in this abstract class, as well as any fields specific to the extended class.
	 * Extended classes must override this method to ensure the params get saved properly.  See
	 * the {@link BasicFreqDomParams#clone BasicFreqDomParams.clone} method for an example;
	 */
	public abstract BeamAlgorithmParams clone();

	/**
	 * @return the beamOGramFreqRange
	 */
	public double[] getBeamOGramFreqRange() {
		if (beamOGramFreqRange == null) {
			beamOGramFreqRange = new double[2];
		}
		return beamOGramFreqRange;
	}

	/**
	 * @param beamOGramFreqRange the beamOGramFreqRange to set
	 */
	public void setBeamOGramFreqRange(double[] beamOGramFreqRange) {
		this.beamOGramFreqRange = beamOGramFreqRange;
	}

	/**
	 * Whether or not this instance of the algorithm can/should allow the user to create individual beams.
	 * True = ok to show beams.
	 * 
	 * @return True = ok to show beams.
	 */
	public boolean isCanBeam() {
		return canBeam;
	}

	/**
	 * Whether or not this instance of the algorithm can/should allow the user to create individual beams.
	 * True = ok to show beams.
	 * 
	 * @param canBeam True = ok to show beams.
	 */
	public void setCanBeam(boolean canBeam) {
		this.canBeam = canBeam;
	}

	/**
	 * Whether or not this instance of the algorithm can/should allow the user to create a beamogram.
	 * True = ok to show beamogram
	 * 
	 * @return True = ok to show beamogram
	 */
	public boolean isCanBeamogram() {
		return canBeamogram;
	}

	/**
	 * Whether or not this instance of the algorithm can/should allow the user to create a beamogram.
	 * True = ok to show beamogram
	 * 
	 * @param canBeamogram True = ok to show beamogram
	 */
	public void setCanBeamogram(boolean canBeamogram) {
		this.canBeamogram = canBeamogram;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}


}
