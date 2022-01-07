/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package clickDetector;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import rocca.RoccaControl;
import soundPlayback.PlaybackControl;
import targetMotionOld.TargetMotionLocaliser;


import binaryFileStorage.BinaryStore;
import Filters.FilterDialog;
import Filters.FilterParams;
import Localiser.detectionGroupLocaliser.GroupDetection;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.PamControlledGUISwing;
import PamView.PamGui;
import PamView.WrapperControlledGUISwing;
import PamView.dialog.warn.WarnOnce;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.debug.Debug;
import alarm.AlarmCounterProvider;
import angleVetoes.AngleVetoes;
import clickDetector.ClickClassifiers.ClickClassifierManager;
import clickDetector.ClickClassifiers.ClickClassifyDialog;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.ClickClassifiers.ClickTypeMasterManager;
import clickDetector.alarm.ClickAlarmProvider;
import clickDetector.clicktrains.ClickTrainIdParams;
import clickDetector.dialogs.ClickAlarmDialog;
import clickDetector.dialogs.ClickParamsDialog;
import clickDetector.dialogs.ClickStorageOptionsDialog;
import clickDetector.dialogs.ClickTrainIdDialog;
import clickDetector.echoDetection.EchoDetectionSystem;
import clickDetector.echoDetection.SimpleEchoDetectionSystem;
import clickDetector.layoutFX.ClickControlGUI;
import clickDetector.localisation.ClickLocParams;
import clickDetector.localisation.ClickLocalisationDialog;
import clickDetector.offlineFuncs.ClickMarkHandler;
import clickDetector.offlineFuncs.ClicksOffline;
import clickDetector.offlineFuncs.EventListDialog;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickDetector.offlineFuncs.OfflineToolbar;
import clickDetector.offlineFuncs.rcImport.BatchRainbowFileConversion;
import clickDetector.offlineFuncs.rcImport.RainbowDatabaseConversion;
import clickDetector.tdPlots.ClickPlotProvider;
import clickDetector.toad.ClickFFTOrganiser;
import dataPlots.data.TDDataProviderRegister;
import dataPlotsFX.clickPlotFX.ClickPlotProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import detectionPlotFX.data.DDPlotRegister;
import detectionPlotFX.rawDDPlot.ClickDDPlotProvider;
import fftManager.fftorganiser.FFTDataOrganiser;

/**
 * Main Controller for click detection.
 * <p>
 * ClickControl contains both the detector and the display panel. It also
 * contains information on Detection and Display menus which will get added to
 * the main PamGuard menu.
 * 
 * @author Doug Gillespie
 * 
 */

public class ClickControl extends PamControlledUnit implements PamSettings {

	protected ClickDetector clickDetector;

	protected ClickTrainDetector clickTrainDetector;

	protected EchoDetectionSystem echoDetectionSystem;

	protected TrackedClickLocaliser trackedClickLocaliser;

	protected ClickTabPanelControl tabPanelControl;

	protected ClickParameters clickParameters = new ClickParameters();

	protected JMenu rightMouseMenu;

	protected ClickTabPanel clickPanel;

	protected ClickControl clickControl;

	private OfflineToolbar offlineToolbar;

	protected ClickSidePanel clickSidePanel;

	protected AngleVetoes angleVetoes;

	protected ClickAlarmManager clickAlarmManager;

	/**
	 * The current internal click type classifier
	 */
	private ClickIdentifier clickIdentifier;
	
	/**
	 * Manages internal click classifiers.
	 */
	private ClickClassifierManager classifierManager;

	/**
	 * Manages click types from both the internal ClickIdentifier and externel 
	 * downstream processes which change click types. 
	 */
	private ClickTypeMasterManager clickTypeMasterManager;
	
	/**
	 * Offline processing for the click detector
	 */
	private ClicksOffline clicksOffline;

	/**
	 * True or falswe depdning whether PG is in viewer mode. 
	 */
	private boolean viewerMode = false;

	private OfflineEventDataUnit latestOfflineEvent;

	/**
	 * The offline target motion localisation mmodule. 
	 */
	private TargetMotionLocaliser<GroupDetection> targetMotionLocaliser;

	private String dataBlockPrefix = "";

	/**
	 * The click alarm provider for trigered alrams. 
	 */
	private ClickAlarmProvider alarmProvider;

	/**
	 * reference to roccaControl module (if it exists)
	 */
	private RoccaControl roccaControl;

	private ClickMarkHandler clickMarkHandler;

	/**
	 * Provides the display for clicks in the FX Time display. 
	 */
	private ClickPlotProviderFX clickPlotProviderFX;
	
	/**
	 * Click plot provider for detection displays
	 */
	private ClickDDPlotProvider clickDDPlotProvider; 
	
	/**
	 * The FX GUI for the click detector
	 */
	private ClickControlGUI clickGUIFX;

