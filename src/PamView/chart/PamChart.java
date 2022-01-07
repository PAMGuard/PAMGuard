package PamView.chart;

import java.util.ArrayList;

import Layout.PamAxis;

/**
 * Base class for PAMGurad chart, which can be projected into a graphics object - 
 * either a bitmap or a screen component. Will ultimately be able to support fx as
 * well as swing since the renderer class are separate to the section holding 
 * the actual data 
 * <p>This class is the one holding the data, make this, add all the data series 
 * and then render it using a ChartRenderer. 
 * @author dg50
 *
 */
public class PamChart {

	private PamAxis westAxis, southAxis, northAxis, eastAxis;
	
	private ArrayList<PamChartSeries> chartSeries = new ArrayList<>();
	
	private String chartTitle;
	
	/**
	 * @param westAxis
	 * @param southAxis
	 */
	public PamChart(PamAxis southAxis, PamAxis westAxis) {
		super();
		this.southAxis = southAxis;
		this.westAxis = westAxis;
	}
	
	public void addSeries(PamChartSeries newSeries) {
		chartSeries.add(newSeries);
	}
	
	public boolean removeSeries(PamChartSeries oldSeries) {
		return chartSeries.remove(oldSeries);
	}
	
	public void clearSeries() {
		chartSeries.clear();
	}

	/**
	 * @return the chartTitle
	 */
	public String getChartTitle() {
		return chartTitle;
	}

	/**
	 * @param chartTitle the chartTitle to set
	 */
	public void setChartTitle(String chartTitle) {
		this.chartTitle = chartTitle;
	}

	/**
	 * @return the westAxis
	 */
	public PamAxis getWestAxis() {
		return westAxis;
	}

	/**
	 * @param westAxis the westAxis to set
	 */
	public void setWestAxis(PamAxis westAxis) {
		this.westAxis = westAxis;
	}

	/**
	 * @return the southAxis
	 */
	public PamAxis getSouthAxis() {
		return southAxis;
	}

	/**
	 * @param southAxis the southAxis to set
	 */
	public void setSouthAxis(PamAxis southAxis) {
		this.southAxis = southAxis;
	}

	/**
	 * @return the northAxis
	 */
	public PamAxis getNorthAxis() {
		return northAxis;
	}

	/**
	 * @param northAxis the northAxis to set
	 */
	public void setNorthAxis(PamAxis northAxis) {
		this.northAxis = northAxis;
	}

	/**
	 * @return the eastAxis
	 */
	public PamAxis getEastAxis() {
		return eastAxis;
	}

	/**
	 * @param eastAxis the eastAxis to set
	 */
	public void setEastAxis(PamAxis eastAxis) {
		this.eastAxis = eastAxis;
	}

	/**
	 * @return the chartSeries
	 */
	public ArrayList<PamChartSeries> getChartSeries() {
		return chartSeries;
	}
	
	
	
}
