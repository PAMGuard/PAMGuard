package soundtrap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import clickDetector.ClickParameters;
import dataMap.filemaps.OfflineFileParameters;
import soundtrap.xml.CDETInfo;
import Acquisition.AcquisitionControl;
import Acquisition.FolderInputSystem;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamModel.PamModuleInfo;
import PamUtils.FileList;
import PamUtils.FolderChangeListener;
import PamUtils.PamUtils;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.debug.Debug;

public class ImportBCLDialog extends PamDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ImportBCLDialog singleInstance;
	private STToolsParams stToolsParams;
	private SelectFolder sourceFolder;
//	private SelectFolder destFolder;
	private JPanel soundTrapDate;
	private JTextField customDateTimeFormat;
	private JLabel fileCountInfo;
	private JLabel thisFileInfo;
	private JProgressBar allProgress;
	private JComboBox<String> detectorName;
	private int nSoundTraps;	
	private Hashtable<String, String> uniqueDevices = new Hashtable<>();
	private JButton startButton;	
	private DWVConverter dwvConverter;
	ArrayList<STGroupInfo> fileGroupInfo;
	
	private static String infoString = "Sound trap detector data can be imported into PAMGuard."
			+ "<br>Detector data are stored in .dwv and .bcl files, and these "
			+ "will be converted to standard PAMGuard Click Detector binary files.  "
			+ "<br><br>Note that you will need to have a binary storage module added "
			+ "and configured before importing data.  If you do not currently "
			+ "have a binary storage module, please close this dialog and "
			+ "add it first."
			+"<br><br>";
	
	private ImportBCLDialog(Window parentFrame, STToolsControl stControl) {
		super(parentFrame, "Import Sound Trap Data", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(new CompoundBorder(new TitledBorder("Sound Trap Data"), new EmptyBorder(5, 10, 0, 10)));
		infoPanel.add(BorderLayout.CENTER, new JLabel("<html><body style='width: 300px'>" + infoString + "</html>"));
		mainPanel.add(infoPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		sourceFolder = new SelectFolder("Source Folder", 50, true);
		sourceFolder.setMustExist(true);
		sourceFolder.addFolderChangeListener(new SourceFolderChange());
		JPanel 	p = new JPanel(new BorderLayout());
		p.setBorder(new CompoundBorder(new TitledBorder("Source Folder"), new EmptyBorder(0, 10, 0, 10)));
		p.add(BorderLayout.CENTER, sourceFolder.getFolderPanel());
		mainPanel.add(p);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		
//		destFolder = new SelectFolder("Destination Folder", 50, false);
//		destFolder.setCreateIfNeeded(true);	
//		p = new JPanel(new BorderLayout());
//		p.setBorder(new CompoundBorder(new TitledBorder("Destination Folder"), new EmptyBorder(0, 10, 0, 10)));
//		p.add(BorderLayout.CENTER, destFolder.getFolderPanel());
//		mainPanel.add(p);
//		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		
		soundTrapDate = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		customDateTimeFormat = new JTextField(30);
		customDateTimeFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listSourceFiles(sourceFolder.getFolderName(false), sourceFolder.isIncludeSubFolders());
			}
		});
		c.gridx = c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		soundTrapDate.setBorder(new CompoundBorder(new TitledBorder("Soundtrap Date/Time Format "), new EmptyBorder(0, 10, 0, 0)));
		soundTrapDate.add(new JLabel("Enter the date/time format to use ", JLabel.LEFT),c);
		c.gridx++;
		soundTrapDate.add(customDateTimeFormat,c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		String text = "<html><body style='width: 350px'>" + 
				"See https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for " +
				"information on date and time codes, as well as examples of common formats.";
		soundTrapDate.add(new JLabel(text, JLabel.LEFT),c);
		c.gridy++;
		text = "The default format is yyyy-MM-dd'T'HH:mm:ss";
		soundTrapDate.add(new JLabel(text, JLabel.LEFT),c);
		mainPanel.add(soundTrapDate);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

//		JPanel acquisitionPanel = new JPanel(new GridBagLayout());
//		acquisitionPanel.setBorder(new CompoundBorder(new TitledBorder("Sound Acquisition Name"), new EmptyBorder(0, 10, 0, 10)));
//		GridBagConstraints c = new PamGridBagContraints();
//		acquisitionPanel.add(new JLabel("Sound Acquisition Name: "));
//		c.gridx++;
//		acquisitionPanel.add(acquisitionName = new JComboBox<String>());
//		acquisitionName.setEditable(true);
//		mainPanel.add(acquisitionPanel);
		
		c = new PamGridBagContraints();
		JPanel detectorPanel = new JPanel(new GridBagLayout());
		detectorPanel.setBorder(new CompoundBorder(new TitledBorder("Click Detector Name"), new EmptyBorder(0, 10, 0, 10)));
		c = new PamGridBagContraints();
		detectorPanel.add(new JLabel("Click Detector Name: "));
		c.gridx++;
		detectorPanel.add(detectorName = new JComboBox<String>());
		detectorName.setEditable(true);
		mainPanel.add(detectorPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
//		progressPanel.setBackground(Color.RED);
		JPanel pOuter = new JPanel();
		pOuter.setBorder(new CompoundBorder(new TitledBorder("File information"), new EmptyBorder(0, 10, 0, 10)));
		pOuter.setLayout(new BoxLayout(pOuter, BoxLayout.Y_AXIS));
		mainPanel.add(pOuter);
		pOuter.add(progressPanel);
		fileCountInfo = new JLabel("0 Files ");
		progressPanel.add(fileCountInfo);
		thisFileInfo = new JLabel(" - ");
		progressPanel.add(thisFileInfo);
		
		allProgress = new JProgressBar();
		progressPanel.add(allProgress);
		
		startButton = new JButton("Start");
		getButtonPanel().add(startButton, 0);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startButtton();
			}
		});
