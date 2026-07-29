package NMEA.udp;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import NMEA.NMEAControl;
import NMEA.NMEAParameters;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextField;

public class UdpNMEAPanel implements PamDialogPanel {

	private JPanel udpPortSelection;
	private JLabel portSettingsLabel;
	private JTextField portTextField;
	private PamCheckBox multicastCheckBox;
	private PamTextField groupTextField;
	private NMEAControl nmeaControl;

	public UdpNMEAPanel(UdpNMEAProvider udpNMEAProvider) {
		this.nmeaControl = udpNMEAProvider.getNmeaControl();
		udpPortSelection = new JPanel();
		udpPortSelection.setBorder(BorderFactory
				.createTitledBorder("UDP Settings"));
		udpPortSelection.setLayout(new GridLayout(3,2));
		portSettingsLabel = new JLabel();
		portSettingsLabel.setText("UDP Port Number");
		udpPortSelection.add(portSettingsLabel);
		udpPortSelection.add(portTextField = new JTextField(4));
		udpPortSelection.add(new PamLabel("Multicast"));
		udpPortSelection.add(multicastCheckBox = new PamCheckBox());
		multicastCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				multiCastClicked();
			}
		});
		udpPortSelection.add(new PamLabel("Group IP address"));
		udpPortSelection.add(groupTextField = new PamTextField(12));
	}

	protected void multiCastClicked() {
enableControls();
	}

	private void enableControls() {
		groupTextField.setEnabled(multicastCheckBox.isSelected());		
	}

	@Override
	public JComponent getDialogComponent() {
		return udpPortSelection;
	}

	@Override
	public void setParams() {
		NMEAParameters nmeaParameters = nmeaControl.getNmeaParameters();

		portTextField.setText(String.format("%d", nmeaParameters.port));
		groupTextField.setText(nmeaParameters.multicastGroup);

		enableControls();
	}

	@Override
	public boolean getParams() {
		NMEAParameters nmeaParameters = nmeaControl.getNmeaParameters();
		nmeaParameters.port = Integer.valueOf(portTextField.getText());
		nmeaParameters.multicast = multicastCheckBox.isSelected();
		if (multicastCheckBox.isSelected()) {
			nmeaParameters.multicastGroup = groupTextField.getText();
		}
		return true;
	}

}
