package dataMap.layoutFX;

import java.awt.Insets;
import java.util.Arrays;
import java.util.Iterator;

import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryStore;
import dataGram.DatagramImageData;
import dataGram.DatagramManager;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;
import dataMap.DataMapControl;
import dataMap.DataMapDrawing;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import PamController.OfflineDataStore;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import pamViewFX.fxNodes.pamAxis.PamAxisPane;
import pamViewFX.fxNodes.utilsFX.ColourArray;

public class DataStreamPaneFX extends PamBorderPane {
	
	/**
	 * The preferred width of the axis.
	 */
	public static double PREF_AXIS_WIDTH=80; 
	
	public static double PREF_HEADER_HEIGHT=20; 

	/**
	 * Reference to the data map control
	 */
	private DataMapControl dataMapControl;
	
	/**
	 * Reference to the ScrollingDataPaneFX the DataStreamPaneFX belongs to. 
	 */
	private ScrollingDataPaneFX scrollingDataPanel;

	/**
	 * Indicates whether the data block has an associated datagram. 
	 */
	private boolean hasDatagram;

	/**
	 * The data graph for the datablock 
	 */
	private DataGraphFX dataGraph;

	/**
	 * Name associated with the data stream.
	 */
	private DataName dataName;
	
	private int totalDatas, maxDatas;

	/**
	 * The data block associated with the graph. 
	 */
	private PamDataBlock dataBlock;

	private boolean graphVisible;
	
	private double[] minMaxVal;

	private boolean showDatagram = true;

	private boolean showDataCounts = false;
	
	/**
	 * Pixels per millisecond for horizontal scale. 
	 */
	private double pixelsPerMilli; 
	
	/**
	 * Y scale minimum and maximum
	 */
	private double yScaleMin, yScaleMax; 
	
	
	private double lastPlotted2DmaxVal, lastPlotted2DminVal;
	
	/**
	 * Default data colour
	 */
	private Color haveDataColour = Color.DARKGRAY;

	/**
	 * The axis for the datagraph. 
	 */
	private PamAxisFX datastreamAxis;

	/**
	 * Pane which sits at top of data stream map and shows whihc datablock data belongs to/. 
	 */
	private Pane topPane;

	private boolean collapsed;
	
	/**
	 * Timer that repaints after time diff has been reached 
	 */
	private Timeline timeline;

	private PamButton showButton;

	/**
	 * Constructor for the data stream pane. 
	 * @param dataMapControl - the DataMapControl control the  DataStreamPaneFX  belongs to 
	 * @param scrollingDataPanel - the ScrollingDataPaneFX the new DataStreamPaneFX. 
	 * @param dataBlock - datablock associated with the DataStreamPaneFX
	 */
	public DataStreamPaneFX(DataMapControl dataMapControl, ScrollingDataPaneFX scrollingDataPanel, 
			PamDataBlock dataBlock) {
		this.dataMapControl = dataMapControl;
		this.scrollingDataPanel = scrollingDataPanel;
		this.dataBlock = dataBlock;
		hasDatagram = (dataBlock.getDatagramProvider() != null);
		dataGraph = new DataGraphFX();
		dataGraph.setupAxis();
		dataName = new DataName();
		
		this.setTop(topPane=createTopPane());
		this.setCenter(dataGraph);
	}

	/*
	 * Create pane which holds datasream label and allows the split pane to collapse
	 */
	private Pane createTopPane(){
		PamHBox topPane=new PamHBox();
		topPane.getStyleClass().add("pane-opaque");
		topPane.getChildren().add(new Label(this.dataBlock.getDataName()));
		topPane.setAlignment(Pos.CENTER);
		
		PamBorderPane pane = new PamBorderPane();
		
		pane.setCenter(topPane);
		
		showButton = new PamButton();
		showButton.setStyle("-fx-padding: 0 10 0 10; -fx-border-radius: 0 0 0 0; -fx-background-radius: 0 0 0 0;");
		showButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-down", 18));
		showButton.setOnAction((action)->{
			this.setCollapsed(!this.isCollapsed()); 
		});
		
		pane.setLeft(showButton);
		
		pane.setPrefHeight(PREF_HEADER_HEIGHT);
		
		return pane;
	}
	
	
	/**
	 * @return the dataGraph
	 */
	public DataGraphFX getDataGraph() {
		return dataGraph;
	}
	
	/**
	 * @return the dataName
	 */
	public DataName getDataName() {
		return dataName;
	}
	
