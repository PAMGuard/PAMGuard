package dataMap.layoutFX;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettings;
import dataMap.DataMapControl;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import pamViewFX.fxNodes.pamAxis.PamDateAxis;
import pamViewFX.fxStyles.PamStylesManagerFX;
import userDisplayFX.UserDisplayControlFX;
import userDisplayFX.UserDisplayNodeFX;
import userDisplayFX.UserDisplayNodeParams;

/**
 * Pane which shows users all data currently stored in binary files and
 * databases within PAMGuard. Also allows for navigation to different part of
 * the data time series.
 * 
 * @author Jamie Macaulay
 *
 */
public class DataMapPaneFX extends PamBorderPane implements UserDisplayNodeFX, PamSettings {

	private static final double HIDE_PANE_WIDTH = 400;

	/**
	 * Reference to the data map control. 
	 */
	private DataMapControl dataMapControl;

	/**
	 * Reference to the scrolling pane
	 */
	public ScrollingDataPaneFX scrollingDataPanel;


	private SummaryPaneFX summaryPane;

	/**
	 * The hiding pane which holds the summary pane. 
	 */
	private HidingPane hidingSettingsPane;

	/**
	 * The buttons which shows the top hiding pane. 
	 */
	private PamButton showButton;

	/**
	 * Pane which allows users to change scale on datamap. 
	 */
	private DataMapSettingsPane dataMapSettingsPane;

	private PamVBox settingsPane;
	/**
	 * The parameters for the data map 
	 */
	private DataMapParametersFX dataMapParamsFX = new DataMapParametersFX();

	public DataMapPaneFX(DataMapControl dataMapControl){
		this.dataMapControl=dataMapControl; 
		createDataMapPaneFX();
	}

	/**
	 * Create the pane.
	 */
	private void createDataMapPaneFX(){

		//create all the different panes, 
		summaryPane = new SummaryPaneFX(dataMapControl, this);

		scrollingDataPanel= new ScrollingDataPaneFX(dataMapControl, this); 

		dataMapSettingsPane=new DataMapSettingsPane(dataMapControl,this);
		dataMapSettingsPane.addSettingsListener(()->{
			//make sure paramters updates whenever user changes a setting. 
			dataMapParamsFX = dataMapSettingsPane.getParams(dataMapParamsFX);
			
			if (scrollingDataPanel !=null) {
				//forces things like the date axis to change. 
				scrollingDataPanel.notifyScrollChange();
			}
		});

		//create the setting spane
		settingsPane=new PamVBox(); 
		//		settingsPane.getChildren().add(summaryPane);
		settingsPane.getChildren().add(dataMapSettingsPane.getContentNode()); 
		settingsPane.setPadding(new Insets(40,10,10,10));
		settingsPane.setPrefWidth(HIDE_PANE_WIDTH);

		//		//have a horizontal scroll pane 
		//		PamScrollPane topScrollHolder=new PamScrollPane(topHolder); 
		//		topScrollHolder.setPrefHeight(180);
		//		topScrollHolder.setVbarPolicy(ScrollBarPolicy.NEVER)
		//topHolder.prefHeightProperty().bind(summaryPane.prefHeightProperty());

		//hiding summary pane
		hidingSettingsPane=new HidingPane(Side.RIGHT, settingsPane, scrollingDataPanel, true);
		hidingSettingsPane.setVisibleImmediatly(false); 
		hidingSettingsPane.showHidePane(true);
		hidingSettingsPane.getStyleClass().add("pane-trans");
		hidingSettingsPane.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
		StackPane.setAlignment(hidingSettingsPane, Pos.TOP_RIGHT);
		hidingSettingsPane.setPrefWidth(HIDE_PANE_WIDTH);

		//style the show button. 
		showButton=hidingSettingsPane.getShowButton();
		showButton.getStyleClass().add("close-button-left");
		showButton.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());


		//		showButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_DOWN, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));\
		showButton.setGraphic( PamGlyphDude.createPamIcon("mdi2c-cog", Color.WHITE, PamGuiManagerFX.iconSize));
		showButton.setPrefHeight(60);
		scrollingDataPanel.setRight(showButton);

		StackPane.setAlignment(showButton, Pos.CENTER_RIGHT);

		//add the settings pane to the scroll pane. 
		scrollingDataPanel.getCenterStackPane().getChildren().addAll(hidingSettingsPane, showButton);

		this.setCenter(scrollingDataPanel);
	}

	public void newSettings() {
		// TODO Auto-generated method stub
	}

	public void createDataGraphs() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				/**
				 * First check the limits of the database and binary stores. 
				 */
				setGraphDimensions();
				scrollingDataPanel.createDataGraphs();
			}
		});
	}

	/**
	 * Based on the scale and on the total length of data
	 * work out how big the little panels need to be 
	 */
	private void setGraphDimensions() {
		long totalLength = dataMapControl.getLastTime() - dataMapControl.getFirstTime();
	}

	public void repaintAll() {
		//refresh the ScrollingDataPaneFX
		this.scrollingDataPanel.repaintAll(); 		
	}

	public void newDataSources() {
		scrollingDataPanel.newDataSources();
		summaryPane.newDataSources();		
		//hidingSummaryPane.resetHideAnimation();
	}