//		getOkButton().setText("Start");
		getOkButton().setVisible(false);
		getCancelButton().setText("Close");
		
		setDialogComponent(mainPanel);
	}
	
	/**
	 * Start button has been pressed !
	 */
	protected void startButtton() {
		if (getParams() == false) {
			return;
		}
		/*
		 * this is all a bit painful and needs to somehow be improved. Unfortunately there is no 
		 * consistency whatsoever between Marks xml format and the way I store parameters. I think though 
		 * that we (i.e. me, someone, someone else) needs to write something which will configure every 
		 * module to generate a configuration which exactly matches the xml. 
		 */
		/*
		 * May as well do things in a sensible order and check there is an acquisition
		 * module before we create the click detector. 
		 * To be pedantic, it could be any raw datablock, even though the reality is that 
		 * i's most likely to be an acquisition control module. 
		 */
//		PamRawDataBlock rawDataBlock = checkAudioSource();
//		if (rawDataBlock==null) {
//			return;
//		}
		
		/*
		 * Check that there is a click detector and configure it
		 */
		STClickControl clickControl = checkClickDetector();
		if (clickControl == null) {
			showWarning("Click Detector Error", "There was an error creating/configuring the SoundTrap Click Detector module.  Please check the console window for error messages");
			return;
		}

		// now import the data
		dwvConverter = new DWVConverter(fileGroupInfo, new ConvertObserver(), clickControl);
		dwvConverter.start();
		enableControls();
	}
	
	/**
	 * Check there is a viable audio data source and configure it.
	 * @return a rawdatablock with one channel of data at the correct sample rate. 
	 */
	private PamRawDataBlock checkAudioSource(STClickControl clickControl) {
//		STAcquisitionControl daqControl = (STAcquisitionControl) PamController.getInstance().findControlledUnit(STAcquisitionControl.STUNITTYPE, stToolsParams.getSoundAcqName());
//		
//		// if the sound acquisition module wasn't found, make sure the user wants to create it
//		if (daqControl == null) {
//			String msg = String.format("SoundTrap Sound Acquisition %s does not exist,  do you want to create it? ", stToolsParams.getSoundAcqName());
//			int ans = JOptionPane.showConfirmDialog(getOwner(), msg, "SoundTrap Sound Acquisition", JOptionPane.YES_NO_CANCEL_OPTION);
//			if (ans == JOptionPane.YES_OPTION) {
//				// create a new sound acquisition module. 
//				daqControl = createAcquisition();
//			}
//			else {
//				return null;
//			}
//		}
//		
//		// if it was found but has the wrong parameters, ask the user if they want to create a new one.  If not, just exit - don't mess up the settings of an existing
//		// sound acquisition module
//		else {
//			if (daqControl.getStAcquisitionProcess().getSampleRate() != fileGroupInfo.get(0).getDwvSampleRate()) {
//				String msg = String.format("The selected SoundTrap Sound Acquisition module does not have the correct configuration. Do you want to create a new one instead?");
//				int ans = JOptionPane.showConfirmDialog(getOwner(), msg, "SoundTrap Sound Acquisition", JOptionPane.YES_NO_CANCEL_OPTION);
//				if (ans == JOptionPane.YES_OPTION) {
//					
//					// create a new unique name, select it and create the module
//					fillSoundAcqList();
//					acquisitionName.setSelectedIndex(0);
//					stToolsParams.setSoundAcqName((String) acquisitionName.getSelectedItem());
//					daqControl = createAcquisition();
//				}
//				else {
//					return null;
//				}
//			}
//		}
		
		// check if the STClickControl is set up with a raw data source.  If not, exit
		AcquisitionControl daqControl = clickControl.getSTAcquisition();
		if (daqControl == null) {
			return null;
		}
		
		 // Configure the daq module so that it's got the right sample rate and number of channels for the click data.
		daqControl.getAcquisitionProcess().getRawDataBlock().setChannelMap(1); // only 1 channel of click data right now
		STGroupInfo fileInfo = fileGroupInfo.get(0);
		daqControl.getAcquisitionProcess().setSampleRate(fileInfo.getDwvSampleRate(), true);

		// set daq system parameters for wav files.  First set the folder information
		FolderInputSystem fis = daqControl.getFolderSystem();
		fis.getDaqSpecificDialogComponent(null);	// forces creation of the dialog components; without these, the rest of the code will throw null pointer exceptions
		fis.getFolderInputParameters().subFolders = false;
		fis.getFolderInputParameters().mergeFiles = false;
		fis.setNewFile(sourceFolder.getFolderName(false));
		File[] folderList = new File[1];
		folderList[0] = new File (sourceFolder.getFolderName(false));
		fis.getFolderInputParameters().setSelectedFiles(folderList);

		// now set the acquisition parameters for the wav folder, using info from the first wav file if possible
		daqControl.acquisitionParameters.setDaqSystemType(fis.getSystemType());
		daqControl.acquisitionParameters.nChannels = 1; 
		daqControl.acquisitionParameters.sampleRate = fileInfo.getDwvSampleRate();
		daqControl.acquisitionParameters.voltsPeak2Peak = 2;
		daqControl.acquisitionParameters.subtractDC = false;
		daqControl.acquisitionParameters.preamplifier.setGain(0);
//		try {
//			AudioInputStream audioStream = PamAudioSystem.getAudioInputStream(fis.getCurrentFile());
//			AudioFormat audioFormat = audioStream.getFormat();
////			fsi.fileSamples = audioStream.getFrameLength(); don't think I need to set this now
//			daqControl.acquisitionParameters.sampleRate = audioFormat.getSampleRate();
//			daqControl.acquisitionParameters.nChannels = audioFormat.getChannels();
//			fis.getFolderInputParameters().bitDepth = audioFormat.getSampleSizeInBits();
//			audioStream.close();
//		}
//		catch (Exception Ex) {
//			Ex.printStackTrace();
//		}
//		daqControl.acquisitionParameters.setDefaultChannelList();
		
		// set offline parameters
		OfflineFileParameters p = new OfflineFileParameters();
		p.enable = true;
		p.includeSubFolders = false;
		p.folderName = sourceFolder.getFolderName(false);
		daqControl.getOfflineFileServer().setOfflineFileParameters(p); // where does this get saved?

		// return the raw data block
		return daqControl.getAcquisitionProcess().getRawDataBlock();
	}

