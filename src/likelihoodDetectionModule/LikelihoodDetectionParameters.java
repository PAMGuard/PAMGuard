package likelihoodDetectionModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Iterator;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * The Class LikelihoodDetectionParameters provides the standard PamGuard
 * abstraction of system-save-able settings for a module. It is used by the
 * LikelihoodDetectionUnit (is-a PamControlledUnit) and returned by the
 * system's call to getParameters() when PamGuard wants to store its
 * persistent settings.
 * 
 * The LikelihoodDetectionParameters class is a farily deep and complicated
 * entity, so to make life within Java easier, it uses a deep copy technique
 * to make copies of itself via the deepCopy() method.
 */
public class LikelihoodDetectionParameters implements Serializable, Cloneable, ManagedParameters {

	/** The constant serialVersionUID required by Serializable. */
	static final long serialVersionUID = 555;

	/** The target configurations that have been configured for the module. This is made public just to make life a little easier. It isn't expected that encapsulation of this member is necessary. */
	public ArrayList<TargetConfiguration> targetConfigurations;

	/** The selected channels from the audio data source. */
	private int selectedChannels = 0xFFFF;
	
	/** The acquisition settings (the audio data source). */
	private AcquisitionSettings acquisitionSettings;
	
	/** The configuration dialog settings. These are non-processing related values that are used in managing the display and layout of the module's configuration dialog - and it's nice to have them persistent between program executions. */
	private ConfigurationDialogSettings configurationDialogSettings;

	/**
	 * Instantiates a new likelihood detection parameters object.
	 */
	public LikelihoodDetectionParameters() {
		this.targetConfigurations = new ArrayList<TargetConfiguration>();
		this.acquisitionSettings = new AcquisitionSettings();
		this.configurationDialogSettings = new ConfigurationDialogSettings();
	}
	
	/**
	 * Deep copy. The parameters object is sufficiently complex enough
	 * that a deep copy method utilizing a bytestream technique is
	 * helpful.
	 * 
	 * @return A copy of the likelihood detection parameters.
	 */
	public LikelihoodDetectionParameters deepCopy() {
		LikelihoodDetectionParameters params = null;
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream( bos );
			out.writeObject( this );
			out.flush();
			out.close();
			
			ObjectInputStream ins = new ObjectInputStream( new ByteArrayInputStream( bos.toByteArray()));
			params = (LikelihoodDetectionParameters) ins.readObject();
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		catch( ClassNotFoundException e ) {
			e.printStackTrace();
		}
		finally {}
		
		return params;
	}
	
	/**
	 * Sync with aquisition settings. When a likelihood detection module is
	 * added and the persistent settings are read in, it is possible that the
	 * restored settings are not viable for the currently configured audio
	 * data source. This function takes in an likelihood AcquisitionSettings object
	 * and will check the stored parameters and flag any that are no longer
	 * functionable with respect to the settings. After this call is used, the
	 * function configurationsWithErrors() can be called to see if there are
	 * any target configurations that need adjustment or removal.
	 * 
	 * @param acquisitionSettings the acquisition settings
	 */
	public void syncWithAquisitionSettings( AcquisitionSettings acquisitionSettings ) {
		this.acquisitionSettings = acquisitionSettings;
		
		Iterator<TargetConfiguration> i = this.targetConfigurations.iterator();
		while ( i.hasNext() ) {
			(i.next()).calibrate(acquisitionSettings, this.selectedChannels );	
		}
	}
	
	/**
	 * Sync with channel map. Separately, but similar to syncWithAcquisitionSettings(), the
	 * channel map alone can be adjusted between the current configurations and the
	 * current (changed) audio data settings.
	 * 
	 * @param channelMap the channel map
	 */
	public void syncWithChannelMap( int channelMap ) {
		this.selectedChannels = channelMap;	
		Iterator<TargetConfiguration> i = this.targetConfigurations.iterator();
		while ( i.hasNext() ) {
			(i.next()).calibrate(this.acquisitionSettings, this.selectedChannels );	
		}
	}
	
