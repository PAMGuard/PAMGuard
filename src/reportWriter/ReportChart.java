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



package reportWriter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * @author mo55
 *
 */
public class ReportChart {

	/** The chart, sitting on the FX thread */
	ReportChartFX fxChart;
	
	/**
	 * Main constructor.  This initializes the FX Toolkit by creating a JFXPanel, so
	 * that we can then use the JFX objects later
	 * 
	 * @param title The title of the chart (can be null)
	 */
	public  ReportChart(String title) {
		// we can only create a Scene in a JavaFX thread
		SwingUtilities.invokeLater(() -> {

			// Initialize FX Toolkit, so that we can use the Java FX objects
			new JFXPanel();
		});
		fxChart = new ReportChartFX(title);
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
	 * Return a BufferedImage of the chart.
	 * 
	 * @return an image of the chart, or null if there was a problem
	 */
	public BufferedImage getImage() {
		return fxChart.getImage();
	}
	
	/**
	 * Set the axis titles.  If this method is not called, the axes will be
	 * unlabelled
	 * 
	 * @param xTitle
	 * @param yTitle
	 */
	public void setAxisTitles(String xTitle, String yTitle) {
		fxChart.setAxisTitles(xTitle, yTitle);
	}

	/**
	 * Set the x axis parameters.  If this method is not called, the parameters will
	 * be generated automatically
	 * 
	 * @param lower the value of the lower bound of the axis
	 * @param upper the value of the upper bound of the axis
	 * @param tick the value between each major tick mark
	 */
	public void setXAxisParams(double lower, double upper, double tick) {
		fxChart.setXAxisParams(lower, upper, tick);
	}

	/**
	 * Set the y axis parameters.  If this method is not called, the parameters will
	 * be generated automatically
	 * 
	 * @param lower the value of the lower bound of the axis
	 * @param upper the value of the upper bound of the axis
	 * @param tick the value between each major tick mark
	 */
	public void setYAxisParams(double lower, double upper, double tick) {
		fxChart.setYAxisParams(lower, upper, tick);
	}
	
	/**
	 * Return a link to the chart's x-axis object.  Can be used for advanced
	 * control of the x axis parameters
	 * @return NumberAxis representing the x-axis
	 */
	public NumberAxis getXAxisObject() {
		return fxChart.getXAxisObject();
	}

	/**
	 * Return a link to the chart's y-axis object.  Can be used for advanced
	 * control of the y axis parameters
	 * @return NumberAxis representing the y-axis
	 */
	public NumberAxis getyAxisObject() {
		return fxChart.getYAxisObject();
	}

	
	
	/**
	 * Inner class that does all of the work.  This uses a lot of JavaFX objects, and so
	 * before we create it the FX Toolkit needs to have been initialized.  That's why we
	 * couldn't simply declare the FX fields (like NumberAxis and LineChart) in the outer
	 * class - they would have been created before the constructor was run, and therefore before
	 * the FX Toolkit had been initialized
	 * 
	 * @author mo55
	 *
	 */
	public class ReportChartFX {

		/** a list of the data series to be shown on the chart */
		private ArrayList<XYChart.Series<Number, Number>> seriesList = new ArrayList<XYChart.Series<Number, Number>>();

		/** the x axis */
		private NumberAxis xAxis = new NumberAxis();

		/** the y axis */
		private NumberAxis yAxis = new NumberAxis();

		/** the chart object */
		private LineChart<Number,Number> lineChart;

		/** the buffered image of the chart */
		private BufferedImage newImage = null;

		Runnable javaFXThread;

		/**
		 * Main Constructor
		 * 
		 * @param chartTitle The title of the chart - can be null
		 */
		private ReportChartFX(String chartTitle) {
			xAxis.setAutoRanging(true);
			yAxis.setAutoRanging(true);
			lineChart = new LineChart<Number,Number>(xAxis,yAxis);
			lineChart.setTitle(chartTitle);
			lineChart.setAnimated(false);
		}

		/**
		 * Add a data series to the chart
		 * 
		 * @param name A descriptor of the series (required)
		 * @param x The x-values
		 * @param y	The y-values
		 */
		private void addSeries(String name, double[] x, double[] y) {
			XYChart.Series<Number, Number> seriesData = new XYChart.Series<Number, Number>();
			seriesData.getData().clear();
			seriesData.setName(name);
			for (int i=0; i<x.length; i++) {
				seriesData.getData().add(new XYChart.Data<Number, Number>(x[i], y[i]));
			}
			seriesList.add(seriesData);
		}

		/**
		 * Set the axis titles
		 * @param xTitle
		 * @param yTitle
		 */
		private void setAxisTitles(String xTitle, String yTitle) {
			xAxis.setLabel(xTitle);
			yAxis.setLabel(yTitle);
		}

		/**
		 * Set the x axis parameters
		 * @param lower the value of the lower bound of the axis
		 * @param upper the value of the upper bound of the axis
		 * @param tick the value between each major tick mark
		 */
		private void setXAxisParams(double lower, double upper, double tick) {
			xAxis.setAutoRanging(false);
			xAxis.setLowerBound(lower);
			xAxis.setUpperBound(upper);
			xAxis.setTickUnit(tick);
		}

		/**
		 * Set the y axis parameters
		 * @param lower the value of the lower bound of the axis
		 * @param upper the value of the upper bound of the axis
		 * @param tick the value between each major tick mark
		 */
		private void setYAxisParams(double lower, double upper, double tick) {
			yAxis.setAutoRanging(false);
			yAxis.setLowerBound(lower);
			yAxis.setUpperBound(upper);
			yAxis.setTickUnit(tick);
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
		 * Starts a JavaFX thread to create the chart and convert it into a BufferedImage
		 */
		private void generateImage() {
			lineChart.getData().clear();
			if (seriesList.size()==0) {	// immediately return if we don't have any data - getImage will timeout after a second and return null
				return;
			} else if (seriesList.size()==1) {
				lineChart.setLegendVisible(false);	// don't bother showing a legend if there is only 1 series
			} else {
				lineChart.setLegendVisible(true);
			}

			for (int i=0; i<seriesList.size(); i++) {
				lineChart.getData().add(seriesList.get(i));
			}

			// we can only create a Scene in a JavaFX thread
//			SwingUtilities.invokeLater(() -> {
				Platform.runLater(() -> {
					Scene scene = new Scene(lineChart,800,600);
					newImage = SwingFXUtils.fromFXImage(scene.snapshot(null), null);
				});
//			});
		}
		
		private NumberAxis getXAxisObject() {
			return xAxis;
		}
		
		private NumberAxis getYAxisObject() {
			return yAxis;
		}

	}
}
