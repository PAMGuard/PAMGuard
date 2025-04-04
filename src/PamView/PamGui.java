/*	PAMGuard - Passive Acoustic Monitoring GUARDianship.
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
package PamView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import Acquisition.DaqSystemInterface;
import Array.ArrayManager;
//import Logging.LogDataObserver;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamguardVersionInfo;
import PamController.settings.SettingsImport;
import PamModel.AboutPluginDisplay;
import PamModel.CommonPluginInterface;
import PamModel.PamModel;
import PamModel.PamModuleInfo;
import PamModel.PamPluginInterface;
import PamModel.SMRUDevFunctions;
import PamUtils.PamCalendar;
import PamUtils.Splash;
import PamUtils.time.CalendarControl;
import PamView.dialog.warn.WarnOnce;
import PamView.help.PamHelp;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamTabbedPane;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamguardMVC.datakeeper.DataKeeper;
import annotation.tasks.AnnotationManager;
import metadata.MetaDataContol;
import performanceTests.PerformanceDialog;
import tipOfTheDay.TipOfTheDayManager;
/**
 * @author Doug Gillespie
 * 
 * <p>
 * Simple PamGui implementing a tab control.
 * 
 */
public class PamGui extends PamView implements WindowListener, PamSettings {

	boolean gpsSimActive = true;
	private JPanel mainPanel;
	private PamTabbedPane mainTab;
	private HidingSidePanel sidePanel;
	//	private JScrollPane scrollingSidePanel;
	private boolean initializationComplete = false;
//	private boolean outputToScreen = true;  // output is currently directed to console screen - Michael Oswald 9/18/10
//	private JMenuItem openLogFileFolder;  // menu item - define here so we can change text in actionlistener - Michael Oswald 9/19/10
	private JMenuItem toggleLogFile;	 // menu item - define here so we can change text in actionlistener
//	private PrintStream origStream = System.out;  // keep track of default output - Michael Oswald 9/18/10
//	private String outputFileString = "PamguardLog.txt";       // filename used when outputting to file - Michael Oswald 9/19/10
	protected GuiParameters guiParameters = new GuiParameters();
	private TopToolBar topToolbar;

	private MenuItemEnabler startMenuEnabler, stopMenuEnabler, addModuleEnabler, removeModuleEnabler,
	orderModulesEnabler;
	
	/**
	 * Outer layered pane which allows things to be added the GUI. 
	 */
	private JLayeredPane layeredPane;
	private PamController pamController;

	public PamGui(PamController pamControllerInterface, 
			PamModel pamModelInterface, int frameNumber)
	{
		super(pamControllerInterface, pamModelInterface, frameNumber);
		
		this.pamController = pamControllerInterface;

		startMenuEnabler = new MenuItemEnabler();
		stopMenuEnabler = new MenuItemEnabler();
		stopMenuEnabler.enableItems(false);
		addModuleEnabler = new MenuItemEnabler();
		removeModuleEnabler = new MenuItemEnabler();
		orderModulesEnabler = new MenuItemEnabler();

		frame = new MainFrame(getModeName());

		if (FullScreen.isGoFullScreen()) {
			//			frame.setUndecorated(true);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					goFullScreen();
				}
			});
		}

		initializationComplete = PamController.getInstance().isInitializationComplete();

		//		frame.setJMenuBar(new JMenuItem("Configuring PAMGuard please be patient ..."));
		if (getFrameNumber() == 0) {
			frame.setJMenuBar(getDummyMenuBar());
		}

		frame.setSize(800,500);

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		String iconLoc;
		if (System.getProperty("os.name").startsWith("Mac")) {
			iconLoc = PamIcon.getPAMGuardIconPath(PamIcon.NORMAL);
		}
		else {
			iconLoc = PamIcon.getPAMGuardIconPath(PamIcon.OLD);
		}
//		switch (PamController.getInstance().getRunMode()){
//		case(PamController.RUN_NETWORKRECEIVER):iconLoc=PamIcon.getPAMGuardIconPath(PamIcon.NORMAL);break;
//		case(PamController.RUN_PAMVIEW):iconLoc=PamIcon.getPAMGuardIconPath(PamIcon.NORMAL);break;
//		case(PamController.RUN_MIXEDMODE):iconLoc=PamIcon.getPAMGuardIconPath(PamIcon.NORMAL);break;
//		default:iconLoc=PamIcon.getPAMGuardIconPath(PamIcon.LARGE);
//		}

		frame.setIconImage(new ImageIcon(ClassLoader
				.getSystemResource(iconLoc)).getImage());

		frame.setExtendedState(Frame.MAXIMIZED_BOTH);

		frame.addWindowListener(this);
		

		mainPanel = new PamBorderPanel(new BorderLayout());
		mainPanel.setOpaque(true);
		mainPanel.addComponentListener(new GUIComponentListener());

		JPanel centralPanel = new PamBorderPanel(new BorderLayout());
		centralPanel.setOpaque(true);

		sidePanel = new HidingSidePanel(this);

		mainTab = new PamTabbedPane(pamControllerInterface, this);
		
				
		//		mainTab.setForeground(Color.BLUE);

		centralPanel.add(BorderLayout.CENTER, mainTab);
		//		JPanel outerSidePanel = new PamBorderPanel();
		//		outerSidePanel.setLayout(new BorderLayout());
		//		outerSidePanel.add(BorderLayout.NORTH, sidePanel.getSidePanel());
		//		scrollingSidePanel = new PamScrollPane(outerSidePanel);
		//		scrollingSidePanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//		scrollingSidePanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		centralPanel.add(BorderLayout.WEST, sidePanel.getSidePanel());

		mainPanel.add(BorderLayout.CENTER, centralPanel);
		if (getFrameNumber() == 0) {
			mainPanel.add(BorderLayout.SOUTH, PamStatusBar.getStatusBar().getToolBar());
		}
		mainPanel.add(BorderLayout.NORTH, topToolbar = new TopToolBar(this));

		mainTab.addChangeListener(new tabListener());

		frame.setContentPane(mainPanel);


		PamSettingManager.getInstance().registerSettings(this);

		if (guiParameters.bounds != null) {
//			/* 
//			 * now need to check that the frame is visible on the
//			 * current screen - a pain when psf files are sent between
//			 * users, or when I work on two screens at work and then one
//			 * at home !
//			 */

			Point topCorner = guiParameters.bounds.getLocation();
			boolean posOK = true;
			try {
				posOK = ScreenSize.isPointOnScreen(topCorner);
				// some weird stuff going down whereby the screen position is
				// given as -8 from the corner where it really it. this can return 
				// false and then push the display onto a different monitor, so alow for this. 
				if (!posOK) {
					topCorner.x += 10;
					topCorner.y += 10;
					posOK = ScreenSize.isPointOnScreen(topCorner);
				}
			} catch (Exception e) {
			}
			if (!posOK) {
				// put it in the top corner of the main screen. 
				guiParameters.bounds.x = guiParameters.bounds.y = 10;
			}

			frame.setBounds(guiParameters.bounds);

		}

		sidePanel.showPanel(!guiParameters.hideSidePanel);

		frame.setExtendedState(guiParameters.extendedState);

		frame.setVisible(true);
				
		hideToolTips(guiParameters.isHideAllToolTips());


		ToolTipManager.sharedInstance().setDismissDelay(20000);
