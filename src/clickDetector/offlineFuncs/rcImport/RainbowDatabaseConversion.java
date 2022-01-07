package clickDetector.offlineFuncs.rcImport;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import binaryFileStorage.BinaryStore;
import clickDetector.ClickControl;
import clickDetector.offlineFuncs.ClicksOffline;

import PamUtils.PamFileFilter;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class RainbowDatabaseConversion extends PamDialog implements DBConvertProgress{

	private static RainbowDatabaseConversion singleInstance;
	private ClickControl clickControl;
	private BinaryStore binaryStore;
	private JTextField databaseName;
	private JButton databaseBrowse;
	private JCheckBox importDatabase;
	private JLabel progressText;
	private JProgressBar progressBar;
	
	private RainbowDatabseConverter dbConverter;
	private ClicksOffline clicksOffline;
	private ConvertWorker conWorker;
	private boolean stopNow;
	
	
	private RainbowDatabaseConversion(Window parentFrame) {
		super(parentFrame, "Import RainbowClick Database", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel dbPanel = new JPanel();
		dbPanel.setLayout(new GridBagLayout());
		dbPanel.setBorder(new TitledBorder("Rainbowclick Database"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(dbPanel, importDatabase = new JCheckBox(), c);
		importDatabase.addActionListener(new ImportDatabase());
		c.gridy++;
		c.gridwidth = 2;
		addComponent(dbPanel, databaseName = new JTextField(50), c);
		databaseName.setText("E:\\BeakedWhales2008\\Canaries\\PT_CANARY_08.mdb");
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 1;
		JPanel bPanel = new JPanel(new BorderLayout());
		bPanel.add(BorderLayout.EAST, databaseBrowse = new JButton("Browse ..."));
		addComponent(dbPanel, bPanel, c);
		databaseBrowse.addActionListener(new DBBrowse());
		
		JPanel pPanel = new JPanel(new BorderLayout());
		pPanel.setBorder(new TitledBorder("Progress"));
		pPanel.add(BorderLayout.CENTER, progressText = new JLabel(" "));
		pPanel.add(BorderLayout.SOUTH, progressBar = new JProgressBar(JProgressBar.HORIZONTAL));
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		
		mainPanel.add(dbPanel);
		mainPanel.add(pPanel);
		setDialogComponent(mainPanel);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	
	

	public static void showDialog(Frame parentFrame, ClickControl clickControl,
			BinaryStore binaryStore) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new RainbowDatabaseConversion(parentFrame);
		}
		singleInstance.clickControl = clickControl;
		singleInstance.binaryStore = binaryStore;
		singleInstance.setVisible(true);
		return;
	}

	class ImportDatabase implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
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



	@Override
	public void cancelButtonPressed() {
		if (conWorker != null) {
			stopNow = true;
		}
		else {
			setVisible(false);
		}
	}

	@Override
	public boolean getParams() {
		// gets called when OK button is pressed. 
		processDatabase();
		return false;
	}

	private boolean processDatabase() {
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
		
		/**
		 * Now things might start to take some time, 
		 * so run in a worker thread. 
		 */
		conWorker = new ConvertWorker();
		enableControls();
		conWorker.execute();
		
		
		return true;
	}

	private boolean checkOutputTables() {
		clicksOffline = clickControl.getClicksOffline();
		
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

	/**
	 * Called back from swing worker thread. 
	 * @param convertWorker
	 */
	public void conWorkWorker(ConvertWorker convertWorker) {
		// do the actual work in a different thread
		// since it may take some time. 
		stopNow = false;
		
		dbConverter.importEvents();

//		dbConverter.importClicks();
		
	}



	public void conWorkDone() {
		dbConverter.closeConnection();
		dbConverter = null;		
		enableControls();
	}



	class ConvertWorker extends SwingWorker<Integer, DBConvertProgressData> {

		@Override
		protected Integer doInBackground() throws Exception {
			conWorkWorker(this);
			
			return null;
		}

		void openPublish(DBConvertProgressData pd) {
			publish(pd);
		}
		
		@Override
		protected void done() {
			super.done();
			conWorkDone();
		}


		@Override
		protected void process(List<DBConvertProgressData> list) {
			DBConvertProgressData pd = list.get(list.size()-1);
			progressBar.setValue(pd.progressPercent);
			progressText.setText(pd.message);
		}
		
	}
	
	class DBConvertProgressData {
		String message; 
		int progressPercent;
		public DBConvertProgressData(String message, int progressPercent) {
			super();
			this.message = message;
			this.progressPercent = progressPercent;
		}
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setProgress(int state, int totalEvents, int totalClicks, int processedClicks) {
		if (conWorker != null) {
//			conWorker.openPublish(new DBConvertProgressData(message, percent));
		}
	}



	@Override
	public boolean stop() {
		return stopNow;
	}
	
	void enableControls() {
		getOkButton().setEnabled(conWorker == null);
		databaseBrowse.setEnabled(importDatabase.isSelected());
		databaseName.setEnabled(importDatabase.isSelected());
	}
}
