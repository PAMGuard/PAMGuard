package warnings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Timer;

import PamController.PamController;

/**
 * System for handling and displaying warnings and errors from various parts of PAMguard. 
 * @author Douglas Gillespie
 *
 */
public class WarningSystem {

	private static WarningSystem singleInstance;
	private List<PamWarning> warnings = Collections.synchronizedList(new ArrayList<PamWarning>());
	private ArrayList<WarningDisplay> displays = new ArrayList<>();
	private Timer warningTimer;
	
	/**
	 * Private constructor for singleton class
	 */
	private WarningSystem() {
		warningTimer = new Timer(2000, new WarningTimerTask());
		warningTimer.start();
	}
	
	/**
	 * Time task to delete out of date warnings
	 * @author dg50
	 *
	 */
	private class WarningTimerTask implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			removeOldWarnings();
		}		
	}
	
	/**
	 * Get the last listed warning
	 * @return last warning of null
	 */
	public synchronized PamWarning getLastWarning() {
		if (warnings.size() > 0) {
			return warnings.get(warnings.size()-1);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Get reference to singleton WarningSystem
	 * @return Wanring System. 
	 */
	public synchronized static WarningSystem getWarningSystem() {
		if (singleInstance == null) {
			singleInstance = new WarningSystem();
		}
		return singleInstance;
	}
	
	/**
	 * Add a warning, will remove first if the same one is already in there. 
	 * @param warning Warning to add.
	 */
	public synchronized void addWarning(PamWarning warning) {
//		if (warnings.contains(warning) == false) {
//			warnings.add(warning);			
//		}
//		System.err.println(PamCalendar.formatDateTime(PamCalendar.getTimeInMillis()) + " " + warning.getWarningMessage());
		warnings.remove(warning);
		warnings.add(warning);
		if (warning.isRequestRestart()) {
			requestRestart(warning);
		}
		notifyDisplays();
	}
	
	/**
	 * Get an iterator over the warnings list. 
	 * Any display using this list should place code in a block
	 * Synchronized on the WarningSystem to avoid 
	 * concurrent modification exceptions. 
	 * @return list iterator. 
	 */
	public ListIterator<PamWarning> getListIterator() {
		return warnings.listIterator();
	}
	
	/**
	 * Get the current number of warnigns in the list. 
	 * @return number of warnings. 
	 */
	public synchronized int getNumbWarnings() {
		return warnings.size();
	}

	/**
	 * Remove a warning
	 * @param warning warning to remove
	 * @return true if the warnign existed in the warnings list 
	 */
	public synchronized boolean removeWarning(PamWarning warning) {
		boolean r = warnings.remove(warning);
		if (r) {
			notifyDisplays();
		}
		return r;
	}
	
	/**
	 * Update an existing warning 
	 * @param warning warning to update
	 */
	public synchronized void updateWarning(PamWarning warning) {
		if (warning.isRequestRestart()) {
			requestRestart(warning);
		}
		notifyDisplays();
	}
	
	/**
	 * Add a warning display
	 * @param display warning display
	 */
	public void addDisplay(WarningDisplay display) {
		displays.add(display);
	}

	/**
	 * Remove a warning display
	 * @param display display to remove
	 * @return true if the display existed and was removed from the display list
	 */
	public boolean removeDisplay(WarningDisplay display) {
		return displays.remove(display);
	}
	
	/**
	 * Remove old warnings. 
	 */
	private synchronized void removeOldWarnings() {
		long now = System.currentTimeMillis();
		ListIterator<PamWarning> it = warnings.listIterator();
		int removals = 0;
		while (it.hasNext()) {
			PamWarning w = it.next();
			if (now > w.getEndOfLife()) {
				it.remove();
				removals++;
			}
		}
		if (removals > 0) {
			notifyDisplays();
		}
	}

	/**
	 * Notify displays that a warning has been added, removed or updated. 
	 */
	private void notifyDisplays() {
		for (WarningDisplay d:displays) {
			d.updateWarnings();
		}
	}

	/**
	 * Request a restart from PAMGuard - i.e. stop and start modules, don't 
	 * exit the entire program !
	 * @param warning 
	 */
	private void requestRestart(PamWarning warning) {
		warning.setRequestRestart(false);
		String st = String.format("Warning restart requested from %s msg %s.", warning.getWarningSource(), warning.getWarningMessage());
		System.out.println(st);
		PamController.getInstance().restartPamguard();
	}

	/**
	 * Get a specified warning
	 * @param warningIndex index of warning in list.
	 * @return warning or null
	 */
	public synchronized PamWarning getWarning(int warningIndex) {
		if (warningIndex < 0 || warningIndex >= warnings.size()) {
			return null;
		}
		return warnings.get(warningIndex);
	}
}
