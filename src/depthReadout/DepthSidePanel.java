package depthReadout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamSidePanel;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

public class DepthSidePanel extends PamObserverAdapter implements PamSidePanel {

	private DepthControl depthControl;
	
	private DepthPanel depthPanel;
	
	private int panelLifetime = 300; // panel lifetime in seconds. 
	
	int nSensors = 0;
	
	GraphAxisPanel graphAxisPanel;
	
	DepthDataBlock depthDataBlock;
	
	public DepthSidePanel(DepthControl depthControl) {
		super();
		
		this.depthControl = depthControl;
		
		depthDataBlock = depthControl.getDepthDataBlock();

		newSettings();
		
		depthControl.getDepthDataBlock().addObserver(this);
	}

	public JComponent getPanel() {
		return depthPanel;
	}

	public void rename(String newName) {

		depthPanel.setBorder(new TitledBorder(newName));
		
	}
	
	protected void newSettings() {
		if (nSensors != depthControl.depthParameters.nSensors) {
			nSensors = depthControl.depthParameters.nSensors;
		}
		depthPanel = new DepthPanel();
	}

	public String getObserverName() {
		return "Depth Side Panel";
	}

	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return panelLifetime * 1000;
	}

	public void addData(PamObservable o, PamDataUnit arg) {

		DepthDataUnit depthDataUnit = (DepthDataUnit) arg;

		if (depthDataUnit == null) {
			return;
		}
		
		double[] rawData = depthDataUnit.getRawDepthData();
		fillFields(depthPanel.rawDepthData, rawData, "%.4f");
		double[] depthData = depthDataUnit.getDepthData();
		fillFields(depthPanel.depthData, depthData, "%.1f");
		graphAxisPanel.repaintGraph();
	}
	
	private void fillFields(JTextField[] fields, double[] data, String format) {
		if (data == null || fields == null) return;
		int n = Math.min(fields.length, data.length);
		for (int i = 0; i < n; i++) {
			fields[i].setText(String.format(format, data[i]));
		}
	}
	
	protected void newViewTimes() {
		long viewTime = PamCalendar.getTimeInMillis();
//		System.out.println("ViewTime = " + viewTime);
		DepthDataUnit ddu = depthDataBlock.getClosestUnitMillis(viewTime);
		addData(depthDataBlock, ddu);
	}

	class DepthPanel extends PamBorderPanel {

		PamBorderPanel dataPanel = new PamBorderPanel();
		
		JTextField[] depthData;
		
		JTextField[] rawDepthData;
		
		TitledBorder titledBorder;
		
		public DepthPanel() {
			super();
			setBorder(titledBorder = new TitledBorder("Depth Information"));
			setLayout(new BorderLayout());
			JLabel label;
			// layput the 
			depthData = new JTextField[nSensors];
			rawDepthData = new JTextField[nSensors];
			dataPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.HORIZONTAL;
			addComponent(dataPanel, new PamLabel("Sensor "), c);
			c.gridx++;
			addComponent(dataPanel, new PamLabel(" raw (V) "), c);
			c.gridx++;
			addComponent(dataPanel, new PamLabel(" Depth (m) "), c);
			c.gridx++;
			c.gridy++;
			for (int i = 0; i < nSensors; i++) {
				c.gridx = 0;
				addComponent(dataPanel, label = new JLabel(String.format(" %d ", i)), c);
				label.setForeground(PamColors.getInstance().getChannelColor(i));
				c.gridx++;
				addComponent(dataPanel, rawDepthData[i] = new JTextField(6), c);
				c.gridx++;
				addComponent(dataPanel, depthData[i] = new JTextField(6), c);
				c.gridy++;
			}
			
			add(BorderLayout.NORTH, dataPanel);
			
			if (graphAxisPanel == null) {
				graphAxisPanel = new GraphAxisPanel();
			}
			add(BorderLayout.CENTER, graphAxisPanel);
		}
		@Override
		public void setBackground(Color bg) {
			super.setBackground(bg);
			if (titledBorder != null) {
				titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
			}
		}
		
	}
	
	double graphVScale = 1;
	double graphTScale = 1;
	long viewStart, viewEnd;
	
	private class GraphAxisPanel extends PamAxisPanel {
		
		GraphPanel graphPanel;
		
		PamAxis timeAxis, depthAxis;
		
		public GraphAxisPanel() {
			super();
			graphPanel = new GraphPanel();
			depthAxis = new PamAxis(0, 0, 0, 2, 200, -0.1, PamAxis.ABOVE_LEFT, null, PamAxis.LABEL_NEAR_CENTRE, "%d");
			int viewEndTime = 0;
			int interval = 1;
			if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
				viewEndTime = panelLifetime/60;
			}
			timeAxis = new PamAxis(0, 0, 2, 0, -panelLifetime/60, viewEndTime, PamAxis.BELOW_RIGHT, "Time (min)", PamAxis.LABEL_NEAR_CENTRE, "%d");
			timeAxis.setInterval(interval);
			setInnerPanel(graphPanel);
			setWestAxis(depthAxis);
			setSouthAxis(timeAxis);
			
//			System.out.println("Construct graph axis panel");
//			PamColors.getInstance().registerComponent(this, PamColor.BORDER);
			this.setAutoInsets(true);
		}
		
		void repaintGraph() {
			setScales(true);
			repaint();
			graphPanel.repaint();
		}
		
		void setScales(boolean auto) {

			if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
				viewEnd = PamCalendar.getTimeInMillis();
				graphTScale = (double) graphPanel.getWidth()/panelLifetime;
			}
			else {
				viewEnd = PamCalendar.getTimeInMillis() + panelLifetime * 1000;
				graphTScale = (double) graphPanel.getWidth()/(panelLifetime*2);
			}
			viewStart = PamCalendar.getTimeInMillis() - panelLifetime * 1000;
			
			double range = 10;
			double rangeStep = 10;
			if (auto) {
				double maxDepth = getMaxDepth();
				while (maxDepth > range * 10) {
					range *= 10;
					rangeStep = range;
				}
				while (range < maxDepth) {
					range += rangeStep;
				}
				depthAxis.setMinVal(range);
				depthAxis.setInterval(-range/2);
			}
			graphVScale = graphPanel.getHeight()/range;
		}
		
		double getMaxDepth() {
			int nData = depthDataBlock.getUnitsCount();
			if (nData == 0) return 10;
			double maxDepth = 0;
			double[] depthData;
			DepthDataUnit depthDataUnit;
			ListIterator<DepthDataUnit> depthIterator = depthDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
			while (depthIterator.hasPrevious()) {
				depthDataUnit = depthIterator.previous();
				if (depthDataUnit.getTimeMilliseconds() > viewEnd) {
					continue;
				}
				if (depthDataUnit.getTimeMilliseconds() < viewStart) {
					break;
				}
				depthData = depthDataUnit.getDepthData();
				for (int iC = 0; iC < depthData.length; iC++) {
					maxDepth = Math.max(maxDepth, depthData[iC]);
				}
			}
			return maxDepth;
		}
		
	}
	
	private class GraphPanel extends JPanel implements ColorManaged {

		public GraphPanel() {
			super();
			setPreferredSize(new Dimension(100,100));
//			PamColors.getInstance().registerComponent(this, PamColor.PlOTWINDOW);
			setBorder(PamBorder.createInnerBorder());
		}

		@Override
		public PamColor getColorId() {
			return PamColor.PlOTWINDOW;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			Point[] lastPoints;
			int nData = depthDataBlock.getUnitsCount();
			if (nData == 0) return;
			double maxDepth = 0;
			double[] depthData;
			ListIterator<DepthDataUnit> depthIterator = depthDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
			DepthDataUnit	depthDataUnit = depthIterator.previous();
			depthData = depthDataUnit.getDepthData();
			lastPoints = new Point[depthData.length];
			for (int iC = 0; iC < depthData.length; iC++) {
				lastPoints[iC] = getPoint(viewEnd, depthDataUnit.getTimeMilliseconds(), depthData[iC]);
			}
			Point thisPoint;
			while (depthIterator.hasPrevious()) {
				depthDataUnit = depthIterator.previous();
				if (depthDataUnit.getTimeMilliseconds() < PamCalendar.getTimeInMillis() - panelLifetime * 1000) {
					break;
				}
				depthData = depthDataUnit.getDepthData();
				if (lastPoints.length < depthData.length) {
					Point[] newPoints = new Point[depthData.length];
					for (int i = 0; i < lastPoints.length; i++) {
						newPoints[i] = lastPoints[i];
					}
					lastPoints = newPoints;
				}
				for (int iC = 0; iC < depthData.length; iC++) {
					g2d.setColor(PamColors.getInstance().getChannelColor(iC));
					thisPoint = getPoint(viewEnd, depthDataUnit.getTimeMilliseconds(), depthData[iC]);
					if (lastPoints[iC] != null) {
						g2d.drawLine(lastPoints[iC].x, lastPoints[iC].y, thisPoint.x, thisPoint.y);
						lastPoints[iC] = thisPoint;
					}
				}
			}
			
		}
		
		private Point getPoint(long now, long time, double depth) {
			double tP =  getWidth() - 1 - graphTScale * (now - time)/1000.;
			double dP = Math.max(0, graphVScale * depth);
			return new Point((int) tP, (int) dP);
		}
	}
}
