package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.HashMap;

import Localiser.algorithms.Correlations;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;

/**
 * Manages correlation values and ensures  correlation calculation between 
 * two data units is stored and not calculated multiple times. 
 * 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes") 
public class CorrelationManager {
	
	/**
	 * The FFT filter for the correlation. 
	 */
	public FFTFilter fftFilter; 
	
	/**
	 * A list of UID values and associate. This should be 1 to 1 correspondence with the data unit list 
	 * in the MHTKernel. 
	 */
	private HashMap<Long, CorrelationMap> correlationIndexRef = new HashMap<Long, CorrelationMap>();
	
	/**
	 * Performs correlations calculations. 
	 */
	private Correlations correlations;
	
	/**
	 * Normalise the correlation function. 
	 */
	private boolean normalise = true; 
	
	/**
	 * If in a grouped detection then the index to use correlation calculations. 
	 * Note: change this from 0 at your peril. Likely to crash a lot of stuff.  We
	 * are assuming here that the hydrophones are close together and so the waveforms do 
	 * not change a lot. 
	 */
	private int wavindex = 0;
	
	public CorrelationManager() {
		correlations = new Correlations(); 
	}
	
	/**
	 * Called when a new detection is added. 
	 * @param dataUnit - the data unit to add to the correlation index. 
	 */
	public void addDetection(PamDataUnit dataUnit) {
		correlationIndexRef.put(dataUnit.getUID(), new CorrelationMap(dataUnit.getUID()));
	}
	
	/**
	 * Get the correlation between two data units which have already been added to the 
	 * data unit list using addDetection(). The function searches for an already calculated correlation
	 * value and if one does not exist calculates one. 
	 * @param dataUnit1 - the first data unit in time. (It is important data units are in chronological order)
	 * @param dataUnit2 - the second data unit in time
	 * @param filterParams - the filter params to use to filter data,. 
	 * @return the correlation value between the two units. The returned value is between 0 and 1.
	 */
	public CorrelationValue getCorrelationValue(PamDataUnit dataUnit1, PamDataUnit dataUnit2, FFTFilterParams filterParams) {
		//what is faster, an array or a hashmap. Array is faster but will not works unless one to one
		//correspondence with the master unit list. Going for Hashmap for now. 		
		if (fftFilter==null && filterParams!=null) fftFilter= new FFTFilter(filterParams, 1); 
		
		CorrelationMap corrMap = correlationIndexRef.get(dataUnit2.getUID()); 
		
		if (corrMap == null) {
			System.err.println("CorrelationManager: The data unit is not in the correlationIndexRef list. Something has gone wrong");
			return null;
		}
		
		CorrelationValue corrVal = corrMap.get(dataUnit1.getUID()); 
		
		//System.out.println("Corr val for: Found! " + dataUnit1.getUID() + " and " + dataUnit2.getUID() + " val: " + corrVal.correlationValue);

		if (corrVal==null) {
			//now we have to calculate the correlation value between these two units.
			corrVal= calculateCorrelationValue( dataUnit1,  dataUnit2,  filterParams); 
			
			//System.out.println("Corr val for " + dataUnit1.getUID() + " and " + dataUnit2.getUID() + " val: " + corrVal.correlationValue);

			corrMap.put(dataUnit1.getUID(), corrVal); 
			
		}		
		
		return corrVal; 
	}
	
	/**
	 * Calculate the correlation value between two data units. 
	 * @param dataUnit1 - the first data unit in time.
	 * @param dataUnit2 - the second data unit in time
	 * @return the correlation values. 
	 */
	private CorrelationValue calculateCorrelationValue(PamDataUnit dataUnit1, PamDataUnit dataUnit2, FFTFilterParams filterParams) {
		
		//make sure correct parameters are set. 
		if (filterParams!=null) fftFilter.setParams(filterParams, dataUnit1.getParentDataBlock().getSampleRate());
		
		double[] corrFunction;
		double[][] interpolatedMaxima;
		double maxTime;
	
		//should throw an exception if there is no wave data. 
		corrFunction = correlations.getCorrelation(getDataUnitWavData(dataUnit1, filterParams),
				getDataUnitWavData(dataUnit2, filterParams), normalise);
		interpolatedMaxima = correlations.getInterpolatedMaxima(corrFunction);
		double maxValue = 0.;
		maxTime = 0.;
		if (interpolatedMaxima != null && interpolatedMaxima[0].length > 0) {
			int nMaxima = interpolatedMaxima[0].length;
			for (int j = 0; j < nMaxima; j++) {
				if (interpolatedMaxima[1][j] > maxValue) {
					maxValue = interpolatedMaxima[1][j];
					maxTime = interpolatedMaxima[0][j] - corrFunction.length / 2;
				}
			}
		}		
		
		CorrelationValue corrVal = new CorrelationValue(); 
		
		corrVal.correlationValue = maxValue; 
		corrVal.correlationLag = maxTime; 
		
		return corrVal;
	}
	
	
	/**
	 * Get a data unit's wav data.
	 * 
	 * @return an array of wav data. 
	 */
	private double[] getDataUnitWavData(PamDataUnit pamDataUnit, FFTFilterParams filterParams) {
		
		
		if (pamDataUnit instanceof RawDataHolder) {
			double[] wvfrm1 = ((RawDataHolder) pamDataUnit).getWaveData()[wavindex]; ; 
			if (filterParams!=null) {
				double[] wvfrm1out = new double[wvfrm1.length]; 
				fftFilter.runFilter(wvfrm1, wvfrm1out);
			}
			return wvfrm1;
		} 
		else {
			System.err.println("CorrelationManager: There is no raw wave data for this data. Cannot be used with correlation manager");
			return null; //there is no raw data will crash the click train detector
		}
	}

	/**
	 * Clear the correlation manager.
	 */
	public void clear() {
		correlationIndexRef.clear(); 
	}

	/**
	 * Get the correlation between two data units which have already been added to the 
	 * data unit list using addDetection(). The function searches for an already calculated correlation
	 * value and if one does not exist calculates one. 
	 * @param dataUnit1 - the first data unit in time. (It is important data units are in chronological order)
	 * @param dataUnit2 - the second data unit in time
	 * @return the correlation value between the two units. The returned value is between 0 and 1.
	 */
	public CorrelationValue getCorrelationValue(PamDataUnit pamDataUnitPrev, PamDataUnit pamDataUnitNext) {
		//just set filter to null. 
		return getCorrelationValue(pamDataUnitPrev, pamDataUnitNext, null);
	}

}
