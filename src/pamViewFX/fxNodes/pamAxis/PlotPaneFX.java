package pamViewFX.fxNodes.pamAxis;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * A quick implementation of a chart/plot. Note, JavaFX has its own chart library which is perfectly suited to many charts but slow when a
 * large number of data is points.
 * <p>
 * The advantage of using the standard JavaFX library is that it can be integrated with CSS, scale dynamically with high DPUI screens and will
 * remain supported for further releases.  
 * <p>
 * So use PLtPaneFX if standard FX were too slow for the volume data to be plotted, otherwise use basic JavaFX implementation of chart. 
 * @author Jamie Macaulay
 *
 */
public class PlotPaneFX extends BorderPane {
	
	/**
	 * Canvas for plotting data. 
	 */
	private Canvas plotCanvas;
	
	/**
	 * Canvas for drawing or making annotations. 
	 */
	private Canvas annotationCanvas;

	/*
	 *The y axis. 
	 */
	private PamAxisFX yAxisLeft;

	/**
	 * The x axis. 
	 */
	private PamAxisFX xAxisBottom;
	

	/*
	 *The y axis. 
	 */
	private PamAxisFX yAxisRight;

	/**
	 * The x axis. 
	 */
	private PamAxisFX xAxisTop;
	
	/**
	 * Pane which holds x axis 
	 */
	private PamAxisPane xAxisTopPane;
	
	/**
	 * Pane which holds x axis  on the right
	 */
	private PamAxisPane xAxisBottomPane;

	/**
	 * Pane which holds y axis 
	 */
	private PamAxisPane yAxisLeftPane;
	
	/**
	 * Pane which holds y axis 
	 */
	private PamAxisPane yAxisRightPane;
	
	/**
	 * The graph data. Each represents DataSeries is a different seris on the line graph. 
	 */
	private ObservableList<DataSeries> data=FXCollections.observableArrayList();
	
