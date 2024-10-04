package whistlesAndMoans;

import PamDetection.PamDetection;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import whistleClassifier.WhistleContour;;

public abstract class AbstractWhistleDataUnit extends PamDataUnit<PamDataUnit, PamDataUnit> 
	implements WhistleContour, PamDetection {

	public AbstractWhistleDataUnit(long timeMilliseconds, int channelBitmap,
			long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

	public AbstractWhistleDataUnit(DataUnitBaseData basicData) {
		super(basicData);
	}

	/**
	 * Get the total number of slices
	 * @return total number of slices
	 */
	abstract public int getSliceCount();
	
	/**
	 * Get an array of the times of each slice in seconds
	 * @return times in seconds
	 */
	@Override
	abstract public double[] getTimesInSeconds();
	
	/**
	 * Get an array of the peak frequencies in Hz. 
	 * @return peak frequencies in Hz.
	 */
	@Override
	abstract public double[] getFreqsHz();
}
