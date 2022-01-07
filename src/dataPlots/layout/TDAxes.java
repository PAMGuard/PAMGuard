package dataPlots.layout;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dataPlots.TDControl;
import pamScrollSystem.PamScroller;
import Layout.PamAxis;
import PamUtils.PamCalendar;
import PamView.PamColors.PamColor;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;

/**
 * The axis panel forms the main Component of the display, The time 
 * scroller and various other components get added and controlled 
 * from TDControl though. 
 * @author Doug Gillespie
 *
 */
public class TDAxes {

	private JPanel outerPanel;
	
	private JLayeredPane layeredPane;
	
	private AxisPanel axisPanel;

	private PamPanel axisInnerPanel;

	private PamAxis timeAxis;

	private TDControl tdControl;

	private PamPanel timeRangePanel;

	public TDAxes(TDControl tdControl) {
		super();
		this.tdControl = tdControl;

		outerPanel = new JPanel(new BorderLayout());
		layeredPane = new JLayeredPane();
		outerPanel.add(BorderLayout.CENTER, layeredPane);
		CornerLayoutContraint c = new CornerLayoutContraint();
		layeredPane.setLayout(new CornerLayout(c));
		
		axisPanel = new AxisPanel();
		axisPanel.setLayout(new BorderLayout());
//		axisPanel.setBorder(new EmptyBorder(new Insets(20,20,50,10)));

		c.anchor = CornerLayoutContraint.FILL;
		layeredPane.add(axisPanel, c, JLayeredPane.DEFAULT_LAYER);
		

		axisInnerPanel = new PamPanel(PamColor.BORDER);
		axisInnerPanel.setLayout(new BorderLayout());
		axisPanel.add(BorderLayout.CENTER, axisInnerPanel);

		timeRangePanel = new PamPanel(PamColor.BORDER);
		timeRangePanel.setOpaque(false);
		timeRangePanel.setLayout(new FlowLayout());
		timeRangePanel.add(new JLabel("Time range "));
		c.anchor = CornerLayoutContraint.LAST_LINE_END;
		layeredPane.add(timeRangePanel, c, JLayeredPane.DEFAULT_LAYER);
		layeredPane.setLayer(timeRangePanel, 100);

		timeAxis = new PamAxis(0, 1, 0, 1, 0, 300, PamAxis.ABOVE_LEFT, "Time", PamAxis.LABEL_NEAR_CENTRE, "%d");
		timeAxis.setCrampLabels(true);
	}

	/**
	 * Work out the minimum insets required for the 
	 * graph axis. 
	 * @return minimum insets required by graph axes. 
	 */
	public Insets getAxisInsets() {
		Graphics g = axisPanel.getGraphics();
		if (g == null) {
			return null;
		}
		int standardBorder = 5;
		int graphAxesExtent = getGrahAxisExtent(g);
		int timeAxisExtent = timeAxis.getExtent(g);
		if (tdControl.getTdParameters().orientation == PamScroller.HORIZONTAL) {
			return new Insets(timeAxisExtent, graphAxesExtent, standardBorder, standardBorder);
		}
		else {
			return new Insets(standardBorder, timeAxisExtent, graphAxesExtent, standardBorder);
		}
	}

	/**
	 * Get the maximum extent of any of the gaph axis. 
	 * @param g
	 * @return
	 */
	public int getGrahAxisExtent(Graphics g) {
		int extent = 0;
		for (TDGraph graph:tdControl.getGraphs()) {
			extent = Math.max(extent, graph.getAxisExtent(g));
		}
		return extent;
	}

	/**
	 * @return the axisPanel
	 */
	public PamPanel getAxisPanel() {
		return axisPanel;
	}

	/**
	 * @return the axisInnerPanel
	 */
	public PamPanel getAxisInnerPanel() {
		return axisInnerPanel;
	}

	/**
	 * @return the timeAxis
	 */
	public PamAxis getTimeAxis() {
		return timeAxis;
	}

	private Insets lastBorderInsets = null;
	public void setBorderInsets() {
		Insets borderInsets = getAxisInsets();
		if (borderInsets == null) {
			return;
		}
		if (differentInsets(lastBorderInsets, borderInsets)) {
			getAxisPanel().setBorder(new EmptyBorder(borderInsets));
			lastBorderInsets = borderInsets;
		}

	}

	private boolean differentInsets(Insets ins1,
			Insets ins2) {
		if (ins1 == ins2) {
			return false;
		}
		if (ins1 == null || ins2 == null) {
			return true;
		}
		if (ins1.equals(ins2)) {
			return false;
		}
		return true;
	}

	private class AxisPanel extends PamPanel {

