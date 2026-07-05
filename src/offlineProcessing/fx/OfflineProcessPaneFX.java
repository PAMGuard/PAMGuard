package offlineProcessing.fx;

import java.util.ArrayList;
import java.util.List;

import PamController.PamConfiguration;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import offlineProcessing.OfflineTask;
import offlineProcessing.OfflineTaskGroup;
import offlineProcessing.TaskActivity;
import offlineProcessing.TaskGroupParams;
import offlineProcessing.TaskMonitor;
import offlineProcessing.TaskMonitorData;
import offlineProcessing.TaskStatus;

/**
 * A JavaFX pane for offline reprocessing that shows all available offline
 * tasks from all modules in a single unified pane. Tasks are displayed
 * grouped by their existing {@link OfflineTaskGroup} so that the existing
 * processing infrastructure is reused directly.
 * <p>
 * The user can select tasks across multiple task groups. Groups that have
 * at least one selected task will be run sequentially when the user presses
 * Start.
 * <p>
 * Can be used either as a standalone dialog (via {@link #showDialog(Window)})
 * or as an embeddable pane (via {@link #createContentPane()}) for use inside
 * a hiding pane or similar container.
 * 
 * @author Jamie Macaulay
 */
public class OfflineProcessPaneFX {

	/**
	 * The stage for dialog mode (null when used as embedded pane).
	 */
	private Stage stage;

	/**
	 * Container for the task group panels.
	 */
	private VBox taskGroupContainer;

	/**
	 * Data selection combo box (Loaded Data, All Data, New Data, Select Data).
	 */
	private ComboBox<String> dataSelectionCombo;

	/**
	 * Delete old data check box.
	 */
	private CheckBox deleteOldDataCheck;

	/**
	 * Notes text area.
	 */
	private TextArea notesArea;

	/**
	 * Start time text field for specific time period selection.
	 */
	private TextField startTimeField;

	/**
	 * End time text field for specific time period selection.
	 */
	private TextField endTimeField;

	/**
	 * Container for date selection controls (shown/hidden based on combo
	 * selection).
	 */
	private VBox dateSelectionPane;

	/**
	 * Status label.
	 */
	private Label statusLabel;

	/**
	 * Current file / info label.
	 */
	private Label currentFileLabel;

	/**
	 * Progress bar for progress through loaded data.
	 */
	private ProgressBar loadedProgress;

	/**
	 * Progress bar for overall / global progress.
	 */
	private ProgressBar globalProgress;

	/**
	 * Start button.
	 */
	private Button startButton;

	/**
	 * Close / stop button.
	 */
	private Button closeButton;

	/**
	 * List of task group UI entries, each wrapping an existing OfflineTaskGroup.
	 */
	private List<TaskGroupEntry> taskGroupEntries = new ArrayList<>();

	/**
	 * Current processing status.
	 */
	private volatile TaskStatus currentStatus = TaskStatus.IDLE;

	/**
	 * The task group currently being processed (if any).
	 */
	private OfflineTaskGroup runningTaskGroup;

	/**
	 * Index of the currently running group in the sequential processing queue.
	 */
	private int currentRunIndex = 0;

	/**
	 * Optional callback invoked when the user closes or finishes processing.
	 * Used when the pane is embedded (e.g. in a hiding pane) rather than
	 * shown as a dialog.
	 */
	private Runnable onCloseCallback;

	/**
	 * Show the offline processing dialog as a standalone window.
	 * 
	 * @param owner the owner window (can be null).
	 */
	public static void showDialog(Window owner) {
		OfflineProcessPaneFX pane = new OfflineProcessPaneFX();
		pane.createAndShow(owner);
	}

	/**
	 * Create the content pane for embedding in another container (e.g. a
	 * hiding pane). The returned Node contains all the controls.
	 * Call {@link #setOnClose(Runnable)} before or after to receive close
	 * notifications.
	 * 
	 * @return the content node.
	 */
	public Node createContentPane() {
		Node content = buildContent();
		populateTasks();
		enableControls();
		return content;
	}

