package whistleClassifier.training;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import pamMaths.PamMatrix;
import whistleClassifier.FragmentClassifierParams;
import PamController.PamguardVersionInfo;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.debug.Debug;

public class BatchTrainingDialog extends PamDialog implements BatchTrainingMonitor {

	ClassifierTrainingDialog trainingDialog;

	BatchTrainingParams batchTrainingParams = new BatchTrainingParams();

	JTextField[] fragmentLength = new JTextField[3];
	JTextField[] sectionLength = new JTextField[3];
	JTextField[] minProbability = new JTextField[3];
	String[] rowHeadings = {"Fragment Length ", "Section Length ", "Min' Probability "};
	String[] colHeadings = {"Min", "Step", "Max"};

	JProgressBar progressBar;
	JLabel progressLabel, timeRemaining;

	private FragmentClassifierParams fragmentClassifierParams;

	private BatchTrainingWorker batchTrainingWorker;

	public BatchTrainingDialog(Window parentFrame, ClassifierTrainingDialog trainingDialog, 
			FragmentClassifierParams fragmentClassifierParams) {
		super(parentFrame, "Batch Classifier Training", false);
		this.trainingDialog = trainingDialog;
		this.fragmentClassifierParams = fragmentClassifierParams;

		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Parameter ranges"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 1;
		for (int i = 0; i < 3; i++) {
			addComponent(p, new JLabel(colHeadings[i], SwingConstants.CENTER), c);
			c.gridx++;
		}
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel(rowHeadings[0], SwingConstants.RIGHT), c);
		c.gridx++;
		for (int i = 0; i < 3; i++) {
			addComponent(p, fragmentLength[i] = new JTextField(5), c);
			c.gridx++;
		}
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel(rowHeadings[1], SwingConstants.RIGHT), c);
		c.gridx++;
		for (int i = 0; i < 3; i++) {
			addComponent(p, sectionLength[i] = new JTextField(5), c);
			c.gridx++;
		}
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel(rowHeadings[2], SwingConstants.RIGHT), c);
		c.gridx++;
		for (int i = 0; i < 3; i++) {
			addComponent(p, minProbability[i] = new JTextField(5), c);
			c.gridx++;
		}

		JPanel q = new JPanel(new BorderLayout());
		q.setBorder(new TitledBorder("Progres"));
		q.add(BorderLayout.NORTH, progressLabel = new JLabel("   "));
		q.add(BorderLayout.CENTER, progressBar = new JProgressBar());
		q.add(BorderLayout.SOUTH, timeRemaining = new JLabel("   "));

		mainPanel.add(BorderLayout.CENTER, p);
		mainPanel.add(BorderLayout.SOUTH, q);
		setDialogComponent(mainPanel);

		getOkButton().setText("Start");
		setParams();
	}

	@Override
	public synchronized void cancelButtonPressed() {
		if (batchTrainingWorker != null) {
			batchTrainingWorker.stopNow();
		}
	}

	public void setParams() {
		fragmentLength[0].setText(String.format("%d", fragmentClassifierParams.getFragmentLength()));
		fragmentLength[1].setText(String.format("%d", 0));
		fragmentLength[2].setText(String.format("%d", fragmentClassifierParams.getFragmentLength()));
		sectionLength[0].setText(String.format("%d", fragmentClassifierParams.getSectionLength()));
		sectionLength[1].setText(String.format("%d", 0));
		sectionLength[2].setText(String.format("%d", fragmentClassifierParams.getSectionLength()));
		minProbability[0].setText(String.format("%3.3f", fragmentClassifierParams.getMinimumProbability()));
		minProbability[1].setText(String.format("%3.3f", 0.));
		minProbability[2].setText(String.format("%3.3f", fragmentClassifierParams.getMinimumProbability()));
		setCancelButton("Cancel");
	}

	@Override
	public boolean getParams() {
		try {
			for (int i = 0; i < 3; i++) {
				batchTrainingParams.fragmentLength[i] = Integer.valueOf(fragmentLength[i].getText());
				batchTrainingParams.sectionLength[i] = Integer.valueOf(sectionLength[i].getText());
				batchTrainingParams.minProbability[i] = Double.valueOf(minProbability[i].getText());
			}
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter value");
		}
		if (checkGroup(batchTrainingParams.fragmentLength) == false) {
			return showWarning("Invalid fragment length parameters");
		}
		if (checkGroup(batchTrainingParams.sectionLength) == false) {
			return showWarning("Invalid section length parameters");
		}
		if (checkGroup(batchTrainingParams.minProbability) == false) {
			return showWarning("Invalid minimum probability parameters");
		}
		if (batchTrainingParams.fragmentLength[0] < 3) {
			return showWarning("The Minimum Fragment Length must be at least 3 FFT bins");
		}
		if (batchTrainingParams.sectionLength[0] < 1) {
			return showWarning("The Minimum Section Length must be > 0");
		}
		if (batchTrainingParams.minProbability[0] < 0. || batchTrainingParams.minProbability[2] > 1.) {
			return showWarning("The Probability range must be between 0.0 and 1.0");
		}

		startBatch();
		return false;
	}

	private boolean checkGroup(int[] vals) {
		if (vals[0] != vals[2] && vals[1] <= 0) {
			return false;
		}
		return true;
	}
	private boolean checkGroup(double[] vals) {
		if (vals[0] != vals[2] && vals[1] <= 0.0) {
			return false;
		}
		return true;
	}

	private void startBatch() {
		batchTrainingWorker = new BatchTrainingWorker(trainingDialog, batchTrainingParams, this);
		enableControls(true);
		setCancelButton("Cancel");
		batchTrainingWorker.execute();
	}

	@Override
	public void restoreDefaultSettings() {

	}

	@Override
	synchronized public void doneTraining(List<BatchResultSet> batchResults) {
		processResults(batchResults);
		batchTrainingWorker = null;
		enableControls(false);
		setCancelButton("Close");
	}
	
	private void setCancelButton(String name) {
		getCancelButton().setText(name);
	}

	private void processResults(List<BatchResultSet> batchResults) {
		/*
		 * Sort all the data in the BatchResultSets ...
		 */
		Debug.out.println("Sorting results");
		Collections.sort(batchResults);
		
		StringBuilder sb = new StringBuilder();
		int nRes = batchResults.size();
//		String[] species = fragmentClassifierParams.getSpeciesList();
		/*
		 * Get species from training data - the above is for classified data and will 
		 * not yet have the spp list. 
		 */
		String[] species = trainingDialog.getTrainingDataCollection().getSpeciesList();
		String[] speciesPlus = Arrays.copyOf(species, species.length+1);
		speciesPlus[species.length] = "??";
		/**
		 * First some general information about the run.
		 */
		long now = System.currentTimeMillis();
		sb.append("Whistle Classifier batch training output\r\n");
		sb.append("File Version 1.0\r\n");
		sb.append(PamCalendar.formatDateTime(now) + "\r\n");
		sb.append("Pamguard Version " + PamguardVersionInfo.getReleaseType() + ", " + 
				PamguardVersionInfo.version + "\r\n");
		sb.append("Classifier type: " + fragmentClassifierParams.getClassifierType() + "\r\n");
		sb.append("FFT Length: " + fragmentClassifierParams.getFftLength() + "\r\n");
		sb.append("FFT Hop: " + fragmentClassifierParams.getFftHop() + "\r\n");
		sb.append("Sample Rate: " + (int)fragmentClassifierParams.getSampleRate() + "\r\n");
		sb.append("Minimum Contour Length: " + fragmentClassifierParams.getMinimumContourLength() + "\r\n");
		double f[] = fragmentClassifierParams.getFrequencyRange();
		sb.append("Minimum Frequency: " + f[0] + "\r\n");
		sb.append("Maximum Frequency: " + f[1] + "\r\n");
		sb.append(String.format("Number of parameter sets = %d\r\n", nRes));
		sb.append(String.format("Number of bootstaps per set = %d\r\n", fragmentClassifierParams.getNBootstrap()));
		sb.append(String.format("Number of species = %d\r\n", species.length));
		sb.append(String.format("Results ordered so best is first\r\n"));
		
		BatchResultSet brs;
		ListIterator<BatchResultSet> resIterator = batchResults.listIterator(nRes);
		int iSet = 0;
		while (resIterator.hasPrevious()) {
			sb.append(String.format("Result set %d\r\n", ++iSet));
			
			brs = resIterator.previous();
			sb.append(brs.toString());
			sb.append("\r\n");
			sb.append("Mean Confusion Matrix\r\n");
			sb.append(PamMatrix.matrixToString(brs.meanConfusion, "%6.4f", ",\t", 
					speciesPlus, species, true));
			sb.append("STD Confusion Matrix\r\n");
			sb.append(PamMatrix.matrixToString(brs.stdConfusion, "%6.4f", ",\t", 
					speciesPlus, species, true));
		}
		Debug.out.println(sb.toString());
		
		/*
		 * Now also give option to write that to a text file ...
		 */
		String fileName = PamCalendar.createFileName(now, "WhistleClassifierBatchOutput_", ".txt");
		Debug.out.println("Dump to file " + fileName);
		PamFileFilter fileFilter = new PamFileFilter("Text Files", ".txt");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setSelectedFile(new File(fileName));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showSaveDialog(this);
		boolean writeOk = false;
		if (state == JFileChooser.APPROVE_OPTION) {
			File currFile = fileChooser.getSelectedFile();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(currFile));
				writer.append(sb.toString());
				writer.close();
				writeOk = true;		
				if (writeOk) {
					// open the text file with the default program. 
					Desktop.getDesktop().open(currFile);
				}		
			} catch (IOException e) {
				System.out.println("Unable to dump text file: " + currFile.getAbsolutePath());
			}
		}
			
		
	}


	private void enableControls(boolean running) {
		getOkButton().setEnabled(running == false);
	}

	@Override
	public void setBatchTrainingProgress(int totalRuns, BatchTrainingProgress btp) {
		if (btp == null) {
			progressBar.setMaximum(totalRuns);
			progressBar.setValue(0);
			progressLabel.setText(String.format("Starting bootstrap 1 of %d", totalRuns));
			timeRemaining.setText(" ");
			return;
		}
		switch(btp.type) {
		case BatchTrainingProgress.NBOOTS:
			if (btp.isEnd){
				progressBar.setValue(btp.nRun+1);
				progressLabel.setText(String.format("Completed bootstrap %d of %d", btp.nRun+1, totalRuns));
			}
			break;
		case BatchTrainingProgress.ETA:
			timeRemaining.setText(String.format("Time remaining = %s", PamCalendar.formatDuration(btp.etaMillis)));
			break;
		case BatchTrainingProgress.MESSAGE:
//			System.out.println("Batch training: " + btp.message);
			break;
		}
	}

	/**
	 * Gets status information back from the boostrap monitor in the main training dialog. 
	 * @param statusMessage
	 * @param statusValue
	 */
	public void setStatus(ClassifierTrainingProgress trainingProgress) {
		if (batchTrainingWorker != null) {
			batchTrainingWorker.setStatus(trainingProgress);
		}
	}


}
