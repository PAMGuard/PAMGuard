package loggerForms.network;

import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;

public class MulticastDialogPanel implements PamDialogPanel {
	
	private JPanel mainPanel;
	private LoggerMulticastManager loggerMulticastManager;
	
	private JTextField mAddr, mPort;

	public MulticastDialogPanel(LoggerMulticastManager loggerMulticastManager) {
		this.loggerMulticastManager = loggerMulticastManager;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Multicast Net Options"));
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

}
