package videoRangePanel.layoutFX;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import PamUtils.LatLong;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.NumberStringConverter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamInternalDialogFX;
import pamViewFX.fxNodes.pamDialogFX.PamInternalDialogFX.InternalDialogAction;
import pamViewFX.fxNodes.table.AcceptOnExitTableCell;
import pamViewFX.fxNodes.table.ChoiceTable;
import pamViewFX.fxNodes.table.ChoiceTableItem;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import videoRangePanel.LocationManager;
import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import videoRangePanel.VRHorzCalcMethod;
import videoRangePanel.VRParameters;
import videoRangePanel.pamImage.ImageTimeOffset;
import videoRangePanel.pamImage.ImageTimeParser;
import videoRangePanel.pamImage.PamImage;
import videoRangePanel.vrmethods.landMarkMethod.LandMark;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkCSVLogging;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkGroupPane;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkGroup;


/**
 * Pane for changing the main video range settings class. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class VRSettingsPane extends DynamicSettingsPane<VRParameters>{

	/**
	 * Flag for opening the height tab
	 */
	public static final int HEIGHTTAB =0; 


	/**
	 * Flag for opening the landmark tab
	 */
	public static final int LANDMARKTAB =1; 


	/**
	 * Flag for opening the time settings tab
	 */
	public static final int TIMETAB =2; 


	/**
	 * Flag for opening the tide tab
	 */
	public static final int TIDETAB =3; 

	/**
	 * Flag for opening the image location tab. 
	 */
	public static final int IMAGELOCATIONTAB =4; 

	/**
	 * Flag for opening the image calibration tab. 
	 */
	public static final int CALIBRATIONTAB =5; 


	PamBorderPane mainHolder;

	/**
	 * The image location settings pane. 
	 */
	private ImageLocPane imageLocPane;

	/**
	 * Pane for settings landmarks 
	 */
	private LandMarkPane landMarkPane;

	/**
	 * Pane for changing image time offset and setting the format for 
	 * reading time from the filenmae. 
	 */
	private TimePane imageTimePane;

	/**
	 * Pane for changing the camera height
	 */
	private HeightPane heightPane;

	/**
	 * Pane ofr changing image calibration values.
	 */
	private CalibrationPane calibrationPane;

	/**
	 * Pane for adding external tide dtaa 
	 */
	private TidePane tidePane;

	/**
	 * Reference to the VRControl;
	 */
	private VRControl vrControl; 

	/**
	 * Imports and exports landmark groups to .csv file. 
	 */
	private  LandMarkCSVLogging landMarkCSVLogging = new  LandMarkCSVLogging();

	/**
	 * Tab pane which holds all settings tabs. 
	 */
	private TabPane mainTabPane;


	private NumberFormat latLongNumberFormat;

	/**
	 * The range method pane. 
	 */
	private RangeMethodPane rangeMethodPane; 

	public VRSettingsPane(VRControl vrControl, Object ownerWindow) {
		super(ownerWindow);
		this.vrControl=vrControl; 
		mainHolder = new PamBorderPane(createPane()); 

		//number format for lat/long data
		latLongNumberFormat=NumberFormat.getInstance();
		latLongNumberFormat.setMaximumFractionDigits(10);
	}

	/**
	 * The settings pane
	 */
	public TabPane createPane() {

		//tab pane to hold everything
		mainTabPane = new TabPane(); 
		mainTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		imageLocPane=new ImageLocPane();
		landMarkPane = new LandMarkPane();
		imageTimePane = new TimePane();
		heightPane = new HeightPane();
		calibrationPane = new CalibrationPane();
		tidePane = new TidePane();
		rangeMethodPane = new RangeMethodPane();


		Tab tab; 
		tab = new Tab(imageTimePane.getName()); 
		imageTimePane.setPadding(new Insets(5,5,5,5));
		tab.setContent(imageTimePane);
		mainTabPane.getTabs().add(tab); 

		tab = new Tab(imageLocPane.getName()); 
		imageLocPane.setPadding(new Insets(5,5,5,5));
		tab.setContent(imageLocPane);
		mainTabPane.getTabs().add(tab); 

		tab = new Tab(heightPane.getName()); 
		heightPane.setPadding(new Insets(5,5,5,5));
		tab.setContent(heightPane);
		mainTabPane.getTabs().add(tab); 

		tab = new Tab(landMarkPane.getName()); 
		landMarkPane.setPadding(new Insets(5,5,5,5));
		tab.setContent(landMarkPane);
		mainTabPane.getTabs().add(tab); 

		tab = new Tab(tidePane.getName()); 
		tidePane.setPadding(new Insets(5,5,5,5));
		tab.setContent(tidePane);
		mainTabPane.getTabs().add(tab);

		tab = new Tab(calibrationPane.getName()); 
		calibrationPane.setPadding(new Insets(5,5,5,5));
		tab.setContent(calibrationPane);
		mainTabPane.getTabs().add(tab); 
		
		tab = new Tab(rangeMethodPane.getName()); 
		rangeMethodPane.setPadding(new Insets(5,5,5,5));
		tab.setContent(rangeMethodPane);
		mainTabPane.getTabs().add(tab); 

		return mainTabPane;
	}


	@Override
	public VRParameters getParams(VRParameters currParams) {

		currParams=imageTimePane.getParams(currParams);
		currParams=imageLocPane.getParams(currParams);
		currParams=landMarkPane.getParams(currParams);
		currParams=imageTimePane.getParams(currParams);
		currParams=heightPane.getParams(currParams);
		currParams=calibrationPane.getParams(currParams);
		currParams=tidePane.getParams(currParams);
		currParams=rangeMethodPane.getParams(currParams);

		return currParams;
	}

	@Override
	public void setParams(VRParameters input) {
		imageLocPane.setParams(input);
		landMarkPane.setParams(input);
		imageTimePane.setParams(input);
		heightPane.setParams(input);
		calibrationPane.setParams(input);
		tidePane.setParams(input);
		rangeMethodPane.setParams(input);
	}

	@Override
	public String getName() {
		return "VR Settings";
	}

	@Override
	public Node getContentNode() {
		return mainHolder;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
	}

	class CheckBoxLocation extends CheckBox{

		public CheckBoxLocation(String name){
			super(name);
		}

		private int locationType;

		public int getLocationType(){
			return locationType;
		}

		public void setLocationType(int locationType){
			this.locationType=locationType;
		}

	}

	/**
	 * Image location pane.
	 * @author Jamie Macaulay
	 *
	 */
	class ImageLocPane extends VRSettingsTab  {

		//check boxes for different location types. 
		private CheckBoxLocation pamguardBoatGPS;
		private CheckBoxLocation photoGeoTag;
		private CheckBoxLocation manualLoc;
		private CheckBoxLocation landMark; 

		/**
		 * Observable lsit of manual location values. 
		 */
		private ObservableList<ManualImageLocationItem> manualLocations = FXCollections.observableArrayList();

		/**
		 * Check boxes for which types of location information use
		 */
		protected ArrayList<CheckBoxLocation> checkBoxes=new ArrayList<CheckBoxLocation>();


		/**
		 * Spinners used to sort out the order in which multiple sources of location information
		 * are use.d 
		 */
		protected ArrayList<PamSpinner> prioritySpinners=new ArrayList<PamSpinner>();

		//		/**
		//		 * 
		//		 */
		//		private LandMarkGroup localGPSMarkList;

		/**
		 * List of methods to get the image location
		 */
		private int[] localMethodsList;

		//		/**
		//		 * The currently selected manual location
		//		 */
		//		private int currentlySelected=0;

		/**
		 * Table which allows the create of manual locations
		 */
		private ImageLocChoiceTable imageLocTable;



		public ImageLocPane(){

			pamguardBoatGPS = new CheckBoxLocation("GPS Data Block");
			pamguardBoatGPS.setLocationType(LocationManager.ARRAY_MANAGER);
			pamguardBoatGPS.setOnAction((action)->{
				checkBoxSel();
				notifySettingsListeners();
			});

			photoGeoTag = new CheckBoxLocation("Image Geo Tag");
			photoGeoTag.setLocationType(LocationManager.PHOTO_TAG);
			photoGeoTag.setOnAction((action)->{
				checkBoxSel();
				notifySettingsListeners();
			});

			manualLoc=new CheckBoxLocation("Manual Location");
			manualLoc.setLocationType(LocationManager.MANUAL_INPUT);
			manualLoc.setOnAction((action)->{
				checkBoxSel();
				notifySettingsListeners();
			});

			landMark=new CheckBoxLocation("LandMark Measurment");
			landMark.setLocationType(LocationManager.LANDMARK_GROUP);
			//landMark is a mandatory method if using landmarks- set disabled and selected. 
			landMark.setSelected(true);
			landMark.setDisable(true);

			checkBoxes.add(pamguardBoatGPS);
			checkBoxes.add(photoGeoTag);
			checkBoxes.add(manualLoc);

			vrControl.getLocationManager();

			//note: in the same order as checkboxes. 
			for (int i=0; i<LocationManager.getAllMethods().length; i++){
				prioritySpinners.add( new PamSpinner<Integer>(0, LocationManager.getAllMethods().length-1, 1, 1));
				prioritySpinners.get(i).getEditor().setAlignment(Pos.CENTER);
				prioritySpinners.get(i).setPrefWidth(80); 
				prioritySpinners.get(i).valueProperty().addListener(new PrioritySpinnerChanged(prioritySpinners.get(i)));
				prioritySpinners.get(i).valueProperty().addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners();
				});
				//				prioritySpinners.get(i).add
			}

			int row=0; 
			PamGridPane gridPane = new PamGridPane(); 
			gridPane.setHgap(5);
			gridPane.setVgap(5);

			Label label = new Label("Location Information Source"); 
			PamGuiManagerFX.titleFont2style(label);
			//label.setFont(PamGuiManagerFX.titleFontSize2);
			gridPane.add(label, 0,row);
			row++; 

			for (int i=0; i<checkBoxes.size(); i++) {
				gridPane.add(checkBoxes.get(i), 0, row);
				gridPane.add(prioritySpinners.get(i), 1, row);
				row++; 
			}
			gridPane.add(landMark, 0, row);

			//table to hold the data. 
			imageLocTable=createTable();
			imageLocTable.setPrefHeight(300);
			manualLocations.addListener((Change<? extends ManualImageLocationItem> c) -> {
				notifySettingsListeners();
			});


			PamBorderPane borderPane = new PamBorderPane(); 
			borderPane.setPadding(new Insets(10,0,0,0));
			Label locLabel =new Label("Manual Locations");
			PamGuiManagerFX.titleFont2style(locLabel);

			//locLabel.setFont(PamGuiManagerFX.titleFontSize2);
			borderPane.setTop(locLabel);
			borderPane.setCenter(imageLocTable);


			this.setTop(gridPane);
			this.setCenter(borderPane);

			setPriorityParams();

			//			this.add(BorderLayout.NORTH,priorityPanel);
			//			addButton.addActionListener(new AddButton());
			//			editbutton.addActionListener(new EditButton());
			//			deleteButton.addActionListener(new DeleteButtonSel());

		}

		class PrioritySpinnerChanged implements ChangeListener{

			private PamSpinner prioritySpinner;
			private int lastValue;

			@SuppressWarnings("unchecked")
			public PrioritySpinnerChanged(PamSpinner prioritySpinner) {
				this.prioritySpinner=prioritySpinner;
				this.lastValue=(int) prioritySpinner.getValue();
			}

			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				for (int i=0; i<prioritySpinners.size(); i++){
					if (prioritySpinners.get(i).equals(prioritySpinner)) continue;
					if (prioritySpinners.get(i).getValue()==prioritySpinner.getValue()){
//						System.out.println("Same Value: spinner val: "+ prioritySpinners.get(i).getValue()+" spiner no. "+i + " lastValue: "+lastValue);
						prioritySpinners.get(i).getValueFactory().setValue(lastValue);
					}
				}
				lastValue=(int) prioritySpinner.getValue();

			}

			//			@Override
			//			public void stateChanged(ChangeEvent arg0) {
			//				// TODO Auto-generated method stub
			//				
			//			}

		}

		private void checkBoxSel() {
			for (int i=0; i<checkBoxes.size(); i++){
				prioritySpinners.get(i).setDisable(!checkBoxes.get(i).isSelected());
			}
		}


		//		//make sure that if the selected location is deleted then the first location in the list is selected
		//		class DeleteButtonSel extends DeleteButton {
		//
		//			public void actionPerformed(ActionEvent e) {
		//				super.actionPerformed(e);
		//				if (currentlySelected>=localGPSMarkList.size());
		//				currentlySelected=0;
		//				//tableData.fireTableDataChanged();
		//			}
		//			
		//		}

		@Override
		void setParams(VRParameters vrParameters) {
			LandMarkGroup localGPSMarkList = vrParameters.getManualGPSDatas();
			if (localGPSMarkList==null) localGPSMarkList= new LandMarkGroup();

			int currentlySelected = vrParameters.getCurrentManualGPSIndex();

			manualLocations.clear(); 
			for (int i=0; i<localGPSMarkList.size(); i++) {
				manualLocations.add(new ManualImageLocationItem(localGPSMarkList.get(i).getName(), localGPSMarkList.get(i).getPosition())); 
			}

			if (manualLocations.size()>0) {
				this.imageLocTable.getTableView().getSelectionModel().select(currentlySelected);
				manualLocations.get(currentlySelected).selectedProperty.set(true);
			}

			setPriorityParams();
		}

		/**
		 * Get parameters from the controls.
		 */
		@Override
		public VRParameters getParams(VRParameters vrParameters) {

			LandMarkGroup localGPSMarkList = new LandMarkGroup(); 

			for (int i=0; i<this.manualLocations.size(); i++) {
				localGPSMarkList.add(new LandMark(manualLocations.get(i).name.get(), 
						new LatLong(manualLocations.get(i).latitudeProperty.get(), manualLocations.get(i).longitudeProperty.get()), 0.0 )); 
			}

			vrParameters.setGPSLocData(localGPSMarkList);
			vrParameters.setGPSLocDataSelIndex(this.imageLocTable.getSelectedChoice());
			getPrioirtyParams();

			//TODO
			vrControl.getLocationManager().setCurrentMethods(localMethodsList);


			return vrParameters;
		}


		public void setPriorityParams(){
			localMethodsList=vrControl.getLocationManager().getCurrentMethods();
			//set the check boxes required. 
			boolean sel=false;
			for (int i=0; i<checkBoxes.size();i++){
				sel=false;
				for (int j=0; j<localMethodsList.length;j++){
					if (localMethodsList[j]==checkBoxes.get(i).getLocationType()){
						sel=true;
					}
				}

				checkBoxes.get(i).setSelected(sel);
				prioritySpinners.get(i).setDisable(!sel);
				prioritySpinners.get(i).getValueFactory().setValue(
						vrControl.getLocationManager().getPriority(checkBoxes.get(i).getLocationType()));
			}
		}


		private void getPrioirtyParams(){

			Integer[] methods=new Integer[LocationManager.getAllMethods().length];
			Arrays.fill(methods, null);

			for (int i=0; i<checkBoxes.size();i++){
				if (checkBoxes.get(i).isSelected()){
					methods[(int) prioritySpinners.get(i).getValue()]=checkBoxes.get(i).getLocationType();
				}
			}
			//so now have a methods array which is in the correct order but may have negative ones. Need to get rid of these. 
			ArrayList<Integer> newMethods=new ArrayList<Integer>();
			for (int j=0; j<methods.length; j++){
				if (methods[j]!=null) newMethods.add(methods[j]);
			}

			localMethodsList=new int[newMethods.size()];
			for (int k=0; k<newMethods.size(); k++){
				localMethodsList[k]=(int) newMethods.get(k);
			}			
		}

		/**
		 * Manual location class. Just for making it easier to alter table data. 
		 * @author Jamie Macaulay 
		 *
		 */
		class ManualImageLocationItem extends ChoiceTableItem {

			ManualImageLocationItem(){

			}

			ManualImageLocationItem( String name, double lat, double lon){
				this.latitudeProperty.set(lat);
				this.longitudeProperty.set(lon);
				this.name.set(name); 
				addSettingsListeners();
			}

			ManualImageLocationItem( String name, LatLong latLong){
				this.latitudeProperty.set(latLong.getLatitude());
				this.longitudeProperty.set(latLong.getLongitude());
				this.name.set(name); 
				addSettingsListeners();
			}


			/**
			 * Add listeners for when change happens. 
			 */
			private void addSettingsListeners() {
				super.selectedProperty.addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners(); 
				});
				latitudeProperty.addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners(); 
				});
				longitudeProperty.addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners(); 
				});
				name.addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners(); 
				});
			}

			/**
			 * Latitude property
			 */
			public DoubleProperty latitudeProperty = new SimpleDoubleProperty(56); 

			/**
			 * Longitude property
			 */
			public DoubleProperty longitudeProperty = new SimpleDoubleProperty(-6); 


			/**
			 * Name property. 
			 */
			public StringProperty name = new SimpleStringProperty("New Location"); 


		}

		class ImageLocChoiceTable extends ChoiceTable<ManualImageLocationItem> {

			public ImageLocChoiceTable(ObservableList<ManualImageLocationItem> list) {
				super(list);
			}


			@Override
			public ManualImageLocationItem newDataItem() {
				return new ManualImageLocationItem();
			}

		}

		/**
		 * Create the table. 	
		 */
		private ImageLocChoiceTable createTable() {

			ImageLocChoiceTable choiceTable = new ImageLocChoiceTable(manualLocations); 

			TableColumn< ManualImageLocationItem,String>  locationName = new TableColumn<ManualImageLocationItem,String>("Name");
			locationName.setCellValueFactory(cellData -> cellData.getValue().name);
			locationName.setEditable(true);
			locationName.setCellFactory(AcceptOnExitTableCell.<ManualImageLocationItem, String>forTableColumn(new DefaultStringConverter()));

			TableColumn<ManualImageLocationItem,Number>  latitude = new TableColumn<ManualImageLocationItem,Number>("Latitude (decimal)");
			latitude.setCellValueFactory(cellData -> cellData.getValue().latitudeProperty);
			latitude.setEditable(true);
			latitude.setCellFactory(column -> {
					return new AcceptOnExitTableCell<ManualImageLocationItem, Number>(new NumberStringConverter(latLongNumberFormat)); 
				});

			TableColumn<ManualImageLocationItem,Number>  longitude = new TableColumn<ManualImageLocationItem,Number>("Longitude (decimal)");
			longitude.setCellValueFactory(cellData -> cellData.getValue().longitudeProperty);
			longitude.setEditable(true);
			longitude.setCellFactory(column -> {
				return new AcceptOnExitTableCell<ManualImageLocationItem, Number>(new NumberStringConverter(latLongNumberFormat)); 
			});

			choiceTable.getTableView().getColumns().addAll(locationName, latitude, longitude);

			return choiceTable;

		}

		@Override
		String getName() {
			return "Image Location";
		}

	}

	/**
	 * Pane which allows the changing of land marks
	 * @author Jamie Macaulay 
	 *
	 */
	public class LandMarkPane extends VRSettingsTab  {

		/**
		 * 
		 */
		ObservableList<LandMarkGroupItem> landMarkGroups = FXCollections.observableArrayList();

		/**
		 * 
		 */
		private LandMarkGroupChoiceTable landMarkGroupTable;


		LandMarkGroupPane landMarkPane;

		/**
		 * Internal dialog which shows an image edit pane;
		 */
		private PamInternalDialogFX internalDialog; 

		public LandMarkPane() {
			this.setCenter(createPane());
		}

		private Pane createPane() {
			landMarkPane= new LandMarkGroupPane(VRSettingsPane.this);

			landMarkGroups.addListener((Change<? extends LandMarkGroupItem> c) -> {
				notifySettingsListeners();
			});


			Label landMarkLabel = new Label("Landmark Groups"); 
			PamGuiManagerFX.titleFont2style(landMarkLabel);
			//landMarkLabel.setFont(PamGuiManagerFX.titleFontSize2);

			landMarkGroupTable =  new LandMarkGroupChoiceTable(landMarkGroups);
			landMarkGroupTable.getButtonPane();
			landMarkGroupTable.getTableView().setEditable(true);

			landMarkGroupTable.getButtonPane().getChildren().clear(); 

			PamButton addButton = new PamButton(); 
//			addButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, PamGuiManagerFX.iconSize));
			addButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize));
			addButton.setOnAction((action)->{
				//first open the dialog. 
				landMarkGroups.add(new LandMarkGroupItem()); 
				showLandMarkDialog(landMarkGroups.get(landMarkGroups.size()-1), landMarkGroups.size()-1);  
				notifySettingsListeners(); 
				//landMarkGroupTable.getTableData().add(landMarkGroupTable.newDataItem());
			});
			addButton.setTooltip(new Tooltip("Add a new landmark group"));


			PamButton editButton = new PamButton(); 
