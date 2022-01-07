package soundPlayback.fx;

import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;

/**
 * Slight modifications to sourcePanel so that only the correct number of 
 * channels can be selected.
 * 
 * @author Jamie Macaulay
 *
 */
public class PlaybackSourcePane extends SourcePaneFX {
	
	
	PlayBackPane playbackDialog;
	
	public PlaybackSourcePane(PlayBackPane playbackDialog, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		super(sourceType, hasChannels, includeSubClasses);
		playExtras(playbackDialog);
	}

	public PlaybackSourcePane(PlayBackPane playbackDialog, String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		super(borderTitle, sourceType, hasChannels, includeSubClasses);
		playExtras(playbackDialog);
	}
	
	private void playExtras(PlayBackPane playbackDialog) {
		this.playbackDialog = playbackDialog;
	}

	@Override
	public void selectionChanged(int channel) {
		// called when one of the channel check boxes is clicked
		super.selectionChanged(channel);
		if (playbackDialog != null) {
			playbackDialog.selectionChanged(channel);
		}
	}

	@Override
	public void sourceChanged() {
		// called when a different data source is selected. 
		super.sourceChanged();
		if (playbackDialog != null) {
			playbackDialog.sourceChanged();
		}
	}

}
