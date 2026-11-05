package pamViewFX.fxStartup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.kordamp.ikonli.javafx.FontIcon;

import PamController.PamSettingManager;
import PamController.PamguardVersionInfo;
import PamController.startup.StartupEntry;
import PamController.startup.StartupHistory;
import PamController.startup.StartupSelection;
import PamView.FXInitialiser;
import PamView.PamIcon;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;

/**
 * Start up dialog which brings together the choice of run mode (normal or viewer)
 * and the choice of settings file (normal mode) or database (viewer mode).
 * <p>
 * Shown when PAMGuard is started with the -c option. The user picks Normal or
 * Viewer and is then shown the recent or favourite psfx files or databases for
 * that mode, from which they can open one, or create a new one. A right click on
 * a file allows it to be duplicated, shown in the system file browser, added to
 * the favourites or removed from the list.
 * <p>
 * The dialog is a JavaFX window, styled with the AtlantaFX theme when PAMGuard
 * runs the JavaFX GUI and with the FlatLaf matching style sheets when it runs the
 * Swing GUI.
 *
 * @author Jamie Macaulay
 */
public class StartupDialogFX {

	private static final String CSS = "/Resources/css/startupDialog.css";
	private static final String CSS_ATLANTA = "/Resources/css/startupDialogAtlanta.css";
	private static final String CSS_FLATLAF = "/Resources/css/startupDialogFlatLaf.css";
	private static final String CSS_FLATLAF_DARK = "/Resources/css/startupDialogFlatLafDark.css";

	/*
	 * Theme sheets for dark mode. These are the same sheets PAMGuard uses for the 
	 * dark colour scheme (see PamAtlantaStyle and PamDefaultStyle).
	 */
	private static final String FLATLAF_LIGHT = "/Resources/css/pamFlatLafLight.css";
	private static final String FLATLAF_DARK = "/Resources/css/pamFlatLafDark.css";
	private static final String PRIMER_PAMGUARD = "/Resources/css/primer-pamguard.css";
	private static final String PRIMER_PAMGUARD_DARK = "/Resources/css/primer-pamguard-dark.css";

	private static final String[] PSF_ENDS = {"*.psfx", "*.psf"};
	private static final String[] DB_ENDS = {"*.sqlite3", "*.sqlite", "*.db", "*.db3"};

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	private final boolean fxGUI;

	private final StartupHistory history;

	private Stage stage;

	private ToggleGroup modeGroup;
	private ToggleButton normalCard, viewerCard;

	private ToggleGroup listGroup;
	private ToggleButton recentTab, favouritesTab;

	private TableView<StartupEntry> table;

	private Label placeholder;

	private Button openButton;

	private StartupSelection result;

	/**
	 * True once the user has used the light / dark switch, in which case PAMGuard
	 * opens with the same scheme. Otherwise PAMGuard keeps the colour scheme it was
	 * last closed with.
	 */
	private boolean colourSchemeChosen = false;

	/**
	 * Create the dialog.
	 * @param fxGUI - true if PAMGuard will run the JavaFX GUI, false for Swing.
	 */
	private StartupDialogFX(boolean fxGUI) {
		this.fxGUI = fxGUI;
		this.history = StartupHistory.getInstance();
		history.importOldRecentLists();
		createStage();
	}