//			editButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.EDIT, PamGuiManagerFX.iconSize));
			editButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-pencil", Color.WHITE, PamGuiManagerFX.iconSize));
			editButton.setOnAction((action)->{
				showLandMarkDialog(landMarkGroupTable.getTableView().getSelectionModel().getSelectedItem(), 
						landMarkGroupTable.getTableView().getSelectionModel().getSelectedIndex());  
				notifySettingsListeners(); 
			});
			editButton.disableProperty().bind(Bindings.isEmpty(landMarkGroupTable.getTableView().getSelectionModel().getSelectedItems()));
			editButton.setTooltip(new Tooltip("Edit the landmark group"));

			PamButton removeButton = new PamButton(); 
//			removeButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.DELETE,  PamGuiManagerFX.iconSize));
			removeButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-delete",  Color.WHITE, PamGuiManagerFX.iconSize));
			removeButton.setOnAction((action)->{
				landMarkGroups.remove(landMarkGroupTable.getTableView().getSelectionModel().getSelectedItem());
				notifySettingsListeners(); 
			});
			removeButton.disableProperty().bind(Bindings.isEmpty(landMarkGroupTable.getTableView().getSelectionModel().getSelectedItems()));
			removeButton.setTooltip(new Tooltip("Delete the landmark group (this cannot be undone)"));

			PamButton importButton = new PamButton(); 
