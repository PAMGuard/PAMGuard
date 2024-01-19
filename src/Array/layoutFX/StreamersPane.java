package Array.layoutFX;

import java.util.ArrayList;

import Array.PamArray;
import Array.Streamer;
import PamController.PamController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.flipPane.PamFlipPane;
import pamViewFX.fxNodes.table.TableSettingsPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;

/**
 *  A pane for setting up hydrophones. Note that this is entirely separate from PAMGuard so can be used in
 *  other projects. 
 *  
 * @author Jamie Macaulay
 *
 */
public class StreamersPane extends PamBorderPane {

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
	
	/**
	 * A list of listeners which are called whenever a streamer is added removed or changed. 
	 */
	public ArrayList<ArrayChangeListener> streamerChangeListeners = new ArrayList<ArrayChangeListener>();
	
	
	public StreamersPane() {

		tableArrayPane = new BasicArrayTable(streamerData); 

		tableArrayPane.setPadding(new Insets(5,5,5,5));
		this.setCenter(tableArrayPane);

		pamFlipePane = new PamFlipPane(); 
		pamFlipePane.getAdvLabel().setText("Streamer");
		pamFlipePane.setMaxWidth(Double.MAX_VALUE);

		((Pane) streamerPane.getContentNode()).setPadding(new Insets(5,5,5,15)); 

		pamFlipePane.setAdvPaneContent(streamerPane.getContentNode()); 
		pamFlipePane.setFrontContent(tableArrayPane);

		pamFlipePane.getFront().setPadding(new Insets(5,5,5,10));
		pamFlipePane.setAdvLabelEditable(true); 
		pamFlipePane.getPostAdvLabel().setText("Settings");
		
		
		pamFlipePane.backButtonProperty().addListener((obsval, oldVal, newVal)->{
			
//			System.out.println("Hello back button pressed: " +  newVal.intValue());

			//the flip pane
			if (newVal.intValue()==PamFlipPane.OK_BACK_BUTTON) {
				
				Streamer streamer = streamerPane.getParams(currentStreamerData.getStreamer());
								
				if (streamer==null) {
					//the warning dialog is shown in the streamer settings pane
					return;
				}
				
				streamer.setStreamerName(pamFlipePane.getAdvLabel().getText()); 
				
				currentStreamerData.setStreamer(streamer);
				
				notifyStreamerListeners(currentStreamerData);
//				System.out.println("Update streamer: " + tableArrayPane.getStreamers().indexOf(currentStreamerData) + " no. streamers: " + currentArray.getNumStreamers());

				//need to refresh table to show symbol. 
				tableArrayPane.getTableView().refresh();
				
				if (streamer != null) {
					streamer.setupLocator(currentArray);
					streamer.makeStreamerDataUnit();
					//update the streamer in the current array
				}
			}
		});

		this.setCenter(pamFlipePane);

	}

	/**
	 * Notify the streamer listeners of a change
	 * @param streamer - the changed streamer
	 */
	public void notifyStreamerListeners(StreamerProperty streamer) {
		for (ArrayChangeListener listener: streamerChangeListeners) {
			listener.arrayChanged(ArrayChangeListener.STREAMER_CHANGE, streamer);
		}
	}

	/**
	 * Class which extends TableSettingsPane and creates a sliding pane instead of a dialog when an item is added. 
	 * @author Jamie Macaulay 
	 *
	 */
	class BasicArrayTable extends TableSettingsPane<StreamerProperty> {
		
		private TableColumn<StreamerProperty, Number> z;


		public BasicArrayTable(ObservableList<StreamerProperty> data) {
			super(data);
			//need to set up all the rows.
			TableColumn<StreamerProperty,Number>  streamerID = new TableColumn<StreamerProperty,Number>("ID");
			streamerID.setCellValueFactory(cellData -> cellData.getValue().getID());
			streamerID.setEditable(false);

			TableColumn<StreamerProperty,String>  name = new TableColumn<StreamerProperty,String>("Name");
			name.setCellValueFactory(cellData -> cellData.getValue().getName());
			name.setEditable(true);


			TableColumn<StreamerProperty,Number>  x = new TableColumn<StreamerProperty,Number>("x");
			x.setCellValueFactory(cellData -> cellData.getValue().getX());
			x.setEditable(false);

			TableColumn<StreamerProperty,Number>  y = new TableColumn<StreamerProperty,Number>("y");
			y.setCellValueFactory(cellData -> cellData.getValue().getY());
			y.setEditable(false);

			z = new TableColumn<StreamerProperty,Number>("depth");
			z.setCellValueFactory(cellData -> cellData.getValue().getZ().multiply(PamController.getInstance().getGlobalMediumManager().getZCoeff()));
			z.setEditable(false);
			
			TableColumn posColumn=new TableColumn("Position (m)"); 
			posColumn.getColumns().addAll(x, y, z);

			TableColumn<StreamerProperty,String>  reference = new TableColumn<StreamerProperty,String>("Reference");
			reference.setCellValueFactory(cellData -> cellData.getValue().getHydrophoneOrigin());
			reference.setEditable(true);

			TableColumn<StreamerProperty,String>  locator = new TableColumn<StreamerProperty,String>("Locator");
			locator.setCellValueFactory(cellData -> cellData.getValue().getHydrophineLocator());
			locator.setEditable(true);
			
			TableColumn geoColumn=new TableColumn("Geo-reference"); 
			geoColumn.getColumns().addAll(reference, locator);


			getTableView().getColumns().addAll(streamerID, name, posColumn, geoColumn);

		}

