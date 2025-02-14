package dataPlotsFX.spectrogramPlotFX;

import PamUtils.FrequencyFormat;
import javafx.util.StringConverter;

/**
 * Format frequency string converter.
 */
public class FrequencyStringConverter extends StringConverter<Number> {
	
	
	private FrequencyFormat format;

	public FrequencyStringConverter(FrequencyFormat format) {
		this.format = format;
	}

	@Override
	public String toString(Number object) {
		return String.format(format.getNumberFormat(), object.doubleValue() / format.getScale());
	}

	@Override
	public Number fromString(String string) {
		return Double.valueOf(string)*format.getScale();
	}

}
