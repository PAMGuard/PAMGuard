package whistlesAndMoans;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class WMRecordTriggerdialog extends PamDialog {

	private static WMRecordTriggerdialog singleInstance;
	
	private WMRecorderTriggerData wmrtData;
	
	private boolean ok = false;
	
	private JTextField minFreq, maxFreq;

	private WMRecorderTrigger wmRecordTrigger;
	
	private WMRecordTriggerdialog(Window parentFrame) {
		super(parentFrame, "", true);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel freqPanel = new JPanel();
		mainPanel.add(BorderLayout.CENTER, freqPanel);
		freqPanel.setLayout(new GridBagLayout());
		freqPanel.setBorder(new TitledBorder("Trigger Options"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(freqPanel, new JLabel("Minimum Frequency ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(freqPanel, minFreq = new JTextField(6), c);
		c.gridx++;
		addComponent(freqPanel, new JLabel(" Hz", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(freqPanel, new JLabel("Maximum Frequency ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(freqPanel, maxFreq = new JTextField(6), c);
		c.gridx++;
		addComponent(freqPanel, new JLabel(" Hz", SwingConstants.LEFT), c);
		
		setDialogComponent(mainPanel);
		
	}
	
	public static boolean showDialog(Window parent, WMRecorderTrigger wmRecordTrigger, WMRecorderTriggerData wmrtData) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new WMRecordTriggerdialog(parent);
		}
		singleInstance.setTitle(wmRecordTrigger.getName());
		singleInstance.wmrtData = wmrtData;
		singleInstance.wmRecordTrigger = wmRecordTrigger;
		singleInstance.setParams();
		singleInstance.ok = true;
		singleInstance.setVisible(true);
		return singleInstance.ok;
	}

	private void setParams() {
		minFreq.setText(String.format("%3.1f", wmrtData.minFreq));
		if (wmrtData.maxFreq == 0) {
			WhistleToneConnectProcess process = wmRecordTrigger.wmControl.getWhistleToneProcess();
			wmrtData.maxFreq = process.getSampleRate()/2;
		}
		maxFreq.setText(String.format("%3.1f", wmrtData.maxFreq));
	}

	@Override
	public boolean getParams() {
		double min, max;
		try {
			min = Double.valueOf(minFreq.getText());
			max = Double.valueOf(maxFreq.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Error in frequency value");
		}
		WhistleToneConnectProcess process = wmRecordTrigger.wmControl.getWhistleToneProcess();
		if (max == 0) {
			max = process.getSampleRate()/2;
		}
		wmrtData.minFreq = min;
		wmrtData.maxFreq = max;
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		ok = false;
	}

	@Override
	public void restoreDefaultSettings() {
		WhistleToneConnectProcess process = wmRecordTrigger.wmControl.getWhistleToneProcess();
		wmrtData.minFreq = 0;
		wmrtData.maxFreq = process.getSampleRate();
		setParams();
	}

}
