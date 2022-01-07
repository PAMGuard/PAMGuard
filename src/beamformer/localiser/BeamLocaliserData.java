package beamformer.localiser;

import java.util.List;

import PamguardMVC.PamDataUnit;
import beamformer.continuous.BeamOGramDataBlock;
import beamformer.continuous.BeamOGramDataUnit;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * Temporary data used during beam forming, e.g. for updating displays. 
 * @author Doug Gillespie
 *
 */
public class BeamLocaliserData extends PamDataUnit<PamDataUnit, PamDataUnit>{

	private List<FFTDataUnit> collatedFFTData;
	private List<BeamOGramDataUnit> collatedBeamOGram;
	private FFTDataBlock fftDataBlock;
	private BeamOGramDataBlock beamOGramDataBlock;
	private double[] angle1Data;
	private double[] beamAngles;

	/**
	 * 
	 * @param timeMilliseconds
	 */
	public BeamLocaliserData(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	/**
	 * 
	 * @param timeMilliseconds
	 * @param collatedFFTData
	 * @param collatedBeamOGram
	 * @param angle1Data 
	 */
	public BeamLocaliserData(long timeMilliseconds, FFTDataBlock fftDataBlock, BeamOGramDataBlock beamOGramDataBlock,
			List<FFTDataUnit> collatedFFTData, List<BeamOGramDataUnit> collatedBeamOGram, double[] frequencyRange, double[] beamAngles) {
		super(timeMilliseconds);
		this.collatedFFTData = collatedFFTData;
		this.collatedBeamOGram = collatedBeamOGram;
		this.fftDataBlock = fftDataBlock;
		this.beamOGramDataBlock = beamOGramDataBlock;
		this.setFrequency(frequencyRange);
		this.beamAngles = beamAngles;
	}
	
	/**
	 * @return the dataBlock2D
	 */
	public FFTDataBlock getFFTDataBlock() {
		return fftDataBlock;
	}

	/**
	 * @return the collatedFFTData
	 */
	public List<FFTDataUnit> getCollatedFFTData() {
		return collatedFFTData;
	}

	/**
	 * @param collatedFFTData the collatedFFTData to set
	 */
	public void setCollatedFFTData(List<FFTDataUnit> collatedFFTData) {
		this.collatedFFTData = collatedFFTData;
	}

	/**
	 * @return the collatedBeamOGram
	 */
	public List<BeamOGramDataUnit> getCollatedBeamOGram() {
		return collatedBeamOGram;
	}

	/**
	 * @param collatedBeamOGram the collatedBeamOGram to set
	 */
	public void setCollatedBeamOGram(List<BeamOGramDataUnit> collatedBeamOGram) {
		this.collatedBeamOGram = collatedBeamOGram;
	}

	/**
	 * @return the beamOGramDataBlock
	 */
	public BeamOGramDataBlock getBeamOGramDataBlock() {
		return beamOGramDataBlock;
	}

	/**
	 * @return the beamAngles
	 */
	public double[] getBeamAngles() {
		return beamAngles;
	}

//	/**
//	 * @return the angle1Data
//	 */
//	public double[] getAngle1Data() {
//		return angle1Data;
//	}




}
