package angleMeasurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamUtils.PamUtils;
import PamView.ColorManaged;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamBorder;

public class AngleCalibrationDialog extends PamDialog implements AngleMeasurementListener{

	private AngleParameters angleParameters;
	
	private AngleControl angleControl;
	
	private static AngleCalibrationDialog singleInstance;
	
	private CalibrationPanel calibrationPanel;
	
	private PlotPanel plotPanel;
	
	private Double latestRawAngle;
	
	private TrueToMeasured trueToMeasured;
	
	private MeasuredCorrection measuredCorrection;
	
	private AngleCalibration angleCalibration;
	
	private AngleCalibrationDialog(Frame parentFrame, AngleControl angleControl, AngleParameters angleParameters) {
		super(parentFrame, angleControl.getUnitName() + " calibration", false);

		if (angleParameters == null) {
			this.angleParameters = new AngleParameters();
		}
		else {
			this.angleParameters = angleParameters.clone();
		}
		
		calibrationPanel = new CalibrationPanel();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BorderLayout());
		westPanel.add(BorderLayout.NORTH, calibrationPanel);
		panel.add(BorderLayout.WEST, westPanel);
		panel.add(BorderLayout.CENTER, plotPanel = new PlotPanel());
		
		setDialogComponent(panel);
		setResizable(true);

