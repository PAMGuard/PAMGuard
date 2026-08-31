package clickTrainDetector.layout.adaptive;

import PamController.SettingsPane;
import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.RawDataHolder;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.adaptive.AdaptiveCTParams;
import clickTrainDetector.clickTrainAlgorithms.adaptive.AdaptiveClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.adaptive.DefaultAdaptiveParams;
import clickTrainDetector.clickTrainAlgorithms.adaptive.DefaultAdaptiveParams.DefaultAdaptiveSpecies;
import clickTrainDetector.layout.mht.BearingJumpPane;

import org.controlsfx.control.PopOver;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.validator.PamValidator;

/**
 * Settings pane for the adaptive click train algorithm. Exposes a handful of
 * intuitive parameters (maximum ICI, sensitivity, detection probability) plus a
 * set of toggles to select which features the detector uses to group clicks
 * (ICI, amplitude, bearing, waveform correlation). Features that the source data
 * cannot provide (bearing, waveform correlation) are greyed out.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveCTSettingsPane extends SettingsPane<AdaptiveCTParams> {

	private final PamBorderPane mainPane;

	private final AdaptiveClickTrainAlgorithm adaptiveClickTrainAlgorithm;

	private PamSpinner<Double> maxICISpinner;

	private Slider sensitivitySlider;

	private Slider detectionProbSlider;

	private PamToggleSwitch useICI;

	private PamToggleSwitch useAmplitude;

	private PamToggleSwitch useBearing;

	/** Reusable controls for the maximum bearing jump cutoff and its direction. */
	private BearingJumpPane bearingJumpPane;

	/** Popover holding {@link #bearingJumpPane}, opened by the bearing settings button. */
	private PopOver bearingPopOver;

	private PamToggleSwitch useCorrelation;

	private PamToggleSwitch usePeakFreq;

	private PamSpinner<Integer> maxCoastSpinner;

	private PamSpinner<Integer> nHoldSpinner;

	private PamSpinner<Integer> nPrunebackSpinner;

	private PamSpinner<Integer> nPruneBackStartSpinner;

	/** Content of the advanced (multi-hypothesis search) settings, shown in a popover. */
	private Pane advancedPane;

	/** Popover holding {@link #advancedPane}, opened by the advanced settings button. */
	private PopOver advancedPopOver;

	/** Width (pixels) used for small square icon-only buttons (e.g. settings buttons). */
	private static final double ICON_BUTTON_WIDTH = 30;

	/** Validates the advanced (multi-hypothesis) settings, e.g. prune-back vs prune-back start. */
	private final PamValidator validator = new PamValidator();

	public AdaptiveCTSettingsPane(AdaptiveClickTrainAlgorithm adaptiveClickTrainAlgorithm) {
		super(null);
		this.adaptiveClickTrainAlgorithm = adaptiveClickTrainAlgorithm;
		mainPane = new PamBorderPane();
		mainPane.setCenter(createPane());
	}

	/** Tooltip text shared between each control and its descriptive label. */
	private static final String TIP_MAXICI =
			"The absolute maximum inter-click interval (ICI) allowed within a click train.\n"
					+ "Trains whose median ICI exceeds this are rejected.";

	private static final String TIP_SENSITIVITY =
			"How strictly clicks must conform to a train. Loose (left) detects more but may merge trains;\n"
					+ "tight (right) is stricter and produces cleaner but possibly fragmented trains.";

	private static final String TIP_DETPROB =
			"The expected fraction of clicks that are detected. Lower values let the detector bridge\n"
					+ "more missed clicks (coasts); higher values keep trains tighter.";

	private static final String TIP_ICI =
			"Group clicks by the consistency of their inter-click interval. ICI is the single most\n"
					+ "reliable cue and is normally left on.";

	private static final String TIP_AMPLITUDE =
			"Group clicks by the consistency of their amplitude.";

	private static final String TIP_BEARING =
			"Group clicks by the consistency of their bearing. Available only for multi-channel data\n"
					+ "that provides a bearing.";

	private static final String TIP_CORRELATION =
			"Group clicks by waveform similarity (cross correlation), and refine the ICI measurement.\n"
					+ "More accurate but more processor intensive. Available only when waveform data is present.";

	private static final String TIP_PEAKFREQ =
			"Group clicks by the consistency of their peak frequency. The clicks of a train usually share\n"
					+ "a similar peak frequency. Available only when waveform (or CPOD) data is present.";

	private static final String TIP_MAXCOAST =
			"Maximum number of consecutive missed clicks (coasts) bridged before a train is ended.";

	private static final String TIP_NHOLD =
			"Number of candidate track hypotheses the multi-hypothesis search keeps at each step.\n"
					+ "Larger is more thorough but uses more memory and processing.";

	private static final String TIP_NPRUNEBACK =
			"How many clicks back the search looks before pruning (committing) the older part of a\n"
					+ "hypothesis. Larger defers decisions longer (more robust to ambiguity, more processing).";

	private static final String TIP_NPRUNEBACKSTART =
			"Number of clicks that must accumulate before pruning of hypotheses begins.";

	private Pane createPane() {

		Label title = new Label("Adaptive Detector Settings");
		PamGuiManagerFX.titleFont2style(title);
//		title.setFont(Font.font(null, FontWeight.BOLD, 11));

		PamGridPane grid = new PamGridPane();
		grid.setHgap(5);
		grid.setVgap(8);
		// let the grid fill the available width so the slider column can grow.
		grid.setMaxWidth(Double.MAX_VALUE);

		// column layout: label | min-label | slider (grows) | max-label | value
		ColumnConstraints labelCol = new ColumnConstraints();
		ColumnConstraints minLabelCol = new ColumnConstraints();
		ColumnConstraints sliderCol = new ColumnConstraints();
		sliderCol.setHgrow(Priority.ALWAYS);
		sliderCol.setFillWidth(true);
		ColumnConstraints maxLabelCol = new ColumnConstraints();
		ColumnConstraints valueCol = new ColumnConstraints();
		grid.getColumnConstraints().addAll(labelCol, minLabelCol, sliderCol, maxLabelCol, valueCol);

		int row = 0;

		// sensitivity
		sensitivitySlider = new Slider(0, 1, 0.5);
		sensitivitySlider.setShowTickMarks(true);
		sensitivitySlider.setMajorTickUnit(0.5);
		sensitivitySlider.setTooltip(new Tooltip(TIP_SENSITIVITY));
		addSliderRow(grid, row++, "Sensitivity", TIP_SENSITIVITY, sensitivitySlider, "loose", "tight");

		// detection probability
		detectionProbSlider = new Slider(0, 1, 0.9);
		detectionProbSlider.setShowTickMarks(true);
		detectionProbSlider.setMajorTickUnit(0.5);
		detectionProbSlider.setTooltip(new Tooltip(TIP_DETPROB));
		addSliderRow(grid, row++, "Detection prob.", TIP_DETPROB, detectionProbSlider, "lenient", "strict");

		PamVBox holder = new PamVBox();
		holder.setSpacing(8);
		holder.setPadding(new Insets(10, 0, 0, 0));
		holder.getChildren().addAll(title, grid, createAdvancedSettingsRow(), createFeaturePane(),
				new javafx.scene.control.Separator(), createDefaultSpeciesPane());

		return holder;
	}

	/**
	 * Add a labelled slider to the grid, laid out so that the sliders (and their
	 * min/max labels) align across rows and grow to fill the available width.
	 */
	private void addSliderRow(PamGridPane grid, int row, String labelText, String tip, Slider slider, String minLabel,
			String maxLabel) {
		// any value between the limits is allowed; the increment only affects
		// keyboard/scroll steps.
		slider.setBlockIncrement(0.05);
		slider.setMinorTickCount(1);
		slider.setSnapToTicks(false);
		// grow to fill the slider column.
		slider.setMaxWidth(Double.MAX_VALUE);

		Label value = new Label();
		value.setMinWidth(32);
		value.textProperty().bind(slider.valueProperty().asString("%.2f"));

		grid.add(tipLabel(labelText, tip), 0, row);
		grid.add(new Label(minLabel), 1, row);
		grid.add(slider, 2, row);
		GridPane.setHgrow(slider, Priority.ALWAYS);
		grid.add(new Label(maxLabel), 3, row);
		grid.add(value, 4, row);
	}

	/**
	 * Create the row (label + settings button) shown at the bottom of the pane that
	 * opens the advanced (multi-hypothesis search) settings in a popover. These have
	 * sensible defaults and are not usually changed, so they are tucked away.
	 */
	private Node createAdvancedSettingsRow() {
		advancedPane = createAdvancedPane();

		PamButton advancedSettings = new PamButton();
		advancedSettings.getStyleClass().add("icon-button");
		advancedSettings.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		advancedSettings.setMaxWidth(ICON_BUTTON_WIDTH);
		advancedSettings.setTooltip(new Tooltip("Advanced multi-hypothesis search settings."));
		advancedSettings.setOnAction(action -> showAdvancedPane(advancedSettings));

		Label label = new Label("Multi-hypothesis settings");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		PamHBox box = new PamHBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setSpacing(5);
		box.setPadding(new Insets(5, 0, 0, 0));
		box.getChildren().addAll(label, spacer, advancedSettings);
		return box;
	}

	/**
	 * Create the content of the advanced pane exposing the underlying multi-hypothesis
	 * (MHT) kernel search parameters. Shown in a popover from the advanced settings
	 * button.
	 */
	private Pane createAdvancedPane() {
		PamGridPane grid = new PamGridPane();
		grid.setHgap(5);
		grid.setVgap(8);
		int row = 0;

		maxCoastSpinner = intSpinner(1, 100, 4);
		maxCoastSpinner.setTooltip(new Tooltip(TIP_MAXCOAST));
		grid.add(tipLabel("Max. coasts", TIP_MAXCOAST), 0, row);
		grid.add(maxCoastSpinner, 1, row++);

		nHoldSpinner = intSpinner(1, 1000, 20);
		nHoldSpinner.setTooltip(new Tooltip(TIP_NHOLD));
		grid.add(tipLabel("Hypotheses held", TIP_NHOLD), 0, row);
		grid.add(nHoldSpinner, 1, row++);

		nPrunebackSpinner = intSpinner(1, 100, 4);
		nPrunebackSpinner.setTooltip(new Tooltip(TIP_NPRUNEBACK));
		grid.add(tipLabel("Prune-back", TIP_NPRUNEBACK), 0, row);
		grid.add(withUnit(nPrunebackSpinner, "clicks"), 1, row++);

		nPruneBackStartSpinner = intSpinner(1, 100, 5);
		nPruneBackStartSpinner.setTooltip(new Tooltip(TIP_NPRUNEBACKSTART));
		grid.add(tipLabel("Prune-back start", TIP_NPRUNEBACKSTART), 0, row);
		grid.add(withUnit(nPruneBackStartSpinner, "clicks"), 1, row++);

		// prune-back must be less than prune-back start: pruning cannot commit further
		// back than the point at which pruning is allowed to begin.
		validator.createCheck()
				.dependsOn("pruneBack", nPrunebackSpinner.valueProperty())
				.dependsOn("pruneBackStart", nPruneBackStartSpinner.valueProperty())
				.withMethod(c -> {
					Integer pruneBack = c.get("pruneBack");
					Integer pruneBackStart = c.get("pruneBackStart");
					if (pruneBack != null && pruneBackStart != null && pruneBack >= pruneBackStart) {
						c.error("Prune-back must be less than prune-back start.");
					}
				})
				.decorates(nPrunebackSpinner)
				.immediate();

		return grid;
	}

	/**
	 * Show the advanced settings popover next to the given node.
	 */
	private void showAdvancedPane(Node node) {
		if (advancedPopOver == null) {
			advancedPopOver = new PopOver();
			advancedPane.setPadding(new Insets(5, 5, 5, 5));
			advancedPopOver.setContentNode(advancedPane);
		}
		advancedPopOver.show(node);
	}

	private PamSpinner<Integer> intSpinner(int min, int max, int init) {
		PamSpinner<Integer> spinner = new PamSpinner<>(min, max, init, 1);
		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		spinner.setEditable(true);
		spinner.setPrefWidth(90);
		return spinner;
	}

	private Pane withUnit(Node control, String unit) {
		PamHBox box = new PamHBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setSpacing(5);
		box.getChildren().addAll(control, new Label(unit));
		return box;
	}

	/**
	 * Create a pane allowing the user to load default settings, optionally tuned for
	 * a particular species.
	 * @return pane for selecting default species settings.
	 */
	private Pane createDefaultSpeciesPane() {
		MenuButton speciesChoiceBox = new MenuButton("Select Species");
		for (DefaultAdaptiveSpecies species : DefaultAdaptiveSpecies.values()) {
			MenuItem menuItem = new MenuItem(DefaultAdaptiveParams.getDefaultSpeciesName(species));
			menuItem.setOnAction(action -> setParams(DefaultAdaptiveParams.getDefaultAdaptiveParams(species,
					adaptiveClickTrainAlgorithm.getClickTrainControl().getParentDataBlock())));
			speciesChoiceBox.getItems().add(menuItem);
		}

		PamHBox pamHBox = new PamHBox();
		pamHBox.setSpacing(5);
		pamHBox.setAlignment(Pos.CENTER_RIGHT);
		pamHBox.setPadding(new Insets(5, 0, 0, 0));
		pamHBox.getChildren().addAll(new Label("Optimise for Species"), speciesChoiceBox);
		return pamHBox;
	}

	/**
	 * Create the pane that lets the user pick which features the detector uses.
	 */
	private Pane createFeaturePane() {
		Label label = new Label("Click Association Settings");
		PamGuiManagerFX.titleFont2style(label);
		
		Label label1 = new Label("Inter Click Interval");
		label1.setFont(Font.font(null, FontWeight.BOLD, 11));
		
		// max ICI
		maxICISpinner = new PamSpinner<>(0.0, Double.MAX_VALUE, 0.4, 0.01);
		maxICISpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxICISpinner.setEditable(true);
		maxICISpinner.setPrefWidth(90);
		maxICISpinner.setTooltip(new Tooltip(TIP_MAXICI));
		PamHBox iciBox = new PamHBox();
		iciBox.setAlignment(Pos.CENTER_LEFT);
		iciBox.setSpacing(5);
		iciBox.getChildren().addAll(tipLabel("Max. ICI", TIP_MAXICI), maxICISpinner, new Label("s"));
		

		
		Label label2 = new Label("Click Features Used");
		label2.setFont(Font.font(null, FontWeight.BOLD, 11));
		
		label.setTooltip(new Tooltip("Select which click features are used to group clicks into trains."));

		useICI = new PamToggleSwitch("Inter-click interval (ICI)");
		useICI.setTooltip(new Tooltip(TIP_ICI));

		useAmplitude = new PamToggleSwitch("Amplitude");
		useAmplitude.setTooltip(new Tooltip(TIP_AMPLITUDE));

		useBearing = new PamToggleSwitch("Bearing");
		useBearing.setTooltip(new Tooltip(TIP_BEARING));

		// the maximum bearing jump cutoff, shown in a popover from the settings button.
		bearingJumpPane = new BearingJumpPane();
		// the jump cutoff only applies when bearing grouping is on.
		useBearing.selectedProperty().addListener((o, ov, nv) -> bearingJumpPane.setAvailable(nv));

		PamButton bearingSettings = new PamButton();
		bearingSettings.getStyleClass().add("icon-button");
		bearingSettings.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		bearingSettings.setMaxWidth(ICON_BUTTON_WIDTH);
		bearingSettings.setTooltip(new Tooltip("Bearing jump settings."));
		bearingSettings.setOnAction(action -> showBearingPane(bearingSettings));

		Region bearingSpacer = new Region();
		HBox.setHgrow(bearingSpacer, Priority.ALWAYS);
		PamHBox bearingRow = new PamHBox();
		bearingRow.setAlignment(Pos.CENTER_LEFT);
		bearingRow.setSpacing(5);
		bearingRow.getChildren().addAll(useBearing, bearingSpacer, bearingSettings);

		useCorrelation = new PamToggleSwitch("Waveform correlation");
		useCorrelation.setTooltip(new Tooltip(TIP_CORRELATION));

		usePeakFreq = new PamToggleSwitch("Peak frequency");
		usePeakFreq.setTooltip(new Tooltip(TIP_PEAKFREQ));

		PamVBox box = new PamVBox();
		box.setSpacing(5);
		box.setPadding(new Insets(5, 0, 0, 0));
		box.getChildren().addAll(label, label1, iciBox, label2, useICI, useAmplitude, bearingRow, usePeakFreq, useCorrelation);
		return box;
	}

	/**
	 * Show the bearing settings popover (the maximum bearing jump cutoff) next to the
	 * given node.
	 */
	private void showBearingPane(Node node) {
		if (bearingPopOver == null) {
			bearingPopOver = new PopOver();
			bearingJumpPane.setPadding(new Insets(5, 5, 5, 5));
			bearingPopOver.setContentNode(bearingJumpPane);
		}
		bearingPopOver.show(node);
	}

	/**
	 * Create a label with a tooltip installed, so hovering the field name shows the
	 * same help as its control.
	 */
	private Label tipLabel(String text, String tip) {
		Label label = new Label(text);
		label.setTooltip(new Tooltip(tip));
		return label;
	}

	/**
	 * Grey out features the source data cannot provide. The toggle's selected state
	 * (the user's preference) is preserved; the algorithm ignores unavailable
	 * features regardless.
	 *
	 * @param dataBlock - the current source data block, or null to query the
	 *                  algorithm's current parent.
	 */
	private void updateFeatureAvailability(PamDataBlock<?> dataBlock) {
		PamDataBlock<?> block = dataBlock;
		if (block == null) {
			block = adaptiveClickTrainAlgorithm.getClickTrainControl().getParentDataBlock();
		}

		boolean bearingAvailable = block != null && block.getLocalisationContents() != null
				&& block.getLocalisationContents().hasLocContent(LocContents.HAS_BEARING);
		boolean waveformAvailable = block != null && RawDataHolder.class.isAssignableFrom(block.getUnitClass());

		useBearing.setDisable(!bearingAvailable);
		if (bearingJumpPane != null) {
			bearingJumpPane.setAvailable(bearingAvailable && useBearing.isSelected());
		}
		useCorrelation.setDisable(!waveformAvailable);
		usePeakFreq.setDisable(!waveformAvailable);
	}

	@Override
	public AdaptiveCTParams getParams(AdaptiveCTParams currParams) {
		validator.validate();
		if (validator.containsErrors()) {
			return null;
		}
		AdaptiveCTParams params = currParams.clone();
		params.maxICI = maxICISpinner.getValue();
		params.sensitivity = sensitivitySlider.getValue();
		params.detectionProb = detectionProbSlider.getValue();
		params.useICI = useICI.isSelected();
		params.useAmplitude = useAmplitude.isSelected();
		params.useBearing = useBearing.isSelected();
		params.bearingJumpEnable = bearingJumpPane.isJumpEnabled();
		params.maxBearingJumpDeg = bearingJumpPane.getMaxJumpDeg();
		params.bearingJumpDrctn = bearingJumpPane.getJumpDirection();
		params.useCorrelation = useCorrelation.isSelected();
		params.usePeakFreq = usePeakFreq.isSelected();
		if (params.mhtKernel != null) {
			params.mhtKernel.maxCoast = maxCoastSpinner.getValue();
			params.mhtKernel.nHold = nHoldSpinner.getValue();
			params.mhtKernel.nPruneback = nPrunebackSpinner.getValue();
			params.mhtKernel.nPruneBackStart = nPruneBackStartSpinner.getValue();
		}
		return params;
	}

	@Override
	public void setParams(AdaptiveCTParams input) {
		maxICISpinner.getValueFactory().setValue(input.maxICI);
		sensitivitySlider.setValue(input.sensitivity);
		detectionProbSlider.setValue(input.detectionProb);
		useICI.setSelected(input.useICI);
		useAmplitude.setSelected(input.useAmplitude);
		useBearing.setSelected(input.useBearing);
		bearingJumpPane.setParams(input.bearingJumpEnable, input.maxBearingJumpDeg, input.bearingJumpDrctn);
		useCorrelation.setSelected(input.useCorrelation);
		usePeakFreq.setSelected(input.usePeakFreq);
		if (input.mhtKernel != null) {
			maxCoastSpinner.getValueFactory().setValue(input.mhtKernel.maxCoast);
			nHoldSpinner.getValueFactory().setValue(input.mhtKernel.nHold);
			nPrunebackSpinner.getValueFactory().setValue(input.mhtKernel.nPruneback);
			nPruneBackStartSpinner.getValueFactory().setValue(input.mhtKernel.nPruneBackStart);
		}
		updateFeatureAvailability(null);
	}

	@Override
	public void notifyChange(int flag, Object data) {
		if (flag == ClickTrainControl.NEW_PARENT_DATABLOCK) {
			updateFeatureAvailability(data instanceof PamDataBlock ? (PamDataBlock<?>) data : null);
		}
	}

	@Override
	public String getName() {
		return "Adaptive Click Train Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}

}
