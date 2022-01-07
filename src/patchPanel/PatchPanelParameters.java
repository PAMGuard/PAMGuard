package patchPanel;

import java.io.Serializable;

import PamguardMVC.PamConstants;

public class PatchPanelParameters implements Cloneable, Serializable {

	static public final long serialVersionUID = 0;
	
	/**
	 * matric of data in relating to data out
	 * First index is input channel,
	 * second index is output channel
	 * Ultimately, these will be gains, initially just using 0 and 1 though. 
	 */
	double[][] patches;
	
	int dataSource = 0;
	
	boolean immediate = false;
	
	public PatchPanelParameters() {
		patches = new double[PamConstants.MAX_CHANNELS][PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			patches[i][i] = 1;
		}
	}

	@Override
	public PatchPanelParameters clone() {

		try {
			return (PatchPanelParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private int outputChannels, inputChannels;
	private int[] firstChannels;
	
	public void configureSummary(int inputChannels) {
		this.inputChannels = inputChannels;
		for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
			if ((inputChannels & (1<<in)) == 0) continue;
			for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
				if (patches[in][out] > 0) {
					outputChannels |= (1 << out);
				}
			}
		}
		
		// first channels is the first input channel for each output channel. 
		firstChannels = new int[PamConstants.MAX_CHANNELS];
		for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
			firstChannels[out] = -1;
			for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
				if (patches[in][out] > 0) {
					firstChannels[out] = in;
					break;
				}
			}
		}
	}

	/**
	 * @return the outputChannels
	 */
	public int getOutputChannels() {
		return outputChannels;
	}

	/**
	 * @return the inputChannels
	 */
	public int getInputChannels() {
		return inputChannels;
	}

	/**
	 * @return the firstChannels
	 */
	public int[] getFirstChannels() {
		return firstChannels;
	}
}
