package loggerForms.network;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextField;
import PamView.dialog.ScrollingPamLabel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamPanel;
import it.geosolutions.imageio.maskband.DatasetLayout;
import loggerForms.FormsAlertSidePanel;

public class MQTTSidePanel implements LoggerNetworkObserver {

	private LoggerMQTTManager mqttManager;

	private WarningPanel mainPanel;

	private JLabel status;

	private JTextField nCon;

	private boolean errorState = false;

	private WarningPanel contactsPanel;

	private HashMap<String, WarningRow> contactRows = new HashMap<String, MQTTSidePanel.WarningRow>();

	private Timer stateTimer;

	//	private Timer statusTimer;

	public MQTTSidePanel(LoggerMQTTManager mqttManager) {
		this.mqttManager = mqttManager;

		mainPanel = new WarningPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Logger Network"));
		contactsPanel = new WarningPanel(null);
		contactsPanel.setLayout(new BoxLayout(contactsPanel, BoxLayout.X_AXIS));
		JPanel topPanel = new WarningPanel(new GridBagLayout());
		JPanel topWestPanel = new WarningPanel(new BorderLayout());
		topWestPanel.add(BorderLayout.WEST, topPanel);
		mainPanel.add(topWestPanel, BorderLayout.NORTH);
		mainPanel.add(BorderLayout.SOUTH, contactsPanel);

		GridBagConstraints c = new PamGridBagContraints();
		status = new PamLabel("MQTT Status");
		nCon = new PamTextField(3);
		nCon.setEditable(false);
		//		c.gridwidth = 2;
		topPanel.add(status, c);
		c.gridx++;
		topPanel.add(nCon, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		//		mainPanel.add(contacts, c);

		status.setToolTipText("Network status");
		nCon.setToolTipText("Number of client connections");

		mqttManager.addNetworkObserver(this);

		updateState(false, 0);

		stateTimer = new Timer(10000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stateTimer();
			}
		});
		stateTimer.start();

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

	protected void stateTimer() {
		updateContacts();

	}

	@Override
	public void updateState(boolean connected, int nClient) {
		status.setText(connected ? "Connected " : "Disconnected ");
		nCon.setText(String.format("%d", nClient));
		errorState = connected == false;
		mainPanel.repaint();
		updateContacts();
	}

	/**
	 * Display a list of contacts. 
	 */
	synchronized public void updateContacts() {
		HashMap<String, ContactData> loggerContacts = mqttManager.getLoggerContacts();
		String txt;
		int n = 0;
		long now = System.currentTimeMillis();
		errorState = false;
		
		synchronized (loggerContacts) {
			Set<String> keys = loggerContacts.keySet();
			for (String key : keys) {
				WarningRow warningRow = findWarningRow(key);
				ContactData cd = loggerContacts.get(key);
				Long t = cd.getLastUpdateTime();
				txt = key;
				int del = (int)((now-t)/1000);
				if (del > 20) {
					txt += String.format(", %ds ago", del);
				}
				else {
					txt += ", ok";
				}
				if (del > 40) {
					errorState = true;
				}
				String bat = cd.getBattery();
				if (bat != null) {
					txt += ", Bat " + bat + "%";
					try {
						double batPercent = Double.valueOf(bat);
						warningRow.setBatterWarning(batPercent < 50); 
					}
					catch (NumberFormatException e) {

					}
				}
				warningRow.setText(txt);
			}
		}
//		errorState = true;
//		if (errorState) {
			mainPanel.repaint();
//		}
		//		System.out.println(txt);
		//		contacts.setText(txt);
	}

	private class WarningPanel extends PamPanel {

		public WarningPanel(LayoutManager gridBagLayout) {
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

	private class WarningRow extends PamLabel {

		private boolean warning;
		private boolean batteryWarning = true;

		public WarningRow() {
			super(" ", JLabel.LEFT);
		}

		@Override
		public Color getBackground() {
			if (batteryWarning) {
				return Color.ORANGE;
			}
			return super.getBackground();
		}


		public void setBatterWarning(boolean b) {
			batteryWarning = b;
			repaint();
			setOpaque(b);
			setBackground(getBackground());
		}

		@Override
		public void setBackground(Color bg) {
			if (batteryWarning) {
				bg = Color.ORANGE;
			}
			super.setBackground(bg);
		}

	}

	/**
	 * find a warning row, adding it if necessary
	 * @return
	 */
	private WarningRow findWarningRow(String platform) {
		WarningRow row = contactRows.get(platform);
		if (row == null) {
			row = new WarningRow();
			contactRows.put(platform, row);
			contactsPanel.add(row);
			mainPanel.repaint();
		}
		return row;
	}

	//	public void update() {
	//		String stat = mqttManager.getStatus();
	//		status.setText(stat);
	//		mqttManager.getNConnections();
	//	}

}
