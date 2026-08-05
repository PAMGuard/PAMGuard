package noiseBandMonitor.layoutFX;

import java.util.ArrayList;

import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamUtils.FrequencyFormat;
import PamguardMVC.PamDataBlock;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import noiseBandMonitor.BandAnalyser;
import noiseBandMonitor.BandPerformance;
import noiseBandMonitor.BandType;
import noiseBandMonitor.DecimatorMethod;
import noiseBandMonitor.NoiseBandControl;
import noiseBandMonitor.NoiseBandSettings;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import pamViewFX.validator.PamValidator;

/**
 * JavaFX settings pane for the Noise Band Monitor, replicating
 * the Swing NoiseBandDialog.
 * <p>
 * The pane is split horizontally:
 * <ul>
 *   <li><b>Left</b> – tabbed settings (source, output, bands, filters) and
 *       a band property table.</li>
 *   <li><b>Right</b> – an interactive Bode plot showing the combined filter
 *       bank frequency response, decimator responses and optional ANSI
 *       standard overlays.</li>
 * </ul>
 *
 * @author PAMGuard
 */
public class NoiseBandSettingsPane extends SettingsPane<NoiseBandSettings> {

	/** Root node. */
	private PamBorderPane mainPane;

	/** Reference to the controller. */
	private NoiseBandControl noiseBandControl;

	// --- source ---
	private SourcePaneFX sourcePane;

	// --- output ---
	private TextField outputIntervalField;

	// --- measurement bands ---
	private ComboBox<BandType> bandTypeCombo;
	private TextField refFrequencyField;
	private TextField maxFrequencyField;
	private TextField minFrequencyField;

	// --- filters ---
	private ComboBox<String> filterTypeCombo;
	private Spinner<Integer> filterOrderSpinner;
	private TextField filterGammaField;

	// --- table ---
	private TableView<BandPerformance> bandTable;
	private ObservableList<BandPerformance> bandTableData;

	// --- Bode plot ---
	private BodePlotPaneFX bodePlotPane;

	/** Validator. */
	private PamValidator validator;

	/** Working copy of settings while the pane is open. */
	private NoiseBandSettings workingSettings;

	/** Most recent analyser – kept so the plot and table stay in sync. */
	private BandAnalyser lastAnalyser;

	public NoiseBandSettingsPane(NoiseBandControl noiseBandControl) {
		super(null);
		this.noiseBandControl = noiseBandControl;
		validator = new PamValidator();
		mainPane = new PamBorderPane();
		mainPane.setCenter(createMainPane());
	}

	/* ------------------------------------------------------------------ */
	/*  Layout                                                             */
	/* ------------------------------------------------------------------ */

	private Node createMainPane() {
		// Left side: tabbed settings + band table
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		Tab settingsTab = new Tab("Settings", createSettingsTab());
		Tab tableTab    = new Tab("Band Table", createBandTableTab());
		tabPane.getTabs().addAll(settingsTab, tableTab);

		// Right side: Bode plot
		bodePlotPane = new BodePlotPaneFX(noiseBandControl);
		bodePlotPane.setBandSelectionListener(bandIndex -> {
			if (bandTable != null && bandIndex >= 0 && bandIndex < bandTableData.size()) {
				bandTable.getSelectionModel().select(bandIndex);
				bandTable.scrollTo(bandIndex);
			}
		});

		// SplitPane: settings left, plot right
		SplitPane split = new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		split.getItems().addAll(tabPane, bodePlotPane);
		split.setDividerPositions(0.38);
		SplitPane.setResizableWithParent(tabPane, false);

		return split;
	}

	/**
	 * Settings tab – source, output, bands, filters.
	 */
	private Node createSettingsTab() {
		PamVBox vbox = new PamVBox(10);
		vbox.setPadding(new Insets(10));

		vbox.getChildren().addAll(
				createSourceSection(),
				createOutputSection(),
				createBandSection(),
				createFilterSection()
		);

		ScrollPane scroll = new ScrollPane(vbox);
		scroll.setFitToWidth(true);
		return scroll;
	}

	/* ---------- source ---------- */

