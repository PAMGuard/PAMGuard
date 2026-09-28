package helpFX;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * Main JavaFX help viewer window for PAMGuardFX.
 *
 * <p>The window contains a navigation tree on the left listing all available
 * help topics and a {@link MarkdownHelpPane} content pane on the right.
 * Selecting a tree entry loads the corresponding Markdown file.</p>
 *
 * <p>History navigation (back / forward) is provided so that users can follow
 * cross-page links and return to the previous page.</p>
 *
 * @author PAMGuard team
 */
public class HelpViewerFX extends Stage {

	/** Width of the navigation tree panel. */
	private static final double NAV_PREF_WIDTH = 230;

	/** Preferred width of the whole viewer window. */
	private static final double STAGE_PREF_WIDTH = 1000;

	/** Preferred height of the whole viewer window. */
	private static final double STAGE_PREF_HEIGHT = 700;

	// ---- Content pane -------------------------------------------------------
	private final MarkdownHelpPane contentPane;

	// ---- Navigation tree ----------------------------------------------------
	private final TreeView<HelpNavEntry> navTree;

	// ---- History ------------------------------------------------------------
	private final List<HelpPoint> history = new ArrayList<>();
	private int historyIndex = -1;
	private boolean navigatingHistory = false;

	// ---- Header buttons -----------------------------------------------------
	private Button backButton;
	private Button forwardButton;

	/** Toggles the dark / light theme of both the window chrome and the content. */
	private PamToggleSwitch themeToggle;

	/** Root pane; carries the {@code help-root} (and {@code dark}) style classes. */
	private VBox rootPane;

	/** All registered module help entries (drives the navigation tree). */
	private static final List<HelpNavEntry> MODULE_ENTRIES = buildModuleEntries();

	// =========================================================================
	// Constructor
	// =========================================================================

	public HelpViewerFX() {
		setTitle("PAMGuard Help");

		// Try to set the PAMGuard icon
		try {
			Image icon = new Image(
					HelpViewerFX.class.getResourceAsStream(
							"/Resources/modules/PamguardSmall.png"));
			getIcons().add(icon);
		} catch (Exception ignored) {
		}

		contentPane = new MarkdownHelpPane();
		navTree = buildNavTree();
		rootPane = buildLayout();

		Scene scene = new Scene(rootPane, STAGE_PREF_WIDTH, STAGE_PREF_HEIGHT);
		
		java.net.URL css = HelpViewerFX.class.getResource("/helpFX/helpviewer.css");
		if (css != null) {
			scene.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());	
		}

		// Dark theme by default (matching the rendered help content).
		applyTheme(true);

		setScene(scene);
		navigateToIndex();
	}

	/**
	 * Apply the dark or light theme to both the window chrome (via the
	 * {@code help-root} stylesheet) and the rendered help content.
	 *
	 * @param dark {@code true} for dark, {@code false} for light
	 */
	private void applyTheme(boolean dark) {
		if (contentPane != null) {
			contentPane.setDarkMode(dark);
		}
//		if (rootPane != null) {
//			rootPane.getStyleClass().remove("dark");
//			if (dark) {
//				rootPane.getStyleClass().add("dark");
//			}
//		}
		if (themeToggle != null) {
			themeToggle.setSelected(dark);
//			themeToggle.setGraphic(PamGlyphDude.createPamIcon(
//					dark ? "mdi2w-weather-night" : "mdi2w-weather-sunny", 16));
			themeToggle.setTooltip(new javafx.scene.control.Tooltip(
					dark ? "Switch to light theme" : "Switch to dark theme"));
		}
	}

	// =========================================================================
	// Public navigation API
	// =========================================================================

	/**
	 * Navigate to the help index / overview page.
	 */
	public void navigateToIndex() {
		navigateTo(new HelpPoint("/helpFX/index.md"));
	}

	/**
	 * Navigate to a specific {@link HelpPoint}.
	 *
	 * @param helpPoint the page and optional anchor to display
	 */
	public void navigateTo(HelpPoint helpPoint) {
		if (helpPoint == null) {
			navigateToIndex();
			return;
		}
		// Record in history
		if (!navigatingHistory) {
			// Trim any forward history when a new page is loaded
			if (historyIndex < history.size() - 1) {
				history.subList(historyIndex + 1, history.size()).clear();
			}
			history.add(helpPoint);
			historyIndex = history.size() - 1;
		}
		updateNavButtons();
		contentPane.display(helpPoint);
		// Highlight matching tree node
		selectTreeEntry(helpPoint.getMarkdownFile());
	}

	// =========================================================================
	// Layout
	// =========================================================================

	private VBox buildLayout() {
		// --- Toolbar ---
		backButton = new Button();
		backButton.getStyleClass().add("icon-button");
		backButton.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-left", 16));
		backButton.setTooltip(new javafx.scene.control.Tooltip("Back"));
		backButton.setDisable(true);
		backButton.setOnAction(e -> navigateBack());

		forwardButton = new Button();
		forwardButton.getStyleClass().add("icon-button");
		forwardButton.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-right", 16));
		forwardButton.setTooltip(new javafx.scene.control.Tooltip("Forward"));
		forwardButton.setDisable(true);
		forwardButton.setOnAction(e -> navigateForward());

		Label titleLabel = new Label("PAMGuard Help");
		titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
		HBox.setHgrow(titleLabel, Priority.ALWAYS);

		// Dark / light theme toggle (GitHub-style). Configured by applyTheme().
		themeToggle = new PamToggleSwitch("Dark Text Mode");
		themeToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> applyTheme(isSelected));

		HBox toolbar = new HBox(8, backButton, forwardButton, new Separator(Orientation.VERTICAL), titleLabel);
		toolbar.setAlignment(Pos.CENTER_LEFT);
		toolbar.setPadding(new Insets(6, 10, 6, 10));
