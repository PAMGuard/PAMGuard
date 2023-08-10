package Array.layoutFX;

import Array.Hydrophone;
import Array.PamArray;
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

	public HydrophonesPane() {
		
			tableArrayPane = new HydrophoneTable(hydrophoneList); 
			
			tableArrayPane.setPadding(new Insets(5,5,5,5));

			pamFlipePane = new PamFlipPane(); 
			pamFlipePane.getAdvLabel().setText("Hydrophone Settings");
			
			((Pane) hydrophonePane.getContentNode()).setPadding(new Insets(5,5,5,5)); 
			
			pamFlipePane.setAdvPaneContent(hydrophonePane.getContentNode()); 
			pamFlipePane.setFrontContent(tableArrayPane);
			
			pamFlipePane.getFront().setPadding(new Insets(5,5,5,5));
			
//			tableArrayPane.setPadding(new Insets(5,5,5,5));
			
			//this.setStyle("-fx-background-color: grey;");

			this.setCenter(pamFlipePane);
	}
	
	/**
	 * Class which extends TableSettingsPane and creates a sliding pane instead of a dialog when an item is added. 
	 * @author Jamie Macaulay 
	 *
	 */
	class HydrophoneTable extends TableSettingsPane<HydrophoneProperty> {


		public HydrophoneTable(ObservableList<HydrophoneProperty> hydrophoneData) {
			super(hydrophoneData);
			//need to set up all the rows.
			TableColumn<HydrophoneProperty,Number>  streamerID = new TableColumn<HydrophoneProperty,Number>("ID");
			streamerID.setCellValueFactory(cellData -> cellData.getValue().getID());
			streamerID.setEditable(false);
			
			
			TableColumn<HydrophoneProperty,Number>  x = new TableColumn<HydrophoneProperty,Number>("x (m)");
			x.setCellValueFactory(cellData -> cellData.getValue().getX());
			x.setEditable(false);
			
			TableColumn<HydrophoneProperty,Number>  y = new TableColumn<HydrophoneProperty,Number>("y (m)");
			y.setCellValueFactory(cellData -> cellData.getValue().getY());
			y.setEditable(false);
			
			TableColumn<HydrophoneProperty,Number>  z = new TableColumn<HydrophoneProperty,Number>("depth (m)");
			z.setCellValueFactory(cellData -> cellData.getValue().getZ());
			z.setEditable(false);

			
			TableColumn<HydrophoneProperty,Number>  xErr = new TableColumn<HydrophoneProperty,Number>("x error (m)");
			x.setCellValueFactory(cellData -> cellData.getValue().getX());
			x.setEditable(false);
			
			TableColumn<HydrophoneProperty,Number>  yErr = new TableColumn<HydrophoneProperty,Number>("y error (m)");
			y.setCellValueFactory(cellData -> cellData.getValue().getY());
			y.setEditable(false);
			
			TableColumn<HydrophoneProperty,Number>  zErr = new TableColumn<HydrophoneProperty,Number>("z error (m)");
			z.setCellValueFactory(cellData -> cellData.getValue().getZ());
			z.setEditable(false);

			getTableView().getColumns().addAll(streamerID, x, y, z, xErr, yErr, zErr);

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
			
			pamFlipePane.flipToBack();	
		}
		
		/**
		 * Set hydrophone pane within hiding pane.
		 * @param clickTypeProperty
		 */
		public void setHydrophonePane(HydrophoneProperty data){

			hydrophonePane.setCurrentArray(getCurrentArray()); 
			
			hydrophonePane.setCurrentArray(currentArray);
			hydrophonePane.setParams(data.getHydrophone());

			//now need to make sure on closing the pane that settings are saved. Need to 
			//remove the old click type from the list and add new one in the same position. 
			getFlipPaneCloseButton().setOnAction((action)->{
				pamFlipePane.flipToFront();
				
				Hydrophone hydro = hydrophonePane.getParams(data.getHydrophone());
				data.setHydrophone(hydro);

				//need to refresh table to show symbol. 
				this.getTableView().refresh();
			});
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
//			this.getDa
			hydrophoneList.add(createDefaultHydrophoneProperty(hydrophoneList.size())); 
		}

		private HydrophoneProperty createDefaultHydrophoneProperty(int id) {
			return new HydrophoneProperty(new  Hydrophone(id,  defaultx, defaulty,defaultz, defaultxErr, defaultyErr, defaultzErr,  defaulttype, defaultsensitivity,
			null, 0. ));
		}

	}
	
	public void setParams(PamArray currentArray) {
		this.currentArray=currentArray;
	}


}
