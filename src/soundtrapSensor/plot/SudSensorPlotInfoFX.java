package soundtrapSensor.plot;

import java.io.Serializable;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.SimpleSymbolChooserFX;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import soundtrapSensor.SudSensorControl;
import soundtrapSensor.SudSensorDataBlock;
import soundtrapSensor.SudSensorDataUnit;

/**
 * FX data info for the SudSensor module.
 * <p>
 * On the <b>Orientation</b> axis (0–360 °) it draws:
 * heading (red), pitch (green), roll (blue).
 * <p>
 * On the <b>Raw Sensor</b> axis it draws:
 * mag X/Y/Z and accel X/Y/Z as six separate lines.
 * <p>
 * Line colours and visibility are configurable via the {@link SudSensorSettingsPane}
 * that appears in any TD graph hosting this overlay.
 *
 * @author Jamie Macaulay
 */
public class SudSensorPlotInfoFX extends TDDataInfoFX {

    // -----------------------------------------------------------------
    // Scale infos
    // -----------------------------------------------------------------
    private final SudSensorScaleInfo scaleOrientation = new SudSensorScaleInfo(-180, 180, "Orientation (\u00b0)");
//    private final SudSensorScaleInfo scalePitch        = new SudSensorScaleInfo(-90,   90, "Pitch (\u00b0)");
//    private final SudSensorScaleInfo scaleRoll         = new SudSensorScaleInfo(-180, 180, "Roll (\u00b0)");
    private final TDScaleInfo scaleRaw          = new SudSensorScaleInfo(-256, 256, "Accel/Mag" , ParameterType.AMPLITUDE_LIN, ParameterUnits.RAW);

    // -----------------------------------------------------------------
    // Display params (colours + enabled flags)
    // -----------------------------------------------------------------
    private SudSensorDisplayParams displayParams = new SudSensorDisplayParams();

    // -----------------------------------------------------------------
    // State for line drawing: last drawn point per line
    // -----------------------------------------------------------------
    private static final int N = SudSensorDisplayParams.N_LINES;
    private final double[] lastX = new double[N];
    private final double[] lastY = new double[N];

    private final SimpleSymbolChooserFX symbolChooser = new SimpleSymbolChooserFX();

    /** Lazily created settings pane. */
    private SudSensorSettingsPane settingsPane;

    public SudSensorPlotInfoFX(SudSensorControl sensorControl,
                                TDDataProviderFX provider,
                                TDGraphFX tdGraph,
                                SudSensorDataBlock dataBlock) {
        super(provider, tdGraph, dataBlock);
        addScaleInfo(scaleOrientation);
        addScaleInfo(scaleRaw);
        resetLastPoints();
    }

    // -----------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------

    public SudSensorDisplayParams getDisplayParams() {
        return displayParams;
    }

    /**
     * Returns true when the currently active scale info is the Raw Sensor axis.
     * Used by {@link SudSensorSettingsPane} to enable/disable the correct controls.
     */
    public boolean isRawAxisSelected() {
        return getCurrentScaleInfo() == scaleRaw;
    }

    // -----------------------------------------------------------------
    // Drawing
    // -----------------------------------------------------------------

