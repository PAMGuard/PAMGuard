package simulatedAcquisition.layoutFX;

import Acquisition.layoutFX.DAQSettingsPane;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.flipPane.PamFlipPane;
import simulatedAcquisition.SimObject;
import simulatedAcquisition.SimParameters;
import simulatedAcquisition.SimProcess;
import simulatedAcquisition.sounds.SimSignal;
import simulatedAcquisition.sounds.SimSignals;

/**
 * JavaFX settings pane for the Simulated Data acquisition system.
 * Provides controls for background noise, propagation model selection,
 * and a table of simulated objects with add/edit/copy/remove functionality.
 * <p>
 * Uses a FlipPane: the front shows the main settings and table, while the
 * back shows the SimObject edit form. The back button acts as OK to confirm edits.
 * 
 * @author Jamie Macaulay
 */
public class SimDAQPane extends DAQSettingsPane {

	/**
	 * The flip pane containing front (table) and back (edit form).
	 */
	private PamFlipPane flipPane;

	/**
	 * Background noise text field.
	 */
	private TextField noiseField;

	/**
	 * Propagation model combo box.
	 */
	private ComboBox<String> propModelCombo;

	/**
	 * Table showing simulated objects.
	 */
	private TableView<SimObjectRow> simTable;

	/**
	 * Observable list backing the table.
	 */
	private ObservableList<SimObjectRow> tableData;

	/**
	 * Reference to the SimProcess.
	 */
	private SimProcess simProcess;

	// --- Edit form fields (back of flip pane) ---
	private TextField nameField;
	private ComboBox<SimSignal> soundTypeCombo;
	private TextField amplitudeField;
	private TextField meanIntervalField;
	private CheckBox randomIntervalsCheck;
	private CheckBox echoesCheck;
	private TextField echoDelayField;
	private CheckBox separateEchoesCheck;
	private TextField latField;
	private TextField longField;
	private TextField depthField;
	private TextField speedField;
	private TextField courseField;
	private TextField slantField;

	/**
	 * Buttons for table operations.
	 */
	private PamButton addButton, copyButton, removeButton, editButton;

	/**
	 * The SimObject currently being edited. Null if adding new.
	 */
	private SimObject editingObject;

	/**
	 * True if we are adding a new object, false if editing existing.
	 */
	private boolean isAddingNew;

