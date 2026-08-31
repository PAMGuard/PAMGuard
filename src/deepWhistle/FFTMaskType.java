package deepWhistle;

/**
 * The available {@link PamFFTMask} implementations that the user can choose
 * between. Each mask type has a human readable name, an optional URL to a
 * downloadable model and is able to create its own settings pane.
 * <p>
 * Currently only {@link #DEEP_WHISTLE} is implemented but more masks will be
 * added in the future - they simply need to be added to this enum along with a
 * case in {@link #createSettingsPane()}.
 *
 * @author Jamie Macaulay
 */
public enum FFTMaskType {

	/**
	 * The DeepWhistle mask which removes noise from a spectrogram using a deep
	 * learning model.
	 */
	DEEP_WHISTLE("Deep Whistle", DeepWhistleMask.MODEL_URL, new String[] {DeepWhistleMask.MODEL_FILE_NAME},
			"Removes non-whistle energy from the spectrogram using the DeepWhistle convolutional network "
			+ "(from silbido). The model was trained on synthetic whistle data and outputs, for each "
			+ "time-frequency bin, a confidence that the bin belongs to a tonal whistle contour.",
			"Li, P. et al. (2020) 'Learning deep models from synthetic data for extracting dolphin whistle "
			+ "contours', 2020 International Joint Conference on Neural Networks (IJCNN). IEEE, pp. 1-10. "
			+ "doi:10.1109/IJCNN48605.2020.9206992.",
			"https://doi.org/10.1109/IJCNN48605.2020.9206992"),

	/**
	 * The SAM-Whistle mask which removes noise from a spectrogram using a
	 * Segment-Anything based deep learning model trained on the DCLDE dataset.
	 */
	SAM_WHISTLE("SAM-Whistle", SamWhistleMask.MODEL_URL, new String[] {SamWhistleMask.MODEL_FILE_NAME},
			"Removes non-whistle energy from the spectrogram using SAM-Whistle, an adaptation of Meta's "
			+ "Segment Anything Model (SAM). A fine-tuned Vision-Transformer encoder and a lightweight "
			+ "decoder output, for each time-frequency bin, a confidence that the bin belongs to a delphinid "
			+ "whistle. The distributed model was trained on the DCLDE 2011 dataset.",
			"Zhang, X. et al. (2025) 'Automating time x frequency annotations of delphinid whistles by "
			+ "adapting a foundational transformer neural network', Scientific Reports, 15, 37809. "
			+ "doi:10.1038/s41598-025-21642-x.",
			"https://doi.org/10.1038/s41598-025-21642-x");

	/**
	 * Human readable name shown to the user in the selection control.
	 */
	private final String name;

	/**
	 * URL to the downloadable model used by the mask. May be null if the mask
	 * does not require a model.
	 */
	private final String modelURL;

	/**
	 * Candidate model file names to locate within a downloaded archive (e.g. the
	 * <code>.pt</code> file inside a zip). May be null if the model URL points
	 * directly at the model file rather than an archive.
	 */
	private final String[] modelFileNames;

	/**
	 * Brief description of the model, shown in the model information pop-up.
	 */
	private final String description;

	/**
	 * Full reference (Harvard format) for the paper describing the model.
	 */
	private final String reference;

	/**
	 * DOI URL for the paper describing the model.
	 */
	private final String doiURL;

	FFTMaskType(String name, String modelURL, String[] modelFileNames, String description, String reference, String doiURL) {
		this.name = name;
		this.modelURL = modelURL;
		this.modelFileNames = modelFileNames;
		this.description = description;
		this.reference = reference;
		this.doiURL = doiURL;
	}

	/**
	 * Get the human readable name of the mask.
	 *
	 * @return the name of the mask.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the URL to the downloadable model used by the mask.
	 *
	 * @return the model URL or null if the mask does not require a downloadable
	 *         model.
	 */
	public String getModelURL() {
		return modelURL;
	}

	/**
	 * Get a brief description of the model, shown in the model information pop-up.
	 *
	 * @return the model description, or null.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the full reference (Harvard format) for the paper describing the model.
	 *
	 * @return the reference, or null.
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Get the DOI URL for the paper describing the model.
	 *
	 * @return the DOI URL, or null.
	 */
	public String getDoiURL() {
		return doiURL;
	}

	/**
	 * Get the candidate model file names to locate within a downloaded archive.
	 *
	 * @return the model file names, or null if the URL points directly at the
	 *         model file.
	 */
	public String[] getModelFileNames() {
		return modelFileNames;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Create the settings pane associated with this mask. The settings pane holds
	 * any mask specific controls (e.g. the confidence spinner for the DeepWhistle
	 * mask).
	 *
	 * @return a new settings pane for this mask or null if the mask has no
	 *         specific settings.
	 */
	public FFTMaskSettingsPane createSettingsPane() {
		switch (this) {
		case DEEP_WHISTLE:
			return new DeepWhistleMaskPane();
		case SAM_WHISTLE:
			return new SamWhistleMaskPane();
		default:
			return null;
		}
	}

	/**
	 * Create a new instance of the {@link PamFFTMask} for this mask type.
	 *
	 * @param process - the process the mask will be used in.
	 * @return a new mask instance or null if the mask type is unknown.
	 */
	public PamFFTMask createMask(MaskedFFTProcess process) {
		switch (this) {
		case DEEP_WHISTLE:
			return new DeepWhistleMask(process);
		case SAM_WHISTLE:
			return new SamWhistleMask(process);
		default:
			return null;
		}
	}

}
