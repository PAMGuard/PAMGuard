/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package beamformer.algorithms;

import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.PamArray;
import PamController.PamController;
import PamController.SettingsPane;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.AcousticDataBlock;
import Spectrogram.WindowFunction;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseControl;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import pamMaths.PamVector;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;

/**
 * 
 * 
 * @author mo55
 *
 */
public class BeamAlgoParamsPane extends SettingsPane<BeamAlgorithmParams>  {

	/**
	 * The BeamformerControl object in control
	 */
	private BeamFormerBaseControl beamformerControl;

	/**
	 * The parameters to display in the dialog
	 */
	protected BeamAlgorithmParams curParams;
	
	/**
	 * The frequency to use when generating the beam pattern
	 */
	private int frequency = 150;
	
	/**
	 * Speed of sound in the water.  Gets updated to whatever has been entered in the Array Manager
	 */
	private double soundSpeed = 1500;


	/**
	 * List of beams in this group
	 */
	protected final ObservableList<BeamInfo> beamList = FXCollections.observableArrayList();

	/**
	 * The beam that is currently selected in the spreadsheet
	 */
	protected BeamInfo selectedBeam;
	
	/**
	 * The next beam number to use in the spreadsheet
	 */
	protected int nextBeamNum;
	
	/**
	 * The TableView object containing the beam information
	 */
	protected TableView<BeamInfo> beamTable;
	
	protected Button addButton;
	
	/**
	 * The line chart showing the beam pattern
	 */
	protected LineChart<Number,Number> lineChart;
	
	/**
	 * The beam main angle text box
	 */
	private TextField incomingAzimuth;
	
	/**
	 * The beam main angle slider
	 */
	private Slider azSlider;
	
	/**
	 * The beam secondary angle text box
	 */
	private TextField incomingElevation;
	
	/**
	 * Label associated with incomingElevation TextField
	 */
	private Label incomingElevationLabel;

	/**
	 * The beam secondary angle slider
	 */
	private Slider elevSlider;
	
	/**
	 * The frequency to evaluate the beam pattern at
	 */
	private TextField sourceFreq;
	
	/**
	 * A slider that can change the frequency to evaluate the beam pattern at
	 */
	private Slider freqSlider;
	
	/**
	 * A choicebox listing the types of windows available
	 */
	private ChoiceBox<String> windowType;
	
	/**
	 * The hydrophone element locations
	 */
	protected PamVector[] elementLocs;

	/**
	 * A boolean indicating whether beampatterns for all beams should be shown in the chart (true) or just
	 * the beampattern for the currently selected beam
	 */
	private boolean showAllBeams=false;
	
	/**
	 * The full frequency range, calculated as the min and max frequency of the current FFT
	 */
	protected double[] fullFreqRange = new double[2];
	
	/**
	 * CheckBox indicating whether or not a beamogram should be calculated
	 */
	private CheckBox beamogramToggle; 
	
	/**
	 * Minimum beamogram main angle (relative to the array primary axis)
	 */
	private TextField minMainAngle;
	
	/**
	 * Label associated with minMainAngle TextField
	 */
	private Label minMainAngleLabel;
	
	/**
	 * Maximum beamogram main angle (relative to the array primary axis)
	 */
	private TextField maxMainAngle;
	
	/**
	 * Label associated with maxMainAngle TextField
	 */
	private Label maxMainAngleLabel;
	
	/**
	 * beamogram main angle step size (relative to the array primary axis)
	 */
	private TextField stepMainAngle;
	
	/**
	 * Label associated with stepMainAngle TextField
	 */
	private Label stepMainAngleLabel;
	
	/**
	 * Minimum beamogram secondary angle (relative to perpendicular to the array primary axis)
	 */
	private TextField minSecAngle;
	
	/**
	 * Label associated with minSecAngle TextField
	 */
	private Label minSecAngleLabel;
	
	/**
	 * Maximum beamogram secondary angle (relative to perpendicular to the array primary axis)
	 */
	private TextField maxSecAngle;
	
	/**
	 * Label associated with maxSecAngle TextField
	 */
	private Label maxSecAngleLabel;
	
	/**
	 * beamogram secondary angle step size (relative to perpendicular to the array primary axis)
	 */
	private TextField stepSecAngle;
	
	/**
	 * Label associated with stepSecAngle TextField
	 */
	private Label stepSecAngleLabel;
	
	/**
	 * Minimum beamogram frequency
	 */
	private TextField minFreq;
	
	/**
	 * Maximum beamogram frequency
	 */
	private TextField maxFreq;
	
	/**
	 * minimum allowable primary angle
	 */
	protected int minPrime;
	
	/**
	 * maximum allowable primary angle
	 */
	protected int maxPrime;
	
	/**
	 * minimum allowable secondary angle
	 */
	protected int minSec;
	
	/**
	 * maximum allowable secondary angle
	 */
	protected int maxSec;
	
	/**
	 * The window type to use for the beamogram
	 */
	private ChoiceBox<String> beamogramWindow;
	
	/**
	 * The FFT source currently selected in the main GUI
	 */
	private AcousticDataBlock acousticDataSource;
	
	/**
	 * The main pane holding everything
	 */
	private PamBorderPane mainPane;
	
	/**
	 * The pane holding the tabs
	 */
	private TabPane topPane;
	
	/**
	 * The pane holding the individual beams
	 */
	private Tab beamsTabPane;
	
	/**
	 * The pane holding the beamogram
	 */
	private Tab beamogramTabPane;
	
	public enum BeamgramStatus {
		BEAMOGRAMGOOD,
		BEAMOGRAMBAD,
		BEAMOGRAMNOTUSED;
	}

	/**
	 * The array shape
	 */
	private int arrayShape;
	
	/**
	 * Text giving the current array type on beamogram tab
	 */
	private Text arrayTypeLabel;
	
	/**
	 * Text giving the current array type on the beam tab
	 */
	private Text arrayTypeLabelBeam;
	
	/**
	 * Info for the user regarding min/max main angle values, based on current array type, on beamogram tab
	 */
	private Text mainAngleInfo;
	
	/**
	 * Info for the user regarding min/max secondary angle values, based on current array type, on beamogram tab
	 */
	private Text secAngleInfo;
	
	/**
	 * Info for the user regarding min/max main angle values, based on current array type, on beamogram tab
	 */
	private Text mainAngleInfoBeam;
	
	/**
	 * Info for the user regarding min/max secondary angle values, based on current array type, on beamogram tab
	 */
	private Text secAngleInfoBeam;
	
	/**
	 * Pane containing the min/max/step values for the main angle
	 */
	private GridPane mainAnglePane;
	
	/**
	 * Pane containing the min/max/step values for the secondary angle
	 */
	private GridPane secAnglePane;
	
	/**
	 * A vector containing the array axes
	 */
	private PamVector[] arrayMainAxes;

	/**
	 * Secondary angle text box
	 */
	protected TextField addSecAngle;

	protected TextField addAngle;
	
	protected HBox addBox;

	protected GridPane grid;

	private Text freqInfo;
	
	/**
	 * @param basicFreqDomBeamProvider
	 */
	public BeamAlgoParamsPane(Object window, BeamFormerBaseControl beamFormerControl2) {
		super(window);
		// save the link to the control unit
		this.beamformerControl = beamFormerControl2;
		mainPane = new PamBorderPane();
		
		// define the top level pane containing both individual beams and beamogram info
		topPane = new TabPane();
//		topPane.setAddTabButton(false);
		topPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		// define the pane containing individual beam info
		PamHBox topBeamPane = new PamHBox();
		topBeamPane.setSpacing(10);
		topBeamPane.setPadding(new Insets(10));

		// add the pane holding the list of beams
		Node firstPane = this.createBeamListPane();
		HBox.setHgrow(firstPane, Priority.NEVER);
		topBeamPane.getChildren().add(firstPane);

		// add in the beam pattern pane
		Node secondPane = this.createBeamPatternPane();
		HBox.setHgrow(secondPane, Priority.ALWAYS);
		topBeamPane.getChildren().add(secondPane);
		
		// add it all to the top level pane
		PamBorderPane individBeamsPanel = new PamBorderPane(topBeamPane);
		VBox.setVgrow(individBeamsPanel, Priority.ALWAYS);
		beamsTabPane = new Tab("Individual Beams", individBeamsPanel);
		topPane.getTabs().add(beamsTabPane);

		// add the beamogram pane
		PamBorderPane beamogramPanel = new PamBorderPane(this.createBeamogramPane());
		VBox.setVgrow(beamogramPanel, Priority.NEVER);
		beamogramTabPane = new Tab("BeamOGram", beamogramPanel);
		topPane.getTabs().add(beamogramTabPane);
		
		// add everything to the dialog window
		mainPane.setCenter(new PamBorderPane(topPane));
	}

