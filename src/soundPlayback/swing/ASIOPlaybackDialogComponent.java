package soundPlayback.swing;

import java.awt.Component;

import asiojni.ASIOPlaybackSystem;
import asiojni.ASIOSoundSystem;
import soundPlayback.PlaybackParameters;


/**
 * This currently doens't seem to need to do anything.
 * Output will be through the same ASIO device as input is
 * coming from. I'm automatically restricting to two channels
 * the dialog will only be needed in the future if it's altered
 * to output over > 2 channels. 
 * @author Doug Gillespie
 * @see ASIOPlaybackSystem
 * @see ASIOSoundSystem
 *
 */
public class ASIOPlaybackDialogComponent extends PlaybackDialogComponent {

	ASIOSoundSystem asioSoundSystem;
	
	ASIOPlaybackSystem asioPlaybackSystem;
	
	public ASIOPlaybackDialogComponent(ASIOSoundSystem asioSoundSystem, ASIOPlaybackSystem asioPlaybackSystem) {
		super();
		this.asioSoundSystem = asioSoundSystem;
		this.asioPlaybackSystem = asioPlaybackSystem;
	}

	@Override
	Component getComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	PlaybackParameters getParams(PlaybackParameters playbackParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void setParams(PlaybackParameters playbackParameters) {
		// TODO Auto-generated method stub

	}

}
