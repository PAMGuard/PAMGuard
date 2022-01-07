package nmeaEmulator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import NMEA.NMEAParameters;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Panel of information near top of main frame. 
 * @author Doug Gillespie
 *
 */
public class InfoPanel {

	private JPanel panel;
	
	private NMEAFrontEnd nmeaFrontEnd;
	
	private SerialTextField serialPort, startTime, endTime, dbTime, simTime,
	runStatus, message;
	private JProgressBar simProgress;
	private JCheckBox repeat;
	
	public InfoPanel(NMEAFrontEnd nmeaFrontEnd) {
		this.nmeaFrontEnd = nmeaFrontEnd;
		
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		panel.setBorder(new TitledBorder("Simulation Data"));

		PamDialog.addComponent(panel, new JLabel("Serial Port ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 3;
		PamDialog.addComponent(panel, serialPort = new SerialTextField(10), c);		
		

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, new JLabel("Data times from", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, startTime = new SerialTextField(20), c);
		c.gridx++;
		PamDialog.addComponent(panel, new JLabel(" to ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, endTime = new SerialTextField(20), c);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(panel, new JLabel("Status ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, runStatus = new SerialTextField(8), c);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(panel, new JLabel("Current Data Time ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, dbTime = new SerialTextField(20), c);
		c.gridx++;
		c.gridwidth = 2;
		PamDialog.addComponent(panel, simProgress = new JProgressBar(), c);
		simProgress.setMaximum(100);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, new JLabel("Current Simulation Time", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, simTime = new SerialTextField(20), c);
		c.gridx++;
		c.gridwidth = 2;
		PamDialog.addComponent(panel, repeat = new JCheckBox("Repeat"), c);

		c.gridy++;
		c.gridx = 0;
//		c.gridwidth = 1;
//		PamDialog.addComponent(panel, new JLabel("Output ", JLabel.RIGHT), c);
//		c.gridx++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, message = new SerialTextField(10), c);	
		
		
	}

	public JPanel getPanel() {
		return panel;
	}
	
	public void fillPanel() {
		NMEAParameters serialParams = nmeaFrontEnd.getSerialParams();
		if (serialParams.serialPortName == null) {
			serialPort.setText("No serial port selected");
		}
		else {
			serialPort.setText(String.format("%s, Baud Rate %d bps", 
					serialParams.serialPortName, serialParams.serialPortBitsPerSecond));
		}
		runStatus.setText(nmeaFrontEnd.getStatusString());
		NMEAEmulatorParams params = nmeaFrontEnd.emulatorParams;
		long[] times = nmeaFrontEnd.getTimeLimits();
		startTime.setText(PamCalendar.formatDBDateTime(times[0]));
		endTime.setText(PamCalendar.formatDBDateTime(times[1]));
		repeat.setSelected(params.repeat);
	}

	public void setProgress(EmulationProgress emulationProgress) {
		if (emulationProgress.status == EmulationProgress.STATUS_IDLE) {
			runStatus.setText("Idle");
		}
		else if (emulationProgress.status == EmulationProgress.STATUS_RUNNING) {
			dbTime.setText(PamCalendar.formatDBDateTime(emulationProgress.dataTime));
			simTime.setText(PamCalendar.formatDBDateTime(emulationProgress.currTime));
			simProgress.setValue(emulationProgress.percentProgress);
			runStatus.setText("Running");
		}
		else if (emulationProgress.status == EmulationProgress.STATUS_MESSAGE) {
			message.setText(emulationProgress.message);
		}
	}

	public boolean isRepeat() {
		return repeat.isSelected();
	}
	
}
