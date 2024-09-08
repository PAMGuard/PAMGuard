package clipgenerator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ClipDisplayDialog extends PamDialog {

	private ClipSettings clipSettings;

	private JTextField[] rangeFields;

	private ClipDisplayDialog(Window parentFrame, ClipControl clipControl) {
		super(parentFrame, clipControl.getUnitName() + " display", false);
		this.clipSettings = clipControl.clipSettings.clone();

		JPanel rangePanel = new JPanel(new GridBagLayout());
		rangePanel.setBorder(new TitledBorder("Map bearing lines"));
		GridBagConstraints c = new PamGridBagContraints();
		rangePanel.add(new JLabel("Clip trigger ", SwingConstants.RIGHT), c);
		c.gridx++;
		rangePanel.add(new JLabel("Range (m)"));
		rangeFields = new JTextField[clipSettings.getNumClipGenerators()];
		for (int i = 0; i < clipSettings.getNumClipGenerators(); i++) {
			c.gridx = 0;
			c.gridy++;
			ClipGenSetting genSet = clipSettings.getClipGenSetting(i);
			rangePanel.add(new JLabel(genSet.dataName, SwingConstants.RIGHT), c);
			c.gridx++;
			rangePanel.add(rangeFields[i] = new JTextField(4), c);
			rangeFields[i].setText(String.format("%3.1f", genSet.mapLineLength));
		}

		setDialogComponent(rangePanel);
	}

	public static ClipSettings showDialog(Window owner, ClipControl clipControl) {
		ClipDisplayDialog displayDialog = new ClipDisplayDialog(owner, clipControl);

		displayDialog.setParams();
		displayDialog.setVisible(true);
		return displayDialog.clipSettings;
	}

	private void setParams() {
		// nothing to do - all done in constructor
	}

	@Override
	public boolean getParams() {
		for (int i = 0; i < clipSettings.getNumClipGenerators(); i++) {
			ClipGenSetting genSet = clipSettings.getClipGenSetting(i);
					try {
						genSet.mapLineLength = Double.valueOf(rangeFields[i].getText());
					}
			catch (NumberFormatException e) {
				return showWarning("Invalid range value for " + genSet.dataName);
			}
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		clipSettings = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
