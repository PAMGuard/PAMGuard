package AIS;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import NMEA.NMEADataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;

public class AISSettingsDialog extends PamDialog {

	private AISParameters aisParameters;

	private static AISSettingsDialog singleInstance; 

	private SourcePanel sourcePanel;

	private JCheckBox limitRange;

	private JTextField maxRange;

	private AISSettingsDialog(Frame parentFrame) {
		super(parentFrame, "AIS Options", false);

		sourcePanel = new SourcePanel(this, "Select NMEA Source", NMEADataUnit.class, false, false);

		JPanel rangePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		rangePanel.setBorder(new TitledBorder("Distant vessels"));
		c.gridwidth = 3;
		addComponent(rangePanel, limitRange = new JCheckBox("Ignore distant vessels"), c);
		limitRange.addActionListener(new LimitRange());
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(rangePanel, new JLabel("Max range "), c);
		c.gridx++;
		addComponent(rangePanel, maxRange = new JTextField(3), c);
		c.gridx++;
		addComponent(rangePanel, new JLabel(" km"), c);

		JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.NORTH, sourcePanel.getPanel());
		p.add(BorderLayout.CENTER, rangePanel);

		setHelpPoint("mapping.AIS.docs.AISConfiguration");
		setDialogComponent(p);

	}

	public static AISParameters showDialog(Frame parentFrame, AISParameters aisParameters) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new AISSettingsDialog(parentFrame);
		}
		singleInstance.aisParameters = aisParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return aisParameters;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		aisParameters = null;
	}

	private void setParams() {
		sourcePanel.setSource(aisParameters.nmeaSource);
		limitRange.setSelected(aisParameters.limitRange);
		maxRange.setText(formatDouble(aisParameters.maxRange_km));
		enableControls();
	}

	@Override
	public boolean getParams() {
		aisParameters.nmeaSource = sourcePanel.getSource().getDataName();
		aisParameters.limitRange = limitRange.isSelected();
		if (aisParameters.limitRange) {
			try {
				aisParameters.maxRange_km = Double.valueOf(maxRange.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid maximum range");
			}
		}

		return (aisParameters.nmeaSource != null);
	}
	
	class LimitRange implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	public void enableControls() {
		maxRange.setEnabled(limitRange.isSelected());
	}

}
