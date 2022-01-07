package likelihoodDetectionModule;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.JMenuItem;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import likelihoodDetectionModule.linearAverageSpectra.LinearAverageSpectraProcess;
import likelihoodDetectionModule.normalizer.NormalizerProcess;
import likelihoodDetectionModule.spectralEti.SpectralEtiProcess;
import likelihoodDetectionModule.thresholdDetector.ThresholdDetectorProcess;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamDetection.RawDataUnit;
import java.util.ArrayList;
import java.util.HashMap;
import PamController.PamControllerInterface;

/**
 * The class LikelihoodDetectionUnit implements that standard PamControlledUnit functionality
 * required by PamGuard for a plug-in module.
 */
public class LikelihoodDetectionUnit extends PamControlledUnit implements PamSettings {

	/** The likelihood detection parameters for the module. */
	private LikelihoodDetectionParameters likelihoodDetectionParameters;
	
	/** The fft controller for the module. */
	static private LikelihoodFFTController fftController;
	
	/** A flag indicating whether the controller is in the middle of configuring itself. */
	private boolean isConfiguring = false;
	
	/** A flag indicating whether the controller has been initialized. */
	private boolean isInitialized = false;
	
	/** A flag indicating whether the controller has received its first notification
	 * from the PamGuard system. */
	private boolean firstNotification = true;
	
	/** A flag indicating whether the controller has received its first notification
	 * from itself via the PamGuard system. */
	private boolean selfNotification = false;

	/** The name assigned to the controller. */
	private String name;
	
	/**
	 * The ProcessGroup objects is used by the controlling unit to keep track of
	 * one or more set of detection processes for a target configuration.
	 */
	public class ProcessGroup {
		
		/** The linear average spectra process. */
		LinearAverageSpectraProcess linearAverageSpectraProcess;
		
		/** The spectral eti process. */
		SpectralEtiProcess spectralEtiProcess;
		
		/** The normalizer process. */
		NormalizerProcess normalizerProcess;
		
		/** The threshold detector process. */
		ThresholdDetectorProcess thresholdDetectorProcess;
	}
	
	/** A map of processes groups, organized by the name of the target configuration
	 * that they are for. */
	private HashMap<String, ProcessGroup> processes = new HashMap<String, ProcessGroup>();
	
	/**
	 * Instantiates a new likelihood detection unit.
	 * 
	 * @param name The assigned name of the detection module.
	 */
	public LikelihoodDetectionUnit( String name ) {
		super( "LikelihoodDetectionUnit", name );
		this.name = name;
		
		// Create a default set of parameters. This needs to be done before the
		// call to registerSettings() below, otherwise any persistent settings
		// that might exist will be clobbered and you'll spend like two hours with
		// the debugger figuring out why your damn settings aren't working correctly.
		likelihoodDetectionParameters = new LikelihoodDetectionParameters();

		// Register this class with the PAMGUARD settings manager. Implementing 
		// the PamSettings interface allows the plug-in to have settings that are
		// stored between runs.
		PamSettingManager.getInstance().registerSettings( this );
	}

