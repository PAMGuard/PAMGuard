package difar;

import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.Predicate;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import Array.ArrayManager;
import fftManager.FFTDataBlock;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamUtils.PamUtils;
import PamView.PamSymbol;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramMarkObserver;
import Spectrogram.SpectrogramMarkObservers;
import clipgenerator.clipDisplay.ClipDisplayParameters;
import dataPlots.data.TDDataProviderRegister;
import dataPlotsFX.layout.TDGraphFX;
import difar.DifarParameters.SpeciesParams;
import difar.dialogs.DifarDisplayParamsDialog;
import difar.dialogs.DifarParamsDialog;
import difar.display.DIFARDisplayUnit;
import difar.display.DIFARGram;
import difar.display.DIFARQueuePanel;
import difar.display.DIFARUnitControlPanel;
import difar.display.DemuxProgressDisplay;
import difar.display.DifarActionsVesselPanel;
import difar.display.DifarDisplayContainer;
import difar.display.DifarDisplayContainer2;
import difar.display.DifarDisplayProvider;
import difar.display.DifarDisplayProvider2;
import difar.display.DifarSidePanel;
import difar.display.DifarMatchContainer;
import difar.display.DifarMatchProvider;
import difar.display.SonobuoyManagerContainer;
import difar.display.SonobuoyManagerProvider;
import difar.crossings.CrossingLocaliser;
import difar.crossings.DifarCrossing;
import difar.offline.RematchTask;
import difar.offline.UpgradeTask;
import difar.offline.ViewerClipStore;
import difar.plots.DifarBearingPlotProvider;
import difar.plots.DifarIntensityPlotProvider;
import difar.trackedGroups.TrackedGroupProcess;
import difar.offline.ViewerEdits;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import offlineProcessing.TaskGroupParams;
import userDisplay.UserDisplayControl;
import warnings.PamWarning;

public class DifarControl extends PamControlledUnit implements PamSettings {

	//Display components
	DifarProcess difarProcess;
	private DifarActionsVesselPanel internalActionsPanel;
	
	private DifarDisplayProvider displayProvider;
	
	private DifarDisplayProvider2 displayProvider2;

	private DifarDisplayContainer difarDisplayContainer;
	
	protected DifarParameters difarParameters = new DifarParameters();
	
	private SonobuoyManagerProvider sonobuoyManagerProvider;
	
	private SonobuoyManagerContainer sonobuoyManagerContainer;

	private DifarMatchProvider matchProvider;

	private DifarMatchContainer matchContainer;
	 
	private SpectrogramObserver spectrogramObserver = new SpectrogramObserver();
	
	private ArrayList<DIFARDisplayUnit> displayUnits = new ArrayList<>();
	
	private DIFARGram difarGram; // specrogram and difargram display
	
	private DIFARUnitControlPanel difarUnitControlPanel;
	
	private DIFARQueuePanel difarQueue; // display of queues clips to process. 

	private DIFARQueuePanel savedClips; // display of clips already saved
	
	private DifarSidePanel difarSidePanel; // Side panel for easy access to frequenctly used DIFAR controls
	
	private DemuxProgressDisplay demuxProgressDisplay;
	
	private DifarDataUnit currentDemuxedUnit = null;
	private DifarDisplayContainer2 difarDisplayContainer2;
	private TrackedGroupProcess trackedGroupProcess;

	/** How viewer edits reach displays, files and the database; null in normal mode. */
	private ViewerEdits viewerEdits;
	
	private KeyboardFocusManager keyManager;
	
	public static final boolean SPLITDISPLAYS = false;
	
	private OfflineTaskGroup offlineTaskGroup;
	
	public SonobuoyManager sonobuoyManager;

	/** Which buoy record was in force on each channel at any time. */
	private SonobuoyHistorySource sonobuoyHistorySource;
	
	private static PamWarning warningMessage = new PamWarning("Difar", "", 2);