	private int getTotalDatas() {

		int nMaps = dataBlock.getNumOfflineDataMaps();
		OfflineDataMap aMap;
		totalDatas = maxDatas = 0;
		for (int i = 0; i < nMaps; i++) {
			aMap = dataBlock.getOfflineDataMap(i);
			totalDatas += aMap.getNumMapPoints();
			maxDatas = Math.max(maxDatas, aMap.getNumMapPoints());
		}
		return totalDatas;
	}
	
//	public void autoHide() {
//		getTotalDatas();
//		setGraphVisible(totalDatas > 0);
//	}
	
	public boolean isGraphVisible() {
		return graphVisible;
	}

	public class DataGraphFX extends PamBorderPane {
		
		private Canvas plotCanvas;
		
		private double lastPlotted2DmaxVal;

		private double lastPlotted2DminVal;
		
		private static final int NCOLOURPOINTS = 100;
		
		//for 3D data gram 
		
		private ColourArray datagramColours;
		
		/**
		 * The wheel scroll factor. 
		 */
		private double wheelScrollFactor = 0.1;
		
		/**
		 * Writable image for 3D datagram.
		 */
		private WritableImage datagramImage;

		/**
		 * Pane which holds the axis. 
		 */
		private PamAxisPane axisPane;

		/**
		 * Pane which holds the plot canvas.
		 */
		private PamBorderPane canvasHolder;

		/**
		 * The opacity of the bars showing data.
		 */
		private double dataBarOpacity= 0.4;

		/**
		 * The canvas for drawing stuff on. 
		 */
		private Canvas drawCanvas;

		/**
		 * Total wheel rotation of the mouse. 
		 */
		private int totalWheelRotation;

		private long lastTime;


		private DataGraphFX() {
			createDataGraph();
			addDataGraphMouse();
			
			
		}
		
		
		private void addDataGraphMouse() {
			
			canvasHolder.setOnMouseEntered(e->{
				scrollingDataPanel.getDataMapPane().selectedDataTime(dataBlock.getCurrentViewDataStart(), dataBlock.getCurrentViewDataEnd());
			});

			canvasHolder.setOnMouseMoved( e->{
				long tm = getTimeFromX(e.getX());
				
				scrollingDataPanel.getDataMapPane().selectedDataTime(dataBlock.getCurrentViewDataStart(), dataBlock.getCurrentViewDataEnd());
				scrollingDataPanel.getDataMapPane().dataGraphMouseTime(tm);
			});
			
			canvasHolder.setOnMousePressed( e->{
				//TEMP
				//System.out.println("DataStreamPaneFX.DataGraphFX Loading Viewer Data: " + getTimeFromX(e.getX()));
				dataMapControl.centreDataAt(dataBlock, getTimeFromX(e.getX()));
				paintCanvas(100);
				//System.out.println("DataStreamPaneFX.DataGraphFX Viewer data start " + dataBlock.getCurrentViewDataStart());
				scrollingDataPanel.getDataMapPane().selectedDataTime(dataBlock.getCurrentViewDataStart(), dataBlock.getCurrentViewDataEnd());
				//showGraphMenu(e);
			});
			
			canvasHolder.setOnScroll(e->{
				//only change colours of the control key is down. 
				if (e.isControlDown()) {
					wheelMoved(e);
				}
			});
			
		}
		
		
		public void wheelMoved(ScrollEvent e) {
			totalWheelRotation += e.getDeltaY();
			wheelScrollFactor = Math.pow(2.0, totalWheelRotation);
//			System.out.println(String.format("totalRotation = %d, scroll factor = %3.5f", 
//					totalWheelRotation, wheelScrollFactor));
			this.paintCanvas(100);
		}


