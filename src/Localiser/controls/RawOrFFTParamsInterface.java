package Localiser.controls;

import java.io.Serializable;

public interface RawOrFFTParamsInterface extends Serializable {

	/**
	 * @return the sourceName
	 */
	String getSourceName();

	/**
	 * @param sourceName the sourceName to set
	 */
	void setSourceName(String sourceName);

	/**
	 * @return the fftLength
	 */
	int getFftLength();

	/**
	 * @param fftLength the fftLength to set
	 */
	void setFftLength(int fftLength);

	/**
	 * @return the fftHop
	 */
	int getFftHop();

	/**
	 * @param fftHop the fftHop to set
	 */
	void setFftHop(int fftHop);

	/**
	 * @return the windowFunction
	 */
	int getWindowFunction();

	/**
	 * @param windowFunction the windowFunction to set
	 */
	void setWindowFunction(int windowFunction);

}