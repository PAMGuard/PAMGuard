package binaryFileStorage;

import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import PamController.AWTScheduler;
import PamController.DataIntegrityChecker;
import PamController.DataOutputStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamFolders;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamSettingsGroup;
import PamController.PamSettingsSource;
import PamController.StorageOptions;
import PamController.StorageParameters;
import PamController.fileprocessing.StoreStatus;
import PamController.status.ModuleStatus;
import PamController.status.QuickRemedialAction;
import PamModel.SMRUEnable;
import PamUtils.FileFunctions;
import PamUtils.FileParts;
import PamUtils.PamCalendar;
import PamUtils.PamFileFilter;
import PamView.PamControlledGUISwing;
import PamView.WrapperControlledGUISwing;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.background.BackgroundDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryData;
import annotation.handler.AnnotationHandler;
import backupmanager.BackupInformation;
import binaryFileStorage.backup.BinaryBackupStream;
import binaryFileStorage.checker.BinaryIntegrityChecker;
import binaryFileStorage.checker.BinaryUpdater;
import binaryFileStorage.layoutFX.BinaryStoreGUIFX;

//import com.mysql.jdbc.NdbLoadBalanceExceptionChecker;

import dataGram.Datagram;
import dataGram.DatagramManager;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import pamScrollSystem.ViewLoadObserver;
import pamViewFX.pamTask.PamTaskUpdate;
import pamguard.GlobalArguments;

/**
 * The binary store will work very much like the database in that it 
 * monitors the output of data streams and when data is added to them
 * it writes it to the binary store. 
 * <p>
 * The binary store has a number of advantages over database storage, particularly
 * when it comes to writing objects with varying record lengths such as clicks and 
 * whistle contours. 
 * <p>
 * Further information about binary storage and information on formats can be found 
 * <a href="../books/BinaryFileStructure.html">here</a>.
 * 
 * @author Doug Gillespie
 *
 */