		/**
		 * Create the DataGraph
		 */
		private void createDataGraph(){

			OfflineDataMap aMap;
			DataMapDrawing dataMapDrawing;
			int nMaps = dataBlock.getNumOfflineDataMaps();
			for (int i = 0; i < nMaps; i++) {
				aMap = dataBlock.getOfflineDataMap(i);
				dataMapDrawing = aMap.getSpecialDrawing();
			}
			
			//create a pane to hold canvas
			canvasHolder=new PamBorderPane();
			
			//create canvas to plot stuff on
			plotCanvas=new Canvas(90,90); 
//			plotCanvas.heightProperty().bind(canvasHolder.heightProperty());
//			plotCanvas.widthProperty().bind(canvasHolder.widthProperty());

			//create canvas for overlaid drawings
			drawCanvas=new Canvas(90,90); 

			plotCanvas. getGraphicsContext2D(). setImageSmoothing(false);
			
			Pane pane = new Pane();
			pane.getChildren().add(plotCanvas);
			pane.getChildren().add(drawCanvas);
			drawCanvas.toFront();
			    
			canvasHolder.setCenter(pane);
			canvasHolder.setMinWidth(10);
			canvasHolder.setMinHeight(1);

			//add listener 
			canvasHolder.widthProperty().addListener((change)->{
				plotCanvas.setWidth(canvasHolder.getWidth());
				drawCanvas.setWidth(canvasHolder.getWidth());

				paintPlotCanvas(plotCanvas.getGraphicsContext2D());
				paintDrawCanvas(drawCanvas.getGraphicsContext2D());
				//canvasTestPlot(plotCanvas.getGraphicsContext2D());
				//System.out.println("canvas width has changed. Plot Canvas: "+plotCanvas.getWidth()+" canvas holder: "+canvasHolder.getWidth());
			});
			
			canvasHolder.heightProperty().addListener((change)->{
				plotCanvas.setHeight(canvasHolder.getHeight());
				drawCanvas.setHeight(canvasHolder.getHeight());

				paintPlotCanvas(plotCanvas.getGraphicsContext2D());
				paintDrawCanvas(drawCanvas.getGraphicsContext2D());

				//canvasTestPlot(plotCanvas.getGraphicsContext2D());
				//System.out.println("canvas height has changed. Plot Canvas: "+plotCanvas.getHeight() +" canvas holder: "+canvasHolder.getHeight());
			});
			
			//create the axis for this panel. 
			datastreamAxis = new PamAxisFX(0, 0, 10, 10, 0, 10, PamAxisFX.ABOVE_LEFT, "Graph Units", PamAxisFX.LABEL_NEAR_CENTRE, "%4d");
			datastreamAxis.setCrampLabels(true);
			datastreamAxis.setRange(0, 0);
			datastreamAxis.setFractionalScale(true);
			datastreamAxis.setLogScale(false); 

			axisPane=new PamAxisPane(datastreamAxis, Orientation.VERTICAL); 
			axisPane.getStyleClass().add("pane");
			axisPane.setOrientation(Orientation.VERTICAL);
			axisPane.setPrefWidth(DataStreamPaneFX.PREF_AXIS_WIDTH);
			axisPane.setStrokeColor(Color.BLACK);
			
			this.setLeft(axisPane);
			this.setCenter(canvasHolder);
		}
		
		
	
		/**
		 * Paint the plot canvas- i.e. datagrams, summary counts etc.
		 */
		public void paintCanvas(long tm){
			// Start of block moved over from the panel repaint(tm) function. 
			long currentTime=System.currentTimeMillis();
			if (currentTime-lastTime<tm){
				//start a timer. If a repaint hasn't be called because diff is too short this will ensure that 
				//the last repaint which is less than diff is called. This means a final repaint is always called 
				if (timeline!=null) timeline.stop();
				timeline = new Timeline(new KeyFrame(
						Duration.millis(tm),
						ae -> {
//							System.out.println("Paint Canvas zero");
							paintCanvas(0);	
						}));
				timeline.play();
				return;
			}
			
			lastTime=currentTime;

			long time1 = System.currentTimeMillis();
			paintPlotCanvas(plotCanvas.getGraphicsContext2D()); 
			paintDrawCanvas(drawCanvas.getGraphicsContext2D()); 
			
			long time2 = System.currentTimeMillis();

//			System.out.println("Paint Canvas: " + this + "   " + System.currentTimeMillis() + "  " + (time2-time1));

		}
		
		/**
		 * Paint any annotation marks on the draw canvas. 
		 * @param graphicsContext2D - the graphcis handle to paint on. 
		 */
		private void paintDrawCanvas(GraphicsContext g) {
			g.clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());

			//create some sort of annotation user interface here?

			//draw marks
			/**
			 * Draw on the chart the period of data loaded into memory.
			 * @param g graphics
			 */
			long dataStart = dataBlock.getCurrentViewDataStart();
			long dataEnd = dataBlock.getCurrentViewDataEnd();

			if (dataStart <= 0 || dataEnd < dataStart) {
				return;
			}
			
//			if (getScaleType() == DatagramScaleInformation.PLOT_3D){
//				System.out.println("DataSTreamPaneFX: datastart: " + PamCalendar.formatDateTime2(dataStart) +  " "+dataBlock.getDataName()); 
//				System.out.println("DataSTreamPaneFX: dataend: " + PamCalendar.formatDateTime2(dataEnd)+  " "+dataBlock.getDataName()); 
//			}

