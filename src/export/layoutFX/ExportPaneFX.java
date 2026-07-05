package export.layoutFX;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import export.ExportOptions;
import export.ExportParams;
import export.PamDataUnitExporter;
import export.PamExporterManager;
import export.swing.ClickEventExportTask;
import export.swing.ExportTask;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
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
import pamViewFX.fxGlyphs.PamGlyphDude;

/**
 * A JavaFX dialog for exporting PAMGuard data to external file formats
 * (MATLAB, R, WAV, etc.). This mirrors the Swing {@code ExportProcessDialog}
 * but presents all exportable data blocks in a single unified JavaFX pane.
 * <p>
 * The dialog uses the existing {@link PamExporterManager} and
 * {@link ExportTask} infrastructure. An internal {@link ExportTaskGroup}
 * is created containing one {@link ExportTask} per compatible data block.
 * Tasks are run sequentially (one per data block) using the same mechanism
 * as the Swing dialog.
 *
 * @author Jamie Macaulay
 */
public class ExportPaneFX {

	/** The dialog stage. */
	private Stage stage;

	/** The export manager that handles file writing. */
	private PamExporterManager exportManager;

	/** The internal task group that holds all export tasks. */
	private ExportTaskGroup exportTaskGroup;

	/** Container for the data block check boxes. */
	private VBox dataBlockContainer;

	/** List of check-box entries for each export task. */
	private List<TaskCheckBoxEntry> taskCheckBoxes = new ArrayList<>();

	/** Data selection combo (Loaded Data, All Data, New Data, Select Data). */
	private ComboBox<String> dataSelectionCombo;

	/** Date selection pane (visible only for "Select Data"). */
	private VBox dateSelectionPane;

	/** Start time text field. */
	private TextField startTimeField;

	/** End time text field. */
	private TextField endTimeField;

	/** Export folder text field. */
	private TextField exportFolderField;

	/** Toggle buttons for selecting the export format. */
	private ToggleButton[] exportFormatButtons;

	/** Toggle group for export format selection. */
	private ToggleGroup exportFormatGroup;

	/** Spinner for maximum file size in MB. */
	private Spinner<Double> fileSizeSpinner;

	/** Container for exporter-specific options pane. */
	private VBox extraOptionsContainer;

	/** Status label. */
	private Label statusLabel;

	/** Current file label. */
	private Label currentFileLabel;

	/** Progress bar for per-file / loaded-data progress. */
	private ProgressBar loadedProgress;

	/** Progress bar for overall progress. */
	private ProgressBar globalProgress;

	/** Start / export button. */
	private Button startButton;

	/** Close / stop button. */
	private Button closeButton;

	/** Current processing status. */
	private volatile TaskStatus currentStatus = TaskStatus.IDLE;

	/** Number of completed active tasks (for global progress tracking). */
	private volatile int completeActiveTasks = 0;

	/** Total number of active (selected + runnable) tasks. */
	private volatile int totalActiveTasks = 0;

	/** Optional callback invoked when the user closes the pane. */
	private Runnable onCloseCallback;

	/**
	 * Show the export dialog as a standalone window.
	 *
	 * @param owner the owner window (can be null).
	 */
	public static void showDialog(Window owner) {
		ExportPaneFX pane = new ExportPaneFX();
		pane.createAndShow(owner);
	}

	/**
	 * Create the content pane for embedding in another container (e.g.
	 * a hiding pane). Call {@link #setOnClose(Runnable)} to receive
	 * close notifications.
	 *
	 * @return the content node.
	 */
	public Node createContentPane() {
		exportManager = ExportOptions.getInstance().getExportManager();
		Node content = buildContent();
		createExportGroup();
		setParams(exportManager.getExportParams());
		enableControls();
		return content;
	}

