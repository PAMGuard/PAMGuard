package clickTrainDetector.layout;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.controlsfx.control.PopOver;

import PamController.PamController;
import PamController.PamGUIManager;
import PamController.SettingsPane;
import PamView.GroupedDataSource;
import PamView.dialog.GroupedSourcePanel;
import PamguardMVC.PamDataBlock;
import clickDetector.ClickDetection;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainParams;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import clickTrainDetector.layout.classification.CTClassifiersPane;
import cpod.CPODClick;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;


/**
 * Main settings pane for the click train detector. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClickTrainAlgorithmPaneFX extends SettingsPane<ClickTrainParams> {
	
	//there is something a little weird about template spectrum classifier which in Swing means
	//it needs to be 850 in height or the template covers some of the controls and they
	//cannot be clicked on??
	private static final double prefHeight = 750; 
	
	private static final double prefWidth = 450; 

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane;

	/**
	 * The source pane.
	 */
	private SourcePaneFX sourcePane;

	/**
	 * Holds the group. 
	 */
	private PamVBox groupHolder;

	/**
	 * Check boxes for selecting which groups the click train detector should run on. 
	 */
	private ArrayList<CheckBox> groupCheckBoxes;

	/**
	 * Combo box for the click train detector algorithms
	 */
	private ComboBox<String> clickTrainAlgorithmBox;

	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	/**
	 * Holder for the click train algorithm specific settings.
	 */
	private PamBorderPane ctSettingsHolder;

	/**
	 * Holds the classifier pane. 
	 */
	private CTClassifiersPane ctClassifierHolder;

	/**
	 * The data selector holder
	 */
	private PamBorderPane dataSelectorHolder;

	/**
	 * The data selector check box
	 */
	private PamToggleSwitch dataSelectorCheckBox;

	/**
	 * The data selector pane
	 */
	private Pane dataSelectorPane;

	/**
	 * Pop over for data selector. 
	 */
	private PopOver popOver;

	/**
	 * Button that opens data selector settings. 
	 */
	private PamButton dataSelectorButton;

	/**
	 * The tab pane holding detection and classification tabs
	 */
	private TabPane tabPane;

	private PreClassifierPane preClassifierPane; 

	public ClickTrainAlgorithmPaneFX(ClickTrainControl clickTrainControl) {
		super(null);
		this.clickTrainControl=clickTrainControl; 
		mainPane= new PamBorderPane(); 
		mainPane.setCenter(createPane());
	}

	/**
	 * Create the pane for the click detection settings. 
	 */
	private Pane createPane() {

		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);
		
		holder.setPrefWidth(prefWidth);
		holder.setPrefHeight(prefHeight);

		sourcePane = new SourcePaneFX("Click Data Source", ClickDetection.class, false, true); 
//		sourcePane = new SourcePaneFX("Click Data Source", RawDataHolder.class, false, true); //should be a raw data source but not fully tested so sticking to clicks for now. 
		sourcePane.addSourceType(CPODClick.class, false);
