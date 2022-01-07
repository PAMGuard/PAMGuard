package soundPlayback;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import Acquisition.AcquisitionProcess;
import Acquisition.DaqSystem;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.MenuItemEnabler;
import PamView.TopToolBar;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoading;
import dataMap.filemaps.OfflineFileServer;
import soundPlayback.fx.PlayBackGUI;
import soundPlayback.swing.PlaybackDialog;
import soundPlayback.swing.PlaybackSidePanel;

/**
 * Main Pam Controller for sound playback modules.
 * @author Doug Gillespie
 *
 */
public class PlaybackControl extends PamControlledUnit implements PamSettings {

	public static final int DEFAULT_OUTPUT_RATE = 48000;

	protected PlaybackParameters playbackParameters = new PlaybackParameters();
	
	private PlaybackProcess playbackProcess;
	
	protected FilePlayback filePlayback;
	
	protected PlaybackSystem playbackSystem;

	private LoadDataWorker loadWorker;

	private boolean realTimePlayback;

	private PamRawDataBlock sourceDataBlock;
	
	private static PlaybackControl viewerPlayback;
		
	private static MenuItemEnabler playButtonEnabler = new MenuItemEnabler();
	private static MenuItemEnabler stopButtonEnabler = new MenuItemEnabler();
	
	private PlaybackSidePanel playbackSidePanel;

	/**
	 * The GUI components of the play back pane. 
	 */
	private PlayBackGUI playBackGUI;

	
//	private JButton viewerStopButton;
	
	public PlaybackControl(String unitName) {
		
		super("Sound Playback", unitName);
		
		filePlayback = new FilePlayback(this);
		
		addPamProcess(playbackProcess = new PlaybackProcess(this));

		playbackSidePanel = new PlaybackSidePanel(this);

//		newSettings();

		PamSettingManager.getInstance().registerSettings(this);
		
		if (viewerPlayback == null) {
			viewerPlayback = this;
		}	
		setSidePanel(playbackSidePanel);
				
		enablePlayControls(false);
	}
	
	/**
	 * @return the filePlayback
	 */
	public FilePlayback getFilePlayback() {
		return filePlayback;
	}

	/**
	 * Static easy access to the playback module in PAMGUARD viewer 
	 * mode. Other modules will be sending commands to this module
	 * instructing it to play sections of data out through 
	 * the speakers. <p>
	 * This may involve allowing the playback module to use it's
	 * own data source, but it may also be instructed to take 
	 * data from elsewhere - e.g. reconstructed clicks from the 
	 * click detector. 
	 * @return
	 */
	public static PlaybackControl getViewerPlayback() {
		return viewerPlayback;
	}

	public static void registerPlayButton(AbstractButton button) {
		playButtonEnabler.addMenuItem(button);
	}
	
	public static void registerStopButton(AbstractButton button) {
		stopButtonEnabler.addMenuItem(button);
	}

	public Serializable getSettingsReference() {
		return playbackParameters;
	}

	public long getSettingsVersion() {
		return PlaybackParameters.serialVersionUID;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		playbackParameters = ((PlaybackParameters) pamControlledUnitSettings.getSettings()).clone();
		newSettings();
		return true;
	}
	
	public void newSettings() {
		// work out the data source and process.
		sourceDataBlock = PamController.getInstance().getRawDataBlock(playbackParameters.dataSource);
		playbackSystem = findPlaybackSystem(sourceDataBlock);
		playbackProcess.noteNewSettings();
		playbackSidePanel.newSettings();
//		if (this.getSidePanel() != null){
//			this.getSidePanel().getPanel().setVisible(!isRealTimePlayback());
//		}
		
		
			
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem playbackMenu;
		playbackMenu = new JMenuItem(getUnitName() + " ...");
		playbackMenu.addActionListener(new PlaybackSettings(this, parentFrame));
		return playbackMenu;
	}
	
