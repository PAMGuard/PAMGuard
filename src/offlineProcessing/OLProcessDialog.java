package offlineProcessing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamUtils.TxtFileUtils;
import PamView.CancelObserver;
import PamView.DBTextArea;
import PamView.dialog.PamDialog;
import PamView.dialog.PamFileBrowser;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamPanel;
import PamView.panel.PamProgressBar;
import PamguardMVC.PamDataBlock;
import offlineProcessing.logging.OldTaskData;
import offlineProcessing.logging.TaskLogging;
import offlineProcessing.superdet.OfflineSuperDetFilter;

/**
 * Dialog for offline processing of a particular data type.<br>
 * Will offer the user choices in how to select data (e.g. loaded, or all)
 * and then scroll through those data passing one data unit at a time to a series
 * of tasks which will have been added by the programmer, but can be individually 
 * turned off and on by the user. Each task will have a check box to enable it and 
 * an optional button to configure it. 
 * Bottom part of the dialog shows a progress indicator.  
 * @author Douglas Gillespie
 *
 */
public class OLProcessDialog extends PamDialog {

	private OfflineTaskGroup taskGroup;

	private JComboBox<String> dataSelection;
	private JCheckBox[] taskCheckBox;
	private JButton[] settingsButton;
	private JLabel status, currFile;
	private JProgressBar globalProgress; // file by file progress 1: nFiles
	private JProgressBar loadedProgress; // progress throgh loaded data
	private JCheckBox deleteOldData;
	private JLabel dataInfo;
	private DBTextArea noteText;
	/**
	 * Pane which can be used to add extra controls for different 'dataSelection' types.  
	 */
	private PamPanel dateSelectionPanel;

	/**
	 * Text field to change start time of data to be analysed
	 */
	private JTextField startTime;
	
	/**
	 * Text field to change start time of data to be analysed
	 */
	private JTextField endTime;
	
	/**
	 * Specific panel for selecting start and end times. 
	 */
	private PamPanel selectDataPanel; 
	
	/**
	 * Specific panel for importing time chunks from .csv file. 
	 */
	private PamPanel timeChunkDataPanel;


