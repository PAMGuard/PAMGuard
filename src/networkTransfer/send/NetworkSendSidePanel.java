package networkTransfer.send;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

public class NetworkSendSidePanel implements PamSidePanel {

	private NetworkSender networkSender;
	
	private JPanel mainPanel;
	
	private JTextField ipAddress;
	
	private JTextField status;
	
	private JTextField queueSize, queueLength;

	private TitledBorder titledBorder;
	 
	public NetworkSendSidePanel(NetworkSender networkSender) {
		this.networkSender = networkSender;
		mainPanel = new PamPanel();
		titledBorder = new TitledBorder(networkSender.getUnitName());
		mainPanel.setBorder(titledBorder);
		mainPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new PamGridBagContraints();

		c.gridwidth = 1;
		PamDialog.addComponent(mainPanel, new PamLabel("Address ", JLabel.RIGHT), c);
		c.gridwidth = 2;
		c.gridx++;
		PamDialog.addComponent(mainPanel, ipAddress = new JTextField(14), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(mainPanel, new PamLabel("Status ", JLabel.RIGHT), c);
		c.gridwidth = 2;
		c.gridx++;
		PamDialog.addComponent(mainPanel, status = new JTextField(10), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		PamDialog.addComponent(mainPanel, new PamLabel("Q' Size ", JLabel.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, queueSize = new JTextField(4), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, new PamLabel(" kilobytes"), c);
		c.gridx = 0;
		c.gridy++;
		PamDialog.addComponent(mainPanel, new PamLabel("Length ", JLabel.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, queueLength = new JTextField(4), c);
		c.gridx++;
		PamDialog.addComponent(mainPanel, new PamLabel(" objects"), c);
		
		ipAddress.setEditable(false);
		status.setEditable(false);
		queueLength.setEditable(false);
		queueSize.setEditable(false);
		
		Timer t = new Timer(1000, new TimerCall());
		t.start();
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		titledBorder.setTitle(newName);
	}

	private class TimerCall implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			timerCall();
		}
	}

	public void timerCall() {
		ipAddress.setText(String.format("%s (%d)", networkSender.networkSendParams.ipAddress, networkSender.networkSendParams.portNumber));
		status.setText(networkSender.getStatus());
		queueLength.setText(String.format("%d", networkSender.getQueueLength()));
		queueSize.setText(String.format("%d", networkSender.getQueueSize()));
	}
}
