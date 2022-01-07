package radardisplay;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Map.MapDetectionData;
import PamController.PamController;
import PamView.GeneralProjector.ParameterType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectDialog;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

public class RadarParametersDialog extends PamDialog {

	private static RadarParameters radarParameters;
	private static RadarParametersDialog singleInstance;
	StylePanel stylePanel;
	ScalePanel scalePanel;
	RadarProjector radarProjector;
	DetectorsPanel detectorsPanel;
	public int maxRadialIndex;
	private RadarDisplay parentRadar;
	
	private RadarParametersDialog(RadarDisplay parentRadar, Frame parentFrame) {
		
		super(parentFrame, "Radar Display options", true);
		this.parentRadar = parentRadar;
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Scales", new FirstTab());
		detectorsPanel = new DetectorsPanel();
		tabbedPane.add("Detectors", detectorsPanel);
		setDialogComponent(tabbedPane);
		
		setHelpPoint("displays.radarDisplayHelp.docs.UserDisplay_Radar_Configuring");
		
	}
	
	public static RadarParameters showDialog(RadarDisplay parentRadar, Frame parentFrame, 
			RadarParameters radarParameters, RadarProjector radarProjector) {
		
		if (singleInstance == null || parentRadar != singleInstance.parentRadar || parentFrame != singleInstance.getOwner()) {
			singleInstance = new RadarParametersDialog(parentRadar, parentFrame);
		}
		RadarParametersDialog.radarParameters = radarParameters.clone();
		singleInstance.radarProjector = radarProjector;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return RadarParametersDialog.radarParameters;
	}
	
	void setParams() {
		stylePanel.setParams();
		scalePanel.setParams();
		detectorsPanel.setParams();
	}

	@Override
	public void cancelButtonPressed() {
		
		radarParameters = null;
		
	}

	@Override
	public boolean getParams() {
		if (stylePanel.getParams() == false) return false;
		if (scalePanel.getParams() == false) return false;
		if (detectorsPanel.getParams() == false) return false;
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		RadarParametersDialog.radarParameters = new RadarParameters();
		
		setParams();
		
	}

	private boolean showDataSelectDialog(PamDataBlock radarData, DataSelector ds, PamSymbolChooser symbolChooser) {
		DataSelectDialog dataSelectDialog = new DataSelectDialog(this.getOwner(), radarData, ds, symbolChooser);
		boolean ok = dataSelectDialog.showDialog();
		//	selectDialogToOpen();
		//	boolean ok = dataSelectDialog.showDialog();
		//	selectDialogClosed(ok);
		return ok;
	}
	
	class FirstTab extends JPanel {

