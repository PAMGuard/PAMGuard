package dataPlotsFX.rawClipDataPlot;

import java.io.Serializable;


public class RawClipParams extends FFTPlotSettings implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	/**
	 * True to show a spectrogram clip - otherwise frequency box. 
	 */
	public boolean showSpectrogram = false;

	/**
	 * The window type for the spectrogram
	 */
	public int windowType = 1; 

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected RawClipParams clone() {
		try {
			return (RawClipParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
