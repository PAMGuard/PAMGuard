package soundtrapSensor;

import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

/**
 * Datagram provider for the SudSensor module. Displays heading, pitch
 * and roll in the PAMGuard datagram panel as three separate bands using a 2D
 * plot (one value per time slice per band).
 * <p>
 * All three orientation values are produced by {@link SudSensorFusion} in a
 * ±180 ° / ±90 ° range.  They are shifted to strictly non-negative values
 * before storage so they survive the datagram rendering pipeline:
 * <ol>
 *   <li>Heading – ±180 °, shifted +180 → 0–360 °</li>
 *   <li>Pitch   – ±90 °,  shifted +90  → 0–180 °</li>
 *   <li>Roll    – ±180 °, shifted +180 → 0–360 °</li>
 * </ol>
 *
 * @author Jamie Macaulay
 */
public class SudSensorDatagramProvider implements DatagramProvider {

    /** Three bands: heading, pitch (shifted), roll (shifted). */
    private static final int N_POINTS = 3;

    private final SudSensorControl sensorControl;

    public SudSensorDatagramProvider(SudSensorControl sensorControl) {
        this.sensorControl = sensorControl;
    }

    @Override
    public int getNumDataGramPoints() {
        return N_POINTS;
    }

    /**
     * Writes the current orientation reading into the datagram line.
     * <p>
     * Values are <em>assigned</em> (not accumulated) so the stored value is
     * always one representative sample and is independent of how many data
     * units happened to fall inside the datagram time bin.  Accumulating sums
     * would cause the stored value to scale with the sample count, producing
     * regular spikes at file-boundary intervals.
     * <p>
     * Each value is shifted to a non-negative range before storage (negative
     * stored values are treated as "no data" by the rendering pipeline):
     * <ul>
     *   <li>Heading ±180 °  + 180 → 0–360 °</li>
     *   <li>Pitch   ±90 °   +  90 → 0–180 °</li>
     *   <li>Roll    ±180 °  + 180 → 0–360 °</li>
     * </ul>
     */
    @Override
    public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
        if (!(dataUnit instanceof SudSensorDataUnit)) {
            return 0;
        }
        SudSensorDataUnit su = (SudSensorDataUnit) dataUnit;
        dataGramLine[0] = (float) (su.getHeading() + 180.0);  // −180..+180 → 0..360
        dataGramLine[1] = (float) (su.getPitch()   +  90.0);  // −90..+90   → 0..180
        dataGramLine[2] = (float) (su.getRoll()    + 180.0);  // −180..+180 → 0..360
        return 1;
    }

    /**
     * Returns scale information for the datagram display. The y-axis spans
     * 0–360 ° (the maximum of the three shifted ranges) and is plotted as a
     * flat 2-D colour band rather than a spectrogram.
     */
    @Override
    public DatagramScaleInformation getScaleInformation() {
        return new DatagramScaleInformation(0, 360, "degrees",
                false, DatagramScaleInformation.PLOT_2D);
    }
}
