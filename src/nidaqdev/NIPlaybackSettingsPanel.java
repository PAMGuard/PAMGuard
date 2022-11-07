package nidaqdev;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class NIPlaybackSettingsPanel implements PamDialogPanel {

	private NIFilePlayback niFilePlayback;
	
	private JPanel mainPanel;
	
	private JComboBox<String> outputLevel;

	public NIPlaybackSettingsPanel(NIFilePlayback niFilePlayback) {
		this.niFilePlayback = niFilePlayback;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("NI Play options"));
		GridBagConstraints c = new PamGridBagContraints();
		
		mainPanel.add(new JLabel("Output level ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(outputLevel = new JComboBox<>(), c);
		c.gridx++;
		mainPanel.add(new JLabel(" V(0-p)"), c);
		
		outputLevel.addItem("1.0");
		outputLevel.addItem("10.0");
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		NIDeviceInfo deviceInfo = niFilePlayback.getCurrentDeviceInfo();
		NIFilePlaybackParams params = niFilePlayback.getNiFilePlaybackParams();
		if (deviceInfo == null) {
			return;
		}
		int nRange = deviceInfo.getNumAOVoltageRanges();
		outputLevel.removeAllItems();
		int selInd = -1;
		for (int i = 0; i < nRange; i++) {
			double aRange = deviceInfo.getAOVoltageRangeEnd(i);
			outputLevel.addItem(String.format("%3.1f", aRange));
			if (aRange == params.outputRange) {
				selInd = i;
			}
		}
		if (selInd >= 0) {
			outputLevel.setSelectedIndex(selInd);
		}
	}

	@Override
	public boolean getParams() {
		NIDeviceInfo deviceInfo = niFilePlayback.getCurrentDeviceInfo();
		NIFilePlaybackParams params = niFilePlayback.getNiFilePlaybackParams();
		if (deviceInfo == null) {
			return false;
		}
		int nRange = deviceInfo.getNumAOVoltageRanges();
		int selInd = outputLevel.getSelectedIndex();
		if (selInd < 0 || selInd >= nRange) {
			return PamDialog.showWarning(null, "Invalid parameter", "Invalid output range");
		}
		params.outputRange = deviceInfo.getAOVoltageRangeEnd(selInd);
		return true;
	}

}
