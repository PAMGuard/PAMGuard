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

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import Array.ArrayManager;
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
import difar.display.SonobuoyManagerContainer;
import difar.display.SonobuoyManagerProvider;
import difar.offline.DifarDataCopyTask;
import difar.offline.UpdateCrossingTask;
import difar.plots.DifarBearingPlotProvider;
import difar.plots.DifarIntensityPlotProvider;
import difar.trackedGroups.TrackedGroupProcess;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
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
	 
	private SpectrogramObserver spectrogramObserver = new SpectrogramObserver();
	
	private ArrayList<DIFARDisplayUnit> displayUnits = new ArrayList<>();
	
	private DIFARGram difarGram; // specrogram and difargram display
	
	private DIFARUnitControlPanel difarUnitControlPanel;
	
	private DIFARQueuePanel difarQueue; // display of queues clips to process. 
	
	private DifarSidePanel difarSidePanel; // Side panel for easy access to frequenctly used DIFAR controls
	
	private DemuxProgressDisplay demuxProgressDisplay;
	
	private DifarDataUnit currentDemuxedUnit = null;
	private DifarDisplayContainer2 difarDisplayContainer2;
	private TrackedGroupProcess trackedGroupProcess;
	
	private KeyboardFocusManager keyManager;
	
	public static final boolean SPLITDISPLAYS = false;
	
	private OfflineTaskGroup offlineTaskGroup;
	
	public SonobuoyManager sonobuoyManager;
	
	private static PamWarning warningMessage = new PamWarning("Difar", "", 2);

	public DifarControl(String unitName) {
	
		super("DIFAR Processing", unitName);

		PamSettingManager.getInstance().registerSettings(this);		
		
		addPamProcess(difarProcess = new DifarProcess(this));
		addPamProcess(setTrackedGroupProcess(new TrackedGroupProcess(this, difarProcess.getProcessedDifarData(), "Difar Tracked Groups")));
		addPamProcess(sonobuoyManager = new SonobuoyManager(this));
		// make the displays here
		displayUnits.add(difarUnitControlPanel = new DIFARUnitControlPanel(this));
		displayUnits.add(difarGram = new DIFARGram(this));
		displayUnits.add(internalActionsPanel=new DifarActionsVesselPanel(this));
		displayUnits.add(difarQueue = new DIFARQueuePanel(this, "Queued Data"));
		displayUnits.add(demuxProgressDisplay = new DemuxProgressDisplay(this));
		

		if (SPLITDISPLAYS) {
			displayProvider2 = new DifarDisplayProvider2(this);
			UserDisplayControl.addUserDisplayProvider(displayProvider2);
		}
		displayProvider = new DifarDisplayProvider(this);
		UserDisplayControl.addUserDisplayProvider(displayProvider);
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
			if (!isViewer) {
				processNextIfAnyAndCanAndShould();
			}
			break;
		
		case DIFARMessage.DeleteFromQueue:
			if (!isViewer) {
				difarProcess.getQueuedDifarData().remove(message.difarDataUnit);
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
			if (!isViewer) {
				currentDemuxedUnit = null;
				difarProcess.getQueuedDifarData().remove(message.difarDataUnit);
				getDemuxProgressDisplay().newMessage(new DemuxWorkerMessage(message.difarDataUnit, 
						DemuxWorkerMessage.STATUS_DELETED, 0L, 0));
				processNextIfAnyAndCanAndShould();
			}else{
				currentDemuxedUnit = null;
			}
			break;
		case DIFARMessage.SaveDatagramUnit:
			if (!isViewer) {
				message.difarDataUnit.saveCrossing(true);
				difarProcess.finalProcessing(message.difarDataUnit);
				currentDemuxedUnit = null;
				getDemuxProgressDisplay().newMessage(new DemuxWorkerMessage(message.difarDataUnit, 
						DemuxWorkerMessage.STATUS_SAVED, 0L, 100));
				difarProcess.cancelAutoSaveTimer();
				processNextIfAnyAndCanAndShould();
			}
			break;
			
		case DIFARMessage.SaveDatagramUnitWithoutRange:
		
			if (!isViewer) {
				//remove Range/Localisation Information
				message.difarDataUnit.saveCrossing(false);
				difarProcess.finalProcessing(message.difarDataUnit);
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
			JMenuItem offlineDataItem = new JMenuItem("Copy DIFAR Binaries to Database");
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
	
	private void runOfflineTasks() {
		if (offlineTaskGroup == null) {
			offlineTaskGroup = new OfflineTaskGroup(this, getUnitName());
			offlineTaskGroup.setPrimaryDataBlock(difarProcess.getProcessedDifarData());
			offlineTaskGroup.addTask(new UpdateCrossingTask<DifarDataUnit>(difarProcess.getProcessedDifarData()));
			offlineTaskGroup.addTask(new DifarDataCopyTask<DifarDataUnit>(difarProcess.getProcessedDifarData()));
//			offlineTaskGroup.addTask(task);
		}
		OLProcessDialog olProcessDialog;
		olProcessDialog = new OLProcessDialog(getGuiFrame(), offlineTaskGroup, "DIFAR Data Export");
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
			difarProcess.setupProcess();
			break;
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			sonobuoyManager.updateSonobuoyTableData();
		}
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
		return (isViewer() || (currentDemuxedUnit == null
				&& !difarProcess.isProcessing()
				));
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
		return (!isViewer && 
				getCurrentDemuxedUnit() != null && 
				getCurrentDemuxedUnit().getSelectedAngle() != null);
	}
	
	public boolean isSaveWithoutCrossEnabled() {
		return (!isViewer && 
				getCurrentDemuxedUnit() != null &&
				getCurrentDemuxedUnit().getSelectedAngle() !=null && 
				getCurrentDemuxedUnit().getTempCrossing() != null);
	}	
	
	public boolean isDeleteEnabled() {
		return (!isViewer && getCurrentDemuxedUnit() != null);
	}
	

}
