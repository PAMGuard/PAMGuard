package clipgenerator.clipDisplay;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPopupMenu;

import clipgenerator.ClipDataUnit;

public class ClipDisplayDecorations {

	private ClipDisplayUnit clipDisplayUnit;

	public ClipDisplayDecorations(ClipDisplayUnit clipDisplayUnit) {
		super();
		this.clipDisplayUnit = clipDisplayUnit;
	}

	/**
	 * Decorate the display - adding any additional panels 
	 * and creating extra menus, etc. 
	 */
	public void decorateDisplay() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Remove any additional decorations
	 */
	public void removeDecoration() {
	}

	/**
	 * @return the clipDisplayUnit
	 */
	public ClipDisplayUnit getClipDisplayUnit() {
		return clipDisplayUnit;
	}

	/**
	 * Add additional functionality to the popup menu on a display unit. 
	 * @param basicMenu basic menu for clip display units
	 * @param clipDisplayUnit clip display unit
	 * @param clipDataUnit clip data unit
	 * @return the modifies (or a completely different) menu
	 */
	public JPopupMenu addDisplayMenuItems(JPopupMenu basicMenu) {
		return basicMenu;
	}
	
	/**
	 * Get a background colour for the clip
	 * @param clipDataUnit
	 * @return a background colour or null, in which case the standard PAMColour will be used. 
	 */
	public Color getClipBackground() {
		return null;
	}
	
	/**
	 * Can be used to decorate / draw on the clip panel axis. 
	 * @param g graphics handle. 
	 */
	public void drawOnClipAxis(Graphics g) {
		
	}
	
	/**
	 * Can be used to decorate / draw on the clip panel axis. 
	 * @param g graphics handle. 
	 */
	public void drawOnClipBorder(Graphics g) {
		
	}
	
}
