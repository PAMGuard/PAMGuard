package pamViewFX.fxNodes.sliders;

import pamViewFX.fxNodes.sliders.skin.PamSliderSkin;
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

public class PamSlider extends Slider {

	private PamSliderSkin sliderSkin;

	public PamSlider(double min, double max, double value) {
		super(min, max, value);
		createDefaultSkin(); 
	}
	
	@Override
	protected Skin<?> createDefaultSkin() {
		if (sliderSkin==null) return sliderSkin=new PamSliderSkin(this);
		else return sliderSkin;
	}

	/**
	 * Set the colour of the slider track. 
	 * @param colour - colour to set the slider track to. 
	 */
	public void setTrackColor(Color colour){
		if (this.sliderSkin==null) createDefaultSkin(); 
		sliderSkin.setTrackColor(colour);
	
	}

	/**
	 * Set the colour of the slider track. 
	 * @param colour- string value of colour to set the slider track to. 
	 */
	public void setTrackColour(String colour) {
		setTrackColor(Color.valueOf(colour));
	}

}
