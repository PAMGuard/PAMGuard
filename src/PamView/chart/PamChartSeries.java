package PamView.chart;

import java.awt.Color;

import Layout.PamAxis;
import PamView.PamSymbolBase;
import PamView.symbol.SymbolData;

/**
 * Series data for PAmChart.
 * @author dg50
 *
 */
public class PamChartSeries {
	
	private double[] xValues, yValues;
	
	private SeriesType seriesType;
	
	private String seriesName;
	
	/**
	 * Color for lines. If null, symbol line colour should be used. 
	 */
	private Color lineColor;
	
	private float lineWidth;
	
	private SymbolData symbolData;
	
	private PamAxis seriesYAxis;

	/**
	 * Make a chart series. For bar charts, if the x array is the same length 
	 * as the y array, assume the values refer to bin centres. If the x array is 
	 * 1 greater than the y array length, assume they are histogram bin edges. 
	 * @param xValues array of x axis values
	 * @param yValues array of y axis values
	 */
	public PamChartSeries(SeriesType seriesType, String seriesName, double[] xValues, double[] yValues) throws PamChartSeriesException {
		super();
		this.seriesType = seriesType;
		this.seriesName = seriesName;
		this.xValues = xValues;
		this.yValues = yValues;
		if (xValues == null || yValues == null) {
			throw new PamChartSeriesException("x and y arrays cannot be null");
		}
		switch (seriesType) {
		case BOTTOMBAR:
		case HANGINGBAR:
			if (xValues.length != yValues.length && xValues.length != yValues.length+1) {
				throw new PamChartSeriesException("For bar charts, x values array must be the same length of one longer than the y values array");
			}
			break;
		case LINE:
		case SCATTER:
			if (xValues.length != yValues.length) {
				throw new PamChartSeriesException("For line charts, x values array must be the same length as the y values array");
			}
			break;
		default:
			break;
		
		}
	}
	
	/**
	 * Sort the series into order by X value 
	 * (needed if plotting lines, not important for scatter)
	 */
	public void sortByX() {
		double[][] xyData = {xValues, yValues};
		xyData = PairedSort.sortPairedArray(xyData);
		xValues = xyData[0];
		yValues = xyData[1];
	}

	/**
	 * @return the xValues
	 */
	public double[] getxValues() {
		return xValues;
	}

	/**
	 * @param xValues the xValues to set
	 */
	public void setxValues(double[] xValues) {
		this.xValues = xValues;
	}

	/**
	 * @return the yValues
	 */
	public double[] getyValues() {
		return yValues;
	}

	/**
	 * @param yValues the yValues to set
	 */
	public void setyValues(double[] yValues) {
		this.yValues = yValues;
	}

	/**
	 * @return the seriesType
	 */
	public SeriesType getSeriesType() {
		return seriesType;
	}

	/**
	 * @param seriesType the seriesType to set
	 */
	public void setSeriesType(SeriesType seriesType) {
		this.seriesType = seriesType;
	}

	/**
	 * @return the pamSymbolBase
	 */
	public SymbolData getSymbolData() {
		return symbolData;
	}

	/**
	 * @param pamSymbolBase the pamSymbolBase to set
	 */
	protected void setPamSymbolBase(SymbolData symbolData) {
		this.symbolData = symbolData;
	}

	/**
	 * @return the lineColor
	 */
	public Color getLineColor() {
		if (lineColor != null) {
			return lineColor;
		}
		if (symbolData != null) {
			return symbolData.getLineColor();
		}
		return Color.BLACK;
	}

	/**
	 * @param lineColor the lineColor to set
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	/**
	 * @return the seriesYAxis
	 */
	public PamAxis getSeriesYAxis() {
		return seriesYAxis;
	}

	/**
	 * @param seriesYAxis the seriesYAxis to set
	 */
	public void setSeriesYAxis(PamAxis seriesYAxis) {
		this.seriesYAxis = seriesYAxis;
	}

	/**
	 * @param symbolData the symbolData to set
	 */
	public void setSymbolData(SymbolData symbolData) {
		this.symbolData = symbolData;
	}

	/**
	 * @return the seriesName
	 */
	public String getSeriesName() {
		return seriesName;
	}

	/**
	 * @return the lineWidth
	 */
	public float getLineWidth() {
		return Math.max(lineWidth, 1);
	}

	/**
	 * @param lineWidth the lineWidth to set
	 */
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	
}