		public FirstTab() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(stylePanel = new StylePanel());
			add(scalePanel = new ScalePanel());
		}
		
	}
	class StylePanel extends JPanel {
		
		JTextField windowName;
		JComboBox sides;
		JComboBox radialAxis;
		JComboBox orientation;
		
		public StylePanel () {
			super();
						
			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			GridBagConstraints constraints = new PamGridBagContraints();
//			constraints.anchor = GridBagConstraints.WEST;
//			constraints.fill = GridBagConstraints.HORIZONTAL;
//			constraints.ipadx = 2;
//			constraints.ipady = 3;

			setBorder(new TitledBorder("Plot layout"));
			constraints.gridx = 0;
			constraints.gridy = 0;
			addComponent(this, new JLabel("Name ", JLabel.RIGHT), constraints);
			constraints.gridx ++;
			constraints.gridwidth = 2;
			addComponent(this, windowName = new JTextField(20), constraints);
			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy ++;
			addComponent(this, new JLabel("Style ", JLabel.RIGHT), constraints);
			constraints.gridx ++;
			addComponent(this, sides = new JComboBox(), constraints);

			constraints.gridx = 0;
			constraints.gridy ++;
			addComponent(this, new JLabel("Orientation ", JLabel.RIGHT), constraints);
			constraints.gridx ++;
			addComponent(this, orientation = new JComboBox(), constraints);
			orientation.addItem("Heading Up");
			orientation.addItem("North Up");
			
			
			constraints.gridx = 0;
			constraints.gridy ++;
			addComponent(this, new JLabel("Radial Axis ", JLabel.RIGHT), constraints);
			constraints.gridx ++;
			addComponent(this, radialAxis = new JComboBox(), constraints);
			radialAxis.addActionListener(new ChangeListener());
		}
		
		void setParams() {
			if (radarParameters.windowName != null)
				windowName.setText(radarParameters.windowName);
			
			sides.removeAllItems();
			sides.addItem("Full display");
			sides.addItem("Right half only");
			sides.addItem("Left half only");
			sides.addItem("Front half only");
			sides.addItem("Back half only");
			sides.setSelectedIndex(radarParameters.sides);
			
			radialAxis.removeAllItems();
			radialAxis.addItem("Amplitude scale");
			radialAxis.addItem("Distance scale");
			maxRadialIndex = 1;
			if (ArrayManager.getArrayManager().getCurrentArray().getArrayShape() >= ArrayManager.ARRAY_TYPE_PLANE) {
				radialAxis.addItem("Slant Angle");
				maxRadialIndex = 2;
			}
			if (radarParameters.radialAxis <= maxRadialIndex) {
				radialAxis.setSelectedIndex(radarParameters.radialAxis);
			}
			else {
				radialAxis.setSelectedIndex(0);
			}
			
			orientation.setSelectedIndex(radarParameters.orientation);
		}
		
		boolean getParams() {
			radarParameters.windowName = windowName.getText();
			radarParameters.sides = sides.getSelectedIndex();
			radarParameters.radialAxis = radialAxis.getSelectedIndex();
			radarParameters.orientation = orientation.getSelectedIndex();
			return true;
		}
		
		int getRadialAxisSelection() {
			return radialAxis.getSelectedIndex();
		}
	}
	
	class ChangeListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			if (scalePanel != null) {
				scalePanel.setParams();
				detectorsPanel.setParams();
			}
			
		}
		
	}
	
	class ScalePanel extends JPanel {

		TitledBorder titledBorder;
		JTextField minValue, maxValue;
		JLabel minLabel, maxLabel;
		public ScalePanel () {
			super();
						
			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.WEST;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.ipadx = 2;
			constraints.ipady = 3;

			setBorder(titledBorder = new TitledBorder("Scales"));
			constraints.gridx = 0;
			constraints.gridy = 0;
			addComponent(this, new JLabel("Min "), constraints);
			constraints.gridx ++;
			addComponent(this, minValue = new JTextField(6), constraints);
			constraints.gridx ++;
			addComponent(this, minLabel = new JLabel("m"), constraints);
			constraints.gridy++;
			constraints.gridx = 0;
			addComponent(this, new JLabel("Max "), constraints);
			constraints.gridx ++;
			addComponent(this, maxValue = new JTextField(6), constraints);
			constraints.gridx ++;
			addComponent(this, maxLabel = new JLabel("m"), constraints);
		}
		void setParams() {
			if (stylePanel == null) return;
			int axisSelection = stylePanel.getRadialAxisSelection();
			switch (axisSelection) {
			case RadarParameters.RADIAL_AMPLITIDE:
//				titledBorder.setTitle("Amplitude range");
				setBorder(titledBorder = new TitledBorder("Amplitude range"));
				minValue.setText(String.format("%d", radarParameters.rangeStartdB));
				maxValue.setText(String.format("%d", radarParameters.rangeEnddB));
				minLabel.setText(" dB");
				maxLabel.setText(" dB");
				break;
			case RadarParameters.RADIAL_DISTANCE:
//				titledBorder.setTitle("Distance range");
				setBorder(titledBorder = new TitledBorder("Distance range"));
				minValue.setText(String.format("%d", radarParameters.rangeStartm));
				maxValue.setText(String.format("%d", radarParameters.rangeEndm));
				minLabel.setText(" metres");
				maxLabel.setText(" metres");
				break;
			case RadarParameters.RADIAL_SLANT_ANGLE:
				setBorder(titledBorder = new TitledBorder("Slant Angle"));
				minValue.setText("90");
				maxValue.setText("0");
				minLabel.setText(" degrees");
				maxLabel.setText(" degrees");
				break;
			}
			enableControls();
		}
		
		void enableControls() {
			int axisSelection = stylePanel.getRadialAxisSelection();
			minValue.setEnabled(axisSelection != RadarParameters.RADIAL_SLANT_ANGLE);
			maxValue.setEnabled(axisSelection != RadarParameters.RADIAL_SLANT_ANGLE);
		}
		
		boolean getParams() {
			if (stylePanel == null) return false;
			int axisSelection = stylePanel.getRadialAxisSelection();
			try {
				switch (axisSelection) {
				case RadarParameters.RADIAL_AMPLITIDE:
					radarParameters.rangeStartdB = Integer.valueOf(minValue.getText());
					radarParameters.rangeEnddB = Integer.valueOf(maxValue.getText());
					break;
				case RadarParameters.RADIAL_DISTANCE:
					radarParameters.rangeStartm = Integer.valueOf(minValue.getText());
					radarParameters.rangeEndm = Integer.valueOf(maxValue.getText());
					break;
				}
			}
			catch (Exception Ex) {
				return false;
			}
			return true;
		}
	}
	class DetectorsPanel extends JPanel {
		
		JCheckBox[] selectDetector;
		
		JCheckBox[] fadeDetector;
		
		JTextField[] textFields;
		
		JButton[] selectButtons;

		private ArrayList<PamDataBlock> detectorDataBlocks;
		
		void setParams() {
			fillPanel();
		}
		
		boolean getParams() {
			if (selectDetector == null || textFields == null) return true;
			for (int i = 0; i < selectDetector.length; i++) {
				if (selectDetector[i] == null) {
					continue;
				}
				try {
					// use the datablock long name in the tool tip to match to the correct datablock
					RadarDataInfo rdi = radarParameters.getRadarDataInfo(selectDetector[i].getToolTipText());
					rdi.select = selectDetector[i].isSelected();
					rdi.setFadeDetector(fadeDetector[i].isSelected());
					rdi.setDetectorLifetime(Integer.valueOf(textFields[i].getText()));
					radarParameters.setRadarDataInfo(selectDetector[i].getToolTipText(), rdi);
				}
				catch (Exception Ex) {
					return false;
				}
			}
			return true;
		}
		
		void fillPanel() {

			int axisSelection = stylePanel.getRadialAxisSelection();
			if (axisSelection == RadarParameters.RADIAL_AMPLITIDE)
				radarProjector.setParmeterType(1, ParameterType.AMPLITUDE);
			else if (axisSelection == RadarParameters.RADIAL_DISTANCE)
				radarProjector.setParmeterType(1, ParameterType.RANGE);
			else if (axisSelection == RadarParameters.RADIAL_SLANT_ANGLE)
				radarProjector.setParmeterType(1, ParameterType.SLANTANGLE);
			
			
			detectorDataBlocks = 
				PamController.getInstance().getDataBlocks(PamDataUnit.class, true);
			
			this.removeAll();

			ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));

			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			GridBagConstraints constraints = new PamGridBagContraints();
			constraints.anchor = GridBagConstraints.WEST;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.ipadx = 2;
			constraints.ipady = 1;

			setBorder(new TitledBorder("Show Detector Data"));
			constraints.gridx = 0;
			constraints.gridy = 0;

			addComponent(this, new JLabel("Detector"), constraints);
			constraints.gridx++;
			addComponent(this, new JLabel("Lifetime (s)", JLabel.CENTER), constraints);
			constraints.gridx++;
			addComponent(this, new JLabel(" Fade ", JLabel.CENTER), constraints);
			
			if (detectorDataBlocks != null) {
//				if (radarParameters.showDetector == null ||
//						detectorDataBlocks.size() > radarParameters.showDetector.length) {
//					radarParameters.showDetector = new boolean[detectorDataBlocks.size()];
//					radarParameters.fadeDetector = new boolean[detectorDataBlocks.size()];
//					radarParameters.detectorLifetime = new int[detectorDataBlocks.size()];
//				}
				
				// check if there is old data that needs to be converted.  If so, convert
				if (radarParameters.isThereOnlyOldData()) {
					radarParameters.convertOldData(detectorDataBlocks);
				}
				selectDetector = new JCheckBox[detectorDataBlocks.size()];
				fadeDetector = new JCheckBox[detectorDataBlocks.size()];
				textFields = new JTextField[detectorDataBlocks.size()];
				selectButtons = new JButton[detectorDataBlocks.size()];
			}
			PamDataBlock dataBlock;
			EnableListener enableListener = new EnableListener();
			for (int i = 0; i < detectorDataBlocks.size(); i++) {
				dataBlock = detectorDataBlocks.get(i);
				if (dataBlock.canDraw(radarProjector) == false) continue;
				constraints.gridx = 0;
				constraints.gridy ++;
				constraints.anchor = GridBagConstraints.WEST;
				RadarDataInfo rdi = radarParameters.getRadarDataInfo(dataBlock);
				addComponent(this, selectDetector[i] = new JCheckBox(dataBlock.getDataName()), constraints);
				selectDetector[i].setToolTipText(dataBlock.getLongDataName()); 	// use the long data name in the tool tip text, so that we can match it back to the datablock later 
				selectDetector[i].setSelected(rdi.select);
				selectDetector[i].addActionListener(enableListener);
				constraints.gridx++;
				constraints.anchor = GridBagConstraints.CENTER;
				addComponent(this, textFields[i] = new JTextField(6), constraints);
				textFields[i].setText(String.format("%d", rdi.getDetectorLifetime()));
				constraints.gridx++;
				addComponent(this, fadeDetector[i] = new JCheckBox(""), constraints);
				fadeDetector[i].setSelected(rdi.isFadeDetector());
				constraints.gridx++;
				addComponent(this,  selectButtons[i] = new JButton(settingsIcon), constraints);
				if (dataBlock.getDataSelectCreator() != null) {
					selectButtons[i].setToolTipText("More data selection options");
				}
				selectButtons[i].addActionListener(new SelectListener(dataBlock));
			}
			enableControls();
			pack();
		}
		
		private void enableControls() {
			for (int i = 0; i < selectDetector.length; i++) {
				if (selectDetector[i] == null) {
					continue;
				}
				boolean en = (selectDetector[i].isSelected());
				if (textFields[i] == null) continue;
				textFields[i].setEnabled(en);
				fadeDetector[i].setEnabled(en);
				DataSelectorCreator dsc = detectorDataBlocks.get(i).getDataSelectCreator();
				en &= dsc != null;
				if (selectButtons[i] == null) continue;
				selectButtons[i].setEnabled(en);
			}
		}
		
		private class EnableListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
		}
		private class SelectListener implements ActionListener {
			
			PamDataBlock dataBlock;
			
			public SelectListener(PamDataBlock dataBlock) {
				super();
				this.dataBlock = dataBlock;
			}

			@Override
			public void actionPerformed(ActionEvent arg0) {
				DataSelectorCreator dsc = dataBlock.getDataSelectCreator();
				if (dsc == null || parentRadar == null) return;
				DataSelector ds = dsc.getDataSelector(parentRadar.getDataSelectTitle(), false, null);
//				ds.showSelectDialog(getOwner());
				PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
				PamSymbolChooser symbolChooser = null;
				if (symbolManager != null) {
					symbolChooser = symbolManager.getSymbolChooser(parentRadar.radarDisplayComponent.getUniqueName(), radarProjector);
				}
				if (ds == null && symbolChooser == null) {
					return;
				}
				if (showDataSelectDialog(dataBlock, ds, symbolChooser)) {
					parentRadar.newSettings();
				}
			}
		}
	}
}
