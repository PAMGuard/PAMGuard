package Acquisition;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import Acquisition.filedate.FileDateDialogStrip;
import Acquisition.layoutFX.AcquisitionPaneFX;
import Acquisition.layoutFX.DAQSettingsPane;
import Acquisition.layoutFX.FolderInputPane;
import javafx.application.Platform;
import Acquisition.pamAudio.PamAudioSystem;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettings;
import PamUtils.PamAudioFileFilter;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamUtils.worker.PamWorker;
import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;
import PamUtils.worker.filelist.WavListUser;
import PamUtils.worker.filelist.WavListWorker;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamView.panel.PamProgressBar;

/**
 * Read multiple files in sequence. Options exist to either pause and
 * restart analysis after each file, or to merge files into one long
 * continuous sound stream.
 * 
 * @author Doug Gillespie
 *
 */
public class FolderInputSystem extends FileInputSystem implements PamSettings{

	//	Timer timer;
	public static final String daqType = "File Folder Acquisition System";

	public static final String sysType = "Audio file folder or multiple files";

	private boolean running = false;

	protected ArrayList<WavFileType> allFiles = new ArrayList<WavFileType>();

	protected int currentFile;

	private PamFileFilter audioFileFilter = getFolderFileFilter();

	private Timer newFileTimer;

	private JCheckBox subFolders, mergeFiles;

	private JButton checkFiles;

	protected long eta = -1;

	private FolderInputParameters folderInputParameters;


	/**
	 * Text field for skipping initial few seconds of a file. 
	 */
	private JTextField skipSecondsField;

	@Override
	public boolean runFileAnalysis() {
		currentFileStart = System.currentTimeMillis();
		return super.runFileAnalysis();
	}

	@Override
	public boolean prepareInputFile() {
		boolean ans = super.prepareInputFile();
		if (ans == false && ++currentFile < allFiles.size()) {
			System.out.println("Failed to open sound file. Try again with file " + allFiles.get(currentFile).getName());

			PamController.getInstance().pamStop();
			PamController.getInstance().startLater();
		}
		return ans;
	}

	long currentFileStart;


	public FolderInputSystem(AcquisitionControl acquisitionControl) {
		super(acquisitionControl);
		if (folderInputParameters == null)
			setFolderInputParameters(new FolderInputParameters(getSystemType()));
		//		PamSettingManager.getInstance().registerSettings(this); //calling super already registers this in the FileInputSystem constructor
		makeSelFileList();
		newFileTimer = new Timer(1000, new RestartTimer());
		newFileTimer.setRepeats(false);
		//		timer = new Timer(1000, new TimerAction());
	}

