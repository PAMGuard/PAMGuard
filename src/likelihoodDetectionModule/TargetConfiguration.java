package likelihoodDetectionModule;

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import likelihoodDetectionModule.normalizer.NormalizerProcess.NormalizerAlgorithm;

/**
 * The Class TargetConfiguration represents a single configuration
 * for the likelihood detector, which can run multiple configurations
 * simultaneously. A configuration is represented by a number of 
 * parameters that control the scope and accuracy of the area and
 * the characteristics to be detected. Also there are one or more
 * SignalBand definitions and zero or more GuardBand definitions.
 * 
 */
public class TargetConfiguration implements Serializable, ManagedParameters {

	/** The Constant serialVersionUIDi required by Serializable. */
	static final long serialVersionUID = 562;

	/**
	 * The State enumeration represents the current activity state of the
	 * TargetConfiguration, that is, whether is is currently requested to
	 * be active by the user (or, alternately inactive), or whether there
	 * is an error in one of the parameters of the configuration.
	 */
	public enum State {
		
		/** The configuration is Active. */
		Active,
		
		/** The configuration is Inactive. */
		Inactive,
		
		/** The configuration is in Error. */
		Error
	};

	/** The state. */
	private State state = State.Active;

	/** The reference gain. */
	private double referenceGain = 1.5;
	
	/** The time resolution. */
	private double timeResolution = 0.1;
	
	/** The frequency resolution. */
	private double frequencyResolution = 5.0;
	
	/** The seconds between detections. */
	private double secondsBetweenDetections = 1.0;
	
	/** The algorithm being used. */
	private NormalizerAlgorithm algorithm = NormalizerAlgorithm.SplitWindow;
	
	/** The channel map in use. */
	private int channelMap;
	
	/** The time resolution limits. */
	private double[] timeResolutionLimits = new double[2];
	
	/** The frequency resolution limits. */
	private double[] frequencyResolutionLimits = new double[2];
	
	/** The seconds between detections limits. */
	private double[] secondsBetweenDetectionsLimits = new double[2];

	/** The signal bands. */
	private ArrayList<SignalBand> signalBands;
	
	/** The guard bands. */
	private ArrayList<GuardBand> guardBands;

	/** The identifier for the configuration. The configuration dialog
	 * will ensure that this string is unique for the configuration
	 */
	private String identifier;
	
	/** The acquisition settings used to computer various parameter limits
	 * and other settings.
	 */
	private AcquisitionSettings acquisitionSettings = new AcquisitionSettings();
	
	/** The calculated fft parameters that will be required to run this
	 * configuration.
	 */
	private LikelihoodFFTParameters fftParameters;

	/**
	 * Instantiates a new target configuration. The audio source settings and the
	 * channel map are required in order to automatically configure and limit
	 * various parameters for the detector.
	 * 
	 * @param acquisitionSettings the acquisition settings
	 * @param channelMap the channel map
	 */
	public TargetConfiguration( AcquisitionSettings acquisitionSettings, int channelMap ) {
		// Target configurations are active by default.
		this.state = TargetConfiguration.State.Active;
		
		// This class does not define any signal bands by default, but it is
		// possible that other parts of the module will ensure that a default
		// is always present for the user, since it doesn't make sense for a
		// configuration to be valid without at least one.
		this.signalBands = new ArrayList<SignalBand>();
		this.guardBands = new ArrayList<GuardBand>();
		
		this.identifier = new String("Unnamed");
		
		this.channelMap = channelMap;
		
		this.acquisitionSettings = acquisitionSettings;
		
		// The limits on the time resolution parameter are fixed.
		this.timeResolutionLimits[0] = 0.0001;
		this.timeResolutionLimits[1] = 10.0;
		
		// The limits on the frequency resolution parameter are fixed.
		this.frequencyResolutionLimits[0] = 0.1;
		this.frequencyResolutionLimits[1] = 50000.0;
		
		// The limits on the seconds between detection parameter are fixed.
		this.secondsBetweenDetectionsLimits[0] = 0.0;
		this.secondsBetweenDetectionsLimits[1] = 1000.0;
		
		// The calibrate() function performs the checking of the audio
		// source parameters and adjusts variable parameters and limits
		// based on them.
		calibrate( this.acquisitionSettings, this.channelMap );
	}