			double xStart = getXCoord(dataStart);
			double xEnd = getXCoord(dataEnd);
			
			//TEMP
			Color color=Color.DODGERBLUE;
			
			g.setStroke(color);
			g.setFill(Color.rgb(((int) color.getRed()*255), ((int) color.getGreen()*255), ((int) color.getBlue()*255), this.dataBarOpacity=0.4));

			int yEff = 5;
			g.strokeRect(xStart, 0, xEnd-xStart, drawCanvas.getHeight());
			g.fillRect(xStart, 0, xEnd-xStart, drawCanvas.getHeight());
		}


		
		/**
		 * Paint the plot canvas- i.e. datagrams, summary counts etc.
		 * @param gc- the graphics context handle to use. 
		 */
		private void paintPlotCanvas(GraphicsContext gc){
			setupAxis();
			gc.clearRect(0, 0, plotCanvas.getWidth(), plotCanvas.getHeight());
			if (hasDatagram && showDatagram) {
				datagramPaint(gc);
			}
			else {
				standardPaint(gc);
			}
		}
		
		/**
		 * Setup the axis correctly and insure that all numbers are correct for painting plot data
		 */
		public void setupAxis(){
			sortScales();
			//use log scale only if datagram is null. 
			if (findDatagramScaleInfo()==null)datastreamAxis.setLogScale(dataMapControl.dataMapParameters.vLogScale);
			datastreamAxis.setRange(getYScaleMin(), getYScaleMax());
			datastreamAxis.setLabel(getScaleUnits());
			paintAxis();
		}
		
		/**
		 * Paint the axis. 
		 */
		public void paintAxis() {
			axisPane.repaint();	
		}
		
		private void datagramPaint(GraphicsContext g) {
			if (getScaleType() == DatagramScaleInformation.PLOT_3D) {
				datagramPaint3D(g);
			}
			else {
				datagramPaint2D(g);
			}
		}
		
		
		private void datagramPaint2D(GraphicsContext g) {
			long startMillis = scrollingDataPanel.getScreenStartMillis();
			long endMillis = scrollingDataPanel.getScreenEndMillis();
			endMillis = startMillis + (long) (scrollingDataPanel.getScreenSeconds() * 1000.);
			double millisPerPixel = 1./pixelsPerMilli;
			// find a first datagram point and hope that all the rest are the same. 
			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findOfflineDataStore(BinaryStore.class);
			if (binaryStore == null) {
				return;
			}
			BinaryOfflineDataMap binaryDataMap = (BinaryOfflineDataMap) dataBlock.getOfflineDataMap(binaryStore);
			if (binaryDataMap == null) {
				return;
			}
			DatagramManager datagramManager = binaryStore.getDatagramManager();
			if (datagramManager == null) {
				return;
			}
			DatagramImageData datagramImageData = datagramManager.getImageData(dataBlock, startMillis, endMillis, (int) getWidth());
			double[][] imageData = datagramImageData.imageData;
			int x1, x2;
			x1 = (int) ((datagramImageData.imageStartTime - startMillis) * pixelsPerMilli);
			x2 = (int) ((datagramImageData.imageEndTime - startMillis) * pixelsPerMilli);

			int nTimePoints = imageData.length;
			int nAmpPoints = imageData[0].length;
			
			double[] minMaxValue;
			if (getMinMaxValues(imageData, false)!=null){
				minMaxValue = Arrays.copyOf(minMaxVal,2);
			}
			else{
				return;
			}
			
			lastPlotted2DminVal = minMaxValue[0];
			lastPlotted2DmaxVal = minMaxValue[1];
			
			double xScale = (double)(x2-x1)/(double)nTimePoints;
			double yScale = getHeight()/(minMaxValue[1]-minMaxValue[0]);
			double lastX = -9999., lastY=0, x, y;
			double h = getHeight();
			for (int iy = 0; iy < nAmpPoints; iy++) {
				g.setStroke(Color.BLUE);
				//g.setStroke(PamColors.getInstance().getWhaleColor(iy+1));

				lastX = -9999;
				for (int ix = 0; ix < nTimePoints; ix++) {
					x = (int) (xScale * ix) + x1;
					y = h - (int) (yScale * (imageData[ix][iy]-minMaxValue[0]));
					if (lastX != -9999.) {
						g.strokeLine(lastX, lastY, x, y);
					}
					lastX = x;
					lastY = y;
				}
			}
			
		}
		
