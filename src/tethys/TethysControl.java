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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamFolders;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.PamTabPanel;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import metadata.MetaDataContol;
import metadata.deployment.DeploymentData;
import tethys.TethysState.StateType;
import tethys.calibration.CalibrationHandler;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.DBXMLQueries;
import tethys.dbxml.ServerStatus;
import tethys.dbxml.TethysException;
import tethys.dbxml.TethysQueryException;
import tethys.deployment.DeploymentHandler;
import tethys.detection.DetectionsHandler;
import tethys.niluswraps.PDeployment;
import tethys.output.DatablockSynchInfo;
//import nilus.Deployment;
import tethys.output.TethysExportParams;
import tethys.output.TethysExporter;
import tethys.output.swing.TethysExportDialog;
import tethys.species.ITISFunctions;
import tethys.species.SpeciesMapManager;
import tethys.swing.ProjectDeploymentsDialog;
import tethys.swing.TethysTabPanel;
import tethys.swing.XMLStringView;
import tethys.swing.documents.TethysDocumentsFrame;

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
	
	private ITISFunctions itisFunctions;

	public TethysControl(String unitName) {
		super(unitType, unitName);
		stateObservers = new ArrayList();
		dbxmlConnect = new DBXMLConnect(this);
		dbxmlQueries = new DBXMLQueries(this, dbxmlConnect);
		deploymentHandler = new DeploymentHandler(this);
		detectionsHandler = new DetectionsHandler(this);
		calibrationHandler = new CalibrationHandler(this);
		
		serverCheckTimer = new Timer(10000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkServer();
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
	public JMenuItem createTethysMenu(Frame parentFrame) {
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
		
		JMenuItem cals = new JMenuItem("Export calibrations");
		cals.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				calibrationHandler.exportAllCalibrations();
			}
		});
		tethysMenu.add(cals);
		
		tethysMenu.addSeparator();
		JMenuItem mapItem = new JMenuItem("Export species maps ...");
		mapItem.setToolTipText("Export all species maps (PAMGuard codes to ITIS codes to file for import into other configurations");
				mapItem.addActionListener(SpeciesMapManager.getInstance().getExportAction(parentFrame));
		tethysMenu.add(mapItem);
		
		mapItem = new JMenuItem("Import species maps ...");
		mapItem.setToolTipText("Import species maps (PAMGuard codes to ITIS codes to file for import into other configurations");
		mapItem.addActionListener(SpeciesMapManager.getInstance().getImportAction(parentFrame));
		tethysMenu.add(mapItem);
		
		return tethysMenu;
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

	public ArrayList<PamDataBlock> getExportableDataBlocks() {
		ArrayList<PamDataBlock> sets = new ArrayList<>();
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
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
	public DeploymentData getGlobalDeplopymentData() {
		PamControlledUnit aUnit = PamController.getInstance().findControlledUnit(MetaDataContol.class, null);
//		if (aUnit instanceof MetaDataContol == false || true) {
//			deployment.setProject("thisIsAProject");
//			deployment.setPlatform("Yay a platform");
//			Instrument instrument = new Instrument();
//			instrument.setType("machiney");
//			instrument.setInstrumentId("12345555");
//			deployment.setInstrument(instrument);
//			return false;
//		}

		MetaDataContol metaControl = (MetaDataContol) aUnit;
		DeploymentData deploymentData = metaControl != null ? metaControl.getDeploymentData() : getTethysProjectData();

//		deploymentData.setProject("thisIsAProject");
////		deploymentData.setPlatform("Yay a platform");
//		deploymentData.setCruise("cruisey");
//		deploymentData.setDeploymentId(142536);
////		deploymentData.setInstrumentId("super instrument");
//		deploymentData.setSite("in the ocean somewhere");
//		deploymentData.setRegion("ocean water");
////		deploymentData.setInstrumentType("sensor of sorts");

		return deploymentData;
	}

	/**
	 * Gets a copy of Deployment data stored with other Tethys params when the more
	 * general meta data provider is not present.
	 * @return
	 */
	private DeploymentData getTethysProjectData() {
		return tethysExportParams.getProjectData();
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
		}
	}

	/**
	 * Stuff to do on initial load (initialization complete or addition of
	 * a Tethys module after initialisation).
	 */
	private void initializationStuff() {
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
			sendStateUpdate(new TethysState(StateType.UPDATESERVER));
		}
		lastServerStatus = serverState;
		return serverState;
	}

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
			countProjectDetections();
			break;
		}
	}

	private void countProjectDetections() {
		if (dataBlockSynchInfos == null) {
			return;
		}
		DeploymentData deplData = getGlobalDeplopymentData();
		String[] dataPrefixes = new String[dataBlockSynchInfos.size()];
		int i = 0;
		ArrayList<PDeployment> matchedDeployments = deploymentHandler.getMatchedDeployments();
		for (DatablockSynchInfo synchInfo : dataBlockSynchInfos) {
//			dataPrefixes[i] = DetectionsHandler.getDetectionsDocIdPrefix(deplData.getProject(), synchInfo.getDataBlock());
			int detectionCount = 0;
			int documentCount = 0;
			for (PDeployment pDepl : matchedDeployments) {
				detectionCount += dbxmlQueries.countData(synchInfo.getDataBlock(), pDepl.deployment.getId());
				ArrayList<String> detectionsNames = getDbxmlQueries().getDetectionsDocuments(synchInfo.getDataBlock(), pDepl.deployment.getId());
				if (detectionsNames != null) {
					documentCount += detectionsNames.size();
				}
			}
			synchInfo.setDataCount(detectionCount);
			synchInfo.setDetectionDocumentCount(documentCount);
			
			i++;
		}
//		int[] counts = dbxmlQueries.countDataForProject(deplData.getProject(), dataPrefixes);
//		if (counts != null) {
//			for ( i = 0; i < counts.length; i++ ) {
//				dataBlockSynchInfos.get(i).setDataCount(counts[i]);
//			}
//		}
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
		sendStateUpdate(new TethysState(StateType.EXPORTRDATA, Collection.Detections));
		countProjectDetections();
		sendStateUpdate(new TethysState(StateType.NEWPAMGUARDSELECTION, Collection.Detections));
	}

	/**
	 * @return the calibrationHandler
	 */
	public CalibrationHandler getCalibrationHandler() {
		return calibrationHandler;
	}


}
