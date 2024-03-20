package dataMap.layoutFX;

import dataGram.DatagramManager;
import dataGram.DatagramSettings;
import dataMap.DataMapControl;
import dataMap.DataMapParameters;
import PamController.PamController;
import PamUtils.PamCalendar;
import binaryFileStorage.BinaryStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.sliders.PamSlider;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * Allows uses to change the horizontal and vertical scales on the data map. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ScalePaneFX extends PamBorderPane {
	
	/*
	 * Reference to the data map control. 
	 */
	private DataMapControl dataMapControl;
	
	/**
	 * Reference to the dataMapPane. 
	 */
	private DataMapPaneFX dataMapPane;

	/**
	 * The slider which determines time scale. 
	 */
	private Slider timeSlider;

	/**
	 * Shows the time scale in pix/hour
	 */
	private Label timeScaleLabel;

	/**
	 * Selects unit count on vertical scale
	 */
	private ComboBox<String> scaleBox;

	/**
	 * Check box for log vertical scale. 
	 */
	private PamToggleSwitch logScaleBox;
	
	/**
	 * The chosen time values. 
	 */
	private double[] timeScaleChoices = DataMapParameters.hScaleChoices;
	
	/**
	 * Combo box holding options to channge the datargam bin size
	 */
	private ComboBox<String> datagramBox;

	/**
	 * Holds a list of times for the datagram bin size. 
	 */
	private ComboBox<String> dataGramComboBox;

	/**
	 * Holds everything. 
	 */
	private PamVBox holder;

	/**
	 * Grid pane with settings for the data scales
	 */
	private PamGridPane scaleSettingsPane;

	private Label dataGramLabel; 



	public ScalePaneFX(DataMapControl dataMapControl, DataMapPaneFX dataMapPane) {
		this.dataMapControl = dataMapControl;
		this.dataMapPane = dataMapPane;
		
//		//create the holder pane. 
//		PamVBox vBoxHolder=new PamVBox(); 
//		vBoxHolder.setSpacing(5);
//		Label title=new Label("Scales"); 
//		PamGuiManagerFX.titleFont2style(title);
//		vBoxHolder.getChildren().addAll(title, controlPane);
		
		holder=new PamVBox(); 
		holder.setSpacing(20);
		holder.getChildren().add(scaleSettingsPane = createScalePane());
		
		this.setCenter(holder); 
		
		//set params for the pane		
		setParams(dataMapControl.dataMapParameters);
		checkDataGramPane(); // create datagram pane if a binary store already added. 
		sayHScale();
		
	}

	/**
	 * Adds a settings pane for the datagram if a binary store is present. 
	 */
	public void checkDataGramPane(){
		if (BinaryStore.findBinaryStoreControl()!=null){
			DatagramManager dataGramManager=BinaryStore.findBinaryStoreControl().getDatagramManager();
			if (dataGramManager!=null && datagramBox==null) {
				datagramBox=createDatagramPane(dataGramManager);
				scaleSettingsPane.add(dataGramLabel= new Label("Datagram bin size"), 0, 2);
				scaleSettingsPane.add(dataGramComboBox, 1, 2);
			}
		}
		else {
			scaleSettingsPane.getChildren().remove(dataGramLabel);
			scaleSettingsPane.getChildren().remove(dataGramComboBox);

			datagramBox=null; 
		}
	}

	
	/**
	 * Create the a datagram combo box to change the size of the datagram
	 * @param dataGramManager - the datagram manager for the current biinary store. 
	 * @return a combo box with datagram bin sizes. 
	 */
	private ComboBox<String> createDatagramPane(DatagramManager dataGramManager){

		dataGramComboBox=new ComboBox<String>(createDurationList(dataGramManager)); 
		
		dataGramComboBox.getSelectionModel().select(durationToString(dataGramManager.getDatagramSettings().datagramSeconds*1000L));
		dataGramComboBox.valueProperty().addListener(( ov,  t,  t1) -> {                
			if (t==t1) return; 
			else {
				PamController.getInstance();
				boolean ans=PamDialogFX.showWarning(PamController.getMainStage(), "Warning", "<html>Recalculating the datagram for a large dataset may take a long time<br>" +
						"Are you sure you want to continue ?</html>"); 
				if (ans){
					int index=dataGramComboBox.getSelectionModel().getSelectedIndex();
					//messy- datagramSeconfds should be in millis but left for backwards compatibility. 
					dataGramManager.getDatagramSettings().datagramSeconds=(int) (DatagramSettings.defaultDatagramSeconds[index]/1000);
					dataGramManager.updateDatagrams();
					dataMapPane.repaintAll();
				}
			}
	     });
		
		return dataGramComboBox;
		
	}
	
	/**
	 * Convert duration to string.
	 * @param duration in  millis.
	 */
	private String durationToString(long duration){
		return (" " + PamCalendar.formatDuration(duration));
	}
	
	/**
	 * Create list of load times. 
	 * @return list of load times duration. 
	 */
	private ObservableList<String> createDurationList(DatagramManager dataGramManager){
		ObservableList<String> loadTimeList=FXCollections.observableArrayList();
		for (int i=0; i<DatagramSettings.defaultDatagramSeconds.length; i++){
			loadTimeList.add(durationToString(DatagramSettings.defaultDatagramSeconds[i]));
		}
		return loadTimeList;
	}

	
	private PamGridPane createScalePane() {
	
		PamGridPane controlPane=new PamGridPane(); 
		controlPane.setHgap(5);
		controlPane.setVgap(5);
		
		//create time scale controls
		controlPane.add(new Label("Time window"),0,1);
		
		//create time slider 
		timeSlider=new Slider(0, timeScaleChoices.length-1, 1); 
		timeSlider.setShowTickLabels(true);
		timeSlider.setShowTickMarks(true);
		timeSlider.setMajorTickUnit(1);
		
		timeSlider.setLabelFormatter(new ScaleStringConverter());
		
//		PamGridPane.setHalignment(timeSlider, Pos.BOTTOM_CENTER);

		controlPane.add(timeSlider,1,1);
		//add listener to time slider to change datamap. 
		timeSlider.valueProperty().addListener((ov, oldVal, newVal)->{
			sayHScale();
			dataMapPane.scaleChanged();
		}); 
		 
		controlPane.add(timeScaleLabel=new Label(""),3,1);
		
		//create vertical scale controls
		controlPane.add(new Label("Data counts"),0,0);
		

		scaleBox=new ComboBox<String> (); 
		scaleBox.getItems().add("No Scaling");
		scaleBox.getItems().add("per Second");
		scaleBox.getItems().add("per Minute");
		scaleBox.getItems().add("per Hour");
		scaleBox.getItems().add("per Day");
		controlPane.add(scaleBox,1,0);
		scaleBox.setPrefWidth(200);

		scaleBox.valueProperty().addListener((ov, oldVal, newVal)->{
			dataMapPane.scaleChanged();
		}); 

		 
		logScaleBox=new PamToggleSwitch("Log Scale"); 
		logScaleBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			dataMapPane.scaleChanged();
		});
		
		
		controlPane.add(logScaleBox,3,0);
			

		return controlPane;
	}
	
	class ScaleStringConverter extends StringConverter<Double> {

		@Override
		public String toString(Double object) {
			return String.valueOf(timeScaleChoices[object.intValue()]);
		}

		@Override
		public Double fromString(String string) {
			return Double.valueOf(string);
		}
		
	}
	
	/**
	 * Show the horizontal scale. 
	 */
	private void sayHScale() {
		double hChoice = timeSlider.getValue();
		timeScaleLabel.setText(String.format("%s pixs/hour", new Double(timeScaleChoices[(int) hChoice]).toString()));
	}
	
	//HACK use setting flag to avoid immediate callback which overwrites changes 2 and 3 ! 
	boolean setting = false;
	
	public void setParams(DataMapParameters dataMapParameters) {
		setting = true;
		timeSlider.setValue(dataMapParameters.hScaleChoice);
		scaleBox.getSelectionModel().select(dataMapParameters.vScaleChoice);
		logScaleBox.setSelected(dataMapParameters.vLogScale);
		setting = false;
	}
	
	
	public void getParams(DataMapParameters dataMapParameters) {
		if (setting) return;
		dataMapParameters.hScaleChoice = (int) timeSlider.getValue(); 
		dataMapParameters.vScaleChoice = scaleBox.getSelectionModel().getSelectedIndex();
		dataMapParameters.vLogScale = logScaleBox.isSelected();
	}


}
