package soundtrapSensor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSidePanel;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;

/**
 * Side panel for the SudSensor module. Displays the most recently computed
 * heading, pitch and roll values whilst PAMGuard is processing SWV data.
 *
 * @author Jamie Macaulay
 */
public class SudSensorSidePanel extends PamObserverAdapter implements PamSidePanel {

    private final SudSensorControl sensorControl;

    private TitledBorder titledBorder;
    private InnerPanel innerPanel;

    /** Most recent values – kept so the timer can paint without a data lookup. */
    private volatile double lastHeading = Double.NaN;
    private volatile double lastPitch   = Double.NaN;
    private volatile double lastRoll    = Double.NaN;

    /** Refresh display at ~4 Hz so it doesn't flicker. */
    private final Timer refreshTimer;

    public SudSensorSidePanel(SudSensorControl sensorControl) {
        this.sensorControl = sensorControl;

        innerPanel = new InnerPanel();

        // Observe the data block so we receive new data units as they arrive
        sensorControl.getSensorProcess().getSensorDataBlock().addObserver(this);

        refreshTimer = new Timer(250, e -> innerPanel.refreshLabels());
        refreshTimer.start();
    }

    // ------------------------------------------------------------------
    // PamObserverAdapter – called on every new SudSensorDataUnit
    // ------------------------------------------------------------------

    @Override
    public void addData(PamObservable o, PamDataUnit dataUnit) {
        if (!(dataUnit instanceof SudSensorDataUnit)) return;
        SudSensorDataUnit su = (SudSensorDataUnit) dataUnit;
        lastHeading = su.getHeading();
        lastPitch   = su.getPitch();
        lastRoll    = su.getRoll();
    }

    @Override
    public String getObserverName() {
        return sensorControl.getUnitName() + " side panel";
    }

    @Override
    public long getRequiredDataHistory(PamObservable o, Object arg) {
        return 0; // only need the latest value
    }

    // ------------------------------------------------------------------
    // PamSidePanel
    // ------------------------------------------------------------------

    @Override
    public JComponent getPanel() {
        return innerPanel;
    }

    @Override
    public void rename(String newName) {
        titledBorder.setTitle(newName);
        innerPanel.repaint();
    }

    // ------------------------------------------------------------------
    // Inner panel
    // ------------------------------------------------------------------

    private class InnerPanel extends PamBorderPanel {

        private final JLabel headingValue;
        private final JLabel pitchValue;
        private final JLabel rollValue;

        InnerPanel() {
            super();
            setBorder(titledBorder = new TitledBorder(sensorControl.getUnitName()));
            titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.ipadx = 4;
            c.ipady = 2;
            c.anchor = GridBagConstraints.WEST;

            // Row: Heading
            c.gridx = 0; c.gridy = 0;
            addComponent(this, new PamLabel("Heading:"), c);
            c.gridx = 1;
            headingValue = new PamLabel("---");
            addComponent(this, headingValue, c);
            c.gridx = 2;
            addComponent(this, new PamLabel("°"), c);

            // Row: Pitch
            c.gridx = 0; c.gridy = 1;
            addComponent(this, new PamLabel("Pitch:"), c);
            c.gridx = 1;
            pitchValue = new PamLabel("---");
            addComponent(this, pitchValue, c);
            c.gridx = 2;
            addComponent(this, new PamLabel("°"), c);

            // Row: Roll
            c.gridx = 0; c.gridy = 2;
            addComponent(this, new PamLabel("Roll:"), c);
            c.gridx = 1;
            rollValue = new PamLabel("---");
            addComponent(this, rollValue, c);
            c.gridx = 2;
            addComponent(this, new PamLabel("°"), c);
        }

        void refreshLabels() {
            headingValue.setText(formatAngle(lastHeading));
            pitchValue.setText(formatAngle(lastPitch));
            rollValue.setText(formatAngle(lastRoll));
        }

        private String formatAngle(double v) {
            return Double.isNaN(v) ? "---" : String.format("%.1f", v);
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            if (titledBorder != null) {
                titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
            }
        }
    }
}
