package clickDetector.layoutFX;

import java.text.DecimalFormat;
import java.text.ParseException;

import clickDetector.ClickControl;
import clickDetector.ClickParameters;
import clickDetector.layoutFX.clickClassifiers.ClickClassifyPaneFX;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.StringConverter;
import net.synedra.validatorfx.Validator;
import PamController.PamController;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.FilterPaneFX;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;
import pamViewFX.validator.PamValidator;

/**
 * A pane to change click detector settings. 
 * @author Jamie Macaulay	
 */
public class ClickSettingsPane extends SettingsPane<ClickParameters>{

	public static double PREF_SPINNER_WIDTH = 140; 

	/**
	 * Group source pane for the click settings pane.
	 */
	private GroupedSourcePaneFX sourcePane;

	/*
	 * Clone of current click parameters.
	 */
	private ClickParameters clickParameters;

	/**
	 * Reference to the click controller. 
	 */
	private ClickControl clickControl;

	/**
	 * The main tab pane. 
	 */
	private PamTabPane pamTabbedPane;

	/**
	 * Pane for the pre-filter. The acoustic data detected click waveforms are extracted from but not
	 * the data the actual detector works on, that is the trigger filter.  
	 */
	private FilterPaneFX preFilter;

	/**
	 * The trigger filter. i.e. the data the click detector works on
	 */
	private FilterPaneFX triggerFilter;

	/**
	 * Spinner for threshold. 
	 */
	private PamSpinner<Double> threshold;

	/**
	 * Spinner for long filter
	 */
	private PamSpinner<Double> longFilter;

	/**
	 * Long filter 2 spinner. 
	 */
	private PamSpinner<Double> longFilter2;

	/**
	 * Short filter.
	 */
	private PamSpinner<Double> shortFilter;

	/**
	 * All trigger boxes. 
	 */
	protected CheckBox[] triggerBoxes;

	/**
	 * Pane containing trigger channels. 
	 */
	private Pane triggerChannels;

	/**
	 * Select all trigger boxes check box. 
	 */
	private CheckBox selectAll;

	/**
	 * The click trigger graph. 
	 */
	private ClickTriggerGraph clickTriggerGraph;

	/**
	 * Pane for click classification- this is classification of individual clicks, not trains
	 */
	private ClickClassifyPaneFX clickClassificationPane;

	/**Click length settings**/

	/**
	 * Spinner to set the minimum click separation in bins. 
	 */
	private PamSpinner<Integer> minClickSep;

	/**
	 * Spiner for maximum click lenght in bins
	 */
	private PamSpinner<Integer> maxClickLength;

	/**
	 * Spinner to select the number of pre samples for a click detection
	 */
	private PamSpinner<Integer> preSampleSpinner;

	/**
	 * Spinner to select the number of post samples for a click detection. 
	 */
	private PamSpinner<Integer> postSampleSpinner;

	/**
	 * Pane which allows different time delay measurements to be set by different click types. 
	 */
	private ClickDelayPane clickDelayPane;

	/**
	 * Pane which contains controls to change click echo detection settings. 
	 */
	private ClickEchoPane echoDetection;

	/**
	 * The click delay tab. Needed because the click delay needs updated when the tab is selected. 
	 */
	private Tab tdoaTab;

	/**
	 * The main holder pane. 
	 */
	private PamBorderPane mainPane;

	/**
	 * The default pane height. 
	 */
	public static double PREF_PANE_HEIGHT=850;

	/**
	 * The default pane width
	 */
	public static double PREF_PANE_WIDTH=560;
	
	
	/**
	 * Validator which checks for errors
	 */
    private Validator clickValidator;