	/**
	 * Set a callback to be invoked when the user presses Close.
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
		exportManager = ExportOptions.getInstance().getExportManager();

		stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		if (owner != null) {
			stage.initOwner(owner);
		}
		stage.setTitle("Export Data");

		Node content = buildContent();

		ScrollPane scrollPane = new ScrollPane(content);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(false);

		Scene scene = new Scene(scrollPane, 550, 650);
		stage.setScene(scene);
		stage.setResizable(true);

		createExportGroup();
		setParams(exportManager.getExportParams());
		enableControls();

		stage.show();
	}

	/**
	 * Build the content VBox containing all sections.
	 */
	private Node buildContent() {
		VBox root = new VBox(10);
		root.setPadding(new Insets(10));

		root.getChildren().add(createExportSettingsPane());
		root.getChildren().add(createDataOptionsPane());
		root.getChildren().add(createDataBlocksPane());
		root.getChildren().add(createProgressPane());
		root.getChildren().add(createButtonPane());

		ScrollPane scrollPane = new ScrollPane(root);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		return scrollPane;
	}

	// =========================================================================
	// UI creation
	// =========================================================================

	/**
	 * Create the export settings pane: format selection, folder, file size.
	 */
	private TitledPane createExportSettingsPane() {
		VBox content = new VBox(8);
		content.setPadding(new Insets(5));

		// --- Format toggle buttons ---
		HBox formatRow = new HBox(5);
		formatRow.setAlignment(Pos.CENTER_LEFT);
		formatRow.getChildren().add(new Label("Format:"));

		exportFormatGroup = new ToggleGroup();
		int nExporters = exportManager.getNumExporters();
		exportFormatButtons = new ToggleButton[nExporters];
		for (int i = 0; i < nExporters; i++) {
			PamDataUnitExporter exporter = exportManager.getExporter(i);
			ToggleButton tb = new ToggleButton();
			tb.setGraphic(PamGlyphDude.createPamIcon(exporter.getIconString(), 20));
			tb.setTooltip(new Tooltip("Export to " + exporter.getName()
					+ " file (." + exporter.getFileExtension() + ")"));
			tb.setToggleGroup(exportFormatGroup);
			final int idx = i;
			tb.setOnAction(e -> onExportFormatChanged(idx));
			exportFormatButtons[i] = tb;
			formatRow.getChildren().add(tb);
		}
		content.getChildren().add(formatRow);

		// --- Export folder ---
		HBox folderRow = new HBox(5);
		folderRow.setAlignment(Pos.CENTER_LEFT);
		exportFolderField = new TextField();
		exportFolderField.setPromptText("Export folder...");
		HBox.setHgrow(exportFolderField, Priority.ALWAYS);
		Button browseButton = new Button("Browse...");
		browseButton.setOnAction(e -> browseFolder());
		folderRow.getChildren().addAll(exportFolderField, browseButton);
		content.getChildren().add(folderRow);

		// --- Max file size ---
		HBox sizeRow = new HBox(8);
		sizeRow.setAlignment(Pos.CENTER_LEFT);
		sizeRow.getChildren().add(new Label("Maximum file size:"));

		SpinnerValueFactory.ListSpinnerValueFactory<Double> sizeFactory =
				new SpinnerValueFactory.ListSpinnerValueFactory<>(
						javafx.collections.FXCollections.observableArrayList(
								10., 30., 60., 100., 200., 300., 600., 1000.));
		sizeFactory.setValue(1000.);
		fileSizeSpinner = new Spinner<>();
		fileSizeSpinner.setValueFactory(sizeFactory);
		fileSizeSpinner.setEditable(false);
		fileSizeSpinner.setPrefWidth(100);
		sizeRow.getChildren().addAll(fileSizeSpinner, new Label("MB"));
		content.getChildren().add(sizeRow);

		// --- Extra exporter options ---
		extraOptionsContainer = new VBox();
		content.getChildren().add(extraOptionsContainer);

		TitledPane titledPane = new TitledPane("Export Settings", content);
		titledPane.setCollapsible(false);
		return titledPane;
	}

