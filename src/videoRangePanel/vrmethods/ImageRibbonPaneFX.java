package videoRangePanel.vrmethods;

import PamController.PamController;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import videoRangePanel.VRControl;

/**
 * Pane which shows image angles and animal bearing and distance.  
 * @author Jamie Macaulay
 *
 */
public class ImageRibbonPaneFX extends PamBorderPane {
	

	private Label imageTilt;
	private Label imageBearing;
	private Label imagePitch;
	private Label animalBearing;
	private Label animalDistance;

	public ImageRibbonPaneFX(){
		

		PamHBox hBox = new PamHBox(); 
		hBox.setSpacing(5);
		
		hBox.getChildren().add(imageBearing=new Label("Image Bearing: - "));	
		hBox.getChildren().add(imagePitch=new Label("Image Pitch: - "));	
		hBox.getChildren().add(imageTilt=new Label("Image Tilt: - "));

		
//		hBox.getChildren().add(new Label("     "));
//		hBox.getChildren().add(new Label("     "));
		
		//show current animal location
		hBox.getChildren().add(animalBearing=new Label("Location Bearing: - "));	
		hBox.getChildren().add(animalDistance=new Label("Distance: - "));
		
		
		PamGuiManagerFX.titleFont2style(imageBearing);
		PamGuiManagerFX.titleFont2style(imagePitch);
		PamGuiManagerFX.titleFont2style(imageTilt);
		PamGuiManagerFX.titleFont2style(animalBearing);
		PamGuiManagerFX.titleFont2style(animalDistance);
		
//		imageBearing.setFont(PamGuiManagerFX.titleFontSize2);
//		imagePitch.setFont(PamGuiManagerFX.titleFontSize2);
//		imageTilt.setFont(PamGuiManagerFX.titleFontSize2);
//		animalBearing.setFont(PamGuiManagerFX.titleFontSize2);
//		animalDistance.setFont(PamGuiManagerFX.titleFontSize2);

		
		hBox.setPadding(new Insets(PamGuiManagerFX.iconSize/2,5,5,50));
		this.setCenter( hBox);
		
	}
	
	/**
	 * Get the image tilt label
	 * @return the image tilt label. 
	 */
	public Label getImageTilt() {
		return imageTilt;
	}

	
	/**
	 * Get the label which shows bearing
	 * @return the bearing label
	 */
	public Label getImageBearing() {
		return imageBearing;
	}

	/**
	 * Get the label for pitch
	 * @return the pitch label
	 */
	public Label getImagePitch() {
		return imagePitch;
	}

	/**
	 * Set image tilt text
	 * @param imageTiltVal- image tilt in degrees
	 * @param imageTiltError-image tilt error in degrees
	 */
	public void setImageTilt(Double imageTiltVal, Double imageTiltError) {
		if (imageTiltVal==null || imageTiltError==null) imageTilt.setText("Image Tilt: - ");
		else imageTilt.setText(String.format("Image Tilt=%.2f\u00B0: ",imageTiltVal)+  String.format("\u00B1 %.2f\u00B0: ",imageTiltError));
	}

	
	/**
	 * Set the image pitch text.
	 * @param imageBearingVal - the pitch of the image (middle of image) in degrees
	 * @param imageBearingError - the error pitch of the image (middle of image) in degrees
	 */
	public void setImageBearing(Double imageBearingVal, Double imageBearingError) {
		if (imageBearingVal==null || imageBearingError==null) imageBearing.setText("Image Bearing: - ");
		else imageBearing.setText(String.format("Image Bearing=%.2f\u00B0: ",imageBearingVal)+ String.format("\u00B1 %.2f\u00B0: ",imageBearingError));
	}

	/**
	 * Set the image pitch text.
	 * @param imagePitchVal - the pitch of the image (middle of image) in degrees
	 * @param imagePitchVal - the error pitch of the image (middle of image) in degrees
	 */
	public void setImagePitch(Double imagePitchVal, Double imagePitchError) {
		if (imagePitchVal==null || imagePitchError==null) imagePitch.setText("Image Pitch: - ");
		else imagePitch.setText(String.format("Image Pitch=%.2f\u00B0: ",imagePitchVal)+ String.format("\u00B1 %.2f\u00B0: ",imagePitchError));
	}
	
	
	/**
	 * Clear all values from the labels. 
	 */
	public void clearImageLabels(){
		setImageBearing(null,null);
		setImagePitch(null,null);
		setImageTilt(null,null);
	}
	
	public Label getAnimalBearing() {
		return animalBearing;
	}

	/**
	 * Set the animal bearing. 
	 * @param animalBearingVal - the animal bearing from the image source in degrees
	 * @param anBearingError - the error in the animal bearing in degrees. 
	 */
	public void setAnimalBearing(Double animalBearingVal, Double anBearingError) {
		if (animalBearing==null || anBearingError==null) animalBearing.setText("Location Bearing: - ");
		else animalBearing.setText(String.format("Location Bearing=%.2f\u00B0: ",animalBearingVal)+ String.format("\u00B1 %.2f\u00B0: ",anBearingError));
	}

	/**
	 * Get the animal distance label.
	 * @return the animals distance label
	 */
	public Label getAnimalDistance() {
		return animalDistance;
	}

	/**
	 * Set the animal distance label
	 * @param animalDistanceVal - the distance in meters
	 * @param animalDistanceEr - the distance error in meters
	 */
	public void setAnimalDistance(Double animalDistanceVal, Double animalDistanceEr) {
		if (animalDistance==null || animalDistanceEr==null) animalDistance.setText(" Distance: - ");
		else animalDistance.setText(String.format(" Distance=%.2f: ",animalDistanceVal)+ String.format("\u00B1 %.2f: ",animalDistanceEr));

	}
	
	
	/**
	 * Clear all the labels
	 */
	public void clearAnimalLabels(){
		setAnimalBearing(null,null);
		setAnimalDistance(null,null);
	}

}
