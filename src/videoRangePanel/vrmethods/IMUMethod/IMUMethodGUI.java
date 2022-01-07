package videoRangePanel.vrmethods.IMUMethod;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import PamView.PamColors;
import PamView.PamSymbol;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import angleMeasurement.AngleDataUnit;
import videoRangePanel.VRMeasurement;
import videoRangePanel.layoutAWT.VRPanel;
import videoRangePanel.layoutAWT.VRParametersDialog;
import videoRangePanel.layoutAWT.VRSidePanel;
import videoRangePanel.vrmethods.AbstractVRGUIAWT;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.ImageRotRibbonPanel;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkGUIAWT;


public class IMUMethodGUI extends AbstractVRGUIAWT {

	//components
	private JButton clearLM;
	private PamPanel sidePanel;
	private PamLabel instruction;
	private ImageRotRibbonPanel ribbonPanel;
	private JTextField imuDatText; 
	//labels showing the Euler angles of the image. 
	private JLabel imageHeadingLabel;
	private JLabel imagePitchLabel;
	private JLabel imageTiltLabel;

	/**
	 * The imu methopd
	 */
	private IMUMethod imuMethod;


	public IMUMethodGUI(IMUMethod imuMethod) {
		super(imuMethod);
		this.imuMethod=imuMethod; 
		this.sidePanel=createSidePanel();
//		this.imuLayerOverlay=new IMUMarkMethodUI(vrControl);
		this.ribbonPanel=new ImageRotRibbonPanel(vrControl);
	}

