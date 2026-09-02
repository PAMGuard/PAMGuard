package soundtrapSensor;

/**
 * Factory for device-specific default {@link SudSensorParams} presets.
 * <p>
 * Each preset is represented as an enum constant. Call
 * {@link #createParams()} to get a fresh {@link SudSensorParams} instance
 * populated with the appropriate values.
 * <p>
 * To add a new device in the future, add a new constant and supply its
 * values in the switch block of {@link #createParams()}.
 *
 * @author Jamie Macaulay
 */
public enum SudSensorDefaults {

    /**
     * SoundTrap 600 fitted with an ST600 (LSM303AGR) sensor.
     * <ul>
     *   <li>Accelerometer: ±2 g full-scale, high-resolution mode →
     *       1 mg/LSB = 9.80665×10⁻³ m/s²/LSB</li>
     *   <li>Magnetometer: full-scale ±50 gauss →
     *       1.5 mGauss/LSB = 0.15 µT/LSB</li>
     * </ul>
     */
    ST600_LSM303AGR("ST600 (LSM303AGR)");

    // -----------------------------------------------------------------------

    private final String displayName;

    SudSensorDefaults(String displayName) {
        this.displayName = displayName;
    }

    /** Human-readable name shown in the defaults menu. */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Create a new {@link SudSensorParams} instance pre-populated with the
     * defaults for this device.
     *
     * @return fresh params object
     */
    public SudSensorParams createParams() {
        SudSensorParams p = new SudSensorParams();

        switch (this) {
            case ST600_LSM303AGR:
                // --- Accelerometer (LSM303AGR ±2 g, high-resolution mode) ---
                p.accelOffsetX     = 0.0;
                p.accelOffsetY     = 0.0;
                p.accelOffsetZ     = 0.0;
                p.accelLsbToMsq    = 9.80665e-3;   // 1 mg/LSB × 9.80665 m/s²/g

                // --- Magnetometer (LSM303AGR ±50 gauss range) ---
                p.magOffsetX       = 0.0;
                p.magOffsetY       = 0.0;
                p.magOffsetZ       = 0.0;
                p.magScaleX        = 1.0;
                p.magScaleY        = 1.0;
                p.magScaleZ        = 1.0;
                p.magLsbToMicroTesla = 0.15;        // 1.5 mGauss/LSB = 0.15 µT/LSB

                // --- Mahony filter gains ---
                p.fusionKp = 2.0;
                p.fusionKi = 0.005;

                // Declination left at 0 – site-specific, user must set
                p.magneticDeclinationDeg = 0.0;
                break;

            default:
                // Unknown preset – return bare defaults
                break;
        }

        return p;
    }
}
