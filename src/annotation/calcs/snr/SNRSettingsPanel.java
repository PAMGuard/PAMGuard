package annotation.calcs.snr;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import annotation.AnnotationSettingsPanel;
import annotation.handler.AnnotationOptions;

public class SNRSettingsPanel implements AnnotationSettingsPanel {
	
	private JPanel mainPanel;

	private JTextField measureSecs, bufferSecs;

	private SNRAnnotationOptions snrOptions;
	
	public SNRSettingsPanel(SNRAnnotationType snrAnnotationType) {
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("SNR Measurement"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Noise measurement period ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(measureSecs = new JTextField(4), c);
		c.gridx++;
		mainPanel.add(new JLabel(" s", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Buffer between sound and noise ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(bufferSecs = new JTextField(4), c);
		c.gridx++;
		mainPanel.add(new JLabel(" s", JLabel.LEFT), c);
		
		measureSecs.setToolTipText("Seconds over which noise will be measured both before and after the sound (if data are available)");
		bufferSecs.setToolTipText("Seconds gap betwen the start and end of the sound and the noise measurements");
		
	}

	@Override
	public JComponent getSwingPanel() {
		return mainPanel;
	}

	@Override
	public void setSettings(AnnotationOptions annotationOptions) {
//		if (annotationOptions instanceof SNRAnnotationOptions) {
			this.snrOptions = (SNRAnnotationOptions) annotationOptions;
//		}
			measureSecs.setText(String.format("%3.2f", snrOptions.getSnrAnnotationParameters().noiseMillis / 1000.));
			bufferSecs.setText(String.format("%3.2f", snrOptions.getSnrAnnotationParameters().bufferMillis / 1000.));
	}

	@Override
	public AnnotationOptions getSettings() {
		SNRAnnotationParameters params = snrOptions.getSnrAnnotationParameters().clone();
		try {
			params.noiseMillis = (int) (Double.valueOf(measureSecs.getText()) * 1000.);
			params.bufferMillis = (int) (Double.valueOf(bufferSecs.getText()) * 1000.);
		}
		catch (NumberFormatException e) {
			PamDialog.showWarning(null, "Bad Parameter", "Invalid parameter in SNR settings");
			return null;
		}
		snrOptions.setSnrAnnotationParameters(params);
		return snrOptions;
	}

}
