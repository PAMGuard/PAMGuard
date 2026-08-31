package soundtrapSensor;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Data block that holds {@link SudSensorDataUnit} objects generated from the
 * SWV sensor stream inside SoundTrap SUD files.
 *
 * The datagram is provided by {@link SudSensorDatagramProvider}, registered
 * via {@link #setDatagramProvider(dataGram.DatagramProvider)} from
 * {@link SudSensorProcess}.
 *
 * @author Jamie Macaulay
 */
public class SudSensorDataBlock extends PamDataBlock<SudSensorDataUnit> {

    private SudSensorBinaryStore binaryStore;

    /**
     * Sample rate of the SWV sensor stream in Hz.
     * Set during normal processing by {@link SudSensorSWVHandler} and restored
     * from the binary module header when running in viewer mode.
     * Defaults to 25 Hz (the standard SoundTrap SWV rate).
     */
    private double sampleRateHz = 25.0;

    public SudSensorDataBlock(PamProcess parentProcess) {
        super(SudSensorDataUnit.class, "SUD Sensor Data", parentProcess, 0);
        binaryStore = new SudSensorBinaryStore(this);
        setBinaryDataSource(binaryStore);
    }

    public SudSensorBinaryStore getBinaryStore() {
        return binaryStore;
    }

    /**
     * Returns the sample rate of the magnetometer/accelerometer stream in Hz.
     * Valid both during normal processing and after the binary module header
     * has been read back in viewer mode.
     *
     * @return sample rate in Hz
     */
    public double getSampleRateHz() {
        return sampleRateHz;
    }

    /**
     * Sets the sample rate. Called by {@link SudSensorSWVHandler} when the
     * SWV stream sample rate is determined, and by {@link SudSensorBinaryStore}
     * when the module header is read back from a binary file in viewer mode.
     *
     * @param sampleRateHz sample rate in Hz
     */
    public void setSampleRateHz(double sampleRateHz) {
        this.sampleRateHz = sampleRateHz;
    }
}