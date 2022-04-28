package Acquisition.layoutFX;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamComboBox;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTextField;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.flipPane.FlipPane;
import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.ChannelListPanel;
import Acquisition.DaqSystem;
import Acquisition.FileInputSystem;
import PamController.SettingsPane;
import PamguardMVC.PamConstants;
import dataMap.filemaps.OfflineFileParameters;

/**
 * Settings pane for the sound acquisition parameters. 
 * @author Jamie Macaulay and Doug Gillespie. 
 *
 */
public class AcquisitionPaneFX extends SettingsPane<AcquisitionParameters>{
	
	private final static int TEXT_FIELD_WIDTH = 60; 

	/**
	 * Reference to acquisition control. 
	 */
	private AcquisitionControl acquisitionControl;

	/**
	 * The system pane- holds DAQ specific node. 
	 * 
	 */
	private PamBorderPane systemPane;

	/**
	 * Shows current sample rate.
	 */
	private PamTextField sampleRate;

	/**
	 * Shows number of channels. 
	 */
	private PamComboBox<DaqSystem> deviceTypes;

	/**
	 * Shows number of channels. 
	 */
	private PamComboBox<Integer>  nChannels;

	/**
	 * Shows DAQ system peak to peak. 	
	 */
	private TextField vPeak2Peak;

	/**
	 * The total gain in the system 
	 */
	private TextField preampGain; 

	/**
	 * Pane which allows users to map hardware channels to software channels	 
	 */
	private ChannelListPanel channelMappingPane;

	/**
	 * Holds the channelMappingPane
	 */
	private PamBorderPane channelMappingHolder;

	/**
	 * Reference to the acquisition parameters. 
	 */
	private AcquisitionParameters acquisitionParameters;

	/**
	 * A standard channel list pane. 
	 */
	private ChannelListPanel standardChListPane=new StandardChannelListPane();

	/**
	 * The current DAQ system. 
	 */
	private DaqSystem currentDaqSystem;


	/**
	 * Settings pane for offline 
	 */
	private OfflineDAQPane offlineDAQPaneFX;

	/**
	 * The main holder pane. 
	 */
	private PamBorderPane mainPane;

	/**
	 * 
	 */
	private FlipPane flipPane;
	
	
	/**
	 * Pane which can be used for advanced settings. 
	 */
	private PamBorderPane advancedSettingPane;

	/**
	 * Title label for the advanced pane. 
	 */
	private Label advLabel; 

	/**
	 * Default spacing for VBox 
	 */
	private static double defaultVSpacing=5; 

	/**
	 * Default spacing for VBox 
	 */
	private static double defaultHSpacing=5; 

	public AcquisitionPaneFX(AcquisitionControl aquisitionControl){
		super(null);
		mainPane = new PamBorderPane();
		mainPane.setPrefWidth(400);

		this.acquisitionControl=aquisitionControl;
		this.acquisitionParameters=acquisitionControl.getAcquisitionParameters();
		
		//create the flip pane. 
		flipPane=new FlipPane(); 
		flipPane.setFlipDirection(Orientation.HORIZONTAL);
		flipPane.setFlipTime(250); //default is 700ms- way too high
		//flipPane.prefWidthProperty().bind(mainPane.widthProperty());

		if (aquisitionControl.isViewer()){
			this.mainPane.setCenter(createViewerModePane()); 
		}
		else {
			this.mainPane.setCenter(createRealTimePane());
		}
		flipPane.getFront().getChildren().add(mainPane);
		
		//create the advanced flip pane.
		advancedSettingPane = createAdvSettingsPane(); 
		flipPane.getBack().getChildren().add(advancedSettingPane);
		//System.out.println("MAKE PANE: "  +  acquisitionParameters.getDaqSystemType());

	} 
	
