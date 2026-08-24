package PamController.pamWizard.layoutFX;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import PamController.UsedModuleInfo;
import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.PamImportFileType;
import PamController.pamWizard.SoundFileSummary;
import PamController.pamWizard.configurations.ConfigApplyContext;
import PamController.pamWizard.configurations.ConfigSpeciesGroup;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamController.pamWizard.configurations.FileConfigAutoConfig;
import PamController.pamWizard.configurations.PamConfigDescription;
import PamController.pamWizard.configurations.PamConfigInspection;
import PamController.pamWizard.configurations.SpeciesIconFactory;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * The JavaFX import wizard: takes the user from a set of dropped files to a
 * working PAMGuard configuration.
 * <p>
 * The same four pages as the Swing wizard - what was imported, which
 * configuration to use, where to put the data, and a summary - built as a single
 * dialog whose content is swapped as the user moves through. The storage page is
 * skipped for configurations which have nothing to store.
 *
 * @author Jamie Macaulay
 */
public class ConfigImportWizardFX {

	private static final int ICON_SIZE = 40;

	private static final String ALL_GROUPS = "All species";

	/** Colour of a species group the selected configuration targets. */
	private static final javafx.scene.paint.Color ACTIVE_TINT = javafx.scene.paint.Color.BLACK;

	/** Colour of a species group the selected configuration does not target. */
	private static final javafx.scene.paint.Color MUTED_TINT = javafx.scene.paint.Color.rgb(180, 180, 180);

	private final ConfigWizardData wizardData;

	private final Dialog<ButtonType> dialog = new Dialog<>();

	private final StackPane pageHolder = new StackPane();

	private final Label headerLabel = new Label();

	private Button backButton;
	private Button nextButton;

	/**
	 * The pages, in order. Each returns its content and validates itself.
	 */
	private final List<WizardPage> pages = new ArrayList<>();

	private int currentPage = 0;

	/*
	 * Controls whose values are read when a page is left.
	 */
	private ListView<PamAutoConfig> configList;
	private ComboBox<Object> groupFilter;
	private ComboBox<SoundMedium> mediumChooser;
	private Label mediumLabel;
	private final java.util.Map<ConfigSpeciesGroup, ImageView> speciesIcons = new java.util.LinkedHashMap<>();
	private TextField projectField;
	private TextField binaryField;
	private TextField databaseField;
	private TextArea summaryArea;
	private Label binaryRowLabel;
	private HBox binaryRow;
	private Label databaseRowLabel;
	private HBox databaseRow;

	private ConfigImportWizardFX(ConfigWizardData wizardData) {
		this.wizardData = wizardData;
		buildDialog();
	}

	/**
	 * Show the wizard and, if the user completes it, build the configuration they
	 * chose. Must be called on the JavaFX application thread; the configuration
	 * itself is built by the caller on the Swing thread.
	 *
	 * @param wizardData what was imported and what can be built from it.
	 * @return the chosen configuration, or null if the user cancelled.
	 */
	public static PamAutoConfig showWizard(ConfigWizardData wizardData) {
		if (wizardData == null || wizardData.getAvailableConfigs().isEmpty()) {
			return null;
		}
		ConfigImportWizardFX wizard = new ConfigImportWizardFX(wizardData);
		return wizard.run();
	}

	/**
	 * Show any warnings raised while the configuration was built.
	 *
	 * @param wizardData the wizard state, after the configuration was applied.
	 */
	public static void showWarnings(ConfigWizardData wizardData) {
		List<String> warnings = wizardData.getApplyContext().getWarnings();
		if (warnings.isEmpty()) {
			return;
		}
		StringBuilder text = new StringBuilder();
		for (String warning : warnings) {
			text.append("• ").append(warning).append("\n");
		}
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Configuration created with warnings");
		alert.setHeaderText("Please check the following before you start processing");
		alert.setContentText(text.toString());
		alert.getDialogPane().setPrefWidth(560);
		alert.showAndWait();
	}

	private PamAutoConfig run() {
		showPage(0);
		ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
		if (result != ButtonType.FINISH) {
			return null;
		}
		return wizardData.getSelectedConfig();
	}

