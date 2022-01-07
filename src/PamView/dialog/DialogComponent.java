package PamView.dialog;

import java.awt.Window;

import javax.swing.JComponent;

/**
 * General interface for components which are going to get included in alarge dialogs 
 * @author dg50
 *
 */
public interface DialogComponent {

	/**
	 * the swing component to show
	 * @return the swing component to display
	 * @param owner Owner window (needed for any dialogs to fire off the component). 
	 */
	public JComponent getComponent(Window owner);
	
	/**
	 * Set the parameters in the controls of that component
	 */
	public void setParams();
	
	/**
	 * Get parameters out of the controls in that component - the concrete class
	 * will have to work out what to do with them !
	 * @return true if the parameters were OK, false otherwise. 
	 */
	public boolean getParams();
}
