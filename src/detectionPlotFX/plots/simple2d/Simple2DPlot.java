package detectionPlotFX.plots.simple2d;

import java.util.ArrayList;
import java.util.Arrays;

import com.sun.javafx.geom.Point2D;

import PamView.PamSymbolType;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import detectionPlotFX.DetectionDisplayControl;
import detectionPlotFX.layout.DetectionPlotDisplay;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamColorsFX;
import pamViewFX.fxNodes.PamColorsFX.PamColor;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import pamViewFX.fxPlotPanes.PlotPane;

/**
 * Class for a simple 2D plot which can hold both 
 * a data array and a related image. No scrolling since only 
 * intended for small quantities of data. 
 * @author Doug Gillespie
 *
 */
public class Simple2DPlot {

//	private DetectionPlotDisplay detectionPlotDisplay;
	private PlotPane plotPane;
	private PamBorderPane borderPane;
	private boolean autoScale = true;
	private boolean copyData = true;
	
	private StandardPlot2DColours spectrogramColours= new StandardPlot2DColours();
	private double[][] currentData;
	private double[] dataRangeX;
	private double[] dataRangeY;

	private PamSymbolFX peakSymbol = new PamSymbolFX(PamSymbolType.SYMBOL_CIRCLE, 10, 10, false, Color.WHITE, Color.WHITE);
	/**
	 * Paint a cross at the peak position
	 */
	private boolean paintPeakPos;
	
	private WritableImage plotImage;
	private javafx.geometry.Point2D peakPoint;
	
	private ArrayList<SimpleLineData> lineDatas = new ArrayList<>();
	private ArrayList<SimpleSymbolData> symbolDatas = new ArrayList<>();
	
	public Simple2DPlot(String unitName) {
		super();
		plotPane = new PlotPane();
		plotPane.setAxisVisible(false, false, true, true);
		borderPane = new PamBorderPane();
		borderPane.setCenter(plotPane);
		HBox.setHgrow(borderPane, Priority.ALWAYS);
		Invalidator inv = new Invalidator();
		borderPane.widthProperty().addListener(inv);
		borderPane.heightProperty().addListener(inv);
		peakSymbol.setLineThickness(3);
	}
	