	/**
	 * Create the pane holding the list of beams
	 * @return
	 */
	public Node createBeamListPane() {

		// create the pane
		PamVBox beamPane = new PamVBox();
		
		// create some labels to inform the user of the array type and limitations
		PamVBox arrayTypePane = new PamVBox();
		arrayTypePane.setPadding(new Insets(5, 0, 10, 0));
//		arrayTypePane.setSpacing(5);
		PamHBox arrayBox = new PamHBox();
		arrayBox.setAlignment(Pos.CENTER_LEFT);
		arrayBox.setSpacing(10);
		Text arrayTypeHeader = new Text("Current Array type:");
		arrayTypeLabelBeam = new Text(ArrayManager.getArrayTypeString(arrayShape));
		arrayTypeLabelBeam.setFont(Font.font(arrayTypeLabelBeam.getFont().getFamily(), FontWeight.BOLD, arrayTypeLabelBeam.getFont().getSize()));
		arrayBox.getChildren().addAll(arrayTypeHeader,arrayTypeLabelBeam);
		arrayTypePane.getChildren().add(arrayBox);
		mainAngleInfoBeam = new Text("temp");
		arrayTypePane.getChildren().add(mainAngleInfoBeam);
		secAngleInfoBeam = new Text("temp");
		arrayTypePane.getChildren().add(secAngleInfoBeam);
		beamPane.getChildren().add(arrayTypePane);
		
		// create the table and define the columns
		beamTable = new TableView<BeamInfo>();
		beamTable.setEditable(false);
		beamTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		TableColumn<BeamInfo, Integer> beamNumCol = new TableColumn<>("Beam");
		beamNumCol.setMinWidth(20);
		beamNumCol.setCellValueFactory(new PropertyValueFactory<BeamInfo, Integer>("beamNum"));
		beamNumCol.setStyle("-fx-alignment: CENTER;");
		TableColumn<BeamInfo, Integer> angleCol = new TableColumn<>("Primary Angle");
		angleCol.setMinWidth(20);
		angleCol.setCellValueFactory(new PropertyValueFactory<BeamInfo, Integer>("angle"));
		angleCol.setStyle("-fx-alignment: CENTER;");
		TableColumn<BeamInfo, Integer> secAngleCol = new TableColumn<>("Sec Angle");
		secAngleCol.setMinWidth(20);
		secAngleCol.setCellValueFactory(new PropertyValueFactory<BeamInfo, Integer>("secAngle"));
		secAngleCol.setStyle("-fx-alignment: CENTER;");
		beamTable.getColumns().addAll(beamNumCol, angleCol, secAngleCol);
		beamTable.setItems(beamList);

		// add the table to the pane
		VBox.setVgrow(beamTable, Priority.ALWAYS);
		beamPane.getChildren().add(beamTable);

		// add a listener to the table so we know when the selection has changed
		beamTable.getSelectionModel().selectedItemProperty().addListener((obj, oldSel, newSel) -> {
			if (newSel != null) {
				
				synchronized (lineChart) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {

							// if we're not showing all the beams, then add the new selection to the chart first or else
							// it won't have a node number later.
							if(!showAllBeams) {
								lineChart.getData().add(newSel.getBeamPattern());
							}
							Node thisSeries = (Node) newSel.getBeamPattern().getNode();
							thisSeries.setStyle("-fx-stroke: orange; ");
							thisSeries.toFront();

							// change beampattern color of previously selected beam to gray if we're
							// showing all of the beams, or simply remove it from the chart
							if (oldSel!=null) {
								if (showAllBeams) {
									thisSeries = (Node) oldSel.getBeamPattern().getNode();
									thisSeries.setStyle("-fx-stroke: lightgrey; ");
								} else {
									lineChart.getData().remove(oldSel.getBeamPattern());
								}
							}

							// change beampattern color of new beam to orange
							selectedBeam = newSel;
							incomingAzimuth.setText(String.valueOf(selectedBeam.getAngle()));
							azSlider.setValue(selectedBeam.getAngle());
							incomingElevation.setText(String.valueOf(selectedBeam.getSecAngle()));
							elevSlider.setValue(selectedBeam.getSecAngle());
						}
					});
				}
			}
		});

		// create the text fields and button to add new beams
		addAngle = new TextField();
		addAngle.setPromptText("Primary Angle");
		addAngle.setMaxWidth(angleCol.getPrefWidth());
		addAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("\\d-")) {
//	            	addAngle.setText(newValue.replaceAll("[^\\d]", ""));
//	            }
	        }
	    });
		addSecAngle = new TextField();
		addSecAngle.setPromptText("Secondary Angle");
		addSecAngle.setMaxWidth(secAngleCol.getPrefWidth());
		addSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d-")) {
	            	addSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });

		addButton = new Button("Add Beam");
		addButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				if (addSecAngle.isDisabled()) {
					addSecAngle.setText("0");
				}
				beamList.add(new BeamInfo(
						nextBeamNum++,
						Integer.parseInt(addAngle.getText()),
						Integer.parseInt(addSecAngle.getText()),
						WindowFunction.RECTANGULAR,
						fullFreqRange
						));
				addAngle.clear();
				beamTable.getSelectionModel().select(beamTable.getItems().size()-1);
			}
		});		
		BooleanBinding angleField = Bindings.isEmpty(addAngle.textProperty());
		addButton.disableProperty().bind(angleField);
		addBox = new HBox();
		addBox.setSpacing(5);
		addBox.setPadding(new Insets(10, 0, 5, 0));
		addBox.getChildren().addAll(addAngle, addSecAngle, addButton);

		// add the add-button to the pane
		VBox.setVgrow(addBox, Priority.NEVER);
		beamPane.getChildren().add(addBox);

		// create a few more buttons
		final Button delButton = new Button("Delete Beam");
		delButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				lineChart.getData().remove(beamTable.getSelectionModel().getSelectedItem().getBeamPattern());
				beamList.remove(beamTable.getSelectionModel().getSelectedItem());
				beamTable.getSelectionModel().selectPrevious();
			}

		});
		delButton.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		final Button defButton = new Button("Create Default Beams");
		defButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				createDefaults();
			}
		});
		FlowPane buttonsPane = new FlowPane();
//		buttonsPane.setPadding(new Insets(5, 5, 5, 5));
		buttonsPane.setVgap(5);
		buttonsPane.setHgap(5);
		buttonsPane.setPrefWrapLength(beamTable.getWidth());
		buttonsPane.getChildren().addAll(delButton, defButton);
		VBox.setVgrow(buttonsPane, Priority.NEVER);
		beamPane.getChildren().add(buttonsPane);

		// return the pane
		return beamPane;
	}

	/**
	 * Create a list of default beams, evenly spread between 0 and 180 degrees.  For these,
	 * the elevation is 0 deg
	 */
	public void createDefaults() {
		
		// Clear the line chart data.  Synchronize on
		// line chart so that we don't start adding beams to it (the code after this
		// synchronized block) before it's properly cleared
		synchronized (lineChart) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					lineChart.getData().clear();
				}
			});
		}
		
		int nBeams = PamUtils.getNumChannels(curParams.getChannelMap());
		beamList.clear();
		int beamIdx = 0;
		for (int i=0; i<nBeams; i++) {
			if (i==0) {
				beamList.add(new BeamInfo(beamIdx, 0, 0, WindowFunction.RECTANGULAR, fullFreqRange));
				beamIdx++;
			} else if (i==1) {
				// don't do anything here - add 180 deg to the end of the list instead
			} else {
				double curAngle = Math.toDegrees(Math.acos(1.-2.*beamIdx/(nBeams-1)));
				beamList.add(new BeamInfo(beamIdx, (int) curAngle, 0, WindowFunction.RECTANGULAR, fullFreqRange));
				beamIdx++;
			}
		}
		
		// if we have more than just a beam at 0 deg, add 180 deg to the end of the list
		if (nBeams>1) {
			beamList.add(new BeamInfo(beamIdx, 180, 0, WindowFunction.RECTANGULAR, fullFreqRange));
		}

		nextBeamNum = beamList.size()+1;
		beamTable.getSelectionModel().selectFirst();
	}

	/**
	 * 
	 * @return
	 */
	public Node createBeamPatternPane() {
		
		VBox chartPane = new VBox();

		//defining the chart axes
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
//		xAxis.setLabel("Angle of Arrival [deg], 0\u00b0 = heading");
		xAxis.setLabel("Primary Angle [deg]");
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(180);
		xAxis.setTickUnit(15);
		yAxis.setLabel("Gain [dB]");
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(-50);
		yAxis.setUpperBound(0);

		//creating the chart
		lineChart =	new LineChart<Number,Number>(xAxis,yAxis);
		lineChart.setTitle("Beam Pattern at " + frequency + " Hz and Secondary Angle = 0 deg");
		lineChart.setAnimated(false);
		lineChart.setLegendVisible(false);

		lineChart.setCreateSymbols(false);
		VBox.setVgrow(lineChart, Priority.ALWAYS);
		chartPane.getChildren().add(lineChart);

		// set up the grid layout
		grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));

		// add some text and text boxes
		grid.add(new Text("Primary Angle"), 0, 2);
		incomingAzimuth = new TextField(String.valueOf(90));
		incomingAzimuth.setMaxWidth(40);
		incomingAzimuth.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		grid.add(incomingAzimuth, 1, 2);
		azSlider = new Slider(0, 180, 90);
		azSlider.valueProperty().addListener(new AzListener());
		azSlider.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		grid.add(azSlider, 2, 2);
		incomingElevationLabel = new Label("Secondary Angle");
		grid.add(incomingElevationLabel, 0, 3);
		incomingElevation = new TextField(String.valueOf(90));
		incomingElevation.setMaxWidth(40);
