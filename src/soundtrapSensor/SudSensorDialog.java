package soundtrapSensor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamPanel;

/**
 * Settings dialog for the SudSensor module.
 *
 * @author Jamie Macaulay
 */
public class SudSensorDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private SudSensorParams params;

	// Magnetometer offsets
	private JTextField tfMagOffX, tfMagOffY, tfMagOffZ;
	// Magnetometer scale factors
	private JTextField tfMagScaleX, tfMagScaleY, tfMagScaleZ;
	// Accelerometer offsets
	private JTextField tfAccelOffX, tfAccelOffY, tfAccelOffZ;
	// Fusion gains
	private JTextField tfKp, tfKi;
	// Magnetic declination
	private JTextField tfDeclination;
	// ADC scale factors
	private JTextField tfMagLsb, tfAccelLsb;
	// File continuity
	private JTextField tfMaxGapMs;

	// ---- Streamer tab controls ----
	private JCheckBox  cbEnableStreamer;
	private JSpinner   spInterval;
	private JSpinner   spStreamerIndex;
	private JTextField tfHeadingOffset;
	private JTextField tfPitchOffset;
	private JTextField tfRollOffset;

	private JButton defaultsBtn;

	private PamPanel offsetPanel;

	private PamPanel streamerPanel;

	private SudSensorDialog(Frame parentFrame, SudSensorControl control) {
		super(parentFrame, "SudSensor Settings", false);
		this.params = control.getParams().clone();

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Calibration",       buildCalibrationTab());
		tabs.addTab("Streamer Output",   buildStreamerTab());

		setDialogComponent(tabs);

		addDefaultsButton();
		setHelpPoint("sensors/soundtrapSensor/docs/soundtrapSensor.html");
		populateFields();

		if (control.isViewer()) {
			setAllChildrenEnabled(tabs, false);
			defaultsBtn.setEnabled(false);
			getOkButton().setEnabled(false);
		}

		pack();
	}

	// -----------------------------------------------------------------------
	// Defaults popup button
	// -----------------------------------------------------------------------

	private void addDefaultsButton() {
		defaultsBtn = new JButton("Defaults \u25ba");
		defaultsBtn.setToolTipText("Load factory defaults for a specific device");

		JPopupMenu menu = new JPopupMenu();
		for (SudSensorDefaults preset : SudSensorDefaults.values()) {
			JMenuItem item = new JMenuItem(preset.getDisplayName());
			item.addActionListener(e -> applyDefaults(preset));
			menu.add(item);
		}

		defaultsBtn.addActionListener(e ->
		menu.show(defaultsBtn, 0, defaultsBtn.getHeight()));

		getButtonPanel().add(defaultsBtn);
	}

	private void applyDefaults(SudSensorDefaults preset) {
		double currentDeclination = parseField(tfDeclination, params.magneticDeclinationDeg);
		params = preset.createParams();
		params.magneticDeclinationDeg = currentDeclination;
		populateFields();
	}

	// -----------------------------------------------------------------------
	// Tab builders
	// -----------------------------------------------------------------------

	private PamPanel buildCalibrationTab() {
		PamPanel p = new PamPanel(new BorderLayout(4, 4));
		p.add(buildMagPanel(),   BorderLayout.NORTH);
		p.add(buildAccelPanel(), BorderLayout.CENTER);

		PamPanel bottom = new PamPanel(new BorderLayout(4, 4));
		bottom.add(buildFusionPanel(), BorderLayout.NORTH);
		p.add(bottom, BorderLayout.SOUTH);
		return p;
	}

	private PamPanel buildMagPanel() {
		PamPanel p = new PamPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createTitledBorder("Magnetometer Calibration"));
		GridBagConstraints c = new PamGridBagContraints();

		addRow(p, c, "Hard-iron offset X (\u00b5T):",  tfMagOffX   = new JTextField(8));
		addRow(p, c, "Hard-iron offset Y (\u00b5T):",  tfMagOffY   = new JTextField(8));
		addRow(p, c, "Hard-iron offset Z (\u00b5T):",  tfMagOffZ   = new JTextField(8));
		addRow(p, c, "Scale factor X:",                tfMagScaleX = new JTextField(8));
		addRow(p, c, "Scale factor Y:",                tfMagScaleY = new JTextField(8));
		addRow(p, c, "Scale factor Z:",                tfMagScaleZ = new JTextField(8));
		addRow(p, c, "LSB \u2192 \u00b5T factor:",     tfMagLsb    = new JTextField(8));

		tfMagOffX.setToolTipText("<html>Hard-iron calibration offset subtracted from the raw magnetometer X reading (\u00b5T).<br>"
				+ "Compensates for static magnetic bias from nearby ferromagnetic material.</html>");
		tfMagOffY.setToolTipText("<html>Hard-iron calibration offset subtracted from the raw magnetometer Y reading (\u00b5T).</html>");
		tfMagOffZ.setToolTipText("<html>Hard-iron calibration offset subtracted from the raw magnetometer Z reading (\u00b5T).</html>");
		tfMagScaleX.setToolTipText("<html>Soft-iron scale factor applied to the calibrated magnetometer X axis.<br>"
				+ "Corrects for distortion of the magnetic field by magnetically permeable material. Default 1.0 (no correction).</html>");
		tfMagScaleY.setToolTipText("<html>Soft-iron scale factor applied to the calibrated magnetometer Y axis. Default 1.0 (no correction).</html>");
		tfMagScaleZ.setToolTipText("<html>Soft-iron scale factor applied to the calibrated magnetometer Z axis. Default 1.0 (no correction).</html>");
		tfMagLsb.setToolTipText("<html>Conversion factor from raw 16-bit ADC counts to microtesla (\u00b5T/LSB).<br>"
				+ "For the LSM303AGR at full-scale \u00b150\u00a0gauss: 0.15\u00a0\u00b5T/LSB.</html>");
		return p;
	}

	private PamPanel buildAccelPanel() {
		PamPanel p = new PamPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createTitledBorder("Accelerometer Calibration"));
		GridBagConstraints c = new PamGridBagContraints();

		addRow(p, c, "Offset X:",                         tfAccelOffX = new JTextField(8));
		addRow(p, c, "Offset Y:",                         tfAccelOffY = new JTextField(8));
		addRow(p, c, "Offset Z:",                         tfAccelOffZ = new JTextField(8));
		addRow(p, c, "LSB \u2192 m/s\u00b2 factor:",     tfAccelLsb  = new JTextField(8));

		tfAccelOffX.setToolTipText("<html>Calibration offset subtracted from the raw accelerometer X reading (ADC counts).<br>"
				+ "Compensates for zero-g bias error on the X axis.</html>");
		tfAccelOffY.setToolTipText("<html>Calibration offset subtracted from the raw accelerometer Y reading (ADC counts).</html>");
		tfAccelOffZ.setToolTipText("<html>Calibration offset subtracted from the raw accelerometer Z reading (ADC counts).</html>");
		tfAccelLsb.setToolTipText("<html>Conversion factor from raw 16-bit ADC counts to m/s\u00b2 per LSB.<br>"
				+ "For the LSM303AGR at \u00b12\u00a0g high-resolution mode: 9.80665\u00d710\u207b\u00b3\u00a0m/s\u00b2/LSB.</html>");
		return p;
	}

	private PamPanel buildFusionPanel() {
		PamPanel p = new PamPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createTitledBorder("Sensor Fusion (Mahony Filter)"));
		GridBagConstraints c = new PamGridBagContraints();

		addRow(p, c, "Proportional gain Kp:",              tfKp          = new JTextField(8));
		addRow(p, c, "Integral gain Ki:",                  tfKi          = new JTextField(8));
		addRow(p, c, "Magnetic declination (\u00b0, +E):", tfDeclination = new JTextField(8));
		addRow(p, c, "Max contiguous gap (ms):",           tfMaxGapMs    = new JTextField(8));

		tfKp.setToolTipText("<html>Proportional gain of the Mahony complementary filter.<br>"
				+ "Higher values make the filter respond faster to sensor errors but increase noise sensitivity.<br>"
				+ "Typical range: 0.5\u20135.0. Default 2.0.</html>");
		tfKi.setToolTipText("<html>Integral gain of the Mahony complementary filter.<br>"
				+ "Eliminates steady-state gyroscope bias (no gyroscope here, so keep small).<br>"
				+ "Typical range: 0.0\u20130.05. Default 0.005.</html>");
		tfDeclination.setToolTipText("<html>Magnetic declination at the deployment site in degrees.<br>"
				+ "Added to the computed magnetic heading to obtain true north heading.<br>"
				+ "Positive values are East of north. Find your local value at ngdc.noaa.gov/geomag.</html>");
		tfMaxGapMs.setToolTipText("<html>Maximum gap (in milliseconds) between the last sample of one SUD file and<br>"
				+ "the first sample of the next before the sensor-fusion filter is reset.<br>"
				+ "Within this threshold the filter state is carried over, avoiding a bedding-in spike at file boundaries.<br>"
				+ "Default 2000\u00a0ms.</html>");
		return p;
	}

	/** Build the Streamer Output tab. */
	private PamPanel buildStreamerTab() {

		// ---- Enable / interval panel ----
		streamerPanel = new PamPanel(new GridBagLayout());
		streamerPanel.setBorder(BorderFactory.createTitledBorder("Array Data Output"));
		GridBagConstraints c = new PamGridBagContraints();
		

		// Enable checkbox spans both columns
		cbEnableStreamer = new JCheckBox("Add orientation to hydrophone array");
		cbEnableStreamer.setToolTipText("<html>When enabled, the module periodically creates data units containing the mean heading, pitch and roll of the sensor fusion output.<br>"
				+ "These data units are added to the hydrophone array as a separate streamer (index specified below) and can be used for rotating acoustic detections - see Hydrophone array...<br>"
				+ "Minimum recommended interval is 5 seconds; smaller values produce large database volumes.</html>");

		c.gridwidth = 2;
		streamerPanel.add(cbEnableStreamer, c);
		c.gridy++;
		c.gridwidth = 1;
		

		// Interval spinner
		spInterval = new JSpinner(new SpinnerNumberModel(5, 1, 3600, 1));
		spInterval.setToolTipText("<html>How often (in seconds) a new streamer data unit containing the mean heading, pitch and roll is created.<br>"
				+ "Minimum recommended value is 5 seconds; smaller values produce large database volumes.</html>");
		
		addSpinnerRow(streamerPanel, c, "Output interval (s):", spInterval);

		//        // Warn when interval drops below 5 s
		//        spInterval.addChangeListener(new ChangeListener() {
		//            @Override
		//            public void stateChanged(ChangeEvent e) {
		//                int val = (Integer) spInterval.getValue();
		//                if (val < 5) {
		//                    Window w = SudSensorDialog.this;
		//                    SudSensorStreamerPublisher.warnIfIntervalTooShort(val, w);
		//                }
		//            }
		//        });

		// Streamer index
		spStreamerIndex = new JSpinner(new SpinnerNumberModel(0, 0, 63, 1));
		spStreamerIndex.setToolTipText("<html>Index of the streamer in the current hydrophone array that this module should update with orientation data.<br>"
				+ "Defaults to and will usually be 0 (the first / only streamer).</html>");
		addSpinnerRow(streamerPanel, c, "Streamer index:", spStreamerIndex);


		// ---- Offset panel ----
		offsetPanel = new PamPanel(new GridBagLayout());
		offsetPanel.setBorder(BorderFactory.createTitledBorder(
				"Orientation offsets"));
		GridBagConstraints c2 = new PamGridBagContraints();

		addRow(offsetPanel, c2, "Heading offset (\u00b0):", tfHeadingOffset = new JTextField(8));
		addRow(offsetPanel, c2, "Pitch offset (\u00b0):",   tfPitchOffset   = new JTextField(8));
		addRow(offsetPanel, c2, "Roll offset (\u00b0):",    tfRollOffset    = new JTextField(8));

		String offsets = "<html><i>Use offsets to correct for sensor misalignment relative to the "
				+ "hydrophone array,<br>e.g. if the sensor is rotated with respect to the array "
				+ "axis.</i></html>";

		tfHeadingOffset.setToolTipText(offsets);
		tfPitchOffset.setToolTipText(offsets);
		tfRollOffset.setToolTipText(offsets);

		//        PamPanel desc = new PamPanel(new BorderLayout());
		//        desc.add(new JLabel("<html><i>Use offsets to correct for sensor misalignment relative to the "
		//                + "hydrophone array,<br>e.g. if the sensor is rotated with respect to the array "
		//                + "axis.</i></html>"), BorderLayout.CENTER);
		//        desc.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		
		PamPanel mainPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints cmain = new PamGridBagContraints();
		cmain.ipady = 4; 
		cmain.gridy = 0;
		PamPanel.addComponent(mainPanel, cbEnableStreamer, cmain);
		cmain.gridy++;
		PamPanel.addComponent(mainPanel, streamerPanel, cmain);
		cmain.gridy++;
		PamPanel.addComponent(mainPanel, offsetPanel, cmain);

		PamPanel centre = new PamPanel(new BorderLayout(4, 4));
		centre.add(mainPanel, BorderLayout.NORTH);
		
		cbEnableStreamer.addActionListener((a)->{
			enableStreamerPanel();
		});

		return centre;
	}

	// -----------------------------------------------------------------------
	// Helper: add a labelled spinner row
	// -----------------------------------------------------------------------

	private void enableStreamerPanel() {
		offsetPanel.setEnabled(cbEnableStreamer.isSelected());
		streamerPanel.setEnabled(cbEnableStreamer.isSelected());
		
		for (int i=0; i<streamerPanel.getComponentCount(); i++) {
			streamerPanel.getComponent(i).setEnabled(cbEnableStreamer.isSelected());
		}
		
		for (int i=0; i<offsetPanel.getComponentCount(); i++) {
			offsetPanel.getComponent(i).setEnabled(cbEnableStreamer.isSelected());
		}
		
		
	}

	private void addSpinnerRow(PamPanel panel, GridBagConstraints c, String label, JSpinner spinner) {
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(2, 4, 2, 4);
		panel.add(new JLabel(label), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(spinner, c);
		c.gridy++;
	}

	private void addRow(PamPanel panel, GridBagConstraints c, String label, JTextField field) {
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(2, 4, 2, 4);
		panel.add(new JLabel(label), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		panel.add(field, c);
		c.gridy++;
	}

	// -----------------------------------------------------------------------
	// Population / reading
	// -----------------------------------------------------------------------

	private void populateFields() {
		tfMagOffX.setText(df(params.magOffsetX));
		tfMagOffY.setText(df(params.magOffsetY));
		tfMagOffZ.setText(df(params.magOffsetZ));
		tfMagScaleX.setText(df(params.magScaleX));
		tfMagScaleY.setText(df(params.magScaleY));
		tfMagScaleZ.setText(df(params.magScaleZ));
		tfMagLsb.setText(df(params.magLsbToMicroTesla));

		tfAccelOffX.setText(df(params.accelOffsetX));
		tfAccelOffY.setText(df(params.accelOffsetY));
		tfAccelOffZ.setText(df(params.accelOffsetZ));
		tfAccelLsb.setText(df(params.accelLsbToMsq));

		tfKp.setText(df(params.fusionKp));
		tfKi.setText(df(params.fusionKi));
		tfDeclination.setText(df(params.magneticDeclinationDeg));
		tfMaxGapMs.setText(String.valueOf(params.maxContiguousGapMs));

		// Streamer tab
		cbEnableStreamer.setSelected(params.createStreamerDataUnits);
		spInterval.setValue(params.streamerIntervalSeconds);
		spStreamerIndex.setValue(params.streamerIndex);
		tfHeadingOffset.setText(df(params.headingOffsetDeg));
		tfPitchOffset.setText(df(params.pitchOffsetDeg));
		tfRollOffset.setText(df(params.rollOffsetDeg));
	}

	private String df(double v) { return String.valueOf(v); }

	private double parseField(JTextField tf, double defaultValue) {
		try {
			return Double.parseDouble(tf.getText().trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	@Override
	public boolean getParams() {


		SudSensorParams params= this.params.clone(); 

		params.magOffsetX = parseField(tfMagOffX,   params.magOffsetX);
		params.magOffsetY = parseField(tfMagOffY,   params.magOffsetY);
		params.magOffsetZ = parseField(tfMagOffZ,   params.magOffsetZ);
		params.magScaleX  = parseField(tfMagScaleX, params.magScaleX);
		params.magScaleY  = parseField(tfMagScaleY, params.magScaleY);
		params.magScaleZ  = parseField(tfMagScaleZ, params.magScaleZ);
		params.magLsbToMicroTesla = parseField(tfMagLsb, params.magLsbToMicroTesla);

		params.accelOffsetX  = parseField(tfAccelOffX,  params.accelOffsetX);
		params.accelOffsetY  = parseField(tfAccelOffY,  params.accelOffsetY);
		params.accelOffsetZ  = parseField(tfAccelOffZ,  params.accelOffsetZ);
		params.accelLsbToMsq = parseField(tfAccelLsb,   params.accelLsbToMsq);

		params.fusionKp = parseField(tfKp, params.fusionKp);
		params.fusionKi = parseField(tfKi, params.fusionKi);
		params.magneticDeclinationDeg = parseField(tfDeclination, params.magneticDeclinationDeg);
		try {
			params.maxContiguousGapMs = Long.parseLong(tfMaxGapMs.getText().trim());
		} catch (NumberFormatException e) {
			// keep existing value
		}

		// Streamer tab
		params.createStreamerDataUnits = cbEnableStreamer.isSelected();
		params.streamerIntervalSeconds = (Integer) spInterval.getValue();

		params.streamerIndex           = (Integer) spStreamerIndex.getValue();
		params.headingOffsetDeg = parseField(tfHeadingOffset, params.headingOffsetDeg);
		params.pitchOffsetDeg   = parseField(tfPitchOffset,   params.pitchOffsetDeg);
		params.rollOffsetDeg    = parseField(tfRollOffset,     params.rollOffsetDeg);

		Window w = SudSensorDialog.this;
		int ans = SudSensorStreamerPublisher.warnIfIntervalTooShort(params.streamerIntervalSeconds, w);
		if (ans == WarnOnce.CANCEL_OPTION) {
			//go back to the dialog. 
			return false;
		}

		this.params = params;

		return true;
	}

	private void setAllChildrenEnabled(java.awt.Container container, boolean enabled) {
		for (java.awt.Component comp : container.getComponents()) {
			comp.setEnabled(enabled);
			if (comp instanceof java.awt.Container) {
				setAllChildrenEnabled((java.awt.Container) comp, enabled);
			}
		}
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// Not used – defaults are applied via the popup menu
	}

	// -----------------------------------------------------------------------
	// Static factory
	// -----------------------------------------------------------------------

	public static SudSensorParams showDialog(Frame parentFrame, SudSensorControl control) {
		SudSensorDialog dlg = new SudSensorDialog(parentFrame, control);
		dlg.setVisible(true);
		return dlg.params;
	}
}
