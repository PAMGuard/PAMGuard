package clickDetector.offlineFuncs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import clickDetector.ClickControl;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class DatabaseCheckDialog extends PamDialog {


	private static final long serialVersionUID = 1L;
	private static DatabaseCheckDialog singleInstance;
	private ClickControl clickControl;
	private JTextArea textOutput;
	private JRadioButton check, checkAndFix;
	private JTextField taskText;
	private JProgressBar taskProgress;
	private JButton okButton, cancelButton;
	private volatile boolean stopChecks;
	private volatile boolean isRunning;
	
	private DatabaseCheckDialog(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, clickControl.getUnitName() + " database checks", false);
		this.clickControl = clickControl;
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel cPanel = new JPanel();
		cPanel.setBorder(new TitledBorder("Database Tasks"));
		cPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(cPanel, check = new JRadioButton("Check only"), c);
		c.gridx++;
		addComponent(cPanel, checkAndFix = new JRadioButton("Check and fix"), c);
		ButtonGroup bg = new ButtonGroup();
		bg.add(check);
		bg.add(checkAndFix);
		check.setSelected(true);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		addComponent(cPanel, taskText = new JTextField(100), c);
		taskText.setEditable(false);
		c.gridy++;
		addComponent(cPanel, taskProgress = new JProgressBar(), c);
		taskProgress.setMaximum(100);
		JPanel c2Panel = new JPanel(new BorderLayout());
		c2Panel.add(BorderLayout.CENTER, cPanel);
		mainPanel.add(BorderLayout.NORTH, c2Panel);
		
		
		textOutput = new JTextArea(20, 50);
		textOutput.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textOutput);
		scrollPane.setPreferredSize(new Dimension(450, 270));
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		
		okButton = getOkButton();
		cancelButton = getCancelButton();
		
		okButton.setText("Start");
		cancelButton.setText("Close");
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setDialogComponent(mainPanel);
		
		setResizable(true);
	}

	public static boolean showDialog(Window parentFrame, ClickControl clickControl) {
		if (singleInstance == null || singleInstance.clickControl != clickControl || singleInstance.getOwner() != parentFrame) {
			singleInstance = new DatabaseCheckDialog(parentFrame, clickControl);
		}
		singleInstance.enableControls(false);
		singleInstance.isRunning = false;
		singleInstance.setVisible(true);
		
		return true;
	}
	
	@Override
	public void cancelButtonPressed() {
		if (isRunning) {
			stopChecks = true;
		}
		else {
			setVisible(false);
		}
	}

	@Override
	public boolean getParams() {
		CheckWorker cw = new CheckWorker();
		cw.execute();
		
		return false; // don't close !
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	class CheckWorker extends SwingWorker<Integer, CheckProgress> implements DatabaseCheckObserver
	{

		private DatabaseChecks databseChecks;

		public CheckWorker() {
			databseChecks = new DatabaseChecks(clickControl, this);
			stopChecks = false;
			enableControls(true);
			isRunning = true;
		}
		
		@Override
		protected Integer doInBackground() {
			try {
				databseChecks.runChecks(checkAndFix.isSelected());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void checkProgress(String text, int totalTasks, int taskNumber, int percent) {
			publish(new CheckProgress(text, totalTasks, taskNumber, percent));
		}

		@Override
		public void checkOutputText(String text, int warnLevel) {
			publish(new CheckProgress(text, warnLevel));
		}

		@Override
		public boolean stopChecks() {
			return stopChecks;
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			super.done();
			enableControls(false);
			isRunning = false;
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<CheckProgress> chunks) {
			for (CheckProgress cp:chunks) {
				sayProgress(cp);
			}
		}
		
	}
	class CheckProgress {
		String message;
		int percent;
		int totalTasks;
		int taskNumber;
		
		String outputMessage;
		int warningLevel;
		public CheckProgress(String message, int totalTasks, int taskNumber, int percent) {
			super();
			this.message = message;
			this.percent = percent;
			this.totalTasks = totalTasks;
			this.taskNumber = taskNumber;
		}
		public CheckProgress(String outputMessage, int warningLevel) {
			super();
			this.outputMessage = outputMessage;
			this.warningLevel = warningLevel;
		}
	}
	public void enableControls(boolean isActive) {
		okButton.setEnabled(!isActive);
		cancelButton.setText(isActive ? "Stop" : "Close");
		check.setEnabled(!isActive);
		checkAndFix.setEnabled(!isActive);
	}

	public void sayProgress(CheckProgress cp) {
		if (cp.message != null) {
			taskText.setText(cp.message);
			taskProgress.setValue(cp.percent);
		}
		else {
			textOutput.append(cp.outputMessage);
			textOutput.append("\n");
		}
	}

}
