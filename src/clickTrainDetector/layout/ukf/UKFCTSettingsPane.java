package clickTrainDetector.layout.ukf;

import PamController.SettingsPane;
import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.RawDataHolder;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.ukf.AffinityNN;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFParams;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFTracker;
import clickTrainDetector.clickTrainAlgorithms.ukf.DefaultUKFParams;
import clickTrainDetector.clickTrainAlgorithms.ukf.DefaultUKFParams.DefaultUKFSpecies;
import clickTrainDetector.layout.mht.BearingJumpPane;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.controlsfx.control.PopOver;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxGlyphs.PamSVGIcon;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.flipPane.PamFlipPane;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.validator.PamValidator;

/**
 * Settings pane for the UKF click train algorithm.
 *
 * @author Jamie Macaulay
 */
public class UKFCTSettingsPane extends SettingsPane<UKFParams> {

	private final PamBorderPane mainPane;

	private final UKFClickTrainAlgorithm ukfClickTrainAlgorithm;

	private PamToggleSwitch useAmplitude;
	private PamToggleSwitch useBearing;
	private PamToggleSwitch useCorrelation;
	private PamToggleSwitch usePeakFreq;

	/** Reusable controls for the maximum bearing jump cutoff and its direction. */
	private BearingJumpPane bearingJumpPane;

	/** Popover holding {@link #bearingJumpPane}, opened by the bearing settings button. */
	private PopOver bearingPopOver;
	private PamSpinner<Double> maxICISpinner;
	private PamSpinner<Double> frameDurationSpinner;
	private PamSpinner<Integer> maxCoastSpinner;
	private PamSpinner<Integer> minLengthSpinner;
	private PamSpinner<Integer> confirmHitsSpinner;
	private PamSpinner<Double> confidenceAmplitudeSpinner;
	private PamSpinner<Integer> nScanDepthSpinner;
	private PamSpinner<Integer> beamWidthSpinner;
	private PamSpinner<Integer> nScanKBestSpinner;
	private PamSpinner<Double> nScanCoastCostSpinner;
	private PamSpinner<Double> nScanNewTrackCostSpinner;
	private PamToggleSwitch useCustomAffinity;
	private TextField affinityFileField;
	private Label affinityInfoLabel;
	private PamButton affinityBrowseButton;
	private PamButton trainNetworkButton;
	private PamFlipPane flipPane;
	private UKFAffinityTrainPane trainPane;

	private PamValidator validator = new PamValidator();

	/**
	 * Pane for the multi-hypothesis (N-scan) association settings.
	 */
	private Pane nScanePane;

	/**
	 * Popover for the multi-hypothesis (N-scan) association settings. Created on
	 * first use.
	 */
	private PopOver popOver;

	private PamToggleSwitch useMultiHypothesis;

	private PamVBox ampltudeThreshBox;

	private PopOver ampPopOver;

	/** Width (pixels) used for small square icon-only buttons (e.g. settings/browse buttons). */
	private static final double ICON_BUTTON_WIDTH = 30;

