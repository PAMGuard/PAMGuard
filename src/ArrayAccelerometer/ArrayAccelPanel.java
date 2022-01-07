package ArrayAccelerometer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ListIterator;

import javax.swing.JPanel;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamUtils.LatLong;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

/*
 * Display panel for array accelerometer data
 */
public class ArrayAccelPanel {

	private JPanel accelPanel;
	private AccelPlotGraph plotGraph;
	private PamAxisPanel plotAxis;
	private PamAxis timeAxis, angleAxis;
	private ArrayAccelControl accelControl;
	private long zeroGraphTime;
	private ArrayAccelDataBlock accelDataBlock;
	private int panelDurationSecs = 300;
	private double timeScale = 1;;


	public ArrayAccelPanel(ArrayAccelControl accelControl) {
		this.accelControl = accelControl;
		accelPanel = new PamPanel(PamColor.BORDER);
		accelPanel.setLayout(new BorderLayout());
		plotAxis = new AccelPlotAxis();
		accelPanel.add(BorderLayout.NORTH, plotAxis);
//		accelPanel.setMinimumSize(new Dimension(300, 200));
		plotGraph.setPreferredSize(new Dimension(100, 100));
		accelDataBlock = accelControl.accelProcess.accelDataBlock;
		accelDataBlock.addObserver(new DataObserver());
		setPanelDuration(60);
	}

	public JPanel getPanel() {
		return accelPanel;
	}
	
	public void setPanelDuration(int durationSecs) {
		this.panelDurationSecs = durationSecs;
		timeScale = 1.;
		String unit = "Time (secs)";
		if (panelDurationSecs >= 120) {
			timeScale = 60.;
			unit = "Time (min)";
		}
		timeAxis.setRange(-panelDurationSecs/timeScale, 0);
		timeAxis.setLabel(unit);
	}

	public void newAccelData(ArrayAccelDataUnit accelDataUnit) {
		zeroGraphTime = accelDataUnit.getTimeMilliseconds();
		repaintAll();
	}

	private void repaintAll() {
		plotGraph.repaint();
		plotAxis.repaint();
	}

	private class AccelPlotAxis extends PamAxisPanel {

		public AccelPlotAxis() {
			super();
			timeAxis = new PamAxis(0, 0, 1, 1, 0, 120, PamAxis.BELOW_RIGHT, "S", PamAxis.LABEL_NEAR_CENTRE, "%d");
			angleAxis = new PamAxis(0, 0, 1, 1, -180, 180, PamAxis.ABOVE_LEFT, "", PamAxis.LABEL_NEAR_CENTRE, "%d");
			angleAxis.setInterval(90);
			setWestAxis(angleAxis);
			setSouthAxis(timeAxis);
			plotGraph = new AccelPlotGraph();
			//			setPlotPanel(plotGraph);
			setInnerPanel(plotGraph);
			this.setAutoInsets(true);
		}

	}

	private class AccelPlotGraph extends PamPanel {

		public AccelPlotGraph() {
			super(PamColor.PlOTWINDOW);
			setBorder(PamBorder.createInnerBorder());
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Color rollColour = PamColors.getInstance().getChannelColor(0);
			Color pitchColour = PamColors.getInstance().getChannelColor(1);
			ArrayAccelDataUnit thisDU, lastDU = null, firstDU = null;
			int x1, lastX=0, y1, y2;
			Double thisRoll, lastRoll, thisPitch, lastPitch;
			synchronized (accelDataBlock.getSynchLock()) {
				ListIterator<ArrayAccelDataUnit> it = accelDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
				while (it.hasPrevious()) {
					thisDU = it.previous();
					x1 = (int) timeAxis.getPosition((thisDU.getTimeMilliseconds()-zeroGraphTime)/timeScale/1000);
					if (lastDU != null) {
						thisRoll = thisDU.getRoll();
						lastRoll = lastDU.getRoll();
						thisPitch = thisDU.getPitch();
						lastPitch = lastDU.getPitch();
						if (thisRoll != null && lastRoll != null) {
							g.setColor(rollColour);
							y1 = (int) angleAxis.getPosition(thisRoll);
							y2 = (int) angleAxis.getPosition(lastRoll);
							g.drawLine(x1, y1, lastX, y2);
						}
						if (thisPitch != null && lastPitch != null) {
							g.setColor(pitchColour);
							y1 = (int) angleAxis.getPosition(thisPitch);
							y2 = (int) angleAxis.getPosition(lastPitch);
							g.drawLine(x1, y1, lastX, y2);
						}
					}
					else {
						firstDU = thisDU;
					}
					lastDU = thisDU;
					lastX = x1;
				}
			}
			Double val;
			if (firstDU != null) {
				FontMetrics fm = g.getFontMetrics();
				int x = fm.charWidth(' ');
				int y = fm.getHeight();
				val = firstDU.getPitch();
				if (val != null) {
					g.setColor(pitchColour);
					g.drawString(String.format("Pitch: %3.1f%s", val, LatLong.deg), x, y);
					y *=2;
				}
				val = firstDU.getRoll();
				if (val != null) {
					g.setColor(rollColour);
					g.drawString(String.format("Roll: %3.1f%s", val, LatLong.deg), x, y);
				}
			}
		}

	}

	private class DataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return (long) ((panelDurationSecs+1) * 1000);
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			if (arg.getClass() == ArrayAccelDataUnit.class) {
				newAccelData((ArrayAccelDataUnit) arg);
			}
		}

		@Override
		public String getObserverName() {
			return accelControl.getUnitName() + " plot";
		}

	}
}