	public ClickSettingsPane(ClickControl clickControl){
		super(null);
		this.clickControl=clickControl; 
		mainPane= new PamBorderPane(); 
		
		clickValidator = new PamValidator(); 

		pamTabbedPane=new PamTabPane();
		pamTabbedPane.setAddTabButton(false);
		pamTabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		//create a combined detection and length pane
		PamVBox detectionPane=new PamVBox();
		detectionPane.setSpacing(20);
		detectionPane.getChildren().add(createClickDetectionPane());


		PamHBox holder=new PamHBox(); 
		holder.setSpacing(20);
		holder.getChildren().addAll(createClickLengthPane(), createClickTriggerPane());
		detectionPane.getChildren().add(holder);

		detectionPane.getChildren().add(createTriggerGraph()); 

		//add everything to tabs.
		pamTabbedPane.getTabs().add(new Tab("Click Detection", detectionPane));
		clickDelayPane=createDelayPane();
		pamTabbedPane.getTabs().add(tdoaTab=new Tab("TDOA and Echoes", clickDelayPane.getContentNode()));
		tdoaTab.setOnSelectionChanged((event)->{
			if (pamTabbedPane.getSelectionModel().getSelectedItem()==tdoaTab){
				//System.out.println("clickDelayPane: "+clickDelayPane);
				//need to update the tab with a copy of the current click params. 
				clickDelayPane.setParams(clickClassificationPane.getParams(clickParameters.clone())); 
			}
		});


		//pre filter pane.
		preFilter=new FilterPaneFX(Orientation.VERTICAL); 
		pamTabbedPane.getTabs().add(new Tab("Pre Filter", preFilter.getContentNode()));

		//trigger pane 
		triggerFilter=new FilterPaneFX(Orientation.VERTICAL); 
		pamTabbedPane.getTabs().add(new Tab("Trigger Filter", triggerFilter.getContentNode()));

		//		//echo detection pane. 
		//		echoDetection= new ClickEchoPane(clickControl); 
		//		pamTabbedPane.getTabs().add(new Tab("Echo Detection", echoDetection.getContentNode()));

		/***Note: FX does not implment click train detection in click detector****/

		//classifiaction pane. 
		pamTabbedPane.getTabs().add(new Tab("Classification", clickClassificationPane=new ClickClassifyPaneFX(clickControl)));

		//want a slightly bigger pane as a lot going on in this dialog. 
		//Note JavaFX 8u61 + has auto DPI scaling so this is really the size of a dialog on a standard HD monitor of 
		//reasonable size, rather than actual pixels 
		mainPane.setPrefSize(PREF_PANE_WIDTH, PREF_PANE_HEIGHT);

		//addTabListeners();
		mainPane.setCenter(new PamBorderPane(pamTabbedPane));
	}

	//	private void addTabListeners(){
	//		for (int i=0; i<pamTabbedPane.getTabs().size(); i++){
	//			final int n=i; 
	//			pamTabbedPane.getTabs().get(i).setOnSelectionChanged((value)->{
	//				System.out.println("Tab selected "+pamTabbedPane.getTabs().get(n).getText());
	//				
	//				/**
	//				 * Find default size of content pane 
	//				 */
	//				double defualtHWidth=pamTabbedPane.getTabs().get(n).getContent().....
	//				double defualtHeight=pamTabbedPane.getTabs().get(n).getContent().....
	//				/*******************************/
	//				
	//				pamTabbedPane.setPrefSize(defualtHWidth, defualtHeight);
	//				Stage stage = (Stage) pamTabbedPane.getScene().getWindow();
	//				stage.sizeToScene();
	//			});
	//		}
	//	}