public class BinaryStore extends PamControlledUnit implements PamSettings, 
PamSettingsSource, DataOutputStore {

	public static final String fileType = "pgdf";

	public static final String indexFileType = "pgdx";

	public static final String settingsFileType = "psfx";

	public static final String noiseFileType = "pgnf";

	/**
	 * Current format for writing files.
	 * Reved up to 7 for NOAA GPL work since we've added more noise measures to 
	 * the binary base data. 
	 */
	private static int CURRENT_FORMAT = 7;

	/**
	 * Maximum format that can be understood by this build of PAMGuard.<br>
	 * If a file is opened with a higher format, then PAMGuard will refuse to process 
	 * the file.  
	 */
	public static int MAX_UNDERSTANDABLE_FORMAT = 7;

	private PamController pamController;

	protected BinaryStoreSettings binaryStoreSettings = new BinaryStoreSettings();

	private Vector<BinaryOutputStream> storageStreams = new Vector<BinaryOutputStream>();

	private BinaryStoreProcess binaryStoreProcess;

	private BinarySettingsStorage binarySettingsStorage;

	public static final String defUnitType = "Binary Storage";

	public static final String defUnitName = "Binary Store";

	private DatagramManager datagramManager;

	private DataMapSerialiser dataMapSerialiser;

	private boolean isNetRx;

	private boolean storesOpen;

	//TODO- temp; for debug; 
	private int nUnits=0;

	// arg name for global change to store name. 
	public static final String GlobalFolderArg = "-binaryfolder";

	/**
	 * The FX GUI for binary store
	 */
	private BinaryStoreGUIFX binaryStoreGUIFX;

	/**
	 * The Swing GUI for the binary store. 
	 */
	private PamControlledGUISwing binaryStoreGUISwing;

	private BackupInformation backupInformation;

	private BinaryDataMapMaker dataMapMaker;

	private BinaryUpdater binaryUpdater;

	public static int getCurrentFileFormat() { 
		return CURRENT_FORMAT;
	}
	/**
	 * Current maximum understandable format. 
	 * @return maximum understandable file format. 
	 */
	public static int getMaxUnderstandableFormat() {
		return CURRENT_FORMAT;
	}

	/**
	 * Fixed constructor actions ...
	 */
	{
		this.pamController = PamController.getInstance();

		addPamProcess(binaryStoreProcess = new BinaryStoreProcess(this));

		isNetRx = pamController.getRunMode() == PamController.RUN_NETWORKRECEIVER;


		PamSettingManager.getInstance().registerSettings(this);

		datagramManager = new DatagramManager(this);

		dataMapSerialiser = new DataMapSerialiser(this);

		backupInformation = new BackupInformation(new BinaryBackupStream(this));
	}

	/**
	 * @param unitType
	 * @param unitName
	 */
	protected BinaryStore(String unitType, String unitName) {
		super(unitType, unitName);
	}

	public BinaryStore(String unitName) {
		// ignore the given unit name and always call it "Binary Store"
		super(defUnitType, defUnitName);

		binarySettingsStorage = new BinarySettingsStorage(this);

	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			if (GlobalArguments.getParam(GlobalArguments.BATCHVIEW) == null) {
				doInitialStoreChecks(PamController.getMainFrame());
			}
			break;
		case PamControllerInterface.CHANGED_OFFLINE_DATASTORE:
			// this gets called after INITIALIZATION_COMPLETE
			if (isViewer()) {
				checkDatagrams();
			}
			break;
		}
	}

	@Override
	public void pamToStart() {
		super.pamToStart();
		prepareStores();
		openStores();
		binaryStoreProcess.checkFileTimer();
	}

	@Override
	public void pamHasStopped() {
		super.pamHasStopped();
		closeStores();
	}

	private void prepareStores() {
		closeStores();
		createNewStores();
	}

	/**
	 * Called from the process to close and reopen each datastream in 
	 * a new file. Probably gets called about once an hour on the hour. 
	 */
	protected synchronized void reOpenStores(int endReason, long newFileTime) {

		long dataTime = newFileTime;//PamCalendar.getTimeInMillis();
		long analTime = System.currentTimeMillis();
		BinaryOutputStream dataStream;
		for (int i = 0; i < storageStreams.size(); i++) {
			dataStream = storageStreams.get(i);
			dataStream.reOpen(dataTime, analTime, endReason);
		}
	}

	private void openStores() {
		long dataTime = PamCalendar.getTimeInMillis();
		long analTime = System.currentTimeMillis();
		BinaryOutputStream outputStream;
		if (!checkOutputFolder()) {
			storesOpen = false;
			return;
		}
		for (int i = 0; i < storageStreams.size(); i++) {
			outputStream = storageStreams.get(i);
			outputStream.openOutputFiles(dataTime);
			outputStream.writeHeader(dataTime, analTime);
			outputStream.writeModuleHeader();
		}
		storesOpen = true;
	}

	private boolean checkOutputFolder() {
		return checkOutputFolder(binaryStoreSettings.getStoreLocation());
	}

	private boolean checkOutputFolder(String folderPath) {
		/*
		 * Check the output folder exists and if it doesn't, then throw up the dialog to select a folder. 
		 */
		File folder = new File(folderPath);
		if (folder.exists()) {
			return true;
		}

		BinaryStoreSettings newSettings = BinaryStorageDialog.showDialog(null, this);
		if (newSettings != null) {
			binaryStoreSettings = newSettings.clone();
			folder = new File(folderPath);
			FileFunctions.setNonIndexingBit(folder);
			return folder.exists();
		}

		return false;
	}

	private void closeStores() {
		long dataTime = PamCalendar.getTimeInMillis();
		long analTime = System.currentTimeMillis();
		BinaryOutputStream outputStream;
		for (int i = 0; i < storageStreams.size(); i++) {
			outputStream = storageStreams.get(i);
			outputStream.writeModuleFooter();
			outputStream.writeFooter(dataTime, analTime, BinaryFooter.END_RUNSTOPPED);
			outputStream.closeFile();
			outputStream.createIndexFile();
		}
		storesOpen = false;
	}

	private void createNewStores() {

		storageStreams.clear();

		//		ArrayList<BinaryDataSource> dataSources;
		ArrayList<PamDataBlock> streamingDataBlocks = getStreamingDataBlocks(true);
		if (streamingDataBlocks == null) {
			return;
		}
		int n = streamingDataBlocks.size();
		for (int i = 0; i < n; i++) {
			storageStreams.add(new BinaryOutputStream(this, streamingDataBlocks.get(i)));
		}

	}


	/**
	 * Get a list of data blocks with binary storage. If input parameter is true, then
	 * stores which have their binary storage disabled will NOT be included in the list. 
	 * @param binaryStore true if binary storage - blocks with binaryStore flag set false will not 
	 * be included. 
	 * @return list of data blocks which have binary storage. 
	 */
	public static ArrayList<PamDataBlock> getStreamingDataBlocks(boolean binaryStore) {
		ArrayList<PamDataBlock> streamingDataBlocks = new ArrayList<PamDataBlock>();
		PamDataBlock pamDataBlock;
		PamControlledUnit pcu;
		PamProcess pp;
		int nP;
		PamController pamController = PamController.getInstance();
		int nUnits = pamController.getNumControlledUnits();
		for (int i = 0; i < nUnits; i++) {
			pcu = pamController.getControlledUnit(i);
			nP = pcu.getNumPamProcesses();
			for (int j = 0; j < nP; j++) {
				pp = pcu.getPamProcess(j);
				int nDataBlocks = pp.getNumOutputDataBlocks();
				for (int d = 0; d < nDataBlocks; d++) {
					pamDataBlock = pp.getOutputDataBlock(d);
					if (pamDataBlock.getBinaryDataSource() != null ) {
						if (!binaryStore || 
								(pamDataBlock.getBinaryDataSource().isDoBinaryStore() && pamDataBlock.getShouldBinary(null))) {
							streamingDataBlocks.add(pamDataBlock);
						}
					}
				}
			}
		}
		return streamingDataBlocks;
	}

	@Override
	public Serializable getSettingsReference() {
		return binaryStoreSettings;
	}

	@Override
	public long getSettingsVersion() {
		return BinaryStoreSettings.serialVersionUID;
	}

	//	@Override
	//	public String getUnitName() {
	//		return "Binary Store";
	//	}
	//
	//	@Override
	//	public String getUnitType() {
	//		return unitType;
	//	}

	/**
	 * Get the name of the current storage folder and 
	 * check the folder exists, if necessary, creating it. 
	 * @param timeStamp current time in milliseconds. 
	 * @param addSeparator if true, add a path separator character to the
	 * end of the path. 
	 * @return true if folder exists (or has been created)
	 */
	public String getFolderName(long timeStamp, boolean addSeparator) {
		String fileSep = FileParts.getFileSeparator();
		String folderName = new String(binaryStoreSettings.getStoreLocation());
		if (binaryStoreSettings.datedSubFolders) {
			folderName += fileSep + PamCalendar.formatFileDate(timeStamp);

			// now check that that folder exists. 
			File folder = FileFunctions.createNonIndexedFolder(folderName);
			if (folder == null || !folder.exists()) {
				return null;
			}
		}
		if (addSeparator) {
			folderName += fileSep;
		}
		return folderName;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		binaryStoreSettings = ((BinaryStoreSettings) pamControlledUnitSettings.getSettings()).clone();
		/*
		 * Then check to see if there is a command line override of the currently stored folder name. 
		 */
		String globFolder = GlobalArguments.getParam(GlobalFolderArg);
		if (globFolder != null) {
			boolean ok = checkGlobFolder(globFolder);
			if (ok) {
				binaryStoreSettings.setStoreLocation(globFolder); // remember it. 
			}
			else {
				System.err.println("Unable to set binary storage folder " + globFolder);
			}
		}
		return true;
	}

	public boolean checkCommandLine() {
		/*
		 * check to see if there is a command line override of the currently stored folder name. 
		 */
		String globFolder = GlobalArguments.getParam(GlobalFolderArg);
		if (globFolder == null) {
			return false;
		}
		boolean ok = checkGlobFolder(globFolder);
		if (ok) {
			binaryStoreSettings.setStoreLocation(globFolder); // remember it. 
			return true;
		}
		else {
			System.err.println("Unable to set binary storage folder " + globFolder);
			return false;
		}
	}

	/**
	 * Set and create if necessary the global folder. 
	 * @param globFolder
	 */
	private boolean checkGlobFolder(String globFolder) {
		File outFold = new File(globFolder);
		if (outFold.exists()) {
			if (outFold.isDirectory()) {
				return true; // all OK
			}
			else {
				return false; // it must be a file - that's bad !
			}
		}
		// try to create it. 
		try {
			if (outFold.mkdirs()) {
				FileFunctions.setNonIndexingBit(outFold);
				return true;
			}
			else {
				return false; // unable to make the folder. 
			}
		}
		catch (Exception e) {
			System.err.println("Can't set binary store folder: " + e.getLocalizedMessage());
			return false;
		}

	}
	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		JMenuItem m;
		m = new JMenuItem("Storage options ...");
		m.setToolTipText("Configure binary storage location and file lengths");
		m.addActionListener(new BinaryStorageOptions(parentFrame));
		JMenu settingsMenu = new JMenu(getUnitName());
		settingsMenu.add(m);

		if (isViewer || SMRUEnable.isEnable()) {
			m = new JMenuItem("Datagram Options ...");
			m.addActionListener(new DatagramOptions(parentFrame));
			settingsMenu.add(m);
		}

		m = new JMenuItem("Open Binary Folder ...");
		m.setToolTipText("Open folder in Explorer");
		m.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openBinaryFolder();
			}
		});
		settingsMenu.add(m);

		if (isViewer) {
			binaryUpdater = getBinaryUpdater();
			m = new JMenuItem("Update Old Binary Files ...");
			m.setToolTipText("Update all binary files to the latest PAMGuard file format");
			m.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					binaryUpdater.showDialog();
				}
			});
			settingsMenu.add(m);
		}

		return settingsMenu;
	}

	private BinaryUpdater getBinaryUpdater() {
		if (binaryUpdater == null) {
			binaryUpdater = new BinaryUpdater(this);
		}
		return binaryUpdater;
	}

	protected void openBinaryFolder() {
		File file = new File(binaryStoreSettings.getStoreLocation());
		if (file.exists() == false || file.isDirectory() == false) {
			return;
		}
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(file);
		} catch (IOException e1) {
			System.out.println("Unable to open folder " + file);
		}

	}

	class DatagramOptions implements ActionListener {

		private JFrame parentFrame;

		public DatagramOptions(JFrame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (datagramManager != null) {
				datagramManager.showDatagramDialog(false);
			}
		}

	}

	class BinaryStorageOptions implements ActionListener {

		JFrame parentFrame;

		public BinaryStorageOptions(JFrame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			BinaryStoreSettings newSettings = BinaryStorageDialog.showDialog(parentFrame, BinaryStore.this);
			if (newSettings != null) {
				boolean immediateChanges = binaryStoreSettings.isChanged(newSettings);
				binaryStoreSettings = newSettings.clone();
				/*
				 *  possible that storage location will have changed, so depending on mode, may have to close
				 *  and reopen some files. 
				 */
				if (immediateChanges) {
					if (storesOpen) {
						reOpenStores(BinaryFooter.END_UNKNOWN, PamCalendar.getTimeInMillis());
					}
				}

			}
		}
	}

	class NewFileTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}


	@Override
	public boolean saveStartSettings(long timeNow) {
		if (binarySettingsStorage == null) {
			return false;
		}
		return binarySettingsStorage.saveStartSettings(timeNow);
	}

	@Override
	public boolean saveEndSettings(long timeNow) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumSettings() {
		if (binarySettingsStorage == null) {
			return 0;
		}
		return binarySettingsStorage.getNumSettings();
	}

	@Override
	public PamSettingsGroup getSettings(int settingsIndex) {
		if (binarySettingsStorage == null) {
			return null;
		}
		return binarySettingsStorage.getSettings(settingsIndex);
	}

	@Override
	public String getSettingsSourceName() {
		return getUnitName();
	}

	/**
	 * @return the binaryStoreSettings
	 */
	public BinaryStoreSettings getBinaryStoreSettings() {
		return binaryStoreSettings;
	}

	/**
	 * @param binaryStoreSettings the binaryStoreSettings to set
	 */
	public void setBinaryStoreSettings(BinaryStoreSettings binaryStoreSettings) {
		this.binaryStoreSettings = binaryStoreSettings;
	}

	private void doInitialStoreChecks(Window mainFrame) {
		//		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
		makeInitialDataMap(mainFrame);
		//		}
		//		else if (isNetRx) {
		//			// act as though acquisition is about to start, i.e. open some stores. 
		//			pamToStart(); 
		//			binaryStoreProcess.pamStart();
		//		}
	}

	/**
	 * Called on PAMGuard start up to check the correct binary file storage folder is selected
	 * and subsequently handles data map creation. 
	 * @param mainFrame - the main Swing frame. Null if not a swing GUI. 
	 */
	private void makeInitialDataMap(Window mainFrame) {

		// this first operation should be fast enough that it doesn't
		// need rethreading. 
		boolean hasCommandLine = checkCommandLine();
		if (isViewer() && !hasCommandLine) {
			BinaryStoreSettings newSettings = null;
			if (PamGUIManager.isSwing()) {
				//open the swing dialog.
				newSettings = BinaryStorageDialog.showDialog(mainFrame, this);
			}
			else if (PamGUIManager.isFX()) {
				//open the FX dialog
				if (binaryStoreGUIFX ==null) {
					binaryStoreGUIFX= new BinaryStoreGUIFX(this);
				}
				newSettings =  binaryStoreGUIFX.showDialog(binaryStoreSettings);
			}

			if (newSettings != null) {
				binaryStoreSettings = newSettings.clone();
			}
		}
		//		if (SAILEnable.isEnable()) {
		//			unzipNewData();
		//		}
		if (binarySettingsStorage != null) {
			binarySettingsStorage.makeSettingsMap();
		}
		createOfflineDataMap(null);
	}

	//	/**
	//	 * Automatically check the binary store folders for tar / zipped folders which will automatically
	//	 * be unpacked and their files copied to the binary data store. 
	//	This is now all done in a more general ZipUnpacker class which handles recordings as well as binary data
	//	 */
	//	private int unzipNewData() {
	//		FileList fileList = new FileList();
	//		String[] fileEnd = new String[1];
	//		fileEnd[0] = ".gz";
	//		ArrayList<File> files = fileList.getFileList(binaryStoreSettings.getStoreLocation(), fileEnd, true);
	//		if (files == null){ 
	//			return 0;
	//		}
	//		System.out.println(String.format("Found %d gz files", files.size()));
	//		for (File aFile:files) {
	//			File tarFile = unzipNewData(aFile);
	//			if (tarFile != null) {
	//				unTarNewData(tarFile);
	//			}
	//		}
	//		return files.size();
	//	}
	//
	//	/**
	//	 * Pull files out of a tar archive and add to binary data. 
	//	 * @param tarFile
	//	 */
	//	private int unTarNewData(File tarFile) {
	//		InputStream is;
	//		TarArchiveInputStream tarInputStream = null;
	//		try {
	//			is = new FileInputStream(tarFile);
	//		} catch (FileNotFoundException e) {
	//			System.out.println("Unable to find tar archive file: " + tarFile);
	//			return 0;
	//		}
	//		try {
	//			tarInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
	//		} catch (ArchiveException e) {
	//			System.out.println("Unable to open tar input stream from file " + tarFile);
	//		}
	//		TarArchiveEntry entry = null; 
	//	    try {
	//			while ((entry = (TarArchiveEntry)tarInputStream.getNextEntry()) != null) {
	//				File nextFile = new File(binaryStoreSettings.getStoreLocation(), entry.getName());
	//			    if (entry.isDirectory()) {
	//			    	System.out.println("New directory: " + nextFile.getAbsolutePath());
	//			    	nextFile.mkdirs();
	//			    	continue;
	//			    }
	//			    else {
	//			    	if (nextFile.exists()) {
	//				    	System.out.println("New file already exists. Won't overwrite: " + nextFile.getAbsolutePath());
	//			    		continue; // file already exists. 
	//			    	}
	//			    	System.out.println("Unpack tar file: " + nextFile.getAbsolutePath());
	//			    	/*
	//			    	 *  check the path on the file name since none of the files seem to 
	//			    	 *  be being recognised as folders so don't fire the above isDirectory() 
	//			    	 */
	//			    	String folderName = nextFile.getPath();
	//			    	int lastSep = folderName.lastIndexOf('\\');
	//			    	if (lastSep > 0) {
	//			    		File folder = new File(folderName.substring(0, lastSep));
	//			    		if (folder.exists() == false) {
	//			    			folder.mkdirs();
	//			    		}
	//			    	}
	//			    	OutputStream fileStream = new FileOutputStream(nextFile);
	//			    	org.apache.commons.compress.utils.IOUtils.copy(tarInputStream, fileStream);
	//			    	fileStream.close();
	//			    }
	//			}
	//			tarInputStream.close();
	//		} catch (IOException e1) {
	//			e1.printStackTrace();
	//		}
	//	    try {
	//			tarInputStream.close();
	//		    is.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		return 0;
	//	}
	//
	//	/**
	//	 * Unzip a single file and add to the binary data store. 
	//	 * @param aFile
	//	 */
	//	private File unzipNewData(File aFile) {
	//		String outName = aFile.getAbsolutePath();
	//		String binData = "binData-";
	//		int dateStart = outName.lastIndexOf(binData);
	//		if (dateStart < 0) {
	//			return null;
	//		}
	//		int lastDot = outName.lastIndexOf(".gz");
	//		if (lastDot < 0) {
	//			return null;
	//		}		
	//		outName = outName.substring(0, lastDot);
	//		File outFile = new File(outName);
	//		String dateStr = outName.substring(dateStart+binData.length());
	//		long fileDate = 0;
	//		try {
	//			fileDate = Long.valueOf(dateStr);
	//		}
	//		catch (NumberFormatException e) {
	//			System.err.println("Unable to find date in file name " + outName);
	//			return null;
	//		}
	//		if (fileDate <= binaryStoreSettings.lastZippedFileDate) {
	//			return null;
	//		}
	//		binaryStoreSettings.lastZippedFileDate = fileDate;
	//
	//		if (outFile.exists()) {
	//			return outFile;
	//		}
	//		
	//		try {
	//			InputStream is = new GZIPInputStream(new FileInputStream(aFile));
	//			FileOutputStream out = new FileOutputStream(outFile);
	//			byte[] buffer = new byte[1024];
	//			int bytesRead = 1;
	//			while(bytesRead > 0) {
	//				bytesRead = is.read(buffer);
	//				if (bytesRead > 0) {
	//					out.write(buffer, 0, bytesRead);
	//				}
	//			}
	//
	//			is.close();
	//			out.close();
	//		} catch (IOException e) {
	//			System.err.println("Error with gz file: " + e.getLocalizedMessage());
	//			return null;
	//		}
	//		return outFile;
	//	}

	/* (non-Javadoc)
	 * @see PamController.OfflineDataSource#createDataMap()
	 */
	@Override
	public void createOfflineDataMap(Window parentFrame) {
		if (!isViewer) {
			/*
			 * fudge May 2020 - will eventually allow some historic map making. 
			 */
			ArrayList<PamDataBlock> streams = getStreamingDataBlocks(true);
			createBinaryDataMaps(streams);
			return;
		}
		/*
		 *  do this by opening a dialog in the middle of the frame
		 *  which is modal so nothing else can be executed by the 
		 *  operator. Then do the work in a SwingWorker, which sends 
		 *  updates to the dialog to display progress, then close the 
		 *  dialog. 
		 */
		dataMapMaker = new BinaryDataMapMaker(this);
		AWTScheduler.getInstance().scheduleTask(dataMapMaker);

	}

	private BinaryMapMakingDialog binaryMapDialog;

	class BinaryDataMapMaker extends SwingWorker<Integer, BinaryMapMakeProgress> {

		private BinaryStore binaryStore;

		private BinaryStoreSettings viewerSettings;


		/**
		 * @param binaryStore
		 */
		public BinaryDataMapMaker(BinaryStore binaryStore) {
			super();
			this.binaryStore = binaryStore;
			viewerSettings = binaryStore.binaryStoreSettings;
		}


		@Override
		protected Integer doInBackground() {
			try {
				if (isViewer) {
					//					for some reason, settings get reloaded before this gets to run which reverts it back to old settings. 
					binaryStore.binaryStoreSettings = viewerSettings;
				}
				ArrayList<PamDataBlock> streams = getStreamingDataBlocks(true);

				File serFile = new File(binaryStoreSettings.getStoreLocation() + FileParts.getFileSeparator() + "serialisedBinaryMap.data");
				long serFileDate = Long.MAX_VALUE;
				if (serFile != null && serFile.exists()) {
					serFileDate = serFile.lastModified();
				}
				createBinaryDataMaps(streams);

				publish(new BinaryMapMakeProgress(BinaryMapMakeProgress.STATUS_DESERIALIZING, null, 0, 0));
				dataMapSerialiser.loadDataMap(streams, serFile);

				BinaryHeaderAndFooter bhf;
				int nStreams = streams.size();
				if (nStreams == 0) {
					return null;
				}
				publish(new BinaryMapMakeProgress(BinaryMapMakeProgress.STATUS_COUNTING_FILES, null, 0, 0));

				List<File> fileList = listAllFiles();
				if (fileList == null) {
					return null;
				}
				int nFiles = fileList.size();
				int updateAt = Math.max(nFiles/100,1);
				int fileIndex;
				int nNew = 0;
				for (int i = 0; i < nFiles; i++) {
					if (i%updateAt == 0){
						publish(new BinaryMapMakeProgress(BinaryMapMakeProgress.STATUS_ANALYSING_FILES, 
								fileList.get(i).toString(), nFiles, i));
					}

					boolean killwhatever = false;
					//					if (fileList.get(i).getName().contains("Click_Detector_Click_Detector_Clicks_20140526_105200")) {
					//						System.out.println(fileList.get(i).getName());
					//						killwhatever = true;
					//					}
					//				Thread.sleep(10);
					/*
					 * go through the real data files and check that each one is included in the map
					 */
					fileIndex = dataMapSerialiser.findFile(fileList.get(i));
					if (fileIndex >= 0) {
						/*
						 * Check the date of the existing file and if 
						 * it's been modified AFTER the serialised map, re-read it. 
						 */
						long fileDate = fileList.get(i).lastModified();
						if (fileDate > serFileDate - 600000L || killwhatever) {
							dataMapSerialiser.removeFileAtIndex(fileIndex);
							removeMapPoint(fileList.get(i), streams);
						}
						else {
							// consider updating some information in the appropriate data map point in 
							// order to ensure the file has the correct path., 
							continue;
						}
					}
					bhf = getFileHeaderAndFooter(fileList.get(i));
					createMapPoint(fileList.get(i), bhf, streams);
					nNew++;
				}

				sortBinaryDataMaps(streams);

				if (nNew > 0) {
					dataMapSerialiser.setHasChanges(true);
				}
				// don't save the datamap here - need to wait until the datagrams have been created too. 
				// always save in case the datagrams have changed. 
				//					publish(new BinaryMapMakeProgress(BinaryMapMakeProgress.STATUS_SERIALIZING, null, 0, 0));
				//					System.out.println(String.format("Added %d new files to data map. Total now %d", nNew, nFiles));
				//					dataMapSerialiser.saveDataMaps(binaryStore, streams, serFile);
				//				}

				publish(new BinaryMapMakeProgress(BinaryMapMakeProgress.STATUS_IDLE, "Done", nStreams, nStreams));

			}
			catch (Exception e){
				System.out.println("Error in BinaryDataMapMaker SwingWorker thread");
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void done() {
			//System.out.println("BinaryDataMapMaker done " + this);
			super.done();
			if (PamGUIManager.isSwing()) {
				if (binaryMapDialog != null) {
					binaryMapDialog.setVisible(false);
					binaryMapDialog = null;
				}
			}
			else {
				//need to send through a done message to PG. 
				PamController.getInstance().notifyTaskProgress(new BinaryMapMakeProgress(PamTaskUpdate.STATUS_DONE, "Done", 0, 0));
			}
			PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_OFFLINE_DATASTORE);
			//			System.out.println("BinaryDataMapMaker really done " + this);
			dataMapMaker = null;

			getInegrityChecker().checkDataStore();
		}

		@Override
		protected void process(List<BinaryMapMakeProgress> chunks) {
			if (PamGUIManager.isSwing()) {
				if (binaryMapDialog == null) {
					binaryMapDialog = BinaryMapMakingDialog.showDialog(PamController.getMainFrame());
				}
				super.process(chunks);
				for (int i = 0; i < chunks.size(); i++) {
					binaryMapDialog.setProgress(chunks.get(i));
				}
			}
			else {
				super.process(chunks);
				for (int i = 0; i < chunks.size(); i++) {
					PamController.getInstance().notifyTaskProgress(chunks.get(i));
				}
			}
		}

	}


	/**
	 * Make a set of empty datamaps. One for each datablock
	 * @param streams list of data blocks to map
	 */
	private void createBinaryDataMaps(ArrayList<PamDataBlock> streams) {
		for (int i = 0; i < streams.size(); i++) {
			BinaryOfflineDataMap dm = new BinaryOfflineDataMap(this, streams.get(i));
			BinaryDataSource bds = streams.get(i).getBinaryDataSource();
			dm.setSpecialDrawing(bds.getSpecialDrawing());
			streams.get(i).addOfflineDataMap(dm);
		}
	}

	/**
	 * This should get called when the map maker has completed and will check the status of all 
	 * the datagrams. 
	 */
	private void checkDatagrams() {
		ArrayList<PamDataBlock> updateList = datagramManager.checkAllDatagrams();
		if (updateList != null && updateList.size() > 0) {
			dataMapSerialiser.setHasChanges(true);
			datagramManager.updateDatagrams(updateList);
		}
		/*
		 *  still can't save the serialised data here since the datatram Manager is going
		 *  to make the datagrams in a different thread so they won't be ready yet !  
		 */

		if (dataMapSerialiser.isHasChanges()) {
			AWTScheduler.getInstance().scheduleTask(new SaveDataMap());
		}
	}

	class SaveDataMap implements Runnable {
		@Override
		public void run() {
			//			System.out.println("Save changed serialised data map to " + dataMapSerialiser.getSerialisedFile().getAbsolutePath());
			dataMapSerialiser.saveDataMaps();
		}
	}

	private void sortBinaryDataMaps(ArrayList<PamDataBlock> streams) {
		OfflineDataMap dm;
		for (int i = 0; i < streams.size(); i++) {
			dm = streams.get(i).getOfflineDataMap(this);
			if (dm == null) {
				continue;
			}
			dm.sortMapPoints();
			dm.removeDuplicates();
		}
	}

	//	/**
	//	 * Dump all the map data into a serialised file which can be easily read back 
	//	 * in next time PAMGAURD starts. That way, it will only be necessary to 
	//	 * unpack new data map points which should massively speed up data loading
	//	 * for large data sets. 
	//	 * @param streams
	//	 */
	//	public boolean serialiseDataMaps(ArrayList<PamDataBlock> streams) {
	//		File mapFile = new File(binaryStoreSettings.storeLocation + FileParts.getFileSeparator() + "serialisedBinaryMap.data");
	//		OutputStream os;
	//		try {
	//			os = new FileOutputStream(mapFile);
	//		} catch (FileNotFoundException e) {
	//			e.printStackTrace();
	//			return false;
	//		}
	//		try {
	//			ObjectOutputStream oos = new ObjectOutputStream(os);
	//			OfflineDataMap dm;
	//			Object o;
	//			for (int i = 0; i < streams.size(); i++) {
	//				dm = streams.get(i).getOfflineDataMap(this);
	//				if (dm == null) {
	//					continue;
	//				}
	//				oos.writeObject(streams.get(i).getDataName());
	////				o = ((BinaryOfflineDataMapPoint) dm.getMapPoints().get(0)).getBinaryHeader();
	////				o = dm.getMapPoints();
	//				oos.writeObject(dm.getMapPoints());
	//			}
	//			oos.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//			return false;
	//		}
	//		return true;
	//	}


	/**
	 * List all the files in the binary storage folder
	 * @return List of files. 
	 */
	public List<File> listAllFiles() {
		ArrayList<File> fileList = new ArrayList<File>();
		String root = binaryStoreSettings.getStoreLocation();
		if (root == null) {
			return null;
		}
		File rootFolder = new File(root);
		PamFileFilter binaryDataFilter = new PamFileFilter("Binary Data Files", fileType);
		binaryDataFilter.setAcceptFolders(true);

		listDataFiles(fileList, rootFolder, binaryDataFilter);

		return fileList;
	}


	/**
	 * List all the pgdf files in the binary storage folder with the passed prefix
	 * 
	 * @return List of files. 
	 */
	public List<File> listAllFilesWithPrefix(String prefix) {
		ArrayList<File> fileList = new ArrayList<File>();
		String root = binaryStoreSettings.getStoreLocation();
		if (root == null) {
			return null;
		}
		File rootFolder = new File(root);
		PamFileFilter binaryDataFilter = new PamFileFilter("Binary Data Files", fileType);
		binaryDataFilter.addFilePrefix(prefix);
		binaryDataFilter.setAcceptFolders(true);

		listDataFiles(fileList, rootFolder, binaryDataFilter);

		return fileList;
	}


	/**
	 * List all the index files in the binary storage folder
	 * @return List of index files
	 */
	public List<File> listIndexFiles() {
		ArrayList<File> fileList = new ArrayList<File>();
		String root = binaryStoreSettings.getStoreLocation();
		if (root == null) {
			return null;
		}
		File rootFolder = new File(root);
		PamFileFilter binaryIndexFilter = new PamFileFilter("Binary Index Files", indexFileType);
		binaryIndexFilter.setAcceptFolders(true);

		listDataFiles(fileList, rootFolder, binaryIndexFilter);

		return fileList;
	}

	/**
	 * Run checks on each file. 
	 * @param file file to check. 
	 */
	public BinaryHeaderAndFooter getFileHeaderAndFooter(File file) {
		File indexFile = findIndexFile(file, true);
		BinaryHeaderAndFooter bhf = null;
		if (indexFile != null) {
			bhf = readHeadAndFoot(indexFile);
		}
		if (bhf == null || bhf.binaryHeader == null || bhf.binaryFooter == null) {
			bhf = readHeadAndFoot(file);
		}
		if (indexFile == null && bhf != null && bhf.binaryHeader != null) {
			//			writeMissingIndexFile(file, bhf);
		}

		return bhf;
	}

	String lastFailedStream = null;

	private BinaryStoreStatusFuncs binaryStoreStatusFuncs;

	public boolean removeMapPoint(File aFile, ArrayList<PamDataBlock> streams) {
		BinaryHeaderAndFooter bhf = getFileHeaderAndFooter(aFile);
		if (bhf == null || bhf.binaryHeader == null) {
			return false;
		}
		PamDataBlock parentStream = findDataStream(bhf.binaryHeader, streams);
		if (parentStream == null) {
			return false;
		}
		BinaryOfflineDataMap dm = (BinaryOfflineDataMap) parentStream.getOfflineDataMap(this);
		if (dm == null) {
			System.out.println(String.format("removeMapPoint(...): Cannot locate binary offline data map for %s %s %s",
					bhf.binaryHeader.getModuleType(), bhf.binaryHeader.getModuleName(), 
					bhf.binaryHeader.getStreamName()));
			return false;
		}
		int nRemoved = dm.removeMapPoint(aFile);
		return (nRemoved > 0);
	}
	/**
	 * Create a data map point and add it to the map. 
	 * <p>
	 * First the correct data stream (PamDataBlock) must be located
	 * then the correct map within that stream. All this searching is
	 * necessary since the files will not be read in order so we've no 
	 * idea up until now which stream it's going to be. 
	 * @param bhf binary header and footer. 
	 * @param streams list of data streams
	 * @return true if stream and map found and map point added. 
	 */
	public boolean createMapPoint(File aFile, BinaryHeaderAndFooter bhf,
			ArrayList<PamDataBlock> streams) {
		if (bhf.binaryHeader == null) {
			return false;
		}
		PamDataBlock parentStream = findDataStream(bhf.binaryHeader, streams);
		if (parentStream == null) {
			if (lastFailedStream == null || !lastFailedStream.equals(bhf.binaryHeader.getModuleName())) {
				System.out.println(String.format("No internal data stream for %s %s %s",
						bhf.binaryHeader.getModuleType(), bhf.binaryHeader.getModuleName(), 
						bhf.binaryHeader.getStreamName()));
				lastFailedStream = bhf.binaryHeader.getModuleName();
			}
			return false;
		}
		BinaryDataSource binaryDataSouce = parentStream.getBinaryDataSource();
		BinaryOfflineDataMap dm = (BinaryOfflineDataMap) parentStream.getOfflineDataMap(this);
		if (dm == null) {
			System.out.println(String.format("Cannot locate binary offline data map for %s %s %s",
					bhf.binaryHeader.getModuleType(), bhf.binaryHeader.getModuleName(), 
					bhf.binaryHeader.getStreamName()));
			return false;
		}
		ModuleHeader mh = null;
		ModuleFooter mf = null;
		if (bhf.moduleHeaderData != null) {
			mh = binaryDataSouce.sinkModuleHeader(bhf.moduleHeaderData, bhf.binaryHeader);
		}
		if (bhf.moduleFooterData != null) {
			mf = binaryDataSouce.sinkModuleFooter(bhf.moduleFooterData, bhf.binaryHeader, mh);
		}
		BinaryOfflineDataMapPoint mapPoint = new BinaryOfflineDataMapPoint(this,aFile, bhf.binaryHeader,
				bhf.binaryFooter, mh, mf, bhf.datagram);
		if (bhf.binaryFooter == null) {
			mapPoint.setEndTime(bhf.lastDataTime);
			mapPoint.setNDatas(bhf.nDatas);
		}
		dm.addDataPoint(mapPoint);

		return true;
	}

	/**
	 * Find the datastream to go with a particular file header. 
	 * @param binaryHeader binary header read in from a file
	 * @param streams Array list of available data streams
	 * @return single stream which matches the header. 
	 */
	private PamDataBlock findDataStream(BinaryHeader binaryHeader,
			ArrayList<PamDataBlock> streams) {
		int nStreams = streams.size();
		PamDataBlock aBlock;
		BinaryDataSource binaryDataSource;
		for (int i = 0; i < nStreams; i++) {
			aBlock = streams.get(i);
			binaryDataSource = aBlock.getBinaryDataSource();
			//			System.out.printf("%s,%s,%s,%s,%s,%s\n", binaryDataSource.getModuleName(), binaryHeader.getModuleName(),
			//					binaryDataSource.getModuleType(), binaryHeader.getModuleType(),
			//					binaryDataSource.getStreamName(), binaryHeader.getStreamName());
			if (binaryDataSource.getModuleName().equalsIgnoreCase(binaryHeader.getModuleName()) &&
					binaryDataSource.getModuleType().equals(binaryHeader.getModuleType()) &&
					binaryDataSource.getStreamName().equals(binaryHeader.getStreamName())){
				return aBlock;
			}
		}
		return null;
	}

	/**
	 * Read the header and footer from a binary file. 
	 * @param file
	 * @return header and footer packed up together. 
	 */
	private BinaryHeaderAndFooter readHeadAndFoot(File file) {
		/**
		 * reading in is a pain since you don't know what anything is until
		 * you've read the first two words. 
		 * Read everything into byte buffers, then can skip easily if
		 * necessary or interpret if it's interesting. 
		 */		
		BinaryHeaderAndFooter bhf = new BinaryHeaderAndFooter();
		int objectLength;
		int objectType;
		byte[] dataBytes;
		int fileFormat = -1;
		DataInputStream fileStream = null;
		try {
			fileStream = new DataInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		BinaryHeader bh = new BinaryHeader();
		//		if (file.getAbsolutePath().contains("Click_Detector_Click_Detector_Clicks_20180823_100006.pgdf")) {
		//			System.out.println(file.getAbsolutePath());
		//		}
		if (bh.readHeader(fileStream)) {
			bhf.binaryHeader = bh;
			fileFormat = bh.getHeaderFormat();
		}
		else {
			reportError("No valid header in file " + file.getAbsolutePath());
		}
		bhf.lastDataTime = bh.getDataDate();

		BinaryFooter bf = new BinaryFooter();
		//		bhf.binaryFooter = bf;
		byte[] byteData;
		int objectDataLength = 0;
		int moduleVersion = 0;
		try {
			while (true) {
				objectLength = fileStream.readInt();
				objectType = fileStream.readInt();
				if (objectType == BinaryTypes.FILE_FOOTER) {
					if (bf.readFooterData(fileStream, fileFormat)) {
						bhf.binaryFooter = bf;
					}
				}
				else if (objectType == BinaryTypes.MODULE_HEADER) {
					moduleVersion = fileStream.readInt();
					objectDataLength = fileStream.readInt();
					if (objectDataLength > 0) {
						byteData = new byte[objectDataLength];
						fileStream.read(byteData);
						bhf.moduleHeaderData = new BinaryObjectData(fileFormat, objectType, byteData, objectDataLength);
					}
				}
				else if (objectType == BinaryTypes.MODULE_FOOTER) {
					objectDataLength = fileStream.readInt();
					if (objectDataLength > 0) {
						byteData = new byte[objectDataLength];
						fileStream.read(byteData);
						bhf.moduleFooterData = new BinaryObjectData(fileFormat, objectType, byteData, objectDataLength);
					}
				}
				else if (objectType == BinaryTypes.DATAGRAM) {
					bhf.datagram = new Datagram(0);
					bhf.datagram.readDatagramData(fileStream, objectLength);
				}
				else {
					bhf.lastDataTime = fileStream.readLong();
					fileStream.skipBytes(objectLength-8-8);
					bhf.nDatas++;
				}
			}
		} 
		catch (EOFException eof) {

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fileStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bhf.binaryFooter == null) {
			//			bf.setnObjects(bhf.nDatas);
			//			bf.setDataDate(bhf.lastDataTime);
			//			bf.setAnalysisDate(System.currentTimeMillis());
			//			bf.setFileEndReason(BinaryFooter.END_CRASHED);
			bf = this.rebuildFileFooter(file);
			if (bf == null) {
				String errStr = String.format("No valid file header in %s. Cannot recreate file footer", 
						file.getName());
				System.out.println(errStr);
			}
			else {
				String errStr = String.format("No valid file footer in %s. Creating default with %d objects end time %s", 
						file.getName(), bf.getNObjects(), PamCalendar.formatDateTime(bf.getDataDate()));
				System.out.println(errStr);
			}
			bhf.binaryFooter = bf;
		}
		return bhf;
	}

	/**
	 * Public version of {@link #readHeadAndFoot(File)} method
	 * 
	 * @param file to read
	 * @return header and footer packed up together. 
	 */
	public BinaryHeaderAndFooter readHeaderAndFooter(File file) {
		return (readHeadAndFoot(file));
	}


	/**
	 * Find the index file to match a given data file. 
	 * @param dataFile data file. 
	 * @param checkExists check teh file exists and if it doens't return null
	 * @return index file to go with the data file. 
	 */
	public File findIndexFile(File dataFile, boolean checkExists) {
		//		String filePath = dataFile.getAbsolutePath();
		//		// check that the last 4 characters are "pgdf"
		//		int pathLen = filePath.length();
		//		String newPath = filePath.substring(0, pathLen-4) + indexFileType;
		File newFile = makeIndexFile(dataFile);
		if (checkExists) {
			if (!newFile.exists()) {
				return null;
			}
		}
		return newFile;
	}

	/**
	 * Find the noise file to match a given data file. 
	 * @param dataFile data file. 
	 * @param checkExists check the file exists and if it doens't return null
	 * @return index file to go with the data file. 
	 */
	public File findNoiseFile(File dataFile, boolean checkExists) {
		//		String filePath = dataFile.getAbsolutePath();
		//		// check that the last 4 characters are "pgdf"
		//		int pathLen = filePath.length();
		//		String newPath = filePath.substring(0, pathLen-4) + indexFileType;
		File newFile = swapFileType(dataFile, noiseFileType);
		if (checkExists) {
			if (!newFile.exists()) {
				return null;
			}
		}
		return newFile;
	}

	/**
	 * Create an index file (pgdx) name from a data file (pgdf) file name
	 * @param dataFile data file name
	 * @return index file name
	 */
	public File makeIndexFile(File dataFile) {
		//		String filePath = dataFile.getAbsolutePath();
		//		// should check that the last 4 characters are "pgdf", but don't 
		//		int pathLen = filePath.length();
		//		String newPath = filePath.substring(0, pathLen-4) + indexFileType;
		//		File newFile = new File(newPath);
		//		return newFile;
		return swapFileType(dataFile, indexFileType);
	}

	/**
	 * Swap a file type from whatever is on the end of datafile
	 * to newType
	 * @param dataFile existing data file, probably .pgdf
	 * @param newType new file type (should not have the . on)
	 * @return new file with changed type. 
	 */
	public File swapFileType(File dataFile, String newType) {
		String filePath = dataFile.getAbsolutePath();
		int lastDot = filePath.lastIndexOf('.');
		if (lastDot < 0) {
			filePath += ".";
			lastDot = filePath.length();
		}
		String newPath = filePath.substring(0, lastDot) + "." + newType;
		return new File(newPath);
	}

	//	/**
	//	 * rebuild the index file based on the header and footer information 
	//	 * read from a pgdf file. 
	//	 * @param file pgdf file
	//	 * @param bhf header and footer information
	//	 */
	//	private void writeMissingIndexFile(File file, BinaryHeaderAndFooter bhf) {
	//		try {
	//			
	//			DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
	//			
	//			dos.close();
	//		}
	//		catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//	}

	/**
	 * rewrite the index file. 
	 * @param dmp 
	 * @param dataBlock 
	 */
	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint offlineDataMapPoint) {
		BinaryOfflineDataMapPoint dmp = (BinaryOfflineDataMapPoint) offlineDataMapPoint;
		String fileName = dmp.getBinaryFile(this).getAbsolutePath();
		String endBit = fileName.substring(fileName.length()-4);
		String indexName = fileName.substring(0, fileName.length()-4) + "pgdx";
		//		System.out.println(fileName + " change to  " + indexName);
		File indexFile = new File(indexName);
		return rewriteIndexFile(dataBlock, offlineDataMapPoint, indexFile);
	}
	/**
	 * 
	 * @param dataBlock
	 * @param offlineDataMapPoint
	 * @param indexFile
	 * @return
	 */
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint offlineDataMapPoint, File indexFile) {
		/*
		 * First work out the index file name from the binaryFile;
		 */
		BinaryOfflineDataMapPoint dmp = (BinaryOfflineDataMapPoint) offlineDataMapPoint;
		BinaryDataSource binaryDataSource = dataBlock.getBinaryDataSource();
		if (binaryDataSource == null) {
			return false;
		}
		BinaryOutputStream outputStream = new BinaryOutputStream(this, dataBlock);

		BinaryHeader header = dmp.getBinaryHeader();
		BinaryFooter footer = dmp.getBinaryFooter();
		ModuleHeader moduleHeader = dmp.getModuleHeader();
		ModuleFooter moduleFooter = dmp.getModuleFooter();
		Datagram datagram = dmp.getDatagram();

		if (header == null) {
			return false; 
			// can't do anything if there isn't at least a header file !
		}

		outputStream.openPGDFFile(indexFile);
		outputStream.writeHeader(header.getDataDate(), header.getAnalysisDate());

		/*
		 * Always write a module header. 
		 */
		outputStream.writeModuleHeader();

		if (moduleFooter != null) {
			outputStream.writeModuleFooter(moduleFooter);
		}

		if (datagram != null) {
			outputStream.writeDatagram(datagram);
		}

		/**
		 * If the file crashed, then there is a strong possibility that the 
		 * footer won't exist, so make one and flag it as crashed,
		 */
		if (footer == null) {
			footer = new BinaryFooter(dmp.getEndTime(), System.currentTimeMillis(), 
					dmp.getNDatas(), outputStream.getFileSize());
			footer.setFileEndReason(BinaryFooter.END_CRASHED);
		}
		outputStream.writeFileFooter(footer);

		outputStream.closeFile();

		return true;
	}


	/**
	 * List all data files - get's called recursively
	 * @param fileList current fiel list - get's added to
	 * @param folder folder to search
	 * @param filter file filter
	 */
	public void listDataFiles(ArrayList<File> fileList, File folder, PamFileFilter filter) {
		File[] newFiles = folder.listFiles(filter);
		if (newFiles == null) {
			return;
		}
		for (int i = 0; i < newFiles.length; i++) {
			if (newFiles[i].isFile()) {
				fileList.add(newFiles[i]);
			}
			else if (newFiles[i].isDirectory()) {
				listDataFiles(fileList, newFiles[i].getAbsoluteFile(), filter);
			}
		}
	}

	@Override
	public String getDataSourceName() {
		return getUnitName();
	}

	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		//TODO In principle, this should load a little data just before the main data start - hard to do with binary stores 
		// though and primary need is for stuff from the database, so leave for now. 

		/**
		 * First use the map to identify a list of files which will have data 
		 * between these times. 
		 */
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) dataBlock.getOfflineDataMap(this);
		if (dataMap == null) {
			return false;
		}
		ArrayList<BinaryOfflineDataMapPoint> mapPoints = dataMap.getFileList(offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis());
		if (mapPoints.size() == 0) {
			//			System.out.println("No data to load from " + dataBlock.getDataName() + " between " +
			//					PamCalendar.formatDateTime(dataStart) + " and " + PamCalendar.formatDateTime(dataEnd));
			return true;
		}
		else {
			//			System.out.println(String.format("Loading data for %s from %d files", dataBlock.getDataName(),
			//					mapPoints.size()));
			BinaryDataSink dataSink = new DataLoadSink();
			for (int i = 0; i < mapPoints.size(); i++) {
				//				System.out.println(mapPoints.get(i).getBinaryFile().getPath());
				if (!loadData(dataBlock, mapPoints.get(i), offlineDataLoadInfo.getStartMillis(),  offlineDataLoadInfo.getEndMillis(), dataSink)) {
					break;
				}
			}
		}
		dataBlock.sortData();

		return true;
	}

	/**
	 * Load the data from a single file.
	 * <p>
	 * Generally, PAMGUARD will use the above function which loads between
	 * two times, but for some offline analysis tasks, it's convenient
	 * to scroll through file at a time in which case this function
	 * can be used. 
	 * @param dataBlock PamDataBlock to receive the data
	 * @param binaryOfflineDataMapPoint data map point
	 * @param dataStart data start
	 * @param dataEnd data end
	 * @return true if data were loaded. false if out of memory in which case
	 * the calling load function will drop out of the loop over files. 
	 */
	public boolean loadData(PamDataBlock dataBlock, BinaryOfflineDataMapPoint mapPoint,
			long dataStart, long dataEnd, BinaryDataSink dataSink) {

		BinaryHeaderAndFooter bhf = new BinaryHeaderAndFooter();
		BinaryInputStream inputStream = new BinaryInputStream(this, dataBlock);
		if (mapPoint.getBinaryFile(this).getAbsolutePath().contains("Click_Detector_Click_Detector_Clicks_20180823_100006.pgdf")) {
			System.out.println("Load file");
		}
		if (!inputStream.openFile(mapPoint.getBinaryFile(this))) {
			return false;
		}
		BinaryDataSource binarySource = dataBlock.getBinaryDataSource();
		BinaryHeader bh = inputStream.readHeader();
		if (bh==null) {
			return false; 
		}
		int inputFormat = bh.getHeaderFormat();
		dataSink.newFileHeader(bh);
		BinaryFooter bf = null;
		ModuleHeader mh = null;
		ModuleFooter mf = null;
		long objectTime;
		PamDataUnit createdUnit;
		int moduleVersion = 0;

		//				System.out.println(String.format("Loading data from file %s", mapPoint.getBinaryFile().getAbsolutePath()));
		if (bh.getHeaderFormat() > getMaxUnderstandableFormat()) {
			System.out.printf("File %s was created with a later version of PAMGuard using format %d\n", mapPoint.getBinaryFile(this), bh.getHeaderFormat());
			System.out.printf("The maximum understood file version is %d. You must upgrade to a later PAMGuard version\n", getMaxUnderstandableFormat());
			return false;
		}

		boolean passedBad = false;

		BinaryObjectData binaryObjectData;
		while ((binaryObjectData = inputStream.readNextObject(inputFormat)) != null) {
			//			if (binaryObjectData.getObjectNumber() == 371){
			//				System.out.println(binaryObjectData.toString());
			//			}
			switch(binaryObjectData.getObjectType()){
			case  BinaryTypes.MODULE_HEADER:
				mh = binarySource.sinkModuleHeader(binaryObjectData, bh);
				moduleVersion = binaryObjectData.getVersionNumber();
				dataSink.newModuleHeader(binaryObjectData, mh);
				break;
			case  BinaryTypes.MODULE_FOOTER:
				mf = binarySource.sinkModuleFooter(binaryObjectData, bh, mh);
				dataSink.newModuleFooter(binaryObjectData, mf);
				break;
			case BinaryTypes.FILE_FOOTER:
				bf = new BinaryFooter();
				if (bf.readFooterData(binaryObjectData.getDataInputStream(), inputFormat)) {
					bhf.binaryFooter = bf;
				}
				dataSink.newFileFooter(binaryObjectData, bf);
				break;
			case BinaryTypes.DATAGRAM:
				dataSink.newDatagram(binaryObjectData);
				break;
			default:
				//				if (binaryObjectData.getDataUnitBaseData().getUID() == 115140821) {
				//					System.out.println("Last at " + PamCalendar.formatDBDateTime(binaryObjectData.getDataUnitBaseData().getTimeMilliseconds()));
				//					passedBad = true;
				////					break;
				//				}
				objectTime = binaryObjectData.getTimeMilliseconds();
				//				if (passedBad) {
				//								System.out.println(binaryObjectData.getDataUnitBaseData().getTimeMilliseconds());
				//				}
				if (objectTime < dataStart) {
					continue;
				}
				if (objectTime > dataEnd) {
					//					System.out.println("Break on object at " +
					//							PamCalendar.formatDateTime(objectTime));
					break;
				}				

				/**
				 * Shift channels for binary store
				 */
				if (binaryStoreSettings.channelShift != 0) {
					binaryObjectData.shiftChannels(binaryStoreSettings.channelShift);
				}
				if (binaryObjectData.getObjectType() == BinaryTypes.BACKGROUND_DATA) {
					createdUnit = binarySource.getBackgroundBinaryWriter().unpackBackgroundData(binaryObjectData, bh, moduleVersion);
					if (createdUnit == null) {
						continue;
					}
				}
				else {
					createdUnit = binarySource.sinkData(binaryObjectData, bh, moduleVersion);
					if (createdUnit == null) {
						break;
					}
				}
				/**
				 * Need to bodge here a bit to make sure that all data units have
				 * the same base data that they were originally created with. 
				 */
				createdUnit.getBasicData().mergeBaseData(binaryObjectData.getDataUnitBaseData());

				if (createdUnit != null) {
					createdUnit.setDataUnitFileInformation(
							new DataUnitFileInformation(this, mapPoint.getBinaryFile(this), binaryObjectData.getObjectNumber()));

					unpackAnnotationData(bh.getHeaderFormat(), createdUnit, binaryObjectData, dataSink);

					if (!dataSink.newDataUnit(binaryObjectData, dataBlock, createdUnit)) {
						return false;
					}
				}
			}
		}
		bf = inputStream.getBinaryFooter();
		inputStream.closeFile();


		return true;
	}

	/**
	 * Unpack annotation data. 
	 * @param createdUnit
	 * @param binaryObjectData
	 * @param dataSink
	 */
	protected void unpackAnnotationData(int fileVersion, PamDataUnit createdUnit, BinaryObjectData binaryObjectData, BinaryDataSink dataSink) {

		//System.out.println("Hello annotation  " + binaryObjectData.getAnnotationDataLength());
		if (binaryObjectData.getAnnotationDataLength() == 0) {
			return;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getAnnotationData());
		DataInputStream dis = new DataInputStream(bis);
		try {
			int nAnnotations = dis.readShort();
			for (int i = 0; i < nAnnotations; i++) {
				int nextLength = dis.readShort();
				String nextIdCode = dis.readUTF();
				short nextVersion = dis.readShort();

				// 2020/05/13 changed next line from -6 to -8, to match BinaryDataSource.getPackedAnnotationData:
				// line 294 dos.writeShort(abd.data.length + abd.shortIdCode.length() + 2 + 4 + 2);
				// see comments just above line 294 for explanation of added numbers
				byte[] nextData = new byte[nextLength - nextIdCode.length() - 8];
				int bytesRead = dis.read(nextData);

				DataAnnotationType<?> annotationType = getAnnotationType(nextIdCode, dataSink);
				AnnotationBinaryData abd = new AnnotationBinaryData(fileVersion, nextVersion, annotationType, nextIdCode, nextData);
				DataAnnotation an = annotationType.getBinaryHandler().setAnnotationBinaryData(createdUnit, abd);
				if (an != null) {
					createdUnit.addDataAnnotation(an);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}		
	}

	private DataAnnotationType<?> getAnnotationType(String idCode, BinaryDataSink dataSink) {
		PamDataBlock parentDataBlock = null;
		DataAnnotationType<?> annotationType = null;
		if (dataSink instanceof PamDataBlock) {
			parentDataBlock = (PamDataBlock) dataSink;
			AnnotationHandler anHandler = parentDataBlock.getAnnotationHandler();
			if (anHandler != null) {
				annotationType = anHandler.findAnnotationTypeFromCode(idCode);
			}
		}
		if (annotationType == null) {
			annotationType = CentralAnnotationsList.getList().findTypeFromCode(idCode);
		}
		if (annotationType == null) {
			annotationType = CentralAnnotationsList.getDummyAnnotationType();
		}
		return annotationType;
	}

	/**
	 * Standard data sink used when file data is loaded into memory
	 * @author dg50
	 *
	 */
	private class DataLoadSink implements BinaryDataSink {

		@Override
		public void newFileHeader(BinaryHeader binaryHeader) {
		}

		@Override
		public void newModuleHeader(BinaryObjectData binaryObjectData, ModuleHeader moduleHeader) {
		}

		@Override
		public void newModuleFooter(BinaryObjectData binaryObjectData, ModuleFooter moduleFooter) {
		}

		@Override
		public void newFileFooter(BinaryObjectData binaryObjectData, BinaryFooter binaryFooter) {
		}

		@Override
		public boolean newDataUnit(BinaryObjectData binaryObjectData, PamDataBlock dataBlock, PamDataUnit dataUnit) {
			if (binaryObjectData.getObjectType() == BinaryTypes.BACKGROUND_DATA) {
				dataBlock.getBackgroundManager().addData((BackgroundDataUnit) dataUnit);
				return true;
			}
			else {
				dataBlock.addPamData(dataUnit, dataUnit.getUID());
				return checkMemory(dataBlock);
			}
		}

		@Override
		public void newDatagram(BinaryObjectData binaryObjectData) {
		}

	}

	/**
	 * Saves data in a PamDataBlock. 
	 * <p>
	 * First scans all data in the pamDataBlock and works out which 
	 * files actually need updating based on info in their DataUnitFileInformation
	 * then rewrites those files.
	 * <p>
	 * Note that data from many files may be in memory and it's also possible 
	 * that files are only partially read in, in which case, it will be 
	 * necessary to partially take data from the old file and partially 
	 * from the stuff in memory !
	 * @param pamDataBlock data block holding the data. 
	 * @return true if saved Ok. 
	 */
	@Override
	public boolean saveData(PamDataBlock pamDataBlock) {
		ArrayList<File> changedFileList = createChangedFileList(pamDataBlock);
		for (int i = 0; i < changedFileList.size(); i++) {
			if (!saveData(changedFileList.get(i), pamDataBlock)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Rewrite a specific file using data from pamDataBlock.
	 * <p> N.B. Not all data from the original file will have 
	 * been read in, so it will be necessary to take data both from
	 * the original file and from the PamDataBlock
	 * @param file file to recreate
	 * @param pamDataBlock 
	 * @return true if file recreated OK, false otherwise. 
	 */
	private boolean saveData(File file, PamDataBlock pamDataBlock) {

		BinaryDataSource binarySource = pamDataBlock.getBinaryDataSource();
		BinaryOutputStream outputStream = new BinaryOutputStream(this, pamDataBlock);
		binarySource.setBinaryStorageStream(outputStream);
		File tempFile = new File(file.getAbsolutePath() + ".tmp");

		BinaryInputStream inputStream = new BinaryInputStream(this, pamDataBlock);

		BinaryHeader binaryHeader;
		BinaryFooter binaryFooter;
		ModuleHeader mh = null;
		ModuleFooter mf = null;
		BinaryObjectData binaryObjectData;
		int oldModuleVersion = binarySource.getModuleVersion();
		int inputFormat = CURRENT_FORMAT;
		int outputFormat = CURRENT_FORMAT;

		if (!outputStream.openPGDFFile(tempFile)) {
			return reportError("Unable to open temp output file for rewriting " + 
					tempFile.getAbsolutePath()); 
		}

		if (!inputStream.openFile(file)) {
			return reportError("Unable to open data file for rewriting " + 
					file.getAbsolutePath()); 
		}
		/**
		 * Read the header from the main file, then write to the temp file. 
		 * The appropriate module will handle the Module Header since this
		 * may change. 
		 */
		binaryHeader = inputStream.readHeader();
		if (binaryHeader == null) {
			return reportError("Unable to read header from " +
					file.getAbsolutePath());
		}
		if (binaryHeader.getHeaderFormat() > getMaxUnderstandableFormat()) {
			System.out.printf("File %s was created with a later version of PAMGuard using format %d\n", file, binaryHeader.getHeaderFormat());
			System.out.printf("The maximum understood file version is %d. You must upgrade to a later PAMGuard version\n", getMaxUnderstandableFormat());
			return false;
		}
		inputFormat = binaryHeader.getHeaderFormat();
		binaryHeader.setHeaderFormat(outputFormat);

		outputStream.writeHeader(binaryHeader.getDataDate(), binaryHeader.getAnalysisDate());
		//		ModuleHeader mh = 
		byte[] moduleHeaderData = binarySource.getModuleHeaderData();
		outputStream.writeModuleHeader(moduleHeaderData);

		/**
		 * Now comes the clever bit where we have to read from the file, 
		 * but stay in synch with units which are in storage and 
		 * if any of the stored unit are flagged as changed, then 
		 * use their data instead of the data we just read from the 
		 * original file. 
		 * <p>
		 * Even if data is being taken from file, convert back into a 
		 * data unit so that the binary source can know about everything
		 * being written into the file in order to create the correct
		 * module footer. This is also extrememly important if module 
		 * data formats change - we're assuming that data will always
		 * be written in the absolutely latest format. 
		 */
		StoredUnitServer storedUnitServer = new StoredUnitServer(file, pamDataBlock);
		PamDataUnit aDataUnit;
		int n=0;
		long time = 0;
		long time2 =0;
		//need to reset counter incase we have unsynced channels. 
		int previousObjectType = Integer.MAX_VALUE;

		while((binaryObjectData = inputStream.readNextObject(inputFormat)) != null) {
			switch(binaryObjectData.getObjectType()) {
			case BinaryTypes.FILE_HEADER:
				break;
			case BinaryTypes.FILE_FOOTER:
				break;
			case BinaryTypes.MODULE_HEADER:
				mh = binarySource.sinkModuleHeader(binaryObjectData, binaryHeader);
				oldModuleVersion = binaryObjectData.getVersionNumber();
				break;
			case BinaryTypes.MODULE_FOOTER:
				break;
			case BinaryTypes.DATAGRAM:
				break;
			case BinaryTypes.BACKGROUND_DATA:
				outputStream.storeData(BinaryTypes.BACKGROUND_DATA, binaryObjectData.getDataUnitBaseData(), binaryObjectData);
				break;
			default:// it's data !
				//				if (previousObjectType == BinaryTypes.FILE_HEADER) {
				//					System.out.printf("Writing data to file with no module header in %s\n", null)
				//				}
				long t1=System.currentTimeMillis();
				aDataUnit = storedUnitServer.findStoredUnit(binaryObjectData);
				long t2=System.currentTimeMillis();
				time+=t2-t1;
				if (aDataUnit != null) {
					//System.out.println("BinaryStore: aDataUnit: Datablock: "+PamUtils.getSingleChannel(((ClickDetection) aDataUnit).getChannelBitmap()));
					long t11=System.currentTimeMillis();
					binarySource.saveData(aDataUnit);
					aDataUnit.getDataUnitFileInformation().setNeedsUpdate(false);
					long t12=System.currentTimeMillis();
					time2+=t12-t11;

					if (n%500==0){
						//						System.out.println("BinaryStore: "+n+" data units found in "+time+" ms and save in: "+time2+ "  "+nUnits+ " out of 500 saved without list reset " );
						nUnits=0;
						time=0;
						time2=0;
					}
					n++;
				}
				else {
					//					if (n%500==0){
					//						System.out.println("BinaryStore: aDataUnit = null:");
					//					}
					aDataUnit = binarySource.sinkData(binaryObjectData, binaryHeader, oldModuleVersion);
					if (aDataUnit == null) {
						continue;
					}
					aDataUnit.getBasicData().mergeBaseData(binaryObjectData.getDataUnitBaseData());
					unpackAnnotationData(binaryHeader.getHeaderFormat(), aDataUnit, binaryObjectData, null);
					binarySource.saveData(aDataUnit);
					n++;
					//					outputStream.storeData(binaryObjectData);
				}

			}
			previousObjectType = binaryObjectData.getObjectType();

		}
		//		System.out.printf("Finished saving data units in %s ...\n", tempFile.getAbsolutePath());

		byte[] moduleFooterData = binarySource.getModuleFooterData(); //saving (copying file)
		outputStream.writeModuleFooter(moduleFooterData);

		binaryFooter = inputStream.getBinaryFooter();
		if (binaryFooter == null) {
			outputStream.writeFooter(inputStream.getLastObjectTime(), binaryHeader.getAnalysisDate(),
					BinaryFooter.END_CRASHED);
		}
		else {
			outputStream.writeFooter(binaryFooter.getDataDate(), binaryFooter.getAnalysisDate(), 
					binaryFooter.getFileEndReason());
		}
		outputStream.closeFile();
		inputStream.closeFile();

		/*
		 * Now file final stage - copy the temp file in place of the 
		 * original file. 
		 */
		boolean deletedOld = false;
		try {
			deletedOld = file.delete();
		}
		catch (SecurityException e) {
			System.out.println("Error deleting old pgdf file: " + file.getAbsolutePath());
			e.printStackTrace();
		}

		boolean renamedNew = false;
		try {
			renamedNew = tempFile.renameTo(file);
		}
		catch (SecurityException e) {
			System.out.println("Error renaming new pgdf file: " + tempFile.getAbsolutePath() + 
					" to " + file.getAbsolutePath());
			e.printStackTrace();
		}
		if (!renamedNew) {
			if (!deletedOld) {
				reportError("Unable to delete " + file.getAbsolutePath());
			}
			return reportError(String.format("Unable to rename %s to %s", 
					tempFile.getAbsolutePath(), file.getAbsolutePath()));
		}

		/*
		 * And try to find and update the data map point
		 * Will need to make a module header in order to do this. 
		 */
		BinaryObjectData moduleData;
		if (moduleHeaderData != null) {
			moduleData = new BinaryObjectData(0, BinaryTypes.MODULE_HEADER,
					moduleHeaderData, moduleHeaderData.length);
			moduleData.setVersionNumber(binarySource.getModuleVersion());
			mh = binarySource.sinkModuleHeader(moduleData, binaryHeader);
		}

		if (moduleFooterData != null) {
			moduleData = new BinaryObjectData(0, BinaryTypes.MODULE_FOOTER,
					moduleFooterData, moduleFooterData.length);
			mf = binarySource.sinkModuleFooter(moduleData, binaryHeader, mh);
		}

		BinaryOfflineDataMapPoint mapPoint = findMapPoint(pamDataBlock, file);
		if (mapPoint != null) {
			Datagram oldDataGram = mapPoint.getDatagram();
			mapPoint.update(this,file, binaryHeader, binaryFooter, mh, mf, oldDataGram);
			dataMapSerialiser.setHasChanges(true);
		}

		/*
		 * Finally, write a new index file. 
		 */
		File indexFile = findIndexFile(file, false);
		outputStream.createIndexFile(indexFile, mapPoint);


		return true;
	}

	/**
	 * Update a binary file, by reading it all in, and writing it all out again in the
	 * latest format. 
	 * @param dataBlock
	 * @param srcFile
	 * @param dstFile
	 * @param b 
	 * @return
	 */
	public boolean updateBinaryFile(PamDataBlock pamDataBlock, BinaryOfflineDataMapPoint mapPoint, 
			File srcFile, File dstFile, boolean createIndexFile) {

		BinaryDataSource binarySource = pamDataBlock.getBinaryDataSource();
		BinaryOutputStream outputStream = new BinaryOutputStream(this, pamDataBlock);
		binarySource.setBinaryStorageStream(outputStream);
		File tempFile = new File(dstFile.getAbsolutePath() + ".tmp");
		/*
		 * Check the path (subfolder) of the binary output. 
		 */
		File dstFolder = tempFile.getParentFile();
		if (dstFolder.exists() == false) {
			dstFolder.mkdirs();
		}

		BinaryInputStream inputStream = new BinaryInputStream(this, pamDataBlock);

		BinaryHeader binaryHeader;
		BinaryFooter binaryFooter;
		ModuleHeader mh = null;
		ModuleFooter mf = null;
		BinaryObjectData binaryObjectData;
		int oldModuleVersion = binarySource.getModuleVersion();
		int inputFormat = CURRENT_FORMAT;
		int outputFormat = CURRENT_FORMAT;

		if (!outputStream.openPGDFFile(tempFile)) {
			return reportError("Unable to open temp output file for rewriting " + 
					tempFile.getAbsolutePath()); 
		}

		if (!inputStream.openFile(srcFile)) {
			return reportError("Unable to open data file for rewriting " + 
					srcFile.getAbsolutePath()); 
		}
		/**
		 * Read the header from the main file, then write to the temp file. 
		 * The appropriate module will handle the Module Header since this
		 * may change. 
		 */
		binaryHeader = inputStream.readHeader();
		if (binaryHeader == null) {
			return reportError("Unable to read header from " +
					srcFile.getAbsolutePath());
		}
		if (binaryHeader.getHeaderFormat() > getMaxUnderstandableFormat()) {
			System.out.printf("File %s was created with a later version of PAMGuard using format %d\n", srcFile, binaryHeader.getHeaderFormat());
			System.out.printf("The maximum understood file version is %d. You must upgrade to a later PAMGuard version\n", getMaxUnderstandableFormat());
			return false;
		}
		inputFormat = binaryHeader.getHeaderFormat();
		binaryHeader.setHeaderFormat(outputFormat);

		outputStream.writeHeader(binaryHeader.getDataDate(), binaryHeader.getAnalysisDate());
		//		ModuleHeader mh = 
		byte[] moduleHeaderData = binarySource.getModuleHeaderData();
		outputStream.writeModuleHeader(moduleHeaderData);


		PamDataUnit aDataUnit;
		int n=0;
		long time = 0;
		long time2 =0;
		//need to reset counter incase we have unsynced channels. 
		int previousObjectType = Integer.MAX_VALUE;

		while((binaryObjectData = inputStream.readNextObject(inputFormat)) != null) {
			switch(binaryObjectData.getObjectType()) {
			case BinaryTypes.FILE_HEADER:
				break;
			case BinaryTypes.FILE_FOOTER:
				break;
			case BinaryTypes.MODULE_HEADER:
				mh = binarySource.sinkModuleHeader(binaryObjectData, binaryHeader);
				oldModuleVersion = binaryObjectData.getVersionNumber();
				break;
			case BinaryTypes.MODULE_FOOTER:
				break;
			case BinaryTypes.DATAGRAM:
				break;
			case BinaryTypes.BACKGROUND_DATA:
				outputStream.storeData(BinaryTypes.BACKGROUND_DATA, binaryObjectData.getDataUnitBaseData(), binaryObjectData);
				break;
			default:// it's data !
				//			if (previousObjectType == BinaryTypes.FILE_HEADER) {
				//				System.out.printf("Writing data to file with no module header in %s\n", null)
				//			}
				long t1=System.currentTimeMillis();
				//				if (n%500==0){
				//					System.out.println("BinaryStore: aDataUnit = null:");
				//				}
				aDataUnit = binarySource.sinkData(binaryObjectData, binaryHeader, oldModuleVersion);
				if (aDataUnit == null) {
					continue;
				}
				aDataUnit.getBasicData().mergeBaseData(binaryObjectData.getDataUnitBaseData());
				unpackAnnotationData(binaryHeader.getHeaderFormat(), aDataUnit, binaryObjectData, null);
				binarySource.saveData(aDataUnit);
				n++;
				//					outputStream.storeData(binaryObjectData);

			}
			previousObjectType = binaryObjectData.getObjectType();

		}
		//	System.out.printf("Finished saving data units in %s ...\n", tempFile.getAbsolutePath());

		byte[] moduleFooterData = binarySource.getModuleFooterData(); //saving (copying file)
		outputStream.writeModuleFooter(moduleFooterData);

		binaryFooter = inputStream.getBinaryFooter();
		if (binaryFooter == null) {
			outputStream.writeFooter(inputStream.getLastObjectTime(), binaryHeader.getAnalysisDate(),
					BinaryFooter.END_CRASHED);
		}
		else {
			outputStream.writeFooter(binaryFooter.getDataDate(), binaryFooter.getAnalysisDate(), 
					binaryFooter.getFileEndReason());
		}
		outputStream.closeFile();
		inputStream.closeFile();

		/*
		 * Now file final stage - copy the temp file in place of the 
		 * original file. This may be the same file, in which case it needs to be
		 * replaced, or it might be in a new folder, in which case dstfile just needs renamed. 
		 */
		boolean sameFile = srcFile.equals(dstFile);
		if (sameFile) {
			// delete the original file. 
			boolean deletedOld = false;
			try {
				deletedOld = srcFile.delete();
			}
			catch (SecurityException e) {
				System.out.println("Error deleting old pgdf file: " + srcFile.getAbsolutePath());
				e.printStackTrace();
			}
		}

		boolean renamedNew = false;
		/*
		 *  then rename the dst file from it's temp to it's final, which may or may not have been
		 *  the source file, which will have been deleted if they were the same.  
		 */		
		try {
			renamedNew = tempFile.renameTo(dstFile);
		}
		catch (SecurityException e) {
			System.out.println("Error renaming new pgdf file: " + tempFile.getAbsolutePath() + 
					" to " + dstFile.getAbsolutePath());
			e.printStackTrace();
		}
		if (!renamedNew) {
			return reportError(String.format("Unable to rename %s to %s", 
					tempFile.getAbsolutePath(), dstFile.getAbsolutePath()));
		}
		else {
			tempFile.delete();
		}

		if (createIndexFile) {
			File indexFile = findIndexFile(dstFile, false);
			outputStream.createIndexFile(indexFile, mapPoint);
		}


		return true;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamClose()
	 */
	@Override
	public void pamClose() {
		super.pamClose();
		closeStores(); // not needed in normal mode, but needed in NR mode since may not be running. 
		/**
		 * Save the datamap which may have changed as a result of offline processing. 
		 */
		if (dataMapSerialiser != null && dataMapSerialiser.isHasChanges()) {
			System.out.println("Save serialised binary data map");
			dataMapSerialiser.saveDataMaps();
		}

	}

	/**
	 * Find a data map point for a specific file. 
	 */
	private BinaryOfflineDataMapPoint findMapPoint(PamDataBlock pamDataBlock, File file) {
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) pamDataBlock.getOfflineDataMap(this);
		if (dataMap == null) {
			return null;
		}
		return dataMap.findMapPoint(file);
	}

	/**
	 * Class to serve up the next data unit 
	 * that needs to be stored from a particular file.
	 * @author Doug Gillespie
	 *
	 */
	class StoredUnitServer {
		private PamDataBlock pamDataBlock;
		private File file;
		private ListIterator<PamDataUnit> dataUnitIterator;
		private PamDataUnit currentUnit = null;
		Integer n=0;
		Integer lastPos=0;

		public StoredUnitServer(File file, PamDataBlock pamDataBlock) {
			super();
			this.file = file;
			this.pamDataBlock = pamDataBlock;
			dataUnitIterator = pamDataBlock.getListIterator(0);
			if (dataUnitIterator.hasNext()) {
				currentUnit = dataUnitIterator.next();
			}
		}

		/**
		 * Finds a data unit which needs storing. 
		 * @param binaryObjectData
		 * @return
		 */
		public PamDataUnit findStoredUnit(BinaryObjectData binaryObjectData) {
			//			int[] lastPos=new int[PamConstants.MAX_CHANNELS]; 
			int maxSearchNumber=1000;
			int millisOver=1000;

			if (binaryObjectData.getTimeMilliseconds()<(pamDataBlock.getFirstUnit().getTimeMilliseconds()-millisOver) || 
					binaryObjectData.getTimeMilliseconds()>pamDataBlock.getLastUnit().getTimeMilliseconds()+millisOver) return null;

			/*
			 * Often, if detections are saved on a large number of channels, they're not necessarily in chronological order. 
			 * They are, however roughly in chronological order. The way this function works is to iterate through the binary file to try find the correct data unit. The class maintains it place in
			 * the binary file in order to prevent iterating through the entire file hundreds or even thousands of times. This presents a problem, if the units are slightly out of chronological order, then it is possible
			 * a data unit will be missed. If this happens, reclassification etc will not be saved. The solution here is to try finding the unit in the remainder of the file- if it's not there try from the beginning.  
			 */
			if (currentUnit == null) {
				//				return null;
				/*
				 * Need to reset this. When loading data which didn't start at the beginning 
				 * of a file, then when it comes to resave that file, the first clicks 
				 * (before the load period) will not be in memory, so this iterator will run to 
				 * it's end and currentUnit will be null. As a consequence, subsequent clicks never
				 * get found either if this returns here as it did in previous versions.  
				 */
				dataUnitIterator=pamDataBlock.getListIterator(0);
				if (dataUnitIterator.hasNext()) {
					currentUnit = dataUnitIterator.next();
				}
			}
			lastPos=dataUnitIterator.nextIndex()-1;
			//now find the data unit; 
			PamDataUnit foundUnit = null;
			foundUnit=findDataUnit(binaryObjectData, null);

			if (n>maxSearchNumber){ 
				n=100;
			}

			for (int i=0; i<n+1; i++){
				if (dataUnitIterator.hasPrevious()) dataUnitIterator.previous();
				else break;
			}
			if (dataUnitIterator.hasNext()) {
				currentUnit = dataUnitIterator.next();
			}


			//			if (nUnits%100==0 && currentUnit!=null){
			//				System.out.println("n: " + n+"   "+ currentUnit.getDatabaseIndex());
			//			}

			//if the data unit has not been found it may be earlier or later in the list. Use brute force; reset the list iterator and iterate through until the unit is found. 
			if (foundUnit==null){
				//				System.out.println("BinaryStores: Found unit is null; Resetting");
				dataUnitIterator=pamDataBlock.getListIterator(0);
				if (dataUnitIterator.hasNext()) {
					currentUnit = dataUnitIterator.next();
				}
				foundUnit=findDataUnit(binaryObjectData, null);
			}
			else  nUnits++;

			//if the foundUnit==null it's not in the binary file- the binary file has been completely searched. Continue and return the unit if it needs updating. 
			if (foundUnit != null) {
				if (foundUnit.getDataUnitFileInformation().isNeedsUpdate()) {
					return foundUnit;
				}
				else {
					//System.out.println("BinaryStores: Found unit is null after Reset");
					return null;
				}
			}

			//the unit has not been found
			return null;
		}


		private PamDataUnit findDataUnit(BinaryObjectData binaryObjectData, Integer maxNumber){
			DataUnitFileInformation fileInfo;
			PamDataUnit foundUnit = null;
			n=0;
			while (currentUnit != null) {
				fileInfo = currentUnit.getDataUnitFileInformation();

				//				System.out.println("IndexFile: "+fileInfo.getIndexInFile() + " ObjectNum: "+  binaryObjectData.getObjectNumber());
				//				System.out.println(fileInfo.getFile().equals(file));
				BinaryStore unitBinary = fileInfo.getBinaryStore();
				boolean sameBinary = (unitBinary == null && BinaryStore.this.getClass() == BinaryStore.class) 
						|| unitBinary == BinaryStore.this;

				if (sameBinary &&
						fileInfo.getIndexInFile() == binaryObjectData.getObjectNumber() &&
						fileInfo.getFile() != null && fileInfo.getFile().equals(file)) {

					if (currentUnit.getTimeMilliseconds() != binaryObjectData.getTimeMilliseconds()) {
						System.out.println("Non matching time on stored object");
						return null;
					}
					foundUnit = currentUnit;
				}

				//if we reach the maximum number of iterations return null
				if (dataUnitIterator.hasNext() && checkN(n, maxNumber)) {
					currentUnit = dataUnitIterator.next();
				}
				else {
					currentUnit = null;
				}

				if (foundUnit!=null) {
					lastPos=dataUnitIterator.nextIndex();
					return foundUnit; 
				}
				n++;
			}
			//			System.out.println("n...: "+n);
			return null;
		}
	}

	private static boolean checkN(Integer n, Integer maxNumber){
		if (maxNumber==null) return true;
		if (n<=maxNumber) return true; 
		return false;		
	}



	boolean reportError(String string) {
		System.out.println(string);
		return false;
	}

	/**
	 * Create a list of files which have data in that datablock 
	 * that has been flagged as changed. 
	 * @param pamDataBlock
	 * @return ArrayList of files that have changed data 
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<File> createChangedFileList(PamDataBlock pamDataBlock) {
		ArrayList<File> changedFiles = new ArrayList<File>();
		/*
		 *  go through the data in the data block and see which have 
		 *  data that has been flagged as changed.  
		 */
		synchronized (pamDataBlock.getSynchLock()) {
			ListIterator<PamDataUnit> iterator = pamDataBlock.getListIterator(0);
			PamDataUnit aDataUnit;
			DataUnitFileInformation fileInfo;
			File lastFile = null;
			File aFile;
			while (iterator.hasNext()) {
				aDataUnit = iterator.next();
				//				if (aDataUnit.getUpdateCount() > 0) {
				//					System.out.println(pamDataBlock.getDataName() + " update count " + aDataUnit.getUpdateCount());
				//				}
				fileInfo = aDataUnit.getDataUnitFileInformation();
				if (fileInfo == null) {
					continue;
				}
				/*
				 * Probably don't want to go on update count since this is often used just to trigger 
				 * a display redraw, e.g. if a data unit is added to a superdet, the subdet won't need
				 * to be restored in the binary file. Need to more explicityly set the fileInfo needs
				 * update flag when a data unit has changes to the extent that it needs a re-save.  
				 */
				if (!fileInfo.isNeedsUpdate()) {
					continue;
				}
				aFile = fileInfo.getFile();
				if (aFile == lastFile) {
					continue;
				}
				if (changedFiles.indexOf(aFile) >= 0) {
					continue;
				}
				/*
				 * If it gets here it's a new file for our list. 
				 */
				lastFile = aFile;
				//				System.out.println("Binary store files to alter: " +aFile);
				changedFiles.add(aFile);
			}
		}
		return changedFiles;
	}

	/**
	 * Check available memory. 
	 * If it's less than 10 megabytes, return false since we're about to 
	 * run out. 
	 * @return true if there is > 1o Meg of memory left. 
	 */
	private boolean checkMemory(PamDataBlock dataBlock) {
		return checkMemory(dataBlock, 10000000L); // standard to test to check still have 10MBytes. 
	}

	private boolean checkMemory(PamDataBlock dataBlock, long minAmount) {
		Runtime r = Runtime.getRuntime();
		long totalMemory = r.totalMemory();
		long maxMemory = r.maxMemory();
		long freeMemory = r.freeMemory();
		if (freeMemory > minAmount) {
			return true;
		}
		else if (freeMemory + totalMemory < maxMemory - minAmount) {
			return true;
		}
		// run the garbage collector and try again ...
		r.gc();

		totalMemory = r.totalMemory();
		maxMemory = r.maxMemory();
		freeMemory = r.freeMemory();
		if (freeMemory > minAmount) {
			return true;
		}
		else if (freeMemory + totalMemory < maxMemory - minAmount) {
			return true;
		}

		// not enoughmemory, so throw a warning. 
		JOptionPane.showMessageDialog(null, "System memory is getting low and no more " +
				"\ndata can be loaded. Select a shorter load time for offline data", 
				dataBlock.getDataName(), JOptionPane.ERROR_MESSAGE);

		return false;
	}

	/**
	 * Convenience function to determine whether a data block is beiung written to binary files
	 * @return only true if binary store is present and the data block subscribes to it.
	 */
	public static boolean isDataBlockBinaryOut(PamDataBlock pamDataBlock){
		StorageParameters storageParameters=StorageOptions.getInstance().getStorageParameters(); 
		boolean hasBinaryStore = pamDataBlock.getBinaryDataSource() != null;
		if (hasBinaryStore) {
			hasBinaryStore = pamDataBlock.getBinaryDataSource().isDoBinaryStore();
		}

		if (hasBinaryStore && storageParameters.isStoreBinary(pamDataBlock, true)){
			System.out.println("The PamControlledUnit "+pamDataBlock.getDataName()+ 
					" subscribed to the Binary Store");
			return true;
		}
		else return false; 
	}

	/**
	 * Find the binary store
	 * @return binary storage controller, or null if no binary storage module loaded. 
	 */
	public static BinaryStore findBinaryStoreControl() {
		BinaryStore dbc = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.getBinaryUnitType());
		return dbc;
	}

	/**
	 * Get the unit type for the binary store. 
	 * @return the binary store unit type. 
	 */
	public static String getBinaryUnitType() {
		return defUnitType;
	}

	/**
	 * @return the datagramManager
	 */
	@Override
	public DatagramManager getDatagramManager() {
		return datagramManager;
	}

	/**
	 * Reads the data objects in the passed file, and records the first/last UID, times and sample
	 * numbers.  Uses the information to create a new BinaryFooter object, and then passes that
	 * back to the calling function.  Note that this method sets the BinaryFooter's FileEndReason
	 * as {@link BinaryFooter#END_CRASHED END_CRASHED}, which is usually the case if the footer needs to be rebuilt.
	 * 
	 * @param file the binary file to examine
	 * @return a BinaryFooter object, or null if there was an error
	 */
	public BinaryFooter rebuildFileFooter(File file) {

		BinaryInputStream inputStream = new BinaryInputStream(this, null);
		if (!inputStream.openFile(file)) {
			return null;
		}

		int fileFormat = -1;
		BinaryHeader bh = inputStream.readHeader();
		if (bh != null) {
			fileFormat = bh.getHeaderFormat();
		}
		else {
			reportError("No valid header in file " + file.getAbsolutePath());
			inputStream.closeFile();
			return null;
		}

		int objCount = 0;
		long firstUID = 0;
		long lastUID = 0;
		long dataDate = 0;
		long fileEndSample = 0;
		boolean firstObject=true;
		BinaryObjectData binaryObjectData;
		BinaryFooter bf = new BinaryFooter();
		try {
			while ((binaryObjectData = inputStream.readNextObject(fileFormat)) != null) {
				switch(binaryObjectData.getObjectType()){
				case  BinaryTypes.MODULE_HEADER:
					break;
				case  BinaryTypes.MODULE_FOOTER:
					break;
				case BinaryTypes.FILE_FOOTER:
					break;
				case BinaryTypes.DATAGRAM:
					break;
				default:
					DataUnitBaseData dubd = binaryObjectData.getDataUnitBaseData();
					if (dubd == null) {
						continue;
					}
					objCount++;

					// set the UIDs
					if (firstObject) {
						firstUID = dubd.getUID()-1;
						firstObject=false;
					}
					lastUID = dubd.getUID();
					dataDate = dubd.getTimeMilliseconds();
					if (dubd.getStartSample() != null) {
						fileEndSample = dubd.getStartSample();
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Error reconstructing damaged file footer: " + e.getMessage());
		}

		bf.setnObjects(objCount);
		bf.setDataDate(dataDate);
		bf.setLowestUID(firstUID);
		bf.setHighestUID(lastUID);
		bf.setAnalysisDate(System.currentTimeMillis());
		bf.setFileEndReason(BinaryFooter.END_CRASHED);
		bf.setFileEndSample(fileEndSample);
		inputStream.closeFile();
		return bf;
	}

	/**
	 * Get the GUI for the PAMControlled unit. This has multiple GUI options 
	 * which are instantiated depending on the view type. 
	 * @param flag. The GUI type flag defined in PAMGuiManager. 
	 * @return the GUI for the PamControlledUnit unit. 
	 */
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (binaryStoreGUIFX ==null) {
				binaryStoreGUIFX= new BinaryStoreGUIFX(this);
			}
			return binaryStoreGUIFX;
		}
		if (flag==PamGUIManager.SWING) {
			if (binaryStoreGUISwing ==null) {
				binaryStoreGUISwing= new WrapperControlledGUISwing(this);	
			}
			return binaryStoreGUISwing;
		}
		return null;
	}

	@Override
	public ModuleStatus getModuleStatus() {
		boolean storeState = storesOpen;
		if (PamController.getInstance().getPamStatus() == PamController.PAM_IDLE) {
			storeState = true;
		}
		if (!storeState || binaryStoreSettings.getStoreLocation() == null) {
			ModuleStatus moduleStatus = new ModuleStatus(ModuleStatus.STATUS_ERROR, "No binary storage folder");
			moduleStatus.setRemedialAction(new QuickRemedialAction(this, "Select binary storage folder",
					new BinaryStorageOptions((JFrame) getGuiFrame())));
			return moduleStatus;
		}
		long freeSpace = -1;
		try {
			File storageFolder = new File(binaryStoreSettings.getStoreLocation());
			freeSpace = storageFolder.getFreeSpace();
		}
		catch (Exception e) {
			freeSpace = -2;
		}
		if (freeSpace < 0) {
			ModuleStatus moduleStatus = new ModuleStatus(ModuleStatus.STATUS_ERROR, "No binary storage folder");
			moduleStatus.setRemedialAction(new QuickRemedialAction(this, "Select binary storage folder",
					new BinaryStorageOptions((JFrame) getGuiFrame())));
			return moduleStatus;
		}
		else if (freeSpace < 1e9) {
			String msg = String.format("Binary storage location %s now only has %d free Megabyts of data", 
					binaryStoreSettings.getStoreLocation(), freeSpace >> 20);
			ModuleStatus moduleStatus = new ModuleStatus(ModuleStatus.STATUS_WARNING, msg);
			moduleStatus.setRemedialAction(new QuickRemedialAction(this, "Select new binary storage folder",
					new BinaryStorageOptions((JFrame) getGuiFrame())));
			return moduleStatus;
		}

		return new ModuleStatus(ModuleStatus.STATUS_OK);
	}

	@Override
	public BackupInformation getBackupInformation() {
		return backupInformation;
	}

	/**
	 * Store noise in pgdf or pgnf files. 
	 * @return where noise data are to be stored. 
	 */
	public NoiseStoreType getNoiseStore() {
		return binaryStoreSettings.getNoiseStoreType();
	}

	@Override
	public StoreStatus getStoreStatus(boolean getDetail) {
		if (binaryStoreStatusFuncs == null) {
			binaryStoreStatusFuncs = new BinaryStoreStatusFuncs(this);
		}
		return binaryStoreStatusFuncs.getStoreStatus(getDetail);
	}
	@Override
	public boolean deleteDataFrom(long timeMillis) {
		BinaryStoreDeleter storeDeleter = new BinaryStoreDeleter(this);
		return storeDeleter.deleteDataFrom(timeMillis);
	}
	@Override
	public int getOfflineState() {
		int state = super.getOfflineState();
		if (dataMapMaker != null) {
			System.out.println("Binary store is map making");
			state = Math.max(state, PamController.PAM_MAPMAKING);
		}
		if (datagramManager != null & datagramManager.getStatus()) {
			state = Math.max(state, PamController.PAM_MAPMAKING);
			System.out.println("Binary store is creating datagram");
		}
		return state;
	}

	@Override
	public String getDataLocation() {
		return binaryStoreSettings.getStoreLocation();
	}
	@Override
	public DataIntegrityChecker getInegrityChecker() {
		return new BinaryIntegrityChecker(this);
	}
	/**
	 * @return the binaryStoreProcess
	 */
	public BinaryStoreProcess getBinaryStoreProcess() {
		return binaryStoreProcess;
	}

}
