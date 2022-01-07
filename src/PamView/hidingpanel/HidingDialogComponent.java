package PamView.hidingpanel;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;

public abstract class HidingDialogComponent {

	private ArrayList<HidingDialogChangeListener> changeListeners = new ArrayList<>();
	/**
	 * 
	 * @return component to show in the hiding dialog panel
	 */
	public abstract JComponent getComponent();
	
	/**
	 * Can hide - generally this should return true if at all possible
	 * however you may need to stop it hiding if any controls are in an 
	 * impossible state. This function also gives users an opportunity 
	 * to check / use any dialog parameters. 
	 * @return true if the panel can be hidden. 
	 */
	public abstract boolean canHide();
	
	/**
	 * Called when the component is about to be set visible or hidden. 
	 */
	public abstract void showComponent(boolean visible);
	
	/**
	 * A short name - gets used when multiple components need to be tabbed up. 
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * A small icon around 18x18 pixels. This is used for tabs to allow users to quickly identify the control panel. 
	 * Can be null if no symbol is needed
	 */
	public Icon getIcon(){
		return null; 
	}
		
	/**
	 * Sliding dialog has more options. If this 
	 * returns true a 'more' button will show next to the pin
	 * which can be used to open a more complex dialog. 
	 * @return true if there are more options to show. 
	 */
	public boolean hasMore() {
		return false;
	}
	
	/**
	 * Get's called from the More button, which shows if 
	 * hasMore is returning true. Can be used to show an 
	 * evenmore exciting dialog.  
	 * @param hidingDialog
	 * @return
	 */
	public boolean showMore(HidingDialog hidingDialog) {
		return false;
	}
	
	/**
	 * Notification from a sliding dialog panel when something changes. 
	 * 
	 * @param changeLevel quick way of indicating levels of change. <p>-ve numbers
	 * are used by the system. +ve ones can be used by programmer in a dialog specific context. 
	 * @param object an object if you need to send other information. 
	 */
	public void notifyChangeListeners(int changeLevel, Object object) {
		for (HidingDialogChangeListener cl:changeListeners) {
			cl.dialogChanged(changeLevel, object);
		}
	}
	
	public void addChangeListener(HidingDialogChangeListener changeListener) {
		changeListeners.add(changeListener);
	}
	
	public void removeChangeListener(HidingDialogChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	public void repackDisplay() {
		Component c = getComponent();
		while (c != null) {
			if (HidingDialog.class.isAssignableFrom(c.getClass())) {
				((HidingDialog) c).repackDialog();
			}
			c = c.getParent();
		}
	}
}
