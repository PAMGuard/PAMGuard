package soundPlayback.swing;

import java.awt.Component;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import soundPlayback.PlaybackParameters;
import soundPlayback.PlaybackSystem;

/**
 * Class for playback systems to add a system specific dialog component
 * to the sound playback dialog. 
 * <p>
 * Generally, playback of incoming audio data must go out through the same device that 
 * it came in on so that the sound devices clock is correctly synchronised. 
 * So if you're acquiring data from a sound card, the output must be through the sound card, if
 * you're acquiring data through an NI board, the output must be through the same NI board. 
 * <p>
 * The class allows you to add a specific dialog component to the general playback dialog. 
 *  
 * @author Doug Gillespie
 * @see PlaybackSystem
 *
 */
public abstract class PlaybackDialogComponent  {

	private PlaybackDialog playbackDialog;

	/**
	 * Get the graphics component to be included in the playback dialog 
	 * @return
	 */
	abstract Component getComponent();
	
	/**
	 * Set the parameters in the dialog component
	 * @param playbackParameters
	 */
	abstract void setParams(PlaybackParameters playbackParameters);
	
	/**
	 * Get the parameters from the dialog component
	 * @param playbackParameters
	 * @return PlaybackParameters or null.
	 * @see PlaybackParameters
	 */
	abstract PlaybackParameters getParams(PlaybackParameters playbackParameters);

	/**
	 * Called from the parent Playbackdialog to 
	 * set it's reference when this thing get's added
	 * to the dialog. 
	 * @param playbackDialog
	 */
	public void setParentDialog(PlaybackDialog playbackDialog) {
		this.playbackDialog = playbackDialog;
	}

	/**
	 * 
	 * Get the reference to the parent playback dialog. Likely to 
	 * be null until the component is actually used. 
	 * @return the playbackDialog
	 */
	public PlaybackDialog getParentDialog() {
		return playbackDialog;
	}
	
	public void dataSourceChanged(PamDataBlock pamDataBlock) {
		
	}
	
}
