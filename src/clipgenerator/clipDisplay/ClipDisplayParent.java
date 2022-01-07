package clipgenerator.clipDisplay;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPopupMenu;

import clipgenerator.ClipDataUnit;
import clipgenerator.ClipDisplayDataBlock;

/**
 * A set of functions that can be used to add additional items to a 
 * clip display unit. Most functions have now been removed from this
 * class and it's primary purpose is to return a ClipDisplayDecorations
 * class. The base class for ClipDisplayDecorations is not abstract
 * but should be overridden by any decorator (i.e. the Difar system) which 
 * wishes to add decorations to the clips. 
 * @author doug
 *
 */
public interface ClipDisplayParent {

	/**
	 * 
	 * @return the data block who's clips are to be displayed
	 */
	public ClipDisplayDataBlock<ClipDataUnit> getClipDataBlock();
	
	/**
	 * A name (though I don't think this ever gets used by current displays)
	 * @return display name
	 */
	public String getDisplayName();

	/**
	 * Add additional functionality and controls to a clip display unit
	 * @param clipDisplayUnit display unit to decorate. 
	 */
	public ClipDisplayDecorations getClipDecorations(ClipDisplayUnit clipDisplayUnit);

	public void displaySettingChange();

	
}
