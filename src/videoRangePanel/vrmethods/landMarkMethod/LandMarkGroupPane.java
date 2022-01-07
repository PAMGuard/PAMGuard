package videoRangePanel.vrmethods.landMarkMethod;

import java.text.NumberFormat;
import java.util.ArrayList;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import PamUtils.LatLong;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.table.ChoiceTable;
import pamViewFX.fxNodes.table.ChoiceTableItem;
import pamViewFX.fxStyles.PamStylesManagerFX;
import videoRangePanel.layoutFX.VRSettingsPane;
import videoRangePanel.layoutFX.VRSettingsPane.LandMarkGroupItem;

/**
 * Pane to edit a group of landmarks 
 * 
 * 
 * @author Jamie Macaulay 
 *
 */
public class LandMarkGroupPane extends PamBorderPane {

	/**
	 * The table
	 */
	private LandMarkTable landMarkTable;

	/**
	 * The list of landmarks. 
	 */
	ObservableList<LandMarkItem> landMarkList= FXCollections.observableArrayList();

	/**
	 * The group text field. 
	 */
	private TextField groupTextField; 
	
	/**
	 * Imports and exports landmark groups to .csv file. 
	 */
	private  LandMarkCSVLogging landMarkCSVLogging = new  LandMarkCSVLogging();

	
	/**
	 * The VR settings pane. 
	 */
	private VRSettingsPane settingsPane;

	private LandMarkGroupItem currentLandMarkGroupItem;

	private PopOver popOver;

	/**
	 * Pane with controls to enter data for a landmark. 
	 */
	private LandMarkPane landMarkPane; 
	
	/**
	 * The current table item selected by the pop up menu. 
	 */
	private LandMarkItem currentPopItem; 

	/**
	 * Number format for latitutde and longitude
	 */
	private NumberFormat latLongNumberFormat;

	private NumberFormat angleNumberFormat; 

	
	public LandMarkGroupPane(VRSettingsPane settingsPane) {
		this.settingsPane=settingsPane; 
		this.setCenter(createPane());
		
		latLongNumberFormat=NumberFormat.getInstance();
		latLongNumberFormat.setMaximumFractionDigits(10);
		
		
		angleNumberFormat=NumberFormat.getInstance();
		angleNumberFormat.setMaximumFractionDigits(8);
	}

	/**
	 * Create the pane. 
	 * @return the pane. 
	 */
	private Pane createPane() {
		
		//the group name 
		groupTextField = new TextField(); 
		PamHBox hBox= new PamHBox(); 
		hBox.setSpacing(5);
		hBox.getChildren().addAll(new Label("Group Name"),  groupTextField);
		hBox.setAlignment(Pos.CENTER);
		HBox.setHgrow(groupTextField, Priority.ALWAYS);
		hBox.setPadding(new Insets(5,0,5,0));
		
		landMarkTable = new LandMarkTable(landMarkList); 
		
		//add option to show pop up to enter data; bit more like the old system 
		//create pop over menu
		landMarkPane= new LandMarkPane(null); 
		
		popOver=new PopOver(); 
		popOver.setFadeInDuration(new Duration(100));
		popOver.setFadeOutDuration(new Duration(100));
		popOver.setContentNode(landMarkPane.getContentNode()); 
		popOver.setArrowLocation(ArrowLocation.TOP_LEFT);
		popOver.setCornerRadius(5);
		
		//when pop over is closed must set the params 
		
		popOver.setOnHidden((value)->{
			if (currentPopItem==null) return; 
			LandMark landMark = landMarkPane.getParams(null); 
			if (landMark!=null) {
				currentPopItem.setLandMarkValues(landMark); 
			}
			else {
				//TODO - warning dialog. 
			}
			currentPopItem=null; 
		});
	
		
		//add option to show pop up to enter data; bit more like the old system 
		landMarkTable.getTableView().setOnMousePressed((e)->{
		        if (e.isSecondaryButtonDown()) {
		        	//create a landmark to set params 
		        	landMarkPane.setParams(this.landMarkItem2LandMark(currentPopItem=landMarkTable.getTableView().getSelectionModel().getSelectedItem()));
		        	
		        	//show the pop over 
		        	popOver.show(landMarkTable.getTableView(),e.getScreenX(), e.getScreenY());
		        	
		        	//make pop over dark 
		        	((Parent) popOver.getSkin().getNode()).getStylesheets()
		    	    .add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
//		            System.out.println(landMarkTable.getTableView().getSelectionModel().getSelectedItem());                   
		        }
		});

		//		PamButton button = new PamButton("Test"); 
		//		button.setOnAction((action)->{
		//			landMarkTable.getTableView().refresh();
		//		});

		landMarkTable.getButtonPane().getChildren().clear(); 
		
		PamButton importButton = new PamButton(); 
//		importButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_IMPORT, PamGuiManagerFX.iconSize));
		importButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-import", PamGuiManagerFX.iconSize));
		importButton.setOnAction((action)->{
			//import a landmark group
			LandMarkGroup newLandMarkGroup = landMarkCSVLogging.importLandMarks(this.getScene().getWindow());
			if (newLandMarkGroup!=null) {
				LandMarkGroupItem item =settingsPane.new LandMarkGroupItem(newLandMarkGroup);
				setParams(item); 
			}
		});
		importButton.setTooltip(new Tooltip("Import a landmark group from a .csv file"));

		PamButton exportButton = new PamButton(); 
//		exportButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_EXPORT, PamGuiManagerFX.iconSize));
		exportButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-export", PamGuiManagerFX.iconSize));
		exportButton.setOnAction((action)->{
			//export a landmark group. 
			landMarkCSVLogging.export(VRSettingsPane.getLandMarkGroupObject(getParams(currentLandMarkGroupItem)),
					this.getScene().getWindow());
		});
		exportButton.setTooltip(new Tooltip("Export a landmark group to a .csv file"));

		landMarkTable.getButtonPane().getChildren().addAll(landMarkTable.getAddButton(), landMarkTable.getRemoveButton(), importButton, exportButton);

		PamBorderPane mainPane = new PamBorderPane(); 
		
		mainPane.setTop(hBox);
		mainPane.setCenter(landMarkTable);
		//mainPane.setLeft(button);


		return mainPane;
	}

