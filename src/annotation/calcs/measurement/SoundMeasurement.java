package annotation.calcs.measurement;

/**
 * Standard bioacoustic measurements of the sound within a time-frequency box: energy
 * distribution (robust) measurements, spectrogram power and entropy measurements,
 * waveform amplitude measurements and selection measurements. Measurements already
 * stored for every annotation (begin / end time and low / high frequency) are not
 * included.
 * <p>
 * Times are in seconds from the start of the annotation. Spectrogram based times are the
 * centre of the spectrogram frame. Power values are dB re 1 (sample units)<sup>2</sup>/Hz,
 * i.e. uncalibrated.
 * <p>
 * The column names are written to the database, so don't change them.
 *
 * @author Jamie Macaulay
 */
public enum SoundMeasurement {

	// measurements based on spectrogram values
	AVERAGE_POWER(Category.SPECTROGRAM, "Average Power", "AvgPower", "dB",
			"The power spectral density averaged over all time-frequency bins in the annotation"),
	DELTA_POWER(Category.SPECTROGRAM, "Delta Power", "DeltaPower", "dB",
			"The selection spectrum at the upper frequency limit minus the selection spectrum at the lower frequency limit"),
	ENERGY(Category.SPECTROGRAM, "Energy", "Energy", "dB",
			"The total energy within the annotation bounds"),
	AGGREGATE_ENTROPY(Category.SPECTROGRAM, "Aggregate Entropy", "AggEntropy", "bits",
			"The disorder of the energy distribution over frequency for the whole annotation. A pure tone has zero entropy"),
	AVERAGE_ENTROPY(Category.SPECTROGRAM, "Average Entropy", "AvgEntropy", "bits",
			"The entropy of each spectrogram frame, averaged over the annotation"),
	PEAK_FREQUENCY(Category.SPECTROGRAM, "Peak Frequency", "PeakFreq", "Hz",
			"The frequency at which the peak power occurs"),
	PEAK_POWER(Category.SPECTROGRAM, "Peak Power", "PeakPower", "dB",
			"The maximum power in the annotation (the darkest point on the spectrogram)"),
	PEAK_POWER_TIME(Category.SPECTROGRAM, "Peak Power Time", "PeakPowerTime", "s",
			"The time at which the peak power first occurs"),

	// robust signal measurements
	CENTER_FREQUENCY(Category.ROBUST, "Center Frequency", "CenterFreq", "Hz",
			"The frequency which divides the annotation into two frequency intervals of equal energy"),
	FREQ_25(Category.ROBUST, "1st Quartile Frequency", "Freq25", "Hz",
			"The frequency below which 25% of the energy in the annotation lies"),
	FREQ_75(Category.ROBUST, "3rd Quartile Frequency", "Freq75", "Hz",
			"The frequency below which 75% of the energy in the annotation lies"),
	IQR_BANDWIDTH(Category.ROBUST, "IQR Bandwidth", "IQRBandwidth", "Hz",
			"The difference between the 1st and 3rd quartile frequencies"),
	FREQ_5(Category.ROBUST, "Frequency 5%", "Freq5", "Hz",
			"The frequency below which 5% of the energy in the annotation lies"),
	FREQ_95(Category.ROBUST, "Frequency 95%", "Freq95", "Hz",
			"The frequency below which 95% of the energy in the annotation lies"),
	BANDWIDTH_90(Category.ROBUST, "Bandwidth 90%", "Bandwidth90", "Hz",
			"The difference between the 5% and 95% frequencies"),
	CENTER_TIME(Category.ROBUST, "Center Time", "CenterTime", "s",
			"The time which divides the annotation into two time intervals of equal energy"),
	TIME_25(Category.ROBUST, "1st Quartile Time", "Time25", "s",
			"The time before which 25% of the energy in the annotation lies"),
	TIME_75(Category.ROBUST, "3rd Quartile Time", "Time75", "s",
			"The time before which 75% of the energy in the annotation lies"),
	IQR_DURATION(Category.ROBUST, "IQR Duration", "IQRDuration", "s",
			"The difference between the 1st and 3rd quartile times"),
	TIME_5(Category.ROBUST, "Time 5%", "Time5", "s",
			"The time before which 5% of the energy in the annotation lies"),
	TIME_95(Category.ROBUST, "Time 95%", "Time95", "s",
			"The time before which 95% of the energy in the annotation lies"),
	DURATION_90(Category.ROBUST, "Duration 90%", "Duration90", "s",
			"The difference between the 5% and 95% times"),

