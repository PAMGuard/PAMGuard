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
package PamController;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import Acquisition.AcquisitionProcess;

//import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import pamScrollSystem.AbstractScrollManager;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.pamTask.PamTaskUpdate;
import pamguard.GlobalArguments;
import pamguard.Pamguard;
import soundPlayback.PlaybackControl;
import warnings.PamWarning;
import warnings.WarningSystem;
import zipUnpacker.ZipUnpacker;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import generalDatabase.DBControlUnit;
import javafx.application.Platform;
import javafx.stage.Stage;
import Array.ArrayManager;
import PamController.command.MultiportController;
import PamController.command.NetworkController;
import PamController.command.TerminalController;
import PamController.command.WatchdogComms;
import PamController.masterReference.MasterReferencePoint;
import PamController.settings.output.xml.PamguardXMLWriter;
import PamController.settings.output.xml.XMLWriterDialog;
import PamController.soundMedium.GlobalMediumManager;
import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamModel.PamModel;
import PamModel.PamModelInterface;
import PamModel.PamModelSettings;
import PamModel.PamModuleInfo;
import PamModel.SMRUEnable;
import PamUtils.PamCalendar;
import PamUtils.time.GlobalTimeManager;
import PamView.GeneralProjector;
import PamView.GuiFrameManager;
import PamView.PamColors;
import PamView.PamView;
import PamView.PamViewInterface;
import PamView.PanelOverlayDraw;
import PamView.TopToolBar;
import PamView.dialog.warn.WarnOnce;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.ThreadedObserver;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.datakeeper.DataKeeper;
import PamguardMVC.uid.UIDManager;
import PamguardMVC.uid.UIDOnlineManager;
import PamguardMVC.uid.UIDViewerManager;
import binaryFileStorage.BinaryStore;
import PamguardMVC.debug.Debug;

/**
 * @author Doug Gillespie
 *         <p>
 *         Main Pam Controller class which will communicate with the
 *         PamModelInterface and with the PamViewInterface
 *         <p>
 *         PamController contains a list of PamControlledUnit's each of which
 *         has it's own process,
		simpleMapRef.gpsTextPanel.setPixelsPerMetre(getPixelsPerMetre()); input and output data and display (Tab Panel,
 *         Menus, etc.)
 * @see PamController.PamControlledUnit
 * @see PamView.PamTabPanel
 * 
 */
public class PamController implements PamControllerInterface, PamSettings {

	// status can depend on the run mode !

	public static final int PAM_IDLE = 0;
	public static final int PAM_RUNNING = 1;
	public static final int PAM_STALLED = 3;
	public static final int PAM_INITIALISING = 4;
	public static final int PAM_STOPPING = 5;

	// status' for RunMode = RUN_PAMVIEW
	public static final int PAM_LOADINGDATA = 2;

	public static final int RUN_NORMAL = 0;
	public static final int RUN_PAMVIEW = 1;
	public static final int RUN_MIXEDMODE = 2;
	public static final int RUN_REMOTE = 3;
	public static final int RUN_NOTHING = 4;
	public static final int RUN_NETWORKRECEIVER = 5;

	private int runMode = RUN_NORMAL;
	
	// flag used in main() to indicate that processing should start immediately. 
	public static final String AUTOSTART = "-autostart";
	// flag used in main() to indicate that pamguard should exit as soon as processing ends. 
	public static final String AUTOEXIT = "-autoexit";

	/**
	 * The pam model. 
	 */
	private PamModel pamModelInterface;

	/**
	 * List of the current controlled units (PAMGuard modules)
	 */
	private ArrayList<PamControlledUnit> pamControlledUnits;

	/**
	 * The current PAM status
	 */
	private transient int pamStatus = PAM_IDLE;

	/**
	 * PamGuard view params. 
	 */
	public PamViewParameters pamViewParameters = new PamViewParameters();

	//	ViewerStatusBar viewerStatusBar;

	/*
	 * Swing GUI manager 
	 */
	private PAMControllerGUI guiFrameManager;

	/**
	 * Indicates whether initialisation has completed
	 */
	private boolean initializationComplete = false;

	/**
	 * The java version being run. e.g. Java 8u111 will be 8.111; 
	 */
	public static double JAVA_VERSION = getVersion ();


	// PAMGUARD CREATION IS LAUNCHED HERE !!!
	//	private static PamControllerInterface anyController = new PamController();
	private static PamController uniqueController;

	private Timer diagnosticTimer;

	private NetworkController networkController;
	private int nNetPrepared;
	private int nNetStarted;
	private int nNetStopped;

	private Timer garbageTimer;

	/**
	 * The UID manager. 
	 */
	private UIDManager uidManager;

	/**
	 * A global time manager to manage corrections to the PC clock
	 * from various sources. 
	 */
	private GlobalTimeManager globalTimeManager;

	/**
	 * A global medium manager which handles the type of medium sound is propogating through. 
	 */
	private GlobalMediumManager globalMediumManager; 

	/**
	 * A reference to the module currently being loaded.  Used by the PamExceptionHandler to
	 * monitor runtime errors that occur during load
	 */
	private static PamControlledUnit unitBeingLoaded=null;


	/**
	 * Folder where Pamguard is installed and running out of.  This string
	 * includes the file separator at the end, or is null if there was a
	 * problem trying to determine the installation folder
	 */
	private String installFolder=null;
	private boolean haveGlobalTimeUpdate;
	private WatchdogComms watchdogComms;
	
	private PamWarning statusWarning = new PamWarning("PAMGuard control", "Status", 0);
	
	/** 
	 * A separate thread that checks all ThreadedObservers to see if they still have
	 * data in their buffers
	 */
	private Thread statusCheckThread;
	private WaitDetectorThread detectorEndThread;


	private PamController(int runMode, Object object) {

		uniqueController = this;

		this.runMode = runMode;

		if (runMode == PamController.RUN_PAMVIEW) {
			uidManager = new UIDViewerManager(this);
		}
		else {
			uidManager = new UIDOnlineManager(this);
		}


		sayMemory();

		globalTimeManager = new GlobalTimeManager(this);
		globalMediumManager = new GlobalMediumManager(this);

		setPamStatus(PAM_IDLE);

		watchdogComms = new WatchdogComms(this);

		if (pamBuoyGlobals.getNetworkControlPort() != null) {
			networkController = new NetworkController(this);
		}
		if (pamBuoyGlobals.getMultiportAddress() != null) {
			new MultiportController(this);
		}

		guiFrameManager = PamGUIManager.createGUI(this, object);
		guiFrameManager.init(); //perform any start up processes for the GUI. 

		// figure out the installation folder
		try {
			File theURL = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			installFolder = theURL.getParentFile().getPath() + File.separator;
		} catch (URISyntaxException e) {
			System.out.println("Error finding installation folder of jar file: " + e.getMessage());
			e.printStackTrace();
			installFolder = null;
		}

		setupPamguard();

		setupGarbageCollector();
		

//		if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
//		}
		
		//		diagnosticTimer = new Timer(1000, new DiagnosticTimer());
		//		diagnosticTimer.start();
	}

