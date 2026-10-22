package PamController.pamWizard.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.SoundFileSummary;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * Builds the text of the tooltips shown on the species group icons in the import
 * wizard.
 * <p>
 * The wizard always shows every species group, with the ones the selected
 * configuration targets picked out and the rest greyed. An icon alone cannot say
 * which is which for a reader who is not sure what the silhouette is, so each one
 * carries a tooltip naming the group, saying whether the configuration targets it
 * and, where the configuration names particular species rather than a whole
 * group, listing them.
 * <p>
 * What "targeted" means depends on the configuration. One read from file names
 * the species it detects. A plain spectrogram detects nothing at all - it shows
 * whatever was recorded - so its groups are the ones which live in the chosen
 * medium and whose sounds fit below the Nyquist frequency of the imported files,
 * and the tooltips talk about what the recordings can show rather than about what
 * will be detected.
 * <p>
 * The text is built here rather than in the wizards so that the Swing and
 * JavaFX versions say the same thing. Only the joining differs: Swing tooltips
 * need HTML for a line break, JavaFX ones take a plain newline.
 *
 * @author Jamie Macaulay
 */
public class SpeciesTooltips {

	private SpeciesTooltips() {
	}

	/**
	 * The species groups a configuration targets.
	 *
	 * @param config     the configuration, may be null.
	 * @param sampleRate the sample rate of the imported recordings in Hz, or zero
	 *                   if it is not known. Only used by configurations which are
	 *                   not tied to any species of their own.
	 * @param medium     the medium the recordings were made in, or null if it is
	 *                   not known. Likewise only used by those configurations.
	 * @return the targeted groups, never null; empty for no selection or for a
	 *         configuration which has nothing to say about species.
	 */
	public static Set<ConfigSpeciesGroup> getTargetedGroups(PamAutoConfig config, double sampleRate,
			SoundMedium medium) {
		if (config == null) {
			return java.util.Collections.emptySet();
		}
		return config.getSpeciesGroups(sampleRate, medium);
	}

	/**
	 * The lines of the tooltip for one species group.
	 *
	 * @param group      the species group.
	 * @param config     the selected configuration, may be null.
	 * @param sampleRate the sample rate of the imported recordings in Hz, or zero
	 *                   if it is not known.
	 * @param medium     the medium the recordings were made in, or null if it is
	 *                   not known.
	 * @return the lines to show, never null or empty.
	 */
	public static List<String> getLines(ConfigSpeciesGroup group, PamAutoConfig config, double sampleRate,
			SoundMedium medium) {
		List<String> lines = new ArrayList<>();
		lines.add(group.getGroupName());
		if (config == null) {
			lines.add("No configuration selected");
			return lines;
		}
		Set<ConfigSpeciesGroup> targeted = getTargetedGroups(config, sampleRate, medium);
		if (config instanceof FileConfigAutoConfig) {
			addDetectionLines(lines, group, (FileConfigAutoConfig) config, targeted);
		}
		else if (targeted.isEmpty()) {
			lines.add("This configuration is not for any particular species");
		}
		else {
			addRecordingLines(lines, group, targeted, sampleRate, medium);
		}
		return lines;
	}

	/**
	 * What a configuration written for particular species has to say about a group.
	 */
	private static void addDetectionLines(List<String> lines, ConfigSpeciesGroup group,
			FileConfigAutoConfig config, Set<ConfigSpeciesGroup> targeted) {
		if (!targeted.contains(group)) {
			lines.add("Not detected by this configuration");
			return;
		}
		lines.add("Detected by this configuration");
		List<String> species = getSpecies(group, config);
		if (!species.isEmpty()) {
			lines.add("Species: " + String.join(", ", species));
		}
	}

	/**
	 * What a configuration with no species of its own - a spectrogram - has to say
	 * about a group: not what will be detected, but whether these recordings could
	 * hold the group at all. The medium is the blunter of the two tests, so it is
	 * the one worth giving as the reason where both apply - there is no point
	 * telling someone their air recording is too slow for porpoises.
	 */
	private static void addRecordingLines(List<String> lines, ConfigSpeciesGroup group,
			Set<ConfigSpeciesGroup> targeted, double sampleRate, SoundMedium medium) {
		String rate = SoundFileSummary.formatRate((float) sampleRate);
		if (targeted.contains(group)) {
			lines.add("Within the frequency range of these " + rate + " recordings");
			return;
		}
		if (!group.isFoundIn(medium)) {
			lines.add("Not found in " + mediumName(medium) + " recordings");
			return;
		}
		lines.add("Too high in frequency for these " + rate + " recordings");
		lines.add("Needs " + SoundFileSummary.formatRate((float) group.getMinSampleRate()) + " or above");
	}

	/**
	 * The medium as it reads in a sentence.
	 */
	private static String mediumName(SoundMedium medium) {
		return (medium == null) ? "these" : medium.toString().toLowerCase();
	}

	/**
	 * The tooltip for one species group, as HTML for a Swing tooltip.
	 *
	 * @param group      the species group.
	 * @param config     the selected configuration, may be null.
	 * @param sampleRate the sample rate of the imported recordings in Hz.
	 * @param medium     the medium the recordings were made in, may be null.
	 * @return the tooltip text.
	 */
	public static String getSwingTip(ConfigSpeciesGroup group, PamAutoConfig config, double sampleRate,
			SoundMedium medium) {
		List<String> lines = getLines(group, config, sampleRate, medium);
		StringBuilder text = new StringBuilder("<html>");
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0) {
				text.append("<br>");
			}
			text.append(escape(lines.get(i)));
		}
		return text.append("</html>").toString();
	}

	/**
	 * The tooltip for one species group, as plain text for a JavaFX tooltip.
	 *
	 * @param group      the species group.
	 * @param config     the selected configuration, may be null.
	 * @param sampleRate the sample rate of the imported recordings in Hz.
	 * @param medium     the medium the recordings were made in, may be null.
	 * @return the tooltip text.
	 */
	public static String getFXTip(ConfigSpeciesGroup group, PamAutoConfig config, double sampleRate,
			SoundMedium medium) {
		return String.join("\n", getLines(group, config, sampleRate, medium));
	}

	/**
	 * The species a configuration names within one group. A group targeted as a
	 * whole names no species, in which case the group name has already said all
	 * there is to say.
	 */
	private static List<String> getSpecies(ConfigSpeciesGroup group, FileConfigAutoConfig config) {
		List<String> species = new ArrayList<>();
		for (ConfigTaxonTarget target : config.getDescription().getTaxa()) {
			if (target.getGroup() == group) {
				species.addAll(target.getSpecies());
			}
		}
		return species;
	}

	/**
	 * Make a name safe to drop into an HTML tooltip. Species names come from
	 * configuration files, so they are not guaranteed to be free of markup
	 * characters.
	 */
	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
