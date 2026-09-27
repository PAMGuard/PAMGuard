package soundtrapSensor.plot;

import dataPlotsFX.layout.TDSettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamScrollPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import rawDeepLearningClassifier.dataPlotFX.LineInfo;

/**
 * TD settings pane for the SudSensor module. Shown as a sliding panel inside
 * any TD graph that has the SudSensor overlay active.
 * <p>
 * Presents a {@link PamToggleSwitch} and colour picker for each of the 9
 * plotted lines (heading, pitch, roll, mag X/Y/Z, accel X/Y/Z).
 * <p>
 * The orientation controls (heading/pitch/roll) are disabled when the
 * <em>Raw Sensor</em> axis is selected, and the magnetometer/accelerometer
 * controls are disabled when any orientation axis is selected.
 *
 * @author Jamie Macaulay
 */
public class SudSensorSettingsPane extends PamBorderPane implements TDSettingsPane {

    private static final String[] SECTION_LABELS = {"Orientation", "Magnetometer", "Accelerometer"};
    private static final int[][] SECTION_INDICES = {
        {SudSensorDisplayParams.IDX_HEADING, SudSensorDisplayParams.IDX_PITCH, SudSensorDisplayParams.IDX_ROLL},
        {SudSensorDisplayParams.IDX_MAG_X,   SudSensorDisplayParams.IDX_MAG_Y, SudSensorDisplayParams.IDX_MAG_Z},
        {SudSensorDisplayParams.IDX_ACCEL_X, SudSensorDisplayParams.IDX_ACCEL_Y, SudSensorDisplayParams.IDX_ACCEL_Z}
    };

    // Section index constants
    private static final int SECTION_ORIENTATION   = 0;
    private static final int SECTION_MAGNETOMETER  = 1;
    private static final int SECTION_ACCELEROMETER = 2;

    private final SudSensorPlotInfoFX plotInfo;

    /** One toggle switch per line. */
    private final PamToggleSwitch[] toggles = new PamToggleSwitch[SudSensorDisplayParams.N_LINES];
    /** One colour picker per line. */
    private final ColorPicker[]     pickers = new ColorPicker[SudSensorDisplayParams.N_LINES];

    /** Rows in each section – for bulk enable/disable. */
    private final GridPane[] sectionGrids = new GridPane[SECTION_LABELS.length];

    private boolean settingParams = false;

    private final PamBorderPane mainPane = new PamBorderPane();

    public SudSensorSettingsPane(SudSensorPlotInfoFX plotInfo) {
        this.plotInfo = plotInfo;
        buildUI();
        setCenter(mainPane);
        setParams();
    }

    // -----------------------------------------------------------------------
    // UI construction
    // -----------------------------------------------------------------------

    private void buildUI() {
        PamVBox vbox = new PamVBox();
        vbox.setSpacing(6);
        vbox.setPadding(new Insets(8, 10, 8, 10));

        for (int s = 0; s < SECTION_LABELS.length; s++) {
            vbox.getChildren().add(makeSectionLabel(SECTION_LABELS[s]));

            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(4);
            grid.setPadding(new Insets(2, 0, 4, 12));
            sectionGrids[s] = grid;

            int[] indices = SECTION_INDICES[s];
            for (int r = 0; r < indices.length; r++) {
                int idx = indices[r];

                // PamToggleSwitch with empty string (no label text)
                PamToggleSwitch ts = new PamToggleSwitch("");
                ts.getLabel().setVisible(false);   // hide the empty label node
                ts.getLabel().setManaged(false);

                ColorPicker cp = new ColorPicker();
                cp.setPrefWidth(100);

                Label lbl = new Label(SudSensorDisplayParams.LINE_NAMES[idx]);

                toggles[idx] = ts;
                pickers[idx] = cp;

                ts.selectedProperty().addListener((obs, o, n) -> getParams());
                cp.valueProperty().addListener((obs, o, n)    -> getParams());

                grid.add(ts,  0, r);
                grid.add(lbl, 1, r);
                grid.add(cp,  2, r);
            }

            vbox.getChildren().add(grid);
            if (s < SECTION_LABELS.length - 1) vbox.getChildren().add(new Separator());
        }

        mainPane.setCenter(new PamScrollPane(vbox));
        mainPane.setPrefWidth(310);
    }

    private Label makeSectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    // -----------------------------------------------------------------------
    // Params transfer
    // -----------------------------------------------------------------------

    /** Populate controls from the current display params, then update enablement. */
    public void setParams() {
        settingParams = true;
        SudSensorDisplayParams p = plotInfo.getDisplayParams();
        for (int i = 0; i < SudSensorDisplayParams.N_LINES; i++) {
            LineInfo li = p.lineInfos[i];
            toggles[i].setSelected(li.enabled);
            pickers[i].setValue(li.color != null ? li.color : Color.WHITE);
        }
        settingParams = false;
        updateAxisEnablement();
    }

    /** Read controls back into display params and repaint. */
    private void getParams() {
        if (settingParams) return;
        SudSensorDisplayParams p = plotInfo.getDisplayParams();
        for (int i = 0; i < SudSensorDisplayParams.N_LINES; i++) {
            p.lineInfos[i].enabled = toggles[i].isSelected();
            p.lineInfos[i].color   = pickers[i].getValue();
        }
        plotInfo.getTDGraph().repaint(50);
    }

    /**
     * Enable or disable each section's controls depending on which axis the
     * TD graph currently has active:
     * <ul>
     *   <li>Raw Sensor axis  → orientation section disabled, raw sections enabled</li>
     *   <li>Any other axis   → orientation section enabled,  raw sections disabled</li>
     * </ul>
     * Called from {@link #setParams()} and from
     * {@link SudSensorPlotInfoFX#notifyChange(int)} whenever the axis changes.
     */
    public void updateAxisEnablement() {
        boolean isRaw = plotInfo.isRawAxisSelected();
        setSectionDisabled(SECTION_ORIENTATION,   isRaw);
        setSectionDisabled(SECTION_MAGNETOMETER,  !isRaw);
        setSectionDisabled(SECTION_ACCELEROMETER, !isRaw);
    }

    /** Disable or enable all controls in a section grid. */
    private void setSectionDisabled(int sectionIndex, boolean disabled) {
        sectionGrids[sectionIndex].setDisable(disabled);
    }

    // -----------------------------------------------------------------------
    // TDSettingsPane
    // -----------------------------------------------------------------------

    @Override
    public Node getHidingIcon() {
        return null;
    }

    @Override
    public String getShowingName() {
        return "Orientation Sensor";
    }

    @Override
    public Node getShowingIcon() {
        return null;
    }

    @Override
    public Pane getPane() {
        return mainPane;
    }
}