	private Node createSourceSection() {
		sourcePane = new SourcePaneFX("Raw Data Source", RawDataUnit.class, true, true);
		sourcePane.addSelectionListener((obs, oldVal, newVal) -> recalculate());
		return sourcePane;
	}

	/* ---------- output ---------- */

	private Node createOutputSection() {
		PamGridPane grid = createGrid();
		int row = 0;

		grid.add(new Label("Output Interval"), 0, row);
		outputIntervalField = new TextField();
		outputIntervalField.setPrefColumnCount(6);
		outputIntervalField.setTooltip(new Tooltip("Measurement output interval in seconds"));
		grid.add(outputIntervalField, 1, row);
		grid.add(new Label("s"), 2, row);

		validator.createCheck()
			.dependsOn("outputInterval", outputIntervalField.textProperty())
			.withMethod(c -> {
				try {
					String v = c.get("outputInterval");
					if (v == null || v.trim().isEmpty() || Integer.parseInt(v.trim()) <= 0) {
						c.error("Output interval must be a positive integer (seconds)");
					}
				} catch (NumberFormatException e) {
					c.error("Output interval must be a valid integer");
				}
			})
			.decorates(outputIntervalField)
			.immediate();

		PamVBox wrapper = wrapSection("Output", grid);
		return wrapper;
	}

	/* ---------- measurement bands ---------- */

	private Node createBandSection() {
		PamGridPane grid = createGrid();
		int row = 0;

		// Band type
		grid.add(new Label("Band Type"), 0, row);
		bandTypeCombo = new ComboBox<>();
		bandTypeCombo.getItems().addAll(BandType.values());
		bandTypeCombo.setMaxWidth(Double.MAX_VALUE);
		bandTypeCombo.setTooltip(new Tooltip("Frequency band type"));
		bandTypeCombo.setOnAction(e -> recalculate());
		grid.add(bandTypeCombo, 1, row);

		// Reference frequency
		row++;
		grid.add(new Label("Reference Frequency"), 0, row);
		refFrequencyField = new TextField();
		refFrequencyField.setPrefColumnCount(8);
		refFrequencyField.setTooltip(new Tooltip(
				"Reference centre frequency – other bands are calculated relative to this. Default 1000 Hz"));
		refFrequencyField.setOnAction(e -> recalculate());
		grid.add(refFrequencyField, 1, row);

		PamButton defRefButton = new PamButton("Default");
		defRefButton.setTooltip(new Tooltip("Reset to default 1000 Hz"));
		defRefButton.setOnAction(e -> {
			refFrequencyField.setText("1000.0");
			recalculate();
		});
		grid.add(defRefButton, 2, row);

		validator.createCheck()
			.dependsOn("refFreq", refFrequencyField.textProperty())
			.withMethod(c -> {
				try {
					String v = c.get("refFreq");
					if (v == null || v.trim().isEmpty() || Double.parseDouble(v.trim()) <= 0) {
						c.error("Reference frequency must be a positive number (Hz)");
					}
				} catch (NumberFormatException e) {
					c.error("Reference frequency must be a valid number");
				}
			})
			.decorates(refFrequencyField)
			.immediate();

		// Max frequency
		row++;
		grid.add(new Label("Maximum Frequency"), 0, row);
		maxFrequencyField = new TextField();
		maxFrequencyField.setPrefColumnCount(8);
		maxFrequencyField.setTooltip(new Tooltip("Maximum frequency of upper edge of highest band"));
		maxFrequencyField.setOnAction(e -> recalculate());
		grid.add(maxFrequencyField, 1, row);

		PamButton maxButton = new PamButton("Max");
		maxButton.setTooltip(new Tooltip("Set to Nyquist frequency of selected source"));
		maxButton.setOnAction(e -> {
			float fs = getSampleRate();
			maxFrequencyField.setText(String.format("%.1f", fs / 2.0));
			recalculate();
		});
		grid.add(maxButton, 2, row);

		validator.createCheck()
			.dependsOn("maxFreq", maxFrequencyField.textProperty())
			.withMethod(c -> {
				try {
					String v = c.get("maxFreq");
					if (v == null || v.trim().isEmpty() || Double.parseDouble(v.trim()) <= 0) {
						c.error("Maximum frequency must be a positive number (Hz)");
					}
				} catch (NumberFormatException e) {
					c.error("Maximum frequency must be a valid number");
				}
			})
			.decorates(maxFrequencyField)
			.immediate();

		// Min frequency
		row++;
		grid.add(new Label("Minimum Frequency"), 0, row);
		minFrequencyField = new TextField();
		minFrequencyField.setPrefColumnCount(8);
		minFrequencyField.setTooltip(new Tooltip("Minimum frequency of lower edge of lowest band"));
		minFrequencyField.setOnAction(e -> recalculate());
		grid.add(minFrequencyField, 1, row);

		grid.add(new Label("Hz"), 2, row);

		validator.createCheck()
			.dependsOn("minFreq", minFrequencyField.textProperty())
			.withMethod(c -> {
				try {
					String v = c.get("minFreq");
					if (v == null || v.trim().isEmpty() || Double.parseDouble(v.trim()) <= 0) {
						c.error("Minimum frequency must be a positive number (Hz)");
					}
				} catch (NumberFormatException e) {
					c.error("Minimum frequency must be a valid number");
				}
			})
			.decorates(minFrequencyField)
			.immediate();

		PamVBox wrapper = wrapSection("Measurement Bands", grid);
		return wrapper;
	}