	class DiagnosticTimer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			sayMemory();
		}
	}

	private void sayMemory() {
		Runtime r = Runtime.getRuntime();
		System.out.println(String.format("System memory at %s Max %d, Free %d", 
				PamCalendar.formatDateTime(System.currentTimeMillis()), 
				r.maxMemory(), r.freeMemory()));
	}

	/**
	 * Create an instance of the PAMController. 
	 * @param runMode - the run mode
	 */
	public static void create(int runMode) {
		if (uniqueController == null) {
			PamController pamcontroller = new PamController(runMode, null);
			/*
			 *  I don't see any reason not have have this running with the GUI.
			 *  It launches in a new thread, so it should be fine to have 
			 *  additional commands afterwards.  
			 */
			TerminalController tc = new TerminalController(pamcontroller);
			tc.getTerminalCommands();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				uniqueController.creationComplete();
			}
		});
	}
	
	/**
	 * Not to sound God like, but this will be called on the AWT dispatch thread shortly 
	 * after all modules are created, PAMGuard should be fully setup and all modules will 
	 * have recieved INITIALISATION_COMPLETE and should be good to run 
	 */
	private void creationComplete() {
		if (GlobalArguments.getParam(PamController.AUTOSTART) != null) {
			startLater(); // may as well give AWT time to loop it's queue once more
		}
	}
	
	/**
	 * Create an instance of the PAMcController. 
	 * @param runMode - the run mode
 	 * @param object - extra information. Can be null.
	 */
	public static void create(int runMode, Object object) {
		if (uniqueController == null) {
			new PamController(runMode, object);
		}
	}

	/**
	 * Setup the PAMController. 
	 */
	public void setupPamguard() {


		// create the array list to hold multiple views
		pamControlledUnits = new ArrayList<PamControlledUnit>();

		/**
		 * Set Locale to English so that formated writes to text fields
		 * in dialogs use . and not , for the decimal. 
		 */
		Locale.setDefault(Locale.ENGLISH);

		/*
		 * 15/8/07 Changed creation order of model and view. 
		 * Need to be able to create a database pretty early on 
		 * (in the Model) in order to read back settings that 
		 * the GUI may require. 
		 * 
		 */
		// create the model
		pamModelInterface = new PamModel(this);
		pamModelInterface.createPamModel();

		/*
		 * 9 February 2009
		 * Trying to sort out settings file loading. 
		 * Was previously done when the first modules registered itself
		 * with the settings manager. Gets very confusing. Will be much easier 
		 * to load up the settings first, depending on the type of module
		 * and then have them ready when the modules start asking for them.   
		 */
		int loadAns = PamSettingManager.getInstance().loadPAMSettings(runMode);
		
		System.out.println("Pamcontroller: loadPAMSettings: " + loadAns); 

		if (loadAns == PamSettingManager.LOAD_SETTINGS_NEW) {
			//			if (runMode == RUN_PAMVIEW) {
			//				// no model, no gui, so PAMGAURD will simply exit.
			//				String str = String.format("PAMGUARD cannot run in %s mode without a valid database\nPAMGUARD will exit.",
			//						getRunModeName());
			//				str = "You have opened a database in viewer mode that contains no settings\n" +
			//				"Either load settings from the binary store, import a psf settings file or create modules by hand.\n" +
			//				"Press OK to continue or Cancel to exit the viewer";
			//
			//				int ans = JOptionPane.showConfirmDialog(null, str, "PAMGuard viewer", JOptionPane.OK_CANCEL_OPTION);
			//				if (ans == JOptionPane.CANCEL_OPTION) {
			//					System.exit(0);
			//				}
			//			}
			//			else if (loadAns == ){
			//				// normal settings will probably return an error, but it's OK still !
			////				System.exit(0);
			//			}
			//			return;
		}
		else if (loadAns == PamSettingManager.LOAD_SETTINGS_CANCEL) {
			JOptionPane.showMessageDialog(null, "No settings loaded. PAMGuard will exit", "PAMGuard", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}

		if (getRunMode() == RUN_NOTHING) {
			return;
		}

		// get the general settings out of the file immediately.
		//		PamSettingManager.getInstance().loadSettingsFileData();

		/*
		 * prepare to add a database to the model. 
		 * this will then re-read it's settings from the 
		 * settings file - which we dont' want yet !!!!!
		 * But now we have the database, it should be possible to
		 * alter the code that reads in all settings from a selected
		 * file and alter it so it gets them from the db instead.
		 * Then remove this database module immediately
		 * and let Pamguard create a new one based on the settings !
		 */
		//		PamModuleInfo mi = PamModuleInfo.findModuleInfo("generalDatabase.DBControl");
		//		PamControlledUnitSettings dbSettings = PamSettingManager.getInstance().findGeneralSettings(DBControl.getDbUnitType());
		//		if (mi != null) {
		//			addModule(mi, "Temporary Database");	
		//		}

		// Add a note to the putput console for the user to ignore the SLF4J warning (see http://www.slf4j.org/codes.html#StaticLoggerBinder
		// for details).  I spent a few hours trying to get rid of this warning, but without any luck.  If you do a google search
		// there are a lot of forum suggestions on how to fix, but none seemed to work for me.  Added both slf4j-nop and
		// slf4j-simple to dependency list, neither made a difference.  Changed order of dependencies, ran purges and updates,
		// added slf4j-api explicitly, made sure I don't have duplicate bindings, but nothing helped.
		// 
		// Error occurs when PamDataBlock.sortTypeInformation() calls
		// superDetectionClass = GenericTypeResolver.resolveReturnType(method, unitClass); (currently line 397).  I don't want
		// to add the note there because that gets called every time a PamDataBlock is created.  So I add the note here, which
		// is just before the error occurs
		//
		// Oddly enough, this warning DOES NOT occur when running the non-Maven version (Java12 branch).  The dependencies in the
		// classpath are the same as the ones here in Maven, so I don't know what to say.
		System.out.println("");
		System.out.println("Note - ignore the following SLF4J warn/error messages, they are not applicable to this application");
		ArrayManager.getArrayManager(); // create the array manager so that it get's it's settings

		/**
		 * Check for archived files and unpack automatically. 
		 */
		if (runMode == RUN_PAMVIEW && SMRUEnable.isEnable()) {
			ZipUnpacker zipUnpacker = new ZipUnpacker(this);
			zipUnpacker.checkforFiles();
		}
		// create the view

		// Worldwind Map removed 2017/05/05 because the worldwind.jar library duplicates
		// the jsonNode class in jackson-all-1.9.9.jar library, but
		// it isn't the same. While Eclipse can keep it straight and use class info from
		// jackson instead of worldmap, the PamguardCompiler installer
		// seems to want to use the worldmap instead (even though the classes are listed
		// correctly). This causes problems when Pamguard tries to
		// load a database with click events that have an error message in them. It
		// simply stops reading the database when it hits an error message,
		// but does not throw any sort of error itself.
		// If we want to reinstate Worldwind Map, this duplication of code needs to be
		// resolved.
		// if (SMRUEnable.isEnable()) {
		// try {
		// Class.forName("worldWindMap.WorldMapProvider");
		// } catch (ClassNotFoundException e) {
		//// // TODO Auto-generated catch block
		//// e.printStackTrace();
		// }
		// }


		/*
		 * We are running as a remote application, start process straight away!
		 */
		if (PamSettingManager.RUN_REMOTE == false) {
			addView(guiFrameManager.initPrimaryView(this, pamModelInterface)); 
		}

		PamSettingManager.getInstance().registerSettings(this);

		pamModelInterface.startModel();

		setupProcesses();

		//				if (getRunMode() == RUN_PAMVIEW) {
		//					createViewerStatusBar();			
		//					pamControlledUnits.add(new OfflineProcessingControlledUnit("OfflineProcessing"));
		//				}

		/*
		 * We are running as a remote application, start process straight away!
		 */
		if (getRunMode() == RUN_NOTHING) {

		}else if (PamSettingManager.RUN_REMOTE == true) {
			// Initialisation is complete.
			initializationComplete = true;
			notifyModelChanged(PamControllerInterface.INITIALIZATION_COMPLETE);
			System.out.println("Starting Pamguard in REMOTE execution mode.");
			pamStart();
		}else{

			//			if (getRunMode() == RUN_PAMVIEW) {
			//				createViewerStatusBar();
			//			}

			// call before initialisation complete, so that processes can re-do. 
			createAnnotations();

			organiseGUIFrames();

			//sort the frame titles (Swing convenience)
			if (PamGUIManager.isSwing()) sortFrameTitles();

			initializationComplete = true;
			notifyModelChanged(PamControllerInterface.INITIALIZATION_COMPLETE);

			/**
			 * Trigger loading of relationships between markers and mark observers. 
			 * No need to do anything more than call the constructor and 
			 * everything else will happen...
			 */
			MarkRelationships.getInstance();  
		}
		if (getRunMode() == RUN_PAMVIEW) {
			/**
			 * Tell any modules implementing OfflineDataSource to check
			 * their maps. 
			 */
			AWTScheduler.getInstance().scheduleTask(new DataInitialised());
			//			PamControlledUnit pcu;
			//			OfflineDataSource offlineDataSource;
			//			for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			//				pcu = pamControlledUnits.get(iU);
			//				if (OfflineDataSource.class.isAssignableFrom(pcu.getClass())) {
			//					offlineDataSource = (OfflineDataSource) pcu;
			//					offlineDataSource.createOfflineDataMap(null);
			//				}
			//			}

			//			PamSettingManager.getInstance().registerSettings(new ViewTimesSettings());
			//			getNewViewTimes(null);
		}
		uidManager.runStartupChecks();
		
		clearSelectorsAndSymbols();

		
		/**
		 * Debug code for starting PG as soon as it's initialised. 
		 */
		//		SwingUtilities.invokeLater(new Runnable() {	
		//			@Override
		//			public void run() {
		//				pamStart();
		//			}
		//		});
	}
	
	/**
	 * Clear all data selectors and symbol managers. Required since some of these will have loaded as various modules were created, 
	 * but may also require additional data selectors and symbol managers from super detections which were not availble. 
	 * Deleting the lot, will cause them to be recreated as soon as they are next needed. 
	 * Should probably also call these on any call to addModule as well ? 
	 */
	private void clearSelectorsAndSymbols() {		
		DataSelectorCreator.globalClear();
		PamSymbolManager.globalClear();
		
	}

	class DataInitialised implements Runnable {
		@Override
		public void run() {
			notifyModelChanged(PamControllerInterface.INITIALIZE_LOADDATA);	
			// tell all scrollers to reload their data. 
			//			loadViewerData();
		}
	}


	/**
	 * Called when the number of Networked remote stations changes so that the
	 * receiver can make a decision as to what to do in terms of 
	 * preparing detectors, opening files, etc.  
	 * @param timeMilliseconds 
	 * @param nPrepared number of remote stations currently prepared (called just before start)
	 * @param nStarted number of remote stations currently started
	 * @param nStopped number of remote stations currently stopped 
	 */
	public void netReceiveStatus(long timeMilliseconds, int nPrepared, int nStarted, int nStopped) {
		if (this.nNetStarted == 0 && nStarted >= 1) {
			System.out.println("Starting \"processing\" Received network data");
			this.pamStart(true, timeMilliseconds);
		}
		if (this.nNetStarted >= 1 && nStarted == 0) {
			System.out.println("Stopping processing Received network data");
			this.pamStop();
		}

		this.nNetPrepared = nPrepared;
		this.nNetStarted = nStarted;
		this.nNetStopped = nStopped;
	}
	/**
	 * Loop through all controllers and processes and datablocks and set up all 
	 * of their annotations. 
	 */
	private void createAnnotations() {
		PamControlledUnit pcu;
		PamProcess pp;
		PamDataBlock pdb;
		int nPcu, nPp, nPdb;
		nPcu = getNumControlledUnits();
		for (int iPcu = 0; iPcu < nPcu; iPcu++) {
			pcu = getControlledUnit(iPcu);
			nPp = pcu.getNumPamProcesses();
			for (int iPp = 0; iPp < nPp; iPp++) {
				pp = pcu.getPamProcess(iPp);
				// only do top processes (ones which have no source datablock
				if (pp.getSourceDataBlock() == null) {
					pp.createAnnotations(true);
				}
				//				nPdb = pp.getNumOutputDataBlocks();
				//				for (int iPdb = 0; iPdb < nPdb; iPdb++) {
				//					pdb = pp.getOutputDataBlock(iPdb);
				//					pdb.createAnnotations(pp.getSourceDataBlock(), pp);
				//				}
			}
		}

	}

	/**
	 * Organise the GUI frames on start up or after a module was added 
	 * or after the frames menus have changed. 
	 */
	private void organiseGUIFrames() {

	}


	//	private void createViewerStatusBar() {
	//		
	//		viewerStatusBar = new ViewerStatusBar(this);
	//		PamStatusBar.getStatusBar().getToolBar().setLayout(new BorderLayout());
	//		PamStatusBar.getStatusBar().getToolBar().add(BorderLayout.CENTER, 
	//				viewerStatusBar.getStatusBarComponent());
	//	}

	void setupProcesses() {
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).setupControlledUnit();
		}
	}

	/**
	 * Can PAMGUARD shut down. This question is asked in turn to 
	 * every module. Each module should attempt to make sure it can 
	 * answer true, e.g. by closing files, but if any module
	 * returns false, then canClose() will return false;
	 * @return whether it's possible to close PAMGUARD 
	 * without corrupting or losing data. 
	 */
	public boolean canClose() {
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			if (pamControlledUnits.get(i).canClose() == false) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Called after canClose has returned true to finally tell 
	 * all modules that PAMGUARD is definitely closing down.so they
	 * can free any resources, etc.  
	 */
	@Override
	public void pamClose() {

		getUidManager().runShutDownOps();
		
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).pamClose();
		}
	}

	/**
	 * Shut down Pamguard
	 */
	public void shutDownPamguard() {
		// force close the javaFX thread (because it won't close by itself - see Platform.setImplicitExit(false) in constructor
		Platform.exit();
		
		// terminate the JVM
		System.exit(0);
	}

	/**
	 * Go through all data blocks in all modules and tell them to save. 
	 * This has been built into PamProcess and PamDataBlock since we want
	 * it to be easy to override this for specific modules / processes / data blocks. 
	 */
	public void saveViewerData() {
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).saveViewerData();
		}
		// Commit the database. 
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbControl.commitChanges();
		}
	}

	@Override
	public void addControlledUnit(PamControlledUnit controlledUnit) {
		pamControlledUnits.add(controlledUnit);

		guiFrameManager.addControlledUnit(controlledUnit);

		notifyModelChanged(PamControllerInterface.ADD_CONTROLLEDUNIT);
	}

	@Override
	public PamControlledUnit addModule(Frame parentFrame, PamModuleInfo moduleInfo) {
		// first of all we need to get a name for the new module
		//		String question = "Enter a name for the new " + moduleInfo.getDescription();
		//		String newName = JOptionPane.showInputDialog(null, question, 
		//				"New " + moduleInfo.getDescription(), JOptionPane.OK_CANCEL_OPTION);
		//String newName = NewModuleDialog.showDialog(parentFrame ,moduleInfo,null);
		String newName = guiFrameManager.getModuleName(parentFrame, moduleInfo); 

		if (newName == null) return null;
		return addModule(moduleInfo, newName);

	}

	/**
	 * Add a module to the controller. 
	 * @param moduleInfo - the module info i.e. the type of module to add
	 * @param moduleName - the module name. 
	 * @return
	 */
	public PamControlledUnit addModule(PamModuleInfo moduleInfo, String moduleName) {

		//		Comment this section out and replace with code below, to provide custom error handling with PamExceptionHandler
		//		PamControlledUnit pcu = moduleInfo.create(moduleName);
		//		if (pcu == null) return null;
		//		addControlledUnit(pcu);
		//		if (initializationComplete) {
		//			pcu.setupControlledUnit();
		//		}
		//		return pcu;

		// try to load the unit.  If moduleInfo.create returns null, clear the name and exit.  Otherwise, save
		// the name of the module being loaded
		unitBeingLoaded = moduleInfo.create(moduleName);
		if (unitBeingLoaded == null) {
			return null;
		}

		// try to add the unit to the list.  
		// Put this method call in a try/catch, in case the developer hasn't coded
		// the plugin properly.  We need to catch Throwable, not Exception, in order
		// to catch everything (e.g. if one of the abstract methods is missing, java
		// throws AbstractMethodError.  This is an error, not an exception, so if
		// we want to catch it we need to catch Throwable.  Same with a ClassDefNotFoundError,
		// in case the plugin is looking for a class in the Pamguard core that no longer
		// exists).
		// Also check if unitBeingLoaded=null afterwards because this would indicate
		// that the PamExceptionHandler caught a runtime error during the class instantiation and therefore
		// removed the module to prevent further errors.  This could also happen due to incompatibilities between
		// the current version of Pamguard and older plugin modules.
		try {
			addControlledUnit(unitBeingLoaded);
		} catch (Throwable e) {
			e.printStackTrace();
			String title = "Error adding module";
			String msg = "There is an error with the module " + moduleName + ".<p>" +
					"If this is a plug-in, the error may have been caused by an incompatibility between " +
					"it and this version of PAMGuard.  Please check the developer's website " +
					"for help.<p>" +
					"If this is a core Pamguard module, please copy the error message text and email to" +
					"support@pamguard.org.<p>" +
					"This module will not be loaded.";
			String help = null;
			int ans = WarnOnce.showWarning(title, msg, WarnOnce.WARNING_MESSAGE, help, e);
			System.err.println("Exception while loading " +	moduleName);
			this.removeControlledUnt(unitBeingLoaded);
			this.clearLoadedUnit();;
		}
		if (unitBeingLoaded == null) {
			return null;
		}

		// run the unit's setupProcess method.  Again, check if nitBeingLoaded=null afterwards because this would indicate
		// that the PamExceptionHandler caught a runtime error during the class instantiation and therefore
		// removed the module to prevent further errors.  This could happen due to incompatibilities between
		// the current version of Pamguard and older plugin modules.
		if (initializationComplete) {
			unitBeingLoaded.setupControlledUnit();
		}
		if (unitBeingLoaded == null) {
			return null;
		}

		// move the controlled unit reference to a temp variable, so that we can
		// clear the unitBeingLoaded variable and still pass a reference to the
		// new unit back to the calling function.  In this way, we can always use
		// the unitBeingLoaded variable as a de facto flag to know whether or not
		// a module is currently being loaded
		PamControlledUnit unitNowLoaded = unitBeingLoaded;
		clearLoadedUnit();

		//		guiFrameManager.notifyModelChanged(ADD_CONTROLLEDUNIT); //this should be handled above in addControlledUnit

		return unitNowLoaded;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControllerInterface#RemoveControlledUnt(PamguardMVC.PamControlledUnit)
	 */
	@Override
	public void removeControlledUnt(PamControlledUnit controlledUnit) {

		// The PamExceptionHandler will call this to remove a controlled unit that fails
		// during load.  Depending when it failed, it may or may not have been instantiated
		// yet.  So if the controlledUnit is still null, just exit
		// 
		if (controlledUnit==null) {
			return;
		}

		/**
		 * NEVER delete the array manager. 
		 */
		if (controlledUnit.getClass() == ArrayManager.class) {
			return;
		}

		guiFrameManager.removeControlledUnit(controlledUnit);

		while (pamControlledUnits.contains(controlledUnit)) {
			pamControlledUnits.remove(controlledUnit);
			notifyModelChanged(PamControllerInterface.REMOVE_CONTROLLEDUNIT);
		}
		//		getMainFrame().revalidate(); //handled inside the GUIFrameManager by notify model changed. The controller should have 
		//as few direct GUI calls as possible. 
	}


	/* (non-Javadoc)
	 * @see PamController.PamControllerInterface#orderModules()
	 */
	@Override
	public boolean orderModules(Frame parentFrame) {
		int[] newOrder = ModuleOrderDialog.showDialog(this, parentFrame); 
		if (newOrder != null) {
			// re-order the modules according the new list.
			reOrderModules(newOrder);

			notifyModelChanged(PamControllerInterface.REORDER_CONTROLLEDUNITS);

			return true;
		}

		return false;
	}

	private boolean reOrderModules(int[] newOrder) {

		if (pamControlledUnits.size() != newOrder.length) return false;

		ArrayList<PamControlledUnit> newList = new ArrayList<PamControlledUnit>();

		for (int i = 0; i < newOrder.length; i++) {

			newList.add(pamControlledUnits.get(newOrder[i]));

		}

		pamControlledUnits = newList;

		return true;
	}

	/**
	 * Swaps the positions of two modules in the main list of modules and 
	 * also swaps their tabs (if they have them). 
	 * @param m1 First PamControlledUnit to swap
	 * @param m2 Second PamControlledUnit to swap.
	 */
	private void switchModules(PamControlledUnit m1, PamControlledUnit m2) {

	}

	/**
	 * Sets the position of a particular PamControlledUnit in the list. 
	 * Also sets the right tab position, to match that order. 
	 * @param pcu
	 * @param position
	 * @return
	 */
	private boolean setModulePosition(PamControlledUnit pcu, int position) {

		return false;
	}

	@Override
	public PamControlledUnit getControlledUnit(int iUnit) {
		if (iUnit < getNumControlledUnits()) {
			return pamControlledUnits.get(iUnit);
		}
		return null;
	}

	@Override
	public PamControlledUnit findControlledUnit(String unitType) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getUnitType().equalsIgnoreCase(unitType)) {
				return pamControlledUnits.get(i);
			}
		}
		return null;
	}

	/**
	 * Get a list of PamControlledUnit units of a given type
	 * @param unitType Controlled unit type
	 * @return list of units. 
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(String unitType) {
		ArrayList<PamControlledUnit> l = new ArrayList<PamControlledUnit>();
		int n = getNumControlledUnits();
		PamControlledUnit pcu;
		for (int i = 0; i < n; i++) {
			pcu = getControlledUnit(i);
			if (pcu.getUnitType().equals(unitType)) {
				l.add(pcu);
			}
		}

		return l;
	}
	/**
	 * Get a list of PamControlledUnit units of a given type and name, allowing for nulls. 
	 * @param unitType Controlled unit type, can be null for all units of name
	 * @param unitName Controlled unit name, can be null for all units of type
	 * @return list of units. 
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(String unitType, String unitName) {
		ArrayList<PamControlledUnit> l = new ArrayList<PamControlledUnit>();
		int n = getNumControlledUnits();
		PamControlledUnit pcu;
		for (int i = 0; i < n; i++) {
			pcu = getControlledUnit(i);
			if (unitType != null && !unitType.equals(pcu.getUnitType())) {
				continue;
			}
			if (unitName != null && !unitName.equals(pcu.getUnitName())) {
				continue;
			}
			l.add(pcu);
		}

		return l;
	}

	@Override
	public PamControlledUnit findControlledUnit(String unitType, String unitName) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getUnitType().equalsIgnoreCase(unitType) &&
					pamControlledUnits.get(i).getUnitName().equalsIgnoreCase(unitName)) {
				return pamControlledUnits.get(i);
			}
		}
		return null;
	}

	/**
	 * Find the first instance of a module with a given class type and name.
	 * <p>Name can be null in which case the first module with the correct class
	 * will be returned
	 * @param unitClass Module class (sub class of PamControlledUnit)
	 * @param unitName Module Name
	 * @return Existing module with that class and name. 
	 */
	public PamControlledUnit findControlledUnit(Class unitClass, String unitName) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getClass() == unitClass && (unitName == null ||
					pamControlledUnits.get(i).getUnitName().equalsIgnoreCase(unitName))) {
				return pamControlledUnits.get(i);
			}
		}
		return null;
	}

	/**
	 * Get an Array list of PamControlledUnits of a particular class (exact matches only). 
	 * @param unitClass PamControlledUnit class
	 * @return List of current instances of this class. 
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(Class unitClass) {
		ArrayList<PamControlledUnit> foundUnits = new ArrayList<>();
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getClass() == unitClass) {
				foundUnits.add(pamControlledUnits.get(i));
			}
		}
		return foundUnits;
	}

	/**
	 * Check whether a controlled unit exists based on it's name. 
	 * @param the controlled unit name e.g. "my crazy click detector", not the default name. 
	 */
	public boolean isControlledUnit(String controlName) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getUnitName().equalsIgnoreCase(controlName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getNumControlledUnits() {
		if (pamControlledUnits == null) {
			return 0;
		}
		return pamControlledUnits.size();
	}

	static public PamController getInstance() {
		return uniqueController;
	}

	@Override
	public PamModelInterface getModelInterface() {
		return pamModelInterface;
	}

	@Override
	public void addView(PamViewInterface newView) {
		guiFrameManager.addView(newView);
	}

	public void showControlledUnit(PamControlledUnit unit) {
		guiFrameManager.showControlledUnit(unit);
	}

	/**
	 * Restart PAMguard. Can be called when something is mildly wrong
	 * such as a DAQ glitch, so that acquisition is stopped and
	 * restarted. 
	 */
	public void restartPamguard() {
		pamStop();
		startLater();		
	}
	/**
	 * calls pamStart using the SwingUtilities
	 * invokeLater command to start PAMGAURD 
	 * later in the AWT event queue. 
	 */
	public void startLater() {
		SwingUtilities.invokeLater(new StartLater(true));
	}

	public void startLater(boolean saveSettings) {
		SwingUtilities.invokeLater(new StartLater(saveSettings));
	}
	/**
	 * Runnable for use with startLater. 
	 * @author Doug
	 *
	 */
	private class StartLater implements Runnable {

		boolean saveSettings;

		/**
		 * @param saveSettings
		 */
		public StartLater(boolean saveSettings) {
			super();
			this.saveSettings = saveSettings;
		}

		@Override
		public void run() {
			pamStart(saveSettings);
		}
	}

	/**
	 * calls pamStop using the SwingUtilities
	 * invokeLater command to stop PAMGAURD 
	 * later in the AWT event queue. 
	 */
	public void stopLater() {
		SwingUtilities.invokeLater(new StopLater());
	}

	/**
	 * Runnable to use with the stopLater() command
	 * @author Doug Gillespie
	 *
	 */
	private class StopLater implements Runnable {
		@Override
		public void run() {
			pamStop();
		}
	}


	/**
	 * Start PAMGUARD. This function also gets called from the 
	 *  GUI menu start button and from the Network control system.
	 *  <p>As well as actually starting PAMGUARD it will write
	 *  settings to the database and to the binary data store. 
	 * @return true if all modules start successfully
	 */
	@Override
	public boolean pamStart() {
		//		Debug.println("PAMController: pamStart");
		return pamStart(true);
	}

	/**
	 * Start PAMGuard with an option on saving settings. 
	 * @param saveSettings flag to save settings to database and binary store
	 * @return true if all modules start successfully
	 */
	public boolean pamStart(boolean saveSettings) {
		//		Debug.println("PAMController: pamStart2");
		return pamStart(saveSettings, PamCalendar.getTimeInMillis());
	}

	/**
	 * Starts PAMGuard, but with the option to save settings (to binary and to database)
	 * and also to give a specific start time for the session. When data are being received over
	 * the network, this may be in the past !
	 * @param saveSettings flag to say whether or not settings should be saved. 
	 * @param startTime start time in millis
	 * @return true if all modules start successfully
	 */
	public boolean pamStart(boolean saveSettings, long startTime) {
		//		Debug.println("PAMController: pamStart3");

		globalTimeManager.waitForGlobalTime(getMainFrame(), 
				globalTimeManager.getGlobalTimeParameters().getStartupDelay());

		manualStop = false;

		PamCalendar.setSessionStartTime(startTime);
		setPamStatus(PAM_RUNNING);
		long t1 = System.currentTimeMillis();
		int prepErrors = 0;
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			for (int iP = 0; iP < pamControlledUnits.get(iU).getNumPamProcesses(); iP++) {
				if (getRunMode() != RUN_NETWORKRECEIVER) {
					pamControlledUnits.get(iU).getPamProcess(iP).clearOldData();
				}
				if (pamControlledUnits.get(iU).getPamProcess(iP).prepareProcessOK() == false) {
					setPamStatus(PAM_IDLE);
					System.out.println("Can't start since unable to prepare process " + 
							pamControlledUnits.get(iU).getPamProcess(iP).getProcessName());
					prepErrors++;
				};
			}
			//			long t2 = System.currentTimeMillis();
			//			System.out.printf("***********************************Time taken to prepare %s was %d millis\n",  pamControlledUnits.get(iU).getUnitName(), t2-t1);
			//			t1 = t2;
		}
		boolean prepError = (runMode == PamController.RUN_NORMAL && prepErrors > 0);
		if (prepError) {
			return false;
		}

		if (saveSettings) {
			startTime = PamCalendar.getSessionStartTime();
//			System.out.printf("Saving settings for start time %s\n", PamCalendar.formatDBDateTime(startTime));
			saveSettings(PamCalendar.getSessionStartTime());
		}

		StorageOptions.getInstance().setBlockOptions();

		t1 = System.currentTimeMillis();
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			pamControlledUnits.get(iU).pamToStart();
			//			long t2 = System.currentTimeMillis();
			//			System.out.printf("+++++++++++++++++++++++++++++++++++Time taken to call pamtoStart %s was %d millis\n",  pamControlledUnits.get(iU).getUnitName(), t2-t1);
			//			t1 = t2;
		}
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			for (int iP = 0; iP < pamControlledUnits.get(iU)
					.getNumPamProcesses(); iP++) {
				pamControlledUnits.get(iU).getPamProcess(iP).pamStart();
			}
			//			long t2 = System.currentTimeMillis();
			//			System.out.printf("==================================Time taken to call pamStart %s was %d millis\n",  pamControlledUnits.get(iU).getUnitName(), t2-t1);
			//			t1 = t2;
		}

		// starting the DAQ may take a little while, so recheck and reset the 
		// start time.
		long startDelay = PamCalendar.getTimeInMillis() - PamCalendar.getSessionStartTime();
		if (PamCalendar.isSoundFile() == false) {
			PamCalendar.setSessionStartTime(PamCalendar.getTimeInMillis());
		}
		if (PamCalendar.isSoundFile() == false) {
			System.out.printf("PAMGUARD Startup took %d milliseconds at time %s\n", startDelay, PamCalendar.formatDateTime(PamCalendar.getSessionStartTime()));
		}
		guiFrameManager.pamStarted();

		return true;
	}

	/**
	 * Stopping PAMGUARD. Harder than you might think !
	 * First a pamStop() is sent to all processes, then once
	 * that's done, a pamHasStopped is sent to all Controllers. 
	 * <p>This is necessary when running in a multi-thread mode
	 * since some processes may still be receiving data and may still 
	 * pass if on to other downstream processes, storage, etc. 
	 * 
	 */
	@Override
	public void pamStop() {

		setPamStatus(PAM_STOPPING);
		
		// start the status check timer, so that we know when all the threads have
		// actually stopped
//		statusCheckThread = new Thread(new StatusTimer());
//		statusCheckThread.start();

		// tell all controlled units to stop
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			for (int iP = 0; iP < pamControlledUnits.get(iU)
					.getNumPamProcesses(); iP++) {
				pamControlledUnits.get(iU).getPamProcess(iP).pamStop();
			}
		}
		
		/*
		 *  now launch another thread to wait for everything to have stopped, but
		 *  leave this function so that AWT is released and graphics can update, the
		 *  wait thread will make a fresh call into AWT which will continue the stopping 
		 *  of everything. 
		 */
		detectorEndThread = new WaitDetectorThread();
		Thread t = new Thread(detectorEndThread);
		t.start();
	}
	
	/**
	 * Non AWT thread that sits and waits for detectors to actually finish their 
	 * processing, leaving AWT unblocked. Then calls a function back into AWT to 
	 * finish stopping, which will do stuff like closing binary files. 
	 * @author dg50
	 *
	 */
	private class WaitDetectorThread implements Runnable {

		@Override
		public void run() {
			while (checkRunStatus()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// arrive here when all detectors have ended. 
			finishStopping();
		}
		
	}
	
	/**
	 * Called once the detectors have actually stopped and puts a few finalising 
	 * functions into the AWT thread. 
	 */
	private void finishStopping() {
		detectorEndThread = null;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				pamStopped();
			}
		});
	}
		
	
	/**
	 * Called from the thread that was launched in PamStop to continue
	 * the stopping funcions once the detectors have emptied their input queues. 
	 */
	private void pamStopped() {
		/*
		 * If it's running in multithreading mode, then at this point
		 * it is necessary to make sure that all internal datablock 
		 * buffers have had time to empty.
		 */
		if (PamModel.getPamModel().isMultiThread()) {
			for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
				pamControlledUnits.get(iU).flushDataBlockBuffers(2000);
			}
		}
		setPamStatus(PAM_IDLE);

		// wait here until the status has changed to Pam_Idle, so that we know
		// that we've really finished processing all data
