package PamController.pamWizard.configurations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One taxonomic target of a configuration, read from the {@code taxa} array of a
 * configuration's JSON descriptor. A target always names a broad
 * {@link ConfigSpeciesGroup} and may additionally name specific species within
 * that group, for example:
 *
 * <pre>
 * { "group": "BALEEN_WHALE",
 *   "species": ["North Atlantic right whale"],
 *   "itisCodes": [180537] }
 * </pre>
 *
 * A configuration with no species listed targets the whole group.
 *
 * @author Jamie Macaulay
 */
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 * Bind from the annotated fields only. Several accessors here derive a value
 * rather than returning a field - getGroup() turns the group name into an enum -
 * and letting Jackson auto detect them clashes with the fields they are derived
 * from.
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE,
		setterVisibility = Visibility.NONE, fieldVisibility = Visibility.NONE,
		creatorVisibility = Visibility.NONE)
public class ConfigTaxonTarget {

	@JsonProperty("group")
	private String groupName;

	@JsonProperty("species")
	private List<String> species;

	@JsonProperty("itisCodes")
	private List<Integer> itisCodes;

	/**
	 * Empty constructor required by Jackson.
	 */
	public ConfigTaxonTarget() {
	}

	/**
	 * Convenience constructor, primarily for tests.
	 *
	 * @param group   the species group.
	 * @param species specific species names, may be null or empty.
	 */
	public ConfigTaxonTarget(ConfigSpeciesGroup group, String... species) {
		this.groupName = group.name();
		this.species = (species == null) ? null : new ArrayList<>(Arrays.asList(species));
	}

	/**
	 * The broad group this target belongs to. Never null - an unrecognised or
	 * missing group name resolves to {@link ConfigSpeciesGroup#OTHER}.
	 *
	 * @return the species group.
	 */
	public ConfigSpeciesGroup getGroup() {
		return ConfigSpeciesGroup.fromString(groupName);
	}

	/**
	 * The raw group name as written in the JSON file.
	 * @return the group name, possibly null.
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Specific species targeted within the group. An empty list means the whole
	 * group is targeted.
	 *
	 * @return the species names, never null.
	 */
	public List<String> getSpecies() {
		return (species == null) ? new ArrayList<>() : species;
	}

	/**
	 * ITIS taxonomic serial numbers for the targeted species, in the same order as
	 * {@link #getSpecies()} where both are given. Used to tie a configuration to the
	 * species codes PAMGuard already uses elsewhere (see {@code tethys.species}).
	 *
	 * @return the ITIS codes, never null.
	 */
	public List<Integer> getItisCodes() {
		return (itisCodes == null) ? new ArrayList<>() : itisCodes;
	}

	/**
	 * Whether this target names specific species rather than a whole group.
	 * @return true if at least one species is named.
	 */
	public boolean isSpeciesSpecific() {
		return species != null && !species.isEmpty();
	}

	@Override
	public String toString() {
		if (!isSpeciesSpecific()) {
			return getGroup().getGroupName();
		}
		return String.join(", ", species);
	}
}