	/**
	 * Create the data options pane.
	 */
	private TitledPane createDataOptionsPane() {
		VBox content = new VBox(5);
		content.setPadding(new Insets(5));

		HBox dataSelRow = new HBox(10);
		dataSelRow.setAlignment(Pos.CENTER_LEFT);
		dataSelRow.getChildren().add(new Label("Data:"));
		dataSelectionCombo = new ComboBox<>();
		dataSelectionCombo.getItems().addAll(
				"Loaded Data", "All Data", "New Data", "Select Data");
		dataSelectionCombo.getSelectionModel().select(0);
		dataSelectionCombo.setOnAction(e -> onDataSelectionChanged());
		HBox.setHgrow(dataSelectionCombo, Priority.ALWAYS);
		dataSelectionCombo.setMaxWidth(Double.MAX_VALUE);
		dataSelRow.getChildren().add(dataSelectionCombo);
		content.getChildren().add(dataSelRow);

		// Date fields for "Select Data".
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

		TitledPane titledPane = new TitledPane("Data Options", content);
		titledPane.setCollapsible(false);
		return titledPane;
	}

	/**
	 * Create the data blocks (tasks) pane.
	 */
	private TitledPane createDataBlocksPane() {
		dataBlockContainer = new VBox(3);
		dataBlockContainer.setPadding(new Insets(5));

		TitledPane titledPane = new TitledPane("Export Data", dataBlockContainer);
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
	 * Create the button pane.
	 */
	private HBox createButtonPane() {
		HBox buttonBox = new HBox(10);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		buttonBox.setPadding(new Insets(5, 0, 0, 0));

		startButton = new Button("Export");
		startButton.setOnAction(e -> startExport());

		closeButton = new Button("Close");
		closeButton.setOnAction(e -> closeOrStop());

		buttonBox.getChildren().addAll(startButton, closeButton);
		return buttonBox;
	}

	// =========================================================================
	// Task group / population
	// =========================================================================

	/**
	 * Create the export task group and populate it with one ExportTask per
	 * compatible data block (mirroring
	 * {@code ExportProcessDialog.createExportGroup}).
	 */
	private void createExportGroup() {
		exportTaskGroup = new ExportTaskGroup("FX Export data");

		ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock dataBlock : dataBlocks) {
			if (exportManager.canExportDataBlock(dataBlock)) {
				exportTaskGroup.addTask(createExportTask(dataBlock));
			}
		}
	}

	/**
	 * Create an ExportTask for a data block. Mirrors
	 * {@code ExportProcessDialog.createExportTask}.
	 */
	private ExportTask createExportTask(PamDataBlock dataBlock) {
		if (dataBlock.getDataName().equals("Tracked Events")) {
			return new ClickEventExportTask(dataBlock, exportManager);
		}
		return new ExportTask(dataBlock, exportManager);
	}

	/**
	 * Populate the data block check-box list from the current task group.
	 */
	private void populateDataBlocks() {
		dataBlockContainer.getChildren().clear();
		taskCheckBoxes.clear();

		int nTasks = exportTaskGroup.getNTasks();
		if (nTasks == 0) {
			dataBlockContainer.getChildren().add(
					new Label("No exportable data blocks found."));
			return;
		}

		for (int i = 0; i < nTasks; i++) {
			OfflineTask task = exportTaskGroup.getTask(i);

			HBox row = new HBox(8);
			row.setAlignment(Pos.CENTER_LEFT);

			CheckBox cb = new CheckBox(task.getName());
			cb.setSelected(task.isDoRun());
			cb.setDisable(!task.canRun());

			if (!task.canRun()) {
				String why = task.whyNot();
				cb.setTooltip(new Tooltip(
						why != null ? why : "Cannot export this data block"));
			} else {
				cb.setTooltip(new Tooltip(task.getLongName()));
			}

			cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
				task.setDoRun(newVal);
				enableControls();
			});

			TaskCheckBoxEntry entry = new TaskCheckBoxEntry(task, cb);

