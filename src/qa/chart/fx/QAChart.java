/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package qa.chart.fx;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;

/**
 * Create a chart showing missed detections as bars along the bottom, found detections as
 * bars along the top (inverted), and a probability line graph through the middle (overlayed
 * on top of everything).
 * 
 * Used a number of websites for code examples...
 * 
 * How to layer charts using a StackPane:
 * https://gist.github.com/jewelsea/3668862
 * 
 * There is a problem with the x-axis not lining up when you have y-axis on both left and right sides:
 * https://stackoverflow.com/questions/30204122/aligning-the-primary-and-secondary-x-axis-in-stackpane-xychart-in-javafx
 * 
 * To fix problem of x-axis not lining up (resizing charts to make room for y-axis on left and right):
 * https://gist.github.com/MaciejDobrowolski/9c99af00668986a0a303#file-style-css
 * 
 * 
 * 
 * 
 * @author mo55
 *
 */
public class QAChart {

	/** The chart, sitting on the FX thread */
	private ReportChartFX fxChart;
	

	private XYChart.Series<String, Number> primarySeries;
	
	private LineChart<String, Number> primaryChart;

	/**
	 * Main constructor.  Need to pass in all of the data right away.
	 * 
	 * @param title The title of the chart (not currently used, as it overlaps with the inverted axis)
	 * @param xCat range values to be used along the x-axis
	 * @param yDetected the number of detected sounds at each range location - length must match xCat vector
	 * @param yMissed the number of missed sounds at each range location - length must match xCat vector
	 */
	public QAChart(String title, int[] xCat, int[] yDetected, int[] yMissed) {
		if (yDetected.length != xCat.length || yMissed.length != xCat.length) {
			fxChart = null;
		}
		
		else {
			// we can only create a Scene in a JavaFX thread
			SwingUtilities.invokeLater(() -> {

				// Initialize FX Toolkit, so that we can use the Java FX objects
				new JFXPanel();
			});

			// create the actual chart
			fxChart = new ReportChartFX(title, xCat, yDetected, yMissed);
		}
	}

	/**
	 * Return a BufferedImage of the chart.
	 * 
	 * @return an image of the chart, or null if there was a problem
	 */
	public BufferedImage getImage() {
		if (fxChart==null) {
			return null;
		}
		return fxChart.getImage();
	}

	public void setAxisTitles(String xTitle, String yTitle) {
		fxChart.setAxisTitles(xTitle, yTitle);
	}
	
	/**
	 * Add a data series to the chart
	 * 
	 * @param seriesName A descriptor of the series (required)
	 * @param xVals The x-values (must be the same size as yVals)
	 * @param yVals The y-values (must be the same size as xVals)
	 */
	public void addSeries(String seriesName, double[] xVals, double[] yVals) {
		if (xVals.length != yVals.length || seriesName==null ) {
			return;
		}
		fxChart.addSeries(seriesName, xVals, yVals);
	}
	/**
	 * Inner class - does all the work.
	 * getImage() and generateImage() methods copied from reportWriter.ReportChart
	 * 
	 * @author mo55
	 *
	 */
	public class ReportChartFX {
		
		/** The x-axis - defined as a category for the bar chart, but uses numbers that are converted to strings */
		private CategoryAxis axisPrimary;
		
		/** The first chart in the stack.  The other charts are placed on top of this one, with transparent backgrounds */
		private LineChart<String, Number> baseChart;
		
		/** A list of the charts in the stack */
		private ArrayList<XYChart<String,Number>> charts = new ArrayList<>();
		
		/** The width of the y-axis */
		private double yAxisWidth = 50.0;
		
		/** The overall chart, with all the individual charts stacked on top of each other */
		private StackPane fullChart;
		
		/** The maximum y-axis value */
		private int maxYAxis;
		
		/** The tick values along the y-axis */
		private int tickVal;

		/** the buffered image of the chart */
		private BufferedImage newImage = null;

		/** Relative location of the CSS style sheet to be used for the chart */
		// private String overlayChartCSS = "/qa/overlay-chart.css";
		private String overlayChartCSS = "qachart.css";
		
		/** the y axis */
		private NumberAxis yAxis;

		
		
		/**
		 * Main constructor
		 * 
		 * @param title NOT USED RIGHT NOW - overlapping with inverted series
		 * @param yDetected 
		 * @param yMissed 
		 * @param xCat 
		 */
		private ReportChartFX(String title, int[] xCat, int[] yDetected, int[] yMissed) {
			axisPrimary = new CategoryAxis();
			
			// get the largest y value (rounded up to nearest 100) to set the scales
			int maxDet = Arrays.stream(yDetected).max().getAsInt();
			int maxMiss = Arrays.stream(yMissed).max().getAsInt();
			maxYAxis = ((Math.max(maxDet, maxMiss)  + 99) / 100 ) * 100;
			tickVal = maxYAxis / 4;
			
			
			// create the probability series and generate the base chart
			double[] detectProb = new double[xCat.length];
			for (int i=0; i<xCat.length; i++) {
				detectProb[i] = (double) yDetected[i]/(yDetected[i]+yMissed[i]);
			}
			baseChart = createProbChart(xCat, detectProb);	// must create this one first

			// generate the other 2 charts and stack them all together
			charts.add(createSecondaryChart(xCat,yDetected,true));
			charts.add(createSecondaryChart(xCat, yMissed,false));
			charts.add(baseChart);
			fullChart = layerCharts(charts);
		}
	