	public DifarControl(String unitName) {
	
		super("DIFAR Processing", unitName);

		PamSettingManager.getInstance().registerSettings(this);		
		
		addPamProcess(difarProcess = new DifarProcess(this));
		addPamProcess(setTrackedGroupProcess(new TrackedGroupProcess(this, difarProcess.getProcessedDifarData(), "Difar Tracked Groups")));
		addPamProcess(sonobuoyManager = new SonobuoyManager(this));
		sonobuoyHistorySource = new SonobuoyHistorySource(
				sonobuoyManager.sonobuoyEndTimeAnnotation.getAnnotationName());
		// make the displays here
		displayUnits.add(difarUnitControlPanel = new DIFARUnitControlPanel(this));
		displayUnits.add(difarGram = new DIFARGram(this));
		displayUnits.add(internalActionsPanel=new DifarActionsVesselPanel(this));
		displayUnits.add(difarQueue = new DIFARQueuePanel(this, "Queued Data",
				difarProcess.getQueuedDifarData(), false));
		displayUnits.add(savedClips = new DIFARQueuePanel(this, "Saved Data",
				difarProcess.getProcessedDifarData(), true));
		displayUnits.add(demuxProgressDisplay = new DemuxProgressDisplay(this));
		

		if (SPLITDISPLAYS) {
			displayProvider2 = new DifarDisplayProvider2(this);
			UserDisplayControl.addUserDisplayProvider(displayProvider2);
		}
		displayProvider = new DifarDisplayProvider(this);
		UserDisplayControl.addUserDisplayProvider(displayProvider);
		displayUnits.add(getMatchContainer().getMatchPanel());
		matchProvider = new DifarMatchProvider(this);
		UserDisplayControl.addUserDisplayProvider(matchProvider);
		sonobuoyManagerProvider = new SonobuoyManagerProvider(this);
		UserDisplayControl.addUserDisplayProvider(sonobuoyManagerProvider);
		
		difarSidePanel = new DifarSidePanel(this);
		setSidePanel(difarSidePanel);

		keyManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
		keyManager.addKeyEventDispatcher(new DifarKeyEventDispatcher());
		
		TDDataProviderRegister.getInstance().registerDataInfo(new DifarBearingPlotProvider(this));
		TDDataProviderRegister.getInstance().registerDataInfo(new DifarIntensityPlotProvider(this));
		SpectrogramMarkObservers.addSpectrogramMarkObserver(spectrogramObserver);
	}

	@Override
	public boolean removeUnit() {
		UserDisplayControl.removeDisplayProvider(displayProvider);
		if (displayProvider2 != null) {
			UserDisplayControl.removeDisplayProvider(displayProvider2);
		}
		SpectrogramMarkObservers.removeSpectrogramMarkObserver(spectrogramObserver);
		return super.removeUnit();
	}

	@Override
	public Serializable getSettingsReference() {
		return difarParameters;
	}

	@Override
	public long getSettingsVersion() {
		return DifarParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		difarParameters = ((DifarParameters) pamControlledUnitSettings.getSettings()).clone();
		return (difarParameters != null);
	}

	/**
	 * Called from just about anywhere in the DIFAR system, this 
	 * will process various difar messages and then pass the 
	 * notification on to all the difar displays. 
	 * @param message DIFAR message
	 */
	public void sendDifarMessage(DIFARMessage message) {
//		System.out.println(String.format("Process difar message %d data unit %s", message.message, message.difarDataUnit));
		// use the message here. 
		switch (message.message) {
		case DIFARMessage.NewDifarUnit:
			processNextIfAnyAndCanAndShould();
			break;
		
		case DIFARMessage.DeleteFromQueue:
			difarProcess.getQueuedDifarData().remove(message.difarDataUnit);
			if (isViewer) {
				getViewerEdits().removed(difarProcess.getQueuedDifarData(), message.difarDataUnit);
			}
			break;
		case DIFARMessage.MatchChanged:
			if (isViewer && message.difarDataUnit != null) {
				// redraws the map with the new match
				getViewerEdits().changed(difarProcess.getQueuedDifarData(), message.difarDataUnit);
			}
			break;
		case DIFARMessage.ReturnToQueue:
			if (!isViewer) {
				returnToQueue();
				processNextIfAnyAndCanAndShould();
			}
			break;
		case DIFARMessage.ProcessFromQueue:
			difarProcess.queueDemuxProcess(message.difarDataUnit);
			break;
		case DIFARMessage.EditVesselBuoySettings:
			DifarParamsDialog.showDialog(getGuiFrame(), this, difarParameters);
			break;
		case DIFARMessage.DemuxComplete:
			currentDemuxedUnit = message.difarDataUnit;
			break;
		case DIFARMessage.DeleteDatagramUnit:
			// a deleted clip must not go on counting down to an auto save
			difarProcess.cancelAutoSaveTimer();
			// a new clip is deleted as in normal mode; a saved one being looked
			// at again is just put away
			if (!isViewer || isQueued(message.difarDataUnit)) {
				currentDemuxedUnit = null;
				difarProcess.getQueuedDifarData().remove(message.difarDataUnit);
				difarProcess.getCrossingRecorder().forget(message.difarDataUnit);
				if (isViewer) {
					getViewerEdits().removed(difarProcess.getQueuedDifarData(), message.difarDataUnit);
				}
				getDemuxProgressDisplay().newMessage(new DemuxWorkerMessage(message.difarDataUnit, 
						DemuxWorkerMessage.STATUS_DELETED, 0L, 0));
				processNextIfAnyAndCanAndShould();
			}else{
				currentDemuxedUnit = null;
			}
			break;
		case DIFARMessage.SaveDatagramUnit:
			if ((!isViewer || isQueued(message.difarDataUnit)) && canSaveInViewer()) {
				prepareViewerSave(message.difarDataUnit);
				difarProcess.finalProcessing(message.difarDataUnit);
				difarProcess.getCrossingRecorder().record(message.difarDataUnit);
				message.difarDataUnit.clearTempCrossing();
				completeViewerSave(message.difarDataUnit);
				viewerSaved(message.difarDataUnit);
				currentDemuxedUnit = null;
				getDemuxProgressDisplay().newMessage(new DemuxWorkerMessage(message.difarDataUnit, 
						DemuxWorkerMessage.STATUS_SAVED, 0L, 100));
				difarProcess.cancelAutoSaveTimer();
				processNextIfAnyAndCanAndShould();
			}
			break;
			
		case DIFARMessage.SaveDatagramUnitWithoutRange:
		
			if ((!isViewer || isQueued(message.difarDataUnit)) && canSaveInViewer()) {
				prepareViewerSave(message.difarDataUnit);
				difarProcess.cancelAutoSaveTimer();
				//remove Range/Localisation Information
				message.difarDataUnit.clearTempCrossing();
				difarProcess.finalProcessing(message.difarDataUnit);
				difarProcess.getCrossingRecorder().forget(message.difarDataUnit);
				completeViewerSave(message.difarDataUnit);
				viewerSaved(message.difarDataUnit);
				currentDemuxedUnit = null;
				getDemuxProgressDisplay().newMessage(new DemuxWorkerMessage(message.difarDataUnit, 
						DemuxWorkerMessage.STATUS_SAVED, 0L, 100));
				processNextIfAnyAndCanAndShould();
			}
			break;
		case DIFARMessage.ClickDatagramUnit:
			if (message.difarDataUnit != null) {
				getDemuxProgressDisplay().newMessage(new DemuxWorkerMessage(message.difarDataUnit, 
						DemuxWorkerMessage.STATUS_AUTOSAVEINTERRUPTED, 0L, 0));
			}
			
			break;
		case DIFARMessage.Deploy:
			ArrayManager.getArrayManager().showArrayDialog(getGuiFrame());
			break;
		}
		
		// loop over display units, 
		for (DIFARDisplayUnit difarDisplay:displayUnits) {
			difarDisplay.difarNotification(message);
		}
		
	}
	

