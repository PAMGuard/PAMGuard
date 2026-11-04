package PamController.pamWizard.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import PamController.pamWizard.SoundFileSummary;
import PamController.pamWizard.SpectrogramOptions;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamView.dialog.PamDialog;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;

/**
 * Spectrogram page of the import wizard: whether to decimate the data before the
 * FFT, and the FFT length and hop. With decimation on, a Decimator module is put
 * between Sound Acquisition and the FFT, so both the FFT and the spectrogram run
 * on the decimated data.
 * <p>
 * Only shown for configurations which build a spectrogram.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramOptionsCard extends PamWizardCard<ConfigWizardData> {

	private static final long serialVersionUID = 1L;

	private final JCheckBox decimateCheck = new JCheckBox("Decimate the data before the FFT");
	private final JComboBox<String> rateChooser = new JComboBox<>();
	private final JLabel rateLabel = new JLabel("Decimated sample rate (Hz):");
	private final JLabel sourceLabel = new JLabel();
	private final JComboBox<Integer> fftLengthChooser = new JComboBox<>();
	private final JTextField fftHopField = new JTextField();
	private final JLabel resolutionLabel = new JLabel(" ");

	private float sourceSampleRate;

	/** Width of the rate, length and hop controls, in pixels. */
	private static final int CONTROL_WIDTH = 110;

	public SpectrogramOptionsCard(PamWizard pamWizard) {
		super(pamWizard, "Spectrogram");
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel("How should the spectrogram be calculated?");
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		rateChooser.setEditable(true);
		for (Integer len : SpectrogramOptions.getFFTLengths()) {
			fftLengthChooser.addItem(len);
		}

		JPanel fields = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		fields.add(sourceLabel, c);
		// a compact, common width rather than stretching across the page.
		setControlWidth(rateChooser);
		setControlWidth(fftLengthChooser);
		setControlWidth(fftHopField);
		c.gridy++;
		fields.add(decimateCheck, c);
		c.gridwidth = 1;
		addRow(fields, c, ++c.gridy, rateLabel, rateChooser);
		addRow(fields, c, ++c.gridy, new JLabel("FFT length (samples):"), fftLengthChooser);
		addRow(fields, c, ++c.gridy, new JLabel("FFT hop (samples):"), fftHopField);

		decimateCheck.setToolTipText("<html>Add a Decimator between Sound Acquisition and the FFT. Useful for "
				+ "viewing low frequency sounds<br>in high sample rate recordings - the spectrogram shows only "
				+ "the frequencies of interest<br>and is much quicker to calculate.</html>");
		fftLengthChooser.setToolTipText("Longer FFTs give finer frequency resolution but coarser time resolution");
		fftHopField.setToolTipText("Number of samples between successive FFTs. Half the FFT length is usual.");

		decimateCheck.addActionListener(e -> {
			enableControls();
			updateResolution();
		});
		rateChooser.addActionListener(e -> updateResolution());
		fftLengthChooser.addActionListener(e -> fftLengthChanged());
		fftHopField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateResolution();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateResolution();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateResolution();
			}
		});

		resolutionLabel.setFont(resolutionLabel.getFont().deriveFont(Font.ITALIC));
		resolutionLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

		// hold the content against the top of the page rather than centring it.
		JPanel left = new JPanel(new BorderLayout());
		left.add(BorderLayout.WEST, fields);
		JPanel top = new JPanel(new BorderLayout());
		top.add(BorderLayout.NORTH, title);
		top.add(BorderLayout.CENTER, left);
		top.add(BorderLayout.SOUTH, resolutionLabel);

		add(BorderLayout.NORTH, top);
	}

	private void addRow(JPanel panel, GridBagConstraints c, int row, JLabel label, java.awt.Component field) {
		c.gridy = row;
		c.gridx = 0;
		c.weightx = 0;
		panel.add(label, c);
		c.gridx = 1;
		panel.add(field, c);
	}

	private static void setControlWidth(JComponent component) {
		component.setPreferredSize(new Dimension(CONTROL_WIDTH, component.getPreferredSize().height));
	}

	@Override
	public void setParams(ConfigWizardData cardParams) {
		SpectrogramOptions options = cardParams.getApplyContext().getSpectrogramOptions();
		sourceSampleRate = cardParams.getSourceSampleRate();

		sourceLabel.setText(sourceSampleRate > 0
				? "The imported files are sampled at " + SoundFileSummary.formatRate(sourceSampleRate) + "."
				: "The sample rate of the imported files is not known.");

		rateChooser.removeAllItems();
		for (Float rate : SpectrogramOptions.getPresetRates(sourceSampleRate)) {
			rateChooser.addItem(formatRate(rate));
		}
		float rate = options.getDecimatedSampleRate();
		if (rate <= 0) {
			rate = SpectrogramOptions.suggestDecimatedRate(sourceSampleRate);
		}
		rateChooser.setSelectedItem(formatRate(rate));

		decimateCheck.setSelected(options.isDecimate());
		fftLengthChooser.setSelectedItem(options.getFftLength());
		fftHopField.setText(Integer.toString(options.getFftHop()));

		enableControls();
		updateResolution();
	}

	@Override
	public boolean getParams(ConfigWizardData cardParams) {
		SpectrogramOptions options = readOptions();
		if (options == null) {
			return PamDialog.showWarning(getPamWizard(), "Spectrogram",
					"Please enter numbers for the decimated sample rate and FFT hop.");
		}
		String error = options.validate(sourceSampleRate);
		if (error != null) {
			return PamDialog.showWarning(getPamWizard(), "Spectrogram", error);
		}
		cardParams.getApplyContext().setSpectrogramOptions(options);
		return true;
	}

	/**
	 * Read the controls into a new set of options.
	 *
	 * @return the options, or null if a number could not be read.
	 */
	private SpectrogramOptions readOptions() {
		SpectrogramOptions options = new SpectrogramOptions();
		options.setDecimate(decimateCheck.isSelected());
		Integer len = (Integer) fftLengthChooser.getSelectedItem();
		options.setFftLength(len == null ? 0 : len);
		try {
			options.setFftHop(Integer.parseInt(fftHopField.getText().trim()));
			Object rate = rateChooser.getSelectedItem();
			// the rate only matters, and so only has to be readable, when decimating.
			if (rate != null && options.isDecimate()) {
				options.setDecimatedSampleRate(Float.parseFloat(rate.toString().trim()));
			}
		}
		catch (NumberFormatException e) {
			return null;
		}
		return options;
	}

	/**
	 * Keep the hop at half the FFT length when the length changes, which is what
	 * almost everyone wants. A user who wants something else can still type it.
	 */
	private void fftLengthChanged() {
		Integer len = (Integer) fftLengthChooser.getSelectedItem();
		if (len != null) {
			fftHopField.setText(Integer.toString(len / 2));
		}
		updateResolution();
	}

	private static String formatRate(float rate) {
		return (rate == Math.rint(rate)) ? String.format("%.0f", rate) : Float.toString(rate);
	}

	private void enableControls() {
		rateLabel.setEnabled(decimateCheck.isSelected());
		rateChooser.setEnabled(decimateCheck.isSelected());
	}

	private void updateResolution() {
		SpectrogramOptions options = readOptions();
		String text = (options == null) ? "" : options.describeResolution(sourceSampleRate);
		resolutionLabel.setText(text.isEmpty() ? " " : text);
	}
}
