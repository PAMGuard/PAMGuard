package soundtrapSensor;

import PamguardMVC.PamDataUnit;

/**
 * A single data unit from the SoudTrap SWV sensor file. Each instance
 * represents one sample point and stores:
 * <ul>
 *   <li>Raw (calibrated) magnetometer values – X, Y, Z (µT)</li>
 *   <li>Raw (calibrated) accelerometer values – X, Y, Z (m/s²)</li>
 *   <li>Heading, pitch and roll produced by the sensor-fusion algorithm (°)</li>
 * </ul>
 *
 * @author Jamie Macaulay
 */
public class SudSensorDataUnit extends PamDataUnit {

    /** Calibrated magnetometer X, Y, Z in µT */
    private double magX, magY, magZ;

    /** Calibrated accelerometer X, Y, Z in m/s² */
    private double accelX, accelY, accelZ;

    /** Sensor-fusion heading, pitch and roll in degrees */
    private double heading;
    private double pitch;
    private double roll;

    /**
     * Construct a new sensor data unit.
     *
     * @param timeMilliseconds  absolute UTC time in milliseconds
     * @param magX   magnetometer X (µT)
     * @param magY   magnetometer Y (µT)
     * @param magZ   magnetometer Z (µT)
     * @param accelX accelerometer X (m/s²)
     * @param accelY accelerometer Y (m/s²)
     * @param accelZ accelerometer Z (m/s²)
     * @param heading heading (degrees, ±180)
     * @param pitch   pitch   (degrees, ±90)
     * @param roll    roll    (degrees, ±180)
     */
    public SudSensorDataUnit(long timeMilliseconds,
                              double magX, double magY, double magZ,
                              double accelX, double accelY, double accelZ,
                              double heading, double pitch, double roll) {
        super(timeMilliseconds);
        this.magX   = magX;
        this.magY   = magY;
        this.magZ   = magZ;
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.heading = heading;
        this.pitch   = pitch;
        this.roll    = roll;
    }

    // -----------------------------------------------------------------
    // Getters / setters
    // -----------------------------------------------------------------

    public double getMagX()  { return magX; }
    public double getMagY()  { return magY; }
    public double getMagZ()  { return magZ; }

    public double getAccelX() { return accelX; }
    public double getAccelY() { return accelY; }
    public double getAccelZ() { return accelZ; }

    public double getHeading() { return heading; }
    public double getPitch()   { return pitch;   }
    public double getRoll()    { return roll;    }

    public void setHeading(double heading) { this.heading = heading; }
    public void setPitch  (double pitch)   { this.pitch   = pitch;   }
    public void setRoll   (double roll)    { this.roll    = roll;    }
}
