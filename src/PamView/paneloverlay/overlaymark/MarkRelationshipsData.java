package PamView.paneloverlay.overlaymark;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Centralised location where all relationships between markers
 * and the marked are stored. Both markers and marked are 
 * identified by name, so mustn't repeat names - if they do
 * then they will share the same data. <p>
 * @author dg50
 *
 */
public class MarkRelationshipsData implements Serializable {

	public static final long serialVersionUID = 0L;
	
	/**
	 * This will have to be serialised, so think about how to store it. 
	 * For operational work, the OverlayMarkObserves and OverlayMarkProviders 
	 * lists tell us what is currently in memory so can for the basis
	 * for any dialogs. All this has to do is to hold the relationships between them, 
	 * i.e. which are selected.  
	 */
	private Hashtable<String, Boolean> markRelations = new Hashtable<>();
	
	/**
	 * Immediate menus on displays after a mark has been made, otherwise, have to right click.
	 */
	private boolean immediateMenus;
	
	public void setRelationship(String markerName, String observerName, boolean linked) {
		markRelations.put(combineNames(markerName, observerName), linked);
	}
	
	public boolean getRelationship(String markerName, String observerName) {
		Boolean val = markRelations.get(combineNames(markerName, observerName));
		if (val == null) {
			return false;
		}
		else {
			return val;
		}
	}
	
	private String combineNames(String markerName, String observerName) {
		return markerName + "-marks-" + observerName;
	}

	/**
	 * @return the immediateMenus
	 */
	public boolean isImmediateMenus() {
		return immediateMenus;
	}

	/**
	 * @param immediateMenus the immediateMenus to set
	 */
	public void setImmediateMenus(boolean immediateMenus) {
		this.immediateMenus = immediateMenus;
	}
}
