package detectionPlotFX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.data.DDPlotRegister;
import detectionPlotFX.layout.DDDataPane2;
import detectionPlotFX.layout.DetectionPlotDisplay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * 
 * A detection plot display with convenience functions to set any type of data unit. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class DetectionGroupDisplay extends PamBorderPane {
	
	/**
	 * Show the settings within hiding panes within the display. 
	 */
	public static final int DISPLAY_COMPACT = 0;
	
	/**
	 * Show settings on top and to the right of the display
	 */
	public static final int DISPLAY_EXTENDED = 1;

	/**
	 * Index of the current normal unit with the detection summary. 
	 */
	private int currentUnitIndex;

	/**
	 * Arrow to move data units
	 */
	public PamButton arrowLeft;

	/**
	 * Arrow to move data units to the left. 
	 */
	public PamButton arrowRight;

	/**
	 * Label for the top holder. 
	 */
	private Label dataLabel;

	/**
	 * Holds the arrow pane and hiding pane. 
	 */
	private PamBorderPane topHolder;


	/**
	 * A preview display of the data unit
	 */
	public DetectionPlotDisplay detectionDisplay;

	/**
	 * The current data info showing the graph. 
	 */
	private DDDataInfo currentDataInfo;

	/**
	 * A hash map which holds information on which data block has which DDDataInfo. 
	 */
	private HashMap<PamDataBlock, DDDataInfo> dDataInfoHashMap;

	/**
	 * Holds the detection display and controls for viewing standard detections. 
	 */
	public Pane detectionDisplayHolder;

	/**
	 * The group detection listeners. 
	 */
	public ArrayList<GroupDisplayListener> displayListeners = new ArrayList<GroupDisplayListener>();

	/**
	 * The current detection group. 
	 */
	private List<PamDataUnit> detectionGroup;

	/**
	 * Hiding pane for the plot settings. 
	 */
	private HidingPane hidingPane;

	/**
	 * Flag for how the deteciton plot is laid out.  
	 */
	private int layoutType = DISPLAY_EXTENDED;

	/**
	 * Toggle switch for showing the scroll pane. 
	 */
	private PamToggleSwitch showScrollSwitch;
	
	/**
	 * Constructor for the detection group display. 
	 */
	public DetectionGroupDisplay() {
		//create hash map to map DDDataInfos to datablocks for quick access. 
		dDataInfoHashMap = new HashMap<PamDataBlock, DDDataInfo>(); 
		this.layoutType = DISPLAY_EXTENDED;
		createDetectionDisplay(DISPLAY_EXTENDED);
		this.setCenter(detectionDisplayHolder);
	}
	
	/**
	 * Constructor for the detection group display. 
	 * @param layoutType - the layout of the display - e.g. DetectionGroupDisplay.DISPLAY_COMPACT
	 */
	public DetectionGroupDisplay(int layoutType) {
		this.layoutType = layoutType;

		//create hash map to map DDDataInfos to datablocks for quick access. 
		dDataInfoHashMap = new HashMap<PamDataBlock, DDDataInfo>(); 
		createDetectionDisplay(layoutType);
		this.setCenter(detectionDisplayHolder);
	}

	/**
	 * Create the detection display. 
	 * @return the detection display
	 */
	private Pane createDetectionDisplay(int layoutType){
		
		detectionDisplay= new DetectionPlotDisplay();

		arrowLeft= new PamButton();
//		arrowLeft.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.KEYBOARD_ARROW_LEFT, PamGuiManagerFX.iconSize));
		arrowLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));
		arrowLeft.setOnAction((action)->{
			detectionDisplay.drawCurrentUnit();
			//move to the previous data unit
			nextUnit(false);
		});

		arrowRight= new PamButton();
