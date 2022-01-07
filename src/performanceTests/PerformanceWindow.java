package performanceTests;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Layout.PamAxisPanel;

/**
 * Simple (but reasonably pleasant) graphics window for drawing
 * in during performance tests. 
 * @author Doug Gillespie
 *
 */
public class PerformanceWindow {

	private Frame windowFrame;
	
	private PamAxisPanel axisPanel;
	
	private JPanel plotPanel;

	public PerformanceWindow(String title) {
		super();
		windowFrame = new JFrame();
		windowFrame.setTitle(title);
		axisPanel = new PamAxisPanel();
		plotPanel = new JPanel();
		plotPanel.setBackground(Color.WHITE);
		
		axisPanel.setInnerPanel(plotPanel);
		windowFrame.add(axisPanel);
		windowFrame.setSize(new Dimension(800,600));
//		windowFrame.setVisible(true);
	}
	
	public void setVisible(boolean b) {
		windowFrame.setVisible(b);
	}

	public JPanel getPlotPanel() {
		return plotPanel;
	}

	public void setPlotPanel(JPanel plotPanel) {
		if (this.plotPanel != null) {
			axisPanel.remove(this.plotPanel);
		}
		this.plotPanel = plotPanel;
		axisPanel.setInnerPanel(plotPanel);
	}

	public void setTitle(String title) {
		windowFrame.setTitle(title);
	}
	
}
