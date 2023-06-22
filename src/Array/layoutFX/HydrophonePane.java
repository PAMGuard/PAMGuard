package Array.layoutFX;

import Array.Streamer;
import Array.layoutFX.BasicArrayPane.BasicArrayTable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.table.TableSettingsPane;

/**
 * Table which allows users to add and edit hydrophones. 
 * @author Jamie Macaulay
 *
 */
public class HydrophonePane extends PamBorderPane {
	
	/**
	 * A list of all the current hydrophones. 
	 */
	ObservableList<HydrophoneProperty> hydrophoneList = FXCollections.observableArrayList();
	
	/**
	 * The hydrophone array table.
	 */
	private HydrophoneTable tableArrayPane;

	public HydrophonePane() {
		
			tableArrayPane = new HydrophoneTable(hydrophoneList); 
			this.setCenter(tableArrayPane);
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
			// TODO Auto-generated method stub	
		}

		@Override
		public Dialog<HydrophoneProperty> createSettingsDialog(HydrophoneProperty data) {
			//we do not use dialogs here- sliding pane instead. 
//			setClassifierPane(data);
//			showFlipPane(true);		
			return null;
		}

		@Override
		public void editData(HydrophoneProperty data){
//			setClassifierPane(data);
			//showFlipPane(true);		
		}

		@Override
		public void createNewData(){
			//create a new classifier. 
//			this.getDa
//			hydrophoneData.add(createDefaultStreamerProperty()); 
		}

		private StreamerProperty createDefaultHydrophoneProperty() {
			return new StreamerProperty(new Streamer(1, 0.,0.,0.,0.,0.,0.));
		}

	}


}
