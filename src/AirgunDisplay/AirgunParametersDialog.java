package AirgunDisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import GPS.ShipDimensionsFields;
import GPS.ShipDimensionsPanel;
import PamUtils.LatLong;
import PamUtils.LatLongEditableDialogStrip;
import PamView.dialog.PamDialog;

public class AirgunParametersDialog extends PamDialog {

	private static AirgunParametersDialog airgunParametersDialog;

	private AirgunParameters airgunParameters;

	private ShipDimensionsPanel shipDrawing;

	private ShipDimensionsFields shipDimensionsFields;

	private ShipIDPanel shipIDPanel;

	private ExclusionPanel exclusionPanel;

	private String[] fieldNames = {"E","F","Depth"};

	public AirgunParametersDialog(Frame parentFrame) {
		super(parentFrame, "Airgun display parameters", false);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		AirgunDimensionsDrawing ad = new AirgunDimensionsDrawing();
		p.add(shipDrawing = new ShipDimensionsPanel(ad,
				shipDimensionsFields = new ShipDimensionsFields(fieldNames)));

		JPanel q = new JPanel();
		q.setLayout(new BoxLayout(q, BoxLayout.Y_AXIS));
		q.add(shipIDPanel = new ShipIDPanel());
		q.add(exclusionPanel = new ExclusionPanel());
		p.add(q);
		setDialogComponent(p);
//		setModal(true);
	}

