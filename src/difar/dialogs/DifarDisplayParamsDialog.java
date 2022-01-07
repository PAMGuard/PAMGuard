/**
 * 
 */
package difar.dialogs;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import PamController.UsedModuleInfo;
import difar.DifarControl;
import difar.DifarParameters;
import PamView.dialog.PamDialog;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;

/**
 * @author gw
 *
 */
public class DifarDisplayParamsDialog extends PamDialog{

	
	private DifarControl difarControl;
	private DifarParameters difarParameters;
	private static DifarDisplayParamsDialog singleInstance;
	
	JCheckBox amplitudeScaledLineLength;
	ButtonGroup rangeType;
	JRadioButton fixedRange, geometricRange, cylindricalRange;
	JTextField defaultLength;
	JTextField sourceLevel, maxSourceLevel;
	JTextField spreadingFactor;
	JTextField cylindricalStartDistance;
	JTextField minimumOpacity; // Percent minimum opacity (0=fully transparent);
	
	JCheckBox timeScaledOpacity, useMaxSourceLevel;
	JTextField timeToFade;
	
	JCheckBox showVessels;
	private JTextField lineWidth;
	
	/**
	 * @param parentFrame
	 * @param title
	 * @param hasDefault
	 */
	private DifarDisplayParamsDialog(Window parentFrame, DifarControl difarControl) {
		super(parentFrame, "Difar Display Parameters", true);
		this.difarControl=difarControl;
		
		JPanel dispPanel=new JPanel(new GridBagLayout());
		dispPanel.setBorder(new TitledBorder("Display Options"));
		amplitudeScaledLineLength = new JCheckBox();
		amplitudeScaledLineLength.setName("Bearing line length");
		amplitudeScaledLineLength.setToolTipText("Make louder difar detections have shorter lines");
		
		JPanel rangePanel = new PamBorderPanel(new GridBagLayout());
//		rangePanel.setBorder(new TitledBorder("Length of bearing lines"));
		rangePanel.setName("Propagation model for line length");
		rangeType = new ButtonGroup();
		rangeType.add(fixedRange = new JRadioButton("Fixed"));
		rangeType.add(geometricRange = new JRadioButton("Geometric"));
		rangeType.add(cylindricalRange = new JRadioButton("Cylindrical"));

		rangePanel.add(fixedRange);
		rangePanel.add(geometricRange);
		rangePanel.add(cylindricalRange);
		
		defaultLength = new JTextField();
		defaultLength.setName("Default length");
		defaultLength.setToolTipText("Default line length to show for difar localisations on the map in meters");
		
		sourceLevel = new JTextField(5);
		sourceLevel.setName("Nominal source level for calls");
		sourceLevel.setToolTipText("Maximum source level used to determine the length of bearing lines on the map display");
		
		maxSourceLevel = new JTextField(5);
		maxSourceLevel.setName("Max source level for calls");
		maxSourceLevel.setToolTipText("Maximum source level used to determine the start of bearing lines");
		
		
		useMaxSourceLevel = new JCheckBox("Use minimum source level");
		useMaxSourceLevel.setToolTipText("If checked, bearing lines are only drawn between minimum and nominal source level instead of originating from the sonobuoy.");

		spreadingFactor = new JTextField(5);
		spreadingFactor.setName("Geometric spreading factor");
		spreadingFactor.setToolTipText("Nominal geometric spreading factor Xlog(r) used to determine the length of bearing lines on the map display");
		
		cylindricalStartDistance = new JTextField(5);
		cylindricalStartDistance.setName("Cylindrical spreading start");
		cylindricalStartDistance.setToolTipText("Nominal distance (m) at which cylindrical propagation loss starts 10log(r)-10log(X). Used to determine the length of bearing lines on the map display");
		
		lineWidth = new JTextField();
		lineWidth.setName("Line width (px)");
		lineWidth.setToolTipText("Width of bearing lines (in pixels)");
		
		
		timeScaledOpacity = new JCheckBox();
		timeScaledOpacity.setName("Fade bearings");
		timeScaledOpacity.setToolTipText("Fade older difar detections");
		
		timeToFade = new JTextField();
		timeToFade.setName("Time to Fade (m)");
		timeToFade.setToolTipText("Time for a detection to fade completely");
		
		minimumOpacity = new JTextField();
		minimumOpacity.setName("Minimum Opacity (%)");
		minimumOpacity.setToolTipText("0 is completely transparent; 100 is fully opaque. ");

		showVessels = new JCheckBox();
		showVessels.setName("Show Vessels");
		showVessels.setToolTipText("Show Vessel calibration data");
		
		ActionListener enabler= new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		};
		
		fixedRange.addActionListener(enabler);
		geometricRange.addActionListener(enabler);
		cylindricalRange.addActionListener(enabler);
		amplitudeScaledLineLength.addActionListener(enabler);
		timeScaledOpacity.addActionListener(enabler);
		minimumOpacity.addActionListener(enabler);
		useMaxSourceLevel.addActionListener(enabler);
		JComponent[] ary = {
				rangePanel,
				defaultLength,
//				amplitudeScaledLineLength,
				sourceLevel,
				maxSourceLevel,
				useMaxSourceLevel,
				spreadingFactor,
				cylindricalStartDistance,
				lineWidth,
				timeScaledOpacity,
				timeToFade, 
				minimumOpacity,
				showVessels};
		
