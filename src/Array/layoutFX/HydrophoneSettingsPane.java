package Array.layoutFX;

import Array.Hydrophone;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.synedra.validatorfx.Validator;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.validator.PamValidator;

/**
 * The settings pane for a single hydrophones. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class HydrophoneSettingsPane extends DynamicSettingsPane<Hydrophone> {

	private static final double COLUMN_0_WIDTH = 120;
	
	
	private final static double MAX_TEXTFIELD_WIDTH = 80; 

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
	
	private boolean ressetHydrophoneType = false; 

	/**
	 * The main holder pane. 
	 */
	private PamBorderPane mainPane;

	private InterpChoicePane interpPane;

	private ComboBox<String> defaultHydro;

	//create the dialog
	public HydrophoneSettingsPane() {
		super(null); 

		PamVBox holderPane = new PamVBox(); 
		holderPane.setSpacing(5);


		recieverIDLabel = new Label("General");
		PamGuiManagerFX.titleFont2style(recieverIDLabel);

		Label coOrdLabel = new Label("Coordinates");
		PamGuiManagerFX.titleFont2style(coOrdLabel);

		Label interpLabel = new Label("Interpolation");
		PamGuiManagerFX.titleFont2style(interpLabel);

		interpPane = new InterpChoicePane();
		
		PamHBox interpHolder = new PamHBox(); 
		interpHolder.setSpacing(5);
		interpHolder.setAlignment(Pos.CENTER_LEFT);
		interpHolder.getChildren().addAll(new Label("Method"), interpPane); 

		holderPane.getChildren().addAll(recieverIDLabel, createGeneralPane(), coOrdLabel, createPositionPane(), interpLabel, interpHolder); 

		mainPane = new PamBorderPane(); 
		mainPane.setCenter(holderPane);
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
		recieverSensLabel.setText(recieverString + " sens ");
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
		parentArrayLabel.setAlignment(Pos.CENTER_LEFT);
		mainControls.add(parentArrayLabel, 0, gridy);
		streamers = new ComboBox<String>(); 
		mainControls.add(streamers, 1, gridy);

		gridy++;
		mainControls.add(recieverTypeLabel = new Label(""), 0, gridy);
		recieverTypeLabel.setAlignment(Pos.CENTER_LEFT);
		defaultHydro = new ComboBox<String>(); 
		
		for (int i=0; i<DefaultHydrophone.values().length; i++) {
			defaultHydro.getItems().add(DefaultHydrophone.values()[i].toString()); 
		}
		defaultHydro.getItems().add(0, "User defined"); 
		defaultHydro.getSelectionModel().select(0);
		
		defaultHydro.setOnAction((action)->{
			//don't want to trigger this if we are programtically setting it back
			if (defaultHydro.getSelectionModel().getSelectedIndex() <= 0 || ressetHydrophoneType) {
				//do nothing.
				return;
			}
			ressetHydrophoneType=true;
			hSens.getValueFactory().setValue(Double.valueOf(DefaultHydrophone.values()[defaultHydro.getSelectionModel().getSelectedIndex()-1].getSens()));
			preampGain.getValueFactory().setValue(Double.valueOf(DefaultHydrophone.values()[defaultHydro.getSelectionModel().getSelectedIndex()-1].getGain()));
			ressetHydrophoneType=false;
		});
	
		mainControls.add(defaultHydro, 1, gridy);

		gridy++;
		mainControls.add(recieverSensLabel = new Label(""), 0, gridy);
		recieverSensLabel.setAlignment(Pos.CENTER_LEFT);
		hSens = new PamSpinner<Double>(-Double.MAX_VALUE, Double.MAX_VALUE, -200., 1.); 
		hSens.setEditable(true);
		
		hSens.valueProperty().addListener((obs, oldval, newVal)->{
			if (ressetHydrophoneType) return;
			ressetHydrophoneType = true; //make sure we don't trigger anything when resetting the combo box
			defaultHydro.getSelectionModel().select(0);
			ressetHydrophoneType= false;
		});

		mainControls.add(hSens, 1, gridy);
		mainControls.add(dBSensLabel  = new Label(""), 2, gridy);


		gridy++;
		Label preAmpLabel = new Label("Preamplifier gain");
		mainControls.add(preAmpLabel, 0, gridy);
		preAmpLabel.setAlignment(Pos.CENTER_LEFT);
		preampGain =new PamSpinner<Double>(-Double.MAX_VALUE, Double.MAX_VALUE, 0., 1.); 
		preampGain.valueProperty().addListener((obs, oldval, newVal)->{
			if (ressetHydrophoneType) return;
			ressetHydrophoneType = true;//make sure we don't trigger anything when resetting the combo box
			defaultHydro.getSelectionModel().select(0);
			ressetHydrophoneType= false;
		});
		preampGain.setEditable(true);

		
		mainControls.add(preampGain, 1, gridy);
		mainControls.add(new Label("dB"), 2, gridy);

	    ColumnConstraints col1 = new ColumnConstraints();
	    col1.setMinWidth(COLUMN_0_WIDTH);
	    col1.setMaxWidth(COLUMN_0_WIDTH);
	    mainControls.getColumnConstraints().addAll(col1);
		
		setGeneralInfoLabelText();


		return mainControls; 
	}

	/**
	 * Create the pane to allow users to change the position of hydrophones
	 */
	private Pane createPositionPane(){

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
	
		ColumnConstraints rc = new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, MAX_TEXTFIELD_WIDTH);
		//this sets all text fields to the correct width - but of naff hack but what grid pane needs to work. 
		for (int i=1; i<5; i++) {
			positionPane.getColumnConstraints().add(rc);
		}
		
		double maxWidth =10; 

		xPos=new TextField();
		xPos.textProperty().addListener((obsVal, oldVal, newVal)->{
			notifySettingsListeners();
		});
		xPos.setMaxWidth(maxWidth);
		addTextValidator(xPos, "x position", validator); 
		
		yPos=new TextField();
		yPos.setMaxWidth(maxWidth);
		addTextValidator(yPos, "y position", validator); 
		yPos.textProperty().addListener((obsVal, oldVal, newVal)->{
			notifySettingsListeners();
		});
		
		zPos=new TextField();
		zPos.setMaxWidth(maxWidth);
		zPos.textProperty().addListener((obsVal, oldVal, newVal)->{
			notifySettingsListeners();
		});
		
		addTextValidator(zPos, "z position", validator); 
		depthLabel = new Label("Depth"); 
		depthLabel.setAlignment(Pos.CENTER);

		xPosErr=new TextField();
		xPosErr.setMaxWidth(50);
		addTextValidator(xPosErr, "x error",validator); 
		yPosErr=new TextField();
		yPosErr.setMaxWidth(50);
		addTextValidator(yPosErr, "y error",validator); 
		zPosErr=new TextField();
		zPosErr.setMaxWidth(50);
		depthLabel2 = new Label(""); //changes with air or water mode. 
		depthLabel2.setAlignment(Pos.CENTER);
		addTextValidator(zPosErr, "z error", validator); 

		int col=0; 
		int row =0; 
		
		Label xLabel = new Label("x"); 
		xLabel.setAlignment(Pos.CENTER);
		
		Label yLabel = new Label("y"); 
		yLabel.setAlignment(Pos.CENTER);

		col=1;
		positionPane.add(xLabel, col++, row);
		positionPane.add(yLabel, col++, row);
		positionPane.add(depthLabel, col++, row);

		col=0; 
		row++; 
		
		Label positionLabel = new Label("Position"); 
		positionPane.add(positionLabel, col++, row);
		positionPane.add(xPos, col++, row);
		positionPane.add(yPos, col++, row);
		positionPane.add(zPos, col++, row);
		positionPane.add(new Label("(m)"), col++, row);

		col=0;
		row++;
		
		Label errLabel = new Label("Error"); 
		positionPane.add(errLabel, col++, row);
		positionPane.add(xPosErr, col++, row);
		positionPane.add(yPosErr, col++, row);
		positionPane.add(zPosErr, col++, row);
		positionPane.add(new Label("(m)"), col++, row);