	private static final String TIP_AMPLITUDE = "Track amplitude consistency (in addition to ICI).";
	private static final String TIP_BEARING =
			"Track bearing consistency. Available only for multi-channel data that provides a bearing.";
	private static final String TIP_CORRELATION =
			"Use waveform cross-correlation (similarity between a click and the track's most recent click)\n"
					+ "as an association feature. Only a trained affinity network uses it (the default network\n"
					+ "ignores it). More discriminating but more processor intensive. Needs waveform data.";
	private static final String TIP_PEAKFREQ =
			"Use peak-frequency consistency (difference from the track's most recent click) as an\n"
					+ "association feature. Only a trained affinity network uses it. Needs waveform (or CPOD) data.";
	private static final String TIP_MAXICI =
			"The absolute maximum inter-click interval allowed within a click train (seconds).";
	private static final String TIP_FRAME =
			"Duration of the detection batching window. Clicks within a window are jointly assigned to\n"
					+ "tracks by the Hungarian algorithm. Use a value below the shortest expected ICI.";
	private static final String TIP_MAXCOAST =
			"Maximum number of consecutive missed clicks (coasts) before a track is closed.";
	private static final String TIP_MINLENGTH = "Minimum number of clicks for a track to be saved.";
	private static final String TIP_CONFIRM =
			"Number of clicks a track must gather before it is 'confirmed'. Until then it is tentative\n"
					+ "and is only offered clicks after every confirmed track has claimed one, so a new (possibly\n"
					+ "spurious) track cannot steal a click from an established train. 2 to the minimum train length.";
	private static final String TIP_NSCAN_DEPTH =
			"Multi-hypothesis association deferral, in frames. 0 uses greedy single-frame association\n"
					+ "(each frame's assignment is committed immediately). A positive value keeps the best few\n"
					+ "global assignments for this many frames before committing, so an ambiguous association\n"
					+ "(e.g. two trains crossing) can be corrected by later evidence. 4-8 is a reasonable range.";
	private static final String TIP_BEAMWIDTH =
			"Number of competing global hypotheses kept in the N-scan search. Larger explores more\n"
					+ "association possibilities at proportionally more processing cost.";
	private static final String TIP_NSCAN_KBEST =
			"Number of best assignments (Murty k-best) generated per hypothesis per frame in the N-scan\n"
					+ "search. Larger considers more alternatives at more processing cost.";
	private static final String TIP_NSCAN_COAST =
			"Cost (nats) charged in the N-scan search when an active, currently-due track is left without\n"
					+ "a click in a frame. Should be cheaper than accepting a clearly wrong match.";
	private static final String TIP_NSCAN_NEWTRACK =
			"Cost (nats) charged in the N-scan search when a loud click is not associated to any existing\n"
					+ "track and instead starts a new one.";
	private static final String TIP_CONFAMP =
			"Amplitude (dB) threshold for two-stage (ByteTrack) association: loud clicks at or above it\n"
					+ "associate first and may start new click trains; quieter clicks can only join existing\n"
					+ "trains. Set to 0 (the default) to disable and treat all clicks the same.";
	private static final String TIP_CUSTOM_AFFINITY =
			"Use a custom (trained) affinity network instead of the built-in default. The network\n"
					+ "scores how well a detection matches a track during association. Provide a JSON file\n"
					+ "listing the network's 'weights' and 'biases'. If unset, the default Gaussian-gating\n"
					+ "network is used.";

	public UKFCTSettingsPane(UKFClickTrainAlgorithm ukfClickTrainAlgorithm) {
		super(null);
		this.ukfClickTrainAlgorithm = ukfClickTrainAlgorithm;
		mainPane = new PamBorderPane();

		// front = the settings (which include the "Train affinity network…" button);
		// back = the training pane. Flipping is triggered by that button.
		Pane front = createPane();
		trainPane = new UKFAffinityTrainPane(ukfClickTrainAlgorithm, this::onNetworkTrained);

		flipPane = new PamFlipPane();
		flipPane.getAdvLabel().setText("Train Affinity Network");
		flipPane.setFrontContent(front);
		flipPane.setAdvPaneContent(trainPane.getNode());

		trainNetworkButton.setOnAction(e -> {
			trainPane.refresh();
			flipPane.flipToBack();
		});

		mainPane.setCenter(flipPane);
	}

	/**
	 * Called when training has produced and saved a network file: point the custom
	 * affinity controls at it and flip back to the settings.
	 */
	private void onNetworkTrained(java.io.File file) {
		useCustomAffinity.setSelected(true);
		affinityFileField.setText(file.getAbsolutePath());
		enableAffinityControls(true);
		flipPane.flipToFront();
	}

