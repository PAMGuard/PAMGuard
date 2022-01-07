package qa.chart.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import Layout.PamAxis;
import PamView.chart.PamChartSeries;
import PamView.chart.swing.SwingChartRenderer;
import qa.chart.QAPamChart;

public class QASwingChartRenderer extends SwingChartRenderer {

	private QAPamChart qaPamChart;

	/**
	 * @param qaPamChart
	 */
	public QASwingChartRenderer(QAPamChart qaPamChart) {
		super(qaPamChart);
		this.qaPamChart = qaPamChart;
	}

	@Override
	public boolean renderborder(Graphics g, Dimension dimension, Insets plotInsets, boolean drawBox) {
		boolean ok = super.renderborder(g, dimension, plotInsets, drawBox);
		PamAxis missAx = qaPamChart.getMissHistAxes();
		if (missAx != null) {
			int axH = (int) ((dimension.getHeight() - plotInsets.bottom - plotInsets.top) /3);
			missAx.drawAxis(g, dimension.width-plotInsets.right, dimension.height-plotInsets.bottom, 
					dimension.width-plotInsets.right,dimension.height-plotInsets.bottom-axH);
		}
		PamAxis hitAx = qaPamChart.getHitHistAxes();
		if (hitAx != null) {
			int axH = (int) ((dimension.getHeight() - plotInsets.bottom - plotInsets.top) /3);
			hitAx.drawAxis(g, dimension.width-plotInsets.right, plotInsets.top+axH, 
					dimension.width-plotInsets.right,plotInsets.top);
		}
		return ok;
	}

	@Override
	protected void plotBarChart(Graphics g, Dimension dimension, Insets plotInsets, PamChartSeries series, PamAxis xAxis, PamAxis yAxis) {
		if (series == qaPamChart.getMissedSeries()) {
			yAxis = qaPamChart.getMissHistAxes();
		}
		if (series == qaPamChart.getHitSeries()) {
			yAxis = qaPamChart.getHitHistAxes();
		}
		super.plotBarChart(g, dimension, plotInsets, series, xAxis, yAxis);
	}

	@Override
	public Insets calculateInsets(Graphics g) {
		Insets ins = super.calculateInsets(g);
		if (qaPamChart.getMissHistAxes() != null) {
			int me = qaPamChart.getMissHistAxes().getExtent(g);
			ins.right = Math.max(ins.right, me);
		}
//		ins.right = 60;
		return ins;
	}
}
