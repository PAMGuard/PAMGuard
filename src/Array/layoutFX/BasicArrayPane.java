package Array.layoutFX;

import Array.Streamer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.table.TableSettingsPane;
import javafx.scene.control.TableColumn;

/**
 *  A pane for setting up hydrophones. Note that this is entirely separate from PAMGuard so can be used in
 *  other projects. 
 *  
 * @author Jamie Macaulay
 *
 */
public class BasicArrayPane extends PamBorderPane {
	
	BasicArrayTable tableArrayPane;
	
	ObservableList<StreamerProperty> streamerData = FXCollections.observableArrayList();
	
		
	public BasicArrayPane() {
		
		 tableArrayPane = new BasicArrayTable(streamerData); 
		 
		 this.setCenter(tableArrayPane);
		
	}
	
	/**
	 * Class which extends TableSettingsPane and creates a sliding pane instead of a dialog when an item is added. 
	 * @author Jamie Macaulay 
	 *
	 */
	class BasicArrayTable extends TableSettingsPane<StreamerProperty> {

		public BasicArrayTable(ObservableList<StreamerProperty> data) {
			super(data);
			//need to set up all the rows.
			TableColumn<StreamerProperty,Number>  streamerID = new TableColumn<StreamerProperty,Number>("ID");
			streamerID.setCellValueFactory(cellData -> cellData.getValue().getID());
			streamerID.setEditable(false);
		        
			TableColumn<StreamerProperty,String>  name = new TableColumn<StreamerProperty,String>("Name");
			name.setCellValueFactory(cellData -> cellData.getValue().getName());
			name.setEditable(false);
			
			
			TableColumn<StreamerProperty,Number>  x = new TableColumn<StreamerProperty,Number>("x (m)");
			x.setCellValueFactory(cellData -> cellData.getValue().getX());
			x.setEditable(false);
			
			TableColumn<StreamerProperty,Number>  y = new TableColumn<StreamerProperty,Number>("y (m)");
			y.setCellValueFactory(cellData -> cellData.getValue().getY());
			y.setEditable(false);
			
			TableColumn<StreamerProperty,Number>  z = new TableColumn<StreamerProperty,Number>("z (m)");
			z.setCellValueFactory(cellData -> cellData.getValue().getZ());
			z.setEditable(false);

	
			TableColumn<StreamerProperty,String>  reference = new TableColumn<StreamerProperty,String>("Reference");
			reference.setCellValueFactory(cellData -> cellData.getValue().getHydrophineLocator());
			reference.setEditable(true);
			
			TableColumn<StreamerProperty,String>  locator = new TableColumn<StreamerProperty,String>("Locator");
			locator.setCellValueFactory(cellData -> cellData.getValue().getHydrophineLocator());
			locator.setEditable(true);


			getTableView().getColumns().addAll(streamerID, name, x, y, z, reference, locator);

		}

		@Override
		public void dialogClosed(StreamerProperty data) {
			// TODO Auto-generated method stub	
		}

		@Override
		public Dialog<StreamerProperty> createSettingsDialog(StreamerProperty data) {
			//we do not use dialogs here- sliding pane instead. 
//			setClassifierPane(data);
//			showFlipPane(true);		
			return null;
		}

		@Override
		public void editData(StreamerProperty data){
//			setClassifierPane(data);
			//showFlipPane(true);		
		}

		@Override
		public void createNewData(){
			//create a new classifier. 
			streamerData.add(createDefaultStreamerProperty()); 
		}

		private StreamerProperty createDefaultStreamerProperty() {
			return new StreamerProperty(new Streamer(1, 0.,0.,0.,0.,0.,0.));
		}

	}


}