	private Pane createPane() {
		Label title = new Label("UKF Detector Settings");
		PamGuiManagerFX.titleFont2style(title);

		PamGridPane grid = new PamGridPane();
		grid.setHgap(5);
		grid.setVgap(5);
		int row = 0;
		int col = 0;
		
		frameDurationSpinner = doubleSpinner(0.0000, Double.MAX_VALUE, 0.02, 0.0001);
		frameDurationSpinner.setTooltip(new Tooltip(TIP_FRAME));
		validator.createCheck()
				.dependsOn("frameDuration", frameDurationSpinner.valueProperty())
				.withMethod(c -> {
					Double val = c.get("frameDuration");
					if (val == null || val <= 0.0) {
						c.error("Frame window must be greater than 0.");
					}
				})
				.decorates(frameDurationSpinner)
				.immediate();
		grid.add(tipLabel("Frame window", TIP_FRAME), col, row);
		grid.add(frameDurationSpinner, ++col, row);
		grid.add(new Label("s"), ++col, row);

		maxCoastSpinner = intSpinner(1, 100, 4);
		maxCoastSpinner.setTooltip(new Tooltip(TIP_MAXCOAST));
		grid.add(tipLabel("Max. coasts", TIP_MAXCOAST), ++col, row);
		grid.add(maxCoastSpinner, ++col, row++);

		col = 0; 
		
		confirmHitsSpinner = intSpinner(2, 100, 3);
		confirmHitsSpinner.setTooltip(new Tooltip(TIP_CONFIRM));
		grid.add(tipLabel("Confirm after", TIP_CONFIRM), col, row);
		grid.add(confirmHitsSpinner, ++col, row);
		grid.add(new Label("clicks"), ++col, row);
		
		minLengthSpinner = intSpinner(3, 100, 3);
		minLengthSpinner.setTooltip(new Tooltip(TIP_MINLENGTH));
		grid.add(tipLabel("Min. train length", TIP_MINLENGTH), ++col, row);
		grid.add(minLengthSpinner, ++col, row++);
		 ++col;
		 

		
		nScanePane = createNScanPane(); 

		PamButton multiHypothesisSettings = new PamButton();
		multiHypothesisSettings.getStyleClass().add("icon-button");
		multiHypothesisSettings.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		multiHypothesisSettings.setOnAction((action)->{
			showMultiHypothesisPane(multiHypothesisSettings);
		});
		
		useMultiHypothesis = new PamToggleSwitch("Multi hypothesis association");
		useMultiHypothesis.setTooltip(new Tooltip(
				"Use a fixed-lag multi-hypothesis (N-scan) association search instead of the default\n"
						+ "single-frame greedy nearest-neighbour association. Deferring the decision for a few\n"
						+ "frames lets an ambiguous association (e.g. two trains crossing) be corrected by later\n"
						+ "evidence, at the cost of more processing. Configure via the settings button."));
		useMultiHypothesis.selectedProperty().addListener((o, ov, nv) -> {
			multiHypothesisSettings.setDisable(!nv);
		});
		multiHypothesisSettings.setDisable(!useMultiHypothesis.isSelected());

		grid.add(useMultiHypothesis,0, row);
		GridPane.setColumnSpan(useMultiHypothesis, 4);
		grid.add(multiHypothesisSettings,4, row++);
		multiHypothesisSettings.setMaxWidth(30);
		PamGridPane.setHalignment(multiHypothesisSettings, HPos.RIGHT);
		
		col=0;
		Label title2 = new Label("Click Train Association Settings");
		PamGuiManagerFX.titleFont2style(title2);
		grid.add(title2,col, row++);
		GridPane.setColumnSpan(title2, 4);
		

		Label label = new Label("Inter Click Interval");
		label.setFont(Font.font(null, FontWeight.BOLD, 11));
		grid.add(label,col, row++);
		GridPane.setColumnSpan(title2, 4);
		
		
		maxICISpinner = doubleSpinner(0.0, Double.MAX_VALUE, 0.4, 0.01);
		maxICISpinner.setTooltip(new Tooltip(TIP_MAXICI));
		grid.add(new Label("Max. ICI") ,col, row);
		grid.add(maxICISpinner,++col, row);
		grid.add(new Label("s"),++col, row++);
		
		//add the feature pane to the grid pane. 
		row = createFeaturePane(grid, row);
	
		row++;

		row = createAffinityPane(grid, row); 

		
	
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);
		
