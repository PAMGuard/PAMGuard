package Array.layoutFX;

import java.util.ArrayList;
import java.util.List;

import Array.Hydrophone;
import Array.PamArray;
import PamController.PamController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamColorsFX;
import pamViewFX.fxNodes.flipPane.PamFlipPane;
import pamViewFX.fxNodes.table.TableSettingsPane;

/**
 * Table which allows users to add and edit hydrophones.
 *  
 * @author Jamie Macaulay
 *
 */
public class HydrophonesPane extends PamBorderPane {


	static final double  defaultx = 0.;
	static final double  defaulty = 0.;
	static final double defaultz = 0.;
	static final double defaultxErr = 0.;
	static final double defaultyErr = 0.; 
	static final double defaultzErr = 0.;
	static final		String defaulttype = "Unknown";
	static final double defaultsensitivity = -201; 

	/**
	 * Reference to the current array
	 */
	protected PamArray currentArray;

	/**
	 * The current hydrophone data. 
	 */
	private HydrophoneProperty currentHydrophoneData;


	/**
	 * A list of all the current hydrophones. 
	 */
	ObservableList<HydrophoneProperty> hydrophoneList = FXCollections.observableArrayList();


	/**
	 * The hydrophone array table.
	 */
	private HydrophoneTable tableArrayPane;

	private PamFlipPane  pamFlipePane; 

	/**
	 * Settings pane for a single hydrophone. 
	 */
	private HydrophoneSettingsPane hydrophonePane = new HydrophoneSettingsPane();

	/**
	 * A list of listeners which are called whenever a hydrophone is added removed or changed. 
	 */
	public ArrayList<ArrayChangeListener> hydrophoneChangeListeners = new ArrayList<ArrayChangeListener>();

	public HydrophonesPane() {

		tableArrayPane = new HydrophoneTable(hydrophoneList); 

		tableArrayPane.setPadding(new Insets(5,5,5,5));

		pamFlipePane = new PamFlipPane(); 
		pamFlipePane.getAdvLabel().setText("Hydrophone Settings");
		//			pamFlipePane.minWidthProperty().bind(this.widthProperty());
		//			pamFlipePane.setStyle("-fx-background-color: green;");


		((Pane) hydrophonePane.getContentNode()).setPadding(new Insets(5,5,5,15)); 

		pamFlipePane.setAdvPaneContent(hydrophonePane.getContentNode()); 
		pamFlipePane.setFrontContent(tableArrayPane);

		pamFlipePane.getFront().setPadding(new Insets(5,5,5,10));

		pamFlipePane.backButtonProperty().addListener((obsval, oldVal, newVal)->{

			//				System.out.println("Hello back button pressed: " +  newVal.intValue());
			//the flip pane
			if (newVal.intValue()==PamFlipPane.OK_BACK_BUTTON) {

				Hydrophone hydro = hydrophonePane.getParams(currentHydrophoneData.getHydrophone());

				if (hydro==null) {
					//the warning dialog is shown in the streamer settings pane
					return;
				}

				//					System.out.println("Hydro: " + currentHydrophoneData.getX().get()+ " "  + currentHydrophoneData.getY().get() + "  " + currentHydrophoneData.getZ().get() + " ID: " +hydro.getID()); 
				//					System.out.println("Hydro err: " + currentHydrophoneData.getXErr().get()+ " "  + currentHydrophoneData.getYErr().get() + "  " + currentHydrophoneData.getZErr().get()); 

				currentHydrophoneData.setHydrophone(hydro);

				notifyHydrophoneListeners(currentHydrophoneData);

				//need to refresh table to show symbol. 
				tableArrayPane.getTableView().refresh();
				//					
				//					System.out.println("Table size: " + tableArrayPane.getTableView().getItems().size()); 
				//					for (int i=0; i<tableArrayPane.getTableView().getItems().size(); i++) {
				//						System.out.println("Item : " + tableArrayPane.getTableView().getItems().get(i) + "  " + currentHydrophoneData);
				//					}
			}
		});

		this.setCenter(pamFlipePane);
	}

	/**
	 * Notify the hydrophone listeners of a change
	 * @param streamer - the changed streamer
	 */
	public void notifyHydrophoneListeners(HydrophoneProperty hydrophone) {
		for (ArrayChangeListener listener: hydrophoneChangeListeners) {
			listener.arrayChanged(ArrayChangeListener.HYDROPHONE_CHANGE, hydrophone);
		}
	}

	/**
	 * Class which extends TableSettingsPane and creates a sliding pane instead of a dialog when an item is added. 
	 * @author Jamie Macaulay 
	 *
	 */
	class HydrophoneTable extends TableSettingsPane<HydrophoneProperty> {


