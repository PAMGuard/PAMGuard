package clickTrainDetector.layout.classification.templateClassifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SubDetectionLoader;
import clickDetector.ClickDetection;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
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
import matchedTemplateClassifer.MatchTemplate;
import matchedTemplateClassifer.TemplateExport;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * The "generate template" panel shown on the back of the spectrum template
 * classifier flip pane. Lists the manually-annotated click events
 * ({@link OfflineEventDataUnit}) in the configuration; the user selects some or
 * all, an average normalised spectrum of the clicks is generated which can be
 * exported to a .mat or .csv file (the same formats the template importers
 * read) and is set as the classification template.
 *
 * @author Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public class TemplateGeneratePane {

	/** Called (on the FX thread) with the generated template once it is applied. */
	private final Consumer<MatchTemplate> onApplied;

	private final PamBorderPane mainPane;
	private final TableView<OfflineEventDataUnit> table;
	private final ProgressBar progressBar;
	private final Label statusLabel;
	private final PamButton generateButton;
	private final PamButton selectAllButton;

	/**
	 * Fraction of the progress bar given over to loading click data out of the
	 * binary store; the rest is the spectrum calculation.
	 */
	private static final double LOAD_FRACTION = 0.5;

	/**
	 * File chooser for saving the generated template - field so the last used
	 * directory is remembered.
	 */
	private final FileChooser fileChooser;

	public TemplateGeneratePane(Consumer<MatchTemplate> onApplied) {
		this.onApplied = onApplied;

		table = createTable();

		selectAllButton = new PamButton("Select all");
		selectAllButton.setOnAction(e -> table.getSelectionModel().selectAll());

		generateButton = new PamButton("Generate & save…");
		generateButton.setTooltip(new Tooltip("Generate an average spectrum from the clicks in the selected events,\n"
				+ "then choose where to save it (.mat or .csv). The classifier will use the new template."));
		generateButton.setOnAction(e -> startGenerating());

		progressBar = new ProgressBar(0);
		progressBar.setMaxWidth(Double.MAX_VALUE);
		progressBar.setVisible(false);

		statusLabel = new Label("");
		statusLabel.setWrapText(true);

		Label info = new Label("Select the click events to generate an average spectrum template from, "
				+ "then press Generate & save. Use Ctrl/Cmd or Shift to multi-select.");
		info.setWrapText(true);

		Region spacer = new Region();
		PamHBox.setHgrow(spacer, Priority.ALWAYS);

		PamHBox topBar = new PamHBox();
		topBar.setSpacing(5);
		topBar.setAlignment(Pos.CENTER_LEFT);
		topBar.getChildren().addAll(selectAllButton, spacer);

		PamHBox bottomBar = new PamHBox();
		bottomBar.setSpacing(5);
		bottomBar.setAlignment(Pos.CENTER_LEFT);
		PamHBox.setHgrow(progressBar, Priority.ALWAYS);
		bottomBar.getChildren().addAll(progressBar, generateButton);

		PamVBox holder = new PamVBox();
		holder.setSpacing(8);
		holder.setPadding(new Insets(10, 10, 10, 10));
		holder.getChildren().addAll(info, topBar, table, statusLabel, bottomBar);
		PamVBox.setVgrow(table, Priority.ALWAYS);

		mainPane = new PamBorderPane(holder);

		fileChooser = new FileChooser();
		fileChooser.setTitle("Save spectrum template");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MATLAB template (*.mat)", "*.mat"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV template (*.csv)", "*.csv"));
	}

	private TableView<OfflineEventDataUnit> createTable() {
		TableView<OfflineEventDataUnit> t = new TableView<>();
		t.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		t.setPlaceholder(new Label("No annotated click events found in this configuration."));

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

	private void startGenerating() {
		List<OfflineEventDataUnit> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
		if (selected.isEmpty()) {
			statusLabel.setText("Select at least one event to generate a template from.");
			return;
		}
		final int nEvents = selected.size();
		// number of clicks actually used, set by the task and read once it succeeds.
		AtomicInteger nClicks = new AtomicInteger();

		setBusy(true);
		Task<MatchTemplate> task = new Task<MatchTemplate>() {
			@Override
			protected MatchTemplate call() throws Exception {
				// the clicks of events outside the currently loaded time period are not in
				// memory, so read whatever is missing back out of the binary store.
				List<List<PamDataUnit>> eventUnits = SubDetectionLoader.loadSubDetections(selected,
						(frac, msg) -> {
							updateProgress(frac * LOAD_FRACTION, 1.0);
							updateMessage(msg);
						});
				List<ClickDetection> clicks = new ArrayList<>();
				for (List<PamDataUnit> units : eventUnits) {
					for (PamDataUnit unit : units) {
						if (unit instanceof ClickDetection) {
							clicks.add((ClickDetection) unit);
						}
					}
				}
				if (clicks.isEmpty()) {
					throw new RuntimeException("the selected events contain no clicks");
				}
				nClicks.set(clicks.size());
				return generateTemplate(clicks, (frac, msg) -> {
					updateProgress(LOAD_FRACTION + frac * (1 - LOAD_FRACTION), 1.0);
					updateMessage(msg);
				});
			}
		};
		progressBar.progressProperty().bind(task.progressProperty());
		statusLabel.textProperty().bind(task.messageProperty());

		task.setOnSucceeded(e -> {
			unbind();
			setBusy(false);
			saveAndApply(task.getValue(), nEvents, nClicks.get());
		});
		task.setOnFailed(e -> {
			unbind();
			setBusy(false);
			Throwable ex = task.getException();
			statusLabel.setText("Template generation failed: " + (ex == null ? "unknown error" : ex.getMessage()));
		});

		Thread thread = new Thread(task, "Spectrum-template-generation");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Progress callback for template generation.
	 */
	private interface GenerateProgress {
		void update(double frac, String msg);
	}

	/**
	 * Generate an average normalised spectrum template from a list of clicks. All
	 * channels of every click are included. The FFT length is set from the
	 * shortest click so no waveform sections are cut off.
	 * @param clicks - the clicks to average.
	 * @param progress - progress callback.
	 * @return the average spectrum as a match template.
	 */
	private static MatchTemplate generateTemplate(List<ClickDetection> clicks, GenerateProgress progress) {
		// find an FFT length from the shortest click so no waveform is cut off.
		int minLength = Integer.MAX_VALUE;
		for (ClickDetection click : clicks) {
			if (click.getWaveData() != null && click.getWaveData()[0].length < minLength) {
				minLength = click.getWaveData()[0].length;
			}
		}
		if (minLength == Integer.MAX_VALUE) {
			throw new RuntimeException("no click waveforms available");
		}
		int fftLength = PamUtils.getMinFftLength(minLength);

		float sR = 0;
		for (ClickDetection click : clicks) {
			if (click.getParentDataBlock() != null) {
				sR = click.getParentDataBlock().getSampleRate();
				break;
			}
		}

		double[] average = null;
		int n = 0;
		int count = 0;
		for (ClickDetection click : clicks) {
			int nChan = PamUtils.getNumChannels(click.getChannelBitmap());
			for (int c = 0; c < nChan; c++) {
				double[] spectrum = click.getPowerSpectrum(c, fftLength);
				if (spectrum == null) {
					continue;
				}
				spectrum = normalise(spectrum);
				if (average == null) {
					average = new double[spectrum.length];
				}
				for (int i = 0; i < Math.min(average.length, spectrum.length); i++) {
					if (!Double.isNaN(spectrum[i])) {
						average[i] += spectrum[i];
					}
				}
				n++;
			}
			count++;
			progress.update(count / (double) clicks.size(),
					String.format("Calculating spectra: click %d of %d", count, clicks.size()));
		}
		if (average == null || n == 0) {
			throw new RuntimeException("no click spectra could be calculated");
		}
		for (int i = 0; i < average.length; i++) {
			average[i] /= n;
		}
		return new MatchTemplate("Event spectrum template", normalise(average), sR);
	}

	/**
	 * Normalise an array so its maximum value is 1.
	 * @param array - the array to normalise.
	 * @return the normalised array.
	 */
	private static double[] normalise(double[] array) {
		double maxVal = 0;
		for (double v : array) {
			if (!Double.isNaN(v) && v > maxVal) {
				maxVal = v;
			}
		}
		if (maxVal == 0) {
			return array;
		}
		double[] normalised = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			normalised[i] = array[i] / maxVal;
		}
		return normalised;
	}

	/** Ask where to save the generated template, write it and apply it to the classifier. */
	private void saveAndApply(MatchTemplate template, int nEvents, int nClicks) {
		fileChooser.setInitialFileName("spectrum_template.mat");
		File file = fileChooser.showSaveDialog(mainPane.getScene() == null ? null : mainPane.getScene().getWindow());
		if (file == null) {
			statusLabel.setText(String.format("Generated template from %d event%s (%d clicks) but not saved to file.",
					nEvents, nEvents == 1 ? "" : "s", nClicks));
			apply(template);
			return;
		}
		String name = file.getName().toLowerCase();
		try {
			if (name.endsWith(".csv")) {
				TemplateExport.exportTemplateCSV(file, template);
			}
			else {
				if (!name.endsWith(".mat")) {
					file = new File(file.getAbsolutePath() + ".mat");
				}
				TemplateExport.exportTemplateMAT(file, template);
			}
		} catch (Exception ex) {
			statusLabel.setText("Could not save template: " + ex.getMessage());
			return;
		}
		template.name = file.getName();
		statusLabel.setText(String.format("Saved %s (%d event%s, %d clicks).", file.getName(), nEvents,
				nEvents == 1 ? "" : "s", nClicks));
		apply(template);
	}

	private void apply(MatchTemplate template) {
		if (onApplied != null) {
			onApplied.accept(template);
		}
	}

	private void unbind() {
		progressBar.progressProperty().unbind();
		statusLabel.textProperty().unbind();
	}

	private void setBusy(boolean busy) {
		progressBar.setVisible(busy);
		generateButton.setDisable(busy);
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

}
