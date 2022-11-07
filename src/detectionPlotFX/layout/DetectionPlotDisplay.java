package detectionPlotFX.layout;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import detectionPlotFX.DetectionDisplayControl;
import detectionPlotFX.DetectionPlotParams;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import pamViewFX.fxNodes.pamAxis.PamAxisPane2;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.ScrollBarPane;
import pamViewFX.fxStyles.PamStylesManagerFX;
import userDisplayFX.UserDisplayNodeFX;
import userDisplayFX.UserDisplayNodeParams;

/**
 * A detection display plot usually plots a single detection. 
 * The axis on a detection display plot can be anything, 
 * e.g. dB and bin number, linear amplitude and frequency. 
 * <p>
 * TODO - explain data structure.
 * <p>
 * @author Jamie Macaulay
 *
 */
public class DetectionPlotDisplay extends PamBorderPane implements UserDisplayNodeFX  {
	

	private static final double PREF_SETTINGS_WIDTH = 250;

	/**
	 * Holds all the various bits of the graph. 
	 */
	private PamBorderPane holderPane; 

	/**
	 * The currentDataInfo for this display. The DDDataInfo is essentially a wrapper for a data block which controls
	 * how data units from that data block are displayed on the plot pane. 
	 */
	private DDDataInfo currentDataInfo;

	/**
	 * This is were all the action happens. The plot pane draws the data and contains all controls etc.
	 */
	private DDPlotPane dDPlotPane;

	/**
	 * Allows users to select which data block is a parent to the display and therefore
	 * what data units the detection plot displays. 
	 */
	private ComboBox<String> dataBlockSelBox;

	/**
	 * The settings pane which contains controls to change parent data block and select which
	 *way to display the data unit.
	 */
	private DDDataPane2 dataSettingsPane;

	/**
	 * Pane which sits on the right hand side and 
	 */
	private PamBorderPane settingsHolder; 



	/**
	 * Enable the settings pane to be opened by user. Otherwise it can only be opened by code. 
	 */
	private boolean enableSettingsButton = true; 

	/**
	 * Convenience reference to the settings css style
	 */
	String cssSettingsResource=PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS();

	/**
	 * The pane which allows users to settings specific to the type of display
	 */
	private Pane displaySettingsPane;

	/**
	 * Reference to the detection display control. 
	 */
	private DetectionDisplayControl detectionDisplayControl;

	/**
	 * The last detection to be plotted. 
	 */
	private PamDataUnit lastDetection;

	/**
	 * The detection plot projector. This handles all conversion from data unit values to pixels 
	 * on the detection plot.  
	 */
	private DetectionPlotProjector detectionPlotProjector;

	/**
	 * Check whether the display is in viewer mode. 
	 */
	private boolean isViewer;

	private DetectionPlotParams detectionPlotParams;

	private ScrollBarPane scrollBarPane;

	
	/**
	 * True to redraw the scroll bar;
	 */
	private boolean reDrawScroll = false;

	/**
	 * Enable the scroll bar. 
	 */
	private boolean enableScrollBar = true; 



	public DetectionPlotDisplay(DetectionDisplayControl detectionDisplayControl) {
		super(); 
		this.detectionDisplayControl=detectionDisplayControl;
		createDisplay(true);
		createScrollBarPane(); 
		this.detectionPlotProjector = new DetectionPlotProjector(dDPlotPane); 
		detectionPlotParams=new DetectionPlotParams(); 
	}


	public DetectionPlotDisplay() {
		super(); 
		createDisplay(false);
		createScrollBarPane(); 
		this.detectionPlotProjector = new DetectionPlotProjector(dDPlotPane); 
		detectionPlotParams=new DetectionPlotParams(); 
	}

