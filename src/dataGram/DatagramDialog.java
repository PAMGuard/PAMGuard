package dataGram;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class DatagramDialog extends PamDialog {

	private DatagramManager datagramManager;

	private DatagramSettings datagramSettings;
		
	private static DatagramDialog singleInstance;
	
	private JLabel infoArea;
	private JTextField datagramSeconds;

	private DatagramDialog(Window parentFrame, DatagramManager datagramManager) {
		super(parentFrame, "Datagram Settings", true);
		this.datagramManager = datagramManager;
		String infoString = "<html>" + 
		"Datagrams are an efficient way to provide you with an overview of the contents of a large dataset. <br>" +
		"Several PAMGuard modules are able to summarise their data in the form of spectrogram like displays <br>" +
		"on the datamap. for large datasets, lasting several months, 10 minute (or longer) bis are appropriate. <br>" +
		"However, for shorter datasets, you may want to select bins of less than a minute in length. Note that <br>" +
		"the entire datagram is always held in memory, so if you select short bin lengths for a very long dataset <br>"
		+ "you may run out of memory</html>";
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Datagrams"));
		mainPanel.add(BorderLayout.NORTH, infoArea = new JLabel(infoString));
//		infoArea.setPreferredSize(preferredSize);
//		infoArea.setWrapStyleWord(true);
		JPanel tPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(tPanel, new JLabel("Datagram bin size ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(tPanel, datagramSeconds = new JTextField(5), c);
		c.gridx++;
		addComponent(tPanel, new JLabel(" seconds", JLabel.LEFT), c);
		mainPanel.add(BorderLayout.CENTER, tPanel);
		
		
		setDialogComponent(mainPanel);
	}
	
	public static DatagramSettings showDialog(Window parentFrame, DatagramManager datagramManager) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || datagramManager != singleInstance.datagramManager) {
			singleInstance = new DatagramDialog(parentFrame, datagramManager);
		}
		singleInstance.datagramSettings = datagramManager.getDatagramSettings().clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
				
		return singleInstance.datagramSettings;
	}
	
	
	private void setParams() {
		datagramSeconds.setText(String.format("%d", datagramSettings.datagramSeconds));
	}

	@Override
	public boolean getParams() {
		try {
			datagramSettings.datagramSeconds = Integer.valueOf(datagramSeconds.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid time period. Must be an integer number of seconds.");
		}
		if (datagramSettings.datagramSeconds <= 0) {
			return showWarning("Invalid time period. Must be an positive integer number of seconds.");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		datagramSettings = null;
	}

	@Override
	public void restoreDefaultSettings() {
		datagramSettings = datagramManager.getDatagramSettings().clone();
		setParams();
	}

}