	/**
	 * Pane which deals with click detection settings
	 */
	private Node createClickDetectionPane(){

		//		PamTabPane pamTabbedPane=new PamTabPane();
		//		pamTabbedPane.setAddTabButton(false);
		//		pamTabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); 
		//		pamTabbedPane.setSide(Side.BOTTOM);

		//channels, groups and trigger are all in one pane to make it easy not to make mistakes 
		sourcePane=createClickSourcePane(); //create the source pane. 


		GridPane.setColumnSpan(sourcePane.getDataBlockBox(), 2); 
		//now create trigger pane. The trigger pane is added to the source pane. 
		Label triggerLabel = new Label("Trigger Channels");
		PamGuiManagerFX.titleFont2style(triggerLabel);
		sourcePane.getSourcePane().add(triggerLabel,1,2);

		triggerChannels=new Pane();
		sourcePane.getSourcePane().add(triggerChannels,1,3);
		GridPane.setHalignment(triggerChannels, HPos.RIGHT);
		createTriggerChannels();
		//sourcePane.getSourcePane().add(createClickTriggerPane(), 2, 3);

		//		pamTabbedPane.getTabs().add(new Tab("Click Source", sourcePane));
		//		pamTabbedPane.getTabs().add(new Tab("Click Length ", createClickLengthPane()));
		//		pamTabbedPane.getTabs().add(new Tab("Click Echoes", createEchoPane()));
		//		pamTabbedPane.getTabs().add(new Tab("Click Delays", createDelayPane()));

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHgrow(Priority.ALWAYS);

		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHgrow(Priority.SOMETIMES);
		col2.setHalignment(HPos.RIGHT);
		sourcePane.getSourcePane().getColumnConstraints().addAll(col1, col2); 
		
		PamHBox.setHgrow(sourcePane.getChannelPane(), Priority.NEVER);

		//sourcePane.setMinWidth(PREF_PANE_WIDTH);
		sourcePane.getSourcePane().setPrefWidth(PREF_PANE_WIDTH);

		return sourcePane;
	}

	/**
	 * Create pane to alter click length values used in detection. 
	 * @return node with controls to change click length vlaues for detection. 
	 */
	private Node createClickLengthPane(){

		Label triggerLabel = new Label("Click Length");
		PamGuiManagerFX.titleFont2style(triggerLabel);

		PamGridPane lengthPane=new PamGridPane();
		lengthPane.setVgap(5);
		lengthPane.setHgap(5);

		lengthPane.add(new Label("Min separation"),0,0);
		minClickSep=new PamSpinner<Integer>(0, 10000000, 100, 20);
		minClickSep.setEditable(true);
		minClickSep.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minClickSep.setPrefWidth(PREF_SPINNER_WIDTH);

		lengthPane.add(minClickSep,1,0);
		lengthPane.add(new Label("samples"),2,0);

		lengthPane.add(new Label("Max length"),0,1);
		maxClickLength=new PamSpinner<Integer>(0, 10000000, 100, 20);
		maxClickLength.setEditable(true);
		maxClickLength.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxClickLength.setPrefWidth(PREF_SPINNER_WIDTH);

		lengthPane.add(maxClickLength,1,1);
		lengthPane.add(new Label("samples"),2,1);


		lengthPane.add(new Label("Pre samples"),0,2);
		preSampleSpinner=new PamSpinner<Integer>(0, 10000000, 100, 20);
		preSampleSpinner.setEditable(true);
		preSampleSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		//preSampleSpinner.setPrefWidth(PREF_SPINNER_WIDTH);

		lengthPane.add(preSampleSpinner,1,2);
		lengthPane.add(new Label("samples"),2,2);


		lengthPane.add(new Label("Post samples"),0,3);
		postSampleSpinner=new PamSpinner<Integer>(0, 10000000, 100, 20);
		postSampleSpinner.setEditable(true);
		postSampleSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		//postSampleSpinner.setPrefWidth(PREF_SPINNER_WIDTH);


		lengthPane.add(postSampleSpinner,1,3);
		lengthPane.add(new Label("samples"),2,3);

		PamVBox holder=new PamVBox();
		holder.setSpacing(5);
		holder.getChildren().addAll(triggerLabel, lengthPane);

		return holder; 
	}


	/**
	 * Create trigger channels. 
	 */
	private void createTriggerChannels(){
		triggerBoxes =new CheckBox[PamConstants.MAX_CHANNELS];
		selectAll=new CheckBox("All"); 
		selectAll.setOnAction((action)->{
			if (selectAll.isSelected()) selectAllChannels();
			else selectNoChannels();
		});

		//create a list of trigger boxesc
		for (int i=0; i<triggerBoxes.length; i++){
			triggerBoxes[i]=new CheckBox(("Channel "+i));
			final int n=i;
			triggerBoxes[i].setOnAction((action)->{
				selectionChanged(n);
	        	clickValidator.validate(); //make sure all nodes are resrt when one channel is ticked. 
			});
			clickValidator.createCheck()
	          .dependsOn(("trigger " + n), triggerBoxes[i].selectedProperty())
	          .withMethod(c -> {
	            if (!isATriggerSelected() ) {
		              c.error("At least one trigger channel needs to be selected for the module to work");
	            }
	          })
	          .decorates(triggerBoxes[n])
	          .immediate();
	        
		}

		populateTriggerPane(); 
	}