	/**
	 * Creates the display.
	 * @param userDisplay- true if the display is a muser display i.e. user has full control of data block input etc. 
	 */
	private void createDisplay(boolean userDisplay){
		//		System.out.println("CREATE DD DIPLSAY "  +userDisplay);
		holderPane=new PamBorderPane();
		holderPane.setCenter(dDPlotPane=new DDPlotPane());

		//create settings panes
		this.dataSettingsPane=new DDDataPane2(this, userDisplay); 
		//			dataSettingsPane.setDataBlockSelect(userDisplay);
		
		//stop using this - having a hiding pane is useless here- better a combo box instead. 
//		dDPlotPane.setHidePane(dataSettingsPane, PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_RIGHT, 
//		dDPlotPane.setHidePane(dataSettingsPane, PamGlyphDude.createPamIcon("mdi2c-chevron-right", 
//				PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize), Side.LEFT);

		this.settingsHolder=new PamBorderPane(); 
		settingsHolder.setPrefWidth(PREF_SETTINGS_WIDTH);
		//add to panes on plot. 
//		dDPlotPane.setHidePane(settingsHolder, PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, 
//		dDPlotPane.setHidePane(settingsHolder, PamGlyphDude.createPamIcon("mdi2c-cog", 
//		PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize), Side.RIGHT);

//		dDPlotPane.getHidePane(Side.RIGHT).getShowButton().setVisible(enableSettingsButton);

		this.isViewer=PamController.getInstance().getRunMode()==PamController.RUN_PAMVIEW;

		//add listeners
		dDPlotPane.getPlotCanvas().widthProperty().addListener((obsVal, oldval, newval)->{
			this.drawCurrentUnit();
		});


		dDPlotPane.getPlotCanvas().heightProperty().addListener((obsVal, oldval, newval)->{
			this.drawCurrentUnit();
		});


		this.setCenter(holderPane);

	}

	/**
	 * Create the scroll bar pane. 
	 */
	private void createScrollBarPane() {
		this.scrollBarPane = new ScrollBarPane(); 
		scrollBarPane.setShowMillis(true);

		scrollBarPane.currentValueProperty().addListener((obsVal, oldVala, newVal)->{
			scrollBarChanged();

		});

		scrollBarPane.visibleAmountProperty().addListener((obsVal, oldVala, newVal)->{
			scrollBarChanged();
		});		

		scrollBarPane.setPadding(new Insets(5,0,5,0));
		
		scrollBarPane.getDrawCanvas().widthProperty().addListener((obsVal, oldVala, newVal)->{
			repainScrollBar();
		});	
		
		scrollBarPane.getDrawCanvas().heightProperty().addListener((obsVal, oldVala, newVal)->{
			//System.out.println("ScrollBar height changed: "); 
			repainScrollBar(); 
		});	
		
		
		//realy importan as in some displays the canvas repaints and resizes by one pixel and then keeps on increasing...forever...in height 
		scrollBarPane.setPrefHeight(40);
		scrollBarPane.setMaxHeight(40);

		this.setTop(scrollBarPane);
	}

	/**
	 * Called whenever the scroll values change
	 */
	void scrollBarChanged() {
		if (detectionPlotProjector.enableScrollBar) {
			detectionPlotProjector.setAxisMinMax(scrollBarPane.getCurrentValue(), 
					scrollBarPane.getCurrentValue()+scrollBarPane.getVisibleAmount(), detectionPlotProjector.getScrollAxis());
			drawCurrentUnit();
		}
	}
	
	/**
	 * Repaint the scroll bar. 
	 */
	private void repainScrollBar() {
		if (currentDataInfo!=null && detectionPlotProjector.enableScrollBar && lastDetection!=null) {
			currentDataInfo.drawData(scrollBarPane.getDrawCanvas().getGraphicsContext2D(), 
					new Rectangle(0,0,scrollBarPane.getDrawCanvas().getWidth(),scrollBarPane.getDrawCanvas().getHeight()), 
					this.detectionPlotProjector, this.lastDetection, DetectionPlot.SCROLLPANE_DRAW);
		}
	}
	
	/**
	 * Get whether PAMGuard is in viewer mode.  
	 * @return true if in viewer mode. 
	 */
	public boolean isViewer() {
		return isViewer;
	}