	/**
	 * Set a callback to be invoked when the user presses Close or processing
	 * completes/stops.
	 * 
	 * @param onClose the callback.
	 */
	public void setOnClose(Runnable onClose) {
		this.onCloseCallback = onClose;
	}

	/**
	 * Create and show the dialog (standalone mode).
	 */
	private void createAndShow(Window owner) {
		stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		if (owner != null) {
			stage.initOwner(owner);
		}
		stage.setTitle("Offline Reprocessing");

		Node content = buildContent();

		ScrollPane scrollPane = new ScrollPane(content);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(false);

		Scene scene = new Scene(scrollPane, 550, 700);
		stage.setScene(scene);
		stage.setResizable(true);

		populateTasks();
		enableControls();

		stage.show();
	}

	/**
	 * Build the content VBox containing all sections.
	 */
	private Node buildContent() {
		VBox root = new VBox(10);
		root.setPadding(new Insets(10));

		root.getChildren().add(createDataOptionsPane());
		root.getChildren().add(createTasksPane());
		root.getChildren().add(createNotesPane());
		root.getChildren().add(createProgressPane());
		root.getChildren().add(createButtonPane());

		ScrollPane scrollPane = new ScrollPane(root);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		return scrollPane;
	}

	/**
	 * Create the data options pane (data selection combo, delete old data, date
	 * fields).
	 */
	private TitledPane createDataOptionsPane() {
		VBox content = new VBox(5);
		content.setPadding(new Insets(5));

		HBox dataSelRow = new HBox(10);
		dataSelRow.setAlignment(Pos.CENTER_LEFT);
		dataSelRow.getChildren().add(new Label("Data:"));
		dataSelectionCombo = new ComboBox<>();
		dataSelectionCombo.getItems().addAll("Loaded Data", "All Data",
				"New Data", "Select Data");
		dataSelectionCombo.getSelectionModel().select(0);
		dataSelectionCombo.setOnAction(e -> onDataSelectionChanged());
		HBox.setHgrow(dataSelectionCombo, Priority.ALWAYS);
		dataSelectionCombo.setMaxWidth(Double.MAX_VALUE);
		dataSelRow.getChildren().add(dataSelectionCombo);
		content.getChildren().add(dataSelRow);

		// Date selection pane (for "Select Data" option).
		dateSelectionPane = new VBox(5);
		dateSelectionPane.setPadding(new Insets(5, 0, 0, 0));

		GridPane dateGrid = new GridPane();
		dateGrid.setHgap(10);
		dateGrid.setVgap(5);
		dateGrid.add(new Label("Start time:"), 0, 0);
		startTimeField = new TextField();
		startTimeField.setPrefColumnCount(20);
		dateGrid.add(startTimeField, 1, 0);
		dateGrid.add(new Label("End time:"), 0, 1);
		endTimeField = new TextField();
		endTimeField.setPrefColumnCount(20);
		dateGrid.add(endTimeField, 1, 1);
		dateSelectionPane.getChildren().add(dateGrid);
		dateSelectionPane.setVisible(false);
		dateSelectionPane.setManaged(false);
		content.getChildren().add(dateSelectionPane);

		deleteOldDataCheck = new CheckBox("Delete old database entries");
		deleteOldDataCheck.setTooltip(new Tooltip(
				"Delete old data entries in the corresponding database table\n"
						+ "(Binary file data will always be overwritten)"));
		content.getChildren().add(deleteOldDataCheck);

		TitledPane titledPane = new TitledPane("Data Options", content);
		titledPane.setCollapsible(false);
		return titledPane;
	}

	/**
	 * Handle changes to the data selection combo box.
	 */
	private void onDataSelectionChanged() {
		int sel = dataSelectionCombo.getSelectionModel().getSelectedIndex();
		boolean showDates = (sel == TaskGroupParams.PROCESS_SPECIFICPERIOD);
		dateSelectionPane.setVisible(showDates);
		dateSelectionPane.setManaged(showDates);
	}

