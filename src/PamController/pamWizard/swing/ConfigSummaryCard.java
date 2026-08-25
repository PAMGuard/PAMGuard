package PamController.pamWizard.swing;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import PamController.UsedModuleInfo;
import PamController.pamWizard.PODViewerAutoConfig;
import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.PamFileImport;
import PamController.pamWizard.PamImportFileType;
import PamController.pamWizard.SoundFileSummary;
import PamController.pamWizard.configurations.ConfigApplyContext;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamController.pamWizard.configurations.PamConfigInspection;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;

/**
 * Last page of the import wizard: exactly what will happen when Finish is
 * clicked. Nothing has been changed at this point, so this is the user's chance
 * to go back - which matters, because building a configuration replaces whatever
 * is currently loaded.
 *
 * @author Jamie Macaulay
 */
public class ConfigSummaryCard extends PamWizardCard<ConfigWizardData> {

	private static final long serialVersionUID = 1L;

	private final JTextArea summaryArea = new JTextArea(14, 50);

	public ConfigSummaryCard(PamWizard pamWizard) {
		super(pamWizard, "Summary");
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel("PAMGuard will now build this configuration");
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		summaryArea.setEditable(false);
		summaryArea.setOpaque(false);
		summaryArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		add(BorderLayout.NORTH, title);
		add(BorderLayout.CENTER, new JScrollPane(summaryArea));
	}

	@Override
	public void setParams(ConfigWizardData cardParams) {
		summaryArea.setText(buildSummary(cardParams));
		summaryArea.setCaretPosition(0);
	}

	@Override
	public boolean getParams(ConfigWizardData cardParams) {
		return true;
	}

	/**
	 * Describe in plain words what applying the chosen configuration will do.
	 *
	 * @param data the wizard state.
	 * @return the summary text.
	 */
	public static String buildSummary(ConfigWizardData data) {
		StringBuilder text = new StringBuilder();

		PamAutoConfig selected = data.getSelectedConfig();
		if (selected == null) {
			return "No configuration selected.";
		}
		text.append(selected.getConfigName()).append("\n\n");

		SoundFileSummary summary = data.getSoundSummary();
		if (summary != null && summary.getFileCount() > 0) {
			text.append("Sound Acquisition will read ").append(summary.getFileCount())
					.append(summary.getFileCount() == 1 ? " file" : " files");
			if (summary.isValid()) {
				text.append(" at ").append(summary.getFormatDescription());
			}
			text.append(".\n");
		}

		int podFiles = countPodFiles(selected, data.getFileImport());
		if (podFiles > 0) {
			text.append(podFiles).append(podFiles == 1 ? " POD file" : " POD files")
					.append(" will be converted into PAMGuard binary files.\n");
		}

		PamConfigInspection inspection = data.getSelectedInspection();

		if (data.willDecimate() && inspection != null && summary != null) {
			text.append("A decimator will reduce the sample rate from ")
					.append(SoundFileSummary.formatRate(summary.getMinSampleRate()))
					.append(" to ")
					.append(SoundFileSummary.formatRate(inspection.getTargetSampleRate().floatValue()))
					.append(", which is what this configuration's detectors expect.\n");
		}

		ConfigApplyContext context = data.getApplyContext();
		if (selected.needsBinaryStore() && context.getBinaryFolder() != null) {
			text.append("Binary data will be written to ")
					.append(context.getBinaryFolder().getAbsolutePath()).append("\n");
		}
		if (selected.needsDatabase() && context.getDatabaseFile() != null) {
			text.append("The database will be ")
					.append(context.getDatabaseFile().getAbsolutePath()).append("\n");
		}

		if (inspection != null && inspection.isValid()) {
			text.append("\nModules created:\n");
			for (UsedModuleInfo module : inspection.getModules()) {
				if (module == null) {
					continue;
				}
				text.append("    ").append(module.unitName);
				if (!module.unitName.equals(module.getUnitType())) {
					text.append("  (").append(module.getUnitType()).append(")");
				}
				text.append("\n");
			}
		}

		return text.toString();
	}

	/**
	 * The number of CPOD/FPOD detection files the chosen configuration will import.
	 * These are converted into binary files rather than being read while processing,
	 * so they are worth mentioning separately from the sound files - but only when
	 * the configuration actually does something with them. POD files may well have
	 * been dropped alongside sound files that the user has chosen a sound
	 * configuration for.
	 *
	 * @param selected   the chosen configuration, may be null.
	 * @param fileImport the imported files, may be null.
	 * @return the number of POD files which will be imported.
	 */
	private static int countPodFiles(PamAutoConfig selected, PamFileImport fileImport) {
		if (fileImport == null || !(selected instanceof PODViewerAutoConfig)) {
			return 0;
		}
		return fileImport.getFileCount(PamImportFileType.CPOD) + fileImport.getFileCount(PamImportFileType.FPOD);
	}

}
