package dataPlotsFX.layout;

import java.util.ArrayList;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.PamScrollObserver;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSplitPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import pamViewFX.fxNodes.pamAxis.PamAxisPane2;
import pamViewFX.fxNodes.pamScrollers.VisibleRangeObserver;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.AcousticScrollerFX;
import pamViewFX.fxStyles.PamStylesManagerFX;
import Layout.PamAxis;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import dataPlotsFX.TDControl;
import dataPlotsFX.TDGraphParametersFX;
import dataPlotsFX.TDParametersFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.overlaymark.TDOverlayAdapter;
import dataPlotsFX.scroller.TDAcousticScroller;
import dataPlotsFX.sound.SoundOutputManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

/**
 * The main display node for showing tdGraphs. Contains time axis, tdGraphs, time axis and control pane. 
 * @author Jamie Macaulay
 * 
 */
public class TDDisplayFX extends PamBorderPane {

	/**
	 * The standard refresh rate in millis
	 */
	public static long STANDARD_REFRESH_MILLIS=50;

	//flags for canvas repaint

	/**
	 * Reference to TDControlFX
	 */
	private TDControl tdControl;

	/**
	 * Array which holds all the tdGraphs for this display. 
	 */
	private ArrayList<TDGraphFX> tdGraphs;

	/**
	 * The time scroller for the display. Controls current position in time. 
	 */
	private TDAcousticScroller timeScrollerFX;

	/**
	 * The time axis
	 */
	private PamAxisFX timeAxis;

	/**
	 * the time axis pane. 
	 */
	private PamAxisPane2 timeAxisPane;

	/**
	 * Time of the last update. 
	 */
	private long lastUpdate; 

	/**
	 * Graph parameters.
	 */
	private TDParametersFX tdParametersFX;

	/*
	 * Pane which holds controls for the display. Hides at the top of the display. 
	 */
	private TDControlPaneFX controlPane;

	/**
	 * Hiding pane which hides controlPane.
	 */
	private HidingPane hidingControlPane;

	/**
	 * The graph holder is a split pane which holds all tdGraphs in the display
	 */
	private PamSplitPane tdGraphHolder;

	/**
	 * Shows/un-hides the control pane. 
	 */
	private PamButton showButton;

	/**
	 * Holds all tdGraphs. The main center pane for the display. 
	 */
	private PamBorderPane graphHolder;

	/**
	 * A pane for holding the time axis and a bit of blank space so it doesn't overlap with the data axis of tdGraphs. 
	 */
	private Pane timeAxisHolder;

	/**
	 * Listener for time scroll bar changing. 
	 */
	private TimeRangeListener timeRangeListener;

	/**
	 * Label which shows the current time stamp based on scroller position.
	 */
	private Label timeStamp;

	/**
	 * Show mouse coordinates in the corner somewhere. 
	 */
	private Label mousePositionData;

	/**
	 * The size of the time axis in pixels i.e it's height when horizontal and width when vertical. 
	 */
	public final static double timeAxisSize=40; 

	/**
	 * The size of the data axis in pixels i.e it's width when horizontal and height when vertical. 
	 */
	public final static double dataAxisSize=50; 

	/**
	 * The height of the control pane. 
	 */
	final static double CONTROL_PANE_HEIGHT=70;


	/**
	 * The time of the last wrap start. This allows the current position of rap to be calculated with scrollStart. 
	 */
	long lastWrap=-1; 

	/**
	 * Because we don't get an update every millisecond, need a wrap factor to stop a slight offset in the display occurring. 
	 */
	long wrapFactor=0;

	/**
	 * True if wrap has been request. False if scroll has been requested. Null if there are no requests. 
	 */
	private Boolean wrapRequest;

	/**
	 * True if the display has been created. 
	 */
	private boolean intilized=false; 


	private final Object lock = new Object();

	/**
	 * Sound output manager. Handles sound playback. 
	 */
	private SoundOutputManager soundOutputManager;

	/**
	 * Holds the axis pane. 
	 */
	private PamSplitPane splitPaneHolder;