//		arrowRight.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.KEYBOARD_ARROW_RIGHT, PamGuiManagerFX.iconSize));
		arrowRight.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-right", PamGuiManagerFX.iconSize));
		arrowRight.setOnAction((action)->{
			detectionDisplay.drawCurrentUnit();
			//move to the right data
			nextUnit(true);
		});

		PamHBox arrowPane = new PamHBox(); 
		arrowPane.setAlignment(Pos.CENTER_RIGHT);

		arrowPane.getChildren().addAll(arrowLeft, arrowRight);
		BorderPane.setAlignment(arrowPane, Pos.CENTER_RIGHT);
		
		//a label to show information of the data unit
		dataLabel = new Label();
		
		if (layoutType==DISPLAY_EXTENDED) {
			//the display has controls above the axis and a hiding pane that increases the width of the display. 
			
			//the holder for the detection display. 
			PamBorderPane detectionDisplayHolder= new PamBorderPane();
			
			//create the hiding pane to show advanced settings. 
			hidingPane = new HidingPane(Side.RIGHT, detectionDisplay.getSettingsHolder(), detectionDisplayHolder, layoutType==DISPLAY_COMPACT, 0);
	
			topHolder=new PamBorderPane();
			topHolder.setRight(arrowPane);
	
			topHolder.setCenter(dataLabel);
			topHolder.setLeft(detectionDisplay.getDataTypePane());
			
			//whenever the detection plot selection box e.g. from waveform to wigner then check there is a settings pane. If not
			//then get rid of the settings button. 
			detectionDisplay.getDataTypePane().getDetectionPlotBox().valueProperty().addListener((obsVal, oldVal, newVal)->{
				enableControls(); 
			});
	
			detectionDisplayHolder.setTop(topHolder);
			detectionDisplayHolder.setCenter(detectionDisplay);
			this.detectionDisplayHolder = detectionDisplayHolder;
	
			arrowPane.getChildren().add(hidingPane.getShowButton()); 
			
			hidingPane.getShowButton().setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", 
					PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize)); 
			hidingPane.setShowButtonOpacity(1.0); //don't want show button to gray out if mouse not over it
			this.setRight(hidingPane); //bit of a hack but works. 
			hidingPane.showHidePane(false);
			
			 //this makes the hide button appear top right which is nicer for closing the pane. 
			StackPane.setAlignment(hidingPane.getHideButton(),  Pos.TOP_RIGHT);
			//hidingPane.removeHideButton();
			hidingPane.styleHideButton(hidingPane.getHideButton(), Side.LEFT);
			
			//make the background dark for settings pane. 
			detectionDisplay.getSettingsHolder().setStyle("-fx-background-color: -fx-darkbackground");
			
		}
		else {
			//the display is compact with all controls within an internal hiding pane. 
			
			detectionDisplayHolder = new PamStackPane(); 
			
			PamTabPane settingsPane = new PamTabPane(); 
			//this has to be before removing the heading button
			settingsPane.setAddTabButton(false);
			settingsPane.setTabMinHeight(60);
			settingsPane.setMinHeight(100);
			
//			settingsPane.getStyleClass().add(Styles.TABS_FLOATING);
			settingsPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
			
			PamGridPane gridPane = new PamGridPane();
			gridPane.setHgap(5.);
			gridPane.setVgap(5.);
			gridPane.setPadding(new Insets(0,0,0,5));
			
			gridPane.add(new Label("Plot type"), 0, 0);
			gridPane.add(detectionDisplay.getDataTypePane(), 1, 0);
			
			showScrollSwitch = new PamToggleSwitch("Show scroll bar");
			showScrollSwitch.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				//show or hide the scroll bar. 
				this.setEnableScrollBar(newVal);
			});
			showScrollSwitch.setSelected(true);
			gridPane.add(showScrollSwitch, 0, 1);
			GridPane.setColumnSpan(showScrollSwitch, GridPane.REMAINING);


			Tab dataTab = new Tab("Plot",gridPane);
			
			ScrollPane scrollPane = new ScrollPane();
			scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
			scrollPane.setContent(detectionDisplay.getSettingsHolder());
			Tab settingsTab = new Tab("Settings", scrollPane);
			
			//here add the option to show the scroll bar or not. 
			settingsPane.getTabs().add(dataTab);
			settingsPane.getTabs().add(settingsTab);
			
			//set the hiding pane
			Node icon = PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize); 
			detectionDisplay.getPlotPane().setHidePane(new PamBorderPane(settingsPane), icon,  Side.RIGHT);

			//move the hiding pane button into the top of the tab pane - this makes best
			//use of space. 
			hidingPane = detectionDisplay.getPlotPane().getHidePane(Side.RIGHT);
			
			hidingPane.removeHideButton();
			
			settingsPane.setTabStartRegion(hidingPane.getHideButton());