	public SimDAQPane(SimProcess simProcess) {
		this.simProcess = simProcess;

		flipPane = new PamFlipPane();

		// --- FRONT: main settings + table ---
		flipPane.setFrontContent(createFrontPane());

		// --- BACK: sim object edit form ---
		flipPane.setAdvPaneContent(createEditPane());
		flipPane.getAdvLabel().setText("Simulated ");
		flipPane.getPostAdvLabel().setText("Object");

		// Listen for back button (OK) press
		flipPane.backButtonProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.intValue() == PamFlipPane.OK_BACK_BUTTON) {
				applyEditToObject();
			}
		});
	}

	/**
	 * Create the front pane with environment settings and the simulated objects table.
	 */
	private Node createFrontPane() {
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);
		holder.setPadding(new Insets(5));

		// --- Environment section ---
		Label envTitle = new Label("Environment");
		PamGuiManagerFX.titleFont2style(envTitle);

		PamGridPane envGrid = new PamGridPane();
		envGrid.setHgap(5);
		envGrid.setVgap(5);

		envGrid.add(new Label("Background Noise"), 0, 0);
		noiseField = new TextField();
		noiseField.setPrefColumnCount(6);
		envGrid.add(noiseField, 1, 0);
		envGrid.add(new Label("dB re.1\u00B5Pa/\u221AHz"), 2, 0);

		envGrid.add(new Label("Propagation Model"), 0, 1);
		propModelCombo = new ComboBox<>();
		propModelCombo.setMaxWidth(Double.MAX_VALUE);
		envGrid.add(propModelCombo, 1, 1, 2, 1);

		// --- Simulated objects table ---
		Label objTitle = new Label("Simulated Objects");
		PamGuiManagerFX.titleFont2style(objTitle);

		tableData = FXCollections.observableArrayList();
		simTable = new TableView<>(tableData);
		simTable.setPrefHeight(150);

		TableColumn<SimObjectRow, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setPrefWidth(80);

		TableColumn<SimObjectRow, String> latCol = new TableColumn<>("Lat");
		latCol.setCellValueFactory(new PropertyValueFactory<>("lat"));
		latCol.setPrefWidth(70);

		TableColumn<SimObjectRow, String> lonCol = new TableColumn<>("Long");
		lonCol.setCellValueFactory(new PropertyValueFactory<>("lon"));
		lonCol.setPrefWidth(70);

		TableColumn<SimObjectRow, String> cogCol = new TableColumn<>("COG");
		cogCol.setCellValueFactory(new PropertyValueFactory<>("cog"));
		cogCol.setPrefWidth(50);

		TableColumn<SimObjectRow, String> spdCol = new TableColumn<>("SPD");
		spdCol.setCellValueFactory(new PropertyValueFactory<>("spd"));
		spdCol.setPrefWidth(50);

		TableColumn<SimObjectRow, String> depthCol = new TableColumn<>("Depth");
		depthCol.setCellValueFactory(new PropertyValueFactory<>("depth"));
		depthCol.setPrefWidth(50);

		TableColumn<SimObjectRow, String> typeCol = new TableColumn<>("Sound");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("signalType"));
		typeCol.setPrefWidth(100);

		simTable.getColumns().addAll(nameCol, latCol, lonCol, cogCol, spdCol, depthCol, typeCol);

		// Double-click to edit
		simTable.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				editAction();
			}
		});

		VBox.setVgrow(simTable, Priority.ALWAYS);

		// --- Buttons ---
		addButton = new PamButton("Add...");
		addButton.setOnAction(e -> addAction());

		copyButton = new PamButton("Copy...");
		copyButton.setOnAction(e -> copyAction());
		copyButton.setDisable(true);

		removeButton = new PamButton("Remove");
		removeButton.setOnAction(e -> removeAction());
		removeButton.setDisable(true);

		editButton = new PamButton("Edit...");
		editButton.setOnAction(e -> editAction());
		editButton.setDisable(true);

		// Enable/disable buttons based on selection
		simTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			boolean hasSelection = (newVal != null);
			copyButton.setDisable(!hasSelection);
			removeButton.setDisable(!hasSelection);
			editButton.setDisable(!hasSelection);
		});

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		PamHBox buttonBar = new PamHBox();
		buttonBar.setSpacing(5);
		buttonBar.setAlignment(Pos.CENTER_RIGHT);
		buttonBar.getChildren().addAll(spacer, addButton, copyButton, removeButton, editButton);

		holder.getChildren().addAll(envTitle, envGrid, objTitle, simTable, buttonBar);

		return holder;
	}

	/**
	 * Create the edit pane (back of flip pane) for editing a SimObject.
	 */
	private Node createEditPane() {
		PamVBox editHolder = new PamVBox();
		editHolder.setSpacing(5);
		editHolder.setPadding(new Insets(5));

		PamGridPane grid = new PamGridPane();
		grid.setHgap(5);
		grid.setVgap(5);

		int row = 0;

		// Name
		grid.add(new Label("Name"), 0, row);
		nameField = new TextField();
		nameField.setPrefColumnCount(12);
		grid.add(nameField, 1, row, 3, 1);

		// Sound type
		row++;
		grid.add(new Label("Sound Type"), 0, row);
		soundTypeCombo = new ComboBox<>();
		soundTypeCombo.setMaxWidth(Double.MAX_VALUE);
		grid.add(soundTypeCombo, 1, row, 3, 1);

		// Source level
		row++;
		grid.add(new Label("Source Level"), 0, row);
		amplitudeField = new TextField();
		amplitudeField.setPrefColumnCount(6);
		grid.add(amplitudeField, 1, row);
		grid.add(new Label("dB re.1\u00B5Pa p-p"), 2, row, 2, 1);

		// Mean interval
		row++;
		grid.add(new Label("Mean Interval"), 0, row);
		meanIntervalField = new TextField();
		meanIntervalField.setPrefColumnCount(6);
		grid.add(meanIntervalField, 1, row);
		grid.add(new Label("s"), 2, row);
		randomIntervalsCheck = new CheckBox("Randomise");
		grid.add(randomIntervalsCheck, 3, row);

		// Echoes
		row++;
		grid.add(new Label("Echoes"), 0, row);
		echoesCheck = new CheckBox("Add Echoes");
		grid.add(echoesCheck, 1, row);
		grid.add(new Label("Delay (ms)"), 2, row);
		echoDelayField = new TextField();
		echoDelayField.setPrefColumnCount(5);
		grid.add(echoDelayField, 3, row);

		row++;
		separateEchoesCheck = new CheckBox("Separate echoes");
		grid.add(separateEchoesCheck, 1, row, 3, 1);

		echoesCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
			echoDelayField.setDisable(!newVal);
			separateEchoesCheck.setDisable(!newVal);
		});

		// Position - latitude
		row++;
		grid.add(new Label("Latitude"), 0, row);
		latField = new TextField();
		latField.setPrefColumnCount(10);
		grid.add(latField, 1, row, 3, 1);

		// Position - longitude
		row++;
		grid.add(new Label("Longitude"), 0, row);
		longField = new TextField();
		longField.setPrefColumnCount(10);
		grid.add(longField, 1, row, 3, 1);

		// Depth and Speed
		row++;
		grid.add(new Label("Depth (m)"), 0, row);
		depthField = new TextField();
		depthField.setPrefColumnCount(5);
		grid.add(depthField, 1, row);
		grid.add(new Label("Speed (m/s)"), 2, row);
		speedField = new TextField();
		speedField.setPrefColumnCount(5);
		grid.add(speedField, 3, row);

		// Course and slant
		row++;
		grid.add(new Label("Course (\u00B0)"), 0, row);
		courseField = new TextField();
		courseField.setPrefColumnCount(5);
		grid.add(courseField, 1, row);
		grid.add(new Label("Slant (\u00B0)"), 2, row);
		slantField = new TextField();
		slantField.setPrefColumnCount(5);
		grid.add(slantField, 3, row);

		editHolder.getChildren().add(grid);
		return editHolder;
	}

	/**
	 * Add a new SimObject.
	 */
	private void addAction() {
		SimObject simObject = new SimObject();
		simObject.name = "New Source";

		// Set default position from master reference point
		LatLong latLong = MasterReferencePoint.getLatLong();
		if (latLong != null) {
			simObject.startPosition.setLatitude(latLong.getLatitude());
			simObject.startPosition.setLongitude(latLong.getLongitude());
		}

		editingObject = null;
		isAddingNew = true;
		setEditFormParams(simObject);
		flipPane.flipToBack();
	}

	/**
	 * Copy the selected SimObject.
	 */
	private void copyAction() {
		int row = simTable.getSelectionModel().getSelectedIndex();
		if (row < 0) return;

		SimParameters params = simProcess.getSimParameters();
		SimObject original = params.getObject(row);
		SimObject copy = original.clone();
		copy.name = copy.name + " (copy)";

		editingObject = null;
		isAddingNew = true;
		setEditFormParams(copy);
		flipPane.flipToBack();
	}

	/**
	 * Remove the selected SimObject.
	 */
	private void removeAction() {
		int row = simTable.getSelectionModel().getSelectedIndex();
		if (row < 0) return;

		SimParameters params = simProcess.getSimParameters();
		params.removeObject(row);
		refreshTable();
	}

	/**
	 * Edit the selected SimObject.
	 */
	private void editAction() {
		int row = simTable.getSelectionModel().getSelectedIndex();
		if (row < 0) return;

		SimParameters params = simProcess.getSimParameters();
		editingObject = params.getObject(row);
		isAddingNew = false;
		setEditFormParams(editingObject);
		flipPane.flipToBack();
	}

	/**
	 * Set the edit form fields from a SimObject.
	 * @param obj - the SimObject to populate the form from.
	 */
	private void setEditFormParams(SimObject obj) {
		nameField.setText(obj.name != null ? obj.name : "");

		// Fill sound types combo
		SimSignals simSignals = simProcess.getSimSignals();
		soundTypeCombo.getItems().clear();
		if (simSignals != null) {
			for (int i = 0; i < simSignals.getNumSignals(); i++) {
				soundTypeCombo.getItems().add(simSignals.getSignal(i));
			}
			if (obj.signalName != null) {
				SimSignal found = simSignals.findSignal(obj.signalName);
				soundTypeCombo.getSelectionModel().select(found);
			}
		}

		amplitudeField.setText(String.format("%.1f", obj.amplitude));
		meanIntervalField.setText(String.format("%.1f", obj.meanInterval));
		randomIntervalsCheck.setSelected(obj.randomIntervals);

		echoesCheck.setSelected(obj.echo);
		echoDelayField.setText(String.format("%.3f", obj.echoDelay));
		separateEchoesCheck.setSelected(obj.seperateEcho);
		echoDelayField.setDisable(!obj.echo);
		separateEchoesCheck.setDisable(!obj.echo);

		latField.setText(String.format("%.6f", obj.startPosition.getLatitude()));
		longField.setText(String.format("%.6f", obj.startPosition.getLongitude()));
		depthField.setText(String.format("%.1f", -obj.getHeight()));
		speedField.setText(String.format("%.1f", obj.speed));
		courseField.setText(String.format("%.1f", obj.course));
		slantField.setText(String.format("%.1f", obj.slantAngle));
	}

	/**
	 * Apply the edit form values to the SimObject and update the table.
	 * Called when the back button (OK) is pressed on the flip pane.
	 */
	private void applyEditToObject() {
		SimObject obj;
		if (isAddingNew) {
			obj = new SimObject();
		} else {
			obj = editingObject.clone();
		}

		obj.name = nameField.getText();
		obj.randomIntervals = randomIntervalsCheck.isSelected();
		obj.echo = echoesCheck.isSelected();
		obj.seperateEcho = separateEchoesCheck.isSelected();

		try {
			obj.amplitude = Double.parseDouble(amplitudeField.getText());
			obj.meanInterval = Double.parseDouble(meanIntervalField.getText());
			obj.setHeight(-Double.parseDouble(depthField.getText()));
			obj.speed = Double.parseDouble(speedField.getText());
			obj.course = Double.parseDouble(courseField.getText());
			obj.slantAngle = Double.parseDouble(slantField.getText());
			obj.echoDelay = Double.parseDouble(echoDelayField.getText());
			obj.startPosition.setLatitude(Double.parseDouble(latField.getText()));
			obj.startPosition.setLongitude(Double.parseDouble(longField.getText()));
		} catch (NumberFormatException e) {
			System.err.println("SimDAQPane: Invalid number in edit form: " + e.getMessage());
			return;
		}

		SimSignal selectedSignal = soundTypeCombo.getSelectionModel().getSelectedItem();
		if (selectedSignal != null) {
			obj.signalName = selectedSignal.getName();
		}

		SimParameters params = simProcess.getSimParameters();
		if (isAddingNew) {
			params.addSimObject(obj);
		} else {
			params.replaceSimObject(editingObject, obj);
		}

		refreshTable();
	}

	/**
	 * Refresh the table data from the SimParameters.
	 */
	private void refreshTable() {
		tableData.clear();
		SimParameters params = simProcess.getSimParameters();
		if (params != null) {
			for (int i = 0; i < params.getNumObjects(); i++) {
				tableData.add(new SimObjectRow(params.getObject(i)));
			}
		}
	}

	@Override
	public void setParams() {
		SimParameters params = simProcess.getSimParameters();
		if (params == null) return;

		noiseField.setText(String.format("%3.0f", params.backgroundNoise));

		propModelCombo.getItems().clear();
		for (int i = 0; i < simProcess.getPropagationModels().size(); i++) {
			propModelCombo.getItems().add(simProcess.getPropagationModels().get(i).toString());
		}
		if (simProcess.getPropagationModel() != null) {
			String currentName = simProcess.getPropagationModel().toString();
			propModelCombo.getSelectionModel().select(currentName);
		}

		refreshTable();
	}

	@Override
	public boolean getParams() {
		SimParameters params = simProcess.getSimParameters();
		if (params == null) return false;

		try {
			params.backgroundNoise = Double.parseDouble(noiseField.getText());
		} catch (NumberFormatException e) {
			return false;
		}

		int propIndex = propModelCombo.getSelectionModel().getSelectedIndex();
		if (propIndex >= 0 && propIndex < simProcess.getPropagationModels().size()) {
			simProcess.setPropagationModel(simProcess.getPropagationModels().get(propIndex));
			params.propagationModel = simProcess.getPropagationModels().get(propIndex).getName();
		}

		return true;
	}

	@Override
	public Object getParams(Object currParams) {
		return null;
	}

	@Override
	public void setParams(Object input) {
	}

	@Override
	public String getName() {
		return "Simulated data settings";
	}

	@Override
	public Node getContentNode() {
		return flipPane;
	}

	@Override
	public void paneInitialized() {
	}

	/**
	 * Data class for rows in the simulated objects table. Matches the Swing table columns:
	 * Name, Lat, Long, COG, SPD, Depth, Sound.
	 */
	public static class SimObjectRow {
		private final String name;
		private final String lat;
		private final String lon;
		private final String cog;
		private final String spd;
		private final String depth;
		private final String signalType;

		public SimObjectRow(SimObject simObject) {
			this.name = simObject.name;
			this.lat = simObject.startPosition.formatLatitude();
			this.lon = simObject.startPosition.formatLongitude();
			this.cog = String.format("%.0f", simObject.course);
			this.spd = String.format("%.1f", simObject.speed);
			this.depth = String.format("%.1f", -simObject.getHeight());
			this.signalType = (simObject.signalName != null) ? simObject.signalName : "Unknown";
		}

		public String getName() { return name; }
		public String getLat() { return lat; }
		public String getLon() { return lon; }
		public String getCog() { return cog; }
		public String getSpd() { return spd; }
		public String getDepth() { return depth; }
		public String getSignalType() { return signalType; }
	}
}