	/**
	 * now applies to vessel clips and all clips that have a lookupItem
	 */
	void processNextIfAnyAndCanAndShould(){
		DifarDataBlock db = difarProcess.getQueuedDifarData();
		DifarDataUnit unit=null;
		if (!difarParameters.autoProcess) return;
		if (!canDemux()) return;
		if (db.getUnitsCount()<1) return;
		
		synchronized (db.getSynchLock()) {
			ListIterator<DifarDataUnit> iterator = db.getListIterator(0);
			getUnit:
			while (iterator.hasNext()){
				unit=iterator.next();
				if (unit.canAutoProcess()){
					break getUnit;
				}
			}
		}
		if (unit.canAutoProcess()){
			sendDifarMessage(new DIFARMessage(DIFARMessage.ProcessFromQueue, unit));
		}
		// reached last one finding none that can be auto-processed 
	}
		
	/**
	 * return a demuxed data unit from the difargram area to the queue. 
	 */
	private void returnToQueue() {
		DifarDataUnit unit = currentDemuxedUnit;
		if (unit == null) {
			return;
		}
		sendDifarMessage(new DIFARMessage(DIFARMessage.DeleteDatagramUnit, unit));
		unit.clearUpdateCount();
		difarProcess.getQueuedDifarData().addPamData(unit);
		difarProcess.getQueuedDifarData().sortData();		
	}

	public DifarDisplayContainer getDifarDisplayContainer() {
		if (difarDisplayContainer == null) {
			difarDisplayContainer = new DifarDisplayContainer(this);
		}
		return difarDisplayContainer;
	}

	public DifarDisplayContainer2 getDifarDisplayContainer2() {
		if (difarDisplayContainer2 == null) {
			difarDisplayContainer2 = new DifarDisplayContainer2(this);
		}
		return difarDisplayContainer2;
	}
	
	/**
	 * @return the container for the match candidate table, making it if needed.
	 */
	public DifarMatchContainer getMatchContainer() {
		if (matchContainer == null) {
			matchContainer = new DifarMatchContainer(this);
		}
		return matchContainer;
	}

	public SonobuoyManagerContainer getSonobuoyManagerContainer() {
		if (sonobuoyManagerContainer == null) {
			sonobuoyManagerContainer = new SonobuoyManagerContainer(this);
		}
		return sonobuoyManagerContainer;
	}
	
	class SpectrogramObserver implements SpectrogramMarkObserver {

