package soundtrapSensor;

/**
 * Tilt-compensated compass / sensor fusion for magnetometer + accelerometer.
 * <p>
 * No gyroscope is available in the SoundTrap SWV stream, so this class
 * implements a complementary (Mahony-style) filter driven solely by
 * accelerometer and magnetometer measurements.
 * <p>
 * The filter maintains an orientation quaternion that is corrected each sample
 * using the error between the measured gravity/field vectors and the predicted
 * ones from the current quaternion estimate.
 *
 * @author Jamie Macaulay
 */
public class SudSensorFusion {

    // Mahony gains
    private double kp;
    private double ki;

    // Magnetic declination (radians, added to computed heading)
    private double declinationRad;

    // Integral error accumulators
    private double eIntX = 0, eIntY = 0, eIntZ = 0;

    // Quaternion (w, x, y, z) – start with identity
    private double q0 = 1, q1 = 0, q2 = 0, q3 = 0;

    // Sample interval in seconds (set when the first sample arrives)
    private double dt = 1.0 / 25.0; // default 25 Hz

    /**
     * Create a fusion instance with the given settings.
     *
     * @param params       sensor parameters (gains, declination)
     * @param sampleRateHz sample rate in Hz of the SWV stream
     */
    public SudSensorFusion(SudSensorParams params, double sampleRateHz) {
        this.kp = params.fusionKp;
        this.ki = params.fusionKi;
        this.declinationRad = Math.toRadians(params.magneticDeclinationDeg);
        if (sampleRateHz > 0) {
            this.dt = 1.0 / sampleRateHz;
        }
    }

    /**
     * Update the filter with a new accelerometer and magnetometer measurement.
     *
     * @param ax  accelerometer X (any unit, will be normalised)
     * @param ay  accelerometer Y
     * @param az  accelerometer Z
     * @param mx  magnetometer X (any unit, will be normalised)
     * @param my  magnetometer Y
     * @param mz  magnetometer Z
     */
    public void update(double ax, double ay, double az,
                       double mx, double my, double mz) {

        // Normalise accelerometer
        double normA = Math.sqrt(ax * ax + ay * ay + az * az);
        if (normA == 0) return;
        ax /= normA; ay /= normA; az /= normA;

        // Normalise magnetometer
        double normM = Math.sqrt(mx * mx + my * my + mz * mz);
        if (normM == 0) return;
        mx /= normM; my /= normM; mz /= normM;

        // Reference directions of Earth's magnetic and gravitational field
        // expressed in body frame from the current quaternion
        double q0q0 = q0 * q0, q0q1 = q0 * q1, q0q2 = q0 * q2, q0q3 = q0 * q3;
        double q1q1 = q1 * q1, q1q2 = q1 * q2, q1q3 = q1 * q3;
        double q2q2 = q2 * q2, q2q3 = q2 * q3;
        double q3q3 = q3 * q3;

        // Estimated direction of gravity in body frame
        double vx = 2.0 * (q1q3 - q0q2);
        double vy = 2.0 * (q0q1 + q2q3);
        double vz = q0q0 - q1q1 - q2q2 + q3q3;

        // Reference magnetic field in world frame (horizontal component only)
        double hx = 2.0 * mx * (0.5 - q2q2 - q3q3) + 2.0 * my * (q1q2 - q0q3) + 2.0 * mz * (q1q3 + q0q2);
        double hy = 2.0 * mx * (q1q2 + q0q3) + 2.0 * my * (0.5 - q1q1 - q3q3) + 2.0 * mz * (q2q3 - q0q1);
        double hz = 2.0 * mx * (q1q3 - q0q2) + 2.0 * my * (q2q3 + q0q1) + 2.0 * mz * (0.5 - q1q1 - q2q2);

        double bx = Math.sqrt(hx * hx + hy * hy);
        double bz = hz;

        // Estimated direction of magnetic field in body frame
        double wx = 2.0 * bx * (0.5 - q2q2 - q3q3) + 2.0 * bz * (q1q3 - q0q2);
        double wy = 2.0 * bx * (q1q2 - q0q3) + 2.0 * bz * (q0q1 + q2q3);
        double wz = 2.0 * bx * (q1q3 + q0q2) + 2.0 * bz * (0.5 - q1q1 - q2q2);

        // Error: cross product between estimated and measured directions
        double ex = (ay * vz - az * vy) + (my * wz - mz * wy);
        double ey = (az * vx - ax * vz) + (mz * wx - mx * wz);
        double ez = (ax * vy - ay * vx) + (mx * wy - my * wx);

        // Integral feedback
        if (ki > 0) {
            eIntX += ki * ex * dt;
            eIntY += ki * ey * dt;
            eIntZ += ki * ez * dt;
        }

        // Apply proportional + integral feedback as a pseudo-gyroscope
        double gx = kp * ex + eIntX;
        double gy = kp * ey + eIntY;
        double gz = kp * ez + eIntZ;

        // Integrate quaternion rate
        double dq0 = 0.5 * (-q1 * gx - q2 * gy - q3 * gz) * dt;
        double dq1 = 0.5 * ( q0 * gx + q2 * gz - q3 * gy) * dt;
        double dq2 = 0.5 * ( q0 * gy - q1 * gz + q3 * gx) * dt;
        double dq3 = 0.5 * ( q0 * gz + q1 * gy - q2 * gx) * dt;

        q0 += dq0; q1 += dq1; q2 += dq2; q3 += dq3;

        // Normalise quaternion
        double normQ = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 /= normQ; q1 /= normQ; q2 /= normQ; q3 /= normQ;
    }

    /**
     * Heading in degrees [−180, +180) with magnetic declination applied.
     */
    public double getHeadingDeg() {
        double yaw = Math.atan2(2.0 * (q0 * q3 + q1 * q2),
                                1.0 - 2.0 * (q2 * q2 + q3 * q3));
        yaw += declinationRad;
        double deg = Math.toDegrees(yaw);
        // Normalise to (-180, +180]
        if (deg > 180.0)  deg -= 360.0;
        if (deg <= -180.0) deg += 360.0;
        return deg;
    }

    /**
     * Pitch in degrees [−90, +90].
     */
    public double getPitchDeg() {
        return Math.toDegrees(
                Math.asin(Math.max(-1.0, Math.min(1.0,
                        2.0 * (q0 * q2 - q3 * q1)))));
    }

    /**
     * Roll in degrees [−180, +180].
     */
    public double getRollDeg() {
        return Math.toDegrees(
                Math.atan2(2.0 * (q0 * q1 + q2 * q3),
                           1.0 - 2.0 * (q1 * q1 + q2 * q2)));
    }

    /** Reset the quaternion to the identity (no rotation). */
    public void reset() {
        q0 = 1; q1 = 0; q2 = 0; q3 = 0;
        eIntX = 0; eIntY = 0; eIntZ = 0;
    }

    /**
     * Update filter gains from a revised parameter set without replacing the
     * instance.
     */
    public void updateParams(SudSensorParams params) {
        this.kp = params.fusionKp;
        this.ki = params.fusionKi;
        this.declinationRad = Math.toRadians(params.magneticDeclinationDeg);
    }
}