	/**
	 * Create the tasks pane which will contain all task groups.
	 */
	private TitledPane createTasksPane() {
		taskGroupContainer = new VBox(5);
		taskGroupContainer.setPadding(new Insets(5));

		TitledPane titledPane = new TitledPane("Tasks", taskGroupContainer);
		titledPane.setCollapsible(false);
		return titledPane;
	}

	/**
	 * Create the notes pane.
	 */
	private TitledPane createNotesPane() {
		notesArea = new TextArea();
		notesArea.setPrefRowCount(2);
		notesArea.setPromptText(
				"Notes to add to database record of completed tasks");
		notesArea.setWrapText(true);

		TitledPane titledPane = new TitledPane("Notes", notesArea);
		titledPane.setCollapsible(false);
		return titledPane;
	}

	/**
	 * Create the progress pane.
	 */
	private TitledPane createProgressPane() {
		VBox content = new VBox(5);
		content.setPadding(new Insets(5));

		statusLabel = new Label(" ");
		currentFileLabel = new Label(" ");

		GridPane progressGrid = new GridPane();
		progressGrid.setHgap(10);
		progressGrid.setVgap(5);

		progressGrid.add(new Label("File:"), 0, 0);
		loadedProgress = new ProgressBar(0);
		loadedProgress.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(loadedProgress, Priority.ALWAYS);
		progressGrid.add(loadedProgress, 1, 0);

		progressGrid.add(new Label("All Data:"), 0, 1);
		globalProgress = new ProgressBar(0);
		globalProgress.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(globalProgress, Priority.ALWAYS);
		progressGrid.add(globalProgress, 1, 1);

		content.getChildren().addAll(statusLabel, currentFileLabel,
				progressGrid);

		TitledPane titledPane = new TitledPane("Progress", content);
		titledPane.setCollapsible(false);
		return titledPane;
	}

	/**
	 * Create the button pane with Start and Close buttons.
	 */
	private HBox createButtonPane() {
		HBox buttonBox = new HBox(10);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		buttonBox.setPadding(new Insets(5, 0, 0, 0));

		startButton = new Button("Start");
		startButton.setOnAction(e -> startProcessing());

		closeButton = new Button("Close");
		closeButton.setOnAction(e -> closeOrStop());

		buttonBox.getChildren().addAll(startButton, closeButton);
		return buttonBox;
	}

	/**
	 * Populate the task list from all existing offline task groups across all
	 * modules. Each existing {@link OfflineTaskGroup} becomes a collapsible
	 * section with check boxes for each of its tasks.
	 */
	private void populateTasks() {
		taskGroupContainer.getChildren().clear();
		taskGroupEntries.clear();

		PamConfiguration config = PamController.getInstance()
				.getPamConfiguration();
		ArrayList<OfflineTaskGroup> allGroups = config
				.getAllOfflineTaskGroups();

		if (allGroups == null || allGroups.isEmpty()) {
			taskGroupContainer.getChildren()
					.add(new Label("No offline tasks available."));
			return;
		}

		for (OfflineTaskGroup taskGroup : allGroups) {
			if (taskGroup.getNTasks() == 0) {
				continue;
			}

			// Build a descriptive label for this group.
			PamDataBlock primaryBlock = taskGroup.getPrimaryDataBlock();
			String groupLabel = taskGroup.getUnitName();
			if (primaryBlock != null) {
				groupLabel += " (" + primaryBlock.getDataName() + ")";
			}

			// Create UI entry for this group.
			TaskGroupEntry groupEntry = new TaskGroupEntry(taskGroup,
					groupLabel);
			taskGroupEntries.add(groupEntry);
			taskGroupContainer.getChildren().add(groupEntry.getNode());
		}

		if (taskGroupEntries.isEmpty()) {
			taskGroupContainer.getChildren()
					.add(new Label("No offline tasks available."));
		}
	}