	/**
	 * Create the advanced settings pane which can be accessed by DAQ panes if needed. 
	 */
	private PamBorderPane createAdvSettingsPane() {
		
		PamButton back = new PamButton(); 
		back.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", Color.WHITE, PamGuiManagerFX.iconSize));
		
		back.setOnAction((action)->{
			flipPane.flipToFront(); 
		});
		
		PamBorderPane advPane = new PamBorderPane(); 
		advPane.setPadding(new Insets(5,5,5,5));
		
		PamHBox buttonHolder = new PamHBox(); 
		
		buttonHolder.setBackground(null);
		//buttonHolder.setStyle("-fx-background-color: red;");
		buttonHolder.setAlignment(Pos.CENTER_LEFT);
		buttonHolder.getChildren().addAll(back, advLabel = new Label("Adv. Settings")); 
		advLabel.setAlignment(Pos.CENTER);
		advLabel.setMaxWidth(Double.MAX_VALUE); //need to make sure label is in center. 
		PamGuiManagerFX.titleFont2style(advLabel);
		
		advLabel.setAlignment(Pos.CENTER);
		HBox.setHgrow(advLabel, Priority.ALWAYS);
		
		advPane.setTop(buttonHolder);
		
		return advPane; 
		
	}

	/**
	 * Create the Sound Aquisition pane for real time monitoring. 
	 * @return the SoundAquisition pane with real time controls. 
	 */
	private Pane createRealTimePane() {

		//custom pane for each aquisition system. 
		systemPane=new PamBorderPane(); 

		Label sourceLabel=new Label("Sound Source");
		PamGuiManagerFX.titleFont2style(sourceLabel);

		deviceTypes= new PamComboBox<DaqSystem>();
		deviceTypes.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(deviceTypes, Priority.ALWAYS);

		// fill in the different device types.				
		//the file input system is legacy but want to keep compatible with PG swing so remove form the observable list. 
		ObservableList<DaqSystem> deviceList = FXCollections.observableArrayList(acquisitionControl.getSystemList());
		for (DaqSystem system: deviceList) {
			if (system.getSystemType() == null || system.getSystemType().equals(FileInputSystem.sysType)) {
				deviceList.remove(system);
				break; 
			}
		}
		
		//populate the combo box
		deviceTypes.getItems().removeAll(deviceTypes.getItems());
		deviceTypes.getItems().addAll(deviceList);

		//convert DAQ system to string
		deviceTypes.setConverter(new StringConverter<DaqSystem>() {
			@Override
			public String toString(DaqSystem user) {
				if (user== null){
					return null;
				} else {
					return user.getSystemType();
				}
			}

			@Override
			public DaqSystem fromString(String id) {
				return null;
			}
		});

		//add listener
		deviceTypes.setOnAction((value)->{
			systemPane.setCenter(null);
			channelMappingHolder.setCenter(null);

			//set parameters
			acquisitionParameters.setDaqSystemType(deviceTypes.getSelectionModel().getSelectedItem().getSystemType());	
			setParams(acquisitionParameters); 

			//TODO- this is a bit CUMBERSOME and maybe fixed in new version of JavaFX
			//need to get stage and resize because new controls may have been added. 
			if (mainPane!=null && mainPane.getScene()!=null) {
				Stage stage = (Stage) mainPane.getScene().getWindow();
				stage.sizeToScene();
			}

		});

		PamVBox mainBox=new PamVBox(); 
		mainBox.setSpacing(5);
		mainBox.getChildren().addAll(sourceLabel, deviceTypes, systemPane,  createSamplingPane(), 
				channelMappingHolder=new PamBorderPane(), createCalibrationPane());

		return mainBox; 
	}

