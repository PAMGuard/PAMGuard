package soundtrapSensor.plot;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import dataPlotsFX.data.TDScaleInfo;

/**
 * Scale info for the SudSensor TD plot.  Accepts a human-readable axis label
 * that overrides the default ParameterType string so we can show
 * "Orientation (°)" or "Raw Sensor" without needing a new enum value.
 *
 * @author Jamie Macaulay
 */
public class SudSensorScaleInfo extends TDScaleInfo {

    private final String axisLabel;

    public SudSensorScaleInfo(double minVal, double maxVal, String axisLabel) {
        super(minVal, maxVal, ParameterType.BEARING, ParameterUnits.DEGREES);
        this.axisLabel = axisLabel;
    }
    
    public SudSensorScaleInfo(double minVal, double maxVal, String axisLabel, ParameterType axisType, ParameterUnits unitType) {
        super(minVal, maxVal, axisType, unitType);
        this.axisLabel = axisLabel;
    }


    @Override
    public String getAxisName() {
        return axisLabel;
    }
    
    @Override
    public void calcDivisor() {
		//do nothing; the plot info will set the divisor to 1 since the data is already in display units (degrees)
	}
}