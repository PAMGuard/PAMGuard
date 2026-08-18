package PamController.pamWizard.swing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamController.pamWizard.PamImportFileType;
import PamController.pamWizard.SoundFileSummary;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;

/**
 * First page of the import wizard: what was found in the files the user
 * imported. Shown before any configurations are offered so that it is obvious
 * <i>why</i> a particular set of configurations is on offer - a user who expected
 * to see a configuration and does not can look here and see that, say, their
 * files turned out to be 48 kHz rather than the 500 kHz they thought.
 *
 * @author Jamie Macaulay
 */
public class ScanSummaryCard extends PamWizardCard<ConfigWizardData> {

	private static final long serialVersionUID = 1L;

	private final JLabel fileCountLabel = new JLabel();
	private final JLabel sampleRateLabel = new JLabel();
	private final JLabel channelsLabel = new JLabel();
	private final JLabel typesLabel = new JLabel();
	private final JLabel warningLabel = new JLabel();

	public ScanSummaryCard(PamWizard pamWizard) {
		super(pamWizard, "Imported files");
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel fields = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 8);
		c.anchor = GridBagConstraints.WEST;

		int row = 0;
		addRow(fields, c, row++, "Sound files", fileCountLabel);
		addRow(fields, c, row++, "Sample rate", sampleRateLabel);
		addRow(fields, c, row++, "Channels", channelsLabel);
		addRow(fields, c, row++, "Also found", typesLabel);

		// take up the spare width so the fields sit against the left edge.
		c.gridy = 0;
		c.gridx = 2;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		fields.add(new JPanel(), c);

		warningLabel.setForeground(java.awt.Color.RED.darker());
		warningLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		JLabel title = new JLabel("PAMGuard has scanned the files you imported");
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		// hold the content against the top of the page rather than centring it.
		JPanel top = new JPanel(new BorderLayout());
		top.add(BorderLayout.NORTH, title);
		top.add(BorderLayout.CENTER, fields);
		top.add(BorderLayout.SOUTH, warningLabel);

		add(BorderLayout.NORTH, top);
	}

	private void addRow(JPanel panel, GridBagConstraints c, int row, String name, JLabel valueLabel) {
		c.gridy = row;
		c.gridx = 0;
		panel.add(new JLabel(name + ":"), c);
		c.gridx = 1;
		panel.add(valueLabel, c);
	}

	@Override
	public boolean getParams(ConfigWizardData cardParams) {
		// nothing to collect from this page.
		return true;
	}

	@Override
	public void setParams(ConfigWizardData cardParams) {
		SoundFileSummary summary = cardParams.getSoundSummary();

		if (summary == null || summary.getFileCount() == 0) {
			fileCountLabel.setText("none");
			sampleRateLabel.setText("-");
			channelsLabel.setText("-");
			typesLabel.setText("-");
			warningLabel.setText("No sound files were found in what you imported.");
			return;
		}

		fileCountLabel.setText(String.valueOf(summary.getFileCount()));

		if (!summary.isValid()) {
			sampleRateLabel.setText("unknown");
			channelsLabel.setText("unknown");
			warningLabel.setText("The format of these files could not be read, so configurations "
					+ "cannot be matched to them.");
		}
		else {
			sampleRateLabel.setText(summary.getMinSampleRate() == summary.getMaxSampleRate()
					? SoundFileSummary.formatRate(summary.getMinSampleRate())
					: SoundFileSummary.formatRate(summary.getMinSampleRate()) + " to "
							+ SoundFileSummary.formatRate(summary.getMaxSampleRate()));
			channelsLabel.setText(summary.getMinChannels() == summary.getMaxChannels()
					? String.valueOf(summary.getMinChannels())
					: summary.getMinChannels() + " to " + summary.getMaxChannels());

			/*
			 * Sound acquisition can only be set up with one format, so tell the user now
			 * rather than letting them find out when processing goes wrong.
			 */
			warningLabel.setText(summary.isMixedFormats()
					? "These files do not all have the same format. Configurations are matched to the "
							+ "lowest sample rate and channel count found."
					: "");
		}

		StringBuilder types = new StringBuilder();
		if (summary.hasSudFiles()) {
			types.append("SoundTrap sud files");
		}
		if (cardParams.getFileImport() != null
				&& cardParams.getFileImport().hasType(PamImportFileType.SUD_CLICKS)) {
			types.append(types.length() > 0 ? ", " : "").append("click detections");
		}
		typesLabel.setText(types.length() == 0 ? "-" : types.toString());
	}
}