	/**
	 * Show the start up dialog and wait for the user to choose. Can be called from
	 * any thread - JavaFX is started if it isn't already running.
	 * @param fxGUI - true if PAMGuard will run with the JavaFX GUI, false for Swing.
	 * Sets the theme used to decorate the dialog.
	 * @return the selected mode and file, or null if the dialog was cancelled.
	 */
	public static StartupSelection showDialog(boolean fxGUI) {
		FXInitialiser.initialise();
		// stop the FX thread closing when this window closes, as for the rest of PAMGuard.
		Platform.setImplicitExit(false);
		if (Platform.isFxApplicationThread()) {
			return new StartupDialogFX(fxGUI).showAndWait();
		}
		final StartupSelection[] ans = new StartupSelection[1];
		final CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				ans[0] = new StartupDialogFX(fxGUI).showAndWait();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				latch.countDown();
			}
		});
		try {
			latch.await();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
		return ans[0];
	}

	private StartupSelection showAndWait() {
		stage.showAndWait();
		return result;
	}

	/*
	 * ------------------------------------------------------------------------
	 * Building the window
	 * ------------------------------------------------------------------------
	 */

	private void createStage() {
		stage = new Stage();
		stage.setTitle("PAMGuard " + PamguardVersionInfo.version);
		Image icon = loadIcon(PamIcon.LARGE);
		if (icon != null) {
			stage.getIcons().add(icon);
		}

		// header
		Label title = new Label("Welcome to PAMGuard");
		title.getStyleClass().add("startup-title");
		Label subtitle = new Label("Choose how to run PAMGuard, then pick a configuration to open.");
		subtitle.getStyleClass().add("startup-subtitle");
		Label version = new Label("Version " + PamguardVersionInfo.version);
		version.getStyleClass().add("startup-version");
		HBox titleRow = new HBox(10, title, version);
		titleRow.setAlignment(Pos.BASELINE_LEFT);
		VBox headerText = new VBox(2, titleRow, subtitle);
		HBox.setHgrow(headerText, Priority.ALWAYS);

		// dark mode switch, remembered between runs.
		ToggleButton darkButton = new ToggleButton();
		darkButton.getStyleClass().add("startup-plain");
		darkButton.setSelected(history.isDarkMode());
		updateDarkButton(darkButton);
		darkButton.selectedProperty().addListener((obs, o, dark) -> {
			// the user chose light or dark, so PAMGuard opens with that scheme too.
			colourSchemeChosen = true;
			history.setDarkMode(dark);
			history.save();
			updateDarkButton(darkButton);
			applyStyle(stage.getScene());
			table.refresh();
		});
		HBox header = new HBox(10, headerText, darkButton);
		header.setAlignment(Pos.TOP_LEFT);
		header.getStyleClass().add("startup-header");

		// mode cards
		modeGroup = new ToggleGroup();
		normalCard = createModeCard("Normal", "Process sound in real time or analyse recordings using a settings (.psfx) file");
		viewerCard = createModeCard("Viewer", "Review and reprocess data previously processed in normal mode using its database");
		normalCard.setUserData(StartupHistory.MODE_NORMAL);
		viewerCard.setUserData(StartupHistory.MODE_VIEWER);
		HBox cards = new HBox(14, normalCard, viewerCard);
		HBox.setHgrow(normalCard, Priority.ALWAYS);
		HBox.setHgrow(viewerCard, Priority.ALWAYS);
		cards.setPadding(new Insets(12, 20, 12, 20));
		cards.setMinHeight(Region.USE_PREF_SIZE);
		keepOneSelected(modeGroup);

		// recent / favourites + new
		listGroup = new ToggleGroup();
		recentTab = createListTab("Recent");
		favouritesTab = createListTab("Favourites");
		keepOneSelected(listGroup);

		Button browseButton = new Button("Browse...");
		browseButton.setGraphic(new FontIcon("mdi2f-folder-open-outline"));
		browseButton.setOnAction(e -> browse());
		browseButton.setTooltip(new Tooltip("Open a file which isn't in the list"));

		Button newButton = new Button("New");
		newButton.setGraphic(new FontIcon("mdi2p-plus"));
		newButton.getStyleClass().add("startup-accent");
		newButton.setOnAction(e -> createNew());
		newButton.setTooltip(new Tooltip("Create a new settings file or database"));

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		// let both buttons fill the bar height so Browse and New always match.
		browseButton.setMaxHeight(Double.MAX_VALUE);
		newButton.setMaxHeight(Double.MAX_VALUE);
		browseButton.minHeightProperty().bind(newButton.heightProperty());
		browseButton.getStyleClass().add("startup-plain");
		HBox listBar = new HBox(6, recentTab, favouritesTab, spacer, browseButton, newButton);
		listBar.setFillHeight(true);
		listBar.setAlignment(Pos.CENTER_LEFT);
		listBar.setPadding(new Insets(4, 20, 8, 20));

		// file table
		table = createTable();
		VBox.setVgrow(table, Priority.ALWAYS);
		VBox tableBox = new VBox(table);
		tableBox.setPadding(new Insets(0, 20, 0, 20));
		VBox.setVgrow(tableBox, Priority.ALWAYS);

		// footer
		Label hint = new Label("Double click to open. Right click for more options.");
		hint.getStyleClass().add("startup-hint");
		Region spacer2 = new Region();
		HBox.setHgrow(spacer2, Priority.ALWAYS);
		Button cancelButton = new Button("Cancel");
		cancelButton.setCancelButton(true);
		cancelButton.setOnAction(e -> {
			result = null;
			stage.close();
		});
		openButton = new Button("Open");
		openButton.getStyleClass().add("startup-accent");
		openButton.setDefaultButton(true);
		openButton.setOnAction(e -> openSelected());
		openButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
		HBox footer = new HBox(8, hint, spacer2, cancelButton, openButton);
		footer.setAlignment(Pos.CENTER_LEFT);
		footer.getStyleClass().add("startup-footer");

		VBox centre = new VBox(header, cards, listBar, tableBox);
		VBox.setVgrow(tableBox, Priority.ALWAYS);

		BorderPane root = new BorderPane();
		root.setCenter(centre);
		root.setBottom(footer);
		root.getStyleClass().add("startup-root");

		Scene scene = new Scene(root, 780, 640);
		applyStyle(scene);
		stage.setScene(scene);
		stage.setMinWidth(560);
		stage.setMinHeight(460);

		// listeners - set these up before the initial selection so the list fills.
		modeGroup.selectedToggleProperty().addListener((obs, o, n) -> fillTable());
		listGroup.selectedToggleProperty().addListener((obs, o, n) -> fillTable());

		recentTab.setSelected(true);
		if (history.getLastMode() == StartupHistory.MODE_VIEWER) {
			viewerCard.setSelected(true);
		}
		else {
			normalCard.setSelected(true);
		}

		stage.setOnShown(e -> {
			stage.toFront();
			table.requestFocus();
		});
	}

	private ToggleButton createModeCard(String name, String description) {
		ImageView imageView = new ImageView();
		Image img = loadIcon(PamIcon.LARGE);
		if (img != null) {
			imageView.setImage(img);
		}
		imageView.setFitWidth(56);
		imageView.setFitHeight(56);
		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);

		Label title = new Label(name);
		title.getStyleClass().add("card-title");
		Label desc = new Label(description);
		desc.getStyleClass().add("card-desc");
		desc.setWrapText(true);
		desc.setMinHeight(Region.USE_PREF_SIZE);
		VBox text = new VBox(4, title, desc);
		text.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(text, Priority.ALWAYS);

		HBox graphic = new HBox(14, imageView, text);
		graphic.setAlignment(Pos.CENTER_LEFT);

		ToggleButton card = new ToggleButton();
		card.setGraphic(graphic);
		card.setToggleGroup(modeGroup);
		card.getStyleClass().add("mode-card");
		card.setMaxWidth(Double.MAX_VALUE);
		card.setPrefWidth(300);
		// fixed height - the file list gets any extra space.
		card.setMinHeight(96);
		card.setPrefHeight(96);
		card.setMaxHeight(Region.USE_PREF_SIZE);
		card.setAlignment(Pos.CENTER_LEFT);
		// the graphic must follow the button width so that the description wraps.
		graphic.prefWidthProperty().bind(card.widthProperty().subtract(40));
		return card;
	}

	private ToggleButton createListTab(String name) {
		ToggleButton tab = new ToggleButton(name);
		tab.setToggleGroup(listGroup);
		tab.getStyleClass().add("list-tab");
		return tab;
	}

	/**
	 * Stop a toggle group being left with nothing selected when the user clicks the
	 * selected button again.
	 */
	private void keepOneSelected(ToggleGroup group) {
		group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal == null && oldVal != null) {
				oldVal.setSelected(true);
			}
		});
	}

	private TableView<StartupEntry> createTable() {
		TableView<StartupEntry> table = new TableView<>();
		table.getStyleClass().add("startup-table");
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setFixedCellSize(50);

		TableColumn<StartupEntry, StartupEntry> favCol = new TableColumn<>("");
		favCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
		favCol.setCellFactory(c -> new FavouriteCell());
		favCol.setMinWidth(36);
		favCol.setMaxWidth(36);
		favCol.setPrefWidth(36);
		favCol.setResizable(false);
		favCol.setSortable(false);

		TableColumn<StartupEntry, StartupEntry> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
		nameCol.setCellFactory(c -> new NameCell());
		nameCol.setSortable(false);
		nameCol.setPrefWidth(520);

		TableColumn<StartupEntry, StartupEntry> dateCol = new TableColumn<>("Last opened");
		dateCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
		dateCol.setCellFactory(c -> new DateCell());
		dateCol.setSortable(false);
		dateCol.setMinWidth(140);
		dateCol.setPrefWidth(160);

		table.getColumns().add(favCol);
		table.getColumns().add(nameCol);
		table.getColumns().add(dateCol);

		placeholder = new Label();
		placeholder.getStyleClass().add("startup-placeholder");
		placeholder.setWrapText(true);
		placeholder.setPadding(new Insets(20));
		table.setPlaceholder(placeholder);

		table.setRowFactory(t -> {
			TableRow<StartupEntry> row = new TableRow<>();
			/*
			 * Set the selected row colour inline. The themes colour selected rows in
			 * quite different ways (FlatLaf with a strong accent and white text), and 
			 * inline styles keep the rows the same, and readable, in both. 
			 */
			row.selectedProperty().addListener((obs, o, sel) -> {
				row.setStyle(sel && !row.isEmpty() ? "-fx-background-color: -sd-accent-subtle;" : "");
			});
			row.setOnMouseClicked(e -> {
				if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty()) {
					openEntry(row.getItem());
				}
			});
			ContextMenu menu = new ContextMenu();
			row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty())
					.then((ContextMenu) null).otherwise(menu));
			/*
			 * JavaFX won't show a context menu with no items, so the items must be in
			 * place before the right click - filling them in onShowing is too late. Fill
			 * when the row gets a file and again just before the menu is requested, so
			 * the favourite item is up to date. 
			 */
			row.itemProperty().addListener((obs, o, item) -> fillContextMenu(menu, item));
			row.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, 
					e -> fillContextMenu(menu, row.getItem()));
			return row;
		});

		table.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) {
				StartupEntry entry = table.getSelectionModel().getSelectedItem();
				if (entry != null) {
					removeEntry(entry);
				}
			}
		});
		return table;
	}

	private void fillContextMenu(ContextMenu menu, StartupEntry entry) {
		menu.getItems().clear();
		if (entry == null) {
			return;
		}
		boolean exists = entry.exists();

		MenuItem open = new MenuItem("Open");
		open.setGraphic(new FontIcon("mdi2p-play"));
		open.setDisable(!exists);
		open.setOnAction(e -> openEntry(entry));

		MenuItem duplicate = new MenuItem("Duplicate...");
		duplicate.setGraphic(new FontIcon("mdi2c-content-copy"));
		duplicate.setDisable(!exists);
		duplicate.setOnAction(e -> duplicate(entry));

		MenuItem show = new MenuItem(getShowInFolderName());
		show.setGraphic(new FontIcon("mdi2f-folder-search-outline"));
		show.setDisable(entry.getFile().getParentFile() == null || !entry.getFile().getParentFile().exists());
		show.setOnAction(e -> showInFolder(entry.getFile()));

		MenuItem fav = new MenuItem(entry.isFavourite() ? "Remove from favourites" : "Add to favourites");
		fav.setGraphic(new FontIcon(entry.isFavourite() ? "mdi2s-star-off-outline" : "mdi2s-star-outline"));
		fav.setOnAction(e -> toggleFavourite(entry));

		MenuItem remove = new MenuItem("Remove from list (keeps the file)");
		remove.setGraphic(new FontIcon("mdi2c-close"));
		remove.setOnAction(e -> removeEntry(entry));

		menu.getItems().addAll(open, duplicate, show, new SeparatorMenuItem(), fav, remove);
	}

	private void updateDarkButton(ToggleButton button) {
		boolean dark = button.isSelected();
		button.setGraphic(new FontIcon(dark ? "mdi2w-white-balance-sunny" : "mdi2w-weather-night"));
		button.setText(dark ? "Light" : "Dark");
		Tooltip tip = new Tooltip((dark ? "Switch to light mode." : "Switch to dark mode.")
				+ " Choosing light or dark here also opens PAMGuard in that mode."
				+ " If you don't use this switch, PAMGuard opens with the light or dark"
				+ " colour scheme it was last closed with.");
		tip.setWrapText(true);
		tip.setMaxWidth(320);
		button.setTooltip(tip);
	}

	/**
	 * Set the style sheets for the GUI type (Atlanta for JavaFX, FlatLaf for Swing)
	 * and light or dark mode.
	 */
	private void applyStyle(Scene scene) {
		boolean dark = history.isDarkMode();
		scene.getStylesheets().clear();
		if (fxGUI) {
			if (dark) {
				scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
				addSheet(scene, PRIMER_PAMGUARD);
				addSheet(scene, PRIMER_PAMGUARD_DARK);
			}
			else {
				scene.getStylesheets().add(new PrimerLight().getUserAgentStylesheet());
				addSheet(scene, PRIMER_PAMGUARD);
			}
			addSheet(scene, CSS_ATLANTA);
		}
		else {
			addSheet(scene, FLATLAF_LIGHT);
			if (dark) {
				addSheet(scene, FLATLAF_DARK);
			}
			addSheet(scene, CSS_FLATLAF);
			if (dark) {
				addSheet(scene, CSS_FLATLAF_DARK);
			}
		}
		addSheet(scene, CSS);
	}

	private void addSheet(Scene scene, String resource) {
		URL url = getClass().getResource(resource);
		if (url != null) {
			scene.getStylesheets().add(url.toExternalForm());
		}
		else {
			System.out.println("StartupDialogFX: unable to find style sheet " + resource);
		}
	}

	private static Image loadIcon(int size) {
		String path = PamIcon.getPAMGuardIconPath(size);
		try (InputStream is = StartupDialogFX.class.getResourceAsStream("/" + path)) {
			if (is != null) {
				return new Image(is);
			}
		}
		catch (IOException e) {
			// fall through
		}
		return null;
	}

	/*
	 * ------------------------------------------------------------------------
	 * List contents
	 * ------------------------------------------------------------------------
	 */

	private int getMode() {
		Toggle t = modeGroup.getSelectedToggle();
		if (t == null) {
			return StartupHistory.MODE_NORMAL;
		}
		return (Integer) t.getUserData();
	}

	private boolean isViewer() {
		return getMode() == StartupHistory.MODE_VIEWER;
	}

	private boolean isFavourites() {
		return favouritesTab.isSelected();
	}

	/**
	 * Refill the table for the selected mode and list, keeping the selection if
	 * possible.
	 */
	private void fillTable() {
		StartupEntry selected = table.getSelectionModel().getSelectedItem();
		List<StartupEntry> entries = history.getEntries(getMode(), isFavourites());
		ObservableList<StartupEntry> items = FXCollections.observableArrayList(entries);
		table.setItems(items);

		String what = isViewer() ? "databases" : "settings files";
		if (isFavourites()) {
			placeholder.setText("No favourite " + what + " yet. Right click a file in the Recent list, or click its star, to add it to your favourites.");
		}
		else if (isViewer()) {
			placeholder.setText("No recent databases. Click Browse to open a database, or New to create one.\n"
					+ "Databases used in normal mode will appear here automatically.");
		}
		else {
			placeholder.setText("No recent settings files. Click New to create a new configuration or Browse to open an existing one.");
		}

		int sel = -1;
		if (selected != null) {
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).matches(selected.getFile(), selected.getMode())) {
					sel = i;
					break;
				}
			}
		}
		if (sel < 0) {
			// default to the first file that exists.
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).exists()) {
					sel = i;
					break;
				}
			}
		}
		if (sel >= 0) {
			table.getSelectionModel().select(sel);
			table.scrollTo(sel);
		}
		else {
			table.getSelectionModel().clearSelection();
		}
	}

	private void selectFile(File file) {
		for (int i = 0; i < table.getItems().size(); i++) {
			if (table.getItems().get(i).matches(file, getMode())) {
				table.getSelectionModel().select(i);
				table.scrollTo(i);
				return;
			}
		}
	}

	private class FavouriteCell extends TableCell<StartupEntry, StartupEntry> {

		/**
		 * A label rather than a button, so that the theme's button decoration doesn't
		 * crowd out the star in such a narrow column.
		 */
		private final Label star = new Label();

		FavouriteCell() {
			star.getStyleClass().add("fav-button");
			setAlignment(Pos.CENTER);
			star.setOnMouseClicked(e -> {
				StartupEntry entry = getItem();
				if (entry != null && e.getButton() == MouseButton.PRIMARY) {
					toggleFavourite(entry);
					e.consume();
				}
			});
		}

		@Override
		protected void updateItem(StartupEntry entry, boolean empty) {
			super.updateItem(entry, empty);
			setText(null);
			if (empty || entry == null) {
				setGraphic(null);
				return;
			}
			boolean fav = entry.isFavourite();
			FontIcon icon = new FontIcon(fav ? "mdi2s-star" : "mdi2s-star-outline");
			icon.setIconSize(18);
			star.setGraphic(icon);
			star.getStyleClass().remove("fav-on");
			if (fav) {
				star.getStyleClass().add("fav-on");
			}
			star.setTooltip(new Tooltip(fav ? "Remove from favourites" : "Add to favourites"));
			setGraphic(star);
		}
	}

	private class NameCell extends TableCell<StartupEntry, StartupEntry> {

		@Override
		protected void updateItem(StartupEntry entry, boolean empty) {
			super.updateItem(entry, empty);
			setText(null);
			if (empty || entry == null) {
				setGraphic(null);
				setTooltip(null);
				return;
			}
			File f = entry.getFile();
			Node icon;
			if (entry.getMode() == StartupHistory.MODE_VIEWER) {
				FontIcon dbIcon = new FontIcon("mdi2d-database");
				dbIcon.setIconSize(26);
				icon = dbIcon;
			}
			else {
				ImageView iv = new ImageView(loadIcon(PamIcon.SMALL));
				iv.setFitWidth(26);
				iv.setFitHeight(26);
				iv.setPreserveRatio(true);
				icon = iv;
			}
			StackPane iconPane = new StackPane(icon);
			iconPane.setMinWidth(30);

			Label name = new Label(f.getName() + (entry.exists() ? "" : "  (missing)"));
			name.getStyleClass().add("file-name");
			String parent = f.getParent() == null ? "" : f.getParent();
			Label path = new Label(parent);
			path.getStyleClass().add("file-path");
			// inline so that the themes' selected row text colours don't override them.
			if (entry.exists()) {
				name.setStyle("-fx-text-fill: -sd-fg; -fx-font-weight: bold;");
				path.setStyle("-fx-text-fill: -sd-muted;");
			}
			else {
				name.setStyle("-fx-text-fill: -sd-muted; -fx-font-weight: bold; -fx-font-style: italic;");
				path.setStyle("-fx-text-fill: -sd-muted; -fx-font-style: italic;");
			}
			path.setTextOverrun(javafx.scene.control.OverrunStyle.LEADING_ELLIPSIS);
			VBox text = new VBox(1, name, path);
			text.setAlignment(Pos.CENTER_LEFT);
			text.setMinWidth(0);
			HBox.setHgrow(text, Priority.ALWAYS);

			HBox box = new HBox(10, iconPane, text);
			box.setAlignment(Pos.CENTER_LEFT);
			if (!entry.exists()) {
				box.getStyleClass().add("file-missing");
			}
			setGraphic(box);
			setTooltip(new Tooltip(f.getAbsolutePath()));
		}
	}

	private class DateCell extends TableCell<StartupEntry, StartupEntry> {

		@Override
		protected void updateItem(StartupEntry entry, boolean empty) {
			super.updateItem(entry, empty);
			setText(null);
			if (empty || entry == null) {
				setGraphic(null);
				return;
			}
			Label label = new Label(entry.getLastOpened() > 0 ? dateFormat.format(new Date(entry.getLastOpened())) : "");
			label.getStyleClass().add("file-date");
			label.setStyle("-fx-text-fill: -sd-fg;");
			setGraphic(label);
		}
	}

	/*
	 * ------------------------------------------------------------------------
	 * Actions
	 * ------------------------------------------------------------------------
	 */

	private void openSelected() {
		StartupEntry entry = table.getSelectionModel().getSelectedItem();
		if (entry != null) {
			openEntry(entry);
		}
	}

	private void openEntry(StartupEntry entry) {
		if (!entry.exists()) {
			showWarning("File not found", "The file " + entry.getFile().getAbsolutePath()
					+ " no longer exists. Right click to remove it from the list.");
			return;
		}
		finish(entry.getFile());
	}

	/**
	 * Close the dialog, opening the given file in the selected mode.
	 */
	private void finish(File file) {
		int mode = getMode();
		history.recordOpened(file, mode);
		history.setLastMode(mode);
		history.save();
		Boolean dark = colourSchemeChosen ? history.isDarkMode() : null;
		result = new StartupSelection(mode, file, dark);
		stage.close();
	}

	/*
	 * Starring and removing change the rows in place rather than refilling the
	 * table, so the list doesn't scroll back to the selected row.
	 */
	private void toggleFavourite(StartupEntry entry) {
		history.setFavourite(entry, !entry.isFavourite());
		history.save();
		if (isFavourites() && !entry.isFavourite()) {
			removeRow(entry);
		}
		else {
			table.refresh();
		}
	}

	private void removeEntry(StartupEntry entry) {
		history.remove(entry);
		history.save();
		removeRow(entry);
	}

	private void removeRow(StartupEntry entry) {
		for (int i = 0; i < table.getItems().size(); i++) {
			if (table.getItems().get(i).matches(entry.getFile(), entry.getMode())) {
				table.getItems().remove(i);
				break;
			}
		}
		table.refresh();
	}

	private FileChooser createChooser(String title, File startFile) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(title);
		if (isViewer()) {
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sqlite databases", DB_ENDS));
		}
		else {
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PAMGuard settings files", PSF_ENDS));
		}
		File folder = null;
		if (startFile != null) {
			folder = startFile.isDirectory() ? startFile : startFile.getParentFile();
		}
		if (folder == null || !folder.exists()) {
			List<StartupEntry> recent = history.getEntries(getMode(), false);
			for (StartupEntry e : recent) {
				File p = e.getFile().getParentFile();
				if (p != null && p.exists()) {
					folder = p;
					break;
				}
			}
		}
		if (folder == null || !folder.exists()) {
			folder = new File(pamguard.Pamguard.getSettingsFolder());
		}
		if (folder.exists()) {
			chooser.setInitialDirectory(folder);
		}
		return chooser;
	}

	/**
	 * Open a file which isn't in the list.
	 */
	private void browse() {
		FileChooser chooser = createChooser(isViewer() ? "Open database" : "Open settings file", null);
		File file = chooser.showOpenDialog(stage);
		if (file == null) {
			return;
		}
		finish(file);
	}

	/**
	 * Create a new settings file or database.
	 */
	private void createNew() {
		boolean viewer = isViewer();
		FileChooser chooser = createChooser(viewer ? "Create new database" : "Create new settings file", null);
		chooser.setInitialFileName(viewer ? "PamguardDatabase.sqlite3" : "PamguardSettings.psfx");
		File file = chooser.showSaveDialog(stage);
		if (file == null) {
			return;
		}
		if (viewer) {
			file = checkEnd(file, ".sqlite3", DB_ENDS);
			if (!file.exists() && !createSqliteDatabase(file)) {
				showWarning("Database", "Unable to create the database " + file.getAbsolutePath());
				return;
			}
		}
		else {
			file = checkEnd(file, PamSettingManager.getCurrentSettingsFileEnd(), PSF_ENDS);
			/*
			 * If the file exists (the user chose to replace it) delete it so that
			 * PAMGuard really does start with an empty configuration.
			 */
			if (file.exists()) {
				try {
					Files.delete(file.toPath());
				}
				catch (IOException e) {
					showWarning("Settings file", "Unable to replace " + file.getAbsolutePath() + ": " + e.getMessage());
					return;
				}
			}
		}
		finish(file);
	}

	/**
	 * Make a copy of a file, add it to the list and select it.
	 */
	private void duplicate(StartupEntry entry) {
		File src = entry.getFile();
		FileChooser chooser = createChooser("Duplicate " + src.getName(), src);
		String name = src.getName();
		int dot = name.lastIndexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;
		String ext = dot > 0 ? name.substring(dot) : "";
		chooser.setInitialFileName(base + "_copy" + ext);
		File dest = chooser.showSaveDialog(stage);
		if (dest == null) {
			return;
		}
		if (ext.length() > 0 && !dest.getName().contains(".")) {
			dest = new File(dest.getAbsolutePath() + ext);
		}
		if (dest.getAbsoluteFile().equals(src.getAbsoluteFile())) {
			showWarning("Duplicate", "Choose a different name for the copy.");
			return;
		}
		try {
			Files.copy(src.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			showWarning("Duplicate", "Unable to copy " + src.getAbsolutePath() + " to " + dest.getAbsolutePath() + ": " + e.getMessage());
			return;
		}
		history.recordOpened(dest, entry.getMode());
		history.save();
		// show the copy in the recent list.
		recentTab.setSelected(true);
		fillTable();
		selectFile(dest);
	}

	/**
	 * Add a file end if the file doesn't have one of the allowed ones.
	 */
	private static File checkEnd(File file, String defaultEnd, String[] allowed) {
		String lower = file.getName().toLowerCase();
		for (String a : allowed) {
			if (lower.endsWith(a.substring(1).toLowerCase())) {
				return file;
			}
		}
		if (!defaultEnd.startsWith(".")) {
			defaultEnd = "." + defaultEnd;
		}
		return new File(file.getAbsolutePath() + defaultEnd);
	}

	/**
	 * Create an empty Sqlite database. PAMGuard adds the tables it needs when it opens
	 * the database.
	 */
	private static boolean createSqliteDatabase(File file) {
		try {
			Class.forName("org.sqlite.JDBC");
			try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath())) {
				// opening the connection creates the file.
			}
		}
		catch (Exception e) {
			System.out.println("Unable to create database " + file + ": " + e.getMessage());
			return false;
		}
		return file.exists();
	}

	/**
	 * @return the name of the system file browser for the menu.
	 */
	private static String getShowInFolderName() {
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.startsWith("mac")) {
			return "Show in Finder";
		}
		if (os.startsWith("win")) {
			return "Show in Explorer";
		}
		return "Open containing folder";
	}

	/**
	 * Open the system file browser at the folder holding a file, highlighting the
	 * file where the system allows.
	 */
	public static void showInFolder(File file) {
		Thread t = new Thread(() -> {
			String os = System.getProperty("os.name", "").toLowerCase();
			File folder = file.getParentFile();
			try {
				if (os.startsWith("mac")) {
					if (file.exists()) {
						new ProcessBuilder("open", "-R", file.getAbsolutePath()).start();
					}
					else {
						new ProcessBuilder("open", folder.getAbsolutePath()).start();
					}
				}
				else if (os.startsWith("win")) {
					if (file.exists()) {
						new ProcessBuilder("explorer.exe", "/select,", file.getAbsolutePath()).start();
					}
					else {
						new ProcessBuilder("explorer.exe", folder.getAbsolutePath()).start();
					}
				}
				else {
					try {
						new ProcessBuilder("xdg-open", folder.getAbsolutePath()).start();
					}
					catch (IOException e) {
						java.awt.Desktop.getDesktop().open(folder);
					}
				}
			}
			catch (Exception e) {
				System.out.println("Unable to show " + file + " in the file browser: " + e.getMessage());
			}
		}, "Show in folder");
		t.setDaemon(true);
		t.start();
	}

	private void showWarning(String title, String message) {
		Alert alert = new Alert(AlertType.WARNING, message);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.initOwner(stage);
		alert.getDialogPane().getStylesheets().addAll(stage.getScene().getStylesheets());
		alert.showAndWait();
	}

}