	public PlaybackSystem findPlaybackSystem(PamDataBlock sourceDataBlock) {
		
		if (sourceDataBlock == null) return null;
		
		// from the data source, work out what type of playbacksystem is being used, if any.
		PamProcess sourceProcess = sourceDataBlock.getSourceProcess();
		if (sourceProcess == null) {
			return null;
		}
		// this should be an Acquisition process, but catch exception if it isn't. 
		if (sourceProcess.getClass() != AcquisitionProcess.class) return null;
		AcquisitionProcess daqProcess = null;
		try {
			daqProcess = (AcquisitionProcess) sourceProcess;
		}
		catch (Exception ex) {
			return null;
		}
		if (daqProcess == null) return null;
		DaqSystem daqSystem = daqProcess.getAcquisitionControl().findDaqSystem(null);
		if (daqSystem == null) return null;
		if (daqSystem.isRealTime() == false || isViewer || isMixed){
			realTimePlayback = false;
			return filePlayback;
		}
		else {
			realTimePlayback = true;
			return daqSystem.getPlaybackSystem(this, daqSystem);
		}
	}
	
	public int getMaxPlaybackChannels(PlaybackSystem playbackSystem) {
		if (playbackSystem == null) return 0;
		return playbackSystem.getMaxChannels();
	}
	
	public double getSourceSampleRate() {
		if (sourceDataBlock == null) {
			return DEFAULT_OUTPUT_RATE;
		}
		else {
			return sourceDataBlock.getSampleRate();
		}
	}
	
	public float choseSampleRate() {
		if (playbackSystem == null) return DEFAULT_OUTPUT_RATE;
		else if (playbackSystem == filePlayback) {
//			if (playbackParameters.playbackRate == 0 || playbackParameters.defaultSampleRate) {
//				return playbackProcess.getSampleRate();
//			}
//			else {
//				return playbackParameters.playbackRate;
//			}
			return (float) playbackParameters.getPlaybackSpeed();
		}
		return playbackProcess.getSampleRate();
	}
	
	class PlaybackSettings implements ActionListener {

		Frame parentFrame;
		
		PlaybackControl playbackControl;
		
		public PlaybackSettings(PlaybackControl playbackControl, Frame parentFrame) {
			this.playbackControl = playbackControl;
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {

			PlaybackParameters newParams = PlaybackDialog.showDialog(parentFrame, playbackParameters, playbackControl);
			if (newParams != null) {
				playbackParameters = newParams.clone();
				newSettings();
			}
			
		}
		
	}
	
	class StopPlayback implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			stopViewerPlayback();
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch(changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			newSettings();
		}
	}

	/**
	 * 
	 * @return playback parameters. 
	 */
	public PlaybackParameters getPlaybackParameters() {
		return playbackParameters;
	}
	
	/**
	 * Set the playback parameters. 
	 * @param playbackParameters - the playback parameters. 
	 */
	public void setPlayBackParamters(PlaybackParameters playbackParameters) {
		this.playbackParameters =  playbackParameters;
	}

	/**
	 * Play back raw audio data for a specific channel. 
	 * @param channelMap
	 * @param startMillis
	 * @param endMillis
	 * @param playbackProgressMonitor
	 * @return true if playback seems to have started OK
	 */
	public boolean playViewerData(int channelMap, long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor) {

		PamDataBlock dataBlock = playbackProcess.getParentDataBlock();
		if (dataBlock == null) {
			return false;
		}
		enablePlayControls(true);
		int currentChannels = playbackParameters.channelBitmap;
		playbackParameters.channelBitmap = channelMap;
		playbackProcess.noteNewSettings();
		this.playbackProgressMonitor = playbackProgressMonitor;
		this.playbackStartMillis = startMillis;
		this.playbackEndMillis = endMillis;
		playbackProcess.prepareProcess();
		playbackProcess.pamStart();
		dataBlock.orderOfflineData(playbackProcess, new PlayLoadObserver(),
				startMillis, endMillis, 0,
				OfflineDataLoading.OFFLINE_DATA_CANCEL, true);
		if (playbackProgressMonitor != null) {
			playbackProgressMonitor.setStatus(PlaybackProgressMonitor.PLAY_START);
		}

//		playbackParameters.channelBitmap = currentChannels;
		return true;
	}
	