		angleControl.angleMeasurement.addMeasurementListener(this);
	}


	public static AngleParameters showDialog(Frame parentFrame, AngleControl angleControl, AngleParameters angleParameters) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new AngleCalibrationDialog(parentFrame, angleControl, angleParameters);
		}
		if (angleParameters == null) {
			singleInstance.angleParameters = new AngleParameters();
		}
		else {
			singleInstance.angleParameters = angleParameters.clone();
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.angleParameters;
	}
	
	@Override
	public void cancelButtonPressed() {
		angleParameters = null;
	}
	
	private void setParams() {

		singleInstance.latestRawAngle = null;
		singleInstance.calibrationPanel.enableControls(false);
		calibrationPanel.setParams();
		rePlot();
	}

	private boolean paramErrors;
	@Override
	public boolean getParams() {
		paramErrors = false;
		angleParameters.setCalibrationData(getNewCalData());
		return !paramErrors;
	}
	
	protected double[] getNewCalData() {
		double[] newData = new double[angleParameters.getNumPoints()];
		for (int i = 0; i < newData.length; i++) {
			newData[i] = getValue(i);
		}
		return newData;
	}
	
	private double getValue(int iCal) {
		double value = 0;
		try {
			value  = Double.valueOf(calibrationPanel.calValues[iCal].getText());
		}
		catch (NumberFormatException e) {
			paramErrors = true;
			return 0;
		}
		return value;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	void rePlot() {
		double[] measuredValues = getNewCalData();
		angleCalibration = new AngleCalibration(angleParameters.getCalibrationPoints(), measuredValues);
		trueToMeasured.repaint();
		measuredCorrection.repaint();
	}
	
	class CalibrationPanel extends JPanel {

		JLabel[] calLabels1;
		JLabel[] calLabels2;
		JTextField[] calValues;
		JButton[] calButtons;
		JLabel rawAngle;
		JButton setInterval;
		JTextField calInterval;
		
		public CalibrationPanel() {
			super();
			checkControls();
		}
		
		
		
		private void checkControls() {
			int nC = angleParameters.getNumPoints();
			calLabels1 = new JLabel[nC];
			calLabels2 = new JLabel[nC];
			calValues = new JTextField[nC];
			calButtons = new JButton[nC];
			JButton defaultButton;
			CalKeyListener calKeyListener = new CalKeyListener();
						
			removeAll();
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			
			c.gridwidth = 6;
			addComponent(this, rawAngle = new JLabel(" "), c);
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			addComponent(this, new JLabel("Calibration interval (\u00B0)"), c);
			c.gridx += c.gridwidth;
			c.gridwidth = 1;
			addComponent(this, calInterval = new JTextField(6), c);
			c.gridx += c.gridwidth;
			addComponent(this, setInterval = new JButton("Set"), c);
			setInterval.addActionListener(new SetInterval());
			
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy++;
			addComponent(this, new JLabel("True"), c);
			c.gridx++;
			addComponent(this, new JLabel("Measured"), c);
			c.gridx+=2;
			addComponent(this, new JLabel("True"), c);
			c.gridx++;
			addComponent(this, new JLabel("Measured"), c);
			c.gridy++;
			int startY = c.gridy;
			int startX = 0;
			for (int i = 0; i < nC; i++) {
				c.gridx = startX;
				addComponent(this, calLabels1[i] = new JLabel("9999"), c);
				c.gridx++;
				addComponent(this, calValues[i] = new JTextField(4), c);
				calValues[i].addKeyListener(calKeyListener);
				c.gridx++;
				addComponent(this, calButtons[i] = new JButton("Set"), c);
				calButtons[i].setEnabled(false);
				calButtons[i].addActionListener(new SetButton(i));
				
				c.gridy++;
				if ((i+1) == nC/2) {
					startX = 3;
					c.gridy = startY;
				}
			}
			c.gridx = 4;
			c.gridwidth = 2;
			addComponent(this, defaultButton = new JButton("Set Defaults"), c);
			defaultButton.addActionListener(new DefaultButton());
		}
		
		protected void setParams() {

			double calPoints[] = angleParameters.getCalibrationPoints();
			double calData[] = angleParameters.getCalibrationData();
			for (int i = 0; i < calData.length; i++) {
				calLabels1[i].setText(String.format("%.1f\u00B0", calPoints[i]));
				calValues[i].setText(String.format("%.1f", calData[i]));
			}
			writeInterval(angleParameters.getCalibrationInterval());
			pack();
		}
		
		class SetButton implements ActionListener {
			int nCal;

			public SetButton(int cal) {
				super();
				nCal = cal;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if (latestRawAngle != null) {
					calValues[nCal].setText(String.format("%.1f", latestRawAngle));
					rePlot();
				}
			}
		}
		
		class SetInterval implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				int newInterval = readInterval();
				if (newInterval <= 0) {
					return;
				}
				if (newInterval == angleParameters.getCalibrationInterval()) {
					return;
				}
				int ans = JOptionPane.showConfirmDialog(getOwner(), 
						"Changing the calibration interval will clear all current data",
						"Calibration Interval", JOptionPane.OK_CANCEL_OPTION);
				if (ans == JOptionPane.CANCEL_OPTION) {
					writeInterval(angleParameters.getCalibrationInterval());
				}
				else {
					changeCalibrationInterval(newInterval);
				}
			}
		}
		
		void changeCalibrationInterval(int newInterval) {
			angleParameters.setCalibrationInterval(newInterval);
			checkControls();
			setParams();
			rePlot();
		}
		
		private void writeInterval(int interval) {
			calInterval.setText(String.format("%d", interval));
		}
		
		private int readInterval() {
			int newInterval = 0;
			try {
				newInterval = Integer.valueOf(calInterval.getText());
			}
			catch (NumberFormatException e) {
				newInterval = 0;
			}
			return newInterval;
		}
		
		class DefaultButton implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				double defaults[] = angleParameters.getCalibrationPoints();
				for (int i = 0; i < defaults.length; i++) {
					calValues[i].setText(String.format("%.1f", defaults[i]));
				}
				rePlot();
			}
		}
		
		class CalKeyListener implements KeyListener {

			@Override
			public void keyPressed(KeyEvent e) {
				rePlot();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				rePlot();
			}

			@Override
			public void keyTyped(KeyEvent e) {
				rePlot();
			}
		}
		
		private boolean currentEnableState = false;
		protected void enableControls(boolean en) {
			if (currentEnableState == en) {
				return;
			}
			for (int i = 0; i < calButtons.length; i++) {
				calButtons[i].setEnabled(en);
			}
			currentEnableState = en;
		}
		
	}
	
	class PlotPanel extends JPanel {

		public PlotPanel() {
			super();
			setPreferredSize(new Dimension(400, 10));
			setLayout(new GridLayout(2,1));
			measuredCorrection = new MeasuredCorrection();
			trueToMeasured = new TrueToMeasured();
			add(trueToMeasured.getPanel());
			add(measuredCorrection.getPanel());
		}
		
	}
	
	class TrueToMeasured {
		
		PamAxisPanel panel;
		TPlotPanel tplotPanel;
		PamAxis southAxis, westAxis;
		public TrueToMeasured() {
			super();
			panel = new PamAxisPanel();
			tplotPanel = new TPlotPanel();
			panel.setInnerPanel(tplotPanel);
			southAxis = new PamAxis(0, 0, 10, 0, 0, 360, PamAxis.BELOW_RIGHT, "True Angle", PamAxis.LABEL_NEAR_CENTRE, "%d");
			westAxis = new PamAxis(0, 0, 0, 10, 0, 360, PamAxis.ABOVE_LEFT, "Measured Angle", PamAxis.LABEL_NEAR_CENTRE, "%d");
			southAxis.setInterval(90);
			westAxis.setInterval(90);
			panel.setSouthAxis(southAxis);
			panel.setWestAxis(westAxis);
			panel.setAutoInsets(true);
		}
		
		void repaint() {
			tplotPanel.repaint();
		}
		
		public JPanel getPanel() {
			return panel;
		}
		
		class TPlotPanel extends JPanel implements ColorManaged {
			PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 6, 6, true, Color.RED, Color.RED);
			public TPlotPanel() {
				super();
//				PamColors.getInstance().registerComponent(this, PamColor.PlOTWINDOW);
				setBorder(BorderFactory.createLoweredBevelBorder());
			}

			@Override
			public PamColor getColorId() {
				return PamColor.PlOTWINDOW;
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (angleCalibration == null) {
					return;
				}
				double[] calData = getNewCalData();
				double[] calPoints = angleParameters.getCalibrationPoints();
				int x, y, lastx=0, lasty=0;
				for (int i = 0; i < calData.length; i++) {
					x = (int) southAxis.getPosition(calPoints[i]);
					y = (int) westAxis.getPosition(calData[i]);
//					y = 20;
//					x = 100;
					symbol.draw(g, new Point(x,y));
					if (i >0) {
						g.setColor(symbol.getFillColor());
						g.drawLine(x, y, lastx, lasty);
					}
					lastx = x;
					lasty = y;
				}
			}
		}
	}
	
	class MeasuredCorrection {

		PamAxisPanel panel;
		MCPlotPanel mcPlotPanel;
		PamAxis southAxis, westAxis;
		PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 6, 6, true, Color.RED, Color.RED);
		public MeasuredCorrection() {
			panel = new PamAxisPanel();
			mcPlotPanel = new MCPlotPanel();
			panel.setInnerPanel(mcPlotPanel);
			southAxis = new PamAxis(0, 0, 10, 0, 0, 360, PamAxis.BELOW_RIGHT, "Measured Angle", PamAxis.LABEL_NEAR_CENTRE, "%d");
			westAxis = new PamAxis(0, 0, 0, 10, -15, 15, PamAxis.ABOVE_LEFT, "Correction", PamAxis.LABEL_NEAR_CENTRE, "%d");
			southAxis.setInterval(90);
//			westAxis.setInterval(5);
			panel.setSouthAxis(southAxis);
			panel.setWestAxis(westAxis);
//			panel.setMinNorth(10);
//			panel.setMinEast(10);
			panel.setAutoInsets(true);
		}
		
		public void repaint() {
			double maxError = angleCalibration.getMaxError();
			double axisMax = Math.ceil(maxError / 10) * 10;
			axisMax = Math.max(axisMax, 2);
			westAxis.setMaxVal(axisMax);
			westAxis.setMinVal(-axisMax);
			westAxis.setInterval(axisMax/2);
			mcPlotPanel.repaint();
		}
		
		public JPanel getPanel() {
			return panel;
		}
		
		class MCPlotPanel extends JPanel implements ColorManaged {
			public MCPlotPanel() {
				super();
//				PamColors.getInstance().registerComponent(this, PamColor.PlOTWINDOW);
				setBorder(PamBorder.createInnerBorder());
			}

			@Override
			public PamColor getColorId() {
				return PamColor.PlOTWINDOW;
			}
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				if (angleCalibration == null) {
					return;
				}
				double[] truePoints = angleCalibration.getSortedTrueValues();
				double[] measuredPoints = angleCalibration.getSortedMeasuredValues();
				int x, y, lastx=0, lasty=0;
				for (int i = 0; i < truePoints.length; i++) {
					x = (int) southAxis.getPosition(measuredPoints[i]);
					y = (int) westAxis.getPosition(PamUtils.constrainedAngle(
							truePoints[i]-measuredPoints[i]-angleCalibration.getZeroTrue(),180));
					symbol.draw(g, new Point(x,y));
//					if (i >0) {
//						g.setColor(symbol.getFillColor());
//						g.drawLine(x, y, lastx, lasty);
//					}
//					lastx = x;
//					lasty = y;
				}
				g.setColor(symbol.getLineColor());
				double measured, calibrated, correction;
				for (int i = 0; i <= 359; i++) {
					measured = i;
					calibrated = angleCalibration.getCalibratedAngle(measured);
					correction = calibrated - measured;
					correction = PamUtils.constrainedAngle(correction, 180);
					x = (int) southAxis.getPosition(measured);
					y = (int) westAxis.getPosition(correction);
//					y = (int) southAxis.getPosition(angleCalibration.getCalibratedAngle(i));

					if (i >0) {
						g.drawLine(x, y, lastx, lasty);
					}
					lastx = x;
					lasty = y;
				}
			}
		}
	}
	
	@Override
	public void newAngle(Double rawAngle, Double calibratedAngle, Double correctedAngle) {

		latestRawAngle = rawAngle;
		if (rawAngle != null) {
			calibrationPanel.rawAngle.setText(String.format("Current raw angle measurement = %.1f\u00B0", rawAngle));
			calibrationPanel.enableControls(true);
		}
		if (correctedAngle != null) {
		}
		
		
	}

}