		/**
		 * Create the regular (non-inverted) y axis.  Formatted so that it's double the height of the
		 * maximum y value from the data, but only half of the labels are shown.  That way this axis
		 * and the inverted axis can overlap and the labels won't interfere
		 * 
		 * @return a number axis
		 */
		private NumberAxis createYAxis() {
			final NumberAxis axis = new NumberAxis(0, maxYAxis*2, tickVal);
			axis.setPrefWidth(yAxisWidth);
			axis.setMinorTickCount(2);
			axis.setSide(Side.RIGHT);

			axis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(axis) {
				@Override
				public String toString(Number object) {
					float theValue = object.floatValue();
					String stringToShow;
					if (theValue <= maxYAxis) {
						stringToShow = String.format("%7.0f", theValue);
					}
					else {
						stringToShow = "";
					}
					return stringToShow;
				}
			});
			return axis;
		}

		/**
		 * Create the inverted y axis.  Formatted so that it's double the height of the
		 * maximum y value from the data, but only half of the labels are shown.  That way this axis
		 * and the regular axis can overlap and the labels won't interfere.
		 * Note that in order to invert, what we actually do is use negative values but display them
		 * as positive.  This also means that we need to convert all the y-data to negative before
		 * plotting.
		 * 
		 * @return a number axis
		 */
		private NumberAxis createInvertYAxis() {
			final NumberAxis axis = new NumberAxis(-maxYAxis*2, 0, tickVal);
			axis.setPrefWidth(yAxisWidth);
			axis.setMinorTickCount(2);
			axis.setSide(Side.RIGHT);
			axis.setLabel("Frequency");
			axis.lookup(".axis-label").setStyle("-fx-label-padding: 0 0 -30 0;");

			axis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(axis) {
				@Override
				public String toString(Number object) {
					float theValue = object.floatValue();
					String stringToShow;
					if (theValue == 0) {
						stringToShow = String.format("%7.0f", theValue);
					}
					else if (theValue >= -maxYAxis) {
						stringToShow = String.format("%7.0f", -theValue);
					}
					else {
						stringToShow = "";
					}
					return stringToShow;
				}
			});

