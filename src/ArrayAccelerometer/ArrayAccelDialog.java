package ArrayAccelerometer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import mcc.MccJniInterface;
import mcc.MccPanel;
import mcc.mccjna.MCCException;
import mcc.mccjna.MCCUtils;

public class ArrayAccelDialog extends PamDialog {
	
	private static ArrayAccelDialog singleInstance;
	
	private ArrayAccelParams accelParams;

	private ArrayAccelControl accelControl;

	private MccPanel mccPanel;
	private JTextField[] zeroVolts = new JTextField[ArrayAccelParams.NDIMENSIONS];
	private JTextField[] voltsPerG = new JTextField[ArrayAccelParams.NDIMENSIONS];
	private JTextField[] dimChannel = new JTextField[ArrayAccelParams.NDIMENSIONS];

	private JTextField readInterval;
	
	private JTextField rollOffset, pitchOffset;
	
	private JButton setZeroButton;

	private ArrayAccelDialog(Window parentFrame, ArrayAccelControl accelControl) {
		super(parentFrame, accelControl.getUnitName(), true);
		this.accelControl = accelControl;
		
		JPanel conPanel = new JPanel(new BorderLayout());
		mccPanel = new MccPanel();
		mccPanel.getPanel().setBorder(new TitledBorder("MCC Device"));
		conPanel.add(BorderLayout.NORTH, mccPanel.getPanel());
		JPanel rPanel = new JPanel(new GridBagLayout());
		rPanel.setBorder(new TitledBorder("Readout"));
		GridBagConstraints c = new PamGridBagContraints();
		rPanel.add(new JLabel("Read interval ", SwingConstants.RIGHT), c);
		c.gridx++;
		rPanel.add(readInterval = new JTextField(5), c);
		readInterval.setToolTipText("Readout interval in seconds");
		c.gridx++;
		rPanel.add(new JLabel(" s", SwingConstants.LEFT), c);
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			c.gridx = 0;
			c.gridy++;
			c.fill = GridBagConstraints.HORIZONTAL;
			rPanel.add(new JLabel("Channel " + ArrayAccelParams.DIMENSIONNAME[i] + " ", SwingConstants.RIGHT), c);
			c.gridx++;
			c.fill = GridBagConstraints.NONE;
			rPanel.add(dimChannel[i] = new JTextField(3), c);
			dimChannel[i].setToolTipText(String.format("<html>MCC Channel number for %s coordinate"
					+ "<p>Enter -1 to skip this channel</html>", ArrayAccelParams.DIMENSIONNAME[i]));
		}
		conPanel.add(BorderLayout.CENTER, rPanel);	
		
