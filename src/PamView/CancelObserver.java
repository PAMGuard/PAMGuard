package PamView;

/**
 * Observer to use with Pamdialog which is called whenever the cancel button is pressed. 
 * It can perform actions outside the dialog box and can also refuse the cancel request by returning
 * false. 
 * @author Doug Gillespie
 *
 */
public interface CancelObserver {

	/**
	 * Called whenever a PamDialog cancel button is pressed. 
	 * @return true if the cancel function can proceed. False otherwise. 
	 */
	boolean cancelPressed();
	
}
