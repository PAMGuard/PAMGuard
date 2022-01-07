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

public class SimpleEchoDialogPanel implements EchoDialogPanel {
	
	private SimpleEchoDetectionSystem simpleEchoDetectionSystem;
	
	private JPanel panel;
	
	private JTextField maxDelay;

	/**
	 * @param simpleEchoDetectionSystem
	 */
	public SimpleEchoDialogPanel(
			SimpleEchoDetectionSystem simpleEchoDetectionSystem) {
		super();
		this.simpleEchoDetectionSystem = simpleEchoDetectionSystem;
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder("Echo Detection"));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(panel, new JLabel("Max interval ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(panel, maxDelay = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(panel, new JLabel(" (seconds)", SwingConstants.LEFT), c);
	}

	@Override
	public JComponent getDialogComponent() {
		return panel;
	}

	@Override
	public boolean getParams() {
		SimpleEchoParams params = simpleEchoDetectionSystem.simpleEchoParams;
		try {
			double newTime = Double.valueOf(maxDelay.getText());
			
			params.maxIntervalSeconds = newTime;
			
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Echo Parameters", "Invalid delay value");
		}
		return true;
	}

	@Override
	public void setParams() {
		SimpleEchoParams params = simpleEchoDetectionSystem.simpleEchoParams;
		maxDelay.setText(String.format("%3.5f", params.maxIntervalSeconds));
	}

}
