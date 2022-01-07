package PamView.paneloverlay;

import PamguardMVC.PamDataBlock;

/**
 * Observer for when a checkbox menu item associated with an OverlayDataManager is changed. 
 * @author dg50
 *
 */
public interface OverlayDataObserver {

	/**
	 * Called whenever a checkbox in a menuitem created by an OverlayDataManager is selected. 
	 * @param dataBlock associated datablock. 
	 * @param selected selection state. 
	 */
	public void selectionChanged(PamDataBlock dataBlock, boolean selected);
	
}