//		toolbar.getStyleClass().add("help-toolbar");

		// --- Nav tree (left) ---
		ScrollPane navScroll = new ScrollPane(navTree);
		navScroll.setFitToWidth(true);
		navScroll.setFitToHeight(true);
		navScroll.setPrefWidth(NAV_PREF_WIDTH);
		navScroll.setMinWidth(150);

		// --- Split pane ---
		SplitPane split = new SplitPane(navScroll, contentPane);
		split.setDividerPositions(NAV_PREF_WIDTH / STAGE_PREF_WIDTH);
		VBox.setVgrow(split, Priority.ALWAYS);
		
		PamBorderPane splitBorder = new PamBorderPane();
		splitBorder.setLeft(toolbar);
		splitBorder.setRight(themeToggle);


		VBox root = new VBox(splitBorder, split);
//		root.getStyleClass().add("help-root");
		VBox.setVgrow(split, Priority.ALWAYS);
		return root;
	}

	// =========================================================================
	// Navigation tree
	// =========================================================================

	private TreeView<HelpNavEntry> buildNavTree() {
		TreeItem<HelpNavEntry> root = new TreeItem<>(
				new HelpNavEntry("PAMGuard Help", "index.md"));
		root.setExpanded(true);

		// Categories
		TreeItem<HelpNavEntry> utilities = new TreeItem<>(
				new HelpNavEntry("Utilities", null));
		TreeItem<HelpNavEntry> soundProcessing = new TreeItem<>(
				new HelpNavEntry("Sound Processing", null));
		TreeItem<HelpNavEntry> detectors = new TreeItem<>(
				new HelpNavEntry("Detectors", null));
		TreeItem<HelpNavEntry> classifiers = new TreeItem<>(
				new HelpNavEntry("Classifiers", null));
		TreeItem<HelpNavEntry> displays = new TreeItem<>(
				new HelpNavEntry("Displays", null));

		utilities.setExpanded(true);
		soundProcessing.setExpanded(true);
		detectors.setExpanded(true);
		classifiers.setExpanded(true);
		displays.setExpanded(true);

		// Populate using the shared module entry list
		for (HelpNavEntry entry : MODULE_ENTRIES) {
			TreeItem<HelpNavEntry> item = new TreeItem<>(entry);
			switch (entry.getCategory()) {
				case "utilities":     utilities.getChildren().add(item);      break;
				case "sound":         soundProcessing.getChildren().add(item); break;
				case "detector":      detectors.getChildren().add(item);       break;
				case "classifier":    classifiers.getChildren().add(item);     break;
				case "display":       displays.getChildren().add(item);        break;
				default:              root.getChildren().add(item);            break;
			}
		}

		root.getChildren().addAll(utilities, soundProcessing, detectors, classifiers, displays);

		TreeView<HelpNavEntry> tree = new TreeView<>(root);
		tree.setShowRoot(true);
		tree.setPrefWidth(NAV_PREF_WIDTH);
		tree.getSelectionModel().selectedItemProperty().addListener(
				(obs, oldItem, newItem) -> {
					if (newItem != null && newItem.getValue().getMarkdownFile() != null) {
						navigateTo(new HelpPoint(newItem.getValue().getMarkdownFile()));
					}
				});
		return tree;
	}

	/** Attempt to select the tree item whose markdown file matches the given path. */
	private void selectTreeEntry(String markdownFile) {
		selectInSubtree(navTree.getRoot(), markdownFile);
	}

	private boolean selectInSubtree(TreeItem<HelpNavEntry> item, String markdownFile) {
		if (item == null) return false;
		if (markdownFile.equals(item.getValue().getMarkdownFile())) {
			navTree.getSelectionModel().select(item);
			int idx = navTree.getRow(item);
			if (idx >= 0) navTree.scrollTo(idx);
			return true;
		}
		for (TreeItem<HelpNavEntry> child : item.getChildren()) {
			if (selectInSubtree(child, markdownFile)) return true;
		}
		return false;
	}

	// =========================================================================
	// History navigation
	// =========================================================================

	private void navigateBack() {
		if (historyIndex > 0) {
			historyIndex--;
			navigatingHistory = true;
			navigateTo(history.get(historyIndex));
			navigatingHistory = false;
		}
	}

	private void navigateForward() {
		if (historyIndex < history.size() - 1) {
			historyIndex++;
			navigatingHistory = true;
			navigateTo(history.get(historyIndex));
			navigatingHistory = false;
		}
	}

	private void updateNavButtons() {
		if (backButton != null)    backButton.setDisable(historyIndex <= 0);
		if (forwardButton != null) forwardButton.setDisable(historyIndex >= history.size() - 1);
	}

	// =========================================================================
	// Module entry list
	// =========================================================================

	/**
	 * Build the ordered list of module help entries that populate the navigation
	 * tree and provide the canonical mapping from module name to markdown file.
	 */
	private static List<HelpNavEntry> buildModuleEntries() {
		List<HelpNavEntry> list = new ArrayList<>();
		// Utilities
		list.add(new HelpNavEntry("Database",            "/generalDatabase/database_help.md",                          "utilities"));
		list.add(new HelpNavEntry("Binary Storage",      "/binaryFileStorage/binary_storage_help.md",                  "utilities"));
		// Sound processing
		list.add(new HelpNavEntry("Sound Acquisition",   "/Acquisition/sound_acquisition_help.md",                     "sound"));
		list.add(new HelpNavEntry("Sound Output",        "/soundPlayback/sound_output_help.md",                        "sound"));
		list.add(new HelpNavEntry("FFT Engine",          "/fftManager/fft_engine_help.md",                             "sound"));
		list.add(new HelpNavEntry("Filters",             "/Filters/filters_help.md",                                   "sound"));
		list.add(new HelpNavEntry("Decimator",           "/decimator/decimator_help.md",                               "sound"));
		list.add(new HelpNavEntry("Noise Band Monitor",  "/noiseBandMonitor/noise_band_monitor_help.md",               "sound"));
		// Detectors
		list.add(new HelpNavEntry("Click Detector",      "/clickDetector/click_detector_help.md",                      "detector"));
		list.add(new HelpNavEntry("Click Train Detector","/clickTrainDetector/click_train_help.md",                    "detector"));
		list.add(new HelpNavEntry("Whistle & Moan Detector","/whistlesAndMoans/whistle_moan_help.md",                  "detector"));
		list.add(new HelpNavEntry("CPOD Importer",       "/cpod/cpod_help.md",                                         "detector"));
		// Classifiers
		list.add(new HelpNavEntry("Matched Template Classifier","/matchedTemplateClassifer/matched_click_classifer_help.md","classifier"));
		list.add(new HelpNavEntry("Deep Learning Classifier",   "/rawDeepLearningClassifier/deep_learning_help.md",         "classifier"));
		return list;
	}

	/** @return the full list of registered module help entries */
	public static List<HelpNavEntry> getModuleEntries() {
		return MODULE_ENTRIES;
	}

	// =========================================================================
	// Inner class – navigation tree entry
	// =========================================================================

	/**
	 * Simple data holder for a navigation tree entry.
	 */
	public static class HelpNavEntry {
		private final String displayName;
		private final String markdownFile;
		private final String category;

		HelpNavEntry(String displayName, String markdownFile) {
			this(displayName, markdownFile, "other");
		}

		HelpNavEntry(String displayName, String markdownFile, String category) {
			this.displayName  = displayName;
			this.markdownFile = markdownFile;
			this.category     = category;
		}

		public String getDisplayName()  { return displayName; }
		public String getMarkdownFile() { return markdownFile; }
		public String getCategory()     { return category; }

		@Override
		public String toString() { return displayName; }
	}
}
