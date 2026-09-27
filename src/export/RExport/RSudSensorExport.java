package export.RExport;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.ListVector.NamedBuilder;

import PamguardMVC.PamDataBlock;
import soundtrapSensor.SudSensorControl;
import soundtrapSensor.SudSensorDataBlock;
import soundtrapSensor.SudSensorDataUnit;

/**
 * Export a {@link SudSensorDataUnit} (SoundTrap SWV sensor data) to an R list.
 * <p>
 * Each exported list entry contains the generic PAMGuard detection fields plus:
 * <ul>
 *   <li>Magnetometer X, Y, Z (µT)</li>
 *   <li>Accelerometer X, Y, Z (m/s²)</li>
 *   <li>Heading, Pitch, Roll (degrees)</li>
 * </ul>
 *
 * @author Jamie Macaulay
 */
public class RSudSensorExport extends RDataUnitExport<SudSensorDataUnit> {

    @Override
    public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, SudSensorDataUnit dataUnit, int index) {

        // Magnetometer (µT)
        rData.add("magX",  dataUnit.getMagX());
        rData.add("magY",  dataUnit.getMagY());
        rData.add("magZ",  dataUnit.getMagZ());

        // Accelerometer (m/s²)
        rData.add("accelX", dataUnit.getAccelX());
        rData.add("accelY", dataUnit.getAccelY());
        rData.add("accelZ", dataUnit.getAccelZ());

        // Sensor-fusion orientation (degrees)
        rData.add("heading", dataUnit.getHeading());
        rData.add("pitch",   dataUnit.getPitch());
        rData.add("roll",    dataUnit.getRoll());

        return rData;
    }

    /**
     * Returns a metadata list containing the magnetometer/accelerometer sample
     * rate and the physical units for every exported field.
     * <p>
     * Fields written:
     * <ul>
     *   <li>{@code sampleRateHz}     – sample rate of the SWV sensor stream (Hz)</li>
     *   <li>{@code magUnits}         – physical unit of the magnetometer axes ("uT")</li>
     *   <li>{@code accelUnits}       – physical unit of the accelerometer axes ("m/s2")</li>
     *   <li>{@code orientationUnits} – unit of heading, pitch and roll ("degrees")</li>
     * </ul>
     */
    @Override
    protected NamedBuilder detectionHeader(PamDataBlock pamDataBlock) {
        ListVector.NamedBuilder rData = new ListVector.NamedBuilder();
        try {
            SudSensorDataBlock sensorDataBlock = (SudSensorDataBlock) pamDataBlock;
            SudSensorControl sensorControl =
                    (SudSensorControl) sensorDataBlock.getParentProcess().getPamControlledUnit();

            double sampleRateHz = sensorControl.getSWVHandler().getSampleRateHz();

            rData.add("sampleRateHz",      sampleRateHz);
            rData.add("magUnits",          "uT");
            rData.add("accelUnits",        "m/s2");
            rData.add("orientationUnits",  "degrees");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rData;
    }

    @Override
    public Class<?> getUnitClass() {
        return SudSensorDataUnit.class;
    }

    @Override
    public String getName() {
        return "sud_sensor_data";
    }
}
