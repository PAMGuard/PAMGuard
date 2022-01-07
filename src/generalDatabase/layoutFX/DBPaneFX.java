package generalDatabase.layoutFX;

import java.util.ArrayList;

import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import PamController.SettingsPane;
import generalDatabase.DBControl;
import generalDatabase.DBParameters;
import generalDatabase.DBSystem;

/**
 * Pane for selecting a database.
 * @author Jamie Macaulay
 *
 */
public class DBPaneFX extends SettingsPane<DBParameters> {
	
	/**
	 * Holds a list of database types. 
	 */
	private ComboBox<String> systemList;
	
	/**
	 * Reference to the database control. 
	 */
	private DBControl dBControl;

	/**
	 * Clone of the current database params class. 
	 */
	private DBParameters dBParams;

	/**
	 * The main pane
	 */
	private BorderPane mainPane;

	/**
	 * The current database specific pane
	 */
	private SystemDialogPaneFX systemDialogPanel;

	
	public DBPaneFX(DBControl dbControl){
		super(null);
		this.dBControl=dbControl;
		mainPane=new PamBorderPane();
		mainPane.setCenter(createDBPane());
	}
	
	@Override
	public DBParameters getParams(DBParameters params) {

		if (systemDialogPanel != null && systemDialogPanel.getParams() == false) return null;

		dBParams.setDataBaseSystem(systemList.getSelectionModel().getSelectedIndex());

		//dBParams.setUseAutoCommit(useAutoCommit.isSelected());

		return dBParams;
	}

	
	private Node createDBPane(){
		mainPane=new PamBorderPane();
		
		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);
		
		Label dBLabel = new Label("Database System");
//		dBLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(dBLabel);
		vBox.getChildren().add(dBLabel);
		systemList=new ComboBox<String>();
		systemList.setMaxWidth(Double.MAX_VALUE);
		systemList.setOnAction((action)->{
			selectSystem();
		});
		vBox.getChildren().add(systemList);
		
		mainPane.setTop(vBox);
		mainPane.setPrefWidth(400);
		
		return mainPane;
	}

	@Override
	public void setParams(DBParameters dBParams) {
		
		this.dBParams=dBParams.clone();
		
		systemList.getItems().removeAll(systemList.getItems());
		ArrayList<DBSystem> dbSystems = dBControl.getDatabaseSystems();
		for (int i = 0; i < dbSystems.size(); i++) {
			systemList.getItems().add(dbSystems.get(i).getSystemName());
		}
		if (dBParams.getDatabaseSystem() < dbSystems.size()) {
			systemList.getSelectionModel().select(dBParams.getDatabaseSystem());
		}
		
		selectSystem();
		
	}
	
	/**
	 * Called whenever the database system is changed.
	 */
	private void selectSystem() {

		int currenIndex = systemList.getSelectionModel().getSelectedIndex();
		DBSystem currentSystem = dBControl.getSystem(currenIndex);
		if (currentSystem != null) {
			systemDialogPanel = currentSystem.getDialogPaneFX();
			if (systemDialogPanel != null) {
				mainPane.setCenter(currentSystem.getDialogPaneFX().getPane());
				systemDialogPanel.setParams();
			}
		}
		else {
			systemDialogPanel = null;
			mainPane.setCenter(null);
		}
		
		//make sure parent scene resizes 
		if (mainPane.getScene()!=null){
			Stage stage = (Stage) mainPane.getScene().getWindow();
			stage.sizeToScene();
		}
	}

	@Override
	public String getName() {
		return "Database Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}


}

