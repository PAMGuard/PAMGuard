package networkTransfer.receive.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ListIterator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextDisplay;
import PamView.panel.PamPanel;
import networkTransfer.receive.BuoyStatusDataBlock;
import networkTransfer.receive.BuoyStatusDataUnit;
import networkTransfer.receive.NetworkReceiver;

public class RXSummaryPanel {

	private NetworkReceiver networkReceiver;
	
	private JPanel mainPanel;
	
	private JTextField rxPort;
	
	private JTextField nStations, nActive, nDisconnected;

	public RXSummaryPanel(NetworkReceiver networkReceiver) {
		super();
		this.networkReceiver = networkReceiver;
		mainPanel = new PamPanel(new BorderLayout());
		JPanel leftPanel = new PamPanel();
		mainPanel.add(BorderLayout.WEST, leftPanel);
		leftPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Summery"));
		leftPanel.add(new PamLabel("Listening on port "), c);
		c.gridx++;
		leftPanel.add(rxPort = new PamTextDisplay(5));
		c.gridx++;
		leftPanel.add(new PamLabel("          "));
		c.gridx++;
		leftPanel.add(new PamLabel("Active stations "), c);
		c.gridx++;
		leftPanel.add(nActive = new PamTextDisplay(5));
		c.gridx++;
		leftPanel.add(new PamLabel(";  inactive "), c);
		c.gridx++;
		leftPanel.add(nDisconnected = new PamTextDisplay(5));
		c.gridx++;
		
		Timer t = new Timer(2000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				timerAction();
			}
		});
		t.start();
	}
	
	protected void timerAction() {
		if (mainPanel.isVisible() == false) {
			return;
		}
		rxPort.setText(String.format("%d", networkReceiver.getNetworkReceiveParams().receivePort));
		
		BuoyStatusDataBlock bsdb = networkReceiver.getBuoyStatusDataBlock();
		int nDead = 0, nActive = 0;
		synchronized (bsdb) {
			ListIterator<BuoyStatusDataUnit> it = bsdb.getListIterator(0);
			while (it.hasNext()) {
				BuoyStatusDataUnit bs = it.next();
				if (bs.getCommandStatus() == NetworkReceiver.NET_PAM_COMMAND_START) {
					nActive++;
				}
				else {
					nDead++;
				}
			}
		}
		this.nActive.setText(String.format("%d", nActive));
		this.nDisconnected.setText(String.format("%d", nDead));
	}

	public JComponent getComponent() {
		return mainPanel;
	}
}
