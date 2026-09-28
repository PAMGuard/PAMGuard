package soundtrapSensor.plot;

import java.io.Serializable;

import javafx.scene.paint.Color;
import rawDeepLearningClassifier.dataPlotFX.LineInfo;

/**
 * Serializable display parameters for the SudSensor TD plot.
 * Holds one {@link LineInfo} (colour + enabled flag) for each plotted line:
 * <ul>
 *   <li>0 – Heading</li>
 *   <li>1 – Pitch</li>
 *   <li>2 – Roll</li>
 *   <li>3 – Mag X</li>
 *   <li>4 – Mag Y</li>
 *   <li>5 – Mag Z</li>
 *   <li>6 – Accel X</li>
 *   <li>7 – Accel Y</li>
 *   <li>8 – Accel Z</li>
 * </ul>
 *
 * @author Jamie Macaulay
 */
public class SudSensorDisplayParams implements Serializable, Cloneable {

    public static final long serialVersionUID = 1L;

    /** Index constants for readability. */
    public static final int IDX_HEADING = 0;
    public static final int IDX_PITCH   = 1;
    public static final int IDX_ROLL    = 2;
    public static final int IDX_MAG_X   = 3;
    public static final int IDX_MAG_Y   = 4;
    public static final int IDX_MAG_Z   = 5;
    public static final int IDX_ACCEL_X = 6;
    public static final int IDX_ACCEL_Y = 7;
    public static final int IDX_ACCEL_Z = 8;

    public static final int N_LINES = 9;

    /** Display names shown in the settings pane. */
    public static final String[] LINE_NAMES = {
        "Heading", "Pitch", "Roll",
        "Mag X",   "Mag Y", "Mag Z",
        "Accel X", "Accel Y", "Accel Z"
    };

    /** Default colours. */
    private static final Color[] DEFAULTS = {
        Color.RED,         // Heading
        Color.LIMEGREEN,   // Pitch
        Color.DODGERBLUE,  // Roll
        Color.ORCHID,      // Mag X
        Color.HOTPINK,     // Mag Y
        Color.DEEPPINK,    // Mag Z
        Color.ORANGE,      // Accel X
        Color.GOLD,        // Accel Y
        Color.DARKORANGE   // Accel Z
    };

    /** One LineInfo per line. Colour is transient — serialised via colorSerializable. */
    public LineInfo[] lineInfos = createDefaults();

    private static LineInfo[] createDefaults() {
        LineInfo[] infos = new LineInfo[N_LINES];
        for (int i = 0; i < N_LINES; i++) {
            infos[i] = new LineInfo(i < 3, DEFAULTS[i]); // orientation on by default, raw off
            infos[i].colorSerializable = toDoubleArray(DEFAULTS[i]);
        }
        return infos;
    }

    /** Serialise a JavaFX Color to a double[3]. */
    public static double[] toDoubleArray(Color c) {
        return new double[]{c.getRed(), c.getGreen(), c.getBlue()};
    }

    /** Restore JavaFX Color from a double[3] after deserialisation. */
    public void restoreColors() {
        if (lineInfos == null) return;
        for (LineInfo li : lineInfos) {
            if (li.colorSerializable != null && li.colorSerializable.length == 3) {
                li.color = Color.color(
                        li.colorSerializable[0],
                        li.colorSerializable[1],
                        li.colorSerializable[2]);
            }
        }
    }

    /** Serialise colors before saving. */
    public void prepareForSave() {
        if (lineInfos == null) return;
        for (LineInfo li : lineInfos) {
            if (li.color != null) {
                li.colorSerializable = toDoubleArray(li.color);
            }
        }
    }

    @Override
    public SudSensorDisplayParams clone() {
        try {
            SudSensorDisplayParams c = (SudSensorDisplayParams) super.clone();
            c.lineInfos = new LineInfo[lineInfos.length];
            for (int i = 0; i < lineInfos.length; i++) {
                LineInfo src = lineInfos[i];
                LineInfo copy = new LineInfo(src.enabled, src.color);
                copy.colorSerializable = src.colorSerializable != null
                        ? src.colorSerializable.clone() : null;
                c.lineInfos[i] = copy;
            }
            return c;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
