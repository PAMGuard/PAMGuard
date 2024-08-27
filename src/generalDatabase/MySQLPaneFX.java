package generalDatabase;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import PamUtils.PamCalendar;
import generalDatabase.layoutFX.SystemDialogPaneFX;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;

public class MySQLPaneFX extends PamBorderPane implements SystemDialogPaneFX {
	
	ServerBasedSystem serverBasedSystem;
		
	TextField userName, ipAddress, portNumber;
	
	PasswordField passWord;
	
	Button connectServer, newDatabase;
	
	Label connectionStatus;
	
	ComboBox databaseList;
	
	public MySQLPaneFX (ServerBasedSystem serverBasedSystem) {
		this.serverBasedSystem = serverBasedSystem;
		this.setCenter(createMYSQLPane());
	}
	
	private Pane createMYSQLPane(){
		
		PamVBox vBox=new PamVBox(); 
		
		Label serverLabel = new Label(serverBasedSystem.getSystemName() + " Server");
		PamGuiManagerFX.titleFont2style(serverLabel);
//		serverLabel.setFont(PamGuiManagerFX.titleFontSize2);
		vBox.getChildren().add(serverLabel);
		
		PamGridPane serverGridPane=new PamGridPane(); 
		serverGridPane.setVgap(5.);
		serverGridPane.setHgap(5.);
		
		serverGridPane.add(new Label("Server Address"), 0, 0);
		serverGridPane.add(ipAddress=new TextField(), 1, 0);
		
		serverGridPane.add(new Label("Port Number"), 0, 1);
		serverGridPane.add(portNumber=new TextField(), 1, 1);
		
		serverGridPane.add(new Label("User Name"), 0, 2);
		serverGridPane.add(userName=new TextField(), 1, 2);
		
		serverGridPane.add(new Label("Password"), 0, 3);
		serverGridPane.add(passWord=new PasswordField(), 1, 3);
		
		serverGridPane.add(connectServer=new PamButton("Connect"), 1, 4);
		connectServer.setOnAction((action->{
			connectServer();
		}));
		GridPane.setHalignment(connectServer, HPos.RIGHT);

		
		vBox.getChildren().add(serverGridPane);
		
		Label databaseLabel = new Label(serverBasedSystem.getSystemName() + " Database");
//		databaseLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(databaseLabel);

		vBox.getChildren().add(databaseLabel);
		
		PamGridPane databaseGridPane=new PamGridPane(); 
		databaseGridPane.setVgap(5.);
		databaseGridPane.setHgap(5.);
		//keep this grid pane the same as server grid pane
		databaseGridPane.prefWidthProperty().bind(serverGridPane.widthProperty());
		
		databaseGridPane.add(new Label("Database"), 0, 0);
		databaseGridPane.add(databaseList=new ComboBox<String>(), 1, 0);
		databaseList.setMaxWidth(Double.MAX_VALUE);
		
		databaseGridPane.add(connectionStatus=new Label("No Connection"), 0, 1);
		databaseGridPane.add(newDatabase=new PamButton("Create New"), 1, 1);
		newDatabase.setOnAction((action)->{
			createNewDatabase();
		});
		GridPane.setHalignment(newDatabase, HPos.RIGHT);

		vBox.getChildren().add(databaseGridPane);
				
		//now add the section
		return vBox; 
		

		
	}

	@Override
	public Pane getPane() {
		return this;
	}

	MySQLParameters tempParams;
	@Override
	public boolean getParams() {
		tempParams = serverBasedSystem.mySQLParameters.clone();
		
		if (!getServerParams()) return false;
		
		if (!getDbParams()) return false;
		
		// now if all ok, copy back.
		serverBasedSystem.mySQLParameters = tempParams.clone();
		return true;
	}
	
	public boolean getServerParams() {
		if (tempParams == null){
			tempParams = serverBasedSystem.mySQLParameters.clone();
		}
		try {
			tempParams.ipAddress = ipAddress.getText();
			tempParams.userName = userName.getText();
			tempParams.passWord = new String(passWord.getText());
			tempParams.portNumber = Integer.valueOf(portNumber.getText());
		}
		catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Override
	public void setParams() {
		ipAddress.setText(serverBasedSystem.mySQLParameters.ipAddress);
		userName.setText(serverBasedSystem.mySQLParameters.userName);
		passWord.setText(serverBasedSystem.mySQLParameters.passWord);
		portNumber.setText(String.format("%d",serverBasedSystem.mySQLParameters.portNumber));
		
		setDbParams(false);

		sayServerStatus();
	}
	
	public void setDbParams(boolean newList) {
		databaseList.getItems().removeAll(databaseList.getItems());
		ArrayList<String> dbList = serverBasedSystem.getAvailableDatabases(newList);
		int selIndex = -1;
		if (dbList != null) for (int i = 0; i < dbList.size(); i++) {
			databaseList.getItems().add(dbList.get(i));
			if (dbList.get(i).equalsIgnoreCase(serverBasedSystem.mySQLParameters.databaseName)) {
				selIndex = i;
			}
		}
		databaseList.getSelectionModel().select(selIndex);
	}
	public boolean getDbParams() {
		if (tempParams == null){
			tempParams = serverBasedSystem.mySQLParameters.clone();
		}
		tempParams.databaseName = (String) databaseList.getSelectionModel().getSelectedItem();
		return (tempParams.databaseName != null);
	}

	/**
	 * Connect the SQL server. 
	 */
	private void connectServer(){
		if (!getServerParams()) return;
		serverBasedSystem.serverConnect(tempParams);
		sayServerStatus();
	}
		

	
	private boolean lastStatus = false;
	void sayServerStatus() {
		if (serverBasedSystem.isServerConnected()) {
			connectionStatus.setText("Connected");
		}
		else  {
			connectionStatus.setText("No Connection");
		}
		if (lastStatus != serverBasedSystem.isServerConnected()) {
			setDbParams(true);
			lastStatus = serverBasedSystem.isServerConnected();
		}
		enableControls();
	}
	
	void enableControls() {
		boolean ok = serverBasedSystem.isServerConnected();
		newDatabase.setDisable(!ok);
		databaseList.setDisable(!ok);
	}
	
	
	/**
	 * Create a new MYSQL database. 
	 */
	private void createNewDatabase(){

			String defaultName = null;
			long now = PamCalendar.getTimeInMillis();
			if (now == 0) {
				now = System.currentTimeMillis();
			}
			String newName = PamCalendar.createFileName(now, "PamDatabase_", "");
//			if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
				defaultName = (String) JOptionPane.showInputDialog(null, "Enter a name for a new datase", 
						"Database selection", JOptionPane.QUESTION_MESSAGE, null, null, newName);
//			}
			if (defaultName == null || defaultName.length() == 0) {
				defaultName = PamCalendar.createFileName(now, "PamDatabase_", "");
			}
			// fill any blanks
			defaultName = defaultName.replace(" ", "_");
			if (serverBasedSystem.createNewDatabase(defaultName)) {
				setDbParams(true);
		}
		
	}
}