	/**
	 * Create the settings pane for the click detector. 
	 * @return pane with controls for changing click detector settings. 
	 */
	private GroupedSourcePaneFX createClickSourcePane(){

		sourcePane = new GroupedSourcePaneFX( "Raw Data Source for Clicks", RawDataUnit.class, true, true, true);

		return sourcePane;
	}

	private Pane createClickTriggerPane(){

		PamGridPane triggerPane=new PamGridPane();
		triggerPane.setVgap(5);
		triggerPane.setHgap(10);

		//trigger settings
		Label triggerLabel = new Label("Trigger");
		PamGuiManagerFX.titleFont2style(triggerLabel);
		triggerPane.add(triggerLabel,0,0);

		//settings such as threshold etc. 
		PamGridPane triggerBox=new PamGridPane();
		triggerBox.setHgap(5);
		triggerBox.setVgap(5);
		//triggerBox.setPadding(new Insets(0,0,0,30));

		triggerBox.add(new Label("Threshold"),0,0);
		threshold=new PamSpinner<Double>(1., 100., 10., 1.);
		threshold.setEditable(true);
		threshold.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		threshold.getValueFactory().valueProperty().addListener((obs, before, after)->{
			clickParameters.dbThreshold=after;
			clickTriggerGraph.updateGraphFilter();
		});
		triggerBox.add(threshold,1,0);
		triggerBox.add(new Label("dB"),2,0);
		threshold.setPrefWidth(PREF_SPINNER_WIDTH);

		triggerBox.add(new Label("Long Filter"),0,1);
		longFilter=new PamSpinner<Double>(0.0000001, 0.1, 0.000001, 0.000001);
		longFilter.setEditable(true);
		longFilter.getValueFactory().setConverter(new DecimalConverter());
		longFilter.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		longFilter.getValueFactory().valueProperty().addListener((obs, before, after)->{
			clickParameters.longFilter=after;
			clickTriggerGraph.setLongFilter(clickParameters.longFilter);
			clickTriggerGraph.updateGraphFilter();
		});
		triggerBox.add(longFilter,1,1);
		longFilter.setPrefWidth(PREF_SPINNER_WIDTH);

		triggerBox.add(new Label("Long Filter 2"),0,2);
		longFilter2=new PamSpinner<Double>(0.0000001, 0.1, 0.000001, 0.000001);
		longFilter2.setEditable(true);
		longFilter2.getValueFactory().setConverter(new DecimalConverter());
		longFilter2.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		longFilter2.getValueFactory().valueProperty().addListener((obs, before, after)->{
			clickParameters.longFilter2=after;
		});
		longFilter2.setPrefWidth(PREF_SPINNER_WIDTH);
		triggerBox.add(longFilter2,1,2);

		triggerBox.add(new Label("Short Filter"),0,3);
		shortFilter=new PamSpinner<Double>(0.0000001, 1, 0.000000, 0.01);
		shortFilter.setEditable(true);
		shortFilter.getValueFactory().setConverter(new DecimalConverter());
		shortFilter.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		shortFilter.getValueFactory().valueProperty().addListener((obs, before, after)->{
			clickParameters.shortFilter=after;
			clickTriggerGraph.setShortFilter(clickParameters.shortFilter);
			clickTriggerGraph.updateGraphFilter();
		});
		shortFilter.setPrefWidth(PREF_SPINNER_WIDTH);
		triggerBox.add(shortFilter,1,3);

		//forces the grid pane to be larger - grid panes can be a little funny. 
		shortFilter.setMinWidth(PREF_SPINNER_WIDTH);

		triggerPane.add(triggerBox,0,1);

		//triggerPane.setGridLinesVisible(true);

		return triggerPane; 
	}


