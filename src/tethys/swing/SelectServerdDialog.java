package tethys.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import tethys.TethysControl;
import tethys.output.TethysExportParams;

public class SelectServerdDialog extends PamDialog {
	
	private static final long serialVersionUID = 1L;

	private JTextField serverHost, serverPort;

	private TethysExportParams exportParams;
	
	private static SelectServerdDialog singleInstance;

	private SelectServerdDialog(TethysControl tethysControl, Window parentFrame) {
		super(parentFrame, "Tethys Server", true);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Tethys Server"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Host: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(serverHost = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(new JLabel("Port: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(serverPort = new JTextField(4), c);
		
		setDialogComponent(mainPanel);
		
	}
	
	public static final TethysExportParams showDialog(TethysControl tethysControl, Window parentFrame, TethysExportParams exportParams) {
		if (singleInstance == null) {
			singleInstance = new SelectServerdDialog(tethysControl, parentFrame);
		}
		singleInstance.setParams(exportParams);
		singleInstance.setVisible(true);
		return singleInstance.exportParams;
	}

	private void setParams(TethysExportParams exportParams) {
		this.exportParams = exportParams;
		serverHost.setText(exportParams.serverName);
		serverPort.setText(String.format("%d", exportParams.port));
	}

	@Override
	public boolean getParams() {
		String newHost = serverHost.getText();
		int newPort = 0;
		try {
			newPort = Integer.valueOf(serverPort.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Server port must be a valid integer number");
		}
		exportParams.serverName = newHost;
		exportParams.port = newPort;
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		exportParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		TethysExportParams defaultParams = new TethysExportParams();
		exportParams.serverName = defaultParams.serverName;
		exportParams.port = defaultParams.port;
		setParams(exportParams);
	}

}