//		Debug.out.println("PamController waiting for all threaded observers to stop...");
//
//		while (getPamStatus()!=PAM_IDLE) {
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		
		// send out the pamHasStopped message
//		Debug.out.println("PamController letting everyone know PAMGuard has stopped.");
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			pamControlledUnits.get(iU).pamHasStopped();
		}
		guiFrameManager.pamEnded();
		
		long stopTime = PamCalendar.getTimeInMillis();
		saveEndSettings(stopTime);
		
		// no good having this here since it get's called at the end of every file. 
//		if (GlobalArguments.getParam(PamController.AUTOEXIT) != null) {
////			can exit here, since we've auto started, can auto exit.
//			if (canClose()) {
//				pamClose();
//				shutDownPamguard();
//			}
//		}
	}
	
	public void batchProcessingComplete( ) {
		if (GlobalArguments.getParam(PamController.AUTOEXIT) != null) {
			//	can exit here, since we've auto started, can auto exit.
			if (canClose()) {
				pamClose();
				shutDownPamguard();
			}
		}
	}


	/**
	 * Status Timer class.  Once started, will run in a thread
	 * and constantly check the buffers of all ThreadedObserver
	 * objects.  Once all the buffers are empty, this will set
	 * the pam status to PAM_IDLE and let the thread die
	 * 
	 * @author mo55
	 *
	 */