	@Override
	public String getName() {
		return "Detection Display";
	}

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
		return false;
	}

	@Override
	public boolean isResizeableDisplay() {
		return true;
	}

	@Override
	public void closeNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType){
		case PamControllerInterface.INITIALIZATION_COMPLETE:

			break; 
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			this.dataSettingsPane.notifyDataChange(); 
			break; 
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//this is were the data block may have been added. Need to add an observer to this data block to say when the thing has 
			//thing has a new detection. 
			this.dataSettingsPane.notifyDataChange(); 
			break;
		}

	}

	/**
	 * Set the DataInfo for the display. 
	 * @param newDataProviderFX - a DDDataProvider used to create a new DDDataInfo instance. 
	 */
	public void setDataInfo(DDDataProvider newDataProviderFX) {
		if (newDataProviderFX==null) {
			removeDataInfo();
		}
		else{
			this.currentDataInfo=newDataProviderFX.createDataInfo(this); 
			//now need to set relevent panes. 
			this.settingsHolder.setCenter(currentDataInfo.getSettingsPane()); 

		}
		//enableLeftSettingsPane();
		reDrawScroll=true;
	}

	/**
	 * Set the DataInfo for the display. 
	 * @param dDDataInfo - the DataInfo to set. 
	 */
	public void setDataInfo(DDDataInfo dDDataInfo) {
		if (dDDataInfo==null) {
			removeDataInfo();
		}
		else{
			this.currentDataInfo=dDDataInfo;
			//now need to set relevant panes. 
			this.settingsHolder.setCenter(currentDataInfo.getSettingsPane());
		}
		//enableLeftSettingsPane();
		reDrawScroll=true; 
	}

