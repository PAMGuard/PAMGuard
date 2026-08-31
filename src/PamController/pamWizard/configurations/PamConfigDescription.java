package PamController.pamWizard.configurations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import PamController.PamController;
import PamController.pamWizard.PamImportFileType;
import PamController.soundMedium.GlobalMedium.SoundMedium;

/**
 * The metadata describing a single PAMGuard configuration which can be offered
 * by the import wizard. Each configuration on disk is a pair of files: a
 * {@code .psfx} holding the actual modules and their settings, and a
 * {@code .json} of the same base name holding this description.
 * <p>
 * The JSON is deliberately tolerant: unknown fields are ignored so that
 * configurations written for a newer version of PAMGuard still load, and every
 * field except {@code name} has a sensible default. See
 * {@link PamConfigRepository} for where the files are found and
 * {@link PamConfigLoader} for how a configuration is applied.
 *
 * <pre>
 * {
 *   "configType": "default",
 *   "name": "North Atlantic right whale (deep learning)",
 *   "description": "Deep learning detector for right whale upcalls.",
 *   "minSampleRate": 2000,
 *   "targetSampleRate": 2000,
 *   "minChannels": 1,
 *   "medium": "water",
 *   "requiredFileTypes": ["SOUND"],
 *   "taxa": [ { "group": "BALEEN_WHALE", "species": ["North Atlantic right whale"] } ]
 * }
 * </pre>
 *
 * @author Jamie Macaulay
 */
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 * Bind from the annotated fields only. Most accessors here apply a default or
 * convert the raw value - getMedium() returns an enum, getName() falls back to
 * the file name - so auto detecting them would clash with the fields they are
 * derived from.
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE,
		setterVisibility = Visibility.NONE, fieldVisibility = Visibility.NONE,
		creatorVisibility = Visibility.NONE)
public class PamConfigDescription {

	/**
	 * The configuration type used when the JSON file does not specify one. A
	 * configuration of this type is loaded by {@link DefaultConfigLoader}.
	 */
	public static final String DEFAULT_CONFIG_TYPE = "default";

	/**
	 * Module class names which must keep reading full rate data from the sound
	 * acquisition module even when a decimator is inserted. Used when a
	 * configuration does not list its own. The SoundTrap click detector is the
	 * obvious case - it extracts clicks from the raw sud stream, so decimating its
	 * input would destroy the very clicks it exists to find.
	 */
	public static final List<String> DEFAULT_KEEP_RAW_MODULES =
			Arrays.asList("soundtrap.STClickControl");

	@JsonProperty("configType")
	private String configType;

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("version")
	private String version;

	@JsonProperty("author")
	private String author;

	@JsonProperty("psfx")
	private String psfxName;

	@JsonProperty("minSampleRate")
	private Double minSampleRate;

	@JsonProperty("maxSampleRate")
	private Double maxSampleRate;

	@JsonProperty("targetSampleRate")
	private Double targetSampleRate;

	@JsonProperty("minChannels")
	private Integer minChannels;

	@JsonProperty("maxChannels")
	private Integer maxChannels;

	@JsonProperty("medium")
	private String medium;

	@JsonProperty("requiredFileTypes")
	private List<String> requiredFileTypes;

	@JsonProperty("runModes")
	private List<String> runModes;

	@JsonProperty("taxa")
	private List<ConfigTaxonTarget> taxa;

	@JsonProperty("decimatorUnitName")
	private String decimatorUnitName;

	@JsonProperty("keepRawSourceModules")
	private List<String> keepRawSourceModules;

	/*
	 * Fields below are filled in by the repository once the file pair has been
	 * located; they are not part of the JSON.
	 */

	@JsonIgnore
	private File jsonFile;

	@JsonIgnore
	private File psfxFile;

	/**
	 * Empty constructor required by Jackson.
	 */
	public PamConfigDescription() {
	}

	/**
	 * The configuration type, which selects the loader used to apply this
	 * configuration. Null or {@value #DEFAULT_CONFIG_TYPE} means the standard
	 * loading behaviour; any other value is looked up in
	 * {@link PamConfigLoaderFactory}, allowing an organisation to add its own
	 * loader which might, for example, download extra files from a server before
	 * the configuration is applied.
	 *
	 * @return the configuration type, never null.
	 */
	public String getConfigType() {
		return (configType == null || configType.isBlank()) ? DEFAULT_CONFIG_TYPE : configType.trim();
	}

	/**
	 * The name of the configuration, as shown to the user. Falls back to the JSON
	 * file's base name if the JSON did not supply one.
	 *
	 * @return the configuration name.
	 */
	public String getName() {
		if (name != null && !name.isBlank()) {
			return name;
		}
		return (jsonFile == null) ? "Unnamed configuration" : stripExtension(jsonFile.getName());
	}