	/**
	 * Enable or disable controls based on the current status.
	 */
	private void enableControls() {
		boolean notRunning = currentStatus != TaskStatus.RUNNING;

		dataSelectionCombo.setDisable(!notRunning);
		deleteOldDataCheck.setDisable(!notRunning);
		notesArea.setDisable(!notRunning);
		startTimeField.setDisable(!notRunning);
		endTimeField.setDisable(!notRunning);

		// Count selected tasks and update check box / button states.
		int selectedCount = 0;
		for (TaskGroupEntry entry : taskGroupEntries) {
			for (TaskCheckBoxEntry cbEntry : entry.taskCheckBoxes) {
				cbEntry.checkBox.setDisable(
						!notRunning || !cbEntry.task.canRun());
				if (!cbEntry.task.canRun()) {
					cbEntry.checkBox.setSelected(false);
				}
				if (cbEntry.settingsButton != null) {
					cbEntry.settingsButton.setDisable(!notRunning);
				}
				if (cbEntry.checkBox.isSelected()
						&& cbEntry.task.canRun()) {
					selectedCount++;
				}
			}
		}

		startButton.setDisable(selectedCount == 0 || !notRunning);
		closeButton.setText(notRunning ? "Close" : "Stop!");
	}

	/**
	 * Read parameters from the dialog, apply them to each task group, validate,
	 * and return true if valid.
	 */
	private boolean getParams() {
		int dataChoice = dataSelectionCombo.getSelectionModel()
				.getSelectedIndex();
		boolean deleteOld = deleteOldDataCheck.isSelected();
		String note = notesArea.getText();

		if (note == null || note.trim().isEmpty()) {
			showWarning("Task note",
					"You must enter a note about what you are doing.");
			return false;
		}

		long startTime = 0;
		long endTime = Long.MAX_VALUE;

		if (dataChoice == TaskGroupParams.PROCESS_SPECIFICPERIOD) {
			startTime = PamCalendar
					.msFromDateString(startTimeField.getText());
			endTime = PamCalendar
					.msFromDateString(endTimeField.getText());
			if (startTime < 0) {
				showWarning("Start value invalid",
						"The start time is invalid.");
				return false;
			}
			if (endTime < 0) {
				showWarning("End value invalid",
						"The end time is invalid.");
				return false;
			}
			if (startTime == endTime) {
				showWarning("Error in start and end value",
						"The start time is the same as the end time.");
				return false;
			}
		}

		// Apply parameters to each existing task group.
		for (TaskGroupEntry entry : taskGroupEntries) {
			TaskGroupParams params = entry.taskGroup.getTaskGroupParams();
			params.dataChoice = dataChoice;
			params.deleteOld = deleteOld;
			params.taskNote = note;

			if (dataChoice == TaskGroupParams.PROCESS_SPECIFICPERIOD) {
				params.startRedoDataTime = startTime;
				params.endRedoDataTime = endTime;
			}

			// Set task selection based on check boxes.
			for (int i = 0; i < entry.taskCheckBoxes.size(); i++) {
				TaskCheckBoxEntry cbEntry = entry.taskCheckBoxes.get(i);
				cbEntry.task.setDoRun(cbEntry.checkBox.isSelected());
				params.setTaskSelection(i,
						cbEntry.checkBox.isSelected());
			}
		}

		return true;
	}

	/**
	 * Start processing all task groups that have at least one selected task.
	 * Groups are run sequentially.
	 */
	private void startProcessing() {
		if (!getParams()) {
			return;
		}

		// Collect task groups that have at least one selected task.
		List<TaskGroupEntry> groupsToRun = new ArrayList<>();
		for (TaskGroupEntry entry : taskGroupEntries) {
			boolean hasSelected = false;
			for (TaskCheckBoxEntry cbEntry : entry.taskCheckBoxes) {
				if (cbEntry.checkBox.isSelected()
						&& cbEntry.task.canRun()) {
					hasSelected = true;
					break;
				}
			}
			if (hasSelected) {
				groupsToRun.add(entry);
			}
		}

		if (groupsToRun.isEmpty()) {
			return;
		}

		currentStatus = TaskStatus.RUNNING;
		enableControls();
		currentRunIndex = 0;

		// Run groups sequentially.
		runNextGroup(groupsToRun);
	}

