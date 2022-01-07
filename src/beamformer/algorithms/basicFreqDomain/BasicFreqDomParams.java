package beamformer.algorithms.basicFreqDomain;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import beamformer.BeamAlgorithmParams;


public class BasicFreqDomParams extends BeamAlgorithmParams implements ManagedParameters {
	/**
	 * version number
	 */
	public static final long serialVersionUID = 1L;
	
	/**
	 * 2D array containing weights.  The second array is of length numElements, and holds weight values for each of the elements
	 * in the channelMap.  The first array will have one index for each of the beams to generate.
	 */
	private double[][] weights;
	
	/**
	 * 2D array containing weights.  The array is of length numElements, and holds weight values for each of the elements
	 * in the channelMap.
	 */
	private double[] beamogramWeights;
	
	/**
	 * Array containing the type of window used for each set of beam weights.  Numbers match the WindowFunction list, with an
	 * extra index value added at the end to indicate a custom list.  Length of array is numBeams.
	 * NOTE: EXTRA VALUE NOT IMPLEMENTED YET.  Will probably have to subclass the WindowFunction class to add an extra 'Custom' name
	 * at the end, and then figure out how to put it into the GUI.  Not important enough right now, will leave this for v2
	 */
	private int[] windowTypes;
	
	/**
	 * Main constructor
	 * 
	 * @param algorithmName
	 * @param groupNumber
	 */
	public BasicFreqDomParams(String name, int groupNumber, int channelMap) {
		super(name, groupNumber, channelMap);
	}

	/**
	 * @return the weights
	 */
	public double[][] getWeights() {
		return weights;
	}

	/**
	 * @param weights the weights to set
	 */
	public void setWeights(double[][] weights) {
		this.weights = weights;
	}

	/**
	 * @return the windowTypes
	 */
	public int[] getWindowTypes() {
		return windowTypes;
	}

	/**
	 * @param windowTypes the windowTypes to set
	 */
	public void setWindowTypes(int[] windowTypes) {
		this.windowTypes = windowTypes;
	}

	/* (non-Javadoc)
	 * @see beamformer.BeamAlgorithmParams#clone()
	 */
	@Override
	public BasicFreqDomParams clone() {
		
		// create new object
		BasicFreqDomParams newOne = new BasicFreqDomParams(this.getAlgorithmName(), this.getGroupNumber(), this.getChannelMap());
		
		// clone the fields in the abstract class
		newOne.beamOGramAngles = this.beamOGramAngles.clone();
		newOne.beamOGramSlants = this.beamOGramSlants.clone();
		newOne.freqRange = this.getFreqRange().clone();
		newOne.setBeamOGramFreqRange(this.getBeamOGramFreqRange().clone());
		newOne.headings = this.getHeadings().clone();
		newOne.slants = this.slants.clone();
		newOne.setNumBeamogram(this.getNumBeamogram());
		newOne.numBeams = this.getNumBeams();
		newOne.canBeam = this.isCanBeam();
		newOne.canBeamogram = this.isCanBeamogram();
		
		// clone the fields in this class
		newOne.weights = this.getWeights().clone();
		newOne.beamogramWeights = this.getBeamogramWeights().clone();
		newOne.windowTypes = this.getWindowTypes().clone();
		
		// return the new object
		return newOne;
	}

	/**
	 * @return the beamogramWeights
	 */
	public double[] getBeamogramWeights() {
		return beamogramWeights;
	}

	/**
	 * @param beamogramWeights the beamogramWeights to set
	 */
	public void setBeamogramWeights(double[] beamogramWeights) {
		this.beamogramWeights = beamogramWeights;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