		/**
		 * The z table
		 */
		private TableColumn<HydrophoneProperty, Number>    z;

		public HydrophoneTable(ObservableList<HydrophoneProperty> hydrophoneData) {
			super(hydrophoneData);

			z = new TableColumn<HydrophoneProperty,Number>("depth");
			z.setCellValueFactory(cellData -> cellData.getValue().getZ().multiply(PamController.getInstance().getGlobalMediumManager().getZCoeff()));
			z.setEditable(true);

			//need to set up all the rows.
			TableColumn<HydrophoneProperty,Integer>  hydroID = new TableColumn<HydrophoneProperty,Integer>("ID");
			hydroID.setCellValueFactory(cellData -> cellData.getValue().getID().asObject());
			hydroID.setEditable(false);
			
		      // Default cell factory provides text field for editing and converts text in text field to int.
	        Callback<TableColumn<HydrophoneProperty, Integer>, TableCell<HydrophoneProperty, Integer>> defaultCellFactory = 
	                TextFieldTableCell.forTableColumn(new IntegerStringConverter());		
	        
		       // Cell factory implementation that uses default cell factory above, and augments the implementation
	        // by updating the value of the looked-up color cell-selection-color for the cell when the item changes:
	        Callback<TableColumn<HydrophoneProperty, Integer>, TableCell<HydrophoneProperty, Integer>> cellFactory = col -> {
	            TableCell<HydrophoneProperty, Integer> cell = defaultCellFactory.call(col);
	            cell.itemProperty().addListener((obs, oldValue, newValue) -> {
//                	System.out.println("Hello set colour: " + newValue); 
	                if (newValue == null) {
	                    cell.setStyle("cell-selection-color: -fx-selection-bar ;");
	                } else {
	                    Color color = createColor(newValue.intValue());
	                    String formattedColor = formatColor(color);
//	                    cell.setStyle("cell-selection-color: "+ formattedColor + " ;");
	                    cell.setStyle("-fx-background: "+ formattedColor + " ;");
	                    cell.setStyle("-fx-background-color: "+ formattedColor + " ;");
//	                	System.out.println("Hello set style: " + formattedColor); 
	                }
	            });
	            return cell;
	        };
	        
	        hydroID.setCellFactory(cellFactory);


			TableColumn<HydrophoneProperty,Number>  x = new TableColumn<HydrophoneProperty,Number>("x");
			x.setCellValueFactory(cellData -> cellData.getValue().getX());
			x.setEditable(true);

			TableColumn<HydrophoneProperty,Number>  y = new TableColumn<HydrophoneProperty,Number>("y");
			y.setCellValueFactory(cellData -> cellData.getValue().getY());
			y.setEditable(true);


			TableColumn posColumn=new TableColumn("Position (m)"); 
			posColumn.getColumns().addAll(x, y, z);

			TableColumn<HydrophoneProperty,Number>  xErr = new TableColumn<HydrophoneProperty,Number>("x");
			xErr.setCellValueFactory(cellData -> cellData.getValue().getXErr());
			xErr.setEditable(true);

			TableColumn<HydrophoneProperty,Number>  yErr = new TableColumn<HydrophoneProperty,Number>("y");
			yErr.setCellValueFactory(cellData -> cellData.getValue().getYErr());
			yErr.setEditable(true);

			TableColumn<HydrophoneProperty,Number>  zErr = new TableColumn<HydrophoneProperty,Number>("z");
			zErr.setCellValueFactory(cellData -> cellData.getValue().getZErr());
			zErr.setEditable(true);

			TableColumn errorColumn=new TableColumn("Errors (m)"); 
			errorColumn.getColumns().addAll(xErr, yErr, zErr);

			getTableView().getColumns().addAll(hydroID, posColumn, errorColumn);

		}
		

	    // Create color based on int value. Just use value as hue, full saturation and brightness:
	    private Color createColor(int i) {
	    	//get channel colour and add a bit of transparancy to make less abnoxious
           return  PamColorsFX.getInstance().getChannelColor(i).deriveColor(1, 1, 1, 0.5); 

//	        return Color.hsb(x, 1.0, 1.0);
	    }

	    // Format color as string for CSS (#rrggbb format, values in hex).
	    private String formatColor(Color c) {
	        int r = (int) (255 * c.getRed());
	        int g = (int) (255 * c.getGreen());
	        int b = (int) (255 * c.getBlue());
	        return String.format("#%02x%02x%02x", r, g, b);
	    }

		@Override
		public void dialogClosed(HydrophoneProperty data) {
			System.out.println("Get hydrophone paramters"); 
			Hydrophone hydro = hydrophonePane.getParams(data.getHydrophone());
			data.setHydrophone(hydro);
		}