	/**
	 * The GUI for swing (currently just references back to function in the clickControl. )
	 */
	private PamControlledGUISwing clickGUISwing;


	public static final String UNITTYPE = "Click Detector";

	public ClickControl(String name) {

		super(UNITTYPE, name);

		sortDataBlockPrefix();

		viewerMode = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		//
		//		if (inputControl.getPamProcess(0).getOutputDataBlock(0).getDataType() == DataType.RAW) {
		//			rawDataBlock = (PamRawDataBlock) inputControl.getPamProcess(0).getOutputDataBlock(0);
		//		}
		clickControl = this;


		angleVetoes = new AngleVetoes(this);

		offlineToolbar = new OfflineToolbar(this);


		clickMarkHandler = new ClickMarkHandler(this);
		OverlayMarkObservers.singleInstance().addObserver(clickMarkHandler);

		PamSettingManager.getInstance().registerSettings(this);

		//		clickParameters.clickLocParams.generateLocList(); //generate a list of loclaisers. 

		addPamProcess(clickDetector = new ClickDetector(this));

		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			clicksOffline = new ClicksOffline(this);
			//			offlineToolbar.addButtons(getClicksOffline().getCommandButtons());
		}

		//click train detection
		clickTrainDetector = new ClickTrainDetector(this, clickDetector.getClickDataBlock());
		addPamProcess(clickTrainDetector);

		trackedClickLocaliser = new TrackedClickLocaliser(this, clickDetector.getTrackedClicks());
		addPamProcess(trackedClickLocaliser);

		//misc identifiers 
		echoDetectionSystem = new SimpleEchoDetectionSystem(this);
		
		//click alarms
		clickAlarmManager = new ClickAlarmManager(this, clickDetector.getClickDataBlock());
		addPamProcess(clickAlarmManager);

		// click classifiation 
		setClassifierManager(new ClickClassifierManager(this));

		clickTypeMasterManager = new ClickTypeMasterManager(this); 
		clickTypeMasterManager.updateSpeciesList();

		clickIdentifier = getClassifierManager().getClassifier(clickParameters.clickClassifierType);
		setClickIdentifier(clickIdentifier);
		