	public PamPanel createSidePanel(){
		PamLabel lab;
		PamPanel panel=new PamPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx=0;
		c.gridy=0;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, lab=new PamLabel("IMU Method "), c);
		lab.setFont(PamColors.getInstance().getBoldFont());

		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, createCalibrationList(), c);

		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, createIMUImportPanel(), c);


		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, imageHeadingLabel=new JLabel(), c);

		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, imagePitchLabel=new JLabel(), c);

		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, imageTiltLabel=new JLabel(), c);

		c.insets = new Insets(15,0,0,0);  //top padding

		c.gridy++;
		PamDialog.addComponent(panel, instruction = new PamLabel("...."), c);
		instruction.setFont(PamColors.getInstance().getBoldFont());

		c.insets = new Insets(0,0,0,0);  //no padding


		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, clearLM = new JButton("Clear Marks"), c);
		clearLM.addActionListener(new ClearMarks());
		c.gridy++;
		c.gridy++;

		newCalibration();
		setIMUText(); 

		return panel;
	}


	private PamPanel createIMUImportPanel(){

		PamPanel panel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy++;
		PamDialog.addComponent(panel, new PamLabel("IMU File"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridy++;
		c.gridwidth = 3;
		PamDialog.addComponent(panel, imuDatText=new JTextField(10), c);
		imuDatText.setEditable(false);
		imuDatText.setText("No IMU Data: ");
		c.weightx = 0;
		c.gridx =3;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, imuSettings=new JButton(VRSidePanel.settings), c);
		imuSettings.setPreferredSize(VRPanel.settingsButtonSize);
		imuSettings.addActionListener(new SelectIMUData());

		return panel;
	}
	
	
	@Override
	public void mouseAction(MouseEvent e, boolean motion) {
		if (e.getID() == MouseEvent.MOUSE_CLICKED){
			//			if (e.getButton() == MouseEvent.BUTTON1) {
			switch (imuMethod.getCurrentStatus()) {
			case AbstractVRMethod.MEASURE_ANIMAL:
				imuMethod.measureAnimal(vrControl.getVRPanel().screenToImage(e.getPoint()));
				break; 

			}
			vrControl.getVRPanel().repaint();
		}
		
	}

	/**
	 * Set instruction. 
	 */
	public void setInstruction(){
		if (imuMethod.getCurrentIMUData()==null) instruction.setText("Add IMU data ");
		if (imuMethod.getCurrentIMUData()!=null) instruction.setText("Click Animal");
		super.setInstruction(instruction);
	}

	/**
	 * Set the text for the number of IMU data units found for the image and the average values of these measurments. 
	 */
	private void setIMUText() {
		if (imuMethod.getCurrentIMUData()==null){ 
			imuDatText.setText("No IMU Data: ");
			imageHeadingLabel.setText("Heading: "+"-"+(char) 0x00B0);
			imagePitchLabel.setText("Pitch:   "+"-"+(char) 0x00B0);
			imageTiltLabel.setText("Tilt:    "+"-"+(char) 0x00B0);
		}
		else{ 
			imuDatText.setText("Found: "+ imuMethod.getCurrentIMUData().getNUnits()+" IMU units in" + vrControl.getIMUListener().getIMUDataBlock().getLoggingName());
			imageHeadingLabel.setText("Heading: "+Math.toDegrees(imuMethod.getCurrentIMUData().getTrueHeading())+(char) 0x00B0);
			imagePitchLabel.setText("Pitch:   "+Math.toDegrees(imuMethod.getCurrentIMUData().getPitch())+(char) 0x00B0);
			imageTiltLabel.setText("Tilt:    "+Math.toDegrees(imuMethod.getCurrentIMUData().getTilt())+(char) 0x00B0);
		}
		imuDatText.setCaretPosition(0);
	}
	
	public void clearRibbonPanel(){
		ribbonPanel.clearImageLabels();
		ribbonPanel.clearAnimalLabels();
	}
	

	public PamPanel getRibbonPanel() {
		return ribbonPanel;
	}
	
	public void updateLabels(){
		setImageLabels(imuMethod.getCurrentIMUData());
		setInstruction();
		setIMUText();
	}
	
	private void setImageLabels(AngleDataUnit imageMeasurement){
		if (imageMeasurement==null) ribbonPanel.clearImageLabels();
		else {
			ribbonPanel.setImageBearing(imageMeasurement.getTrueHeading(),imageMeasurement.getErrorHeading());
			ribbonPanel.setImagePitch(imageMeasurement.getPitch(),imageMeasurement.getErrorPitch());
			ribbonPanel.setImageTilt(imageMeasurement.getTilt(),imageMeasurement.getErrorTilt());
		}
	}
	
	public void setAnimalLabels(VRMeasurement imageMeasurement){
		if (imageMeasurement.locBearing==null)  ribbonPanel.clearImageLabels();
		else{
			ribbonPanel.setAnimalBearing(Math.toRadians(imageMeasurement.locBearing), Math.toRadians(imageMeasurement.locBearingError));
			ribbonPanel.setAnimalDistance(imageMeasurement.locDistance, imageMeasurement.locDistanceError);
		}
	}

	private class SelectIMUData implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			vrControl.settingsButtonAWT(null,VRParametersDialog.ANGLE_TAB);
		}
	}

	private class ClearMarks implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			imuMethod.clearOverlay() ;
		}
	}

	public PamPanel getSidePanel() {
		return this.sidePanel;
	}

	@Override
	public PamPanel getSettingsPanel() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void paintMarks(Graphics g) {
		addMeasurementMarks(g);
		addAnimals(g);

	}
	
	private void addMeasurementMarks(Graphics g) {
		drawImageTilt(g, LandMarkGUIAWT.landMarkMarker);
	}


	protected void addAnimals(Graphics g) {
		if (imuMethod.getCandidateMeasurement()!=null) drawAnimal(g, imuMethod.getCandidateMeasurement(), LandMarkGUIAWT.candAnimalMarker);
		drawAnimals(g, LandMarkGUIAWT.animalMarker);
	}   
	
	
	/**
	 * Draw the image tilt cross. 
	 * @param g-graphics handle.
	 * @param landMarkMarker
	 */
	private void drawImageTilt(Graphics g, PamSymbol tiltSymbol) {	
		if (imuMethod.getCurrentIMUData()==null) return;
		//don't draw if there are no measurements. 
		if (imuMethod.getCandidateMeasurement()==null && vrControl.getMeasuredAnimals()==null) return;
		//set the colour
		Graphics2D g2=(Graphics2D) g;
		g2.setStroke(LandMarkGUIAWT.dashedtmplate);
		g2.setColor(Color.green);
		
		Point imageScreenSize=new Point(vrControl.getVRPanel().getImageWidth(), vrControl.getVRPanel().getImageHeight());

		//create a cross showing the image tilt 		
		//bearing line;
		double yOffset=Math.tan(imuMethod.getCurrentIMUData().getTilt())*imageScreenSize.x/2;
		Point p1=new Point(0, (int) (imageScreenSize.y/2+yOffset));
		Point p2=new Point(imageScreenSize.x, (int) (imageScreenSize.y/2-yOffset));
		p1=vrControl.getVRPanel().imageToScreen(p1);
		p2=vrControl.getVRPanel().imageToScreen(p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		//pitch line
		double xOffset=Math.tan(imuMethod.getCurrentIMUData().getTilt())*imageScreenSize.y/2;
		p1=new Point((int) (imageScreenSize.x/2-xOffset), 0);
		p2=new Point((int) (imageScreenSize.x/2+xOffset), imageScreenSize.y);
		p1=vrControl.getVRPanel().imageToScreen(p1);
		p2=vrControl.getVRPanel().imageToScreen(p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	
	
	private void drawAnimals(Graphics g, PamSymbol pamSymbol) {

		//check for previous animals
		if (vrControl.getMeasuredAnimals()==null) return;
			
		for (int i=0; i<vrControl.getMeasuredAnimals().size(); i++){
			drawAnimal( g, vrControl.getMeasuredAnimals().get(i), pamSymbol); 
		}
	}
	
	@Override
	protected void drawAnimal(Graphics g, VRMeasurement vr, PamSymbol symbol) {
		
		Graphics2D g2=(Graphics2D) g;
		g2.setStroke(LandMarkGUIAWT.solid);
		//draw a line from the centre of the image to the animal
		Point imageCentre=new Point(vrControl.getVRPanel().getImageWidth()/2, vrControl.getVRPanel().getImageHeight()/2);
		//draw the animal, form the center of the image to the 
		Point p1, p2;
		symbol.draw(g, p1 = vrControl.getVRPanel().imageToScreen(vr.animalPoint));
		//get a second point at the centre of the image; 
		symbol.draw(g, p2 = vrControl.getVRPanel().imageToScreen(imageCentre));
		//line from the center of the image to the selected animal location.
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		g2.setStroke(LandMarkGUIAWT.dashedtmplate);
		//show the bearing and pitch of the animal aligned with the tilt of the image. 
		//get the animal bearing and pitch. 
		double calVal=(1/vr.calibrationData.degreesPerUnit);
		double bearingDiff=	(vr.locBearing-vr.imageBearing);
		double pitchDiff=	(vr.locPitch-vr.imagePitch);
		//show bearing on tilt line....
		double yOffset=Math.sin(Math.toRadians(vr.imageTilt))*bearingDiff*calVal;
		double xOffset=Math.cos(Math.toRadians(vr.imageTilt))*bearingDiff*calVal;
		p2=vrControl.getVRPanel().imageToScreen(new Point(new Point((int) (imageCentre.x+xOffset),(int) (imageCentre.y-yOffset))));
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		//show pitch on tilt line....
		xOffset=Math.sin(Math.toRadians(vr.imageTilt))*pitchDiff*calVal;
		yOffset=Math.cos(Math.toRadians(vr.imageTilt))*pitchDiff*calVal;
		p2=vrControl.getVRPanel().imageToScreen(new Point(new Point((int) (imageCentre.x-xOffset),(int) (imageCentre.y-yOffset))));
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	


}
