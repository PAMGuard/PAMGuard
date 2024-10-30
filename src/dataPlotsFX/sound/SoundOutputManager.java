package dataPlotsFX.sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;

import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;
import PamguardMVC.RawDataHolder;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.overlaymark.TDMarkerAdapter;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackDataServer;
import soundPlayback.PlaybackProgressMonitor;

/**
 * Handles play back in the TDisplayFX
 * 
 * @author Jamie Macaulay
 *
 */
public class SoundOutputManager {

	private TDDisplayFX tdDisplayFX;

	private ToggleButton play, pause;

	private ProgressMonitor progressMonitor;

	private int currentStatus, currentChannels;

	private long currentMillis; 

	public SoundOutputManager(TDDisplayFX tdDisplayFX) {
		super();

		final ToggleGroup group = new ToggleGroup();

		progressMonitor = new ProgressMonitor();

		this.tdDisplayFX = tdDisplayFX;
		play=new ToggleButton();
//		play.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PLAY_ARROW, Color.WHITE, PamGuiManagerFX.iconSize));
		play.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", PamGuiManagerFX.iconSize));
		play.setToggleGroup(group);
		play.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				playButtonPressed();
			}
		});

		//create button to open settings
		pause=new ToggleButton();
//		pause.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PAUSE, Color.WHITE, PamGuiManagerFX.iconSize));
		pause.setGraphic(PamGlyphDude.createPamIcon("mdi2p-pause", PamGuiManagerFX.iconSize));
		pause.setToggleGroup(group);
		pause.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				playPause();
			}
		});
	}

	public void playPause() {
		PlaybackControl player = getPlayer();
		if (player == null) {
			return;
		}
		player.stopViewerPlayback();
	}

	public void playButtonPressed() {
		playButtonPressed(getDisplayStart());
	}

	protected void playButtonPressed(long playStartTime) {
		playButtonPressed(playStartTime, getDisplayEnd(), null);
	}

	/**
	 * The play button has been pressed. 
	 * @param playStartTime
	 * @param playEndTime
	 * @param detGroup
	 */
	protected void playButtonPressed(Long playStartTime, Long playEndTime, DetectionGroupSummary detGroup) {
		PlaybackControl player = getPlayer();
		System.out.println("TDDisplayFX playback: " + player); 
		if (player == null) {
			//no sound output module added
			return;
		}

		if (playStartTime!=null && playEndTime!=null && player.hasPlayDataSource()) {
			//raw data has been loaded into PAMGuard and can be played within the mark
			player.playViewerData(playStartTime, playEndTime, progressMonitor);
		}
		else if (hasRawDataUnits(detGroup)){
			//there are selected raw data units which could be played in the mark
			ArrayList<PamDataUnit> rawDataUnits = getRawDataUnits(detGroup.getDataList());
			//this is a little complicated - must get the channels from the detection group...but
			//it could contain multiple channels from different data units. 

			int chanList = getDataUnitsChan(rawDataUnits);
			int nChan = PamUtils.getNumChannels(chanList);
			int channels = 3 & chanList; 

			if (playStartTime==null || playEndTime==null) {
				//use selected data units to define time limits for playing sound
				long[] minMax = getMinMaxMillis(getRawDataUnits(detGroup.getDataList())); 
				if (playStartTime==null) {
					//there is no mar but at least one raw data unit. Play the data units. 
					playStartTime=minMax[0]-1; 
				}
				if (playEndTime==null) {
					playEndTime=minMax[1]+1; 
				}
			}

			//play raw data units. 
			RawPlayLoader cpl = new RawPlayLoader(channels, detGroup);

			PlaybackControl.getViewerPlayback().playViewerData(channels, playStartTime, 
					playEndTime, progressMonitor, cpl);
		}
		else {
			System.err.println("SoundOutputManager: no audio data to play"); 
		}

	}

	/**
	 * Get the minimum and maximum time in millis of a list of data units. The min time is the start of the 
	 * first data unit and the end time is the end time of the last data unit. 
	 * @return the start millis of the first data unit and end time of the last data unit in the list. 
	 */
	private long[] getMinMaxMillis(List<PamDataUnit> dataUnits) {
		long[] minMax = {Long.MAX_VALUE, Long.MIN_VALUE}; 

		for (PamDataUnit dataUnit: dataUnits) {
			if (minMax[0]>dataUnit.getTimeMilliseconds()) {
				minMax[0] = dataUnit.getTimeMilliseconds(); 
			}
			if (minMax[1]<dataUnit.getEndTimeInMilliseconds()) {
				minMax[1] = dataUnit.getEndTimeInMilliseconds(); 
			}
		}

		return minMax; 
	}

	/**
	 * Check if PAMGaurd can play sound from the sound output module in viewer mode
	 * nad that the 
	 * 
	 * @return
	 */
	private boolean canPlayRawViewerSound() {
		PlaybackControl player = getPlayer();
		if (player == null) {
			//no sound output module added
			return false;
		}
		//System.out.println("CAN PLAY VIEWER SOUND: " + player.hasPlayDataSource());
		if (player.hasPlayDataSource()) {
			//raw data has been loaded into PAMGuard
			return true;
		}
		else return false;
	}


	private ArrayList<PamDataUnit>  getRawDataUnits(List<PamDataUnit> dataUnits ) {
		//extract only the raw data units. 
		ArrayList<PamDataUnit> rawDataUnits = new ArrayList<PamDataUnit>(); 
		for (int i=0; i<dataUnits.size(); i++) {
			if (dataUnits.get(i) instanceof RawDataHolder) {
				rawDataUnits.add(dataUnits.get(i));
			}
		}
		return rawDataUnits; 
	}

	/**
	 * Get the channel bitmap for a list of data units. Includes all channels for all units. 
	 * @param dataUnits - the data units to get channel bitmap for. 
	 * @return the channel bitmap for all data units in the list
	 */
	private int getDataUnitsChan(List<PamDataUnit> dataUnits) {
		int chans = 0;
		PamUtils.makeChannelMap(chans);
		for (int i=0; i<dataUnits.size(); i++) {
			chans = chans | dataUnits.get(i).getChannelBitmap();
		}
		return chans;
	}

	//	void playReconstructedClicks() {
	//		int chanList = clickControl.clickParameters.getChannelBitmap();
	//		int nChan = PamUtils.getNumChannels(chanList);
	//		if (nChan == 0) {
	//			return;
	//		}
	//		long startMillis = hScrollManager.getDisplayStartMillis();
	//		long endMillis = startMillis + displayLengthMillis;
	//		if (zoomer != null) {
	//			ZoomShape topShape = zoomer.getTopMostShape();
	//			if (topShape != null) {
	//				startMillis = (long) topShape.getXStart();
	//				endMillis = (long) (startMillis + topShape.getXLength());
	//			}
	//		}
	//		int channels = 3 & chanList; 
	//		ClickPlayLoader cpl = new ClickPlayLoader(channels);
	//		PlaybackControl.getViewerPlayback().playViewerData(channels, startMillis, 
	//				endMillis, clickPlaybackMonitor, cpl);
	//	}

	/**
	 * Check whether a DetectionGroupSummary contains any raw data units. 
	 * @param detGroup - the DetectionGroupSummary to test. 
	 * @return true if there are RawDataHolder data units in the DetectionGroupSummary
	 */
	private boolean hasRawDataUnits( DetectionGroupSummary detGroup) {
		if (detGroup==null) return false; 
		//check if there are any raw data units in the det group. 
		return getRawDataUnits(detGroup.getDataList()).size()>0 ;
	}

	/**
	 * Get the start of the display in millis. 
	 * @return the start of the display in millis. 
	 */
	private long getDisplayStart() {
		return tdDisplayFX.getTimeStart();
	}

	private long getDisplayEnd() {
		return getDisplayStart() + tdDisplayFX.getVisibleTime();
	}

	private PlaybackControl getPlayer() {
		return PlaybackControl.getViewerPlayback();
	}

	public Node getPlayButton() {
		return play;
	}

	public Node getPauseButton() {
		return pause;
	}

	private void enableControls() {
		PlaybackControl player = getPlayer();
		if (player == null) {
			pause.setSelected(true);
			return;
		}		
	}

	private void updateDisplay() {
		tdDisplayFX.playbackUpdate();
	}

	private class ProgressMonitor implements PlaybackProgressMonitor {

		@Override
		public void setProgress(int channels, long timeMillis, double percent) {
			currentChannels = channels;
			currentMillis = timeMillis;
			updateDisplay();
		}

		@Override
		public void setStatus(int status) {
			currentStatus = status;
			updateDisplay();
		}

	}

	public ArrayList<MenuItem> getMenuItems(TDPlotPane tdPlotPanel, long currentTime) {
		//System.out.println("SoundOutputManager: Get playback menu items");

		ArrayList<MenuItem> items = new ArrayList<>();
		TDGraphFX tdGraph = tdPlotPanel.getTDGraph();

		if (!(tdGraph.getOverlayMarkerManager().getCurrentMarker() instanceof TDMarkerAdapter)) {
			//could be the drag marker adapter. 
			return null; 
		}
		//get the detection group if selected. 
		TDMarkerAdapter currentMarker = (TDMarkerAdapter) tdGraph.getOverlayMarkerManager().getCurrentMarker();

		//get the overlay mark
		OverlayMark currentMark = currentMarker.getOverlayMark();


		DetectionGroupSummary detGroup = currentMarker.getSelectedDetectionGroup();

		//System.out.println("SoundOutputManager: detGroup: " + detGroup.getDataList());

		MenuItem menuItem = null;

		if (currentMark != null && (currentMark.getLimits()[0] != currentMark.getLimits()[1])) {
			//System.out.println("Current mark: " + currentMark.getMarkType() + "   " + currentMark.getLimits()[0] + " " + currentMark.getLimits()[1]);
			double[] markLimits = currentMark.getLimits();
			String icon;
			if (canPlayRawViewerSound()) {
				menuItem = new MenuItem("Play sound in mark");
				icon = "mdi2b-border-outside"; 
			}
			else if (hasRawDataUnits(detGroup)) {
				menuItem = new MenuItem("Play detections in mark");
				icon = "mdi2b-border-outside"; 
			}
			else {
				//no sound data is available 
				menuItem= new MenuItem("No raw sound data available");
//				icon = MaterialIcon.WARNING; 
				icon = "mdi2m-music-note-off"; 
				menuItem.setGraphic(PamGlyphDude.createPamIcon(icon, Color.WHITE, PamGuiManagerFX.iconSize));
				menuItem.setOnAction((action)->{
					PamDialogFX.showWarning(	"There is no raw slound data available to play. \n"
											+ 	"Go the Sound Acquisition module and add raw wave files\n" +
												"and/or ensure the Sound Ouput module has been added");
					//playButtonPressed((long) markLimits[0], (long) markLimits[1], detGroup);
				});
				items.add(menuItem);
				return items; 
			}

			menuItem.setGraphic(PamGlyphDude.createPamIcon(icon, Color.WHITE, PamGuiManagerFX.iconSize));
			menuItem.setOnAction((action)->{
				playButtonPressed((long) markLimits[0], (long) markLimits[1], detGroup);
			});
			items.add(menuItem);
		}
		else if (detGroup!=null && hasRawDataUnits(detGroup)){
			//only one detection selected (or perhaps more with say a ctrl select command - but no mark)
			menuItem = new MenuItem("Play detection");
//			menuItem.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PLAY_ARROW, Color.WHITE, PamGuiManagerFX.iconSize));
			menuItem.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", Color.WHITE, PamGuiManagerFX.iconSize));
			menuItem.setOnAction((action)->{
				playButtonPressed(null, null, detGroup);
			});			
			items.add(menuItem);
		}
		else {
			return items; 
		}

		
		//check if we can play the start to the end of the display. 
		if (canPlayRawViewerSound()) {
			menuItem = new MenuItem("Play from start");
//			menuItem.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PLAY_ARROW, Color.WHITE, PamGuiManagerFX.iconSize));
			menuItem.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", Color.WHITE, PamGuiManagerFX.iconSize));
			menuItem.setOnAction((action)->{
				playButtonPressed();
			});
			items.add(menuItem);

			menuItem = new MenuItem("Play from mouse position");
//			menuItem.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.MOUSE, Color.WHITE, PamGuiManagerFX.iconSize));
			menuItem.setGraphic(PamGlyphDude.createPamIcon("mdi2m-mouse", Color.WHITE, PamGuiManagerFX.iconSize));
			menuItem.setOnAction((action)->{
				playButtonPressed(currentTime);
			});
			items.add(menuItem);
		}

		menuItem = new MenuItem("Stop playback");
//		menuItem.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PAUSE, Color.WHITE, PamGuiManagerFX.iconSize));
		menuItem.setGraphic(PamGlyphDude.createPamIcon("mdi2p-pause", Color.WHITE, PamGuiManagerFX.iconSize));
		menuItem.setOnAction((action)->{
			playPause();
		});
		items.add(menuItem);
		return items;
	}


	class RawPlayLoader implements PlaybackDataServer {

		private int channels;
		private DetectionGroupSummary detGroupSummary;
		private double[][] rawData;
		private int nChannels;
		private int[] channelNumbers;
		private ListIterator<PamDataUnit> clickIterator;

		private volatile boolean cancelNow = false;

		/**
		 * The samplerate. 
		 */
		private double sampleRate;

		/**
		 * @param channels
		 * @param offlineEvent
		 */
		public RawPlayLoader(int channels, DetectionGroupSummary detGroupSummary) {
			super();
			this.channels = channels;
			this.detGroupSummary = detGroupSummary;
		}

		/**
		 * @param channels
		 */
		public RawPlayLoader(int channels) {
			super();
			this.channels = channels;
		}

		@Override
		public void cancelPlaybackData() {
			cancelNow = true;
		}

		@Override
		public void orderPlaybackData(PamObserver dataObserver,
				PlaybackProgressMonitor progressMonitor, float playbackRate, long startMillis,
				long endMillis) {

			//System.out.println("SoundOutputManager: Play raw data units!!!");

			/*
			 * Need to go through the clicks packing them into dummy
			 * rawDataUnits which can get sent on to dataObserver. 
			 */
			PamDataUnit pamDataUnit;

			nChannels = PamUtils.getNumChannels(channels);
			channelNumbers = PamUtils.getChannelArray(channels);

			//extract only the raw data units. 
			ArrayList<PamDataUnit> rawDataUnits = new ArrayList<PamDataUnit>(); 
			List<PamDataUnit> dataUnits = detGroupSummary.getDataList();
			sampleRate = -1; //the highest sample rate of the data units in the list. 
			for (int i=0; i<dataUnits.size(); i++) {
				if (dataUnits.get(i) instanceof RawDataHolder) {
					//set the maxsampler rate
					if (dataUnits.get(i).getParentDataBlock().getSampleRate()>sampleRate) {
						sampleRate = dataUnits.get(i).getParentDataBlock().getSampleRate(); 
					}
					rawDataUnits.add(dataUnits.get(i));
				}
			}

			//sort the data units in the list by their time in millis
			Collections.sort(dataUnits);


			int blockLength = (int) (playbackRate / 20); // will output data in 1/10 s blocks
			if (blockLength < 1000) {
				blockLength = 1000;
			}
			long blockMillis = (long) ((blockLength * 1000) / sampleRate);
			rawData = new double[nChannels][blockLength];
			RawDataUnit rawDataUnit;
			PamDataUnit click;
			PamDataUnit prevClick = null;


			long dataUnitMillis = startMillis;
			long dataUnitSample = 0;
			int blockPosition = 0;
			long currentClickSample = 0;
			int clickWavePos = 0;
			double[][] clickWave = null;
			int clickLen = 0;

			clickIterator= rawDataUnits.listIterator(); 
			try {
				click = clickIterator.next(); 
				if (click != null) {
					clickWave = ((RawDataHolder) click).getWaveData();
					clickLen = clickWave[0].length;
					clickWavePos = 0;

					blockPosition = (int) ((click.getTimeMilliseconds() - dataUnitMillis) * sampleRate / 1000.);
				}
				/*
				 * Main loop is over output data blocks since they will always exist. 
				 * For each set out output units, move through clicks (if any) and copy
				 * data from those clicks into the output data units. 
				 */
				while (dataUnitMillis < endMillis) {
					if (click == null) {
						break;
					}
					while (blockPosition < blockLength) {
						for (int iChan = 0; iChan < nChannels; iChan++) {
							rawData[iChan][blockPosition] = clickWave[iChan][clickWavePos];
						}
						blockPosition++;
						clickWavePos++;
						if (clickWavePos == clickLen) { // move onto the next click. 
							prevClick = click;
							if (clickIterator.hasNext()) {
								click = clickIterator.next(); ; //GET THE NEXT RAW DATA UNIT
								if (click != null) {
									clickWave = ((RawDataHolder) click).getWaveData();
									clickLen = clickWave[0].length;
									clickWavePos = 0;
									if (click.getStartSample() < prevClick.getStartSample() - sampleRate) {
										/**
										 * probably started a new file, so work out time of next click based
										 * on millisecond time. 
										 */
										blockPosition = (int) ((click.getTimeMilliseconds() - dataUnitMillis) * sampleRate / 1000.);
									}
									else {
										blockPosition += (click.getStartSample() - prevClick.getStartSample());
									}
								}
								else {
									break;
								}
							}
							else {
								break;
							}
						}
					}
					if (cancelNow) {
						break;
					}
					for (int i = 0; i < nChannels; i++) {
						rawDataUnit = new RawDataUnit(dataUnitMillis, 1<<channelNumbers[i],
								dataUnitSample, blockLength);
						rawDataUnit.setRawData(rawData[i]);
						dataObserver.addData(null, rawDataUnit);
						//						setSelectedClick(prevClick);
						rawData[i] = new double[blockLength];
					}
					dataUnitSample += blockLength;
					dataUnitMillis += blockMillis;
					blockPosition -= blockLength;
				}
			}
			catch (ConcurrentModificationException e) {
				// will hit here if click data block is modified in any way. 

			}
			catch (Exception e) {
				e.printStackTrace();
			}

			progressMonitor.setStatus(PlaybackProgressMonitor.PLAY_END);
		}


		@Override
		public double getDataSampleRate() {
			return sampleRate;
		}
	}


	/**
	 * @return the currentStatus
	 */
	public int getCurrentStatus() {
		return currentStatus;
	}

	/**
	 * @return the currentChannels
	 */
	public int getCurrentChannels() {
		return currentChannels;
	}

	/**
	 * @return the currentMillis
	 */
	public long getCurrentMillis() {
		return currentMillis;
	}
}
