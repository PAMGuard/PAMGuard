package Array.layoutFX;

import Array.Hydrophone;
import Array.PamArray;
import Array.Streamer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.flipPane.PamFlipPane;
import pamViewFX.fxNodes.table.TableSettingsPane;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;

/**
 *  A pane for setting up hydrophones. Note that this is entirely separate from PAMGuard so can be used in
 *  other projects. 
 *  
 * @author Jamie Macaulay
 *
 */
public class StreamerPane extends PamBorderPane {

	BasicArrayTable tableArrayPane;

	ObservableList<StreamerProperty> streamerData = FXCollections.observableArrayList();

	/**
	 * The current hydrophone array
	 */
	private PamArray currentArray;

	/**
	 * The pam flip pane. 
	 */
	private PamFlipPane pamFlipePane;

	/**
	 * The current streamer data. 
	 */
	private StreamerProperty currentStreamerData;

	/**
	 * Settings pane for a single hydrophone. 
	 */
	private StreamerSettingsPane streamerPane = new StreamerSettingsPane(); 

	public StreamerPane() {

		tableArrayPane = new BasicArrayTable(streamerData); 

		tableArrayPane.setPadding(new Insets(5,5,5,5));
		this.setCenter(tableArrayPane);

		pamFlipePane = new PamFlipPane(); 
		pamFlipePane.getAdvLabel().setText("Hydrophone Settings");

		((Pane) streamerPane.getContentNode()).setPadding(new Insets(5,5,5,5)); 

		pamFlipePane.setAdvPaneContent(streamerPane.getContentNode()); 
		pamFlipePane.setFrontContent(tableArrayPane);

		pamFlipePane.getFront().setPadding(new Insets(5,5,5,10));

		pamFlipePane.flipFrontProperty().addListener((obsval, oldVal, newVal)->{
			//the flip pane
			if (newVal) {
				Streamer hydro = streamerPane.getParams(currentStreamerData.getStreamer());
				currentStreamerData.setStreamer(hydro);

				//need to refresh table to show symbol. 
				tableArrayPane.getTableView().refresh();
			}
		});

		this.setCenter(pamFlipePane);

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
			Streamer hydro = streamerPane.getParams(data.getStreamer());
			data.setStreamer(hydro);
		}

		@Override
		public Dialog<StreamerProperty> createSettingsDialog(StreamerProperty data) {
			//we do not use dialogs here- sliding pane instead. 
			//			setClassifierPane(data);
			//			showFlipPane(true);		
			pamFlipePane.flipToBack();	
			return null;
		}

		@Override
		public void editData(StreamerProperty data){

			pamFlipePane.getAdvLabel().setText("Streamer " +  data.getID().get() + " Settings");
			
//			streamerPane.setCurrentArray(currentArray);
			streamerPane.setParams(data.getStreamer());
		
			currentStreamerData = data; 
			
			pamFlipePane.flipToBack();					
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

	public void setParams(PamArray currentArray) {
		this.currentArray=currentArray;
	}

	public PamArray getParams(PamArray currParams) {
		// TODO Auto-generated method stub
		return null;
	}


}