	public static ImageIcon settings = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));

	TaskStatus currentStatus = TaskStatus.IDLE;

	/**
	 * Reference to the main panel
	 */
	private JPanel mainPanel;


	public OLProcessDialog(Window parentFrame, OfflineTaskGroup taskGroup, String title) {
		super(parentFrame, title, false);
		this.taskGroup = taskGroup;
		taskGroup.setTaskMonitor(new OLMonitor());

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel dataSelectPanel = new PamAlignmentPanel(BorderLayout.WEST);
		dataSelectPanel.setLayout(new BorderLayout());
		dataSelectPanel.setBorder(new TitledBorder("Data Options"));
		dataSelectPanel.add(BorderLayout.WEST, new JLabel("Data "));
		dataInfo = new JLabel(" ", SwingConstants.CENTER); // create this first to avoid null pointer exception
		dateSelectionPanel=new PamPanel(new BorderLayout()); //create extra panel which can be populated to allow for controls to select dates. 
		dataSelectPanel.add(BorderLayout.CENTER, dataSelection = new JComboBox());
		dataSelection.addActionListener(new DataSelectListener());
		dataSelection.addItem("Loaded Data");
		dataSelection.addItem("All Data");
		dataSelection.addItem("New Data");
		dataSelection.addItem("Select Data");
		dataSelection.addItem("Specify time chunks");
		//create a specific panel for select data
		selectDataPanel=createDateSelPanel(); 
		//create specific pane for time chunks 
		timeChunkDataPanel = createTimeChunkPanel(); 
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(BorderLayout.NORTH, dataInfo);
		//		dataSelection.setSelectedIndex(offlineClassifierParams.dataChoice);
		southPanel.add(BorderLayout.CENTER,dateSelectionPanel); 
		southPanel.add(BorderLayout.SOUTH, deleteOldData = new JCheckBox("Delete old database entries"));
		deleteOldData.setToolTipText("<html>" +
				"Delete old data entries in the corresponding database table<p>" +
				"(Binary file data will always be overwritten)</html>)");
		dataSelectPanel.add(BorderLayout.SOUTH, southPanel);
				

		JPanel tasksPanel = new PamAlignmentPanel(BorderLayout.WEST);
		tasksPanel.setLayout(new GridBagLayout());
		tasksPanel.setBorder(new TitledBorder("Tasks"));
		int nTasks = taskGroup.getNTasks();
		taskCheckBox = new JCheckBox[nTasks];
		settingsButton = new JButton[nTasks];
		OfflineTask aTask;		
		JButton aButton;
		GridBagConstraints c = new PamGridBagContraints();
		for (int i = 0; i < nTasks; i++) {
			c.gridx = 0;
			aTask = taskGroup.getTask(i);
			addComponent(tasksPanel, taskCheckBox[i] = new JCheckBox(aTask.getName()), c);
			taskCheckBox[i].addActionListener(new SelectionListener(aTask, taskCheckBox[i]));
			c.gridx++;
			if (aTask.hasSettings()) {
				addComponent(tasksPanel, settingsButton[i] = new JButton(settings), c);
				settingsButton[i].addActionListener(new SettingsListener(aTask));
			}
			c.gridy++;
		}
		
		JPanel notePanel = new JPanel(new BorderLayout());
		notePanel.setBorder(new TitledBorder("Notes"));
		noteText = new DBTextArea(2, 40, TaskLogging.TASK_NOTE_LENGTH);
		noteText.getComponent().setToolTipText("Notes to add to database record of complete tasks");
		notePanel.add(BorderLayout.CENTER, noteText.getComponent());

//		JPanel progressPanel = new JPanel(new GridBagLayout());
		JPanel progressPanel = new PamAlignmentPanel(BorderLayout.WEST);
		progressPanel.setLayout(new GridBagLayout());
		progressPanel.setBorder(new TitledBorder("Progress"));
		c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(progressPanel, status = new JLabel(" "), c);
		c.gridy++;
		addComponent(progressPanel, currFile = new JLabel(" "), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		addComponent(progressPanel, new JLabel("File ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(progressPanel, loadedProgress = new PamProgressBar(0, 100), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(progressPanel, new JLabel("All Data ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(progressPanel, globalProgress = new PamProgressBar(00, 100), c);

		mainPanel.add(dataSelectPanel);
		
		OfflineSuperDetFilter offlineSuperDetFilter = taskGroup.getSuperDetectionFilter();
		if (offlineSuperDetFilter != null) {
			mainPanel.add(offlineSuperDetFilter.getSwingComponent(this));
		}

		mainPanel.add(tasksPanel);
		mainPanel.add(notePanel);
		mainPanel.add(progressPanel);

		getOkButton().setText("Start");
		
		setCancelObserver(new CancelObserverOLDialog());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WinListener());

		setDialogComponent(mainPanel);

		setParams();

		enableControls();
		
		setResizable(true);

	}
	
	/**
	 * Get the main panel. This can be used to add additional controls if needed. 
	 * @return the main panel. 
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * Get the delete old data check box
	 * @return the delete data check box;
	 */
	public JCheckBox getDeleteOldDataBox() {
		return deleteOldData;
	}

	/**
	 * The data selection ComboBox. Use this for changing names. 
	 * @return- the data selection combo box; 
	 */
	public JComboBox<String> getDataSelBox() {
		return dataSelection;
	}

	private void setParams() {
		TaskGroupParams taskGroupParams = taskGroup.getTaskGroupParams();
		int nTasks = taskGroup.getNTasks();
		dataSelection.setSelectedIndex(taskGroupParams.dataChoice);
		deleteOldData.setSelected(taskGroupParams.deleteOld);
		OfflineTask aTask;
		for (int i = 0; i < nTasks; i++) {
			aTask = taskGroup.getTask(i);
			taskCheckBox[i].setSelected(taskGroupParams.getTaskSelection(i));
			
		}
		noteText.setText(taskGroupParams.taskNote);
		setTaskToolTips();
//		deleteOldData.setSelected(offlineClassifierParams.deleteOld);
	}
	

	
	class CancelObserverOLDialog implements CancelObserver {

		@Override
		public boolean cancelPressed() {
			if (currentStatus==TaskStatus.RUNNING) {	
			cancelButtonPressed(); 
			return false;
			}
			return true;
		}
		
	}

	@Override
	protected void okButtonPressed() {
		if (getParams() == false) {
			return;
		}
		if (taskGroup.runTasks()) {
			currentStatus = TaskStatus.RUNNING;
			getCancelButton().setText("Stop!");
		}
	}
	
	/**
//	 * Enable controls within the dialog with the input OfflineTask group being null.
	 */
	public void enableControls() {
		enableControls(null);
	}


	/**
	 * Enable controls in the dialog. 
	 * @param task - the task group in whihc enable controls has been called from
	 */
	public void enableControls(OfflineTask task) {
		boolean nr = currentStatus != TaskStatus.RUNNING;		
		int nTasks = taskGroup.getNTasks();
		OfflineTask aTask;
		int selectedTasks = 0;
		for (int i = 0; i < nTasks; i++) {
			aTask = taskGroup.getTask(i);
			taskCheckBox[i].setEnabled(aTask.canRun() && nr);
			if (aTask.canRun() == false) {
				taskCheckBox[i].setSelected(false);
			}
			if (settingsButton[i] != null) {
				settingsButton[i].setEnabled(nr);
			}
			if (taskCheckBox[i].isSelected()) {
				selectedTasks++;
			}
		}
		getOkButton().setEnabled(selectedTasks > 0 && nr);
	}

	@Override
	public void cancelButtonPressed() {
		if (currentStatus == TaskStatus.RUNNING) {
			taskGroup.killTasks();
			currentStatus=TaskStatus.INTERRUPTED;
			enableControls();
			getCancelButton().setText("Close");
			
		}
		else  {}
	}

	@Override
	public boolean getParams() {
		
		TaskGroupParams taskGroupParams = taskGroup.getTaskGroupParams();
		taskGroupParams.dataChoice = dataSelection.getSelectedIndex();
		int nTasks = taskGroup.getNTasks();
		
		OfflineTask aTask;
		for (int i = 0; i < nTasks; i++) {
			aTask = taskGroup.getTask(i);
			aTask.setDoRun(taskCheckBox[i].isSelected());
			taskGroupParams.setTaskSelection(i, taskCheckBox[i].isSelected());
		}
		taskGroupParams.deleteOld = deleteOldData.isSelected();
		
		//set start and end times. Maybe not have been changed in which otherwise will return zeros;
		
		
		if (taskGroupParams.dataChoice==TaskGroupParams.PROCESS_SPECIFICPERIOD){
			System.out.println("Start date " + this.readStartDate() + "   "  + startTime.getText() + "   End date "+ this.readEndDate());
			taskGroupParams.startRedoDataTime=this.readStartDate();
			if (taskGroupParams.startRedoDataTime<0 || taskGroupParams.endRedoDataTime>Long.MAX_VALUE){
				PamDialog.showWarning(super.getOwner(), "Start value invalid", "The start time is invalid");
				return false; 
			}
			taskGroupParams.endRedoDataTime=this.readEndDate();
			if (taskGroupParams.endRedoDataTime<0 || taskGroupParams.endRedoDataTime>Long.MAX_VALUE) {
				PamDialog.showWarning(super.getOwner(), "End value invalid", "The end time is invalid");
				return false; 
			}
			if (taskGroupParams.endRedoDataTime==taskGroupParams.startRedoDataTime){
				PamDialog.showWarning(super.getOwner(), "Error in start and end value", "The start time is the same as the end time");
				return false; 
			}
		}
		
		String note = noteText.getText();
		if (note == null || note.length() == 0) {
			return PamDialog.showWarning(super.getOwner(), "Task note", "you must enter a note about what you are doing");
		}
		taskGroupParams.taskNote = note;
		
		return true;
	}
	
	public void setTaskToolTips() {
		int nTasks = taskGroup.getNTasks();
		
		OfflineTask aTask;
		for (int i = 0; i < nTasks; i++) {
			aTask = taskGroup.getTask(i);
			OldTaskData taskData = TaskLogging.getTaskLogging().readLastTaskData(taskGroup, aTask);
			if (taskData == null) {
				taskCheckBox[i].setToolTipText("Task not run");
			}
			else {
				String tip = "<html>Last run: " + taskData.toString() ;
				tip = tip.replace("\n", "<br>");
				taskCheckBox[i].setToolTipText(tip);
			}
		}
		
	}

	public void newDataSelection() {
		int sel = dataSelection.getSelectedIndex();
		PamDataBlock primaryDataBlock = taskGroup.getPrimaryDataBlock();
		String selStr = null;
		dateSelectionPanel.removeAll();
		dateSelectionPanel.invalidate();
		switch (sel) {
		case TaskGroupParams.PROCESS_ALL:
			selStr = "Process all data";
			break;
		case TaskGroupParams.PROCESS_LOADED:
			long msPerDay = 3600L*24000L;
			if (primaryDataBlock != null ) {
			long startDay = primaryDataBlock.getCurrentViewDataEnd()/msPerDay;
			long endDay = primaryDataBlock.getCurrentViewDataEnd()/msPerDay;
			if (endDay == startDay) {
				selStr = String.format("%s to %s", PamCalendar.formatDateTime(primaryDataBlock.getCurrentViewDataStart()),
						PamCalendar.formatTime(primaryDataBlock.getCurrentViewDataEnd()));
			}
			else {
				selStr = String.format("%s to %s", PamCalendar.formatDateTime(primaryDataBlock.getCurrentViewDataStart()),
						PamCalendar.formatDateTime(primaryDataBlock.getCurrentViewDataEnd()));
			}
			}
			break;
		case TaskGroupParams.PROCESS_NEW:
			selStr = String.format("All data from %s", 
					PamCalendar.formatDateTime(taskGroup.getTaskGroupParams().lastDataTime));
			break;
			
		case TaskGroupParams.PROCESS_SPECIFICPERIOD:
//			selStr = String.format("All data from %s", 
//			PamCalendar.formatDateTime(taskGroup.getTaskGroupParams().lastDataTime));
			//say start data and end date. If zero (default) then say loaded data time so easier
			// for user to change date around current time. 
			if (taskGroup.getTaskGroupParams().startRedoDataTime<=0) sayStartDate(primaryDataBlock.getCurrentViewDataStart());
			else sayStartDate(taskGroup.getTaskGroupParams().startRedoDataTime); 

			if (taskGroup.getTaskGroupParams().endRedoDataTime<=0) sayEndDate(primaryDataBlock.getCurrentViewDataEnd());
			else sayEndDate(taskGroup.getTaskGroupParams().endRedoDataTime); 
			
			dateSelectionPanel.add(BorderLayout.CENTER, selectDataPanel); 
			break;
		case TaskGroupParams.PROCESS_TME_CHUNKS:
			if (taskGroup.getTaskGroupParams().timeChunks==null || taskGroup.getTaskGroupParams().timeChunks.size()==0) {
				selStr="<html> No time chunks have been imported. <p> No data to process. </html>"; 
			}
			else {
				selStr = String.format("<html>Data in %d specific time chunks between <p> %s to <p> %s</html>", 
						taskGroup.getTaskGroupParams().timeChunks.size(), 
						PamCalendar.formatDateTime(taskGroup.getTaskGroupParams().timeChunks.get(0)[0]), 				
						PamCalendar.formatDateTime(taskGroup.getTaskGroupParams().timeChunks.get(taskGroup.getTaskGroupParams().timeChunks.size()-1)[1])
						);
			}			
			dateSelectionPanel.add(BorderLayout.CENTER, timeChunkDataPanel); 
			break;
		}
		
		dataInfo.setText(selStr);
		dataSelection.setToolTipText(selStr);
		
		//revalidate and pack dialog to make sure no weird things happens to displayed components. 
		dateSelectionPanel.validate();
		this.pack();
	}
	
	/**
	 * Create a panel which allows users to select a start and end date. 
	 * @param startDate - the start date to set pane to 
	 * @param endData - the end date to set pane to 
	 * @return
	 */
	private PamPanel createDateSelPanel(){
		
		startTime= new JTextField(16); 
		endTime= new JTextField(16); 

		PamPanel panel=new PamPanel(new GridBagLayout());  
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx=0; 
		c.gridy=0; 
		c.anchor = GridBagConstraints.WEST;
		
		c.insets=new Insets(3,3,3,3);
		addComponent(panel, new JLabel("Start time "), c);
		c.gridx++; 	
		addComponent(panel, startTime, c);
		
		c.gridy++;
		c.gridx=0; 
		addComponent(panel, new JLabel("End time "), c);
		c.gridx++; 	
		addComponent(panel, endTime, c);

		return panel;
	}
	
	
	/**
	 * Create a panel to import time chunks
	 * @return panel to import time chunks. 
	 */
	private PamPanel createTimeChunkPanel(){

		PamPanel panel=new PamPanel(new GridBagLayout());  
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx=0; 
		c.gridy=0; 
		c.anchor = GridBagConstraints.WEST;

		addComponent(panel, new JLabel("Import Time Chunks"), c);
		c.gridx++; 	
		JButton browseButton =  new JButton("Browse...");
		browseButton.setToolTipText("Select a .csv file which contains two columns, a start time and an end time in EXCEL datenumber format.");
		addComponent(panel, browseButton, c);
		browseButton.addActionListener(new Browse());

		return panel;

	}
	

	/**
	 * Opens a browser to select a .csv file with time chunks. 
	 * @author Jamie Macaulay
	 *
	 */
	private class Browse implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {

			String dir = System.getProperty("user.dir");
			String newFile=PamFileBrowser.fileBrowser(OLProcessDialog.this,dir,PamFileBrowser.OPEN_FILE, new String[] {".csv"});

			if (newFile==null) {

				return;
			}
			//set the combo box to display the data
			loadTimeChunks(newFile); 

		}

	}
	
	
	/**
	 * Load time chunks from a .csv file. 
	 * @param filePath
	 */
	private void loadTimeChunks(String filePath) {
		try {
			//System.out.println("");
			ArrayList<ArrayList<Double>> timeChunkImport = TxtFileUtils.importCSVData(filePath);

			if (timeChunkImport==null || timeChunkImport.size()==0) {
				PamDialog.showWarning(this, "Import failed", "Could not import the time chunks. There was no data loaded. "); 
				return; 
			}

			ArrayList<long[]> timeChunks= new ArrayList<long[]>(); 
			//convert to correct format.
			boolean lineFailure = false; 
			for (int i=0; i<timeChunkImport.size(); i++) {
				System.out.println("timeChunkImport: "+ timeChunkImport.size() + " " + timeChunkImport.get(i));
				if (timeChunkImport.get(i)!=null && timeChunkImport.get(i).size()>=2) {
					timeChunks.add(new long[]{PamCalendar.excelSerialtoMillis(timeChunkImport.get(i).get(0)), 
							PamCalendar.excelSerialtoMillis(timeChunkImport.get(i).get(1))}); 
					System.out.println("Time: "+ PamCalendar.formatDateTime(PamCalendar.excelSerialtoMillis(timeChunkImport.get(i).get(0))));
				}
				else {
					lineFailure=true;
				}
			}
			
			if (lineFailure) {
				PamDialog.showWarning(this, "Import failed", 
						"<html/>Could not import one or all of the time chunks. There was a problem with the format. The format is N rows, "
						+ "<p>each with two columns. The first column value is the start time of a time chunk in excel datenum "
						+ "<p>format and the second column is the end time in excel datenum format. No  data was loaded </html>"); 
				return;
			}

			//if we've got this far have successfully loaded file
			this.taskGroup.getTaskGroupParams().timeChunks=timeChunks;
			newDataSelection();

		}
		catch (Exception e) {
			e.printStackTrace();
			PamDialog.showWarning(this, "Import failed", "Could not import the time chunks. There was no data loaded. "); 
		}
	}
	
	/**
	 * Write the current start date into the dialog. 
	 */
	private void sayStartDate(long startDate) {
		startTime.setText(PamCalendar.formatDBDateTime(startDate));
	}
	/**
	 * 
	 * @return the current date as read from the dialog. 
	 */
	private long readStartDate() {
		long t = PamCalendar.msFromDateString(startTime.getText());
		return t;
	}
	
	/**
	 * Write the current start date into the dialog. 
	 */
	private void sayEndDate(long endDate) {
		endTime.setText(PamCalendar.formatDBDateTime(endDate));
	}
	/**
	 * 
	 * @return the current date as read from the dialog. 
	 */
	private long readEndDate() {
		long t = PamCalendar.msFromDateString(endTime.getText());
		return t;
	}
	
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	class WinListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent arg0) {
			if (currentStatus == TaskStatus.RUNNING) {
				return;
			}
			setVisible(false);
		}

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setParams();
			enableControls();
		}
	}

	class DataSelectListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			newDataSelection();
		}
	}
	/**
	 * Listener for selecting / deselecting individual tasks. 
	 * @author Doug Gillespie
	 *
	 */
	class SelectionListener implements ActionListener {

		private OfflineTask offlineTask;
		
		private JCheckBox checkBox;

		public SelectionListener(OfflineTask offlineTask, JCheckBox checkBox) {
			this.offlineTask = offlineTask;
			this.checkBox = checkBox;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			offlineTask.setDoRun(checkBox.isSelected());
			enableControls(offlineTask);
		}

	}

	/**
	 * Listener for settings buttons
	 * @author Doug Gillespie
	 *
	 */
	class SettingsListener implements ActionListener {

		private OfflineTask offlineTask;

		public SettingsListener(OfflineTask offlineTask) {
			this.offlineTask = offlineTask;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			offlineTask.callSettings();
			enableControls(); 
		}

	}

	/**
	 * Monitor for AWT calls back from the thing running the tasks
	 * which will be running in a separate thread. 
	 * @author Doug Gillespie
	 *
	 */
	class OLMonitor implements TaskMonitor {

		@Override
		public void setTaskStatus(TaskMonitorData taskMonitorData) {
			status.setText(taskMonitorData.taskStatus.toString() + ", " + taskMonitorData.taskActivity.toString());
			if (taskMonitorData.fileOrStatus == null || taskMonitorData.fileOrStatus.length() == 0) {
				currFile.setText("  ");
			}
			else {
				currFile.setText(taskMonitorData.fileOrStatus);
			}
			switch (taskMonitorData.taskActivity) {
			case LINKING:
			case LOADING:
//				globalProgress.setMaximum(taskMonitorData.progMaximum);
				globalProgress.setValue(taskMonitorData.progValue*100/taskMonitorData.progMaximum);
				loadedProgress.setIndeterminate(true);
				break;
			case PROCESSING:
				int prog = taskMonitorData.progValue*100/taskMonitorData.progMaximum;
//				System.out.println("Set loaded progress to " + prog);
				loadedProgress.setIndeterminate(false);
				loadedProgress.setValue(prog);
				break;
			case IDLE:
//				globalProgress.setValue(100);
				loadedProgress.setIndeterminate(false);
				break;
			case SAVING:
			default:
				break;
			}
			switch (taskMonitorData.taskStatus) {
			case COMPLETE:
				globalProgress.setValue(100);
				loadedProgress.setValue(100);
				break;
			case CRASHED:
				break;
			case IDLE:
				break;
			case INTERRUPTED:
				break;
			case RUNNING:
				break;
			case STARTING:
				globalProgress.setValue(0);
				loadedProgress.setValue(0);
				break;
			default:
				break;
			
			}
			setStatus(taskMonitorData.taskStatus);
		}

//		int doneFiles = 0;
//
//		int numFiles = 0;
//
//		@Override
//		public void setFileName(String fileName) {
//			//			currFile.setText(fileName);
//			if (taskGroup.getTaskGroupParams().dataChoice == TaskGroupParams.PROCESS_LOADED) {
//				currFile.setText("Loaded data");
//			}
//			currFile.setText(String.format("File %d of %d", doneFiles, numFiles));
//		}
//
//		@Override
//		public void setNumFiles(int nFiles) {
//			globalProgress.setMaximum(numFiles = nFiles);
//		}
//
//		@Override
//		public void setProgress(int global, double loaded) {
//			doneFiles = global;
//			globalProgress.setValue(global);
//			fileProgress.setValue((int) (loaded*100));
//		}
//
//		@Override
		public void setStatus(TaskStatus taskStatus) {
//			status.setText(TaskMonitorData.getStatusString(taskStatus));
			currentStatus=taskStatus;
			enableControls();
			switch(taskStatus) {
			case COMPLETE:
			case CRASHED:
			case IDLE:
			case INTERRUPTED:
				getCancelButton().setText("Close");
				setTaskToolTips();
				break;
			case RUNNING:
			case STARTING:
				getCancelButton().setText("Stop!");
				break;
			default:
				break;
			
			}
		}
	}

	/**
	 * Get the current status of the dialog. 
	 * @return the current status. 
	 */
	public TaskStatus getCurrentStatus() {
		return currentStatus;
	}
	
	/**
	 * Get the current list of settings buttons which will mirror the current 
	 * offline tasks available in the dialog. 
	 * @return list of settings buttons. 
	 */
	public JButton[] getSettingsButtons() {
		return settingsButton;
	}
	
	/**
	 * Get list of check boxes for the offline tasks
	 * @return a list of check boxes. 
	 */
	public JCheckBox[] getTaskCheckBoxs() {
		return taskCheckBox;
	}
	

	/**
	 * Get the task group for the dialog. 
	 * @return the taks group. 
	 */
	public OfflineTaskGroup getTaskGroup() {
		return this.taskGroup;
	}

}
