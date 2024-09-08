package videoRangePanel.layoutFX;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import PamUtils.PamCalendar;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.sliders.PamSlider;
import videoRangePanel.VRControl;
import videoRangePanel.pamImage.PamImage;

//import ws.schild.jave.Encoder;
//import ws.schild.jave.EncoderException;
//import ws.schild.jave.MultimediaInfo;
//import ws.schild.jave.MultimediaObject;
//import ws.schild.jave.VideoInfo;
//import ws.schild.jave.VideoSize;

/**
 * Basic media player to watch videos in the video range module. 
 */
public class VRMediaView extends PamBorderPane implements VRImage {

	/**
	 * The current media. 
	 */
	private Media media;

	/**
	 * Plays the video. 
	 */
	private MediaView mediaView;

	/**
	 * The media player. 
	 */
	private MediaPlayer player;

	/**
	 * Slider which allows users to select video time frame
	 */
	private PamSlider seekslider;

	private ChangeListener<Duration> timeListener;

	/**
	 * The control pane with controls to start stop video etc. 
	 */
	private Pane controlPane;

	/**
	 * Button to play and pause media
	 */
	private PamButton playButton;

	/**
	 * Button to move one frame to the left. 
	 */
	private PamButton leftFrame;

	/**
	 * Button to move one frame to the right 
	 */
	private PamButton rightFrame;

	/**
	 * Reference to the current media file 
	 */
	private File currentFile;

	/**
	 * The volume button
	 */
	private PamButton volumeButton;

	/**
	 * Button which moves to next media
	 */
	private PamButton nextMedia;

	/**
	 * Button which moves to previous media
	 */
	private PamButton prevMedia;

	/**
	 * The volume slider
	 */
	private PamSlider volumeSlider;

	/**
	 * Volume listener
	 */
	private ChangeListener<Number> volumeListener;

	/**
	 * True if volume is muted.  
	 */
	private boolean isMuted;

	/**
	 * The volume before the mute button was pressed
	 */
	private double lastVolume;

	/**
	 * The meta data for the current video file. 
	 */
	private MetaData metaData;

	/**
	 * The current file time. 
	 */
	private long fileTime;

	/**
	 * Reference to the VRDisplay. 
	 */
	private VRDisplayFX vrDisplayFX;

	/**
	 * Skip forwards. 
	 */
	private PamButton skipForward;

	/**
	 * 	Skip backwards
	 */
	private PamButton skipBackward;

	/**
	 * The number of millis to jump for skip buttons. 
	 */
	private long skipMillis=2000;


	final static int controlIconSize=VRImagePane.iconSize; 

	/**
	 * Pane which can edit the video colours etc. 
	 */
	private VRImageEditPane editPane;

	private Label timeLabel;


	/**
	 * The height of the hiding bar. 
	 */
	public static double hideHeight=100; 