			if (task.hasSettings()) {
				Button settingsBtn = new Button("\u2699");
				settingsBtn.setTooltip(new Tooltip(
						"Data selector for " + task.getName()));
				settingsBtn.setOnAction(e -> task.callSettings());
				entry.settingsButton = settingsBtn;
				row.getChildren().addAll(cb, settingsBtn);
			} else {
				row.getChildren().add(cb);
			}

			taskCheckBoxes.add(entry);
			dataBlockContainer.getChildren().add(row);
		}
	}

	// =========================================================================
	// Event handlers
	// =========================================================================

	/**
	 * Called when the export format toggle changes.
	 */
	private void onExportFormatChanged(int index) {
		ExportParams params = exportManager.getExportParams();
		params.exportChoice = index;
		exportManager.setExportParams(params);

		// Re-check which tasks can run for this format.
		enableControls();

		// Swap in exporter-specific options pane.
		extraOptionsContainer.getChildren().clear();
		PamDataUnitExporter exporter = exportManager.getExporter(index);
		Pane optionsPane = exporter.getOptionsPane();
		if (optionsPane != null) {
			extraOptionsContainer.getChildren().add(optionsPane);
		}
	}

	/**
	 * Handle data selection combo changes.
	 */
	private void onDataSelectionChanged() {
		int sel = dataSelectionCombo.getSelectionModel().getSelectedIndex();
		boolean showDates = (sel == TaskGroupParams.PROCESS_SPECIFICPERIOD);
		dateSelectionPane.setVisible(showDates);
		dateSelectionPane.setManaged(showDates);
	}

	/**
	 * Open a directory chooser for the export folder.
	 */
	private void browseFolder() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Select Export Folder");
		String current = exportFolderField.getText();
		if (current != null && !current.isEmpty()) {
			File f = new File(current);
			if (f.exists() && f.isDirectory()) {
				dc.setInitialDirectory(f);
			}
		}
		File chosen = dc.showDialog(stage);
		if (chosen != null) {
			exportFolderField.setText(chosen.getAbsolutePath());
		}
	}

	// =========================================================================
	// Parameter handling
	// =========================================================================

	/**
	 * Set parameters into the dialog.
	 */
	private void setParams(ExportParams params) {
		if (params == null) params = new ExportParams();
		ExportParams p = params.clone();

		// Select the format button.
		if (p.exportChoice >= 0 && p.exportChoice < exportFormatButtons.length) {
			exportFormatButtons[p.exportChoice].setSelected(true);
			onExportFormatChanged(p.exportChoice);
		}

		exportFolderField.setText(p.folder);
		fileSizeSpinner.getValueFactory().setValue(p.maximumFileSize);

		// Populate tasks after setting format (so canRun reflects choice).
		populateDataBlocks();
	}

	/**
	 * Read parameters from the dialog. Returns null if invalid.
	 */
	private ExportParams getExportParams() {
		ExportParams params = exportManager.getExportParams().clone();

		// Export choice.
		int sel = -1;
		for (int i = 0; i < exportFormatButtons.length; i++) {
			if (exportFormatButtons[i].isSelected()) {
				sel = i;
				break;
			}
		}
		if (sel < 0) {
			showWarning("No format selected",
					"You must select an export format.");
			return null;
		}
		params.exportChoice = sel;

		// Folder.
		String folder = exportFolderField.getText();
		if (folder == null || folder.isEmpty()) {
			showWarning("No folder selected",
					"You must select an output folder.");
			return null;
		}
		File f = new File(folder);
		if (!f.exists() || !f.isDirectory()) {
			showWarning("Invalid folder",
					"The selected folder does not exist.");
			return null;
		}
		params.folder = folder;

		// File size.
		params.maximumFileSize = fileSizeSpinner.getValue();

		return params;
	}

	/**
	 * Read and validate all parameters (export settings + data selection +
	 * task selection). Returns true if valid.
	 */
	private boolean validateAndApplyParams() {
		ExportParams exportParams = getExportParams();
		if (exportParams == null) return false;

		exportManager.setExportParams(exportParams);

		int dataChoice = dataSelectionCombo.getSelectionModel()
				.getSelectedIndex();

		long startTime = 0;
		long endTime = Long.MAX_VALUE;
		if (dataChoice == TaskGroupParams.PROCESS_SPECIFICPERIOD) {
			startTime = PamCalendar.msFromDateString(startTimeField.getText());
			endTime = PamCalendar.msFromDateString(endTimeField.getText());
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

		// Apply to task group params.
		TaskGroupParams tgParams = exportTaskGroup.getTaskGroupParams();
		tgParams.dataChoice = dataChoice;
		tgParams.deleteOld = false; // never delete for export
		tgParams.taskNote = "Data export";
		if (dataChoice == TaskGroupParams.PROCESS_SPECIFICPERIOD) {
			tgParams.startRedoDataTime = startTime;
			tgParams.endRedoDataTime = endTime;
		}

		// Apply task selection.
		for (int i = 0; i < taskCheckBoxes.size(); i++) {
			TaskCheckBoxEntry entry = taskCheckBoxes.get(i);
			entry.task.setDoRun(entry.checkBox.isSelected());
			tgParams.setTaskSelection(i, entry.checkBox.isSelected());
		}

		return true;
	}

	// =========================================================================
	// Control state
	// =========================================================================

	/**
	 * Enable or disable controls based on current status and format
	 * selection.
	 */
	private void enableControls() {
		boolean notRunning = currentStatus != TaskStatus.RUNNING;

		dataSelectionCombo.setDisable(!notRunning);
		exportFolderField.setDisable(!notRunning);
		fileSizeSpinner.setDisable(!notRunning);
		startTimeField.setDisable(!notRunning);
		endTimeField.setDisable(!notRunning);
		for (ToggleButton tb : exportFormatButtons) {
			tb.setDisable(!notRunning);
		}

		int selectedCount = 0;
		for (TaskCheckBoxEntry entry : taskCheckBoxes) {
			boolean canRun = entry.task.canRun();
			entry.checkBox.setDisable(!notRunning || !canRun);
			if (!canRun) {
				entry.checkBox.setSelected(false);
			}
			if (entry.settingsButton != null) {
				entry.settingsButton.setDisable(!notRunning);
			}
			if (entry.checkBox.isSelected() && canRun) {
				selectedCount++;
			}
		}

		boolean formatSelected = exportFormatGroup.getSelectedToggle() != null;
		startButton.setDisable(
				selectedCount == 0 || !notRunning || !formatSelected);
		closeButton.setText(notRunning ? "Close" : "Stop!");
	}

	// =========================================================================
	// Export execution
	// =========================================================================

	/**
	 * Start the export.
	 */
	private void startExport() {
		if (!validateAndApplyParams()) {
			return;
		}

		currentStatus = TaskStatus.RUNNING;
		completeActiveTasks = 0;
		enableControls();

		// Count active tasks.
		totalActiveTasks = 0;
		for (TaskCheckBoxEntry entry : taskCheckBoxes) {
			if (entry.task.isDoRun() && entry.task.canRun()) {
				totalActiveTasks++;
			}
		}

		exportTaskGroup.runTasks();
	}

	/**
	 * Close the dialog or stop the current export.
	 */
	private void closeOrStop() {
		if (currentStatus == TaskStatus.RUNNING) {
			exportTaskGroup.killTasks();
			currentStatus = TaskStatus.INTERRUPTED;
			enableControls();
		} else {
			if (stage != null) {
				stage.close();
			}
			if (onCloseCallback != null) {
				onCloseCallback.run();
			}
		}
	}

	/**
	 * Show a warning alert.
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
	 * Custom OfflineTaskGroup for export. Mirrors the Swing
	 * {@code ExportProcessDialog.ExportTaskGroup}. It runs tasks
	 * sequentially (one per data block), swapping the primary data block
	 * before each run.
	 */
	private class ExportTaskGroup extends OfflineTaskGroup {

		public ExportTaskGroup(String settingsName) {
			super(null, settingsName);
		}

		@Override
		public String getUnitType() {
			return "Export Data";
		}

		/**
		 * Override to run tasks sequentially per data block.
		 * Starts from the first active (selected and runnable) task.
		 */
		@Override
		public boolean runTasks() {
			completeActiveTasks = 0;
			int firstActive = findNextActiveTask(-1);
			if (firstActive < 0) return false;
			runTaskFrom(firstActive);
			return true;
		}

		/**
		 * Run from a specific task index, setting the primary data block
		 * accordingly. Sets up a monitor that will chain to the next task
		 * on completion.
		 */
		public void runTaskFrom(int taskIndex) {
			setPrimaryDataBlock(getTask(taskIndex).getDataBlock());
			setTaskMonitor(new FXExportTaskMonitor(taskIndex));
			super.runTasks();
		}

		/**
		 * Find the next active task after the given index.
		 *
		 * @param afterIndex search from afterIndex+1 onwards (-1 to start
		 *                   from beginning).
		 * @return index of next active task, or -1 if none.
		 */
		private int findNextActiveTask(int afterIndex) {
			for (int i = afterIndex + 1; i < getNTasks(); i++) {
				OfflineTask t = getTask(i);
				if (t.isDoRun() && t.canRun()) {
					return i;
				}
			}
			return -1;
		}
	}

	/**
	 * Task monitor for export that updates the FX progress UI and chains
	 * to the next task on completion. Mirrors the Swing
	 * {@code ExportTaskMonitor}.
	 */
	private class FXExportTaskMonitor implements TaskMonitor {

		private final int taskIndex;
		private boolean chainStarted = false;

		FXExportTaskMonitor(int taskIndex) {
			this.taskIndex = taskIndex;
		}

		@Override
		public void setTaskStatus(TaskMonitorData data) {
			Platform.runLater(() -> {
				updateProgressUI(data);

				if (data.taskStatus == TaskStatus.COMPLETE && !chainStarted) {
					completeActiveTasks++;
					int next = exportTaskGroup.findNextActiveTask(taskIndex);
					if (next >= 0) {
						chainStarted = true;
						exportTaskGroup.runTaskFrom(next);
					} else {
						// All tasks done.
						currentStatus = TaskStatus.COMPLETE;
						statusLabel.setText("Export complete");
						globalProgress.setProgress(1.0);
						loadedProgress.setProgress(1.0);
						enableControls();
					}
				} else if (data.taskStatus == TaskStatus.CRASHED
						|| data.taskStatus == TaskStatus.INTERRUPTED) {
					currentStatus = data.taskStatus;
					enableControls();
				}
			});
		}

		/**
		 * Update progress UI, including global progress across all tasks.
		 */
		private void updateProgressUI(TaskMonitorData data) {
			String statusText = data.taskStatus.toString();
			if (data.taskActivity != null) {
				statusText += ", " + data.taskActivity.toString();
			}
			statusLabel.setText(statusText);

			if (data.fileOrStatus != null && !data.fileOrStatus.isEmpty()) {
				currentFileLabel.setText(data.fileOrStatus);
			} else {
				currentFileLabel.setText(" ");
			}

			int maxProg = Math.max(1, data.progMaximum);

			switch (data.taskActivity) {
			case LINKING:
			case LOADING:
				// Per-file progress is indeterminate while loading.
				loadedProgress.setProgress(-1);
				// Compute global progress across all tasks.
				double fileProgress = (double) data.progValue / maxProg;
				double totalProgress = (double) completeActiveTasks / totalActiveTasks
						+ fileProgress / totalActiveTasks;
				globalProgress.setProgress(totalProgress);
				break;
			case PROCESSING:
				loadedProgress.setProgress((double) data.progValue / maxProg);
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
			case STARTING:
				loadedProgress.setProgress(0);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Associates an export task with its check box and optional settings
	 * button.
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