	/**
	 * A description of what the configuration does, shown alongside the name.
	 * @return the description, never null.
	 */
	public String getDescription() {
		return (description == null) ? "" : description;
	}

	/**
	 * @return the configuration version string, or null.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the configuration author, or null.
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * The name of the psfx file holding the modules and settings. Defaults to the
	 * JSON file's base name with a {@code .psfx} extension.
	 *
	 * @return the psfx file name.
	 */
	public String getPsfxName() {
		if (psfxName != null && !psfxName.isBlank()) {
			return psfxName;
		}
		return (jsonFile == null) ? null : stripExtension(jsonFile.getName()) + ".psfx";
	}

	/**
	 * The lowest sample rate, in Hz, this configuration can be used with. Zero
	 * means there is no lower limit.
	 *
	 * @return the minimum sample rate in Hz.
	 */
	public double getMinSampleRate() {
		return (minSampleRate == null) ? 0 : minSampleRate;
	}

	/**
	 * The highest sample rate, in Hz, this configuration can be used with, or null
	 * if there is no upper limit. Most configurations have no upper limit since
	 * data can always be decimated down to the rate the detectors expect.
	 *
	 * @return the maximum sample rate in Hz, or null.
	 */
	public Double getMaxSampleRate() {
		return maxSampleRate;
	}

	/**
	 * The sample rate, in Hz, the configuration's detectors are designed to run at.
	 * If the imported data are at a higher rate then a decimator is inserted (or an
	 * existing one retuned) to bring the data down to this rate. Null means the
	 * rate should be read from the psfx file's own sound acquisition settings.
	 *
	 * @return the target sample rate in Hz, or null if it should be taken from the psfx.
	 */
	public Double getTargetSampleRate() {
		return targetSampleRate;
	}

	/**
	 * The minimum number of channels the configuration needs. Defaults to one.
	 * @return the minimum channel count.
	 */
	public int getMinChannels() {
		return (minChannels == null) ? 1 : minChannels;
	}

	/**
	 * The maximum number of channels the configuration can use, or null if there is
	 * no limit.
	 *
	 * @return the maximum channel count, or null.
	 */
	public Integer getMaxChannels() {
		return maxChannels;
	}

	/**
	 * The sound medium this configuration is for. Null means it is suitable for
	 * both air and water.
	 *
	 * @return the sound medium, or null for either.
	 */
	public SoundMedium getMedium() {
		if (medium == null || medium.isBlank()) {
			return null;
		}
		String tidy = medium.trim();
		if (tidy.equalsIgnoreCase("water")) {
			return SoundMedium.Water;
		}
		if (tidy.equalsIgnoreCase("air")) {
			return SoundMedium.Air;
		}
		// "any", "both" and anything unrecognised mean no restriction.
		return null;
	}

	/**
	 * The file types which must all be present among the imported files for this
	 * configuration to be offered. Defaults to {@link PamImportFileType#SOUND}. A
	 * configuration which processes SoundTrap click detections declares
	 * {@code ["SOUND", "SUD_CLICKS"]} and so is only offered when sud files
	 * containing detections have been imported.
	 *
	 * @return the required file types, never null or empty.
	 */
	public Set<PamImportFileType> getRequiredFileTypes() {
		Set<PamImportFileType> types = new LinkedHashSet<>();
		if (requiredFileTypes == null || requiredFileTypes.isEmpty()) {
			types.add(PamImportFileType.SOUND);
			return types;
		}
		for (String typeName : requiredFileTypes) {
			if (typeName == null) {
				continue;
			}
			try {
				types.add(PamImportFileType.valueOf(typeName.trim().toUpperCase()));
			}
			catch (IllegalArgumentException e) {
				System.out.println(String.format("PamConfig \"%s\": unrecognised file type \"%s\"",
						getName(), typeName));
			}
		}
		if (types.isEmpty()) {
			types.add(PamImportFileType.SOUND);
		}
		return types;
	}

