package PamView;

import PamView.PamColors.PamColor;

/**
 * Interface that enables the colour manager (PamColors) to change the 
 * colour of any swing component. When night / day settings
 * are changes, the colour manager iterates through the entire
 * tree of frames, containers and components and for each one
 * tests to see if it's implemented ColorManaged. If it has, 
 * then the components back and foreground colours are changed. 
 * @author Doug Gillespie
 * @see PamColors
 * @see PamColor
 */
public interface ColorManaged {

	/**
	 * The components colour scheme. 
	 * @return PamColor
	 */
	public PamColor getColorId();
	
	
}