//	/**
//	 * Enable the left settings pane depending on how many dd data info ploits there
//	 * are and if the data block selection is available. 
//	 */
//	private void enableLeftSettingsPane() {
//		boolean leftPane; 
//		if (dataSettingsPane.isDataBlockSelect()) leftPane = true; 
//		else leftPane = currentDataInfo.getDetectionPlotCount()>1;
//		dDPlotPane.getHidePane(Side.LEFT).getShowButton().setVisible(leftPane);
//	}

	/*
	 * Remove the current data info. 
	 */
	public void removeDataInfo() {
		currentDataInfo=null;
		this.settingsHolder.setCenter(null); 
		reDrawScroll=true; 
	}

	/**
	 * Get the current DDDataInfo for the graph. 
	 * @return the current DDDataInfo. Can be null. 
	 */
	public DDDataInfo getCurrentDataInfo() {
		return currentDataInfo;
	}

	/**
	 * Display a new data unit on the graph.
	 * @param the new data unit to display
	 */
	public synchronized void newDataUnit(PamDataUnit newDataUnit) {
		this.lastDetection=newDataUnit; 
		reDrawScroll= true; 
		drawDataUnit(newDataUnit); 
	}
	
	/**
	 * Setup the scroll bar if the pot changes. 
	 */
	public void setupScrollBar(){
		setupScrollBar(this.lastDetection); 
	}

	/**
	 * Setup the scroll bar so that it has the correct limits and datagram for 
	 * the current data unit. 
	 */
	public void setupScrollBar(PamDataUnit newDataUnit){
		//setup the scroll bar (or not)
		if (enableScrollBar && this.detectionPlotProjector.enableScrollBar && newDataUnit!=null) {
			
			this.setTop(scrollBarPane);
			
			if (currentDataInfo!=null) {
			//important we put this here as it allows the plot to set up the scroll bar pane. 
				this.currentDataInfo.setupAxis(detectionPlotProjector, newDataUnit); 
			}
			
			//System.out.println("Set min and max limits for scroll bar: " +  detectionPlotProjector.getMinScrollLimit() +  "   " + detectionPlotProjector.getMaxScrollLimit()); 
			scrollBarPane.setMinVal(detectionPlotProjector.getMinScrollLimit());
			scrollBarPane.setMaxVal(detectionPlotProjector.getMaxScrollLimit());

			scrollBarPane.setCurrentValue(scrollBarPane.getMinVal());
			scrollBarPane.setVisibleAmount(scrollBarPane.getMaxVal()-scrollBarPane.getMinVal());
			
			detectionPlotProjector.setAxisMinMax(scrollBarPane.getCurrentValue(), 
					scrollBarPane.getCurrentValue()+scrollBarPane.getVisibleAmount(), detectionPlotProjector.getScrollAxis());

			if (currentDataInfo!=null) {
				currentDataInfo.drawData(scrollBarPane.getDrawCanvas().getGraphicsContext2D(), 
						new Rectangle(0,0,scrollBarPane.getDrawCanvas().getWidth(),scrollBarPane.getDrawCanvas().getHeight()), 
						this.detectionPlotProjector, newDataUnit, DetectionPlot.SCROLLPANE_DRAW);
			}
	

		}
		else {
			this.setTop(null);
		}
	}

	/**
	 * Convenience function. Same as {@link #newDataUnit}
	 * @param the new data unit to display
	 */
	public void setDataUnit(PamDataUnit newDataUnit) {
		newDataUnit(newDataUnit); 
	}

	/**
	 * Clear tyhe plot. 
	 */
	public void clearPane(){
		dDPlotPane.getPlotCanvas().getGraphicsContext2D().clearRect(0, 0, dDPlotPane.getPlotCanvas().getWidth(),dDPlotPane.getPlotCanvas().getHeight());
	}

	/*
	 *Draw the data unit.  
	 */
	private void drawDataUnit(PamDataUnit newDataUnit) {
		//Debug.out.println("DetectionPlotDisplay DrawDataUnit: " +newDataUnit);
		if (currentDataInfo!=null){
			//sometimes the axis just need a little push to make sure the pane and axis object bindings have been updated
			for (int i=0; i<Side.values().length; i++) {
				dDPlotPane.getAxisPane(Side.values()[i]).layout(); 
			}

			currentDataInfo.drawData(dDPlotPane.getPlotCanvas().getGraphicsContext2D(), 
					new Rectangle(0,0,dDPlotPane.getPlotCanvas().getWidth(),dDPlotPane.getPlotCanvas().getHeight()), 
					this.detectionPlotProjector, newDataUnit);
		}
		if (reDrawScroll) {
			 setupScrollBar( newDataUnit);
			 reDrawScroll=false; 
		}
		//dDPlotPane.repaintAxis(); 
	}

	/**
	 * Redraw the current dataunit. 
	 */
	public void drawCurrentUnit(){
		if (lastDetection==null){
			dDPlotPane.getPlotCanvas().getGraphicsContext2D().clearRect(0, 0, dDPlotPane.getPlotCanvas().getWidth(),
					dDPlotPane.getPlotCanvas().getHeight());
			return;
		}
		drawDataUnit(lastDetection);
	}

	/**
	 * Set which axis are shown on the graph. 
	 * @param axis the axis in order TOP, RIGHT BOTTOM, LEFT. true to show, false to not show. Default is to show. 
	 * This should not be more thna four elements long
	 */
	public void setAxisVisible(boolean top, boolean right, boolean bottom, boolean left ) {
		dDPlotPane.setAxisVisible(top,right,bottom,left); 
	}


	/**
	 * Get a plot pane axis.
	 * @param the axis side
	 * @return the pam axis
	 */
	public PamAxisPane2[] getAllAxisPanes() {
		return dDPlotPane.getAllAxisPanes(); 
	}


	/**
	 * Set the data model to represent the parent currently selected in the display. (Only FX GUI)
	 */
	public void dataModelToDisplay(){
		if (detectionDisplayControl==null) return; 
		detectionDisplayControl.dataModelToDisplay();
	}	

	@Override
	public boolean requestNodeSettingsPane() {
		if (dDPlotPane.getHidePane(Side.RIGHT)!=null) dDPlotPane.getHidePane(Side.RIGHT).showHidePane(true);
		if (dDPlotPane.getHidePane(Side.LEFT)!=null) dDPlotPane.getHidePane(Side.LEFT).showHidePane(true);
		return true;
	}

	/**
	 * Get an axis pane
	 * @param side the axis to get. 
	 */
	public PamAxisPane2 getAxisPane(Side side) {
		return dDPlotPane.getAxisPane(side); 	
	}


	/**
	 * Get the hiding pane. 
	 * @param side - the side. 
	 * @return the hiding pane. 
	 */
	public HidingPane getHidingPane(Side side){
		return dDPlotPane.getHidePane(side);
	}

	@Override
	public boolean isMinorDisplay() {
		// these are generally smaller minor displays- only used for automatic resize. 
		return true;
	}

	/**
	 * Called whenever a new datablock is added. 
	 * @param dataBlock - the data blovck to add
	 */
	public void newDataBlockAdded(PamDataBlock dataBlock) {
		if (detectionDisplayControl==null){
			Debug.err.println("DetectionPlotDisplay: There is no display control associated with the detection display");
		}
		else detectionDisplayControl.newDataBlockAdded(dataBlock);
	}

	/**
	 * Set the minimum height of the hiding side panes. If the detection plot
	 * goes below this height the hiding panes pop out of the display. 
	 * This ensures all controls are easily accessble to the user. 
	 * @param minHeight the minimum height of the hiding pane. 
	 */
	public void setMinHidePaneHeight(double minHeight) {
		dDPlotPane.setMinHidePaneHeight(minHeight);
	}

	/**
	 * Get the settings pane which allows changing of axis and data blocks. 
	 * @return the settings pane for changing axis and data blocks. 
	 */
	public DDDataPane2 getDataTypePane(){
		return this.dataSettingsPane;
	}

	/**
	 * Get the plot pane. This is where all the basic nodes such as axis and
	 * drawing canvas are held. 
	 * @return the plot pane. 
	 */
	public DDPlotPane getPlotPane() {
		return dDPlotPane;
	}