	/**
	 * Configure the controlling unit (module). This function goes out into the 
	 * system and gets the information necessary from the acquisition source and
	 * the constructs a processing stream for each of the user-configured target
	 * configurations.
	 */
	private void configure() {

		// If configuration has already started, then return. It's possible that the 
		// controlling unit may recieve multiple notifications from the PamGuard system
		// that trigger a (re)configuration.
		if ( this.isConfiguring ) return;
		else {
			this.isConfiguring = true;	
		}
		
		// Get a list of all of the available raw data sources (acquisition settings).
		ArrayList<PamDataBlock> sourceList 
		= PamController.getInstance().getDataBlocks( RawDataUnit.class, true );

		if ( sourceList == null || sourceList.size() <= 0 ) {
			// This should really throw up some kind of message box or other.
			this.isConfiguring = false;
			return;
		}

		// If the parameters don't already have a pre-configured source of data,
		// just pick the first one from the list and hope for the best.
		//String daqName = this.likelihoodDetectionParameters.getDaqName();
		String sourceName = this.likelihoodDetectionParameters.getSourceName();
		if ( sourceName == null || sourceName.isEmpty() ) {
			sourceName = sourceList.get(0).getDataName();
		}

		// Find the data source and get its parameters.
		int sourceIndex = 0;
		Acquisition.AcquisitionParameters acquisitionParameters = null;

		for ( ; sourceIndex != sourceList.size(); ++sourceIndex ) {
			if( sourceList.get( sourceIndex ).getParentProcess().getClass() == Acquisition.AcquisitionProcess.class ) {
				String name = sourceList.get(sourceIndex).getDataName();
				if ( name.equals( sourceName ) ) {
					// DG - small spelling change to Acq ... 
					acquisitionParameters = ((Acquisition.AcquisitionControl)sourceList.
							get(sourceIndex).getParentProcess().getPamControlledUnit()).getAcquisitionParameters();
					break;	                                                            
				}
			}
		}

		// If there isn't an active audio data source, then we have a big problem.
		if ( acquisitionParameters == null ) {
			this.isConfiguring = false;
			return;
		}

		// Obtain the raw data block from the acquisition source.
		PamRawDataBlock rawDataBlock = PamController.getInstance().getRawDataBlock( sourceIndex );

		// Mark the FFT processes as unused. Any required processes that are still 
		// needed will be flagged below when they are requested again.
		if( fftController != null ) {
			fftController.markFFTProcessesUnused();
		}
		else {
			// Create a likelihood FFT controller.
			fftController = new LikelihoodFFTController( this.getUnitName(), rawDataBlock );
			fftController.setupControlledUnit();			
		}
		
		// Purget the other processes that form the processing streams for the target
		// configurations.
		removeAllProcessingGroups();
		
		// Grab the acquisitions settings from the acquisition module
		AcquisitionSettings acquisitionSettings = new AcquisitionSettings();
		acquisitionSettings.samplingRateHz = acquisitionParameters.getSampleRate();
		acquisitionSettings.numberChannels = acquisitionParameters.getNChannels();
		acquisitionSettings.voltageRange = acquisitionParameters.getVoltsPeak2Peak();
		acquisitionSettings.minBandwidthHz = acquisitionParameters.getPreamplifier().getBandwidth()[0];
		acquisitionSettings.maxBandwidthHz = acquisitionParameters.getPreamplifier().getBandwidth()[1];
		acquisitionSettings.preampGainDb = acquisitionParameters.getPreamplifier().getGain();

		// Sync the acquisition settings with the configuration
		this.likelihoodDetectionParameters.syncWithAquisitionSettings( acquisitionSettings );
		
		if ( this.likelihoodDetectionParameters.areConfigurationsWithErrors() ) {
			ArrayList<String> names = this.likelihoodDetectionParameters.configurationsWithErrors();
			
			if ( names.size() != 0 ) {
				// If there are target configurations with error states, then flag them to the user.
			}
		}

		// For each target configuration, create an entire processing stream.
		for( TargetConfiguration t : this.likelihoodDetectionParameters.targetConfigurations ) {

			if( t.getState() == TargetConfiguration.State.Active ) {
				LikelihoodFFTParameters params = t.getFFTParameters();

				LikelihoodDetectionUnit.fftController.createFFTProcess( params );
				LikelihoodFFTProcess fftProcess = fftController.getProcess( params );

				LinearAverageSpectraProcess linavg = new LinearAverageSpectraProcess( this, fftProcess.getOutputDataBlock( 0 ),
						params.getNumberAverages(), params.getFFTHop() );

				SpectralEtiProcess spectralEti = new SpectralEtiProcess( this, linavg.getOutputDataBlock( 0 ),
						t.getSignalBands(), t.getGuardBands() );

				NormalizerProcess normalizer = new NormalizerProcess( this, spectralEti.getOutputDataBlock( 0 ),
						t.getReferenceGain(),
						t.getAlgorithm(),
						t.getSignalBands(),
						t.getGuardBands(),
						params.getActualTimeResolution() );

				ThresholdDetectorProcess threshold = new ThresholdDetectorProcess(
						this,
						t.getIdentifier(),
						params.getActualTimeResolution(),
						normalizer.getOutputDataBlock( 0 ),
						t.getSecondsBetweenDetections(),
						t.getSignalBands(),
						t.getGuardBands() );
				
				ProcessGroup group = new ProcessGroup();
				group.linearAverageSpectraProcess = linavg;
				group.normalizerProcess = normalizer;
				group.spectralEtiProcess = spectralEti;
				group.thresholdDetectorProcess = threshold;
				addProcessingGroup( t.getIdentifier(), group );
			}
		}
		
		// Purge any existing FFT processes that are no longer required.
		fftController.removeUnusedProcesses();

		this.isConfiguring = false;
	}

