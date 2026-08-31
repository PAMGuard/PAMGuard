package clickTrainDetector.layout.ukf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SubDetectionLoader;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickTrainDetector.clickTrainAlgorithms.ukf.AffinityTrainer;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFParams;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * The "train affinity network" panel shown on the back of the UKF settings flip
 * pane. Lists the manually-annotated click trains ({@link OfflineEventDataUnit})
 * in the configuration; the user selects some or all, trains the affinity
 * network on them, chooses where to save the resulting JSON, and the detector is
 * set to use it.
 *
 * @author Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public class UKFAffinityTrainPane {

	private final UKFClickTrainAlgorithm algorithm;

	/** Called (on the FX thread) with the saved network file once training applies. */
	private final Consumer<File> onApplied;

	private final PamBorderPane mainPane;
	private final TableView<OfflineEventDataUnit> table;
	private final ProgressBar progressBar;
	private final Label statusLabel;
	private final PamButton trainButton;
	private final PamButton selectAllButton;

	/**
	 * Fraction of the progress bar given over to loading click data out of the
	 * binary store; the rest is the training itself.
	 */
	private static final double LOAD_FRACTION = 0.3;

	public UKFAffinityTrainPane(UKFClickTrainAlgorithm algorithm, Consumer<File> onApplied) {
		this.algorithm = algorithm;
		this.onApplied = onApplied;

		table = createTable();

		selectAllButton = new PamButton("Select all");
		selectAllButton.setOnAction(e -> table.getSelectionModel().selectAll());

		trainButton = new PamButton("Train & save…");
		trainButton.setTooltip(new Tooltip("Train the affinity network on the selected annotated click trains,\n"
				+ "then choose where to save it. The detector will use the trained network."));
		trainButton.setOnAction(e -> startTraining());

		progressBar = new ProgressBar(0);
		progressBar.setMaxWidth(Double.MAX_VALUE);
		progressBar.setVisible(false);

		statusLabel = new Label("");
		statusLabel.setWrapText(true);

		Label info = new Label("Select the manually-annotated click trains to train the affinity network on, "
				+ "then press Train & save. Use Ctrl/Cmd or Shift to multi-select.");
		info.setWrapText(true);

		Region spacer = new Region();
		HBox_setHgrow(spacer);

		PamHBox topBar = new PamHBox();
		topBar.setSpacing(5);
		topBar.setAlignment(Pos.CENTER_LEFT);
		topBar.getChildren().addAll(selectAllButton, spacer);

		PamHBox bottomBar = new PamHBox();
		bottomBar.setSpacing(5);
		bottomBar.setAlignment(Pos.CENTER_LEFT);
		HBox_setHgrow(progressBar);
		bottomBar.getChildren().addAll(progressBar, trainButton);

		PamVBox holder = new PamVBox();
		holder.setSpacing(8);
		holder.setPadding(new Insets(10, 10, 10, 10));
		holder.getChildren().addAll(info, topBar, table, statusLabel, bottomBar);
		PamVBox.setVgrow(table, Priority.ALWAYS);

		mainPane = new PamBorderPane(holder);
	}

	private TableView<OfflineEventDataUnit> createTable() {
		TableView<OfflineEventDataUnit> t = new TableView<>();
		t.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		t.setPlaceholder(new Label("No annotated click train events found in this configuration."));

		TableColumn<OfflineEventDataUnit, Number> numCol = new TableColumn<>("Event");
		numCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEventNumber()));

		TableColumn<OfflineEventDataUnit, String> typeCol = new TableColumn<>("Type");
		typeCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEventType()));

		TableColumn<OfflineEventDataUnit, Number> clicksCol = new TableColumn<>("Clicks");
		clicksCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNClicks()));

		TableColumn<OfflineEventDataUnit, String> durCol = new TableColumn<>("Duration");
		durCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(durationString(c.getValue())));

		TableColumn<OfflineEventDataUnit, String> timeCol = new TableColumn<>("Start time");
		timeCol.setCellValueFactory(
				c -> new ReadOnlyObjectWrapper<>(PamCalendar.formatDateTime(c.getValue().getTimeMilliseconds())));

		t.getColumns().add(numCol);
		t.getColumns().add(typeCol);
		t.getColumns().add(clicksCol);
		t.getColumns().add(durCol);
		t.getColumns().add(timeCol);
		return t;
	}

	private static String durationString(OfflineEventDataUnit event) {
		Double dur = event.getDurationInMilliseconds();
		if (dur == null) {
			return "-";
		}
		return String.format("%.1f s", dur / 1000.0);
	}

	/** (Re)populate the table from all annotated event data blocks in the model. */
	public void refresh() {
		table.getItems().clear();
		ArrayList<PamDataBlock> blocks = PamController.getInstance().getDataBlocks(OfflineEventDataUnit.class, true);
		for (PamDataBlock block : blocks) {
			for (Object unit : block.getDataCopy()) {
				table.getItems().add((OfflineEventDataUnit) unit);
			}
		}
		statusLabel.setText(table.getItems().isEmpty() ? "No annotated events found." : "");
	}

	private void startTraining() {
		List<OfflineEventDataUnit> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
		if (selected.isEmpty()) {
			statusLabel.setText("Select at least one event to train on.");
			return;
		}

		final UKFParams params = algorithm.getParams();

		setBusy(true);
		Task<AffinityTrainer.Result> task = new Task<AffinityTrainer.Result>() {
			@Override
			protected AffinityTrainer.Result call() throws Exception {
				// the clicks of events outside the currently loaded time period are not in
				// memory, so read whatever is missing back out of the binary store.
				List<List<PamDataUnit>> trainEvents = SubDetectionLoader.loadSubDetections(selected, (frac, msg) -> {
					updateProgress(frac * LOAD_FRACTION, 1.0);
					updateMessage(msg);
				});
				// bearing is only used if the clicks actually carry one.
				boolean bearingAvailable = false;
				int nClicks = 0;
				for (List<PamDataUnit> clicks : trainEvents) {
					nClicks += clicks.size();
					for (PamDataUnit click : clicks) {
						if (click.getLocalisation() != null && click.getLocalisation().getAngles() != null
								&& click.getLocalisation().getAngles().length > 0) {
							bearingAvailable = true;
							break;
						}
					}
				}
				if (nClicks == 0) {
					throw new RuntimeException("the selected events contain no clicks");
				}
				return AffinityTrainer.trainAndExport(trainEvents, params, bearingAvailable,
						AffinityTrainer.DEFAULT_HIDDEN, AffinityTrainer.DEFAULT_EPOCHS, (frac, msg) -> {
							updateProgress(LOAD_FRACTION + frac * (1 - LOAD_FRACTION), 1.0);
							updateMessage(msg);
						}, null);
			}
		};
		progressBar.progressProperty().bind(task.progressProperty());
		statusLabel.textProperty().bind(task.messageProperty());

		task.setOnSucceeded(e -> {
			unbind();
			setBusy(false);
			saveAndApply(task.getValue());
		});
		task.setOnFailed(e -> {
			unbind();
			setBusy(false);
			Throwable ex = task.getException();
			statusLabel.setText("Training failed: " + (ex == null ? "unknown error" : ex.getMessage()));
		});

		Thread thread = new Thread(task, "UKF-affinity-training");
		thread.setDaemon(true);
		thread.start();
	}

	/** Ask where to save the trained network, write it and apply it to the detector. */
	private void saveAndApply(AffinityTrainer.Result result) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Save trained affinity network");
		chooser.setInitialFileName("affinity_network.json");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Affinity network (*.json)", "*.json"));
		File file = chooser.showSaveDialog(mainPane.getScene() == null ? null : mainPane.getScene().getWindow());
		if (file == null) {
			statusLabel.setText(String.format("Trained (accuracy %.1f%%) but not saved.", result.accuracy * 100));
			return;
		}
		if (!file.getName().toLowerCase().endsWith(".json")) {
			file = new File(file.getAbsolutePath() + ".json");
		}
		try {
			result.network.writeJson(file);
		} catch (Exception ex) {
			statusLabel.setText("Could not save network: " + ex.getMessage());
			return;
		}
		statusLabel.setText(String.format("Saved %s (accuracy %.1f%%).", file.getName(), result.accuracy * 100));
		if (onApplied != null) {
			onApplied.accept(file);
		}
	}

	private void unbind() {
		progressBar.progressProperty().unbind();
		statusLabel.textProperty().unbind();
	}

	private void setBusy(boolean busy) {
		progressBar.setVisible(busy);
		trainButton.setDisable(busy);
		selectAllButton.setDisable(busy);
		table.setDisable(busy);
		if (!busy) {
			progressBar.progressProperty().unbind();
			progressBar.setProgress(0);
		}
	}

	public PamBorderPane getNode() {
		return mainPane;
	}

	private static void HBox_setHgrow(javafx.scene.Node node) {
		PamHBox.setHgrow(node, Priority.ALWAYS);
	}

}
