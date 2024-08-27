package pamViewFX.symbol;

import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

/**
 * Color picket which allows users to set a default opacity for the default colours selected by a user. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamColorPicker extends ColorPicker {
	
	private double defaultOpacity = 1.; 
	
	private  boolean lastColDefault = true;
	
	
	public PamColorPicker() {
		
		//A bit of a HACK - if a user selects a new colour then the opacity is set to defaultOpacity rather than 1. 
		//If the new colour already has the opacity set by the user then nothing happens.  
		this.valueProperty().addListener((obsVal, oldVal, newVal)->{
			
//			System.out.println("New colour default opacity: " + defaultOpacity + "  " +newVal.getOpacity() + "  " + (Math.abs(newVal.getOpacity()-defaultOpacity)));
			
//			if (oldVal.getRed()!=newVal.getRed() || oldVal.getGreen()!=newVal.getGreen() || oldVal.getBlue()!=newVal.getBlue()) {
				//we have a new colour - let's set the opacity
				if (newVal.getOpacity()==1.) {
					//change the opacity to default. Other values of opacity indicate the user has changed
					PamColorPicker.this.setValue(Color.color(newVal.getRed(), newVal.getGreen(), newVal.getBlue(), defaultOpacity));
					lastColDefault=true;
				}
				else if (Math.abs(newVal.getOpacity()-defaultOpacity)>(0.5/255)) {
					lastColDefault=false;
				}
//			}
		});		
	}
	
	/**
	 * Set the default opacity of a colour when selected. 
	 */
	public void setDefaultOpacity(double opacity) {
				
//		System.out.println("Set default opacity: " + opacity + " currently default:? " + lastColDefault + "  " + this.getValue().getOpacity());
		
		this.defaultOpacity=opacity; 
		
		//when it is set, then change the current colour if it the default opacity only - otherwise keep user specific settings. 
		if (lastColDefault) {
			PamColorPicker.this.setValue(Color.color(this.getValue().getRed(), this.getValue().getGreen(), this.getValue().getBlue(), defaultOpacity));
		}
		
	}
	
	public double getDefaultOpacity() {
		return defaultOpacity;
	}


}
