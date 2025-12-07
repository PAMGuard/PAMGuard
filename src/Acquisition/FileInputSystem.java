package Acquisition;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.frame.Frame;
import org.jflac.metadata.StreamInfo;
import org.jflac.sound.spi.FlacEncoding;
import org.jflac.util.ByteData;
import org.pamguard.x3.sud.Chunk;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudFileListener;

import Acquisition.filedate.FileDate;
import Acquisition.filedate.FileDateDialogStrip;
import Acquisition.filedate.FileDateObserver;
import Acquisition.filetypes.SoundFileType;
import Acquisition.pamAudio.PamAudioFileFilter;
import Acquisition.pamAudio.PamAudioFileManager;
import PamController.DataInputStore;
import PamController.InputStoreInfo;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.worker.PamWorkMonitor;
import PamUtils.worker.filelist.WavFileType;
import PamView.dialog.PamLabel;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamPanel;
import PamView.panel.PamProgressBar;
import pamguard.GlobalArguments;
import warnings.PamWarning;

//import org.kc7bfi.jflac.FLACDecoder;
//import org.kc7bfi.jflac.PCMProcessor;
//import org.kc7bfi.jflac.frame.Frame;
//import org.kc7bfi.jflac.metadata.SeekPoint;
//import org.kc7bfi.jflac.metadata.StreamInfo;
//import org.kc7bfi.jflac.sound.spi.FlacEncoding;
//import org.kc7bfi.jflac.util.ByteData;

import wavFiles.ByteConverter;

/**
 * Implementation of DaqSystem for reading data from audio files.
 *
 * @author Doug Gillespie
 * @see Acquisition.DaqSystem
 * @see Acquisition.AcquisitionProcess
 * @see FolderInputSystem
 *
 */
public class FileInputSystem  extends DaqSystem implements ActionListener, PamSettings, FileDateObserver, DataInputStore {

	public static final String sysType = "Audio File";

	private JPanel daqDialog;

	protected JComboBox fileNameCombo;

	protected JButton fileSelect;

	//	protected JTextField fileDateText;

	protected AcquisitionDialog acquisitionDialog;

	protected FileInputParameters fileInputParameters = new FileInputParameters(sysType);

	protected AcquisitionControl acquisitionControl;

	protected int blockSamples = 4800;

	protected PamProgressBar fileProgress = new PamProgressBar(PamProgressBar.defaultColor);

	protected PamLabel etaLabel;

	protected PamLabel speedLabel;

	/**
	 * using a system.currentTimeMS not PamCalander time to predict eta.
	 */
	protected long fileStartTime;

	protected volatile boolean dontStop;

	private double fileData[];

	protected AudioFormat audioFormat;

	protected AudioInputStream audioStream;

	protected CollectorThread collectorThread;

	protected Thread theThread;

	protected AudioDataQueue newDataUnits;

	long startTimeMS;

	int nChannels;

	float sampleRate;

	//	protected FileDate fileDate = new StandardFileDate();

	long fileDateMillis;

	//	long fileLength;

	long fileSamples;

	long readFileSamples;

	long millisToSkip;

	protected JCheckBox repeat;

	protected ByteConverter byteConverter;

	protected FileDateDialogStrip fileDateStrip;

	/**
	 * Sound file types present in the current selections.
	 */
	private List<SoundFileType> selectedFileTypes;

	/**
	 * Text field for skipping initial few seconds of a file.
	 */
	private JTextField skipSecondsField;

	/**
	 * PamWarning when something goes wrong
	 */
	private PamWarning fileWarning;

	private SudAudioInputStream sudAudioInputStream;

	private SudListener sudListener;

	private boolean fullyStopped;

	/**
	 * Current analysis time - start of last data unit created. 
	 * Can help to control restarts. 
	 */
	protected volatile long currentAnalysisTime;

	private WavFileType currentFile;

	public FileInputSystem(AcquisitionControl acquisitionControl) {
		this.acquisitionControl = acquisitionControl;
		PamSettingManager.getInstance().registerSettings(this);
		fileWarning = new PamWarning(acquisitionControl.getUnitName(), "File System Warning", 2);
	}

	@Override
	public JPanel getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {

		this.acquisitionDialog = acquisitionDialog;

		return getDialogPanel();
	}

	/**
	 * Gets and where necessary creates ...
	 * @return Daq dialog panel.
	 */
	protected JPanel getDialogPanel() {
		if (daqDialog == null) {
			daqDialog = createDaqDialogPanel();
		}
		return daqDialog;
	}

	protected JPanel createDaqDialogPanel() {

		PamPanel p = new PamPanel();
		p.setBorder(new TitledBorder("Select sound file"));
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{100, 100, 10};
		p.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2,2,2,2);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		addComponent(p, fileNameCombo = new JComboBox(), constraints);
		fileNameCombo.addActionListener(this);
		fileNameCombo.setMinimumSize(new Dimension(30,2));
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		addComponent(p, repeat = new JCheckBox("Repeat"), constraints);
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.EAST;
		addComponent(p, fileSelect = new JButton("Select File"), constraints);
		fileSelect.addActionListener(this);
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 3;
		fileDateStrip = new FileDateDialogStrip(acquisitionControl.getFileDate(), acquisitionControl.getGuiFrame());
		fileDateStrip.addObserver(this);
		p.add(fileDateStrip.getDialogComponent(), constraints);

