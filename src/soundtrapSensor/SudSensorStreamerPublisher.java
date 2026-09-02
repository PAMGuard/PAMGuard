package soundtrapSensor;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataUnit;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;

/**
 * Accumulates sensor-fusion heading, pitch and roll from
 * {@link SudSensorDataUnit}s and periodically emits a
 * {@link StreamerDataUnit} containing the circular mean of the accumulated
 * orientation, plus any fixed offsets configured by the user.
 * <p>
 * An instance is held by {@link SudSensorControl} and is called by
 * {@link SudSensorSWVHandler} immediately after each data unit is added to
 * the data block.
 *
 * @author Jamie Macaulay
 */
public class SudSensorStreamerPublisher {

    private static final String WARN_INTERVAL_KEY = "SudSensor_StreamerInterval";

	private static final int SHORT_INTERVAL = 5;

    private final SudSensorControl sensorControl;

    /** Circular-mean accumulators for heading (sin / cos components). */
    private double headingSinSum = 0;
    private double headingCosSum = 0;

    /** Simple mean accumulators for pitch and roll (small-angle assumption). */
    private double pitchSum = 0;
    private double rollSum  = 0;

    /** Number of data units accumulated in the current interval. */
    private int count = 0;

    /** UTC time (ms) at which the current accumulation interval started. */
    private long intervalStartMillis = -1;

    public SudSensorStreamerPublisher(SudSensorControl sensorControl) {
        this.sensorControl = sensorControl;
    }

    /**
     * Called once per {@link SudSensorDataUnit} immediately after it has been
     * added to the data block.  Accumulates orientation and emits a
     * {@link StreamerDataUnit} when the configured interval has elapsed.
     *
     * @param du the newly created sensor data unit
     */
    public void processSensorData(SudSensorDataUnit du) {
        SudSensorParams params = sensorControl.getParams();
        if (!params.createStreamerDataUnits) {
            return;
        }

        long tNow = du.getTimeMilliseconds();

        // Initialise the interval on the first call, or after a reset.
        if (intervalStartMillis < 0) {
            intervalStartMillis = tNow;
        }

        // Accumulate using circular statistics for heading (to handle 0/360 wrap)
        // and simple running sums for pitch and roll.
        double headingRad = Math.toRadians(du.getHeading());
        headingSinSum += Math.sin(headingRad);
        headingCosSum += Math.cos(headingRad);
        pitchSum      += du.getPitch();
        rollSum       += du.getRoll();
        count++;

        long intervalMs = (long) params.streamerIntervalSeconds * 1000L;
        if (tNow - intervalStartMillis >= intervalMs) {
            emitStreamerDataUnit(tNow, params);
            reset(tNow);
        }
    }

    /**
     * Build a {@link StreamerDataUnit} from the accumulated orientation means,
     * apply the user-defined offsets, then push it to the array streamer data
     * block.
     */
    private void emitStreamerDataUnit(long timeMillis, SudSensorParams params) {
        if (count == 0) return;

        // Circular mean of heading
        double meanHeading = Math.toDegrees(Math.atan2(headingSinSum / count,
                                                        headingCosSum / count));
        // Wrap to −180..+180
        if (meanHeading >  180.0) meanHeading -= 360.0;
        if (meanHeading <= -180.0) meanHeading += 360.0;

        double meanPitch = pitchSum / count;
        double meanRoll  = rollSum  / count;

        // Apply user-supplied offsets
        double heading = wrapHeading(meanHeading + params.headingOffsetDeg);
        double pitch   = meanPitch + params.pitchOffsetDeg;
        double roll    = meanRoll  + params.rollOffsetDeg;

        // Clamp pitch and roll to sensible ranges
        pitch = Math.max(-90.0,  Math.min(90.0,  pitch));
        roll  = Math.max(-180.0, Math.min(180.0, roll));

        // Locate the target streamer in the current array
        ArrayManager am = ArrayManager.getArrayManager();
        if (am == null) return;
        PamArray currentArray = am.getCurrentArray();
        if (currentArray == null) return;

        int si = params.streamerIndex;
        if (si < 0 || si >= currentArray.getNumStreamers()) {
            si = 0;  // fall back to first streamer
        }
        Streamer streamer = currentArray.getStreamer(si);
        if (streamer == null) return;

        // Clone the streamer so we do not modify the array's master copy,
        // then set the orientation fields from the sensor-fusion means.
        Streamer clone = streamer.clone();
        clone.setHeading(heading);
        clone.setPitch(pitch);
        clone.setRoll(roll);

        StreamerDataUnit sdu = new StreamerDataUnit(timeMillis, clone);
        am.getStreamerDatabBlock().addPamData(sdu);
    }

    /** Reset accumulators for the next interval. */
    private void reset(long newIntervalStart) {
        headingSinSum    = 0;
        headingCosSum    = 0;
        pitchSum         = 0;
        rollSum          = 0;
        count            = 0;
        intervalStartMillis = newIntervalStart;
    }

    /**
     * Full reset called when PAMGuard stops, the settings change, or the SWV
     * stream is reset.  The next data unit will start a fresh interval.
     */
    public void reset() {
        reset(-1);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static double wrapHeading(double deg) {
        while (deg >  180.0) deg -= 360.0;
        while (deg <= -180.0) deg += 360.0;
        return deg;
    }

    /**
     * Show a one-time warning if the user sets an interval shorter than 5 s.
     * Should be called from the dialog when the user changes the interval.
     *
     * @param intervalSeconds the new value entered by the user
     * @param parent          parent window for the dialog (may be null)
     */
    public static int warnIfIntervalTooShort(int intervalSeconds, java.awt.Window parent) {
        if (intervalSeconds < SHORT_INTERVAL) {
            String msg = "<html><b>Short streamer output interval</b><br><br>"
                    + "You have set the streamer output interval to <b>" + intervalSeconds + " s</b>.<br>"
                    + "Values below 5 seconds will produce a very large number of records in the<br>"
                    + "PAMGuard database and may significantly increase database file size.<br><br>"
                    + "Consider using an interval of 5 seconds or more.</html>";
           return  WarnOnce.showWarning(parent, "Streamer Interval Warning", msg, WarnOnce.WARNING_MESSAGE);
        }
        else return WarnOnce.OK_OPTION; 
    }
}
