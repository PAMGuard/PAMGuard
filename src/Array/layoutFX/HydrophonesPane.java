package Array.layoutFX;

import java.util.ArrayList;

import Array.Hydrophone;
import Array.PamArray;
import PamController.PamController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;
import javafx.scene.control.TableColumn;
import pamViewFX.fxNodes.PamBorderPane;
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
			z.setCellValueFactory(cellData -> cellData.getValue().getZ());
			z.setEditable(false);

			//need to set up all the rows.
			TableColumn<HydrophoneProperty,Number>  hydroID = new TableColumn<HydrophoneProperty,Number>("ID");
			hydroID.setCellValueFactory(cellData -> cellData.getValue().getID());
			hydroID.setEditable(false);
			
			
			TableColumn<HydrophoneProperty,Number>  x = new TableColumn<HydrophoneProperty,Number>("x");
			x.setCellValueFactory(cellData -> cellData.getValue().getX());
			x.setEditable(false);
			
			TableColumn<HydrophoneProperty,Number>  y = new TableColumn<HydrophoneProperty,Number>("y");
			y.setCellValueFactory(cellData -> cellData.getValue().getY());
			y.setEditable(false);
			
			
			TableColumn posColumn=new TableColumn("Position (m)"); 
			posColumn.getColumns().addAll(x, y, z);
			
			TableColumn<HydrophoneProperty,Number>  xErr = new TableColumn<HydrophoneProperty,Number>("x");
			xErr.setCellValueFactory(cellData -> cellData.getValue().getXErr());
			xErr.setEditable(false);
			
			TableColumn<HydrophoneProperty,Number>  yErr = new TableColumn<HydrophoneProperty,Number>("y");
			yErr.setCellValueFactory(cellData -> cellData.getValue().getYErr());
			yErr.setEditable(false);
			
			TableColumn<HydrophoneProperty,Number>  zErr = new TableColumn<HydrophoneProperty,Number>("z");
			zErr.setCellValueFactory(cellData -> cellData.getValue().getZErr());
			zErr.setEditable(false);
			
			TableColumn errorColumn=new TableColumn("Errors (m)"); 
			errorColumn.getColumns().addAll(xErr, yErr, zErr);

			getTableView().getColumns().addAll(hydroID, posColumn, errorColumn);

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
			//create a new classifier. 
			hydrophoneList.add(createDefaultHydrophoneProperty(hydrophoneList.size())); 
		}

		private HydrophoneProperty createDefaultHydrophoneProperty(int id) {
			return new HydrophoneProperty(new  Hydrophone(id,  defaultx, defaulty,defaultz, defaultxErr, defaultyErr, defaultzErr,  defaulttype, defaultsensitivity,
			null, 0. ));
		}
		

		public TableColumn<HydrophoneProperty, Number> getZColumn() {
			return z;
		}


	}
	
	public void setParams(PamArray currentArray) {
		//TODO
		this.currentArray=currentArray;
	}

	public PamArray getParams(PamArray currParams) {
		//TODO
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

}