	/* ---------- filters ---------- */

	private Node createFilterSection() {
		PamGridPane grid = createGrid();
		int row = 0;

		grid.add(new Label("Filter Type"), 0, row);
		filterTypeCombo = new ComboBox<>();
		filterTypeCombo.getItems().addAll("Butterworth", "FIR Filter");
		filterTypeCombo.setMaxWidth(Double.MAX_VALUE);
		filterTypeCombo.setOnAction(e -> {
			enableFilterControls();
			recalculate();
		});
		grid.add(filterTypeCombo, 1, row);

		row++;
		grid.add(new Label("Filter Order"), 0, row);
		filterOrderSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 20, 6, 1));
		filterOrderSpinner.setEditable(true);
		filterOrderSpinner.setPrefWidth(100);
		filterOrderSpinner.valueProperty().addListener((obs, o, n) -> recalculate());
		grid.add(filterOrderSpinner, 1, row);

		row++;
		grid.add(new Label("Filter Gamma"), 0, row);
		filterGammaField = new TextField();
		filterGammaField.setPrefColumnCount(6);
		filterGammaField.setTooltip(new Tooltip("Gamma parameter for FIR filter"));
		grid.add(filterGammaField, 1, row);

		PamVBox wrapper = wrapSection("Filters", grid);
		return wrapper;
	}

	/* ---------- band table ---------- */

	@SuppressWarnings("unchecked")
	private Node createBandTableTab() {
		bandTableData = FXCollections.observableArrayList();
		bandTable = new TableView<>(bandTableData);
		bandTable.setPlaceholder(new Label("Configure settings to see band properties"));

		TableColumn<BandPerformance, String> loCol = new TableColumn<>("Lo Freq");
		loCol.setCellValueFactory(cell -> {
			FilterParams fp = cell.getValue().getFilterMethod().getFilterParams();
			return new SimpleStringProperty(FrequencyFormat.formatFrequency(fp.highPassFreq, true));
		});
		loCol.setPrefWidth(120);

		TableColumn<BandPerformance, String> centreCol = new TableColumn<>("Centre");
		centreCol.setCellValueFactory(cell -> {
			FilterParams fp = cell.getValue().getFilterMethod().getFilterParams();
			return new SimpleStringProperty(FrequencyFormat.formatFrequency(fp.getCenterFreq(), true));
		});
		centreCol.setPrefWidth(120);

		TableColumn<BandPerformance, String> hiCol = new TableColumn<>("Hi Freq");
		hiCol.setCellValueFactory(cell -> {
			FilterParams fp = cell.getValue().getFilterMethod().getFilterParams();
			return new SimpleStringProperty(FrequencyFormat.formatFrequency(fp.lowPassFreq, true));
		});
		hiCol.setPrefWidth(120);

		TableColumn<BandPerformance, String> responseCol = new TableColumn<>("Response");
		responseCol.setCellValueFactory(cell -> {
			return new SimpleStringProperty(String.format("%.2f dB", cell.getValue().getFilterIntegratedResponse()));
		});
		responseCol.setPrefWidth(100);

		bandTable.getColumns().addAll(loCol, centreCol, hiCol, responseCol);

		// selecting a row in the table highlights it on the Bode plot
		bandTable.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
			if (idx != null && bodePlotPane != null) {
				bodePlotPane.setSelectedBand(idx.intValue());
			}
		});

		PamBorderPane pane = new PamBorderPane();
		pane.setPadding(new Insets(10));
		pane.setCenter(bandTable);
		return pane;
	}

	/* ------------------------------------------------------------------ */
	/*  Helpers                                                            */
	/* ------------------------------------------------------------------ */

	private PamGridPane createGrid() {
		PamGridPane grid = new PamGridPane();
		grid.setHgap(5);
		grid.setVgap(5);

		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHgrow(Priority.NEVER);
		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setHgrow(Priority.ALWAYS);
		ColumnConstraints extraCol = new ColumnConstraints();
		extraCol.setHgrow(Priority.NEVER);
		grid.getColumnConstraints().addAll(labelCol, fieldCol, extraCol);

		return grid;
	}

	private PamVBox wrapSection(String title, Node content) {
		PamVBox wrapper = new PamVBox(5);
		wrapper.setPadding(new Insets(5));
		Label label = new Label(title);
		label.setStyle("-fx-font-weight: bold;");
		wrapper.getChildren().addAll(label, content);
		return wrapper;
	}

	private void enableFilterControls() {
		boolean isFIR = filterTypeCombo.getSelectionModel().getSelectedIndex() == 1;
		filterGammaField.setDisable(!isFIR);
	}

	private float getSampleRate() {
		PamDataBlock src = sourcePane.getSource();
		if (src != null) {
			return src.getSampleRate();
		}
		return 1.f;
	}

	/**
	 * Read current GUI values into workingSettings, recalculate the bands,
	 * refresh the table and repaint the Bode plot.
	 */
	private void recalculate() {
		if (workingSettings == null) {
			return;
		}
		// silently read current values
		try {
			readFieldsIntoSettings(workingSettings);
		} catch (Exception e) {
			// ignore – user may still be typing
			return;
		}
		try {
			float sr = getSampleRate();
			lastAnalyser = new BandAnalyser(noiseBandControl, sr, workingSettings);
			BandPerformance[] perfs = lastAnalyser.calculatePerformance();

			// update table
			bandTableData.clear();
			if (perfs != null) {
				bandTableData.addAll(perfs);
			}

			// build decimator / band filter lists for the plot
			ArrayList<DecimatorMethod> decFilters = lastAnalyser.getDecimationFilters();
			ArrayList<FilterMethod> bFilters = lastAnalyser.getBandFilters();
			int[] decIndexes = noiseBandControl.getDecimatorIndexes();

			// update Bode plot
			bodePlotPane.update(workingSettings, lastAnalyser,
					decFilters, bFilters, decIndexes, sr);

		} catch (Exception e) {
			// computation may fail for extreme parameter ranges – just clear
			bandTableData.clear();
		}
	}

	/**
	 * Read GUI fields into the supplied settings object. Throws on parse errors.
	 */
	private void readFieldsIntoSettings(NoiseBandSettings settings) {
		settings.bandType = bandTypeCombo.getValue();
		settings.filterType = getSelectedFilterType();
		switch (settings.filterType) {
		case BUTTERWORTH:
			settings.iirOrder = filterOrderSpinner.getValue();
			break;
		case FIRWINDOW:
			settings.firOrder = filterOrderSpinner.getValue();
			settings.firGamma = Double.parseDouble(filterGammaField.getText().trim());
			break;
		default:
			break;
		}
		settings.setReferenceFrequency(Double.parseDouble(refFrequencyField.getText().trim()));
		settings.setMinFrequency(Double.parseDouble(minFrequencyField.getText().trim()));
		settings.setMaxFrequency(Double.parseDouble(maxFrequencyField.getText().trim()));
		settings.outputIntervalSeconds = Integer.parseInt(outputIntervalField.getText().trim());

		PamDataBlock src = sourcePane.getSource();
		if (src != null) {
			settings.rawDataSource = src.getLongDataName();
			settings.channelMap = sourcePane.getChannelList();
		}
	}

	private FilterType getSelectedFilterType() {
		return filterTypeCombo.getSelectionModel().getSelectedIndex() == 0
				? FilterType.BUTTERWORTH : FilterType.FIRWINDOW;
	}

	/* ------------------------------------------------------------------ */
	/*  SettingsPane contract                                              */
	/* ------------------------------------------------------------------ */

	@Override
	public NoiseBandSettings getParams(NoiseBandSettings currParams) {
		if (currParams == null) {
			currParams = noiseBandControl.getNoiseBandSettings();
		}

		// validate
		if (validator.containsErrors()) {
			String content = PamValidator.list2String(validator.getValidationResult().getMessages());
			PamDialogFX.showWarning(null, "Noise Band Settings",
					"Some required fields are not valid. Please correct the "
					+ "highlighted fields before closing the dialog:\n\n" + content);
			return null;
		}

		// read into the settings
		NoiseBandSettings result = currParams.clone();
		try {
			readFieldsIntoSettings(result);
		} catch (Exception e) {
			PamDialogFX.showWarning("Unable to read settings: " + e.getMessage());
			return null;
		}

		// write display options from the Bode plot checkboxes
		bodePlotPane.writeDisplayOptions(result);

		// extra validations
		PamDataBlock src = sourcePane.getSource();
		if (src == null) {
			PamDialogFX.showWarning(null, "Noise Band Settings",
					"You must select a source of raw audio data");
			return null;
		}
		if (result.channelMap == 0) {
			PamDialogFX.showWarning(null, "Noise Band Settings",
					"You must select at least one data channel");
			return null;
		}
		if (result.getMinFrequency() >= result.getMaxFrequency()) {
			PamDialogFX.showWarning(null, "Noise Band Settings",
					"The minimum frequency must be lower than the maximum frequency");
			return null;
		}
		if (result.filterType == FilterType.BUTTERWORTH && result.iirOrder < 4) {
			PamDialogFX.showWarning(null, "Noise Band Settings",
					"The IIR filter order should be at least 4 for accurate measurement");
		}
		if (result.filterType == FilterType.BUTTERWORTH && result.iirOrder % 2 == 1) {
			PamDialogFX.showWarning(null, "Noise Band Settings",
					"The IIR filter order must be even (6 or greater recommended)");
			return null;
		}

		return result;
	}

	@Override
	public void setParams(NoiseBandSettings input) {
		workingSettings = input.clone();

		// source
		sourcePane.setSourceList();
		sourcePane.setSource(input.rawDataSource);
		sourcePane.setChannelList(input.channelMap);

		// output
		outputIntervalField.setText(String.valueOf(input.outputIntervalSeconds));

		// bands
		bandTypeCombo.setValue(input.bandType);
		refFrequencyField.setText(String.format("%.1f", input.getReferenceFrequency()));
		maxFrequencyField.setText(String.format("%.1f", input.getMaxFrequency()));
		minFrequencyField.setText(String.format("%.1f", input.getMinFrequency()));

		// filters
		filterTypeCombo.getSelectionModel().select(
				input.filterType == FilterType.BUTTERWORTH ? 0 : 1);
		filterOrderSpinner.getValueFactory().setValue(
				input.filterType == FilterType.BUTTERWORTH ? input.iirOrder : input.firOrder);
		filterGammaField.setText(String.valueOf(input.firGamma));

		enableFilterControls();
		validator.validate();
		recalculate();
	}

	@Override
	public String getName() {
		return "Noise Band Monitor";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// nothing needed
	}
}
