package PamController.pamWizard.configurations;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * The broad taxonomic groups that PAMGuard configurations target. Configurations
 * declare one or more of these in their JSON descriptor (see
 * {@link PamConfigDescription}) so that the import wizard can group and filter
 * them, and so that each configuration can be given a recognisable icon.
 * <p>
 * The groups are deliberately coarse and acoustically motivated rather than
 * strictly phylogenetic - {@link #NBHF} covers the narrow band high frequency
 * clicking species, which includes the porpoises and <i>Kogia</i> but not the
 * broadband clicking dolphins. A configuration that targets a single species
 * (e.g. North Atlantic right whale) still declares the group it belongs to, and
 * by default inherits that group's icon.
 * <p>
 * Each group also carries the medium it lives in (see {@link #getMedium()}) and
 * the lowest sample rate at which its sounds can usefully be seen (see
 * {@link #getMinSampleRate()}). Together those are what a configuration with no
 * species of its own - a plain spectrogram - uses to say which groups a set of
 * recordings could possibly show: nothing above the Nyquist frequency, and no
 * bats in the sea.
 * <p>
 * {@link #OTHER} is the exception to all of this. It exists so that a
 * configuration file naming a group PAMGuard does not know still loads, and is
 * never drawn - see {@link #getDisplayGroups()}.
 *
 * @author Jamie Macaulay
 */
public enum ConfigSpeciesGroup {

	/**
	 * Bats (echolocating, in air). Most calls sit between 20 and 60 kHz, so 96 kHz
	 * shows the majority of species; the high frequency ones (horseshoe bats, at 80
	 * kHz and above) need the 192 - 384 kHz a bat recorder normally runs at.
	 */
	BAT("Bats", "bat.png", "mdi2b-bat", 96000, SoundMedium.Air),

	/**
	 * Baleen whales, e.g. right, humpback, blue, fin and minke whales. Calls run
	 * from a few Hz to a few kHz, so anything but the very slowest sample rate
	 * shows them.
	 */
	BALEEN_WHALE("Baleen whales", "baleen_whale.png", "mdi2w-whale", 1000, SoundMedium.Water),

	/**
	 * Sperm whales. The clicks are broadband with most of their energy below 15
	 * kHz, and are recognisable on quite modest sample rates.
	 */
	SPERM_WHALE("Sperm whales", "sperm_whale.png", "mdi2w-whale", 24000, SoundMedium.Water),

	/**
	 * Beaked whales. The FM pulses sweep up to around 40 - 50 kHz, so the whole
	 * sweep needs a sample rate of about 96 kHz or above.
	 */
	BEAKED_WHALE("Beaked whales", "beaked_whale.png", "mdi2w-whale", 96000, SoundMedium.Water),

	/**
	 * Broadband clicking and whistling delphinids. Most whistle energy is below 18
	 * kHz, so 36 kHz is enough to see the whistles; the clicks go a great deal
	 * higher and need a faster recording.
	 */
	DOLPHIN("Dolphins", "dolphin.png", "mdi2d-dolphin", 36000, SoundMedium.Water),

	/**
	 * Narrow band high frequency clicking species - the porpoises and
	 * <i>Kogia</i> (dwarf and pygmy sperm whales). All the energy is in a narrow
	 * band around 130 kHz, so nothing at all is seen below about 250 kHz.
	 */
	NBHF("Porpoises and other NBHF species", "nbhf.png", "mdi2d-dolphin", 250000, SoundMedium.Water),

	/**
	 * Anything which does not fit one of the groups above - fish, anthropogenic
	 * noise, an unknown sound. Neither the sample rate nor the medium rules it out.
	 * <p>
	 * This is a bucket for reading configuration files, not something to draw: it
	 * is what an unrecognised or missing group name resolves to, so that a
	 * descriptor naming something PAMGuard has never heard of still loads. It is
	 * left out of {@link #getDisplayGroups()} - a single fish standing for
	 * "anything else" reads as a claim about fish.
	 */
	OTHER("Other", "other.png", "mdi2f-fish", 0, null);

	/**
	 * Folder within the packaged resources holding the species group icons.
	 */
	public static final String ICON_FOLDER = "/Resources/species/";

	private final String groupName;

	private final String iconName;

	private final String glyphName;

	private final double minSampleRate;

	private final SoundMedium medium;

	ConfigSpeciesGroup(String groupName, String iconName, String glyphName, double minSampleRate,
			SoundMedium medium) {
		this.groupName = groupName;
		this.iconName = iconName;
		this.glyphName = glyphName;
		this.minSampleRate = minSampleRate;
		this.medium = medium;
	}

	/**
	 * A human readable name for the group, suitable for a filter list or a label.
	 * @return the group name.
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * The classpath resource path of this group's icon.
	 * @return the icon resource path, e.g. {@code /Resources/species/dolphin.png}.
	 */
	public String getIconResource() {
		return ICON_FOLDER + iconName;
	}

	/**
	 * An Ikonli glyph code used as a fallback when the icon resource is missing.
	 * @return the glyph code, e.g. {@code mdi2d-dolphin}.
	 */
	public String getGlyphName() {
		return glyphName;
	}

	@Override
	public String toString() {
		return groupName;
	}

	/**
	 * The lowest sample rate at which this group's sounds can usefully be seen on a
	 * spectrogram, in Hz. This is a rule of thumb about what the recording can
	 * contain rather than a promise that the animal is there - the group's calls
	 * have to fall below the Nyquist frequency before there is any point looking
	 * for them at all.
	 *
	 * @return the minimum useful sample rate in Hz; zero for a group no sample rate
	 *         rules out.
	 */
	public double getMinSampleRate() {
		return minSampleRate;
	}

	/**
	 * The groups worth drawing, in order: every group except {@link #OTHER}, which
	 * is a catch-all for reading files rather than an animal anyone would recognise
	 * from a silhouette.
	 *
	 * @return the groups to show, never null.
	 */
	public static List<ConfigSpeciesGroup> getDisplayGroups() {
		List<ConfigSpeciesGroup> groups = new ArrayList<>();
		for (ConfigSpeciesGroup group : values()) {
			if (group != OTHER) {
				groups.add(group);
			}
		}
		return groups;
	}

	/**
	 * The medium this group lives in. Bats are recorded in air and everything else
	 * here in water, so the medium a recording was made in rules most of the groups
	 * out on its own.
	 *
	 * @return the medium, or null for a group which could be either.
	 */
	public SoundMedium getMedium() {
		return medium;
	}

	/**
	 * Whether this group's sounds could be seen in recordings made at the given
	 * sample rate.
	 *
	 * @param sampleRate the sample rate of the recordings, in Hz.
	 * @return true if the group is worth looking for at that sample rate.
	 */
	public boolean isVisibleAt(double sampleRate) {
		return sampleRate >= minSampleRate;
	}

	/**
	 * Whether this group could be found in the given medium.
	 *
	 * @param medium the medium the recordings were made in, or null if it is not
	 *               known - in which case no group is ruled out.
	 * @return true if the group lives in that medium.
	 */
	public boolean isFoundIn(SoundMedium medium) {
		return this.medium == null || medium == null || this.medium == medium;
	}

	/**
	 * Every group which could be seen in recordings made at the given sample rate
	 * in the given medium. Used by configurations which are not tied to any species
	 * - a plain spectrogram will show whatever is there, so the only thing which can
	 * be said about it is what the recordings themselves are capable of holding.
	 *
	 * @param sampleRate the sample rate of the recordings, in Hz.
	 * @param medium     the medium the recordings were made in, or null if it is
	 *                   not known.
	 * @return the groups worth looking for, never null; empty if the sample rate is
	 *         unknown (zero or less).
	 */
	public static Set<ConfigSpeciesGroup> getVisibleGroups(double sampleRate, SoundMedium medium) {
		Set<ConfigSpeciesGroup> groups = new LinkedHashSet<>();
		if (sampleRate <= 0) {
			return groups;
		}
		for (ConfigSpeciesGroup group : values()) {
			if (group.isVisibleAt(sampleRate) && group.isFoundIn(medium)) {
				groups.add(group);
			}
		}
		return groups;
	}

	/**
	 * Find a group from the name used in a JSON configuration descriptor. Matching
	 * is case insensitive and ignores spaces and hyphens, so {@code "baleen whale"},
	 * {@code "BALEEN_WHALE"} and {@code "Baleen-Whale"} all resolve to
	 * {@link #BALEEN_WHALE}.
	 *
	 * @param name the name from the JSON file.
	 * @return the matching group, or {@link #OTHER} if the name is null or unrecognised.
	 */
	public static ConfigSpeciesGroup fromString(String name) {
		if (name == null) {
			return OTHER;
		}
		String tidy = name.trim().replaceAll("[\\s\\-]+", "_");
		for (ConfigSpeciesGroup group : values()) {
			if (group.name().equalsIgnoreCase(tidy)) {
				return group;
			}
		}
		// also allow matching on the display name.
		for (ConfigSpeciesGroup group : values()) {
			if (group.groupName.equalsIgnoreCase(name.trim())) {
				return group;
			}
		}
		System.out.println("PamConfig: unrecognised species group \"" + name + "\", using " + OTHER);
		return OTHER;
	}
}
