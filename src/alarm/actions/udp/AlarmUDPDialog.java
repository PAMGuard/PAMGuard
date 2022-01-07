package alarm.actions.udp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class AlarmUDPDialog extends PamDialog {

	private static final long serialVersionUID = 1L;
	
	private static AlarmUDPDialog singleInstance;
	
	private AlarmUDPParams alarmUDPParams;
	
	private JTextField udpAddr, udpPort;

	private AlarmUDPDialog(Window parentFrame) {
		super(parentFrame, "UDP Alarm Messages", false);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("UDP Destination"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel(" Destination address ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(udpAddr = new JTextField(10), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel(" Destination port ", JLabel.RIGHT), c);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add(udpPort = new JTextField(5), c);
		
		setDialogComponent(mainPanel);
	}
	
	public static AlarmUDPParams showDialog(Window window, AlarmUDPParams params) {
		if (singleInstance == null || singleInstance.getOwner() != window) {
			singleInstance = new AlarmUDPDialog(window);
		}
		singleInstance.setParams(params);
		singleInstance.setVisible(true);
		
		return singleInstance.alarmUDPParams;
	}

	private void setParams(AlarmUDPParams params) {
		alarmUDPParams = params.clone();
		udpAddr.setText(params.destAddr);
		udpPort.setText(String.format("%d", params.destPort));
	}

	@Override
	public boolean getParams() {
		alarmUDPParams.destAddr = udpAddr.getText();
		try {
			alarmUDPParams.destPort = Integer.valueOf(udpPort.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid UDP Port");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		alarmUDPParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
	}

}