			return axis;
		}
		
		/**
		 * Create the probability axis
		 * 
		 * @return a number axis
		 */
		private NumberAxis createYProbAxis() {
			final NumberAxis axis = new NumberAxis(0, 1, .2);
			axis.setPrefWidth(yAxisWidth);
			axis.setMinorTickCount(2);
			axis.setLabel("Probability");
			axis.lookup(".axis-label").setStyle("-fx-label-padding: -15 0 0 0;");
			return axis;
		}

		/**
		 * Create the x axis
		 * @return
		 */
		private CategoryAxis createXaxis() {
			axisPrimary = new CategoryAxis();
//			axisPrimary.setLabel("Range");
			return axisPrimary;
		}

		
		/**
		 * Create the primary (probability) chart.  This one needs to be first, because we link the x-axis
		 * of the secondary charts to this x-axis so that everything stays properly lined up
		 * 
		 * @param xCat the x category values
		 * @param detectProb the y probability values
		 * @return a line chart
		 */
		private LineChart<String, Number> createProbChart(int[] xCat, double[] detectProb) {
			yAxis = createYProbAxis();
			primaryChart = new LineChart<String, Number>(axisPrimary, yAxis);
			primarySeries = new XYChart.Series<>();
			for (int i=0; i<xCat.length; i++) {
				XYChart.Data<String, Number> data;
				data = new XYChart.Data<>(String.valueOf(xCat[i]), detectProb[i]);
//				series.getData().add(data);
			}
//			primaryChart.getData().add(primarySeries);

			primaryChart.setCreateSymbols(false);

			return primaryChart;
		}


		/**
		 * Create the secondary charts (the detected and missed charts).  Note that if we are
		 * working with the inverted chart, we need to multiply each y data point by -1 so that
		 * the bar stretches downwards and not upwards.
		 * 
		 * @param xCat the x category values
		 * @param yVals the y values
		 * @param isInverted whether or not this is an inverted chart
		 * 
		 * @return a bar chart
		 */
		private BarChart<String, Number> createSecondaryChart(int[] xCat, int[] yVals, boolean isInverted) {
	        final BarChart<String, Number> chart;
	        int modifier = 1;
	        if (isInverted) {
	        	chart = new BarChart<String, Number>(axisPrimary, createInvertYAxis());
	        	modifier = -1;
	        	
	        }
	        else {
	        	chart = new BarChart<String, Number>(axisPrimary, createYAxis());
	        }
	        
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			for (int i=0; i<xCat.length; i++) {
				XYChart.Data<String, Number> data;
				data = new XYChart.Data<>(String.valueOf(xCat[i]), modifier*yVals[i]);
				series.getData().add(data);
			}
			chart.getData().add(series);
			chart.setBarGap(0);
			chart.setCategoryGap(0);

			chart.translateXProperty().bind(baseChart.getYAxis().widthProperty());
			chart.getYAxis().setTranslateX(yAxisWidth);
	        
			return chart;
		}
		

		/**
		 * Layer all of the charts on top of each other, in the order the charts are in the charts arraylist.  Note that
		 * the baseChart object needs to be in the list last, or else it will be under the secondary charts instead of
		 * on top of the secondary charts.
		 * 
		 * @param charts ArrayList of charts to layer
		 * 
		 * @return the layered chart
		 */
		private StackPane layerCharts(ArrayList<XYChart<String, Number>> charts) {
			StackPane bigChart = new StackPane();
			for (int i = 0; i < charts.size(); i++) {
				charts.get(i).getStylesheets().addAll(getClass().getResource(overlayChartCSS).toExternalForm());
				if (i>0) {
					configureOverlayChart(charts.get(i));
				}
				StackPane.setAlignment(charts.get(i), Pos.CENTER_LEFT);
				bigChart.getChildren().add(charts.get(i));
			}

			return bigChart;
		}

		/**
		 * Configure the overlayed charts
		 * 
		 * @param chart the chart to configure
		 */
		private void configureOverlayChart(final XYChart<String, Number> chart) {
			chart.setAlternativeRowFillVisible(false);
			chart.setAlternativeColumnFillVisible(false);
			chart.setHorizontalGridLinesVisible(false);
			chart.setVerticalGridLinesVisible(false);
			chart.getXAxis().setVisible(false);
			chart.getYAxis().setVisible(false);
			chart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
		}
		
		/**
		 * Resize all the charts.  If we don't do this, the x-axis categories will not line up between
		 * the chart with the primary y axis and the charts with the secondary y-axes.  We have to force space
		 * on both sides of the chart for the axes (normally, the chart is sized for an axis on one side and
		 * then stretches to the edge on the other side).  
		 */
		private void rebuildChart() {
			ObservableList<Node> allCharts = fullChart.getChildren();
			for (int i=0; i<allCharts.size(); i++) {
				XYChart<String,Number> aChart = (XYChart<String, Number>) allCharts.get(i);
				aChart.setLegendVisible(false);
				aChart.setAnimated(false);
				aChart.minWidthProperty().bind(fullChart.widthProperty().subtract(yAxisWidth*3));
				aChart.prefWidthProperty().bind(fullChart.widthProperty().subtract(yAxisWidth*3));
				aChart.maxWidthProperty().bind(fullChart.widthProperty().subtract(yAxisWidth*3));
			}
		}
		
		/**
		 * Return a BufferedImage of the chart.  Note that because the image is actually
		 * being generated in a JavaFX thread, this method sits and waits until the
		 * newImage object has been created.  It times-out and simply returns null if
		 * it takes longer than 5 seconds or there is an exception thrown.
		 * 
		 * @return an image of the chart, or null if there was a problem
		 */
		private BufferedImage getImage() {
			newImage = null;
			generateImage();
			int numTries = 0;
			while (newImage==null && numTries<50) {
				try {
					Thread.sleep(1000);
//					System.out.println("Waiting for image, numTries = " + numTries);
					numTries++;
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
			return newImage;
		}

		/**
		 * Set the axis titles.  If this method is not called, the axes will be
		 * unlabelled
		 * 
		 * @param xTitle
		 * @param yTitle
		 */
		public void setAxisTitles(String xTitle, String yTitle) {
			if (axisPrimary != null) {
				axisPrimary.setLabel(xTitle);
			}
			if (yAxis != null) {
				yAxis.setLabel(yTitle);
			}
		}
		/**
		 * Add a data series to the chart
		 * 
		 * @param name A descriptor of the series (required)
		 * @param x The x-values
		 * @param y	The y-values
		 */
		private void addSeries(String name, double[] x, double[] y) {
			XYChart.Series<String, Number> seriesData = new XYChart.Series();
			seriesData.getData().clear();
			seriesData.setName(name);
			for (int i=0; i<x.length; i++) {
				/*
				 * 
				XYChart.Data<String, Number> data;
				data = new XYChart.Data<>(String.valueOf(xCat[i]), detectProb[i]);
				 */
				seriesData.getData().add(new XYChart.Data(String.valueOf(x[i]), y[i]));
			}
			primaryChart.getData().add(seriesData);
		}

		/**
		 * Starts a JavaFX thread to create the chart and convert it into a BufferedImage
		 */
		private void generateImage() {

			// we can only create a Scene in a JavaFX thread
//			SwingUtilities.invokeLater(() -> {
				Platform.runLater(() -> {
					Scene scene = new Scene(fullChart,800,600);
					newImage = SwingFXUtils.fromFXImage(scene.snapshot(null), null);
				});
//			});
		}
		
	}


	/**
	 * @return the primaryChart
	 */
	public XYChart.Series<String, Number> getPrimaryChart() {
		return primarySeries;
	}
}
