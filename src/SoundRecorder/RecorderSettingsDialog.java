package SoundRecorder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.lang.model.SourceVersion;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionProcess;
import Acquisition.DaqSystem;
import Acquisition.pamAudio.PamAudioSystem;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamModel.SMRUEnable;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;

public class RecorderSettingsDialog extends PamDialog {
	
	private static RecorderSettingsDialog singleInstance;
	
//	private RawSourceDialogPanel sourcePanel;
	private SourcePanel sourcePanel;
	
	private StartupPanel bufferPanel;
	
	private SelectFolder selectFolder;
	
	private OutputFormat outputFormat;
	
	private FileLengthPanel fileLengthPanel;
	
	private AutoPanel autoPanel;
	
	private RecorderSettings recorderSettings;
	

	private JCheckBox enableBuffer;
	private JTextField bufferLength;
	
//	JButton okButton, cancelButton;
	
	private RecorderSettingsDialog (Frame parentFrame) {
		
		super(parentFrame, "Sound Recording Settings", false);
		
		sourcePanel = new SourcePanel(this, "Raw data source", RawDataUnit.class, false, true);
		bufferPanel = new StartupPanel();
		selectFolder = new SelectFolder("Select output folder", 30, true);
		selectFolder.setSubFolderButtonName("Store in sub folders by date");
		selectFolder.setSubFolderButtonToolTip("Store recordings in sub folders, starting a new folder each day");
		outputFormat = new OutputFormat();
		fileLengthPanel = new FileLengthPanel();
		autoPanel = new AutoPanel();
		
		JPanel bufPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = -1;
		bufPanel.add(enableBuffer = new JCheckBox("Enable Buffer - length  "),c);
		bufPanel.add(bufferLength = new JTextField(4),c);
		bufPanel.add( new JLabel(" s "),c);
		JPanel bwp = new JPanel(new BorderLayout());
		bwp.add(BorderLayout.WEST, bufPanel);
		sourcePanel.getPanel().add(BorderLayout.SOUTH, bwp);
		
		JTabbedPane mainPanel = new JTabbedPane();
		
		JPanel p = new JPanel();
		//p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		GridBagLayout gb = new GridBagLayout();
		c = new GridBagConstraints();
		//p.setLayout(new GridLayout(2,1));
		p.setLayout(gb);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		addComponent(p, sourcePanel.getPanel(), c);
		c.gridy ++;
		addComponent(p, bufferPanel, c);
		c.gridy ++;
		addComponent(p, autoPanel, c);
		mainPanel.add("Control", p);
		
		JPanel f = new JPanel();
		f.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
//		c.gridy ++;
		f.add(outputFormat, c);
		c.gridy ++;
		f.add(fileLengthPanel, c);
		
		mainPanel.add("Files and Folders", f);

		setHelpPoint("sound_processing.soundRecorderHelp.docs.RecorderOverview");
		
		setDialogComponent(mainPanel);
		
	}
	