	/*
	 * ------------------------------------------------------------------
	 * Dialog shell
	 * ------------------------------------------------------------------
	 */

	private void buildDialog() {
		dialog.setTitle("Set up PAMGuard from imported files");
		dialog.setResizable(true);

		pages.add(new WizardPage("PAMGuard has scanned the files you imported", this::buildScanPage,
				() -> true, () -> true));
		pages.add(new WizardPage("Choose a configuration", this::buildSelectionPage,
				this::readSelectionPage, () -> true));
		pages.add(new WizardPage("Where should PAMGuard put the data it creates?", this::buildStoragePage,
				this::readStoragePage, wizardData::needsStoragePaths));
		pages.add(new WizardPage("PAMGuard will now build this configuration", this::buildSummaryPage,
				() -> true, () -> true));

		headerLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
		headerLabel.setPadding(new Insets(0, 0, 8, 0));

		pageHolder.setPrefSize(700, 420);

		BorderPane content = new BorderPane();
		content.setPadding(new Insets(12));
		content.setTop(headerLabel);
		content.setCenter(pageHolder);
		dialog.getDialogPane().setContent(content);

		/*
		 * Back and Next are ordinary buttons whose actions are consumed, so that
		 * clicking them moves between pages instead of closing the dialog. Finish and
		 * Cancel close it in the normal way.
		 */
		ButtonType backType = new ButtonType("Back", ButtonData.BACK_PREVIOUS);
		ButtonType nextType = new ButtonType("Next", ButtonData.NEXT_FORWARD);
		dialog.getDialogPane().getButtonTypes().addAll(backType, nextType, ButtonType.FINISH, ButtonType.CANCEL);

		backButton = (Button) dialog.getDialogPane().lookupButton(backType);
		nextButton = (Button) dialog.getDialogPane().lookupButton(nextType);

		backButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
			e.consume();
			movePage(-1);
		});
		nextButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
			e.consume();
			movePage(+1);
		});

		// Finish must not close the dialog unless the current page is happy.
		Button finishButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.FINISH);
		finishButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
			if (!pages.get(currentPage).read.getAsBoolean()) {
				e.consume();
			}
		});
	}

	/**
	 * Move forwards or backwards, skipping pages which do not apply, and validating
	 * the current page when moving forwards.
	 */
	private void movePage(int step) {
		if (step > 0 && !pages.get(currentPage).read.getAsBoolean()) {
			return;
		}
		for (int i = currentPage + step; i >= 0 && i < pages.size(); i += step) {
			if (pages.get(i).enabled.getAsBoolean()) {
				showPage(i);
				return;
			}
		}
	}

	private void showPage(int index) {
		currentPage = index;
		WizardPage page = pages.get(index);
		headerLabel.setText(page.header);
		pageHolder.getChildren().setAll(page.build.get());

		backButton.setDisable(findEnabled(index - 1, -1) < 0);
		boolean isLast = findEnabled(index + 1, +1) < 0;
		nextButton.setDisable(isLast);
		dialog.getDialogPane().lookupButton(ButtonType.FINISH).setDisable(!isLast);
	}

	private int findEnabled(int from, int step) {
		for (int i = from; i >= 0 && i < pages.size(); i += step) {
			if (pages.get(i).enabled.getAsBoolean()) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * ------------------------------------------------------------------
	 * Page 1 - what was imported
	 * ------------------------------------------------------------------
	 */

	private javafx.scene.Node buildScanPage() {
		SoundFileSummary summary = wizardData.getSoundSummary();
		VBox box = new VBox(6);

		if (summary == null || summary.getFileCount() == 0) {
			box.getChildren().add(new Label("No sound files were found in what you imported."));
			return box;
		}

		box.getChildren().add(field("Sound files", String.valueOf(summary.getFileCount())));
		if (summary.isValid()) {
			box.getChildren().add(field("Sample rate",
					summary.getMinSampleRate() == summary.getMaxSampleRate()
							? SoundFileSummary.formatRate(summary.getMinSampleRate())
							: SoundFileSummary.formatRate(summary.getMinSampleRate()) + " to "
									+ SoundFileSummary.formatRate(summary.getMaxSampleRate())));
			box.getChildren().add(field("Channels",
					summary.getMinChannels() == summary.getMaxChannels()
							? String.valueOf(summary.getMinChannels())
							: summary.getMinChannels() + " to " + summary.getMaxChannels()));
		}
		else {
			box.getChildren().add(field("Sample rate", "unknown"));
			box.getChildren().add(field("Channels", "unknown"));
		}

		StringBuilder types = new StringBuilder();
		if (summary.hasSudFiles()) {
			types.append("SoundTrap sud files");
		}
		if (wizardData.getFileImport() != null
				&& wizardData.getFileImport().hasType(PamImportFileType.SUD_CLICKS)) {
			types.append(types.length() > 0 ? ", " : "").append("click detections");
		}
		box.getChildren().add(field("Also found", types.length() == 0 ? "-" : types.toString()));

		String warning = null;
		if (!summary.isValid()) {
			warning = "The format of these files could not be read, so configurations cannot be matched to them.";
		}
		else if (summary.isMixedFormats()) {
			warning = "These files do not all have the same format. Configurations are matched to the "
					+ "lowest sample rate and channel count found.";
		}
		if (warning != null) {
			Label warnLabel = new Label(warning);
			warnLabel.setWrapText(true);
			warnLabel.setStyle("-fx-text-fill: firebrick;");
			warnLabel.setPadding(new Insets(10, 0, 0, 0));
			box.getChildren().add(warnLabel);
		}
		return box;
	}

	private HBox field(String name, String value) {
		Label nameLabel = new Label(name + ":");
		nameLabel.setMinWidth(110);
		return new HBox(6, nameLabel, new Label(value));
	}

	/*
	 * ------------------------------------------------------------------
	 * Page 2 - choose a configuration
	 * ------------------------------------------------------------------
	 */

	private javafx.scene.Node buildSelectionPage() {
		List<PamAutoConfig> available = wizardData.getAvailableConfigs();

		configList = new ListView<>();
		configList.setPrefWidth(300);
		configList.setCellFactory(lv -> new ConfigListCell());

		TextArea description = new TextArea();
		description.setEditable(false);
		description.setWrapText(true);

		Label speciesLabel = new Label();
		speciesLabel.setWrapText(true);
		Label requirementsLabel = new Label();
		requirementsLabel.setWrapText(true);
		Label modulesLabel = new Label();

		HBox speciesIconRow = buildSpeciesIconRow();
		HBox mediumRow = buildMediumRow();

		configList.getSelectionModel().selectedItemProperty().addListener((obs, old, config) -> {
			description.setText(config == null ? "" : config.getConfigDescription());
			speciesLabel.setText(speciesText(config));
			requirementsLabel.setText(requirementsText(config));
			modulesLabel.setText(modulesText(config));
			showSpeciesIcons(config);
			showMediumChoice(config);
		});

		groupFilter = new ComboBox<>();
		buildFilterOptions(available);
		groupFilter.setOnAction(e -> applyFilters(available));

		HBox filters = new HBox(6, new Label("Show:"), groupFilter);
		filters.setAlignment(Pos.CENTER_LEFT);
		filters.setPadding(new Insets(0, 0, 8, 0));

		VBox details = new VBox(6, description, speciesLabel, requirementsLabel, modulesLabel,
				speciesIconRow, mediumRow);
		details.setPadding(new Insets(0, 0, 0, 10));
		VBox.setVgrow(description, Priority.ALWAYS);

		BorderPane centre = new BorderPane();
		centre.setLeft(configList);
		centre.setCenter(details);

		BorderPane page = new BorderPane();
		page.setTop(filters);
		page.setCenter(centre);

		applyFilters(available);
		if (wizardData.getSelectedConfig() != null) {
			configList.getSelectionModel().select(wizardData.getSelectedConfig());
		}
		else {
			configList.getSelectionModel().selectFirst();
		}
		return page;
	}

	private void buildFilterOptions(List<PamAutoConfig> available) {
		Set<ConfigSpeciesGroup> groups = new LinkedHashSet<>();
		for (PamAutoConfig config : available) {
			if (config instanceof FileConfigAutoConfig) {
				groups.addAll(((FileConfigAutoConfig) config).getDescription().getGroups());
			}
		}

		List<Object> groupItems = new ArrayList<>();
		groupItems.add(ALL_GROUPS);
		groupItems.addAll(groups);
		groupFilter.setItems(FXCollections.observableArrayList(groupItems));
		groupFilter.getSelectionModel().selectFirst();
		groupFilter.setDisable(groupItems.size() < 2);
	}

	/**
	 * The row of species group icons shown under the description. Every group is
	 * always present; which of them are picked out depends on the selection.
	 */
	private HBox buildSpeciesIconRow() {
		HBox row = new HBox(4);
		row.setAlignment(Pos.CENTER_LEFT);
		for (ConfigSpeciesGroup group : ConfigSpeciesGroup.values()) {
			ImageView view = new ImageView(SpeciesIconFactory.getInstance().getFXImage(group, MUTED_TINT));
			view.setFitWidth(ICON_SIZE);
			view.setFitHeight(ICON_SIZE);
			view.setPreserveRatio(true);
			javafx.scene.control.Tooltip.install(view, new javafx.scene.control.Tooltip(group.getGroupName()));
			speciesIcons.put(group, view);
			row.getChildren().add(view);
		}
		return row;
	}

	/**
	 * The air / water chooser shown under the icons.
	 */
	private HBox buildMediumRow() {
		mediumLabel = new Label("Medium:");
		mediumChooser = new ComboBox<>(FXCollections.observableArrayList(SoundMedium.values()));
		mediumChooser.getSelectionModel().select(wizardData.getMedium());
		HBox row = new HBox(6, mediumLabel, mediumChooser);
		row.setAlignment(Pos.CENTER_LEFT);
		return row;
	}

	/**
	 * Pick out the species groups the configuration targets and grey the rest.
	 *
	 * @param config the selected configuration, or null if none is selected.
	 */
	private void showSpeciesIcons(PamAutoConfig config) {
		Set<ConfigSpeciesGroup> targeted = new LinkedHashSet<>();
		if (config instanceof FileConfigAutoConfig) {
			targeted.addAll(((FileConfigAutoConfig) config).getDescription().getGroups());
		}
		for (java.util.Map.Entry<ConfigSpeciesGroup, ImageView> entry : speciesIcons.entrySet()) {
			ConfigSpeciesGroup group = entry.getKey();
			boolean active = targeted.contains(group);
			entry.getValue().setImage(SpeciesIconFactory.getInstance()
					.getFXImage(group, active ? ACTIVE_TINT : MUTED_TINT));
			javafx.scene.control.Tooltip.install(entry.getValue(), new javafx.scene.control.Tooltip(active
					? group.getGroupName() + " - targeted by this configuration"
					: group.getGroupName() + " - not targeted by this configuration"));
		}
	}

	/**
	 * Offer the air / water choice, but only where the configuration leaves it open.
	 * A configuration written for a particular medium shows that medium, fixed - a
	 * right whale detector is of no use in air.
	 *
	 * @param config the selected configuration, or null if none is selected.
	 */
	private void showMediumChoice(PamAutoConfig config) {
		SoundMedium fixed = (config == null) ? null : config.getGlobalMediumSettings();
		if (fixed != null) {
			mediumChooser.getSelectionModel().select(fixed);
			mediumChooser.setDisable(true);
			mediumChooser.setTooltip(new javafx.scene.control.Tooltip(
					"This configuration is only for use in " + fixed.toString().toLowerCase()));
		}
		else {
			mediumChooser.getSelectionModel().select(wizardData.getMedium());
			mediumChooser.setDisable(config == null);
			mediumChooser.setTooltip(new javafx.scene.control.Tooltip(
					"Whether these recordings were made in air or in water"));
		}
		mediumLabel.setDisable(mediumChooser.isDisable());
	}

	private void applyFilters(List<PamAutoConfig> available) {
		Object group = groupFilter.getSelectionModel().getSelectedItem();

		PamAutoConfig previous = configList.getSelectionModel().getSelectedItem();
		List<PamAutoConfig> shown = new ArrayList<>();
		for (PamAutoConfig config : available) {
			if (matchesGroup(config, group)) {
				shown.add(config);
			}
		}
		configList.setItems(FXCollections.observableArrayList(shown));

		if (previous != null && shown.contains(previous)) {
			configList.getSelectionModel().select(previous);
		}
		else {
			configList.getSelectionModel().selectFirst();
		}
	}

	private boolean matchesGroup(PamAutoConfig config, Object group) {
		if (!(group instanceof ConfigSpeciesGroup)) {
			return true;
		}
		if (!(config instanceof FileConfigAutoConfig)) {
			return false;
		}
		return ((FileConfigAutoConfig) config).getDescription().getGroups().contains(group);
	}

	private String speciesText(PamAutoConfig config) {
		if (config == null) {
			return "";
		}
		String[] species = config.getSpeciesList();
		return (species == null || species.length == 0) ? "" : "Targets: " + String.join(", ", species);
	}

	private String requirementsText(PamAutoConfig config) {
		PamConfigDescription description = descriptionOf(config);
		if (description == null) {
			return "";
		}
		StringBuilder needs = new StringBuilder("Needs ");
		needs.append(SoundFileSummary.formatRate((float) description.getMinSampleRate())).append(" or above");
		if (description.getMinChannels() > 1) {
			needs.append(", ").append(description.getMinChannels()).append(" channels");
		}
		SoundFileSummary summary = wizardData.getSoundSummary();
		if (summary != null && summary.isValid() && description.getTargetSampleRate() != null
				&& summary.getMinSampleRate() > description.getTargetSampleRate()) {
			needs.append("; data will be decimated to ")
					.append(SoundFileSummary.formatRate(description.getTargetSampleRate().floatValue()));
		}
		return needs.toString();
	}

	private String modulesText(PamAutoConfig config) {
		if (!(config instanceof FileConfigAutoConfig)) {
			return "";
		}
		PamConfigInspection inspection = ((FileConfigAutoConfig) config).getInspection();
		return (inspection != null && inspection.isValid())
				? "Creates " + inspection.getModules().size() + " modules" : "";
	}

	private PamConfigDescription descriptionOf(PamAutoConfig config) {
		return (config instanceof FileConfigAutoConfig)
				? ((FileConfigAutoConfig) config).getDescription() : null;
	}

	private boolean readSelectionPage() {
		PamAutoConfig selected = configList.getSelectionModel().getSelectedItem();
		if (selected == null) {
			warn("No configuration", "Please choose a configuration to continue.");
			return false;
		}
		if (selected instanceof FileConfigAutoConfig) {
			PamConfigInspection inspection = ((FileConfigAutoConfig) selected).getInspection();
			if (inspection == null || !inspection.isValid()) {
				String reason = (inspection == null) ? "no loader is available" : inspection.getError();
				warn("Configuration cannot be used",
						String.format("\"%s\" cannot be loaded: %s", selected.getConfigName(), reason));
				return false;
			}
		}
		wizardData.setSelectedConfig(selected);
		wizardData.setMedium(mediumChooser.getSelectionModel().getSelectedItem());
		return true;
	}

	/**
	 * Draws each configuration by name. The species icons live under the description
	 * rather than in the list, so that the full set can be shown for whichever
	 * configuration is selected.
	 */
	private static class ConfigListCell extends ListCell<PamAutoConfig> {
		@Override
		protected void updateItem(PamAutoConfig item, boolean empty) {
			super.updateItem(item, empty);
			setGraphic(null);
			setText((empty || item == null) ? null : item.getConfigName());
		}
	}

	/*
	 * ------------------------------------------------------------------
	 * Page 3 - storage
	 * ------------------------------------------------------------------
	 */

	private javafx.scene.Node buildStoragePage() {
		ConfigApplyContext context = wizardData.getApplyContext();
		PamConfigInspection inspection = wizardData.getSelectedInspection();

		File project = context.getProjectFolder();
		if (project == null) {
			project = ConfigApplyContext.getDefaultProjectFolder();
		}
		// re-derive, since the configuration name may have changed since last time.
		wizardData.setProjectFolder(project);

		projectField = new TextField(project == null ? "" : project.getAbsolutePath());
		binaryField = new TextField(context.getBinaryFolder() == null
				? "" : context.getBinaryFolder().getAbsolutePath());
		databaseField = new TextField(context.getDatabaseFile() == null
				? "" : context.getDatabaseFile().getAbsolutePath());

		Label projectLabel = new Label("Project folder:");
		projectLabel.setMinWidth(120);
		binaryRowLabel = new Label("Binary store folder:");
		binaryRowLabel.setMinWidth(120);
		databaseRowLabel = new Label("Database file:");
		databaseRowLabel.setMinWidth(120);

		HBox projectRow = pathRow(projectLabel, projectField, () -> browseFolder(projectField, "Select a project folder"));
		binaryRow = pathRow(binaryRowLabel, binaryField, () -> browseFolder(binaryField, "Select a binary storage folder"));
		databaseRow = pathRow(databaseRowLabel, databaseField, this::browseDatabase);

		// keep the derived paths in step with the project folder.
		projectField.textProperty().addListener((obs, old, text) -> {
			if (text == null || text.trim().isEmpty()) {
				return;
			}
			File folder = new File(text.trim());
			binaryField.setText(new File(folder, ConfigApplyContext.BINARY_FOLDER_NAME).getAbsolutePath());
			String current = databaseField.getText();
			String name = (current == null || current.trim().isEmpty())
					? "pamguard.sqlite3" : new File(current.trim()).getName();
			databaseField.setText(new File(folder, name).getAbsolutePath());
		});

		boolean hasBinary = inspection != null && inspection.hasBinaryStore();
		boolean hasDatabase = inspection != null && inspection.hasDatabase();
		binaryRow.setVisible(hasBinary);
		binaryRow.setManaged(hasBinary);
		databaseRow.setVisible(hasDatabase);
		databaseRow.setManaged(hasDatabase);

		Label note = new Label("The folders are created if they do not already exist.");
		note.setStyle("-fx-font-style: italic;");
		note.setPadding(new Insets(10, 0, 0, 0));

		return new VBox(6, projectRow, binaryRow, databaseRow, note);
	}

	private HBox pathRow(Label label, TextField field, Runnable browse) {
		Button button = new Button("Browse...");
		button.setOnAction(e -> browse.run());
		HBox.setHgrow(field, Priority.ALWAYS);
		HBox row = new HBox(6, label, field, button);
		row.setAlignment(Pos.CENTER_LEFT);
		return row;
	}

	private boolean readStoragePage() {
		ConfigApplyContext context = wizardData.getApplyContext();
		PamConfigInspection inspection = wizardData.getSelectedInspection();

		String projectText = projectField.getText().trim();
		if (projectText.isEmpty()) {
			warn("No project folder", "Please choose a folder for PAMGuard to write its data to.");
			return false;
		}
		context.setProjectFolder(new File(projectText), wizardData.getSelectedConfig().getConfigName());

		if (inspection != null && inspection.hasBinaryStore()) {
			String binaryText = binaryField.getText().trim();
			if (binaryText.isEmpty()) {
				warn("No binary store",
						"This configuration writes binary data, so it needs a folder to write them to.");
				return false;
			}
			context.setBinaryFolder(new File(binaryText));
		}
		else {
			context.setBinaryFolder(null);
		}

		if (inspection != null && inspection.hasDatabase()) {
			String databaseText = databaseField.getText().trim();
			if (databaseText.isEmpty()) {
				warn("No database", "This configuration uses a database, so it needs a database file.");
				return false;
			}
			context.setDatabaseFile(new File(databaseText));
		}
		else {
			context.setDatabaseFile(null);
		}
		return true;
	}

	private void browseFolder(TextField field, String title) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(title);
		String current = field.getText();
		if (current != null && !current.trim().isEmpty()) {
			File folder = new File(current.trim());
			if (folder.isDirectory()) {
				chooser.setInitialDirectory(folder);
			}
		}
		File chosen = chooser.showDialog(dialog.getOwner());
		if (chosen != null) {
			field.setText(chosen.getAbsolutePath());
		}
	}

	private void browseDatabase() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select a database file");
		String current = databaseField.getText();
		if (current != null && !current.trim().isEmpty()) {
			File file = new File(current.trim());
			if (file.getParentFile() != null && file.getParentFile().isDirectory()) {
				chooser.setInitialDirectory(file.getParentFile());
			}
			chooser.setInitialFileName(file.getName());
		}
		File chosen = chooser.showSaveDialog(dialog.getOwner());
		if (chosen != null) {
			databaseField.setText(chosen.getAbsolutePath());
		}
	}

	/*
	 * ------------------------------------------------------------------
	 * Page 4 - summary
	 * ------------------------------------------------------------------
	 */

	private javafx.scene.Node buildSummaryPage() {
		summaryArea = new TextArea(buildSummaryText());
		summaryArea.setEditable(false);
		summaryArea.setWrapText(true);
		return summaryArea;
	}

	/**
	 * Describe in plain words what applying the chosen configuration will do.
	 * Deliberately the same content as the Swing wizard shows.
	 */
	private String buildSummaryText() {
		StringBuilder text = new StringBuilder();
		PamAutoConfig selected = wizardData.getSelectedConfig();
		if (selected == null) {
			return "No configuration selected.";
		}
		text.append(selected.getConfigName()).append("\n\n");

		SoundFileSummary summary = wizardData.getSoundSummary();
		if (summary != null && summary.getFileCount() > 0) {
			text.append("Sound Acquisition will read ").append(summary.getFileCount())
					.append(summary.getFileCount() == 1 ? " file" : " files");
			if (summary.isValid()) {
				text.append(" at ").append(summary.getFormatDescription());
			}
			text.append(".\n");
		}

		PamConfigInspection inspection = wizardData.getSelectedInspection();
		if (wizardData.willDecimate() && inspection != null && summary != null) {
			text.append("A decimator will reduce the sample rate from ")
					.append(SoundFileSummary.formatRate(summary.getMinSampleRate()))
					.append(" to ")
					.append(SoundFileSummary.formatRate(inspection.getTargetSampleRate().floatValue()))
					.append(", which is what this configuration's detectors expect.\n");
		}

		ConfigApplyContext context = wizardData.getApplyContext();
		if (inspection != null && inspection.hasBinaryStore() && context.getBinaryFolder() != null) {
			text.append("Binary data will be written to ")
					.append(context.getBinaryFolder().getAbsolutePath()).append("\n");
		}
		if (inspection != null && inspection.hasDatabase() && context.getDatabaseFile() != null) {
			text.append("The database will be ")
					.append(context.getDatabaseFile().getAbsolutePath()).append("\n");
		}

		if (inspection != null && inspection.isValid()) {
			text.append("\nModules created:\n");
			for (UsedModuleInfo module : inspection.getModules()) {
				if (module == null) {
					continue;
				}
				text.append("    ").append(module.unitName);
				if (!module.unitName.equals(module.getUnitType())) {
					text.append("  (").append(module.getUnitType()).append(")");
				}
				text.append("\n");
			}
		}
		return text.toString();
	}

	private void warn(String header, String message) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("PAMGuard");
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * One page of the wizard: its heading, how to build its content, how to read and
	 * validate it, and whether it applies at all.
	 */
	private static class WizardPage {

		final String header;
		final java.util.function.Supplier<javafx.scene.Node> build;
		final java.util.function.BooleanSupplier read;
		final java.util.function.BooleanSupplier enabled;

		WizardPage(String header, java.util.function.Supplier<javafx.scene.Node> build,
				java.util.function.BooleanSupplier read, java.util.function.BooleanSupplier enabled) {
			this.header = header;
			this.build = build;
			this.read = read;
			this.enabled = enabled;
		}
	}
}
