package export.MLExport;

import PamguardMVC.PamDataBlock;
import soundtrapSensor.SudSensorControl;
import soundtrapSensor.SudSensorDataBlock;
import soundtrapSensor.SudSensorDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Struct;

/**
 * Export a {@link SudSensorDataUnit} (SoundTrap SWV sensor data) to a MATLAB structure.
 * <p>
 * Each exported structure contains the generic PAMGuard detection fields plus:
 * <ul>
 *   <li>Magnetometer X, Y, Z (µT)</li>
 *   <li>Accelerometer X, Y, Z (m/s²)</li>
 *   <li>Heading, Pitch, Roll (degrees)</li>
 * </ul>
 *
 * @author Jamie Macaulay
 */
public class MLSudSensorExport extends MLDataUnitExport<SudSensorDataUnit> {

    @Override
    public Struct addDetectionSpecificFields(Struct mlStruct, int index, SudSensorDataUnit dataUnit) {

        // Magnetometer (µT)
        mlStruct.set("magX",  index, Mat5.newScalar(dataUnit.getMagX()));
        mlStruct.set("magY",  index, Mat5.newScalar(dataUnit.getMagY()));
        mlStruct.set("magZ",  index, Mat5.newScalar(dataUnit.getMagZ()));

        // Accelerometer (m/s²)
        mlStruct.set("accelX", index, Mat5.newScalar(dataUnit.getAccelX()));
        mlStruct.set("accelY", index, Mat5.newScalar(dataUnit.getAccelY()));
        mlStruct.set("accelZ", index, Mat5.newScalar(dataUnit.getAccelZ()));

        // Sensor-fusion orientation (degrees)
        mlStruct.set("heading", index, Mat5.newScalar(dataUnit.getHeading()));
        mlStruct.set("pitch",   index, Mat5.newScalar(dataUnit.getPitch()));
        mlStruct.set("roll",    index, Mat5.newScalar(dataUnit.getRoll()));

        return mlStruct;
    }

    @Override
    public Class<?> getUnitClass() {
        return SudSensorDataUnit.class;
    }

    @Override
    public String getName() {
        return "sud_sensor_data";
    }

    /**
     * Returns a metadata struct containing the magnetometer/accelerometer sample
     * rate and the physical units for every exported field.
     * <p>
     * Fields written:
     * <ul>
     *   <li>{@code sampleRateHz}   – sample rate of the SWV sensor stream (Hz)</li>
     *   <li>{@code magUnits}       – physical unit of the magnetometer axes ("uT")</li>
     *   <li>{@code accelUnits}     – physical unit of the accelerometer axes ("m/s2")</li>
     *   <li>{@code orientationUnits} – unit of heading, pitch and roll ("degrees")</li>
     * </ul>
     */
    @Override
    protected Struct detectionHeader(PamDataBlock pamDataBlock) {
        try {
            SudSensorDataBlock sensorDataBlock = (SudSensorDataBlock) pamDataBlock;
            SudSensorControl sensorControl =
                    (SudSensorControl) sensorDataBlock.getParentProcess().getPamControlledUnit();

            double sampleRateHz = sensorControl.getSWVHandler().getSampleRateHz();

            Struct header = Mat5.newStruct(1, 1);
            header.set("sampleRateHz",      0, Mat5.newScalar(sampleRateHz));
            header.set("magUnits",          0, Mat5.newString("uT"));
            header.set("accelUnits",        0, Mat5.newString("m/s2"));
            header.set("orientationUnits",  0, Mat5.newString("degrees"));
            return header;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