    @Override
    public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit,
                                GraphicsContext g, double scrollStart,
                                TDProjectorFX tdProjector, int type) {
    	
    	//System.out.println("Drawing SudSensor data unit at time " + PamCalendar.formatDateTime(  pamDataUnit.getTimeMilliseconds()));

        if (!(pamDataUnit instanceof SudSensorDataUnit)) return null;
        SudSensorDataUnit su = (SudSensorDataUnit) pamDataUnit;

        double t = su.getTimeMilliseconds() - scrollStart;
        double x = tdProjector.getTimePix(t);
        

        g.setLineWidth(1.5);
        g.setLineDashes(null);

        // Determine which scale is currently selected to know which lines to draw

        if (getCurrentScaleInfo()==scaleRaw) {
            // Raw sensor lines: mag X/Y/Z (indices 3-5), accel X/Y/Z (indices 6-8)
            drawLine(g, x, tdProjector.getYPix(su.getMagX()),   SudSensorDisplayParams.IDX_MAG_X);
            drawLine(g, x, tdProjector.getYPix(su.getMagY()),   SudSensorDisplayParams.IDX_MAG_Y);
            drawLine(g, x, tdProjector.getYPix(su.getMagZ()),   SudSensorDisplayParams.IDX_MAG_Z);
            drawLine(g, x, tdProjector.getYPix(su.getAccelX()), SudSensorDisplayParams.IDX_ACCEL_X);
            drawLine(g, x, tdProjector.getYPix(su.getAccelY()), SudSensorDisplayParams.IDX_ACCEL_Y);
            drawLine(g, x, tdProjector.getYPix(su.getAccelZ()), SudSensorDisplayParams.IDX_ACCEL_Z);
        } else if (getCurrentScaleInfo()==scaleOrientation) {
            // Orientation lines: heading, pitch, roll (indices 0-2)
            drawLine(g, x, tdProjector.getYPix(su.getHeading()), SudSensorDisplayParams.IDX_HEADING);
            drawLine(g, x, tdProjector.getYPix(su.getPitch()),   SudSensorDisplayParams.IDX_PITCH);
            drawLine(g, x, tdProjector.getYPix(su.getRoll()),    SudSensorDisplayParams.IDX_ROLL);
        }

        return null;
    }

    private void drawLine(GraphicsContext g, double x, double y, int idx) {
        if (!displayParams.lineInfos[idx].enabled) {
            // still update last position so we don't draw a line when re-enabled
            lastX[idx] = x;
            lastY[idx] = y;
            return;
        }
        javafx.scene.paint.Color col = displayParams.lineInfos[idx].color;
        if (col == null) col = javafx.scene.paint.Color.WHITE;
        g.setStroke(col);

        if (!Double.isNaN(lastX[idx])) {
            g.strokeLine(lastX[idx], lastY[idx], x, y);
        }
        lastX[idx] = x;
        lastY[idx] = y;
    }

    @Override
    public void clearDraw() {
        super.clearDraw();
        resetLastPoints();
    }

    private void resetLastPoints() {
        for (int i = 0; i < N; i++) {
            lastX[i] = Double.NaN;
            lastY[i] = Double.NaN;
        }
    }

    // -----------------------------------------------------------------
    // Settings pane
    // -----------------------------------------------------------------

    @Override
    public TDSettingsPane getGraphSettingsPane() {
        if (settingsPane == null) {
            settingsPane = new SudSensorSettingsPane(this);
        }
        return settingsPane;
    }

    // -----------------------------------------------------------------
    // Stored settings (persisted in the psf file)
    // -----------------------------------------------------------------

    @Override
    public Serializable getStoredSettings() {
        displayParams.prepareForSave();
        return displayParams;
    }

    @Override
    public boolean setStoredSettings(Serializable storedSettings) {
        if (storedSettings instanceof SudSensorDisplayParams) {
            displayParams = (SudSensorDisplayParams) storedSettings;
            displayParams.restoreColors();
            if (settingsPane != null) settingsPane.setParams();
            return true;
        }
        return false;
    }

    // -----------------------------------------------------------------
    // Misc overrides
    // -----------------------------------------------------------------

    @Override
    public Double getDataValue(PamDataUnit pamDataUnit) {
        if (!(pamDataUnit instanceof SudSensorDataUnit)) return null;
        return ((SudSensorDataUnit) pamDataUnit).getHeading();
    }

    @Override
    public TDSymbolChooserFX getSymbolChooser() {
        return symbolChooser;
    }

    @Override
    public void notifyChange(int changeType) {
        switch (changeType) {
            case PamController.CHANGED_PROCESS_SETTINGS:
            case PamController.RUN_NORMAL:
            case PamController.PAM_STOPPING:
                resetLastPoints();
                break;
            default:
                break;
        }
      
    }
    
    @Override
	public boolean setCurrentAxisName(ParameterType dataType, ParameterUnits dataUnits) {
    	
    	boolean val = super.setCurrentAxisName(dataType, dataUnits);
    	  // Always update section enablement – the axis may have changed
        if (settingsPane != null) {
            settingsPane.updateAxisEnablement();
        }
        
        return val;
	}
}