//		ToolTipManager.sharedInstance().setReshowDelay(5000);
//		System.out.println("Tool tip : getReshowDelay" + ToolTipManager.sharedInstance().getReshowDelay());
//		System.out.println("Tool tip : getInitialDelay" + ToolTipManager.sharedInstance().getInitialDelay());
//		System.out.println("Tool tip : getDismissDelay" + ToolTipManager.sharedInstance().getDismissDelay());

		somethingShowing = true;
	}

	private class MainFrame extends JFrame implements KeyEventDispatcher {

		private static final long serialVersionUID = 1L;

		private Timer noTipTimer;
				
		public MainFrame(String title) throws HeadlessException {
			super(title);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
			noTipTimer = new Timer(6000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					hideToolTips(guiParameters.isHideAllToolTips());
					noTipTimer.stop();
				}
			});
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				if (noTipTimer.isRunning()) {
					noTipTimer.restart();
				}
				else {
					noTipTimer.start();
				}
				hideToolTips(true);
			}
			return false;
		}
		
	}

	private static volatile boolean somethingShowing = false;

	/**
	 * Static flag to say that at least one GUI has opened. 
	 * @return true when one or more GUI frames are visible. 
	 */
	public static boolean isSomethingShowing() {
		return somethingShowing;
	}
	/**
	 * Get the virtual bounds of all graphics screens. In a single
	 * screen environment, this should be the dimensions of the screen.
	 * In a multi-screen environment, it will be a rectangle that 
	 * encompasses many screens.  
	 * @return rectangle encompassing all screens (note may have gaps in 
	 * corners if screens are different sizes).
//	 */
	//	private Rectangle getScreenBounds() {
	//		if (virtualBounds == null) {
	//			virtualBounds = new Rectangle();
	//			GraphicsEnvironment ge = GraphicsEnvironment.
	//			getLocalGraphicsEnvironment();
	//			GraphicsDevice[] gs =
	//				ge.getScreenDevices();
	//			for (int j = 0; j < gs.length; j++) { 
	//				GraphicsDevice gd = gs[j];
	//				GraphicsConfiguration[] gc =
	//					gd.getConfigurations();
	//				for (int i=0; i < gc.length; i++) {
	//					virtualBounds =
	//						virtualBounds.union(gc[i].getBounds());
	//				}
	//			} 
	//		}
	//		return virtualBounds;
	//	}
	/**
	 * Static for getScreenBounds. 
	 */
	private static Rectangle virtualBounds;

	private String getModeName() {
		int runMode = PamController.getInstance().getRunMode();
		switch (runMode) {
		case PamController.RUN_NORMAL:
			return "PAMGuard";
		case PamController.RUN_PAMVIEW:
			return "PAMGuard - Viewer";
		case PamController.RUN_MIXEDMODE:
			return "PAMGuard - Mixed mode Offline Analysis";
		}
		return "PAMGuard";
	}
	/**
	 * Makes a dummy menu bar with some text in it
	 * which is displayed as PAMGuard is first starting up
	 * @return dummy Menu.
	 */
	private JMenuBar getDummyMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new JMenu("Configuring PAMGuard please be patient while modules are loaded ..."));
		return menuBar;
	}

	class GUIComponentListener extends ComponentAdapter {

		@Override
		public void componentResized(ComponentEvent e) {

			if (getFrameNumber() == 0) {
				PamStatusBar.getStatusBar().resize();
			}
			mainPanel.invalidate();
		}

	}

	@Override
	public void addControlledUnit(PamControlledUnit unit) {
		if (unit.getFrameNumber() != this.getFrameNumber()) {
			return;
		}
		unit.setPamView(this);
		if (unit.getTabPanel() != null){
			mainTab.addTab(unit.getUnitName(), null, unit.getTabPanel().getPanel(), getTabTipText());
			if (mainTab.getTabCount() == 1) {
				mainTab.setSelectedIndex(0);
			}
			//set the size of the tab
			decorateTab(mainTab.getTabCount()-1, guiParameters.tabSize); 
		}
		if (unit.getSidePanel() != null) sidePanel.add(unit.getSidePanel().getPanel());
		int tabHeight = 0;
		if (mainTab.getComponentCount() > 0) {
			if (mainTab.isShowing() && mainTab.getComponent(0).isShowing()) {
				tabHeight = mainTab.getComponent(0).getLocationOnScreen().y - mainTab.getLocationOnScreen().y;
			}
		}
		if (sidePanel != null) {
			//			scrollingSidePanel.setBorder(new EmptyBorder(tabHeight, 0, 0, 0));
		}
		//		showSidePanel();
		if (initializationComplete) {
			ShowTabSpecificSettings();
		}
	}

	@Override
	public void setTitle(String title) {
		frame.setTitle(title);
	}
	private String getTabTipText() {
		return "Right click for more options";
	}

	@Override
	public void removeControlledUnit(PamControlledUnit unit) {
		unit.setPamView(null);
		if (unit.getTabPanel() != null) mainTab.remove(unit.getTabPanel().getPanel());
		if (unit.getSidePanel() != null) sidePanel.remove(unit.getSidePanel().getPanel());
		//		showSidePanel();
		ShowTabSpecificSettings();
	}

	@Override
	public void renameControlledUnit(PamControlledUnit unit) {
		if (unit.getTabPanel() != null) {
			int tabIndex = mainTab.indexOfComponent(unit.getTabPanel().getPanel());
			if (tabIndex >= 0) {
				mainTab.setTitleAt(tabIndex, unit.getUnitName());
			}
		}
	}

	class tabListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e) {
			if (initializationComplete) {
				guiParameters.selectedTab = mainTab.getSelectedIndex();
				if (guiParameters.selectedTab >= 0) {
					guiParameters.setCurrentSelectedTab(mainTab.getTitleAt(guiParameters.selectedTab));
				}
			}
			ShowTabSpecificSettings();

		}	
	}

	public PamControlledUnit findControlledUnit(int tabNo)
	{
		if (mainTab == null) return null;
		if (tabNo < 0 || tabNo >= mainTab.getComponentCount()) return null;
		Component tabComponent = mainTab.getComponentAt(tabNo);
		//System.out.println("Comp: "+tabNo);
		PamControlledUnit unit;
		for (int i = 0; i < pamControllerInterface.getNumControlledUnits(); i++) {
			unit = pamControllerInterface.getControlledUnit(i);
			if (unit.getTabPanel() != null && tabComponent == unit.getTabPanel().getPanel()){ 
				return unit;}
		}

		return null;
	}

	/**
	 * Set up specific settings for the tab - get's called quite a lot, 
	 * including whenever modules are added or removed in order to 
	 * make sure that menus contain the correct options for 
	 * existing modules. 
	 *
	 */
	public void ShowTabSpecificSettings()
	{   
		if (!initializationComplete) return;
		//System.out.println("Index: "+mainTab.getSelectedIndex());
		//System.out.println("Placement: "+ mainTab.getTabPlacement());

		if (FullScreen.isGoFullScreen()) {
			frame.setJMenuBar(null);
			return;
		}

		PamControlledUnit pamUnit = findControlledUnit(mainTab.getSelectedIndex());
		JMenuBar guiMenuBar;
		topToolbar.setActiveControlledUnit(pamUnit);
		if (pamUnit == null) {
			guiMenuBar = makeGuiMenu();
		}
		else {
			guiMenuBar = pamUnit.getTabSpecificMenuBar(frame, makeGuiMenu(), this);
		}
		//		if (pamUnit.getTabPanel() == null) return;
		//System.out.println(pamUnit.GetUnitName() + " tab activated");
		//if (getFrameNumber() == 0) {
		/*
		 * Before setting a new menu bar, clean up the old
		 * one from the menu item enablers. 
		 */
		MenuItemEnabler.removeMenuBar(frame.getJMenuBar());

		frame.setJMenuBar(guiMenuBar);
		enableMenus();
		//		}

		//JToolBar toolBar = pamUnit.GetTabPanel().GetToolBar();
		//if (toolBar != null) mainPanel.add(toolBar);
	}


	//	MakeGuiMenu()
	/**
	 * Makes a standard GUI menu for the display which will include add ins
	 * taken from the varous PamControlledUnits. 
	 */

	JMenu fileMenu;
	JMenuBar menuBar;
	
	/**
	 * Keep a reference to the hydrophone menu so that the text can change if the medium
	 * changes. 
	 */
	private JMenuItem hydrophoneArrayMenu;

	public JMenuBar makeGuiMenu() {

		//		if (getFrameNumber() > 0) {
		//			return null;
		//		}

		int runMode = PamController.getInstance().getRunMode();

		boolean isViewer = (runMode == PamController.RUN_PAMVIEW);

		menuBar = new JMenuBar();

		JMenuItem menuItem;
		JMenu menu;

		// =======================
		/* File menu */
		fileMenu = new JMenu("File");
		fileMenu.getPopupMenu().setLightWeightPopupEnabled(false);

		if (isViewer) {
			menuItem = new JMenuItem("Save Data");
			menuItem.addActionListener(new MenuDataSave());
			fileMenu.add(menuItem);
			fileMenu.addSeparator();
		}
		/*
		 * Settings load, import and export functions
		 */
		menuItem = new JMenuItem("Save Configuration");
		menuItem.addActionListener(new menuSave());
		fileMenu.add(menuItem);
		if (isViewer) {
			menuItem.setToolTipText("Save configuration to current database");
		}
		else {
			menuItem.setToolTipText("Save configuration to psf file");
		}
		if (isViewer) {
			menuItem = new JMenuItem("Export Configuration  ...");
			menuItem.setToolTipText("Export configuration to a new psfx file");
		}
		else {
			menuItem = new JMenuItem("Save Configuration As ...");
			menuItem.setToolTipText("Save configuration to a new psfx file");
		}
		menuItem.addActionListener(new menuSaveAs());
		fileMenu.add(menuItem);
		
		menuItem = new JMenuItem("Export XML Configuration");
		menuItem.addActionListener(new MenuGeneralXMLExport(getGuiFrame()));
		fileMenu.add(menuItem);

		if (!isViewer) {
			menuItem = new JMenuItem("Load Configuration ...");
			menuItem.addActionListener(new menuLoadSettings());
			fileMenu.add(menuItem);
		}
		else {
			//			if (PamguardVersionInfo.getReleaseType() == PamguardVersionInfo.ReleaseType.SMRU) {
			//				menuItem = new JMenuItem("Export XML OfflineSettings Configuration");
			//				menuItem.setToolTipText("Save configuration to an XML file");
			//				menuItem.addActionListener(new MenuXMLOfflineSettingsExport());
			//				fileMenu.add(menuItem);
			//			}

			menuItem = new JMenuItem("Import Configuration ...");
			menuItem.addActionListener(new menuImportSettings());
			menuItem.setToolTipText("Import a configuration from a psf file");
			fileMenu.add(menuItem);
		}

		//		if (SMRUEnable.isEnable()) {
		menuItem = new JMenuItem("Import PAMGuard Modules");
		menuItem.setToolTipText("Import module settings from a different PAMGuard configuration (psfx files only");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importSettings();
			}
		});
		fileMenu.add(menuItem);
