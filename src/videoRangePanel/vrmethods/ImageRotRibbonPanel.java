package videoRangePanel.vrmethods;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;

import PamView.panel.PamPanel;
import videoRangePanel.VRControl;


public class ImageRotRibbonPanel extends PamPanel{

	private VRControl vrControl;

	private JLabel imageTilt;
	private JLabel imageBearing;
	private JLabel imagePitch;
	private JLabel animalBearing;
	private JLabel animalDistance;

	public ImageRotRibbonPanel(VRControl vrControl){
		
		this.vrControl=vrControl; 
		this.setLayout(new BorderLayout());
		
		PamPanel imageInfo=new PamPanel(new FlowLayout());
		
		imageInfo.add(imageBearing=new JLabel("Image Bearing: "));	
		imageInfo.add(imagePitch=new JLabel("Image Pitch:"));	
		imageInfo.add(imageTilt=new JLabel("Image Tilt: "));

		
		imageInfo.add(new JLabel("     "));
		imageInfo.add(new JLabel("     "));
		
		//show current animal location
		imageInfo.add(animalBearing=new JLabel("Location Bearing: "));	
		imageInfo.add(animalDistance=new JLabel("Distance: "));
		
		this.add(BorderLayout.WEST, imageInfo);
		this.setPreferredSize(new Dimension(1,35));
		
	}
	
	public JLabel getImageTilt() {
		return imageTilt;
	}

	public JLabel getImageBearing() {
		return imageBearing;
	}

	public JLabel getImagePitch() {
		return imagePitch;
	}

	/**
	 * Set image tilt text
	 * @param imageTiltVal- image tilt in radians
	 * @param imageTiltError-image tilt error in radians
	 */
	public void setImageTilt(Double imageTiltVal, Double imageTiltError) {
		if (imageTiltVal==null || imageTiltError==null) imageTilt.setText("Image Tilt: ");
		else imageTilt.setText(String.format("Image Tilt=%.2f\u00B0: ",imageTiltVal)+  String.format("\u00B1 %.2f\u00B0: ",imageTiltError));
	}

	public void setImageBearing(Double imageBearingVal, Double imageBearingError) {
		if (imageBearingVal==null || imageBearingError==null) imageBearing.setText("Image Bearing: ");
		else imageBearing.setText(String.format("Image Bearing=%.2f\u00B0: ",imageBearingVal)+ String.format("\u00B1 %.2f\u00B0: ",imageBearingError));
	}

	public void setImagePitch(Double imagePitchVal, Double imagePitchError) {
		if (imagePitchVal==null || imagePitchError==null) imagePitch.setText("Image Pitch: ");
		else imagePitch.setText(String.format("Image Pitch=%.2f\u00B0: ",imagePitchVal)+ String.format("\u00B1 %.2f\u00B0: ",imagePitchError));
	}
	
	public void clearImageLabels(){
		setImageBearing(null,null);
		setImagePitch(null,null);
		setImageTilt(null,null);
	}
	
	public JLabel getAnimalBearing() {
		return animalBearing;
	}

	public void setAnimalBearing(Double animalBearingVal, Double anBearingError) {
		if (animalBearing==null || anBearingError==null) animalBearing.setText("Location Bearing: ");
		else animalBearing.setText(String.format("Location Bearing=%.2f\u00B0: ",animalBearingVal)+ String.format("\u00B1 %.2f\u00B0: ",anBearingError));
	}

	public JLabel getAnimalDistance() {
		return animalDistance;
	}

	public void setAnimalDistance(Double animalDistanceVal, Double animalDistanceEr) {
		if (animalDistance==null || animalDistanceEr==null) animalDistance.setText(" Distance: ");
		else animalDistance.setText(String.format(" Distance=%.2f: ",animalDistanceVal)+ String.format("\u00B1 %.2f: ",animalDistanceEr));

	}
	
	public void clearAnimalLabels(){
		setAnimalBearing(null,null);
		setAnimalDistance(null,null);
	}

}
