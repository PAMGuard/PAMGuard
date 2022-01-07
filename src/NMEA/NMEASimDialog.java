package NMEA;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.LatLongDialogStrip;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class NMEASimDialog extends PamDialog {

	private NMEAParameters nmeaParameters;
	
	private static NMEASimDialog nmeaSimDialog = null;
	
	private LatLongDialogStrip latStrip, longStrip;
	private JTextField speed, course;
	private JTextField drunkenness, timeInterval, decPlaces;
	private JCheckBox simAIS;
	private JRadioButton[] headingButtons = new JRadioButton[3];
	private String[] headingStrings = {"No heading sim", "Magnetic Headings", "True Headings (e.g. from Gyro)"};
	private JCheckBox continousChange;
	
	private NMEASimDialog(Frame parentFrame) {
		super(parentFrame, "NMEA Simulation", true);
		JTabbedPane t = new JTabbedPane();
		
		JPanel p = new JPanel();
		
//		p.setBorder(new TitledBorder("GPS Sim settings"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 1;
		addComponent(p, latStrip = new LatLongDialogStrip(true), c);
		c.gridy++;
		addComponent(p, longStrip = new LatLongDialogStrip(false), c);
		c.gridwidth = 1;
		c.gridy++;
		
		JPanel q = new JPanel();
		q.setLayout(new GridBagLayout());
		q.setBorder(new TitledBorder("Speed, course and sim timing"));
		GridBagConstraints d = new PamGridBagContraints();
		d.gridx=0;
		addComponent(q, new JLabel("Vessel speed"), d);
		d.gridx++;
		addComponent(q, speed = new JTextField(5), d);
		d.gridx++;
		addComponent(q, new JLabel(" knots"), d);
		d.gridy++;
		d.gridx=0;
		addComponent(q, new JLabel("Vessel course"), d);
		d.gridx++;
		addComponent(q, course = new JTextField(5), d);
		d.gridx++;
		addComponent(q, new JLabel(" degrees"), d);
		d.gridy++;
		d.gridx=0;
		addComponent(q, new JLabel("Course changes"), d);
		d.gridx++;
		addComponent(q, drunkenness = new JTextField(5), d);
		d.gridx++;
		addComponent(q, new JLabel(" degrees/s"), d);
		d.gridy++;
		d.gridx=0;
		d.gridwidth=3; 
		addComponent(q, continousChange = new JCheckBox("Continous course change"), d);
		continousChange.setToolTipText("<html><p width=\"200\">The course change can be random or continous. Random will cause random changes in heading with an average value defined by the <i>Course change</i> text field. Continous means the vessel "
				+ "will continually change course by the <i>Course change</i> value. <i>i.e.</i> it will eventually go round in a circle.</p></html>"); 
		d.gridy++;
		d.gridwidth=1; 
		d.gridx=0;
		addComponent(q, new JLabel("Time Interval"), d);
		d.gridx++;
		addComponent(q, timeInterval = new JTextField(5), d);
		d.gridx++;
		addComponent(q, new JLabel(" s"), d);
		d.gridy++;
		d.gridx=0;
		d.gridwidth = 2;
		addComponent(q, new JLabel("Lat / Long Decimal Places"), d);
		d.gridx+=2;
		d.gridwidth = 1;
		addComponent(q, decPlaces = new JTextField(5), d);
		
		addComponent(p, q, c);
		
		
		JPanel e = new JPanel();
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		JPanel r = new JPanel();
//		r.setLayout(new GridBagLayout());
		r.setBorder(new TitledBorder("AIS Data"));
		r.setLayout(new BorderLayout());
		r.add(BorderLayout.CENTER, simAIS = new JCheckBox("Generate AIS data"));
		r.add(BorderLayout.SOUTH, new JLabel("(Data recorded in English Channel"));
		
		JPanel m = new JPanel();
		m.setLayout(new GridBagLayout());
		GridBagConstraints mc = new PamGridBagContraints();
		JPanel m2 = new JPanel(new BorderLayout());
		m2.setBorder(new TitledBorder("Heading Data"));
		m2.add(BorderLayout.WEST, m);
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < 3; i++) {
			addComponent(m, headingButtons[i] = new JRadioButton(headingStrings[i]), mc);
			bg.add(headingButtons[i]);
			mc.gridy++;
		}
		
		
//		c.gridy++;
//		addComponent(p, r, c);
		e.add(r);
		e.add(m2);
		
		JPanel et = new JPanel(new BorderLayout());
		et.add(BorderLayout.NORTH, e);
		
		t.add("GPS Data", p);
		t.add("Extras", et);
		setDialogComponent(t);
	}
	
	public static NMEAParameters showDialog(Frame frame, NMEAParameters nmeaParameters) {
		if (nmeaSimDialog == null || frame!=nmeaSimDialog.getParent()) {
			nmeaSimDialog = new NMEASimDialog(frame);
		}
		nmeaSimDialog.nmeaParameters = nmeaParameters.clone();
		nmeaSimDialog.setParams();
		nmeaSimDialog.setVisible(true);
		return nmeaSimDialog.nmeaParameters;
	}
	

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}
	
	private void setParams()  {
		latStrip.sayValue(nmeaParameters.simStartLatitude);
		longStrip.sayValue(nmeaParameters.simStartLongitude);
		speed.setText(String.format("%.1f", nmeaParameters.simStartSpeed));
		course.setText(String.format("%.1f", nmeaParameters.simStartHeading));
		drunkenness.setText(String.format("%.1f", nmeaParameters.drunkenness));
		timeInterval.setText(String.format("%.1f", nmeaParameters.simTimeInterval));
		decPlaces.setText(String.format("%d", nmeaParameters.getLatLongDecimalPlaces()));
		simAIS.setSelected(nmeaParameters.generateAIS);
		for (int i = 0; i < headingButtons.length; i++) {
			headingButtons[i].setSelected(nmeaParameters.simHeadingData == i);
		}
		continousChange.setSelected(nmeaParameters.continousChange);
	}

	@Override
	public boolean getParams() {
		nmeaParameters.simStartLatitude = latStrip.getValue();
		nmeaParameters.simStartLongitude = longStrip.getValue();
		int dp = 3;
		try {
			nmeaParameters.simStartSpeed = Double.valueOf(speed.getText());
			nmeaParameters.simStartHeading = Double.valueOf(course.getText());
			nmeaParameters.drunkenness = Double.valueOf(drunkenness.getText());
			nmeaParameters.simTimeInterval = Double.valueOf(timeInterval.getText());
			dp = Integer.valueOf(decPlaces.getText());
			nmeaParameters.setLatLongDecimalPlaces(dp);
			nmeaParameters.continousChange=continousChange.isSelected();
		}
		catch (NumberFormatException e) {
			return false;
		}
		nmeaParameters.generateAIS = simAIS.isSelected();
		for (int i = 0; i < headingButtons.length; i++) {
			if (headingButtons[i].isSelected()) {
				nmeaParameters.simHeadingData = i;
				break;
			}
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		NMEAParameters np = new NMEAParameters();
		nmeaParameters.simStartHeading = np.simStartHeading;
		nmeaParameters.simStartSpeed = np.simStartSpeed;
		nmeaParameters.simStartLatitude = np.simStartLatitude;
		nmeaParameters.simStartLongitude = np.simStartLongitude;
		nmeaParameters.drunkenness = np.drunkenness;
		nmeaParameters.generateAIS = np.generateAIS;
		continousChange.setSelected(np.continousChange);

		setParams();

	}

}