//			hidingPane.getHideButton().getStyleClass().add("close-button-right-trans");
//			hidingPane.getHideButton().setStyle(" -fx-background-radius: 0 0 0 0;");
			hidingPane.setPadding(new Insets(0,0,0,0));
			
			hidingPane.getHideButton().prefHeightProperty().bind(settingsPane.tabMinHeightProperty());
			
			//set the show button to be slight larger
			hidingPane.getShowButton().setPrefHeight(60.);
			
			//now everything to pane. 
			detectionDisplayHolder.getChildren().add(detectionDisplay);
			StackPane.setAlignment(detectionDisplay, Pos.CENTER);
			
			//settingsPane.setPadding(new Insets(35,0,0,0));
		}
		
		//set styles
//		detectionDisplay.getSettingsHolder().getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
//		detectionDisplay.getSettingsHolder().setStyle("-fx-background-color: -fx-darkbackground");
		hidingPane.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());

		//set default size
		detectionDisplayHolder.setPrefSize(500, 400);
		detectionDisplayHolder.setPadding(new Insets(0,10,0,0));
		
		return detectionDisplayHolder;
	}

	
	/**
	 * Enable a disable controls. 
	 */
	private void enableControls() {
		//System.out.println("Enable controls: " + detectionDisplay.getCurrentDataInfo().getCurrentDetectionPlot().getSettingsPane());
		hidingPane.getShowButton().setVisible(detectionDisplay.getCurrentDataInfo().getCurrentDetectionPlot().getSettingsPane()!=null);
		//if there are no settings and the hide pane is hsowing then close it
		if (hidingPane.showingProperty().get() && detectionDisplay.getCurrentDataInfo().getCurrentDetectionPlot().getSettingsPane()==null) {
			hidingPane.showHidePane(false, false);//don;t use animation as sets show button visible again.,
		}
	}

	/**
	 * Get the detection display. 
	 * @return the detection display. 
	 */
	public DetectionPlotDisplay getDetectionDisplay() {
		return detectionDisplay;
	}
	
	/**
	 * Get the DDDataPane2 - this allows users to change between different DataInfos. 
	 * @return the DDDataPane2. 
	 */
	public DDDataPane2 getDataTypePane() {
		return detectionDisplay.getDataTypePane(); 
	}

	/**
	 * Select and show the next data unit. 
	 * @param forward true to move the the next data in the last. False to move to the previous data unit. 
	 */
	private void nextUnit(boolean forward){
		if (detectionGroup==null || detectionGroup.size()==0) return; 

		if (forward) currentUnitIndex++;
		else currentUnitIndex--; 

		if (currentUnitIndex>detectionGroup.size()-1) currentUnitIndex=0;
		if (currentUnitIndex<0) currentUnitIndex=detectionGroup.size()-1;

		setDataUnit(detectionGroup.get(currentUnitIndex));
		setLabelText();

		//TODO - add old data 
		//trigger the detection listeners
		triggerListeners(null, detectionGroup.get(currentUnitIndex)); 
	}

	/**
	 * Set text on the label showing the unit UID and which units out the selected group is currently selected.
	 */
	private void setLabelText(){
		if (detectionGroup.size()>1){
			dataLabel.setText(detectionGroup.get(currentUnitIndex).getParentDataBlock().getDataName() + " UID: " + detectionGroup.get(currentUnitIndex).getUID() +
					": " + (currentUnitIndex+1) + " of " + detectionGroup.size());
		}
		else if (detectionGroup.size()==1){
			dataLabel.setText(detectionGroup.get(currentUnitIndex).getParentDataBlock().getDataName() + " UID: " + detectionGroup.get(currentUnitIndex).getUID());
		}
		else {
			//selected area with data units. 
			dataLabel.setText("No data units in area");
		}
		
		//add sub detection count if there are sub detections. 
		if (detectionGroup.get(currentUnitIndex) instanceof SuperDetection) {
			SuperDetection superDet = (SuperDetection) detectionGroup.get(currentUnitIndex);
			int subCount = superDet.getSubDetectionsCount(); 
			if (subCount>0) {
				dataLabel.setText(dataLabel.getText()+ " : " + superDet.getSubDetectionsCount() + " sub detection" + (subCount>1 ? "s":"")); 
			}
		}
	}

	/**
	 * Called whenever display is first opened. 
	 */
	public void prepareDisplay() {

		if (detectionGroup==null) return; 

		this.currentDataInfo=null; 

		if (detectionGroup.size()>=1) {
			setDataUnit(detectionGroup.get(0));
			//trigger the detection listeners
			triggerListeners(null, detectionGroup.get(0)); 
		}

		//don't need to see arrows if there is only one data unit. 
		if (detectionGroup.size()==1){
			this.arrowLeft.setVisible(false);
			this.arrowRight.setVisible(false);
		}
		else {
			this.arrowLeft.setVisible(true);
			this.arrowRight.setVisible(true);
		}

	}

	
	/**
	 * Clear the plot pane. 
	 */
	public void clearDisplay() {
		setDataUnit(null);
		detectionDisplay.clearPane();
		this.dataLabel.setText("");
	}


	/**
	 * Sets the current in the display. 
	 * @param pamDataUnit - the current data unit to set. 
	 * @param detectionDisplay- the detection display plot to set the data unit for. 
	 * @return true of a new data info has been added - usually means a different type of detection to display compared to the last detection.
	 */
	public boolean setDataUnit(PamDataUnit<?, ?> dataUnit){

		detectionDisplay.clearPane();
		
		if (dataUnit==null) {
			detectionDisplay.removeDataInfo();
			return true; 
		}

		//		TDDataInfoFX dataInfo = this.tdGraphFX.findDataInfo(dataUnit);
		//		//set up the display; 
		//		//only set the data info if it's actually different
		//		if (dataInfo.getDDataProvider(detectionDisplay)==null){
		//			detectionDisplay.removeDataInfo();
		//		}
		//				
		//		DDDataInfo dDataInfo = dataInfo.getDDataProvider(detectionDisplay);

		//try to find the DDDataInfo in the hashmap

		DDDataInfo dDataInfo = this.dDataInfoHashMap.get(dataUnit.getParentDataBlock());

		//Debug.out.println("DDDataInfo from HashMap: " + dDataInfo);

		//if null and the data block does not map to null...
		if (dDataInfo==null && !dDataInfoHashMap.containsKey(dataUnit.getParentDataBlock())) {
			DDDataProvider ddPlotProvider = DDPlotRegister.getInstance().findDataProvider(dataUnit.getParentDataBlock()); 
			if (ddPlotProvider!=null) {
				dDataInfo =  ddPlotProvider.createDataInfo(this.detectionDisplay); 
			}
			else {
				dDataInfo = null; 
			}
			//now map the data block to the dDataInfo. 
			//Debug.out.println("Add to HashMap");
			dDataInfoHashMap.put(dataUnit.getParentDataBlock(), dDataInfo); 
		}

		if (dDataInfo==null) return true; 

		//only change the dDataInfo if it's different,. 
		boolean newDataInfo = false; 
		if (currentDataInfo!=dDataInfo){
			detectionDisplay.removeDataInfo();
			currentDataInfo=dDataInfo;
			detectionDisplay.setDataInfo(currentDataInfo);
			newDataInfo=true; 
		}

		//set the unit
		detectionDisplay.newDataUnit(dataUnit); 


		//notify change causes a repaint which slows the display down
		//only need to do this if the data info has changes. But messy but meh. 
		if (newDataInfo) {
			detectionDisplay.getDataTypePane().notifyDataChange(); 
		}

		//		//draw the unit
		//		detectionDisplay.drawCurrentUnit();

		//now highlight the data unit on the graph Bit of a hack, but works. 
		//		clearSingleType();

		//TODO....highlight data unit. 
		
		return newDataInfo;
	}
	

	/**
	 * Attempts to set the detectionPlot
	 * @param plotName
	 * @return
	 */
	public boolean setDetectionPlot(String plotName) {
	
		//set the current detection plot based in the name
		boolean setOk = currentDataInfo.setCurrentDetectionPlot(plotName);
		
		//update the detection settings pane so it shows the correct plot names etc. 
		detectionDisplay.getDataTypePane().notifyDataChange(); 

		return setOk;
	}


	/**
	 * Trigger the data unit changing listeners. 
	 * @param oldDataUnit - the old current data unit. 
	 * @param newDataUnit - the new current data unit. 
	 */
	public void triggerListeners(PamDataUnit oldDataUnit, PamDataUnit newDataUnit) {
		for (GroupDisplayListener aListener : displayListeners) {
			aListener.newDataUnitSelected(oldDataUnit, newDataUnit);
		}
		
	}

	/**
	 * Add a display GroupDisplayListener. 
	 * @return remove a detection display listener
	 */
	public void addDisplayListener(GroupDisplayListener groupDisplayListener) {
		this.displayListeners.add(groupDisplayListener);
	}


	/**
	 * Remove a display GroupDisplayListener. 
	 * @return remove a detection display listener
	 * 
	 */
	public boolean removeDisplayListener(GroupDisplayListener groupDisplayListener) {
		return this.displayListeners.remove(groupDisplayListener);
	}

	/**
	 * Draw the current unit. 
	 */
	public void drawCurrentUnit() {
		this.detectionDisplay.drawCurrentUnit();
	}

	/**
	 * Set the detection group. 
	 * @param dataList - the detection group. 
	 */
	public void setDetectionGroup( List<PamDataUnit> dataList) {
		this.detectionGroup=dataList; 

		if (dataList!=null && dataList.size()>0) {
			currentUnitIndex=0; 
			this.setDataUnit(dataList.get(0));
			setLabelText();
		}
	}

	/**
	 * Get the currently displayed data unit. 
	 * @return the currently displayed data unit. 
	 */
	public PamDataUnit getCurrentUnit() {
		if (detectionGroup==null) return null;
		return detectionGroup.get(currentUnitIndex);
	}

	
	/**
	 * Show the scroll bar which allows the user to change time limits. 
	 * @param enableScrollBarPane - true to enable the time scroll bar. 
	 */
	public void setEnableScrollBar(boolean enableScrollBarPane) {
		if (this.layoutType==DISPLAY_COMPACT) {
			showScrollSwitch.setSelected(enableScrollBarPane);
		}
		detectionDisplay.setEnableScrollBar(enableScrollBarPane);
		detectionDisplay.setupScrollBar();
	}
	
	/**
	 * Check whether the scroll bar is changing. The scroll bar allows the user to change time limits. 
	 * @return true if the scroll bar pane is showing. 
	 */
	public boolean isEnableScrollBar() {
		return this.detectionDisplay.isEnableScrollBar();
	}


	
//	@Override
//	public boolean requestNodeSettingsPane() {
//		if (dDPlotPane.getHidePane(Side.RIGHT)!=null) dDPlotPane.getHidePane(Side.RIGHT).showHidePane(true);
//		if (dDPlotPane.getHidePane(Side.LEFT)!=null) dDPlotPane.getHidePane(Side.LEFT).showHidePane(true);
//		return true;
//	}


}