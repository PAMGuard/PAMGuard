package PamView.paneloverlay.overlaymark;

import java.util.ArrayList;

/**
 * Static list of overlay mark observers. 
 * @author dg50
 *
 */
public class OverlayMarkObservers {

	private ArrayList<OverlayMarkObserver> markObservers = new ArrayList();
	
	private static OverlayMarkObservers singleInstance;
	
	private OverlayMarkObservers() {
		
	}
	
	/**
	 * 
	 * @return Singleton object for handling all mark observers. 
	 */
	public static OverlayMarkObservers singleInstance() {
		if (singleInstance == null) {
			singleInstance = new OverlayMarkObservers();
		}
		return singleInstance;
	}

	/**
	 * Add a mark observer to the list. 
	 * @param markObserver 
	 */
	public void addObserver(OverlayMarkObserver markObserver) {
		if (markObservers.contains(markObserver) == false) {
			markObservers.add(markObserver);
		}
	}
	
	/**
	 * Remove a mark observer from the list. 
	 * @param markObserver 
	 */
	public void removeObserver(OverlayMarkObserver markObserver) {
		markObservers.remove(markObserver);
	}

	/**
	 * @return the list of markObservers
	 */
	public ArrayList<OverlayMarkObserver> getMarkObservers() {
		return markObservers;
	}
	
	
}
