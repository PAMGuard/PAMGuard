package loggerForms.network;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.ScrollingPamLabel;
import PamView.panel.PamPanel;
import loggerForms.FormsAlertSidePanel;

public class MQTTSidePanel implements LoggerNetworkObserver {

	private LoggerMQTTManager mqttManager;
	
	private WarningPanel mainPanel;
	
	private JLabel status;
	
	private JTextField nCon;
	
	private boolean errorState = false;
	
	private JLabel contacts;
	
//	private Timer statusTimer;

	public MQTTSidePanel(LoggerMQTTManager mqttManager) {
		this.mqttManager = mqttManager;
		
		mainPanel = new WarningPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("MQTT Connection"));
		GridBagConstraints c = new PamGridBagContraints();
		status = new PamLabel("MQTT Status");
		nCon = new JTextField(3);
		nCon.setEditable(false);
		contacts = new JLabel();
//		c.gridwidth = 2;
		mainPanel.add(status, c);
		c.gridx++;
		mainPanel.add(nCon, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		mainPanel.add(contacts, c);
		
		status.setToolTipText("Network status");
		nCon.setToolTipText("Number of client connections");
		
		mqttManager.addNetworkObserver(this);
		
		updateState(false, 0);
				
//		statusTimer = new Timer(10000, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				update();
//			}
//		});
//		statusTimer.setInitialDelay(1000);
//		statusTimer.start();
	}
	
	public JPanel getPanel() {
		return mainPanel;
	}

	@Override
	public void updateState(boolean connected, int nClient) {
		status.setText(connected ? "Connected " : "Disconnected ");
		nCon.setText(String.format("%d", nClient));
		errorState = connected == false;
		mainPanel.repaint();
		updateContacts();
	}
	
	private class WarningPanel extends PamPanel {

		public WarningPanel(GridBagLayout gridBagLayout) {
			super(gridBagLayout);
		}

		@Override
		public Color getBackground() {
			if (errorState) {
				return FormsAlertSidePanel.warningColour;
			}
			else {
				return super.getBackground();
			}
		}
		
	}

	/**
	 * Display a list of contacts. 
	 */
	public void updateContacts() {
		HashMap<String, Long> loggerContacts = mqttManager.getLoggerContacts();
		String txt = "<html>";
		int n = 0;
		long now = System.currentTimeMillis();
		synchronized (loggerContacts) {
			Set<String> keys = loggerContacts.keySet();
			for (String key : keys) {
				Long t = loggerContacts.get(key);
				if (n++ > 0) {
					txt += "<br>";
				}
				txt += String.format("%s:\t %ds ago", key, (int)((now-t)/1000));
			}
			txt += "</html>";
		}
//		System.out.println(txt);
		contacts.setText(txt);
	}
	
//	public void update() {
//		String stat = mqttManager.getStatus();
//		status.setText(stat);
//		mqttManager.getNConnections();
//	}

}
