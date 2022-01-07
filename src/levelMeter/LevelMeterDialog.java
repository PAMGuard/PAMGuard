package levelMeter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class LevelMeterDialog extends PamDialog {

	private SourcePanel sourcePanel;
	private LevelMeterControl levelMeterControl;
	private LevelMeterParams levelMeterParams; 
	private static LevelMeterDialog singleInstance;
	private JComboBox<String> scaleReference;
	private JRadioButton peak, rms;
	private JTextField scaleRange;
	
	private LevelMeterDialog(Window parentFrame, LevelMeterControl levelMeterControl) {
		super(parentFrame, levelMeterControl.getUnitName(), false);
		this.levelMeterControl = levelMeterControl;
		JPanel mainPanel = new JPanel(new BorderLayout());
		sourcePanel = new SourcePanel(this, "Raw Data Source", RawDataUnit.class, false, true);
		mainPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());
		
		JPanel scalePanel = new JPanel(new BorderLayout());
		scalePanel.setBorder(new TitledBorder("Scale selection"));
		scaleReference = new JComboBox<>();
		scalePanel.add(scaleReference, BorderLayout.CENTER);
		scaleReference.addItem("Relative to full scale");
		scaleReference.addItem("Volts");
		scaleReference.addItem("Micropascal");
		
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
		typePanel.add(peak = new JRadioButton("Peak"));
		typePanel.add(rms = new JRadioButton("RMS"));
		ButtonGroup bg = new ButtonGroup();
		bg.add(peak);
		bg.add(rms);
		scalePanel.add(typePanel, BorderLayout.NORTH);
		
		JPanel rangebit = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		rangebit.setLayout(new BoxLayout(rangebit, BoxLayout.X_AXIS));
		rangebit.add(new JLabel("Scale range ", JLabel.RIGHT));
		rangebit.add(scaleRange = new JTextField(5));
		rangebit.add(new JLabel(" dB", JLabel.LEFT));
		scalePanel.add(rangebit, BorderLayout.SOUTH);
		
		mainPanel.add(scalePanel,BorderLayout.CENTER);
		
		
		
		setDialogComponent(mainPanel);
	}
	
	public static LevelMeterParams showDialog(Window frame, LevelMeterControl levelMeterControl) {
		if (singleInstance == null || singleInstance.getOwner() != frame || singleInstance.levelMeterControl != levelMeterControl) {
			singleInstance = new LevelMeterDialog(frame, levelMeterControl);
		}
		singleInstance.levelMeterParams = levelMeterControl.levelMeterParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.levelMeterParams;
	}

	private void setParams() {
		sourcePanel.setSource(levelMeterParams.dataName);
		scaleReference.setSelectedIndex(levelMeterParams.scaleReference);
		peak.setSelected(levelMeterParams.scaleType == LevelMeterParams.DISPLAY_PEAK);
		rms.setSelected(levelMeterParams.scaleType == LevelMeterParams.DISPLAY_RMS);
		scaleRange.setText(String.format("%d", Math.abs(levelMeterParams.minLevel)));
	}

	@Override
	public boolean getParams() {
		PamDataBlock block = sourcePanel.getSource();
		if (block == null) {
			return showWarning("No data source selected");
		}
		levelMeterParams.dataName = block.getDataName();
		
		levelMeterParams.scaleReference = scaleReference.getSelectedIndex();
		levelMeterParams.scaleType = peak.isSelected() ? LevelMeterParams.DISPLAY_PEAK : LevelMeterParams.DISPLAY_RMS;
		
		try {
			levelMeterParams.minLevel = (int) (double) Double.valueOf(scaleRange.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("The scale range must have a numeric value");
		}
		levelMeterParams.minLevel = -Math.abs(levelMeterParams.minLevel); // make sure it's negative. 
		if (levelMeterParams.minLevel >= 0) {
			return showWarning("The scale range must be greater than zero");
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		levelMeterParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
