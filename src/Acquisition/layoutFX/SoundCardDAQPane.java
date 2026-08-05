package Acquisition.layoutFX;

import java.util.ArrayList;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Acquisition.SoundCardSystem;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamRawDataBlock;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * SoundCard settings controls for the DAQ pane. 
 * @author Jamie Macaulay
 *
 */
public class SoundCardDAQPane extends DAQSettingsPane {

	/**
	 * The main pane. 
	 */
	PamBorderPane mainPane;

	private ComboBox<String> audioDevices;

	/**
	 * The sound card system. 
	 */
	private SoundCardSystem soundCardSystem; 
	
	/**
	 * The level meter status bar pane.
	 */
	private LevelMeterPane levelMeterPane;

	public SoundCardDAQPane(SoundCardSystem soundCardSystem) {

		this.soundCardSystem=soundCardSystem; 

		mainPane = new PamBorderPane(); 

		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);

		Label title = new Label("Select audio line"); 
		PamGuiManagerFX.titleFont2style(title);

		audioDevices = new ComboBox<String>(); 
		audioDevices.setMaxWidth(Double.MAX_VALUE);
		
		holder.getChildren().addAll(title, audioDevices);
		mainPane.setCenter(holder);
	}



	@Override
	public void setParams() {

		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (soundCardSystem.getSoundCardParameters().systemType==null) 
			soundCardSystem.getSoundCardParameters().systemType=soundCardSystem.getSystemType();


		ArrayList<String> devices = soundCardSystem.getDevicesList();

		audioDevices.getItems().clear();
		for (int i = 0; i < devices.size(); i++) {
			audioDevices.getItems().add(devices.get(i));
		}

		soundCardSystem.getSoundCardParameters().deviceNumber = Math.max(Math.min(devices.size()-1, soundCardSystem.getSoundCardParameters().deviceNumber), 0);
		if (devices.size() > 0) {
			audioDevices.getSelectionModel().select(soundCardSystem.getSoundCardParameters().deviceNumber);
		}

	}

	@Override
	public boolean getParams() {
		if (audioDevices!=null) soundCardSystem.getSoundCardParameters().deviceNumber = audioDevices.getSelectionModel().getSelectedIndex();
		return true;
	}

	@Override
	public Object getParams(Object currParams) {
		return null;
	}

	@Override
	public void setParams(Object input) {
	}

	@Override
	public String getName() {
		return "Sound card params";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}

	@Override
	public Pane getStatusBarPane() {
		if (levelMeterPane == null) {
			levelMeterPane = new LevelMeterPane(soundCardSystem);
		}
		return levelMeterPane;
	}
	
	/**
	 * A compact level meter pane that displays per-channel audio levels
	 * using horizontal progress bars. Designed to fit within the narrow 
	 * control bar below the tabs.
	 */
	public static class LevelMeterPane extends PamHBox implements PamObserver {
		
		/**
		 * Progress bars for each channel.
		 */
		private ProgressBar[] levelBars;
		
		/**
		 * Peak levels per channel (0.0 to 1.0 range).
		 */
		private double[] peakLevels;
		
		/**
		 * The sound card system.
		 */
		private SoundCardSystem soundCardSystem;
		
		/**
		 * Number of channels currently displayed.
		 */
		private int nChannels = 0;
		
		/**
		 * The data block we're observing.
		 */
		private PamRawDataBlock observedBlock;
		
		/**
		 * Label showing "Levels".
		 */
		private Label titleLabel;
		
		/**
		 * VBox holding the level bars.
		 */
		private VBox barsHolder;
		
		/**
		 * Decay factor for peak hold (per update).
		 */
		private static final double DECAY_FACTOR = 0.85;
		
		/**
		 * Minimum time between UI updates in nanoseconds (approx 20fps).
		 */
		private static final long UPDATE_INTERVAL_NS = 50_000_000L;
		
		/**
		 * Last UI update time.
		 */
		private long lastUpdateTime = 0;
		
		public LevelMeterPane(SoundCardSystem soundCardSystem) {
			this.soundCardSystem = soundCardSystem;
			
			this.setSpacing(5);
			this.setAlignment(Pos.CENTER_LEFT);
			this.setPadding(new Insets(2, 10, 2, 10));
			
			titleLabel = new Label("Levels");
			titleLabel.setStyle("-fx-font-size: 10px;");
			titleLabel.setMinWidth(35);
			
			barsHolder = new VBox(1);
			barsHolder.setAlignment(Pos.CENTER);
			barsHolder.setMinWidth(100);
			barsHolder.setPrefWidth(150);
			
			this.getChildren().addAll(titleLabel, barsHolder);
			
			// Set up initial bars
			setupBars(2);
			
			// Start observing when the scene is available
			this.sceneProperty().addListener((obs, oldScene, newScene) -> {
				if (newScene != null) {
					startObserving();
				} else {
					stopObserving();
				}
			});
		}
		
		/**
		 * Set up the level bars for the given number of channels.
		 * @param channels - number of audio channels.
		 */
		private void setupBars(int channels) {
			if (channels <= 0) channels = 1;
			this.nChannels = channels;
			this.peakLevels = new double[channels];
			this.levelBars = new ProgressBar[channels];
			
			barsHolder.getChildren().clear();
			
			// Calculate bar height - must fit in the narrow toolbar
			// Toolbar is ~45px, so bars need to be compact
			double barHeight = Math.max(4, Math.min(10, 30.0 / channels));
			
			for (int i = 0; i < channels; i++) {
				ProgressBar bar = new ProgressBar(0);
				bar.setPrefHeight(barHeight);
				bar.setMaxHeight(barHeight);
				bar.setPrefWidth(150);
				bar.setMaxWidth(Double.MAX_VALUE);
				HBox.setHgrow(bar, Priority.ALWAYS);
				// Style: green to yellow gradient via CSS
				bar.setStyle("-fx-accent: derive(limegreen, -10%);");
				levelBars[i] = bar;
				barsHolder.getChildren().add(bar);
			}
		}
		
		/**
		 * Start observing the raw data block for level metering.
		 */
		private void startObserving() {
			AcquisitionControl control = soundCardSystem.getAcquisitionControl();
			if (control == null) return;
			AcquisitionProcess process = control.getAcquisitionProcess();
			if (process == null) return;
			PamRawDataBlock rawBlock = process.getRawDataBlock();
			if (rawBlock == null) return;
			
			if (observedBlock != rawBlock) {
				stopObserving();
				observedBlock = rawBlock;
				rawBlock.addObserver(this);
			}
			
			// Update channel count
			int chans = control.getAcquisitionParameters().nChannels;
			if (chans != nChannels) {
				setupBars(chans);
			}
		}
		
		/**
		 * Stop observing the raw data block.
		 */
		private void stopObserving() {
			if (observedBlock != null) {
				observedBlock.deleteObserver(this);
				observedBlock = null;
			}
		}
		
		@Override
		public String getObserverName() {
			return "SoundCard Level Meter";
		}
		
		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			if (!(pamDataUnit instanceof RawDataUnit)) return;
			
			RawDataUnit rawUnit = (RawDataUnit) pamDataUnit;
			double[] rawData = rawUnit.getRawData();
			if (rawData == null || rawData.length == 0) return;
			
			int channel = PamUtils.getSingleChannel(rawUnit.getChannelBitmap());
			if (channel < 0 || channel >= nChannels) return;
			
			// Find peak absolute value in this data unit
			double peak = 0;
			for (int i = 0; i < rawData.length; i++) {
				double abs = Math.abs(rawData[i]);
				if (abs > peak) peak = abs;
			}
			
			// Apply decay to existing peak, then take max with new peak
			peakLevels[channel] = Math.max(peak, peakLevels[channel] * DECAY_FACTOR);
			
			// Throttle UI updates
			long now = System.nanoTime();
			if (now - lastUpdateTime > UPDATE_INTERVAL_NS) {
				lastUpdateTime = now;
				final double[] levelsCopy = peakLevels.clone();
				Platform.runLater(() -> updateBars(levelsCopy));
			}
		}
		
		/**
		 * Update the progress bars on the FX thread.
		 * @param levels - the current peak levels.
		 */
		private void updateBars(double[] levels) {
			for (int i = 0; i < Math.min(levels.length, levelBars.length); i++) {
				double level = Math.min(1.0, levels[i]);
				levelBars[i].setProgress(level);
				
				// Change colour based on level
				if (level > 0.9) {
					levelBars[i].setStyle("-fx-accent: red;");
				} else if (level > 0.7) {
					levelBars[i].setStyle("-fx-accent: orange;");
				} else {
					levelBars[i].setStyle("-fx-accent: derive(limegreen, -10%);");
				}
			}
		}
		
		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		}
		
		@Override
		public long getRequiredDataHistory(PamObservable observable, Object arg) {
			return 0;
		}
		
		@Override
		public void removeObservable(PamObservable observable) {
			observedBlock = null;
		}
		
		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
		}
		
		@Override
		public void noteNewSettings() {
			// Channel count may have changed, restart observation
			Platform.runLater(() -> startObserving());
		}
		
		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		}
		
		@Override
		public PamObserver getObserverObject() {
			return this;
		}
		
		@Override
		public void receiveSourceNotification(int type, Object object) {
		}
	}

}