	/**
	 * Returns the configured LikelihoodDetectionParameters for the module.
	 * 
	 * @see LikelihoodDetectionParameters
	 * 
	 * @return the parameters
	 */
	public LikelihoodDetectionParameters getParameters() {
		return likelihoodDetectionParameters;
	}

	// Implementation of the PamSettings.getSettingsReference()
	// interface. This serialise-able object will be stored by the 
	// PAMGUARD settings manager.
	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsReference()
	 */
	public Serializable getSettingsReference() {
		return likelihoodDetectionParameters;
	}

	// This returns an integer version number for the settings.
	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsVersion()
	 */
	public long getSettingsVersion() {
		return LikelihoodDetectionParameters.serialVersionUID;
	}

	// Called by the PAMGUARD settings manager to restore any saved settings
	// for this plug-in.
	/* (non-Javadoc)
	 * @see PamController.PamSettings#restoreSettings(PamController.PamControlledUnitSettings)
	 */
	public boolean restoreSettings( PamControlledUnitSettings settings ) {
		// Point to the settings object stored in the controlled unit, rather
		// than the locally-created one.
		this.likelihoodDetectionParameters = ((LikelihoodDetectionParameters)settings.getSettings()).clone();
		return true;
	}

	// Over-ride the PamControlledUnit.createDetectionMenu to
	// create a JMenu object that contains menu items associated with the PamProcess.
	// Note that if multiple views are to use the same menu, then they should each
	// create a new menu (by setting Create to true) the first time they call this
	// method.

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override 
	public JMenuItem createDetectionMenu( Frame parentFrame ) {
		JMenuItem menuItem;
		menuItem = new JMenuItem( name + " ..." );
		menuItem.addActionListener( new SettingsAction( parentFrame, this ) );
		return menuItem;
	}

	/**
	 * The SettingsAction class is the action listener that is attached to the
	 * module's menu entry, and is invoked by PamGuard to allow the user to
	 * configure the module's parameters.
	 */
	class SettingsAction implements ActionListener {
		
		/** The parent frame. */
		Frame parentFrame;
		
		/** The controlling unit. */
		LikelihoodDetectionUnit unit;
		
		/**
		 * Instantiates a new settings action.
		 * 
		 * @param parentFrame the parent frame
		 * @param unit the module's controlling unit
		 */
		public SettingsAction( Frame parentFrame, LikelihoodDetectionUnit unit ) {
			this.parentFrame = parentFrame;
			this.unit = unit;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {

			LikelihoodDetectionParameters p
			= LikelihoodDetectionParametersDialog.showDialog(parentFrame, unit.likelihoodDetectionParameters );

			if ( p != null ) {
				unit.likelihoodDetectionParameters = p.deepCopy();
				// Flag the likelihood detection controller that the next changed process settings
				// message it will (likely) receive is from itself. This has to happen before
				// the notification call because the call is synchronous.
				unit.selfNotification = true;
				PamController.getInstance().notifyModelChanged( PamControllerInterface.CHANGED_PROCESS_SETTINGS );
				
				/*
				 * Now also do a check to make sure we're not multi threading 
				 * since the ML detector will crash in MT mode. 				 * 
				 */
//				if (PamModel.getPamModel().isMultiThread()) {
//					String warnText = "The Likelihood detector will not work with PAMGUARD running in multithread mode. \n\n Change to single thread operation";
//					int ans = JOptionPane.showConfirmDialog(null, warnText, "Warning", JOptionPane.YES_NO_OPTION);
//					if (ans == JOptionPane.YES_OPTION) {
//						PamModel.getPamModel().setMultithreading(false);
//					}
//					else {
//						JOptionPane.showMessageDialog(null, "PAMGUARD will probably crash if you persist in using the Likelihood detector in multithread mode.");
//					}
//				}
				
			}
		}
	} 