		private synchronized void datagramPaint3D(GraphicsContext g) {
//			System.out.println("Paint 3D Canvas: " + this + "   " + System.currentTimeMillis());
			
			/*
			 *  hopefully, there will be datagram data for this block, so do a pretty
			 *  coloured map of it all.
			 *  The data will be spread out amongst all the data map points, so will need to 
			 *  scroll through a lot of them to find the data we need. 
			 *   
			 */
			long startMillis = scrollingDataPanel.getScreenStartMillis();
			long endMillis = scrollingDataPanel.getScreenEndMillis();
			endMillis = startMillis + (long) (scrollingDataPanel.getScreenSeconds() * 1000.);
			double millisPerPixel = 1./pixelsPerMilli;
			
			// find a first datagram point and hope that all the rest are the same. 
//			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findOfflineDataStore(BinaryStore.class);
//			if (binaryStore == null) {
//				return;
//			}
			
			// try to find a datamap which has a datagram
//			int nMaps = dataBlock.getNumOfflineDataMaps();
//			for (int i = 0; i < nMaps; i++) {
//				OfflineDataMap aMap = dataBlock.getOfflineDataMap(i);
//				if (aMap.)
//			}
			
	
			DatagramManager datagramManager = findDatagramManager();
			if (datagramManager == null) {
				return;
			}
			
			DatagramImageData datagramImageData = datagramManager.getImageData(dataBlock, startMillis, endMillis, (int) getWidth());
			double[][] imageData = datagramImageData.imageData;

			if (datagramColours == null) {
				datagramColours = ColourArray.createHotArray(NCOLOURPOINTS);
			}
			int nXPoints = imageData.length;
			int nYPoints = imageData[0].length;
			
			double[] minMaxValue;
			if (getMinMaxValues(imageData, false)!=null){
				minMaxValue = Arrays.copyOf(minMaxVal,2);
			}
			else{
				return;
			}
			
			minMaxValue[1] *= wheelScrollFactor;
			minMaxValue[0] = Math.log(minMaxValue[0]);
			minMaxValue[1] = Math.log(minMaxValue[1]);
			/* 
			 * now fudge the scale a little since black is zero and we want 
			 * anything > 0 to be significantly away from black.
			 */
			double scaleRange = (minMaxValue[1] - minMaxValue[0]) * 1.2;
			minMaxValue[0] = minMaxValue[1]-scaleRange;

			int iCol, y;
			int x1, x2;
			x1 = (int) ((datagramImageData.imageStartTime - startMillis) * pixelsPerMilli);
			x2 = (int) ((datagramImageData.imageEndTime - startMillis) * pixelsPerMilli);
			if (imageData.length == 0 || imageData[0].length == 0) {
				return;
			}
			datagramImage = new WritableImage(imageData.length, imageData[0].length);
			PixelWriter writableRaster = datagramImage.getPixelWriter();
			g.setFill(Color.LIGHTGRAY);
			g.fillRect(0, 0, nXPoints, nYPoints);
			for (int i = 0; i < nXPoints; i++) {
				for (int j = 0; j < nYPoints; j++) {
					y = nYPoints-j-1;
					if (imageData[i][j] < 0) {
						//writableRaster.setColor(i,y,Color.LIGHTGRAY);
					}
					else if (imageData[i][j] == 0) {
						//writableRaster.setColor(i,y, Color.LIGHTGRAY);
					}
					else {
						iCol = (int) (NCOLOURPOINTS * (Math.log(imageData[i][j]) - minMaxValue[0]) / scaleRange);
						iCol = Math.min(Math.max(0, iCol), NCOLOURPOINTS-1);
						writableRaster.setColor(i, y, datagramColours.getColour(iCol));
						//						datagramImage.setRGB(i, y, 0x0000FF);
					}
				}
			}
			/*
			 * Now finally paint that into the full window ...
			 * 
			 */
			double imageWidth = getWidth();
		

			//javafx version using a writable image (remember reversed compared to swing)
			g.drawImage(datagramImage, 0, 0, nXPoints, nYPoints,
					x1, 0,  x2-x1, getHeight());
			
			//swing version for ref
			//g.drawImage(datagramImage, x1, 0, x2, getHeight(), 0, 0, nXPoints, nYPoints, null);

			if (showDataCounts) {	
				OfflineDataMap offlineDataMap = dataBlock.getDatagrammedMap();
				if (offlineDataMap == null) {
					return;
				}
				OfflineDataStore offlineDataStore = offlineDataMap.getOfflineDataSource();
				if (offlineDataStore == null) {
					return;
				}
				drawDataRate(g, offlineDataMap, scrollingDataPanel.getDataStreamColour(offlineDataMap.getOfflineDataSource()));
			}

		}
		
