package detectionPlotFX.plots.simple2d;

import javafx.scene.paint.Paint;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/**
 * Class to hold information for plotting a line on top of 
 * the simple2Dplot. 
 * 
 * @author Doug Gillespie
 *
 */
public class SimpleLineData {

	private double[] xValues;
	
	private double[] yValues;
	
	private PamAxisFX xAxis;

	private PamAxisFX yAxis;
	
	private Paint lineStroke;
	
	private Double lineWidth;

	public SimpleLineData(double[] xValues, double[] yValues, PamAxisFX xAxis, PamAxisFX yAxis) {
		super();
		this.xValues = xValues;
		this.yValues = yValues;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}

	public SimpleLineData(double[] xValues, double[] yValues, PamAxisFX xAxis, PamAxisFX yAxis, Paint lineStroke,
			Double lineWidth) {
		super();
		this.xValues = xValues;
		this.yValues = yValues;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.lineStroke = lineStroke;
		this.lineWidth = lineWidth;
	}

	/**
	 * @return the lineStroke
	 */
	public Paint getLineStroke() {
		return lineStroke;
	}

	/**
	 * @param lineStroke the lineStroke to set
	 */
	public void setLineStroke(Paint lineStroke) {
		this.lineStroke = lineStroke;
	}

	/**
	 * @return the lineWidth
	 */
	public double getLineWidth() {
		return (lineWidth == null ? 1 : lineWidth);
	}

	/**
	 * @param lineWidth the lineWidth to set
	 */
	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * @return the xValues
	 */
	public double[] getxValues() {
		return xValues;
	}

	/**
	 * @return the yValues
	 */
	public double[] getyValues() {
		return yValues;
	}

	/**
	 * @return the xAxis
	 */
	public PamAxisFX getxAxis() {
		return xAxis;
	}

	/**
	 * @return the yAxis
	 */
	public PamAxisFX getyAxis() {
		return yAxis;
	}
	
}
