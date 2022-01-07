package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import videoRangePanel.VRControl;
import videoRangePanel.vrmethods.landMarkMethod.LandMark;
import PamUtils.LatLong;
import PamView.dialog.PamDialog;

public class ManualLocationDialog extends PamDialog {


	private static ManualLocationDialog singleInstance;
	private LandMark vrLandMarkData;

	private JTextField name;
	private JTextField latitude;
	private JTextField longitude;

	private VRControl vrControl;

	public ManualLocationDialog(Window parentFrame, LandMark
			existingLandMark) {
		super(parentFrame, "Add Manual Image Location", false);
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Location information"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		c.gridwidth = 3;
		addComponent(p, name = new JTextField(20), c);
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Image latitude "), c);
		c.gridx++;
		addComponent(p, latitude = new JTextField(6), c);
		c.gridx++;
		addComponent(p, new JLabel(" N decimal"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Image longitude"), c);
		c.gridx++;
		addComponent(p, longitude = new JTextField(6), c);
		c.gridx++;
		addComponent(p, new JLabel(" E decimal"), c);
		c.gridx = 0;
		c.gridy++;

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.NORTH, p);
				
		setDialogComponent(panel);
		
		pack();
	}
	

	@Override
	public boolean getParams() {
		try {
			if (name.getText()==null) return false;
			vrLandMarkData.setName(name.getText());
			LatLong position=new LatLong();
			position.setLatitude(Double.valueOf(latitude.getText()));
			position.setLongitude(Double.valueOf(longitude.getText()));
			vrLandMarkData.setPosition(position);
		}
		catch (NumberFormatException e) {
			System.out.println("ManualLocationDialog: format exception.");
//			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		vrLandMarkData=null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param frame owner window
	 * @param vrControl VR controller
	 * @param existing land mark null if new landmark to be created, otherwise a landmark to be edited. Not null if it's from the edit of 
	 * new buttons on the main VR dialog. 
	 * @return null if cancel pressed. Otherwise landmark data. 
	 */
	public static LandMark showDialog(Frame frame, VRControl vrControl, LandMark existingLandMark) {
//		if (singleInstance == null || frame != singleInstance.getOwner()) {
			singleInstance = new ManualLocationDialog(frame, existingLandMark);
//		}
			
		if (existingLandMark != null) {
			singleInstance.vrLandMarkData = existingLandMark.clone();
		}
		else{
			singleInstance.vrLandMarkData = new LandMark();
		}
		singleInstance.vrControl = vrControl;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.vrLandMarkData;
	}

	private void setParams() {
		
//		System.out.println("Manual GPS info: "+vrLandMarkData.getName()+ "   "+ vrLandMarkData.getPosition());
		if (vrLandMarkData.getName()!=null && vrLandMarkData.getPosition()!=null){
			name.setText(vrLandMarkData.getName());
			latitude.setText(String.format("%.4f", vrLandMarkData.getPosition().getLatitude()));
			longitude.setText(String.format("%.4f", vrLandMarkData.getPosition().getLongitude()));
		}
	}

}