//		incomingElevation.disableProperty().bind(Bindings.and(beamTable.getSelectionModel().getSelectedItems().isEmpty(),arrayShape>2));
		grid.add(incomingElevation, 1, 3);
		elevSlider = new Slider(-90, 0, 0);
		elevSlider.valueProperty().addListener(new ElevListener());
//		elevSlider.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		grid.add(elevSlider, 2, 3);
		grid.add(new Text("Source Frequency"), 0, 4);
		sourceFreq = new TextField(String.valueOf(frequency));
		sourceFreq.setMaxWidth(40);
		sourceFreq.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		grid.add(sourceFreq, 1, 4);
		freqSlider = new Slider(-1, 5, Math.log10(frequency));
		freqSlider.valueProperty().addListener(new FreqListener());
		freqSlider.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		grid.add(freqSlider, 2, 4);
		grid.add(new Text("Display All Beam Patterns"), 0, 6);
		CheckBox showAllToggle=new CheckBox(); 
		showAllToggle.selectedProperty().set(showAllBeams);
		showAllToggle.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		showAllToggle.selectedProperty().addListener((listen)->{
			showAllBeams=showAllToggle.selectedProperty().getValue();
			toggleBeamDisplay();
		});
		GridPane.setHalignment(showAllToggle, HPos.LEFT);
		grid.add(showAllToggle, 1, 6, 1, 1);

		// add actionlisteners to the text fields
		incomingAzimuth.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					int newVal = (int) Double.parseDouble(incomingAzimuth.getText());	// convert to double first then cast to int, to prevent probs with user entering double val
					if (newVal<minPrime) {
						incomingAzimuth.setText(String.valueOf(minPrime));
						newVal=minPrime;
					}
					else if (newVal>maxPrime) {
						incomingAzimuth.setText(String.valueOf(maxPrime));
						newVal=maxPrime;
					}
					azimuthChanged(newVal);
					azSlider.setValue(newVal);
				}
			}
		});
//		incomingAzimuth.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				int newVal = (int) Double.parseDouble(((TextField) event.getTarget()).getText());	// convert to double first then cast to int, to prevent probs with user entering double val
//				azimuthChanged(newVal);
//				azSlider.setValue(newVal);
//			}
//		});
		incomingElevation.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					int newVal = (int) Double.parseDouble(incomingElevation.getText());	// convert to double first then cast to int, to prevent probs with user entering double val
					if (newVal<minSec) {
						incomingElevation.setText(String.valueOf(minSec));
						newVal=minSec;
					}
					else if (newVal>maxSec) {
						incomingElevation.setText(String.valueOf(maxSec));
						newVal=maxSec;
					}
					elevChanged(newVal);
					elevSlider.setValue(newVal);
				}
			}
		});
//		incomingElevation.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				int newVal = (int) Double.parseDouble(((TextField) event.getTarget()).getText());	// convert to double first then cast to int, to prevent probs with user entering double val
//				elevChanged(newVal);
//				elevSlider.setValue(newVal);
//			}
//		});
		sourceFreq.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					int newVal = (int) Double.parseDouble(sourceFreq.getText());	// convert to double first then cast to int, to prevent probs with user entering double val
					if (newVal<fullFreqRange[0]) {
						sourceFreq.setText(String.valueOf(fullFreqRange[0]));
						newVal=(int) fullFreqRange[0];
					}
					else if (newVal>fullFreqRange[1]) {
						sourceFreq.setText(String.valueOf(fullFreqRange[1]));
						newVal=(int) fullFreqRange[1];
					}
					freqChanged(newVal);
					freqSlider.setValue(Math.log10(newVal));
				}
			}
		});
//		sourceFreq.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				int newVal = (int) Double.parseDouble(((TextField) event.getTarget()).getText());	// convert to double first then cast to int, to prevent probs with user entering double val
//				freqChanged(newVal);
//				freqSlider.setValue(Math.log10(newVal));
//			}
//		});
		VBox.setVgrow(grid, Priority.NEVER);
		chartPane.getChildren().add(grid);
		return chartPane;
	}

	/**
	 * Toggle the display between showing all beams and only showing the currently selected beam
	 */
	private void toggleBeamDisplay() {
		for (BeamInfo aBeam : beamList) {
			if (aBeam!=beamTable.getSelectionModel().getSelectedItem()) {
				if (showAllBeams) {
					lineChart.getData().add(aBeam.getBeamPattern());
					((Node) aBeam.getBeamPattern().getNode()).setStyle("-fx-stroke: lightgrey; ");
				} else {
					lineChart.getData().remove(aBeam.getBeamPattern());
				}
			}
		}
		((Node) beamTable.getSelectionModel().getSelectedItem().getBeamPattern().getNode()).toFront();
	}

	/**
	 * ChangeListener for the azimuth slider
	 * @author mo55
	 *
	 */
	private class AzListener implements ChangeListener<Number> {

		/**
		 * Set the text box field and run the azimuthChanged event
		 */
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			incomingAzimuth.textProperty().setValue(String.valueOf((int) azSlider.getValue()));
			azimuthChanged((int) azSlider.getValue());
		}
	}

	/**
	 * ChangeListener for the elevation slider
	 * @author mo55
	 *
	 */
	private class ElevListener implements ChangeListener<Number> {

		/**
		 * Set the text box field and run the azimuthChanged event
		 */
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			incomingElevation.textProperty().setValue(String.valueOf((int) elevSlider.getValue()));
			elevChanged((int) elevSlider.getValue());
		}
	}


	/**
	 * ChangeListener for the frequency slider
	 * @author mo55
	 *
	 */
	private class FreqListener implements ChangeListener<Number> {

		/**
		 * Set the text box field and run the azimuthChanged event
		 */
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			int newVal = (int) Math.pow(10, freqSlider.getValue());
			sourceFreq.textProperty().setValue(String.valueOf(newVal));
			freqChanged(newVal);
		}
	}


	/**
	 * @param newFreq 
	 * 
	 */
	protected void freqChanged(int newFreq) {
		frequency = newFreq;
		lineChart.setTitle("Beam Pattern at " + frequency + " Hz and Secondary Angle = 0 deg");
		
		// loop through all beams and update beam patterns
		for (BeamInfo aBeam : beamList) {
			aBeam.calculateBeamPattern();
		}
	}

	private void azimuthChanged(int newAngle) {
		if (selectedBeam != null) {
			selectedBeam.setAngle(newAngle);
			selectedBeam.calculateBeamPattern();
		}
		beamTable.refresh();
	}

	private void elevChanged(int newAngle) {
		if (selectedBeam != null) {
			selectedBeam.setSecAngle(newAngle);
			selectedBeam.calculateBeamPattern();
		}
		beamTable.refresh();
	}

	/**
	 * @return
	 */
