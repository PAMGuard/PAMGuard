package clickTrainDetector.clickTrainAlgorithms.ukf;

import PamguardMVC.PamDataBlock;

/**
 * Default parameters for different species for the UKF click train detector.
 * <p>
 * Mirrors {@code DefaultMHTParams}: each {@link DefaultUKFSpecies} maps to a set
 * of {@link UKFParams} optimised for that species. For now only a generic "All"
 * preset is provided (the {@link UKFParams} constructor defaults); further
 * species presets can be added to the enum and switch.
 *
 * @author Jamie Macaulay
 */
public class DefaultUKFParams {

	public enum DefaultUKFSpecies {ALL}

	/**
	 * Get the tool-tip for a particular species.
	 * @param defaultSpecies - the default species.
	 * @return description of the species.
	 */
	public static String getDefaultSpeciesTooltip(DefaultUKFSpecies defaultSpecies) {
		switch (defaultSpecies) {
		case ALL:
			return "General-purpose default settings suitable for a wide range of species";
		default:
			return "";
		}
	}

	/**
	 * Get the string name for the enum DefaultUKFSpecies.
	 * @param defaultSpecies - the species.
	 * @return string name of the species.
	 */
	public static String getDefaultSpeciesName(DefaultUKFSpecies defaultSpecies) {
		switch (defaultSpecies) {
		case ALL:
			return "All";
		default:
			return "";
		}
	}

	/**
	 * Get default UKF parameters for a particular default species.
	 * @param defaultSpecies - the default species.
	 * @param pamDataBlock - the source data block (may be null), in case a preset
	 *                     needs to adapt to the data type.
	 * @return the default UKF parameters for the given DefaultUKFSpecies.
	 */
	public static UKFParams getDefaultUKFParams(DefaultUKFSpecies defaultSpecies, PamDataBlock<?> pamDataBlock) {

		UKFParams params = new UKFParams();

		switch (defaultSpecies) {
		case ALL:
		default:
			// use the UKFParams constructor defaults.
			break;
		}

		return params;
	}

}
