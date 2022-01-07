package angleMeasurement;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import PamController.PamControlledUnit;
import PamView.PamSidePanel;

public class AngleControl extends PamControlledUnit {

	AngleProcess angleProcess;

	AngleMeasurement angleMeasurement;
	
	AngleSidePanel angleSidePanel;
	
	private AngleControl THIS;
	
	public AngleControl(String unitName) {
		
		super("Angle Measurement", unitName);
		
		angleMeasurement = new FluxgateWorldAngles(unitName, true);
		
		angleSidePanel = new AngleSidePanel(this);

		addPamProcess(angleProcess = new AngleProcess(this));
		
		THIS = this;
		
	}

	@Override
	public PamSidePanel getSidePanel() {
		return angleSidePanel;
	}

	public void newAngle(AngleDataUnit angleDataUnit) {
		angleSidePanel.newAngle(angleDataUnit);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		JMenuItem menuItem = new JMenuItem("Settings ...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		menu.add(menuItem);
		menuItem = new JMenuItem("Calibration ...");
		menuItem.addActionListener(new CalibrationMenu(parentFrame));
		menu.add(menuItem);
		return menu;
	}
	
	class SettingsMenu implements ActionListener {
		Frame parent;
		
		public SettingsMenu(Frame parent) {
			super();
			this.parent = parent;
		}

		public void actionPerformed(ActionEvent e) {
			angleMeasurement.settings(parent);
		}
	}
	class CalibrationMenu implements ActionListener {
		Frame parent;
		
		public CalibrationMenu(Frame parent) {
			super();
			this.parent = parent;
		}

		public void actionPerformed(ActionEvent e) {
			AngleParameters newParams = AngleCalibrationDialog.showDialog(parent, THIS, angleMeasurement.getAngleParameters());
			if (newParams != null) {
				// copy the cal data over to the old params since they are probably some sub class of 
				// angleParameters, so can't cast the whole thing. 
				angleMeasurement.setCalibrationData(newParams.getCalibrationData().clone());
			}
		}
	}
	
	protected void holdButton() {
		AngleDataUnit heldAngle = angleProcess.holdAngle();
		angleSidePanel.showHeldAngle(heldAngle);
	}

}
