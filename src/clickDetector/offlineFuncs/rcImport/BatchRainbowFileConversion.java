package clickDetector.offlineFuncs.rcImport;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryStorageDialogPanel;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryStoreSettings;

import clickDetector.ClickBinaryDataSource;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import clickDetector.ClickParameters;
import clickDetector.RainbowFile;
import clickDetector.offlineFuncs.rcImport.RainbowDatabaseConversion.DBBrowse;
import clickDetector.offlineFuncs.rcImport.RainbowDatabaseConversion.ImportDatabase;

import PamUtils.PamFileFilter;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class BatchRainbowFileConversion extends PamDialog {

	private ClickControl clickControl;

	private BinaryStore binaryStore;

	private static BatchRainbowFileConversion singleInstance;

	private BinaryStorageDialogPanel binaryStorageDialogPanel;

	private BinaryStoreSettings binaryStoreSettings;

	private SelectFolder rainbowFolder;

	private ClickParameters clickParameters;

	private JLabel readText, writeText;

	private JProgressBar readProgress, writeProgress;

	private ConversionWorker conversionWorker;

	private String rcFolderName;

	private boolean rcSubFolders;

	private volatile boolean stopFlag;

	private ClickDetector clickDetector;

	private ClickBinaryDataSource clickBinaryDataSource;
	
	private RainbowFileMap rainbowFileMap = new RainbowFileMap();

	private JTextField databaseName;

	private JButton databaseBrowse;

	private AbstractButton importDatabase;
	
	private static final int TEXTFIELDLENGTH = 50;

	private BatchRainbowFileConversion(Window parentFrame, ClickControl clickControl, BinaryStore binaryStore) {
		super(parentFrame, "RainbowClick file conversion", false);
		this.clickControl = clickControl;
		this.binaryStore = binaryStore;

		clickDetector = clickControl.getClickDetector();
		clickBinaryDataSource = clickDetector.getClickBinaryDataSource(); 
		//			new ClickBinaryDataSource(clickDetector.getClickDataBlock(), "Clicks");

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		binaryStorageDialogPanel = new BinaryStorageDialogPanel(parentFrame, false);
		rainbowFolder = new SelectFolder("RainbowClick file location",30, true);
		JPanel dbPanel = new JPanel();
		dbPanel.setLayout(new GridBagLayout());
		dbPanel.setBorder(new TitledBorder("Rainbowclick Database"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(dbPanel, importDatabase = new JCheckBox("Import database event data"), c);
		importDatabase.addActionListener(new ImportDatabase());
		c.gridy++;
		c.gridwidth = 2;
		addComponent(dbPanel, databaseName = new JTextField(TEXTFIELDLENGTH), c);
		databaseName.setText("E:\\BeakedWhales2008\\ImportTest\\PT_CANARY_08.mdb");
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 1;
		JPanel bPanel = new JPanel(new BorderLayout());
		bPanel.add(BorderLayout.EAST, databaseBrowse = new JButton("Browse ..."));
		addComponent(dbPanel, bPanel, c);
		databaseBrowse.addActionListener(new DBBrowse());

		p.add(rainbowFolder.getFolderPanel());
		p.add(dbPanel);
		p.add(binaryStorageDialogPanel.getPanel());

		JPanel progPanel = new JPanel();
		progPanel.setBorder(new TitledBorder("Conversion Progress"));
		progPanel.setLayout(new BoxLayout(progPanel, BoxLayout.Y_AXIS));
		progPanel.add(readText = new JLabel("Idle",SwingConstants.LEFT));
		progPanel.add(readProgress = new JProgressBar());
		progPanel.add(writeText = new JLabel("Idle"));
		progPanel.add(writeProgress = new JProgressBar());
		p.add(progPanel);

		getOkButton().setText("Start");
		getCancelButton().setText("Close");

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setDialogComponent(p);
	}

	public static ClickParameters showDialog(Window owner, ClickControl clickControl, BinaryStore binaryStore) {
		if (singleInstance == null || clickControl != singleInstance.clickControl 
				|| binaryStore != singleInstance.binaryStore) {
			singleInstance = new BatchRainbowFileConversion(owner, clickControl, binaryStore);
		}
		singleInstance.clickParameters = clickControl.getClickParameters();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clickParameters;
	}

	@Override
	public void cancelButtonPressed() {
		if (conversionWorker != null) {
			stopFlag = true;
		}
		else {
			setVisible(false);
		}
	}

	private void setParams() {
		rainbowFolder.setFolderName(clickParameters.storageDirectory);		
		binaryStoreSettings = binaryStore.getBinaryStoreSettings();
		binaryStorageDialogPanel.setParams(binaryStoreSettings);
	}

	@Override
	public boolean getParams() {

		binaryStoreSettings = binaryStore.getBinaryStoreSettings();
		if (binaryStorageDialogPanel.getParams(binaryStoreSettings) == false) {
			return false;
		}
		rcFolderName = rainbowFolder.getFolderName(true);
		if (rcFolderName == null) {
			return false;
		}
		rcSubFolders = rainbowFolder.isIncludeSubFolders();

		// should be good to go !
		// do everything in a Swing Worker to keep the GUI free. 
		conversionWorker = new ConversionWorker();
		getCancelButton().setText("Cancel");
		stopFlag = false;
		conversionWorker.execute();



		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	class ImportDatabase implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
	private void enableControls() {
		getOkButton().setEnabled(conversionWorker == null);
		databaseBrowse.setEnabled(importDatabase.isSelected());
		databaseName.setEnabled(importDatabase.isSelected());
		
	}
	class DBBrowse implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			browseDatabase();
		}
	}

	private void browseDatabase() {
		PamFileFilter fileFilter = new PamFileFilter("Microsoft Access Database", ".mdb");
		fileFilter.addFileType(".accdb");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(fileFilter);
		if (databaseName.getText() != null) {
			fileChooser.setSelectedFile(new File(databaseName.getText()));
		}
		else {
			fileChooser.setSelectedFile(new File("E:\\BeakedWhales2008\\Canaries\\PT_CANARY_08.mdb"));
		}
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showOpenDialog(getOwner());
		if (state == JFileChooser.APPROVE_OPTION) {
			File currFile = fileChooser.getSelectedFile();
			databaseName.setText(currFile.getAbsolutePath());
		}

	}


	class ConversionWorker extends SwingWorker<Integer, ConversionProgress> implements DBConvertProgress{

		private ArrayList<File> clickFiles;

		private PamFileFilter clkFileFilter;

		private RainbowDatabseConverter dbConverter;

		@Override
		protected Integer doInBackground() throws Exception {
			boolean clicksOK = importClicks();
			if (clicksOK && importDatabase.isSelected()) {
				importDatabaseEvents();
			}
			return null;
		}
		private boolean importClicks() {
			try {
				publish(new ConversionProgress(ConversionProgress.STATE_COUNTFILES,-1,-1,-1,-1,null));
				rainbowFileMap.clear();
				/*
				 *  develop a list of files in the rainbowFolder. 
				 */
				File fileFolder = new File(rcFolderName);
				if (fileFolder.exists() == false) {
					return false;
				}
				if (fileFolder.isDirectory() == false) {
					return false;
				}
				clkFileFilter = new PamFileFilter("RainbowClick files", ".clk");
				clkFileFilter.setAcceptFolders(true);
				clickFiles = new ArrayList<File>();
				addFilesToList(fileFolder);
				int totalFiles = clickFiles.size();
				publish(new ConversionProgress(ConversionProgress.STATE_RUNNING, totalFiles, -1, 0, -1,null));
				int filesProcessed = 0;
				for (int i = 0; i < totalFiles; i++) {
					if (stopFlag) {
						return false;
					}
					if (processFile(i) == false) {
						break;
					}
					filesProcessed++;
				}

				publish(new ConversionProgress(ConversionProgress.STATE_DONE, totalFiles, 0, 0, 0, null));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return true;

		}

		private boolean processFile(int iFile) {
			try {
			File clickFile = clickFiles.get(iFile);
			publish(new ConversionProgress(ConversionProgress.STATE_RUNNING, 
					clickFiles.size(), iFile+1, -1, -1, clickFile));
			RainbowFile rainbowFile = new RainbowFile(clickDetector);
			if (rainbowFile.openClickStorage(clickFile) == false) {
				return false;
			}
			int nSections = rainbowFile.getNumSections();
			for (int i = 0; i < nSections; i++) {
				if (processFileSection(rainbowFile, clickFile, iFile, i) == false) {
					return false;
				}
			}

			// 7.
			rainbowFile.closeClickStorage();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		private boolean processFileSection(RainbowFile rainbowFile, File clickFile, int iFile, int iSection) {
			/*
			 * 1. Open the rainbowClick file
			 * 2. Create and open the pgdf file
			 * 3. write header to pgdf
			 * 4. write clicks
			 * 5. write footer
			 * 6. create index file 
			 * 7. close Rainbow File
			 */

			// 1. open file
			rainbowFile.gotoSectionData(iSection);
			long startTimeMillis = rainbowFile.getFileStartTime();
			long endTimeMillis = rainbowFile.getFileEndTime();
			int totalClicks = rainbowFile.getNClicksInFile();
			int updateOften = Math.max(1,totalClicks/50);

			// get the binary store to open the output file. 
			BinaryOutputStream binaryOutputStream = new BinaryOutputStream(binaryStore, 
					clickDetector.getOutputClickData());
			binaryOutputStream.openOutputFiles(startTimeMillis);
			rainbowFileMap.addMapPoint(rainbowFile.getRainbowFile().getName(), 
					iSection, binaryOutputStream.getMainFileName());
			binaryOutputStream.writeHeader(startTimeMillis, System.currentTimeMillis());
			binaryOutputStream.writeModuleHeader();

			// read all the clicks.
			int nClicks = 0;
			rainbowFile.moveToClicks();
			ClickDetection click;
			clickDetector.getOutputClickData().clearAll();
			boolean exitLoop = false;
			while ((click = rainbowFile.getNextClick()) != null) {
				switch (click.getDataType()) {
				case RainbowFile.HEADER_CLICK:
					clickBinaryDataSource.saveData(click);
					nClicks ++;
					break;
				case RainbowFile.HEADER_NOISE:
					continue;
				case RainbowFile.HEADER_SECTION:
					exitLoop = true;
					break;
				default:
					break;
				}
				if (exitLoop) {
					break;
				}
				if (nClicks == totalClicks || nClicks%updateOften == 0) {
					publish(new ConversionProgress(ConversionProgress.STATE_RUNNING, 
							clickFiles.size(), iFile+1, totalClicks, nClicks, clickFile));
				}
			}
			// close the binary store file. 
			binaryOutputStream.writeFooter(endTimeMillis, System.currentTimeMillis(), BinaryFooter.END_RUNSTOPPED);
			binaryOutputStream.closeFile();
			binaryOutputStream.createIndexFile();

			return true;
		}

		private void addFilesToList(File fileFolder) {
			File[] newFiles = fileFolder.listFiles(clkFileFilter);
			for (int i = 0; i < newFiles.length; i++) {
				if (newFiles[i].isFile()) {
					clickFiles.add(newFiles[i]);
					if (clickFiles.size()%50 == 0) {
						publish(new ConversionProgress(ConversionProgress.STATE_COUNTFILES,
								clickFiles.size(), -1, -1, -1, null));
					}
				}
				else if (newFiles[i].isDirectory() && rcSubFolders) {
					addFilesToList(newFiles[i]);
				}
			}
		}



		private boolean importDatabaseEvents() {
			boolean ok;
			ok = checkRainbowDatabase();
			if (!ok) {
				System.out.println("Error in rainbow click database. Cannot import");
				dbConverter.closeConnection();
				return false;
			}
			
			ok = checkOutputTables();
			if (!ok) {
				System.out.println("Error in PAMGuard database. Cannot import");
				dbConverter.closeConnection();
				return false;
			}

			dbConverter.importEvents();

			dbConverter.importClicks(rainbowFileMap);
			
			return true;
		}
		private boolean checkOutputTables() {
			
			return dbConverter.checkPamguardTables();
		}



		private boolean checkRainbowDatabase() {
			String dbName = databaseName.getText();
			if (dbName == null || dbName.length() == 0) {
				return false;
			}
			File file = new File(dbName);
			if (file.exists() == false) {
				return false;
			}
			dbConverter = new RainbowDatabseConverter(clickControl, dbName);
			dbConverter.setDbConvertProgress(this);
			
			if (!dbConverter.openRainbowConnection()) {
				return false;
			}
			if (!dbConverter.checkRainbowTables()) {
				return false;
			}
			
			return true;
		}
		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			super.done();
			conversionWorker = null;
			getCancelButton().setText("Close");
			enableControls();
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<ConversionProgress> chunks) {
			int n = chunks.size();
			for (int i = 0; i < n; i++) {
				newProgressData(chunks.get(i));
			}
		}
		@Override
		public void setProgress(int state, int totalEvents, int totalClicks, int processedClicks) {
			publish(new ConversionProgress(state, totalEvents, totalClicks, processedClicks, -1, null));
			
		}
		@Override
		public boolean stop() {
			return stopFlag;
		}

	}

	public void newProgressData(ConversionProgress conversionProgress) {
		if (conversionProgress.getState() == ConversionProgress.STATE_COUNTFILES) {
			readProgress.setIndeterminate(true);
			readText.setText("Creating file list");
			writeProgress.setIndeterminate(true);
			writeText.setText("Idle");
		}
		else if (conversionProgress.getState() == ConversionProgress.STATE_RUNNING) {
			if (conversionProgress.convertedFiles < 0) {
				readProgress.setMaximum(conversionProgress.totalFiles);
				writeProgress.setValue(0);
				readProgress.setIndeterminate(false);
				writeProgress.setIndeterminate(false);
				readText.setText(String.format("Total files = %d", conversionProgress.totalFiles));
			}
			else {
				readProgress.setValue(conversionProgress.convertedFiles);
				readText.setText(String.format("Processing file %d of %d : %s", 
						conversionProgress.convertedFiles, conversionProgress.totalFiles,
						conversionProgress.currentFile.getName()));
				writeProgress.setMaximum(conversionProgress.currentFileLength);
				writeProgress.setValue(conversionProgress.currentFilePosition);
				if (conversionProgress.currentFileLength <= 0) {
					writeText.setText("No clicks in file");
				}
				else {
					writeText.setText(String.format("Written %d clicks of %d (%d%%)", 
							conversionProgress.currentFilePosition,
							conversionProgress.currentFileLength,
							(conversionProgress.currentFilePosition *100 / 
									conversionProgress.currentFileLength)));
				}

			}
			if (conversionProgress.currentFilePosition < 0) {
				writeProgress.setMaximum(conversionProgress.currentFileLength);
				writeProgress.setValue(0);
				writeProgress.setIndeterminate(false);
			}
			else {
				writeProgress.setValue(conversionProgress.currentFilePosition);
			}
		}
		else if (conversionProgress.getState() == ConversionProgress.STATE_DONE) {
			readText.setText("Done");
			writeText.setText("Idle");
		}
		else if (conversionProgress.getState() == ConversionProgress.STATE_IMPORTEVENTS) {
			readText.setText(String.format("Importing event %d", conversionProgress.totalFiles));
			readProgress.setIndeterminate(true);
			writeText.setText(" ");
			writeProgress.setValue(0);
		}
		else if (conversionProgress.getState() == ConversionProgress.STATE_IMPORTCLICKS) {
			int percent = conversionProgress.currentFileLength  * 100 / conversionProgress.convertedFiles;
			readText.setText(String.format("Importing click %d of %d (%d%%)", conversionProgress.currentFileLength,
					conversionProgress.convertedFiles, percent));
			readProgress.setIndeterminate(false);
			readProgress.setMaximum(100);
			readProgress.setValue(percent);
			writeText.setText(" ");
			writeProgress.setValue(0);
		}
		else if (conversionProgress.getState() == ConversionProgress.STATE_DONECLICKS) {
			readText.setText("Click databsae import complete");
		}

	}

	
}