	/**
	 * Set the landmarks to be shown on the pane. 
	 * @param landMarkGroupItem
	 */
	public void setParams(LandMarkGroupItem landMarkGroupItem) {
		
		this.currentLandMarkGroupItem= landMarkGroupItem; 
		
		this.groupTextField.setText(landMarkGroupItem.name.get());
		
		//this is a bit of a pain but making sure the LandMarkItems are converted to standard LandMark 
		//objects keeps this all compatible with non JavaFX stuff. 
		landMarkList.clear();
		if (landMarkGroupItem.landMarks!=null) {
			for (int i=0; i<landMarkGroupItem.landMarks.size(); i++) {
				this.landMarkList.add(new LandMarkItem(landMarkGroupItem.landMarks.get(i)));
			}
		}
	}
	
	/**
	 * Get the params. 
	 * @param - the landmark group to set params for. Create a new group if null.
	 * @return the landmark group. 
	 */
	public LandMarkGroupItem  getParams(LandMarkGroupItem landMarkGroupItem) {
		
		this.landMarkTable.getTableView().refresh();
		
		if (landMarkGroupItem==null) {
			System.out.println("LandMarkGroupPane: The landmark group item is null!!");
			return null; 
		}
		
		ArrayList<LandMark> landMarks = new ArrayList<LandMark>(); 
		LandMark landMark; 
		for (int i=0; i<landMarkList.size(); i++ ) {
			landMark=landMarkItem2LandMark(landMarkList.get(i));
			landMarks.add(landMark);
		}
		
		landMarkGroupItem.nLandMarks.setValue(landMarks.size()); 
		landMarkGroupItem.landMarks=landMarks;
		landMarkGroupItem.name.setValue(groupTextField.getText());
		
		return landMarkGroupItem; 
	}
	
	/**
	 * 
	 * Convert a LandMarkItem to a LandMark object
	 * @param lndMrkItem - the landmark item. 
	 */
	private LandMark landMarkItem2LandMark(LandMarkItem lndMrkItem) {
		LandMark landMark = new LandMark(); 
		
		landMark.setName(lndMrkItem.name.get());
		
		//lat,long landmark
		if (lndMrkItem.heightProperty.get()!=null && lndMrkItem.latilatitudeProperty!=null
				&& lndMrkItem.longitudeProperty!=null)
		landMark.setPosition(new LatLong(lndMrkItem.latilatitudeProperty.get().doubleValue(), 
				lndMrkItem.longitudeProperty.get().doubleValue(), lndMrkItem.heightProperty.get().doubleValue()));
		
		//bearing landmark
		if (lndMrkItem.bearingProperty.get()!=null)
		landMark.setBearing(new Double(lndMrkItem.bearingProperty.get().doubleValue()));
		if (lndMrkItem.pitchProperty.get()!=null)
		landMark.setPitch(new Double(lndMrkItem.pitchProperty.get().doubleValue()));
		
		if (lndMrkItem.sourceLatitudeProperty.get()!=null && lndMrkItem.sourceLongitudeProperty.get()!=null) {
			landMark.setLatLongOrigin(new LatLong(lndMrkItem.sourceLatitudeProperty.get().doubleValue(),
					lndMrkItem.sourceLongitudeProperty.get().doubleValue()));
			landMark.setHeightOrigin(lndMrkItem.sourceHeightProperty.get().doubleValue());
		}
		return landMark; 
	}



