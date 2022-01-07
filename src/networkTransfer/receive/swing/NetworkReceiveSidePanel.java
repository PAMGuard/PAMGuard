package networkTransfer.receive.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ListIterator;

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
import networkTransfer.receive.BuoyStatusDataBlock;
import networkTransfer.receive.BuoyStatusDataUnit;
import networkTransfer.receive.NetworkReceiver;

public class NetworkReceiveSidePanel implements PamSidePanel {

	private NetworkReceiver networkReceiver;
	private JPanel mainPanel;
	private TitledBorder titledBorder;
	private JTextField ipPort, connections, stations;
	private JTextField recentPackets, recentBytes;

	/**
	 * @param networkReceiver
	 */
	public NetworkReceiveSidePanel(NetworkReceiver networkReceiver) {
		super();
		this.networkReceiver = networkReceiver;
		mainPanel = new PamPanel();
		titledBorder = new TitledBorder(networkReceiver.getUnitName());
		mainPanel.setBorder(titledBorder);
		mainPanel.setLayout(new GridBagLayout());		
		GridBagConstraints c = new PamGridBagContraints();		
		c.gridwidth = 1;
		PamDialog.addComponent(mainPanel, new PamLabel("Port ", JLabel.RIGHT), c);
		c.gridwidth = 2;
		c.gridx++;
		PamDialog.addComponent(mainPanel, ipPort = new JTextField(5), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(mainPanel, new PamLabel("Remote Stations ", JLabel.RIGHT), c);
		c.gridwidth = 2;
		c.gridx++;
		PamDialog.addComponent(mainPanel, stations = new JTextField(5), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(mainPanel, new PamLabel("Open Connections ", JLabel.RIGHT), c);
		c.gridwidth = 2;
		c.gridx++;
		PamDialog.addComponent(mainPanel, connections = new JTextField(5), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(mainPanel, new PamLabel("Packets / sec ", JLabel.RIGHT), c);
		c.gridwidth = 2;
		c.gridx++;
		PamDialog.addComponent(mainPanel, recentPackets = new JTextField(5), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(mainPanel, new PamLabel("kBytes / sec ", JLabel.RIGHT), c);
		c.gridwidth = 2;
		c.gridx++;
		PamDialog.addComponent(mainPanel, recentBytes = new JTextField(5), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;

		ipPort.setEditable(false);
		connections.setEditable(false);
		stations.setEditable(false);
		recentPackets.setEditable(false);
		recentBytes.setEditable(false);;

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
		BuoyStatusDataBlock statsBlock = networkReceiver.getBuoyStatusDataBlock();
		ListIterator<BuoyStatusDataUnit> it = statsBlock.getListIterator(0);
		BuoyStatusDataUnit b;
		int nPrepared = 0, nStarted = 0, nStopped = 0;
		int n = 0;
		int nConnected = 0;
		while (it.hasNext()) {
			b = it.next();
			n++;
			if (b.getSocket() != null) {
				nConnected++;
			}
			switch(b.getCommandStatus()) {
			case NetworkReceiver.NET_PAM_COMMAND_PREPARE:
				nPrepared++;
				break;
			case NetworkReceiver.NET_PAM_COMMAND_START:
				nStarted++;
				break;
			case NetworkReceiver.NET_PAM_COMMAND_STOP:
				nStopped++;
				break;
			}
			
		}
		connections.setText(String.format("%d", nConnected));
		ipPort.setText(String.format("%d", networkReceiver.getNetworkReceiveParams().receivePort));
		stations.setText(String.format("%d",n));
		recentPackets.setText(String.format("%d", networkReceiver.getRecentPackets()));
		recentBytes.setText(String.format("%d", networkReceiver.getRecentDataBytes()/1024));
	}
	
}
