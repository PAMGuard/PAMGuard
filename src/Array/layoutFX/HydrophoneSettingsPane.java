package Array.layoutFX;

import Array.Hydrophone;
import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import Array.PamArray;
import Array.Streamer;
import PamController.PamController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.validator.PamValidator;

/**
 * The settings pane for a single hydrophones. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class HydrophoneSettingsPane extends SettingsPane<Hydrophone> {

	/**
	 * 
	 * Check inputs in real time 
	 */
	PamValidator validator = new PamValidator(); 


	@Override
	public String getName() {
		return "Hydrophone Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	private TextField iD;

	private TextField yPos;
	private TextField xPos;
	private TextField zPos;

	private TextField yPosErr;
	private TextField xPosErr;
	private TextField zPosErr;

	private PamSpinner<Double> hSens;
	private PamSpinner<Double> preampGain;

	private ComboBox<String> streamers;
	private ChoiceBox<DefaultHydrophone> defaultArrays;
	private TextField nameField;

	private PamArray currentArray;

	///Labels which might change name if in air or water (i.e. hydrophone or microphone). 

	private Labeled depthLabel;
	
	private Labeled depthLabel2;

	private Label recieverIDLabel;

	private Label recieverTypeLabel;

	private Label recieverSensLabel;

	private Label dBSensLabel;


	/**
	 * The main holder pane. 
	 */
	private PamVBox mainPane;

	private InterpSettingsPane interpPane;

	private ComboBox<String> defaultHydro;

	//create the dialog
	public HydrophoneSettingsPane() {
		super(null); 

		mainPane = new PamVBox(); 
		mainPane.setSpacing(5);


		recieverIDLabel = new Label("General");
		PamGuiManagerFX.titleFont2style(recieverIDLabel);

		Label coOrdLabel = new Label("Coordinates");
		PamGuiManagerFX.titleFont2style(coOrdLabel);

		Label interpLabel = new Label("Interpolation");
		PamGuiManagerFX.titleFont2style(interpLabel);

		interpPane = new InterpSettingsPane();

		mainPane.getChildren().addAll(recieverIDLabel, createGeneralPane(), coOrdLabel, createPositionPane(), interpLabel, interpPane); 
	}

	//	
	//	public Boolean getParams(){
	//		array.nameProperty().setValue(nameField.getText());
	//		array.hArrayTypeProperty().setValue(arrayType.getValue());
	//		try {
	//		array.xPosProperty().setValue(Double.valueOf(xPos.getText()));
	//		array.yPosProperty().setValue(Double.valueOf(yPos.getText()));
	//		array.zPosProperty().setValue(Double.valueOf(zPos.getText()));
	//		}
	//		catch (Exception e){
	//			System.err.println("Invalid field in Array Dialog"); 
	//			return false;
	//		}
	//		return true; 
	//	}
	//	
	//	public void setParams(Hydrophone hydrophone){
	//		
	//		iD.setText(String.format("%d", hydrophone.getID()));
	//		streamers.getItems().clear();
	//		
	//		//set thre text values for the recieevrs. 
	////		setRecieverLabelText();
	//		if (currentArray != null) {
	//			Streamer s;
	//			for (int i = 0; i < currentArray.getNumStreamers(); i++) {
	//				s = currentArray.getStreamer(i);
	//				streamers.getItems().add(String.format("Streamer %d, x=%3.1f", i, s.getX()));
	//			}
	//		}
	//		if (hydrophone.getStreamerId() < currentArray.getNumStreamers()) {
	//			streamers.getSelectionModel().select(hydrophone.getStreamerId());
	//		}
	//		hSens.setText(String.format("%.1f", hydrophone.getSensitivity()-PamController.getInstance().getGlobalMediumManager().getdBSensOffset()));
	//		preampGain.setText(String.format("%.1f", hydrophone.getPreampGain()));
	//		//			bandwidth0.setText(String.format("%.1f", hydrophone.getBandwidth()[0]));
	//		//			bandwidth1.setText(String.format("%.1f", hydrophone.getBandwidth()[1]));
	//		
	//		
	////		this.array=array; 
	////		nameField.setText(hydrophone.getType());
	//		parentArrayComboBox.setValue(array.get);
	//		
	//		//attachmentComboBox.setItems(ArrayModelControl.getInstance().getArrays());
	//		parentArrayComboBox.setValue(array.parentHArrayProperty().getValue());
	//				
	//		xPos.setText(Double.toString(hydrophone.);
	//		yPos.setText(Double.toString(array.yPosProperty().get()));
	//		zPos.setText(Double.toString(array.zPosProperty().get()));
	//		
	//		createArrayPane(array);
	//
	//	}


	/**
	 * Set the receiver labels depending on whether air or water is being used. 
	 */
	private void setGeneralInfoLabelText() {
		String recieverString = PamController.getInstance().getGlobalMediumManager().getRecieverString(); 
		String dbSens = PamController.getInstance().getGlobalMediumManager().getdBSensString(); 

		recieverIDLabel.setText(recieverString+ " ID Info"); 
		recieverTypeLabel.setText(recieverString + " type "); 
		recieverSensLabel.setText(recieverString + " sensitivity ");
		dBSensLabel.setText(dbSens); 
	}


	/**
	 * Set the receiver labels depending on whether air or water is being used. 
	 */
	private void setCoordsText() {
		String recieverDepthString = PamController.getInstance().getGlobalMediumManager().getZString(); 

		depthLabel.setText(recieverDepthString + " "); 

		switch (PamController.getInstance().getGlobalMediumManager().getCurrentMedium()) {
		case Air:
			depthLabel2.setText(" m (height above streamer)"); 
			break;
		case Water:
			depthLabel2.setText(" m (depth below streamer)"); 
			break;
		}

	}




	/**
	 * Create the pane to allow users to change the position of hydrophones
	 */
	private Pane createGeneralPane() {

		PamGridPane mainControls=new PamGridPane(); 
		mainControls.setHgap(5);
		mainControls.setVgap(5);

		int gridy = 0; 
		Label parentArrayLabel = new Label("Parent Array");
		parentArrayLabel.setAlignment(Pos.CENTER_RIGHT);
		mainControls.add(parentArrayLabel, 0, gridy);
		streamers = new ComboBox<String>(); 
		mainControls.add(streamers, 1, gridy);

		gridy++;
		mainControls.add(recieverTypeLabel = new Label(""), 0, gridy);
		recieverTypeLabel.setAlignment(Pos.CENTER_RIGHT);
		defaultHydro = new ComboBox<String>(); 
		
		for (int i=0; i<DefaultHydrophone.values().length; i++) {
			defaultHydro.getItems().add(DefaultHydrophone.values()[i].toString()); 
		}
		defaultHydro.getItems().add(0, "User defined"); 
		defaultHydro.getSelectionModel().select(0);
		
		defaultHydro.setOnAction((action)->{
			if (defaultHydro.getSelectionModel().getSelectedIndex() == 0) {
				//do nothing.
			}
			hSens.getValueFactory().setValue(Double.valueOf(DefaultHydrophone.values()[defaultHydro.getSelectionModel().getSelectedIndex()-1].getSens()));
			preampGain.getValueFactory().setValue(Double.valueOf(DefaultHydrophone.values()[defaultHydro.getSelectionModel().getSelectedIndex()-1].getGain()));
		});
	
		mainControls.add(defaultHydro, 1, gridy);

		gridy++;
		mainControls.add(recieverSensLabel = new Label(""), 0, gridy);
		recieverSensLabel.setAlignment(Pos.CENTER_RIGHT);
		hSens = new PamSpinner<Double>(); 
		
		hSens.valueProperty().addListener((obs, oldval, newVal)->{
			defaultHydro.getSelectionModel().select(0);
		});

		mainControls.add(hSens, 1, gridy);
		mainControls.add(dBSensLabel  = new Label(""), 2, gridy);


		gridy++;
		Label preAmpLabel = new Label("Preamplifier gain");
		mainControls.add(preAmpLabel, 0, gridy);
		preAmpLabel.setAlignment(Pos.CENTER_RIGHT);
		preampGain =new PamSpinner<Double>(); 
		preampGain.valueProperty().addListener((obs, oldval, newVal)->{
			defaultHydro.getSelectionModel().select(0);
		});
		
		mainControls.add(preampGain, 1, gridy);
		mainControls.add(new Label("dB"), 2, gridy);

		setGeneralInfoLabelText();


		return mainControls; 
	}

	/**
	 * Create the pane to allow users to change the position of hydrophones
	 */
	private Pane createPositionPane( ){

		double sectionPadding=15; 


		PamVBox mainControls=new PamVBox(); 
		mainControls.setSpacing(5);

		Insets h;

		Label nameLabel=new Label("Array Name"); 
		nameLabel.setPadding(new Insets(5,0,0,0));
		nameField=new TextField();

		//parent array. 
		Label parentArrayLabel=new Label("Parent Streamer");
		parentArrayLabel.setPadding(new Insets(sectionPadding,0,0,0));

		PamGridPane positionPane = new PamGridPane(); 
		positionPane.setHgap(5);
		positionPane.setVgap(5);

		double maxWidth =10; 

		xPos=new TextField();
		xPos.setMaxWidth(maxWidth);
		addTextValidator(xPos, "x position"); 
		yPos=new TextField();
		yPos.setMaxWidth(maxWidth);
		addTextValidator(yPos, "y position"); 
		zPos=new TextField();
		zPos.setMaxWidth(maxWidth);
		addTextValidator(zPos, "z position"); 
		depthLabel = new Label("Depth"); 
		depthLabel.setAlignment(Pos.CENTER_RIGHT);

		xPosErr=new TextField();
		xPosErr.setMaxWidth(50);
		addTextValidator(xPosErr, "x error"); 
		yPosErr=new TextField();
		yPosErr.setMaxWidth(50);
		addTextValidator(yPosErr, "y error"); 
		zPosErr=new TextField();
		zPosErr.setMaxWidth(50);
		depthLabel2 = new Label(""); //changes with air or water mode. 
		addTextValidator(zPosErr, "z error"); 

		int y=0; 
		Label xLabel = new Label("x"); 
		xLabel.setAlignment(Pos.CENTER_RIGHT);
		positionPane.add(xLabel, 0, y);
		positionPane.add(xPos, 1, y);
		positionPane.add(new Label("\u00B1"), 2, y);
		positionPane.add(xPosErr, 3, y);
		positionPane.add(new Label("m (right of streamer)"), 4, y);
		y++; 

		Label yLabel = new Label("y"); 
		yLabel.setAlignment(Pos.CENTER_RIGHT);
		positionPane.add(yLabel, 0, y);
		positionPane.add(yPos, 1, y);
		positionPane.add(new Label("\u00B1"), 2, y);
		positionPane.add(yPosErr, 3, y);
		positionPane.add(new Label("m (ahead of streamer)"), 4, y);
		y++;


		positionPane.add(depthLabel, 0, y);
		positionPane.add(zPos, 1, y);
		positionPane.add(new Label("\u00B1"), 2, y);
		positionPane.add(zPosErr, 3, y);
		positionPane.add(depthLabel2, 4, y);

		//	    ColumnConstraints col1 = new ColumnConstraints();
		//	    col1.setHgrow(Priority.ALWAYS);
		//	    positionPane.getColumnConstraints().add(col1); 

		//		Label positionLabel = new Label("Coordinates");
		//		PamGuiManagerFX.titleFont2style(positionLabel);

		mainControls.getChildren().addAll(positionPane); 

		setCoordsText(); 

		return mainControls;

	}



	/**
	 * Creates a text filed and adds a validator to check that the input is OK.
	 * @return
	 */
	private void addTextValidator(TextField userTextField, String description) {
		userTextField.setPrefColumnCount(8);

		validator.createCheck()
		.dependsOn(description, userTextField.textProperty())
		.withMethod(c -> {
			String posVal = c.get(description);
			try {
				if (Double.valueOf(posVal)==null) {
					c.error("The input for " + description + " is invalid");
				}
			}
			catch (Exception e) {
				c.error("The input for " + description + " is invalid");
			}
		})
		.decorates(userTextField)
		.immediate();
		;
	}



	@Override
	public void setParams(Hydrophone hydrophone) {
		
		//parent array stuff. 
		
		//iD.setText(String.format("%d", hydrophone.getID()));
		
		streamers.getItems().clear();
		
		//set thre text values for the recieevrs. 
		setGeneralInfoLabelText();
		if (currentArray != null) {
			Streamer s;
			for (int i = 0; i < currentArray.getNumStreamers(); i++) {
				s = currentArray.getStreamer(i);
				streamers.getItems().add(String.format("Streamer %d, x=%3.1f", i, s.getX()));
			}
		}
		if (hydrophone.getStreamerId() < currentArray.getNumStreamers()) {
			streamers.getSelectionModel().select(hydrophone.getStreamerId());
		}
		
		//hydrophone stuff
		hSens.getValueFactory().setValue(hydrophone.getSensitivity()-PamController.getInstance().getGlobalMediumManager().getdBSensOffset());
		preampGain.getValueFactory().setValue(hydrophone.getPreampGain());
		
		double zCoeff = PamController.getInstance().getGlobalMediumManager().getZCoeff(); 
		setCoordsText(); 

		xPos.setText(Double.toString(hydrophone.getX()));
		yPos.setText(Double.toString(hydrophone.getY()));
		zPos.setText(Double.toString(zCoeff*hydrophone.getZ()));
		xPosErr.setText(Double.toString(hydrophone.getdX()));
		yPosErr.setText(Double.toString(hydrophone.getdY()));
		zPosErr.setText(Double.toString(hydrophone.getdZ()));

	}


	@Override
	public Hydrophone getParams(Hydrophone hydrophone) {
		double zCoeff = PamController.getInstance().getGlobalMediumManager().getZCoeff(); 

		try {
			
			//hydrophone.setID(Integer.valueOf(iD.getText()));
			//hydrophone.setType(type.getText());
			
			hydrophone.setStreamerId(streamers.getSelectionModel().getSelectedIndex());
			hydrophone.setSensitivity(hSens.getValue()+PamController.getInstance().getGlobalMediumManager().getdBSensOffset());
			hydrophone.setPreampGain(preampGain.getValue());
//			double[] bw = new double[2];
				//				bw[0] = Double.valueOf(bandwidth0.getText());
				//				bw[1] = Double.valueOf(bandwidth1.getText());
				//				hydrophone.setBandwidth(bw);
		
			hydrophone.setX(Double.valueOf(xPos.getText()));
			hydrophone.setY(Double.valueOf(yPos.getText()));
			hydrophone.setZ(zCoeff*Double.valueOf(zPos.getText()));
			hydrophone.setdX(Double.valueOf(xPosErr.getText()));
			hydrophone.setdY(Double.valueOf(yPosErr.getText()));
			hydrophone.setdZ(Double.valueOf(zPosErr.getText()));
		}
		catch (Exception Ex) {
			System.err.println("There is a problem with one of the parameters in the Coordinates panel");
			return null;
		}
		return hydrophone;
	}

	/**
	 * Set the current array associated with the hydrophone. 
	 * @param currentArray - the current array. 
	 */
	public void setCurrentArray(PamArray currentArray) {
		this.currentArray= currentArray; 
		
	}



}
