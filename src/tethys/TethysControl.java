package tethys;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamFolders;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.SMRUEnable;
import PamUtils.PamFileFilter;
import PamView.PamGui;
import PamView.PamTabPanel;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import metadata.MetaDataContol;
import metadata.PamguardMetaData;
import nilus.Deployment;
import nilus.Helper;
import pamguard.GlobalArguments;
import tethys.TethysState.StateType;
import tethys.calibration.CalibrationHandler;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.DBXMLQueries;
import tethys.dbxml.DocumentMap;
import tethys.dbxml.ServerStatus;
import tethys.dbxml.ServerVersion;
import tethys.dbxml.TethysException;
import tethys.dbxml.TethysQueryException;
import tethys.deployment.DeploymentHandler;
import tethys.detection.DetectionsHandler;
import tethys.localization.LocalizationHandler;
import tethys.niluswraps.PDeployment;
import tethys.output.DatablockSynchInfo;
import tethys.output.TethysExportParams;
import tethys.species.ITISFunctions;
import tethys.species.SpeciesMapManager;
import tethys.swing.ProjectDeploymentsDialog;
import tethys.swing.TethysTabPanel;
import tethys.swing.XMLStringView;
import tethys.swing.documents.TethysDocumentsFrame;
import tethys.tasks.TethysTaskManager;

/**
 * Quick play with a simple system for outputting data to Tethys. At it's start
 * this is simply going to offer a dialog and have a few functions which show how
 * to access data within PAMGuard.
 * @author dg50
 *
 */
public class TethysControl extends PamControlledUnit implements PamSettings, TethysStateObserver {

	public static final String unitType = "Tethys Interface";
	public static String defaultName = "Tethys";
	public static String xmlNameSpace = "http://tethys.sdsu.edu/schema/1.0";

	private TethysExportParams tethysExportParams = new TethysExportParams();

	private DBXMLConnect dbxmlConnect;

	private TethysTabPanel tethysTabPanel;

	private DBXMLQueries dbxmlQueries;

	private ArrayList<TethysStateObserver> stateObservers;

	private Timer serverCheckTimer;

	private ServerStatus lastServerStatus;

	private ArrayList<DatablockSynchInfo> dataBlockSynchInfos;

	private DeploymentHandler deploymentHandler;
	private DetectionsHandler detectionsHandler;
	private CalibrationHandler calibrationHandler;
	private LocalizationHandler localizationHandler;
	
	private ITISFunctions itisFunctions;
	private TethysTaskManager tethysTaskManager;
	private DocumentMap documentMap;

	public TethysControl(String unitName) {
		super(unitType, unitName);
		setUnitTaskManager(tethysTaskManager = new TethysTaskManager(this));
		stateObservers = new ArrayList();
		dbxmlConnect = new DBXMLConnect(this);
		dbxmlQueries = new DBXMLQueries(this, dbxmlConnect);
		deploymentHandler = new DeploymentHandler(this);
		detectionsHandler = new DetectionsHandler(this);
		calibrationHandler = new CalibrationHandler(this);
		localizationHandler = new LocalizationHandler(this);
		documentMap = new DocumentMap(this);
		
		
		if (PamController.getInstance().isInitializationComplete()) {
			// added module, so need to do more work
			tethysTaskManager.generateTasks();
		}
		
		serverCheckTimer = new Timer(10000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerStatus serverState = checkServer();
				// check less often when it's OK to generate less debug output. 
				serverCheckTimer.setDelay(serverState.ok ? 600000 : 5000);
			}
		});
		serverCheckTimer.setInitialDelay(0);
		PamSettingManager.getInstance().registerSettings(this);
		addStateObserver(this);

		if (PamController.getInstance().isInitializationComplete()) {
			// must be adding module later on ...
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					initializationStuff();
				}
			});
		}
	}

	/**
	 * Get DBXML Connector. This class contains all the functions that are needed
	 * to talk to the database.
	 * @return DBXML functions.
	 */
	public DBXMLConnect getDbxmlConnect() {
		return dbxmlConnect;
	}
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return createTethysMenu(parentFrame);
	}

	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		// TODO Auto-generated method stub
		return super.createFileMenu(parentFrame);
	}

	/**
	 * Make a menu. Can go either in File or Settings. TBD.
	 * @param parentFrame
	 * @return
	 */
	public JMenu createTethysMenu(Frame parentFrame) {
		JMenu tethysMenu = new JMenu("Tethys");
//		JMenuItem tethysExport = new JMenuItem("Export ...");
//		tethysMenu.add(tethysExport);
//		tethysExport.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				tethysExport(parentFrame);
//			}
//		});
		JMenuItem menuItem;
		menuItem = new JMenuItem("Open client in browser");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openTethysClient();
			}
		});
		
		tethysMenu.add(menuItem);
		menuItem = new JMenuItem("Open temp document folder");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openTempDocuments();
			}
		});
		tethysMenu.add(menuItem);
		
		
		JMenuItem collections = new JMenu("Collections");
		Collection[] mainCollections = Collection.mainList();
		for (int i = 0; i < mainCollections.length; i++) {
			Collection col = mainCollections[i];
			menuItem = new JMenuItem("Open " + col.collectionName() + " collection in browser");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openTethysCollection(col);
				}
			});
			collections.add(menuItem);
		}
		
		tethysMenu.add(collections);
		tethysMenu.addSeparator();
		JMenuItem showDeps = new JMenuItem("Show project deployments");
		showDeps.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showProjectDeploymentsDialog();
			}
		});
		tethysMenu.add(showDeps);
		