	private class Invalidator implements InvalidationListener {
		@Override
		public void invalidated(Observable observable) {	
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					repaint();
				}
			});
		}
	}

	public Node getNode() {
		return borderPane;
	}
	
	/**
	 * Set the XAxis range, scale and label. 
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param labelScale label scale for the axis
	 * @param axisLabel  label for the axis. 
	 */
	public void setBottomAxisRange(double minVal, double maxVal, double labelScale, String axisLabel) {
		plotPane.getxAxisBottom().setLabelScale(labelScale);
		plotPane.getxAxisBottom().setRange(minVal, maxVal);
		plotPane.getxAxisBottom().setLabel(axisLabel);
	}
	
	/**
	 * Set the XAxis range
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param axisLabel 
	 * @param axisScale 
	 */
	public void setBottomAxisRange(double minVal, double maxVal) {
		plotPane.getxAxisBottom().setRange(minVal, maxVal);
	}
	
	/**
	 * Set the X Axis label
	 * @param label
	 */
	public void setBottomLabel(String label) {
		plotPane.getxAxisBottom().setLabel(label);
	}

	/**
	 * Set the Y Axis range
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param labelScale label scale for the axis
	 * @param axisLabel  label for the axis. 
	 */
	public void setLeftAxisRange(double minVal, double maxVal, double labelScale, String axisLabel) {
		plotPane.getyAxisLeft().setLabelScale(labelScale);
		plotPane.getyAxisLeft().setRange(minVal, maxVal);
		plotPane.getyAxisLeft().setLabel(axisLabel);
	}

	/**
	 * Set the Y Axis range
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 */
	public void setLeftAxisRange(double minVal, double maxVal) {
		plotPane.getyAxisLeft().setRange(minVal, maxVal);
	}
	
	/**
	 * Set the Y Axis label
	 * @param label
	 */
	public void setLeftLabel(String label) {
		plotPane.getyAxisLeft().setLabel(label);
	}

	/**
	 * Set the right Y Axis range
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param labelScale label scale for the axis
	 * @param axisLabel  label for the axis. 
	 */
	public void setRightAxisRange(double minVal, double maxVal, double labelScale, String axisLabel) {
		plotPane.getyAxisRight().setLabelScale(labelScale);
		plotPane.getyAxisRight().setRange(minVal, maxVal);
		plotPane.getyAxisRight().setLabel(axisLabel);
	}

	/**
	 * Set the  Right Y Axis range
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 */
	public void setRightAxisRange(double minVal, double maxVal) {
		plotPane.getyAxisRight().setRange(minVal, maxVal);
	}
	
	/**
	 * Set the right Y Axis label
	 * @param label
	 */
	public void setRightLabel(String label) {
		plotPane.getyAxisRight().setLabel(label);
	}

	/**
	 * Set the right Y Axis range
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @param labelScale label scale for the axis
	 * @param axisLabel  label for the axis. 
	 */
	public void setTopAxisRange(double minVal, double maxVal, double labelScale, String axisLabel) {
		plotPane.getxAxisTop().setLabelScale(labelScale);
		plotPane.getxAxisTop().setRange(minVal, maxVal);
		plotPane.getxAxisTop().setLabel(axisLabel);
	}

	/**
	 * Set the  Right Y Axis range
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 */
	public void setTopAxisRange(double minVal, double maxVal) {
		plotPane.getxAxisTop().setRange(minVal, maxVal);
	}
	
	/**
	 * Set the right Y Axis label
	 * @param label
	 */
	public void setTopLabel(String label) {
		plotPane.getxAxisTop().setLabel(label);
	}
	
	public void setData(double[][] data, double[] rangeX, double[] rangeY) {
		copyRawData(data);
		this.dataRangeX = rangeX;
		this.dataRangeY = rangeY;
		setBottomAxisRange(rangeX[0], rangeX[1]);
		setLeftAxisRange(rangeY[0], rangeY[1]);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				newData();
			}
		});
	}

	/**
	 * New data have arrived are are available
	 * in currentData
	 */
	private synchronized void newData() {
		createImage();
		repaint();
	}

	/**
	 * Create a coloured image from the data. 
	 */
	private void createImage() {
		if (currentData == null || currentData.length == 0) {
			plotImage = null;
			return;
		}
		int width = currentData.length;
		int height = currentData[0].length;
		if (plotImage == null || plotImage.getHeight() != height || plotImage.getWidth() != width) {
			plotImage = new WritableImage(width, height);
		}
		if (autoScale) {
			double[] minMax = getDataMinMax();
			DoubleProperty[] ampLims = spectrogramColours.getAmplitudeLimits();
			ampLims[0].set(minMax[0]);
			ampLims[1].set(minMax[1] + (minMax[1]-minMax[0])*.05);
		}
		// now set all the pixels in the image. 
		PixelWriter pixelWriter = plotImage.getPixelWriter();
		for (int i = 0; i < width; i++) {
			for (int j = 0, k = height-1; j < height; j++, k--) {
				Color col = spectrogramColours.getColours(currentData[i][j]);
				pixelWriter.setColor(i, k, col);
			}
		}
		double peakVal = currentData[0][0];
		int xx = 0, yy = 0;
		double z;
		if (paintPeakPos) {
			for (int i = 0; i < width; i++) {
				for (int j = 0, k = height-1; j < height; j++, k--) {
					z = currentData[i][j];
					if (z > peakVal) {
						peakVal = z;
						xx = i;
						yy = j;
					}
				}
			}
			// convert it from image to data coordinates.
			double xVal = (double) (xx+.5) / width * (dataRangeX[1]-dataRangeX[0]) + dataRangeX[0];
			double yVal = (double) (yy+.5) / height * (dataRangeY[1]-dataRangeY[0]) + dataRangeY[0];
			peakPoint = new javafx.geometry.Point2D(xVal, yVal);
		}
	}

	/**
	 * Get the minimum and maximum values in the data ignoring NaN's and Infinities. 
	 * @return
	 */
	private double[] getDataMinMax() {
		double[] minmax = new double[2];
		int width = currentData.length;
		int height = currentData[0].length;
		double min = Double.MAX_VALUE;
		double minNonZero = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double v = currentData[i][j];
				if (Double.isFinite(v)) {
					min = Math.min(min, v);
					if (v != 0) {
						minNonZero = Math.min(minNonZero, v);
					}
					max = Math.max(max, v);
				}
			}
		}
		minmax[0] = minNonZero;
		minmax[1] = max;
		return minmax;
	}
	
	/**
	 * Repaint on the FX thread. 
	 */
	public void repaintLater() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
	}

	public synchronized void repaint() {
		
		Canvas canvas = plotPane.getPlotCanvas();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		if (plotImage == null) {
			clearDisplay();
		}
		else {
			double sx = 0;
			double sw = plotImage.getWidth();
			double sy = 0; 
			double sh = plotImage.getHeight();
			if (plotPane.getyAxisLeft().getMaxVal() < dataRangeY[1]) {
				sh *= plotPane.getyAxisLeft().getMaxVal() / dataRangeY[1];
				sy = plotImage.getHeight() - sh;
			}
			gc.drawImage(plotImage, sx, sy, sw, sh, 0, 0, canvas.getWidth(), canvas.getHeight());
		}

		synchronized (lineDatas) {
			for (SimpleLineData lineData:lineDatas) {
				paintSimpleLine(gc, lineData);
			}
		}
		synchronized (symbolDatas) {
			for (SimpleSymbolData symbolData:symbolDatas) {
				symbolData.pamSymbol.draw(gc, symbolData.pt);
			}}

		if (paintPeakPos && peakPoint != null) {
			double peakX = plotPane.getxAxisBottom().getPosition(peakPoint.getX());
			double peakY = plotPane.getyAxisLeft().getPosition(peakPoint.getY());
			peakSymbol.draw(gc, new javafx.geometry.Point2D(peakX, peakY));
		}
	}

	public void addSymbol(PamSymbolFX symbol, double x, double y) {
		addSymbol(symbol, new javafx.geometry.Point2D(x, y));
	}

	public void addSymbol(PamSymbolFX symbol, javafx.geometry.Point2D pt) {
		synchronized (symbolDatas) {
			symbolDatas.add(new SimpleSymbolData(symbol, pt));
		}
	}
	/**
	 * Paint a line overlaying the 2D plot (if the 2D plot was even drawn)
	 * @param gc 
	 * @param lineData
	 */
	private void paintSimpleLine(GraphicsContext gc, SimpleLineData lineData) {
		double[] x = lineData.getxValues().clone();
		double[] y = lineData.getyValues().clone();
		PamAxisFX xAx = lineData.getxAxis();
		PamAxisFX yAx = lineData.getyAxis();
		if (xAx != null) {
			for (int i = 0; i < x.length; i++) {
				x[i] = xAx.getPosition(x[i]);
			}
		}
		if (yAx != null) {
			for (int i = 0; i < y.length; i++) {
				y[i] = yAx.getPosition(y[i]);
			}
		}
		Paint stroke = lineData.getLineStroke();
		if (stroke != null) {
			gc.setStroke(stroke);
		}
		Double width = lineData.getLineWidth();
		if (width != null) {
			gc.setLineWidth(width);
		}
		for (int i = 0, j = 1; j < x.length; i++, j++) {
			gc.strokeLine(x[i], y[i], x[j], y[j]);
		}
	}

	/**
	 * Clear the display 
	 */
	private void clearDisplay() {
		Canvas canvas = plotPane.getPlotCanvas();
		PamColorsFX.getInstance().getColor(PamColor.PlOTWINDOW);
		canvas.getGraphicsContext2D().setFill(Color.BLACK);
		canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	/**
	 * Hard copy of incoming data.
	 * @param data
	 */
	private synchronized void copyRawData(double[][] data) {
		if (copyData == false) {
			currentData = data;
			return;
		}
		else {
			if (data == null || data.length == 0) {
				currentData = null;
			}
			else {
				int n1 = data.length;
				currentData = new double[n1][];
				for (int i = 0; i < n1; i++) {
					currentData[i] = Arrays.copyOf(data[i], data[i].length);
				}
			}
		}	
	}

	/**
	 * @return the autoScale
	 */
	public boolean isAutoScale() {
		return autoScale;
	}

	/**
	 * @param autoScale the autoScale to set
	 */
	public void setAutoScale(boolean autoScale) {
		this.autoScale = autoScale;
	}

	/**
	 * @return the plotPane
	 */
	public PlotPane getPlotPane() {
		return plotPane;
	}

	/**
	 * @return the spectrogramColours
	 */
	public StandardPlot2DColours getSpectrogramColours() {
		return spectrogramColours;
	}

	/**
	 * @return the currentData
	 */
	public double[][] getCurrentData() {
		return currentData;
	}
	
	/**
	 * @return the paintPeakPos
	 */
	public boolean isPaintPeakPos() {
		return paintPeakPos;
	}


	/**
	 * @param paintPeakPos the paintPeakPos to set
	 */
	public void setPaintPeakPos(boolean paintPeakPos) {
		this.paintPeakPos = paintPeakPos;
	}
	
	public void clearLineData(boolean repaint) {
		synchronized (lineDatas) {
			lineDatas.clear();
		}
		synchronized (symbolDatas) {
			symbolDatas.clear();
		}
		if (repaint) {
			repaintLater();
		}
	}
	
	public void addLineData(SimpleLineData lineData, boolean repaint) {
		synchronized (lineDatas) {
			lineDatas.add(lineData);
		}
		if (repaint) {
			repaintLater();
		}
	}

	/**
	 * @return the peakPoint
	 */
	public javafx.geometry.Point2D getPeakPoint() {
		return peakPoint;
	}

	/**
	 * @param peakPoint the peakPoint to set
	 */
	public void setPeakPoint(javafx.geometry.Point2D peakPoint) {
		this.peakPoint = peakPoint;
	}

	/**
	 * @return the peakSymbol
	 */
	public PamSymbolFX getPeakSymbol() {
		return peakSymbol;
	}

	/**
	 * @param peakSymbol the peakSymbol to set
	 */
	public void setPeakSymbol(PamSymbolFX peakSymbol) {
		this.peakSymbol = peakSymbol;
	}
	
}