	/**
	 * Constructor for the main JavaFX display.
	 * @param tdControl - the TDControlFX. 
	 */
	public TDDisplayFX(TDControl tdControl){
		//reference to tdControl
		this.tdControl=tdControl;

		//set up params
		//these should have been loaded with data. 
		tdParametersFX=tdControl.getTdParameters();

		if (tdParametersFX.graphParameters == null) {
			tdParametersFX.graphParameters = new ArrayList<TDGraphParametersFX>();
		}		

		//		System.out.println("TDDisplay graphs: "+tdParametersFX.graphParameters.size());

		if (tdParametersFX.graphParameters.size() == 0) {
			tdParametersFX.graphParameters.add(new TDGraphParametersFX());
		}

		//create an array to hold the tdGraphs for this display
		tdGraphs=new ArrayList<TDGraphFX>();

		//create pane to hold tdGraphs.
		tdGraphHolder=new PamSplitPane();

		//manages sound output form the graph
		soundOutputManager = new SoundOutputManager(this);

		if (isViewer()) {
			timeRangeListener = new ViewerTimeRanges();
		}
		else {
			timeRangeListener = new NormalTimeRanges();
		}

		//create the time scroller. 
		this.timeScrollerFX=createScroller();

		//create top hiding panel. 
		controlPane=new TDControlPaneFX(tdControl,this);
		controlPane.setParams(tdParametersFX);
		controlPane.setPrefHeight(CONTROL_PANE_HEIGHT);

		hidingControlPane=new HidingPane(Side.TOP, controlPane, this, false );
		hidingControlPane.showHidePane(tdParametersFX.showControl);
		hidingControlPane.getStylesheets().clear();
//		hidingControlPane.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getGUICSS()); //style as a settings pane. 
		hidingControlPane.getHideButton().setStyle("-fx-border-color: none;");

		hidingControlPane.showingProperty().addListener((valProp, oldVal, newVal)->{
			//set correct showing property.
			tdParametersFX.showControl=newVal; 
		});
		this.setTop(hidingControlPane);

		//create the button which shows the hiding panel. Although we get this button from the hiding pane, where to place
		//it and what colour it is etc has to be set for whatever pane it is to be located in. 
		showButton=hidingControlPane.getShowButton();
		hidingControlPane.setShowButtonOpacity(1.0);
//		showButton.getStyleClass().add("transparent-button-square");
		showButton.setStyle("-fx-background-radius: 0 0 0 10; -fx-border-color: none;");
		showButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-down", PamGuiManagerFX.iconSize));
		showButton.setPrefWidth(30);
		showButton.setMaxHeight(timeAxisSize-20);

		//create the time axis for the display. 
		timeAxis = new PamAxisFX(0, 1, 0, 1, 0, tdParametersFX.visibleTimeRange/1000., PamAxis.ABOVE_LEFT, null, PamAxis.LABEL_NEAR_CENTRE, null);
		//timeScrollerFX.getTimeRangeSpinner().setTime(tdParametersFX.visibleTimeRange);
		timeAxis.setCrampLabels(true);

		//create the pane which holds the time axis
		timeAxisPane=new PamAxisPane2(timeAxis, Side.TOP);
		timeStamp=new Label();

		mousePositionData = new Label();

		//layout the main pane
		layoutTDMainPane(tdParametersFX.orientation);

		//create pane to hold all nodes and allow overlaid buttons
		StackPane mainGraphPane=new StackPane();
		mainGraphPane.getChildren().add(splitPaneHolder);
		mainGraphPane.getChildren().add(showButton);
		StackPane.setAlignment(showButton, Pos.TOP_RIGHT);		

		this.setCenter(mainGraphPane);

		createGraphs(); //create the graphs	
		//layout the graphs within the main panel
		layoutTDGraphs(tdParametersFX.orientation);		

		//need to set the divder positon after the graphs as this doesn;t work if set before. 
		splitPaneHolder.setDividerPosition(0, 0.95);

		intilized=true; 
	}


	/**
	 * Create the TDgraphsFX. Called during initialisation. 
	 */
	private void createGraphs() {
		int n = 0;
		if (tdParametersFX.graphParameters != null) {
			for (TDGraphParametersFX gp:tdParametersFX.graphParameters) {
				TDGraphFX aGraph = new TDGraphFX(tdControl, this, n++);
				tdGraphs.add(aGraph);
				aGraph.setGraphParameters(gp);
			}
		}
		timeScrollerFX.setCurrentGraphicsIndex(tdParametersFX.scrollerDataGramIndex);

		//there must always be a graph on start up
		if (tdGraphs.size()==0) tdGraphs.add(new TDGraphFX(tdControl, this, 0));

		//set overlay marker type
		for (TDGraphFX tdGraph: this.getTDGraphs() ){
			tdGraph.getOverlayMarkerManager().setCurrentMarkIndex(tdParametersFX.overlayMarkerType); 
		}

	}


	/**
	 * Add a new graph to the time base display
	 * @return the new TDGraph which has been added. 
	 */
	public TDGraphFX addTDGraph(){
		TDGraphFX newTDGraph= new TDGraphFX(tdControl, this, tdGraphs.size());
		tdGraphs.add(newTDGraph);
		//set overlay marker type
		newTDGraph.getOverlayMarkerManager().setCurrentMarkIndex(tdParametersFX.overlayMarkerType); 
		layoutTDGraphs(tdParametersFX.orientation);

		return newTDGraph;
	}


	/**
	 * Remove one of the current graphs from the time display
	 * @param iGraph- graph to remove.
	 */
	public void removeTDGraph(int iGraph){
		if (iGraph>=0 && iGraph<tdGraphs.size()){
			tdGraphs.remove(iGraph);
			layoutTDGraphs(tdParametersFX.orientation);
		}
		else System.err.println("Error: TDMainDdiplay.removeTdGraph(int iGraph): attempting to remove a graph that does not exist.");
	}