	/**
	 * Create the Sound Aquisition pane for viewer mode. this only shows the sample rate and has an import dialog. 
	 * @return the viewer mode settings pane. 
	 */
	private Pane createViewerModePane() {

		PamVBox mainBox=new PamVBox(); 

		//custom pane for each aquisition system. 
		systemPane=new PamBorderPane(); 

		offlineDAQPaneFX= new OfflineDAQPane(acquisitionControl, this);

		//the main pane is for reference only in viewer mode. 
		Pane samplingPane=createSamplingPane();
		samplingPane.setDisable(true);

		mainBox.getChildren().add(offlineDAQPaneFX.getContentNode());
		mainBox.getChildren().addAll(samplingPane,channelMappingHolder=new PamBorderPane(), createCalibrationPane());

		return mainBox; 
	}

	/**
	 * Called by the specific DaqSystem to set sample rate when it is set by
	 * the DaqSystem (for instance FileInputSystem will set sample rate to the 
	 * sample rate of data in the current file.  
	 * @param sampleRate Current sample rate
	 */
	public void setSampleRate(float sampleRate) {
		this.sampleRate.setText(String.format("%.0f", sampleRate));
	}

	/**
	 * Called by the specific DaqSystem to set the number of channels when it is set by
	 * the DaqSystem (for instance FileInputSystem will set it to the 
	 * number of channels in the current file.  
	 * @param nChannels Number of channels
	 */
	public void setChannels(int nChannels) {
		this.nChannels.getSelectionModel().select(nChannels);
	}

	/**
	 * Called by the specific DaqSystem to set the peak to peak voltage range.
	 * This is used for calculating absolute SPL's in various detectors
	 * the DaqSystem   
	 * @param vPeak2Peak Peak to Peak input voltage
	 */
	public void setVPeak2Peak(double vPeak2Peak) {
		this.vPeak2Peak.setText(String.format("%4.2f", vPeak2Peak));
	}

	/**
	 * The DAQ specific pane
	 * @param currentSystem - the current DAQ system to show pane for. 
	 */
	private void showDAQSpecificPane(DaqSystem currentSystem) {
		if (currentSystem==null) return; 

		if (currentSystem.getDAQSpecificPane(this)!=null) {
			Node daqNode = currentSystem.getDAQSpecificPane(this).getContentNode();

			if (daqNode!=null) {
				systemPane.setCenter(daqNode);
			}
		}
		else systemPane.setCenter(new Label("No settings pane available"));
	}

	/**
	 * Only need to show the channel panel for certain device types,
	 */
	private void showHideChannelPane(DaqSystem currentSystem) {
		if (currentSystem==null) return; 

		//check for a special channel pane. 
		channelMappingPane=currentSystem.getDaqSpecificChannelListNode(this);
		if (channelMappingPane==null && currentSystem.supportsChannelLists()) {
			//need to have a standard channel pane 
			channelMappingPane=standardChListPane;
		}

		if (channelMappingPane!=null) channelMappingHolder.setCenter(channelMappingPane.getNode());
	}

	/**
	 * Create pane which shows current sample rate,  the total number of channels and a software
	 * to hardware channel mapping pane. 
	 * @return sample rate and current channels 
	 */
	public Pane createSamplingPane(){

		PamBorderPane borderPane=new PamBorderPane(); 

		PamGridPane gridpane = new PamGridPane();
		gridpane.setHgap(defaultHSpacing);
		gridpane.setVgap(defaultVSpacing);
		gridpane.setPrefWidth(300);

		//create sample rate text field
		sampleRate=new PamTextField(); 
		sampleRate.setPrefWidth(TEXT_FIELD_WIDTH);
		gridpane.add(new Label("Sample Rate"),0,0);
		gridpane.add(sampleRate, 1,0); 
		//sampleRate.setPrefColumnCount(7);
		gridpane.add(new Label("Hz"), 2,0); 

		//create channel pane
		nChannels=new PamComboBox<Integer>();
		gridpane.add(new Label("Number of Channels"), 0,1); 
		gridpane.add(nChannels, 1,1); 

		//populate combo box.
		ArrayList<Integer> channelsList=new ArrayList<Integer>(); 
		for (int i=0; i<PamConstants.MAX_CHANNELS; i++){
			channelsList.add(i);
		}
		ObservableList<Integer> channelsObsList= FXCollections.observableArrayList(channelsList);
		nChannels.setItems(channelsObsList);

		//sampling
		Label samplingLabel=new Label("Sampling");
		PamGuiManagerFX.titleFont2style(samplingLabel);
		borderPane.setTop(samplingLabel);
		borderPane.setCenter(gridpane);

		return borderPane; 
	}

