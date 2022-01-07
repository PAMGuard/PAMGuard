package alfa.status;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.status.ModuleStatus;
import PamModel.PamModuleInfo;
import alfa.ALFAControl;
import alfa.ControlledModuleInfo;
import alfa.clickmonitor.eventaggregator.ClickEventAggregate;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/**
 * Status monitor, will scan a list of modules for 
 * @author dg50
 *
 */
public class StatusMonitor {
	
	private boolean monitorAll;

	private ArrayList<ControlledModuleInfo> modulesList;
	
	private int monitorInterval = 2; // monitor interval in seconds. 
	
	private Timer checkTimer;
	
	private ModuleStatus[] moduleStatus;
	
	private boolean[] moduleExists;
	
	private ArrayList<StatusObserver> statusObservers = new ArrayList<>();

	/**
	 * @param monitorAll
	 */
	public StatusMonitor(boolean monitorAll) {
		this(monitorAll, null);
	}

	/**
	 * @param monitorAll
	 * @param essentialModulesList
	 */
	public StatusMonitor(boolean monitorAll, ArrayList<ControlledModuleInfo> essentialModulesList) {
		super();
		this.monitorAll = monitorAll;
		this.modulesList = essentialModulesList;
		
		checkTimer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkTimerActions();
			}
		});
		checkTimer.start();
	}
	protected void checkTimerActions() {
		checkAllModules();
	}

	private synchronized void checkAllModules() {
		/**
		 * Get some single status object array for all of the modules. 
		 */
		checkStatusAllocation();
		
		int index = 0;
		for (ControlledModuleInfo moduleInfo:modulesList) {
			checkModule(moduleInfo, index++);
		}
		
		notifyStatusUpdate();
	}

	private synchronized void checkStatusAllocation() {
		int n = modulesList.size();
		if (moduleStatus == null || moduleStatus.length != n) {
			moduleStatus = new ModuleStatus[n];
		}
		if (moduleExists == null || moduleExists.length != n) {
			moduleExists = new boolean[n];
		}
	}

	private Integer checkModule(ControlledModuleInfo moduleInfo, int index) {
		PamController pamController = PamController.getInstance();
		if (pamController == null) return null;
		PamControlledUnit pamControlledUnit = pamController.findControlledUnit(moduleInfo.getPamModuleInfo().getModuleClass(), moduleInfo.getFixedModuleName());
		moduleExists[index] = (pamControlledUnit != null);
		if (moduleExists[index]) {
			moduleStatus[index] = pamControlledUnit.getModuleStatus();
			if (moduleStatus[index] != null) {
				moduleStatus[index].setName(pamControlledUnit.getUnitName());
				return moduleStatus[index].getStatus();
			}
			else {
				return null;
			}
		}
		else {
			moduleStatus[index] = new ModuleStatus(ModuleStatus.STATUS_ERROR, "Module not present");
			moduleStatus[index].setRemedialAction(new CreateModuleAction(moduleInfo));
			return moduleStatus[index].getStatus();
		}
//		sayModuleStatus(index);
	}
	
	private void sayModuleStatus(int index) {
		ControlledModuleInfo moduleInfo = modulesList.get(index);
		if (moduleExists[index]) {
			System.out.printf("Module %s Status %s\n", moduleInfo.getDefaultName(), moduleStatus[index]);
		}
		else {
			System.out.printf("Module %s is not present\n", moduleInfo.getDefaultName());			
		}
	}

	/**
	 * Add an observer to get status updates.
	 * @param statusObserver Status observer
	 */
	public void addObserver(StatusObserver statusObserver) {
		if (statusObservers.contains(statusObserver) == false) {
			statusObservers.add(statusObserver);
		}
	}
	
	/**
	 * Remove a status update observer
	 * @param statusObserver to remove
	 * @return true if the observer existed in the list
	 */
	public boolean removeObserver(StatusObserver statusObserver) {
		return statusObservers.remove(statusObserver);
	}
	
	/**
	 * Notify all observers that the status has changed. 
	 */
	private void notifyStatusUpdate() {
		for (StatusObserver obs:statusObservers) {
			obs.newStatus();
		}
	}
	/**
	 * @return monitorAll flag to monitor all Modules in PAMGuard.
	 */
	public boolean isMonitorAll() {
		return monitorAll;
	}

	/**
	 * @param monitorAll set flag to monitor all Modules in PAMGuard
	 */
	public void setMonitorAll(boolean monitorAll) {
		this.monitorAll = monitorAll;
	}

	/**
	 * Set list of modules to monitor
	 * @return the modulesList
	 */
	public ArrayList<ControlledModuleInfo> getModulesList() {
		return modulesList;
	}

	/**
	 * List of modules to monitor
	 * @param modulesList the modulesList to set
	 */
	public void setModulesList(ArrayList<ControlledModuleInfo> modulesList) {
		this.modulesList = modulesList;
	}

	/**
	 * Monitor interval in seconds.
	 * @return the monitorInterval
	 */
	public int getMonitorInterval() {
		return monitorInterval;
	}

	/**
	 * interval in seconds, set to zero for no timed monitoring
	 * @param monitorInterval the monitorInterval to set
	 */
	public void setMonitorInterval(int monitorInterval) {
		this.monitorInterval = monitorInterval;
		if (monitorInterval <= 0) {
			checkTimer.stop();
		}
		else {
			checkTimer.setDelay(monitorInterval*1000);
			checkTimer.start();
		}
	}

	/**
	 * @return the moduleStatus of all modules monitored
	 */
	public ModuleStatus[] getModuleStatus() {
		return moduleStatus;
	}

	/**
	 * @return the moduleExists existence of all modules monitored
	 */
	public boolean[] getModuleExists() {
		return moduleExists;
	}

	public synchronized ModuleStatus getSummaryStatus() {
		if (moduleStatus == null || moduleStatus.length == 0) {
			return new ModuleStatus(ModuleStatus.STATUS_ERROR, "No PAM modules present");
		}
		int maxStatus = 0;
		String statusString = null;
		int n = Math.min(moduleStatus.length, modulesList.size());
		for (int i = 0; i < n; i++) {
			if (modulesList.get(i).getPamModuleInfo().getClassName().equals(ALFAControl.class.getName())) {
				continue;
			}
			ModuleStatus ms = moduleStatus[i];
			if (ms == null) continue;
			maxStatus = Math.max(maxStatus, ms.getStatus());
			if (ms.getStatus() > 0) {
				String sBit = String.format("%s: %s %s", ModuleStatus.getStatusString(ms.getStatus()),
						modulesList.get(i).getDefaultName(), ms.getMessage());
				
				if (statusString == null) {
					statusString = "<p>" + sBit;
				}
				else {
					statusString +=  "<p>" + sBit;
				}
			}
		}
//		if (statusString != null) {
//			statusString += "</html>";
//		}
		return new ModuleStatus(maxStatus, statusString);
	}



}