	/**
	 * Enable any controls associated with playback
	 * @param playing true if playback active
	 */
	private static void enablePlayControls(boolean playing) {
		playButtonEnabler.enableItems(!playing);
		stopButtonEnabler.enableItems(playing);
		TopToolBar.enableStopButton(playing);
		TopToolBar.enableStartButton(!playing);
	}

	/**
	 * The simplest of the viewer instructions simply 
	 * instructs the playback module to play data from 
	 * it's own data source between the given times. 
	 * @param startMillis start time
	 * @param endMillis  end time
	 * @param playbackProgressMonitor progress monitor
	 * @return true if playback seems to have started OK
	 */
	public boolean playViewerData(long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor) {
		
		int defaultChannels = playbackParameters.channelBitmap;
		return playViewerData(defaultChannels, startMillis, endMillis, playbackProgressMonitor);
		
	}
	
	/**
	 * This one is for playing back non-raw data through the sound card. 
	 * e.g. the click detector will play back clicks spaced with zeros between
	 * them. 
	 * <p>A new thread will be launched which will call back into playDataServer. 
	 * playDataServer will have to prepare rawDataUnits which get passed on to the 
	 * PlaybackProcess. These rawDataUnits will have to follow the basic form of 
	 * any normal rawDataUnit, but can probably vary a bit in length if necessary. 
	 * @param channelMap
	 * @param startMillis
	 * @param endMillis
	 * @param playbackProgressMonitor
	 * @param playDataServer
	 * @return true if playback started successfully. 
	 */
	public boolean playViewerData(int channelMap, long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor, PlaybackDataServer playDataServer) {

		enablePlayControls(true);
		int currentChannels = playbackParameters.channelBitmap;
		playbackParameters.channelBitmap = channelMap;
		playbackProcess.noteNewSettings();
		this.playbackProgressMonitor = playbackProgressMonitor;
		this.playbackStartMillis = startMillis;
		this.playbackEndMillis = endMillis;
		playbackProcess.prepareProcess();
		playbackProcess.pamStart();
		loadWorker = new LoadDataWorker(channelMap, startMillis, endMillis, playbackProgressMonitor, playDataServer);
		loadWorker.execute();
		if (playbackProgressMonitor != null) {
			playbackProgressMonitor.setStatus(PlaybackProgressMonitor.PLAY_START);
		}

		return true;
	}
	
	class LoadDataWorker extends SwingWorker<Integer, ProgressData> implements PlaybackProgressMonitor {
		private int channelMap;
		private long startMillis;
		private long endMillis;
		private PlaybackProgressMonitor playbackProgressMonitor;
		private PlaybackDataServer playDataServer;
		private LoadDataWorker(int channelMap, long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor, PlaybackDataServer playDataServer) {
			this.channelMap = channelMap;
			this.startMillis = startMillis;
			this.endMillis = endMillis;
			this.playbackProgressMonitor = playbackProgressMonitor;
			this.playDataServer = playDataServer;
		}
		
		@Override
		protected Integer doInBackground() {
			playDataServer.orderPlaybackData(playbackProcess, this, choseSampleRate(), 
					startMillis, endMillis);
			return null;
		}

		@Override
		protected void done() {
			super.done();
		}

		@Override
		protected void process(List<ProgressData> chunks) {
			int n = chunks.size();
			ProgressData pd;
			for (int i = 0; i < n; i++) {
				pd = chunks.get(i);
				switch (pd.type) {
				case ProgressData.TYPE_PROGRESS:
					this.playbackProgressMonitor.setProgress(pd.channels, pd.millis, pd.percent);
					break;
				case ProgressData.TYPE_STATUS:
					this.playbackProgressMonitor.setStatus(pd.status);
					if (pd.status == PlaybackProgressMonitor.PLAY_END) {
						enablePlayControls(false);
					}
					break;
				}
			}
		}

		@Override
		public void setProgress(int channels, long timeMillis, double percent) {
			ProgressData pd = new ProgressData(channels, timeMillis, percent);
			publish(pd);
		}