		//		if (SMRUEnable.isEnable()) {
		// no reason to hide this option from users.
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 1;

		PamPanel skipPanel = new PamPanel(new GridBagLayout()); 

		addComponent(skipPanel,  new JLabel("Skip initial "), constraints);
		constraints.gridx++;
		addComponent(skipPanel, skipSecondsField = new JTextField(4), constraints);
		constraints.gridx++;
		addComponent(skipPanel,  new JLabel("seconds"), constraints);
		constraints.anchor = GridBagConstraints.EAST;
		
		constraints.gridwidth = 3;
		constraints.gridx = 0; 
		addComponent(p,  skipPanel, constraints);

		//		}

		//		addComponent(p, new JLabel("File date :"), constraints);
		//		constraints.gridx++;
		//		constraints.fill = GridBagConstraints.HORIZONTAL;
		//		constraints.gridwidth = 2;
		//		addComponent(p, fileDateText = new JTextField(), constraints);
		//		fileDateText.setEnabled(false);

		return p;

	}

	void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}

	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the FileInputParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (fileInputParameters.systemType==null) fileInputParameters.systemType=getSystemType();

		fillFileList();

		if (repeat != null) {
			repeat.setSelected(fileInputParameters.repeatLoop);
		}
		if (skipSecondsField!=null) {
			skipSecondsField.setText(String.format("%.1f", fileInputParameters.skipStartFileTime/1000.));
		}

	}

	private void fillFileList() {
		// the array list will always be set up so that the items are in most
		// recently used order ...
		fileNameCombo.removeAllItems();
		String file;
		if (fileInputParameters.recentFiles.size() == 0) return;
		for (String element : fileInputParameters.recentFiles) {
			file = element;
			if (file == null || file.length() == 0) continue;
			fileNameCombo.addItem(file);
		}
		fileNameCombo.setSelectedIndex(0);
	}

	@Override
	public boolean dialogGetParams() {
		String file = (String) fileNameCombo.getSelectedItem();
		if (file != null && file.length() > 0) {
			fileInputParameters.recentFiles.remove(file);
			fileInputParameters.recentFiles.add(0, file);
			// check we're not building up too long a list.
			while (fileInputParameters.recentFiles.size() > FileInputParameters.MAX_RECENT_FILES) {
				fileInputParameters.recentFiles.remove(fileInputParameters.recentFiles.size()-1);
				fileInputParameters.recentFiles.trimToSize();
			}
		}

		if (repeat == null) {
			fileInputParameters.repeatLoop = false;
		}
		else {
			fileInputParameters.repeatLoop = repeat.isSelected();
		}

		if (skipSecondsField!=null) {
			try {
				Double skipSeconds = Double.valueOf(skipSecondsField.getText())*1000.; // saved in millis.
				fileInputParameters.skipStartFileTime = skipSeconds.longValue();
			}
			catch (Exception e) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String getSystemType() {
		return sysType;
	}

	@Override
	public String getSystemName() {
		if ((fileInputParameters.recentFiles == null) || (fileInputParameters.recentFiles.size() < 1)) return null;
		File f = getCurrentFile();
		if (f == null) return null;
		return f.getName();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == fileSelect) {
			selectFile();
		}
		else if (e.getSource() == fileNameCombo) {
			setNewFile((String) fileNameCombo.getSelectedItem());
		}

	}
	protected void selectFile() {
		//IshmaelDetector.MatchFiltParamsDialog copies a bunch of this.  If you
		//modifiy this, please check that too.

		String currFile = (String) fileNameCombo.getSelectedItem();
		// seems to just support aif and wav files at the moment
		//		Type[] audioTypes = AudioSystem.getAudioFileTypes();
		//		for (int i = 0; i < audioTypes.length; i++) {
		//			System.out.println(audioTypes[i]);
		//		}
		//		AudioStream audioStream = AudioSystem.getaudioin
		JFileChooser fileChooser = null;
		if (fileChooser == null) {
			fileChooser = new PamFileChooser();
			fileChooser.setFileFilter(new PamAudioFileFilter());
			fileChooser.setDialogTitle("Select audio input file...");
			//fileChooser.addChoosableFileFilter(filter);
			fileChooser.setFileHidingEnabled(true);
			fileChooser.setApproveButtonText("Select");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			javax.swing.filechooser.FileFilter[] filters = fileChooser
					.getChoosableFileFilters();
			for (FileFilter filter : filters) {
				fileChooser.removeChoosableFileFilter(filter);
			}
			fileChooser.addChoosableFileFilter(new PamAudioFileFilter());

			if (currFile!=null) {
				fileChooser.setSelectedFile(new File(currFile));
			}
		}
		int state = fileChooser.showOpenDialog(daqDialog);
		if (state == JFileChooser.APPROVE_OPTION) {
			currFile = fileChooser.getSelectedFile().getAbsolutePath();
			//System.out.println(currFile);
			setNewFile(currFile);
		}
	}

	/**
	 * Called when user selects a file in the dialog.
	 * @param newFile
	 */
	public void setNewFile (String newFile) {
		if (newFile == null) {
			return;
		}
		String currentFirst = getFirstFile();
		if (newFile.equals(currentFirst) == false) {
			fileInputParameters.recentFiles.remove(newFile);
			fileInputParameters.recentFiles.add(0, newFile);
			fillFileList();
		}
		interpretNewFile(new WavFileType(newFile));
	}
	
	public String getFirstFile() {
		if (fileInputParameters.recentFiles.size() == 0) {
			return null;
		}
		return fileInputParameters.recentFiles.get(0);
	}

	/**
	 * Called when a new file or folder is selected.
	 * @param newFile
	 */
	public void interpretNewFile(WavFileType newFile){
		if ((newFile == null) || (newFile.length() == 0)) return;

		File file = newFile.getAbsoluteFile();

		setSelectedFileTypes(acquisitionControl.soundFileTypes.getUsedTypes(file));

		if (file == null) return;
		
		fileDateMillis = newFile.getStartMilliseconds();
		if (fileDateMillis == 0) {
			// try to work out the date of the file
			fileDateMillis = getFileStartTime(newFile);
		}
		fileDateStrip.setDate(fileDateMillis);
		fileDateStrip.setFormat(acquisitionControl.getFileDate().getFormat());
		//		if (fileDateMillis <= 0) {
		//			fileDateText.setText("Unknown file time");
		//		}
		//		else if (fileDateText != null) {
		//			fileDateText.setText(PamCalendar.formatDateTime(fileDateMillis));
		//		}
		// work out the number of channels and sample rate and set them in the main dialog
		//		acquisitionDialog.NotifyChange();
		if (file.isFile() && !file.isHidden() && acquisitionDialog != null) {
			try {

//				System.out.println("FileInputSystem - interpretNewFile");
				AudioInputStream audioStream = PamAudioFileManager.getInstance().getAudioInputStream(file);

				      // Get additional information from the header if it's a wav file.
//								if (WavFileInputStream.class.isAssignableFrom(audioStream.getClass())) {
//									WavHeader wavHeader = ((WavFileInputStream) audioStream).getWavHeader();
//									int nChunks = wavHeader.getNumHeadChunks();
//									for (int i = 0; i < nChunks; i++) {
//										WavHeadChunk aChunk = wavHeader.getHeadChunk(i);
//										System.out.println(String.format("Chunk %d %s: %s", i, aChunk.getChunkName(), aChunk.toString()));
//									}
//								}

				if (audioStream instanceof SudAudioInputStream) {
					acquisitionControl.getSUDNotificationManager().interpretNewFile(newFile.getAbsolutePath(), (SudAudioInputStream) audioStream);
				}

				AudioFormat audioFormat = audioStream.getFormat();
				//				fileLength = file.length();
				fileSamples = audioStream.getFrameLength();
				if (currentFile != null && currentFile.getMaxSamples() > 0) {
					fileSamples = currentFile.getMaxSamples();
				}
				acquisitionDialog.setSampleRate(audioFormat.getSampleRate());
				acquisitionDialog.setChannels(audioFormat.getChannels());
				fileInputParameters.bitDepth = audioFormat.getSampleSizeInBits();
				audioStream.close();
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
			loadByteConverter(audioFormat);
		}
	}

	protected boolean loadByteConverter(AudioFormat audioFormat) {
		byteConverter = ByteConverter.createByteConverter(audioFormat);
		return (byteConverter != null);
	}

	@Override
	public void setStreamStatus(int streamStatus) {
		super.setStreamStatus(streamStatus);
		// file has ended, so notify the daq control.
		if (streamStatus == STREAM_ENDED) {
			// tell the rest of PAMGUARD to stop.
			PamController.getInstance().stopLater();
		}
	}

	@Override
	public int getMaxChannels() {
		return PARAMETER_FIXED;
	}

	@Override
	public int getMaxSampleRate() {
		return PARAMETER_FIXED;
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getPeak2PeakVoltage()
	 */
	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return PARAMETER_UNKNOWN;
	}

	@Override
	public boolean isRealTime() {
		return fileInputParameters.realTime;
	}

	public long getSkipStartFileTime() {
		return fileInputParameters.skipStartFileTime;
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Serializable getSettingsReference() {
		return fileInputParameters;
	}

	@Override
	public long getSettingsVersion() {
		return FileInputParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		//		return "File Input System";
		return acquisitionControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		//		return "Acquisition System";
		return "File Input System";
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		//		if (PamSettingManager.getInstance().isSettingsUnit(this, pamControlledUnitSettings)) {
		fileInputParameters = ((FileInputParameters) pamControlledUnitSettings.getSettings()).clone();
		return fileInputParameters != null;
		//		}
		//		return false;
	}

	public int getChannels() {
		return PARAMETER_UNKNOWN;
	}

	public float getSampleRate() {
		if (audioFormat == null) {
			return PARAMETER_UNKNOWN;
		}
		else {
			return audioFormat.getSampleRate();
		}
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {

		this.acquisitionControl = daqControl;

		fileSamples = 0;
		PamCalendar.setSoundFileTimeInMillis(0);
		// check a sound file is selected and open it.
		//		if (fileInputParameters.recentFiles == null) return false;
		//		if (fileInputParameters.recentFiles.size() < 1) return false;
		//		String fileName = fileInputParameters.recentFiles.get(0);
		if (!runFileAnalysis()) {
			return false;
		}
		return true;
	}

	public WavFileType getCurrentFile() {
//		System.out.println("fileInputParameters: " + fileInputParameters);
		if ((fileInputParameters.recentFiles == null) || (fileInputParameters.recentFiles.size() < 1)) return null;
		String fileName = fileInputParameters.recentFiles.get(0);
		return new WavFileType(new File(fileName));
	}


	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getDataUnitSamples()
	 */
	@Override
	public int getDataUnitSamples() {
		return blockSamples;
	}

	/**
	 * Open the audio stream for processing.
	 * @return true if audio stream opened correctly.
	 */
	public boolean prepareInputFile() {

		currentFile = getCurrentFile();
		if (currentFile == null || currentFile.exists() == false) {
			String warning;
			if (currentFile == null) {
				warning = "No sound input file has been selected";
			}
			else {
				warning = "The sound file " + currentFile.getAbsolutePath() + " does not exist";
			}
			WarnOnce.showWarning(acquisitionControl.getGuiFrame(),  "Sound Acquisition system", warning, WarnOnce.WARNING_MESSAGE);
			return false;
		}
		
//		System.out.printf("***********************************             Opening file %s\n", currentFile.getName());

		try {

			if (audioStream != null) {
				audioStream.close();
			}

//			System.out.println("FileInputSystem - prepareInputFile");

			audioStream = PamAudioFileManager.getInstance().getAudioInputStream(currentFile);


			if (audioStream instanceof SudAudioInputStream) {
				sudAudioInputStream = (SudAudioInputStream) audioStream;
				if (sudListener == null) {
					sudListener = new SudListener();
				}
				sudAudioInputStream.addSudFileListener(sudListener);
//				sudAudioInputStream.ad
				acquisitionControl.getSUDNotificationManager().newSudInputStream(sudAudioInputStream);
			}
			else {
				sudAudioInputStream = null;
			}

			if (audioStream == null) {
				return false;
			}

			audioFormat = audioStream.getFormat();

			if (audioFormat==null) {
				System.err.println("AudioFormat was null: " + currentFile.getAbsolutePath());
				return false;
			}
			long toSkip = (long) (millisToSkip * audioFormat.getFrameRate() / 1000) * audioFormat.getFrameSize();
			// this next line deals with harp data offsets.
			toSkip += currentFile.getSamplesOffset() * audioFormat.getFrameSize();
			if (toSkip > 0) {
				audioStream.skip(toSkip);
			}
			millisToSkip = 0; // only ever used once at startup. 

			//			fileLength = currentFile.length();
			fileSamples = audioStream.getFrameLength();
			if (currentFile.getMaxSamples() > 0) {
				fileSamples = currentFile.getMaxSamples();
			}
			readFileSamples = 0;

			acquisitionControl.getAcquisitionProcess().setSampleRate(audioFormat.getSampleRate(), true);
			fileInputParameters.bitDepth = audioFormat.getSampleSizeInBits();

			loadByteConverter(audioFormat);

//			System.out.println("FileInputSystem - prepareInputFile done");


		} catch (UnsupportedAudioFileException ex) {
			ex.printStackTrace();
			return false;
		} catch (FileNotFoundException ex) {
			System.out.println("Input filename: '" + fileNameCombo + "' not found.");
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private class SudListener implements SudFileListener {

		@Override
		public void chunkProcessed(int chunkID, Chunk sudChunk) {
			acquisitionControl.getSUDNotificationManager().chunkProcessed(chunkID, sudChunk);
		}

	}


	public boolean runFileAnalysis() {
		// keep a reference to where data will be put.
		this.newDataUnits = acquisitionControl.getDaqProcess().getNewDataQueue();

		//		if (this.newDataUnits == null) {
		//			System.err.println("newDataUnits: == null: ");
		//			return false;
		//		}

		if (!prepareInputFile() && getCurrentFile()!=null) {

			String audioFileStr = getCurrentFile()==null? "Null File": getCurrentFile().getAbsolutePath();
			String title = "Error loading audio file";
			String msg = "<html><p>There was an error trying to access the audio file </p><b> " +
					audioFileStr +
					"</b><br><br><p>Please check to ensure that the file exists, and that the path entered in PAMGuard is correct.</p>" +
					"<p>Note this error may also indicate that the file is corrupt and unreadable by PAMGuard.</p></html>";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
			return false;
		}

		PamCalendar.setSoundFile(true);
		PamCalendar.setSoundFileTimeInMillis(0);
		long fileTime = getFileStartTime(getCurrentFile());
		if (currentAnalysisTime > 0) {
			PamCalendar.setSessionStartTime(currentAnalysisTime);
		}
		else if (fileTime > 0) {
			PamCalendar.setSessionStartTime(fileTime);
		}
		else {
			PamCalendar.setSessionStartTime(System.currentTimeMillis());
		}

		setStreamStatus(STREAM_OPEN);

		// ideally we would get this from the file information.
		this.startTimeMS = PamCalendar.getTimeInMillis();

		if (audioFormat==null) {

			String audioFileStr = getCurrentFile()==null? "Null File": getCurrentFile().getAbsolutePath();

			System.err.println("FileInputSystem: runFileAnalysis: AudioFile format is null: " + audioFileStr);

			return false;
		}

		nChannels = audioFormat.getChannels();

		acquisitionControl.getDaqProcess().setSampleRate(sampleRate = audioFormat.getSampleRate(), true);
		//		System.out.println("Audio sample rate set to " + sampleRate);

		blockSamples = Math.max((int) sampleRate / 10, 1000); // make sure the
		// block has at
		// least 1000 samples
		acquisitionControl.getDaqProcess().setNumChannels(nChannels);

		//daqControl.getDaqProcess().getRawDataBlock().SetInfo(nChannels, sampleRate, blockSamples);
		fileStartTime = System.currentTimeMillis();

		collectorThread = new CollectorThread();

		theThread = new Thread(collectorThread);

		return true;
	}

	/**
	 * Interpret the file name to get the file time.
	 * <p>Moved to a separate function so it can be overridden in a special version
	 * for the DCL5 data set.
	 * @param file audio file.
	 * @return time in milliseconds.
	 */
	public long getFileStartTime(File file) {
		// if there is no file, return 0
		if (file==null) return 0;
		if (file instanceof WavFileType) {
			WavFileType wt = (WavFileType) file;
			if (wt.getStartMilliseconds() > 0) {
				return wt.getStartMilliseconds();
			}
		}
		return acquisitionControl.getFileDate().getTimeFromFile(file);
	}
	
//	/**
//	 * Get the file duration, either from the file type or the audio format. 
//	 * @param file
//	 * @param af
//	 * @return
//	 */
//	public long getFileDuration(File file, AudioFormat af) {
//		if (file instanceof WavFileType) {
//			WavFileType wt = (WavFileType) file;
//			if (wt.getDurationInSeconds() > 0) {
//				return (long) (wt.getDurationInSeconds() * 1000.);
//			}
//		}
////		return (long) af.getFrameLength() * 1000L / (long) af.getFormat().getFrameSize();	
//		return af.get
//	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {

		if (audioStream == null) return false;

		dontStop = true;

		fullyStopped = false;

		theThread.start();

		setStreamStatus(STREAM_RUNNING);

		return true;
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		/*
		 * This only gets called when daq is stopped manually from the GUI menu.
		 * It does not get called when a file ends.
		 */
		boolean stillRunning = (audioStream != null);

		dontStop = false; // flag to tell the file reading thread to exit

		int count = 0;
		while (++count < 20 && audioStream != null) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ex){
				ex.printStackTrace();
			}
		}

		//		if (audioStream != null) {
		//			try{
		//				audioStream.close();
		//				audioStream = null;
		//			}
		//			catch (Exception Ex) {
		//				Ex.printStackTrace();
		//			}
		//		}

		systemHasStopped(stillRunning);
	}

	public void systemHasStopped(boolean wasRunning) {
		if (fullyStopped) {
			return;
		}
		long stopTime = System.currentTimeMillis();
		if (getCurrentFile() == null) {
			return;
		}
//		double fileSecs = readFileSamples / getSampleRate();
//		double analSecs = (stopTime - fileStartTime) / 1000.;
//		System.out.println(String.format("File %s, SR=%dHz, length=%3.1fs took %3.1fs = %3.1fx real time",
//				getCurrentFile().getName(), (int)getSampleRate(), fileSecs, analSecs, fileSecs / analSecs));
		fullyStopped = true;
	}

	/**
	 * FLAC uses such a different way of decoding data to wav and aif
	 * that it needs to use it's own reader.
	 */
	protected void collectFlacData() {
		FileInputStream fileStream;
		try {
			File currFile = getCurrentFile();
			if (currFile == null) {
				return;
			}
			fileStream = new FileInputStream(getCurrentFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		FLACDecoder flacDecoder = new FLACDecoder(fileStream);
		flacDecoder.addPCMProcessor(new FlacPCM(fileStream, 0));
		//			try {
		//				StreamInfo streamInfo = flacDecoder.readStreamInfo();
		////				audioFormat = streamInfo.getAudioFormat();
		//			} catch (IOException e1) {
		//				e1.printStackTrace();
		//			}
		Frame flacFrame;
		ByteData byteData = null;
		long decodePoint = 0;
		try {
			//				while (!flacDecoder.isEOF()) {
			flacDecoder.decode();
			System.out.println("Flac decode call complete");
			//					flacDecoder.decode(new SeekPoint(decodePoint, 0, 0), new SeekPoint(decodePoint+blockSamples, 0, 0));
			//					decodePoint += blockSamples;
			//					flacFrame = flacDecoder.readNextFrame();
			//					byteData = flacDecoder.decodeFrame(flacFrame, byteData);
			//				}
		} catch (IOException e) {
			//							e.printStackTrace();
		}
		try {
			fileStream.close();
		} catch (IOException e) {
		}

		setStreamStatus(STREAM_ENDED);
	}

	private class FlacPCM implements PCMProcessor {

		private ByteConverter byteConverter;
		private FileInputStream fileStream;
		private int frameSize;
		private long totalSamples;
		private long lastProgressUpdate;
		private long lastProgressTime;
		private int channelOffset;

		public FlacPCM(FileInputStream fileStream, int channelOffset) {
			this.fileStream = fileStream;
			this.channelOffset = channelOffset;
		}

		@Override
		public void processPCM(ByteData byteData) {
			if (!dontStop) {
				try {
					fileStream.close(); // will make the flac reader bomb out !
				}
				catch (IOException e) {

				}
				return;
			}
			//				System.out.println("processPCM(ByteData arg0)");
			int newSamples = byteData.getLen() / frameSize;
			double[][] doubleData = new double[nChannels][newSamples];
			byteConverter.bytesToDouble(byteData.getData(), doubleData, byteData.getLen());

			long ms = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples);
			currentAnalysisTime = ms + (long) (newSamples * 1000L / sampleRate);
			RawDataUnit newDataUnit = null;
			for (int ichan = 0; ichan < nChannels; ichan++) {

				newDataUnit = new RawDataUnit(ms, 1 << (ichan+channelOffset), totalSamples, newSamples);
				newDataUnit.setRawData(doubleData[ichan]);

				newDataUnits.addNewData(newDataUnit);

				// GetOutputDataBlock().addPamData(pamDataUnit);
			}
			//				System.out.println(String.format("new samps %d at %s", newSamples, PamCalendar.formatTime(ms, 3)));

			totalSamples += newSamples;
			readFileSamples += newSamples;

			long blockMillis = (int) ((newDataUnit.getStartSample() * 1000) / sampleRate);
			//				newDataUnit.timeMilliseconds = blockMillis;
			PamCalendar.setSoundFileTimeInMillis(blockMillis);
			if (fileSamples > 0 && totalSamples - lastProgressUpdate >= getSampleRate()*2) {
				int progress = (int) (1000 * readFileSamples / fileSamples);
				fileProgress.setValue(progress);
				sayEta();
				long now = System.currentTimeMillis();
				if (lastProgressTime > 0 && totalSamples > lastProgressUpdate && now-lastProgressTime > 1000) {
					double speed = (double) (totalSamples - lastProgressUpdate) /
							getSampleRate() / ((now-lastProgressTime)/1000.);
					speedLabel.setText(String.format(" (%3.1f X RT)", speed));
				}
				lastProgressTime = now;
				lastProgressUpdate = totalSamples;
			}

			while (newDataUnits.getQueueSize() > 3*nChannels) {
				if (!dontStop) break;
				try {
					Thread.sleep(2);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		@Override
		public void processStreamInfo(StreamInfo streamInfo) {
			frameSize = audioFormat.getChannels() * audioFormat.getSampleSizeInBits() / 8;
			byteConverter = ByteConverter.createByteConverter(audioFormat.getSampleSizeInBits()/8, false, Encoding.PCM_SIGNED);
			fileSamples = streamInfo.getTotalSamples();
		}

	}

	public class CollectorThread implements Runnable {

		@Override
		public void run() {
			if (audioFormat.getEncoding() == FlacEncoding.FLAC) {
				collectFlacData();
			}
			else {
				CollectData();
			}
		}

		private void CollectData() {
			/*
			 * keep reading blocks of data from the file and creating
			 * PamDataUnits from them. Once a unit is created, tell this thread
			 * to wait until it has been used by the main thread.
			 */
//			System.out.println("File system start processing");
			/*
			 * File should have been opened in the constructor so just read it
			 * in in chunks and pass to datablock
			 */
			int blockSize = blockSamples * audioFormat.getFrameSize();
			int bytesRead = 0;
			byte[] byteArray = new byte[blockSize];
			long totalSamples = 0;
			long lastProgressUpdate = 0;
			long lastProgressTime = 0;
			int newSamples;
			double[][] doubleData;
			short sample;
			int startbyte;
			RawDataUnit newDataUnit = null;
			long ms;
			long maxSamples = (long) Integer.MAX_VALUE * 2L;
			if (currentFile.getMaxSamples() > 0) {
				maxSamples = currentFile.getMaxSamples();
			}
			long maxBytes = maxSamples * audioFormat.getFrameSize();
			long totalBytesRead = 0;


			while (dontStop && audioStream != null) {
				int toRead = (int) Math.min(blockSize, maxBytes-totalBytesRead);
				try {
					bytesRead = audioStream.read(byteArray, 0, toRead);
				} catch (Exception ex) {
					ex.printStackTrace();
					break; // file read error
				}
				totalBytesRead += bytesRead;
				while (bytesRead < toRead) {
					// for single file operation, don't do anything, but need to have a hook
					// in here to read multiple files, in which case we may just get the extra
					// samples from the next file.
					if (bytesRead == -1) {
						bytesRead = 0;
					}
					if (openNextFile(totalSamples + bytesRead / audioFormat.getFrameSize())) {
						try {
							int newBytes = audioStream.read(byteArray, bytesRead, blockSize - bytesRead);
							if (newBytes == -1) {
								break;
							}
							bytesRead += newBytes;
						} catch (Exception ex) {
							ex.printStackTrace();
							break; // file read error
						}
					}
					else {
						break;
					}
				}
				if (bytesRead > 0) {
					// convert byte array to set of double arrays, one per
					// channel
					newSamples = bytesRead / audioFormat.getFrameSize();
					doubleData = new double[nChannels][newSamples];
					int convertedSamples = byteConverter.bytesToDouble(byteArray, doubleData, bytesRead);

					ms = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples);
//					currentAnalysisTime = ms;
					currentAnalysisTime = ms + (long) (newSamples * 1000L / sampleRate); // get ms of last sample for this. 

					for (int ichan = 0; ichan < nChannels; ichan++) {

						newDataUnit = new RawDataUnit(ms, 1 << ichan, totalSamples, newSamples);
						newDataUnit.setRawData(doubleData[ichan]);

						if (1000*(readFileSamples/sampleRate)<fileInputParameters.skipStartFileTime) {
							// zero the data. Skipping it causes all the timing to screw up
							Arrays.fill(doubleData[ichan], 0.);
						}
						newDataUnits.addNewData(newDataUnit);

						// GetOutputDataBlock().addPamData(pamDataUnit);
					}
					long blockMillis = (int) ((newDataUnit.getStartSample() * 1000) / sampleRate);
					//					newDataUnit.timeMilliseconds = blockMillis;
					PamCalendar.setSoundFileTimeInMillis(blockMillis);
					long now = System.currentTimeMillis();
					if (fileSamples > 0 && totalSamples - lastProgressUpdate >= getSampleRate()*2 && now-lastProgressTime>1000) {
						int progress = (int) (1000 * readFileSamples / fileSamples);
						fileProgress.setValue(progress);
						sayEta();
						if (lastProgressTime > 0 && totalSamples > lastProgressUpdate) {
							double speed = (double) (totalSamples - lastProgressUpdate) /
									getSampleRate() / ((now-lastProgressTime)/1000.);
							if  (speedLabel!=null) speedLabel.setText(String.format(" (%3.1f X RT)", speed));
						}
						lastProgressTime = now;
						lastProgressUpdate = totalSamples;
					}
					/*
					 * this is the point we wait at for the other thread to
					 * get it's act together on a timer and use this data
					 * unit, then set it's reference to zero.
					 */
					while (newDataUnits.getQueueSize() > 3*nChannels) {
						if (!dontStop) break;
						try {
							Thread.sleep(1);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					totalSamples += newSamples;
					readFileSamples += newSamples;

				}
				else {
					break; // end of file
				}
				if (totalBytesRead == maxBytes) {
					break; // called at end of HARP chunk. 
				}
			}
			if (audioStream != null) {
				if (audioStream instanceof SudAudioInputStream) {
					acquisitionControl.getSUDNotificationManager().sudStreamClosed();
				}
				try {
					audioStream.close();
					audioStream = null;
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			// note the reason why the file has ended.
			if (!dontStop) { // stop button pressed
				setStreamStatus(STREAM_PAUSED);
			}
			else {  // file ended, since dont stop is true.
				setStreamStatus(STREAM_ENDED);
			}
			//acquisitionControl.getDaqProcess().acquisitionStopped();
			//			System.out.println("quit DAQ process thread");
			//			System.out.println(totalSamples + " samples read from audio file "
			//					+ fileName.getSelectedItem().toString());

		}
	}

	/**
	 * Open next file in a list for continuous processing, not the
	 * function that opens a file for normal processing.
	 * @param totalSamples
	 * @return
	 */
	protected boolean openNextFile(long totalSamples) {
		if (!fileInputParameters.repeatLoop) {
			return false;
		}
		// otherwise, open the same file again.
		boolean ok = prepareInputFile();
		System.out.println("Reopening same file in infinite loop " + ok);
		return ok;
	}

	/** Format one channel of the data in a byte array into a sample array.
	 */
	public static double[] bytesToSamples(byte[] byteArray, long nBytes, int channel,
			AudioFormat audioFormat)
	{
		int nSamples = (int)(nBytes / audioFormat.getFrameSize());
		double[] samples = new double[nSamples];

		int bytesPerSample = ((audioFormat.getSampleSizeInBits() + 7) / 8);
		int byteI = channel * bytesPerSample;
		for (int isamp = 0; isamp < nSamples; isamp++) {
			samples[isamp] =
					getSample(byteArray, byteI, bytesPerSample, audioFormat.isBigEndian());
			byteI += audioFormat.getFrameSize();
		}
		return samples;
	}

	/**
	 * Convenience method for getting samples from a byte array.
	 * Samples should be signed, integer, of either endian-ness, and
	 * 8, 16, 24, or 32 bits long.  Result is scaled to the range of [-1,1).
	 * Note that .wav files are little-endian and .aif files are big-endian.
	 */
	public static double getSample(byte[] buffer, int position, int bytesPerSample,
			boolean isBigEndian)
	{
		switch (bytesPerSample) {
		case 1: return buffer[position] / 256.0; //endian-ness doesn't matter here
		case 2: return (isBigEndian
				? (double)(short)(((buffer[position  ] & 0xff) << 8) | (buffer[position+1] & 0xff))
						: (double)(short)(((buffer[position+1] & 0xff) << 8) | (buffer[position  ] & 0xff)))
				/ 32768.0;
		case 3: return (isBigEndian
				? (double)(((buffer[position  ]) << 16) | ((buffer[position+1] & 0xff) << 8) | (buffer[position+2] & 0xff))
						: (double)(((buffer[position+2]) << 16) | ((buffer[position+1] & 0xff) << 8) | (buffer[position  ] & 0xff)))
				/ 8388608.0;
		case 4: return (isBigEndian
				? (double)(((buffer[position  ]) << 24) | ((buffer[position+1] & 0xff) << 16) | ((buffer[position+2] & 0xff) << 8) | (buffer[position+3] & 0xff))
						: (double)(((buffer[position+3]) << 24) | ((buffer[position+2] & 0xff) << 16) | ((buffer[position+1] & 0xff) << 8) | (buffer[position  ] & 0xff)))
				/ 2147483648.0;
		default: return 0.0;
		}
	}

	@Override
	public void daqHasEnded() {
		fileListComplete();
	}

	/**
	 * Called when all files to be processed have been processed.
	 */
	protected void fileListComplete() {
		if (GlobalArguments.getParam(PamController.AUTOEXIT) != null) {
			System.out.println("All sound files processed, PAMGuard can close on " + PamController.AUTOEXIT);
			PamController.getInstance().setPamStatus(PamController.PAM_COMPLETE);
			PamController.getInstance().batchProcessingComplete();
		}
	}

	JPanel statusPanel;
	@Override
	public Component getStatusBarComponent() {
		if (statusPanel == null) {
			statusPanel = new PamPanel();
			statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
			statusPanel.add(new PamLabel("File "));
			statusPanel.add(fileProgress);
			statusPanel.add(new PamLabel("  "));
			statusPanel.add(etaLabel = new PamLabel(" "));
			statusPanel.add(speedLabel = new PamLabel(" "));
			fileProgress.setMinimum(0);
			fileProgress.setMaximum(1000);
			fileProgress.setValue(0);
			etaLabel.setToolTipText("Estimated end time (Local time)");
			speedLabel.setToolTipText("Process speed (factor above real time)");
			fileProgress.setToolTipText("Progress through current file");
		}
		return statusPanel;
	}

	public void sayEta() {
		sayEta(getEta());
	}

	public long getEta() {
		double fileFraction = fileProgress.getValue() / 1000.;
		long now = System.currentTimeMillis();
		return (long) (fileStartTime + (now - fileStartTime) / fileFraction);
	}

	public void sayEta(long timeMs) {

		if (etaLabel==null) return;

		if (timeMs < 0) {
			etaLabel.setText(" ");
			return;
		}

		long now = System.currentTimeMillis();
		DateFormat df;
		String str;
		if (timeMs - now < (6 * 3600 * 1000)) {
//			df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
			df = new SimpleDateFormat("HH:mm:ss");
//			df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//			str = PamCalendar.formatLocalDateTime(timeMs)
		}
		else {
//			df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMs);
		TimeZone zone = c.getTimeZone();
		etaLabel.setText("End " + df.format(c.getTime()));
	}

	@Override
	public String getDeviceName() {
		if (getCurrentFile() == null) {
			return null;
		}
		else {
			return getCurrentFile().getAbsolutePath();
		}
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getSampleBits()
	 */
	@Override
	public int getSampleBits() {
		return fileInputParameters.bitDepth;
	}

	@Override
	public void fileDateChange(FileDate fileDate) {
		File currfile = getCurrentFile();
		if (currfile != null) {
			setNewFile(currfile.getAbsolutePath());
		}
	}

	/**
	 * Get the acquisition control for the input system
	 * @return the acquisition control.
	 */
	public AcquisitionControl getAquisitionControl() {
		return this.acquisitionControl;
	}

	@Override
	public void setSelected(boolean select) {
		super.setSelected(select);
		if (select) {
			getDialogPanel();
		}
	}

	/**
	 * @return the selectedFileTypes
	 */
	public List<SoundFileType> getSelectedFileTypes() {
		return selectedFileTypes;
	}

	/**
	 * Called when the file or file list selection is changes and finds a list of all
	 * sound file types included in the selection. this is only implemented for SUD files
	 * at the moment, the idea being to offer some additional functionality.
	 * @param selectedFileTypes the selectedFileTypes to set
	 */
	public void setSelectedFileTypes(List<SoundFileType> selectedFileTypes) {
		this.selectedFileTypes = selectedFileTypes;
		if (selectedFileTypes == null) {
			return;
		}
		for (SoundFileType aType : selectedFileTypes) {
			aType.selected(this);
		}
	}
	@Override
	public InputStoreInfo getStoreInfo(PamWorkMonitor workMonitor, boolean detail) {
//		System.out.println("FileInputSystem: Get store info start:");
		WavFileType currentFile = getCurrentFile();
		if (currentFile == null || currentFile.exists() == false) {
			return null;
		}
		WavFileType wavType = new WavFileType(currentFile);
		wavType.getAudioInfo();
		long firstFileStart = getFileStartTime(currentFile);
		float duration = wavType.getDurationInSeconds();
		long fileEnd = (long) (firstFileStart + duration*1000.);
		long[] allFileStarts = {firstFileStart};
		long[] allFileEnds = {fileEnd};
		InputStoreInfo storeInf = new InputStoreInfo(acquisitionControl, 1, firstFileStart, firstFileStart, fileEnd);
		storeInf.setFileStartTimes(allFileStarts);
		storeInf.setFileEndTimes(allFileEnds);
		return storeInf;
	}

	@Override
	public boolean setAnalysisStartTime(long startTime) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getBatchStatus() {
		// TODO Auto-generated method stub
		return null;
	}
}