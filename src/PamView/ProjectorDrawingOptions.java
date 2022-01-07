package PamView;

import java.awt.Color;

/**
 * Options that can be added to the GeneralProjector class so that whatever draws
 * the data units can pick up this additional information. <br>
 * Currently, this is handling fading of lines and symbols over time for the 
 * map but may be extended to include other options. 
 * @author Doug Gillespie
 *
 */
public abstract class  ProjectorDrawingOptions {

	/**
	 * Get the opacity for lines. 
	 * @return the opacity for lines (1 = solid, 0 = transparent). 
	 */
	public abstract Float getLineOpacity();
	
	/**
	 * Get the opacity for shapes. 
	 * @return the opacity for shapes (1 = solid, 0 = transparent). 
	 */
	public abstract Float getShapeOpacity();
	
	/**
	 * Convert a floating point 0-1 Object to an integer value. Assume 1 if null. 
	 * @param floatValue
	 * @return
	 */
	public int floatToInt255(Float floatValue) {
		if (floatValue == null) {
			return 255;
		}
		return Math.min(255, (int) (255.f * floatValue));
	}
	
	/**
	 * Generate a new colour object with the said opacity. 
	 * @param oldColor old colour
	 * @param opacity opacity on scale 0 to 1
	 * @return new colour
	 */
	public Color createColor(Color oldColor, Float opacity) {
		if (oldColor == null) {
			return null;
		}
		int op = floatToInt255(opacity);
		return new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), op);
	}
	
}