//			importButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_IMPORT, PamGuiManagerFX.iconSize));
			importButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-import", Color.WHITE, PamGuiManagerFX.iconSize));
			importButton.setOnAction((action)->{
				//import a landmark group
				LandMarkGroup newLandMarkGroup = landMarkCSVLogging.importLandMarks(this.getScene().getWindow());
				if (newLandMarkGroup!=null) {
					landMarkGroups.add(new LandMarkGroupItem(newLandMarkGroup)); 
					notifySettingsListeners(); 
				}
			});
			importButton.setTooltip(new Tooltip("Import a Landmark Group from a .csv file"));

			PamButton exportButton = new PamButton(); 
//			exportButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_EXPORT, PamGuiManagerFX.iconSize));
			exportButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-export", Color.WHITE, PamGuiManagerFX.iconSize));
			exportButton.setOnAction((action)->{
				//export a landmark group. 
				landMarkCSVLogging.export(getLandMarkGroupObject(landMarkGroupTable.getTableView().getSelectionModel().getSelectedItem()),
						this.getScene().getWindow());
				notifySettingsListeners(); 
			});
			exportButton.setTooltip(new Tooltip("Export a landmark group to a .csv file"));

			landMarkGroupTable.getButtonPane().getChildren().addAll(addButton, editButton, removeButton,importButton,exportButton); 

			PamBorderPane holder = new PamBorderPane(); 
			holder.setTop(landMarkLabel); 
			holder.setCenter(landMarkGroupTable); 

			return holder; 

		}

		@Override
		String getName() {
			return "Landmarks";
		}

		/**
		 * Show the landmark dialog. 
		 */
		private void showLandMarkDialog(LandMarkGroupItem item, int index) {			//first open the dialog. 
			if (internalDialog==null)	internalDialog= new PamInternalDialogFX("Landmarks", (Pane) getOwnerWindow());
			internalDialog.setContent(landMarkPane);
			internalDialog.setOnClosed((type)->{
				if (type==InternalDialogAction.OK) {
					landMarkPane.getParams(item);
					notifySettingsListeners(); 
				}
			}); 
			landMarkPane.setParams(item);
			internalDialog.show();
		}

		/**
		 * Table for land mark groups
		 * @author Jamie Macaulay
		 *
		 */
		class LandMarkGroupChoiceTable extends ChoiceTable<LandMarkGroupItem> {

			public LandMarkGroupChoiceTable(ObservableList<LandMarkGroupItem> list) {
				super(list);
				this.getTableView().setEditable(true);

				TableColumn< LandMarkGroupItem,String>  groupName = new TableColumn<LandMarkGroupItem,String>("Group Name");
				groupName.setCellValueFactory(cellData -> cellData.getValue().name);
				groupName.setEditable(false);
				groupName.setCellFactory(TextFieldTableCell.<LandMarkGroupItem, String>forTableColumn(new DefaultStringConverter()));

				TableColumn<LandMarkGroupItem,Number>  nLandMarks = new TableColumn<LandMarkGroupItem,Number>("No. LandMarks");
				nLandMarks.setCellValueFactory(cellData -> cellData.getValue().nLandMarks);
				nLandMarks.setEditable(false);
				nLandMarks.setCellFactory(TextFieldTableCell.< LandMarkGroupItem, Number>forTableColumn(new NumberStringConverter()));

				this.getTableView().getColumns().addAll(groupName, nLandMarks);

			}

			@Override
			public LandMarkGroupItem newDataItem() {
				return new LandMarkGroupItem();
			}

		}


		@Override
		VRParameters getParams(VRParameters vrParameters) {
			if (vrParameters.getLandMarkDatas()==null) vrParameters.setLandMarkDatas(new ArrayList<LandMarkGroup>());
			vrParameters.getLandMarkDatas().clear(); 
			for (int i=0; i<landMarkGroups.size(); i++) {
				vrParameters.getLandMarkDatas().add( getLandMarkGroupObject(landMarkGroups.get(i)));
			}
			vrParameters.setCurrentLandMarkGroupIndex(landMarkGroupTable.getSelectedChoice());

//			System.out.println("Selected LandMarks: " + landMarkGroupTable.getSelectedChoice() + "  " + vrParameters.getLandMarkDatas().size());

			return vrParameters;
		}

		@Override
		void setParams(VRParameters vrParameters) {
			landMarkGroups.clear();
			if (vrParameters.getLandMarkDatas()!=null && vrParameters.getLandMarkDatas().size()>=1) {
				for (int i=0; i<vrParameters.getLandMarkDatas().size(); i++) {
					landMarkGroups.add(new LandMarkGroupItem(vrParameters.getLandMarkDatas().get(i)));
				}
				if (vrParameters.getSelectedLandMarkGroup()>=0) {
					landMarkGroupTable.setSelectedChoice(vrParameters.getSelectedLandMarkGroup()); 
				}
			}
		}

	}


	/**
	 * Get a LandMarkGroup from a LandMarkGroupItem. 
	 * @param landMarkGroupItem - the land mark group property
	 * @return the land mark group item. 
	 */
	public static LandMarkGroup getLandMarkGroupObject(LandMarkGroupItem landMarkGroupItem) {
		LandMarkGroup landMarkGroup = new LandMarkGroup(); 
		landMarkGroup.setGroupName(landMarkGroupItem.name.get());

		landMarkGroup.setLandMarks(landMarkGroupItem.landMarks) ;

		return landMarkGroup;
	}

	/**
	 * The time pane. 
	 * @author Jamie Macaulay
	 *
	 */
	private class TimePane extends VRSettingsTab  {

		//spinner for time offset values
		private Spinner<Integer> days;
		private Spinner<Integer> hours;
		private Spinner<Integer> minutes;
		private Spinner<Integer> millis;
		private Spinner<Integer> seconds;
		private CheckBox useTimeOffset;

		/**
		 * Use the filename time 
		 */
		private CheckBox useFileNameTime;

		/**
		 * The type of file
		 */
		private ComboBox<String> timeParserBox;

		private TimePane(){
			this.setCenter(createTimePane());
			this.setPadding(new Insets(5,5,5,5));
		}

		/**
		 * Create the time pane
		 * @return the time pane. 
		 */
		private Pane createTimePane() {

			PamVBox holder = new PamVBox();
			holder.setSpacing(5); 

			//Combo box for selecting time parser
			Label timeHolderLabel = new Label("Time Parser");
			PamGuiManagerFX.titleFont2style(timeHolderLabel);
//			timeHolderLabel.setFont(PamGuiManagerFX.titleFontSize2);

			timeParserBox = new ComboBox<String>();
			ArrayList<ImageTimeParser> imageTimeParsers = PamImage.getImageTimeParsers(); 
			timeParserBox.getItems();
			for (int i=0; i<imageTimeParsers.size() ; i++) {
				timeParserBox.getItems().add(imageTimeParsers.get(i).getName()); 
			}
			timeParserBox.setPrefWidth(200);
			timeParserBox.setOnAction((action)->{
				notifySettingsListeners(); 
			});

			//the time offseret spinner
			Label timeOffsetLabel = new Label("Time Offset");
			PamGuiManagerFX.titleFont2style(timeOffsetLabel);
//			timeOffsetLabel.setFont(PamGuiManagerFX.titleFontSize2);

			PamHBox spinnerHolder = new PamHBox();
			spinnerHolder.setSpacing(5); 
			spinnerHolder.setAlignment(Pos.CENTER_LEFT);

			days = new PamSpinner<Integer>(0, 10000, 0, 1); 
			styleSpinner(days);
			days.getEditor().setPrefColumnCount(3);
			days.valueProperty().addListener((obsVal, oldVal, newVal)->{
				notifySettingsListeners();
			});

			hours = new PamSpinner<Integer>(0, 24, 0, 1); 
			styleSpinner(hours);
			hours.valueProperty().addListener((obsVal, oldVal, newVal)->{
				notifySettingsListeners();
			});

			minutes = new PamSpinner<Integer>(0, 60, 0, 1); 
			styleSpinner(minutes);
			minutes.valueProperty().addListener((obsVal, oldVal, newVal)->{
				notifySettingsListeners();
			});

			seconds = new PamSpinner<Integer>(0, 60, 0, 1); 
			styleSpinner(seconds);
			seconds.valueProperty().addListener((obsVal, oldVal, newVal)->{
				notifySettingsListeners();
			});

			millis = new PamSpinner<Integer>(0, 1000, 0, 1); 
			styleSpinner(millis);
			millis.getEditor().setPrefColumnCount(4);
			millis.valueProperty().addListener((obsVal, oldVal, newVal)->{
				notifySettingsListeners();
			});

			spinnerHolder.getChildren().addAll(new Label("Days:"), days, new Label("Hours:"),
					hours, new Label("Minutes:"), minutes, new Label("Seconds:"), seconds, 
					new Label("Millis:"), millis);

			useTimeOffset= new CheckBox("Use time offset"); 
			useTimeOffset.setOnAction((action)->{
				setSpinnerDisable(!useTimeOffset.isSelected());
				notifySettingsListeners();
			});

			setSpinnerDisable(!useTimeOffset.isSelected());

			holder.getChildren().addAll(timeHolderLabel, timeParserBox, timeOffsetLabel, useTimeOffset, spinnerHolder);

			return holder; 
		}

		/***
		 * Style spinner a certyian way.
		 * @param spinner
		 */
		private void styleSpinner(Spinner spinner) {
			spinner.getEditor().setPrefColumnCount(2);
			spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
			spinner.getEditor().setAlignment(Pos.CENTER); //center text. 
			spinner.setEditable(true);
		}

		/**
		 * Set the spinner enabled or disblaed. 
		 * @param disable -true to disbale time offset spinners 
		 */
		private void setSpinnerDisable(boolean disable) {
			days.setDisable(disable);
			hours.setDisable(disable);
			minutes.setDisable(disable);
			seconds.setDisable(disable);
			millis.setDisable(disable);
		}


		@Override
		String getName() {
			return "Image Time";
		}

		@Override
		VRParameters getParams(VRParameters vrParameters) {

			//image time parser
			vrParameters.imageTimeParser=timeParserBox.getSelectionModel().getSelectedIndex(); 

			//time offset
			vrParameters.useTimeOffset=useTimeOffset.isSelected();

			ImageTimeOffset imageTimeOffset = new ImageTimeOffset(0);
			imageTimeOffset.setTimeOffset(days.getValue(), hours.getValue(),
					minutes.getValue(), seconds.getValue(), millis.getValue());
			vrParameters.timeOffset= imageTimeOffset;

			return vrParameters;
		}

		@Override
		void setParams(VRParameters vrParameters) {

			timeParserBox.getSelectionModel().select(vrParameters.imageTimeParser);

			if (vrParameters.timeOffset==null) vrParameters.timeOffset= new ImageTimeOffset(0); 

			ImageTimeOffset imageTimeOffset = (ImageTimeOffset) vrParameters.timeOffset;

			long millisOffset=imageTimeOffset.getTimeOffset(); 

			//now work out the number of days
			int days=(int) Math.floor(millisOffset/(24*60*60*1000));
			millisOffset=millisOffset-days*24*60*60*1000;

			//the number of hours
			int hours=(int) Math.floor(millisOffset/(60*60*1000));
			millisOffset=millisOffset-hours*60*60*1000;

			//the number of mins
			int minutes=(int) Math.floor(millisOffset/(60*1000));
			millisOffset=millisOffset-minutes*60*1000;

			int seconds=(int) Math.floor(millisOffset/(24*60*60*1000));
			millisOffset=millisOffset-seconds*1000;

			this.days.getValueFactory().setValue(days);
			this.hours.getValueFactory().setValue(hours);
			this.minutes.getValueFactory().setValue(minutes);
			this.seconds.getValueFactory().setValue(seconds);
			this.millis.getValueFactory().setValue((int) millisOffset);

			useTimeOffset.setSelected(vrParameters.useTimeOffset);
			setSpinnerDisable(!vrParameters.useTimeOffset);

		}

	}



	/**
	 * Pane which holds a set of camera heights
	 * @author Jamie Macaulay
	 *
	 */
	class HeightPane extends VRSettingsTab  {


		/**
		 * Observable lsit of manual location values. 
		 */
		private ObservableList<HeightTableItem> imageHeightsData = FXCollections.observableArrayList();

		/**
		 * Table for editing heights. 
		 */
		private HeightChoiceTable heightTable;


		public HeightPane() {
			this.setCenter( createHeightPane() );
		}


		/**
		 * Create the height pane. 
		 */
		private Pane createHeightPane() {

			Label label = new Label("Image Heights");
			PamGuiManagerFX.titleFont2style(label);
//			label.setFont(PamGuiManagerFX.titleFontSize2);

			this.heightTable= new HeightChoiceTable(imageHeightsData);
			imageHeightsData.addListener((Change<? extends HeightTableItem> c) -> {
				notifySettingsListeners();
			});

			TableColumn< HeightTableItem,String>  heightName = new TableColumn<HeightTableItem,String>("Name");
			heightName.setCellValueFactory(cellData -> cellData.getValue().name);
			heightName.setEditable(true);
			heightName.setCellFactory(AcceptOnExitTableCell.<HeightTableItem, String>forTableColumn(new DefaultStringConverter()));

			TableColumn<HeightTableItem,Number>  height = new TableColumn<HeightTableItem,Number>("Height (m)");
			height.setCellValueFactory(cellData -> cellData.getValue().heightProperty);
			height.setEditable(true);
			height.setCellFactory(AcceptOnExitTableCell.< HeightTableItem, Number>forTableColumn(new NumberStringConverter()));

			heightTable.getTableView().getColumns().addAll(heightName, height); 

			PamBorderPane holder = new PamBorderPane(); 
			holder.setTop(label);
			holder.setCenter(heightTable);

			return holder;
		}

		@Override
		String getName() {
			return "Image Height";
		}

		/**
		 * Manual location class. Just for making it easier to alter table data. 
		 * @author Jamie Macaulay 
		 *
		 */
		class HeightTableItem extends ChoiceTableItem {


			/**
			 * Height property
			 */
			public DoubleProperty heightProperty = new SimpleDoubleProperty(10); 


			/**
			 * Name property. 
			 */
			public StringProperty name = new SimpleStringProperty("New Image Height"); 


			public HeightTableItem() {
				addSettingsListeners();
			}

			public HeightTableItem(String name, double height){
				this.name.setValue(name); 
				this.heightProperty.setValue(height); 
				addSettingsListeners();
			}

			/**
			 * Add listeners for when change happens. 
			 */
			private void addSettingsListeners() {
				super.selectedProperty.addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners(); 
				});
				heightProperty.addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners(); 
				});
				name.addListener((obsVal, oldVal, newVal)->{
					notifySettingsListeners(); 
				});
			}

		}

		private class HeightChoiceTable extends ChoiceTable<HeightTableItem>{

			public HeightChoiceTable(ObservableList<HeightTableItem> list) {
				super(list);
			}

			@Override
			public HeightTableItem newDataItem() {
				return new HeightTableItem();
			}

		}


		@Override
		VRParameters getParams(VRParameters vrParameters) {
			VRHeightData heightData; 
			vrParameters.getHeightDatas().clear(); 
			for (int i=0; i<imageHeightsData.size(); i++) {
				heightData = new VRHeightData(imageHeightsData.get(i).name.get(), imageHeightsData.get(i).heightProperty.get()); 
				vrParameters.getHeightDatas().add(heightData); 
			}
			vrParameters.setCurrentHeightIndex(heightTable.getSelectedChoice());

			return vrParameters;
		}

		@Override
		void setParams(VRParameters vrParameters) {
			imageHeightsData.clear();
			if (vrParameters.getHeightDatas()!=null && vrParameters.getHeightDatas().size()>=1) {
				for (int i=0; i<vrParameters.getHeightDatas().size(); i++) {
					imageHeightsData.add(new HeightTableItem(vrParameters.getHeightDatas().get(i).name, 
							vrParameters.getHeightDatas().get(i).height));
				}
				heightTable.setSelectedChoice(vrParameters.getCurrentHeightIndex()); 
			}
		}

	}

	/**
	 * Pane which holds calibration values
	 * @author Jamie Macaulay 
	 *
	 */
	class CalibrationPane extends VRSettingsTab  {

		@Override
		String getName() {
			return "Image Calibration";
		}

		@Override
		VRParameters getParams(VRParameters vrParameters) {
			// TODO Auto-generated method stub
			return vrParameters;
		}

		@Override
		void setParams(VRParameters vrParameters) {
			// TODO Auto-generated method stub
		}

	}


	/**
	 * Allows the user to import tide data
	 * @author jamie
	 *
	 */
	class TidePane extends VRSettingsTab {

		public TidePane() {
			this.setCenter(createPane());
		}

		/**
		 * Create the tide pane 
		 * @return
		 */
		private Pane createPane() {

			PamButton loadTideData = new PamButton("Import Tide Data..."); 
			loadTideData.setOnAction((action)->{
				vrControl.getTideImport().showImportDialog();
			});

			PamBorderPane borderPane = new PamBorderPane(); 
			borderPane.setPadding(new Insets(5,5,5,5));
			BorderPane.setAlignment(loadTideData, Pos.TOP_CENTER);
			borderPane.setTop(loadTideData);

			return borderPane;
		} 

		@Override
		String getName() {
			return "Tide Data";
		}

		@Override
		VRParameters getParams(VRParameters vrParameters) {
			// Nothing to do here. 
			return vrParameters;
		}

		@Override
		void setParams(VRParameters vrParameters) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * Allows the user to chnage Refraction method
	 * @author Jamie Macaulay 
	 *
	 */
	class RangeMethodPane extends VRSettingsTab {

		/**
		 * The range method. 
		 */
		private ComboBox<String> rangeMethodBox;

		/**
		 * The border pane which holds everything. 
		 */
		private PamBorderPane borderPane;

		/**
		 * Constructor for the range method pane. 
		 */
		public RangeMethodPane() {
			this.setCenter(createPane());
		}

		/**
		 * Create the range method pane, allowing users to change range methods.  
		 * @return the pane for the range methods  
		 */
		private Pane createPane() {

			PamVBox topHolder = new PamVBox(); 
			topHolder.setSpacing(5);

			Label label = new Label("Range Methods");
			PamGuiManagerFX.titleFont2style(label);
//			label.setFont(PamGuiManagerFX.titleFontSize2);

			rangeMethodBox= new ComboBox<String>();
			for (int i=0; i<vrControl.getRangeMethods().getNMethods(); i++) {
				rangeMethodBox.getItems().add(vrControl.getRangeMethods().getNames().get(i)); 
			}
			rangeMethodBox.setOnAction((action)->{
				notifySettingsListeners(); 
				setSettingsPane(); 
			});

			topHolder.getChildren().addAll(label, rangeMethodBox); 


			borderPane = new PamBorderPane(); 
			borderPane.setPadding(new Insets(5,5,5,5));
			BorderPane.setAlignment(topHolder, Pos.TOP_CENTER);
			borderPane.setTop(topHolder);

			return borderPane;
		} 
		
		private void setSettingsPane() {
			VRHorzCalcMethod rangeMethod=vrControl.getRangeMethods().getMethod(rangeMethodBox.getSelectionModel().getSelectedIndex()); 
			//add range method specific settings. 
			if (rangeMethod.getRangeMethodPane()!=null) {
			borderPane.setCenter(rangeMethod.getRangeMethodPane().getContentNode()); 
			}
			else borderPane.setCenter(null);
		}

		@Override
		String getName() {
			return "Range Method";
		}

		@Override
		VRParameters getParams(VRParameters vrParameters) {
			vrParameters.rangeMethod=rangeMethodBox.getSelectionModel().getSelectedIndex(); 
			// Nothing to do here. 
			return vrParameters;
		}

		@Override
		void setParams(VRParameters vrParameters) {
			rangeMethodBox.getSelectionModel().select(vrParameters.rangeMethod);
			setSettingsPane(); 
		}

	}


	/**
	 * Each tab in the settings pane shows a node which implements this class. 
	 */
	abstract class VRSettingsTab extends PamBorderPane {

		/**
		 * Gte the name of the settings pane whgich is displayed in a tab
		 */
		abstract String getName();			


		/**
		 * Get the params for this particular pane. 
		 * @return params with settigns from the controls in the pane. 
		 */
		abstract VRParameters getParams(VRParameters vrParameters); 

		/**
		 * Set controls in pane. 
		 */
		abstract void setParams(VRParameters vrParameters); 


	}


	/**
	 * Table item for a landmark group. 
	 * @author Jamie Macaulay
	 *
	 */
	public class LandMarkGroupItem extends ChoiceTableItem {

		public LandMarkGroupItem() {
			addSettingsListeners();
		}

		public LandMarkGroupItem(LandMarkGroup landMarkGroup) {
			name.set(landMarkGroup.getName());
			nLandMarks.set(landMarkGroup.size());
			landMarks=landMarkGroup;

			addSettingsListeners();
		}

		/**
		 * Add listeners for when change happens. 
		 */
		private void addSettingsListeners() {
			super.selectedProperty.addListener((obsVal, oldVal, newVal)->{
				if (oldVal!=newVal) {
					notifySettingsListeners(); 
				}
			});
		}

		/**
		 * The name of the landmark group. 
		 */
		public StringProperty name = new SimpleStringProperty(); 


		/**
		 * The name of the landmark group. 
		 */
		public IntegerProperty nLandMarks = new SimpleIntegerProperty(); 

		/**
		 * List of landmarks. 
		 */
		public ArrayList<LandMark> landMarks; 

	}


	/**
	 * Called when external updates to settings are perfromed. 
	 * @param updateType - the update type. 
	 */
	public void update(int updateType) {

		/**
		 * Don't to reciever a settings update and then dynamic pane to send
		 * a settings update back...leads to crazy loops and stack 
		 * overflows. Disbale the settings listeners during update. 
		 */
		this.setAllowNotify(false);

		switch (updateType) {
		case VRControl.LANDMARKGROUP_CHANGE:
			this.landMarkPane.setParams(this.vrControl.getVRParams());
			break; 
		case VRControl.HEIGHT_CHANGE:
			this.heightPane.setParams(this.vrControl.getVRParams()); 
			break; 
		}

		//re enable settings listeners. 
		this.setAllowNotify(true);


	}

	/**
	 * Set the selected tab
	 * @param tab - the selected tab flag. 
	 */
	public void setTab(int tab) {
		switch (tab) {
		case HEIGHTTAB:
			mainTabPane.getSelectionModel().select(2);
			break; 
		case TIMETAB:
			mainTabPane.getSelectionModel().select(0);
			break; 
		case IMAGELOCATIONTAB:
			mainTabPane.getSelectionModel().select(1);
			break; 
		case LANDMARKTAB:
			mainTabPane.getSelectionModel().select(3);
			break; 
		case TIDETAB:
			mainTabPane.getSelectionModel().select(4);
			break; 
		case CALIBRATIONTAB:
			mainTabPane.getSelectionModel().select(5);
			break; 
		}
	}


}
