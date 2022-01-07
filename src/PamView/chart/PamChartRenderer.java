package PamView.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import PamView.chart.swing.ChartRenderException;

public abstract class PamChartRenderer {

	private PamChart pamChart;

	/**
	 * @param pamChart
	 */
	public PamChartRenderer(PamChart pamChart) {
		super();
		this.pamChart = pamChart;
	}

	/**
	 * @return the pamChart
	 */
	public PamChart getPamChart() {
		return pamChart;
	}
	
	public BufferedImage renderBufferedImage(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.getGraphics().setColor(Color.WHITE);
		image.getGraphics().fillRect(0, 0, width, height);
		try {
			renderChart(image.getGraphics(), new Dimension(width, height));
		} catch (ChartRenderException e) {
			e.printStackTrace();
		}
		return image;
	}

	public abstract boolean renderChart(Graphics g, Dimension dimension) throws ChartRenderException;
	
}
