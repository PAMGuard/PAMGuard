package soundPlayback.fx;

import PamController.PamController;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamguardMVC.PamRawDataBlock;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import soundPlayback.PlaybackChangeObserver;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.PlaybackSystem;

/**
 * FX settings pane for the sound sound playback. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PlayBackPane extends SettingsPane<PlaybackParameters>  implements PlaybackChangeObserver{

	public static final double PREF_WIDTH = 350;

	/**
	 * The main pane. 
	 */
	private BorderPane mainPane; 

	/**
	 * The source pane for selecting audio data source. 
	 */
	private PlaybackSourcePane playbackSourcePane;

	/**
	 * Reference to the playback control. 
	 */
	private PlaybackControl playbackControl;

	/**
	 * The source panel. 
	 */
	private PlaybackSourcePane sourcePanel;

	/*
	 * Create the dialog panel. 
	 */
	private PamBorderPane systemSettingsPane; 


	private int maxChannels = 2;

	private PlaybackSystem playbackSystem;

	private PlaybackSettingsPane playbackDialogComponent;

	private Pane systemHolderPane;

	private PlaybackParameters playbackParameters;


	public PlayBackPane(PlaybackControl playbackControl) {
		super(null);
		this.playbackControl = playbackControl;

		mainPane = new BorderPane(createPlaybackPane()); 
		mainPane.setPrefWidth(PREF_WIDTH);
	}

	/**
	 * Create the playback pane. 
	 * @return the playback pane. 
	 */
	public Pane createPlaybackPane() {
		sourcePanel = new PlaybackSourcePane(this, "Sound Source", RawDataUnit.class, true, true);

		PamGuiManagerFX.titleFont2style(sourcePanel.getTitleLabel());

		systemSettingsPane = new PamBorderPane(); 
		systemSettingsPane.setTop(sourcePanel);

		//systemSettingsPane.setTopSpace(5.0);

		//		TabPane tabbedPane = new TabPane();
		//		tabbedPane.getTabs().add(new Tab("Playback", dialogPanel));

		sourcePanel.addSelectionListener((obsVal, oldVal, newVal)->{
			sourceChanged();
		}); 


		return systemSettingsPane; 
	}

	protected void sourceChanged() {
		playbackSystem = playbackControl.findPlaybackSystem(sourcePanel.getSource());
		if (playbackSystem != null) {
			playbackSystem.addChangeObserver(this);
			maxChannels = playbackSystem.getMaxChannels();
		}
		//		maxChannels = playbackControl.getMaxPlaybackChannels(playbackSystem);
		enableChannelBoxes();
		sortDialogComponent();
		if (playbackDialogComponent != null) {
			playbackDialogComponent.dataSourceChanged(sourcePanel.getSource());
		}
	}

	private void sortDialogComponent() {
		if (playbackSystem != null) {
			playbackDialogComponent = playbackSystem.getSettingsPane(); 
			if (playbackDialogComponent == null) {
				playbackDialogComponent = new EmptyDialogComponent();
			}
			systemHolderPane = playbackDialogComponent.getPane();
			if (systemHolderPane != null) {
				
				systemSettingsPane.setCenter(systemHolderPane);
				playbackDialogComponent.setParams(playbackParameters);
			}

		}
		else {
			playbackDialogComponent = new EmptyDialogComponent();
			systemSettingsPane.setCenter(systemHolderPane = playbackDialogComponent.getPane());
		}
	}



	private void enableChannelBoxes() {
		/*
		 * do two checks 1) that no more than max chnnels are checked 
		 * 2) uncheck any boxes that exceed this limit. 
		 */
		CheckBox channelBoxes[] = sourcePanel.getChannelBoxes();
		int totalChecked = 0;
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i].isVisible() == false) {
				channelBoxes[i].setSelected(false);
			}
			else {
				if (channelBoxes[i].isSelected()) {
					if (totalChecked >= maxChannels) {
						channelBoxes[i].setSelected(false);
					}
					totalChecked++;
				}
			}
		}
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i].isVisible() == false) continue;
			if (totalChecked < maxChannels) {
				channelBoxes[i].setDisable(false);
			}
			else if (channelBoxes[i].isSelected() == false) {
				channelBoxes[i].setDisable(true);
			}
		}

	}

	@Override
	public PlaybackParameters getParams(PlaybackParameters currParams) {
		
		playbackParameters.channelBitmap = sourcePanel.getChannelList();
		playbackParameters.dataSource = sourcePanel.getSourceIndex();
		if (playbackDialogComponent != null) {
			PlaybackParameters nP = playbackDialogComponent.getParams(playbackParameters);
			if (nP != null) {
				playbackParameters = nP.clone();
			}
		}
		return playbackParameters;
	}

	@Override
	public void setParams(PlaybackParameters playbackParameters) {

		this.playbackParameters=playbackParameters.clone(); 

		PamRawDataBlock pamRawDataBlock = PamController.getInstance().getRawDataBlock(playbackParameters.dataSource);
		sourcePanel.setSource(pamRawDataBlock);
		sourcePanel.setChannelList(playbackParameters.channelBitmap);
		if (playbackDialogComponent != null) {
			playbackDialogComponent.setParams(playbackParameters);
			playbackDialogComponent.setParentDialog(this);
		}
		sourceChanged();
		//sideOptionsPanel.setParams(playbackParameters);

	}

	@Override
	public String getName() {
		return "Sound Playback Options";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	public void selectionChanged(int channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playbackChange() {
		// TODO Auto-generated method stub

	}

	private class EmptyDialogComponent extends PlaybackSettingsPane {

		PamBorderPane panel;

		public EmptyDialogComponent() {
			super();

			panel = new PamBorderPane();
			if (playbackSystem == null) {
				panel.setCenter(new Label(" Playback not possible with current input"));
			}
			else {
				panel.setCenter(new Label(" Playback through " + playbackSystem.getName()));
			}

		}

		@Override
		Pane getPane() {
			return panel;
		}

		@Override
		PlaybackParameters getParams(PlaybackParameters playbackParameters) {
			return null;
		}

		@Override
		public void setParams(PlaybackParameters playbackParameters) {

		}

	}

}