		PamPanel.layoutGrid(dispPanel, ary);
		setDialogComponent(dispPanel);
		
	}
	
	public static final DifarParameters showDialog(Window frame, DifarControl difarControl, DifarParameters difarParameters) {
		if (singleInstance == null || singleInstance.difarControl != difarControl || singleInstance.getOwner() != frame) {
			singleInstance = new DifarDisplayParamsDialog(frame, difarControl);
		}
		singleInstance.difarParameters = difarParameters;
		singleInstance.setParams(difarParameters);
//		singleInstance.enableControls();
		singleInstance.pack();
		singleInstance.enableControls();
		singleInstance.setVisible(true);
		return singleInstance.difarParameters;
	}
	
	/* (non-Javadoc)
	 * @see PamView.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {
		try {
//			difarParameters.amplitudeScaledLineLength = amplitudeScaledLineLength.isSelected();
			if (fixedRange.isSelected())
				difarParameters.propLossModel = DifarParameters.PROPLOSS_NONE;
			else if (geometricRange.isSelected())
				difarParameters.propLossModel = DifarParameters.PROPLOSS_GEOMETRIC;
			else if (cylindricalRange.isSelected())
				difarParameters.propLossModel = DifarParameters.PROPLOSS_CYLINDRICAL;
				
			difarParameters.defaultLength=new Double(defaultLength.getText());
			
			difarParameters.nominalSourceLevel = new Double(sourceLevel.getText());
			difarParameters.maxSourceLevel = new Double(maxSourceLevel.getText());
			difarParameters.useMaxSourceLevel = useMaxSourceLevel.isSelected();
			difarParameters.nominalSpreading = new Double(spreadingFactor.getText());
			difarParameters.cylindricalStartDistance = new Double(cylindricalStartDistance.getText());
			difarParameters.bearingLineWidth = new Float(lineWidth.getText());
			difarParameters.timeScaledOpacity = timeScaledOpacity.isSelected();
			difarParameters.timeToFade = new Integer(timeToFade.getText());
			difarParameters.minimumOpacity = (new Integer(minimumOpacity.getText())*255/100);
			
			difarParameters.showVesselBearings = showVessels.isSelected();
			
		}catch(NumberFormatException e){
			return showWarning("Bad parameter in DIFAR display dialog");
		}
		return true;
	}
	
	private void setParams(DifarParameters difarParams) {
		if (difarParams == null) {
			restoreDefaultSettings();
			return;
		}
//		amplitudeScaledLineLength.setSelected(difarParams.amplitudeScaledLineLength);
		if (difarParameters.propLossModel == DifarParameters.PROPLOSS_NONE)
			fixedRange.setSelected(true);
		else if (difarParameters.propLossModel == DifarParameters.PROPLOSS_GEOMETRIC)
			geometricRange.setSelected(true);
		else if (difarParameters.propLossModel == DifarParameters.PROPLOSS_CYLINDRICAL)
			cylindricalRange.setSelected(true);
		
		defaultLength.setText(new Double(difarParams.defaultLength).toString());
		
		sourceLevel.setText(String.format("%3.1f", difarParams.nominalSourceLevel));
		maxSourceLevel.setText(String.format("%3.1f", difarParams.maxSourceLevel));
		useMaxSourceLevel.setSelected(difarParams.useMaxSourceLevel);
		spreadingFactor.setText(String.format("%3.1f", difarParams.nominalSpreading));
		cylindricalStartDistance.setText(String.format("%4.0f", difarParams.cylindricalStartDistance));
		lineWidth.setText(String.format("%2.1f", difarParams.bearingLineWidth));
		timeScaledOpacity.setSelected(difarParams.timeScaledOpacity);
		timeToFade.setText(new Long(difarParams.timeToFade).toString());
		minimumOpacity.setText(new Integer(difarParams.minimumOpacity*100/255).toString());
		
		showVessels.setSelected(difarParams.showVesselBearings);
	}
	
	
	
	void enableControls(){
		
//		defaultLength.setEnabled(!amplitudeScaledLineLength.isSelected());
		defaultLength.setEnabled(fixedRange.isSelected());
		sourceLevel.setEnabled(!fixedRange.isSelected());
		spreadingFactor.setEnabled(geometricRange.isSelected());
		cylindricalStartDistance.setEnabled(cylindricalRange.isSelected());
		timeToFade.setEnabled(timeScaledOpacity.isSelected());
		minimumOpacity.setEnabled(timeScaledOpacity.isSelected());
		maxSourceLevel.setEnabled(useMaxSourceLevel.isSelected());

	}
	
	/* (non-Javadoc)
	 * @see PamView.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {
		difarParameters = null;
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		DifarParameters newDifarParameters = new DifarParameters();
		setParams(newDifarParameters);
	}
	
}