//	/**
//	 * Called from ScalePanel when anything 
//	 * to do with scaling changes. 
//	 */
//	public void scaleChanged() {
//		if (dataMapSettingsPane == null || scrollingDataPanel == null) {
//			return;
//		}
//		dataMapSettingsPane.getParams(dataMapParamsFX);
//		scrollingDataPanel.scaleChange();
//	}

	@Override
	public Region getNode() {
		return this;
	}

	@Override
	public void openNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStaticDisplay() {
		// the data map is indeed a static display. It cannot be removed. 
		return true;
	}

	@Override
	public boolean isResizeableDisplay() {
		//Cannot resize the display
		return false;
	}

	@Override
	public boolean requestNodeSettingsPane() {
		//by setting to true, users are redirected to the data map pane if a settings call is made.
		return true;
	}

	@Override
	public void closeNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyModelChanged(int changeType) {
		//		System.out.println("DataMapPane: Notify model changed!!!: " + changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			scrollingDataPanel.updateScrollBar();
			dataMapSettingsPane.setParams(dataMapParamsFX);
			this.repaintAll();
			break;
		case PamControllerInterface.CHANGED_OFFLINE_DATASTORE:
			scrollingDataPanel.updateScrollBar();
			dataMapSettingsPane.checkDataGramPane();
			dataMapSettingsPane.setParams(dataMapParamsFX);
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			dataMapSettingsPane.checkDataGramPane();
			dataMapSettingsPane.setParams(dataMapParamsFX);
			break;
		case PamControllerInterface.INITIALIZE_LOADDATA:
		case PamControllerInterface.EXTERNAL_DATA_IMPORTED:
			scrollingDataPanel.updateScrollBar();
			dataMapSettingsPane.checkDataGramPane();
			dataMapSettingsPane.setParams(dataMapParamsFX);
			this.repaintAll();
			break;
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			scrollingDataPanel.updateScrollBar();
			dataMapSettingsPane.checkDataGramPane();
			dataMapSettingsPane.setParams(dataMapParamsFX);
			this.repaintAll();
			break;
		case PamControllerInterface.DATA_LOAD_COMPLETE:
			scrollingDataPanel.updateScrollBar();
			this.repaintAll();
			break; 
		}
	}

	@Override
	public String getName() {
		return "Data Map";
	}

	/**
	 * Called when mouse moves over a data graph to set time
	 * on scale Panel. Set null to clear cursor info on panel.
	 * @param timeMillis time in millis or null. 
	 */
	public void dataGraphMouseTime(Long timeMillis) {
		summaryPane.setCursorTime(timeMillis);
	}

	/**
	 * Called when the mouse moves into a new data stream pane. Shows the start and end 
	 * time of the data currently loaded into memory.
	 * @param timeStart - the start of loaded data in millis
	 * @param timeEnd - the end of loaded data in millis. 
	 */
	public void selectedDataTime(Long timeStart, Long timeEnd) {
		summaryPane.setSelectedDataTime(timeStart, timeEnd);
	}


	@Override
	public boolean isMinorDisplay() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UserDisplayNodeParams getDisplayParams() {
		// the datamap is a display which cannot be moved or changed so this is not needed. 
		return null;
	}

	@Override
	public void setFrameHolder(PamInternalPane internalFrame) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserDisplayControlFX getUserDisplayControl() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the current number of data stream panes
	 * @return the number of data stream panes
	 */
	public int getNumDataStreamPanes() {
		return this.scrollingDataPanel.getNumDataStreamPanes();
	}

	/**
	 * Get a data stream pane. 
	 * @param n - the index of the data stream pane
	 * @return the data stream pane or null if the index is out of bounds. 
	 */
	public DataStreamPaneFX getDataStreamPane(int n) {
		return this.scrollingDataPanel.getDataStreamPane( n);

	}

	public DataStreamPaneFX getDataStreamPane(DataMapInfo selectedItem) {
		return scrollingDataPanel.getDataStreamPane(selectedItem);
	}
	
	/**
	 * Set the data map parameters. 
	 * @param dataMapParamsFX
	 */
	public void setDataMapParams(DataMapParametersFX dataMapParamsFX) {
		 this.dataMapParamsFX = dataMapParamsFX;
	}

	/**
	 * Get the data map parameters associated with the FX GUI. Note these are
	 * separate from the parameters in the DataMapControls which are for the default
	 * swing display (not great)
	 * 
	 * @return the current data map parameters.
	 */
	public DataMapParametersFX getDataMapParams() {
		return this.dataMapParamsFX;
	}

	@Override
	public String getUnitName() {
		return this.dataMapControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "data_map_paneFX";
	}

	@Override
	public Serializable getSettingsReference() {
		return this.dataMapParamsFX;
	}

	@Override
	public long getSettingsVersion() {
		return  DataMapParametersFX.serialVersionUID;

	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		dataMapParamsFX = ((DataMapParametersFX) pamControlledUnitSettings.getSettings()).clone();
		return (dataMapParamsFX != null);
	}

}
