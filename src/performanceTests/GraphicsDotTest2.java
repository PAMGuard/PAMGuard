package performanceTests;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GraphicsDotTest2 extends GraphicsDotTest {

//	private PerformanceWindow performanceWindow = new PerformanceWindow("Graphics dot test (with callback)");

	private PlotWindow plotWindow;
	
	private volatile boolean paintComplete;
	
	public GraphicsDotTest2() {
		super();
		performanceWindow.setTitle("Graphics dot test (with callback)");
		plotWindow = new PlotWindow();
		performanceWindow.setPlotPanel(plotWindow);
		performanceWindow.setVisible(true);
//		performanceWindow.setVisible(false);
	}

	class PlotWindow extends JPanel {

		private static final long serialVersionUID = 1L;

		public PlotWindow() {
			super();
			setBackground(Color.WHITE);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			//		plotPanel.getGraphics().set(Color.RED);
			long startTime = System.currentTimeMillis();
			
			drawDots((Graphics2D) g, this);
			
			long endTime = System.currentTimeMillis();
			long timeTaken = endTime-startTime;
			resultString = String.format("Time taken to draw %d dots on the screen = %d ms", nDots, timeTaken);
			paintComplete = true;
		}

	}
	
	@Override
	public String getName() {
		return "Graphics dot test using callback";
	}

	@Override
	public boolean runTest() {
		performanceWindow.setVisible(true);
		paintComplete = false;
		plotWindow.repaint();
		return true;
	}

}
