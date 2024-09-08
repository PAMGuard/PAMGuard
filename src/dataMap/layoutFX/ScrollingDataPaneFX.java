package dataMap.layoutFX;

import java.util.ArrayList;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamUtils.PamCalendar;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import PamguardMVC.PamDataBlock;
import dataMap.DataMapControl;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamColorsFX;
import pamViewFX.fxNodes.PamScrollPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamAxis.PamDateAxis;

public class ScrollingDataPaneFX extends PamBorderPane {

	/**
	 * Standard millis to wait for repaint. 
	 * Do not make this too high i.e. above 50 or the display gets very jerky
	 */
	public static final long REPAINTMILLIS = 50;

	/**
	 * The default expanded hieght for each pane. 
	 */
	private static final int DATASTREAMPANE_HEIGHT = 220;

	/**
	 * Reference to the DataMapControl.
	 */
	private DataMapControl dataMapControl;

	/**
	 * Reference to the DataMapPaneFX.
	 */
	private DataMapPaneFX dataMapPaneFX;

	/**
	 * The scroll pane everything sits in
	 */
	private PamScrollPane mainScrollPane;

	/**
	 * List of panes- each shows an individual data stream. 
	 */
	private ArrayList<DataStreamPaneFX> dataStreamPanels = new ArrayList<DataStreamPaneFX>();

	/**
	 * Split pane which holds different graphs. 
	 */
	private PamVBox dataPanePanes;

	private ArrayList<OfflineDataStore> offlineDataStores;

	/**
	 * Time stamp in millis of start of datamap display
	 */
	private long screenStartMillis = -1;

	/**
	 * Time stamp in millis of end of datamap display
	 */
	private long screenEndMillis = -1;

	private double screenSeconds;

	/**
	 * Scroll bar for time (horizontal)
	 */
	private DataMapScrollBar timeScrollBar;

	/**
	 * Shows the start time of the scroll position
	 */
	private Label scrollEndLabel;

	/**
	 * Shows the end time of the scroll position. 
	 */
	private Label scrollStartLabel;

	private PamBorderPane holder;

	/**
	 * Axis which shows the current dates
	 */
	private PamDateAxis dateAxis;

	/**
	 * Constructor for the ScrollingDataPaneFX
	 * @param dataMapControl
	 * @param dataMapPaneFX
	 */
	public ScrollingDataPaneFX(DataMapControl dataMapControl,
			DataMapPaneFX dataMapPaneFX) {
		this.dataMapControl = dataMapControl;
		this.dataMapPaneFX = dataMapPaneFX;
		this.setCenter(createScrollingDataPane());
	}

	/**
	 * Creates the panes and controls for the display. 
	 * @return the scrolling pane. 
	 */
	private Node createScrollingDataPane() {

		holder=new PamBorderPane(); 

		//create the main scroll pane 
		mainScrollPane = new PamScrollPane();	
		mainScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);


		//create the split pane to hold the graphs. 
		dataPanePanes=new PamVBox(); 
		//dataPanePanes.setOrientation(Orientation.VERTICAL);
		dataPanePanes.prefWidthProperty().bind(mainScrollPane.widthProperty());
		//dataPanePanes.prefHeightProperty().bind(mainScrollPane.heightProperty());

		mainScrollPane.setContent(dataPanePanes);
		//we have a custom scroll bar for horizontal stuff. 
		//		mainScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

		//		///TEMP///
		//		Button buttonTest=new Button("Test Map"); 
		//		buttonTest.setOnAction((action)->{
		//			this.dataMapControl.findDataSources();
		//		});
		//		holder.setTop(buttonTest);
		//		//////////


		//		PamButton test = new PamButton("Test");
		//		test.setOnAction((action)->{
		//			updateScrollBar();
		//		});
		//		holder.setLeft(test);



		//finally make sure the scroll bar recalculates stuff when holder changes size
		holder.widthProperty().addListener((change)->{
			notifyScrollChange(300);
		});


		dateAxis = new PamDateAxis();
		dateAxis.setAutoRanging(false);
		dateAxis.setLabel("Time");
		dateAxis.setSide(Side.TOP);
		dateAxis.setAnimated(false);
		dateAxis.setMinHeight(50);
		//		dateAxis.prefWidthProperty().bind(scrollingDataPanel.widthProperty());
		//		dateAxis.setStyle("-fx-background-color: ORANGE;");		 
		dateAxis.setForceZeroInRange(false);