	private class LandMarkTable extends ChoiceTable<LandMarkItem> {

		public LandMarkTable(ObservableList<LandMarkItem> list) {
			super(list);
			
			super.setSingleChoice(false);

			TableColumn< LandMarkItem,String>  landMarkName = new TableColumn<LandMarkItem,String>("LandMark Name");
			landMarkName.setCellValueFactory(cellData -> cellData.getValue().name);
			landMarkName.setEditable(true);
			//landMarkName.setCellFactory(TextFieldTableCell.<LandMarkItem, String>forTableColumn(new DefaultStringConverter()));
			landMarkName.setCellFactory(column -> {
				return new ErrTextFieldCell<LandMarkItem>(); 
			});


			//			this.getTableView().getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			//				System.out.println("New selected item!"); 
			//				this.getTableView().refresh();
			//			});


			
			TableColumn<LandMarkItem,Number> latitude = new TableColumn<LandMarkItem,Number>("Latitiude (decimal)");
			latitude.setCellValueFactory(cellData -> cellData.getValue().latilatitudeProperty);
			latitude.setEditable(true);
			latitude.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(latLongNumberFormat); 
			});
			
			//latitude.setCellFactory(TextFieldTableCell.< LandMarkItem, Number>forTableColumn(new NumberStringConverter()));

