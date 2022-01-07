package Localiser.controls;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import Spectrogram.WindowFunction;

/**
 * Parameters to use when a data source can be either raw or fft data. 
 * <br for use with RawOrFFTPane dialog component. Should be embedded in 
 * more complex dialog components.  
 * @author dg50
 *
 */
public class RawOrFFTParams implements Serializable, Cloneable, RawOrFFTParamsInterface, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private String sourceName;
	
	private int fftLength = 512;
	
	private int fftHop = 256;
	
	private int windowFunction = WindowFunction.HANNING;

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#getSourceName()
	 */
	@Override
	public String getSourceName() {
		return sourceName;
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#setSourceName(java.lang.String)
	 */
	@Override
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#getFftLength()
	 */
	@Override
	public int getFftLength() {
		return fftLength;
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#setFftLength(int)
	 */
	@Override
	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#getFftHop()
	 */
	@Override
	public int getFftHop() {
		return fftHop;
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#setFftHop(int)
	 */
	@Override
	public void setFftHop(int fftHop) {
		this.fftHop = fftHop;
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#getWindowFunction()
	 */
	@Override
	public int getWindowFunction() {
		return windowFunction;
	}

	/* (non-Javadoc)
	 * @see bearinglocaliser.dialog.RawOrFFTParamsInterface#setWindowFunction(int)
	 */
	@Override
	public void setWindowFunction(int windowFunction) {
		this.windowFunction = windowFunction;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected RawOrFFTParamsInterface clone() {
		try {
			return (RawOrFFTParamsInterface) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