//	private Node createBeamogramPane() {
//		PamVBox beamogramPane = new PamVBox();
//		beamogramPane.setSpacing(50);
//		beamogramPane.setPadding(new Insets(10));
//		beamogramPane.setAlignment(Pos.TOP_LEFT);
//		beamogramPane.setFillWidth(false);
//
//		// Array information label
//		Text arrayType = new Text("Valid angle values are given below, based on a " + ArrayManager.getArrayTypeString(arrayShape));
//		
//		// beamogram on/off toggle
//		PamVBox togPane = new PamVBox();
//		togPane.setAlignment(Pos.CENTER_LEFT);
//		beamogramToggle = new ToggleSwitch("Calculate BeamOGram"); 
//		beamogramToggle.selectedProperty().set(false);
//		togPane.getChildren().add(beamogramToggle);
//		beamogramPane.getChildren().add(togPane);
//		
//		// main angle min/max/step values
//		mainAnglePane = new PamVBox();
//		mainAnglePane.setAlignment(Pos.CENTER);
//		mainAnglePane.setSpacing(5);
//		Text angRange = new Text("Primary Angle Range:");
//		angRange.setTextAlignment(TextAlignment.CENTER);
//		mainAnglePane.getChildren().add(angRange);
//		PamHBox mainAngleRangePane = new PamHBox();
//		mainAngleRangePane.setSpacing(10);
//		minMainAngle = new TextField();
//		minMainAngle.setPromptText("Min");
//		minMainAngle.setMaxWidth(60);
//		minMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		minMainAngle.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("\\d*")) {
////	            	minMainAngle.setText(newValue.replaceAll("[^\\d]", ""));
//	            }
//	        }
//	    });
//		mainAngleRangePane.getChildren().add(minMainAngle);
//		maxMainAngle = new TextField();
//		maxMainAngle.setPromptText("Max");
//		maxMainAngle.setMaxWidth(60);
//		maxMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		maxMainAngle.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("\\d*")) {
//	            	maxMainAngle.setText(newValue.replaceAll("[^\\d]", ""));
//	            }
//	        }
//	    });
//		mainAngleRangePane.getChildren().add(maxMainAngle);
//		stepMainAngle = new TextField();
//		stepMainAngle.setPromptText("Step");
//		stepMainAngle.setMaxWidth(60);
//		stepMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		stepMainAngle.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("\\d*")) {
//	            	stepMainAngle.setText(newValue.replaceAll("[^\\d]", ""));
//	            }
//	        }
//	    });
//		mainAngleRangePane.getChildren().add(stepMainAngle);
//		mainAnglePane.getChildren().add(mainAngleRangePane);
//		PamTitledBorderPane mainAngleBorder = new PamTitledBorderPane("Primary Angle Range", mainAngleRangePane);
//		beamogramPane.getChildren().add(mainAngleBorder);
//		
//		// don't allow users to set the angles yet.  If there are multiple beamograms created, they each have to have the same size
//		// of data in the PamDataUnits in order to be added to the same PamDataBlock.  The number of units is dependent on the angles
//		// and step size, so it would be very easy to mess it up if the users are able to change them.  The error gets thrown in
//		// dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX.fillPowerSpecLine() (line 332 - magData is the actual data unit, and
//		// dataLength is the number of angles set.  There are a number of different ways this could be fixed - add the angle info to the
//		// PamDataUnits, interpolate missing angles or fill with blanks, etc.  But that will have to be done later.
//		
//		// secondary angle min/max/step values
//		secAnglePane = new PamVBox();
//		secAnglePane.setAlignment(Pos.CENTER);
//		secAnglePane.setSpacing(5);
//		Text secAngRange = new Text("Secondary Angle Range:");
//		secAngRange.setTextAlignment(TextAlignment.CENTER);
//		secAnglePane.getChildren().add(secAngRange);
//		PamHBox secAngleRangePane = new PamHBox();
//		secAngleRangePane.setSpacing(10);
//		minSecAngle = new TextField();
//		minSecAngle.setPromptText("Min");
//		minSecAngle.setMaxWidth(60);
//		minSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		minSecAngle.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("\\d*")) {
//	            	minSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
//	            }
//	        }
//	    });
//		secAngleRangePane.getChildren().add(minSecAngle);
//		maxSecAngle = new TextField();
//		maxSecAngle.setPromptText("Max");
//		maxSecAngle.setMaxWidth(60);
//		maxSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		maxSecAngle.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("\\d*")) {
//	            	maxSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
//	            }
//	        }
//	    });
//		secAngleRangePane.getChildren().add(maxSecAngle);
//		stepSecAngle = new TextField();
//		stepSecAngle.setPromptText("Step");
//		stepSecAngle.setMaxWidth(60);
//		stepSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		stepSecAngle.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("\\d*")) {
//	            	stepSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
//	            }
//	        }
//	    });
//		secAngleRangePane.getChildren().add(stepSecAngle);
//		secAnglePane.getChildren().add(secAngleRangePane);
//		beamogramPane.getChildren().add(secAnglePane);
//		
//		// min/max frequency
//		PamVBox freqPane = new PamVBox();
//		freqPane.setAlignment(Pos.CENTER);
//		freqPane.setSpacing(5);
//		Text freqRange = new Text("Frequency Range:");
//		freqRange.setTextAlignment(TextAlignment.CENTER);
//		freqPane.getChildren().add(freqRange);
//		PamHBox freqRangePane = new PamHBox();
//		freqRangePane.setSpacing(10);
//		minFreq = new TextField();
//		minFreq.setPromptText("Min");
//		minFreq.setMaxWidth(60);
//		minFreq.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		minFreq.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("[\\d\\.]*")) {
//	            	minFreq.setText(newValue.replaceAll("[^[\\d\\.]]", ""));
//	            }
//	        }
//	    });
//		freqRangePane.getChildren().add(minFreq);
//		maxFreq = new TextField();
//		maxFreq.setPromptText("Max");
//		maxFreq.setMaxWidth(60);
//		maxFreq.disableProperty().bind(beamogramToggle.selectedProperty().not());
//		maxFreq.textProperty().addListener(new ChangeListener<String>() {
//	        @Override
//	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//	            if (!newValue.matches("[\\d\\.]*")) {
//	            	maxFreq.setText(newValue.replaceAll("[^[\\d\\.]]", ""));
//	            }
//	        }
//	    });
//		freqRangePane.getChildren().add(maxFreq);
//		freqPane.getChildren().add(freqRangePane);
//		beamogramPane.setSpacing(30);
//		beamogramPane.getChildren().add(freqPane);
//		
//		// window function
//		beamogramWindow = new ChoiceBox<>(FXCollections.observableArrayList(WindowFunction.getNames()));
//		beamogramWindow.getSelectionModel().selectFirst();
//		beamogramWindow.setMinWidth(200);
//
//		// return pane
//		return beamogramPane;
//	}

	/**
	 * @return
	 */
	protected Node createBeamogramPane() {
		// overall Pane
		PamVBox beamogramPane = new PamVBox();
		beamogramPane.setSpacing(20);
		beamogramPane.setPadding(new Insets(10));
		beamogramPane.setAlignment(Pos.TOP_LEFT);
		beamogramPane.setFillWidth(false);

		// Pane containing items in the left column (so that items don't stretch over entire beamogramPane)
		PamVBox leftColPane = new PamVBox();
		leftColPane.setAlignment(Pos.TOP_LEFT);
		leftColPane.setFillWidth(true);

		// Array information label
		PamHBox arrayBox = new PamHBox();
		arrayBox.setAlignment(Pos.CENTER_LEFT);
		arrayBox.setSpacing(10);
		Text arrayTypeHeader = new Text("Current Array type:");
		arrayTypeLabel = new Text(ArrayManager.getArrayTypeString(arrayShape));
		arrayTypeLabel.setFont(Font.font(arrayTypeLabel.getFont().getFamily(), FontWeight.BOLD, arrayTypeLabel.getFont().getSize()));
		arrayBox.getChildren().addAll(arrayTypeHeader,arrayTypeLabel);
		leftColPane.getChildren().add(arrayBox);
		leftColPane.setSpacing(20);
		
		// beamogram on/off toggle
		beamogramToggle = new CheckBox("Calculate BeamOGram");
		beamogramToggle.selectedProperty().addListener(new ChangeListener<Boolean>() {
	        public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
	        	if (newVal) {
	        		setAngleRange();
	        	} else {
	        		disableAll();
	        	}
	        }
	    });
		beamogramToggle.setSelected(false);
		leftColPane.getChildren().add(beamogramToggle);
		
		// ****************************************************************************************************
		// DANGER with setting angles: If there are multiple beamograms created, they each have to have the same size
		// of data in the PamDataUnits in order to be added to the same PamDataBlock.  The number of units is dependent on the angles
		// and step size, so it would be very easy to mess it up if the users are able to change them.  The error gets thrown in
		// dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX.fillPowerSpecLine() (line 332 - magData is the actual data unit, and
		// dataLength is the number of angles set.  There are a number of different ways this could be fixed - add the angle info to the
		// PamDataUnits, interpolate missing angles or fill with blanks, etc.
		// ****************************************************************************************************
		
		// main angle min/max/step values
		mainAnglePane = new GridPane();
		mainAnglePane.getColumnConstraints().add(new ColumnConstraints(80));
		mainAnglePane.setAlignment(Pos.CENTER_LEFT);
		mainAnglePane.setHgap(10);
		mainAnglePane.setVgap(5);
		minMainAngleLabel = new Label("Minimum");
		minMainAngleLabel.setLabelFor(minMainAngle);
		mainAnglePane.add(minMainAngleLabel, 0, 0);
		minMainAngle = new TextField();
		minMainAngle.setPromptText("Min");
		minMainAngle.setMaxWidth(60);