			TableColumn<LandMarkItem,Number>  longitude = new TableColumn<LandMarkItem,Number>("Longitude (decimal)");
			longitude.setCellValueFactory(cellData -> cellData.getValue().longitudeProperty);
			longitude.setEditable(true);
			longitude.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(latLongNumberFormat); 
			});
			//l

			TableColumn<LandMarkItem,Number>  height = new TableColumn<LandMarkItem,Number>("Height (m)");
			height.setCellValueFactory(cellData -> cellData.getValue().heightProperty);
			height.setEditable(true);
			height.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(); 
			});

			TableColumn<LandMarkItem,Number>  bearing = new TableColumn<LandMarkItem,Number>("Bearing (\u00B0)");
			bearing.setCellValueFactory(cellData -> cellData.getValue().bearingProperty);
			bearing.setEditable(true);
			bearing.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(angleNumberFormat); 
			});


			TableColumn<LandMarkItem,Number>  pitch = new TableColumn<LandMarkItem,Number>("Pitch (\u00B0)");
			pitch.setCellValueFactory(cellData -> cellData.getValue().pitchProperty);
			pitch.setEditable(true);
			pitch.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(angleNumberFormat); 
			});


			TableColumn<LandMarkItem,Number>  latitudeOrigin = new TableColumn<LandMarkItem,Number>("Latitude Origin (decimal)");
			latitudeOrigin.setCellValueFactory(cellData -> cellData.getValue().sourceLatitudeProperty);
			latitudeOrigin.setEditable(true);
			latitudeOrigin.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(latLongNumberFormat); 
			});


			TableColumn<LandMarkItem,Number>  longitudeOrigin = new TableColumn<LandMarkItem,Number>("LongitudeOrigin (decimal)");
			longitudeOrigin.setCellValueFactory(cellData -> cellData.getValue().sourceLongitudeProperty);
			longitudeOrigin.setEditable(true);
			longitudeOrigin.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(latLongNumberFormat); 
			});


			TableColumn<LandMarkItem,Number>  heightOrigin = new TableColumn<LandMarkItem,Number>("Height Origin (m)");
			heightOrigin.setCellValueFactory(cellData -> cellData.getValue().sourceHeightProperty);
			heightOrigin.setEditable(true);
			heightOrigin.setCellFactory(column -> {
				return new ErrNumberFieldCell<LandMarkItem>(); 
			});

			this.getTableView().getColumns().addAll(landMarkName, latitude, longitude, height, bearing,
					pitch, latitudeOrigin, longitudeOrigin, heightOrigin); 
		}

		@Override
		public LandMarkItem newDataItem() {
			return new LandMarkItem();
		}

	}

	/**
	 * A single landmark.
	 * <p>
	 * Note: Need to use SimpleObjectProperty<Number> rather than DoubleProperty as DoubleProperty does 
	 * not support null values and therefore, because these properties are bound to table cells, are 
	 * incapabable of showing an empty table cell. The value defaults to 0.0. 
	 * 
	 * @author Jamie Macaulay 
	 *
	 */
	class LandMarkItem extends ChoiceTableItem {
	

		/*
		 * The name of the landmark 
		 */
		StringProperty name = new SimpleStringProperty(); 

		//Option 1: latitude and longitude. 

		/**
		 * The latitude of the landmark  
		 */
		SimpleObjectProperty<Number> latilatitudeProperty = new SimpleObjectProperty<Number>(); 


		/**
		 * The longitude of the landmark 
		 */
		SimpleObjectProperty<Number> longitudeProperty = new SimpleObjectProperty<Number>(); 

		/**
		 * The height of the landmark 
		 */
		SimpleObjectProperty<Number> heightProperty = new SimpleObjectProperty<Number>(); 

		//Option 2: Bearing measurements from a known location

		/**
		 * The height of the landmark 
		 */
		SimpleObjectProperty<Number> bearingProperty = new SimpleObjectProperty<Number>(); 

		/**
		 * The height of the landmark 
		 */
		SimpleObjectProperty<Number> pitchProperty  = new SimpleObjectProperty<Number>(); 

		/**
		 * The height of the landmark 
		 */
		SimpleObjectProperty<Number> sourceLatitudeProperty = new SimpleObjectProperty<Number>(); 

		/**
		 * The height of the landmark 
		 */
		SimpleObjectProperty<Number> sourceLongitudeProperty = new SimpleObjectProperty<Number>(); 

		/**
		 * The height of the landmark 
		 */
		SimpleObjectProperty<Number> sourceHeightProperty = new SimpleObjectProperty<Number>(); 

		public LandMarkItem() {
			this.selectedProperty.set(true);
		}


		public LandMarkItem(LandMark landMark) {
			this.selectedProperty.set(true);
			setLandMarkValues(landMark);
		}
		
		
		/**
		 * Set values of the item from LandMark data. 
		 */
		public void setLandMarkValues(LandMark landMark) {
			this.name.setValue(landMark.getName());
			
			if (landMark.getPosition()!=null) {
				this.latilatitudeProperty.setValue(landMark.getPosition().getLatitude());
				this.longitudeProperty.setValue(landMark.getPosition().getLongitude());
				this.heightProperty.setValue(landMark.getHeight());
			}
			
			this.bearingProperty.setValue(landMark.getBearing());
			this.pitchProperty.setValue(landMark.getPitch());
			if (landMark.getLatLongOrigin()!=null) {
				this.sourceLatitudeProperty.setValue(landMark.getLatLongOrigin().getLatitude());
				this.sourceLongitudeProperty.setValue(landMark.getLatLongOrigin().getLongitude());
				this.sourceHeightProperty.setValue(landMark.getHeightOrigin());
			}
		}

		@Override
		public boolean checkItem(ObservableValue value) {

			/**
			 * Here there are a number of options
			 * 1)If both bearing and landmarks have no values then whole row is red
			 * 2)If a lat,lon landmark is filled but not complete then it is red but the other is normal
			 * 3) If either a lat,lon landmark is filled then 
			 */

			if (value==name) {
				if (name.get()!=null && name.get()!="") return true; 
				else return false; 
			}
			else {
				boolean latlonLandmark= checkLatLonLandMarks() ;
				boolean bearingLandMark  = checkBearingLandMarks() ;


				//check if the latitude 
				if (value==latilatitudeProperty || value==longitudeProperty || value==heightProperty) {
					if (!latlonLandmark && !bearingLandMark) return  false ; 
					else return true; 
				}

				if (value==bearingProperty || value==pitchProperty || value==sourceLongitudeProperty
						|| value==sourceLatitudeProperty || value==sourceHeightProperty) {
					if (!latlonLandmark && !bearingLandMark) return  false ; 
					else return true; 
				}
			}

			return false; 
		}

		/**
		 * Check lat, lon, height landmarks. 
		 * @return true if there are all values. 
		 */
		public boolean checkLatLonLandMarks() {
			boolean latOk=latilatitudeProperty.getValue()!=null; 
			boolean lonOk=longitudeProperty.getValue()!=null;
			boolean heightOk=heightProperty.getValue()!=null;

			if (latOk && lonOk && heightOk) return true;
			else return false; 

		}

		/**
		 * Check the bearing landmarks . 
		 * @return
		 */
		public boolean checkBearingLandMarks() {		

			boolean bearingOk=bearingProperty.getValue()!=null;
			boolean pitchOk=pitchProperty.getValue()!=null;
			boolean latOk=sourceLatitudeProperty.getValue()!=null; 
			boolean lonOk=sourceLongitudeProperty.getValue()!=null;
			boolean heightOk=sourceHeightProperty.getValue()!=null;

			if (bearingOk && pitchOk && latOk && lonOk && heightOk) {
				
				//these must always be the same values in the table. 
				for (int i=0; i<landMarkList.size(); i++) {
					landMarkList.get(i).sourceLatitudeProperty.set(sourceLatitudeProperty.get());
					landMarkList.get(i).sourceLongitudeProperty.set(sourceLongitudeProperty.get());
					landMarkList.get(i).sourceHeightProperty.set(sourceHeightProperty.get());

				}
				return true;
			}
			else return false; 
		}

	} 


}