	/**
	 * Restarts after a file has ended when processing multiple files. 
	 * 27 Jan 2011 - this now reschedules in the AWT thread
	 * @author Doug Gillespie
	 *
	 */
	class RestartTimer implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			System.out.println("Restart later time action");
			newFileTimer.stop();
			PamController.getInstance().startLater(false); //don't save settings on restarts

		}

	}
	@Override
	protected JPanel createDaqDialogPanel() {
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Select sound file folder or multiple files"));
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{100, 100, 10};
		p.setLayout(layout);
		GridBagConstraints constraints = new PamGridBagContraints();
		constraints.insets = new Insets(2,2,2,2);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		addComponent(p, fileNameCombo = new JComboBox(), constraints);
		fileNameCombo.addActionListener(this);
		fileNameCombo.setMinimumSize(new Dimension(30,2));
		fileNameCombo.addActionListener(new FileComboListener());
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		addComponent(p, subFolders = new JCheckBox("Include sub folders"), constraints);
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.EAST;
		addComponent(p, fileSelect = new JButton("Select Folder or Files"), constraints);
		fileSelect.addActionListener(new FindAudioFolder());
		repeat = new JCheckBox("Repeat: At end of file list, start again");
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		constraints.anchor = GridBagConstraints.WEST;
		addComponent(p, repeat, constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.WEST;
		//		constraints.gridy++;
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 3;
		fileDateStrip = new FileDateDialogStrip(acquisitionControl.getFileDate(), acquisitionControl.getGuiFrame());
		p.add(fileDateStrip.getDialogComponent(), constraints);
		fileDateStrip.addObserver(this);
		//		addComponent(p, new JLabel("File date :"), constraints);
		//		constraints.gridx++;
		//		constraints.gridwidth = 2;
		//		constraints.fill = GridBagConstraints.HORIZONTAL;
		//		addComponent(p, fileDateText = new JTextField(), constraints);
		//		fileDateText.setEnabled(false);
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		addComponent(p, mergeFiles = new JCheckBox("Merge contiguous files"), constraints);
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			constraints.gridx+=2;
			constraints.gridwidth = 1;
			addComponent(p, checkFiles = new JButton("Check File Headers..."), constraints);
			checkFiles.addActionListener(new CheckFiles());
		}

//		if (SMRUEnable.isEnable()) {
		// no reason to hide this option from users. 
			constraints.gridy++;
			constraints.gridx = 0;
			constraints.gridwidth = 1;
			addComponent(p,  new JLabel("Skip initial :"), constraints);
			constraints.gridx++;
			addComponent(p, skipSecondsField = new JTextField(4), constraints);
			constraints.gridx++;
			addComponent(p,  new JLabel("seconds"), constraints);
			constraints.anchor = GridBagConstraints.EAST;
//		}

		return p;
	}
	
	class FileComboListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
		String fileName = (String) fileNameCombo.getSelectedItem();
		if (fileName != null) {
//			System.out.println(fileName);
			String[] str = new String[1];
			str[0] = fileName;
			folderInputParameters.setSelectedFiles(str);
			makeSelFileList(str);
		}
		}
	}

	class CheckFiles implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			checkFileHeaders();
		}
	}
	
	
	

	/**
	 * Checks file length matched actual file data length and repairs if necessary. 
	 */
	public void checkFileHeaders() {
		CheckWavFileHeaders.showDialog(acquisitionDialog, this);
	}


	class FindAudioFolder implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			selectFolder();

		}

	}	

	@Override
	public void setSelected(boolean select) {
		super.setSelected(select);
		if (select) {
			makeSelFileList();
		}
	}

	/**
	 * Make a list of wav files within a folder. 
	 * @return flag to indicate...nothing?
	 */
	public int makeSelFileList() {

		if (fileInputParameters.recentFiles == null || fileInputParameters.recentFiles.size() < 1) {
			return 0;
		}
		String[] selection = folderInputParameters.getSelectedFiles();
		return makeSelFileList(selection);
	}
	
	/**
	 * Make a list of wav files within a folder. 
	 * @param rootList
	 * @return
	 */
	public int makeSelFileList(String[] rootList) {
		//		File[] selectedFiles = folderInputParameters.getSelectedFiles();
		//		if (selectedFiles.length == 1 && selectedFiles[0].isDirectory()) {
//		String folderName = fileInputParameters.recentFiles.get(0);
		
		//Swing calls a dialog with progress bar from the wavListWorker
		wavListStart = System.currentTimeMillis();
		
		if (folderInputPane==null) {
			//Swing way
			wavListWorker.startFileListProcess(PamController.getMainFrame(), rootList, 
				folderInputParameters.subFolders, true);
		}
		else {
			//FX system
			PamWorker<FileListData<WavFileType>> worker = wavListWorker.makeFileListProcess(rootList, folderInputParameters.subFolders, true); 
			folderInputPane.setFileWorker(worker); 
			if (worker!=null) worker.start(); 
		}
	
		return 0;
	}

	//	private int makeSelFileList(String fileOrFolder) {
	//		File[] file = new File[1];
	//		file[0] = new File(fileOrFolder);
	//		return makeSelFileList(file);
	//
	//	}
	//
	//	public int makeSelFileList(File[] fileList) {
	//
	//		allFiles.clear();
	//
	//		currentFile = 0;
	//
	//		if (fileInputParameters.recentFiles == null || fileInputParameters.recentFiles.size() < 1) return 0;
	//
	//		String folderName = fileInputParameters.recentFiles.get(0);
	//
	//		if (folderName == null) return 0;
	//
	//		File currentFolder = new File(folderName);
	//
	//		for (int i = 0; i < fileList.length; i++) {
	//			if (fileList[i].isDirectory()) {
	//				addFolderFiles(currentFolder);
	//			}
	//			else if (fileList[i].isFile() && !fileList[i].isHidden()) {
	//				allFiles.add(fileList[i]);
	//			}
	//		}
	//
	//		if (allFiles.size() > 0) {
	//
	//		}
	//		folderProgress.setMinimum(0);
	//		folderProgress.setMaximum(allFiles.size());
	//		folderProgress.setValue(0);
	//
	//		Collections.sort(allFiles);
	//		
	//		return allFiles.size();
	//
	//	}
	//
	//	void addFolderFiles(File folder) {
	//		File[] files = folder.listFiles(getFolderFileFilter());
	//		if (files == null) return;
	//		boolean includeSubFolders = folderInputParameters.subFolders;
	//		File file;
	//		for (int i = 0; i < files.length; i++) {
	//			file = files[i];
	//			if (file.isDirectory() && includeSubFolders) {
	////				System.out.println(file.getAbsoluteFile());
	//				addFolderFiles(file.getAbsoluteFile());
	//			}
	//			else if (file.isFile()) {
	//				allFiles.add(file);
	//			}
	//		}
	//	}

	public PamFileFilter getFolderFileFilter() {
		PamAudioFileFilter filter = new PamAudioFileFilter();
		filter.setAcceptFolders(true);
		return filter;
	}

	protected void selectFolder() {
		JFileChooser fc = null;

		if (fc == null) {
			fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setMultiSelectionEnabled(true);
			fc.setFileFilter(getFolderFileFilter());
		}

		if (fileNameCombo.getSelectedIndex() >= 0) {
			fc.setCurrentDirectory(new File(fileNameCombo.getSelectedItem().toString()));
		}

		if (folderInputParameters.getSelectedFiles() != null) {
			fc.setSelectedFiles(folderInputParameters.getSelectedFileFiles());
		}

		int ans = fc.showDialog(null, "Select files and folders");

		if (ans == JFileChooser.APPROVE_OPTION) {
			/*
			 * if it's a single directory that's been selected, then 
			 * set that with setNewFile. If multiple files and directories
			 * are accepted, select the parent directory of all of them. 
			 */
			File[] files = fc.getSelectedFiles();
			if (files.length <= 0) return;
			else if (files.length == 1) {
				setNewFile(fc.getSelectedFile().toString());
			}
			else {
				// take the folder name from the first file
				File aFile = files[0];
				setNewFile(aFile.getAbsolutePath());
			}
			/*
			 *  The file chooser is returning sub classes of File which are not
			 *  serialisable, so we can't use them. We need to convert their 
			 *  names to strings, which can be safely serialized. This will 
			 *  all happen in FolderInputParameters.
			 */
			folderInputParameters.setSelectedFiles(fc.getSelectedFiles());

			makeSelFileList();
			//			makeSelFileList(fc.getSelectedFiles());
		}
	}

	protected String getCurrentFolder() {
		if (folderInputParameters.recentFiles.size() == 0) {
			return null;
		}
		return folderInputParameters.recentFiles.get(0);
	}

	long wavListStart;
	
	/**
	 * Creates a list of wav files. 
	 */
	WavListWorker wavListWorker = new WavListWorker(new WavListReceiver());
	
	private class WavListReceiver implements WavListUser {

		@Override
		public void newFileList(FileListData<WavFileType> fileListData) {
			FolderInputSystem.this.newFileList(fileListData);
		}

	}

	@Override
	public void interpretNewFile(String newFile) {
		if (newFile == null) {
			return;
		}
		/*
		 *  don't actually need to do anything ? Could make a new list, but do it from what's in the
		 *  folder parameters, not the file parameters. do nothing here, or it gets too complicated. 
		 *  Call the search function from the file select part of the dialot. 
		 */

		// test the new Wav list worker ...
//		wavListStart = System.currentTimeMillis();
//		wavListWorker.startFileListProcess(PamController.getMainFrame(), newFile, true, true);
		//		makeSelFileList(newFile);
	}

	/**
	 * Callback when the file list has completed it's background task. 
	 * @param fileListData
	 */
	public  void newFileList(FileListData<WavFileType> fileListData) {
		
//		System.out.printf("Wav list recieved with %d files after %d millis\n", 
//				fileListData.getFileCount(), System.currentTimeMillis() - wavListStart);
		allFiles = fileListData.getListCopy();

		setFileDateText();
		// also open up the first file and get the sample rate and number of channels from it
		// and set these

		File file = getCurrentFile();
		if (file == null) return;
		AudioInputStream audioStream;		
		
		
		/****Swing GUI stuff****/
		if (file.isFile() && !file.isHidden() && acquisitionDialog != null) {
			//Hidden files should not be used in analysis...
			try {
				audioStream = PamAudioSystem.getAudioInputStream(file);
				AudioFormat audioFormat = audioStream.getFormat();
				fileSamples = audioStream.getFrameLength();
				acquisitionDialog.setSampleRate(audioFormat.getSampleRate());
				acquisitionDialog.setChannels(fudgeNumChannels(audioFormat.getChannels()));
				audioStream.close();
			}
			catch (Exception Ex) {
				//				Ex.printStackTrace();
				System.err.println("Error in file " + file.getAbsolutePath() + " " + Ex.getLocalizedMessage());
			}
		}
		
		// set the min and max of the folder progress bar
		folderProgress.setMinimum(0);
		folderProgress.setMaximum(allFiles.size());
		
		/****FX GUI stuff****/
		if (folderInputPane!=null) {
			Platform.runLater(()->{
			folderInputPane.newFileList(fileListData); 
			});
		}
	}

	/**
	 * Fudge function so that the RonaInputsystem can always fudge the number
	 * of channels to be 7. 
	 * @param nChannels
	 * @return
	 */
	protected int fudgeNumChannels(int nChannels) {
		return nChannels;
	}

	public void setFileDateText() {
		if (allFiles.size() > 0) {
			long fileTime = getFileStartTime(getCurrentFile());
			//			fileDateText.setText(PamCalendar.formatDateTime(fileTime));
			getDialogPanel(); // make sure it's created
			fileDateStrip.setDate(fileTime);
			fileDateStrip.setFormat(acquisitionControl.getFileDate().getFormat());
		}
	}

	@Override
	public String getSystemType() {
		return sysType;
	}

	@Override
	public String getUnitName() {
		//		return "File Folder Analysis";
		return acquisitionControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return daqType;
	}

	@Override
	public File getCurrentFile() {
		if (allFiles != null && allFiles.size() > currentFile) {
			return allFiles.get(currentFile);
		}
		return null;
	}

	//	private float currentSampleRate;

	@Override
	protected boolean openNextFile(long totalSamples) {
		boolean ans = false;
		if (folderInputParameters.mergeFiles == false) return false;
		
		
		long currFileStart = 0;
		long currFileLength = 0;
		long currFileEnd = 0;
		if (currentFile >= 0) {
			try {
			WavFileType currentWav = allFiles.get(currentFile);
			currFileStart = getFileStartTime(currentWav.getAbsoluteFile());
			if (audioStream != null) {
				fileSamples = audioStream.getFrameLength();
				currFileLength = (long) (fileSamples * 1000 / audioStream.getFormat().getFrameRate());
				currFileEnd = currFileStart + currFileLength;
			}
			}
			catch (Exception e) {
				
			}
		}
		if (currFileEnd == 0) {
			//			System.out.println("OpenNextfile " + currentFile + " " + allFiles.get(currentFile).getName());
			// also check to see if the start time of the next file is the same as the 
			// end time of the current file.
			currFileEnd = PamCalendar.getTimeInMillis();
			long lastBit = (long) ((blockSamples * 1000L) / getSampleRate());
			currFileEnd += lastBit;
		}
		if (++currentFile < allFiles.size()) {
			long newStartTime = getFileStartTime(getCurrentFile());
			long diff = newStartTime - currFileEnd;
			if (diff > 2000 || diff < -5000 || newStartTime == 0) {
				currentFile--;
				return false;
				/*
				 * Return since it's not possible to merge this file into the 
				 * next one. In this instance, DAQ will restart, and the currentfile
				 * counter will increment elsewhere. 
				 */
			}
			setFolderProgress();
			//			sayEta();
			ans = prepareInputFile();
			currentFileStart = System.currentTimeMillis();
			//			if (ans && audioFormat.getSampleRate() != currentSampleRate && currentFile > 0) {
			//				acquisitionControl.getDaqProcess().setSampleRate(currentSampleRate = audioFormat.getSampleRate(), true);
			//			}
			/**
			 * Send a dataunit to the database to mark the file changeover. 
			 */
			DaqStatusDataUnit daqStatusDataUnit = new DaqStatusDataUnit(currentFileStart, currentFileStart, currentFileStart, 
					totalSamples, null, "NextFile", "File End", 
					acquisitionControl.acquisitionParameters, getSystemName(), totalSamples/getSampleRate(), 0); 
			acquisitionControl.getAcquisitionProcess().getDaqStatusDataBlock().addPamData(daqStatusDataUnit);
		}
		return ans;
	}

	@Override
	public void daqHasEnded() {
		currentFile++;
		if (folderInputParameters.repeatLoop && currentFile >= allFiles.size()) {
			currentFile = 0;
		}
		if (currentFile < allFiles.size()) {
			// only restart if the file ended - not if it stopped
			if (getStreamStatus() == STREAM_ENDED) {
				System.out.println(String.format("Start new file timer (file %d/%d)",currentFile+1,allFiles.size()));
				newFileTimer.start();
			}
		}
		calculateETA();
		setFolderProgress();
//		System.out.println("FolderinputSytem: daqHasEnded");
	}

	private void setFolderProgress() {
		folderProgress.setValue(currentFile);
	}

	protected void calculateETA() {
		long now = System.currentTimeMillis();
		eta = now-currentFileStart;
		eta *= (allFiles.size()-currentFile);
		eta += now;
	}


	JPanel barBit;
	PamProgressBar folderProgress = new PamProgressBar(PamProgressBar.defaultColor);

	private FolderInputPane folderInputPane;
	
	@Override
	public Component getStatusBarComponent() {

		if (barBit == null) {
			barBit = new PamPanel();
			barBit.setLayout(new BoxLayout(barBit, BoxLayout.X_AXIS));
			barBit.add(new PamLabel("Folder "));
			barBit.add(folderProgress);
			barBit.add(new PamLabel("   "));
			barBit.add(super.getStatusBarComponent());
		}
		return barBit;
	}

	@Override
	public long getEta() {
		if (currentFile == allFiles.size()-1) {
			return super.getEta();
		}
		return eta;
	}

	@Override
	public Serializable getSettingsReference() {
		return folderInputParameters;
	}

	@Override
	public long getSettingsVersion() {
		return FolderInputParameters.serialVersionUID;
	}

	@Override
	public boolean dialogGetParams() {
		folderInputParameters.subFolders = subFolders.isSelected();
		folderInputParameters.mergeFiles = mergeFiles.isSelected();
		folderInputParameters.repeatLoop = repeat.isSelected();
		currentFile = 0;
		if (skipSecondsField!=null) {
			try {
				Double skipSeconds = Double.valueOf(skipSecondsField.getText())*1000.; // saved in millis. 
				folderInputParameters.skipStartFileTime = skipSeconds.longValue();
			}
			catch (Exception e) {
				return false; 
			}
		}
		return super.dialogGetParams();
	}

	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the FileInputParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (fileInputParameters.systemType==null) fileInputParameters.systemType=getSystemType();
		
		super.dialogSetParams();
		subFolders.setSelected(folderInputParameters.subFolders);
		mergeFiles.setSelected(folderInputParameters.mergeFiles);
		repeat.setSelected(folderInputParameters.repeatLoop);
		if (skipSecondsField!=null) {
			skipSecondsField.setText(String.format("%.1f", fileInputParameters.skipStartFileTime/1000.));
		}
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		FolderInputParameters newParams;

		try {
			newParams = (FolderInputParameters) pamControlledUnitSettings.getSettings();
		}
		catch (ClassCastException ex) {
			return false;
		}
		setFolderInputParameters(newParams);
		return true;
	}

	public FolderInputParameters getFolderInputParameters() {
		return folderInputParameters;
	}

	public void setFolderInputParameters(FolderInputParameters folderInputParameters) {
		this.folderInputParameters = folderInputParameters;
		fileInputParameters = this.folderInputParameters;
	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
		System.out.println("Start system");
		setFolderProgress();
		return super.startSystem(daqControl);
	}

	//	/**
	//	 * @param audioFileFilter the audioFileFilter to set
	//	 */
	//	public void setAudioFileFilter(PamFileFilter audioFileFilter) {
	//		this.audioFileFilter = audioFileFilter;
	//	}
	//
	//	/**
	//	 * @return the audioFileFilter
	//	 */
	//	public PamFileFilter getAudioFileFilter() {
	//		return audioFileFilter;
	//	}
	@Override
	public String getDeviceName() {
		if (fileInputParameters.recentFiles == null || fileInputParameters.recentFiles.size() < 1) {
			return null;
		}
		return fileInputParameters.recentFiles.get(0);
	}

	public PamFileFilter getAudioFileFilter() {
		return audioFileFilter;
	}

	public void setAudioFileFilter(PamFileFilter audioFileFilter) {
		this.audioFileFilter = audioFileFilter;
	}

	/****JavaFX bits***/

	public DAQSettingsPane getDAQSpecificPane(AcquisitionPaneFX acquisitionPaneFX) {
		if (folderInputPane==null) this.folderInputPane = new FolderInputPane(this, acquisitionPaneFX); 
		return folderInputPane;
	}

	/**
	 * Called by AcquisitionDialog.SetParams so that the dialog node can update it's
	 * fields. 
	 */
	public void dialogFXSetParams() {
		folderInputPane.setParams(folderInputParameters);
	}


}
