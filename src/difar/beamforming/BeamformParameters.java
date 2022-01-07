package difar.beamforming;

import java.io.Serializable;
import Filters.FilterParams;
import Filters.FilterType;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.PamUtils;

public class BeamformParameters implements Serializable, Cloneable, ManagedParameters {
	
	public static final long serialVersionUID = 10L;

	public static float outputSampleRate = 8000;
	
	public int channelMap;
	
	/**
	 * 
	 */
	public BeamformParameters() {

	}
	
	/**
	 * keep rawdata in rawdatablock for this amount of time may make this display dependant
	 */
	public int keepRawDataTime=120;//seconds
	
	/**
	 * name of unit to get raw data from including the frequency bands in which the multiplexed signals are within
	 */
	public String rawDataName;
	
	
	/**
	 * seconds to prepend to each clip to allow for signal locking of the demux algorithm
	 */
	public double secondsToPreceed = 0;
	
	/**
	 * Stores paramaters to correct the frequency response for DIFAR buoys (not yet used)
	 */
	public FilterParams difarFreqResponseFilterParams = getDefaultFreqResponseFilter();
	
	
	//********* Side Panel parameters
	/**
	 * Steering angle of beamformer (theta) 
	 */
	public Double [] theta = new Double[2];
	//********* 
	
	/**
	 * The default Frequency Response Filter used for correction/calibration
	 * of the frequency response of military sonobuoys.
	 * This default frequency response is flat from 5 Hz to 48 kHz, but 
	 * most sonobuoys have a non-flat frequency response, so this filter 
	 * can be adjusted (via the DIFAR parameters Dialog) measurement of 
	 * absolute sound pressure levels is required.
	 * @return
	 */
	public FilterParams getDefaultFreqResponseFilter() {
		difarFreqResponseFilterParams = new FilterParams();
		difarFreqResponseFilterParams.filterType = FilterType.NONE;
		difarFreqResponseFilterParams.filterOrder = 12;
		double[] freq = null;
		double[] gain = null;
		difarFreqResponseFilterParams.setArbFilterShape(freq, gain);
		return difarFreqResponseFilterParams;
	}

	public boolean useGpsNoiseSource = false;
	
	public String noiseGpsSource;

	/**
	 * Use the function below to assign default values to newly added parameters, since they will be 
	 * null when loading older versions of the settings file.
	 */
	@Override
	public BeamformParameters clone() {
		try {
			BeamformParameters ndp = (BeamformParameters) super.clone();
			return ndp;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getNumChannels(){
		return PamUtils.getHighestChannel(channelMap) + 1;
	}

	public Double[] getTheta() {
		if (theta==null || 
				theta.length != PamUtils.getNumChannels(channelMap)) {
			theta = new Double[PamUtils.getNumChannels(channelMap)];
			for (int i = 0; i < theta.length; i++) {
				theta[i] = 0.;
			}
		}
		return this.theta;
	}

	public void setTheta(Double[] theta) {
		this.theta = theta;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