	// measurements based on waveform values
	MAX_AMPLITUDE(Category.WAVEFORM, "Max Amplitude", "MaxAmplitude", "",
			"The maximum sample value in the annotation"),
	MIN_AMPLITUDE(Category.WAVEFORM, "Min Amplitude", "MinAmplitude", "",
			"The minimum sample value in the annotation"),
	PEAK_AMPLITUDE(Category.WAVEFORM, "Peak Amplitude", "PeakAmplitude", "",
			"The greater of the absolute values of the max and min amplitudes"),
	RMS_AMPLITUDE(Category.WAVEFORM, "RMS Amplitude", "RMSAmplitude", "",
			"The root-mean-square amplitude of all samples in the annotation"),
	FILTERED_RMS_AMPLITUDE(Category.WAVEFORM, "Filtered RMS Amplitude", "FiltRMSAmplitude", "",
			"The root-mean-square amplitude within the frequency limits of the annotation"),
	MAX_TIME(Category.WAVEFORM, "Max Amplitude Time", "MaxAmpTime", "s",
			"The time of the first sample equal to the max amplitude"),
	MIN_TIME(Category.WAVEFORM, "Min Amplitude Time", "MinAmpTime", "s",
			"The time of the first sample equal to the min amplitude"),
	PEAK_TIME(Category.WAVEFORM, "Peak Amplitude Time", "PeakAmpTime", "s",
			"The time of the first sample equal to the peak amplitude"),

	// selection based measurements
	DELTA_TIME(Category.SELECTION, "Delta Time", "DeltaTime", "s",
			"The duration of the annotation"),
	DELTA_FREQUENCY(Category.SELECTION, "Delta Frequency", "DeltaFreq", "Hz",
			"The difference between the upper and lower frequency limits of the annotation"),
	LENGTH(Category.SELECTION, "Spectrogram Frames", "NFrames", "frames",
			"The number of spectrogram frames in the annotation");

	/**
	 * Groups of measurements.
	 */
	public enum Category {
		SPECTROGRAM("Spectrogram"), ROBUST("Robust signal"), WAVEFORM("Waveform"), SELECTION("Selection");

		private final String title;

		Category(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

	private final Category category;
	private final String displayName;
	private final String columnName;
	private final String unit;
	private final String description;

	SoundMeasurement(Category category, String displayName, String columnName, String unit,
			String description) {
		this.category = category;
		this.displayName = displayName;
		this.columnName = columnName;
		this.unit = unit;
		this.description = description;
	}

	public Category getCategory() {
		return category;
	}

	/**
	 * @return the measurement name shown to the user.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the database column name.
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the units, or an empty string for dimensionless sample values.
	 */
	public String getUnit() {
		return unit;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Format a value of this measurement for display.
	 * @param value measurement value.
	 * @return formatted value with units.
	 */
	public String format(double value) {
		if (Double.isNaN(value)) {
			return "-";
		}
		String str;
		switch (unit) {
		case "Hz":
			str = String.format("%.1f", value);
			break;
		case "s":
			str = String.format("%.4f", value);
			break;
		case "dB":
			str = String.format("%.2f", value);
			break;
		case "frames":
			str = String.format("%d", (long) value);
			break;
		case "bits":
			str = String.format("%.3f", value);
			break;
		default:
			str = String.format("%.4g", value);
		}
		return unit.isEmpty() ? str : str + " " + unit;
	}
}