		PamVBox vBox = new PamVBox();
		vBox.getChildren().add(createScrollBar());
		vBox.getChildren().add(dateAxis);

		holder.setTop(vBox);
		holder.setCenter(mainScrollPane);

		setupScrollBar();		

		return holder;
	}


	/**
	 * Updates the scrollbar.
	 */
	public void updateScrollBar() {
		setupScrollBar();
		updateScrollBarText();

		calcStartEndMillis();
		updateScrollBarText();
		notifyScrollChange();
	}

	/**
	 * Create the horizontal scroll bar for scrolling through time. 
	 * @return the horizontal scroll bar. 
	 */
	private PamBorderPane createScrollBar(){
		PamBorderPane holder=new PamBorderPane();

		//create a pane to show start and end times
		PamBorderPane timeLabelPane=new PamBorderPane();
		scrollStartLabel=new Label(); 
		scrollEndLabel=new Label(); 
		timeLabelPane.setLeft(scrollStartLabel);
		timeLabelPane.setRight(scrollEndLabel);
		scrollStartLabel.setText(PamCalendar.formatDateTime(screenStartMillis));
		scrollEndLabel.setText(PamCalendar.formatDateTime(screenEndMillis));
		timeLabelPane.setPadding(new Insets(2, 10, 2, 10)); //bit of padding to look better


		//create the scroll bar and listeners. 
		timeScrollBar=new DataMapScrollBar(this.dataMapControl); 
		timeScrollBar.addValueListener((obs_val, old_val, new_val)->{
//			System.out.println("Scroll bar seconds: " + timeScrollBar.getCurrentValue() + " vis amount: " + timeScrollBar.visibleAmountProperty().get());
			calcStartEndMillis();
			updateScrollBarText();
			notifyScrollChange();
		});

		timeScrollBar.getTextBox().setPrefColumnCount(15);
		timeScrollBar.getTextBox().setPrefWidth(100);

		timeScrollBar.setPrefHeight(50);
		
		//set this to zero just so that we know if it has been set or not
		timeScrollBar.setVisibleAmount(0.);

		holder.setCenter(timeScrollBar);
		holder.setBottom(timeLabelPane);

		return holder; 
	}

	/**
	 * Calculate the start and millis based on scroll position and screen seconds. 
	 */
	private void calcStartEndMillis(){
		screenStartMillis = (long) (dataMapControl.getFirstTime() + 
				timeScrollBar.getCurrentValue());
		screenEndMillis = (long) (screenStartMillis + timeScrollBar.getVisibleAmount());
		
		double pixsPerHour = getPixelsPerHour(); 
		double pixsPerSecond = pixsPerHour / 3600;
		double screenWidth = getPlotWidth();
		screenSeconds =  screenWidth / Math.min(600. / 3600, pixsPerSecond);

	}

	/**
	 * Update the text in the scroll bar. Shows the start and end time of the current screen. 
	 */
	private void updateScrollBarText(){
		scrollStartLabel.setText(PamCalendar.formatDateTime(screenStartMillis));
		scrollEndLabel.setText(PamCalendar.formatDateTime(screenEndMillis));
	}

	/**
	 * Notify all panels and the settings strip that the scroll bar moved
	 */
	public void notifyScrollChange() {
		// tell all panlettes to repaint. 
		for (int i = 0; i < dataStreamPanels.size(); i++) {
			if (!dataStreamPanels.get(i).isCollapsed()) {
				dataStreamPanels.get(i).scrollChanged();
			}
		}
//		settingsStrip.scrollChanged();

		updateDateAxis();
	}

	Timeline timeline; 
	long lastTime = 0; 

	/**
	 * Notify all panels and the settings strip that the scroll bar moved - but have a timer to wait to not call too often. 
	 */
	public void notifyScrollChange(long tm) {
		// tell all panlettes to repaint. 
		// Start of block moved over from the panel repaint(tm) function. 
		long currentTime=System.currentTimeMillis();
		if (currentTime-lastTime<tm){
			//start a timer. If a repaint hasn't be called because diff is too short this will ensure that 
			//the last repaint which is less than diff is called. This means a final repaint is always called 
			if (timeline!=null) timeline.stop();
			timeline = new Timeline(new KeyFrame(
					Duration.millis(tm),
					ae ->  notifyScrollChange()));
			timeline.play();
			return;
		}

		lastTime=currentTime;

		updateDateAxis();

	}

	private void updateDateAxis() {
		calcStartEndMillis();
		dateAxis.setUpperBound(screenEndMillis);
		dateAxis.setLowerBound(screenStartMillis);
		double[] ticks = dateAxis.recalculateTicks();
		//		System.out.println("Ticks: " + (ticks[3]/1000/3600) +  "hours");
		dateAxis.setTickUnit(ticks[3]);	
	}

	/**
	 * Create the data graphs to go into the pane. 
	 * @return the number of DataStreamPanes created. 
	 */
	public synchronized int createDataGraphs() {
		//clear the panes from list and split pane. 
		dataStreamPanels.clear();
		dataPanePanes.getChildren().clear(); 

		//now create new set of data stream panes. 
		ArrayList<PamDataBlock> dataBlocks = dataMapControl.getMappedDataBlocks();
		if (dataBlocks == null) {
			System.out.println("DataMapPaneFX:Create Data Graphs: Datablocks are null");
			return 0;
		}
		DataStreamPaneFX aStreamPanel;
		for (int i = 0; i < dataBlocks.size(); i++) {
			aStreamPanel = new DataStreamPaneFX(dataMapControl, this, dataBlocks.get(i));
			dataStreamPanels.add(aStreamPanel);
			dataStreamPanels.get(i).setPrefHeight(DATASTREAMPANE_HEIGHT);
			//now add to a split pane. 
			//SplitPane.setResizableWithParent(aStreamPanel, true);
			dataPanePanes.getChildren().add(aStreamPanel);
			//dataPanePanes.setDividerPosition(0,1.0/dataBlocks.size());
		}

		return dataBlocks.size();
	}


	/***
	 * Get the number of panes which are expanded. 
	 * @return the number of expanded panes. 
	 */
	private int getNExpandedPanes() {
		//work out the number of expanded dividers
		int nExpanded=0; 
		for (int i=0; i<dataStreamPanels.size(); i++) {
			if (!dataStreamPanels.get(i).isCollapsed()) nExpanded++;
		}
		return nExpanded; 
	} 


	/**
	 * Called whenever new data sources are added. 
	 */
	public void newDataSources() {
		offlineDataStores = PamController.getInstance().findOfflineDataStores();
		createDataGraphs();
		setupScrollBar();		
	}

	private void setupScrollBar() {
		
		/**
		 * Do scrolling in seconds - will give up to 68 years with a 
		 * 32 bit integer control of scroll bar. milliseconds would give < 1 year !
		 */
		double currentPos = timeScrollBar.getCurrentValue();
		long dataStart = dataMapControl.getFirstTime();
		long dataEnd = dataMapControl.getLastTime();
		double dataSeconds = ((dataEnd-dataStart)/1000) + 1;

		calcStartEndMillis();
		
		


		//		if (dataStart == Long.MAX_VALUE || screenSeconds >= dataSeconds) {
		//			System.out.println("dataSeconds1: "+dataSeconds+ " pixsPerHour: " +pixsPerHour+" screenWidth: "+screenWidth+" screenSeconds "+screenSeconds+ " holder width: "+holder.getWidth());
		//			/* 
		//			 * hide the scroll bar and stretch the display to fit the window 
		//			 */
		//			timeScrollBar.setVisible(true);
		//			screenStartMillis = dataStart;
		//			screenEndMillis = dataEnd;
		//		}
		//		else {
//		System.out.println("dataSeconds2: "+dataSeconds+ " pixsPerHour: " +pixsPerHour+" screenWidth: "+screenWidth+" screenSeconds "+screenSeconds+" holder width: "+holder.getWidth());
		
		timeScrollBar.setVisible(true);
		timeScrollBar.setMinVal(0);
		timeScrollBar.setMaxVal(Math.max(dataSeconds, screenSeconds)*1000L);
		timeScrollBar.setBlockIncrement(Math.max(1, screenSeconds * 4/5));
		//			timeScrollBar.setUnitIncrement(Math.max(1, screenSeconds / 20));
		
		//there might already have a visible amount in which case we do not wish to change. This is a bit of a hack 
		//to figure out whether the visible amount has already been set. 
		if (timeScrollBar.getVisibleAmount()==0) {
			timeScrollBar.setVisibleAmount(screenSeconds*1000L);
		}
		timeScrollBar.setCurrentValue(currentPos);
		
		//now paint the canvas to show the data. 
		timeScrollBar.paintDataSummary();

	}

	/**
	 * Get the width of the plot pane- note that this is the area where things can plot, not including the axis 
	 * @return the width of the plot area. 
	 */
	private double getPlotWidth() {
		//HACK- seems like there is a lyout delay in datstream panes. 
		return this.holder.getWidth()-DataStreamPaneFX.PREF_AXIS_WIDTH;
		//		if (dataStreamPanels.size()>0){
		//			dataStreamPanels.get(0).layout();
		//			return dataStreamPanels.get(0).getDataGraph().getPlotWidth();
		//		}
		//		return 0;
	}

	public void scrollToData(PamDataBlock dataBlock) {
		long startTime = dataBlock.getCurrentViewDataStart();
		int val = (int) ((startTime - getScreenStartMillis())/1000 - getScreenSeconds()/5)  ;
		timeScrollBar.setCurrentValue(val);
	}

	/**
	 * @return the screenStartMillis
	 */
	public long getScreenStartMillis() {
		return screenStartMillis;
	}

	/**
	 * @return the screenEndMillis
	 */
	public long getScreenEndMillis() {
		return screenEndMillis;
	}


	public double getPixelsPerHour() {
//		System.out.println("Pixels per hour: " + dataMapControl.dataMapParameters.getPixeslPerHour() + " " + this.getPlotWidth()/(this.timeScrollBar.getVisibleAmount()/1000./3600.));
		//return dataMapControl.dataMapParameters.getPixeslPerHour();

		return this.getPlotWidth()/(this.timeScrollBar.getVisibleAmount()/1000./3600.);

	}

	/**
	 * @return the screenSeconds
	 */
	public double getScreenSeconds() {
		return screenSeconds;
	}

	/**
	 * Get a colour for the datastream. 
	 * @param dataSource
	 * @return
	 */
	protected Color getDataStreamColour(OfflineDataStore dataSource) {
		if (offlineDataStores == null) {
			return Color.DARKGREY;
		}
		int ind = offlineDataStores.indexOf(dataSource);
		if (ind < 0) {
			return Color.GREEN;
		}
		return PamColorsFX.getInstance().getChannelColor(ind);
	}

	/**
	 * Reapint all the datastream panels. 
	 */
	public void repaintAll() {
		for (int i=0; i<dataStreamPanels.size(); i++){
			dataStreamPanels.get(i).repaint(REPAINTMILLIS); 
		}
	}

	/**
	 * Get the data map pane, holds both the scrolling pane and the summary pane. 
	 * @return the DataMapPaneFX the scrolling pane belongs to. 
	 */
	public DataMapPaneFX getDataMapPane() {
		return dataMapPaneFX;
	}

	int lastHScaleChoice=-1; 
	public void scaleChange() {
		if (lastHScaleChoice != dataMapControl.dataMapParameters.hScaleChoice) {
			lastHScaleChoice = dataMapControl.dataMapParameters.hScaleChoice;
			setupScrollBar();
		}
		this.notifyScrollChange();
	}

	/**
	 * Get the current number of data stream panes
	 * @return the number of data stream panes
	 */
	public int getNumDataStreamPanes() {
		return this.dataStreamPanels.size();
	}
	
	/**
	 * Get a data stream pane. 
	 * @param n - the index of the data stream pane
	 * @return the data stream pane or null if the index is out of bounds. 
	 */
	public DataStreamPaneFX getDataSyreamPane(int n) {
		if (n<this.dataStreamPanels.size()) {
			return dataStreamPanels.get(n);
		}
		else return null;
	}

	public DataStreamPaneFX getDataStreamPane(DataMapInfo selectedItem) {
		for (int i=0; i<dataStreamPanels.size(); i++) {
			if (selectedItem.equals(dataStreamPanels.get(i).getDataName())) {
				return dataStreamPanels.get(i);
			}
		}
		return null;
	}
	
}
