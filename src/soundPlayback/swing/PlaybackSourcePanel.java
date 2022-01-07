package soundPlayback.swing;

import PamView.dialog.SourcePanel;

/**
 * Slight modifications to sourcePanel so that only the correct number of 
 * channels can be selected.
 * 
 * @author Doug Gillespie
 *
 */
public class PlaybackSourcePanel extends SourcePanel {
	
	PlaybackDialog playbackDialog;
	
	public PlaybackSourcePanel(PlaybackDialog playbackDialog, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		super(playbackDialog, sourceType, hasChannels, includeSubClasses);
		playExtras(playbackDialog);
	}

	public PlaybackSourcePanel(PlaybackDialog playbackDialog, String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		super(playbackDialog, borderTitle, sourceType, hasChannels, includeSubClasses);
		playExtras(playbackDialog);
	}
	
	private void playExtras(PlaybackDialog playbackDialog) {
		this.playbackDialog = playbackDialog;
		
	}

	@Override
	protected void selectionChanged(int channel) {
		// called when one of the channel check boxes is clicked
		super.selectionChanged(channel);
		if (playbackDialog != null) {
			playbackDialog.selectionChanged(channel);
		}
	}

	@Override
	protected void sourceChanged() {
		// called when a different data source is selected. 
		super.sourceChanged();
		if (playbackDialog != null) {
			playbackDialog.sourceChanged();
		}
	}

}