//	private AcquisitionControl createAcquisition() {
//	private STAcquisitionControl createAcquisition() {
//		DependencyManager dm = PamModel.getPamModel().getDependencyManager();
//		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(STAcquisitionControl.class.getCanonicalName());
//		if (moduleInfo == null) {
//			String str = "Cannot find SoundTrap Sound Acquisition module information";
//			System.out.println(str);
//			return null;
//		}
//		STAcquisitionControl newUnit = (STAcquisitionControl) PamController.getInstance().addModule( moduleInfo, stToolsParams.getSoundAcqName());
//		return newUnit;
//	}

	/**
	 * Check the click detector exists and has a reasonably sensible configuration. 
	 * @param daqControl Reference to daq control the click detector should connect to. 
	 * @return Reference to the click detector of null if this fails. 
	 */
	private STClickControl checkClickDetector() {
		STClickControl clickControl = (STClickControl) PamController.getInstance().findControlledUnit(STClickControl.STUNITTYPE, stToolsParams.clickDetName);
		if (clickControl == null) {
			String msg = String.format("Click detector %s does not exist,  do you want to create it ? ", stToolsParams.clickDetName);
			int ans = JOptionPane.showConfirmDialog(getOwner(), msg, "Click Detector", JOptionPane.YES_NO_CANCEL_OPTION);
			if (ans == JOptionPane.YES_OPTION) {
				// create a new click detector. 
				createClickDetector();
				return checkClickDetector();
			} else {
				return null;
			}
		}
		
		// configure the acoustic data block created by the click detector
		PamRawDataBlock acousticDataBlock = checkAudioSource(clickControl);
		if (acousticDataBlock == null) {
			return null;
		}
		
		/*
		 * Now configure the click detector as far as we are able.
		 */
		ClickParameters clickParams = clickControl.getClickParameters();
		STGroupInfo fileInfo = fileGroupInfo.get(0);
		if (acousticDataBlock != null) {
			clickParams.setRawDataSource(acousticDataBlock.getDataName());
		}
		clickParams.setChannelBitmap(acousticDataBlock.getChannelMap());
		clickParams.setChannelGroups(new int[PamUtils.getNumChannels(acousticDataBlock.getChannelMap())]);

		CDETInfo cDetInfo = fileInfo.getCdetInfo();
		clickParams.maxLength = cDetInfo.cdetPredet + cDetInfo.cdetPostDet;
		clickControl.getClickDetector().newParameters();
		return clickControl;
	}
	
	/**
	 * Called when a new click detector is needed in the model.
	 */
	private void createClickDetector() {
		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(STClickControl.class.getCanonicalName());
		if (moduleInfo == null) {
			String str = "Cannot find click detector module information";
			System.out.println(str);
		}
		PamController.getInstance().addModule( moduleInfo, stToolsParams.clickDetName);
	}

	private class ConvertObserver implements DWVConvertObserver {

		@Override
		public void process(DWVConvertInformation dwvConvertInformation) {
			int nDWV = dwvConvertInformation.getnDWV();
			if (nDWV == 0) {
				thisFileInfo.setText("NO DWV records");
				return;
			}
			int prog = dwvConvertInformation.getnDone() * 100 / dwvConvertInformation.getnDWV();
			allProgress.setValue(prog);
			thisFileInfo.setText(String.format("Repacked %d of %d clicks", dwvConvertInformation.getnDone(), 
					dwvConvertInformation.getnDWV()));
		}

		@Override
		public void done() {
			dwvConverter = null;
			enableControls();
			//update the datamap to show the new clicks. 
			PamController.getInstance().updateDataMap();
		}
		
	}

	public static boolean showDialog(Window frame, STToolsControl stControl) {
		if (singleInstance == null) {
			singleInstance = new ImportBCLDialog(frame, stControl);
		}
		singleInstance.stToolsParams = stControl.getStToolsParams();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return true;
	}
	
	private class SourceFolderChange implements FolderChangeListener {

		@Override
		public void folderChanged(String newFolder, boolean showSubfolders) {
			listSourceFiles(newFolder, showSubfolders);
		}
		
	}

	public void listSourceFiles(String newFolder, boolean showSubfolders) {
		FileList fileList = new FileList();
		ArrayList<File> xmlFiles = fileList.getFileList(newFolder, STToolsControl.xmlFileEnd, showSubfolders);
		if (xmlFiles == null) {
			return;
		}
		/*
		 * Work through the xml files and check that we have bcl and dwv files
		 * and that the xml files have dwv information. If there isn't any 
		 * dwv file, then it's possible that there just wasn't a click, so 
		 * we should still make an empty click file. 
		 * It's also possible though that someone will run this on ST data
		 * that didn't include the detector, in which case they just need
		 * to be told that they are doing the wrong thing. 
		 */
		/*
		 * Also need to check that all these data are from a unique sound trap
		 * or we're stuffed. 
		 */
		uniqueDevices.clear();
		fileGroupInfo = new ArrayList<>();
		int nFiles = 0;
		int nCDET = 0;
		int nDWV = 0;
		for (File xmlFile:xmlFiles) {
			Debug.out.println("Opening xml file " + xmlFile.getAbsolutePath());
			if (xmlFile.getAbsolutePath().equals("E:\\STOctober2016\\335839252\\335839252.161031002807.log.xml")) {
				System.out.println("Opening xml file " + xmlFile.getAbsolutePath());
			}
			STXMLFile xmlFileInfo = STXMLFile.openXMLFile(xmlFile, customDateTimeFormat.getText());
			
			if (xmlFileInfo == null || xmlFileInfo.getDwvInfo() == null) {
				String title = "Error with Soundtrap file";
				String msg = "There was an error reading data from the Soundtrap xml file " + xmlFile.getName() +
						".  The file may be corrupt, or missing information needed.  Import is suspended; " +
						"Please fix the file manually or remove it from the folder, and try to import again.";
				String help = null;
				int ans = WarnOnce.showWarning(PamController.getInstance().getGuiFrameManager().getFrame(0), title, msg, WarnOnce.OK_OPTION, null, null, "Cancel Import", null);
				break;
			}
			
			// do a quick check here for the time.  If it's 0, it means we had problems reading the time format.  In
			// that case, warn the user and give them a chance to cancel out of this and submit a different format
			// to use.
			if (xmlFileInfo.getDwvInfo().getTimeInfo()!=null && xmlFileInfo.getDwvInfo().getTimeInfo().samplingStartTimeUTC==0) {
				String title = "Error with Soundtrap date/time format";
				String msg = "There was an error parsing the date/time in the Soundtrap xml file " + xmlFile.getName() +
						".  PAMGuard is trying to use the format " + customDateTimeFormat.getText() + " but without " +
						"success.  Please change the format in this dialog to match the one used for SamplingStartTimeUTC in your *.log.xml files";
				String help = null;
				int ans = WarnOnce.showWarning(PamController.getInstance().getGuiFrameManager().getFrame(0), title, msg, WarnOnce.OK_OPTION, null, null, "Cancel Import", null);
//				if (ans == WarnOnce.OK_OPTION) {
				break;
//				}
			}
			
			
			STGroupInfo groupInfo;
			fileGroupInfo.add(groupInfo = new STGroupInfo(xmlFileInfo));
			nFiles++;
			if (xmlFileInfo.getCdetInfo() != null) {
				nCDET++;
			}
			if (groupInfo.hasDWV()) {
				nDWV++;
			}
			Debug.out.printf("Putting ST id %s in hashtable size %d\n", xmlFileInfo.getSoundTrapId(), uniqueDevices.size());
			uniqueDevices.put(xmlFileInfo.getSoundTrapId(), xmlFileInfo.getSoundTrapId());
		}
		nSoundTraps = uniqueDevices.size();
//		System.out.println("N sound traps = " + nSoundTraps);
		String fc = null;
		if (nSoundTraps == 0) {
			fc = "No sound trap data selected";
		}
		else if (nSoundTraps > 1) {
			fc = String.format("Data delected from %d sound traps. Select data from a single device", nSoundTraps);
		}
		else {
			fc = String.format("%d files, %d with detector output, %d with dwv data", nFiles, nCDET, nDWV);
		}
		fileCountInfo.setText(fc);
		
//		fillSoundAcqList();
		
		fillDetectorList();
		
		enableControls();
	}

	/**
	 * Make a list of available sound acquisition modules. 
	 */