//	private class StatusTimer implements Runnable {
//		@Override
//		public void run() {
//			boolean stillRunning = true;
//			while (stillRunning) {
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				if (checkRunStatus() == true) {
//					setPamStatus(PAM_IDLE);
//					break;
//				}
//			}
//			System.out.println("Status timer completed, all processes have received last data");
//		}
//	}

	/**
	 * Check the status of every threaded observer to see if it has emptied out
	 * it's buffer of events.  If the buffer is empty, return true.  If the thread
	 * is still processing, return false
	 * 
	 * @return true if ANY process is still running
	 */
	private boolean checkRunStatus() {
//		boolean areWeFinished = true;
//		Debug.out.println("Checking run status...");
//		for (PamControlledUnit aUnit : pamControlledUnits) {
//			int numProcesses = aUnit.getNumPamProcesses();
//			for (int i=0; i<numProcesses; i++) {
//				PamProcess aProcess = aUnit.getPamProcess(i);
//				ArrayList<PamDataBlock> outputBlocks = aProcess.getOutputDataBlocks();
//				for (PamDataBlock aBlock : outputBlocks) {
//					int numObs = aBlock.countObservers();
//					for (int j=0; j<numObs; j++) {
//						PamObserver anObs = aBlock.getPamObserver(j);
//						if (ThreadedObserver.class.isAssignableFrom(anObs.getClass())) {
//							if (((ThreadedObserver) anObs).isEmptyRead() == false) {
//								Debug.out.println("   Thread " + anObs.getObserverName() + "-" + System.identityHashCode(anObs) +
//										" (" + Thread.currentThread().getName() + 
//										"-" + Thread.currentThread().getId() +
//										") is STILL processing");
//								areWeFinished = false;
//								break;	// if we've found one still going, no need to check any further
//							}
//							else {
//								Debug.out.println("   Thread " + anObs.getObserverName() +  "-" + System.identityHashCode(anObs) +
//										" (" + Thread.currentThread().getName() + 
//										"-" + Thread.currentThread().getId() +
//										") is finished processing");							
//							}
//						}
//					}
//				}
//			}
//		}
//		Debug.out.println("   Are we finished? " + areWeFinished);
//		return areWeFinished;
		boolean running = false;
		for (PamControlledUnit aUnit : pamControlledUnits) {
			int numProcesses = aUnit.getNumPamProcesses();
			for (int i=0; i<numProcesses; i++) {
				PamProcess aProcess = aUnit.getPamProcess(i);
				int lastNotification = aProcess.getLastSourceNotificationType();
				if (lastNotification == AcquisitionProcess.FIRSTDATA) {
					sayStatusWarning("Waiting for " + aProcess.getProcessName());					
					running = true;
				}
//				System.out.printf("Process %s is in state %d\n", aProcess.getProcessName(), lastNotification);
			}
		}
		return running;
	}


	/**
	 * Gets called in pamStart and may / will attempt to store all
	 * PAMGUARD settings via the database and binary storage modules. 
	 */
	private void saveSettings(long timeNow) {
		PamControlledUnit pcu;
		PamSettingsSource settingsSource;
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			pcu = pamControlledUnits.get(iU);
			if (PamSettingsSource.class.isAssignableFrom(pcu.getClass())) {
				settingsSource = (PamSettingsSource) pcu;
				settingsSource.saveStartSettings(timeNow);
			}
		}
		PamguardXMLWriter.getXMLWriter().writeStartSettings(timeNow);
	}

	/**
	 * Gets called in pamStart and may / will attempt to store all
	 * PAMGUARD settings via the database and binary storage modules. 
	 */
	private void saveEndSettings(long timeNow) {
//		System.out.printf("Updating settings with end time %s\n", PamCalendar.formatDBDateTime(timeNow));
		PamControlledUnit pcu;
		PamSettingsSource settingsSource;
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			pcu = pamControlledUnits.get(iU);
			if (PamSettingsSource.class.isAssignableFrom(pcu.getClass())) {
				settingsSource = (PamSettingsSource) pcu;
				settingsSource.saveEndSettings(timeNow);
			}
		}
	}
	
	

	/**
	 * Export configuration into an XML file
	 * @param parentFrame 
	 * @param timeMillis time stamp that will get added to file name and content. 
	 */
	public void exportGeneralXMLSettings(JFrame parentFrame, long timeMillis) {
		XMLWriterDialog.showDialog(parentFrame);
	}

	/**
	 * Find the path to the binary store ....
	 * @return path to the binary store. 
	 */
	public String findBinaryStorePath() {
		BinaryStore binaryControl = BinaryStore.findBinaryStoreControl();
		if (binaryControl == null) {
			return null;
		}
		String storeLoc = binaryControl.getBinaryStoreSettings().getStoreLocation();
		if (storeLoc == null) {
			return "";
		}
		if (storeLoc.endsWith(File.separator) == false) {
			storeLoc += File.separator;
		}
		return storeLoc;
	}

	/**
	 * 
	 * @return a list of PamControlledUnits which implements the 
	 * PamSettingsSource interface
	 * @see PamSettingsSource
	 */
	public ArrayList<PamSettingsSource> findSettingsSources() {
		ArrayList<PamSettingsSource> settingsSources = new ArrayList<PamSettingsSource>();
		PamControlledUnit pcu;
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			pcu = pamControlledUnits.get(iU);
			if (PamSettingsSource.class.isAssignableFrom(pcu.getClass())) {
				settingsSources.add((PamSettingsSource) pcu);
			}
		}
		return settingsSources;
	}

	@Override
	public boolean modelSettings(JFrame frame) {
		return pamModelInterface.modelSettings(frame);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamControllerInterface#PamStarted()
	 */
	@Override
	public void pamStarted() {
	}

	@Override
	public void pamEnded() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamModel.PamModelInterface#GetFFTDataBlocks() Goes through all
	 *      processes and makes an array list containing only data blocks of FFT
	 *      data
	 */
	@Override
	public ArrayList<PamDataBlock> getFFTDataBlocks() {
		return makeDataBlockList(FFTDataUnit.class, true);
	}

	@Override
	public PamDataBlock getFFTDataBlock(int id) {
		return getDataBlock(FFTDataUnit.class, id);
	}

	@Override
	public PamDataBlock getFFTDataBlock(String name) {
		return getDataBlock(FFTDataUnit.class, name);
	}

	@Override
	public ArrayList<PamDataBlock> getRawDataBlocks() {
		return makeDataBlockList(RawDataUnit.class, true);
	}

	@Override
	public PamRawDataBlock getRawDataBlock(int id) {
		return (PamRawDataBlock) getDataBlock(RawDataUnit.class, id);
	}

	@Override
	public PamRawDataBlock getRawDataBlock(String name) {
		return (PamRawDataBlock) getDataBlock(RawDataUnit.class, name);
	}

	@Override
	public ArrayList<PamDataBlock> getDetectorDataBlocks() {
		return makeDataBlockList(PamDetection.class, true);
	}

	@Override
	public PamDataBlock getDetectorDataBlock(int id) {
		return getDataBlock(PamDetection.class, id);
	}

	@Override
	public PamDataBlock getDetectorDataBlock(String name) {
		return getDataBlock(PamDetection.class, name);
	}

	@Override
	public ArrayList<PamDataBlock> getDetectorEventDataBlocks() {
		//		return makeDataBlockList(PamguardMVC.DataType.DETEVENT);
		return null;
	}

	@Override
	public PamDataBlock getDetectorEventDataBlock(int id) {
		//		return getDataBlock(PamguardMVC.DataType.DETEVENT, id);
		return null;
	}

	@Override
	public PamDataBlock getDetectorEventDataBlock(String name) {
		//		return (PamDataBlock) getDataBlock(PamguardMVC.DataType.DETEVENT, name);
		return null;
	}

	@Override
	/**
	 * Note that in order to return a list of PamDataBlocks that contain objects implementing
	 * the AcousticDataUnit or PamDetection interfaces, the includeSubClasses boolean MUST be
	 * true.
	 */
	public ArrayList<PamDataBlock> getDataBlocks(Class blockType, boolean includeSubClasses) {
		return makeDataBlockList(blockType, includeSubClasses);
	}

	@Override
	public ArrayList<PamDataBlock> getDataBlocks() {
		return makeDataBlockList(PamDataUnit.class, true);
	}

	public ArrayList<PamDataBlock> getPlottableDataBlocks(GeneralProjector generalProjector) {

		ArrayList<PamDataBlock> blockList = new ArrayList<PamDataBlock>();
		PamProcess pP;
		Class unitClass;
		PanelOverlayDraw panelOverlayDraw;

		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			for (int iP = 0; iP < pamControlledUnits.get(iU)
					.getNumPamProcesses(); iP++) {
				pP = pamControlledUnits.get(iU).getPamProcess(iP);
				for (int j = 0; j < pP.getNumOutputDataBlocks(); j++) {
					if(pP.getOutputDataBlock(j).canDraw(generalProjector)) {
						blockList.add(pP.getOutputDataBlock(j));
					}
				}
			}
		}
		return blockList;
	}

	/**
	 * Makes a list of data blocks for all processes in all controllers for a
	 * given DataType or for all DataTypes
	 * 
	 * @param blockType -- PamguardMVC.DataType.FFT, .RAW, etc., or <b>null</b> to
	 *        get all extant blocks
	 * @return An ArrayList of data blocks
	 */
	//	private ArrayList<PamDataBlock> makeDataBlockList(Enum blockType) {
	//		ArrayList<PamDataBlock> blockList = new ArrayList<PamDataBlock>();
	//		PamProcess pP;
	//
	//		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
	//			for (int iP = 0; iP < pamControlledUnits.get(iU)
	//					.getNumPamProcesses(); iP++) {
	//				pP = pamControlledUnits.get(iU).getPamProcess(iP);
	//				for (int j = 0; j < pP.getNumOutputDataBlocks(); j++) {
	//					if (blockType == null
	//							|| pP.getOutputDataBlock(j).getDataType() == blockType) {
	//						blockList.add(pP.getOutputDataBlock(j));
	//					}
	//				}
	//			}
	//		}
	private ArrayList<PamDataBlock> makeDataBlockList(Class classType, boolean includSubClasses) {

		ArrayList<PamDataBlock> blockList = new ArrayList<PamDataBlock>();
		PamProcess pP;
		Class unitClass;

		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			for (int iP = 0; iP < pamControlledUnits.get(iU)
					.getNumPamProcesses(); iP++) {
				pP = pamControlledUnits.get(iU).getPamProcess(iP);
				for (int j = 0; j < pP.getNumOutputDataBlocks(); j++) {
					//System.out.println("Comparing "+pP.getOutputDataBlock(j).getUnitClass().getCanonicalName()+" to "+classType.getCanonicalName());
					if ((unitClass = pP.getOutputDataBlock(j).getUnitClass()) == classType) {
						blockList.add(pP.getOutputDataBlock(j));
					}
					else if (includSubClasses) {
						if (classType != null && classType.isAssignableFrom(unitClass)) {
							blockList.add(pP.getOutputDataBlock(j));
						}
						//						while ((unitClass = unitClass.getSuperclass()) != null) {
						//							if (unitClass == classType) {
						//								blockList.add(pP.getOutputDataBlock(j));
						//								break;
						//							}
						//						}
					}
				}
			}
		}

		return blockList;
	}

	/** 
	 * Find a block of a given type with the id number, or null if the number
	 * is out of range.
	 * 
	 * @param  blockType
	 * @param  id -- the block id number
	 * @return  block, which you may want to cast to a subtype
	 */
	@Override
	public PamDataBlock getDataBlock(Class blockType, int id) {

		ArrayList<PamDataBlock> blocks = getDataBlocks(blockType, true);
		if (id >= 0 && id < blocks.size()) return blocks.get(id);
		return null;
	}

	/** 
	 * Find a block of a given type with the given name, or null if it
	 * doesn't exist.
	 * @param  blockType -- RAW, FFT, DETECTOR, null, etc.
	 * @param  name -- the block name
	 * @return  block, which you may want to cast to a subtype
	 */
	@Override
	public PamDataBlock getDataBlock(Class blockType, String name) {
		if (name == null) return null;
		ArrayList<PamDataBlock> blocks = getDataBlocks(blockType, true);
		for (PamDataBlock dataBlock:blocks) {
			if (name.equals(dataBlock.getLongDataName())) { // check for a long name match first 
				return dataBlock;
			}
			if (dataBlock instanceof FFTDataBlock) {
				FFTDataBlock fb = (FFTDataBlock) dataBlock;
				if (name.equals(fb.getOldLongDataName())) {
					return dataBlock;
				}
			}
			if (name.equals(dataBlock.toString())) {
				return dataBlock;
			}
		}
		return null;
	}

	/**
	 * Find a block with the given long name, or null if it doesn't exist.
	 * @param longName the long name of the PamDataBlock
	 * @return block
	 */
	public PamDataBlock getDataBlockByLongName(String longName) {
		if (longName == null) return null;
		ArrayList<PamDataBlock> allBlocks = getDataBlocks();
		for (PamDataBlock dataBlock:allBlocks) {
			if (longName.equals(dataBlock.getLongDataName())) {
				return dataBlock;
			}
			if (dataBlock instanceof FFTDataBlock) {
				FFTDataBlock fb = (FFTDataBlock) dataBlock;
				if (longName.equals(fb.getOldLongDataName())) {
					return dataBlock;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @return a list of offline data sources.  
	 */
	public ArrayList<OfflineDataStore> findOfflineDataStores() {
		ArrayList<OfflineDataStore> ods = new ArrayList<OfflineDataStore>();
		int n = getNumControlledUnits();
		PamControlledUnit pcu;
		for (int i = 0; i < n; i++) {
			pcu = getControlledUnit(i);
			if (OfflineDataStore.class.isAssignableFrom(pcu.getClass())) {
				ods.add((OfflineDataStore) pcu);
			}
		}
		return ods;
	}

	public OfflineDataStore findOfflineDataStore(Class sourceClass) {
		ArrayList<OfflineDataStore> odss  = findOfflineDataStores();
		for (int i = 0; i < odss.size(); i++) {
			if (sourceClass.isAssignableFrom(odss.get(i).getClass())) {
				return odss.get(i);
			}
		}
		return null;
	}

	/**
	 * Updates the entire datamap. 
	 */
	public void updateDataMap(){
		System.out.println("updateDataMap:"); 

		if (DBControlUnit.findDatabaseControl()==null) return;
		
		System.out.println("updateDataMap: 1"); 

		ArrayList<PamDataBlock> datablocks=getDataBlocks() ;
		
		System.out.println("updateDataMap: 2"); 

		DBControlUnit.findDatabaseControl().updateDataMap(datablocks);
		
		System.out.println("updateDataMap: 3"); 

		BinaryStore.findBinaryStoreControl().getDatagramManager().updateDatagrams();
		
		System.out.println("updateDataMap: 4"); 

		notifyModelChanged(PamControllerInterface.EXTERNAL_DATA_IMPORTED);
	}
	
	
	/**
	 * Create the datamap from the database 
	 */
	public void createDataMap(){
		if (DBControlUnit.findDatabaseControl()==null) return;
		DBControlUnit.findDatabaseControl().createOfflineDataMap(getMainFrame());
		BinaryStore.findBinaryStoreControl().createOfflineDataMap(getMainFrame());
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamControllerInterface#NotifyModelChanged()
	 */
	@Override
	public void notifyModelChanged(int changeType) {

		//System.out.println("PamController: notify model changed: " +changeType );
		if (changeType == CHANGED_MULTI_THREADING) {
			changedThreading();
		}

		// no need for this, since array in now a controlled unit so it
		// will get this notification anyway. 
		//		ArrayManager.getArrayManager().notifyModelChanged(changeType);

		guiFrameManager.notifyModelChanged(changeType);

		PamColors.getInstance().notifyModelChanged(changeType);

		MasterReferencePoint.notifyModelChanged(changeType);

		// also tell all PamControlledUnits since they may want to find their data source 
		// it that was created after they were - i.e. dependencies have got all muddled
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).notifyModelChanged(changeType);
		}

		PamSettingManager.getInstance().notifyModelChanged(changeType);


		if (getRunMode() == PamController.RUN_PAMVIEW) {
			AbstractScrollManager.getScrollManager().notifyModelChanged(changeType);
			if (changeType == CHANGED_OFFLINE_DATASTORE) { // called from both database and binary data mappers. 
				checkOfflineDataUIDs();
			}
		}

		if (networkController != null) {
			networkController.notifyModelChanged(changeType);
		}

		if (changeType == INITIALIZATION_COMPLETE) {
			DataKeeper.getInstance().setAllKeepTimes();
			StorageOptions.getInstance().setBlockOptions();
		}

		// if we've just added a module, try to synchronise the UIDs with the the database and binary stores
		if (changeType == INITIALIZATION_COMPLETE || (initializationComplete && changeType == ADD_CONTROLLEDUNIT)) {
			uidManager.synchUIDs(true);
		}

		if (changeType == GLOBAL_TIME_UPDATE) {
			haveGlobalTimeUpdate = true;
		}
		else {
			globalTimeManager.notifyModelChanged(changeType);
		}
		
		if (moduleChange(changeType)) {
			clearSelectorsAndSymbols();
		}

	}
	
	/**
	 * Has there been a module change AFTER initial module loading. 
	 * @param changeType
	 * @return true if modules added or removed after initialisation complete
	 */
	public boolean moduleChange(int changeType) {
		if (isInitializationComplete() == false) {
			return false;
		}
		switch (changeType) {
		case ADD_CONTROLLEDUNIT:
		case REMOVE_CONTROLLEDUNIT:
			return true;
		default:
			return false;
		}
	}

	private void checkOfflineDataUIDs() {
		ArrayList<PamDataBlock> dataBlocks = getDataBlocks(); 
		for (PamDataBlock dataBlock:dataBlocks) {
			dataBlock.checkOfflineDataUIDs();
		}
	}

	/**
	 * loop over all units and processes, telling them to 
	 * re-subscribe to their principal data source 
	 */
	private void changedThreading() {
		PamProcess pamProcess;
		int nP;
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			nP = pamControlledUnits.get(i).getNumPamProcesses();
			for (int iP = 0; iP < nP; iP++) {
				pamProcess = pamControlledUnits.get(i).getPamProcess(iP);
				pamProcess.changedThreading();
			}
		}
	}

	public void setupGarbageCollector() {
		PamModelSettings ms = pamModelInterface.getPamModelSettings();
		if (ms.enableGC == false) {
			if (garbageTimer != null) {
				garbageTimer.stop();
			}
		}
		else {
			if (garbageTimer == null) {
				garbageTimer = new Timer(ms.gcInterval*1000, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						//						System.out.println("Additional Garbage collection called");
						Runtime.getRuntime().gc();
					}
				});
			}
			garbageTimer.setDelay(ms.gcInterval*1000);
			garbageTimer.start();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		ArrayList<UsedModuleInfo> usedModules = new ArrayList<UsedModuleInfo>();
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			usedModules.add(new UsedModuleInfo(pamControlledUnits.get(i).getClass().getName(), 
					pamControlledUnits.get(i).getUnitType(),
					pamControlledUnits.get(i).getUnitName()));
		}
		return usedModules;
	}

	@Override
	public long getSettingsVersion() {
		return 0;
	}

	@Override
	public String getUnitName() {
		return "Pamguard Controller";
	}

	@Override
	public String getUnitType() {
		return "PamController";
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		if (loadingOldSettings) {
			return false;
		}
		ArrayList<UsedModuleInfo> usedModules = (ArrayList<UsedModuleInfo>) pamControlledUnitSettings.getSettings();
		UsedModuleInfo umi;
		PamModuleInfo mi;
		for (int i = 0; i < usedModules.size(); i++) {
			umi = usedModules.get(i);
			mi = PamModuleInfo.findModuleInfo(umi.className);
			if (mi == null) continue;
			addModule(mi, umi.unitName);
		}
		return true;
	}

	public void destroyModel() {
		pamStop();

		guiFrameManager.destroyModel();

		// also tell all PamControlledUnits since they may want to find their data source 
		// it that was created after they were - i.e. dependencies have got all muddled
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).notifyModelChanged(DESTROY_EVERYTHING);
		}
		pamControlledUnits = null;

		PamSettingManager.getInstance().reset();

	}

	@Override
	public void totalModelRebuild() {

		destroyModel();

		setupPamguard();		
	}
	/**
	 * returns the status of Pamguard. The available status' will
	 * depend on the run mode. For instance, if run mode is RUN_NORMAL
	 * then status can be either PAM_IDLE or PAM_RUNNING.
	 * @return Pamguard status
	 */
	public int getPamStatus() {
		return pamStatus;
	}

	public void setPamStatus(int pamStatus) {
		this.pamStatus = pamStatus;
		if (getRunMode() != RUN_PAMVIEW) {
			TopToolBar.enableStartButton(pamStatus == PAM_IDLE);
			TopToolBar.enableStopButton(pamStatus == PAM_RUNNING);
		}
		showStatusWarning(pamStatus);
	}
	
	/**
	 * show a warning when we're waiting for detectors to stop
	 * @param pamStatus
	 */
	private void showStatusWarning(int pamStatus) {
		switch (pamStatus) {
		case PAM_STOPPING:
			sayStatusWarning("Waiting for detectors to complete");
			break;
		default:
			sayStatusWarning(null);
		}
	}
	
	/**
	 * Show a singleton status warning message. 
	 * @param warningMessage Message - removes warning if set null. 
	 */
	private synchronized void sayStatusWarning(String warningMessage) {
		WarningSystem warningSystem = WarningSystem.getWarningSystem();
		if (warningMessage == null) {
			warningSystem.removeWarning(statusWarning);
		}
		else {
			statusWarning.setWarningMessage(warningMessage);
			statusWarning.setWarnignLevel(1);
			warningSystem.addWarning(statusWarning);
		}
	}

	/**
	 * Gets the Pamguard running mode. This is set at startup (generally
	 * through slightly different versions of the main class). It will be
	 * one of 
	 * RUN_NORMAL
	 * RUN_PAMVIEW
	 * RUN_MIXEDMODE
	 * @return Pamguards run mode
	 */
	public int getRunMode() {
		return runMode;
	}

	public String getRunModeName() {
		switch (runMode) {
		case RUN_NORMAL:
			return "Normal";
		case RUN_PAMVIEW:
			return "Viewer";
		case RUN_MIXEDMODE:
			return "Mixed";
		default:
			return "Unknown";
		}
	}

	//	public void getNewViewTimes(Frame frame) {
	//
	//		PamViewParameters newParams = ViewTimesDialog.showDialog(null, pamViewParameters);
	//		if (newParams != null) {
	//			pamViewParameters = newParams.clone();	
	//			useNewViewTimes();
	//		}	
	//	}

	/**
	 * Class to do some extra saving of view times. 
	 * @author Douglas Gillespie
	 *
	 */
	class ViewTimesSettings implements PamSettings {

		@Override
		public Serializable getSettingsReference() {
			return pamViewParameters;
		}

		@Override
		public long getSettingsVersion() {
			return PamViewParameters.serialVersionUID;
		}

		@Override
		public String getUnitName() {
			return "PamViewParameters";
		}

		@Override
		public String getUnitType() {
			return "PamViewParameters";
		}

		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			pamViewParameters = ((PamViewParameters) pamControlledUnitSettings.getSettings()).clone();
			//			useNewViewTimes();
			return true;
		}

	}



	//	private long trueStart;
	//	private long trueEnd;
	//
	//	public void useNewViewTimes() {
	//		AWTScheduler.getInstance().scheduleTask(new LoadViewerData());	
	//	}

	//	public class LoadViewerData extends SwingWorker<Integer, ViewerLoadProgress> {
	//
	//		/* (non-Javadoc)
	//		 * @see javax.swing.SwingWorker#doInBackground()
	//		 */
	//		@Override
	//		protected Integer doInBackground() throws Exception {
	//			loadViewData();
	//			
	//			return null;
	//		}
	//
	//		private void loadViewData() {
	//			setPamStatus(PAM_LOADINGDATA);
	//			PamCalendar.setViewTimes(pamViewParameters.viewStartTime, pamViewParameters.viewEndTime);	
	//			// need to tell all datablocks to dump existing data and read in new.
	//			ArrayList<PamDataBlock> pamDataBlocks = getDataBlocks();
	//			PamDataBlock pamDataBlock;
	//			/*
	//			 * also need to get the true max and min load times of the data
	//			 *  
	//			 */
	//			PamDataUnit pdu;
	//			trueStart = Long.MAX_VALUE;
	//			trueEnd = Long.MIN_VALUE;
	//			for (int i = 0; i < pamDataBlocks.size(); i++){
	//				pamDataBlock = pamDataBlocks.get(i);
	//				pamDataBlock.clearAll();
	//				pamDataBlock.loadViewData(this, pamViewParameters);
	//				pdu = pamDataBlock.getFirstUnit();
	//				if (pdu != null) {
	//					trueStart = Math.min(trueStart, pdu.getTimeMilliseconds());
	//				}
	//				pdu = pamDataBlock.getLastUnit();
	//				if (pdu != null) {
	//					trueEnd = Math.max(trueEnd, pdu.getTimeMilliseconds());
	//				}
	//			}
	//		}
	//
	//		/* (non-Javadoc)
	//		 * @see javax.swing.SwingWorker#done()
	//		 */
	//		@Override
	//		protected void done() {
	//			if (trueStart != Long.MAX_VALUE) {
	//				pamViewParameters.viewStartTime = trueStart;
	//				pamViewParameters.viewEndTime = trueEnd;			
	//				PamCalendar.setViewTimes(trueStart, trueEnd);
	////				viewerStatusBar.newShowTimes();
	//			}
	//			newViewTime();
	//			setPamStatus(PAM_IDLE);
	//		}
	//
	//		/* (non-Javadoc)
	//		 * @see javax.swing.SwingWorker#process(java.util.List)
	//		 */
	//		@Override
	//		protected void process(List<ViewerLoadProgress> vlp) {
	//			// TODO Auto-generated method stub
	//			for (int i = 0; i < vlp.size(); i++) {
	////				displayProgress(vlp.get(i));
	//			}
	//		}
	//
	//		/**
	//		 * Callback from SQLLogging in worker thread. 
	//		 * @param viewerLoadProgress
	//		 */
	//		public void sayProgress(ViewerLoadProgress viewerLoadProgress) {
	//			this.publish(viewerLoadProgress);
	//		}
	//		
	//	}

	//	public void tellTrueLoadTime(long loadTime) {
	//		trueStart = Math.min(trueStart, loadTime);
	//		trueEnd = Math.max(trueEnd, loadTime);
	//	}
	//	
	//	public void newViewTime() {
	//		// view time has changed (probably from the slider)
	//		notifyModelChanged(PamControllerInterface.NEW_VIEW_TIME);
	//	}

	//public void displayProgress(ViewerLoadProgress viewerLoadProgress) {
	//	if (viewerStatusBar == null) {
	//		return;
	//	}
	////	if (viewerLoadProgress.getTableName() != null) {
	//		viewerStatusBar.setupLoadProgress(viewerLoadProgress.getTableName());
	////	}
	//	
	//}

	//	public void setupDBLoadProgress(String name) {
	//
	//		if (viewerStatusBar != null) {
	//			viewerStatusBar.setupLoadProgress(name);
	//		}
	//	}
	//	public void setDBLoadProgress(long t) {
	//
	//		if (viewerStatusBar != null) {
	//			viewerStatusBar.setLoadProgress(t);
	//		}
	//	}

	public boolean isInitializationComplete() {
		return initializationComplete;
	}


	/**
	 * Called from PamDialog whenever the OK button is pressed. 
	 * Don't do anything immediately to give the module that opened
	 * the dialog time to respond to it's closing (e.g. make the new
	 * settings from the dialog it's default). 
	 * Use invokeLater to send out a message as soon as the awt que is clear. 
	 */
	public void dialogOKButtonPressed() {

		SwingUtilities.invokeLater(new DialogOKButtonPressed());

	}

	/**
	 * Invoked later every time a dialog OK button is pressed. Sends 
	 * out a message to all modules to say settings have changed. 
	 * @author Doug
	 *
	 */
	class DialogOKButtonPressed implements Runnable {
		@Override
		public void run() {
			notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);	
		}
	}

	/**
	 * Enables / Disables GUI for input. This is used when data are being loaded 
	 * in viewer mode to prevetn impatient users from clicking on extra things while
	 * long background processes take place. 
	 * <p>
	 * Many of the processes loading data are run in the background in SwingWorker threads
	 * scheduled with the AWTScheduler so that they are able to update progress on teh screen
	 * @param enable enable or disable the GUI. 
	 */
	public void enableGUIControl(boolean enable) {
		//		System.out.println("Enable GUI Control = " + enable);
		guiFrameManager.enableGUIControl(enable);
	}

	//	/**
	//	 * Load viewer data into all the scrollers.
	//	 */
	//	public void loadViewerData() {
	//		// TODO Auto-generated method stub
	//		AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
	//		scrollManager.reLoad();
	//		
	//	}

	boolean loadingOldSettings = false;

	/**
	 * Flagged true if the manual stop button has been pressed. 
	 * Used to override the watchdog status. 
	 */
	private boolean manualStop;


	/**
	 * Called to load a specific set of PAMGUARD settings in 
	 * viewer mode, which were previously loaded in from a 
	 * database or binary store. 
	 * @param settingsGroup settings information
	 * @param initialiseNow Immediately data are loaded, initialise which will load data from storages. 
	 */
	public void loadOldSettings(PamSettingsGroup settingsGroup) {
		loadOldSettings(settingsGroup, true);
	}
	/**
	 * Called to load a specific set of PAMGUARD settings in 
	 * viewer mode, which were previously loaded in from a 
	 * database or binary store. 
	 * @param settingsGroup settings information
	 * @param initialiseNow Immediately data are loaded, initialise which will load data from storages. 
	 */
	public void loadOldSettings(PamSettingsGroup settingsGroup, boolean initialiseNow) {
		/**
		 * Three things to do:
		 * 1. consider removing modules which exist but are no longer needed
		 * 2. Add modules which aren't present but are needed
		 * 3. re-order modules
		 * 4. Load settings into modules
		 * 5. Ping round an initialisation complete message. 
		 */
		// 1. get a list of current modules no longer needed. 
		PamControlledUnit pcu;
		ArrayList<PamControlledUnit> toRemove = new ArrayList<PamControlledUnit>();
		for (int i = 0 ; i < getNumControlledUnits(); i++) {
			pcu = getControlledUnit(i);
			if (settingsGroup.findUnitSettings(pcu.getUnitType(), pcu.getUnitName()) == null) {
				toRemove.add(pcu);
			}
		}
		ArrayList<UsedModuleInfo> usedModuleInfo = settingsGroup.getUsedModuleInfo();
		ArrayList<UsedModuleInfo> toAdd = new ArrayList<UsedModuleInfo>();
		UsedModuleInfo aModuleInfo;
		Class moduleClass = null;
		if (usedModuleInfo == null) {
			System.out.println("No available used module info in PamController.loadOldSettings");
			return;
		}
		for (int i = 0; i < usedModuleInfo.size(); i++) {
			aModuleInfo = usedModuleInfo.get(i);
			try {
				moduleClass = Class.forName(aModuleInfo.className);
			} catch (ClassNotFoundException e) {
				System.out.println(String.format("The module with class %s is not available",
						aModuleInfo.className));
				continue;
			}
			if (findControlledUnit(moduleClass, aModuleInfo.unitName) == null) {
				toAdd.add(aModuleInfo);
			}
		}

		PamModuleInfo mi;
		// remove unwanted modules
		if (toRemove.size() > 0) {
			System.out.println(String.format("%d existing modules are not needed", toRemove.size()));
			for (int i = 0; i < toRemove.size(); i++) {
				mi = PamModuleInfo.findModuleInfo(toRemove.get(i).getClass().getName());
				if (mi != null && mi.canRemove() == false) {
					continue;
				}
				System.out.println("Remove module " + toRemove.get(i).toString());
				removeControlledUnt(toRemove.get(i));
			}
		}

		initializationComplete = false;
		loadingOldSettings = true;
		PamSettingManager.getInstance().loadSettingsGroup(settingsGroup, false);

		// add required modules
		if (toAdd.size() > 0) {
			System.out.println(String.format("%d additional modules are needed", toAdd.size()));
			for (int i = 0; i < toAdd.size(); i++) {
				aModuleInfo = toAdd.get(i);
				System.out.println("   Add module " + aModuleInfo.toString());
				mi = PamModuleInfo.findModuleInfo(aModuleInfo.className);
				if (mi == null || mi.canCreate() == false) {
					continue;
				}
				addModule(mi, aModuleInfo.unitName);
			}
		}
		/*
		 *  try to get everything in the right order
		 *  Needs a LUT which converts the current order
		 *  into the required order, i.e. the first element
		 *  of the LUT will be the current position of the 
		 *  unit we want to come first. 
		 */
		int[] orderLUT = new int[getNumControlledUnits()];
		PamControlledUnit aUnit;
		int currentPos;
		int n = Math.min(orderLUT.length, usedModuleInfo.size());
		int nFound = 0;
		for (int i = 0; i < orderLUT.length; i++) {
			orderLUT[i] = i;
		}
		int temp;
		for (int i = 0; i < n; i++) {
			aModuleInfo = usedModuleInfo.get(i);
			try {
				moduleClass = Class.forName(aModuleInfo.className);
			} catch (ClassNotFoundException e) {
				System.out.println(String.format("The module with class %s is not available",
						aModuleInfo.className));
				continue;
			}
			aUnit = findControlledUnit(moduleClass, aModuleInfo.unitName);
			currentPos = pamControlledUnits.indexOf(aUnit);
			if (currentPos >= 0) {
				temp = orderLUT[nFound];
				orderLUT[nFound] = currentPos;
				orderLUT[currentPos] = temp;
				nFound++;
			}
		}
		//		reOrderModules(orderLUT);

		/*
		 * Now try to give each module it's settings. 
		 */
		initializationComplete = true;

		// now loop all processes and call setupProcess
		setupProcesses();
		if (initialiseNow) {
			notifyModelChanged(INITIALIZATION_COMPLETE);
		}
		PamSettingManager.getInstance().loadSettingsGroup(settingsGroup, true);
		loadingOldSettings = false;


	}

	/**
	 * Load settings for all modules in this group, then 
	 * export as XML. 
	 * @param settingsGroup
	 */
	public void exportSettingsAsXML(PamSettingsGroup settingsGroup) {
		loadOldSettings(settingsGroup, false);
		this.exportGeneralXMLSettings((JFrame) getMainFrame(), settingsGroup.getSettingsTime());
		//		exportDecimusXMLSettings(settingsGroup.getSettingsTime());
		//now do it with XML encoder to see what it's like ...
		//		String fName = String.format("XMLEncoded_%s.xml", PamCalendar.formatFileDateTime(settingsGroup.getSettingsTime()));
		//		File f = new File(fName);
		//		XMLEncoder encoder=null;
		//		try{
		//		encoder=new XMLEncoder(new BufferedOutputStream(new FileOutputStream(fName)));
		//		}catch(FileNotFoundException fileNotFound){
		//			System.out.println("ERROR: While Creating or Opening the File dvd.xml");
		//		}
		//		Iterator<PamControlledUnitSettings> it = settingsGroup.getUnitSettings().iterator();
		//		while(it.hasNext()) {
		//			PamControlledUnitSettings set = it.next();
		//			encoder.writeObject(set);
		//		}
		//		encoder.close();
	}

	/**
	 * Get the name of the psf or database used to contain settings
	 * for this run. 
	 * @return name of psf or database
	 */
	public String getPSFName() {
		switch (runMode) {
		case RUN_NORMAL:
		case RUN_REMOTE:
		case RUN_NETWORKRECEIVER:
			String fn = PamSettingManager.getInstance().getSettingsFileName();
			if (fn == null) {
				return null;
			}
			File aFile = new File(fn);
			return aFile.getAbsolutePath();
		case RUN_MIXEDMODE:
		case RUN_PAMVIEW:
			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
			if (dbc == null) {
				return null;
			}
			return dbc.getDatabaseName();
		}
		return null;
	}	

	/**
	 * Get the name of the psf or database used to contain settings
	 * for this run. 
	 * @return name of psf or database
	 */
	public String getPSFNameWithPath() {
		switch (runMode) {
		case RUN_NORMAL:
		case RUN_REMOTE:
		case RUN_NETWORKRECEIVER:
			String fn = PamSettingManager.getInstance().getSettingsFileName();
			if (fn == null) {
				return null;
			}
			File aFile = new File(fn);
			return aFile.getAbsolutePath();
		case RUN_MIXEDMODE:
		case RUN_PAMVIEW:
			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
			if (dbc == null) {
				return null;
			}
			return dbc.getDatabaseName();
		}
		return null;
	}

	public void toolBarStartButton(PamControlledUnit currentControlledUnit) {
		if (getRunMode() == RUN_PAMVIEW) {
		}
		else {
			pamStart();
		}
	}

	public void toolBarStopButton(PamControlledUnit currentControlledUnit) {
		if (getRunMode() == RUN_PAMVIEW) {
			PlaybackControl.getViewerPlayback().stopViewerPlayback();
		}
		else {
			pamStop();
			manualStop = true;
		}
	}

	/**
	 * Respond to storage options dialog. Selects whethere data 
	 * are stored in binary, database or both
	 * @param parentFrame 
	 */
	public void storageOptions(JFrame parentFrame) {
		StorageOptions.getInstance().showDialog(parentFrame);
	}

	/**
	 * Return a verbose level for debug output
	 * @return a verbose level for debug output. 
	 */
	public int getVerboseLevel() {
		return 10;
	}

	/**
	 * Create a watchdog which will run independently and keep this thing going !
	 */
	public void createWatchDog() { 
		String runnableJar = null;
		try {
			runnableJar = Pamguard.class.getProtectionDomain().getCodeSource().getLocation().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return the uidManager
	 */
	public UIDManager getUidManager() {
		return uidManager;
	}

	/**
	 * Returns the module that is currently being loaded.  If null, it means we aren't
	 * loading anything right now
	 * 
	 * @return
	 */
	public PamControlledUnit getUnitBeingLoaded() {
		return unitBeingLoaded;
	}

	/**
	 * Clears the variable holding info about the unit currently being loaded
	 */
	public void clearLoadedUnit() {
		unitBeingLoaded=null;
	}

	/**
	 * Get the Java compliance, i.e. what Java version is running. 
	 * @return integer value of java version e.g. Java 8 is return 1.8
	 */
	public double getJCompliance() {
		return JAVA_VERSION;
	}

	/**
	 * Get the Java version.  Only show the first decimal place (e.g. 12.0, not 12.0.1).  If
	 * there is no decimal place (e.g. 13) then just show that.
	 * 
	 * @return the Java version. 
	 */
	static double getVersion () {
		String version = System.getProperty("java.version");
		/*
		 *  strip down to the first non numeric character. and allow 0 or 1 decimal points.
		 *  This should pull out any valid decimal number from the front of the string.  
		 */
		int iLen = 0;
		int nDot = 0;
		for (int i = 0; i < version.length(); i++) {
			char ch = version.charAt(i);
			if (Character.isDigit(ch) || (ch == '.' && nDot == 0)) {
				iLen++;
				if (ch == '.') {
					nDot++;
				}
			}
			else {
				break;
			};
		}
		
//		int pos = version.indexOf('.');		// get the index of the first decimal
//		if (pos==-1) {						// if there is no decimal place (e.g. Java 13) then just use the full string
//			pos=version.length();
//		}
//		else {
//			pos = version.indexOf('.', pos+1);	// get the position of the second decimal
//			if (pos==-1) {						// if there is no second decimal place (e.g. Java 12.0) then just use the full string
//				pos=version.length();
//			}
//		}
		double mainVersion = 0;
		try {
			mainVersion = Double.parseDouble (version.substring (0, iLen));
		}
		catch (NumberFormatException e) {
			
		}
		
		return mainVersion;
	}

	/**
	 * Notify the PamController that progress has been made in loading something.
	 * @param progress - holds progress info. 
	 */
	public void notifyTaskProgress(PamTaskUpdate progress) {
		if (this.guiFrameManager!=null) {
			this.guiFrameManager.notifyLoadProgress(progress);
		}
	}

	/**
	 * Check if PAMGuard is being controlled through the network
	 * 
	 * @return true if network controlled, false otherwise
	 */
	public static boolean checkIfNetworkControlled() {
		if (pamBuoyGlobals.getNetworkControlPort() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the current installation folder, or null if there was a problem determining
	 * the folder location.  Note that the default file separator is included at the end
	 * of the string.
	 * @return
	 */
	public String getInstallFolder() {
		return installFolder;
	}


	/*****Convenience Functions*******/


	/****Swing GUI*****/

	/**
	 * Get the main frame if there is one. 
	 * Can be used by dialogs when no one else has
	 * sorted out a frame reference to pass to them. 
	 * @return reference to main GUI frame. 
	 */
	public static Frame getMainFrame() {
		PamController c = getInstance();
		if (c.guiFrameManager == null) {
			return null;
		}
		if (c.guiFrameManager instanceof GuiFrameManager) {
			GuiFrameManager guiFrameManager =  (GuiFrameManager) c.guiFrameManager;
			if (guiFrameManager.getNumFrames() <= 0) {
				return null;
			}
			return guiFrameManager.getFrame(0);
		}
		return null; 
	}

	@Override
	public GuiFrameManager getGuiFrameManager() {
		if (guiFrameManager instanceof GuiFrameManager) {
			return (GuiFrameManager)  guiFrameManager;
		}
		else {
			return null;
		}
	}

	public void sortFrameTitles(){
		getGuiFrameManager().sortFrameTitles();
	}


	/****FX Gui*****/
	@Deprecated
	public PamGuiManagerFX getGuiManagerFX() {
		return (PamGuiManagerFX) this.guiFrameManager;
	}

	public static Stage getMainStage() {
		return null;
	}

	/**
	 * Set the install folder. 
	 * @param installFolder
	 */
	public void setInstallFolder(String installFolder) {
		this.installFolder=installFolder; 

	}

	/**
	 * @return the globalTimeManager
	 */
	public GlobalTimeManager getGlobalTimeManager() {
		return globalTimeManager;
	}

	/**
	 * @return the manualStop
	 */
	public boolean isManualStop() {
		return manualStop;
	}

	/**
	 * @param manualStop the manualStop to set
	 */
	public void setManualStop(boolean manualStop) {
		this.manualStop = manualStop;
	}

	/**
	 * @return the watchdogComms
	 */
	public WatchdogComms getWatchdogComms() {
		return watchdogComms;
	}

	/**
	 * Get the global medium manager. This indicates whether PG is being used in air
	 * or water and handles things like dB references, default sound speed etc.
	 * 
	 * @return the global medium manager;
	 */
	public GlobalMediumManager getGlobalMediumManager() {
		return this.globalMediumManager;
	}

}