	public VRMediaView(VRDisplayFX vrDisplayFX) {
		this.vrDisplayFX=vrDisplayFX; 
		try {
			mediaView = new MediaView(player);

			editPane= new VRImageEditPane(); 

			mediaView.setEffect(editPane.getColorAdjust()); 
			controlPane=createControlPane(); 
			this.setCenter(mediaView);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
	}

	/**
	 * Set the media file. This will automatically start playing the media. 
	 * @param currentFile - the current file. 
	 */
	public boolean setMedia(File currentFile) {
		this.currentFile=currentFile; 
		this.fileTime=currentFile.lastModified(); 

		try {
			media = new Media(currentFile.toURI().toURL().toExternalForm());


			//player.getMedia().getMetadata(); 
			//        Media media = new Media("file:///home/paul/MoviePlayer/trailers/sintel.mp4");
			player = new MediaPlayer(media);

			//the media player is loaded asynchronously so must wait. 
			player.setOnReady(() -> {
				//the meta data 
				this.metaData = getMetaData(media);
				setUpPlayer() ; 
				setImageFreeze();

				Platform.runLater(()->{
					vrDisplayFX.getInfoPane().setMetaText( getMetaData());
				});

			});
			//			player.setAutoPlay(true);
			mediaView.setMediaPlayer(player);

			//wait for the player to be ready....
			//player.getOnReady().wait();

			//18/09/2023 - really no great but only way I could 
			Thread.sleep(100);


			//			while (player.getStatus()==MediaPlayer.Status.UNKNOWN) {

			//not great just to put a sleep here but while loop to wait on the 
			//			Thread.sleep(100);
			//			System.out.println("Status: " + player.getStatus());
			//				player.play();
			//			}

			return true;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}


	/**
	 * Get meta data from the video file and save in MetaData class. 
	 * @return the metadata for the video file. 
	 */
	private MetaData getMetaData(Media currentFile) {

		MetaData metaData = new MetaData(); 
		try {
			ObservableMap<String, Object> vidMetaData = currentFile.getMetadata(); 

			//			System.out.println("File information: " + currentFile.getHeight() + "  " +  currentFile.getWidth() +   "  "  + currentFile.getMetadata());

			//TODO - need to implement JavaFX metadata here. 
			//			printDetails("Bit-rate", metaData.bitRate=currentFile.getBitRate());
			//			printDetails("Frame Rate", metaData.frameRate=vInfo.getFrameRate());
			printDetails("Height", metaData.height=currentFile.getHeight());
			printDetails("Width", metaData.width=currentFile.getHeight());

			printDetails("Duration", metaData.duration=(long) currentFile.getDuration().toMillis());
			//			printDetails("Format", metaData.format=info.getFormat());
			//
			//			printDetails("Bit-rate", metaData.bitRate=info.getAudio().getBitRate());
			//			printDetails("Channels",  metaData.audioChannels=info.getAudio().getChannels());
			//			printDetails("Sampling Rate", metaData.audioSamplingRate=info.getAudio().getSamplingRate());

		} catch (Exception e) {
			System.out.println("Could not find meta data!");
			e.printStackTrace();
		}
		return metaData; 
	}


	boolean wasPausedOnDrag =false; 


	/**
	 * Set up the player controls to play, pause and set time of media. This is called whenever a new Media File is 
	 * played. 
	 */
	private void setUpPlayer() {

		//		player.setOnReady(new Runnable() {
		//			@Override
		//			public void run() {

		int w = player.getMedia().getWidth();
		int h = player.getMedia().getHeight();

		seekslider.setMin(0.0);
		seekslider.setValue(0.0);
		seekslider.setMax(player.getTotalDuration().toSeconds());

		//				System.out.println("Get total duration:" + player.getTotalDuration()); 

		player.setVolume(0.5);

		//			}
		//		});


		/******Time Slider****/

		//must remove previous lister so we don;t end up generating lots of listeners
		if (timeListener!=null) player.currentTimeProperty().removeListener(timeListener); 
		player.currentTimeProperty().addListener(timeListener = new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration current) {
				seekslider.setValue(current.toSeconds());
				setTimeLabel();
				vrDisplayFX.getVrControl().getCurrentImage().setTimeMilliseconds(getCurrentTime());
				vrDisplayFX.update(VRControl.IMAGE_TIME_CHANGE);
			}
		});


		//need to pause the video if the slider is dragged and than start again when slider is Ok. 
		seekslider.valueChangingProperty().addListener((obsVal, oldVal, newVal)->{
			if (newVal && newVal!=oldVal) {
				if (player.statusProperty().get()==	MediaPlayer.Status.PLAYING) {
					wasPausedOnDrag=false; 
				}
				else wasPausedOnDrag=true;
				player.pause();
			}
			else {
				if (!wasPausedOnDrag) player.play();
			}
		});

		//change the time labels whilst dragging. 
		seekslider.setOnMouseDragged((mouse)->{
			//now set the time but based on the slider, not the current time property as this 
			//is static untill the slider stops dragging. Nort neat but faster.#
			long sliderTime=(long) (this.fileTime+seekslider.getValue()*1000); 
			String time = PamCalendar.formatDateTime2(sliderTime,"yyyy-MM-dd HH:mm:ss.SSS", true); 
			this.timeLabel.setText(time);
			vrDisplayFX.getVrControl().getCurrentImage().setTimeMilliseconds(sliderTime);
			vrDisplayFX.update(VRControl.IMAGE_TIME_CHANGE);
		});


		//need to seek on mouse released or messes up the dragging. 
		seekslider.setOnMouseReleased((mouse)->{
			// multiply duration by percentage calculated by slider position
			player.seek(Duration.seconds(seekslider.getValue()));
		});


		//		///set up controls 
		//		seekslider.setOnMousePressed((mouseEvent) -> {
		//				player.seek(Duration.seconds(seekslider.getValue()));
		//		});

		/*****Volume Controls*****/

		//must remove previous lister so we don't end up generating lots of listeners
		if (volumeListener!=null)volumeSlider.valueProperty().removeListener(volumeListener);
		volumeSlider.valueProperty().addListener(volumeListener=new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (volumeSlider.isValueChanging()) {
					player.setVolume(volumeSlider.getValue() / 100.0);
					setVolumeGraphic(player.getVolume());
				}
			}
		});


		volumeButton.setOnAction((action)->{
			if (!isMuted) {
				isMuted=true; 
				lastVolume=player.getVolume(); 
				volumeSlider.setValue(0); //slider handles animation
			}
			else {
				isMuted=false;
				volumeSlider.setValue(lastVolume*100); 
			}
			player.setVolume(volumeSlider.getValue() / 100.0);
			setVolumeGraphic(player.getVolume());
		}); 


		/*****Play/Pause Controls*****/


		player.setOnPlaying(()->{
			//			playButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PAUSE, controlIconSize));
			playButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-pause", controlIconSize));
		});

		player.setOnPaused(()->{
			//			playButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PLAY_ARROW, controlIconSize));
			playButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", controlIconSize));
		});

		playButton.setOnAction((action)->{
			if (player.statusProperty().get()==	MediaPlayer.Status.PLAYING) {
				player.pause();
				setImageFreeze(); //save image to VRControl. 
			}
			else if (player.statusProperty().get()==	MediaPlayer.Status.PAUSED || player.statusProperty().get()==MediaPlayer.Status.READY) {
				player.play();
			}
		});

		/*****Seek Controls*****/


		leftFrame.setOnAction((action)->{
			player.pause();
			moveFrame( false, getSingleFrameDuration()); 
		});

		rightFrame.setOnAction((action)->{
			player.pause();
			moveFrame(true, getSingleFrameDuration()); 
		});

		skipForward.setOnAction((action)->{
			player.pause();
			moveFrame(true, skipMillis); 
		});

		skipBackward.setOnAction((action)->{
			player.pause();
			moveFrame(false, skipMillis); 
		});

		prevMedia.setOnAction((action)->{
			vrDisplayFX.nextImage(true);
		});

		nextMedia.setOnAction((action)->{
			vrDisplayFX.nextImage(false);
		});

		setTimeLabel() ;


	}


	/**
	 * Set the volume graphic. . 
	 * @param volume
	 */
	private void setVolumeGraphic(double volume) {
		if (volume ==0) {
			//			volumeButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.VOLUME_OFF, controlIconSize));
			volumeButton.setGraphic(PamGlyphDude.createPamIcon("mdi2v-volume-off", controlIconSize));

		}
		else if (volume>0 && volume<=0.33) {
			//			volumeButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.VOLUME_MUTE, controlIconSize));
			volumeButton.setGraphic(PamGlyphDude.createPamIcon("mdi2v-volume-mute", controlIconSize));
		}
		else if (volume>0.33 && volume<=0.66) {
			//			volumeButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.VOLUME_DOWN, controlIconSize));
			volumeButton.setGraphic(PamGlyphDude.createPamIcon("mdi2v-volume-medium", controlIconSize));

		}
		else if (volume>0.66 && volume<1) {
			//			volumeButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.VOLUME_UP, controlIconSize));
			volumeButton.setGraphic(PamGlyphDude.createPamIcon("mdi2v-volume-high", controlIconSize));

		}
	}

	/**
	 * Get the current frame time of the video. This is the duration plus the start time.
	 * @return the current time in millis; 
	 */
	public long getCurrentTime() {
		return (long) (this.fileTime+player.getCurrentTime().toMillis());
	}

	/**
	 * Get the duration of a single frame. 
	 * @return the duration of a single frame in milliseconds. 
	 */
	private long getSingleFrameDuration() {
		if ( metaData.frameRate==0.0) return 100; 
		long frameDuration =  (long) (Math.ceil(1000.*(1/(double) metaData.frameRate)));
		return frameDuration; 
	}

	/**
	 * Move one video frame forward or backward. 
	 * @param forward - True to move forward, false to move frame back. 
	 */
	public void moveFrame(boolean forward, long millis) {
		Duration duration=player.getCurrentTime();

		//figure out the time to move.

		//long frameDuration =  (long) (Math.ceil(1000.*(1/(double) metaData.frameRate)));

		//		System.out.println("Frame duration millis: " + millis);

		if (!forward) millis =-millis;

		Duration seekDuration= new Duration(duration.toMillis()+millis); 

		player.seek(seekDuration);

		//		ObservableMap<String, Object> onsMap= media.getMetadata();
		//		System.out.println("The size of the key set is: " + onsMap.keySet().size()); 
		//		System.out.println("The size of the markers set is: " + media.getMarkers().size()); 
		//
		//		for (int i=0; i<onsMap.keySet().size(); i++) {
		//			System.out.println(onsMap.keySet().iterator().next()); 
		//		}
	}


	private static String metaDataDetails(String item, Object details) {
		return String.format("%s : %s", item, details);
	}

	/**
	 * Print meta data details
	 * @param item
	 * @param details
	 */
	private static void printDetails(String item, Object details) {
		System.out.println(metaDataDetails(item, details));
	}

	/**
	 * Get pane with controls for the Media Player.
	 */
	@Override
	public Pane getControlPane() {
		return controlPane; 
	}

	/**
	 * Create the pane which shows the vid
	 * @return the control pane for the media player
	 */
	private Pane createControlPane() {

		final VBox vbox = new VBox();
		vbox.setSpacing(5);; 
		seekslider = new PamSlider(0, 100, 0);
		seekslider.setTrackColor(Color.GRAY); 

		vbox.getChildren().add(seekslider);

		//Media playback controls. 

		HBox playBackControls = new HBox(); 
		playBackControls.setSpacing(5);
		playBackControls.setAlignment(Pos.CENTER);

		//the play and pause button
		playButton = new PamButton(); 
		//		playButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.PLAY_ARROW, controlIconSize));
		playButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", controlIconSize));
		playButton.getStyleClass().add("square-button-trans");
		playButton.setTooltip(new Tooltip("Play or pause the media"));


		leftFrame = new PamButton(); 
		//		leftFrame.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.CHEVRON_LEFT, controlIconSize));
		leftFrame.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", controlIconSize));
		leftFrame.getStyleClass().add("square-button-trans");
		leftFrame.setTooltip(new Tooltip("Move to the previous frame"));

		rightFrame= new PamButton(); 
		//		rightFrame.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.CHEVRON_RIGHT,controlIconSize));
		rightFrame.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-right",controlIconSize));
		rightFrame.getStyleClass().add("square-button-trans");
		rightFrame.setTooltip(new Tooltip("Move to the next frame"));

		nextMedia= new PamButton(); 
		//		nextMedia.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SKIP_PREVIOUS,controlIconSize));
		nextMedia.setGraphic(PamGlyphDude.createPamIcon("mdi2s-skip-previous",controlIconSize));
		nextMedia.getStyleClass().add("square-button-trans");
		nextMedia.setTooltip(new Tooltip("Move to the next image or video"));

		prevMedia= new PamButton(); 
		//		prevMedia.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SKIP_NEXT,	controlIconSize));
		prevMedia.setGraphic(PamGlyphDude.createPamIcon("mdi2s-skip-next", controlIconSize));
		prevMedia.getStyleClass().add("square-button-trans");
		prevMedia.setTooltip(new Tooltip("Move to the previous image or video"));

		skipForward= new PamButton(); 
		//		skipForward.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.CHEVRON_DOUBLE_RIGHT,	controlIconSize));
		skipForward.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-double-right",	controlIconSize));
		skipForward.getStyleClass().add("square-button-trans");
		skipForward.setTooltip(new Tooltip("Skip 2 seconds forward"));

		skipBackward= new PamButton(); 
		//		skipBackward.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.CHEVRON_DOUBLE_LEFT,	controlIconSize));
		skipBackward.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-double-left", controlIconSize));
		skipBackward.getStyleClass().add("square-button-trans");
		skipBackward.setTooltip(new Tooltip("Skip 2 seconds backward"));


		playBackControls.getChildren().addAll(nextMedia, skipBackward, leftFrame, playButton, rightFrame, skipForward,  prevMedia); 


		//Time label
		timeLabel = new Label("");
		StackPane.setAlignment(timeLabel, Pos.CENTER_RIGHT);
		timeLabel.setPadding(new Insets(0,20,0,0));


		//Volume controls

		HBox volumeControls = new HBox(); 
		volumeControls.setSpacing(5);
		volumeControls.setAlignment(Pos.CENTER_LEFT);

		volumeSlider= new PamSlider(0,100,0); 
		volumeSlider.setValue(50); 
		volumeSlider.setTrackColor(Color.GRAY); 


		volumeButton= new PamButton(); 
		this.setVolumeGraphic(0.5);
		volumeButton.getStyleClass().add("square-button-trans");


		volumeControls.getChildren().addAll(volumeButton, volumeSlider);
		volumeControls.setMaxWidth(150); //so does not overlap the hide pane. 

		StackPane stackPane = new StackPane(); 
		stackPane = new StackPane(); 
		stackPane.getChildren().add(playBackControls); 
		stackPane.getChildren().add(volumeControls); 
		stackPane.getChildren().add(timeLabel); 
		StackPane.setAlignment(playBackControls, Pos.CENTER);
		StackPane.setAlignment(volumeControls, Pos.CENTER_LEFT);
		//		stackPane.setStyle("-fx-background-color:  red"); //from dark stylesheet

		vbox.getChildren().add(stackPane); 

		return vbox; 
	}

	/**
	 * Set the time label. 
	 */
	private void setTimeLabel() {
		String time = PamCalendar.formatDateTime2( getCurrentTime(),"yyyy-MM-dd HH:mm:ss.SSS", true); 
		this.timeLabel.setText(time);
	}

	@Override
	public Node getNode() {
		return this;
	}


	/**
	 * Holds meta data for the video file. 
	 * @author Jamie Macaulay 
	 *
	 */
	protected class MetaData {

		/**
		 * The audio sampling rate in number of samples per second
		 */
		public int audioSamplingRate;

		/**
		 * The num,ber of audio channels
		 */
		public int audioChannels;

		/**
		 * Video file format
		 */
		public String format;

		/**
		 * Duration of the video file
		 */
		public long duration;

		/**
		 * Size of the video file 
		 */
		public int height;

		/**
		 * Size of the video file 
		 */
		public int width;

		/**
		 * The frame rate of the video 
		 */
		public float frameRate;

		/**
		 * 
		 * The bit rate of the video file. 
		 */
		public int bitRate;

	}



	/**
	 * Take a snapshot of the video and return an image with the correct time metadata etc. 
	 * @return the pamImage representing the snapshot. 
	 */
	public PamImage getImageSnapshot() {

		//System.out.println("Fit height: " + mediaView.getFitHeight());
		//		
		//		if (metaData==null || metaData.width==0 || metaData.height == 0 ) {
		//			return null; 
		//		}

		WritableImage image = new WritableImage(metaData.width, metaData.height); 

		mediaView.snapshot(new SnapshotParameters(), image); 

		PamImage pamImage = new PamImage(image,  getCurrentTime()); 
		pamImage.setImageFile(this.currentFile); 

		return pamImage; 
	}

	/**
	 * Grab a still from the video and set as the image in VRControl. 
	 */
	private void setImageFreeze() {
		this.vrDisplayFX.getVrControl().setCurrentImage(getImageSnapshot());

	}

	@Override
	public Region getImageEditPane() {
		// TODO Auto-generated method stub
		return this.editPane;
	}

	@Override
	public ArrayList<String> getMetaData() {

		if (metaData!=null) {
			ArrayList<String> metaDataString= new ArrayList<String>(); 

			metaDataString.add(metaDataDetails("Bit-rate", metaData.bitRate));
			metaDataString.add(metaDataDetails("Frame Rate", metaData.frameRate));
			metaDataString.add(metaDataDetails("Height", metaData.height));
			metaDataString.add(metaDataDetails("Width", metaData.width));

			metaDataString.add(metaDataDetails("Duration", metaData.duration));
			metaDataString.add(metaDataDetails("Format", metaData.format));
			metaDataString.add(metaDataDetails("Bit-rate", metaData.bitRate));
			metaDataString.add(metaDataDetails("Channels",  metaData.audioChannels));
			metaDataString.add(metaDataDetails("Sampling Rate", metaData.audioSamplingRate));

			return metaDataString;
		}
		else return null; 
	}

	@Override
	public int getImageHeight() {
		if (metaData==null) return -1; 
		return metaData.height;
	}

	@Override
	public int getImageWidth() {
		if (metaData==null) return -1; 
		return metaData.width;
	}

}

