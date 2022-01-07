package performanceTests;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class GraphicsDotTestBuffered extends GraphicsDotTest {

//	private PerformanceWindow performanceWindow = new PerformanceWindow("Graphics dot test (buffered)");
	
	public GraphicsDotTestBuffered() {
		super();
		performanceWindow.setTitle("Graphics dot test (buffered)");
	}

	@Override
	public String getName() {
		return "Graphics dot test drawing to a buffered image";
	}

	@Override
	public boolean runTest() {

		performanceWindow.setVisible(true);
		JPanel plotPanel = performanceWindow.getPlotPanel();
		BufferedImage bi = new BufferedImage(plotPanel.getWidth(), plotPanel.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

		AffineTransform baseXform;
		baseXform = new AffineTransform();
		baseXform.scale(1,1);
		
		long startTime = System.currentTimeMillis();
		drawDots((Graphics2D) bi.getGraphics(), plotPanel);
		((Graphics2D) plotPanel.getGraphics()).drawImage(bi, baseXform, plotPanel );
		long endTime = System.currentTimeMillis();
		long timeTaken = endTime-startTime;
		resultString = String.format("Time taken to draw %d dots on the screen = %d ms", nDots, timeTaken);
		performanceWindow.setVisible(false);
		return true;
	}

}