		/**
		 * Get the current streamers. 
		 * @return the current streamers. 
		 */
		public ObservableList<StreamerProperty> getStreamers() {
			return getData();
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
			//edit streamer data. 
			if (data.getName() == null){
				pamFlipePane.getAdvLabel().setText("Streamer " +  data.getID().get());
			}
			
			streamerPane.setCurrentArray(currentArray);
			streamerPane.setParams(data.getStreamer());
		
			currentStreamerData = data; 
			
			pamFlipePane.flipToBack();					
		}

		@Override
		public void createNewData(){
			StreamerProperty newStreamer = createDefaultStreamerProperty();
			//create a new classifier. 
			streamerData.add(newStreamer); 
			//add to the current array.
			currentArray.addStreamer(newStreamer.getStreamer()); 
			System.out.println("Create new streamer: " + currentArray.getNumStreamers());
			
			notifyStreamerListeners(newStreamer);

		}
		
		@Override
		public void deleteData(StreamerProperty data){
			super.deleteData(data);
			notifyStreamerListeners(null);
		}

		private StreamerProperty createDefaultStreamerProperty() {
			Streamer streamer = new Streamer(1, 0.,0.,0.,0.,0.,0.);
			return new StreamerProperty(streamer);
		}
		
		public TableColumn<StreamerProperty, Number> getZColumn() {
			return z;
		}


	}

	/**
	 * Set the parameters for the streamer pane. 
	 * @param currentArray - the current array. 
	 */
	public void setParams(PamArray currentArray) {
		
		this.currentArray=currentArray.clone();
		
		System.out.println("Set params streamer: " + currentArray.getNumStreamers());
		
		tableArrayPane.getStreamers().clear();

		for (int i=0; i<currentArray.getStreamerCount(); i++) {
			tableArrayPane.getStreamers().add(new StreamerProperty(currentArray.getStreamer(i))); 
		}
	}
	
	/**
	 * Get the parameters from the streamer pane. 
	 * @param currParams - the current parameters. 
	 * @return the PamArray with updated streamers. 
	 */
	public PamArray getParams(PamArray currParams) {
		
		//add all new streamers - bit weird because the PamArray requires that at least one streamer exists.
				
		for (int i=0; i<tableArrayPane.getStreamers().size(); i++) {
			
			if (i<currentArray.getStreamerCount()) {
				currParams.updateStreamer(i,tableArrayPane.getStreamers().get(i).getStreamer());
			}
			else {
				currParams.addStreamer(tableArrayPane.getStreamers().get(i).getStreamer());
			}
		}
		
		while (currParams.getStreamerCount()>tableArrayPane.getStreamers().size()) {
			currParams.removeStreamer(currParams.getStreamerCount()-1);
		}
		
//		currentArray.updateStreamer(tableArrayPane.getStreamers().indexOf(currentStreamerData), streamer);
		
		return currParams;
	}

	public void setRecieverLabels() {
		tableArrayPane.getZColumn().setText(PamController.getInstance().getGlobalMediumManager().getZString());
		streamerPane.setRecieverLabels();
	}

	public TableView<StreamerProperty> getStreamerTable() {
		return tableArrayPane.getTableView();
	}

	/**
	 * Add a listener which is called whenever a streamer is added, removed or changed. 
	 * @param e - the listener to add
	 */
	public void addStreamerListener(ArrayChangeListener e) {
		this.streamerChangeListeners.add(e); 
	}

	public void setCurrentArray(PamArray currentArray) {
		this.currentArray=currentArray;
		
	}


}
