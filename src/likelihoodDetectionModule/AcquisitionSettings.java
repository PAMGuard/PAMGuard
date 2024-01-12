package likelihoodDetectionModule;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * The AcquisitionSettings class provides a module-local storage object for holding
 * on to the properties of the audio data source (acquisition module) upon which the
 * module is depending.
 */
public class AcquisitionSettings implements Serializable, ManagedParameters {
	
	/** The constant serialVersionUID that is required by Serializable. */
	static final long serialVersionUID = 34462;
	
	/** The minimum bandwidth of the data source. */
	public double minBandwidthHz;
	
	/** The maximum bandwidth of the data source. */
	public double maxBandwidthHz;
	
	/** The sampling rate of the audio data. */
	public float samplingRateHz;
	
	/** The number of channels in the audio data. */
	public int numberChannels;
	
	/** The peak-to-peak voltage range of the audio data. */
	public double voltageRange;
	
	/** The preamplifier gain of the audio data. */
	public double preampGainDb;
	
	/** The name of the acquisition source module. */
	public String sourceName = new String();
	
	/** The number assigned to the source module by the pamguard controller. */
	public int sourceNumber;
	
	/**
	 * Instantiates a new acquisition settings.
	 */
	public AcquisitionSettings() {
		super();	
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}