package whistlesAndMoans;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class WMDisplayDialog extends PamDialog {

	private WhistleMoanControl whistleMoanControl;
	private static WhistleToneParameters wmParams;
	private static WMDisplayDialog wmDisplayDialog = null;
	
	private JCheckBox showContourOutline, stretchContours;
	private JTextField shortLength;
	private JRadioButton[] radioButtons = new JRadioButton[3];
	private String[] buttonNames = {"Show All","Hide All","Show in Grey"};
	
//	private JTextField mapLineLength;
	
	private WMDisplayDialog(WhistleMoanControl whistleMoanControl, Window parentFrame) {
		super(parentFrame, whistleMoanControl.getUnitName() + " display options", true);
		this.whistleMoanControl = whistleMoanControl;
		
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		p.setBorder(new TitledBorder("Spectrogram options"));
		c.gridwidth = 3;
		addComponent(p, showContourOutline = new JCheckBox(
				"Show full outline of detections "), c);
		c.gridy++;
		addComponent(p, stretchContours = 
			new JCheckBox("Stretch contours to a minimum of 1 pixel per FFT bin"), c);
		c.gridwidth = 1;
		c.gridy++;
		addComponent(p, new JLabel("Short sounds < ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(p, shortLength = new JTextField(4), c);
		c.gridx++;
		addComponent(p, new JLabel(" long", SwingConstants.LEFT), c);
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 2;
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < 3; i++) {
			addComponent(p, radioButtons[i] = new JRadioButton(buttonNames[i]), c);
			bg.add(radioButtons[i]);
			c.gridy++;
		}
		
		
		showContourOutline.setToolTipText("<html>" +
				"Show the minimum and maximum boundaries of the connected<p>" +
				"region as well as the peak contour. </html>"
				);
		stretchContours.setToolTipText("<html>" +
				"Stretch contours in time.<p>" +
				"If a large spectrogram time scale has been selected, then <p>" +
				"contours will be drawn with the correct start time, but they<p>" +
				"will be stretched in time so that a minimum of one pixel is used<p>" +
				"for each FFT bin. This makes it easier to view the shapes of contours<p>" +
				"when several minutes of data are viewed.<p>" +
				"This is particularly useful when viewing data in offline mode");
		
		
//		JPanel q = new JPanel();
//		q.setLayout(new GridBagLayout());
//		c = new PamGridBagContraints();
//		q.setBorder(new TitledBorder("Map Options"));
//		addComponent(q, new JLabel("Map line length ", SwingConstants.RIGHT), c);
//		c.gridx++;
//		addComponent(q, mapLineLength = new JTextField(6), c);
//		c.gridx++;
//		addComponent(q, new JLabel(" m", SwingConstants.LEFT), c);
//		mapLineLength.setToolTipText("Default length of bearing lines drawn on map when range data is unavailable");
		
		JTabbedPane t = new JTabbedPane();
//		t.add("Map", q);
		t.add("Spectrogram", p);
		setDialogComponent(t);
	}
	
	public static WhistleToneParameters showDialog(WhistleMoanControl whistleMoanControl,
			Window parentFrame) {
		if (wmDisplayDialog == null || 
				wmDisplayDialog.whistleMoanControl != whistleMoanControl ||
				wmDisplayDialog.getOwner() != parentFrame) {
			wmDisplayDialog = new WMDisplayDialog(whistleMoanControl, parentFrame);
		}
		wmParams = whistleMoanControl.whistleToneParameters.clone();
		wmDisplayDialog.setParams();
		wmDisplayDialog.setVisible(true);
		return wmParams;
	}

	private void setParams() {
		showContourOutline.setSelected(wmParams.showContourOutline);
		stretchContours.setSelected(wmParams.stretchContours);
		shortLength.setText(String.format("%d",wmParams.shortLength));
		for (int i = 0; i < 3; i++) {
			radioButtons[i].setSelected(wmParams.shortShowPolicy == i);
		}
		
//		mapLineLength.setText(String.format("%3.0f", wmParams.getMapLineLength()));
	}

	@Override
	public void cancelButtonPressed() {
		wmParams = null;
	}

	@Override
	public boolean getParams() {
		wmParams.showContourOutline = showContourOutline.isSelected();
		wmParams.stretchContours = stretchContours.isSelected();
		try {
			wmParams.shortLength = Integer.valueOf(shortLength.getText());

//			double ml = Double.valueOf(mapLineLength.getText());
//			wmParams.setMapLineLength(ml);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter");
		}
		for (int i = 0; i < 3; i++) {
			if (radioButtons[i].isSelected()) {
				wmParams.shortShowPolicy = i;
			}
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		wmParams.showContourOutline = false;
		wmParams.stretchContours = false;
		setParams();
	}

}
