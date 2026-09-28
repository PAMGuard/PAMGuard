package PamController.pamWizard.configurations;

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
 *
 * @author Jamie Macaulay
 */
public enum ConfigSpeciesGroup {

	/** Bats (echolocating, in air). */
	BAT("Bats", "bat.png", "mdi2b-bat"),

	/** Baleen whales, e.g. right, humpback, blue, fin and minke whales. */
	BALEEN_WHALE("Baleen whales", "baleen_whale.png", "mdi2w-whale"),

	/** Sperm whales. */
	SPERM_WHALE("Sperm whales", "sperm_whale.png", "mdi2w-whale"),

	/** Beaked whales. */
	BEAKED_WHALE("Beaked whales", "beaked_whale.png", "mdi2w-whale"),

	/** Broadband clicking and whistling delphinids. */
	DOLPHIN("Dolphins", "dolphin.png", "mdi2d-dolphin"),

	/**
	 * Narrow band high frequency clicking species - the porpoises and
	 * <i>Kogia</i> (dwarf and pygmy sperm whales).
	 */
	NBHF("Porpoises and other NBHF species", "nbhf.png", "mdi2d-dolphin"),

	/** Anything which does not fit one of the groups above. */
	OTHER("Other", "other.png", "mdi2f-fish");

	/**
	 * Folder within the packaged resources holding the species group icons.
	 */
	public static final String ICON_FOLDER = "/Resources/species/";

	private final String groupName;

	private final String iconName;

	private final String glyphName;

	ConfigSpeciesGroup(String groupName, String iconName, String glyphName) {
		this.groupName = groupName;
		this.iconName = iconName;
		this.glyphName = glyphName;
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
