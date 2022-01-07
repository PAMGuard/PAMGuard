package difar.calibration;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pamMaths.HistogramDisplay;
import pamMaths.HistogramGraphicsLayer;
import difar.DifarControl;
import difar.DifarParameters;
import Layout.PamAxis;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamView.CancelObserver;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamTextDisplay;
import PamView.panel.PamPanel;
import PamguardMVC.PamConstants;

public class CalibrationDialog extends PamDialog implements CancelObserver {

	private static CalibrationDialog[] singleInstances = new CalibrationDialog[PamConstants.MAX_CHANNELS];
	
	private CalibrationHistogram trueBearingHistogram;
	private CalibrationHistogram calCorrectionHistogram;
	private CalibrationProcess calibrationProcess;
	
	private HistogramDisplay trueBearingDisplay;
	private HistogramDisplay calCorrectionDisplay;

	private int channel;
	
	private JButton saveButton, stopButton;
	private PamTextDisplay progressText;

	private DifarControl difarControl;
	
	private CalibrationDialog(Window parentFrame,
			DifarControl difarControl, int channel) {
		super(parentFrame, "Difar calibration channel " + channel, false);
		this.difarControl = difarControl;
		this.channel = channel;
		calibrationProcess = difarControl.getDifarProcess().getCalibrationProcess(channel);
		
		setCancelObserver(this);
		
		trueBearingHistogram = difarControl.sonobuoyManager.getCalibrationHistogram(difarControl, channel); 
		trueBearingDisplay = new HistogramDisplay(trueBearingHistogram);
		trueBearingDisplay.getSouthAxis().setInterval(90);
		calCorrectionHistogram = difarControl.sonobuoyManager.getCalCorrectionHistogram(difarControl, channel);
		calCorrectionDisplay = new HistogramDisplay(calCorrectionHistogram);
		calCorrectionDisplay.setGraphicsOverLayer(new CorrectionOverlay());
		CorrectionMouse corrMouse = new CorrectionMouse();
		calCorrectionDisplay.getHistoPlotPanel().addMouseMotionListener(corrMouse);
		calCorrectionDisplay.getHistoPlotPanel().addMouseListener(corrMouse);
		calCorrectionDisplay.getSouthAxis().setInterval(90);
		
		String tip = "Drag selected bearing or right click to select mean / modal values";
		calCorrectionDisplay.getHistoPlotPanel().setToolTipText(tip);
		
		calCorrectionHistogram.addObserver(new CalHistObserver());
		
		trueBearingDisplay.setSelectedStats(HistogramDisplay.STATS_N | HistogramDisplay.STATS_MEAN | HistogramDisplay.STATS_STD);
		calCorrectionDisplay.setSelectedStats(HistogramDisplay.STATS_N | HistogramDisplay.STATS_MEAN | HistogramDisplay.STATS_STD);

		PamPanel mainPanel = new PamPanel(new BorderLayout());
		
		PamPanel histPanel = new PamPanel();
		histPanel.setLayout(new GridLayout(2, 1));
		histPanel.add(trueBearingDisplay.getGraphicComponent());
		histPanel.add(calCorrectionDisplay.getGraphicComponent());
		mainPanel.add(BorderLayout.CENTER, histPanel);
		
		mainPanel.add(BorderLayout.NORTH, progressText = new PamTextDisplay());
		
		mainPanel.setPreferredSize(new Dimension(400, 300));
		
		setResizable(true);
		setDialogComponent(mainPanel);
		setModalityType(ModalityType.MODELESS);
	}
		
	public static void showDialog(Window parentFrame, DifarControl difarControl, int channel) {
		if (singleInstances[channel] == null) {
			singleInstances[channel] = new CalibrationDialog(parentFrame, difarControl, channel);
		}
		singleInstances[channel].draggedMouseValue = null;
		singleInstances[channel].setVisible(true);
	}
	
	/* (non-Javadoc)
	 * @see PamView.PamDialog#setVisible(boolean)
	 */
	@Override
	public synchronized void setVisible(boolean visible) {
		super.setVisible(visible);
		updateControls();
		enableControls();
	}

	private class CalHistObserver implements Observer {

		@Override
		public void update(Observable o, Object arg) {
			updateControls();
			enableControls();
		}
		
	}
	class CorrectionOverlay implements HistogramGraphicsLayer {

