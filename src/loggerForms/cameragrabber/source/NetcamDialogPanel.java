package loggerForms.cameragrabber.source;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import loggerForms.cameragrabber.CameraParams;

public class NetcamDialogPanel implements CameraSourcePanel {

	private CameraParams cameraParams;
	
	private JPanel mainPanel;
	
	private JTextField networkId;

	public NetcamDialogPanel() {
		networkId = new JTextField(15);
		networkId.setToolTipText("Network topic id, something like: ");
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Netork Id ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(networkId, c);
	}

	@Override
	public JComponent getDialogComponent() { 
		return mainPanel;
	}

	@Override
	public void setParams(CameraParams cameraParams) {
		this.cameraParams = cameraParams;
		networkId.setText(cameraParams.cameraName);
	}

	@Override
	public CameraParams getParams() {
		cameraParams.cameraName = networkId.getText();
		if (cameraParams.cameraName == null || cameraParams.cameraName.length() == 0) {
			PamDialog.showWarning(null, "Error", "you must enter a network identifier for the camera to receive data ");
			return null;
		}
		return cameraParams;
	}

}
