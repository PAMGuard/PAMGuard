package PamUtils;

/**
 * A set of static functions for formatting frequency values in a common way, 
 * i.e. if it's relatively low, do it in Hz, it it's high, do it in kHz;
 * @author Doug Gillespie
 *
 */
public class FrequencyFormat {
	
	private String numberFormat;
	private String unitText;
	private double scale;
	
	/**
	 * @param numberFormat
	 * @param unitText
	 * @param scale
	 */
	private FrequencyFormat(String numberFormat, String unitText, double scale) {
		super();
		this.numberFormat = numberFormat;
		this.unitText = unitText;
		this.scale = scale;
	}

	/**
	 * Format a frequency value. 
	 * @param frequency frequency in Hz
	 * @param includeUnits flag to include units (Hz or kHz)
	 * @return formatted string
	 */
	public static String formatFrequency(double frequency, boolean includeUnits) {
		FrequencyFormat ff = getFrequencyFormat(frequency);
		if (includeUnits) {
			return String.format(ff.numberFormat + " " + ff.unitText, frequency / ff.scale);
		}
		else {
			return String.format(ff.numberFormat, frequency / ff.scale);
		}
	}
	/**
	 * Format a frequency range. 
	 * @param frequency array of frequency values in Hz. 
	 * @param includeUnits flag to include units (Hz or kHz)
	 * @return formatted string
	 */
	public static String formatFrequencyRange(double[] frequency, boolean includeUnits) {
		if (frequency == null) {
			return null;
		}
		int n = frequency.length;
		FrequencyFormat ff = getFrequencyFormat(frequency[n-1]);
		String str = String.format(ff.numberFormat, frequency[0] / ff.scale);
		for (int i = 1; i < n; i++) {
			str += String.format(" - " + ff.numberFormat, frequency[i] / ff.scale);
		}
		if (includeUnits) {
			str += " " + ff.unitText;
		}
		return str;
	}

	/**
	 * Get everything you need to format a frequency string in a standard
	 * way. 
	 * @param frequency
	 * @return Frequency format object containing a format string for 
	 * the number, a unit (Hz or kHz) and a scale factor to divide the 
	 * raw Hz value down by (1 or 1000).
	 */
	public static FrequencyFormat getFrequencyFormat(double frequency) {
		if (frequency < 10) {
			return new FrequencyFormat("%3.3f", "Hz", 1);
		}
		else if (frequency < 100) {
			return new FrequencyFormat("%3.2f", "Hz", 1);
		}
		else if (frequency < 1000) {
			return new FrequencyFormat("%3.1f", "Hz", 1);
		}
		else if (frequency < 10000){
			return new FrequencyFormat("%3.3f", "kHz", 1000);
		}
		else if (frequency < 100000){
			return new FrequencyFormat("%3.2f", "kHz", 1000);
		}
		return new FrequencyFormat("%3.1f", "kHz", 1000);
	}

	/**
	 * @return the numberFormat
	 */
	public String getNumberFormat() {
		return numberFormat;
	}

	/**
	 * @return the unitText
	 */
	public String getUnitText() {
		return unitText;
	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}
}
