package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import videoRangePanel.VRControl;
import videoRangePanel.vrmethods.landMarkMethod.LandMark;
import PamUtils.LatLong;
import PamView.dialog.PamDialog;
import PamguardMVC.debug.Debug;

public class LandMarkAddDialog extends PamDialog {

	private static LandMarkAddDialog singleInstance;
	private LandMark vrLandMarkData;
	private LatLong origin;


	private JTextField name;
	private JTextField latitude;
	private JTextField longitude;
	private JTextField height;
	private VRControl vrControl;
	private JTextField bearing;
	private JTextField pitch;
	private JTextField measurementLat;
	private JTextField measurementLong;
	private JTextField measurementHeight;
	private JTabbedPane tabbedPanel;


	public LandMarkAddDialog(Window parentFrame, LandMark
			existingLandMark) {
		super(parentFrame, "Add Landmark", false);
		
		JPanel namePanel=new JPanel();
		namePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		namePanel.setBorder(new TitledBorder("Landmark name"));
		c.gridwidth=3;
		c.gridx = c.gridy = 0;
		addComponent(namePanel, name = new JTextField(20), c);

		
		JPanel mainPanel=new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, namePanel);
		
		
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Landmark GPS"));
		p.setLayout(new GridBagLayout());
		c.gridx = c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		addComponent(p, new JLabel("Landmark latitude "), c);
		c.gridx++;
		addComponent(p, latitude = new JTextField(6), c);
		c.gridx++;
		addComponent(p, new JLabel(" N decimal"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Landmark longitude"), c);
		c.gridx++;
		addComponent(p, longitude = new JTextField(6), c);
		c.gridx++;
		addComponent(p, new JLabel(" E decimal"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Landmark Height "), c);
		c.gridx++;
		addComponent(p, height = new JTextField(9), c);
		c.gridx++;
		addComponent(p, new JLabel(" m"), c);
		
		JPanel  q = new JPanel();
		q.setBorder(new TitledBorder("Landmark Angles"));
		q.setLayout(new GridBagLayout());
		c.gridx = c.gridy = 0;
		c.gridwidth = 1;
		addComponent( q, new JLabel("Landmark bearing "), c);
		c.gridx++;
		addComponent( q, bearing = new JTextField(6), c);
		c.gridx++;
		addComponent(q, new JLabel("0-360"+(char) 0x00B0), c);
		c.gridx = 0;
		c.gridy++;
		addComponent( q, new JLabel("Landmark pitch"), c);
		c.gridx++;
		addComponent( q, pitch = new JTextField(6), c);
		c.gridx++;
		addComponent( q, new JLabel("-90 > +90"+(char) 0x00B0), c);
		c.gridx=0;
		c.gridy++;
		addComponent( q, new JLabel("" +"Measurement lat "), c);
		c.gridx++;
		addComponent( q, measurementLat = new JTextField(6), c);
		c.gridx++;
		addComponent( q, new JLabel(" N decimal"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent( q, new JLabel("Measurement long"), c);
		c.gridx++;
		addComponent( q, measurementLong = new JTextField(6), c);
		c.gridx++;
		addComponent(q, new JLabel(" E decimal"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent( q, new JLabel("Measurement height "), c);
		c.gridx++;
		addComponent( q, measurementHeight = new JTextField(9), c);
		c.gridx++;
		addComponent( q, new JLabel(" m"), c);
		
		tabbedPanel = new JTabbedPane();
		tabbedPanel.addTab("GPS", p);
		tabbedPanel.addTab("Angle", q);
				
		mainPanel.add(BorderLayout.CENTER, tabbedPanel);
		
		setDialogComponent(mainPanel);
		
		pack();
	}
	
	/*
	 *Check whether a field is empty or not.  
	 */
	private boolean isEmptyString(String string){
		if (string == null || string.trim().equals("")){
			return true;
		}
		return false; 
	}
	

	@Override
	public boolean getParams() {
		
		boolean gps=false;
		boolean angle=false;
		try {
			if (name.getText()==null) return false;
			vrLandMarkData.setName(name.getText());
		}
		catch (NumberFormatException e) {
			System.out.println("No Name");
			return false;
		}
		//Do we have GPS info?
		try {
			Debug.out.println("What is the text in latitude: "+isEmptyString(latitude.getText()));
			if ( isEmptyString(latitude.getText()) &&  isEmptyString(longitude.getText()) 
					&&  isEmptyString(height.getText())){
				vrLandMarkData.setPosition(null);
			}
			else {
				LatLong position=new LatLong();
				position.setLatitude(Double.valueOf(latitude.getText()));
				position.setLongitude(Double.valueOf(longitude.getText()));
				position.setHeight(Double.valueOf(height.getText()));
				vrLandMarkData.setPosition(position);
				gps=true;
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("No GPS info");
		}
		
		//If no GPS info do we have angle info?
		try {
			if (!gps) {
				Double bearingAng=Double.valueOf(bearing.getText());
				if (bearingAng<0 || bearingAng>360) return false;
				vrLandMarkData.setBearing(bearingAng);
				Double pitchAng=Double.valueOf(pitch.getText());
				if (pitchAng<-90 || pitchAng>90) return false;
				vrLandMarkData.setPitch(pitchAng);

				LatLong positionOrigin=new LatLong();
				positionOrigin.setLatitude(Double.valueOf(measurementLat.getText()));
				positionOrigin.setLongitude(Double.valueOf(measurementLong.getText()));
				positionOrigin.setHeight(Double.valueOf(measurementHeight.getText()));
				vrLandMarkData.setLatLongOrigin(positionOrigin);
				angle=true;
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("No bearing info");
		}
		
		if (!gps && !angle){
			PamDialog.showWarning(this, "No Landmarks", " The landmark data is either incomplete or data was enterred incorrectly");
			return false; 
		}
		
		if (gps && angle){
			PamDialog.showWarning(this, "Multiple Landmark Type", " There are two entries for the landmark. "
					+ "The GPS point will be used in preference to the bearing. If you wish the bearing to be used in calculations "
					+ "delete the GPS location of the Landmark"); 
			return true; 
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
	 * @param existing landmark data id null if a new landmark is to be created. Otherwise edit a landmark. If origin is not null then that landmark is forced to have an origin latlong. 
	 * @return null if cancel pressed. Otherwise calibration data. 
	 */
	public static LandMark showDialog(Frame frame, VRControl vrControl, LandMark existingLandMark, LatLong origin) {
//		if (singleInstance == null || frame != singleInstance.getOwner()) {
			singleInstance = new LandMarkAddDialog(frame, existingLandMark);
//		}
		if (existingLandMark != null) {
			singleInstance.vrLandMarkData = existingLandMark.clone();
		}
		else{
			singleInstance.vrLandMarkData = new LandMark();
		}
		singleInstance.origin=origin;
		singleInstance.vrControl = vrControl;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.vrLandMarkData;
	}

	private void setParams() {
		
		measurementLat.setEnabled(true);
		measurementLong.setEnabled(true);
		measurementHeight.setEnabled(true); 
		
		if (vrLandMarkData.getName()!=null){
			name.setText(vrLandMarkData.getName());
		}
		if (vrLandMarkData.getPosition()!=null){
			latitude.setText(String.format("%.7f", vrLandMarkData.getPosition().getLatitude()));
			longitude.setText(String.format("%.7f", vrLandMarkData.getPosition().getLongitude()));
			height.setText(String.format("%.2f", vrLandMarkData.getHeight()));
		}
		if (vrLandMarkData.getBearing() !=null) bearing.setText(String.format("%.3f", vrLandMarkData.getBearing()));
		if (vrLandMarkData.getPitch() !=null) pitch.setText(String.format("%.3f", vrLandMarkData.getPitch()));
		if (vrLandMarkData.getLatLongOrigin() !=null){
			measurementLat.setText(String.format("%.7f", vrLandMarkData.getLatLongOrigin().getLatitude()));
			measurementLong.setText(String.format("%.7f", vrLandMarkData.getLatLongOrigin().getLongitude()));
			measurementHeight.setText(String.format("%.2f", vrLandMarkData.getLatLongOrigin().getHeight()));
		}
		
		//if origin is not null then force the user to use that as their gps origin.
		if (origin!=null){
			measurementLat.setText(String.format("%.7f", origin.getLatitude()));
			measurementLong.setText(String.format("%.7f", origin.getLongitude()));
			measurementHeight.setText(String.format("%.2f", origin.getHeight()));
			measurementLat.setEnabled(false);
			measurementLong.setEnabled(false);
			measurementHeight.setEnabled(false);
		}
		
	}
	

}