	/**
	 * Get the names of the available likelihood normalization algorithms.
	 * 
	 * @return the algorithm names
	 */
	static public String[] getAlgorithmNames() {
		String[] names = { "Decaying Average", "Block Average" };
		return names;
	}

	/**
	 * A convenience function to turn an NormalizerAlgorithm enumeration into a string.
	 * 
	 * @param A NormalizerAlgorithm name.
	 * 
	 * @return the string representation
	 */
	static String algorithmToString( NormalizerAlgorithm a ) {
		if ( a == NormalizerAlgorithm.DecayingAverage ) {
			return "Decaying Average";
		}
		else {
			return "Block Average"; 
		}
	}

	/**
	 * Sets the state of the configuration.
	 * 
	 * @param s the new state
	 */
	public void setState( State s ) {
		this.state = s;	
	}
	
	/**
	 * Given a NormalizationAlgorithm in the form of a string, return the
	 * appropriate enumeration value.
	 * 
	 * @param The string.
	 * 
	 * @return the normalizer algorithm
	 */
	static NormalizerAlgorithm algorithmFromString( String s ) {
		if ( s.equals( "Decaying Average" )) {
			return NormalizerAlgorithm.DecayingAverage;	
		}
		else if ( s.equals( "Block Average" )) {
			return NormalizerAlgorithm.SplitWindow;	
		}
		
		return NormalizerAlgorithm.SplitWindow;
	}
	
	/**
	 * Gets the state of the TargetConfiguration. This will check
	 * all of the parameters and bands for their errors states as
	 * well.
	 * 
	 * @see State
	 * 
	 * @return the state
	 */
	public State getState() {
		// If the user has requested this target configuration to be
		// unused, then there isn't any point in checking further.
		if ( this.state == State.Inactive ) return this.state;

		// There are only two viable states at this point, either
		// active or error.
		this.state = State.Active;
		
		// Ensure that the time resolution is within its limits.
		if ( this.timeResolution < this.timeResolutionLimits[0] ||
				this.timeResolution > this.timeResolutionLimits[1] ) {
			this.state = State.Error;
		}
		
		// Ensure that the frequency resolution is within its limits.
		if ( this.frequencyResolution < this.frequencyResolutionLimits[0] ||
				this.frequencyResolution > this.frequencyResolutionLimits[1] ) {
			this.state = State.Error;
		}
		
		// Ensure that the seconds between detections parameter is within its limits.
		if ( this.secondsBetweenDetections < this.secondsBetweenDetectionsLimits[0] ||
				this.secondsBetweenDetections > this.secondsBetweenDetectionsLimits[1] ) {
			this.state = State.Error;
		}

		// If there are no signal bands, that's an error.
		if ( this.signalBands.size() == 0 ) this.state = State.Error;
		
		// Now check that the bands are within the values allowed.
		for ( SignalBand signal : this.signalBands ) {
			if ( signal.isError() ) this.state = State.Error;
		}

		for ( GuardBand guard : this.guardBands ) {
			if ( guard.isError() ) this.state = State.Error;
		}
		
		return this.state;
	}
	
	/**
	 * Sets the seconds between detections.
	 * 
	 * @param s the new seconds between detections
	 */
	public void setSecondsBetweenDetections( double s ) {
		this.secondsBetweenDetections = s;
	}
	
	/**
	 * Gets the seconds between detections.
	 * 
	 * @return the seconds between detections
	 */
	public double getSecondsBetweenDetections( ) {
		return this.secondsBetweenDetections;
	}
	
	/**
	 * Gets the fFT parameters.
	 * 
	 * @return the fFT parameters
	 */
	public LikelihoodFFTParameters getFFTParameters() {
		return this.fftParameters;	
	}
	
	/**
	 * Gets the time resolution limits.
	 * 
	 * @return the time resolution limits
	 */
	public double[] getTimeResolutionLimits() {
		return this.timeResolutionLimits;	
	}

	/**
	 * Gets the frequency resolution limits.
	 * 
	 * @return the frequency resolution limits
	 */
	public double[] getFrequencyResolutionLimits() {
		return this.frequencyResolutionLimits;	
	}
	