//		sourcePane.addSourceType(RawDataHolder.class, false); //any raw data unit can be used for click train detection. 

		
//		sourcePane.setTitleFont(PamGuiManagerFX.titleFontSize2); 
		PamGuiManagerFX.titleFont2style(sourcePane.getTitleLabel());
		
		//the source pane
		sourcePane.getDataBlockBox().setOnAction((action)->{
			populateGroupPane(); 
			setDataSelector();
			//notify all the other panes the sample rate has changed. 
			 getSelectedCTAlgorithm().getClickTrainGraphics().notifyUpdate(ClickTrainControl.NEW_PARENT_DATABLOCK,
					 sourcePane.getDataBlockBox().getSelectionModel().getSelectedItem());
		});

		groupCheckBoxes= new ArrayList<CheckBox>();
		groupHolder= new PamVBox(); 
		groupHolder.setSpacing(5);

		/**The click train algorithm**/

		//pane to hold specific click train detector settings. 
		ctSettingsHolder= new PamBorderPane(); 

		//combo box to select click train algorithm
		clickTrainAlgorithmBox = new ComboBox<String>(); 
		clickTrainAlgorithmBox.setMaxWidth(Double.MAX_VALUE);
		for (int i=0; i<this.clickTrainControl.getClickTrainAlgorithms().size(); i++) {
			clickTrainAlgorithmBox.getItems().add(clickTrainControl.getClickTrainAlgorithms().get(i).getName());
		}
		
		clickTrainAlgorithmBox.setOnAction((action)->{
			//if the click algorithm changes then need to add specific settings pane. 
			ClickTrainAlgorithm clickTrainAlgorithm=getSelectedCTAlgorithm() ;
			
			if (clickTrainAlgorithm.getClickTrainGraphics()!=null) {
				ctSettingsHolder.setCenter(clickTrainAlgorithm.getClickTrainGraphics().getCTSettingsPane());
			}
			else {
				ctSettingsHolder.setCenter(new Label("This algorithm has no settings"));
			}
		});
		
		//the click train selector pane. 
		Label label = new Label("Click Train Detector Algorithm"); 
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(label);
		
		
		dataSelectorHolder = new PamBorderPane(); 

		
		PamVBox ctDetectorHolder = new PamVBox(); 
		ctDetectorHolder.setSpacing(5); 
		ctDetectorHolder.getChildren().addAll(sourcePane, groupHolder, dataSelectorHolder, label, clickTrainAlgorithmBox, ctSettingsHolder);
		ctDetectorHolder.setPadding(new Insets(5,5,5,5));

		//the data selector pane. 
		dataSelectorPane = createDataSelectorPane();
		
		//The pre classifier pane. 
		preClassifierPane = new PreClassifierPane(clickTrainControl); 

		/***The species classifiers***/
		ctClassifierHolder= new CTClassifiersPane(clickTrainControl); 
		ctClassifierHolder.setPadding(new Insets(5,0,0,0));
		
		
	
		
		//the tab pane to hold classifier and the detector pane. 
		tabPane = new TabPane(); 
		Tab tab1 = new Tab("Detector", ctDetectorHolder); 
		tab1.setClosable(false);
		Tab tab2 = new Tab("Species Classifiers", ctClassifierHolder); 
		tab2.setClosable(false);
		Tab tab3 = new Tab("Pre Classifier", preClassifierPane); 
		tab3.setClosable(false);
		tabPane.getTabs().addAll(tab1, tab3, tab2);
		tabPane.setPrefHeight(prefHeight);
		
		tabPane.setSide(Side.TOP);
		

		holder.getChildren().addAll(tabPane); 
		//holder.setPadding(new Insets(5,5,5,5));	

		return holder; 
	}
	
	/**
	 * Set the tab to open. 
	 * @param index - the tab index (0 for detection tab, 1 for classification tab)
	 */
	public void setTab(int index) {
		tabPane.getSelectionModel().select(index);
	}
	
	/**
	 * Get the currently selected CT algorithm 
	 * @return the currently selected CT algorithm. 
	 */
	private ClickTrainAlgorithm getSelectedCTAlgorithm() {
		return clickTrainControl.getClickTrainAlgorithms().get(
				clickTrainAlgorithmBox.getSelectionModel().getSelectedIndex());
	}
	
	
	/**
	 * Set the data selector. 
	 */
	private void setDataSelector(){
		
		clickTrainControl.createDataSelector(sourcePane.getSource());
		
		//System.out.println("ClickTrainAlgorithmPaneFX: Detection Selector: " + clickTrainControl.getDataSelector() + "  " + sourcePane.getSource());
		
		if (clickTrainControl.getDataSelector()!=null) {
			Label dataSelectorLabel  = new Label("Detection Selector");
//			dataSelectorLabel.setFont(PamGuiManagerFX.titleFontSize2);
			PamGuiManagerFX.titleFont2style(dataSelectorLabel);

			dataSelectorHolder.setTop(dataSelectorLabel);
			dataSelectorHolder.setCenter(dataSelectorPane); 
		}
		else {
			dataSelectorHolder.setCenter(null);
			dataSelectorHolder.setTop(null);
		}
	}
	
	/**
	 * Enable the controls. 
	 */
	private void enableControls() {
		dataSelectorButton.setDisable(!dataSelectorCheckBox.isSelected());
	}
	
	/**
	 * Create the data selector. 
	 * @return the data selector. 
	 */
	private Pane createDataSelectorPane() {
		PamHBox hbox = new PamHBox();
		hbox.setSpacing(5);
		hbox.setAlignment(Pos.CENTER_LEFT);
		
		dataSelectorCheckBox = new PamToggleSwitch("Detection Selector"); 
		dataSelectorCheckBox.selectedProperty().addListener((obsval, oldval, newval)->{
			enableControls(); 
		});
		dataSelectorButton = new PamButton();
//		dataSelectorButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		dataSelectorButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		dataSelectorButton.setOnAction((action)->{
			showAdvPane();
		});
		hbox.getChildren().addAll(dataSelectorCheckBox, dataSelectorButton);
		return hbox; 
	}
	
	/**
	 * Creates pane allowing the user to change fine scale things such as error limits. 
	 * @return the pop over pane. 
	 */
	public void showAdvPane() {

		if (popOver==null) {
			popOver = new PopOver(); 
			PamBorderPane holder = new PamBorderPane(clickTrainControl.getDataSelector().getDialogPaneFX().getContentNode()); 
			holder.setPadding(new Insets(5,5,5,5));
			popOver.setContentNode(holder);
		}

		popOver.showingProperty().addListener((obs, old, newval)->{
			if (newval) {
				//the dialog has opened
				clickTrainControl.getDataSelector().getDialogPaneFX().setParams(true);
			}
			else {
				//the dialog has closed
				clickTrainControl.getDataSelector().getDialogPaneFX().getParams(true);
	
			}
		});

		popOver.show(dataSelectorButton);
	} 

	

	/**
	 * Populate the group pane. 
	 */
	private void populateGroupPane() {
		//now add appropriate group/channel check boxes to the pane. 
		groupHolder.getChildren().clear();
		groupCheckBoxes.clear();
		

		if (sourcePane.getSource()==null || !(sourcePane.getSource() instanceof GroupedDataSource)) return; 
		
		GroupedDataSource pamDataBlock = (GroupedDataSource) sourcePane.getSource(); 


//		System.out.println("sourcePane.getDataBlockBox() " + sourcePane.getDataBlockBox().getSelectionModel().getSelectedIndex());
//		System.out.println("Grouped Source Params: " +pamDataBlock);

		int[] groupBitMaps;
		if (pamDataBlock.getGroupSourceParameters().getGroupingType()==GroupedSourcePanel.GROUP_ALL) {
			//first. all channels could be grouped. In this case then we have a disabled check box showing the channels.
			groupBitMaps= new int[] {pamDataBlock.getGroupSourceParameters().getChanOrSeqBitmap()};
		}
		else{
			//otherwise get a list of channel bitmaps for the groups. 
			groupBitMaps=new int[pamDataBlock.getGroupSourceParameters().countChannelGroups()]; 
			int n = 0; 
			for (int i=0; i<groupBitMaps.length; i++) {
				int groupBitMap=pamDataBlock.getGroupSourceParameters().getGroupChannels(i);
				if (groupBitMap!=0) {
					groupBitMaps[n]=groupBitMap;
					n++;
				}
			}
		}

		//now add appropriate group/channel check boxes to the pane. 
		groupHolder.getChildren().clear();
		groupCheckBoxes.clear();
		for (int i=0; i<groupBitMaps.length; i++) {
			CheckBox checkBox=makeGroupCheckBox(groupBitMaps[i]);
			if (checkBox!=null) {
				checkBox.setUserData(new Integer(groupBitMaps[i]));
				this.groupCheckBoxes.add(checkBox); 
				//add to the pane
				this.groupHolder.getChildren().add(checkBox); 
			}
		}
	}

	/**
	 * Make a check box of channels
	 */
	private CheckBox makeGroupCheckBox(int channelBitMap) {
		int[] channelList = PamUtils.PamUtils.getChannelArray(channelBitMap);
		if (channelList!=null) {
			String channelName;
			if (channelList.length==1) channelName = "Channel " + Arrays. toString(channelList); 
			else channelName = "Channels " + Arrays. toString(channelList); 
			CheckBox checkBox= new CheckBox(channelName); 
			return checkBox;
		}
		return null;
	}

	
	@Override
	public ClickTrainParams getParams(ClickTrainParams currParams) {
		try {
			currParams.dataSourceIndex = sourcePane.getSourceIndex();
			currParams.dataSourceName = sourcePane.getSourceName();
			
			
			//use the data selector. 
			currParams.useDataSelector = this.dataSelectorCheckBox.isSelected();

			//get the groups. 
			currParams.channelGroups=getSelectedGroups();

			//get correct algorithm index
			currParams.ctDetectorType= clickTrainAlgorithmBox.getSelectionModel().getSelectedIndex();
			
			//pre-classifier
			currParams = this.preClassifierPane.getParams(currParams);
			if (currParams==null) {
				System.err.println("ClickTrainAlgorithmPaneFX: Error getting pre-classification settings;");
				//TODO warning dialog. 
				return null; 
			}
			
			//general settings to enable use of classifier 
			currParams = this.ctClassifierHolder.getParams(currParams);
			if (currParams==null) {
				System.err.println("ClickTrainAlgorithmPaneFX: Error getting classification settings;");
				return null; 
			}
			
			//get the algorithm parameters
			boolean okD = this.clickTrainControl.getAlgorithmParams(); 

			if (clickTrainControl.getDataSelector()!=null) {
				clickTrainControl.getDataSelector().getDialogPaneFX().getParams(true); 
			}

			if (!okD) {
				System.err.println("ClickTrainAlgorithmPaneFX: Error getting algorithm settings;");
				return null; 
			}
			
//			System.out.print(currParams.toString());

		}
		catch (Exception e){
			e.printStackTrace();
			return null; 
		}


		return currParams;
	}
	
	/**
	 * Show warning
	 * @param owner
	 * @param warningTitle
	 * @param warningText
	 * @return
	 */
	public static boolean showWarning(Window owner, String warningTitle, String warningText) {
		if (PamGUIManager.isSwing()) {
			JOptionPane.showMessageDialog(owner, warningText, warningTitle, JOptionPane.ERROR_MESSAGE);
		}
		else {
			PamGuiManagerFX.showAlertDialog(warningTitle, warningText);
		}
		return false;
	}

	/**
	 * Get channel bitmaps of the selected groups. 
	 * @return the selected groups. 
	 */
	private int[] getSelectedGroups() {
		int count=0;
		for (int i=0; i<this.groupCheckBoxes.size() ; i++) {
			if (groupCheckBoxes.get(i).isSelected()) count++;
		}
		
		//may not have a grouped data source in which case we want channel 0 only 
		//TODO - might be other exceptions that need to be though about
		if (count==0) return new int[] {1}; //channel 0 only. 

		int[] channelGroups=new int[count];

		Integer channelMap;
		count=0;
		for (int i=0; i<this.groupCheckBoxes.size() ; i++) {
			channelMap=(Integer) groupCheckBoxes.get(i).getUserData();
			if (groupCheckBoxes.get(i).isSelected()) {
				channelGroups[count]=channelMap.intValue();
				count++;
			}
		}

		return channelGroups;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setParams(ClickTrainParams clickTrainParams) {

		// and fill in the data source list (may have changed - or might in later versions)
		ArrayList<PamDataBlock> rd = PamController.getInstance().getDataBlocks(ClickDetection.class, true); 
		PamDataBlock  datablock = PamController.getInstance().getRawDataBlock(clickTrainParams.dataSourceName);
		
		//use the data selector. 
		dataSelectorCheckBox.setSelected(clickTrainParams.useDataSelector); 

		if (datablock==null && rd.size()>0){
			datablock=rd.get(0);
		}

		//source settings
		sourcePane.setSourceList();
		sourcePane.setSource(datablock);

		//set the data selector. 
		setDataSelector();

		//now set the channel maps. Check through all the channel groups in settings 
		//and tick any relevant boxes. It does not matter if the channel map is 
		//settings is not the same as the data block as a reference to the datablock sets the 
		//the check boxes each time. 
		if (clickTrainParams.channelGroups!=null) {
			for (int i=0; i<clickTrainParams.channelGroups.length; i++) {
				for (int j=0; j<groupCheckBoxes.size(); j++) {
					if (clickTrainParams.channelGroups[i]==
							((Integer) groupCheckBoxes.get(i).getUserData()).intValue()) {
						groupCheckBoxes.get(i).setSelected(true);
					}
				}
			}
		}

		clickTrainAlgorithmBox.getSelectionModel().select(clickTrainParams.ctDetectorType);
		
		if (clickTrainAlgorithmBox.getSelectionModel().getSelectedIndex() < 0) {
			clickTrainAlgorithmBox.getSelectionModel().select(0);
		}
	

		ClickTrainAlgorithm clickTrainAlgorithm=clickTrainControl.getClickTrainAlgorithms().get(
				clickTrainAlgorithmBox.getSelectionModel().getSelectedIndex());
		
		if (clickTrainAlgorithm.getClickTrainGraphics()!=null) {
			ctSettingsHolder.setCenter(clickTrainAlgorithm.getClickTrainGraphics().getCTSettingsPane());
		}
		
		//set parameters for the click train pre-filter. 
		this.preClassifierPane.setParams(clickTrainParams);
		
		//general settings to enable use of classifier 
		this.ctClassifierHolder.setParams(clickTrainParams);

		//notify the algorithm of updates.
//		System.out.println("getSelectedCTAlgorithm(): " + getSelectedCTAlgorithm()); 
//		System.out.println("getSelectedCTAlgorithm().getClickTrainGraphics(): " + getSelectedCTAlgorithm().getClickTrainGraphics()); 

		getSelectedCTAlgorithm().getClickTrainGraphics().notifyUpdate(ClickTrainControl.NEW_PARENT_DATABLOCK, 
				sourcePane.getDataBlockBox().getSelectionModel().getSelectedItem());
		
		enableControls();

	}

	@Override
	public String getName() {
		return "Click Train Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {

	}
	

}
