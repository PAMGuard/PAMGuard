package PamView;

import java.awt.Component;

/**
 * Interface for passing information from overlay graphics to plots to give information to include in 
 * keys for the various displays. 
 * <p>
 * Currently just a place holder class to include in PanelOverlayDraw implementations. Will 
 * need to work out what members and member functions to put in it later. 
 * 
 * @author Doug Gillespie
 * @see PanelOverlayDraw
 *
 */
public interface PamKeyItem {
	
	/**
	 * A full key
	 */
	public static final int KEY_VERBOSE = 1;
	
	/**
	 * A shortened key
	 */
	public static final int KEY_SHORT = 2;
	/**
	 * 
	 * @return Get's the total number of key items associated with this detection
	 * <p> This can be > 1 for things like the click detector which may want to 
	 * show different symbols for different species. 
	 */
	public int getNumItems(int keyType);
	
	/**
	 * 
	 * @param keyType type of key - verbose or short
	 * @param nComponent component number (where there are > 1)
	 * @return a graphics component to include in the key
	 */
	public Component getIcon(int keyType, int nComponent);

	/**
	 * 
	 * @param keyType type of key - verbose or short
	 * @param nComponent component number (where there are > 1)
	 * @return text to include in the key
	 */
	public String getText(int keyType, int nComponent);
	
}