		public AxisPanel() {
			super(PamColor.BORDER);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#print(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			setBorderInsets();
			timeAxis.setRange(0, tdControl.getTimeRangeSpinner().getSpinnerValue());
			if (tdControl.getTdParameters().orientation == PamScroller.HORIZONTAL) {
				paintHorizontal(g);
			}
			else {
				paintVertical(g);
			}
		}


		/**
		 * Paint with time axis along the top and graph axes down left side. 
		 * @param g
		 */
		private void paintHorizontal(Graphics g) {
			/*
			 * Draw the axes. 
			 */
			ArrayList<TDGraph> graphs = tdControl.getGraphs();
			if (timeAxis == null || graphs == null || graphs.size() == 0) {
				return;
			}
			int nPlots = graphs.get(0).getNumPlotPanels();
			if (nPlots == 0) {
				return;
			}
			// time axis will be drawn relative to the first graph.
			PamPanel graph0 = graphs.get(0).getGraphOuterPanel();
			PamPanel graphIn = graphs.get(0).getGraphPlotPanel(0);
			Point graphPoint = graph0.getLocationOnScreen();
			Point graphInnerPoint = graphIn.getLocationOnScreen();
			Point plotPoint = getLocationOnScreen();
			timeAxis.drawAxis(g, graphInnerPoint.x-plotPoint.x, graphPoint.y-plotPoint.y, 
					graphInnerPoint.x-plotPoint.x + graphIn.getWidth(), graphPoint.y-plotPoint.y);
			// now the individual graph axis on the left. 
			for (int i = 0; i < graphs.size(); i++) {
				PamAxis graphAxis = graphs.get(i).getGraphAxis();
				if (graphAxis == null) {
					continue;
				}
				for (int p = 0; p < graphs.get(i).getNumPlotPanels(); p++) {
					graph0 = graphs.get(i).getGraphPlotPanel(p);
					graphPoint = graph0.getLocationOnScreen();
					graphAxis.drawAxis(g, graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y+graph0.getHeight(), 
							graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y);
				}
			}
			/*
			 * Draw on the min and max time ranges
			 */
			String timeString = PamCalendar.formatDateTime(tdControl.getTimeScroller().getValueMillis());
			FontMetrics fm = g.getFontMetrics();
			g.drawString(timeString, timeAxis.getX1(), 1+fm.getHeight());
		}

		/**
		 * Paint with time axis up left side and graph axis along the top
		 * @param g
		 */
		private void paintVertical(Graphics g) {
			ArrayList<TDGraph> graphs = tdControl.getGraphs();
			if (timeAxis == null || graphs == null || graphs.size() == 0) {
				return;
			}
			int nPlots = graphs.get(0).getNumPlotPanels();
			if (nPlots == 0) {
				return;
			}
			// time axis will be drawn relative to the first graph.
			PamPanel graph0 = graphs.get(0).getGraphPlotPanel(0);
			Point graphPoint = graph0.getLocationOnScreen();
			Point plotPoint = getLocationOnScreen();
			timeAxis.drawAxis(g, graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y+graph0.getHeight(), 
					graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y);
			//			timeAxis.drawAxis(g, graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y, 
			//					graphPoint.x-plotPoint.x + graph0.getWidth(), graphPoint.y-plotPoint.y);
			// now the individual graph axis on the left. 
			for (int i = 0; i < graphs.size(); i++) {
				PamAxis graphAxis = graphs.get(i).getGraphAxis();
				if (graphAxis == null) {
					continue;
				}
				for (int p = 0; p < graphs.get(i).getNumPlotPanels(); p++) {
					graph0 = graphs.get(i).getGraphPlotPanel(p);
					graphPoint = graph0.getLocationOnScreen();
					graphAxis.drawAxis(g, graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y+graph0.getHeight(), 
							graphPoint.x-plotPoint.x + graph0.getWidth(), graphPoint.y-plotPoint.y+graph0.getHeight());
					//				graphAxis.drawAxis(g, graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y+graph0.getHeight(), 
					//						graphPoint.x-plotPoint.x, graphPoint.y-plotPoint.y);
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#repaint()
		 */
		@Override
		public void repaint() {
			super.repaint();
			if (timeRangePanel != null)
				timeRangePanel.repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#repaint(long)
		 */
		@Override
		public void repaint(long tm) {
			super.repaint(tm);
			if (timeRangePanel != null)
				timeRangePanel.repaint(tm);
		}

	}

	/**
	 * @return the outerPanel
	 */
	public JPanel getOuterPanel() {
		return outerPanel;
	}

	/**
	 * @return the layeredPane
	 */
	public JLayeredPane getLayeredPane() {
		return layeredPane;
	}

	/**
	 * @return the timeRangePanel
	 */
	public PamPanel getTimeRangePanel() {
		return timeRangePanel;
	}
}