		@Override
		public void setStatus(int status) {
			ProgressData pd = new ProgressData(status);
			publish(pd);
		}

		public void cancelLoad() {
			this.cancel(false);
			this.playDataServer.cancelPlaybackData();
		}
		
	}
	
	class ProgressData {
		static final int TYPE_STATUS = 0;
		static final int TYPE_PROGRESS = 1;
		public ProgressData(int status) {
			this.type = TYPE_STATUS;
			this.status = status;
		}
		public ProgressData(int channels, long timeMillis, double percent) {
			this.type = TYPE_PROGRESS;
			this.channels = channels;
			this.millis = timeMillis;
			this.percent = percent;
		}
		int type;
		int status;
		int channels;
		long millis;
		double percent;
	}
	
	/**
	 * Stop viewer playback. 
	 */
	public void stopViewerPlayback() {
		PamDataBlock dataBlock = playbackProcess.getParentDataBlock();
		if (dataBlock == null) {
			return;
		}		
		dataBlock.cancelDataOrder();
		
		if (loadWorker != null) {
			loadWorker.cancelLoad();
		}
	}
	
	/**
	 * @return true if there is a valid data source and the options to play 
	 * data are checked. 
	 */
	public boolean hasPlayDataSource() {
		PamDataBlock dataBlock = playbackProcess.getParentDataBlock();
		if (dataBlock == null) {
			return false;
		}
		PamProcess sourceProcess = dataBlock.getSourceProcess();
		if (sourceProcess.getClass() != AcquisitionProcess.class) {
			return false;
		}
		AcquisitionProcess daqProcess = (AcquisitionProcess) sourceProcess;
		OfflineFileServer daqOffline = daqProcess.getAcquisitionControl().getOfflineFileServer();
		if (daqOffline == null) {
			return false;
		}
		return daqOffline.getOfflineFileParameters().enable;
	}
	
	protected long playbackStartMillis, playbackEndMillis;
	protected PlaybackProgressMonitor playbackProgressMonitor;

	
	
	protected void setPlaybackProgress(long timeMillis) {
		if (playbackProgressMonitor == null) {
			return;
		}
		double percent = (double) (timeMillis-playbackStartMillis) * 100 / (playbackEndMillis - playbackStartMillis);
		playbackProgressMonitor.setProgress(playbackParameters.channelBitmap, timeMillis, percent);
	}
	
	class PlayLoadObserver implements LoadObserver {

		@Override
		public void setLoadStatus(int loadState) {
//			switch(loadState) {
//			case PamDataBlock.REQUEST_DATA_LOADED:
//				
//			}
			playbackProcess.pamStop();
			if (playbackProgressMonitor != null) {
				playbackProgressMonitor.setStatus(PlaybackProgressMonitor.PLAY_END);
			}
			enablePlayControls(false);
		}
		
	}

	/**
	 * Returns true if the playback is real time. If source input device is real time, then 
	 * the master PAM clock will be the input device (e.g. sound card, NI card, etc). If
	 * this is false, then we're talking file playback or viewer mode and the master clock will be the
	 * output device itself. 
	 * @return the realTimePlayback
	 */
	public boolean isRealTimePlayback() {
		return realTimePlayback;
	}

	/**
	 * @return the playbackProcess
	 */
	public PlaybackProcess getPlaybackProcess() {
		return playbackProcess;
	}

	public PlayDeviceState getDeviceState() {
		if (playbackSystem == null) {
			return null;
		}
		else {
			return playbackSystem.getDeviceState();
		}
	}

	@Override
	public String getUnitName() {
		// TODO Auto-generated method stub
		return super.getUnitName();
	}

	@Override
	public String getUnitType() {
		// TODO Auto-generated method stub
		return super.getUnitType();
	}

	/**
	 * @return the playbackSystem
	 */
	public PlaybackSystem getPlaybackSystem() {
		return playbackSystem;
	}
	
	
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (playBackGUI ==null) {
				playBackGUI= new PlayBackGUI(this);
			}
			return playBackGUI;
		}
		//TODO swing
		return null;
	}

}
