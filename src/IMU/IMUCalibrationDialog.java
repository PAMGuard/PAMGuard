package IMU;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.panel.PamPanel;

/**
 * Dialog box for IMU calibration values. 
 * @author Jamie Macaulay
 *
 */
public class IMUCalibrationDialog extends PamDialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static IMUCalibrationDialog singleInstance; 
	private IMUParams params; 
	//components
	private JTextField heading;
	private JTextField pitch;
	private JTextField tilt;
	private Window frame; 

	public IMUCalibrationDialog(Window parentFrame) {
		super(parentFrame, "IMU Calibration", false);
		
		setDialogComponent(getPanel());
	}
	
	private PamPanel getPanel(){
		
		PamPanel panel=new PamPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Set Calibration Values"));
		
		GridBagConstraints c= new GridBagConstraints();
		//ensure components are aligned nicely
		c.fill = GridBagConstraints.HORIZONTAL;
		//need some space between components
		c.insets = new Insets(2,2,2,2);

		c.gridx = 0;
		c.gridy = 0;
		addComponent(panel, new JLabel("Heading "),c);
		c.gridx++;
		addComponent(panel, heading = new JTextField("",4),c);
		c.gridx++;
		addComponent(panel, new JLabel(""+(char) 0x00B0),c);
		
		c.gridx = 0;
		c.gridy = 1;
		addComponent(panel, new JLabel("Pitch "),c);
		c.gridx++;
		addComponent(panel, pitch = new JTextField("",4),c);
		c.gridx++;
		addComponent(panel, new JLabel(""+(char) 0x00B0),c);
		
		c.gridx = 0;
		c.gridy = 2;
		addComponent(panel, new JLabel("Tilt "),c);
		c.gridx++;
		addComponent(panel, tilt = new JTextField("",4),c);
		c.gridx++;
		addComponent(panel, new JLabel(""+(char) 0x00B0),c);
	
		return panel;
	}
	
	public static IMUParams showDialog(Window frame, IMUParams params){
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new IMUCalibrationDialog(frame);
		}
		singleInstance.frame=frame;
		singleInstance.params = params.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	@Override
	public boolean getParams() {
		
		try{
			Double headingVal=Double.valueOf(heading.getText());
			Double pitchVal=Double.valueOf(pitch.getText());
			Double tiltVal=Double.valueOf(tilt.getText());
			
			params.headingCal=Math.toRadians(headingVal);
			params.pitchCal=Math.toRadians(pitchVal);
			params.tiltCal=Math.toRadians(tiltVal);
			
		}
		catch (Exception e){
			PamDialog.showWarning(frame, "Invalid value", "One or more fields invalid");
			return false; 
		}
		
		return true;
	}
	
	public void setParams(){
		
		heading.setText(String.valueOf(Math.toDegrees(params.headingCal)));
		pitch.setText(String.valueOf(Math.toDegrees(params.pitchCal)));
		tilt.setText(String.valueOf(Math.toDegrees(params.tiltCal)));
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