		@Override
		public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEvent, 
				int downUp, int channel, long startMilliseconds, long duration,
				double f1, double f2, TDGraphFX tdDisplay) {

			// REMOVE THIS CHECK - Difar already knows the raw data source, set in the parameters.  So it doesn't need to worry about whether the FFT source is actually beamformer data
//    		// do a quick check here of the source.  If the fft has sequence numbers, the channels are ambiguous and Rocca can't use it.  warn the user and exit
//    		FFTDataBlock source = display.getSourceFFTDataBlock();
//    		if (source==null) {return false;}
//    		if (source.getSequenceMapObject()!=null) {
//    			String err = "Error: this Spectrogram uses Beamformer data as it's source, and Beamformer output does not contain "
//    			+ "the link back to a single channel of raw audio data that Difar requires.  You will not be able to select detections "
//    			+ "until the source is changed";
//    			warningMessage.setWarningMessage(err);
//    			WarningSystem.getWarningSystem().addWarning(warningMessage);
//    			return false;
//    		} else {
//    			WarningSystem.getWarningSystem().removeWarning(warningMessage);
//    		}

			double f[] = {f1, f2};
			startMilliseconds += halfWindowMillis(display);
//			System.out.println(String.format("Spec mark chan %d %s duration ms %dms, %s", channel, 
//					PamCalendar.formatDateTime(startMilliseconds), duration, FrequencyFormat.formatFrequencyRange(f, true)));
			// Get the channel map to generate DIFAR clips for all channels
			int channelBitmap = getDifarProcess().getSourceDataBlock().getChannelMap();
			int numChans = PamUtils.getNumChannels(channelBitmap);
			if (downUp == SpectrogramMarkObserver.MOUSE_UP) {
			
				SpeciesParams sP = difarParameters.findSpeciesParams(DifarParameters.Default);
				float sr = sP.sampleRate;

				if (difarParameters.multiChannelClips){ 
					for (int i = 0; i<numChans; i++)
						difarProcess.difarTrigger(1<<i, startMilliseconds, duration, f, null, sr, null, getMarkObserverName());
				}else{
					difarProcess.difarTrigger(1<<channel, startMilliseconds, duration, f, null, sr, null, getMarkObserverName());
				}
			}
			return false;
		}

		@Override
		public String getMarkObserverName() {
			return getUnitName();
		}

		/**
		 * Half the spectrogram's FFT window, in milliseconds. Core's spectrogram
		 * draws each FFT column at its window's start, so the picture, and a box
		 * drawn round a call on it, sit half a window early. Shifting the mark
		 * later by this much makes the clip cover the call the box was drawn
		 * round. Remove this if core comes to draw columns at the window centre;
		 * see "Marked clips end early" in outstanding.md.
		 * @param display the spectrogram marked, or null for a JavaFX display
		 * @return the shift, or 0 if it cannot be found
		 */
		private long halfWindowMillis(SpectrogramDisplay display) {
			if (display == null) {
				return 0;
			}
			FFTDataBlock fftBlock = display.getSourceFFTDataBlock();
			if (fftBlock == null || fftBlock.getSampleRate() <= 0) {
				return 0;
			}
			return Math.round(fftBlock.getFftLength() / 2. / fftBlock.getSampleRate() * 1000.);
		}

		@Override
		public boolean canMark() {
			return (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW);
		}

