package likelihoodDetectionModule;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Represents the parameters that make up a guard band, used as
 * input to the Likelihood detection algorithm. These are stored in
 * a TargetConfiguration object.
 */

public class GuardBand implements Serializable, ManagedParameters {
	
	/** The constant serialVersionUID required by Serializable */
	static final long serialVersionUID = 561;

  	/** The identifier of the signal band that this guard band should be associated with. */
	public String associatedSignalBandIdentifier = null;

  	/** The identifier of this guard band. The configuration dialog should ensure
  	 * that this value is unique for a TargetConfiguration
  	 */
  	public String identifier = "Default Signal Band";
  
  	/** The start frequency hz. */
  	public double startFrequencyHz = 100.0;
  
  	/** The end frequency hz. */
  	public double endFrequencyHz = 1000.0; 
  
  	/** The background seconds. */
  	public double backgroundSeconds = 10.0;
  
  	/** The signal seconds. */
  	public double signalSeconds = 1.0;
  
  	/** The frequency's limits. */
  	public double[] frequencyLimits = new double[2];
  
  	/** The seconds' limits. */
  	public double[] secondsLimits = new double [2];
  
	/**
	 * Instantiates a new guard band.
	 * 
	 * @param acquisitionSettings the acquisition settings
	 * @param fftParams the fft params
	 */
	public GuardBand( AcquisitionSettings acquisitionSettings, LikelihoodFFTParameters fftParams ) {
		super();
		
		// Calibrate the limits values.
		calibrate( acquisitionSettings, fftParams );
		
		// The following limits are not dynamic.
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
	 * Calibrate the guard band with the audio source parameters (acquisition) and
	 * the FFT parameters.
	 * 
	 * @see TargetConfiguration#calibrate
	 * 
	 * @param acquisitionSettings the acquisition settings
	 * @param fftParams the fft params
	 */
	public void calibrate( AcquisitionSettings acquisitionSettings, LikelihoodFFTParameters fftParams ) {
		// For now, the lower bound on frequency limits is 0.001 (essentially >= 0)
		this.frequencyLimits[0] = 0.0;
		
		// The upper bound on the frequencies is the half of the sampling rate.
		this.frequencyLimits[1] = acquisitionSettings.samplingRateHz / 2;
		
		// The lower bound on the seconds parameters is the actual time resolution from
		// the FFT parameters.
		this.secondsLimits[0] = fftParams.getActualTimeResolution();	
	}
	
  /**
   * Checks if is error.
   * 
   * @return true, if is error
   */
  public boolean isError() {
	if ( this.startFrequencyHz < this.frequencyLimits[0] ) return true;
	else if ( this.startFrequencyHz > this.frequencyLimits[1] ) return true;
	else if ( this.endFrequencyHz < this.frequencyLimits[0] ) return true;
	//else if ( this.backgroundSeconds < this.backgroundSecondsLimits[0] ) return true;
	//else if ( this.backgroundSeconds > this.backgroundSecondsLimits[1] ) return true;
	//else if ( this.signalSeconds < this.signalSecondsLimits[0] ) return true;
	//else if ( this.signalSeconds > this.signalSecondsLimits[1] ) return true;
	else return false;
  }
  
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