	/**
	 * Layout the main display including graph axis, scroll bars, all tdGraphs etc.
	 * Generally only called if the orientation is changed. 
	 */
	private void layoutTDMainPane(Orientation orientation){

		if (graphHolder==null) graphHolder= new PamBorderPane();
		if (splitPaneHolder==null) {
			splitPaneHolder=new PamSplitPane(); 
			Node divider = splitPaneHolder.lookup(".split-pane-divider");
			if(divider!=null){
				divider.setStyle("-fx-background-color: transparent;");
			}
		}

		//clear the children 
		splitPaneHolder.getItems().clear(); 
		graphHolder.getChildren().clear();

		//create panel to hold graphs and time axis plus scroll bars
		graphHolder.setCenter(tdGraphHolder); 

		//remove time stamp as in different place if vertical
		timeAxisPane.getChildren().remove(timeStamp);
		timeAxisPane.getChildren().remove(mousePositionData);

		if (tdParametersFX.orientation==Orientation.HORIZONTAL){

			splitPaneHolder.setOrientation(Orientation.VERTICAL);

			//layout for a horizontal display
			timeAxisHolder=new PamHBox();
			timeAxisHolder.setPrefHeight(timeAxisSize);
			timeAxisPane.setOrientation(tdParametersFX.orientation == Orientation.HORIZONTAL ? Side.TOP : Side.RIGHT);

			//			//add time stamp to axis if in horizontal mode.
			timeAxisPane.getChildren().add(timeStamp);
			StackPane.setAlignment(timeStamp, Pos.TOP_LEFT);
			setTimeStamp();

			timeAxisPane.getChildren().add(mousePositionData);
			StackPane.setAlignment(mousePositionData, Pos.TOP_CENTER);

			timeAxisHolder.getChildren().add(timeAxisPane);
			PamHBox.setHgrow(timeAxisPane, Priority.ALWAYS);
			PamVBox.setVgrow(timeAxisPane, Priority.SOMETIMES);

			layoutTimeAxisHolder(orientation);

			graphHolder.setTop(timeAxisHolder); 

			//timeScrollerFX - set correct orientation for scroller. 
			timeScrollerFX.setOrientation(orientation);

			SplitPane.setResizableWithParent(timeScrollerFX.getNode(), false); 
			splitPaneHolder.getItems().addAll(graphHolder, timeScrollerFX.getNode()); 
			//graphHolder.setBottom(timeScrollerFX.getNode()); 
		}
		else {

			//layout for a vertical display
			timeAxisHolder=new PamVBox();
			timeAxisHolder.setPrefWidth(timeAxisSize);
			timeAxisPane.setOrientation(tdParametersFX.orientation == Orientation.HORIZONTAL ? Side.TOP : Side.RIGHT);

			timeAxisHolder.getChildren().add(timeAxisPane);
			PamVBox.setVgrow(timeAxisPane, Priority.ALWAYS);
			PamHBox.setHgrow(timeAxisPane, Priority.SOMETIMES);

			layoutTimeAxisHolder(orientation);

			graphHolder.setLeft(timeAxisHolder); 

			//timeScrollerFX - set correct orientation for scroller. 
			timeScrollerFX.setOrientation(orientation);
			graphHolder.setRight(timeScrollerFX.getNode()); 

		} 

		//Set background so that same as the axis colour-fills in box on corner between x and y axis. 
		timeAxisHolder.getStyleClass().add("pane");

		//layout the graphs within the main panel
		layoutTDGraphs(tdParametersFX.orientation); 
	}


	/**
	 * Lays out the time axis holder, making sure the time axis does not overlap the data axis (there must be a blank square in the corner between the data axis and time axis). 
	 * This must be called anytime the data axis changes, e.g. when a new graph
	 * is added to the display.
	 */
	private void layoutTimeAxisHolder(Orientation orientation){
		double shiftVal;
		if (orientation==Orientation.HORIZONTAL){
			//get the value 
			if (tdGraphs.size()>0) shiftVal= tdGraphs.get(0).getGraphAxisPane().getPrefWidth();
			else shiftVal=0;
			timeAxisHolder.setPadding(new Insets(0,0,0,shiftVal));
		}
		else{
			if (tdGraphs.size()>0) shiftVal= tdGraphs.get(0).getGraphAxisPane().getPrefHeight();
			else shiftVal=0;
			timeAxisHolder.setPadding(new Insets(shiftVal,0,0,0));
		}
	}


	/**
	 * Layout the tdGraphs in the graph holder pane. 
	 */
	private void layoutTDGraphs(Orientation orientation){
		/*For Split pane*/
		//remove all nodes from the grid pane.
		tdGraphHolder.getItems().removeAll(tdGraphHolder.getItems());
		//add tdGraphs to the gridPane.
		/*For Split Pane*/
		//need to swap orientation as what we call vertical is HORIZONTAL in split pane.
		if (orientation==Orientation.HORIZONTAL) tdGraphHolder.setOrientation(Orientation.VERTICAL);
		else tdGraphHolder.setOrientation(Orientation.HORIZONTAL);


		for (int iPanel =0; iPanel<tdGraphs.size(); iPanel++){
			tdGraphs.get(iPanel).layoutTDGraph(orientation);
			tdGraphHolder.getItems().add(tdGraphs.get(iPanel)); 
			//			tdGraphHolder.setDividerPosition(0,1.0/tdGraphs.size());
		}

		if (tdGraphs.size() > 1) {
			TDParametersFX params = tdControl.getTdParameters();
			double[] divPos = params.splitHeights;
			if (divPos == null || divPos.length != tdGraphs.size()-1) {
				divPos = new double[tdGraphs.size()-1];
				for (int i = 0; i < divPos.length; i++) {
					divPos[i] = (double) (i+1)/(double) tdGraphs.size();
				}
			}
			tdGraphHolder.setDividerPositions(divPos);
		}

		//make sure time axis is spaced correctly. 
		layoutTimeAxisHolder(orientation);

		/*For Grid pane*/
		//		int row=0;
		//		int column=0;
		//		for (int i=0; i<tdGraphs.size(); i++){
		//			if (tdParams.orientation==Orientation.HORIZONTAL) row=i;
		//			else column=i;
		//				tdGraphHolder.add(tdGraphs.get(i), column, row);
		//		}
		//		//now sort out distribution of rows and column
		//		sortGridContraints(tdGraphHolder, tdParams.orientation, tdGraphs.size());

		//controlPane.refreshGraphButtonPane();

	}


	/**
	 * Function to distribute spacing in a grid pane. Means rows, column take up 100% of the available space available to 
	 * the grid pane. This function sets the column and row constraints so that each Node takes up an equal share of the column or row spacing depending
	 * on orientation.  
	 * @param gridPane- grid pane to sort column and row constraints for. 
	 * @param orientation- Orientation of the grid pane.  
	 * @param nPanes- number of panes within the grid pane to divide spacing between
	 */
	public static void sortGridContraints(GridPane gridPane, Orientation orientation, int nPanes){
		//remove all previous constraints
		gridPane.getColumnConstraints().removeAll(gridPane.getColumnConstraints()); 
		gridPane.getRowConstraints().removeAll( gridPane.getRowConstraints());  

		//weight the panel so it gets and equal share of column and row space in the grid. 
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(100);
		RowConstraints row1 = new RowConstraints();
		row1.setPercentHeight(100);

		//now distribute the column spacing. If we add tow column contraints with 100% then each column takes up 50%. 
		gridPane.getRowConstraints().add(row1);
		gridPane.getColumnConstraints().add(column1);
		for (int i=1; i<nPanes; i++){
			if (orientation.equals(Orientation.HORIZONTAL))  gridPane.getRowConstraints().add(row1);
			else gridPane.getColumnConstraints().add(column1);
		}

	}


