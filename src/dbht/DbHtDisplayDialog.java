package dbht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class DbHtDisplayDialog extends PamDialog {

	private DbHtControl dbhtControl;
	private static DbHtDisplayDialog singleInstance;
	private DbHtDisplayParams params;
	
	private JCheckBox autoScale;
	private JTextField minAmp, maxAmp;
	private JCheckBox showGrid;
	
	private JCheckBox drawLines;
	private JCheckBox colourByChannel;
	private JTextField symbolSize;
	private JCheckBox[] showMeasure = new JCheckBox[DbHtControl.NMEASURES];
	
	
	private DbHtDisplayDialog(Window parentFrame, DbHtControl dbhtControl) {
		super(parentFrame, dbhtControl.getUnitName() + " Display Options", true);
		this.dbhtControl = dbhtControl;
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel scalePanel = new JPanel(new GridBagLayout());
		scalePanel.setBorder(new TitledBorder("Amplitude Scale"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(scalePanel, showGrid = new JCheckBox("Show Grid"), c);
		c.gridy++;
		addComponent(scalePanel, autoScale = new JCheckBox("Auto Scale"), c);
		autoScale.addActionListener(new EnableAction());
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		addComponent(scalePanel, new JLabel("Min ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(scalePanel, minAmp = new JTextField(5), c);
		c.gridx++;
		addComponent(scalePanel, new JLabel(" dB re 1\u03BCPa ", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(scalePanel, new JLabel("Max ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(scalePanel, maxAmp = new JTextField(5), c);
		c.gridx++;
		addComponent(scalePanel, new JLabel(" dB re 1\u03BCPa ", SwingConstants.LEFT), c);
		
		mainPanel.add(scalePanel);
		
		JPanel symPanel = new JPanel(new GridBagLayout());
		symPanel.setBorder(new TitledBorder("Symbols"));
		c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(symPanel, drawLines = new JCheckBox("Draw Lines"), c);
		c.gridy++;
		addComponent(symPanel, colourByChannel = new JCheckBox("Colour By Channel"), c);
		c.gridwidth = 1;
		c.gridy++;
		addComponent(symPanel, new JLabel("Symbol Size ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(symPanel, symbolSize = new JTextField(5), c);
		c.gridx++;
		addComponent(symPanel, new JLabel(" pixels ", SwingConstants.LEFT), c);
		c.gridwidth = 3;
		for (int i = 0; i < DbHtControl.NMEASURES; i++) {
			c.gridx = 0;
			c.gridy++;
			addComponent(symPanel, showMeasure[i] = new JCheckBox("Show " + DbHtControl.measureNames[i]), c);
		}
		mainPanel.add(symPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static DbHtDisplayParams showDialog(Window frame, DbHtControl dbHtControl, DbHtDisplayParams dbHtDisplayParams) {
		if (singleInstance == null || singleInstance.getOwner() != frame || singleInstance.dbhtControl != dbHtControl) {
			singleInstance = new DbHtDisplayDialog(frame, dbHtControl);
		}
		singleInstance.params = dbHtDisplayParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	private void enableControls() {
		boolean e = !autoScale.isSelected();
		minAmp.setEnabled(e);
		maxAmp.setEnabled(e);
		if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
			drawLines.setEnabled(false);
			drawLines.setSelected(false);
		}
	}

	private void setParams() {
		showGrid.setSelected(params.showGrid);
		autoScale.setSelected(params.autoScale);
		minAmp.setText(new Double(params.minAmplitude).toString());
		maxAmp.setText(new Double(params.maxAmplitude).toString());
		drawLines.setSelected(params.drawLine);
		colourByChannel.setSelected(params.colourByChannel);
		symbolSize.setText(new Integer(params.symbolSize).toString());
		for (int i = 0; i < DbHtControl.NMEASURES; i++) {
			showMeasure[i].setSelected(((1<<i) & params.showWhat) != 0);
		}
		enableControls();
	}

	@Override
	public boolean getParams() {
		params.showGrid = showGrid.isSelected();
		params.autoScale = autoScale.isSelected();
		params.drawLine = drawLines.isSelected();
		params.colourByChannel = colourByChannel.isSelected();
		params.showWhat = 0;
		for (int i = 0; i < DbHtControl.NMEASURES; i++) {
			if (showMeasure[i].isSelected()) {
				params.showWhat |= 1<<i;
			}
		}
		try {
			params.minAmplitude = Double.valueOf(minAmp.getText());
			params.maxAmplitude = Double.valueOf(maxAmp.getText());
			params.symbolSize = Integer.valueOf(symbolSize.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Number format error");
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		params = new DbHtDisplayParams();
		setParams();
	}

	/**
	 * @return the dbhtControl
	 */
	public DbHtControl getDbhtControl() {
		return dbhtControl;
	}

	private class EnableAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
}