//	private void fillSoundAcqList() {
//		acquisitionName.removeAllItems();
//		if (nSoundTraps == 1) {
////			Enumeration<String> devs = uniqueDevices.keys();
////			if (devs.hasMoreElements()) {
////				acquisitionName.addItem("SoundAcq" + devs.nextElement());
////			}
//			
//			PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(STAcquisitionControl.class.getCanonicalName());
//			if (moduleInfo == null) {
//				String str = "Cannot find SoundTrap Sound Acquisition module information";
//				System.out.println(str);
//			} else {
//				acquisitionName.addItem(moduleInfo.getNewDefaultName());
//			}
//		}
//		// add a list of sound acquisition modules from the current configuration. 
//		ArrayList<PamControlledUnit> soundAcquisitionModules = PamController.getInstance().findControlledUnits(STAcquisitionControl.STUNITTYPE);
//		for (int i = 0; i < soundAcquisitionModules.size(); i++) {
//			acquisitionName.addItem(soundAcquisitionModules.get(i).getUnitName());
//		}		
//	}

	/**
	 * Make a list of available click detectors. 
	 */
	private void fillDetectorList() {
		detectorName.removeAllItems();
		if (nSoundTraps == 1) {
//			Enumeration<String> devs = uniqueDevices.keys();
//			if (devs.hasMoreElements()) {
//				detectorName.addItem("Clicks" + devs.nextElement());
//			}
			PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(STClickControl.class.getCanonicalName());
			if (moduleInfo == null) {
				String str = "Cannot find SoundTrap Click Detector module information";
				System.out.println(str);
			} else {
				detectorName.addItem(moduleInfo.getNewDefaultName());
			}
		}
		// add a list of click detectors from the current configuration. 
		ArrayList<PamControlledUnit> clickDetectors = PamController.getInstance().findControlledUnits(STClickControl.STUNITTYPE);
		for (int i = 0; i < clickDetectors.size(); i++) {
			detectorName.addItem(clickDetectors.get(i).getUnitName());
		}		
	}

	private void enableControls() {
		startButton.setEnabled(nSoundTraps == 1 && dwvConverter == null);
//		getCancelButton().setEnabled(dwvConverter != null);
	}

	private void setParams() {
		if (stToolsParams.sourceFolder != null) {
			sourceFolder.setFolderName(stToolsParams.sourceFolder);
		}
		sourceFolder.setIncludeSubFolders(stToolsParams.sourceSubFolders);
//		if (stToolsParams.destFolder != null) {
//			destFolder.setFolderName(stToolsParams.destFolder);
//		}
		
		customDateTimeFormat.setText(stToolsParams.getCustomDate());
		listSourceFiles(stToolsParams.sourceFolder, stToolsParams.sourceSubFolders);
	}

	@Override
	public boolean getParams() {
		stToolsParams.sourceFolder = sourceFolder.getFolderName(true);
		stToolsParams.sourceSubFolders = sourceFolder.isIncludeSubFolders();
//		stToolsParams.destFolder = destFolder.getFolderName(true);
//		stToolsParams.setSoundAcqName((String) acquisitionName.getSelectedItem());
//		if (stToolsParams.getSoundAcqName() == null || stToolsParams.getSoundAcqName().length() == 0) {
//			return showWarning("Invalid Sound Acquisition name!");
//		}
		stToolsParams.clickDetName = (String) detectorName.getSelectedItem();
		if (stToolsParams.clickDetName == null || stToolsParams.clickDetName.length() == 0) {
			return showWarning("Invalid Click Detector name!");
		}
		stToolsParams.setCustomDate(customDateTimeFormat.getText());
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		if (dwvConverter != null) {
			dwvConverter.stop();
		}
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