		public void drawDataRate(GraphicsContext g,
				OfflineDataMap offlineDataMap, Color dataColour) {
			OfflineDataMapPoint mapPoint;
			int n;
			double x1, x2, y1, y2;
			//			boolean logScale = dataMapControl.dataMapParameters.vLogScale;
			long pointStart, pointEnd;
			//			int scaleType = dataMapControl.dataMapParameters.vScaleChoice;
			double h = getHeight();
			g.setFill(dataColour);
			synchronized(offlineDataMap) {
				long startMillis = scrollingDataPanel.getScreenStartMillis();
				long endMillis = scrollingDataPanel.getScreenEndMillis();
				Iterator<OfflineDataMapPoint> iterator = offlineDataMap.getListIterator();
				while (iterator.hasNext()) {
					mapPoint = iterator.next();
					n = mapPoint.getNDatas();
					pointStart = mapPoint.getStartTime();
					pointEnd = mapPoint.getEndTime();
					if (pointEnd < startMillis) {
						continue;
					}
					if (pointStart > endMillis) {
						break;
					}
					//					x1 = (int) ((pointStart - startMillis) * pixelsPerMilli);
					//					x2 = (int) ((pointEnd - startMillis) * pixelsPerMilli);
					x1 = getXCoord(pointStart);
					x2 = getXCoord(pointEnd);
					y2 = h; 
					y1 = getYCoord(n, pointEnd-pointStart);
					//					y1 = h - (int) ((n - yScaleMin) / (yScaleMax-yScaleMin) * h); 
					//					y1 = Math.min(y1, h-1);
					if (x1 == x2) {
						g.strokeLine(x1, y1, x2, y2);
					}
					else {
						g.strokeLine(x1, y1, x2, y2);
					}
				}
			}

		}
		
		/**
		 * Get the item y Coordinate based on the count, the plotHeight, 
		 * @param count Number of data entries
		 * @param plotHeight plot height in pixels
		 * @param scaleType per second, per minute, etc. 
		 * @param itemDuration duration of data map item in milliseconds.
		 * @param logScale log scale
		 * @return the y coordinate (from the top of the panel)
		 */
		public double getYCoord(double count, long itemDuration) {
			double plotHeight = getHeight();
			boolean logScale = dataMapControl.dataMapParameters.vLogScale;
			int scaleType = dataMapControl.dataMapParameters.vScaleChoice;
			double value = OfflineDataMap.scaleData(count, itemDuration, scaleType);
			if (logScale) {
				if (value <= 0) {
					return plotHeight;
				}
				else {
					return plotHeight - (int) (Math.log(value/yScaleMin) / Math.log(yScaleMax/yScaleMin) * plotHeight);
				}
			}
			else {
				return  plotHeight - (int) ((value - yScaleMin) / (yScaleMax-yScaleMin) * plotHeight);
			}
		}

		public double getXCoord(long value) {
			return  ((value - scrollingDataPanel.getScreenStartMillis()) * pixelsPerMilli);
		}
		

		/**
		 * Get a min and max value for the array of data. 
		 * if round10 is true, roune down and up to nearest power of 10
		 * @param data data 
		 * @param round10 flag to round min and max to nearest multiple of 10. 
		 * @return min and max values. 
		 */
		double[] getMinMaxValues(double[][] imageData, boolean round10) {
			if (imageData == null) {
				return null;
			}
			//if minMax is null calculate from binary store. Want to calculate for entire dataset, not just the currently displayed data.
			if (minMaxVal==null){ 
				minMaxVal=calcMinMax(round10);
			}
			else if (Double.isNaN(minMaxVal[0]) || Double.isNaN(minMaxVal[1]) ){
				System.out.println("DataStremPanel: "+ "min: "+minMaxVal[0]+" max: "+minMaxVal[1]);
//				minMaxVal=calcMinMax(round10);
			}
			return minMaxVal;
		}
		