//		JMenuItem cals = new JMenuItem("Export calibrations");
//		cals.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				calibrationHandler.exportAllCalibrations();
//			}
//		});
//		tethysMenu.add(cals);
		
		tethysMenu.addSeparator();
		JMenuItem mapItem = new JMenuItem("Export species maps ...");
		mapItem.setToolTipText("Export species maps (save PAMGuard species names to ITIS codes mapping to file for import into other configurations)");
				mapItem.addActionListener(SpeciesMapManager.getInstance().getExportAction(parentFrame));
		tethysMenu.add(mapItem);
		
		mapItem = new JMenuItem("Import species maps ...");
		mapItem.setToolTipText("Import species maps (PAMGuard species names to ITIS code mappings exported from other configurations)");
		mapItem.addActionListener(SpeciesMapManager.getInstance().getImportAction(parentFrame));
		tethysMenu.add(mapItem);
		
		if (SMRUEnable.isDevEnable()) {
			JMenu menu = new JMenu("Offline tasks");
			int n = tethysTaskManager.addMenuItems(menu);
			if (n > 0) {
				tethysMenu.add(menu);
			}
		}
		
		return tethysMenu;
	}

	public JMenuBar getTabSpecificMenuBar(Frame parentFrame, JMenuBar standardMenu, PamGui pamGui) {
		JMenuItem aMenu;
		// start by making a completely new copy.
		//		if (clickTabMenu == null) {
		JMenuBar tabMenu = standardMenu;
		try {
		for (int i = 0; i < tabMenu.getMenuCount(); i++) {
			if (tabMenu.getMenu(i).getText().equals("Settings")) {
				//clickTabMenu.remove(clickTabMenu.getMenu(i));

				aMenu = createDetectionMenu(parentFrame);
				String txt = aMenu.getText();
				tabMenu.add(aMenu, i+1);


				break;
			}
		}
		}
		catch (Exception e) {
			return standardMenu;
		}
		//		}
		return tabMenu;
	}

	protected void openTempDocuments() {
		File tempFolder = dbxmlConnect.checkTempFolder();
		if (tempFolder == null) {
			WarnOnce.showWarning("Tethys Error", "Unable to obtain a temporary folder name", WarnOnce.WARNING_MESSAGE);
			return;
		}
		try {
//			String cmd = "explorer.exe /select," + tempFolder.getAbsolutePath() + File.separator;
//			Runtime.getRuntime().exec(cmd);
			Desktop.getDesktop().open(tempFolder);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void showProjectDeploymentsDialog() {
		ProjectDeploymentsDialog.showDialog(getGuiFrame(), this);
	}

	/**
	 * Get a list of datablocks that can be exported to Tethys. 
	 * @return
	 */
	public ArrayList<PamDataBlock> getExportableDataBlocks() {
		ArrayList<PamDataBlock> sets = new ArrayList<>();
		ArrayList<PamDataBlock> allDataBlocks = getPamConfiguration().getDataBlocks();
		for (PamDataBlock aDataBlock : allDataBlocks) {
			if (aDataBlock.getTethysDataProvider(this) != null) {
				sets.add(aDataBlock);
			}
		}
		return sets;
	}

	/**
	 * Get the synchronisation info for all datablocks.
	 * This list should be static, but check it in case something has been
	 * added or removed.
	 * @return
	 */
	public ArrayList<DatablockSynchInfo> getSynchronisationInfos() {
		if (dataBlockSynchInfos == null) {
			dataBlockSynchInfos = new ArrayList<>();
		}
		ArrayList<PamDataBlock> dataBlocks = getExportableDataBlocks();
		// check all datablocks are in there ...
		for (PamDataBlock aBlock : dataBlocks) {
			if (findDatablockSynchInfo(aBlock) == null) {
				dataBlockSynchInfos.add(new DatablockSynchInfo(this, aBlock));
			}
		}
		// and remove any which are no longer there.
		for (DatablockSynchInfo synchInfo : dataBlockSynchInfos) {
			if (!dataBlocks.contains(synchInfo.getDataBlock())) {
				dataBlockSynchInfos.remove(synchInfo);
			}
		}
		countProjectDetections();

		return dataBlockSynchInfos;
	}

	public DatablockSynchInfo findDatablockSynchInfo(PamDataBlock dataBlock) {
		if (dataBlockSynchInfos == null) {
			return null;
		}
		for (DatablockSynchInfo synchInfo : dataBlockSynchInfos) {
			if (synchInfo.getDataBlock() == dataBlock) {
				return synchInfo;
			}
		}
		return null;
	}

	/**
	 * open client in the default web browser
	 */
	public void openTethysClient() {
//		String urlString = tethysExportParams.getFullServerName() + "/Client";
//		System.out.println("Opening url " + urlString);
//		URL url = null;
//		try {
//			url = new URL(urlString);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//		if (url == null) {
//			return;
//		}
//		try {
//			Desktop.getDesktop().browse(url.toURI());
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		openCollectionInBrowser("Client");
	}
	/**
	 * open client in the default web browser
	 */
	public void openTethysCollection(Collection collection) {
		if (collection == null) {
			return;
		}
		if (getTethysExportParams().listDocsInPamguard) {
			openCollectionInPAMGuard(collection);
		}
		else {
			openCollectionInBrowser(collection.collectionName());
		}
	}
	public void openCollectionInPAMGuard(Collection collection) {
		TethysDocumentsFrame.showTable(getGuiFrame(), this, collection);
	}
	
	public void openCollectionInBrowser(String collectionName) {
		String urlString = tethysExportParams.getFullServerName() + "/" + collectionName;
//		System.out.println("Opening url " + urlString);
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

	@Override
	public PamTabPanel getTabPanel() {
		if (tethysTabPanel == null) {
			tethysTabPanel = new TethysTabPanel(this);
		}
		return tethysTabPanel;
	}

	/**
	 * @return the tethysExportParams
	 */
	public TethysExportParams getTethysExportParams() {
		return tethysExportParams;
	}

//	/**
//	 * We'll probably want to
//	 * @param parentFrame
//	 */
//	protected void tethysExport(JFrame parentFrame) {
//		TethysExportParams newExportParams = TethysExportDialog.showDialog(parentFrame, this);
//		if (newExportParams != null) {
//			// dialog returns null if cancel was pressed.
//			tethysExportParams = newExportParams;
//			exportTethysData(tethysExportParams);
//		}
//	}
//
//	/**
//	 * We'll arrive here if the dialog has been opened and we want to export Tethys data.
//	 * @param tethysExportParams2
//	 */
//	private void exportTethysData(TethysExportParams tethysExportParams) {
//		TethysExporter tethysExporter = new TethysExporter(this, tethysExportParams);
//		tethysExporter.doExport();
//
//		sendStateUpdate(new TethysState(StateType.TRANSFERDATA));
//		countProjectDetections();
//		sendStateUpdate(new TethysState(StateType.NEWPAMGUARDSELECTION));
//	}

	/**
	 * Get global deployment data. This is a bit of a mess, trying to use a separate module
	 * so that the rest of PAMGuard can use it, but creating the
	 * @return
	 */
	public Deployment getGlobalDeplopymentData() {

		MetaDataContol metaControl = MetaDataContol.getMetaDataControl();
		PamguardMetaData metaData = metaControl.getMetaData();
		return metaData.getDeployment();
//		Deployment deploymentData = metaControl != null ? metaData.getDeployment() : getTethysProjectData();
//
////		deploymentData.setProject("thisIsAProject");
//////		deploymentData.setPlatform("Yay a platform");
////		deploymentData.setCruise("cruisey");
////		deploymentData.setDeploymentId(142536);
//////		deploymentData.setInstrumentId("super instrument");
////		deploymentData.setSite("in the ocean somewhere");
////		deploymentData.setRegion("ocean water");
//////		deploymentData.setInstrumentType("sensor of sorts");
//
//		return deploymentData;
	}

	/**
	 * Add a new state observer.
	 * @param stateObserver
	 */
	public void addStateObserver(TethysStateObserver stateObserver) {
		stateObservers.add(stateObserver);
	}

	/**
	 * Remove a state observer.
	 * @param stateObserver
	 * @return true if it existed.
	 */
	public boolean removeStateObserver(TethysStateObserver stateObserver) {
		return stateObservers.remove(stateObserver);
	}

	/**
	 * Send state updates around to all state observers.
	 * @param tethysState
	 */
	public void sendStateUpdate(TethysState tethysState) {
		for (TethysStateObserver stateObserver : this.stateObservers) {
			stateObserver.updateState(tethysState);
		}
	}
	/**
	 * A name for any deta selectors.
	 * @return
	 */
	public String getDataSelectName() {
		return getUnitName();
	}

	public DBXMLQueries getDbxmlQueries() {
		return dbxmlQueries;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZE_LOADDATA:
			//		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initializationStuff();
			break;
		case PamControllerInterface.HYDROPHONE_ARRAY_CHANGED:
			sendStateUpdate(new TethysState(StateType.UPDATEMETADATA));
			break;
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			tethysTaskManager.generateTasks();
			break;
		case PamControllerInterface.ADD_DATABLOCK:
		case PamControllerInterface.REMOVE_DATABLOCK:
			if (PamController.getInstance().isInitializationComplete()) {
				tethysTaskManager.generateTasks();
			}
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (PamController.getInstance().isInitializationComplete()) {
				sendStateUpdate(new TethysState(StateType.UPDATESERVER)); 
			}
			break;
		}
	}

	/**
	 * Stuff to do on initial load (initialization complete or addition of
	 * a Tethys module after initialisation).
	 */
	private void initializationStuff() {
		if (GlobalArguments.getParam(GlobalArguments.BATCHVIEW) != null) {
			// don't do anything if we're in batch view. 
			return;
		}
		deploymentHandler.createPamguardOverview();
		serverCheckTimer.start();
		sendStateUpdate(new TethysState(StateType.NEWPAMGUARDSELECTION));
	}

	/**
	 * Check the server. This will send around a notification if the state
	 * has changed since the last call to this function, so it's unlikely you'll
	 * need to use the return value
	 * @return server status.
	 */
	public ServerStatus checkServer() {
		ServerStatus serverState = dbxmlConnect.pingServer();
		if (lastServerStatus == null || lastServerStatus.ok != serverState.ok) {
			lastServerStatus = serverState; // set before sending notification!
			if (serverState.ok) {
				// check the version number. 
				String versionErr = checkServerVersion();
				if (versionErr != null) {
					WarnOnce.showWarning(getGuiFrame(), "Tethys Server Warning", versionErr, WarnOnce.WARNING_MESSAGE);
				}
			}
			sendStateUpdate(new TethysState(StateType.UPDATESERVER));
		}
//		lastServerStatus = serverState;
		return serverState;
	}

	/**
	 * Check server version information. 
	 * @return
	 */
	private String checkServerVersion() {
		ServerVersion version = dbxmlConnect.getServerVersion();
		if (version == null) {
			Float minVer = ServerVersion.MINSERVERVERSION;
			return String.format("You appear to be running an early version of the Tethys Sever. Please ensure you upgrade to version %s or above", minVer.toString());
		}
		if (version.getVersionNo() < ServerVersion.MINSERVERVERSION) {
//			Float curVer = version.getVersionNo();
			Float minVer = ServerVersion.MINSERVERVERSION;
			return String.format("You appear to be running Tethys Sever V%s. Please ensure you upgrade to version %s or above", version.toString(), minVer.toString());
		}
		return null;
	}
	
//	private String getServerErrorMessage(ServerStatus serverState) {
//		if (serverState == null) {
//			return "No Server State information";
//		}
//		if (serverState.ok == false) {
//			return "Tethys Server not connected";
//		}
//		
//	}

	@Override
	public Serializable getSettingsReference() {
		return tethysExportParams;
	}

	@Override
	public long getSettingsVersion() {
		return TethysExportParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		tethysExportParams = (TethysExportParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch (tethysState.stateType) {
		case NEWPROJECTSELECTION:
		case EXPORTRDATA:
		case DELETEDATA:
			countProjectDetections();
			break;
		case NEWPAMGUARDSELECTION:
			documentMap.clearMap();
			break;
		}
	}

//	/**
//	 * Count project detections in a worker thread;
//	 */
//	public void countProjectDetectionsT() {
//		sendStateUpdate(new TethysState(StateType.EXPORTRDATA, Collection.Detections));
//		PamWorker<String> worker = new PamWorker<String>(new PamWorkWrapper<String>() {
//	
//			@Override
//			public String runBackgroundTask(PamWorker<String> pamWorker) {
//				countProjectDetections();
//				return null;
//			}
//	
//			@Override
//			public void taskFinished(String result) {
//				sendStateUpdate(new TethysState(StateType.NEWPAMGUARDSELECTION, Collection.Detections));
//	
//			}
//		}, getGuiFrame(), 0, "Updating detections counts");
//		worker.start();
//	}

	/**
	 * Count project detections in the current thread. 
	 */
	private synchronized void countProjectDetections() {
		if (dataBlockSynchInfos == null) {
			return;
		}
		Deployment deplData = getGlobalDeplopymentData();
		String[] dataPrefixes = new String[dataBlockSynchInfos.size()];
		int i = 0;
		ArrayList<PDeployment> matchedDeployments = deploymentHandler.getMatchedDeployments();
		for (DatablockSynchInfo synchInfo : dataBlockSynchInfos) {
//			dataPrefixes[i] = DetectionsHandler.getDetectionsDocIdPrefix(deplData.getProject(), synchInfo.getDataBlock());
			int detectionCount = 0;
			int locDocumentCount = 0;
			int detDocumentCount = 0;
			for (PDeployment pDepl : matchedDeployments) {
				detectionCount += dbxmlQueries.countData(synchInfo.getDataBlock(), pDepl.getDocumentId());
				ArrayList<String> detectionsNames = getDbxmlQueries().getDetectionsDocuments(synchInfo.getDataBlock(), pDepl.getDocumentId());
				if (detectionsNames != null) {
					detDocumentCount += detectionsNames.size();
				}
				ArrayList<String> locDocNames = getDbxmlQueries().getLocalizationDocuments(synchInfo.getDataBlock(), pDepl.getDocumentId());
				if (locDocNames != null) {
					locDocumentCount += locDocNames.size();
				}
			}
			synchInfo.setDataCount(detectionCount);
			synchInfo.setDetectionDocumentCount(detDocumentCount);
			synchInfo.setLocalizationDocumentCount(locDocumentCount);
			
			i++;
		}
	}

	/**
	 * One stop place to get Deployment information. Will provide
	 * both information on record periods in PAMGuard and also Deployment docs in Tethys.
	 * @return set of functions for handling deployments.
	 */
	public DeploymentHandler getDeploymentHandler() {
		return deploymentHandler;
	}

	public DetectionsHandler getDetectionsHandler() {
		return detectionsHandler;
	}

	/**
	 * @return the localizationHandler
	 */
	public LocalizationHandler getLocalizationHandler() {
		return localizationHandler;
	}

	public void showException(TethysException tethysException) {
		String title = tethysException.getMessage();
		StackTraceElement[] stack = tethysException.getStackTrace();
		String msg = "";
		if (stack != null) {
			msg = "Caused in";
			for (int i = 0; i < Math.min(stack.length, 3); i++) {
				msg += "<br>" + stack[i].getClassName() + "." + stack[i].getMethodName();
			}
		}
		if (tethysException instanceof TethysQueryException) {
			TethysQueryException tqe = (TethysQueryException) tethysException;
//			msg += tqe.
		}
			
		String xml = tethysException.getXmlError();
		if (xml != null) {
			/**
			 * html can't handle the < and > in xml without getting very confused
			 * but it seems to work fine if they are replaced with their html codes.
			 */
			xml = xml.replace("<", "&lt;");
			xml = xml.replace(">", "&gt;");
			xml = xml.replace("\n", "<br>");
			msg += "<pre>"+xml+"</pre>";
		}
		WarnOnce.showWarning(title, msg, WarnOnce.WARNING_MESSAGE);
	}

	public void displayDocument(DocumentInfo docInfo) {
		String collectionName = docInfo.getCollection().collectionName();
		String docId = docInfo.getDocumentName();
		displayDocument(collectionName, docId);
	}
	/**
	 * Load a document from the database and display it in a popup window
	 * @param collection
	 * @param documentId
	 */
	public void displayDocument(String collection, String documentId) {
		String doc = getDbxmlQueries().getDocument(collection, documentId);
		if (doc == null | doc.length() == 0) {
			doc = String.format("Unable to retrieve document %s/%s from database\n", collection, documentId);
			
		}
		XMLStringView.showDialog(getGuiFrame(), collection, documentId, doc);
	}

	/**
	 * Load a document from the database and write to a file selected by the user
	 * @param collection
	 * @param documentId
	 */
	public void exportDocument(String collection, String documentId) {
		String doc = getDbxmlQueries().getDocument(collection, documentId);
		if (doc == null | doc.length() == 0) {
			String msg = String.format("Unable to retrieve document %s/%s from database\n", collection, documentId);
			WarnOnce.showWarning("Error", msg, WarnOnce.WARNING_MESSAGE);			
		}
		
		PamFileFilter fileFilter = new PamFileFilter("XML documents", ".xml");
//		fileFilter
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// make a default name based on the document id and the dataset directory. 
		String defFolder = PamFolders.getDefaultProjectFolder();
		File defFile = null;
		if (defFolder != null) {
			defFolder = String.format("%s%s%s_%s.xml", defFolder,File.separator,collection,documentId);
			defFile = new File(defFolder);
			fileChooser.setAcceptAllFileFilterUsed(true);
			fileChooser.setSelectedFile(defFile);
			
//			fileChooser.setSelectedFile(new File(String.format("%s.xml", documentId)));
//			fileChooser.set
		}
		int state = fileChooser.showSaveDialog(getGuiFrame());
		if (state != JFileChooser.APPROVE_OPTION) return;
		File newFile = fileChooser.getSelectedFile();
		if (newFile == null) return;
		newFile = PamFileFilter.checkFileEnd(newFile, "xml", true);
		if (newFile == null) {
			return;
		}
		if (newFile.exists()) {
			int ans2 = WarnOnce.showWarning(newFile.getAbsolutePath(), 
					"The file already exists. Do you want to overwrite it ?", WarnOnce.OK_CANCEL_OPTION);
			if (ans2 == WarnOnce.CANCEL_OPTION) {
				return;
			}
		}
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
			bos.write(doc.getBytes());
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the itisFunctions
	 */
	public ITISFunctions getItisFunctions() {
		if (itisFunctions == null) {
			itisFunctions = new ITISFunctions(this);
		}
		return itisFunctions;
	}

	/**
	 * Called when a detections document has been exported. 
	 * @param dataBlock
	 */
	public void exportedDetections(PamDataBlock dataBlock) {
		countProjectDetections();
//		sendStateUpdate(new TethysState(StateType.DELETEDATA));
		sendStateUpdate(new TethysState(StateType.NEWPAMGUARDSELECTION, Collection.Detections));
	}
	
	public void deletedDetections(PamDataBlock dataBlock) {
		countProjectDetections();
//		sendStateUpdate(new TethysState(StateType.DELETEDATA));
		sendStateUpdate(new TethysState(StateType.NEWPAMGUARDSELECTION, Collection.Detections));		
	}
	
	/**
	 * @return the calibrationHandler
	 */
	public CalibrationHandler getCalibrationHandler() {
		return calibrationHandler;
	}

	/**
	 * @return the lastServerStatus
	 */
	public ServerStatus getLastServerStatus() {
		return lastServerStatus;
	}
	
	/**
	 * Quick way for any controls to see that the server is probably OK
	 * without actually pinging it. 
	 * @return true if last ping of server was OK
	 */
	public boolean isServerOk() {
		if (lastServerStatus == null) {
			return false;
		}
		return lastServerStatus.ok;
	}

	/**
	 * @return the documentMap
	 */
	public DocumentMap getDocumentMap() {
		return documentMap;
	}

}