	public static AirgunParameters showDialog(Frame parentFrame, AirgunParameters airgunParameters) {
		if (airgunParametersDialog == null || airgunParametersDialog.getParent() != parentFrame) {
			airgunParametersDialog = new AirgunParametersDialog(parentFrame);
		}
		airgunParametersDialog.airgunParameters = airgunParameters.clone();
		airgunParametersDialog.setParams();
		airgunParametersDialog.setVisible(true);

		return airgunParametersDialog.airgunParameters;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	public void setParams() {
		double[] dim = new double[3];
		dim[0] = airgunParameters.dimE;
		dim[1] = airgunParameters.dimF;
		dim[2] = airgunParameters.gunDepth;
		shipDimensionsFields.setDimensions(dim);
		shipIDPanel.setParams();
		exclusionPanel.setParams();

	}

	@Override
	public boolean getParams() {
		double[] dim = shipDimensionsFields.getDimensions();
		if (dim == null || dim.length != 3) return false;
		airgunParameters.dimE = dim[0];
		airgunParameters.dimF = dim[1];
		airgunParameters.gunDepth = dim[2];
		if (!shipIDPanel.getParams() || !exclusionPanel.getParams()) return false;
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	class ShipIDPanel extends JPanel implements ActionListener {

		private JRadioButton thisVessel, otherVessel, fixedLocation;
		private JTextField mmsiNumber;
		private LatLongEditableDialogStrip llStrip;

		public ShipIDPanel() {
			super();
			setBorder(new TitledBorder("Source Vessel Identification"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = 0;
			addComponent(this, thisVessel = new JRadioButton("Guns are deployed from this vessel"), c);
			c.gridy++;
			addComponent(this, otherVessel = new JRadioButton("Guns are deployed from a different vessel"), c);
			ButtonGroup bg = new ButtonGroup();
			bg.add(thisVessel);
			bg.add(otherVessel);
			thisVessel.addActionListener(this);
			otherVessel.addActionListener(this);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(this, new JLabel("Source vessel mmsi number ", SwingConstants.RIGHT), c);
			c.gridx++;
			addComponent(this, mmsiNumber = new JTextField(7), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(this, fixedLocation = new JRadioButton("Fixed Location"), c);
			c.gridx = 0;
			c.gridy++;
			llStrip = new LatLongEditableDialogStrip(getOwner(), "Fixed Position");
			c.gridwidth = 2;
			addComponent(this, llStrip.getDialogComponent(), c);
			bg.add(fixedLocation);
			fixedLocation.addActionListener(this);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}

		public void enableControls() {
			mmsiNumber.setEnabled(otherVessel.isSelected());
			llStrip.enableControls(fixedLocation.isSelected());
		}

		public void setParams() {
			thisVessel.setSelected(airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_THIS_VESSEL);
			otherVessel.setSelected(airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_AIS_VESSEL);
			fixedLocation.setSelected(airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_FIXEDPOSITION);
			mmsiNumber.setText(String.format("%d", airgunParameters.gunsMMSIVessel));
			llStrip.setLatLong(airgunParameters.fixedPosition);
			enableControls();
		}

		public boolean getParams() {
			if (thisVessel.isSelected()) {
				airgunParameters.gunsReferencePosition = AirgunParameters.GUNS_THIS_VESSEL;
			}
			else if (otherVessel.isSelected()) {
				airgunParameters.gunsReferencePosition = AirgunParameters.GUNS_AIS_VESSEL;
			}
			else if (fixedLocation.isSelected()) {
				airgunParameters.gunsReferencePosition = AirgunParameters.GUNS_FIXEDPOSITION;
			}
			if (airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_AIS_VESSEL) {
				try {
					airgunParameters.gunsMMSIVessel = Integer.valueOf(mmsiNumber.getText());
				}
				catch (NumberFormatException ex) {
					return false;
				}
			}
			if (airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_FIXEDPOSITION) {
				LatLong fp = llStrip.getLatLong();
				if (fp == null) {
					return showWarning("You must enter a valid position for the fixed position option");
				}
				airgunParameters.fixedPosition = fp;
			}
			return true;
		}

	}
	class ExclusionPanel extends JPanel implements ActionListener {

		JTextField exRadius;

		JCheckBox showExclusion;

		JPanel exPanel;

		JButton exButton;

		JCheckBox predictAhead;

		JTextField predictionTime;

		public ExclusionPanel() {
			super();
			setBorder(new TitledBorder("Guns mitigation zone"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.anchor = GridBagConstraints.WEST;

			c.gridy = c.gridx = 0;
			c.gridwidth = 3;
			addComponent(this, showExclusion = new JCheckBox("Show mitigation zone on map "), c);
			showExclusion.addActionListener(this);

			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(this, new JLabel("Mitigation radius "), c);
			c.gridx++;
			addComponent(this, exRadius = new JTextField(6), c);
			c.gridx++;
			addComponent(this, new JLabel(" m"), c);

			c.gridwidth = 1;
			c.gridy++;
			c.gridx = 0;
			addComponent(this, exPanel = new JPanel(), c);
			exPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
			exPanel.setPreferredSize(new Dimension(30,20));
			c.gridx++;
			addComponent(this, exButton = new JButton("Colour"), c);
			exButton.addActionListener(this);

			c.gridx = 0;
			c.gridy ++;
			c.gridwidth = 3;
			addComponent(this, predictAhead = new JCheckBox("Show predicted zone ... "), c);
			predictAhead.addActionListener(this);
			c.gridx = 0;
			c.gridy ++;
			c.gridwidth = 1;
			addComponent(this, new JLabel("Predict ahead for ", SwingConstants.RIGHT), c);
			c.gridx++;
			addComponent(this, predictionTime = new JTextField(6), c);
			c.gridx++;
			addComponent(this, new JLabel(" seconds ", SwingConstants.LEFT), c);


		}
		public void setParams() {
			showExclusion.setSelected(airgunParameters.showExclusionZone);
			exRadius.setText(String.format("%d", airgunParameters.exclusionRadius));
			setColour(airgunParameters.exclusionColor);
			predictAhead.setSelected(airgunParameters.predictAhead);
			predictionTime.setText(String.format("%d", airgunParameters.secondsAhead));
			enableControls();
		}
		public void setColour(Color exColour) {
			airgunParameters.exclusionColor = exColour;
			exPanel.setBackground(exColour);
		}
		public boolean getParams() {
			airgunParameters.showExclusionZone = showExclusion.isSelected();
			airgunParameters.predictAhead = predictAhead.isSelected();
			if (airgunParameters.showExclusionZone) {
				try {
					airgunParameters.exclusionRadius = Integer.valueOf(exRadius.getText());
					airgunParameters.secondsAhead = Integer.valueOf(predictionTime.getText());
				}
				catch (NumberFormatException ex) {
					return false;
				}
			}
			return true;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == exButton) {
				Color newColor = JColorChooser.showDialog(this, "Mitigation zone colour",
						airgunParameters.exclusionColor);
				if (newColor != null) {
					setColour(newColor);
				}
			}
			else if (e.getSource() == showExclusion) {
				enableControls();
			}
			else if (e.getSource() == predictAhead) {
				enableControls();
			}

		}
		private void enableControls() {
			predictAhead.setEnabled(showExclusion.isSelected());
			exRadius.setEnabled(showExclusion.isSelected());
			exButton.setEnabled(showExclusion.isSelected());
			predictionTime.setEnabled(predictAhead.isSelected() && showExclusion.isSelected());
		}
	}

}