	/**
	 * Run the next task group in the queue.
	 * 
	 * @param groupsToRun the list of groups to process.
	 */
	private void runNextGroup(List<TaskGroupEntry> groupsToRun) {
		if (currentRunIndex >= groupsToRun.size()) {
			// All done.
			currentStatus = TaskStatus.COMPLETE;
			Platform.runLater(() -> {
				statusLabel.setText("All tasks complete");
				globalProgress.setProgress(1.0);
				loadedProgress.setProgress(1.0);
				enableControls();
			});
			return;
		}

		TaskGroupEntry entry = groupsToRun.get(currentRunIndex);
		runningTaskGroup = entry.taskGroup;

		// Update status to show which group is running.
		Platform.runLater(() -> {
			statusLabel.setText("Running: " + entry.label + " ("
					+ (currentRunIndex + 1) + " of "
					+ groupsToRun.size() + ")");
		});

		// Set up a monitor to receive progress updates.
		entry.taskGroup
				.setTaskMonitor(new FXTaskMonitor(groupsToRun));

		// Run the tasks. This starts processing in a SwingWorker
		// background thread. The FXTaskMonitor will receive callbacks
		// and chain to the next group on completion.
		entry.taskGroup.runTasks();
	}

	/**
	 * Close the pane or stop current processing.
	 */
	private void closeOrStop() {
		if (currentStatus == TaskStatus.RUNNING) {
			if (runningTaskGroup != null) {
				runningTaskGroup.killTasks();
			}
			currentStatus = TaskStatus.INTERRUPTED;
			enableControls();
		} else {
			// Close: either close the dialog or invoke the callback.
			if (stage != null) {
				stage.close();
			}
			if (onCloseCallback != null) {
				onCloseCallback.run();
			}
		}
	}

	/**
	 * Show a simple warning alert.
	 */
	private void showWarning(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		if (stage != null) {
			alert.initOwner(stage);
		}
		alert.showAndWait();
	}

	// =========================================================================
	// Inner classes
	// =========================================================================

	/**
	 * TaskMonitor implementation that updates the JavaFX progress UI from
	 * the SwingWorker callbacks (forwarded via Platform.runLater) and chains
	 * to the next task group when the current one completes.
	 */
	private class FXTaskMonitor implements TaskMonitor {

		private final List<TaskGroupEntry> groupsToRun;

		public FXTaskMonitor(List<TaskGroupEntry> groupsToRun) {
			this.groupsToRun = groupsToRun;
		}

		@Override
		public void setTaskStatus(TaskMonitorData taskMonitorData) {
			Platform.runLater(() -> {
				updateProgressUI(taskMonitorData);

				if (taskMonitorData.taskStatus == TaskStatus.COMPLETE
						|| taskMonitorData.taskStatus == TaskStatus.CRASHED
						|| taskMonitorData.taskStatus == TaskStatus.INTERRUPTED) {
					onGroupFinished(taskMonitorData.taskStatus);
				}
			});
		}

		/**
		 * Update the progress UI elements from the monitor data.
		 */
		private void updateProgressUI(TaskMonitorData data) {
			String statusText = data.taskStatus.toString();
			if (data.taskActivity != null) {
				statusText += ", " + data.taskActivity.toString();
			}
			statusLabel.setText(statusText);

			if (data.fileOrStatus != null
					&& !data.fileOrStatus.isEmpty()) {
				currentFileLabel.setText(data.fileOrStatus);
			} else {
				currentFileLabel.setText(" ");
			}

			int maxProg = Math.max(1, data.progMaximum);

			switch (data.taskActivity) {
			case LINKING:
			case LOADING:
				globalProgress.setProgress(
						(double) data.progValue / maxProg);
				loadedProgress.setProgress(-1); // indeterminate
				break;
			case PROCESSING:
				loadedProgress.setProgress(
						(double) data.progValue / maxProg);
				break;
			case SAVING:
				break;
			case IDLE:
				loadedProgress.setProgress(0);
				break;
			default:
				break;
			}

			switch (data.taskStatus) {
			case COMPLETE:
				globalProgress.setProgress(1.0);
				loadedProgress.setProgress(1.0);
				break;
			case STARTING:
				globalProgress.setProgress(0);
				loadedProgress.setProgress(0);
				break;
			default:
				break;
			}
		}

