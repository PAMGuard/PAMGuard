package NMEA.simulated;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import NMEA.NMEAControl;
import NMEA.NMEAParameters;
import NMEA.NMEASimDialog;
import PamView.dialog.PamDialogPanel;

public class SimulatedNMEAPanel implements PamDialogPanel {

	private JPanel simPanel;
	private SimulatedNMEAProvider simulatedNMEAProvider;
	private NMEAControl nmeaControl;
	
	public SimulatedNMEAPanel(SimulatedNMEAProvider simulatedNMEAProvider) {
		this.simulatedNMEAProvider = simulatedNMEAProvider;
		this.nmeaControl = simulatedNMEAProvider.getNmeaControl();
		simPanel = new JPanel(new BorderLayout());
		simPanel.setBorder(new TitledBorder("Simulation Settings"));
		JButton button = new JButton("Settings ...");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettings();
			}
		});
		simPanel.add(BorderLayout.EAST, button);
	}

	protected void showSettings() {
		NMEAParameters nmeaParameters = nmeaControl.getNmeaParameters();
		NMEAParameters newParams = NMEASimDialog.showDialog(null, nmeaParameters);
		if (newParams != null) {
			nmeaControl.setNmeaParameters(newParams);
		}
		
	}

	@Override
	public JComponent getDialogComponent() {
		return simPanel;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		return true;
	}

}
