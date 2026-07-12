package loggerForms.network;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	
//	private Timer statusTimer;

	public MQTTSidePanel(LoggerMQTTManager mqttManager) {
		this.mqttManager = mqttManager;
		
		mainPanel = new WarningPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("MQTT Connection"));
		GridBagConstraints c = new PamGridBagContraints();
		status = new PamLabel("MQTT Status");
		nCon = new JTextField(3);
		nCon.setEditable(false);
//		c.gridwidth = 2;
		mainPanel.add(status, c);
		c.gridx++;
		mainPanel.add(nCon, c);
		
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
	
//	public void update() {
//		String stat = mqttManager.getStatus();
//		status.setText(stat);
//		mqttManager.getNConnections();
//	}

}
