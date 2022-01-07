package soundPlayback.swing;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import soundPlayback.preprocess.PlaybackFilter;
import soundPlayback.preprocess.PreprocessSwingComponent;

public class PlayFilterSideBar implements PreprocessSwingComponent {

	private PlaybackFilter playbackFilter;
		
	private PlayFilterSlider playFilterSlider;
	
	private BasicSidebarLayout basicSidebarLayout;

	public PlayFilterSideBar(PlaybackFilter playbackFilter) {
		this.playbackFilter = playbackFilter;
		playFilterSlider = new PlayFilterSlider();
		basicSidebarLayout = BasicSidebarLayout.makeBasicLayout(playFilterSlider);	
		basicSidebarLayout.setToolTipText("High pass filter the data before playback");
		playFilterSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				filterChanged();
			}
		});
	}

	protected void filterChanged() {
		playbackFilter.setValue(playFilterSlider.getDataValue());
		sayFilter();
	}

	@Override
	public JComponent getComponent() {
		return basicSidebarLayout.getComponent();
	}

	@Override
	public void update() {
		playFilterSlider.setDataValue(playbackFilter.getValue());
		sayFilter();
	}

	private void sayFilter() {
		basicSidebarLayout.setTextLabel(playbackFilter.getTextValue());
	}

}