//	/**
//	 * Check whether the settings button is visible
//	 * @return true if enabled
//	 */
//	public boolean isEnableSettingsButton() {
//		return enableSettingsButton;
//	}
//
//	/**
//	 * Set the settings button to be visible or invisible. 
//	 * @param enableSettingsButton - true to enable the settings button and right hiding pane. 
//	 */
//	public void setEnableSettingsButton(boolean enableSettingsButton) {
//		this.enableSettingsButton = enableSettingsButton;
//		if (!enableSettingsButton) {
//			//must ensure the pane is closed or it stays open and blank with show button removed. 
//			dDPlotPane.getHidePane(Side.RIGHT).showHidePane(false, false);
//			//do not use show here as is threaded thus next part of code is called before the pane actually closes. 
//		}
//		//now hide the button. Note: it automatically reappears if showHidePane is called.
//		dDPlotPane.getHidePane(Side.RIGHT).getShowButton().setVisible(enableSettingsButton);
//	}
	
	/**
	 * 
	 * @param enableSettingsButton
	 */
	public void setEnableScrollBar(boolean enableScrollBarPane) {
		enableScrollBar=enableScrollBarPane;
		setupScrollBar();
	}


	/**
	 * Get the projector for the plot. This handles pixel to data conversion and vice versa.  
	 * @return the projector for the plot.
	 */
	public DetectionPlotProjector getDetectionPlotProjector() {
		return detectionPlotProjector;
	}


	@Override
	public UserDisplayNodeParams getDisplayParams() {
		return this.detectionPlotParams;
	}


	@Override
	public void setFrameHolder(PamInternalPane internalFrame) {
		// TODO Auto-generated method stub
	}
	
	
	/**
	 * The pane which holds settings for the the current plot. 
	 * @return the pane which holds settings for the current plot. 
	 */
	public PamBorderPane getSettingsHolder() {
		return settingsHolder;
	}

	/**
	 * Check whether the time scroll bar is enabled. 
	 * @return true if the time scroll bar is enabled. 
	 */
	public boolean isEnableScrollBar() {
		return this.enableScrollBar;
	}



}


