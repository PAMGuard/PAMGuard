package dataMap.layoutFX;

import dataGram.DatagramManager;
import dataGram.DatagramScaleInformation;
import dataGram.DatagramSettings;
import dataMap.DataMapControl;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;

import org.controlsfx.control.CheckComboBox;

import PamController.PamController;
import PamUtils.PamCalendar;
import binaryFileStorage.BinaryStore;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.comboBox.ColorComboBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.sliders.ColourRangeSlider;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Settings pane which allows users to change settings for data maps. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DataMapSettingsPane extends DynamicSettingsPane<DataMapParametersFX> {

	/*
	 * Reference to the data map control. 
	 */
	private DataMapControl dataMapControl;

	/**
	 * Reference to the dataMapPane. 
	 */
	private DataMapPaneFX dataMapPane;

	//	/**
	//	 * The slider which determines time scale. 
	//	 */
	//	private Slider timeSlider;

	/**
	 * Shows the time scale in pix/hour
	 */
	private Label timeScaleLabel;

	/**
	 * Selects unit count on vertical scale
	 */
	private ComboBox<String> scaleBox;

	/**
	 * Check box for log vertical scale. 
	 */
	private PamToggleSwitch logScaleToggle;

	//	/**
	//	 * The chosen time values. 
	//	 */
	//	private double[] timeScaleChoices = DataMapParameters.hScaleChoices;

	/**
	 * Combo box holding options to channge the datargam bin size
	 */
	private ComboBox<String> datagramBox;

	/**
	 * Holds a list of times for the datagram bin size. 
	 */
	private ComboBox<String> dataGramComboBox;

	/**
	 * Holds everything. 
	 */
	private PamVBox holder;

	/**
	 * Grid pane with settings for the data scales
	 */
	private PamGridPane scaleSettingsPane;

	private Label dataGramLabel;

	private ComboBox<DataMapInfo> dataGramColorBox;

	/**
	 * Holdes datagram settings. 
	 */
	private PamVBox dataGramSettingsPane;

	private DataGramColPane dataGramColPane;

	private PamBorderPane mainPain;

	private Label colourLabel;

	private Pane dataMapChoicePane;

	private CheckComboBox<String> dataMapCheckComboBox; 


	public DataMapSettingsPane(DataMapControl dataMapControl, DataMapPaneFX dataMapPane) {
		super(null);
		this.dataMapControl = dataMapControl;
		this.dataMapPane = dataMapPane;

		//		//create the holder pane. 
		//		PamVBox vBoxHolder=new PamVBox(); 
		//		vBoxHolder.setSpacing(5);
		//		Label title=new Label("Scales"); 
		//		PamGuiManagerFX.titleFont2style(title);
		//		vBoxHolder.getChildren().addAll(title, controlPane);

		//add the scale 
		holder=new PamVBox(); 
		holder.setSpacing(5);

		Label dataLabel = new Label("Show data maps");
		PamGuiManagerFX.titleFont2style(dataLabel);
		holder.getChildren().addAll(dataLabel, dataMapChoicePane = createDataMapPane());
		updateDataGramShownBox();

		Label scaleLabel = new Label("Data scale");
		PamGuiManagerFX.titleFont2style(scaleLabel);
		holder.getChildren().add(scaleLabel);
		holder.getChildren().add(scaleSettingsPane = createScalePane());

		//adds the datagram settings pane which is dependent on binary storage. 
		checkDataGramPane(); 

		mainPain = new PamBorderPane(); 
		mainPain.setCenter(holder); 

		//set params for the pane		
		setParams(dataMapPane.getDataMapParams());
		checkDataGramPane(); // create datagram pane if a binary store already added. 

	}

	/**
	 * Create the data mmpa choice pane. 
	 * @return
	 */
	private Pane createDataMapPane() {

		dataMapCheckComboBox = new 	CheckComboBox<String>(); 

		PamHBox hBox = new PamHBox(); 
		hBox.setSpacing(5.);
		hBox.getChildren().addAll(dataMapCheckComboBox);

		return hBox;
	}

	/**
	 * Update the combo box which allows users to select which data  are shown. 
	 */
	private void updateDataGramShownBox() {
		dataMapCheckComboBox.getItems().clear();
		for (int i=0; i<this.dataMapPane.getNumDataStreamPanes(); i++) {
			
			dataMapCheckComboBox.getItems().add(dataMapPane.getDataStreamPane(i).getDataName().getName()); 
			dataMapCheckComboBox.getItemBooleanProperty(i).set((dataMapPane.getDataStreamPane(i).isVisible()));
			
			dataMapCheckComboBox.getItemBooleanProperty(i).addListener((obsval, oldval, newVal)->{
				notifySettingsListeners(); 
			});
		}
		
	}
	
	
	/**
	 * Adds a settings pane for the datagram if a binary store is present. 
	 */
	public void checkDataGramPane(){
		if (BinaryStore.findBinaryStoreControl()!=null){
			DatagramManager dataGramManager=BinaryStore.findBinaryStoreControl().getDatagramManager();
			if (dataGramManager!=null && dataGramSettingsPane==null) {
				PamVBox datagramholder =   new PamVBox(); 
				datagramholder.setSpacing(5);
				Label datagramLabel = new Label("Datagrams");
				PamGuiManagerFX.titleFont2style(datagramLabel);
				datagramholder.getChildren().add(datagramLabel);
				datagramholder.getChildren().add(createDatagramPane(dataGramManager));

				holder.getChildren().add(this.dataGramSettingsPane = datagramholder); 
			}
		}
		else {
			holder.getChildren().remove(this.dataGramSettingsPane);
			datagramBox=null; 
		}
	}


	/**
	 * Create the a datagram combo box to change the size of the datagram
	 * @param dataGramManager - the datagram manager for the current biinary store. 
	 * @return a combo box with datagram bin sizes. 
	 */
	private ComboBox<String> createDataGramBinPane(DatagramManager dataGramManager){

		dataGramComboBox=new ComboBox<String>(createDurationList(dataGramManager)); 

		dataGramComboBox.getSelectionModel().select(durationToString(dataGramManager.getDatagramSettings().datagramSeconds*1000L));
		dataGramComboBox.valueProperty().addListener(( ov,  t,  t1) -> {                
			if (t==t1) return; 
			else {
				PamController.getInstance();
				boolean ans=PamDialogFX.showWarning(PamController.getMainStage(), "Warning", "Recalculating the datagram for a large dataset may take a long time" +
						"Are you sure you want to continue"); 
				if (ans){
					int index=dataGramComboBox.getSelectionModel().getSelectedIndex();
					//messy- datagramSeconfds should be in millis but left for backwards compatibility. 
					dataGramManager.getDatagramSettings().datagramSeconds=(int) (DatagramSettings.defaultDatagramSeconds[index]/1000);
					dataGramManager.updateDatagrams();
					dataMapPane.repaintAll();
				}
			}
		});

		return dataGramComboBox;

	}

	/**
	 * Convert duration to string.
	 * @param duration in  millis.
	 */
	private String durationToString(long duration){
		return (" " + PamCalendar.formatDuration(duration));
	}

	/**
	 * Create list of load times. 
	 * @return list of load times duration. 
	 */
	private ObservableList<String> createDurationList(DatagramManager dataGramManager){
		ObservableList<String> loadTimeList=FXCollections.observableArrayList();
		for (int i=0; i<DatagramSettings.defaultDatagramSeconds.length; i++){
			loadTimeList.add(durationToString(DatagramSettings.defaultDatagramSeconds[i]));
		}
		return loadTimeList;
	}


	private Pane createDatagramPane(DatagramManager dataGramManager) {

		dataGramLabel= new Label("Datagram bin size");
		ComboBox<String> datagramBinsBox = createDataGramBinPane(dataGramManager); 

		//Pane for colouring datagrams. 
		dataGramColorBox=new ComboBox<DataMapInfo> (); 

		//find all datagrams. 
		updateDatagramColorBox();

		dataGramColorBox.setOnAction((action)->{
			if (dataGramColorBox.getSelectionModel().getSelectedItem()==null) return;
			dataGramColPane.setDataStreamPanel(dataMapPane.getDataStreamPane(dataGramColorBox.getSelectionModel().getSelectedItem()));
			colourLabel.setText(String.format("Colours for %s " , dataGramColorBox.getSelectionModel().getSelectedItem().getName())); 
			colourLabel.setTooltip(new Tooltip(String.format("Colours for %s " , dataGramColorBox.getSelectionModel().getSelectedItem().getName()))); 
			
			notifySettingsListeners();
		});

		//holds settings for the datagram
		dataGramColPane = new DataGramColPane(); 

		PamGridPane holder = new PamGridPane(); 
		holder.setHgap(5);
		holder.setVgap(5);

		//		holder.setGridLinesVisible(true);

		int row = 0;

		holder.add(dataGramLabel, 0,row);
		holder.add(datagramBinsBox, 1, row);
		GridPane.setColumnSpan(datagramBinsBox, 2);

		row++;

		holder.add(new Label("Select datagram"), 0, row);
		holder.add(dataGramColorBox, 1, row);
		GridPane.setColumnSpan(dataGramColorBox, 2);

		row++;

		holder.add(colourLabel = new Label("Colour for"), 0, row);
		GridPane.setColumnSpan(colourLabel, 3);

		row++;

		GridPane.setHgrow(dataGramColPane, Priority.ALWAYS);
		holder.add(dataGramColPane, 0, row);

		//dunno why this always had to be set after the child has been added to work. 
		GridPane.setColumnSpan(dataGramColPane, 3);

		//hack to make sure the third column of the grid expands to fit the pane
		ColumnConstraints rightCol = new ColumnConstraints();
		rightCol.setHgrow(Priority.ALWAYS);

		holder.getColumnConstraints().addAll(new ColumnConstraints(150),  new ColumnConstraints(150), rightCol);

		dataGramColPane.setDataStreamPanel(dataMapPane.getDataStreamPane(0));
		colourLabel.setText(String.format("Colours for %s " , dataMapPane.getDataStreamPane(0))); 


		return holder; 
	}



	/**
	 * Update the datagram colours combo box thjat allows users to select which datagram to select for changing colour settings.. 
	 */
	private void updateDatagramColorBox() {
		dataGramColorBox.getItems().clear();
		for (int i=0; i<this.dataMapPane.getNumDataStreamPanes(); i++) {
			//only include if the data block has a datagram and the data gram is a color plot - some datagrams can be lines e.g. filtered noise meaurements. 
			if (dataMapPane.getDataStreamPane(i).isHasDatagram() && dataMapPane.getDataStreamPane(i).getScaleType() == DatagramScaleInformation.PLOT_3D) {
				dataGramColorBox.getItems().add(dataMapPane.getDataStreamPane(i).getDataName()); 
			}
		}
	}


	/**
	 * Create a pane to change vertical scales. 
	 * @return pamne with controls to change vertical scales. 
	 */
	private PamGridPane createScalePane() {

		PamGridPane controlPane=new PamGridPane(); 
		controlPane.setHgap(5);
		controlPane.setVgap(5);

		//		//create time scale controls
		//		controlPane.add(new Label("Time window"),0,1);
		//		
		//		//create time slider 
		//		timeSlider=new Slider(0, timeScaleChoices.length-1, 1); 
		//		timeSlider.setShowTickLabels(true);
		//		timeSlider.setShowTickMarks(true);
		//		timeSlider.setMajorTickUnit(1);
		//		
		//		timeSlider.setLabelFormatter(new ScaleStringConverter());
		//		
		////		PamGridPane.setHalignment(timeSlider, Pos.BOTTOM_CENTER);
		//
		//		controlPane.add(timeSlider,1,1);
		//		//add listener to time slider to change datamap. 
		//		timeSlider.valueProperty().addListener((ov, oldVal, newVal)->{
		//			sayHScale();
		//			dataMapPane.scaleChanged();
		//		}); 
		//		 
		//		controlPane.add(timeScaleLabel=new Label(""),3,1);

		//create vertical scale controls

		scaleBox=new ComboBox<String> (); 
		scaleBox.getItems().add("No Scaling");
		scaleBox.getItems().add("per Second");
		scaleBox.getItems().add("per Minute");
		scaleBox.getItems().add("per Hour");
		scaleBox.getItems().add("per Day");
		GridPane.setColumnSpan(scaleBox, 2);

		Label showDetLabel  = new Label("Show detections ");

		controlPane.add(new Label("Show detections "),0,0);
		controlPane.add(scaleBox,1,0);
		//		scaleBox.setPrefWidth(200);

		scaleBox.valueProperty().addListener((ov, oldVal, newVal)->{
			notifySettingsListeners(); 
		}); 


		logScaleToggle=new PamToggleSwitch("Log Scale"); 
		logScaleToggle.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			notifySettingsListeners(); 
		});

		ColumnConstraints rightCol = new ColumnConstraints();
		rightCol.setHgrow(Priority.ALWAYS);
		controlPane.getColumnConstraints().addAll(new ColumnConstraints(150),  new ColumnConstraints(150), rightCol);

		controlPane.add(logScaleToggle,0,1);

		return controlPane;
	}
	
	


	// use setting flag to avoid immediate callback which overwrites changes 2 and 3 ! 
	boolean setting = false;

	public void setParams(DataMapParametersFX dataMapParameters) {
		System.out.println("----SET DATAMAP PARAMS");

		// ----- Datagram vertical scalee ----- //
		setting = true;
		//		timeSlider.setValue(dataMapParameters.hScaleChoice);
		scaleBox.getSelectionModel().select(dataMapParameters.vScaleChoice);
		logScaleToggle.setSelected(dataMapParameters.vLogScale);

		// ----- Datagram bin size ----- //
		//make sure combo box for data maps  is sorted
		updateDataGramShownBox();
		
		
		//check which data maps should be collasped or not. 
		for (int i = 0 ; i<this.getNDataStreamPanes() ; i++) {
			Boolean isShown = dataMapParameters.dataMapsCollapsed.get(this.dataMapPane.getDataStreamPane(i).getDataName());
			if (isShown==null) isShown=false;
			this.dataMapPane.getDataStreamPane(i).setCollapsed(isShown);
			
			Boolean isVisible = dataMapParameters.dataMapsShown.get(this.dataMapPane.getDataStreamPane(i).getDataName());
			if (isVisible==null) isVisible=true;
			this.dataMapPane.getDataStreamPane(i).setVisible(isVisible);

		}

		if (BinaryStore.findBinaryStoreControl()!=null){
			DatagramManager dataGramManager=BinaryStore.findBinaryStoreControl().getDatagramManager();
			dataGramComboBox.getSelectionModel().select(durationToString(dataGramManager.getDatagramSettings().datagramSeconds*1000L));

			/*****----- Datagram colours ----- *****/

			//make sure the combo box has correct data streams
			updateDatagramColorBox();


			//get colour data from Hash tables	
			DataStreamPaneFX dataStreamPane;
			for (int i=0; i<dataGramColorBox.getItems().size(); i++) {

				dataStreamPane = dataMapPane.getDataStreamPane(dataGramColorBox.getItems().get(i));

				PlotParams2D plotCols =  dataMapParameters.datagramColours.get(dataGramColorBox.getItems().get(i));

				System.out.println("------Set colours: " + (plotCols == null ? null : plotCols.getAmplitudeLimits()[0] + "  " +  plotCols.getAmplitudeLimits()[1]) + "  " + dataGramColorBox.getItems().get(i)); 

				if (plotCols!=null) {
					dataStreamPane.setMinMaxColourLimits(plotCols.getAmplitudeLimits()[0].get(), plotCols.getAmplitudeLimits()[1].get());
					dataStreamPane.setColourArrayType(plotCols.getColourMap());
				}
				else {
					//if the settings does not exist
					dataStreamPane.setMinMaxColourLimits(-80, 80);
					dataStreamPane.setColourArrayType(ColourArrayType.HSV);
				}

			}
			
			System.out.println("------Set colour box: " + dataMapParameters.selectedColourDatagram);

			//select the correct colour box - not this should also change the colours and limits of the colour settings pane. 
			if (this.dataGramColorBox.getItems().size()>dataMapParameters.selectedColourDatagram && dataMapParameters.selectedColourDatagram>=0) {
				dataGramColorBox.getSelectionModel().select(dataMapParameters.selectedColourDatagram);
			}
			else if (this.dataGramColorBox.getItems().size()>0 && this.dataGramColorBox.getSelectionModel().getSelectedIndex()<0){
				dataGramColorBox.getSelectionModel().select(0);
			}

		}
		setting = false;
	}


	public DataMapParametersFX getParams(DataMapParametersFX dataMapParameters) {
		if (setting) return dataMapParameters;
		System.out.println("---GET DATAMAP PARAMS");

		//		dataMapParameters.hScaleChoice = (int) timeSlider.getValue(); 

		dataMapParameters.vScaleChoice = scaleBox.getSelectionModel().getSelectedIndex();
		dataMapParameters.vLogScale = logScaleToggle.isSelected();

		//could have just cleared the hash map here but didn't want to change the setting object for 
		//data stream panes everytime this is called. 

		if (BinaryStore.findBinaryStoreControl()!=null){


			//set the data in the hash map.
			DataStreamPaneFX dataStreamPane = null;
			for (int i=0; i<dataGramColorBox.getItems().size(); i++) {

				dataStreamPane = dataMapPane.getDataStreamPane(dataGramColorBox.getItems().get(i));

				PlotParams2D plotCols =  dataMapParameters.datagramColours.get(dataGramColorBox.getItems().get(i)) ;
				if (plotCols==null) {
					plotCols = new PlotParams2D();
					dataMapParameters.datagramColours.put(dataGramColorBox.getItems().get(i), plotCols);
				}

				plotCols.getAmplitudeLimits()[0].set(dataStreamPane.getMinColourLimit());
				plotCols.getAmplitudeLimits()[1].set(dataStreamPane.getMaxColourLimit());
				plotCols.setColourMap(dataStreamPane.getColourMapArray());
			}

			//TODO get rid of entries no longer used in HasMap?
			dataMapParameters.selectedColourDatagram = dataGramColorBox.getSelectionModel().getSelectedIndex();

		}
		
		//now record whether a datamap (note not just datagrams) is closed and/or enabled. 
		//Note that these settings are really only ever used on start up.
		for (int i = 0 ; i<this.getNDataStreamPanes() ; i++) {
			
			dataMapParameters.dataMapsCollapsed.put(this.dataMapPane.getDataStreamPane(i).getDataName(), this.dataMapPane.getDataStreamPane(i).isCollapsed());
			dataMapParameters.dataMapsShown.put(this.dataMapPane.getDataStreamPane(i).getDataName(), this.dataMapPane.getDataStreamPane(i).isVisible());

		}

		return dataMapParameters;
	}

	/**
	 * Get the number of data map panes
	 * @return the number of data stream pane.s 
	 */
	private int getNDataStreamPanes() {
		return this.dataMapPane.getNumDataStreamPanes();
	}

	@Override
	public String getName() {
		return "Datamap settings";
	}

	@Override
	public Node getContentNode() {
		return mainPain;
	}

	@Override
	public void paneInitialized() {

	}


	/** 
	 * Allows the user to change datagram colours. 
	 * 
	 */
	public class DataGramColPane extends PamBorderPane {

		private ColourRangeSlider colourSlider;

		private Label ampLabel;

		private ColorComboBox colorBox;

		private DataStreamPaneFX dataStreamPane; 

		public DataGramColPane() {

			//create colour slider
			//colour slider 
			colourSlider=new ColourRangeSlider();
			colourSlider.setShowTickMarks(false);
			colourSlider.setShowTickLabels(false);
			colourSlider.setOrientation(Orientation.HORIZONTAL);
			colourSlider.setMin(-100);
			colourSlider.setMax(100);
			//amplifier label
			//				String dBRef = GlobalMedium.getdBRefString(PamController.getInstance().getGlobalMediumManager().getCurrentMedium());
			//			Label ampLabel = new Label("Colour scale"); 

			colorBox=new ColorComboBox(ColorComboBox.COLOUR_ARRAY_BOX);
			colorBox.setPrefWidth(80);

			colourSlider.lowValueProperty().addListener((obsVal, oldVal, newVal)->{
				setColours();
			});

			colourSlider.highValueProperty().addListener((obsVal, oldVal, newVal)->{
				setColours();
			});

			//Change the colour of the colour slider when combo box is changed. 
			colorBox.valueProperty().addListener(new ChangeListener<String>() {
				@Override public void changed(ObservableValue<? extends String> ov, String t, String t1) {                
					//change the colour of the colour range slider.     
					setColours();
				}
			});


			//need to set up alignment properly. //FIXME- a bit messy 
			BorderPane.setAlignment(colorBox, Pos.CENTER);
			//sliderPane.setPadding(new Insets(10,0,0,0));
			BorderPane.setMargin(colorBox, new Insets(0,5,0,5));

			this.setCenter(colourSlider);
			this.setRight(colorBox);

			//set up so the correct color
			colorBox.setValue(ColourArrayType.HOT);
			colourSlider.setColourArrayType(ColourArrayType.HOT);

		}


		public void setDataStreamPanel(DataStreamPaneFX selectedItem) {
			this.dataStreamPane=selectedItem;

			if (dataStreamPane==null) return;

			//keep a record of the colour limits because the slider has listeners that sets
			//them in the datastream
			double minCol = dataStreamPane.getMinColourLimit(); 
			double maxCol = dataStreamPane.getMaxColourLimit(); 
			
//			System.out.println("Data stream colour limits: " + minCol + "  " + maxCol);
			
			//the settings will already ahve been set in the data stream pane so 
			//make sure those are reflected in the controls. 
			colorBox.setValue(dataStreamPane.getColourMapArray()); 
			

			colourSlider.lowValueProperty().set(minCol);
			colourSlider.highValueProperty().set(maxCol);
			
		}


		/**
		 * Set colours depending on current colour selection in combo box. 
		 */
		private void setColours(){
			
//			System.out.println("Set colours: " + colourSlider.getLowValue() + "  " + colourSlider.getHighValue())

			ColourArrayType colourArrayType = ColourArray.getColourArrayType(colorBox.getValue());

			colourSlider.setColourArrayType(colourArrayType);

			if (dataStreamPane==null) return;

			dataStreamPane.setColourArrayType(colourArrayType); 
			dataStreamPane.setMinMaxColourLimits(colourSlider.getLowValue(), colourSlider.getHighValue()); 
			
			//want to be efficient here so that we do not repaint all datagrams whenever these settings are changed so
			//get the params but not through notify setting listeners. 
			dataMapPane.setDataMapParams(getParams(dataMapPane.getDataMapParams()));
		}



	}

}