	/**
	 * Create pane which allows users to set calibration values. 
	 * @return calibration pane. 
	 */
	private Pane createCalibrationPane(){

		PamGridPane gridpane = new PamGridPane();
		gridpane.setHgap(defaultHSpacing);
		gridpane.setVgap(defaultVSpacing);
		gridpane.setPrefWidth(150);

		Label calibrationLabel=new Label("Calibration");
		PamGuiManagerFX.titleFont2style(calibrationLabel);
		gridpane.add(calibrationLabel, 0,0); 

		gridpane.add(new Label("Peak-Peak voltage"), 0,1); 
		gridpane.add(vPeak2Peak=new PamTextField(), 1,1); 
		vPeak2Peak.setPrefWidth(TEXT_FIELD_WIDTH);
		//vPeak2Peak.setPrefColumnCount(2);
		gridpane.add(new Label("V"), 2,1); 

		gridpane.add(new Label("System Gain"), 0,2); 
		gridpane.add(preampGain=new PamTextField(), 1,2); 
		preampGain.setPrefWidth(TEXT_FIELD_WIDTH);
		//preampGain.setPrefColumnCount(2);
		gridpane.add(new Label("dB"), 2,2); 

		return gridpane;
	}



	public AcquisitionParameters getParams() {

		//used in both in view and real time. 
		acquisitionParameters.sampleRate = Float.valueOf(sampleRate.getText());
		//
		acquisitionParameters.nChannels = nChannels.getSelectionModel().getSelectedItem();
		acquisitionParameters.voltsPeak2Peak = Double.valueOf(vPeak2Peak.getText());
		acquisitionParameters.preamplifier.setGain(Double.valueOf(preampGain.getText()));

		//from swing params
		//			if(!currentDaqSystem.areSampleSettingsOk(acquisitionParameters.nChannels, acquisitionParameters.sampleRate)){			
		//				currentDaqSystem.showSampleSettingsDialog(this);
		//				return false;
		//			}		

		if (!this.acquisitionControl.isViewer()) {
			acquisitionParameters.setDaqSystemType(deviceTypes.getSelectionModel().getSelectedItem().getSystemType());
		}


		if (!this.acquisitionControl.isViewer() && getCurrentDaqSystem().supportsChannelLists() && standardChListPane != null) {
			if (standardChListPane.isDataOk() == false) {
				return null;
			}
			int[] chL = standardChListPane.getChannelList();
			for (int i = 0; i < chL.length; i++) {
				acquisitionParameters.setChannelList(i, chL[i]);
			}
		}
		else {
			acquisitionParameters.setDefaultChannelList();
		}


		if (!this.acquisitionControl.isViewer() && currentDaqSystem != null && currentDaqSystem.getDAQSpecificPane(this)!=null){
			//get params from daq specific system
			if  (!currentDaqSystem.getDAQSpecificPane(this).getParams()) return null;

		}

		//last because this actually sets something.
		if (offlineDAQPaneFX != null) {
			OfflineFileParameters ofp = offlineDAQPaneFX.getParams();
			//System.out.println("ofp:  Offileine Params AqusitionPanFX " + ofp.enable + " " + ofp.folderName);
			//in FX we don;t have option for using files or nut. If no folder selected then don;t use files. 
			if (ofp.folderName==null || ofp.folderName.equals("")){
				ofp.enable=false;
			} 
			else ofp.enable=true; 

			if (ofp == null) {
				return null;
			}
			acquisitionControl.getOfflineFileServer().setOfflineFileParameters(ofp);
		}
		
		//System.out.println("Get Params: Open Aquisition dialog: "  +  acquisitionParameters.getDaqSystemType());

		return acquisitionParameters;
	}