//		}
		SMRUDevFunctions.getDevFuncs().addFileMenuFuncs(fileMenu);

		fileMenu.addSeparator();

		PamController.getInstance().getGlobalMediumManager().addGlobalMediumMenuItems(fileMenu, frame); 
		
		fileMenu.addSeparator();

		fileMenu.add(PamController.getInstance().getGlobalTimeManager().getSwingMenuItem(frame));
		
		fileMenu.addSeparator();

		fileMenu.add(menuItem = PamModuleInfo.getModulesMenu(frame));
		addModuleEnabler.addMenuItem(menuItem);
		fileMenu.add(menuItem = PamModuleInfo.getRemoveMenu());
		removeModuleEnabler.addMenuItem(menuItem);


		menuItem = new JMenuItem("Module Ordering ...");
		menuItem.addActionListener(new menuModuleOrder());
		menuItem.setToolTipText("Change the order of modules in the PAMGuard configuration");
		orderModulesEnabler.addMenuItem(menuItem);
		fileMenu.add(menuItem);

		menuItem = new JMenuItem("Show Object List ...");
		menuItem.addActionListener(new menuShowObjectList());
		menuItem.setToolTipText("Show a list of data and detections currnetly held in memory for each module");
		fileMenu.add(menuItem);

		menuItem = new JMenuItem("Show Data Model ...");
		menuItem.addActionListener(new menuShowObjectDiagram());
		menuItem.setToolTipText("Show a graphical representation of modules and their interconnections");
		fileMenu.add(menuItem);

		if (!isViewer) {
			fileMenu.add(DataKeeper.getInstance().getSwingMenuItem(frame));
		}

		menuItem = new JMenuItem("Multi-Threading ...");
		menuItem.addActionListener(new MenuMultiThreading());
		startMenuEnabler.addMenuItem(menuItem);
		fileMenu.add(menuItem);

		//		if (SMRUEnable.isEnable()) {
		//			menuItem = new JMenuItem("Create Watchdod ...");
		//			menuItem.addActionListener(new CreateWatchDog());
		//			fileMenu.add(menuItem);
		//		}

		fileMenu.addSeparator();

		boolean needSeperator = false;

		menuItem = new JMenuItem("Storage Options ...");
		menuItem.addActionListener(new StorageOptions(getGuiFrame()));
		startMenuEnabler.addMenuItem(menuItem);
		fileMenu.add(menuItem);
		
		if (isViewer) {
		menuItem = new JMenuItem("Export Data ...");
		menuItem.addActionListener(new ExportData(getGuiFrame()));
		startMenuEnabler.addMenuItem(menuItem);
		fileMenu.add(menuItem);
		}

		for (int i = 0; i < pamControllerInterface.getNumControlledUnits(); i++) {

			menuItem = pamControllerInterface.getControlledUnit(i).createFileMenu(frame);

			if (menuItem != null) {
				needSeperator = true;
				fileMenu.add(menuItem);
			}
		}

		JMenuItem annotatorMenu = AnnotationManager.getAnnotationManager().getAutoMenu(frame);
		if (annotatorMenu != null) {
			fileMenu.add(annotatorMenu);
		}

		if (needSeperator) {
			fileMenu.addSeparator();
		}

		if (isViewer) {
			menuItem = new JMenuItem("Save and Exit");
		}
		else {
			menuItem = new JMenuItem("Save Configuration and Exit");
		}
		menuItem.addActionListener(new menuExit());
		fileMenu.add(menuItem);

		menuItem = new JMenuItem("Exit without Save");
		menuItem.addActionListener(new menuExitNoSave());
		fileMenu.add(menuItem);


		menuBar.add(fileMenu);
		//menuBar.addContainerListener(new tempListener());

		//System.out.println("MENU REBUILD");


		// =======================
		/* Detection menu */
		menu = new JMenu("Settings");
		menu.getPopupMenu().setLightWeightPopupEnabled(false);

		boolean needSeparator = false;

		if (runMode == PamController.RUN_NORMAL || runMode == PamController.RUN_MIXEDMODE) {

			//			menuItem = new JMenuItem("Start");
			//			menuItem.addActionListener(new menuPamStart());
			//			startMenuEnabler.addMenuItem(menuItem);
			//			menu.add(menuItem);
			//
			//			menuItem = new JMenuItem("Stop");
			//			menuItem.addActionListener(new menuPamStop());
			//			stopMenuEnabler.addMenuItem(menuItem);
			//			menu.add(menuItem);

			//			menu.addSeparator();
			//
			//			
			//			needSeparator = true;

		}		
		

		if (isViewer){
			//sub menu for hydrophone array and data import functions in viewer mode. 
			JMenu subHydrophoneMenu= new JMenu("Hydrophone array");
			hydrophoneArrayMenu=subHydrophoneMenu;
			subHydrophoneMenu.addMouseListener(new menuArrayListener(menu));
			menuItem = new JMenuItem("Array ...");
			final JMenuItem hMenu =  menuItem;
			menuItem.addActionListener(new menuArray());
			subHydrophoneMenu.add(menuItem);
			menuItem = new JMenuItem("Import Position Data ...");
			subHydrophoneMenu.add(menuItem);
			menuItem.addActionListener(new importArray());
			menuItem = new JMenuItem("Import Streamer Data ...");
			subHydrophoneMenu.add(menuItem);
			menuItem.addActionListener(new importStreamer());
			menu.add(subHydrophoneMenu);
		
		}
		else {
			//in real time mode don't need any data import stuff so just one option to open the hydrophone array dialog. #
			menuItem = new JMenuItem("Hydrophone array ...");
			hydrophoneArrayMenu=menuItem;
			menuItem.addActionListener(new menuArray());
			menu.add(menuItem);
		}
		
		//for changing "hydrophones" to "microphone" and vice versa if medium changes. 
		menu.addMenuListener(new SettingsMenuListener());
		
		menu.add(MetaDataContol.getMetaDataControl().createMenu(frame));
		
		menu.addSeparator();

		// go through the Controllers and see if any have a detection menu to add
		JMenuItem detectorMenu;
		boolean separator = false;
		for (int i = 0; i < pamControllerInterface.getNumControlledUnits(); i++) {
			detectorMenu = pamControllerInterface.getControlledUnit(i).createDetectionMenu(frame);
			if (detectorMenu != null) {
				if (!separator && needSeparator) {
					menu.addSeparator();
					separator = true;
				}
				menu.add(detectorMenu);
			}
		}		
		menuBar.add(menu);		
		
		// =======================
		// DISPLAY MENU
		JMenu displayMenu = null;
		JMenuItem subMenu = null;
		if (displayMenu == null) {
			displayMenu = new JMenu("Display");
		}

		displayMenu.add(MarkRelationships.getInstance().getSwingMenuItem(getGuiFrame()));

		displayMenu.add(PamColors.getInstance().getMenu(getGuiFrame()));

		displayMenu.add(CalendarControl.getInstance().getMenu(getGuiFrame()));

		//		JMenuItem symbolItem = PamOldSymbolManager.getInstance().getMenu(getGuiFrame());
		//		if (symbolItem != null) {
		//			displayMenu.add(symbolItem);
		//		}

		displayMenu.add(PamController.getInstance().getGuiFrameManager().getCollapseMenuItem());

		displayMenu.addSeparator();

		for (int i = 0; i < pamControllerInterface.getNumControlledUnits(); i++) {
			subMenu = pamControllerInterface.getControlledUnit(i).createDisplayMenu(frame);
			if (subMenu != null) {
				displayMenu.add(subMenu);
			}
		}		

		if (displayMenu != null) menuBar.add(displayMenu);


		// =======================
		// HELP MENU
		menu = new JMenu("Help");
		menu.getPopupMenu().setLightWeightPopupEnabled(false);

		//		new PamHelpContentViewerUI()
		//menu.add(PamHelp.getInstance().getMenu());
		//		PamHelp.getInstance();
		menuItem = new JMenuItem("About PAMGuard");
		menuItem.addActionListener(new menuAbout());
		menu.add(menuItem);

		/*
		 * Add menu item to list plugins
		 * Michael Oswald
		 * 2016/09/30
		 */
		List<PamPluginInterface> pluginList = ((PamModel) PamController.getInstance().getModelInterface()).getPluginList();
		List<DaqSystemInterface> daqList = ((PamModel) PamController.getInstance().getModelInterface()).getDaqList();
		if (!pluginList.isEmpty() || !daqList.isEmpty()) {
			JMenu subPluginsMenu= new JMenu("About Plugins");
			//subPluginsMenu.addMouseListener(new menuArrayListener(menu));

			if (!pluginList.isEmpty()) {
				for (int i=0; i<pluginList.size(); i++) {
					menuItem = new JMenuItem(pluginList.get(i).getDefaultName());
					menuItem.addActionListener(new menuPlugin());
					menuItem.setActionCommand(pluginList.get(i).getDefaultName());
					subPluginsMenu.add(menuItem);
				}
			}
			if (!daqList.isEmpty()) {
				for (int i=0; i<daqList.size(); i++) {
					menuItem = new JMenuItem(daqList.get(i).getDefaultName());
					menuItem.addActionListener(new menuPlugin());
					menuItem.setActionCommand(daqList.get(i).getDefaultName());
					subPluginsMenu.add(menuItem);
				}
			}
			menu.add(subPluginsMenu);
		}


		PamHelp.getInstance();
		JMenuItem testHelpMenu = new JMenuItem("Help");
		testHelpMenu.addActionListener(new TestMenuHelpPG() );
		separator = false;
		menu.add(testHelpMenu);


		PamHelp.getInstance();
		JMenuItem tipMenu = new JMenuItem("Tip of the day ...");
		tipMenu.addActionListener(new TipMenu() );
		menu.add(tipMenu);

		menuItem = new JMenuItem("Reset hidden warnings");
		menuItem.setToolTipText("Warnings told \"Not to show again\" will be shown again");
		menuItem.addActionListener(new ClearHiddenWarnings());
		menu.add(menuItem);
		
		JCheckBoxMenuItem hideTips = new JCheckBoxMenuItem("Hide all tool tips", guiParameters.isHideAllToolTips());
		hideTips.setToolTipText("<html>Hide annoying pop-up tool tips which keep getting in the way" +
		"<br>(or press the Escape key to hide them for 6 seconds)</html>");
		hideTips.addActionListener(new HideToolTips(hideTips));
		menu.add(hideTips);

		/*
		 * Add menu item to redirect output to file or console screen
		 *
		 * Michael Oswald
		 * 9/18/10
		 */
		menu.addSeparator();
		JMenu logFileMenu = new JMenu("Log File");
		String logFileName = pamguard.Pamguard.getLogFileName();
		JMenuItem openLogFileFolder = new JMenuItem("Open Folder");
		openLogFileFolder.addActionListener(new OpenFolder());
		logFileMenu.add(openLogFileFolder);
		if (logFileName==null) {
			toggleLogFile = new JMenuItem("Enable Log File");
		} else {
			toggleLogFile = new JMenuItem("Disable Log File");
		}
		toggleLogFile.addActionListener(new ToggleLogFile());
		logFileMenu.add(toggleLogFile);
		menu.add(logFileMenu);

		menu.addSeparator();
		menuItem = new JMenuItem("Set Display Scaling Factor");
		menuItem.addActionListener(new ScalingFactorManager());
		menu.add(menuItem);

		menuItem = new JMenuItem("System performance tests ...");
		menuItem.addActionListener(new PerformanceTests());
		startMenuEnabler.addMenuItem(menuItem);
		menu.add(menuItem);

		menuItem = new JMenuItem("PAMGuard Web site");
		menuItem.addActionListener(new MenuPamguardURL(PamguardVersionInfo.webAddress));
		menu.add(menuItem);

		menuItem = new JMenuItem("Contact and Support");
		menuItem.addActionListener(new MenuPamguardURL("www.pamguard.org/contact.html"));
		menu.add(menuItem);

		menuBar.add(menu);	

		enableMenus();

		return menuBar;
	}
	
	/**
	 * Import settings from an external psf file. 
	 */
	protected void importSettings() {
		SettingsImport importer = new SettingsImport(PamController.getInstance());
		importer.loadSettingsFile();
	}
	
	
	/**
	 * Triggered whenever the settings menu is opened. Used to make sure the the text for the 
	 * reciever is correct depending on the medium. 
	 * @author Jamie Macaulay 
	 *
	 */
	class SettingsMenuListener implements MenuListener {

        @Override
        public void menuSelected(MenuEvent e) {
			//change text whether in air or water
			hydrophoneArrayMenu.setText(PamController.getInstance().getGlobalMediumManager().getRecieverString() + " array...");
//			hMenu.setText(PamController.getInstance().getGlobalMediumManager().getRecieverString() + " array...");
        }

        @Override
        public void menuDeselected(MenuEvent e) {

        }

        @Override
        public void menuCanceled(MenuEvent e) {

        }
	}

	class StorageOptions implements ActionListener {
		private JFrame parentFrame;

		public StorageOptions(JFrame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			PamController.getInstance().storageOptions(parentFrame);
		}
	}
	
	class ExportData implements ActionListener {
		private JFrame parentFrame;

		public ExportData(JFrame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			PamController.getInstance().exportData(parentFrame);
		}
	}
	
	class MenuGeneralXMLExport implements ActionListener {
		private JFrame parentFrame;
		public MenuGeneralXMLExport(JFrame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}
		@Override
		public void actionPerformed(ActionEvent ev){
			PamController.getInstance().exportGeneralXMLSettings(parentFrame, PamCalendar.getTimeInMillis());
		}
	}
	
	//	class MenuXMLOfflineSettingsExport implements ActionListener {
	//		public void actionPerformed(ActionEvent ev){
	//			PamSettingManager.getInstance().saveOfflineSettingsToXMLFile();
	//		}
	//		
	//	}
	class MenuDataSave implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamController.getInstance().saveViewerData();
		}
	}
	
	class menuSave implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamSettingManager.getInstance().saveSettings(frame);
		}
	}
	
	class menuSaveAs implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamSettingManager.getInstance().saveSettingsAs(frame);			
		}
	}
	
	class menuSaveAsXML implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			//			PamSettingManager.getInstance().saveSettingsAsXML(frame);			
		}
	}
	
	class menuLoadSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			//			PamSettingManager.getInstance().loadSettingsFrom(frame);
			/**
			 * 4/3/13 Changed this to stop it attempting to replace the loaded
			 * configuration since this feature never really worked. 
			 */
			String msg = "To load or create a new configuration you should exit and re-start PAMGuard";
			JOptionPane.showMessageDialog(frame, msg, "New configuration", JOptionPane.INFORMATION_MESSAGE, null);
		}
	}
	
	/**
	 * Import a configuration from a psf file during viewer op's
	 * @author Doug Gillespie
	 *
	 */
	class menuImportSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamSettingManager.getInstance().importSettings(frame);
		}
	}
	class menuExportSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamSettingManager.getInstance().exportSettings(frame);
		}
	}
	class TestMenuHelpPG implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamHelp.getInstance().displayHelp();
			//DatabaseController.getInstance().
		}
	}

	class TipMenu implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){	
			TipOfTheDayManager.getInstance().showTip(getGuiFrame(), null);
		}
	}

	/*
	 * Redirect output to either file or screen
	 *
	 * Michael Oswald
	 * 9/18/2010
	 */
	class OpenFolder implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
