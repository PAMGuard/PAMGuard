package qa.chart;

import java.awt.Color;

import Layout.PamAxis;
import PamView.PamSymbolType;
import PamView.chart.PamChart;
import PamView.chart.PamChartSeries;
import PamView.chart.PamChartSeriesException;
import PamView.chart.SeriesType;
import PamView.symbol.SymbolData;
import pamMaths.PamHistogram;

public class QAPamChart extends PamChart {
	
	public PamAxis hitHistAxes, missHistAxes;
	private PamChartSeries missedSeries, hitSeries;

	public QAPamChart(PamAxis southAxis, PamAxis westAxis) {
		super(southAxis, westAxis);
	}

	public PamChartSeries addMissHistogram(PamHistogram missHistogram) {
		return addMissHistogram(missHistogram, "Missed");
	}
	public PamChartSeries addMissHistogram(PamHistogram missHistogram, String title) {
		double[] yValues = missHistogram.getData();
		double[] xValues = missHistogram.getBinEdgeValues();
		double maxVal = 1;
		for (int i = 0; i < yValues.length; i++) {
//			xValues[i] = missHistogram.getBinCentre(i);
			maxVal = Math.max(maxVal, yValues[i]);
		}
		if (missedSeries != null) {
			this.removeSeries(missedSeries);
			missedSeries = null;
		};
		try {
			missedSeries = new PamChartSeries(SeriesType.BOTTOMBAR, title, xValues, yValues);
			missedSeries.setSymbolData(new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 1, 1, 2, true, Color.LIGHT_GRAY, Color.DARK_GRAY));
			this.addSeries(missedSeries);
		} catch (PamChartSeriesException e) {
			e.printStackTrace();
		}
		missHistAxes = new PamAxis(0, 0, 0, 1, 0, Math.ceil(maxVal+1), PamAxis.BELOW_RIGHT, "Misses", PamAxis.LABEL_NEAR_CENTRE, "%3d");
		if (missHistAxes != null && hitHistAxes != null) {
			equaliseAxis();
		}
		return missedSeries;
	}

	public PamChartSeries addHitHistogram(PamHistogram hitHistogram) {
		return addHitHistogram(hitHistogram, "Hits");
	}
	public PamChartSeries addHitHistogram(PamHistogram hitHistogram, String title) {
		double[] yValues = hitHistogram.getData();
		double[] xValues;// = new double[yValues.length];
		double maxVal = 1;
		for (int i = 0; i < yValues.length; i++) {
//			xValues[i] = hitHistogram.getBinEdgeValues(i);
			maxVal = Math.max(maxVal, yValues[i]);
		}
		xValues = hitHistogram.getBinEdgeValues();
		if (hitSeries != null) {
			this.removeSeries(hitSeries);
			hitSeries = null;
		};
		try {
			hitSeries = new PamChartSeries(SeriesType.HANGINGBAR, title, xValues, yValues);
			hitSeries.setSymbolData(new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 1, 1, 2, true, Color.LIGHT_GRAY, Color.DARK_GRAY));
			this.addSeries(hitSeries);
		} catch (PamChartSeriesException e) {
			e.printStackTrace();
		}
		hitHistAxes = new PamAxis(0, 0, 0, 1,  Math.ceil(maxVal+1), 0, PamAxis.BELOW_RIGHT, "Hits", PamAxis.LABEL_NEAR_CENTRE, "%3d");		if (missHistAxes != null && hitHistAxes != null) {
			equaliseAxis();
		}
		return hitSeries;
	}

	private void equaliseAxis() {
		double m = Math.max(hitHistAxes.getMinVal(), missHistAxes.getMaxVal());
		hitHistAxes.setMinVal(m);
		missHistAxes.setMaxVal(m);
		hitHistAxes.setInterval(-m);
		missHistAxes.setInterval(m);
	}

	/**
	 * @return the missHistAxes
	 */
	public PamAxis getMissHistAxes() {
		return missHistAxes;
	}

	/**
	 * @return the missedSeries
	 */
	public PamChartSeries getMissedSeries() {
		return missedSeries;
	}

	/**
	 * @return the hitHistAxes
	 */
	public PamAxis getHitHistAxes() {
		return hitHistAxes;
	}

	/**
	 * @return the hitSeries
	 */
	public PamChartSeries getHitSeries() {
		return hitSeries;
	}
	
	

}