	@Override
	public void setParams(AcquisitionParameters input) {
		
		
		acquisitionParameters=input.clone(); 
		
		int ind = 0;
		for (int i = 0; i < acquisitionControl.getSystemList().size(); i++) {
			//deviceTypes.getItems().add(acquisitionControl.systemList.get(i));
//						System.out.println(" Is this: "+acquisitionControl.getSystemList().get(i).getSystemType()+
//								" the samed as : "+input.getDaqSystemType()); 
			
			//important to put .equals as this compares string values rather than whether it's the same object. 
			if (acquisitionControl.getSystemList().get(i).getSystemType().equals(acquisitionParameters.getDaqSystemType())) {
				ind = i;
			}
		}
		
		//System.out.println("Set Params: Open Aquisition dialog: " + ind + "  " + acquisitionParameters.getDaqSystemType());


		if (!acquisitionControl.isViewer()) {
			if (deviceTypes.getSelectionModel().getSelectedIndex()!=ind){
				deviceTypes.getSelectionModel().select(acquisitionControl.getSystemList().get(ind));
			}
			//set current DAQ system. 
			currentDaqSystem = deviceTypes.getSelectionModel().getSelectedItem();
		}
		else currentDaqSystem=acquisitionControl.getSystemList().get(ind); 

		//set specific panes for the current system 
		showDAQSpecificPane(currentDaqSystem);
		showHideChannelPane(currentDaqSystem);

		//newDeviceType();

		setSampleRate(acquisitionParameters.sampleRate);

		setChannels(acquisitionParameters.nChannels);

		setVPeak2Peak(acquisitionParameters.voltsPeak2Peak);

		preampGain.setText(String.format("%.1f", acquisitionParameters.preamplifier.getGain()));


		if (currentDaqSystem != null){
			if (currentDaqSystem.getDAQSpecificPane(this)!=null) {
				currentDaqSystem.getDAQSpecificPane(this).setParams();
			}
			//enable and disable stuff. 
			sampleRate.setDisable(currentDaqSystem.getMaxSampleRate() == DaqSystem.PARAMETER_FIXED);
			nChannels.setDisable(currentDaqSystem.getMaxChannels() == DaqSystem.PARAMETER_FIXED);
			vPeak2Peak.setDisable(currentDaqSystem.getPeak2PeakVoltage(0) != DaqSystem.PARAMETER_UNKNOWN);
		}


		if (channelMappingPane != null) {
			channelMappingPane.setParams(acquisitionParameters.getHardwareChannelList());
		}

		if (this.offlineDAQPaneFX != null) {
			offlineDAQPaneFX.setParams( acquisitionControl.getOfflineFileServer().getOfflineFileParameters());
		}

	}

	@Override
	public String getName() {
		return "Sound Aquisition Settings";
	}

	@Override
	public Node getContentNode() {
		return flipPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
	}

	/**
	 * Get the current DAQ system
	 * @return the current DAQ system.
	 */
	public DaqSystem getCurrentDaqSystem() {
		return currentDaqSystem;
	}

	@Override
	public AcquisitionParameters getParams(AcquisitionParameters currParams) {
		if (acquisitionParameters==null) acquisitionParameters=currParams;
		return getParams();
	}

	/**
	 * Get the flip pane. 
	 * @return the flip pane. 
	 */
	public FlipPane getFlipPane() {
		return this.flipPane;
	}

	public PamBorderPane getAdvancedPane() {
		return this.advancedSettingPane;
	}

	public Label getAdvancedLabel() {
		return this.advLabel;
	}

}