		/**
		 * Calculate a min and max value for the entire datagram.
		 * if round10 is true, round down and up to nearest power of 10
		 * @param round10 flag to round min and max to nearest multiple of 10. 
		 * @return min and max values. 
		 */
		private double[] calcMinMax(boolean round10){
			
			// find a first datagram point and hope that all the rest are the same. 
//			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findOfflineDataStore(BinaryStore.class);
//			if (binaryStore == null) {
//				return null;
//			}
//			OfflineDataStore offlineDataStore = dataBlock.getDatagrammedMap()
	
			DatagramManager datagramManager = findDatagramManager();
			if (datagramManager == null || dataBlock == null){
				return null;
			}

			double[] minMaxVal =  datagramManager.getMinAndMax(dataBlock, false);

			if (round10) {
				minMaxVal[0] = Math.floor(minMaxVal[0]/10.);
				minMaxVal[0] *= 10;
				minMaxVal[1] = Math.ceil(minMaxVal[1]/10.);
				minMaxVal[1] *= 10.;
			}
			
			return minMaxVal;
		}
		
		
		private DatagramManager findDatagramManager() {		
			OfflineDataMap offlineDataMap = dataBlock.getDatagrammedMap();
			if (offlineDataMap == null) {
				return null;
			}
			OfflineDataStore offlineDataStore = offlineDataMap.getOfflineDataSource();
			if (offlineDataStore == null) {
				return null;
			}
			return offlineDataStore.getDatagramManager();
		}
		
		
		private void sortScales() {
			double pixsPerHour = scrollingDataPanel.getPixelsPerHour();
			pixelsPerMilli = pixsPerHour / 3600 / 1000;

			double highestPoint = 0;
			double aPoint;
			int nMaps = dataBlock.getNumOfflineDataMaps();
			OfflineDataMap aMap, highestMap = null;
			for (int i = 0; i < nMaps; i++) {
				aMap = dataBlock.getOfflineDataMap(i);
				if ((aPoint=aMap.getHighestPoint(OfflineDataMap.SCALE_PERHOUR)) > highestPoint) {
					highestPoint = aPoint;
					highestMap = aMap;
				}
			}

			if (highestMap == null) {
				return;
			}


			yScaleMin = 0;
			yScaleMax = highestMap.getHighestPoint(dataMapControl.dataMapParameters.vScaleChoice);
			if (dataMapControl.dataMapParameters.vLogScale) {
				yScaleMin = highestMap.getLowestNonZeroPoint(dataMapControl.dataMapParameters.vScaleChoice);
				//			if (yScaleMin > 0) {
				//				yScaleMin = Math.floor(Math.log10(yScaleMin*0.9));
				//				yScaleMin = Math.pow(10, yScaleMin);
				//			}
				//			else {
				//				yScaleMin = 0.1;
				//			}
				yScaleMin = 0.1;
				if (yScaleMax > 0) {
					yScaleMax = Math.ceil(Math.log10(yScaleMax));
					yScaleMax = Math.pow(10, yScaleMax);			}
				else {
					yScaleMax = 1;
				}
			}
			else {
				if (yScaleMax > 0) {
					yScaleMax *= 1.1;
				}
				else {
					yScaleMax = 1;
				}
			}
		}

		/**
		 * Paint effort and detections for all data maps. 
		 */
		public void standardPaint(GraphicsContext g) {
			int nMaps = dataBlock.getNumOfflineDataMaps();
			OfflineDataMap aMap;
			DataMapDrawing dataMapDrawing;
			for (int i = 0; i < nMaps; i++) {
				aMap = dataBlock.getOfflineDataMap(i);
				dataMapDrawing = aMap.getSpecialDrawing();
				if (dataMapDrawing == null) {
					drawEffort(g, aMap, haveDataColour);
					//all this appears to do is cause buggy lines.
					//drawDataRate(g, aMap, scrollingDataPanel.getDataStreamColour(aMap.getOfflineDataSource()));
				}
				else {
					dataMapDrawing.drawEffort(g, this, aMap, haveDataColour);
					dataMapDrawing.drawDataRate(g, this, aMap, 
							scrollingDataPanel.getDataStreamColour(aMap.getOfflineDataSource()));
				}
			}
		}
		
		public void drawEffort(GraphicsContext g,
				OfflineDataMap offlineDataMap, Color effortColour) {
			OfflineDataMapPoint mapPoint;
			int n;
			double x1, x2, y1, y2;
			long pointStart, pointEnd;
			double h = getHeight();
			g.setStroke(effortColour);
			g.setFill(Color.rgb(((int) effortColour.getRed()*255), ((int) effortColour.getRed()*255), ((int) effortColour.getRed()*255), this.dataBarOpacity=0.4));
			synchronized(offlineDataMap) {
				long startMillis = scrollingDataPanel.getScreenStartMillis();
				long endMillis = scrollingDataPanel.getScreenEndMillis();
				Iterator<OfflineDataMapPoint> iterator = offlineDataMap.getListIterator();
				while (iterator.hasNext()) {
					mapPoint = iterator.next();
					n = mapPoint.getNDatas();
					pointStart = mapPoint.getStartTime();
					pointEnd = mapPoint.getEndTime();
					if (pointEnd < startMillis) {
						continue;
					}
					if (pointStart > endMillis) {
						break;
					}
					x1 = (pointStart - startMillis) * pixelsPerMilli;
					x2 = (pointEnd - startMillis) * pixelsPerMilli;
					y2 = h; 
					y1 = h - ((n - yScaleMin) / (yScaleMax-yScaleMin) * h); 
					//					y1 = Math.min(y1, h-1);
					if (x1 == x2) {
						g.strokeLine(x1, 0, x2, h);
					}
					else {
						g.strokeRect(x1, 0, x2-x1+1, h);
						g.fillRect(x1, 0, x2-x1+1, h);

					}
				}
			}

		}

