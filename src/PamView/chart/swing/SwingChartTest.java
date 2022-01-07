package PamView.chart.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Layout.PamAxis;
import PamView.PamSymbolType;
import PamView.chart.PamChart;
import PamView.chart.PamChartSeries;
import PamView.chart.PamChartSeriesException;
import PamView.chart.SeriesType;
import PamView.symbol.SymbolData;

public class SwingChartTest {

	public static void main(String[] args) {
		SwingChartTest chartTest = new SwingChartTest();
		chartTest.run();
	}

	private PamChart pamChart;

	private void run() {
		
		PamAxis xAx = new PamAxis(0, 1, 0, 1, 0, 100, PamAxis.BELOW_RIGHT, "Test X Axis", PamAxis.LABEL_NEAR_CENTRE, "%d");
		PamAxis yAx = new PamAxis(0, 1, 0, 1, -20, 20, PamAxis.ABOVE_LEFT, "Test Y Axis", PamAxis.LABEL_NEAR_CENTRE, "%d");
		pamChart = new PamChart(xAx, yAx);
		pamChart.setChartTitle("Test chart");
		double[] x = {1, 3, 30, 60};
		double[] y = {-20, 10, 0, -5};
		double[] xBar = {5, 15, 25, 35, 45};
		double[] yBar = {2, 10, 16, 8, -6};
		try {
			PamChartSeries barSeries = new PamChartSeries(SeriesType.BOTTOMBAR, "Bar chart", xBar, yBar);
			pamChart.addSeries(barSeries);
			barSeries.setSymbolData(new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, Color.GRAY, Color.RED));
			
			PamChartSeries series = new PamChartSeries(SeriesType.LINE, "Test 1", x, y);
			series.setLineColor(Color.RED);
//			series.setSymbolData(new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, Color.BLUE, Color.MAGENTA));
			pamChart.addSeries(series);
		} catch (PamChartSeriesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JFrame jFrame = new JFrame("Pam Plot Test");
		jFrame.setLayout(new BorderLayout());
		jFrame.add(new PlotPanel(), BorderLayout.CENTER);
		jFrame.setSize(new Dimension(900,  500));
		jFrame.setVisible(true);
	}
	
	private class PlotPanel extends JPanel {

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			SwingChartRenderer scr = new SwingChartRenderer(pamChart);
			try {
				scr.renderChart(g, new Dimension(g.getClipBounds().width, g.getClipBounds().height));
			} catch (ChartRenderException e) {
				e.printStackTrace();
			}
		}
		
	}

}