	public static RecorderSettings showDialog(Frame parentFrame, RecorderSettings recorderSettings) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame)  {
			singleInstance = new RecorderSettingsDialog(parentFrame);
//		}
		singleInstance.recorderSettings = recorderSettings.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.recorderSettings;
	}
	
	private void setParams() {
		sourcePanel.setSourceList();
		sourcePanel.setSource(recorderSettings.rawDataSource);
		bufferPanel.setParams();
		selectFolder.setFolderName(recorderSettings.outputFolder);
		selectFolder.setIncludeSubFolders(recorderSettings.datedSubFolders);
		outputFormat.setParams();
		fileLengthPanel.setParams();
		autoPanel.setParams();
		pack();
	}
	
	@Override
	public boolean getParams() {
		recorderSettings.rawDataSource = sourcePanel.getSourceName();
		if (recorderSettings.rawDataSource == null) return showWarning("No raw data source");;
		if (bufferPanel.getParams() == false) return showWarning("Error in buffer settings");;
		recorderSettings.outputFolder = selectFolder.getFolderName(true);
		recorderSettings.datedSubFolders = selectFolder.isIncludeSubFolders();
		if (outputFormat.getParams() == false) return showWarning("Error in output format settings");
		if (fileLengthPanel.getParams() == false) return showWarning("Error in file length settings");
		if (autoPanel.getParams() == false) return false;
		
		return checkBitDepth();
	}

	/**
	 * Check that the bit depth of the recordings vaguely matches the bit depth of the 
	 * acquisition system. If they are different, the user will be asked if they 
	 * wish to continue. 
	 * @return true if they are the same of the user confirms they are happy 
	 * or if no acquisition system can be found. 
	 */
	private boolean checkBitDepth() {
		// first find the source data
		PamRawDataBlock rawBlock = PamController.getInstance().getRawDataBlock(recorderSettings.rawDataSource);
		if (rawBlock == null) {
			return true;
		}
		PamDataBlock sourceBlock = rawBlock.getSourceDataBlock();
		if (sourceBlock == null) {
			return true;
		}
		AcquisitionProcess daqProcess = null;
		try {
			daqProcess = (AcquisitionProcess) sourceBlock.getParentProcess();
		}
		catch (ClassCastException e) {
			System.err.println("Source datablock is not in an acuisition module: " + sourceBlock.toString());
			return true;
		}
		DaqSystem daqSystem = daqProcess.getAcquisitionControl().findDaqSystem(null);
		if (daqSystem == null) {
			System.err.println("Unable to find valid Daq system in in chain root of sound recorder");
		}
		int daqBits = daqSystem.getSampleBits();
		if (daqBits > recorderSettings.bitDepth) {
			String msg = String.format("The sound acquisition system you are using appears to have %d bits\n"
					+ "but the recorder is only set to use %d. This will result in a loss of \n"
					+ "quality in recorded data.\n"
					+ "Do you wish to proceed with these settings ?", daqBits, recorderSettings.bitDepth);
			return showQuestion(msg);
		}
		else if (daqBits < recorderSettings.bitDepth) {
			String msg = String.format("The sound acquisition system you are using appears to have %d bits\n"
					+ "and the recorder is set to use %d. This generate large files with no \n"
					+ "improvement in the quality of recorded data.\n"
					+ "Do you wish to proceed with these settings ?", daqBits, recorderSettings.bitDepth);
			return showQuestion(msg);
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		recorderSettings = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

//	public void actionPerformed(ActionEvent e) {
//
//		if (e.getSource() == okButton) {
//			if (getParams()) setVisible(false);
//		}
//		else if (e.getSource() == cancelButton) {
//			recorderSettings = null;
//			setVisible(false);
//		}
//		
//	}
	class StartupPanel extends JPanel {
		JRadioButton autoStop, autoStart, autoAsLast, autoCycle;
		StartupPanel() {
			super();
			setBorder(new TitledBorder("PAMGuard Startup Options"));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//			c.gridwidth = 1;
//			c.gridx = 0;
//			c.gridy = 0;
//			c.gridwidth = 3;
//			c.gridx = 0;
//			c.gridy++;
			autoStop = new JRadioButton("Remain idle");
			add(autoStop);
//			c.gridy++;
			autoStart = new JRadioButton("Start recording");
			add(autoStart);
//			c.gridy++;
			autoCycle = new JRadioButton("Start recording cycle");
			add(autoCycle);
//			c.gridy++;
			autoAsLast = new JRadioButton("Automatically return to last state at PAMGuard Stop");
			add(autoAsLast);
			
			ButtonGroup bg = new ButtonGroup();
			bg.add(autoStop);
			bg.add(autoStart);
			bg.add(autoCycle);
			bg.add(autoAsLast);
			
		}
		
		void setParams() {
			if (recorderSettings.autoStart) {
				autoAsLast.setSelected(recorderSettings.autoStart);
			}
			else switch(recorderSettings.startStatus) {
			case RecorderView.BUTTON_START:
				autoStart.setSelected(true);
				break;
			case RecorderView.BUTTON_AUTO:
				autoCycle.setSelected(true);
				break;
			default:
				autoStop.setSelected(true);
			}
			enableBuffer.setSelected(recorderSettings.enableBuffer);
			bufferLength.setText(String.format("%d", recorderSettings.bufferLength));
		}
		
		boolean getParams() {
			if (recorderSettings.autoStart = autoAsLast.isSelected()) {
				recorderSettings.startStatus = 0;
			}
			else if (autoStart.isSelected()){
				recorderSettings.startStatus = RecorderView.BUTTON_START;
			}
			else if (autoCycle.isSelected()) {
				recorderSettings.startStatus = RecorderView.BUTTON_AUTO;
			}
			else {
				recorderSettings.startStatus = RecorderView.BUTTON_OFF;
			}
			recorderSettings.enableBuffer = enableBuffer.isSelected();
			try {
				recorderSettings.bufferLength = Integer.valueOf(bufferLength.getText());
			}
			catch (NumberFormatException Ex) {
				return false;
			}
			return true;
		}
	}
	
	class FileLengthPanel extends JPanel {
		
		JCheckBox limitLengthSeconds, limitLengthMegaBytes, roundFileStarts;
		JTextField maxLengthSeconds, maxLengthMegaBytes;
		
		FileLengthPanel() {
			super();
			limitLengthSeconds = new JCheckBox("Limit file length to ");
			limitLengthSeconds.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					enableControls();
				}
			});
			limitLengthMegaBytes = new JCheckBox("Limit file size to ");
			limitLengthMegaBytes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					enableControls();
				}
			});
			maxLengthSeconds = new JTextField(5);
			maxLengthMegaBytes = new JTextField(5);
			roundFileStarts = new JCheckBox("Round file start times");
			roundFileStarts.setToolTipText("File start times will be rounded to rigidly fixed times");
			setBorder(new TitledBorder("Maximum file lengths"));
			GridBagConstraints c = new GridBagConstraints();
			GridBagLayout gb = new GridBagLayout();
			setLayout(gb);
			c.gridx = c.gridy = 0;
			c.anchor = GridBagConstraints.WEST;
			addComponent(this, limitLengthSeconds, c);
			c.gridx++;
			addComponent(this, maxLengthSeconds, c);
			c.gridx++;
			addComponent(this, new JLabel(" Seconds"), c);
			c.gridx++;
			addComponent(this, roundFileStarts, c);
			c.gridx = 0;
			c.gridy = 1;
			addComponent(this, limitLengthMegaBytes, c);
			c.gridx++;
			addComponent(this, maxLengthMegaBytes, c);
			c.gridx++;
			addComponent(this, new JLabel(" Mega Bytes"), c);
			
			maxLengthSeconds.setToolTipText("<html>Short files can be easier to process with software such as Matlab.  "
					+ "However, too many short files can also slow data archiving.<br>");
			maxLengthMegaBytes.setToolTipText("<html>Files should always be restricted to an absolute maximum length of 2000 Mega Bytes.<br>");
		}

		private void enableControls() {
			roundFileStarts.setEnabled(limitLengthSeconds.isSelected());
			maxLengthSeconds.setEnabled(limitLengthSeconds.isSelected());
			maxLengthMegaBytes.setEnabled(limitLengthMegaBytes.isSelected());
		}
		void setParams() {
			limitLengthSeconds.setSelected(recorderSettings.limitLengthSeconds);
			limitLengthMegaBytes.setSelected(recorderSettings.limitLengthMegaBytes);
			maxLengthSeconds.setText(String.format("%d", recorderSettings.maxLengthSeconds));
			maxLengthMegaBytes.setText(String.format("%d", recorderSettings.maxLengthMegaBytes));
			roundFileStarts.setSelected(recorderSettings.isRoundFileStarts());
			enableControls();
		}
		boolean getParams() {
			recorderSettings.limitLengthSeconds = limitLengthSeconds.isSelected();
			recorderSettings.limitLengthMegaBytes = limitLengthMegaBytes.isSelected();
			try {
				recorderSettings.maxLengthSeconds = Integer.valueOf(maxLengthSeconds.getText());
				recorderSettings.maxLengthMegaBytes = Integer.valueOf(maxLengthMegaBytes.getText());
			}
			catch (NumberFormatException Ex) {
				return showWarning("Error in file length or size parameter");
			}
			recorderSettings.setRoundFileStarts(roundFileStarts.isSelected());
			
			return true;
		}
	}
	
	class OutputFormat extends JPanel {
		
		private JTextField fileInitials;
		
		private JComboBox fileType;

		private JComboBox<Integer> bitDepth;
		
		OutputFormat() {
			super();
			fileType = new JComboBox();
			bitDepth = new JComboBox<Integer>();
			for (int i = 0; i < RecorderSettings.BITDEPTHS.length; i++) {
				bitDepth.addItem(RecorderSettings.BITDEPTHS[i]);
			}
			setBorder(new TitledBorder("Output file location, names and format"));
			GridBagConstraints c = new PamGridBagContraints();
			GridBagLayout gb = new GridBagLayout();
			setLayout(gb);
			c.gridx = c.gridy = 0;
			c.gridwidth = 7;
			addComponent(this, new JLabel("Output Folder"), c);
			c.gridy++;
			addComponent(this, selectFolder.getFolderPanel(), c);
			c.gridwidth = 1;
			c.gridy++;
			c.anchor = GridBagConstraints.WEST;
			addComponent(this, new JLabel("File name prefix "), c);
			c.gridx++;
			addComponent(this, fileInitials = new JTextField(5), c);
			c.gridx++;
			addComponent(this, new JLabel("    File type "), c);
			c.gridx++;
			addComponent(this, fileType, c);
			c.gridx++;
			addComponent(this, new JLabel("    Bit depth "), c);
			c.gridx++;
			addComponent(this, bitDepth, c);
			c.gridwidth = 7;
			c.gridx = 0;
			c.gridy ++;
			String infLabel;
			addComponent(this, new JLabel(infLabel = "(file names automatically contain the date in the format YYYYMMDD_HHMMSS_mmm)"), c);
			fileInitials.setToolTipText("Characters forming the start of each file name");
			fileType.setToolTipText("<html>Recording format.  If X3 is listed, ONLY use it for creating a Decimus XML parameters file.<br>" +
			"X3 recording is not implemented in the standard PAMGuard application.</html>");
			bitDepth.setToolTipText("File bit depth (N.B. there is no point in generating 24 bit files from 16 bit sound card input)");
			
		}
		void setParams() {
			fileType.removeAllItems();
			AudioFileFormat.Type types[] = AudioSystem.getAudioFileTypes();
			for (int i = 0; i < types.length; i++) {
				fileType.addItem(types[i]);
			}
			
			// if the decimus flag is set, add the X3 file type
			if (SMRUEnable.isEnableDecimus()) {
				fileType.addItem(RecorderControl.X3);
			}

			fileType.setSelectedItem(recorderSettings.getFileType());
			fileInitials.setText(recorderSettings.fileInitials);
			bitDepth.setSelectedItem(recorderSettings.bitDepth);
		}
		boolean getParams() {
			recorderSettings.setFileType((AudioFileFormat.Type) fileType.getSelectedItem());
			if (recorderSettings.getFileType() == null) return false;
			try {
				recorderSettings.fileInitials = fileInitials.getText();
			}
			catch (NullPointerException Ex) {
				return showWarning("Error in file initials");
			}
			try {
				recorderSettings.bitDepth = (int) bitDepth.getSelectedItem();
			}
			catch (Exception e) {
				return showWarning("Error in Bit Depth selction");
			}
			return true;
		}
	}
	
	class AutoPanel extends JPanel {
		JTextField autoInterval, autoDuration;
		AutoPanel() {
			super();
			setBorder(new TitledBorder("Automatic recordings duty cycle settings"));
			autoInterval = new JTextField(4);
			autoDuration = new JTextField(4);
			GridBagConstraints c = new GridBagConstraints();
			GridBagLayout gb = new GridBagLayout();
			setLayout(gb);
			c.gridx = c.gridy = 0;
			c.anchor = GridBagConstraints.WEST;
//			addComponent(this, new JLabel("Interval between recordings "), c);
			addComponent(this, new JLabel("Recording length "), c);
			c.gridx++;
			addComponent(this, autoDuration, c);
			c.gridx ++;
			addComponent(this, new JLabel(" s"), c);
			c.gridy ++;
			c.gridx = 0;
			addComponent(this, new JLabel("Total Cycle Time "), c);
			c.gridx++;
			addComponent(this, autoInterval, c);
			c.gridx ++;
			addComponent(this, new JLabel(" s"), c);
			autoDuration.setToolTipText("The duration of automatic recordings in seconds");
//			autoInterval.setToolTipText("<html>The interval betwen the starts of automatic recordings.<br>(Not the gap from the end of one to the start of the next)</html>");
			autoInterval.setToolTipText("<html>The total time from the start of one recording to the<br>start of the next recording.<br>This includes the recording length (above) as well<br>as the time that nothing is being recorded</html>");
		}
		void setParams() {
			autoInterval.setText(String.format("%d", recorderSettings.autoInterval));
			autoDuration.setText(String.format("%d", recorderSettings.autoDuration));
		}
		boolean getParams() {
			try {
				recorderSettings.autoInterval = Integer.valueOf(autoInterval.getText());
				recorderSettings.autoDuration = Integer.valueOf(autoDuration.getText());
			}
			catch (NumberFormatException Ex) {
				return showWarning("Automatic Recordings", "Recording Interval and Length values must be positive whole numbers");
			}
			if (recorderSettings.autoDuration <= 0) {
				return showWarning("Sound Recorder Configuration", "The duration of automatic recordings must be greater than 0s");
			}
			if (recorderSettings.autoInterval <= recorderSettings.autoDuration) {
				return showWarning("Sound Recorder Configuration",
						"The interval between starts of automatic recordings must be greater than their duration");
			}
//			if (recorderSettings.maxLengthSeconds < recorderSettings.autoDuration) {
//				return showWarning("Sound Recorder Configuration",
//						"The maximum file length in the Files and Folders tab is less than the desired automatic recording length in the Control tab");
//			}
			return true;
		}
	}
}