		/**
		 * Test some shapes on the canvas 
		 */
		private void canvasTestPlot(GraphicsContext gc){
			gc.clearRect(0, 0, 1500, 1500);
			//System.out.println("Canvas test plot");
			gc.setFill(Color.BLACK);
			gc.fillRect(0,0,canvasHolder.getWidth(),canvasHolder.getHeight());

			for (int i=0; i<1000; i++){
		
				gc.setFill(Color.LIME);
				gc.setStroke(Color.rgb(25, 123, 253));
				gc.fillRect(1000*Math.random(), 1000*Math.random(), 30*Math.random(), 30*Math.random());
			}
		}

		/**
		 * Gte the width of the plot pane i.e the pane in whihc things are plotted for the datamap, not including the axis. 
		 * @return
		 */
		public double getPlotWidth() {
			return canvasHolder.getWidth();	
		}

	}
	
	
	/**
	 * Convert an x coordinate into a time in milliseconds
	 * @param xPos x coordinate in graph
	 * @return milliseconds time
	 */
	private long getTimeFromX(double xPos) {
		return (long) (xPos / pixelsPerMilli) + scrollingDataPanel.getScreenStartMillis();
	}
	
	public class DataName {
		
	}
	
	/**
	 * @return the yScaleMin
	 */
	public double getYScaleMin() {
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			double minVal = dsi.getMinValue();
			if (Double.isNaN(minVal)) {
				return lastPlotted2DminVal;
			}
			else {
				return minVal;
			}
		}
		return yScaleMin;
	}

	/**
	 * @return the yScaleMax
	 */
	public double getYScaleMax() {		
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			double maxVal = dsi.getMaxValue();
			if (Double.isNaN(maxVal)) {
				return lastPlotted2DmaxVal;
			}
			else {
				return maxVal;
			}
		}
		return yScaleMax;
	}
	
	public String getScaleUnits() {	
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			return dsi.getUnits();
		}
		return "n";
	}
	
	public int getScaleType() {
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			return dsi.getDisplayType();
		}
		return DatagramScaleInformation.PLOT_3D;
	}

	/**
	 * Find the scale information for the datagram - return null
	 * if there either isn't a datagram or if it's not currently showing
	 * @return
	 */
	private DatagramScaleInformation findDatagramScaleInfo() {
		if (hasDatagram == false) {
			return null;
		}
		DatagramProvider dp = dataBlock.getDatagramProvider();
		if (dp == null) {
			return null;
		}
		if (showDatagram == false) {
			return null;
		}
		return dp.getScaleInformation();
	}

	/**
	 * Repaint the canvas. 
	 */
	public void repaint(long millis) {
		dataGraph.paintCanvas(millis);
	}
	
	/**
	 * Setup the axis
	 */
	public void setupAxis(){
		dataGraph.setupAxis();
	}

	/*
	 *Called whenever the time scroll bar is changed. Repaints datagraph. 
	 */
	public void scrollChanged() {
		repaint(ScrollingDataPaneFX.REPAINTMILLIS); //update at 10 frames per second
	}
	
	/**
	 * Get the pane which sits at the top of the datagraph and contains a label showing the datablock being displayed. 
	 * @return the pane which sits at the top of the datagraph
	 */
	public Pane getTopPane() {
		return topPane;
	}

	/***
	 * Set a flag that the data stream pane has been collapsed. 
	 * @param collapsed - true if collapsed.
	 */
	public void setCollapsed(boolean collapsed) {
		this.collapsed=collapsed;
		if (collapsed) {
			this.setCenter(null);
			this.setMaxHeight(PREF_HEADER_HEIGHT);

		}
		else {
			this.setCenter(dataGraph);
			this.setMaxHeight(-1);

		}
	}

	/**
	 * Check whether he data stream pane has been collapsed. 
	 * @return true if collapsed. 
	 */
	public boolean isCollapsed() {
		return collapsed;
	}


}