		@Override
		public void paintLayer(Graphics g) {
			Double val = getCurrentValue();
			if (val == null) {
				return;
			}
			PamAxis histAx = calCorrectionDisplay.getSouthAxis();
			if (histAx == null) {
				return;
			}
			int pos = (int) histAx.getPosition(val);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(3));
			g.setColor(Color.red);
			g.drawLine(pos, 0, pos, g.getClipBounds().height);
			
			
			String str;
			if (draggedMouseValue != null) {
				str = "Dragged position ";
			}
			else if (difarControl.getDifarParameters().calibrationChoice == DifarParameters.CALIBRATION_USE_MEAN) {
				str = "Mean ";
			}
			else if (difarControl.getDifarParameters().calibrationChoice == DifarParameters.CALIBRATION_USE_MODE) {
				str = "Modal ";
			}
			else {
				str = "";
			}
			str += String.format("value = %3.1f%s", val, LatLong.deg);
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D b = fm.getStringBounds(str, g);
			g.setColor(PamColors.getInstance().getColor(PamColor.GRID));
			g.drawString(str, (int) (g.getClipBounds().width - b.getWidth() - 5), (int) (b.getHeight() + 5));
		}
		
	}
	
	private Double draggedMouseValue = null;
	class CorrectionMouse extends MouseAdapter {

		private boolean mouseClose;

		@Override
		public void mouseDragged(MouseEvent me) {
			if (!mouseClose) return;
			PamAxis histAx = calCorrectionDisplay.getSouthAxis();
			if (histAx == null) {
				return;
			}
			draggedMouseValue = histAx.getDataValue(me.getX());
			calCorrectionDisplay.repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showPopupMenu(me);
				return;
			}
			
			Double val = getCurrentValue();
			if (val == null) {
				return;
			}
			PamAxis histAx = calCorrectionDisplay.getSouthAxis();
			if (histAx == null) {
				return;
			}
			int pos = (int) histAx.getPosition(val);
			mouseClose = (Math.abs(me.getX() - pos) < 12);
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showPopupMenu(me);
				return;
			}
		}
		
	}
	
	private Double getCurrentValue() {
		if (calCorrectionHistogram.getMaxContent() <= 0) {
			return null;
		}
		if (draggedMouseValue != null) {
			return draggedMouseValue;
		}
		if (difarControl.getDifarParameters().calibrationChoice == DifarParameters.CALIBRATION_USE_MEAN) {
			return calCorrectionHistogram.getMean();
		}
		else {
			return calCorrectionHistogram.getMode();
		}
	}

	public void showPopupMenu(MouseEvent me) {
		JPopupMenu popMenu = new JPopupMenu();
		
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Select Mean");
		popMenu.add(menuItem);
		menuItem.setSelected(difarControl.getDifarParameters().calibrationChoice == DifarParameters.CALIBRATION_USE_MEAN && draggedMouseValue == null);
		menuItem.addActionListener(new SelectMean());
		
		menuItem = new JCheckBoxMenuItem("Select Mode");
		popMenu.add(menuItem);
		menuItem.setSelected(difarControl.getDifarParameters().calibrationChoice == DifarParameters.CALIBRATION_USE_MODE && draggedMouseValue == null);
		menuItem.addActionListener(new SelectMode());
		
		popMenu.show(me.getComponent(), me.getX(), me.getY());
	}

	private class SelectMean implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			draggedMouseValue = null;
			difarControl.getDifarParameters().calibrationChoice = DifarParameters.CALIBRATION_USE_MEAN;
			calCorrectionDisplay.repaint();
		}
	}
	
	private class SelectMode implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			draggedMouseValue = null;
			difarControl.getDifarParameters().calibrationChoice = DifarParameters.CALIBRATION_USE_MODE;
			calCorrectionDisplay.repaint();
		}
	}
	private void updateControls() {
		progressText.setText(calibrationProcess.getStatusString());
	}
	private void enableControls() {
		getOkButton().setEnabled(calCorrectionHistogram.getTotalContent() > 0);
	}
	

	@Override
	public boolean getParams() {
		Double val = getCurrentValue();
		if (val == null) {
			return false;
		}
		boolean ok = calibrationProcess.setCorrectionValue(getOwner(), val);
		if (ok) {
			// stop it doing any more if the calibration is not complete. 
			calibrationProcess.stopBuoyCalibration();
		}
		return ok;
	}

	@Override
	public void cancelButtonPressed() {
		calibrationProcess.stopBuoyCalibration();
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean cancelPressed() {
		return true;
	}

}
