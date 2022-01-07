package clickDetector.echoDetection;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class JamieEchoDialogPanal implements EchoDialogPanel{
	
	
	private JamieEchoDetectionSystem jamieEchoDetectionSystem;
	
	private JPanel panel;
	
	private JTextField maxDelay;
	
	private JTextField maxAmp;
	
	private JTextField maxICI;

	
	
	public JamieEchoDialogPanal(
			JamieEchoDetectionSystem jamieEchoDetectionSystem) {
		super();
		this.jamieEchoDetectionSystem = jamieEchoDetectionSystem;
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder("Echo Detection"));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(panel, new JLabel("Max interval ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, maxDelay = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(panel, new JLabel(" (ms)", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx=c.gridx-2;
		PamDialog.addComponent(panel, new JLabel("Max Amplitude Difference ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, maxAmp = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(panel, new JLabel(" % ", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx=c.gridx-2;
		PamDialog.addComponent(panel, new JLabel("Max ICI Difference ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, maxICI = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(panel, new JLabel(" % ", SwingConstants.LEFT), c);
	;
	;
		
	}
	

	
	
	
	//these appear to be generated automoatically 
	@Override
	//returns the jPanel we've just made. 
	public JComponent getDialogComponent() {
		// TODO Auto-generated method stub
		return panel;
	}
	
	// gets the paramters set by the user in the echo dialogue box
	public boolean getParams() {
		JamieEchoParams params = jamieEchoDetectionSystem.jamieEchoParams;
		try {
			double newTime = Double.valueOf(maxDelay.getText())/1000;
			double newMaxAmp = Double.valueOf(maxAmp.getText())/100.0;
			double newMaxICI = Double.valueOf(maxICI.getText())/100.0;
			params.maxIntervalSeconds = newTime;
			params.maxAmpDifference= newMaxAmp;
			params.maxICIDifference= newMaxICI;
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Echo Parameters", "Invalid delay value");
		}
		return true;
	}





	@Override
	
	public void setParams() {
		JamieEchoParams params = jamieEchoDetectionSystem.jamieEchoParams;
		maxAmp.setText(String.format("%3.5f", params.maxAmpDifference*100.0));
		
		maxDelay.setText(String.format("%3.5f", params.maxIntervalSeconds*1000.0));
	
		maxICI.setText(String.format("%3.5f", params.maxICIDifference*100.0));
	}


	

	
	
	
	
	
	
	
	
	
	
	

}
