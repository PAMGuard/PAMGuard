package Acquisition;

import java.awt.Component;

import javax.swing.JComponent;

import Acquisition.layoutFX.AcquisitionPaneFX;
import Acquisition.layoutFX.DAQSettingsPane;
import PamController.SettingsPane;
import PamUtils.PamUtils;
import javafx.scene.Node;
import soundPlayback.FilePlayback;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;

/**
 * 
 * Abstraction to multiple data acquisition devices. This interface performs 
 * the following tasks:
 * <p>
 * 1. Provides information about the device such as min and max sample rates, number of channelse, etc.
 * 2. Provides a dialog panel with specific options. This automatically gets incorporated into a more
 * general data acquisition panel
 * 3. Provides threads and any other functions necessary to create packets of raw audio data in 
 * PamDataUnit's. 
 * 4. Each concrete class should, if needed, save it's own setting using the PamSettings package. 
 * <p>
 * Current examples are in SoundCardSystem and FileInputSystem
 * 
 * @author Doug Gillespie
 *
 */


// is sampleRate ok? set true - return false from getparams...
// is numChannels ok? set true /override these in asio - before returning false 
public abstract class DaqSystem {

	public static final int PARAMETER_UNKNOWN = -1;
	
	public static final int PARAMETER_FIXED = -2;
	
	private int streamStatus = STREAM_CLOSED;
	
	/**
	 * @return The 'type' of data source for display in the Data Source dialog
	 * <p>  Calling functions should be able to handle 'null'
	 */
	abstract public String getSystemType();
	
	/**
	 * 
	 * @return A string describing the data source, e.g. the sound card name, file name, udp port, etc.
	 */
	abstract public String getSystemName();
	
