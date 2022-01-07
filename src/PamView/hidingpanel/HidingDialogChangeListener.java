package PamView.hidingpanel;

/**
 * Can be used to get notifications into any bit of pamguard when things are changed in 
 * one of the sliding dialogs. 
 * @author Doug Gillespie
 *
 */
public interface HidingDialogChangeListener {

	public static final int MORE_DIALOG_CLOSED = -1;
	/**
	 * Notification from a sliding dialog panel when something changes. 
	 * 
	 * @param changeLevel quick way of indicating levels of change. <p>-ve numbers
	 * are used by the system. +ve ones can be used by programmer in a dialog specific context. 
	 * @param object an object if you need to send other information. 
	 */
	public void dialogChanged(int changeLevel, Object object);
	
}