		@Override
		public Dialog<HydrophoneProperty> createSettingsDialog(HydrophoneProperty data) {
			//we do not use dialogs here- sliding pane instead. 
			//			setClassifierPane(data);
			pamFlipePane.flipToBack();	
			return null;
		}

		@Override
		public void editData(HydrophoneProperty data){
			//			setClassifierPane(data);

			pamFlipePane.getAdvLabel().setText("Hydrophone " +  data.getID().get() + " Settings");

			hydrophonePane.setCurrentArray(currentArray);
			hydrophonePane.setParams(data.getHydrophone());

			currentHydrophoneData = data; 

			pamFlipePane.flipToBack();	
		}


		private PamArray getCurrentArray() {
			return currentArray;
		}

		/**
		 * Get the button which closes the hiding pane. 
		 * @return button which closes the hiding pane. 
		 */
		public Button getFlipPaneCloseButton() {
			return pamFlipePane.getBackButton();
		}

		@Override
		public void createNewData(){
			HydrophoneProperty hydrophone = createDefaultHydrophoneProperty(hydrophoneList.size());
			//create a new classifier. 
			hydrophoneList.add(hydrophone); 
			notifyHydrophoneListeners(hydrophone);
		}


		@Override
		public void deleteData(HydrophoneProperty data){
			super.deleteData(data);
			//the ID number for hydrophone sis actually important for where they are in the list. Bit a legacy issue but no
			//point in messes everything up to fix. So, when a hydrophone is deleted must update all the ID numbers. 
			
			updateIDNumbers(); 
			
			notifyHydrophoneListeners(data);
		}

		/**
		 * Update the ID numbers. 
		 */
		private void updateIDNumbers() {
			for (int i=0; i<getData().size(); i++){
				getData().get(i).id.set(i);
			}
		}

		private HydrophoneProperty createDefaultHydrophoneProperty(int id) {
			return new HydrophoneProperty(new  Hydrophone(id,  defaultx, defaulty,defaultz, defaultxErr, defaultyErr, defaultzErr,  defaulttype, defaultsensitivity,
					null, 0. ));
		}


		public TableColumn<HydrophoneProperty, Number> getZColumn() {
			return z;
		}

		/**
		 * Get the current streamers. 
		 * @return the current streamers. 
		 */
		public ObservableList<HydrophoneProperty> getHydrophones() {
			return getData();
		}


	}

	public void setParams(PamArray currentArray) {
		this.currentArray=currentArray;

		tableArrayPane.getHydrophones().clear();

		for (int i=0; i<currentArray.getHydrophoneCount(); i++) {
			tableArrayPane.getHydrophones().add(new HydrophoneProperty(currentArray.getHiddenHydrophone(i))); 
		}

		//update ID numbers just incase. 
		tableArrayPane.updateIDNumbers();


	}

	public synchronized PamArray getParams(PamArray currParams) {
		
		currParams.clearArray();

		Hydrophone hydrophone;
		for (int i=0; i<tableArrayPane.getHydrophones().size(); i++) {
			hydrophone = tableArrayPane.getHydrophones().get(i).getHydrophone();
			hydrophone.setID(i);
			currParams.addHydrophone(hydrophone); 
		}

		return currParams;
	}


	/**
	 * Get the current hydrophone list. 
	 * @return the current hydrophone list. 
	 */
	public ObservableList<HydrophoneProperty> getHydrophoneList() {
		return hydrophoneList;
	}

	public void setHydrophoneList(ObservableList<HydrophoneProperty> hydrophoneList) {
		this.hydrophoneList = hydrophoneList;
	}

	public void setRecieverLabels() {
		tableArrayPane.getZColumn().setText(PamController.getInstance().getGlobalMediumManager().getZString());
		hydrophonePane.setRecieverLabels();
	}

	/**
	 * Add a listener which is called whenever a hydrophone is added, removed or changed. 
	 * @param e - the listener to add
	 */
	public void addStreamerListener(ArrayChangeListener e) {
		hydrophoneChangeListeners.add(e); 

	}

	/**
	 * Select the current hydrophone in table. 
	 */
	public void selectHydrophone(Hydrophone hydrophone) {
		//select the current hydrophone in the table 
		tableArrayPane.getTableView().getSelectionModel().select(hydrophone.getID());
	}

	public void setCurrentArray(PamArray currentArray) {
		this.currentArray=currentArray;
		
	}

	/**
	 * Get the hydrophone interpolation. Note that this is stored in the
	 * currentArray because the interpolator must be the same for all hydrophones.
	 * 
	 * @return the inteprolation selection.
	 */
	public int getHydrophoneInterp() {
		return currentArray.getHydrophoneInterpolation();
	}

}