	// Handle PAMGUARD data model (PamDataBlock?) change notifications.
	// Tell the controller that the model may have changed (i.e. a process
	// connection changed, or a process added, etc.) This will be 
	// passes on to the view and used by the controller as necessary.
	// changeType can be either CHANGED_PROCESS_SETTINGS (1), ADD_PROCESS (2),
	// REMOVE_PROCESS(3), ADD_DATABLOCK(4), REMOVE_DATABLOCK(5), ADD_CONTROLLEDUNIT(6)
	// or REMOVE_CONTROLLEDUNIT(7)

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged( int changeType ) {
		
		// Let the base class handle its thing.
		super.notifyModelChanged( changeType );

		// If this is the very first notification received, then configure. It is
		// helpful to configure once before initialisation is complete because the
		// notification is probably from this module being added.
		if ( this.firstNotification == true ) {
			configure();
			this.firstNotification = false;
		}
		else if ( this.selfNotification == true ) {
			// If this module is added before any others that are not dependencies,
			// then the notification from the first/subsequent configurations needs to
			// be run in order for those modules to find our output blocks. Sometimes
			// this happens before the initialise complete message, so it is checked
			// independently.
			configure();
			this.selfNotification = false;
		}
		
		// For complicated scenarios, it is very helpful to ignore all of the traffic coming
		// from other modules until the initialisation complete notification is received.
		if ( this.isInitialized == false && changeType == PamControllerInterface.INITIALIZATION_COMPLETE ) {	
			this.isInitialized = true;
			configure();
		}
		
		// After the initialisation complete notification has been seen, any other changes to processing
		// settings or new modules should trigger us to reconfigure.
		if ( isInitialized == true && ( changeType == PamControllerInterface.CHANGED_PROCESS_SETTINGS || changeType == PamControllerInterface.ADD_CONTROLLEDUNIT || changeType == PamControllerInterface.ADD_PROCESS ) ) {
			configure();
		}
	}
	
	/**
	 * Adds a processing group to the list of active processing streams.
	 * 
	 * @param configName the config name
	 * @param processes the processes
	 */
	private void addProcessingGroup( String configName, ProcessGroup processes ) {
		
		// Add the process group to the list of groups that we manage.
		this.processes.put( configName, processes );
		
		// Call addPamProcess() for each of the new processes so that PamGuard
		// knows they exist.
		addPamProcess( processes.linearAverageSpectraProcess );
		addPamProcess( processes.normalizerProcess );
		addPamProcess( processes.spectralEtiProcess );
		addPamProcess( processes.thresholdDetectorProcess );
	}
	
	/**
	 * Removes the all processing groups.
	 */
	private void removeAllProcessingGroups() {
		
		for ( String name : this.processes.keySet() ) {
			ProcessGroup g = this.processes.get( name );
			g.linearAverageSpectraProcess.destroyProcess();
			removePamProcess( g.linearAverageSpectraProcess );
			g.normalizerProcess.destroyProcess();
			removePamProcess( g.normalizerProcess );
			g.spectralEtiProcess.destroyProcess();
			removePamProcess( g.spectralEtiProcess );
			g.thresholdDetectorProcess.destroyProcess();
			removePamProcess( g.thresholdDetectorProcess );
		}
		
		this.processes.clear();
	}
}

