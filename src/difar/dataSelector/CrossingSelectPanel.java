package difar.dataSelector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog panel for the crossing data selector.
 */
public class CrossingSelectPanel implements PamDialogPanel {

	private final CrossingDataSelector selector;

	private final JPanel mainPanel = new JPanel(new GridBagLayout());

	private final JTextField minAngle = new JTextField(5);

	private final JCheckBox useMaxError = new JCheckBox("Largest location error (km)");

	private final JTextField maxErrorKm = new JTextField(5);

	private final JCheckBox hideOnBuoy = new JCheckBox("Hide triangulations on one of their own sonobuoys");

	public CrossingSelectPanel(CrossingDataSelector selector) {
		this.selector = selector;
		mainPanel.setBorder(new TitledBorder("Triangulation quality"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Smallest crossing angle (deg) ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minAngle, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(useMaxError, c);
		c.gridx++;
		mainPanel.add(maxErrorKm, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		mainPanel.add(hideOnBuoy, c);
		minAngle.setToolTipText("<HTML>Triangulations whose bearings meet at a smaller angle are hidden.<br>"
				+ "Near 0 the bearings point the same way and the location is unreliable.</HTML>");
		useMaxError.setToolTipText("Hide triangulations whose larger x or y error exceeds this");
		hideOnBuoy.setToolTipText("<HTML>Within the on-buoy radius set in the DIFAR settings.<br>"
				+ "Their errors are set to that radius.</HTML>");
		useMaxError.addActionListener(e -> maxErrorKm.setEnabled(useMaxError.isSelected()));
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		CrossingSelectParams p = selector.getParams();
		minAngle.setText(Double.toString(p.minAngle));
		useMaxError.setSelected(p.useMaxError);
		maxErrorKm.setText(Double.toString(p.maxErrorKm));
		maxErrorKm.setEnabled(p.useMaxError);
		hideOnBuoy.setSelected(p.hideOnBuoy);
	}

	@Override
	public boolean getParams() {
		CrossingSelectParams p = selector.getParams().clone();
		try {
			p.minAngle = Double.valueOf(minAngle.getText());
			p.maxErrorKm = Double.valueOf(maxErrorKm.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Triangulation filter", "Angle and error must be numbers");
		}
		if (p.minAngle < 0 || p.minAngle > 180) {
			return PamDialog.showWarning(null, "Triangulation filter", "The angle must be from 0 to 180 degrees");
		}
		if (p.maxErrorKm <= 0) {
			return PamDialog.showWarning(null, "Triangulation filter", "The largest error must be greater than zero");
		}
		p.useMaxError = useMaxError.isSelected();
		p.hideOnBuoy = hideOnBuoy.isSelected();
		selector.setParams(p);
		return true;
	}
}