	public PlotPaneFX(){
		
		//axis
		
		//create y axis
		yAxisRight=new PamAxisFX(0, 0, 50, 50, 10,0 , PamAxisFX.BELOW_RIGHT, "Graph Units", PamAxisFX.LABEL_NEAR_MAX, "%4d");
		yAxisRight.setCrampLabels(true);
		yAxisLeft=new PamAxisFX(0, 0, 50, 50, 10,0 , PamAxisFX.ABOVE_LEFT, "Graph Units", PamAxisFX.LABEL_NEAR_MAX, "%4d");
		yAxisLeft.setCrampLabels(true);

		yAxisLeftPane=new PamAxisPane(yAxisLeft,Orientation.VERTICAL); 
		yAxisLeftPane.setPrefWidth(75);

		yAxisRightPane=new PamAxisPane(yAxisRight,Orientation.VERTICAL); 
		yAxisRightPane.setPrefWidth(75);

				
		//create x axis
		xAxisBottom=new PamAxisFX(0, 50, 50, 50, 0, 5000, PamAxisFX.BELOW_RIGHT, "Graph Units", PamAxisFX.LABEL_NEAR_CENTRE, "%4d");
		xAxisBottom.setCrampLabels(true);
		xAxisTop=new PamAxisFX(0, 50, 50, 50, 0, 5000, PamAxisFX.ABOVE_LEFT, "Graph Units", PamAxisFX.LABEL_NEAR_CENTRE, "%4d");
		xAxisTop.setCrampLabels(true);
		 
		xAxisTopPane=new PamAxisPane(xAxisTop, Orientation.HORIZONTAL); 
		xAxisTopPane.setPrefHeight(75);
		xAxisTopPane.leftPaddingProperty().bind(yAxisLeftPane.widthProperty());
		xAxisTopPane.rightPaddingProperty().bind(yAxisRightPane.widthProperty());

		xAxisBottomPane=new PamAxisPane(xAxisBottom, Orientation.HORIZONTAL); 
		xAxisBottomPane.setPrefHeight(75);
		xAxisBottomPane.leftPaddingProperty().bind(yAxisLeftPane.widthProperty());
		xAxisBottomPane.rightPaddingProperty().bind(yAxisRightPane.widthProperty());

		//create canvas to plot data 
		plotCanvas=new Canvas(50,50); 
		annotationCanvas=new Canvas(50,50); 
	    final Pane pane = new Pane();
	    pane.getChildren().add(plotCanvas);
	    pane.getChildren().add(annotationCanvas);
		this.setCenter(pane);
		
//		plotCanvas.widthProperty().bind(pane.widthProperty());
//		plotCanvas.heightProperty().bind(pane.heightProperty());
//		plotCanvas.widthProperty().bind(this.widthProperty().subtract(yAxisRightPane.widthProperty()).
//				subtract(yAxisLeftPane.getWidth()));
//		plotCanvas.heightProperty().bind(this.heightProperty().subtract(xAxisBottomPane.heightProperty()).
//				subtract(xAxisTopPane.heightProperty()));
		
		//need to to resize the canvas inside the graph properly 
		pane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
//            	plotCanvas.setWidth(arg2.doubleValue()-yAxisRightPane.getWidth()-yAxisLeftPane.getWidth());
//            	annotationCanvas.setWidth(arg2.doubleValue()-yAxisRightPane.getWidth()-yAxisLeftPane.getWidth());
            	plotCanvas.setWidth(pane.widthProperty().getValue());
            	annotationCanvas.setWidth(pane.widthProperty().getValue());
            	repaint();
            }
        });
		
		pane.heightProperty().addListener(new ChangeListener<Number>() {
	            @Override
	            public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
//	            	plotCanvas.setHeight(arg2.doubleValue()-xAxisBottomPane.getHeight()-xAxisTopPane.getHeight());
//	            	annotationCanvas.setHeight(arg2.doubleValue()-xAxisBottomPane.getHeight()-xAxisTopPane.getHeight());
	            	plotCanvas.setHeight(pane.heightProperty().getValue());
	            	annotationCanvas.setHeight(pane.heightProperty().getValue());
	            	repaint();
	            }
	     });
		
		//TEMP
		setTopAxis(true);
		setLeftAxis(true);

		repaint(); 
	}

	
	/**
	 * Set the top axis
	 * @param topAxis
	 */
	public void setTopAxis(boolean topAxis){
		if (topAxis){
			this.setTop(xAxisTopPane);
		}
		else this.setTop(null);

	}

	public void setBottomAxis(boolean bottomAxis){
		if (bottomAxis){
			this.setBottom(xAxisBottomPane);
		}
		else this.setBottom(null);
	}

	public void setLeftAxis(boolean leftAxis){
		if (leftAxis){
			this.setLeft(yAxisLeftPane);
		}
		else this.setLeft(null);
	}
	
	public void setRightAxis(boolean rightAxis){
		if (rightAxis){
			this.setRight(yAxisRightPane);
		}
		else this.setRight(null);
	}
	
	/**
	 * Add data to the plot
	 * @param series data series to add to plot
	 */
	public void addDataSeries(DataSeries series) {
		data.add(series);
		repaint(); 
	}
	
	/**
	 * Get the graph data. 
	 * @return the graph data 
	 */
	public ObservableList<DataSeries> getData(){
		return data;
	}
	
	/**
	 * Set the x axis limits. 
	 * @param xLim
	 */
	public void setXLim(double[] xLim){
		xAxisTop.setMinVal(xLim[0]);
		xAxisTop.setMaxVal(xLim[1]);
		xAxisBottom.setMinVal(xLim[0]);
		xAxisBottom.setMaxVal(xLim[1]);
		repaint();
	}
	
	/**
	 * Set the x axis limits. 
	 * @param xLim - x axis limits two values, min and max. 
	 */
	public void setXLim(double xMin, double xMax){
		xAxisTop.setMinVal(xMin);
		xAxisTop.setMaxVal(xMax);
		xAxisBottom.setMinVal(xMin);
		xAxisBottom.setMaxVal(xMax);
		repaint(); 
	}
	
	/**
	 * Set the y axis limits. 
	 * @param yLim - y axis limits two values, min and max. 
	 */
	public void setYLim(double[] yLim){
		yAxisLeft.setMinVal(yLim[1]);
		yAxisLeft.setMaxVal(yLim[0]);
		yAxisRight.setMinVal(yLim[1]);
		yAxisRight.setMaxVal(yLim[0]);
		yAxisLeft.setMinVal(yLim[1]);
		repaint(); 
	}
	
	/**
	 * Set the y axis limits. 
	 * @param yMin - the minimum value fo the y axis. 
	 * @param yMax - the maximum value of the y axis
	 */
	public void setYLim(double yMin, double yMax){
		yAxisLeft.setMinVal(yMin);
		yAxisLeft.setMaxVal(yMax);
		yAxisRight.setMinVal(yMin);
		yAxisRight.setMaxVal(yMax);
		repaint(); 
	}
	
	/**
	 * Set the stroke colour for all axis. 
	 * @param color - colour to set tick marks and color
	 */
	public void setAxisStroke(Color color){
		yAxisLeft.setStrokeColor(color);
		yAxisRight.setStrokeColor(color);
		xAxisTop.setStrokeColor(color);
		xAxisBottom.setStrokeColor(color);
	}
	

	public void repaint(){
		//System.out.println("repaint: "+plotCanvas.getWidth()+" "+plotCanvas.getHeight());
		
		plotCanvas.getGraphicsContext2D().clearRect(0, 0, plotCanvas.getWidth(), plotCanvas.getHeight());
		
		//TODO
		plotCanvas.getGraphicsContext2D().setFill(Color.RED);
		plotCanvas.getGraphicsContext2D().fillRect(0, 0, plotCanvas.getWidth(), plotCanvas.getHeight());

		plotCanvas.getGraphicsContext2D().fill();

		xAxisTopPane.repaint();
		yAxisRightPane.repaint();
		xAxisBottomPane.repaint();
		yAxisLeftPane.repaint();
		
		drawMarkers(plotCanvas.getGraphicsContext2D());
		//TODO-add lines in here. 
	
		//drawShapes(plotCanvas.getGraphicsContext2D());
		//drawTestData(plotCanvas.getGraphicsContext2D());
	}
	
	public Canvas getPlotCanvas() {
		return plotCanvas;
	}


	/***
	 * Draw markers for all data sets. 
	 * @param graphicsContext2D the graphics context
	 */
	private void drawMarkers(GraphicsContext gc) {
		double[][] dataSeries;
		double posX;
		double posY;
		for (int i=0; i<data.size() ;i++){
			dataSeries=data.get(i).getData();
			for (int j=0; j<dataSeries.length; j++){
				//now use top or bottom axis for x. if both axis are there use bottom
				if (this.getBottom()!=null)	 	posX=xAxisBottom.getPosition(dataSeries[j][0]);
				else if (this.getTop()!=null) 	posX=xAxisTop.getPosition(dataSeries[j][0]);
				else return; 
				//use left or right y axis. If both y axis are there use left axis
				if (this.getLeft()!=null)		posY=yAxisLeft.getPosition(dataSeries[j][1]);
				else if (this.getRight()!=null)	posY=yAxisLeft.getPosition(dataSeries[j][1]);
				else return;
				gc.fillOval(posX, posY, 5, 5);
				gc.strokeOval(posX,posY, 5, 5);
			}
		}
	}
	
	///TEST////
	
	public DataSeries createDataSet(int n){
		DataSeries dataSeries=new DataSeries(); 
		double[][] series=new double[n][2];
		for (int i=0; i<n; i++){
			double[] dataPoint={i,Math.random()};
//			double posX=xAxisBottom.getPosition(dataPoint[0]);
//			double posY=yAxisLeft.getPosition(dataPoint[1]);
			series[i]=dataPoint; 
		}
		dataSeries.setData(series);
		return dataSeries; 
	}

//	private void drawShapes(GraphicsContext gc) {
//		gc.setFill(Color.RED);
//		for (int i=0; i<200; i++){
//			int height=(int) (Math.random()*gc.getCanvas().getHeight());
//			int width=(int) (Math.random()*gc.getCanvas().getWidth());
//	        gc.fillOval(width, height, 10, 10);
//	        gc.strokeOval(width, height, 10, 10);
//		}
//	 }
//	
//	private void drawTestData(GraphicsContext gc) {
//		gc.setFill(Color.RED);
//		for (int i=0; i<200; i++){
//			double[] dataPoint={i,i};
//			double posX=xAxis.getPosition(dataPoint[0]);
//			double posY=yAxis.getPosition(dataPoint[1]);
//	        gc.fillOval(posX, posY, 10, 10);
//	        gc.strokeOval(posX,posY, 10, 10);
//		}
//	 }


}