//		positionPane.add(new Label("\u00B1"), col, 2);
//		positionPane.add(xPosErr, col, 3);
//		positionPane.add(new Label("m (right of streamer)"), col, 5);
		
		col++; 

//		Label yLabel = new Label("y"); 
//		yLabel.setAlignment(Pos.CENTER);
//		positionPane.add(yLabel, col, 0);
//		positionPane.add(yPos, col, 1);
//		positionPane.add(new Label("\u00B1"), col, 2);
//		positionPane.add(yPosErr, col, 3);
//		positionPane.add(new Label("m (ahead of streamer)"), col, 4);
//		col++;
//
//
//		positionPane.add(depthLabel, col, 0);
//		positionPane.add(zPos, col, 1);
//		positionPane.add(new Label("\u00B1"), col, 2);
//		positionPane.add(zPosErr, col, 3);
//		positionPane.add(depthLabel2, col, 4);

		//	    ColumnConstraints col1 = new ColumnConstraints();
		//	    col1.setHgrow(Priority.ALWAYS);
		//	    positionPane.getColumnConstraints().add(col1); 

		//		Label positionLabel = new Label("Coordinates");
		//		PamGuiManagerFX.titleFont2style(positionLabel);

		mainControls.getChildren().addAll(positionPane); 
		
	    ColumnConstraints col1 = new ColumnConstraints();
	    col1.setMinWidth(COLUMN_0_WIDTH);
	    col1.setMaxWidth(COLUMN_0_WIDTH);
	    positionPane.getColumnConstraints().addAll(col1);
		

		setCoordsText(); 

		return mainControls;

	}



	/**
	 * Creates a text filed and adds a validator to check that the input is OK.
	 * @return
	 */
	protected static void addTextValidator(TextField userTextField, String description, Validator validator) {
		//userTextField.setPrefColumnCount(8);

		validator.createCheck()
		.dependsOn(description, userTextField.textProperty())
		.withMethod(c -> {
			String posVal = c.get(description);
			
			/**
			 * Ok, this is weird. So if the c.error is called then somehow it messes up
			 * the sizing of the pane i.e. it does not resize..
			 */
			
			try {
				if (posVal.isEmpty() || Double.valueOf(posVal)==null) {
					c.error("The input for " + description + " is invalid");
				}
			}
			catch (Exception e) {
				c.error("The input for " + description + " is invalid");
			}
		})
		.decorates(userTextField).immediate();
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
		
		interpPane.setSelection(currentArray.getHydrophoneInterpolation());

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
			
			int hi = interpPane.getSelection();
			if (hi >= 0) {
				this.currentArray.setHydrophoneInterpolation(interpPane.getSelectedInterpType());
			}			
		}
		catch (Exception Ex) {
			System.err.println("There is a problem with one of the parameters in the hydrophone panel");
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

	public void setRecieverLabels() {
		 setGeneralInfoLabelText();
		
	}
	
	




}
