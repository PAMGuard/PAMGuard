package Acquisition;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import Acquisition.pamAudio.PamAudioFileFilter;
import PamView.dialog.PamDialog;

public class CheckWavFileHeaders extends PamDialog {

	private static CheckWavFileHeaders singleInstance;

	private FolderInputSystem folderInputSystem;

	private JTextArea textArea;

	//	private JButton check, checkRepair;

	private JProgressBar progressBar;

	private int nFiles, nErrors, doneFiles;

	private boolean subFolders;

	private File folder;

	private JLabel folderName;

	private boolean running, ran;

	private CheckFiles checkFilesWorker;

	private ArrayList<File> allFiles = new ArrayList<>();

	private CheckWavFileHeaders(Window parentFrame) {
		super(parentFrame, "Check File Headers", false);
		JPanel top = new JPanel(new BorderLayout());
		top.setBorder(new TitledBorder(""));
		top.add(BorderLayout.NORTH, folderName = new JLabel(" "));
		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(322, 300));
		top.add(BorderLayout.CENTER, scrollPane);
		top.add(BorderLayout.SOUTH, progressBar = new JProgressBar());
		setDialogComponent(top);

		getOkButton().setText("Check and Repair");
		getCancelButton().setText("Close");
	}

	public static void showDialog(Window parentWin, FolderInputSystem folderInputSystem) {
		if (singleInstance == null || singleInstance.getOwner() != parentWin) {
			singleInstance = new CheckWavFileHeaders(parentWin);
		}
		singleInstance.folderInputSystem = folderInputSystem;
		singleInstance.setParams();
		singleInstance.setVisible(true);
	}

	private void setParams() {
		running = ran = false;
		subFolders = folderInputSystem.getFolderInputParameters().subFolders;
		if (subFolders) {
			folderName.setText(folderInputSystem.getCurrentFolder() + " + sub folders");
		}
		else {
			folderName.setText(folderInputSystem.getCurrentFolder());
		}
		folder = new File(folderInputSystem.getCurrentFolder());
		textArea.setText(" ");
		allFiles.clear();
		nFiles = countFiles(folder);
		progressBar.setValue(0);
		progressBar.setMaximum(Math.max(nFiles, 1));
		enableControls();
	}

	private void enableControls() {
		getOkButton().setEnabled(nFiles > 0 & !running && !ran);
		getCancelButton().setEnabled(!running);
	}

	private int countFiles(File folder) {
		int nF = 0;
		File[] files = folder.listFiles(new PamAudioFileFilter());
		if (files == null) return 0;
		File file;
		for (File file2 : files) {
			file = file2;
			if (file.isDirectory() && subFolders) {
				System.out.println(file.getAbsoluteFile());
				nF += countFiles(file.getAbsoluteFile());
			}
			else if (file.isFile()) {
				allFiles.add(file);
				nF++;
			}
		}


		return nF;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		checkFiles();
		return false;
	}

	private void checkFiles() {
		running = true;
		textArea.setText(String.format("Checking %d files ...\n", nFiles));
		doneFiles = 0;
		nErrors = 0;
		enableControls();
		checkFilesWorker = new CheckFiles();
		checkFilesWorker.execute();
	}

	private void jobDone() {
		running = false;
		ran = true;
		enableControls();
		textArea.append(String.format("\n\n%d file headers contained errors", nErrors));
	}

	private void setProgressInfo(ProgressData progressData) {
		progressBar.setValue(++doneFiles);
		String msg = AudioFileFuncs.getMessage(progressData.headerError);
		textArea.append(String.format("\n%s - %s", progressData.file.getName(), msg));
		boolean problem = progressData.headerError != AudioFileFuncs.FILE_OK;
		if (problem) {
			nErrors++;
		}
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
	}

	/**
	 * Return true if there is an error
	 * @param aFile file to check, aif or wav
	 * @return true if there is an error
	 */
	private int checkFile(File aFile) {
		if (!aFile.exists() || aFile.isDirectory()) {
			return AudioFileFuncs.FILE_DOESNTEXIST;
		}
		String fileName = aFile.getName();
		// get the bit after the dot
		int dotPos = fileName.lastIndexOf('.');
		if (dotPos < 0) {
			return AudioFileFuncs.FILE_UNKNOWNTYPE;
		}
		String fileEnd = fileName.substring(dotPos+1);
		if (fileEnd.equalsIgnoreCase("wav")) {
			return checkWavFile(aFile);
		}
		else if (fileEnd.equalsIgnoreCase("aif")) {
			return checkAifFile(aFile);
		}
		return AudioFileFuncs.FILE_UNKNOWNTYPE;
	}

	private int checkAifFile(File file) {
		// TODO Auto-generated method stub
		return AudioFileFuncs.FILE_CANTOPEN;
	}

	private int checkWavFile(File aFile) {
		return WavFileFuncs.checkHeader(aFile, true);
	}

	private class CheckFiles extends SwingWorker<Integer, ProgressData> {

		@Override
		protected Integer doInBackground() throws Exception {
			/*
			 *  need to loop over files again
			 *  for each file, report on progress with it's name and
			 *  whether or not it had an error
			 */
			int error;
			File aFile;
			for (File file : allFiles) {
				error = checkFile(aFile = file);
				publish(new ProgressData(aFile, error));
			}
			return null;
		}



		@Override
		protected void done() {
			super.done();
			jobDone();
		}

		@Override
		protected void process(List<ProgressData> progressData) {
			for (ProgressData element : progressData) {
				setProgressInfo(element);
			}
		}

	}

	private class ProgressData {
		File file;
		int headerError;
		public ProgressData(File file, int headerError) {
			super();
			this.file = file;
			this.headerError = headerError;
		}
	}

}