		JPanel calPanel = new JPanel(new GridBagLayout());
		calPanel.setBorder(new TitledBorder("Calibration"));
		c = new PamGridBagContraints();
		c.gridx = 1;
		calPanel.add(new JLabel("Zero", SwingConstants.CENTER), c);
		c.gridx += 2;
		calPanel.add(new JLabel("Scale", SwingConstants.CENTER), c);
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			c.gridy ++;
			c.gridx = 0;
			calPanel.add(new JLabel(ArrayAccelParams.DIMENSIONNAME[i] + " ", SwingConstants.RIGHT), c);
			c.gridx++;
			calPanel.add(zeroVolts[i] = new JTextField(5), c);
			zeroVolts[i].setToolTipText(String.format("Offset voltage for %s coordinate", ArrayAccelParams.DIMENSIONNAME[i]));
			c.gridx++;
			calPanel.add(new JLabel(" V,   ", SwingConstants.LEFT), c);
			c.gridx++;
			calPanel.add(voltsPerG[i] = new JTextField(5), c);
			voltsPerG[i].setToolTipText(String.format("Voltage scale for %s coordinate", ArrayAccelParams.DIMENSIONNAME[i]));
			c.gridx++;
			calPanel.add(new JLabel(" V/g ", SwingConstants.LEFT), c);
		}
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy++;
		calPanel.add(setZeroButton = new JButton("Set Zeros"), c);
		setZeroButton.setToolTipText("<html>Set zero voltage with array sensor on the level" +
		" (Note that<p>1g will be subtracted from the z coordinate value)</html>");
		setZeroButton.addActionListener(new SetZeroButton());
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		calPanel.add(new JLabel("Roll offset ", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		calPanel.add(rollOffset = new JTextField(5), c);
		rollOffset.setToolTipText("This value gets ADDED to the measured roll");
		c.gridx++;
		calPanel.add(new JLabel(" deg.", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		calPanel.add(new JLabel("Pitch offset ", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		calPanel.add(pitchOffset = new JTextField(5), c);
		pitchOffset.setToolTipText("This value gets ADDED to the measured pitch");
		c.gridx++;
		calPanel.add(new JLabel(" deg.", SwingConstants.LEFT), c);
		
		
		
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(conPanel);
		mainPanel.add(calPanel);
		
		setHelpPoint("sensors.arrayAccelerometer.docs.arrayAccelerometer");
		
		setDialogComponent(mainPanel);
		
	}

	public static ArrayAccelParams showDialog(Window parentFrame, ArrayAccelControl accelControl, ArrayAccelParams accelParams) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.accelControl != accelControl) {
			singleInstance = new ArrayAccelDialog(parentFrame, accelControl);
		}
		singleInstance.accelParams = accelParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.accelParams;
	}
	
	private void setParams() {
		mccPanel.setDeviceIndex(accelParams.boardNumber);
		mccPanel.setRange(accelParams.boardRange);
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			dimChannel[i].setText(String.format("%d", accelParams.dimChannel[i]));
			zeroVolts[i].setText(String.format("%5.3f", accelParams.zeroVolts[i]));
			voltsPerG[i].setText(String.format("%5.3f", accelParams.voltsPerG[i]));
		}
		readInterval.setText(new Double(accelParams.readInterval).toString());
		rollOffset.setText(String.format("%3.1f", accelParams.rollOffset));
		pitchOffset.setText(String.format("%3.1f", accelParams.pitchOffset));
	}

	@Override
	public boolean getParams() {
		accelParams.boardNumber = mccPanel.getDeviceIndex();
		try {
			accelParams.readInterval = Double.valueOf(readInterval.getText());	
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid readout interval ");
		}
		accelParams.boardRange = mccPanel.getRange();
		int i = 0;
		try {
			for (i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
				accelParams.dimChannel[i] = Integer.valueOf(dimChannel[i].getText());
			}
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid channel number for " + ArrayAccelParams.DIMENSIONNAME[i]);
		}
		try {
			for (i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
				accelParams.zeroVolts[i] = Double.valueOf(zeroVolts[i].getText());
			}
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid Zero Voltage for " + ArrayAccelParams.DIMENSIONNAME[i]);
		}
		try {
			for (i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
				accelParams.voltsPerG[i] = Double.valueOf(voltsPerG[i].getText());
			}
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid Voltage scale for " + ArrayAccelParams.DIMENSIONNAME[i]);
		}
		try {
			accelParams.rollOffset = Double.valueOf(rollOffset.getText());
			accelParams.pitchOffset = Double.valueOf(pitchOffset.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid pitch or roll offset value" + ArrayAccelParams.DIMENSIONNAME[i]);
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		accelParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		accelParams = new ArrayAccelParams();
		setParams();
	}

	private class SetZeroButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			findZero();
		}
	}

	/**
	 * Set the zero voltage for each channel. 
	 */
	public void findZero() {
		if (!getParams()) {
			return;
		}
		// read the voltages from the three channels and set as
		// the zero offset.
		double zeroVal;
		int err;
		MccJniInterface mccJni = new MccJniInterface();
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			if (accelParams.dimChannel[i] < 0) {
				continue;
			}
			try {
				int boardNum = MCCUtils.boardIndexToNumber(accelParams.boardNumber);
				zeroVal = MCCUtils.readVoltage(boardNum, accelParams.dimChannel[i], accelParams.boardRange);
			} catch (MCCException e) {
//				e.printStackTrace();
				showWarning("Unable to read MCC board: " + e.getMessage());
				break;
			}
			if (i == 2) {
				zeroVal -= accelParams.voltsPerG[2];
			}
//			err = mccJni.getLastErrorCode();
//			if (err != 0) {
//				showWarning("Unable to read MCC board");
//				break;
//			}
			zeroVolts[i].setText(String.format("%5.3f", zeroVal));
		}
	}
}
