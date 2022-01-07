package clickDetector.dialogs;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamUtils.LatLong;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import clickDetector.clicktrains.ClickTrainIdParams;

public class ClickTrainIdDialog extends PamDialog {

	private static ClickTrainIdDialog singleInstance;
	
	private ClickTrainIdParams clickTrainIdParams;
	
	private JCheckBox runClickTrainId;
	
	private JTextField[] iciRange = new JTextField[2];
	
	private JTextField iciChange;
	
	private JTextField angleError;
	
	private JTextField minTrainClicks, minAngleChange;
	
	private JTextField minUpdateGap;
	
	private ClickTrainIdDialog(Frame parentFrame) {
		
		/*
		 * 
	public boolean runClickTrainId = false;
	public double[] iciRange = {0.1, 2.0};
	public double maxIciChange = 1.2;
	public double okAngleError = 2.0;
	public double initialPerpendicularDistance = 100;
	public int minTrainClicks = 6;
	public double iciUpdateRatio = 0.5; //1 == full update, 0 = no update

		 */

		super(parentFrame, "Click Train Identification", true);
		
		GridBagConstraints c;

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		JPanel ctrlPanel = new WestAlignedPanel(new GridBagLayout());
		p.add(ctrlPanel);
		ctrlPanel.setBorder(new TitledBorder("Control"));
		c = new PamGridBagContraints();
		c.gridwidth = 3;
		ctrlPanel.add(runClickTrainId = new JCheckBox("Run Automatic Click Train Id"), c);
		c.gridwidth = 1;
		c.gridy ++;
		c.gridx = 0;
		ctrlPanel.add(new JLabel("Min number of clicks per train ", SwingConstants.RIGHT), c);
		c.gridx++;
		ctrlPanel.add(minTrainClicks = new JTextField(4), c);
		c.gridy ++;
		c.gridx = 0;
		ctrlPanel.add(new JLabel("Min angle change for TMA ", SwingConstants.RIGHT), c);
		c.gridx++;
		ctrlPanel.add(minAngleChange = new JTextField(4), c);
		c.gridy ++;
		c.gridx = 0;
		ctrlPanel.add(new JLabel("Min interval between updates ", SwingConstants.RIGHT), c);
		c.gridx++;
		ctrlPanel.add(minUpdateGap = new JTextField(4), c);
		c.gridx++;
		ctrlPanel.add(new JLabel(" s"), c);
		
		runClickTrainId.addActionListener(new CheckChanged());
		
		JPanel iciPanel = new WestAlignedPanel();
		p.add(iciPanel);
		iciPanel.setLayout(new GridBagLayout());
		iciPanel.setBorder(new TitledBorder("ICI changes"));
		c = new PamGridBagContraints();
		iciPanel.add(new JLabel("Min ICI ", SwingConstants.RIGHT), c);
		c.gridx++;
		iciPanel.add(iciRange[0] = new JTextField(4), c);
		c.gridx++;
		iciPanel.add(new JLabel("  Max ", SwingConstants.RIGHT), c);
		c.gridx++;
		iciPanel.add(iciRange[1] = new JTextField(4), c);
		c.gridx++;
		iciPanel.add(new JLabel(" s", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		iciPanel.add(new JLabel("Max ICI change ratio ", SwingConstants.RIGHT), c);
		c.gridx+=c.gridwidth;
		c.gridwidth = 1;
		iciPanel.add(iciChange = new JTextField(4), c);
		c.gridx++;
		iciPanel.add(new JLabel(" old/new OR new/old", SwingConstants.LEFT), c);
		

		JPanel anglePanel = new WestAlignedPanel();
		p.add(anglePanel);
		anglePanel.setLayout(new GridBagLayout());
		anglePanel.setBorder(new TitledBorder("Angle changes"));
		c = new PamGridBagContraints();
		anglePanel.add(new JLabel("Max angle error", SwingConstants.RIGHT), c);
		c.gridx++;
		anglePanel.add(angleError = new JTextField(4), c);
		c.gridx++;
		anglePanel.add(new JLabel(" " + LatLong.deg, SwingConstants.LEFT), c);
		
		
		runClickTrainId.setToolTipText("Enable automatic click train detection");
		minTrainClicks.setToolTipText("Enter the minimum number of clicks for a click train to be accepted");
		minAngleChange.setToolTipText("Enter the minimum angle change for target motion analysis");
		minUpdateGap.setToolTipText("Minimum time interval between localisation updates (can be slow for long click trains)");
		iciRange[0].setToolTipText("Minimum allowed inter click interval in seconds");
		iciRange[1].setToolTipText("Maximum allowed inter click interval in seconds");
		iciChange.setToolTipText("Maximum ICI change ratio (old/new OR new/old)");
		angleError.setToolTipText("Max angle change when no track present, or Max angle offset when a predictive track exists");
		
		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_clickTrainIdentification");
		
		setDialogComponent(p);
	}
	
	static public ClickTrainIdParams showDialog(Frame parentFrame, ClickTrainIdParams newParameters) {
		
		
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ClickTrainIdDialog(parentFrame);
		}
		
		singleInstance.clickTrainIdParams = newParameters.clone();
		
		singleInstance.setParams(newParameters);
		
		singleInstance.setVisible(true);
		
		return singleInstance.clickTrainIdParams;
	}


	@Override
	public void cancelButtonPressed() {
		clickTrainIdParams = null;
	}
	
	private void setParams(ClickTrainIdParams newParameters) {
		runClickTrainId.setSelected(newParameters.runClickTrainId);
		minTrainClicks.setText(new Integer(newParameters.minTrainClicks).toString());
		minAngleChange.setText(String.format("%3.1f", newParameters.minAngleChange));
		minUpdateGap.setText(String.format("%d", newParameters.minUpdateGap));
		for (int i = 0; i < 2; i++) {
			iciRange[i].setText(String.format("%3.2f", newParameters.iciRange[i]));
		}
		iciChange.setText(String.format("%3.2f", newParameters.maxIciChange));
		angleError.setText(String.format("%3.2f", newParameters.okAngleError));
		enableControls();
	}

	@Override
	public boolean getParams(){
		clickTrainIdParams.runClickTrainId = runClickTrainId.isSelected();
		try {
			clickTrainIdParams.minTrainClicks = Integer.valueOf(minTrainClicks.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid minimum number of clicks");
		}
		try {
			clickTrainIdParams.minAngleChange = Double.valueOf(minAngleChange.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid minimum angle change");
		}
		try {
			clickTrainIdParams.minUpdateGap = Integer.valueOf(minUpdateGap.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid minimum update gap (must be integer)");
		}
		
		
		try {
			for (int i = 0; i < 2; i++) {
				clickTrainIdParams.iciRange[i] = Double.valueOf(iciRange[i].getText());
			}
			clickTrainIdParams.maxIciChange = Double.valueOf(iciChange.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid ICI parameters");
		}
		try {
			clickTrainIdParams.okAngleError = Double.valueOf(angleError.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid Angle parameters");
		}
		
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new ClickTrainIdParams());
	}


	private class CheckChanged implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
	
	public void enableControls() {
		boolean en = runClickTrainId.isSelected();
		minTrainClicks.setEnabled(en);
		minAngleChange.setEnabled(en);
		for (int i = 0; i < 2; i++) {
			iciRange[i].setEnabled(en);
		}
		iciChange.setEnabled(en);
		angleError.setEnabled(en);
		
	}
}
