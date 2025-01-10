package offlineProcessing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;

/**
 * Task manager for a PamControlledUnit. Can be overridden if fancy behaviour required. 
 * @author dg50
 *
 */
public class UnitTaskManager {

	private ArrayList<OfflineTaskGroup> offlineTaskGroups = new ArrayList<>();
	
	public UnitTaskManager() {
	}

	public void add(OfflineTaskGroup offlineTaskGroup) {
		offlineTaskGroups.add(offlineTaskGroup);
	}

	public int size() {
		return offlineTaskGroups.size();
	}

	public OfflineTaskGroup get(int i) {
		return offlineTaskGroups.get(i);
	}

	public void clearAll() {
		offlineTaskGroups.clear();
	}
	
	public boolean remove(OfflineTaskGroup taskGroup) {
		return offlineTaskGroups.remove(taskGroup);
	}
	
	/**
	 * Add an automatically generated menu item for every task group. 
	 * @param existingMenu
	 * @return
	 */
	public int addMenuItems(JMenu existingMenu) {
		ArrayList<JMenuItem> items = getMenuItems();
		if (items == null) {
			return 0;
		}
		for (int i = 0; i < items.size(); i++) {
			existingMenu.add(items.get(i));
		}
		return items.size();
	}
	
	/**
	 * Get a list of auto generated many items. 
	 * @return
	 */
	public ArrayList<JMenuItem> getMenuItems() {
		ArrayList<JMenuItem> menuItems = new ArrayList();
		for (int i = 0; i < offlineTaskGroups.size(); i++) {
			OfflineTaskGroup group = offlineTaskGroups.get(i);
			JMenuItem menuItem = new JMenuItem(group.getUnitName());
			menuItem.addActionListener(new TaskGroupActionListener(group));
			menuItems.add(menuItem);
		}
		return menuItems;
	}
	
	/**
	 * Action listener for a task group
	 * @author dg50
	 *
	 */
	private class TaskGroupActionListener implements ActionListener {

		private OfflineTaskGroup group;

		public TaskGroupActionListener(OfflineTaskGroup group) {
			this.group = group;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			taskGroupAction(group);
		}
		
	}
	
	/**
	 * Task action. Can be overridden easily enough independently of the action listener. 
	 * @param offlineTaskGroup
	 */
	public void taskGroupAction(OfflineTaskGroup offlineTaskGroup) {
		Frame mainFrame = PamController.getMainFrame();
		try {
			PamDataBlock dataBlock = offlineTaskGroup.getPrimaryDataBlock();
			if (dataBlock != null) {
				PamControlledUnit pcUnit = dataBlock.getParentProcess().getPamControlledUnit();
				mainFrame = pcUnit.getGuiFrame();
			}
		}
		catch (Exception e) {
			
		}
		OLProcessDialog olDialog = new OLProcessDialog(mainFrame, offlineTaskGroup, offlineTaskGroup.getUnitName() + " tasks");
		olDialog.setVisible(true);
	}

}
