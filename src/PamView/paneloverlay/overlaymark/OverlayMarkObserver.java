package PamView.paneloverlay.overlaymark;

import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamView.GeneralProjector;
import PamguardMVC.dataSelector.DataSelector;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.input.MouseEvent;

/**
 * Interface used by something that is observing a spectrogram mark. 
 * @author Doug Gillespie
 *
 */
public interface OverlayMarkObserver {

	/**
	 * Note that start, end and update are the same (0,1,2) as SpectrogramMarkObserver down, up and drag
	 */
	public static final int MARK_START = 0; // start of a marked region
	public static final int MARK_END = 1; // end of a marked region
	public static final int MARK_UPDATE = 2; // in process of making a region
	public static final int MARK_CANCELLED = 3; // cancel region. No longer exists.
	public static final int MOUSE_CLICK = 4; // all other mouse actions, e.g. can pick up right mouse actions. 
	
	/**
	 * Mark update called whenever a mark changes
	 * @param markStatus start, end, update, cancel, etc. 
	 * @param overlayMarker Source of the mark (generally a display)
	 * @param overlayMark updated mark. 
	 * @return true if the observer has used the mark. 
	 */
	public boolean markUpdate(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark);
	
	/**
	 * Get a menu, or menu items which can be used to display on the marking 
	 * display in response to a right click or some other action. <p>
	 * The menu items will be put into a popup menu for display. 
	 * 
	 * @param markSummaryData everything we need to know about the mark, including which data
	 * are within it. 
	 * @return A menu item (can be null)
	 */
	public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData);
	
	/**
	 * Required parameters for the mark to get used. Can be null in which case 
	 * the mark can accept anything, might be something like TIME & FREQUENCY, etc. 
	 * @return list of parameters (up to three). 
	 */
	public GeneralProjector.ParameterType[] getRequiredParameterTypes();
	
	/**
	 * Name of the mark observer. 
	 * @return the observer name 
	 */
	public String getObserverName();
	
	/**
	 * Get a data selector for use with a specific type of mark. 
	 * @param overlayMarker Marker
	 * @return Data selector (can be null)
	 */
	public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker);
	
	/**
	 * Name of the mark. Observers may have several mark types and might want to 
	 * change the name to indicate what action will be taken when a mark is made
	 * 
	 * @return
	 */
	public String getMarkName();
}