	/**
	 * Gets the seconds between detections limits.
	 * 
	 * @return the seconds between detections limits
	 */
	public double[] getSecondsBetweenDetectionsLimits() {
		return this.secondsBetweenDetectionsLimits;	
	}
	
	/**
	 * Gets the signal bands.
	 * 
	 * @return the signal bands
	 */
	public ArrayList<SignalBand> getSignalBands() {
		return this.signalBands;
	}

	/**
	 * Sets the signal bands.
	 * 
	 * @param signalBands the new signal bands
	 */
	public void setSignalBands( ArrayList<SignalBand> signalBands ) {
		this.signalBands = signalBands;
	}

	/**
	 * Gets the guard bands.
	 * 
	 * @return the guard bands
	 */
	public ArrayList<GuardBand> getGuardBands() {
		return this.guardBands;	
	}

	/**
	 * Sets the guard bands.
	 * 
	 * @param bands the new guard bands
	 */
	public void setGuardBands( ArrayList<GuardBand> bands ) {
		this.guardBands = bands;	
	}
	
	/**
	 * Gets the identifier.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return this.identifier;	
	}

	/**
	 * Sets the identifier.
	 * 
	 * @param identifier the new identifier
	 */
	public void setIdentifier( String identifier ) {
		this.identifier = identifier;
	}

	/**
	 * Gets the time resolution.
	 * 
	 * @return the time resolution
	 */
	public double getTimeResolution() {
		return timeResolution;
	}

	/**
	 * Sets the time resolution.
	 * 
	 * @param timeResolution the new time resolution
	 */
	public void setTimeResolution( double timeResolution ) {
		this.timeResolution = timeResolution;
		calibrate( this.acquisitionSettings, this.channelMap );
	}

	/**
	 * Gets the frequency resolution.
	 * 
	 * @return the frequency resolution
	 */
	public double getFrequencyResolution() {
		return frequencyResolution;
	}

	/**
	 * Gets the reference gain.
	 * 
	 * @return the reference gain
	 */
	public double getReferenceGain() {
		return this.referenceGain;
	}
	
	/**
	 * Sets the frequency resolution.
	 * 
	 * @param frequencyResolution the new frequency resolution
	 */
	public void setFrequencyResolution( double frequencyResolution ) {
		this.frequencyResolution = frequencyResolution;	
		calibrate( this.acquisitionSettings, this.channelMap );
	}

	/**
	 * Gets the algorithm.
	 * 
	 * @return the algorithm
	 */
	public NormalizerAlgorithm getAlgorithm() {
		return algorithm;	
	}

	/**
	 * Sets the algorithm.
	 * 
	 * @param algorithm the new algorithm
	 */
	public void setAlgorithm( NormalizerAlgorithm algorithm ) {
		this.algorithm = algorithm;	
	}
	
	/**
	 * Gets the acquisition settings.
	 * 
	 * @return the acquisition settings
	 */
	public AcquisitionSettings getAcquisitionSettings() {
		return this.acquisitionSettings;
	}
	
	public int getChannelMap() {
		return this.channelMap;	
	}
	
	/**
	 * Calibrate the TargetConfiguration against the supplied audio source
	 * parameters (the AcquisitionSettings) and the channel map. This ensures that
	 * the parameter limits and other internal variables are correct for the
	 * source data, and it also checks the current settings for validity against
	 * the source parameters.
	 * 
	 * @param acquisitionSettings the acquisition settings
	 * @param channelMap the channel map
	 */
	public void calibrate( AcquisitionSettings acquisitionSettings, int channelMap ) {
		this.acquisitionSettings = acquisitionSettings;
		this.channelMap = channelMap;
		
		// Create a new FFT parameters object that is required based on these settings.
		this.fftParameters = new LikelihoodFFTParameters( this.acquisitionSettings, this.channelMap, this.frequencyResolution, this.timeResolution );
		
		// Calibrate the bands.
		for ( SignalBand s : this.signalBands ) {
			s.calibrate( this.acquisitionSettings, this.fftParameters );
		}
		
		for ( GuardBand g : this.guardBands ) {
			g.calibrate( this.acquisitionSettings, this.fftParameters );	
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}