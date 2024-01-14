package likelihoodDetectionModule;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Represents the parameters that make up a signal band, used as
 * input to the Likelihood detection algorithm. This class is stored
 * in the TargetConfiguration object.
 */

public class SignalBand implements Serializable, ManagedParameters {
	
	/** The constant serialVersionUID required by Serializable. */
	static final long serialVersionUID = 560;
	
	/** The signal band identifier. The configuration dialog will ensure
	 * that this is unique for each TargetDetection object. 
	 */
	public String identifier = "Default Signal Band";
  
	/** The in-band threshold, in db. */
	public double inBandThresholdDb = 7.0;
  	
	/** The guard band threshold, in db. */
	public double guardBandThresholdDb = 12.0;
	
	/** The start frequency hz. */
	public double startFrequencyHz = 100.0;
	
	/** The end frequency hz. */
	public double endFrequencyHz = 1000.0; 
	
	/** The background seconds. */
	public double backgroundSeconds = 10.0;
	
	/** The signal seconds. */
	public double signalSeconds = 1.0;
	
	/** The in-band threshold's limits. */
	public double[] inBandThresholdLimits = new double[2];
	
	/** The guard band threshold's limits. */
	public double[] guardBandThresholdLimits = new double[2];
	
	/** The frequency's limits. */
	public double[] frequencyLimits = new double[2];
	
	/** The seconds between detection's limits. */
	public double[] secondsLimits = new double[2];
	
	/**
	 * Instantiates a new signal band.
	 * 
	 * @param acquisitionSettings the acquisition settings
	 * @param fftParams the fft params
	 */
	public SignalBand( AcquisitionSettings acquisitionSettings, LikelihoodFFTParameters fftParams ) {
		super();
		
		// Calibrate the limits values.
		calibrate( acquisitionSettings, fftParams );
		
		// The in-band threshold's limits are fixed.
		inBandThresholdLimits[0] = 0.0001; // essentially > 0
		inBandThresholdLimits[1] = 30.0;
		
		// The guard band threshold's limits are fixed.
		guardBandThresholdLimits[0] = 0.0001; // essentially > 0
		guardBandThresholdLimits[1] = 40.0;
		
		// The upper-bound on the seconds between detection parameter is
		// fixed.
		secondsLimits[1] = 600.0;
		
		// Pick some sensible defaults for the dynamically limited values.
		if ( this.startFrequencyHz < this.frequencyLimits[0] ) {
			this.startFrequencyHz = this.frequencyLimits[0];	
		}
		
		if ( this.endFrequencyHz > this.frequencyLimits[1] ) {
			this.endFrequencyHz = this.frequencyLimits[1];	
		}
	}
	
	/**
	 * Calibrates the SignalBand according to the supplied audio data source
	 * (acquisition) and FFT parameters.
	 * 
	 * @see TargetConfiguration#calibrate
	 * 
	 * @param acquisitionSettings the acquisition settings
	 * @param fftParams the fft params
	 */
	public void calibrate( AcquisitionSettings acquisitionSettings, LikelihoodFFTParameters fftParams ) {
		
		// For now, the lower bound on frequency limits is 0.001 (essentially >= 0).
		this.frequencyLimits[0] = 0.0; 
		
		// The upper bound on the frequencies is the half of the sampling rate.
		this.frequencyLimits[1] = acquisitionSettings.samplingRateHz / 2;
		
		// The lower bound on the seconds parameters is the actual time resolution from
		// the FFT parameters.
		this.secondsLimits[0] = fftParams.getActualTimeResolution();	
	}
	
	/**
	 * The user defines InBandThreshold in dB, however the underlying modules require a linear ratio.
	 * This method converts to a ratio, using the formula 10^(x/10)
	 * 
	 * @return The ratio
	 */
	 
	public double InBandAsRatio() {
		return java.lang.Math.pow( 10., this.inBandThresholdDb / 10. );
	}
	  
	/**
	 * The user defines GuardBandThreshold in dB, however the underlying modules require a linear ratio.
	 * This method converts to a ratio, using the formula 10^(x/10)
	 * 
	 * @return The ratio
	 */
	
	public double GuardBandAsRatio() {
		return java.lang.Math.pow( 10., this.guardBandThresholdDb / 10. );
	} 

	/**
	 * Checks if is error.
	 * 
	 * @return true, if is error
	 */
	public boolean isError() {
		if ( this.inBandThresholdDb < this.inBandThresholdLimits[0] ) return true;
		else if ( this.inBandThresholdDb > this.inBandThresholdLimits[1] ) return true;
		else if ( this.guardBandThresholdDb < this.guardBandThresholdLimits[0] ) return true;
		else if ( this.guardBandThresholdDb > this.guardBandThresholdLimits[1] ) return true;
		else if ( this.startFrequencyHz < this.frequencyLimits[0] ) return true;
		else if ( this.startFrequencyHz > this.frequencyLimits[1] ) return true;
		else if ( this.endFrequencyHz < this.frequencyLimits[0] ) return true;
		else if ( this.endFrequencyHz > this.frequencyLimits[1] ) return true;
		
		else return false;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
}
