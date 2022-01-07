package performanceTests;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

public class GraphicsDotTest implements PerformanceTest {

	protected String resultString;

	protected int dotSize = 5;

	protected int nDots = 10000; 

	protected PerformanceWindow performanceWindow;

	public GraphicsDotTest() {
		super();
		performanceWindow = new PerformanceWindow("Graphics dot test (direct drawing)");
	}

	@Override
	public String getName() {
		return "Graphics dot test using direct draw";
	}

	@Override
	public String getResultString() {
		return resultString;
	}

	@Override
	public boolean runTest() {
		performanceWindow.setVisible(true);
		JPanel plotPanel = performanceWindow.getPlotPanel();
		long startTime = System.currentTimeMillis();
		drawDots((Graphics2D) plotPanel.getGraphics(), plotPanel);
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime-startTime;
		resultString = String.format("Time taken to draw %d dots on the screen = %d ms", nDots, timeTaken);
		performanceWindow.setVisible(false);
		return true;
	}
	
	protected void drawDots(Graphics2D g, JPanel panel) {

		double step = Math.sqrt(panel.getWidth() * panel.getHeight()) / Math.sqrt(nDots);
		double x = 0;
		double y = x;
		panel.getGraphics().setColor(Color.BLUE);
		//		plotPanel.getGraphics().set(Color.RED);
		for (int i = 0; i < nDots; i++) {
			Ellipse2D o = new Ellipse2D.Double(x, y, dotSize, dotSize);
			g.setPaint(Color.BLUE);
			g.fill(o);
			g.setPaint(Color.RED);
			g.draw(o);
			x += step;
			if (x >= panel.getWidth()) {
				x = 0;
				y += step;
			}
		}
	}

	@Override
	public void cleanup() {
		performanceWindow.setVisible(false);
	}

}