	/**
	 * Rotate the plot to be vertical->horizontal or horizontal->vertical. (depends on which one the display is currently). 
	 * This is not a simple node rotation by 90 degrees- the display layout is changed to be used as vertical/horizontal. 
	 */
	public void rotate(){
		//only two options-either horizontal or vertical. Therefore change orientation to other possibility and proceed.
		tdParametersFX.orientation = (tdParametersFX.orientation==Orientation.HORIZONTAL) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
		layoutTDMainPane(tdParametersFX.orientation);
		repaintAll();
	}

	/**
	 * Called whenever the show settings pane  button is pressed. 
	 * @author Jamie Macaulay
	 */
	class ShowTopPressed implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent arg0) {
			hidingControlPane.showHidePane(true);
			showButton.setVisible(false);
		}
	}


	//	/**
	//	 * Panel which holds the time axis. 
	//	 * @author Jamie Macaulay
	//	 */
	//	public class TimeAxisPane extends PamAxisPane {
	//
	//		public TimeAxisPane() {
	//			super(timeAxis, Orientation.HORIZONTAL);
	//			
	//			//this.getStyleClass().add("pane");
	//		}
	//
	//		@Override
	//		public void paintHorizontal(Canvas canvas){
	//			//System.out.println("Paint Time Axis Horizontal");
	//			timeAxis.drawAxis(canvas.getGraphicsContext2D(), 0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight());
	//		}
	//
	//		@Override
	//		public void paintVertical(Canvas canvas){
	//			//System.out.println("Paint Time Axis Vertical");
	//			timeAxis.drawAxis(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight(), canvas.getWidth(),0);
	//		}
	//
	//	}


	/**
	 * Sets the the time stamp label to correct time based on scroller position.  
	 */
	private void setTimeStamp(){
		if (timeStamp==null) return; 
		String timeString = PamCalendar.formatDBDateTime(getTimeScroller().getValueMillis(), true);
		timeStamp.setText(timeString);
	}


	/**
	 * Subscribe data blocks to the time scroller.
	 * <br> seems like a daft call sequence, but the time
	 * scroller will only hold one copy of each block, so 
	 * if a block is removed, it may be that it shouldn't be 
	 * removed - so easer just to remake the entire 
	 * list every time 
	 */
	public void subscribeScrollDataBlocks() {
		timeScrollerFX.removeAllDataBlocks();
		for (TDGraphFX aGraph:tdGraphs) {
			aGraph.subscribeScrollDataBlocks(timeScrollerFX);
		}
	}

	/**
	 * Called whenever there is a new master clock update. 
	 * @param milliSeconds - the master clock position. 
	 */
	public void scrollDisplayEnd(long milliSeconds) {
		

		if (!isViewer() && lastUpdate>0 && milliSeconds <= lastUpdate && milliSeconds > lastUpdate - tdParametersFX.visibleTimeRange) {
//			System.out.println("milliSeconds <= lastUpdate && milliSeconds > lastUpdate - visibleRange");
			return;
		}

		//		long scrollPos = milliSeconds + 1000;//(long) (timeRangeSpinner.getSpinnerValue()*1000 * 0.95);

		/**
		 * So the display is updated in real time, but in reality there may be a bit of a delay sometimes. 
		 */
		//		long gap = Math.max(-100, -tdParametersFX.visibleTimeRange/10); 
		//		gap=0; 
		long scrollEnd = milliSeconds;// + (long) (timeRangeSpinner.getSpinnerValue()*1000);


		if (!isViewer() && !needPaused()) {
			//so one of the annoying things abut using so many different data streams is that they are all 
			//on different threads and so all updated at slightly different times. PAMGuard has a master clock
			//which the scroller in the display uses but  most of the data blocks have a processing lag 
			//which leads to a gap. Usually this gap is OK but for continuous data such as FFT or raw waveform 
			//it can be very annoying. So some TDDataInfos which stream continuous data have their own clock update
			//which overrides the master clock update and makes the display look nicer in real time operations. 
			//Obviously this should be treater with caution@
			for (int i=0; i<this.tdGraphs.size(); i++) {
				for (int j=0; j<this.tdGraphs.get(i).getDataList().size(); j++) {
					if (this.tdGraphs.get(i).getDataList().get(j).getMasterClockOverride()>0) {
						scrollEnd = this.tdGraphs.get(i).getDataList().get(j).getMasterClockOverride();
					}
				}
			}
		}


		long scrollStart = scrollEnd - (timeScrollerFX.getMaximumMillis()-timeScrollerFX.getMinimumMillis());//getDefaultLoadtime();

		long scrollPos;
		if (this.needPaused()){
			scrollPos=getTimeScroller().getValueMillis(); 
		}

		else {
			scrollPos= scrollEnd - (long) (tdParametersFX.visibleTimeRange);
		}



		//set wrapping if it has been requested. Only ever want to do that here 
		//as setting wrap in the middle of a repaint is NOT a good and causes freezes. 
		if (wrapRequest !=null ){
			tdParametersFX.wrap=wrapRequest; 
			wrapRequest=null; 
		}


		if (!tdParametersFX.wrap){
			//set wrapping to -1- resets if wrapping is called again. 
			lastWrap=-1; 
		}
		else if ((scrollEnd-lastWrap)>tdParametersFX.visibleTimeRange || lastWrap==-1) {
			//set wrapping to start of display
			if (lastWrap==-1) lastWrap=scrollEnd; 
			else{

				wrapFactor=(scrollEnd-lastWrap)-tdParametersFX.visibleTimeRange;

				//System.out.println("Wrap comp factor "+wrapFactor +  " 1 pixel: "+this.timeAxis.getDataValue(1));
				//FIXME? for some reason need to offset by wrap factor and two pixels
				lastWrap=(long) (scrollEnd-wrapFactor-this.timeAxis.getDataValue(2)*1000);
			}
		}

		lastUpdate = milliSeconds;
		//		System.out.println("timeScrollerFX.getMaximumMillis() "+timeScrollerFX.getMaximumMillis()+" timeScrollerFX.getMinimumMillis() "+
		//						timeScrollerFX.getMinimumMillis()+" "+timeScrollerFX.getObservers().size()+ " "+timeScrollerFX.getNumUsedDataBlocks()+" vis amount "+timeScrollerFX.getVisibleAmount()
		//						+" rangeMillis(): "+timeScrollerFX.getRangeMillis());
		//		System.out.println(String.format("ScrollerFX set start %s, End %s, pos %s pc time %s", PamCalendar.formatTime(scrollStart),
		//		PamCalendar.formatTime(scrollEnd),PamCalendar.formatTime(scrollPos), PamCalendar.formatTime(System.currentTimeMillis())));
		getTimeScroller().setRangeMillis(scrollStart, scrollEnd, true);
		getTimeScroller().setVisibleMillis(tdParametersFX.visibleTimeRange);
		getTimeScroller().setValueMillis(scrollPos);

		setTimeStamp();

		repaintAll(STANDARD_REFRESH_MILLIS);

	}

	/**
	 * Cycles through all the time data graphs and updates the plot display. 
	 * @param tm - if this paint within tm millis of last paint do not repaint.
	 * @param flag - a canvas repaint flag e.g. TDPlotPane.FRONT_CANAVAS.  
	 */
	public void repaintAll(long tm, int flag){
		if (tdGraphs!=null) {
			for (TDGraphFX tdGraph:tdGraphs){
				tdGraph.repaint(tm, flag);
			}
		}
	}

	/**
	 * Cycles through all the time data graphs and updates the plot display. 
	 * @param tm - if this paint within tm millis of last paint do not repaint.
	 */
	public void repaintAll(long tm){
		if (tdGraphs!=null) {
			for (TDGraphFX tdGraph:tdGraphs){
				tdGraph.repaint(tm, TDPlotPane.ALL_CANVAS);
			}
		}
	}

	/**
	 * Cycles through all the time data graphs and updates the plot display. 
	 * @param flag - a canvas repaint flag e.g. TDPlotPane.FRONT_CANAVAS.  
	 */
	public void repaintAll(int flag){
		repaintAll(0, flag);
	}

	/**
	 * Cycles through all the time data graphs and updates the plot display. 
	 */
	public void repaintAll(){
		repaintAll(0, TDPlotPane.ALL_CANVAS);
	}

	/**
	 * Create the old scroller, destroying an old one if it exists. 
	 * @return a PamScrollerFX for the time axis of the data plot.
	 */
	public TDAcousticScroller createScroller() {

		TDAcousticScroller oldScroller = timeScrollerFX;

		/**Create the scroll bar**/
		timeScrollerFX = new TDAcousticScroller("Time display", tdParametersFX.orientation, 100, 120000L, true, this);

		//System.out.println("Time Scroller: " + timeScrollerFX.getRangeMillis()  + "   " + tdParametersFX.scrollableTimeRange);

		//repaint the graph when the scroller has finished moving
		timeScrollerFX.scrollerChangingProperty().addListener((obsVal, oldVal, newVal)->{
			//only repaint if the scroller has stopped moving. 
			if (!newVal && (newVal!=oldVal)){
				this.repaintAll(10);
				oldVal=newVal; 
			}
		});

		/**Set listeners for scroll bar value and visible range changes**/
		//add time range listener which is triggered when scroll bar changes. 
		timeScrollerFX.addObserver(timeRangeListener); //PamScrollObserver
		//add time range listener for spinner changes.  
		timeScrollerFX.addRangeObserver(timeRangeListener); //RangeObserver

		/**Set the initial values**/
		//set the visible amount i.e. how much is shown on screen
		timeScrollerFX.setVisibleMillis(tdParametersFX.visibleTimeRange); 
		if (timeScrollerFX.getVisibleMillis()==0)  timeScrollerFX.setVisibleMillis(200); //don't let visible amount be zero 


		//		//set the range of currently loaded data- this isn't necessarily all shown on screen but can be scrolled through rapidly. 
		//		System.out.println("SCROLLAR CREATION RANGE: "+PamCalendar.formatDateTime(tdParametersFX.startMillis) + 
		//				" "+PamCalendar.formatDateTime(tdParametersFX.scrollableTimeRange+tdParametersFX.startMillis) 
		//				+ " visible "+ tdParametersFX.visibleTimeRange);

		//TODO - these should really be doubles...
		// 16/02/2017 Do NOT notify other scroller of a change here or
		if (!isViewer()) timeScrollerFX.setRangeMillis((long) tdParametersFX.startMillis, (long) (tdParametersFX.scrollableTimeRange+tdParametersFX.startMillis), false); 

		//everything sets to 1970. 
		if (oldScroller != null) {
			timeScrollerFX.setRangeMillis(oldScroller.getMinimumMillis(), oldScroller.getMaximumMillis(), true);
			timeScrollerFX.setValueMillis(oldScroller.getValueMillis());
			timeScrollerFX.setPageStep(oldScroller.getPageStep());
			timeScrollerFX.setBlockIncrement(oldScroller.getBlockIncrement());
			timeScrollerFX.setVisibleMillis(oldScroller.getVisibleMillis());
			int ndb = oldScroller.getNumUsedDataBlocks();
			for (int i = 0; i < ndb; i++) {
				timeScrollerFX.addDataBlock(oldScroller.getUsedDataBlock(i));
			}
			oldScroller.destroyScroller();
		}

		return timeScrollerFX;
	}

	/**
	 * Abstract class for listening to the time scroll bar. 
	 * @author Jamie Macaulay
	 *
	 */
	private abstract class TimeRangeListener implements PamScrollObserver , VisibleRangeObserver {

		/**
		 * Called from the range spinner. 
		 */
		@Override
		public void visibleRangeChanged(long oldValue, long newValue) {

			//System.out.println("Visible Range value: "+newValue);

			if (oldValue==newValue) return; 
			//			System.out.println("TDDisplayFX: Range Value changed: " +  System.currentTimeMillis());

			//timeScrollerFX.setVisibleAmount(newValue); 

			//set in params
			tdParametersFX.visibleTimeRange = newValue;

			//now change axis
			setTimeAxisRange(); 


			for (TDGraphFX aGraph: tdGraphs) {
				aGraph.timeRangeSpinnerChange(oldValue, newValue);
			}

			repaintAll(STANDARD_REFRESH_MILLIS); //here 50 because scrollValueChanged is also called. 

		}
	}

	/**
	 * Listens for the scroll bar changing. 
	 * @author Doug Gillespie, Jamie Macaulay
	 *
	 */
	private class NormalTimeRanges extends TimeRangeListener {


		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			//			System.out.println(String.format("Scroller value changed get start %s, End %s, pos %s", PamCalendar.formatTime(timeScrollerFX.getMinimumMillis()),
			//					PamCalendar.formatTime(timeScrollerFX.getMaximumMillis()),PamCalendar.formatTime(timeScrollerFX.getValueMillis())));
			//have to set the repaint wait millis param to zero or else when scroll bar is moved quickly painting does not occur correctly. 
			//may not be on fx thread
			Platform.runLater(()->{
				setTimeStamp();
				repaintAll(TDDisplayFX.STANDARD_REFRESH_MILLIS);
			});
		}
		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			//may not be on fx thread
			Platform.runLater(()->{
				setTimeStamp();
				repaintAll(0);
			});
		}


	}

	/**
	 * Listens for the scroll bar changing in viewer mode. 
	 * @author Doug Gillespie, Jamie Macaulay
	 *
	 */
	private class ViewerTimeRanges extends TimeRangeListener {

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			tdParametersFX.scrollStartMillis = ((AcousticScrollerFX) pamScroller).getValueMillisD();
			Platform.runLater(() -> {	
				//timeAxisPane.layout();//force layout just in case.
				for (TDGraphFX aGraph:tdGraphs) {
					aGraph.timeScrollValueChanged(((AcousticScrollerFX) pamScroller).getValueMillisD());
				}
				setTimeStamp();
				repaintAll(STANDARD_REFRESH_MILLIS);
			});
		}


		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			Platform.runLater(() -> {
				//TODO convert to fx thread- need to remove once PAMGUARD is JavaFX

				//				System.out.println("TDDisplayFX: Scroll Range changed: " +  System.currentTimeMillis());
				//				System.out.println(String.format("Scroller range changed get start %s, End %s, pos %s visible %d", 
				//				PamCalendar.formatTime(timeScrollerFX.getMinimumMillis()),
				//				PamCalendar.formatDateTime(timeScrollerFX.getMaximumMillis()),PamCalendar.formatDateTime(timeScrollerFX.getValueMillis()), tdParametersFX.visibleTimeRange));
				timeScrollerFX.setRangeMillis(timeScrollerFX.getMinimumMillis(), timeScrollerFX.getMaximumMillis(), false);
				//timeAxisPane.layout();
				for (TDGraphFX aGraph:tdGraphs) {
					aGraph.timeScrollRangeChanged(pamScroller.getMinimumMillis(), pamScroller.getMaximumMillis());
				}
				setTimeStamp();
				repaintAll(0);
			});
		}

	}

	/**
	 * Set the time axis range based on visible range. 
	 */
	private void setTimeAxisRange(){
		if (timeAxis!=null){
			timeAxis.setRange(0, tdParametersFX.visibleTimeRange/1000.);
			timeAxisPane.layout();
			//timeAxisPane.repaint();
		}
	}

	/**
	 * Get the time scroller. This is essentially the GUI for controlling the time axis of the graph
	 * @return the time scroller. 
	 */
	public TDAcousticScroller getTimeScroller() {
		return timeScrollerFX;
	}

	/**
	 * The time axis. This controls where data units are painted on the x (horizontal) axis of every tdGraph. Holds calculations
	 * for converting between time and pixel location.  
	 * @return time axis
	 */
	public PamAxisFX getTimeAxis() {
		return timeAxis;
	}

	/**
	 * Index of time axis = 0 if time is horizontal, 1 if time is vertical. 
	 * @return 0 fr hirizontal axis, 1 for vertical axis. 
	 */
	public int getTimeAxisIndex() {
		switch(tdParametersFX.orientation) {
		case HORIZONTAL:
			return 0;
		case VERTICAL:
			return 1;
		default:
			return 0;		
		}
	}

	/**
	 * Reference to tdControl. 
	 * @return get tdControl. 
	 */
	public TDControl getTDControl() {
		return tdControl;
	}

	/**
	 * Get the parameters for the tdGraph. 
	 * @return the parameters class for the graph.
	 */
	public TDParametersFX getTDParams() {
		return tdParametersFX;
	}

	/**
	 * Get the visible range of the time axis in millis. 
	 * @return the visible range i.e. what can been seen on the graph in millis
	 */
	public long getVisibleTime() {
		return this.tdParametersFX.visibleTimeRange; 
	}

	/**
	 * Get the number of pixels per millisecond
	 */
	public double getTimePixPerMillis(){
		return getTimePixels()/timeScrollerFX.getVisibleMillis();
	}

	/**
	 * Get the scrollable range i.e the total amount of loaded data that can be scrolled through
	 * @return the scrollable range range i.e. what range if data is loaded in memory and can 
	 * be scrolled through
	 * */
	public long getScrollableTime() {
		return timeScrollerFX.getRangeMillis();
	}

	/**
	 * Get the satrt time of the scroller. 
	 * @return the start time of the scroller 
	 */
	public long getTimeStart() {
		return timeScrollerFX.getValueMillis();
	}


	/**
	 * Get the length of the time axis in pixels. 
	 * @return the length of the axis in pixel. 
	 */
	public double getTimePixels(){
		double pixelVal;
		if (tdParametersFX.orientation==Orientation.HORIZONTAL){
			pixelVal=timeAxisPane.getWidth();
		}
		else pixelVal=timeAxisPane.getHeight();

		return pixelVal; 
	}

	/**
	 * Get all TDGraphFX in the td display
	 * @return an array of the current tdgraphs displayed
	 */
	public ArrayList<TDGraphFX> getTDGraphs() {
		return tdGraphs;
	}

	public ArrayList<String> getCSSSettingsResource() {
		return PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS();
	}


	/**
	 * Get the time range listener. This listens for when scroll bar values change. 
	 * @return the time range listener for the scroll bar. 
	 */
	public TimeRangeListener getTimeRangeListener() {
		return timeRangeListener;
	}


	/**
	 * Check whether the display is being use din viewer mode or not. 
	 * @return true if running in viewer mode. 
	 */
	public boolean isViewer(){
		return (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
	}

	/**
	 * Add a data block to the display. Creates a new TDgraph with a new TDDataInfoFX containing the data block.
	 * This is generally called externally. 
	 * @param tdGraph - the tdGraph to add the data block to. If null, then the datablock will be added to a new TDGraphFX. 
	 */
	public boolean addDataBlock(PamDataBlock dataBlock, TDGraphFX tdGraph) {
		//		System.out.println("Add datablock: " + dataBlock.getDataName());
		//first get the list of dataInfos- this will c
		TDDataProviderFX newDataProviderFX=TDDataProviderRegisterFX.getInstance().findDataProvider(dataBlock);

		if (newDataProviderFX==null){
			if (dataBlock!=null) System.out.println("TDDisplayFX: could not find a TDDataPorviderFX for the datablock: "+dataBlock.getDataName());
			return false; 
		}

		//now new add data block
		if (tdGraph==null){
			//first check there no TDgraph which is empty, i.e. has no data block
			TDGraphFX newTDGraphFX=null; 

			//now check there no TDgraph which is empty, i.e. has no data block
			//			System.out.println("No. TDGraphs: "+tdGraphs.size());

			for (int i=0; i<this.tdGraphs.size(); i++){
				//				System.out.println("List size: " + tdGraphs.get(i).getDataList().size());
				if (tdGraphs.get(i).getDataList().size()==0){
					newTDGraphFX=tdGraphs.get(i);
					break; 
				}
			}

			//			//if there is no empty graph then add new tdGraph. 
			//			if (newTDGraphFX==null){
			//				newTDGraphFX=addTDGraph();
			//			}

			//decided to add to first TDgraph for simplicity
			if (newTDGraphFX==null && tdGraphs.size()>0){
				newTDGraphFX=tdGraphs.get(0); 
			}
			else if (newTDGraphFX==null) {
				newTDGraphFX=addTDGraph();
			}

			newTDGraphFX.addDataItem(newDataProviderFX);
		}
		else tdGraph.addDataItem(newDataProviderFX);

		//update settings pane
		//this.controlPane.refreshGraphButtonPane();

		return true; 
	}
	//

	/**
	 * Remove a TDDataInfoFX from the display.
	 * @param- true to remove graph if it no longer contains any data blocks. 
	 * @param tdDataInfoFX - the TDDataInfoFX to remove. 
	 */
	public synchronized void removeTDDataInfo(TDDataInfoFX tdDataInfoFX, boolean remove) {
		if (tdGraphs.size()>=0){
			for (int i=0; i<this.tdGraphs.size(); i++){
				if (i>=0 && tdGraphs.get(i).getDataList().size()>=0){
					//						System.out.println(" i: "+i + " tdGraphs.get(i).getDataList().size() "+tdGraphs.size());
					//						System.out.println(" i: "+i + " tdGraphs.get(i).getDataList().size() "+tdGraphs.get(i).getDataList().size());
					ArrayList<TDDataInfoFX> dataList=tdGraphs.get(i).getDataList();
					for (int j=0; j<dataList.size(); j++){
						//							System.out.println(" j: "+j+" tdGraphs.get(i).getDataList().size(): "+tdGraphs.get(i).getDataList().size());
						if (j>=0 && dataList.get(j)==tdDataInfoFX){
							//remove this 
							tdGraphs.get(i).removeDataItem(j);
							j--;
							if (dataList.size()<=0 && remove){
								this.removeTDGraph(i);
								i--; 
							}
						}
					}
				}
			}
		}
		//update settings pane. 
		//this.controlPane.refreshGraphButtonPane();
	}

	/**
	 * Get the control pane. This contains general settings controls for the entire display. 
	 * @return the control pane. 
	 */
	public TDControlPaneFX getControlPane() {
		return controlPane;
	}

	/**
	 * Show or hide the control pane. The control pane contains general settings controls for the entire display. 
	 * @param show true to show the control pane., 
	 */
	public void showControlPane(boolean show) {
		hidingControlPane.showHidePane(show);
	}


	/**
	 * Notifications passed on from controlled unit. 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		switch (changeType){
		case PamController.INITIALIZATION_COMPLETE:
			//			System.out.println(" INITIALIZATION_COMPLETE ");
			//need to have this here so TDMainDisplay is not calling itself through it's own constructor. 
			//			if (!isViewer()){
			//				createGraphs(); //create the graphs	
			//				//layout the graphs within the main panel
			//				layoutTDGraphs(tdParametersFX.orientation);		
			//			}

			break; 
		case PamController.INITIALIZE_LOADDATA:
			//			System.out.println(" INITIALIZE_LOADDATA ");
			//this is called in viewer mode 
			//			createGraphs(); //create the graphs	
			//			//layout the graphs within the main panel
			//			layoutTDGraphs(tdParametersFX.orientation);			
			break;
		case PamController.PAM_IDLE:
			//must no be in wrap mode if paused or stopped
			//			this.repaintAll();
			break; 
		case PamController.CHANGED_PROCESS_SETTINGS:
			break;
		}

		//forces a repaint after any changes. 
		lastUpdate=-1;
		
		if (tdGraphs != null) {
			for (TDGraphFX tdg:tdGraphs) {
				tdg.notifyModelChanged(changeType);
			}
		}
		
	}

	/**
	 * Get the last wrap time in millis. This is essentially time 0 in a wrapped display. 
	 * @return the last wrap time in millis. 
	 */
	public long getWrapLastMillis() {
		return this.lastWrap;
	}

	/**
	 * Convenience class to get the total millis the wrap has progressed since the lastWrap occurred. 
	 * @param scrollstart. The last visible scroll time. i.e. current scrolltime minus visible amount. 
	 * @return the time in millis since the last wrap
	 */
	public long getWrapMillis(long scrollStart) {
		return scrollStart+timeScrollerFX.getVisibleMillis()-lastWrap;
	}

	/**
	 * Convenience class to determine whether the wrap boolean has been to set to true or false in settings. 
	 * @return true if the display is in wrap mode. 
	 */
	public boolean isWrap() {
		return this.tdParametersFX.wrap;
	}


	/**
	 * Get the current wrap position in pixels. 
	 * @return the position of the wrap in pixels. 
	 */
	public double getWrapPix() {
		return this.timeAxis.getPosition(getWrapMillis(getTimeScroller().getValueMillis())/1000.); 
	}

	/**
	 * Check whether PG is running in real time. 
	 * @return true if running in real time
	 */
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return this.tdControl.isRunning();
	}


	/**
	 * Returns a new list of overlay marker adapters
	 * @returns
	 */
	public ArrayList<TDOverlayAdapter> getOverlayMarkerAdpaters(TDGraphFX tdGraphf) {
		return tdGraphf.getOverlayMarkerManager().getOverlayMarkers(); 
	}


	public PamSplitPane getSplitPane() {
		return tdGraphHolder;
	}

	/**
	 * A change in wrap mode has been requested. 
	 * <p>
	 * Note: This is used rather than changing the params directly 
	 * from the wrap control button becuase that can cause a crash if repaitn is being called. 
	 * Only in real time mode
	 * @param selected - true to request wrapping. False otherwise. 
	 */
	public void setWrapRequest(boolean selected) {
		wrapRequest=selected; 
	}

	/**
	 * Check whether the display is paused or not. 
	 * @return true if the display is paused. 
	 */
	public boolean needPaused() {

		if (this.controlPane.isPaused()) {
			//			System.out.println("TDGraph: isPaused ControlPane:");
			return true;
		}
		if (tdGraphs == null) {
			return false;
		}
		for (TDGraphFX tdGraph:tdGraphs) {
			if (tdGraph.needPaused()) {
				//				Debug.out.println("TDGraph: isPaused TDGraphFX:");
				return true;
			}
		}

		return false;
	}


	public boolean initializationComplete() {
		// TODO Auto-generated method stub
		return this.intilized; 
	}

	/**
	 * Zoom into the display. Either zoom to a current overlay mark or zoom in on the time axis a pre defind amount; 
	 * @param b - to zoom in or out. True to zoom in. 
	 */
	public void zoomDisplay(boolean b) {
		for (int i=0; i<this.tdGraphs.size(); i++){
			if (tdGraphs.get(i).zoomGraph(b)) return; 
		}

		//so none of the graphs was able to zoom. Zoom in to the time axis by a predefined amount
		if (b){
			this.getTimeScroller().setVisibleMillis(getTimeScroller().getVisibleMillis()/2);
		}
		else {
			this.getTimeScroller().setValueMillis(this.getTimeScroller().getMinimumMillis());
			this.getTimeScroller().setVisibleMillis(getTimeScroller().getRangeMillis()-10);
		}

	}


	public double[] getSplitHeights() {
		if (tdGraphHolder == null) {
			return null;
		}
		return tdGraphHolder.getDividerPositions();
	}

	/**
	 * @return the soundOutputManager
	 */
	public SoundOutputManager getSoundOutputManager() {
		return soundOutputManager;
	}


	/**
	 * @return the mousePositionData
	 */
	public Label getMousePositionData() {
		return mousePositionData;
	}


	public void playbackUpdate() {
		// need to launch this on the fx thread or all hell will break loose. 
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (TDGraphFX tdg:tdGraphs) {
					tdg.playbackUpdate();
				}
			}
		});
	}

}