//		minMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d-")) {
	            	minMainAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		mainAnglePane.add(minMainAngle, 1, 0);
		maxMainAngleLabel = new Label("Maximum");
		maxMainAngleLabel.setLabelFor(maxMainAngle);
		mainAnglePane.add(maxMainAngleLabel, 0, 1);
		maxMainAngle = new TextField();
		maxMainAngle.setPromptText("Max");
		maxMainAngle.setMaxWidth(60);
//		maxMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d-")) {
	            	maxMainAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		mainAnglePane.add(maxMainAngle, 1, 1);
		stepMainAngleLabel = new Label("Step Size");
		stepMainAngleLabel.setLabelFor(stepMainAngle);
		mainAnglePane.add(stepMainAngleLabel, 0, 2);
		stepMainAngle = new TextField();
		stepMainAngle.setPromptText("Step");
		stepMainAngle.setMaxWidth(60);
//		stepMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		stepMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d")) {
	            	stepMainAngle.setText(newValue.replaceAll("[^\\d]", ""));
	            }
	        }
	    });
		mainAnglePane.add(stepMainAngle, 1, 2);
		mainAngleInfo = new Text("Temp");
		mainAngleInfo.setWrappingWidth(150);
		mainAnglePane.add(mainAngleInfo, 2, 0, 1, 3);
		PamTitledBorderPane mainAngleBorder = new PamTitledBorderPane("Primary Angle Range", mainAnglePane);
		leftColPane.getChildren().add(mainAngleBorder);
		
		// secondary angle min/max/step values
		secAnglePane = new GridPane();
		secAnglePane.getColumnConstraints().add(new ColumnConstraints(80));
		secAnglePane.setAlignment(Pos.CENTER_LEFT);
		secAnglePane.setHgap(10);
		secAnglePane.setVgap(5);
		minSecAngleLabel = new Label("Minimum");
		minSecAngleLabel.setLabelFor(minSecAngle);
		secAnglePane.add(minSecAngleLabel, 0, 0);
		minSecAngle = new TextField();
		minSecAngle.setPromptText("Min");
		minSecAngle.setMaxWidth(60);
//		minSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	minSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAnglePane.add(minSecAngle, 1, 0);
		maxSecAngleLabel = new Label("Maximum");
		maxSecAngleLabel.setLabelFor(maxSecAngle);
		secAnglePane.add(maxSecAngleLabel, 0, 1);
		maxSecAngle = new TextField();
		maxSecAngle.setPromptText("Max");
		maxSecAngle.setMaxWidth(60);
//		maxSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	maxSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAnglePane.add(maxSecAngle, 1, 1);
		stepSecAngleLabel = new Label("Step Size");
		stepSecAngleLabel.setLabelFor(stepSecAngle);
		secAnglePane.add(stepSecAngleLabel, 0, 2);
		stepSecAngle = new TextField();
		stepSecAngle.setPromptText("Step");
		stepSecAngle.setMaxWidth(60);