		holder.getChildren().addAll(title, grid, 
				createDefaultSpeciesPane());
		return holder;
	}

	private void showMultiHypothesisPane(Node node) {
		if (popOver==null) {
			popOver = new PopOver(); 
			nScanePane.setPadding(new Insets(5,5,5,5));
			popOver.setContentNode(nScanePane);
		}
		
		popOver.show(node);
	}

	/**
	 * Create the collapsible advanced pane for multi-hypothesis (N-scan) association.
	 * Collapsed by default; the sub-controls are disabled when the deferral depth is
	 * 0 (greedy association).
	 */
	private Pane createNScanPane() {
		PamGridPane nScanPane = new PamGridPane();
		nScanPane.setHgap(5);
		nScanPane.setVgap(8);
		int row = 0;

		nScanDepthSpinner = intSpinner(0, 100, 7);
		nScanDepthSpinner.setTooltip(new Tooltip(TIP_NSCAN_DEPTH));
		nScanPane.add(tipLabel("N-scan depth", TIP_NSCAN_DEPTH), 0, row);
		nScanPane.add(withUnit(nScanDepthSpinner, "frames"), 1, row++);

		beamWidthSpinner = intSpinner(1, 50, 3);
		beamWidthSpinner.setTooltip(new Tooltip(TIP_BEAMWIDTH));
		nScanPane.add(tipLabel("Beam width", TIP_BEAMWIDTH), 0, row);
		nScanPane.add(beamWidthSpinner, 1, row++);

		nScanKBestSpinner = intSpinner(1, 20, 3);
		nScanKBestSpinner.setTooltip(new Tooltip(TIP_NSCAN_KBEST));
		nScanPane.add(tipLabel("Assignments (k-best)", TIP_NSCAN_KBEST), 0, row);
		nScanPane.add(nScanKBestSpinner, 1, row++);

		nScanCoastCostSpinner = doubleSpinner(0.0, Double.MAX_VALUE, 1.5, 0.5);
		nScanCoastCostSpinner.setTooltip(new Tooltip(TIP_NSCAN_COAST));
		nScanPane.add(tipLabel("Coast cost", TIP_NSCAN_COAST), 0, row);
		nScanPane.add(withUnit(nScanCoastCostSpinner, "nats"), 1, row++);

		nScanNewTrackCostSpinner = doubleSpinner(0.0, Double.MAX_VALUE, 2.5, 0.5);
		nScanNewTrackCostSpinner.setTooltip(new Tooltip(TIP_NSCAN_NEWTRACK));
		nScanPane.add(tipLabel("New-track cost", TIP_NSCAN_NEWTRACK), 0, row);
		nScanPane.add(withUnit(nScanNewTrackCostSpinner, "nats"), 1, row++);

		// the beam sub-controls only matter when deferral is on.
		nScanDepthSpinner.valueProperty().addListener((o, ov, nv) -> setNScanControlsDisabled(nv == null || nv == 0));

		PamBorderPane nScanBorderPane = new PamBorderPane();
		nScanBorderPane.setCenter(nScanPane);
		
		return nScanBorderPane;
	}

	private void setNScanControlsDisabled(boolean disabled) {
		beamWidthSpinner.setDisable(disabled);
		nScanKBestSpinner.setDisable(disabled);
		nScanCoastCostSpinner.setDisable(disabled);
		nScanNewTrackCostSpinner.setDisable(disabled);
	}

	/**
	 * Create a pane allowing the user to load default settings, optionally tuned for
	 * a particular species.
	 * @return pane for selecting default species settings.
	 */
	private Pane createDefaultSpeciesPane() {
		MenuButton speciesChoiceBox = new MenuButton("Select Species");
		speciesChoiceBox.prefHeightProperty().bind(this.affinityFileField.heightProperty()); //bit of a hack to get correct size;
		for (DefaultUKFSpecies species : DefaultUKFSpecies.values()) {
			MenuItem menuItem = new MenuItem(DefaultUKFParams.getDefaultSpeciesName(species));
			menuItem.setOnAction(action -> setParams(DefaultUKFParams.getDefaultUKFParams(species,
					ukfClickTrainAlgorithm.getClickTrainControl().getParentDataBlock())));
			speciesChoiceBox.getItems().add(menuItem);
		}

		PamHBox box = new PamHBox();
		box.setSpacing(5);
		box.setAlignment(Pos.CENTER_RIGHT);
		box.setPadding(new Insets(5, 0, 0, 0));
		box.getChildren().addAll(new Label("Optimise for Species"), speciesChoiceBox);
		return box;
	}

	private int createAffinityPane(PamGridPane grid, int row) {
		
		int col =0; 
		Label title2a = new Label("Affinity network");
		title2a.setFont(Font.font(null, FontWeight.BOLD, 11));
		grid.add(title2a,col, row++);
		GridPane.setColumnSpan(title2a, 4);
		
		
		useCustomAffinity = new PamToggleSwitch("Use custom affinity network");
		useCustomAffinity.setTooltip(new Tooltip(TIP_CUSTOM_AFFINITY));
		useCustomAffinity.selectedProperty().addListener((o, ov, nv) -> enableAffinityControls(nv));
		grid.add(useCustomAffinity,col, row++);
		GridPane.setColumnSpan(useCustomAffinity, 4);


		affinityFileField = new TextField();
		affinityFileField.setPromptText("Path to affinity network JSON file");
		affinityFileField.setTooltip(new Tooltip(TIP_CUSTOM_AFFINITY));
//		HBox.setHgrow(affinityFileField, Priority.ALWAYS);
//		grid.add(affinityFileField,col, row);
//		GridPane.setColumnSpan(affinityFileField, 4);


		affinityBrowseButton = new PamButton("");
		// "icon-button" opts out of the FlatLaf .button 72px min-width (see pamFlatLafLight.css) -
		// without it, min-width beats max-width and the button stays ~72px wide regardless of
		// setMaxWidth/setPrefWidth below.
		affinityBrowseButton.getStyleClass().add("icon-button");
		affinityBrowseButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file", PamGuiManagerFX.iconSize));
		affinityBrowseButton.setOnAction(e -> browseForNetwork());
		affinityFileField.prefHeightProperty().bind(affinityBrowseButton.heightProperty());
		affinityBrowseButton.setPrefWidth(ICON_BUTTON_WIDTH);
		affinityBrowseButton.setMinWidth(ICON_BUTTON_WIDTH);
		affinityBrowseButton.setMaxWidth(ICON_BUTTON_WIDTH);
		
		PamHBox browseBox = new PamHBox();
		browseBox.getChildren().addAll(affinityFileField, affinityBrowseButton);
		HBox.setHgrow(affinityFileField, Priority.ALWAYS);
		HBox.setHgrow(affinityBrowseButton, Priority.NEVER);

		browseBox.setSpacing(5);
		browseBox.setAlignment(Pos.CENTER_RIGHT);
	
		
		grid.add(browseBox,col, row++);
		GridPane.setColumnSpan(browseBox, 5);


//		grid.add(affinityBrowseButton, col+4, row++);
//		affinityBrowseButton.setMaxWidth(30);
//		PamGridPane.setHalignment(affinityBrowseButton, HPos.RIGHT);
		
		// summary feedback on the network that is (or would be) used.
		affinityInfoLabel = new Label();
		affinityInfoLabel.setWrapText(true);
		affinityInfoLabel.setFont(Font.font(null, 10));
		affinityInfoLabel.setPadding(new Insets(0, 0, 0, 5));
		// refresh the summary whenever the file path changes.
		affinityFileField.textProperty().addListener((o, ov, nv) -> updateAffinityInfo());
		
		affinityFileField.prefHeightProperty().bind(affinityBrowseButton.heightProperty());
		grid.add(affinityInfoLabel,col, row++);
		GridPane.setColumnSpan(affinityInfoLabel, 5);

		// the action is wired in the constructor, once the flip pane exists.
		trainNetworkButton = new PamButton("Train affinity network…");
		
		grid.add(trainNetworkButton,0, row++);
		GridPane.setColumnSpan(trainNetworkButton, 2);
		
		try {
			PamSVGIcon iconMaker = new PamSVGIcon();
			PamSVGIcon svgIcon = iconMaker.create(getClass().getResource("/Resources/modules/noun_Deep Learning_2486374.svg").toURI().toURL());
			svgIcon.setFitWidth(PamGuiManagerFX.iconSize);
			svgIcon.setFitHeight(PamGuiManagerFX.iconSize);
			Node iconNode = svgIcon.getSpriteNode();
			// Apply the same fill colour as Ikonli icons (adapts to the active CSS theme)
			if (iconNode instanceof javafx.scene.Group) {
				for (Node child : ((javafx.scene.Group) iconNode).getChildren()) {
					child.setStyle("-fx-fill: -fx-icon_col;");
				}
			}
			trainNetworkButton.setGraphic(iconNode);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		trainNetworkButton.setTooltip(new Tooltip(
				"Train the affinity network from manually-annotated click trains, then save and use it."));		
		
		return row;
	}

	/**
	 * Update the summary label below the file field with information about the
	 * network that will be used: the built-in default, or the custom network loaded
	 * from the selected file (or an error if it cannot be loaded).
	 */
	private void updateAffinityInfo() {
		if (affinityInfoLabel == null) {
			return;
		}
		String path = affinityFileField.getText() == null ? "" : affinityFileField.getText().trim();
		if (!useCustomAffinity.isSelected() || path.isEmpty()) {
			affinityInfoLabel.setStyle("");
			affinityInfoLabel.setText("Using the built-in default (Gaussian-gating) network.");
			return;
		}
		try {
			AffinityNN network = AffinityNN.fromFile(new File(path), UKFTracker.FEATURE_DIM);
			int[] sizes = network.layerOutputSizes();
			StringBuilder arch = new StringBuilder().append(network.inputDim());
			for (int size : sizes) {
				arch.append(" → ").append(size);
			}
			affinityInfoLabel.setStyle("");
			affinityInfoLabel.setText(String.format("Loaded network: %s  (%d layer%s, %d parameters)", arch.toString(),
					network.numLayers(), network.numLayers() == 1 ? "" : "s", network.numParameters()));
		} catch (Exception e) {
			affinityInfoLabel.setStyle("-fx-text-fill: red;");
			affinityInfoLabel.setText("Could not load network: " + e.getMessage());
		}
	}

	private void browseForNetwork() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select affinity network file");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Affinity network (*.json)", "*.json"));
		if (affinityFileField.getText() != null && !affinityFileField.getText().isEmpty()) {
			File current = new File(affinityFileField.getText());
			if (current.getParentFile() != null && current.getParentFile().isDirectory()) {
				chooser.setInitialDirectory(current.getParentFile());
			}
		}
		File file = chooser.showOpenDialog(mainPane.getScene() == null ? null : mainPane.getScene().getWindow());
		if (file != null) {
			affinityFileField.setText(file.getAbsolutePath());
		}
	}

	private void enableAffinityControls(boolean useCustom) {
		affinityFileField.setDisable(!useCustom);
		affinityBrowseButton.setDisable(!useCustom);
		updateAffinityInfo();
	}

	
	/**
	 * Add controls to the main pane for selecting which features to use in the UKF association.
	 * @param mainPane - the main grid pane to add the controls to
	 * @param row - the row index to start adding controls at
	 * @return the next row index after the added controls
	 */
	private int createFeaturePane(PamGridPane mainPane, int row) {
		
		Label label = new Label("Click Features Used");
		label.setFont(Font.font(null, FontWeight.BOLD, 11));
		mainPane.add(label, 0, row++);
		GridPane.setColumnSpan(label, 5);
		
		PamToggleSwitch iciSwitch = new PamToggleSwitch("Inter-click interval (always used)");
		iciSwitch.setDisable(true);
		iciSwitch.setTooltip(new Tooltip("ICI is always tracked by the UKF."));
		
		mainPane.add(iciSwitch, 0, row++);
		GridPane.setColumnSpan(iciSwitch, 4);

		useAmplitude = new PamToggleSwitch("Amplitude");
		useAmplitude.setTooltip(new Tooltip(TIP_AMPLITUDE));
		mainPane.add(useAmplitude, 0, row);
		GridPane.setColumnSpan(useAmplitude, 4);

		confidenceAmplitudeSpinner = doubleSpinner(0.0, Double.MAX_VALUE, 0.0, 1.0);
		confidenceAmplitudeSpinner.setTooltip(new Tooltip(TIP_CONFAMP));

		PamHBox amplitudeThreshPane = new PamHBox();
		amplitudeThreshPane.setAlignment(Pos.CENTER_LEFT);
		amplitudeThreshPane.setSpacing(5);
		amplitudeThreshPane.getChildren().addAll(tipLabel("Confidence amplitude", TIP_CONFAMP), confidenceAmplitudeSpinner,
				new Label("dB"));
		
		ampltudeThreshBox = new PamVBox(); 
		ampltudeThreshBox.getChildren().add(amplitudeThreshPane); 
		
		
		PamButton confSettings = new PamButton();
		confSettings.getStyleClass().add("icon-button");
		confSettings.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		confSettings.setOnAction((action)->{
			showAmplitudePane(confSettings);
		});
		mainPane.add(confSettings, 4, row++);
		GridPane.setHalignment(confSettings, HPos.RIGHT);
		confSettings.setMaxWidth(30);

		
		useBearing = new PamToggleSwitch("Bearing");
		useBearing.setTooltip(new Tooltip(TIP_BEARING));
		mainPane.add(useBearing, 0, row);
		GridPane.setColumnSpan(useBearing, 4);
		
		
		PamButton bearingSettings = new PamButton();
		bearingSettings.getStyleClass().add("icon-button");
		bearingSettings.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		bearingSettings.setOnAction((action)->{
			showBearingPane(bearingSettings);
		});
		mainPane.add(bearingSettings, 4, row++);
		GridPane.setHalignment(bearingSettings, HPos.RIGHT);
		bearingSettings.setMaxWidth(ICON_BUTTON_WIDTH);

		// the maximum bearing jump cutoff only applies when bearing tracking is on.
		bearingJumpPane = new BearingJumpPane();
		useBearing.selectedProperty().addListener((o, ov, nv) -> bearingJumpPane.setAvailable(nv));

		useCorrelation = new PamToggleSwitch("Waveform correlation");
		useCorrelation.setTooltip(new Tooltip(TIP_CORRELATION));
		mainPane.add(useCorrelation, 0, row);
		GridPane.setColumnSpan(useCorrelation, 4);
		row++;

		usePeakFreq = new PamToggleSwitch("Peak frequency");
		usePeakFreq.setTooltip(new Tooltip(TIP_PEAKFREQ));
		mainPane.add(usePeakFreq, 0, row);
		GridPane.setColumnSpan(usePeakFreq, 4);
		row++;

		// correlation and peak frequency are only consumed by a trained affinity network.
		Label trainedNote = new Label("Correlation and peak frequency are only used by a trained affinity "
				+ "network (below); the built-in default network ignores them.");
		trainedNote.setWrapText(true);
		trainedNote.setFont(Font.font(null, 10));
		mainPane.add(trainedNote, 0, row++);
		GridPane.setColumnSpan(trainedNote, 5);

		return row;
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
	
	/* 
	 * Show the amplitude settings popover next to a given node. 
	 */
	private void showAmplitudePane(Node node) {
		if (ampPopOver == null) {
			ampPopOver = new PopOver();
			ampltudeThreshBox.setPadding(new Insets(5, 5, 5, 5));
			ampPopOver.setContentNode(ampltudeThreshBox);
		}
		ampPopOver.show(node);
	}
	
	


	private PamSpinner<Double> doubleSpinner(double min, double max, double init, double step) {
		PamSpinner<Double> spinner = new PamSpinner<>(min, max, init, step);
		// the default JavaFX converter shows only 2 decimal places, so a small value
		// (e.g. a 0.005 s frame window) displays as "0" and is then committed back to
		// the spinner as zero on de-focus.
		spinner.getValueFactory().setConverter(PamSpinner.createStringConverter(6));
		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		spinner.setEditable(true);
		spinner.setPrefWidth(80);
		return spinner;
	}

	private PamSpinner<Integer> intSpinner(int min, int max, int init) {
		PamSpinner<Integer> spinner = new PamSpinner<>(min, max, init, 1);
		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		spinner.setEditable(true);
		spinner.setPrefWidth(80);
		return spinner;
	}

	private Pane withUnit(Node control, String unit) {
		PamHBox box = new PamHBox();
		box.setAlignment(Pos.CENTER_LEFT);
		box.setSpacing(5);
		box.getChildren().addAll(control, new Label(unit));
		return box;
	}

	private Label tipLabel(String text, String tip) {
		Label label = new Label(text);
		label.setTooltip(new Tooltip(tip));
		return label;
	}

	private void updateFeatureAvailability(PamDataBlock<?> dataBlock) {
		PamDataBlock<?> block = dataBlock;
		if (block == null) {
			block = ukfClickTrainAlgorithm.getClickTrainControl().getParentDataBlock();
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
	public UKFParams getParams(UKFParams currParams) {
		validator.validate();
		if (validator.containsErrors()) {
			return null;
		}
		UKFParams params = currParams.clone();
		params.useAmplitude = useAmplitude.isSelected();
		params.useBearing = useBearing.isSelected();
		params.bearingJumpEnable = bearingJumpPane.isJumpEnabled();
		params.maxBearingJumpDeg = bearingJumpPane.getMaxJumpDeg();
		params.bearingJumpDrctn = bearingJumpPane.getJumpDirection();
		params.useCorrelation = useCorrelation.isSelected();
		params.usePeakFreq = usePeakFreq.isSelected();
		params.maxICI = maxICISpinner.getValue();
		params.frameDuration = frameDurationSpinner.getValue();
		params.maxCoast = maxCoastSpinner.getValue();
		params.minTrackLength = minLengthSpinner.getValue();
		params.confirmHits = confirmHitsSpinner.getValue();
		params.confidenceAmplitude = confidenceAmplitudeSpinner.getValue();
		params.useMultiHypothesis = useMultiHypothesis.isSelected();
		params.nScanDepth = nScanDepthSpinner.getValue();
		params.beamWidth = beamWidthSpinner.getValue();
		params.nScanKBest = nScanKBestSpinner.getValue();
		params.nScanCoastCost = nScanCoastCostSpinner.getValue();
		params.nScanNewTrackCost = nScanNewTrackCostSpinner.getValue();
		params.useCustomAffinity = useCustomAffinity.isSelected();
		params.affinityNetworkFile = affinityFileField.getText() == null || affinityFileField.getText().trim().isEmpty()
				? null
				: affinityFileField.getText().trim();
		return params;
	}

	@Override
	public void setParams(UKFParams input) {
		useAmplitude.setSelected(input.useAmplitude);
		useBearing.setSelected(input.useBearing);
		bearingJumpPane.setParams(input.bearingJumpEnable, input.maxBearingJumpDeg, input.bearingJumpDrctn);
		useCorrelation.setSelected(input.useCorrelation);
		usePeakFreq.setSelected(input.usePeakFreq);
		maxICISpinner.getValueFactory().setValue(input.maxICI);
		frameDurationSpinner.getValueFactory().setValue(input.frameDuration);
		maxCoastSpinner.getValueFactory().setValue(input.maxCoast);
		minLengthSpinner.getValueFactory().setValue(input.minTrackLength);
		confirmHitsSpinner.getValueFactory().setValue(input.confirmHits);
		confidenceAmplitudeSpinner.getValueFactory().setValue(input.confidenceAmplitude);
		useMultiHypothesis.setSelected(input.useMultiHypothesis);
		nScanDepthSpinner.getValueFactory().setValue(input.nScanDepth);
		beamWidthSpinner.getValueFactory().setValue(input.beamWidth);
		nScanKBestSpinner.getValueFactory().setValue(input.nScanKBest);
		nScanCoastCostSpinner.getValueFactory().setValue(input.nScanCoastCost);
		nScanNewTrackCostSpinner.getValueFactory().setValue(input.nScanNewTrackCost);
		setNScanControlsDisabled(input.nScanDepth == 0);
		useCustomAffinity.setSelected(input.useCustomAffinity);
		affinityFileField.setText(input.affinityNetworkFile == null ? "" : input.affinityNetworkFile);
		enableAffinityControls(input.useCustomAffinity);
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
		return "UKF Click Train Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}

}