		//the GUI <- this should be moved elsewhere eventually. 
		if (PamGUIManager.isSwing()) {
			setTabPanel(tabPanelControl = new ClickTabPanelControl(this));
			clickPanel = tabPanelControl.getClickPanel();

			if (offlineToolbar != null) {
				//			tabPanelControl.getPanel().add(BorderLayout.NORTH, offlineToolbar.getToolBar());
				setToolbarComponent(offlineToolbar.getToolBar());
			}

			if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
				setSidePanel(clickSidePanel = new ClickSidePanel(this));
			}
		}

		//viewer mode controls. 
		if (viewerMode) {
			targetMotionLocaliser = new TargetMotionLocaliser(this, 
					clickDetector.getOfflineEventDataBlock(), clickDetector.getClickDataBlock());
			clickDetector.setTargetMotionLocaliser(targetMotionLocaliser);

			// check if a Rocca module is loaded.
			roccaControl = (RoccaControl) PamController.getInstance().findControlledUnit(RoccaControl.unitType);

		}

		if (PamGUIManager.isSwing()) {

			rightMouseMenu = createDisplayMenu(null);

			new ClickSpectrogramPlugin(this);

		}

		alarmProvider = new ClickAlarmProvider(this);

		//register click detector for swing time display 
		TDDataProviderRegister.getInstance().registerDataInfo(new ClickPlotProvider(this, getClickDataBlock()));
		//register click detector for the javafx display. 
		TDDataProviderRegisterFX.getInstance().registerDataInfo(clickPlotProviderFX = new ClickPlotProviderFX(this, getClickDataBlock()));
		//register the DD display
		DDPlotRegister.getInstance().registerDataInfo(clickDDPlotProvider  = new ClickDDPlotProvider(this));
		
	}


	/**
	 * Get the click type master manager. This manages all possible 
	 * click species type, including those of downstream processes. 
	 * @return the clickTypeMasterManager
	 */
	public ClickTypeMasterManager getClickTypeMasterManager() {
		return clickTypeMasterManager;
	}


	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#removeUnit()
	 */
	@Override
	public boolean removeUnit() {
		TDDataProviderRegisterFX.getInstance().unRegisterDataInfo(clickPlotProviderFX);
		return super.removeUnit();
	}


	/**
	 * Get the datablock containing click data
	 * @return Click data block
	 */
	public ClickDataBlock getClickDataBlock() {
		return clickDetector.getClickDataBlock();
	}

	/**
	 * Speedier way of knowing if it's viewer mode than going back to the controller every time 
	 * @return
	 */
	public boolean isViewerMode() {
		return viewerMode;
	}

	public void secondTimer(long sampleNumber) {

	}

	public int getTrueChannelNumber(int iCh) {
		// get the real input channel number from the bitmap.
		return PamUtils.getNthChannel(iCh, clickParameters.getChannelBitmap());
	}

	public void displayTriggerHistogram(TriggerHistogram[] triggerHistogram) {
		//		clickPanel.getTriggerDisplay()
		//				.displayTriggerHistogram(triggerHistogram);
	}

	public void notifyNewStorage(String storageName) {
		//		clickPanel.getBtDisplay().notifyNewStorage(storageName);
	}

	@Override
	public JMenu createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem;
		JMenu menu = new JMenu(getUnitName());

		if (isViewerMode()) {
			if (clicksOffline.addDetectorMenuItems(parentFrame, menu) > 0) {
				if (targetMotionLocaliser != null) {
					targetMotionLocaliser.addDetectorMenuItems(parentFrame, menu);
				}

				// add a menu item to run the Rocca measurement tool, if the Rocca
				// module has been loaded
				// check if a Rocca module is loaded.
				roccaControl = (RoccaControl) PamController.getInstance().findControlledUnit(RoccaControl.unitType);
				if (roccaControl != null) {
					roccaControl.addDetectorMenuItems(this, menu, menu.getItemCount());
				}
				menu.addSeparator();
			}
		}

		menuItem = new JMenuItem("Detection Parameters ...");
		menuItem.addActionListener(new MenuDetection(parentFrame));
		menu.add(menuItem);

		menu.add(clickMarkHandler.getMarkOptionsMenuItem(parentFrame));

		menuItem = new JMenuItem("Digital pre filter ...");
		menuItem.addActionListener(new MenuPreFilter(parentFrame));
		menu.add(menuItem);

		menuItem = new JMenuItem("Digital trigger filter ...");
		menuItem.addActionListener(new MenuTriggerFilter(parentFrame));
		menu.add(menuItem);

		menu.add(angleVetoes.getSettingsMenuItem(parentFrame));

		// menuItem = new JMenuItem("Click types ...");
		// menuItem.addActionListener(new menuClickTypes());
		//		menu.add(menuItem);
		//		if (getClickIdentifier() != null
		//				&& getClickIdentifier().getMenuItem(parentFrame) != null) {
		//			menu.add(getClickIdentifier().getMenuItem(parentFrame));
		//		}
		menuItem = new JMenuItem("Click Classification ...");
		menuItem.addActionListener(new MenuClickClassification(parentFrame));
		menu.add(menuItem);

		menuItem = new JMenuItem("Click Train Identification ...");
		menuItem.addActionListener(new MenuClickTrainId(parentFrame));
		menu.add(menuItem);

		menuItem = new JMenuItem("Click Train Localisation ...");
		menuItem.addActionListener(new ClickLocaliserMenu(parentFrame));
		menuItem.setToolTipText("Target Motion Analysis (TMA) options");
		menu.add(menuItem);

		//		menuItem = new JMenuItem("Storage Options ...");
		//		menuItem.addActionListener(new MenuStorageOptions(parentFrame));
		//		menu.add(menuItem);

		menuItem = new JMenuItem("Audible Alarm ...");
		menuItem.addActionListener(new MenuAlarm(parentFrame));
		menu.add(menuItem);

		/*
		 *  if in viewer mode and there is  a binary store
		 *  include options for batch converting clk files. 
		 */
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
			if (binaryStore != null) {
				menu.addSeparator();
				//				JMenuItem subMenu = new JMenu("Rainbow Click Import");
				menuItem = new JMenuItem("Import RainbowClick Data ...");
				menuItem.addActionListener(new BatchConvertClkFiles(parentFrame));
				//				subMenu.add(menuItem);
				//				menuItem = new JMenuItem("Import event database information ...");
				//				menuItem.addActionListener(new BatchConvertClkDatabase(parentFrame));
				//				subMenu.add(menuItem);

				menu.add(menuItem);
			}

			menu.add(clicksOffline.getDatabaseCheckItem(parentFrame));
			menu.add(clicksOffline.getExportMenuItem(parentFrame));
		}



		return menu;
	}

	//	@Override
	//	public JMenuItem createHelpMenu(Frame parentFrame) {
	//		JMenuItem menuItem;
	//		JMenu menu = new JMenu(getUnitName());
	//		menuItem = new JMenuItem("Click Detector");
	//		menuItem.addActionListener(new StartHelp(parentFrame));
	//		menu.add(menuItem);
	//		return menuItem;
	//	}


	@Override
	public JMenu createDisplayMenu(Frame parentFrame) {
		return tabPanelControl.createMenu(parentFrame);
	}

	private class MenuDetection implements ActionListener {

		Frame pf;

		public MenuDetection(Frame parentFrame) {
			pf = parentFrame;
		}

		public void actionPerformed(ActionEvent ev) {
			ClickParameters newParameters = ClickParamsDialog.showDialog(pf, clickControl, clickParameters);
			if (newParameters != null) {
				clickParameters = newParameters.clone();
				clickDetector.newParameters();
				newClassifySettings();
				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
			}
		}

	}

	private class ClickLocaliserMenu implements ActionListener {

		Frame pf;

		public ClickLocaliserMenu(Frame parentFrame) {
			pf = parentFrame;
		}

		public void actionPerformed(ActionEvent ev) {
			ClickLocParams newLocParameters = ClickLocalisationDialog.showDialog(clickControl, pf, 
					clickControl.clickTrainDetector.getDetectionGroupLocaliser());
			if (newLocParameters != null) {
				clickParameters.clickLocParams=newLocParameters;
			}
		}

	}

	private boolean modelComplete = false;
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);

		// really don't want this here since it causes far too much redrawing
		// need to worry slightly about what it WAS getting called for though !
		//		tabPanelControl.getClickPanel().noteNewSettings();

		switch (changeType) {
		case PamControllerInterface.ADD_CONTROLLEDUNIT: 
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (modelComplete){
				clickDetector.setupProcess();
			}
			break;

		case PamControllerInterface.INITIALIZATION_COMPLETE:
			modelComplete = true;
			clickDetector.setupProcess();
			if (tabPanelControl!=null)  tabPanelControl.getClickPanel().noteNewSettings();
			break;
		case PamControllerInterface.DATA_LOAD_COMPLETE: //update click types. 
			setLatestOfflineEvent(null);
			clearEventCoordinates();
			if (tabPanelControl!=null)  tabPanelControl.getClickPanel().noteNewSettings();
			break;
		case PamControllerInterface.HYDROPHONE_ARRAY_CHANGED:
			clickDetector.notifyArrayChanged();
			if (tabPanelControl!=null)  tabPanelControl.getClickPanel().noteNewSettings();
			break;
		case PamControllerInterface.CHANGED_DISPLAY_SETTINGS:
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			if (tabPanelControl!=null) tabPanelControl.getClickPanel().noteNewSettings();
			break;
		case PamControllerInterface.OFFLINE_PROCESS_COMPLETE:
			if (tabPanelControl!=null)  tabPanelControl.getDisplayManager().findFirstBTDisplay().repaintTotal();
			break;
		case PamControllerInterface.GLOBAL_MEDIUM_UPDATE:
			clickDetector.getClickDataBlock().setForceAmplitudeUpdate(); 
			if (tabPanelControl!=null) {
				tabPanelControl.getDisplayManager().setDefaultAmplitudeScales(); 
				tabPanelControl.getDisplayManager().findFirstBTDisplay().repaintTotal();
			}
			break;
		}
		//if FX GUI then send notification through
		if (clickGUIFX!=null) clickGUIFX.notifyGUIChange(changeType);
		//pass notification to the click type manager. 
		clickTypeMasterManager.notifyModelChanged(changeType);

		EventListDialog.notifyModelChanged(changeType);

	}

	/**
	 * Gets called whenever new data are loaded in the viewer so 
	 * that origin coordinates of offline events can be cleared. 
	 * This is needed because all events are loaded persistently
	 * in the Viewer, and not all GPS data are present. therefore 
	 * most events will have an over extrapolated origin - which 
	 * is really bad ! By clearing them all, events from the loaded
	 * period will at least get the right origin - and the others
	 * don't really matter too much. 
	 */
	private void clearEventCoordinates() {
		OfflineEventDataBlock offlineDataBlock = clickDetector.getOfflineEventDataBlock();
		if (offlineDataBlock == null) {
			return;
		}
		ListIterator<OfflineEventDataUnit> it = offlineDataBlock.getListIterator(0);
		while (it.hasNext()) {
			it.next().clearOandAngles();
		}

	}

	class MenuPreFilter implements ActionListener {

		Frame pf;

		public MenuPreFilter(Frame parentFrame) {
			pf = parentFrame;
		}
		public void actionPerformed(ActionEvent ev) {
			FilterParams newParams = FilterDialog.showDialog(pf,
					clickParameters.preFilter, getClickDetector()
					.getSampleRate());
			if (newParams != null) {
				clickParameters.preFilter = newParams;
				clickDetector.newParameters();
			}
		}
	}

	class MenuTriggerFilter implements ActionListener {

		Frame pf;

		public MenuTriggerFilter(Frame parentFrame) {
			pf = parentFrame;
		}
		public void actionPerformed(ActionEvent ev) {
			FilterParams newParams = FilterDialog.showDialog(pf,
					clickParameters.triggerFilter, getClickDetector()
					.getSampleRate());
			if (newParams != null) {
				clickParameters.triggerFilter = newParams;
				clickDetector.newParameters();
			}
		}
	}

	class MenuClickTrainId implements ActionListener {

		Frame pf;

		public MenuClickTrainId(Frame parentFrame) {
			pf = parentFrame;
		}
		public void actionPerformed(ActionEvent ev) {
			ClickTrainIdParams newParams;
			if ((newParams = ClickTrainIdDialog.showDialog(pf, clickTrainDetector.getClickTrainIdParameters())) != null) {
				clickTrainDetector.setClickTrainIdParameters(newParams.clone());
				//clickDetector.NewParameters();
			}
		}
	}

	class MenuClickClassification implements ActionListener {

		Frame pf;

		public MenuClickClassification(Frame parentFrame) {
			pf = parentFrame;
		}
		public void actionPerformed(ActionEvent ev) {
			classificationDialog(pf);
		}
	}

	/**
	 * Opens the offline click dialog. 
	 * @param pf frame
	 * @return Returns true if settings have changed
	 */
	public boolean classificationDialog(Frame pf) {
		ClickParameters newParams;
		if ((newParams = ClickClassifyDialog.showDialog(this, pf, clickParameters)) != null) {
			clickParameters = newParams.clone();
			tabPanelControl.clickPanel.noteNewSettings();
			newClassifySettings();
			return true;
		}
		return false; // return so that offline dialog can know. 
	}

	private void newClassifySettings() {
		clickIdentifier = getClassifierManager().getClassifier(clickParameters.clickClassifierType);
		clickTypeMasterManager.updateSpeciesList(); //update the species list in the master manager. 
		if (offlineToolbar != null) {
			offlineToolbar.setupToolBar();
		}
	}

	class MenuStorageOptions implements ActionListener {

		Frame pf;

		public MenuStorageOptions(Frame parentFrame) {
			pf = parentFrame;
		}
		public void actionPerformed(ActionEvent ev) {
			ClickParameters newParams;
			if ((newParams = ClickStorageOptionsDialog.showDialog(pf, clickParameters)) != null) {
				clickParameters = newParams.clone();
				clickDetector.newParameters();
			}
		}
	}

	class MenuAlarm implements ActionListener {

		Frame pf;

		public MenuAlarm(Frame parentFrame) {
			pf = parentFrame;
		}
		public void actionPerformed(ActionEvent ev) {
			ClickParameters newParams;
			if ((newParams = ClickAlarmDialog.showDialog(pf, clickParameters))
					!= null) {
				clickParameters = newParams.clone();
				clickDetector.newParameters();
			}
		}
	}


	/**
	 * Batch convert rainbow click files into the latest 
	 * PAMGUARD binary format. 
	 * @author Doug Gillespie
	 *
	 */
	class BatchConvertClkFiles implements ActionListener {

		Frame parentFrame;

		public BatchConvertClkFiles(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent ev) {
			batchConvertClicks(parentFrame);
		}
	}
	class BatchConvertClkDatabase implements ActionListener {

		Frame parentFrame;

		public BatchConvertClkDatabase(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent ev) {
			batchConvertClickDatabase(parentFrame);
		}
	}


	/**
	 * Batch convert RainbowClick files to the latest
	 * PAMGUARD binary format. 
	 * @param parentFrame parent frame for dialog. 
	 */
	private void batchConvertClicks(Frame parentFrame) {
		BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
		if (binaryStore == null) {
			return;
		}
		BatchRainbowFileConversion.showDialog(parentFrame, this, binaryStore);
	}

	public void batchConvertClickDatabase(Frame parentFrame) {
		BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
		if (binaryStore == null) {
			return;
		}
		RainbowDatabaseConversion.showDialog(parentFrame, this, binaryStore);
	}


	public ClickDetector getClickDetector() {
		return clickDetector;
	}

	protected void newRawData(PamObservable source, PamDataUnit data) {
		//		tabPanelControl.clickPanel.triggerDisplay.update(source, data);
		//		tabPanelControl.clickPanel.btDisplay.update(source, data);
	}

	/*
	 * Since this is not an observer of anything, these actually get called from
	 * the equivalent functions in the Detector
	 */
	public void pamStart() {
		//		this.clickPanel.btDisplay.reset();
		if (clickTrainDetector != null) clickTrainDetector.clearAllTrains();
		if (tabPanelControl != null) { //tab panel can be null if another GUI is used. This eventually should be in Swing GUI class. 
			ArrayList<ClickDisplay> displays = tabPanelControl.clickDisplayManager.getWindowList();
			for (int i = 0; i < displays.size(); i++) {
				displays.get(i).pamStart();
			}
		}

		/*
		 * These processes are now hidden, so will need to call their start and stop function explicitly. 
		 */
		clickTrainDetector.pamStart();
		trackedClickLocaliser.pamStart();
		clickAlarmManager.pamStart();

		//		clickDetector.getSourceProcess().getParentDataBlock().addObserver(tabPanelControl.clickPanel.triggerDisplay);
		//		clickDetector.getSourceProcess().getParentDataBlock().addObserver(tabPanelControl.clickPanel.btDisplay);
		//		tabPanelControl.clickPanel.btDisplay.sampleRate = clickDetector.getSampleRate();
	}

	/*
	 * Since this is not an observer of anything, these actually get called from
	 * the equivalent functions in the Detector
	 */
	public void pamStop() {
		if (tabPanelControl!=null) {
			ArrayList<ClickDisplay> displays = tabPanelControl.clickDisplayManager.getWindowList();
			for (int i = 0; i < displays.size(); i++) {
				displays.get(i).pamStop();
			}
		}
		clickTrainDetector.pamStop();
		trackedClickLocaliser.pamStop();
		clickAlarmManager.pamStop();
	}

	/*
	 * Stuff for settings interface
	 */

	@Override
	public boolean canClose() {
		//		if (isViewerMode()) {
		//			getClicksOffline().saveClicks();
		//		}
		return super.canClose();
	}

	public long getSettingsVersion() {
		return ClickParameters.serialVersionUID;
	}

	public Serializable getSettingsReference() {
		return clickParameters;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.clickParameters = ((ClickParameters) pamControlledUnitSettings.getSettings()).clone();
		if (clickParameters.createRCFile) {
			// show a warning message that this is no longer supported. 
			String title = "Rainbow Click compatible output files";
			String msg = "<html>You are opening a configuration which is set up to create RainbowClick compatible output (.clk) files.<p>" +
					"This feature is no longer supported in PAMGuard. <p><p>You are advised to switch to using the PAMGuard binary storage system instead.</html>";
			String help = "utilities.BinaryStore.docs.binarystore_overview";
			int ans = WarnOnce.showWarning(getGuiFrame(), title, msg, WarnOnce.OK_CANCEL_OPTION, help);
			if (ans == WarnOnce.OK_OPTION) {
				clickParameters.createRCFile = false;
			}
		}
		return true;
	}

	JMenuBar clickTabMenu = null;

	private ClickFFTOrganiser clickFFTDataOrganiser;




	@Override
	public JMenuBar getTabSpecificMenuBar(Frame parentFrame, JMenuBar standardMenu, PamGui pamGui) {
		JMenu aMenu;
		// start bymaking a completely new copy.
		//		if (clickTabMenu == null) {
		clickTabMenu = standardMenu;
		for (int i = 0; i < clickTabMenu.getMenuCount(); i++) {
			if (clickTabMenu.getMenu(i).getText().equals("Display")) {
				//clickTabMenu.remove(clickTabMenu.getMenu(i));

				aMenu = createDetectionMenu(parentFrame);
				aMenu.setText("Click Detection");
				clickTabMenu.add(aMenu, i+1);

				aMenu = tabPanelControl.createMenu(parentFrame);
				aMenu.setText("Click Display");
				clickTabMenu.add(aMenu, i+2);

				break;
			}
		}
		//		}
		return clickTabMenu;
	}

	@Override
	public void addOtherRelatedMenuItems(Frame parentFrame, JMenu menu, String name) {
		//		if (name.equals("Map")) {
		//			
		//			JMenuItem menuItem = new JMenuItem("Click Bearings ...");
		//			menuItem.addActionListener(new MapOptions(parentFrame));
		//			menu.add(menuItem);
		//		}
	}
	protected void clickedOnClick(ClickDetection click) {
		tabPanelControl.clickDisplayManager.clickedOnClick(click);
	}
	//	/**
	//	 * 
	//	 * @author dgillespie
	//	 * This is a direct copy of the class in ClickTabPanelControl - am 
	//	 * thinking of getting rid of TabPanelControl though and doing everything
	//	 * from within each PamControlledUnit. 
	//	 */
	//	 class MapOptions implements ActionListener {
	//		 Frame parentFrame;
	//		 MapOptions(Frame parentFrame) {
	//			 this.parentFrame = parentFrame;
	//		 }
	//		 public void actionPerformed(ActionEvent ev) {
	//			 ClickParameters newParameters = 
	//				 ClickMapDialog.showDialog(parentFrame, clickParameters);
	//			 if (newParameters != null){
	//				 clickParameters = newParameters.clone();
	//			 }
	//		 }
	//	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	@Override
	public void setupControlledUnit() {
		// if there is no input data block, get th first one in the system.
		//		PamDataBlock rawBlock = PamController.getInstance().getRawDataBlock(clickParameters.rawDataSource);
		//		if (rawBlock == null) {
		//			rawBlock = PamController.getInstance().getRawDataBlock(0);
		//			if (rawBlock != null) clickParameters.rawDataSource = rawBlock.toString();
		//		}
		super.setupControlledUnit();
		//tabPanelControl.clickToolBar.setControls(clickParameters);		
	}

	public ClickIdentifier getClickIdentifier() {
		return clickIdentifier;
	}

	public void setClickIdentifier(ClickIdentifier clickIdentifier) {
		this.clickIdentifier = clickIdentifier;
		newClassifySettings();
	}

	/**
	 * @param classifierManager the classifierManager to set
	 */
	public void setClassifierManager(ClickClassifierManager classifierManager) {
		this.classifierManager = classifierManager;
	}

	/**
	 * @return the classifierManager
	 */
	public ClickClassifierManager getClassifierManager() {
		return classifierManager;
	}

	public ClickParameters getClickParameters() {
		return clickParameters;
	}

	public void setClickParameters(ClickParameters clickParameters) {
		this.clickParameters = clickParameters;
	}

	//	public void newOfflineStore() {
	//		clickDetector.setSampleRate(getClicksOffline().getSampleRate(), false);
	//		tabPanelControl.newOfflineStore();
	//		offlineToolbar.newStore();
	//	}

	/** 
	 * Called from clicksOffline when data have changed (eg from re doing click id). 
	 * Needs to notify the display and maybe some other classes. 
	 */
	public void offlineDataChanged() {
		tabPanelControl.offlineDataChanged();
	}

	public OfflineToolbar getOfflineToolbar() {
		return offlineToolbar;
	}


	/**
	 * @return the clicksOffline
	 */
	public ClicksOffline getClicksOffline() {
		return clicksOffline;
	}


	/**
	 * @return the latestOfflineEvent
	 */
	public OfflineEventDataUnit getLatestOfflineEvent() {
		return latestOfflineEvent;
	}

	/**
	 * @param latestOfflineEvent the latestOfflineEvent to set
	 */
	public void setLatestOfflineEvent(OfflineEventDataUnit latestOfflineEvent) {
		this.latestOfflineEvent = latestOfflineEvent;
	}

	/**
	 * Show a list of offline events. 
	 * Later on will add more functionality to it. 
	 * @param frame
	 */
	public void showOfflineEvents(Frame frame) {
		EventListDialog.showDialog(frame, clickControl);
	}

	/**
	 * Scrolls the display to a specific event. 
	 * @param event event to scroll to
	 */
	public void gotoEvent(OfflineEventDataUnit event) {
		tabPanelControl.clickDisplayManager.gotoEvent(event);
	}

	/**
	 * Delete an offline event. Un-assign all clicks and 
	 * delete from datablock, ready for deletion from database. 
	 * @param event event to delete. 
	 */
	public void deleteEvent(OfflineEventDataUnit event) {
		if (event == null) {
			return;
		}
		/**
		 * Need to tell all the sub detections so that they get deleted
		 * when the clicks are saved. 
		 */
		int n = event.getSubDetectionsCount();
		for (int i = 0; i < n; i++) {
			PamDataUnit subDet = event.getSubDetection(i);
			if (subDet != null) {
				subDet.removeSuperDetection(event);
			}
		}
		clickDetector.getOfflineEventDataBlock().remove(event);
	}

	@Override
	public boolean canPlayViewerSound() {
		return PlaybackControl.getViewerPlayback().hasPlayDataSource();
	}

	@Override
	public void playViewerSound() {
		clickPanel.clickTabPanelControl.clickDisplayManager.playViewerData();
	}

	public void playClicks() {
		clickPanel.clickTabPanelControl.clickDisplayManager.playClicks();		
	}

	//	/**
	//	 * @return the targetMotionLocaliser
	//	 */
	//	public TargetMotionLocaliser<OfflineEventDataUnit> getTargetMotionLocaliser() {
	//		return targetMotionLocaliser;
	//	}

	/**
	 * @return the echoDetectionSystem
	 */
	public EchoDetectionSystem getEchoDetectionSystem() {
		return echoDetectionSystem;
	}

	/**
	 * @param echoDetectionSystem the echoDetectionSystem to set
	 */
	public void setEchoDetectionSystem(EchoDetectionSystem echoDetectionSystem) {
		this.echoDetectionSystem = echoDetectionSystem;
	}

	public void displayActivated(ClickDisplay clickDisplay) {
		if (offlineToolbar != null) {
			offlineToolbar.displayActivated(clickDisplay);
		}
	}

	public ClickDisplayManager getDisplayManager(){
		return tabPanelControl.clickDisplayManager;
	}

	/**
	 * Solve problems with all click data blocks being called the same thing
	 * if multiple click detectors are in the system. 
	 */
	private void sortDataBlockPrefix() {
		ArrayList<PamControlledUnit> unitList = PamController.getInstance().findControlledUnits(getUnitType());
		boolean haveOthers = (unitList != null && unitList.size() > 0);
		if (haveOthers) {
			dataBlockPrefix = getUnitName() + "_";
		}
	}
	/**
	 * Solve problems with all click data blocks being called the same thing
	 * if multiple click detectors are in the system. 
	 * @return a name, defaulting to "" if it's the first created click detector. 
	 */
	public String getDataBlockPrefix() {
		return dataBlockPrefix;
	}

	@Override
	public String getModuleSummary() {
		// TODO Auto-generated method stub
		return super.getModuleSummary();
	}

	@Override
	public Object getShortUnitType() {
		return "CD";
	}


	/**
	 * Get the counting system for the click alarm. 
	 * @param alarmControl 
	 * @return
	 */
	public AlarmCounterProvider getAlarmCounterProvider() {
		return alarmProvider;
	}


	public TargetMotionLocaliser<GroupDetection> getTargetMotionLocaliser() {
		return targetMotionLocaliser;
	}


	/**
	 * Reassign all the clicks on one event to a different event 
	 * @param clickEvent event to take the clicks from
	 * @param reassignEvent event to add the clicks to. 
	 */
	public void reassignEventClicks(OfflineEventDataUnit clickEvent, OfflineEventDataUnit reassignEvent) {
		/**
		 * Automatic events are of type ClickTrainDetection, manual events are
		 * of type OfflineEventDataUnit
		 */
		long now = PamCalendar.getTimeInMillis();
		OfflineEventDataBlock offlineEventDataBlock = clickDetector.getOfflineEventDataBlock();
		PamDataBlock<PamDataUnit> trackedClicks = clickDetector.getTrackedClicks();
		synchronized (clickEvent.getSubDetectionSyncronisation()) {
			int nClicks = clickEvent.getSubDetectionsCount();
			Debug.out.printf("Reassign %d clicks from event %d into event %d\n", nClicks, clickEvent.getEventId(), reassignEvent.getEventId());
			while (clickEvent.getSubDetectionsCount() > 0) {
				PamDataUnit aClick = clickEvent.getSubDetection(0);
				//				aClick.setEventId(reassignEvent.getEventId());

				aClick.removeSuperDetection(clickEvent); // not sure if I need this or not ? 
				if (ClickDetection.class.isAssignableFrom(aClick.getClass())) {
					trackedClicks.updatePamData((ClickDetection) aClick, now);
				}
				clickEvent.removeSubDetection(aClick);
				reassignEvent.addSubDetection(aClick);
				//				aClick.setEventId(reassignEvent.getEventId());
			}
			clickEvent.setComment(clickEvent.getComment() + " Clicks reassigned to event " + reassignEvent.getEventId());
			offlineEventDataBlock.updatePamData(clickEvent, PamCalendar.getTimeInMillis());
			offlineEventDataBlock.remove(clickEvent);
			reassignEvent.sortSubDetections();
			offlineEventDataBlock.updatePamData(reassignEvent, now);
			if (ClickTrainDetection.class.isAssignableFrom(reassignEvent.getClass())) {
				ClickTrainDetection ctd = (ClickTrainDetection) reassignEvent;
				ctd.setTrainStatus(ClickTrainDetection.STATUS_OPEN);
			}
			//			clickDetector.
			// need to tell it to localise !
		}

	}

	/**
	 * Returns the raw data block
	 * @return
	 */
	public PamRawDataBlock findRawDataBlock() {
		return (PamController.getInstance().getRawDataBlock(clickParameters.getRawDataSource()));
	}


	/**
	 * Get the click train detector. 
	 * @return the clickTrainDetector
	 */
	public ClickTrainDetector getClickTrainDetector() {
		return clickTrainDetector;
	}

	/**
	 * Get the GUI for the PAMControlled unit. This has multiple GUI options 
	 * which are instantiated depending on the view type. 
	 * @param flag. The GUI type flag defined in PAMGuiManager. 
	 * @return the GUI for the PamControlledUnit unit. 
	 */
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (clickGUIFX ==null) {
				clickGUIFX= new ClickControlGUI(this);
			}
			return clickGUIFX;
		}
		if (flag==PamGUIManager.SWING) {
			if (clickGUISwing ==null) {
				clickGUISwing= new WrapperControlledGUISwing(this);	
			}
			return clickGUISwing;
		}
		return null;
	}

	/**
	 * 
	 * @return a master data organiser. 
	 */
	public FFTDataOrganiser getFFTDataOrganiser() {
		/**
		 * Do we need to worry about synchronisation if multiple processes 
		 * use the same organiser in different threads ? 
		 */
		if (clickFFTDataOrganiser == null) {
			clickFFTDataOrganiser = new ClickFFTOrganiser(this);
		}
		return clickFFTDataOrganiser;
	}


}