		@Override
		public String getMarkName() {
			return getCurrentlySelectedSpecies().getText();
		}
		
		
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDisplayMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " display options...");
		menuItem.addActionListener(new DisplayMenu(parentFrame));
		return menuItem;
	}
	
	
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		if (isViewer){
			JMenu menu = new JMenu(getUnitName());
			menu.add(menuItem);
			JMenuItem offlineDataItem = new JMenuItem("DIFAR offline tasks...");
			offlineDataItem.addActionListener(new OfflineTaskAction());
			menu.add(offlineDataItem);
			return menu;
		} else
			return menuItem;
	}
	
	private class OfflineTaskAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			runOfflineTasks();
		}
	}
	
	/**
	 * Carry a buoy change through the data, over the whole time the buoy
	 * record is in force: matches in that period are chosen again, crossings
	 * chosen by the operator keep their clips and are worked out again, and
	 * affected clips' database rows get the buoy's new values.
	 * <p>
	 * The period is the whole time the buoy record is in force, not the loaded
	 * period, so detections outside the viewer's window are covered too. No
	 * binary file is rewritten. Runs without asking, since the user has
	 * already agreed to it.
	 * @param startTime start of the period.
	 * @param endTime end of the period.
	 * @param affected the clips whose buoy changed.
	 */
	public void runCrossingTasks(long startTime, long endTime, Predicate<DifarDataUnit> affected) {
		OfflineTaskGroup taskGroup = new OfflineTaskGroup(this, getUnitName());
		taskGroup.setPrimaryDataBlock(difarProcess.getProcessedDifarData());
		RematchTask task = new RematchTask(this, affected);
		taskGroup.addTask(task);
		/*
		 * A task group takes each task's on or off state from the offline tasks
		 * dialog's saved selection, which is off by default. This group is run
		 * from code, not the dialog, so switch the task on.
		 */
		task.setDoRun(true);
		TaskGroupParams params = taskGroup.getTaskGroupParams();
		params.dataChoice = TaskGroupParams.PROCESS_SPECIFICPERIOD;
		params.startRedoDataTime = startTime;
		params.endRedoDataTime = endTime;
		/*
		 * Run straight away. The user has already been told what this does and
		 * agreed to it, so the offline tasks dialog would only ask again, in
		 * different words.
		 */
		taskGroup.runTasks();
	}

	private void runOfflineTasks() {
		if (offlineTaskGroup == null) {
			offlineTaskGroup = new OfflineTaskGroup(this, getUnitName());
			offlineTaskGroup.setPrimaryDataBlock(difarProcess.getProcessedDifarData());
			offlineTaskGroup.addTask(new RematchTask(this, null));
			offlineTaskGroup.addTask(new UpgradeTask(this));
//			offlineTaskGroup.addTask(task);
		}
		OLProcessDialog olProcessDialog;
		olProcessDialog = new OLProcessDialog(getGuiFrame(), offlineTaskGroup, "DIFAR offline tasks");
		olProcessDialog.setVisible(true);
	}
	
	class SettingsMenu implements ActionListener {

		private Frame parentFrame;

		public SettingsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			settingMenu(parentFrame);
		}
		
	}

	public boolean settingMenu(Frame parentFrame) {
		if (parentFrame == null) {
			parentFrame = this.getGuiFrame();
		}
		DifarParameters newParams = DifarParamsDialog.showDialog(parentFrame, this, difarParameters);
		if (newParams != null) {
			difarParameters = newParams.clone();
			difarProcess.setupProcess();
			return true;
		}
		else {
			return false;
		}

	}
	class DisplayMenu implements ActionListener {

		private Frame parentFrame;

		public DisplayMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			displayMenu(parentFrame);
		}
		
	}

	public boolean displayMenu(Frame parentFrame) {
		if (parentFrame == null) {
			parentFrame = this.getGuiFrame();
		}
		DifarParameters newParams = DifarDisplayParamsDialog.showDialog(parentFrame, this, difarParameters);
		if (newParams != null) {
			difarParameters = newParams.clone();
			difarProcess.setupProcess();
			return true;
		}
		else {
			return false;
		}

	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			sonobuoyHistorySource.connect();
			difarProcess.setupProcess();
			break;
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			sonobuoyHistorySource.markStale();
			sonobuoyManager.updateSonobuoyTableData();
			break;
		case PamControllerInterface.NEW_SCROLL_TIME:
			sonobuoyManager.scrollTimeChanged();
		}
	}

	/**
	 * @return which buoy record was in force on each channel at any time, up to
	 * date with the streamer records.
	 */
	public SonobuoyHistory getSonobuoyHistory() {
		return sonobuoyHistorySource.getHistory();
	}

	/**
	 * Rebuild the buoy history the next time it is asked for. Offline tasks
	 * call this after loading buoy records for each chunk of data, since a
	 * reload can leave the number of records unchanged.
	 */
	public void buoyRecordsReloaded() {
		sonobuoyHistorySource.markStale();
	}

	public DIFARGram getDifarGram() {
		return difarGram;
	}

	public DifarParameters getDifarParameters() {
		return difarParameters;
	}

	public DifarProcess getDifarProcess() {
		return difarProcess;
	}

	public DifarActionsVesselPanel getInternalActionsPanel() {
		return internalActionsPanel;
	}

	public DIFARQueuePanel getDifarQueue() {
		return difarQueue;
	}

	/**
	 * @return the strip of clips already saved.
	 */
	public DIFARQueuePanel getSavedClips() {
		return savedClips;
	}

	/**
	 * In the viewer, make sure the saved clips' UIDs follow every DIFAR UID
	 * already in use, before this clip joins the saved data and is given one.
	 * The clip's queue UID is cleared, so the saved data give it a new one;
	 * queue UIDs start again every viewer session, so they are not unique.
	 * @param unit the clip about to be saved.
	 */
	private void prepareViewerSave(DifarDataUnit unit) {
		if (!isViewer) {
			return;
		}
		ViewerClipStore store = difarProcess.getProcessedDifarData().getViewerClipStore();
		if (store != null) {
			store.prepare();
		}
		unit.setUID(0);
	}

	/**
	 * In the viewer, record a clip just saved. It is written to its binary
	 * file and the database when the viewer next saves its data.
	 * @param unit the clip just saved, now with its UID.
	 */
	private void completeViewerSave(DifarDataUnit unit) {
		if (!isViewer) {
			return;
		}
		ViewerClipStore store = difarProcess.getProcessedDifarData().getViewerClipStore();
		if (store != null) {
			store.clipSaved(unit);
		}
	}

	/**
	 * @param unit a DIFAR clip.
	 * @return true if the clip is waiting on the queue, so has not been saved.
	 */
	public boolean isQueued(DifarDataUnit unit) {
		return unit != null && difarProcess.getQueuedDifarData().getDataCopy().contains(unit);
	}

	/**
	 * @return the demuxProgressDisplay
	 */
	public DemuxProgressDisplay getDemuxProgressDisplay() {
		return demuxProgressDisplay;
	}



	public ClipDisplayParameters getClipDisplayParams(DifarDataUnit difarDataUnit) {
		return difarQueue.getClipDisplayPanel().getClipDisplayParameters();
	}

	/**
	 * Can the system handle demuxing the next data unit ? 
	 * Currently used to enable menus on the clip display
	 * @return true if it's OK to demux the next sound. 
	 */
	public boolean canDemux() {
		if (difarProcess.isProcessing()) {
			return false;
		}
		// a saved clip being looked at again can be replaced; a new clip
		// being worked cannot, until it is saved or deleted
		return currentDemuxedUnit == null || !isQueued(currentDemuxedUnit);
	}
	
	/**
	 * @return the difarUnitControlPanel
	 */
	public DIFARUnitControlPanel getDifarUnitControlPanel() {
		return difarUnitControlPanel;
	}

	/**
	 * @return the currentDemuxedUnit
	 */
	public DifarDataUnit getCurrentDemuxedUnit() {
		return currentDemuxedUnit;
	}

	public String getCurrentlySelectedGroup(){
		return difarGram.getDifarGroupPanel().getCurrentlySelectedGroup();
	}
	
	public void setCurrentlySelectedGroup(String groupName){
		difarGram.getDifarGroupPanel().setCurrentlySelectedGroup(groupName);
	}

	public boolean isTrackedGroupSelectable(String groupName){
		return difarGram.getDifarGroupPanel().isTrackedGroupSelectable(groupName);
		
	}
	
	/**
	 * Get the appropriate symbol for the selected species (or none). 
	 * @param difarDataUnit DIFAR data unit
	 * @return symbol or null if no species assigned. 
	 */
	public PamSymbol getSpeciesSymbol(DifarDataUnit difarDataUnit) {
		if (difarDataUnit.getLutSpeciesItem() != null) {
			return difarDataUnit.getLutSpeciesItem().getSymbol();
		}
		else {
			return null;
		}
	}

	/**
	 * @return how viewer edits reach displays, files and the database, or
	 * null in normal mode.
	 */
	public ViewerEdits getViewerEdits() {
		if (viewerEdits == null && isViewer) {
			viewerEdits = new ViewerEdits(this);
			// tracked groups consume saved clips, as in normal mode
			viewerEdits.addConsumer(trackedGroupProcess);
		}
		return viewerEdits;
	}

	/**
	 * After a save in the viewer: the clip has left the queue and joined the
	 * saved clips, perhaps in a crossing. Tell the displays and consumers,
	 * and write the files and database now.
	 * @param unit the saved clip.
	 */
	private void viewerSaved(DifarDataUnit unit) {
		if (!isViewer) {
			return;
		}
		ViewerEdits edits = getViewerEdits();
		edits.removed(difarProcess.getQueuedDifarData(), unit);
		edits.added(difarProcess.getProcessedDifarData(), unit);
		edits.crossingChanged(unit.getCrossing());
		edits.commit();
	}

	public TrackedGroupProcess getTrackedGroupProcess() {
		return trackedGroupProcess;
	}

	public TrackedGroupProcess setTrackedGroupProcess(TrackedGroupProcess trackedGroupProcess) {
		this.trackedGroupProcess = trackedGroupProcess;
		return trackedGroupProcess;
	}
	
	/**
	 * Save a set of classifier params. Since this is primarily an
	 * export function, it will always show the file save dialog
	 * @param speciesParams parameters to save
	 * @return true if successful. 
	 */
	public boolean saveClassificationParams(Window frame, LookupList speciesList, ArrayList<SpeciesParams> speciesParams) {
		String classifierFileEnd = ".difarClassification";
		String defFileName = PamCalendar.createFileName(System.currentTimeMillis(), 
				"DifarClassification_", classifierFileEnd);
		File file = new File(defFileName);
		JFileChooser jFileChooser = new PamFileChooser(file);
		jFileChooser.setApproveButtonText("Select");
		FileFilter defaultFileFilter = jFileChooser.getFileFilter();
		jFileChooser.removeChoosableFileFilter(defaultFileFilter);
		jFileChooser.addChoosableFileFilter(new PamFileFilter("DIFAR Classification Settings", classifierFileEnd));
		jFileChooser.addChoosableFileFilter(defaultFileFilter);
		int state = jFileChooser.showSaveDialog(frame);
		if (state != JFileChooser.APPROVE_OPTION) return false;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return false;
		newFile = PamFileFilter.checkFileEnd(newFile, classifierFileEnd, true);
		
		// include the file name in the file we're about to save. 
//		params.fileName = newFile.getAbsolutePath();
		
		ObjectOutputStream ooStream;
		try {
			ooStream = new ObjectOutputStream(new FileOutputStream(newFile));
			ooStream.writeObject(speciesList);
			ooStream.writeObject(speciesParams);
			ooStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(newFile.getAbsolutePath());
		
		return true;
	}
	
	public DifarParameters loadClassificationParams(Frame frame, DifarParameters difarParameters) {
		String classifierFileEnd = ".difarClassification";
		JFileChooser jFileChooser = new PamFileChooser();
		jFileChooser.setApproveButtonText("Select");
		FileFilter defaultFileFilter = jFileChooser.getFileFilter();
		jFileChooser.removeChoosableFileFilter(defaultFileFilter);
		jFileChooser.addChoosableFileFilter(new PamFileFilter("DIFAR Classification Settings", classifierFileEnd));
		jFileChooser.addChoosableFileFilter(defaultFileFilter);
		int state = jFileChooser.showOpenDialog(frame);
		if (state != JFileChooser.APPROVE_OPTION) return null;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return null;
		

		ObjectInputStream oiStream;
		ArrayList<SpeciesParams> speciesParams = null;
		LookupList speciesList = null;
		try {
			oiStream = new ObjectInputStream(new FileInputStream(newFile));
			speciesList = (LookupList) oiStream.readObject();
			speciesParams = (ArrayList<SpeciesParams>) oiStream.readObject();
//			whistleClassificationParameters.fragmentClassifierParams = params;
			oiStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		difarParameters.setSpeciesList(speciesList);
		difarParameters.setSpeciesParams(speciesParams);
		
		return difarParameters;
	}

	/**
	 * Allow the user to select the default classification for clips generated by 
	 *	manually marking the spectrogram
	 * @return - The species (DIFAR classification) selected by the user
	 */
	public LookupItem getCurrentlySelectedSpecies() {
//		Object defaultClassificationSelector = getDefaultClassificationSelector();
//		Object selectedSpecies = ((JList) defaultClassificationSelector).getSelectedValue();
		LookupItem selectedSpecies = difarParameters.selectedClassification;
		if (selectedSpecies==null || selectedSpecies.toString().equals(DifarParameters.Default))
			return null;
		else
			return (LookupItem) selectedSpecies;
	}

	public JList getDefaultClassificationSelector() {
		return difarSidePanel.getSpeciesSelector();
//		return internalActionsPanel.selectedClassification;
	}

	public void updateSidePanel() {
		difarSidePanel.updateDifarDefaultSelector();
	}
	
	private class DifarKeyEventDispatcher implements KeyEventDispatcher {
		
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if(e.getID()==KeyEvent.KEY_PRESSED){
				KeyStroke keyPressed = KeyStroke.getKeyStroke(e.getKeyCode(),e.getModifiers());
				KeyStroke saveKey = KeyStroke.getKeyStroke(difarParameters.saveKey);
				KeyStroke saveWithoutCrossKey = KeyStroke.getKeyStroke(difarParameters.saveWithoutCrossKey);
				KeyStroke deleteKey = KeyStroke.getKeyStroke(difarParameters.deleteKey);
				KeyStroke nextClassKey = KeyStroke.getKeyStroke(difarParameters.nextClassKey);
				KeyStroke prevClassKey = KeyStroke.getKeyStroke(difarParameters.prevClassKey);
				
				if (keyPressed == saveKey && isSaveEnabled()){
					difarUnitControlPanel.saveButton();
					return true;
				}
				if (keyPressed == saveWithoutCrossKey && isSaveWithoutCrossEnabled()){
					difarUnitControlPanel.saveWithoutCrossButton();
					return true;
				}
				if (keyPressed == saveWithoutCrossKey && !isSaveWithoutCrossEnabled() && isSaveEnabled()){
					difarUnitControlPanel.saveButton();
					return true;
				}
				if (keyPressed == deleteKey &&	isDeleteEnabled()){
					difarUnitControlPanel.deleteButton();
					return true;
				}
				if (keyPressed == nextClassKey){
					int ix = difarSidePanel.getSpeciesSelector().getSelectedIndex();
					ix = ++ix % difarSidePanel.getSpeciesSelector().getModel().getSize();
					difarSidePanel.getSpeciesSelector().setSelectedIndex(ix);
					return true;
				}
				if (keyPressed == prevClassKey){
					int ix = difarSidePanel.getSpeciesSelector().getSelectedIndex();
					if (--ix < 0)
						ix = difarSidePanel.getSpeciesSelector().getModel().getSize()-1;
					difarSidePanel.getSpeciesSelector().setSelectedIndex(ix);
					return true;
				}
			}
			
			return false;		
		}
	}

	public boolean isSaveEnabled() {
		DifarDataUnit currentDataUnit = getCurrentDemuxedUnit();
		return (isQueued(getCurrentDemuxedUnit()) &&
				getCurrentDemuxedUnit().getSelectedAngle() != null);
	}
	
	public boolean isSaveWithoutCrossEnabled() {
		return (isQueued(getCurrentDemuxedUnit()) &&
				getCurrentDemuxedUnit().getSelectedAngle() !=null && 
				getCurrentDemuxedUnit().getTempCrossing() != null);
	}	
	
	public boolean isDeleteEnabled() {
		DifarDataUnit unit = getCurrentDemuxedUnit();
		return unit != null && (!isViewer || isQueued(unit) || isSaved(unit));
	}

	/**
	 * @param unit a DIFAR clip.
	 * @return true if the clip is among the saved clips.
	 */
	public boolean isSaved(DifarDataUnit unit) {
		return unit != null && unit.getParentDataBlock() == difarProcess.getProcessedDifarData();
	}

	/**
	 * Delete a clip the user has asked to delete. A clip on the queue is
	 * deleted as before. A saved clip, in the viewer, is deleted for good:
	 * it leaves its crossing, which is recalculated or deleted by the usual
	 * rule, then the view reloads, which writes the deletion to its binary
	 * file and the database. The user confirms first.
	 * @param unit the clip.
	 */
	public void deleteClip(DifarDataUnit unit) {
		if (unit == null) {
			return;
		}
		if (!isViewer || !isSaved(unit)) {
			sendDifarMessage(new DIFARMessage(DIFARMessage.DeleteDatagramUnit, unit));
			return;
		}
		deleteSavedClip(unit);
	}

	/** Why an older dataset cannot be edited, and what to do about it. */
	private static final String OLD_FILES_ADVICE = "The data were written by an older version of PAMGuard, "
			+ "and older DIFAR files are read only. To edit them, run \"Upgrade old DIFAR files\" from "
			+ "DIFAR offline tasks, over all data. It backs up the files first.";

	/**
	 * In the viewer, whether the saved clips loaded now include any from files
	 * before the current version. A dataset is written by one version, so this
	 * stands for the dataset.
	 * @return true if an older file's clips are loaded.
	 */
	private boolean hasOldClips() {
		for (DifarDataUnit clip : difarProcess.getProcessedDifarData().getDataCopy()) {
			if (clip.getBinaryVersion() < DifarClipPayload.CURRENT_VERSION) {
				return true;
			}
		}
		return false;
	}

	/**
	 * In the viewer, refuse to save a clip into an older dataset, which would
	 * rewrite a file at the current version and lose the crossings in it.
	 * @return true if the save may go ahead.
	 */
	private boolean canSaveInViewer() {
		if (!isViewer || !hasOldClips()) {
			return true;
		}
		JOptionPane.showMessageDialog(getGuiFrame(), "<html>Cannot save this clip.<p><p>" + OLD_FILES_ADVICE,
				"Save DIFAR clip", JOptionPane.WARNING_MESSAGE);
		return false;
	}

	private void deleteSavedClip(DifarDataUnit unit) {
		int channel = PamUtils.getSingleChannel(unit.getChannelBitmap());
		String clip = String.format("the clip on channel %d at %s, UID %d", channel,
				PamCalendar.formatDateTime(unit.getTimeMilliseconds()), unit.getUID());
		if (unit.getBinaryVersion() < DifarClipPayload.CURRENT_VERSION) {
			JOptionPane.showMessageDialog(getGuiFrame(),
					String.format("<html>Cannot delete %s.<p><p>%s", clip, OLD_FILES_ADVICE),
					"Delete DIFAR clip", JOptionPane.WARNING_MESSAGE);
			return;
		}
		DifarCrossing crossing = unit.getCrossing();
		String effect = "";
		if (crossing != null) {
			int left = crossing.getSubDetectionsCount() - 1;
			effect = left >= CrossingLocaliser.MIN_CLIPS && !difarParameters.alwaysDeleteTrimmedCrossings
					? String.format("<p><p>It belongs to crossing UID %d, which will be worked out again from its other %d clips.",
							crossing.getUID(), left)
					: String.format("<p><p>It belongs to crossing UID %d, which will be deleted.", crossing.getUID());
		}
		int answer = JOptionPane.showConfirmDialog(getGuiFrame(),
				String.format("<html>Delete %s?%s<p><p>The clip is removed from its binary file and the "
						+ "database straight away.", clip, effect), "Delete DIFAR clip",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer != JOptionPane.OK_OPTION) {
			return;
		}
		difarProcess.getCrossingRecorder().removeClip(unit);
		ViewerClipStore store = difarProcess.getProcessedDifarData().getViewerClipStore();
		if (store != null) {
			store.clipDeleted(unit);
		}
		difarProcess.getProcessedDifarData().remove(unit);
		// clears the clip from the DIFARgram, the unit control panel and the saved strip
		sendDifarMessage(new DIFARMessage(DIFARMessage.DeleteDatagramUnit, unit));
		// the displays redraw without the clip, the trimmed crossing's other clips
		// redraw, and the deletion is written at once
		ViewerEdits edits = getViewerEdits();
		edits.removed(difarProcess.getProcessedDifarData(), unit);
		edits.crossingChanged(crossing);
		edits.commit();
	}
	

}
