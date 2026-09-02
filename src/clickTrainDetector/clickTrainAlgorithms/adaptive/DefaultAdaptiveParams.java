package clickTrainDetector.clickTrainAlgorithms.adaptive;

import PamguardMVC.PamDataBlock;

/**
 * Default parameters for different species for the adaptive click train detector.
 * <p>
 * Mirrors {@code DefaultMHTParams}: each {@link DefaultAdaptiveSpecies} maps to a
 * set of {@link AdaptiveCTParams} optimised for that species. For now only a
 * generic "All" preset is provided (the {@link AdaptiveCTParams} constructor
 * defaults); further species presets can be added to the enum and switch.
 *
 * @author Jamie Macaulay
 */
public class DefaultAdaptiveParams {

	public enum DefaultAdaptiveSpecies {ALL}

	/**
	 * Get the tool-tip for a particular species.
	 * @param defaultSpecies - the default species.
	 * @return description of the species.
	 */
	public static String getDefaultSpeciesTooltip(DefaultAdaptiveSpecies defaultSpecies) {
		switch (defaultSpecies) {
		case ALL:
			return "General-purpose default settings suitable for a wide range of species";
		default:
			return "";
		}
	}

	/**
	 * Get the string name for the enum DefaultAdaptiveSpecies.
	 * @param defaultSpecies - the species.
	 * @return string name of the species.
	 */
	public static String getDefaultSpeciesName(DefaultAdaptiveSpecies defaultSpecies) {
		switch (defaultSpecies) {
		case ALL:
			return "All";
		default:
			return "";
		}
	}

	/**
	 * Get default adaptive parameters for a particular default species.
	 * @param defaultSpecies - the default species.
	 * @param pamDataBlock - the source data block (may be null), in case a preset
	 *                     needs to adapt to the data type.
	 * @return the default adaptive parameters for the given DefaultAdaptiveSpecies.
	 */
	public static AdaptiveCTParams getDefaultAdaptiveParams(DefaultAdaptiveSpecies defaultSpecies,
			PamDataBlock<?> pamDataBlock) {

		AdaptiveCTParams params = new AdaptiveCTParams();

		switch (defaultSpecies) {
		case ALL:
		default:
			// use the AdaptiveCTParams constructor defaults.
			break;
		}

		return params;
	}

}