		/**
		 * Called when the current group finishes. Chains to the next group
		 * or finalises.
		 */
		private void onGroupFinished(TaskStatus status) {
			if (status == TaskStatus.INTERRUPTED
					|| status == TaskStatus.CRASHED) {
				currentStatus = status;
				enableControls();
				return;
			}

			currentRunIndex++;
			if (currentRunIndex < groupsToRun.size()) {
				// Run the next group.
				runNextGroup(groupsToRun);
			} else {
				currentStatus = TaskStatus.COMPLETE;
				statusLabel.setText("All tasks complete");
				enableControls();
			}
		}
	}

	/**
	 * Represents a single OfflineTaskGroup in the UI, including check boxes
	 * for each of its tasks.
	 */
	private class TaskGroupEntry {

		final OfflineTaskGroup taskGroup;
		final String label;
		final List<TaskCheckBoxEntry> taskCheckBoxes = new ArrayList<>();
		private final Node node;

		TaskGroupEntry(OfflineTaskGroup taskGroup, String label) {
			this.taskGroup = taskGroup;
			this.label = label;
			this.node = createNode();
		}

		Node getNode() {
			return node;
		}

		/**
		 * Create the UI node for this task group: a collapsible
		 * TitledPane containing a check box row for each task.
		 */
		private Node createNode() {
			VBox content = new VBox(3);
			content.setPadding(new Insets(2));

			int nTasks = taskGroup.getNTasks();
			for (int i = 0; i < nTasks; i++) {
				OfflineTask task = taskGroup.getTask(i);

				HBox row = new HBox(8);
				row.setAlignment(Pos.CENTER_LEFT);

				CheckBox cb = new CheckBox(task.getName());
				cb.setSelected(task.isDoRun());
				cb.setDisable(!task.canRun());

				// Build a tooltip with module info and run status.
				String tooltipText = task.getLongName();
				if (!task.canRun()) {
					String why = task.whyNot();
					if (why != null && !why.isEmpty()) {
						tooltipText = why;
					}
				}
				cb.setTooltip(new Tooltip(tooltipText));

				// Update doRun when check box changes.
				cb.selectedProperty().addListener(
						(obs, oldVal, newVal) -> {
							task.setDoRun(newVal);
							enableControls();
						});

				TaskCheckBoxEntry cbEntry = new TaskCheckBoxEntry(
						task, cb);

				if (task.hasSettings()) {
					Button settingsBtn = new Button("\u2699"); // gear symbol
					settingsBtn.setTooltip(new Tooltip(
							"Settings for " + task.getName()));
					settingsBtn.setOnAction(e -> {
						task.callSettings();
					});
					cbEntry.settingsButton = settingsBtn;
					row.getChildren().addAll(cb, settingsBtn);
				} else {
					row.getChildren().add(cb);
				}

				taskCheckBoxes.add(cbEntry);
				content.getChildren().add(row);
			}

			TitledPane titledPane = new TitledPane(label, content);
			titledPane.setExpanded(true);
			titledPane.setCollapsible(true);
			return titledPane;
		}
	}

	/**
	 * Associates a task with its UI check box and optional settings button.
	 */
	private static class TaskCheckBoxEntry {
		final OfflineTask task;
		final CheckBox checkBox;
		Button settingsButton;

		TaskCheckBoxEntry(OfflineTask task, CheckBox checkBox) {
			this.task = task;
			this.checkBox = checkBox;
		}
	}
}