//			PrintStream newStream = null;
//			/*
//			 * if we're currently outputing to the console screen, redirect to
//			 * a file
//			 */
//			if (outputToScreen) {
//				try {
//					String currentDir = System.getProperty("java.io.tmpdir");
//					File outputFile = new File(currentDir, outputFileString);
//					newStream = new PrintStream(new FileOutputStream(outputFile,true));
//					System.setOut(newStream);
//					System.setErr(newStream);
//					outputToScreen = false;
//					redirectOutputItem.setText("Redirect output to screen");
//					JOptionPane.showMessageDialog(mainPanel,
//							"All information and error messages will now be output to " + outputFile.getAbsolutePath(),
//							"Redirect Output",
//							JOptionPane.INFORMATION_MESSAGE);
//					/*
//					 * if there's a problem redirecting the output, reset the
//					 * output back to the console screen and print an error message
//					 */
//				} catch (Exception ex) {
//					System.setOut(origStream);
//					System.setErr(origStream);
//					System.out.println("Error in output redirection");
//				}
//
//				/*
//				 * if we're currently outputting to the file, redirect to the
//				 * console
//				 */
//			} else {
//				System.setOut(origStream);
//				System.setErr(origStream);
//				outputToScreen = true;
//				redirectOutputItem.setText("Redirect output to file");
//				JOptionPane.showMessageDialog(mainPanel,
//						"All information and error messages will now be output to console screen",
//						"Redirect Output",
//						JOptionPane.INFORMATION_MESSAGE);
//			}
			
			try {
				String name = pamguard.Pamguard.getLogFileName();
				if (name == null) {
					Runtime.getRuntime().exec("explorer.exe " + pamguard.Pamguard.getSettingsFolder());
				} else {
					File theFile = new File(pamguard.Pamguard.getSettingsFolder(), name);
					Runtime.getRuntime().exec("explorer.exe /select," + theFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class ToggleLogFile implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			String logFile = pamguard.Pamguard.getLogFileName();
			if (logFile == null) {
				pamguard.Pamguard.enableLogFile();
				System.out.println("Enabling log file...");
				toggleLogFile.setText("Disable Log File");
			}
			else {
				System.out.println("Disabling log file...");
				pamguard.Pamguard.disableLogFile();
				toggleLogFile.setText("Enable Log File");
			}
		}
	}


	class ScalingFactorManager implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamSettingManager.getInstance().scalingFactorDialog(frame);
		}
	}



	class PerformanceTests implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PerformanceDialog.showDialog(getGuiFrame());
		}
	}

	//	class menuHelpPG implements ActionListener {
	//		public void actionPerformed(ActionEvent ev){
	//			PamHelp.getInstance().displayHelp();
	//			//DatabaseController.getInstance().
	//		}
	//	}

	class menuModuleOrder implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			pamControllerInterface.orderModules(frame);
		}
	}
	
	class menuArray implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			ArrayManager.getArrayManager().showArrayDialog(getGuiFrame());
		}
	}

	class menuArrayListener extends MouseAdapter{

		private JMenu menu;

		public menuArrayListener(JMenu menu){
			this.menu=menu; 
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			menu.setPopupMenuVisible(false);
			ArrayManager.getArrayManager().showArrayDialog(getGuiFrame());
		}

	}

	class importArray implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			ArrayManager.getArrayManager().showHydrophoneImportDialog(getGuiFrame());		}
	}

	class importStreamer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			ArrayManager.getArrayManager().showStreamerImportDialog(getGuiFrame());		}
	}

	class menuShowObjectList implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamObjectList.ShowObjectList(getGuiFrame());
		}
	}

	class menuShowObjectDiagram implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamObjectViewer.Show(getGuiFrame(), pamController.getPamConfiguration());
		}
	}

	class menuOpenLogFile implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamObjectList.ShowObjectList(getGuiFrame());
		}
	}

	class menuExit implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			/*
			 * Really should stop Pam first !
			 */
			prepareToClose(true);

			//			frame.dispose();
			//			System.exit(0);
		}
	}

	class menuExitNoSave implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			prepareToClose(false);

			//			frame.dispose();
			//			System.exit(0);
		}
	}

	private class HideToolTips implements ActionListener {
		
		private JCheckBoxMenuItem hideTipsCheckbox;

		public HideToolTips(JCheckBoxMenuItem hideTips) {
			this.hideTipsCheckbox = hideTips;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean shouldHide = !guiParameters.isHideAllToolTips();
			hideToolTips(shouldHide);
			guiParameters.setHideAllToolTips(shouldHide);
			hideTipsCheckbox.setSelected(shouldHide);
			
		}
	}
	
	/**
	 * hide all tool tips
	 * @param hide
	 */
	private void hideToolTips(boolean hide) {
		ToolTipManager.sharedInstance().setEnabled(!hide);
	}
	
	class ClearHiddenWarnings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			WarnOnce.clearHiddenList(getGuiFrame());
		}
	}

	class menuPamStart implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			pamControllerInterface.manualStart();

		}
	}

	//	class menuViewTimes implements ActionListener {
	//		Frame frame;
	//		public menuViewTimes(Frame frame) {
	//			this.frame = frame;
	//		}
	//		public void actionPerformed(ActionEvent ev){
	//			viewTimes(frame);
	//		}
	//	}

	class menuPamStop implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			pamControllerInterface.manualStop();
			//			enableLoggingMenu();		
		}
	}
	class MenuMultiThreading implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			pamControllerInterface.modelSettings(frame);
			//			enableLoggingMenu();		
		}
	}
	class CreateWatchDog implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			PamController.getInstance().createWatchDog();
		}
	}

	//	private void viewTimes(Frame frame) {
	//		pamControllerInterface.getNewViewTimes(frame);	
	//	}

	/*
	class menuLogStart implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			logDataObserver.setLoggingActive(true);	
			enableLoggingMenu();				
		}
	}

	class menuLogStop implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			logDataObserver.stopLogData();
			enableLoggingMenu();		
		}
	}

	class menuLogOpen implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			logDataObserver.openLogFile();
			enableLoggingMenu();		
		}
	}

	class menuLogClose implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			logDataObserver.closeLogFile();
			enableLoggingMenu();		
		}
	}*/


	/**
	 * Rewrote the menu enablers for the Logging menu (and made it more general
	 * to extend to other menus should they ever need to be enabled). 
	 * 
	 * This arises now that we have multiple main menus on the frame depending on
	 * which tab is being viewed. Since each menu bar has references to different
	 * menus and menu items, we can no longer use the ones set in the constructors for
	 * the menu. Each item is therefore found by name before it's enabled. For now
	 * I'm taking the names out of the reference to the last menu item. 
	 *
	 */


	private void enableMenus()
	{
		int pamStatus = PamController.getInstance().getPamStatus();
		//		enableLoggingMenu();
		startMenuEnabler.enableItems(pamStatus == PamController.PAM_IDLE);
		stopMenuEnabler.enableItems(pamStatus == PamController.PAM_RUNNING);
		addModuleEnabler.enableItems(pamStatus == PamController.PAM_IDLE);
		removeModuleEnabler.enableItems(pamStatus == PamController.PAM_IDLE);
		orderModulesEnabler.enableItems(pamStatus == PamController.PAM_IDLE);
	}
	private void enableLoggingMenu() {
		//		boolean fileOpen = logDataObserver.isLogFileOpened();
		//		boolean loggingActive = logDataObserver.isLoggingActive();
		// no idea what the references are for the loaded menu now so
		// in a right mess and need to search for each item.
		//		JMenuItem item;

		//		logCloseEnabler.enableItems(fileOpen == true && loggingActive == false);
		//
		//		logOpenEnabler.enableItems(fileOpen == false && loggingActive == false);
		//		
		//		logStartEnabler.enableItems(fileOpen == true && loggingActive == false);
		//		
		//		logStopEnabler.enableItems(loggingActive == true);		
	}

	class menuAbout implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			new Splash(30000, PamController.getInstance().getRunMode());
		}
	}

	/**
	 * Listener for the plugin 'about' menu items.
	 *  
	 * @author SCANS
	 *
	 */
	class menuPlugin implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){

			// figure out which plugin was selected.  The plugin default name was saved as the action command
			// when the menu item was created, so loop through the plugin list and the daq list looking for the
			// match.  When found, cast to the generic CommonPluginInterface and call the dialog
			List<PamPluginInterface> pluginList = ((PamModel) PamController.getInstance().getModelInterface()).getPluginList();
			List<DaqSystemInterface> daqList = ((PamModel) PamController.getInstance().getModelInterface()).getDaqList();
			if (!pluginList.isEmpty()) {
				for (int i=0; i<pluginList.size(); i++) {
					if (pluginList.get(i).getDefaultName().equals(ev.getActionCommand())) {
						AboutPluginDisplay.showDialog(frame, ((CommonPluginInterface) pluginList.get(i)));
					}
				}
			}
			if (!daqList.isEmpty()) {
				for (int i=0; i<daqList.size(); i++) {
					if (daqList.get(i).getDefaultName().equals(ev.getActionCommand())) {
						AboutPluginDisplay.showDialog(frame, ((CommonPluginInterface) daqList.get(i)));
					}
				}
			}
		}
	}

	class menuCoreHelp implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			//System.out.println("PamGui: launch core pamguard help here");
		}
	}

	class MenuPamguardURL implements ActionListener {
		String webAddr;
		public MenuPamguardURL(String webAddr) {
			super();
			this.webAddr = webAddr;
		}
		@Override
		public void actionPerformed(ActionEvent ev){
			URL url = null;
			String fullAddr = "http://" + webAddr; 
			//			System.out.println(fullAddr);
			openURL(fullAddr);
		}
	}
	public static void openURL(String urlString) { 
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url == null) {
			return;
		}
		try {
			Desktop.getDesktop().browse(url.toURI());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see PamView.PamViewInterface#PamEnded()
	 */
	@Override
	public void pamEnded() {

		enableMenus();

	}

	/* (non-Javadoc)
	 * @see PamView.PamViewInterface#PamStarted()
	 */
	@Override
	public void pamStarted() {

		enableMenus();

	}


	/* (non-Javadoc)
	 * @see PamView.PamViewInterface#ModelChanged()
	 */
	@Override
	public void modelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initializationComplete = true;
			setSelectedTab();
			ShowTabSpecificSettings();
			break;
		case PamControllerInterface.REORDER_CONTROLLEDUNITS:
			changeUnitOrder();
			ShowTabSpecificSettings();
			break;
		case PamControllerInterface.DESTROY_EVERYTHING:
			this.mainPanel.removeAll();
			frame.removeAll();
			frame.setVisible(false);
			break;	
		case PamControllerInterface.CHANGED_DISPLAY_SETTINGS:
			ShowTabSpecificSettings();
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (initializationComplete) {
				ShowTabSpecificSettings();
			}
			break;
		}
	}

	/*
	 * during the startup phase, this may be the tab we were looking
	 * at last time PamGuard ran - so set the tab control
	 * to be looking at this one
	 */
	private void setSelectedTab() {
		if (guiParameters == null) return;
		String tabName = guiParameters.getCurrentSelectedTab();
		int tabIndex = findTabByName(tabName);
		if (tabIndex < 0) {
			tabIndex = guiParameters.selectedTab;
		}
		if (mainTab.getTabCount() > tabIndex) {
			mainTab.setSelectedIndex(tabIndex);
		}

	}

	/**
	 * Get a tab index by name. 
	 * @param tabName Name of tab
	 * @return index or -1 if not found. 
	 */
	private int findTabByName(String tabName) {
		if (mainTab == null) return -1;
		for (int i = 0; i< mainTab.getTabCount(); i++) {
			String tabTit = mainTab.getTitleAt(i);
			if (tabTit != null) {
				if (tabTit.equals(tabName)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	
	/**
	 * Set all tab sizes to a new dimension
	 * @param dimension - the new tab size to set. 
	 * @param font - the tab font to set. 
	 */
	public int setTabsSize(Dimension dimension) {
		this.guiParameters.tabSize=dimension; 
		if (mainTab == null) return -1;
		for (int i = 0; i< mainTab.getTabCount(); i++) {
			String tabTit = mainTab.getTitleAt(i);
			if (tabTit != null) {
				decorateTab(i, dimension);
			}
		}
		return -1;
	}
	
	/**
	 * Set font for the main tab panel. 
	 * @param font - the font to set. 
	 */
	public void setTabFont(Font font) {
		mainTab.setFont(font);
	}

	/**
	 * Set the size of a tab
	 * @param tabIndex - the tab index (tab should exist already)
	 * @param dimension - the dimension to set. 
	 */
	private void decorateTab(int tabIndex, Dimension dimension) {
		JLabel lab = new JLabel(mainTab.getTitleAt(tabIndex), SwingConstants.CENTER);
		if (dimension!=null) lab.setPreferredSize(dimension); //not setting preferred size should return to default. 
		mainTab.setTabComponentAt(tabIndex, lab);  // tab index, jLabel
	}
	

	private void changeUnitOrder() {
		changeTabOrder();
		changeSidePanelOrder();
	}

	private void changeTabOrder() {
		PamControlledUnit unit;
		int tabNumber = 0;
		int currentTab;
		Component currentComponent = mainTab.getSelectedComponent();
		for (int i = 0; i < pamControllerInterface.getNumControlledUnits(); i++) {
			unit = pamControllerInterface.getControlledUnit(i);
			if (unit.getTabPanel() != null) {
				currentTab = mainTab.indexOfComponent(unit.getTabPanel().getPanel());
				if (currentTab >= 0) {
					mainTab.remove(currentTab);
					mainTab.insertTab(unit.getUnitName(), null, unit.getTabPanel().getPanel(), 
							getTabTipText(), tabNumber);
					tabNumber++;
				}
			}
		}
		if (currentComponent != null) {
			mainTab.setSelectedComponent(currentComponent);
		}
	}

	private void changeSidePanelOrder() {
		PamControlledUnit unit;
		sidePanel.removeAll();
		for (int i = 0; i < pamControllerInterface.getNumControlledUnits(); i++) {
			unit = pamControllerInterface.getControlledUnit(i);
			if (unit.getSidePanel() != null) {
				sidePanel.add(unit.getSidePanel().getPanel());
			}
		}
	}

	//	private void showSidePanel() {
	//		sidePanel.setVisible(sidePanel.getComponentCount() > 0);
	//	}

	/**
	 * Implementation of WindowListener
	 */
	@Override
	public void windowActivated(WindowEvent e) {

	}
	@Override
	public void windowClosing(WindowEvent e) {
		/*
		 * Window closing action will now depend on which frame 
		 * this is. If it's the main frame (getFrameNumber() == 0)
		 * then exit if not running, or check to see if user wants to stop
		 * 
		 * If it's any other frame, then move all the tabs back to the 
		 * main frame and close normally. 
		 */
		if (getFrameNumber() == 0) {
			prepareToClose();
		}
		else {
			closeExtraFrame();
		}
	}
	@Override
	public void windowOpened(WindowEvent e) {
		PamColors.getInstance().setColors();
	}
	@Override
	public void windowIconified(WindowEvent e) {

	}
	@Override
	public void windowDeiconified(WindowEvent e) {

	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		//		PamSettingManager.getInstance().SaveSettings();

	}
	@Override
	public void windowClosed(WindowEvent e) {
		//		PamSettingManager.getInstance().SaveSettings();

	}

	/**
	 * Get the GUi parameters before saving so that these can 
	 * be written to the psf file, even if the Window was previouslu
	 * closed. 
	 */
	protected void getGuiParameters() {
		guiParameters.extendedState = frame.getExtendedState();
		guiParameters.state = frame.getState();
//		if (guiParameters.state != Frame.MAXIMIZED_BOTH) {
			guiParameters.size = frame.getSize();
			guiParameters.bounds = frame.getBounds();
//		}
	}

	/**
	 * Original method.  Instead of calling this we should call prepareToClose(boolean) to
	 * tell Pamguard specifically whether or not to save the psf files.  For now, to maintain the
	 * the legacy functionality which always saved the psf, any calls to here will be sent
	 * as prepareToClose(true).
	 * 
	 * @return true if ok to close, false otherwise. 
	 */
	private boolean prepareToClose() {
		return(prepareToClose(true));
	}


	/**
	 * 
	 * this get's called whenever the main window closes - 
	 * ideally, I'd like to stop this happening when the system
	 * is running, but since that's not possible, just make sure
	 * everything has stopped.
	 * 
	 * @param weShouldSave true if the settings should get saved to the psf file, false if otherwise
	 * @return true if ok to close, false otherwise. 
	 */
	private boolean prepareToClose(boolean weShouldSave) {
		//		System.out.println("Preparing to close PAMGuard");

		PamController pamController = PamController.getInstance();

		if (!pamController.canClose()) {
			return false;
		}

		int pamStatus = pamController.getPamStatus();
		if (pamStatus != PamController.PAM_IDLE) {
			int ans = JOptionPane.showConfirmDialog(frame,  
					"Are you sure you want to stop and exit",
					"PAMGuard is busy",
					JOptionPane.YES_NO_OPTION);
			if (ans == JOptionPane.NO_OPTION) {
				return false;
			}
		}

		/*
		 * Sometimes have trouble stopping in which case we're 
		 * going to have to bomb. If it can't stop in 5s, then die ! 
		 */
		BombThread bombThread = new BombThread();
		Thread t = new Thread(bombThread);
		t.start();
		for (int i = 6; i >= 0 && bombThread.waitingStop; i--) {
			for (int j = 0; j<10 && bombThread.waitingStop; j++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			if (!bombThread.waitingStop) break;
			System.out.println(String.format("Stopping PAMGuard. Will force exit in %d seconds", i));
		}


		//		pamControllerInterface.pamClose();

		pamControllerInterface.getGuiFrameManager().getAllFrameParameters();

		if (pamController.getRunMode() == PamController.RUN_PAMVIEW) {
			pamController.saveViewerData();
		}

		// deal with anything that needs sorting out in the realm of UID's. 
		// move this to pamController.pamClose()
//		pamController.getUidManager().runShutDownOps();

		// if the user doesn't want to save the config file, make sure they know
		// that they'll lose any changes to the settings
		if (!weShouldSave) {
			int ans = JOptionPane.showOptionDialog(frame,  
					"<html><body><p style='width: 300px;'>Are you sure you want to exit without saving your current configuration?  Any changes that have been made to the current configuration will be lost</p></body></html>",
					"Exit without Save",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					new String[]{"Exit without Save","Cancel"},
					null);
			if (ans == JOptionPane.NO_OPTION) {	// Hitting Cancel returns a No value
				return false;
			}
		}

		// finally save all settings just before PAMGuard closes. 
		if (weShouldSave) {
			PamSettingManager.getInstance().saveFinalSettings();
		}

		pamControllerInterface.pamClose();

		// shut down the JavaFX thread and the JVM
		pamController.shutDownPamguard();

		return true;
	}

	class BombThread implements Runnable {

		volatile boolean waitingStop = true;
		@Override
		public void run() {
			pamControllerInterface.pamStop();
			waitingStop = false;
		}

	}


	/**
	 * Get's called if an extra frame is closed. 
	 * Moves all tabs back to the main frame then 
	 * closes itself. 
	 */
	private void closeExtraFrame() {

		getGuiParameters();

		pamControllerInterface.getGuiFrameManager().closeExtraFrame(this);
	}

	public void showDialog(String s1, String s2, int dialogType) 
	{ 	// custom title, error icon
		JOptionPane.showMessageDialog(frame,
				"Eggs aren't supposed to be green.",
				"Inane error",
				JOptionPane.ERROR_MESSAGE);


		// JOptionPane.showMessageDialog(frame,
		// s1, s2, dialogType
	}


	@Override
	public void showControlledUnit(PamControlledUnit pamControlledUnit) {
		PamTabPanel pamTabPanel = pamControlledUnit.getTabPanel();
		if (pamTabPanel == null) return;
		mainTab.setSelectedComponent(pamTabPanel.getPanel());		
	}

	@Override
	public String getViewName(){
		return "pamGui";
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsReference()
	 */
	@Override
	public Serializable getSettingsReference() {
		return guiParameters;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsVersion()
	 */
	@Override
	public long getSettingsVersion() {
		return GuiParameters.serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitName()
	 */
	@Override
	public String getUnitName() {
		if (getFrameNumber() == 0) {
			return "PamGUI";
		}
		else {
			return String.format("PamGUI Frame %d", getFrameNumber());
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitType()
	 */
	@Override
	public String getUnitType() {
		return "PamGUI";
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#restoreSettings(PamController.PamControlledUnitSettings)
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		guiParameters = ((GuiParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	public PamTabbedPane getMainTab() {
		return mainTab;
	}

	BusyLayeredPane busyPane;
	private Point normalLocation;
	private Dimension normalSize;
	@Override
	public void enableGUIControl(boolean enable) {
		//		if (enable) {
		//			frame.getLayeredPane().remove(busyPane);
		////			busyPane.di
		//		}
		//		else {
		//			busyPane = new BusyLayeredPane(frame.getLayeredPane());
		//			frame.add(busyPane);
		//		}
	}

	/**
	 *find the frame owning the given component and pack it. 
	 * @param component
	 */
	public static void packFrame(JComponent component) {
		Container c = component; 
		component.revalidate();
		while (c != null) {
			c = c.getParent();
			if (c instanceof JComponent) {
				c.invalidate();
			}
			if (c instanceof Window) {
				((Window) c).pack();
				break;
			}
		}

	}

	/**
	 * Toggle between full screen and not full screen. 
	 */
	public void toggleFullScreen() {
		//		if (FullScreen.isGoFullScreen()) {
		//			FullScreen.setGoFullScreen(false);
		//			stopFullScreen();
		//		}
		//		else {
		//			FullScreen.setGoFullScreen(true);
		//			goFullScreen();
		//		}
	}
	private void goFullScreen() {
		JFrame guiFrame = getGuiFrame();
		if (guiFrame == null) {
			return;
		}
		guiFrame.setJMenuBar(null);
		normalLocation = guiFrame.getLocation();
		normalSize = guiFrame.getSize();
		//		guiFrame.setAlwaysOnTop(true);
		guiFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		//		guiFrame.setVisible(false);
		//		guiFrame.setUndecorated(true);
		//		guiFrame.setVisible(true);
	}

	private void stopFullScreen() {
		JFrame guiFrame = getGuiFrame();
		if (guiFrame == null) {
			return;
		}
		ShowTabSpecificSettings();
		if (normalLocation != null) {
		}
		if (normalSize != null && normalSize.getHeight() > 100 && normalSize.getWidth() > 100 && normalLocation != null) {
			//			guiFrame.setLocation(normalLocation);
			//			guiFrame.setSize(normalSize);
		}
		guiFrame.setAlwaysOnTop(false);
		guiFrame.setExtendedState(Frame.NORMAL);
		//		guiFrame.setUndecorated(false);
	}

	/**
	 * Set whether the top toolbar (with the record/viewer play buttons) is visible or not
	 * @param useToolBar - true to have it visible (default)
	 */
	public void setToolBarVisible(boolean useToolBar) {
		mainPanel.remove(topToolbar);
		if (useToolBar) {
			mainPanel.add(BorderLayout.NORTH, topToolbar);
		}
		mainPanel.validate();
		mainPanel.repaint();
	}

	/**
	 * Get the main panel, this contains all components within the frame. 
	 * @return the main panel. 
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	/**
	 * Get the side panel. This holds all the side pane.s 
	 * @return the side panel. 
	 */
	public HidingSidePanel getSidePanel() {
		return sidePanel;
	}
	
	/**
	 * Get the main tab pane. 
	 * @return the tab pane. 
	 */
	public PamTabbedPane getTabbedPane() {
		return this.mainTab;
	}
	
	/**
	 * find a parent window for a JComponent. This can be useful in 
	 * finding windows to open child dialogs when the object holding 
	 * the component may not have a direct reference back to it's dialog. 
	 * @param component any Swing component
	 * @return parent Window (or frame) if it can be found
	 */
	public static Window findComponentWindow(JComponent component) {
		if (component == null) {
			return null;
		}
		JRootPane root = component.getRootPane();
		if (root == null) {
			return null;
		}
		Container rootP = root.getParent();
		if (rootP instanceof Window) {
			return (Window) rootP;
		}
		else {
			return null;
		}
	}


}