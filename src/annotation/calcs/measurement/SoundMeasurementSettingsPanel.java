package annotation.calcs.measurement;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import Spectrogram.WindowFunction;
import annotation.AnnotationSettingsPanel;
import annotation.handler.AnnotationOptions;

/**
 * Spectrogram settings for the sound measurements. These are shared by all the
 * measurements, so changing them for one changes them for all.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementSettingsPanel implements AnnotationSettingsPanel {

	private static final int[] FFT_LENGTHS = {64, 128, 256, 512, 1024, 2048, 4096, 8192};

	private JPanel mainPanel;

	private JComboBox<Integer> fftLength;

	private JTextField fftHop;

	private JComboBox<String> windowType;

	private SoundMeasurementOptions options;

	public SoundMeasurementSettingsPanel() {
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Measurement spectrogram"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		mainPanel.add(new JLabel("<html>These settings are used by all the sound measurements.</html>"), c);
		c.gridwidth = 1;
		c.gridy++;
		mainPanel.add(new JLabel("FFT length ", JLabel.RIGHT), c);
		c.gridx++;
		fftLength = new JComboBox<>();
		for (int len : FFT_LENGTHS) {
			fftLength.addItem(len);
		}
		mainPanel.add(fftLength, c);
		c.gridx++;
		mainPanel.add(new JLabel(" samples"), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("FFT hop ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(fftHop = new JTextField(6), c);
		c.gridx++;
		mainPanel.add(new JLabel(" samples"), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Window ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(windowType = new JComboBox<>(WindowFunction.getNames()), c);
		fftLength.setToolTipText("<html>FFT length for the spectrogram based measurements. This is in samples of the data<br>"
				+ "the annotation was made on, which may be decimated. Annotations shorter than one<br>"
				+ "FFT are measured with the longest FFT which fits.</html>");
		fftHop.setToolTipText("Number of samples between spectrogram frames");
	}

	@Override
	public JComponent getSwingPanel() {
		return mainPanel;
	}

	@Override
	public void setSettings(AnnotationOptions annotationOptions) {
		options = (SoundMeasurementOptions) annotationOptions;
		SoundMeasurementParameters params = options.getParameters();
		fftLength.setSelectedItem(params.fftLength);
		fftHop.setText(String.valueOf(params.fftHop));
		windowType.setSelectedIndex(params.windowType);
	}

	@Override
	public AnnotationOptions getSettings() {
		SoundMeasurementParameters params = options.getParameters().clone();
		params.fftLength = (Integer) fftLength.getSelectedItem();
		try {
			params.fftHop = Integer.valueOf(fftHop.getText().trim());
		}
		catch (NumberFormatException e) {
			PamDialog.showWarning(null, "Sound measurements", "Invalid FFT hop");
			return null;
		}
		if (params.fftHop <= 0 || params.fftHop > params.fftLength) {
			PamDialog.showWarning(null, "Sound measurements", "The FFT hop must be between 1 and the FFT length");
			return null;
		}
		params.windowType = windowType.getSelectedIndex();
		options.setParameters(params);
		return options;
	}

}