	private Pane createTriggerGraph() {
		//trigger graph
		Label graphLabel = new Label("Filter Graph");
		PamGuiManagerFX.titleFont2style(graphLabel);

		clickTriggerGraph=new ClickTriggerGraph();
		PamGridPane.setHgrow(clickTriggerGraph, Priority.ALWAYS);
		PamGridPane.setVgrow(clickTriggerGraph, Priority.ALWAYS);

		PamVBox triggerGraph = new PamVBox(); 
		triggerGraph.setSpacing(5);
		triggerGraph.getChildren().addAll(graphLabel, clickTriggerGraph); 

		return triggerGraph; 

	}

	/**
	 * Unselect all trigger channels
	 */
	private void selectNoChannels() {
		for (int i = 0; i < triggerBoxes.length; i++) {
			if (triggerBoxes[i] != null) {
				triggerBoxes[i].setSelected(false);
			}
		}
	}

	/**
	 * Select all trigger channels
	 */
	private void selectAllChannels() {
		for (int i = 0; i < triggerBoxes.length; i++) {
			if (triggerBoxes[i] != null && triggerBoxes[i].isVisible()) {
				triggerBoxes[i].setSelected(true);
			}
		}
	}

	/**
	 * Set status of select all trigger channel box. 
	 */
	private void setAllChanStatus(){
		int channels=getChannels(); 
		int n=0; 
		//now add correct trigger children again
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, triggerBoxes.length); i++) {
			if ((channels & 1<<i) != 0  && triggerBoxes[i].isSelected()){
				n++;
			}; 
		} 
		selectAll.setIndeterminate(false);

		if (PamUtils.getNumChannels(channels)==n){
			selectAll.setSelected(true);
		}
		else if (n==0){
			selectAll.setSelected(false);
		}
		else selectAll.setIndeterminate(true);
	}


	private void selectionChanged(int n) {
		setAllChanStatus();
	}

	/**
	 * Get the current channels bitmap. 
	 * @return integer channel bitmap
	 */
	private int getChannels(){
		// called when the selection changes - set visibility of the channel list
		int channels = 0;
		PamDataBlock<?> sb = sourcePane.getSource();
		//Character ch;
		if (sb != null) {
			channels = sb.getChannelMap();
		}
		return channels;
	}
	
	
	/**
	 * Get the number of selected trigger channels.
	 * @return the number of selected trigger channels. 
	 */
	private int getNSelectedTrigger() {
		int channels=getChannels(); 
		int n=0; 
		//now add correct trigger children again
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, triggerBoxes.length); i++) {
			if ((channels & 1<<i) != 0  && triggerBoxes[i].isSelected()){
				n++;
			}; 
		} 
		return n; 
	}
	
	/**
	 * Check whether at least one trigger channel is selected. 
	 * @return true of a trigger channel is selected. 
	 */
	private boolean isATriggerSelected() {
		return  getNSelectedTrigger()>0; 
	}


	/**
	 * Create trigger channels
	 */
	private void populateTriggerPane(){
		int channels=getChannels(); 

		//remove all trigger children from the pane first
		triggerChannels.getChildren().remove(selectAll);
		for (int i = 0; i <triggerBoxes.length; i++) {
			triggerChannels.getChildren().remove(triggerBoxes[i]);
		}

		//now add correct trigger children again
		triggerChannels.getChildren().add(selectAll);		
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, triggerBoxes.length); i++) {
			if ((channels & 1<<i) != 0){
				
				//triggerBoxes[i] = new CheckBox("Channel " + i);
				triggerChannels.getChildren().add(triggerBoxes[i]);
				triggerBoxes[i].layoutYProperty().unbind();
				triggerBoxes[i].layoutYProperty().bind(sourcePane.getChannelBoxes()[i].layoutYProperty());
			}; 
		} 
	}

	/**
	 * Create pane which allows different time delay measurments to be set by different click types. 
	 * This pane changes when click classification settings are changed. 
	 * @return the click delay pane. 
	 */
	private ClickDelayPane createDelayPane(){
		return new ClickDelayPane(this.clickControl);
	}



	private Pane createClickTrainPane(){
		PamVBox vBox=new PamVBox();
		return vBox;
	}


	@Override
	public ClickParameters getParams(ClickParameters params) {

		//		//set trigger params. 
		//		clickParameters.rawDataSource = sourcePane.getSourceName();
		//		clickParameters.channelGroups = sourcePane.getChannelGroups();
		//		
		//		//set filter params
		//		clickParameters.preFilter=preFilter.getParams();
		//		clickParameters.triggerFilter=triggerFilter.getParams();
		//
		//		//add classification params
		//		clickParameters= this.clickClassificationPane.getParams(this.clickParameters);
		//		
		//		//add click delay params. 
		//		clickParameters=clickDelayPane.getParams(this.clickParameters); 

		try {
			PamRawDataBlock rawDataBlock = (PamRawDataBlock) sourcePane.getSource();
			if (rawDataBlock == null){
				PamDialogFX.showWarning("There is no datablock set. The click detector must have a datablock set."); 
				return null;
			}

			//			clickParameters.rawDataSource = rawDataBlock.toString();
			//			clickParameters.channelBitmap = sourcePane.getChannelList();
			//			clickParameters.channelGroups = sourcePane.getChannelGroups();
			//			clickParameters.groupingType = sourcePane.getGrouping();

			//sets the params for source pane. 
			sourcePane.getParams(clickParameters.getGroupedSourceParameters());

			//		if (sourcePanel.getParams() == false) return false;
			try {
				clickParameters.dbThreshold = Double.valueOf(threshold.getValue());
				clickParameters.longFilter = Double.valueOf(longFilter.getValue());
				clickParameters.longFilter2 = Double.valueOf(longFilter2.getValue());
				clickParameters.shortFilter = Double.valueOf(shortFilter.getValue());
			} catch (Exception ex) {
				PamDialogFX.showWarning("There is a problem in the click trigger settings"); 
				return null;
			}
			try{
				clickParameters.preSample = Integer.valueOf(preSampleSpinner.getValue());
				clickParameters.postSample = Integer.valueOf(postSampleSpinner.getValue());
				clickParameters.minSep = Integer.valueOf(minClickSep.getValue());
				clickParameters.maxLength = Integer.valueOf(maxClickLength.getValue());
			} catch (Exception ex) {
				PamDialogFX.showWarning("There is a problem in the click length settings"); 
				return null;
			}

			clickParameters.triggerBitmap = 0;
			boolean boxSelected=false; 
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
				if (triggerBoxes[i].isSelected()) {
					clickParameters.triggerBitmap |= 1<<i;
					boxSelected=true; 
				}
			}
			if (!boxSelected){
				PamDialogFX.showWarning("There are no channels which have the trigger set. Without at least one channel"
						+ " triggerring the click detector will detect no clicks!"); 
			}
			//		if (delayOptionsPanel.getParams(clickParameters.delayMeasurementParams) == false) {
			//			return false;
			//		}
			if ((clickParameters.preFilter=this.preFilter.getParams(clickParameters.preFilter)) == null) {
				System.err.println("ClickSettingsPane: Null pre filter params");
				//wrning shown in pre filter
				return null;
			}

			if ((clickParameters.triggerFilter=this.triggerFilter.getParams(clickParameters.triggerFilter)) == null) {
				System.err.println("ClickSettingsPane: Null trigger filter params");
				//wrning shown in trigger filter
				return null;
			}

			if ((clickParameters=clickDelayPane.getParams(clickParameters)) == null) {
				System.err.println("ClickSettingsPane: Null delay params");
				return null;
			}

			if (this.echoDetection != null) {
				if ((clickParameters=echoDetection.getParams(clickParameters)) == null) {
					System.err.println("ClickSettingsPane: Null classifier params");
					return null;
				}
			}
			
			clickParameters = clickClassificationPane.getParams(clickParameters); 

		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}

		return clickParameters;
	}

	@Override
	public void setParams(ClickParameters input) {

		this.clickParameters=input.clone();

		// source pane
		PamRawDataBlock rawDataBlock = PamController.getInstance().
				getRawDataBlock(clickParameters.getGroupedSourceParameters().getDataSource());
		sourcePane.clearExcludeList();
		excludeDataBlocks();
		sourcePane.excludeDataBlock(clickControl.getClickDetector().getTriggerFunctionDataBlock(), true);
		if (rawDataBlock != null) {
			sourcePane.setSource(rawDataBlock);
		}
		else {
			sourcePane.setSourceIndex(0);
		}

		sourcePane.setParams(clickParameters.getGroupedSourceParameters());

		//		sourcePane.setGrouping(clickParameters.groupingType);
		//		sourcePane.setChannelGroups(clickParameters.channelGroups);
		//		sourcePane.setChannelList(clickParameters.channelBitmap);

		//click length pane
		minClickSep.getValueFactory().setValue(clickParameters.minSep);
		maxClickLength.getValueFactory().setValue(clickParameters.maxLength);
		preSampleSpinner.getValueFactory().setValue(clickParameters.preSample);
		postSampleSpinner.getValueFactory().setValue(clickParameters.postSample);

		//filter panes
		preFilter.setParams(clickParameters.preFilter);
		triggerFilter.setParams(clickParameters.triggerFilter);

		//trigger pane
		populateTriggerPane();
		threshold.getValueFactory().setValue(clickParameters.dbThreshold);
		longFilter.getValueFactory().setValue(clickParameters.longFilter);
		longFilter2.getValueFactory().setValue(clickParameters.longFilter2);
		shortFilter.getValueFactory().setValue(clickParameters.shortFilter);
		updateTriggerGraph();

		//populate trigger box
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, triggerBoxes.length); i++) {
			if ((clickParameters.getGroupedSourceParameters().getChanOrSeqBitmap() & 1<<i) != 0){
				triggerBoxes[i].setSelected((1<<i & clickParameters.triggerBitmap) != 0);
			}
		}
		//need to set all chan status
		setAllChanStatus();


		//echo detector
		//TODO
		//echoIntervalSpinner.getValueFactory().setValue(clickParameters.;

		//click train detector 

		//click classification
		this.clickClassificationPane.setParams(clickParameters);


	}

	/**
	 * Update the graph.
	 */
	private void updateTriggerGraph(){
		clickTriggerGraph.setShortFilter(clickParameters.shortFilter);
		clickTriggerGraph.setLongFilter(clickParameters.longFilter);
	}

	/**
	 * A converter for the Spinner value factory to allow for a spinner with a hell of a lot decimal places. 
	 * @author Jamie Macaulay
	 *
	 */
	private class DecimalConverter extends StringConverter<Double>{

		private  DecimalFormat df = new DecimalFormat("#.############");

		public DecimalConverter(){

		}

		@SuppressWarnings("unused")
		public DecimalConverter(DecimalFormat df){
			this.df=df; 		    	 
		}

		@Override 
		public String toString(Double value) {
			// If the specified value is null, return a zero-length String
			if (value == null) {
				return "";
			}

			return df.format(value);
		}

		@Override public Double fromString(String value) {
			try {
				// If the specified value is null or zero-length, return null
				if (value == null) {
					return null;
				}

				value = value.trim();

				if (value.length() < 1) {
					return null;
				}

				// Perform the requested parsing
				return df.parse(value).doubleValue();
			} catch (ParseException ex) {
				throw new RuntimeException(ex);
			}
		}		

	}


	private void excludeDataBlocks() {
		PamRawDataBlock[] excludeBlocks = clickControl.getClickDetector().getFilteredDataBlocks();
		if (excludeBlocks == null) {
			return;
		}
		for (int i = 0; i < excludeBlocks.length; i++) {
			sourcePane.excludeDataBlock(excludeBlocks[i], true);
		}
	}

	@Override
	public String getName() {
		return "Click Detector Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	/**
	 * Get the pane which  holds the different click classifiers 
	 * @return click classifier pane. 
	 */
	public ClickClassifyPaneFX getClickClassificationPane() {
		return clickClassificationPane;
	}


}