	/**
	 * Sets the acquisition settings were used to create the TargetConfigurations,
	 * but does NOT force a error-checking synchronization the way
	 * syncWithAcquisitionSettings() does.
	 * 
	 * @param settings the new acquisition settings
	 */
	public void setAcquisitionSettings( AcquisitionSettings settings ) {
		this.acquisitionSettings = settings;	
	}
	
	/**
	 * Gets the acquisition settings that were used to configure the
	 * current likelihood detector configurations.
	 * 
	 * @return the acquisition settings
	 */
	public AcquisitionSettings getAcquisitionSettings() {
		return this.acquisitionSettings;	
	}
	
	/**
	 * Gets the channel map that was used to configure the current
	 * likelihood detector settings.
	 * 
	 * @return the channel map
	 */
	public int getChannelMap() {
		return this.selectedChannels;	
	}
	
	/**
	 * Gets the currently selected channels from the parameters.
	 * 
	 * @return the selected channels
	 */
	public int getSelectedChannels() {
		return this.selectedChannels;	
	}
	
	/**
	 * Sets the channels that are to be used from the audio data
	 * source.
	 * 
	 * @param selected the new selected channels
	 */
	public void setSelectedChannels( int selected ) {
		this.selectedChannels = selected;	
	}

	/**
	 * Gets the audio data (acquisition) source name used.
	 * 
	 * @return the source name
	 */
	public String getSourceName() {
		return this.acquisitionSettings.sourceName;
	}

	/**
	 * Sets the source name for the audio data source (acquisition) to
	 * be used.
	 * 
	 * @param sourceName the new source name
	 */
	public void setSourceName( String sourceName ) {
		this.acquisitionSettings.sourceName = sourceName;
	}

	
	/**
	 * Are configurations with errors scans the TargetConfigurations that
	 * are currently configured and returns a boolean value indicating
	 * whether any of them have erroneous values or are inconsistent with
	 * current acquisition (audio data source) settings.
	 * 
	 * @return true, iff all configurations are okay.
	 */
	public boolean areConfigurationsWithErrors() {
		return !(this.configurationsWithErrors().isEmpty());
	}
	
	/**
	 * Configurations with errors returns the names of all of the TargetConfigurations
	 * that are currenly indicating errors.
	 * 
	 * @return the array list< string>
	 */
	public ArrayList<String> configurationsWithErrors() {
		ArrayList<String> names = new ArrayList<String>();
		
		Iterator<TargetConfiguration> i = this.targetConfigurations.iterator();
		while ( i.hasNext() ) {
			TargetConfiguration config = i.next();
			if ( config.getState() == TargetConfiguration.State.Error ) {
				names.add( config.getIdentifier() );
			}
		}
		
		return names;
	}
	
	/**
	 * Returns an ArrayList of LikelihoodFFTParameter objects indicating the
	 * currently active FFT processes that have been launched as a result of
	 * the TargetConfigurations.
	 * 
	 * @return the fFT parameters
	 */
	public ArrayList<LikelihoodFFTParameters> getFFTParameters() {
		ArrayList<LikelihoodFFTParameters> fftParams = new ArrayList<LikelihoodFFTParameters>();
		
		Iterator<TargetConfiguration> i = this.targetConfigurations.iterator();
		while ( i.hasNext() ) {
			TargetConfiguration config = i.next();
			if ( config.getState() == TargetConfiguration.State.Active ) {
				fftParams.add( config.getFFTParameters() );
			}
		}
		
		return fftParams;
	}
	
	/**
	 * Gets the configuration dialog settings that are used to have consistent
	 * user settings for the configuration dialog between program executions.
	 * 
	 * @return the configuration dialog settings
	 */
	public ConfigurationDialogSettings getConfigurationDialogSettings() {
		return this.configurationDialogSettings;	
	}
	
	/**
	 * Sets the configuration dialog settings that are used to have consistent
	 * user settings for the configuration dialog between program executions.
	 * 
	 * @param settings the new configuration dialog settings
	 */
	public void setConfigurationDialogSettings( ConfigurationDialogSettings settings ) {
		this.configurationDialogSettings = settings;	
	}
	
	// --------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected LikelihoodDetectionParameters clone() {
		return this.deepCopy();
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}