//		stepSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		stepSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	stepSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAnglePane.add(stepSecAngle, 1, 2);
		secAngleInfo = new Text("Temp");
		secAngleInfo.setWrappingWidth(150);
		secAnglePane.add(secAngleInfo, 2, 0, 1, 3);
		PamTitledBorderPane secAngleBorder = new PamTitledBorderPane("Secondary Angle Range", secAnglePane);
		leftColPane.getChildren().add(secAngleBorder);
		
		// min/max frequency
		GridPane freqPane = new GridPane();
		freqPane.getColumnConstraints().add(new ColumnConstraints(80));
		freqPane.setAlignment(Pos.CENTER_LEFT);
		freqPane.setHgap(10);
		freqPane.setVgap(5);
		Label minFreqLabel = new Label("Minimum"); 
		minFreqLabel.setLabelFor(minFreq);
		minFreqLabel.disableProperty().bind(beamogramToggle.selectedProperty().not());
		freqPane.add(minFreqLabel, 0, 0);
		minFreq = new TextField();
		minFreq.setPromptText("Min");
		minFreq.setMaxWidth(60);
		minFreq.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minFreq.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("[\\d\\.]*")) {
	            	minFreq.setText(newValue.replaceAll("[^[\\d\\.]]", ""));
	            }
	        }
	    });
		freqPane.add(minFreq, 1, 0);
		Label minHz = new Label("Hz");
		minHz.setLabelFor(minHz);
		minHz.disableProperty().bind(beamogramToggle.selectedProperty().not());
		freqPane.add(minHz, 2, 0);
		Label maxFreqLabel = new Label("Maximum");
		maxFreqLabel.setLabelFor(maxFreq);
		maxFreqLabel.disableProperty().bind(beamogramToggle.selectedProperty().not());
		freqPane.add(maxFreqLabel, 0, 1);
		maxFreq = new TextField();
		maxFreq.setPromptText("Max");
		maxFreq.setMaxWidth(60);
		maxFreq.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxFreq.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("[\\d\\.]*")) {
	            	maxFreq.setText(newValue.replaceAll("[^[\\d\\.]]", ""));
	            }
	        }
	    });
		freqPane.add(maxFreq, 1, 1);
		Label maxHz = new Label("Hz");
		maxHz.setLabelFor(minHz);
		maxHz.disableProperty().bind(beamogramToggle.selectedProperty().not());
		freqPane.add(maxHz, 2, 1);
		freqInfo = new Text("Temp");
		freqInfo.setWrappingWidth(150);
		freqPane.add(freqInfo, 3, 0, 1, 2);
		PamTitledBorderPane freqBorder = new PamTitledBorderPane("Frequency Range", freqPane);
		leftColPane.getChildren().add(freqBorder);
		beamogramPane.getChildren().add(leftColPane);
		
		// return pane
		return beamogramPane;
	}

	/**
	 * Get the current hydrophone locations, and set the field vector elementLocs
	 */
	public void prepareConstants() {
		// get a list of all channels in this channel map
		int[] channelList = PamUtils.getChannelArray(curParams.getChannelMap());
		
		// find the source Acquisition Process by querying the FFT Data Block the user has selected in the Source Tab
		AcquisitionProcess sourceProcess = null;
		if (acousticDataSource==null) {
			System.out.println("Error - FFT Source not set.  Cannot display Algorithm Settings dialog");
			return;
		}
		try {
			sourceProcess = (AcquisitionProcess) acousticDataSource.getSourceProcess();
		}
		catch (ClassCastException e) {
			String title = "Error finding Acquisition module";
			String msg = "There was an error trying to find the Acquisition module.  " +
					"The beamformer needs this information in order to run.  There will be no output until " +
					"a valid Acquisition module is added and the Pamguard run is restarted.";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, e);
			sourceProcess=null;
			e.printStackTrace();
			return;
		}
		if (sourceProcess==null) {
			String title = "Error finding Acquisition module";
			String msg = "There was an error trying to find the Acquisition module.  " +
					"The beamformer needs this information in order to run.  There will be no output until " +
					"a valid Acquisition module is added and the Pamguard run is restarted.";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, null);
			return;
		}

		// get the array shape and element locations
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		PamArray currentArray = arrayManager.getCurrentArray();
		int phones = acousticDataSource.getChannelListManager().channelIndexesToPhones(curParams.getChannelMap());
		arrayShape = arrayManager.getArrayShape(currentArray, phones);
		arrayMainAxes = arrayManager.getArrayDirections(currentArray, phones);
		elementLocs = new PamVector[channelList.length];
		long now = PamCalendar.getTimeInMillis();
		int hydrophoneMap = 0;
		for (int i=0; i<channelList.length; i++) {
			int hydrophone = sourceProcess.getAcquisitionControl().getChannelHydrophone(channelList[i]);
			elementLocs[i] = currentArray.getAbsHydrophoneVector(hydrophone, now);
			hydrophoneMap |= 1<<hydrophone;
		}
		/*
		 * Do some normalisation of those vectors. Start by making everything relative
		 * to the average position. 
		 */
		PamVector arrayCenter = PamVector.mean(elementLocs);
		for (int i = 0; i < channelList.length; i++) {
			elementLocs[i] = elementLocs[i].sub(arrayCenter);
		}
		
		// set the speed of sound and the full frequency range
		soundSpeed = currentArray.getSpeedOfSound();

		if (acousticDataSource != null) {
//			if (acousticDataSource instanceof FFTDataBlock) {
//				FFTDataBlock theFFTSource = (FFTDataBlock) acousticDataSource;
//			double hzPerBin = theFFTSource.getSampleRate() / theFFTSource.getFftLength();
//			}
			fullFreqRange[0] = 0;
			fullFreqRange[1] = acousticDataSource.getSampleRate()/2;
			freqInfo.setText("Frequency range must be between "	+ fullFreqRange[0] + " and " + fullFreqRange[1] + " Hz");
			freqInfo.setVisible(true);
			freqSlider.setMin(Math.log10(fullFreqRange[0]+.1)); // add 0.1 to offset the 0 Hz min, which can't be done on a log scale
			freqSlider.setMax(Math.log10(fullFreqRange[1]));
		} else {
			freqInfo.setVisible(false);
		}
	}
	
	/**
	 * Take settings from the pane and save them into the parameters object
	 * @param p settings passed into this object by PamDialogFX2AWT - ignored
	 */
	@Override
	public BeamAlgorithmParams getParams(BeamAlgorithmParams p) {
		
		// save the current beam information into the parameters
		curParams.setNumBeams(beamList.size());
		if (beamList.size()>0) {
			int[] beamAngles = new int[beamList.size()];
			int[] beamSecAngles = new int[beamList.size()];
			double[][] beamFreqs = new double[beamList.size()][2];
			if (elementLocs != null) {
				for (int i=0; i<beamList.size(); i++) {
					beamAngles[i]=beamList.get(i).getAngle();
					beamSecAngles[i]=beamList.get(i).getSecAngle();
					beamFreqs[i]=beamList.get(i).getFreqs();
				}
			}
			curParams.setHeadings(beamAngles);
			curParams.setSlants(beamSecAngles);
			curParams.setFreqRange(beamFreqs);
		}
		
		// save additional beamogram information.  Check first if the values are ok.  If not, return a null to cancel
		// the OK button press
		int numBeamograms=0;
		if (checkBeamogramStatus()==BeamgramStatus.BEAMOGRAMGOOD) {
			numBeamograms = 1;
		} else if (checkBeamogramStatus()==BeamgramStatus.BEAMOGRAMBAD) {
			return null;
		}
		curParams.setNumBeamogram(numBeamograms);
		if (numBeamograms==1) {

			double[] beamogramFreqs = new double[2];
			beamogramFreqs[0] = Double.parseDouble(minFreq.textProperty().getValue());
			beamogramFreqs[1] = Double.parseDouble(maxFreq.textProperty().getValue());
			curParams.setBeamOGramFreqRange(beamogramFreqs);
			
			int[] beamogramAngles = new int[3];
			beamogramAngles[0] = Integer.parseInt(minMainAngle.textProperty().getValue());
			beamogramAngles[1] = Integer.parseInt(maxMainAngle.textProperty().getValue());
			beamogramAngles[2] = Integer.parseInt(stepMainAngle.textProperty().getValue());
			curParams.setBeamOGramAngles(beamogramAngles);
			
			
			int[] beamogramSlants = {0,0,1};
			if (!minSecAngle.isDisabled()) {
				beamogramSlants[0] = Integer.parseInt(minSecAngle.textProperty().getValue());
				beamogramSlants[1] = Integer.parseInt(maxSecAngle.textProperty().getValue());
				beamogramSlants[2] = Integer.parseInt(stepSecAngle.textProperty().getValue());
			}
			curParams.setBeamOGramSlants(beamogramSlants);
		}
		
		// return the parameters
		return curParams;
	}

	
	/**
	 * Test whether the beamogram data is valid.  This should only be called if the beamogram check box
	 * is checked
	 * 
	 * @return
	 */
	public BeamgramStatus checkBeamogramStatus() {
		if (!beamogramToggle.selectedProperty().getValue()) {
			return BeamgramStatus.BEAMOGRAMNOTUSED;
		}
		if (minMainAngle.getText().isEmpty() ||
				Double.parseDouble(minMainAngle.textProperty().getValue())<minPrime ||
				maxMainAngle.getText().isEmpty() ||
				Double.parseDouble(maxMainAngle.textProperty().getValue())>maxPrime ||
				stepMainAngle.getText().isEmpty() ||
				Double.parseDouble(stepMainAngle.textProperty().getValue())<=0 ||
				Double.parseDouble(minMainAngle.textProperty().getValue())>Double.parseDouble(maxMainAngle.textProperty().getValue())
				) {
			SwingFXDialogWarning.showWarning(this, "Beamogram Parameters", "Warning - Beamogram main angle parameters not correctly configured");
			return BeamgramStatus.BEAMOGRAMBAD;
		}
		if (!minSecAngle.isDisabled()) {
			if (minSecAngle.getText().isEmpty() ||
				maxSecAngle.getText().isEmpty() ||
				stepSecAngle.getText().isEmpty() ||
				Double.parseDouble(stepSecAngle.textProperty().getValue())<=0) {
				SwingFXDialogWarning.showWarning(this, "Beamogram Parameters", "Warning - Beamogram secondary angle parameters not correctly configured");
				return BeamgramStatus.BEAMOGRAMBAD;
			}
			// special case: Planar array angles can go from -90 to 0, OR 0 to 90.  Need to check both possibilities
			if (arrayShape==ArrayManager.ARRAY_TYPE_PLANE) {
				if ( (Double.parseDouble(minSecAngle.textProperty().getValue())<0 && Double.parseDouble(maxSecAngle.textProperty().getValue())>0) 
						||
					 (Double.parseDouble(minSecAngle.textProperty().getValue())>=0 && Double.parseDouble(maxSecAngle.textProperty().getValue())>90) 
					    ||
					    Double.parseDouble(minSecAngle.textProperty().getValue())<-90 ||
					    Double.parseDouble(minSecAngle.textProperty().getValue())>90 ||
  					  Double.parseDouble(minSecAngle.textProperty().getValue())>Double.parseDouble(maxSecAngle.textProperty().getValue())) {
					SwingFXDialogWarning.showWarning(this, "Beamogram Parameters", "Warning - Beamogram secondary angle parameters not correctly configured");
					return BeamgramStatus.BEAMOGRAMBAD;
				}
			} else {
				if (Double.parseDouble(minSecAngle.textProperty().getValue())<minSec ||
						Double.parseDouble(maxSecAngle.textProperty().getValue())>maxSec ||
						Double.parseDouble(minSecAngle.textProperty().getValue())>Double.parseDouble(maxSecAngle.textProperty().getValue())) {
					SwingFXDialogWarning.showWarning(this, "Beamogram Parameters", "Warning - Beamogram secondary angle parameters not correctly configured");
					return BeamgramStatus.BEAMOGRAMBAD;
				}
			}
		}
		if (minFreq.getText().isEmpty() ||
				Double.parseDouble(minFreq.textProperty().getValue())<fullFreqRange[0] ||
				maxFreq.getText().isEmpty() ||
				Double.parseDouble(maxFreq.textProperty().getValue())>fullFreqRange[1] ||
				Double.parseDouble(minFreq.textProperty().getValue())>Double.parseDouble(maxFreq.textProperty().getValue())
				) {
			SwingFXDialogWarning.showWarning(this, "Beamogram Parameters", "Warning - Beamogram frequency parameters not correctly configured");
			return BeamgramStatus.BEAMOGRAMBAD;
		}
		return BeamgramStatus.BEAMOGRAMGOOD;
	}

	/**
	 * Take settings from the parameters object and load them into the pane
	 */
	@Override
	public void setParams(BeamAlgorithmParams newParams) {
		curParams = newParams;
//		curParams = newParams.clone();
		
		// get the hydrophone locations and set constants
		this.prepareConstants();
		this.setAngleRange();

		// Clear the line chart data.  Synchronize on
		// line chart so that we don't start adding beams to it (the code after this
		// synchronized block) before it's properly cleared
		synchronized (lineChart) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					lineChart.getData().clear();
				}
			});
		}
		
		// loop through the beam list and create new beams for each.
		beamList.clear();
		int numBeams = newParams.getNumBeams();
		if (numBeams>0) {
			for (int i=0; i<numBeams; i++) {
				int primeAngle = newParams.getHeadings()[i];
				primeAngle = Math.max(primeAngle, minPrime);
				primeAngle = Math.min(primeAngle, maxPrime);
				int secAngle = newParams.getSlants()[i];
				secAngle = Math.max(secAngle, minSec);
				secAngle = Math.min(secAngle, maxSec);
//				BeamInfo newBeam = new BeamInfo(i, primeAngle, secAngle, WindowFunction.RECTANGULAR, newParams.getFreqRange()[i]);
				BeamInfo newBeam = new BeamInfo(i, primeAngle, secAngle, WindowFunction.RECTANGULAR, fullFreqRange); // since we're not allowing user to set freq range, always use the updated range calc
				beamList.add(newBeam);
			}
			beamTable.getSelectionModel().selectFirst();
		}
		
		// set the beamogram information, or clear it if there is no beamogram
		setBeamogramData();
		
		// hide/show the tabs
		this.setTabVisibility();
	}
	
	/**
	 * Fills in the data on the beamogram tab, or clears it
	 */
	public void setBeamogramData() {
		if (curParams.getNumBeamogram()==1) {
			beamogramToggle.setSelected(true);
//			setAngleRange();
//			beamogramToggle.selectedProperty().set(true);
			double[] beamogramFreqs=curParams.getBeamOGramFreqRange();
			if (beamogramFreqs == null || beamogramFreqs[1] == 0) {
				beamogramFreqs = fullFreqRange;
			}
//			double[] beamogramFreqs=fullFreqRange; // since we're not allowing user to set freq range, always use the updated range calc
			minFreq.textProperty().set(String.valueOf(beamogramFreqs[0]));
			maxFreq.textProperty().set(String.valueOf(beamogramFreqs[1]));
			int[] beamogramAngles = curParams.getBeamOGramAngles();
			if (beamogramAngles != null && beamogramAngles.length == 3) {
				minMainAngle.textProperty().set(String.valueOf(beamogramAngles[0]));
				maxMainAngle.textProperty().set(String.valueOf(beamogramAngles[1]));
				stepMainAngle.textProperty().set(String.valueOf(beamogramAngles[2]));
			}
			beamogramAngles = curParams.getBeamOGramSlants();
			if (beamogramAngles != null && beamogramAngles.length == 3) {
				minSecAngle.textProperty().set(String.valueOf(beamogramAngles[0]));
				maxSecAngle.textProperty().set(String.valueOf(beamogramAngles[1]));
				stepSecAngle.textProperty().set(String.valueOf(beamogramAngles[2]));
			}
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					beamogramToggle.setSelected(false);
					disableAll();
					minMainAngle.clear();
					maxMainAngle.clear();
					stepMainAngle.clear();
					minSecAngle.clear();
					maxSecAngle.clear();
					stepSecAngle.clear();
					minFreq.clear();
					maxFreq.clear();
				}
			});
		}
	}

	/**
	 * Adds/Removes the beam and beamogram tabs, based on the current parameters
	 */
	public void setTabVisibility() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				// remove tabs first, so we don't accidentally end up with 2 of the same
				topPane.getTabs().remove(beamsTabPane);
				topPane.getTabs().remove(beamogramTabPane);

				// add the beams pane if we need to
				if (curParams.isCanBeam()) {
					topPane.getTabs().add(beamsTabPane);
				}
				else {
					beamogramToggle.setSelected(true);
					beamogramToggle.setDisable(true);
				}

				// add the beamogram pane if we need to
				if (curParams.isCanBeamogram()) {
					topPane.getTabs().add(beamogramTabPane);
				}
				
			}
		});
	}

	/**
	 * Sets the valid angle range, based on the array shape
	 */
	public void setAngleRange() {
		switch (arrayShape) {
		case ArrayManager.ARRAY_TYPE_LINE:
			minMainAngle.setDisable(false);
			minMainAngleLabel.setDisable(false);
			maxMainAngle.setDisable(false);
			maxMainAngleLabel.setDisable(false);
			stepMainAngle.setDisable(false);
			stepMainAngleLabel.setDisable(false);
			minPrime = 0;
			maxPrime = 180;
			minSecAngle.setDisable(true);
			minSecAngleLabel.setDisable(true);
			maxSecAngle.setDisable(true);
			maxSecAngleLabel.setDisable(true);
			stepSecAngle.setDisable(true);
			stepSecAngleLabel.setDisable(true);
			incomingElevation.setDisable(true);
			incomingElevationLabel.setDisable(true);
			elevSlider.setDisable(true);
			addSecAngle.setDisable(true);
			break;
			
		case ArrayManager.ARRAY_TYPE_PLANE:
			minMainAngle.setDisable(false);
			minMainAngleLabel.setDisable(false);
			maxMainAngle.setDisable(false);
			maxMainAngleLabel.setDisable(false);
			stepMainAngle.setDisable(false);
			stepMainAngleLabel.setDisable(false);
			minPrime = -180;
			maxPrime = 180;
			minSecAngle.setDisable(false);
			minSecAngleLabel.setDisable(false);
			maxSecAngle.setDisable(false);
			maxSecAngleLabel.setDisable(false);
			stepSecAngle.setDisable(false);
			stepSecAngleLabel.setDisable(false);
			minSec = -90;		// for a linear array, the secondary angle range is either -90 to 0 or 0 to 90.
			maxSec = 90;		// use the extreme limits here, and do more extensive error checking in checkBeamogramStatus
			incomingElevation.setDisable(false);
			incomingElevationLabel.setDisable(false);
			elevSlider.setDisable(false);
			addSecAngle.setDisable(false);
			break;
			
		case ArrayManager.ARRAY_TYPE_VOLUME:
			minMainAngle.setDisable(false);
			minMainAngleLabel.setDisable(false);
			maxMainAngle.setDisable(false);
			maxMainAngleLabel.setDisable(false);
			stepMainAngle.setDisable(false);
			stepMainAngleLabel.setDisable(false);
			minPrime = -180;
			maxPrime = 180;
			minSecAngle.setDisable(false);
			minSecAngleLabel.setDisable(false);
			maxSecAngle.setDisable(false);
			maxSecAngleLabel.setDisable(false);
			stepSecAngle.setDisable(false);
			stepSecAngleLabel.setDisable(false);
			minSec = -90;
			maxSec = 90;
			incomingElevation.setDisable(false);
			incomingElevationLabel.setDisable(false);
			elevSlider.setDisable(false);
			addSecAngle.setDisable(false);
			break;
			
		// if it's not a line, plane or volume array, disable everything and warn the user
		default:
			minMainAngle.setDisable(true);
			minMainAngleLabel.setDisable(true);
			maxMainAngle.setDisable(true);
			maxMainAngleLabel.setDisable(true);
			stepMainAngle.setDisable(true);
			stepMainAngleLabel.setDisable(true);
			minSecAngle.setDisable(true);
			minSecAngleLabel.setDisable(true);
			maxSecAngle.setDisable(true);
			maxSecAngleLabel.setDisable(true);
			stepSecAngle.setDisable(true);
			stepSecAngleLabel.setDisable(true);
			incomingElevation.setDisable(true);
			incomingElevationLabel.setDisable(true);
			elevSlider.setDisable(true);
			addSecAngle.setDisable(true);
		}	
		
		// set the slider limits to the min/max values
		azSlider.setMin(minPrime);
		azSlider.setMax(maxPrime);
		elevSlider.setMin(minSec);
		elevSlider.setMax(maxSec);
		
		// change the labels to reflect the current min/max
		setArrayTypeLables();
	}
	
	public void setArrayTypeLables() {
		arrayTypeLabel.setText(ArrayManager.getArrayTypeString(arrayShape));
		arrayTypeLabelBeam.setText(ArrayManager.getArrayTypeString(arrayShape));
		mainAngleInfo.setOpacity(1);
		secAngleInfo.setOpacity(1);
		mainAngleInfo.setText("Angles must be between "
				+ minPrime + "\u00B0 and "
				+ maxPrime + "\u00B0");
		mainAngleInfoBeam.setText("Primary Angles must be between "
				+ minPrime + "\u00B0 and "
				+ maxPrime + "\u00B0");
		if (arrayShape == ArrayManager.ARRAY_TYPE_PLANE) {
			secAngleInfo.setText("Angles must be between -90\u00B0 and 0\u00B0, or between 0\u00B0 and 90\u00B0");
			secAngleInfoBeam.setText("Secondary angles must be between -90\u00B0 and 0\u00B0, or between 0\u00B0 and 90\u00B0");
		} else if (arrayShape == ArrayManager.ARRAY_TYPE_VOLUME) {
			secAngleInfo.setText("Angles must be between "
					+ minSec + "\u00B0 and "
					+ maxSec + "\u00B0");
			secAngleInfoBeam.setText("Secondary angles must be between "
					+ minSec + "\u00B0 and "
					+ maxSec + "\u00B0");
		} else {
			secAngleInfo.setText("No secondary angle available for this type of array");
			secAngleInfoBeam.setText("No secondary angle available for this type of array");
		}
	}
	
	/**
	 * 
	 */
	protected void disableAll() {
		minMainAngle.setDisable(true);
		minMainAngleLabel.setDisable(true);
		maxMainAngle.setDisable(true);
		maxMainAngleLabel.setDisable(true);
		stepMainAngle.setDisable(true);
		stepMainAngleLabel.setDisable(true);
		minSecAngle.setDisable(true);
		minSecAngleLabel.setDisable(true);
		maxSecAngle.setDisable(true);
		maxSecAngleLabel.setDisable(true);
		stepSecAngle.setDisable(true);
		stepSecAngleLabel.setDisable(true);
		mainAngleInfo.setOpacity(0.4);
		secAngleInfo.setOpacity(0.4);
	}
	
	/**
	 * @return the fftSource
	 */
	public AcousticDataBlock getDataSource() {
		return acousticDataSource;
	}

	/**
	 * @param fftSource the fftSource to set
	 */
	public void setDataSource(AcousticDataBlock fftSource) {
		this.acousticDataSource = fftSource;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getName()
	 */
	@Override
	public String getName() {
//		return staticProperties.getName();
		return ("Group " + String.valueOf(curParams.getGroupNumber()) + " Parameters");
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getContentNode()
	 */
	@Override
	public Node getContentNode() {
		return mainPane;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#paneInitialized()
	 */
	@Override
	public void paneInitialized() {
	}


	/**
	 * Inner class, containing beam information for each beam in the spreadsheet
	 * 
	 * @author mo55
	 *
	 */
	public class BeamInfo {

		private SimpleIntegerProperty beamNum;
		private SimpleIntegerProperty angle;
		private SimpleIntegerProperty secAngle;
		private SimpleIntegerProperty window;
		private SimpleStringProperty windowName;
		double[] weights;
		double[] freqRange;
		private XYChart.Series<Number, Number> beamPattern = new XYChart.Series<Number, Number>();


		/**
		 * @param beamNum
		 * @param angle
		 * @param window
		 */
		public BeamInfo(int beamNum, int angle, int secAngle, int window, double[] freqRange) {
			super();
			this.beamNum = new SimpleIntegerProperty(beamNum);
			this.angle = new SimpleIntegerProperty(angle);
			this.secAngle = new SimpleIntegerProperty(secAngle);
			this.window = new SimpleIntegerProperty(window);
			this.windowName = new SimpleStringProperty(WindowFunction.getNames()[window]);
			this.freqRange = freqRange;
			this.calculateWeights();
			
			// calculate the beampattern add it to the list (if we're displaying all beams)
			this.calculateBeamPattern();
			this.beamPattern.setName("Beam " + String.valueOf(beamNum) + " Pattern");
			if (showAllBeams) {
				// loop through the beam list and create new beams for each
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						synchronized (lineChart) {
							lineChart.getData().add(beamPattern);
							Node thisSeries = (Node) beamPattern.getNode();
							thisSeries.setStyle("-fx-stroke: lightgrey; ");
						}
					}
				});
			}
		}

		/**
		 * @return
		 */
		public void calculateWeights() {
			weights = WindowFunction.getWindowFunc(window.get(), PamUtils.getNumChannels(curParams.getChannelMap()));
			calculateBeamPattern();
		}

		/**
		 * Based on concepts from Matlab code delay-and-sum.zip
		 * 
		 * @param hydrophones
		 * @param beamPatternBeams
		 * @return
		 */
		public void calculateBeamPattern() {
			synchronized (lineChart) {
				beamPattern.getData().clear();

				// generate a vector for the incoming source signal
				PamVector incomingSource = (PamVector.fromHeadAndSlant(angle.get(), secAngle.get()));
				double k = 2*Math.PI*frequency/soundSpeed;

				// loop through look direction azimuth angles 0 to 180, calculating the array pattern at each angle
				for (int i=0; i<180; i++) {

					// create vector for look direction and subtract from source vector; multiply by k param to create wave vector
					PamVector lookDir = PamVector.fromHeadAndSlant(i, secAngle.get());
					PamVector resultantVec = lookDir.sub(incomingSource);
					PamVector kVec = resultantVec.times(k);

					// loop through hydrophone locations
					// calculate the real and imaginary components of the wave at each hydrophone,
					// and sum them
					int numElements = PamUtils.getNumChannels(curParams.getChannelMap());
					double realSum = 0;
					double imagSum = 0;
					for (int j=0; j<numElements; j++) {
						realSum += weights[j]*Math.cos(kVec.dotProd(elementLocs[j]));
						imagSum += weights[j]*Math.sin(kVec.dotProd(elementLocs[j]));
					}

					double output = Math.sqrt(realSum * realSum + imagSum * imagSum) / numElements;
					double logOutput = 20 * Math.log10(output);
					beamPattern.getData().add(new XYChart.Data<Number, Number>(i, logOutput));
				}
			}
		}

		/**
		 * @return the beamNum
		 */
		public int getBeamNum() {
			return beamNum.get();
		}

		/**
		 * @param beamNum the beamNum to set
		 */
		public void setBeamNum(int beamNum) {
			this.beamNum.set(beamNum);
		}

		/**
		 * @return the angle
		 */
		public int getAngle() {
			return angle.get();
		}

		/**
		 * @param angle the angle to set
		 */
		public void setAngle(int angle) {
			this.angle.set(angle);
		}

		/**
		 * 
		 * @return the secondary angle
		 */
		public int getSecAngle() {
			return secAngle.get();
		}
		
		/**
		 * 
		 * @param secAngle
		 */
		public void setSecAngle(int secAngle) {
			this.secAngle.set(secAngle);
		}

		/**
		 * @return the window
		 */
		public int getWindow() {
			return window.get();
		}
		
		/**
		 * 
		 * @return the window name
		 */
		public String getWindowName() {
			return windowName.get();
		}

		/**
		 * @param window the window to set
		 */
		public void setWindow(int window) {
			this.window.set(window);
			this.windowName.set(WindowFunction.getNames()[window]);
		}

		/**
		 * @return the weights
		 */
		public double[] getWeights() {
			return weights;
		}

		/**
		 * @param weights the weights to set
		 */
		public void setWeights(double[] weights) {
			this.weights = weights;
		}

		/**
		 * @return the beamPattern
		 */
		public XYChart.Series<Number, Number> getBeamPattern() {
			return beamPattern;
		}

		/**
		 * @param beamPattern the beamPattern to set
		 */
		public void setBeamPattern(XYChart.Series<Number, Number> beamPattern) {
			this.beamPattern = beamPattern;
		}

		/**
		 * @return
		 */
		public double[] getFreqs() {
			return freqRange;
		}

		/**
		 * @param freqRange the freqRange to set
		 */
		public void setFreqRange(double[] freqRange) {
			this.freqRange = freqRange;
		}

	}

}