	/**
	 * The PAMGuard run modes this configuration can be used in. Defaults to normal
	 * mode only.
	 *
	 * @return the run mode constants from {@link PamController}, never null or empty.
	 */
	public Set<Integer> getRunModes() {
		Set<Integer> modes = new LinkedHashSet<>();
		if (runModes == null || runModes.isEmpty()) {
			modes.add(PamController.RUN_NORMAL);
			return modes;
		}
		for (String modeName : runModes) {
			if (modeName == null) {
				continue;
			}
			switch (modeName.trim().toUpperCase()) {
			case "NORMAL":
				modes.add(PamController.RUN_NORMAL);
				break;
			case "VIEWER":
			case "PAMVIEW":
				modes.add(PamController.RUN_PAMVIEW);
				break;
			case "MIXED":
			case "MIXEDMODE":
				modes.add(PamController.RUN_MIXEDMODE);
				break;
			default:
				System.out.println(String.format("PamConfig \"%s\": unrecognised run mode \"%s\"",
						getName(), modeName));
			}
		}
		if (modes.isEmpty()) {
			modes.add(PamController.RUN_NORMAL);
		}
		return modes;
	}

	/**
	 * The taxonomic groups and species this configuration targets.
	 * @return the taxa, never null.
	 */
	public List<ConfigTaxonTarget> getTaxa() {
		return (taxa == null) ? new ArrayList<>() : taxa;
	}

	/**
	 * The primary species group, used to place the configuration in the wizard's
	 * group filter.
	 *
	 * @return the first declared group, or {@link ConfigSpeciesGroup#OTHER}.
	 */
	public ConfigSpeciesGroup getPrimaryGroup() {
		List<ConfigTaxonTarget> targets = getTaxa();
		if (targets.isEmpty()) {
			return ConfigSpeciesGroup.OTHER;
		}
		return targets.get(0).getGroup();
	}

	/**
	 * Every species group this configuration targets.
	 * @return the groups, never null; empty if no taxa were declared.
	 */
	public Set<ConfigSpeciesGroup> getGroups() {
		Set<ConfigSpeciesGroup> groups = new LinkedHashSet<>();
		for (ConfigTaxonTarget target : getTaxa()) {
			groups.add(target.getGroup());
		}
		return groups;
	}

	/**
	 * A flat list of everything this configuration targets, for display. Specific
	 * species are listed by name; groups with no named species are listed by group
	 * name.
	 *
	 * @return the species list, never null.
	 */
	public String[] getSpeciesList() {
		List<String> list = new ArrayList<>();
		for (ConfigTaxonTarget target : getTaxa()) {
			if (target.isSpeciesSpecific()) {
				list.addAll(target.getSpecies());
			}
			else {
				list.add(target.getGroup().getGroupName());
			}
		}
		return list.toArray(new String[0]);
	}

	/**
	 * The unit name of a decimator which is already present in the psfx file. When
	 * set, that decimator is retuned to the target sample rate rather than a new one
	 * being inserted, which avoids having to rewire the modules downstream of it.
	 *
	 * @return the decimator unit name, or null if the psfx has no decimator.
	 */
	public String getDecimatorUnitName() {
		return (decimatorUnitName == null || decimatorUnitName.isBlank()) ? null : decimatorUnitName.trim();
	}

	/**
	 * Module class names which must continue to read full rate data from the sound
	 * acquisition module when a decimator is inserted.
	 *
	 * @return the module class names, never null.
	 * @see #DEFAULT_KEEP_RAW_MODULES
	 */
	public List<String> getKeepRawSourceModules() {
		if (keepRawSourceModules == null) {
			return new ArrayList<>(DEFAULT_KEEP_RAW_MODULES);
		}
		return keepRawSourceModules;
	}

	/**
	 * The JSON file this description was read from.
	 * @return the JSON file.
	 */
	public File getJsonFile() {
		return jsonFile;
	}

	/**
	 * @param jsonFile the JSON file this description was read from.
	 */
	public void setJsonFile(File jsonFile) {
		this.jsonFile = jsonFile;
	}

	/**
	 * The psfx file holding the modules and settings for this configuration.
	 * @return the psfx file.
	 */
	public File getPsfxFile() {
		return psfxFile;
	}

	/**
	 * @param psfxFile the psfx file holding the modules and settings.
	 */
	public void setPsfxFile(File psfxFile) {
		this.psfxFile = psfxFile;
	}

	/**
	 * The folder this configuration was found in.
	 * @return the containing folder, or null.
	 */
	public File getConfigFolder() {
		return (jsonFile == null) ? null : jsonFile.getParentFile();
	}

	/**
	 * A key which uniquely identifies this configuration within the catalogue, used
	 * so that a configuration in the user folder overrides a shipped one of the same
	 * name.
	 *
	 * @return the identifying key.
	 */
	public String getKey() {
		return (jsonFile == null) ? getName() : stripExtension(jsonFile.getName()).toLowerCase();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Remove the extension from a file name.
	 * @param fileName the file name.
	 * @return the name without its extension.
	 */
	static String stripExtension(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return (dot <= 0) ? fileName : fileName.substring(0, dot);
	}
}