	/**
	 * The AcquisitionDialog has been designed to that each data source type can plug
	 * in it's own panel of device specific controls. These could be things like a file
	 * name, gain settings, channel lists, whatever is available for the specific device
	 * <p>
	 * The AcquisitionDialog will handle placing this component on the dialog when a
	 * particular DaqSystem is selected. The component will be placed between a drop down 
	 * list of available DaqSystem's and a section of the dialog showing the sample rate and
	 * number of channels. 
	 * <p>
	 * @param acquisitionDialog the AcquisitionDialog this component will be added to
	 * @return dialog component specific to this DAQ device
	 */
	abstract public JComponent getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog);
	
	/**
	 * Get a channel list panel (invented for NI cards which have many more options)
	 * @param acquisitionDialog reference to main acquisition dialog
	 * @return ChannelListPanel component. 
	 */
	public ChannelListPanel getDaqSpecificChannelListPanel(AcquisitionDialog acquisitionDialog) {
		return null;
	}
	
	
	/**
	 * Called by AcquisitionDialog.SetParams so that the dialog componenet can update it's
	 * fields
	 *
	 */
	abstract public void dialogSetParams();
	
	/**
	 * Called by AcquisitionDialog.GetParams so that parameters can be extracted from the dialog
	 * component. The DaqSystem should also implement PamSettings and handle storage of parameters
	 * between runs.
	 * @return true if the parameters and selections are OK. If false is returned the Acquisition
	 * dialog will not respond to its Ok button.
	 */
	abstract public boolean dialogGetParams();
	
	/**
	 * Tell a DAQ system it's been selected or deselected. 
	 * @param select
	 */
	public void setSelected(boolean select) {
		
	}

	/**
	 * Get model changed events. 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		
	}
	/**
	 * 
	 * @return the maximum sample rate supporrted by the selected device, or PARAMETER_UNKNOWN
	 */
	abstract public int getMaxSampleRate();

	/**
	 * 
	 * @return the maximum number of channels supporrted by the selected device, or PARAMETER_UNKNOWN
	 */
	abstract public int getMaxChannels();
	
	/**
	 * 
	 * @return peak to peak voltage for the device or PARAMETER_UNKNOWN
	 */
	abstract public double getPeak2PeakVoltage(int swChannel);
	
	/**
	 * Prepare the DaqSystem. 
	 * <p>
	 * Usually this is the time for opening files, preparing data buffers, etc.
	 * @param daqControl AcquisitionControl unit.
	 * @return true if OK, false otherwise.
	 */
	abstract public boolean prepareSystem(AcquisitionControl daqControl);
	
	/**
	 * Start the DaqSystem. This is called immediately after PrepareSystem
	 * <p>
	 * Most things should be ready from the call to PrepareSystem. In StartSystem you
	 * will generally need to start a thread which will read in the data and place PamDataUnits
	 * in a buffer from where they are read by the main thred and passed on to other Pam modules
	 * for processing (see SoundCardSystem for an example)
	 * @param daqControl AcquisitionControl unit.
	 * @return true if OK, false otherwise
	 */
	abstract public boolean startSystem(AcquisitionControl daqControl);
	
	/**
	 * Stop the DaqSystem. 
	 * @param daqControl
	 */
	abstract public void stopSystem(AcquisitionControl daqControl);

	/**
	 * 
	 * @return true for real time systems (e.g. sound cards, NI cards, wav files
	 * which are being played back over speakers. false for reading files which are to be
	 * processed as fast as possible with no playback.
	 */
	abstract public boolean isRealTime();
	
	/**
	 * 
	 * @return true if the system can also play back sound - likely to be true for sound cards 
	 * and ASIO, and some NI cards, false for wav files. Simulator ? 
	 */
	abstract public boolean canPlayBack(float sampleRate);
	
	/**
	 * 
	 * @return the number of samples expected in each data unit. 
	 */
	abstract public int getDataUnitSamples();
	
	/**
	 * Gets a playback system for playing sound back out through headphones / speakers, etc.
	 * <p>
	 * Generally, anything acquiring data in real time should play the sound back through 
	 * itself so that clocks are correctly synchronised. i.e. A sound card input will go to the 
	 * same sound card output, etc. At a later date, we may try to support output through 
	 * different devices at different speeds - but this will be problematic !
	 * <p>
	 * If data are being read from a file, then playback is through a sound card which sits
	 * in a stand alone implementation of PlaybackSystem 
	 * @param playbackControl 
	 * @param daqSystem 
	 * @return null if no playback available or a PlaybackSystem object.
	 * @see PlaybackSystem
	 * @see FilePlayback
	 */
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
		return null;
	}
	/**
	 * Called after DAQ has stopped - for whatever reason. Nost DAQ systems will not
	 * need to do anything here, but they could potentially clean up memory
	 * The motivation for putting this here now is for the folder analysis system
	 * so that it can start analysing the next file.
	 *
	 */
	abstract public void daqHasEnded();
	
	public static final int STREAM_CLOSED = 0;
	public static final int STREAM_OPEN = 1;
	public static final int STREAM_RUNNING = 2;
	public static final int STREAM_PAUSED = 3;
	public static final int STREAM_ENDED = 4;
	/**
	 * Status of the intput stream
	 * @return stream status
	 */
	public int getStreamStatus() {
		return streamStatus;
	}

	public void setStreamStatus(int streamStatus) {
		this.streamStatus = streamStatus;
	}
	
	public boolean areSampleSettingsOk(int numInputsRequested, float sampleRateRequested){
		return true;
	}
	
	public void showSampleSettingsDialog(AcquisitionDialog acquisitionDialog){
		
	}
	
	public Component getStatusBarComponent() {
		return null;
	}
	
	public boolean supportsChannelLists() {
		return false;
	}
	
	/**
	 * 
	 * @return a bitmap of output channels. this should ALWAYS be
	 * a bitmap of channels 0 to n-1 even if different hardware channels
	 * are used since hardware channels are no longer passed through PAMGUARD. 
	 */
	public int getInputChannelMap(AcquisitionParameters acquisitionParameters) {
		return PamUtils.makeChannelMap(acquisitionParameters.nChannels);
	}
	
	/**
	 * Gets a name for the acquisition device (may just be a number
	 * but need to generalise). 
	 * @return
	 */
	public abstract String getDeviceName();

	/**
	 * Included so that can add additional channel specific gain values for SMRU daq card
	 * @param channel channel number
	 * @return channel specific gain
	 */
	public double getChannelGain(int channel) {
		return 0;
	}
	
	/**
	 * Get the number of bits in each sample. 
	 * @return the number of bits in each sample. 
	 */
	public int getSampleBits() {
		return 16;
	}

	/**
	 * 
	 * @return how often its reasonable to check whether or not the system has stalled. 
	 */
	public long getStallCheckSeconds() {
		return 2;
	}


	/**
	 * Get a channel list panel (invented for NI cards which have many more options)
	 * @param acquisitionDialog reference to main acquisition dialog
	 * @return ChannelListPanel component. 
	 */
	public ChannelListPanel getDaqSpecificChannelListNode(AcquisitionPaneFX acquisitionPaneFX) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * The AcquisitionDialog has been designed to that each data source type can plug
	 * in it's own panel of device specific controls. These could be things like a file
	 * name, gain settings, channel lists, whatever is available for the specific device
	 * <p>
	 * The AcquisitionDialog will handle placing this component on the dialog when a
	 * particular DaqSystem is selected. The component will be placed between a drop down 
	 * list of available DaqSystem's and a section of the dialog showing the sample rate and
	 * number of channels. 
	 * <p>
	 * @param acquisitionDialog the AcquisitionPaneFX this component will be added to
	 * @return dialog component specific to this DAQ device
	 */
	public DAQSettingsPane getDAQSpecificPane(AcquisitionPaneFX acquisitionPaneFX) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
