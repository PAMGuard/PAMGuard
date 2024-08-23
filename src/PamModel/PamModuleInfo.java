package PamModel;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamView.MenuItemEnabler;
import PamView.dialog.warn.WarnOnce;
import dataModelFX.connectionNodes.ModuleIconFactory.ModuleIcon;
import javafx.stage.Stage;

/**
 * Holds information about available PAMGUARD modules.
 * 
 * @author Doug Gillespie
 * @see PamModel#createPamModel()
 * @see PamDependency
 *
 */
public class PamModuleInfo implements PamDependent{
	
	private String className;
	private String description;
	private String defaultName;
	private Class moduleClass;
	private String toolTipText;
	
	private static final Class[] constrParams1 = {PamConfiguration.class, String.class};
	private static final Class[] constrParams2 = {String.class};
	
	/**
	 * A list of possible GUI types the module can have. These are received from flags in PAMGuiManager();
	 */
	private ArrayList<Integer> guiCombatibility = new ArrayList<Integer>(); 
	
/**
	 * @return the toolTipText
	 */
	public String getToolTipText() {
		return toolTipText;
	}

	/**
	 * @param toolTipText the toolTipText to set
	 */
	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}

	//	MenuAction menuAction;
//	ModuleDependency moduleDependency;
	private PamDependency pamDependency;
	private int nInstances = 0;
	private MenuItemEnabler removeMenuEnabler, addMenuEnabler;
	/**
	 * minimum number of this type of module that can be created. 
	 */
	private int minNumber = 0;
	/**
	 * maximum number of this type of module that can be created. 
	 */
	private int maxNumber = 0;
	
	/**
	 * group modules in the add modules menu into common groups
	 */
	private ModulesMenuGroup modulesMenuGroup;
	
	
	/**
	 * An 100px x 100px icon to represent the module. Can be null. 
	 */
	private ModuleIcon iconImage;
	
	//Experimental:PR
	private boolean coreModule;
	private boolean hidden = false;
	
	private static ArrayList<PamModuleInfo> moduleList = new ArrayList<PamModuleInfo>();
	
	
	/**
	 * registers different controlled units in an ArrayList so that they
	 * can be instantiated by the user.
	 * Modified to call {@link PamModuleInfo#registerControlledUnit(String, String, ClassLoader)}
	 * using the default class loader.
	 * @param className name of JAVA class.
	 * @param description Description of module
	 * @return PamModuleInfo object of the control class
	 */
	public static PamModuleInfo registerControlledUnit(String className, String description) {
		
		return registerControlledUnit(className, description, PamModuleInfo.class.getClassLoader());
//		Class c = null;
//		try {
//			c = Class.forName(className);
////			System.out.println(c.getName());
//		}
//		catch (Exception Ex) {
//			System.out.println("Can't find class " + className);
//			return null;
//		}
//		Class stringClass = String.class;
//		Class[] classlist = new Class[1];
//		classlist[0] = stringClass;
//		// check that it's a subclass of PamControlledUnit
//		Class superClass = c;
//		while(true) {
//			superClass = superClass.getSuperclass();
//			if (superClass == null) return null;
////			System.out.println(superClass.getSimpleName());
//			if (superClass.getSimpleName().equals("PamControlledUnit")) {
//				break;
//			}
//		}
//		PamModuleInfo newModuleInfo = new PamModuleInfo(className, description, c);
//		moduleList.add(newModuleInfo);
//		
//		return newModuleInfo;
	}
	
	
	/**
	 * registers different controlled units in an ArrayList so that they
	 * can be instantiated by the user. 
	 * @param className name of JAVA class.
	 * @param description Description of module
	 * @param cl classloader to use for the JAVA class
	 * @return PamModuleInfo object of the JAVA class
	 */
	public static PamModuleInfo registerControlledUnit(String className, String description, ClassLoader cl) {
		
		Class c = null;
		try {
			c = Class.forName(className,true,cl);
		}
		catch (Exception Ex) {
			System.out.println("Can't find class " + className);
			return null;
		}
		Class stringClass = String.class;
		Class[] classlist = new Class[1];
		classlist[0] = stringClass;
		// check that it's a subclass of PamControlledUnit
		Class superClass = c;
		while(true) {
			superClass = superClass.getSuperclass();
			if (superClass == null) return null;
//			System.out.println(superClass.getSimpleName());
			if (superClass.getSimpleName().equals("PamControlledUnit")) {
				break;
			}
		}
		PamModuleInfo newModuleInfo = new PamModuleInfo(className, description, c);
		moduleList.add(newModuleInfo);
		
		return newModuleInfo;
	}
	
	public PamModuleInfo(String className, String description, Class moduleClass) {
		super();
		this.className = className;
		this.description = description;
		this.moduleClass = moduleClass;
		this.defaultName = description;
//		menuAction = new MenuAction(this);
		addMenuEnabler = new MenuItemEnabler();
		removeMenuEnabler = new MenuItemEnabler();
		setNInstances(0);
	}
	
	@Override
	public String toString() {
		if (description != null) return description;
		return className;
	}
	
	static public PamModuleInfo findModuleInfo(String className) {
		for (int i = 0; i < moduleList.size(); i++) {
			if (moduleList.get(i).className.equals(className)) {
				return moduleList.get(i);
			}
		}
		return null;
	}
	
	class AddModuleMenuAction implements ActionListener {

		PamModuleInfo moduleInfo;
		 
		Frame parentFrame;
		
		AddModuleMenuAction(Frame parentFrame, PamModuleInfo moduleInfo) {
			this.parentFrame = parentFrame;
			this.moduleInfo = moduleInfo;
		}
		
		public void actionPerformed(ActionEvent e) {
			// first check dependencies to see if everything required
			// by this module actually exists
			if (pamDependency != null && PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			  DependencyManager dependencyManager = PamModel.getPamModel().getDependencyManager();
			  dependencyManager.checkDependency(parentFrame, moduleInfo, true);
			}
			// create a new PamControlledUnit and add it to PamGuard ...
			PamController pamController = PamController.getInstance();
		
			pamController.addModule(parentFrame, moduleInfo);
		}
		
	}
	
	public AddModuleMenuAction getMenuAction(Frame parentFrame) {
		return new AddModuleMenuAction(parentFrame, this);
	}

	public PamControlledUnit create(String unitName) {
		return create(null, unitName);
	}
	
	public PamControlledUnit create(PamConfiguration pamConfiguration, String unitName) {
		
		PamControlledUnit newUnit = null;
//		Class[] paramList = new Class[1];
//		paramList[0] = unitName.getClass();
		boolean error = false;
		long tic = System.currentTimeMillis();
		long toc = tic;
		try {
			Constructor constructor = moduleClass.getConstructor(constrParams1);
			newUnit = (PamControlledUnit) constructor.newInstance(pamConfiguration, unitName);
			newUnit.setPamModuleInfo(this);
		}
		catch (Exception Ex) {
		}
		if (newUnit == null) {
			try {
				Constructor constructor = moduleClass.getConstructor(constrParams2);
				newUnit = (PamControlledUnit) constructor.newInstance(unitName);
				newUnit.setPamModuleInfo(this);
			}
			catch (Exception Ex) {
				String title = "Error loading module";
				String msg = "There was an error trying to load " + unitName + ".<p>" +
						"If this is a core Pamguard module, please copy the error message text and email to " +
						"support@pamguard.org.<p>" +
						"If this is a plug-in, the error may have been caused by an incompatibility between " +
						"it and this version of PAMGuard, or a problem with the code.  Please check the developer's website for help.<p>" +
						"This module will not be loaded.";
				String help = null;
				int ans = WarnOnce.showWarning(title, msg, WarnOnce.WARNING_MESSAGE, help, Ex);
				System.err.println("Exception while loading " +	Ex.getMessage());
				Ex.printStackTrace();
				return null;
			}
		}
		toc = System.currentTimeMillis();
		if (toc-tic > 1000) {
			System.out.printf("Module %s-%s was slow to load, taking %3.1f seconds\n", newUnit.getUnitType(), 
					newUnit.getUnitName(), (double)(toc-tic)/1000.);
		}

		setNInstances(nInstances + 1);
				
		return newUnit;
	}
	
	private Constructor findConstructor() throws NoSuchMethodException, SecurityException {
		Constructor constructor = null;
		try {
			constructor = moduleClass.getConstructor(constrParams1);
			return constructor;
		} catch (NoSuchMethodException | SecurityException e1) {
		}

		constructor = moduleClass.getConstructor(constrParams2);
		return constructor;
	}
	
	private void moduleRemoved(PamControlledUnit controlledUnit) {

		setNInstances(nInstances - 1);
	}

	/**
	 * @param instances The nInstances to set.
	 */
	private void setNInstances(int instances) {
		nInstances = instances;
		
		addMenuEnabler.enableItems(nInstances < maxNumber || maxNumber == 0);
		removeMenuEnabler.enableItems(nInstances > minNumber);
	}

	public static ArrayList<PamModuleInfo> getModuleList() {
		return moduleList;
	}

	public String getClassName() {
		return className;
	}

	public String getDescription() {
		return description;
	}

	public Class getModuleClass() {
		return moduleClass;
	}
	

	
	public static JMenu getModulesMenu(Frame parentFrame) {

		ArrayList<ModulesMenuGroup> moduleGroupsList = new ArrayList<ModulesMenuGroup>();
		ModulesMenuGroup menuGroup;
		
		JMenu modulesMenu = new JMenu("Add Modules ...");
		modulesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		JMenuItem menuItem;
		PamModuleInfo mi;
		ArrayList<PamModuleInfo> moduleList = PamModuleInfo.getModuleList();
		for (int i = 0; i < moduleList.size(); i++) {
			
			mi = moduleList.get(i);
			
			if (mi.isHidden()) {
				continue;
			}
			
			//System.out.println("PamModuleInfo getmodules menu " + moduleList.get(i).getDescription());
			menuItem = new JMenuItem(mi.toString());
			menuItem.addActionListener(mi.getMenuAction(parentFrame));
			if (mi.toolTipText != null){
				menuItem.setToolTipText(mi.toolTipText);
			}
			
			if (mi.canCreate() == false) {
				//continue;
				menuItem.setEnabled(false);
			}
			if ((menuGroup = mi.getModulesMenuGroup()) != null){
				if (moduleGroupsList.indexOf(menuGroup) < 0) {
					moduleGroupsList.add(menuGroup);
					menuGroup.setMenuItem(new JMenu(menuGroup.getMenuName()));
					modulesMenu.add(menuGroup.getMenuItem());
				}
				menuGroup.getMenuItem().add(menuItem);
			}
			else {
				modulesMenu.add(menuItem);
			}
		}
		return modulesMenu;
	}
	
	
	//TODO Items upon which other processer are dependent could/should be greyed-out 
	public static JMenu getRemoveMenu() {
		ArrayList<ModulesMenuGroup> moduleGroupsList = new ArrayList<ModulesMenuGroup>();
		ModulesMenuGroup menuGroup;
		// look in the Controller to see which modules are instantiated.
		JMenu modulesMenu = new JMenu("Remove Modules ...");
		modulesMenu.getPopupMenu().setLightWeightPopupEnabled(false);
		JMenuItem menuItem;
		int nUnits = PamController.getInstance().getNumControlledUnits();
		PamControlledUnit pamControlledUnit;
		for (int i = 0; i < nUnits; i++) {
			pamControlledUnit = PamController.getInstance().getControlledUnit(i);
			PamModuleInfo mi = findModuleInfo(pamControlledUnit.getClass().getName());
			if (mi == null) {
				continue;
			}
			if (mi != null) {
				if (mi.canRemove() == false) continue;
			}
			menuItem = new JMenuItem(pamControlledUnit.getUnitName());
			menuItem.addActionListener(new RemoveModuleMenuAction(pamControlledUnit));
			if ((menuGroup = mi.getModulesMenuGroup()) != null){
				if ((menuGroup = mi.getModulesMenuGroup()) != null){
					if (moduleGroupsList.indexOf(menuGroup) < 0) {
						moduleGroupsList.add(menuGroup);
						menuGroup.setMenuItem(new JMenu(menuGroup.getMenuName()));
						modulesMenu.add(menuGroup.getMenuItem());
					}
					menuGroup.getMenuItem().add(menuItem);
				}
			}
			else {
				modulesMenu.add(menuItem);
			}
		}
		return modulesMenu;
	}
	
	static class RemoveModuleMenuAction implements ActionListener {
		
		PamControlledUnit pamControlledUnit;

		public RemoveModuleMenuAction(PamControlledUnit pamControlledUnit) {
			this.pamControlledUnit = pamControlledUnit;
		}

		public void actionPerformed(ActionEvent e) {
			int ans = JOptionPane.showConfirmDialog(pamControlledUnit.getGuiFrame(),
					"Do you really want to remove the module " 
					+ pamControlledUnit.getUnitName());
			if (ans == JOptionPane.YES_OPTION) {
				PamModuleInfo mi = findModuleInfo(pamControlledUnit.getClass().getName());
				if (mi != null) {
					// do this first since the menu is recreated on a 
					// trigger from moduleRemoved and it needs an accurate
					// module count at that point !
					mi.moduleRemoved(pamControlledUnit);
				}
				pamControlledUnit.removeUnit();
			}
		}
		
	}

	/**
	 * PAMGUARD has a set of modules which are so intertwined with very other module
	 * that they don't follow the typical process->outputdatablock->process2->outputdatablock->process3...
	 * pattern. These are termed core modules
	 * <p>
	 * Note to developer: The meaning of core has changed as of 14/02/2014. Previously referred to supported modules by the PAMGUARD project. 
	 * @return true if a core module
	 */
	public boolean isCoreModule() {
		return coreModule;
	}


	/**
	 * PAMGUARD has a set of modules which are so intertwined with very other module
	 * that they don't follow the typical process->output data block->process2->output data block->process3...
	 * pattern. These are termed core modules
	 * <p>
	 * Note to developer: The meaning of core has changed as of 14/02/2014. Previously referred to supported modules by the PAMGUARD project. 
	 * @param coreModule true if a core module. 
	 */
	public void setCoreModule(boolean coreModule) {
		this.coreModule = coreModule;
	}
//	
//	public void addModuleDependency(DataType dataType, String defaultModule) {
//		moduleDependency = new ModuleDependency(dataType, defaultModule);
//	}
	
	/**
	 * @return Returns the maxNumber.
	 */
	public int getMaxNumber() {
		return maxNumber;
	}

	/**
	 * Set the maximum number of instances of a given module. 
	 * @param maxNumber The maxNumber to set.
	 */
	public void setMaxNumber(int maxNumber) {
		this.maxNumber = maxNumber;
	}

	/**
	 * @return Returns the minNumber.
	 */
	public int getMinNumber() {
		return minNumber;
	}

	/**
	 * Set the minumnum number of instances of a particular module. 
	 * @param minNumber The minNumber to set.
	 */
	public void setMinNumber(int minNumber) {
		this.minNumber = minNumber;
	}

	public boolean hasFixedNumber() {
		return ((minNumber == maxNumber) && maxNumber != 0);
	}
	
	public boolean canCreate() {
		return ((nInstances < maxNumber) || maxNumber == 0);
	}
	
	public boolean canRemove() {
		return (nInstances > minNumber);
	}

	/* (non-Javadoc)
	 * @see PamModel.PamDependent#addDependancy(PamModel.PamDependency)
	 */
	public void addDependency(PamDependency dependancy) {

		pamDependency = dependancy;
		
	}

	/* (non-Javadoc)
	 * @see PamModel.PamDependent#getDependency()
	 */
	public PamDependency getDependency() {
		
		return pamDependency;
		
	}

	/* (non-Javadoc)
	 * @see PamModel.PamDependent#getDependentUserName()
	 */
	public String getDependentUserName() {
		return this.description;
	}

	/**
	 * @return Returns the nInstances.
	 */
	public int getNInstances() {
		return nInstances;
	}

	/**
	 * @return Returns the defaultName.
	 */
	public String getDefaultName() {
		return defaultName;
	}
	
	public String getNewDefaultName() {
		// concatonate numbers onto the end of the defaultName
		// until one is unique. 

		String name = getDefaultName();
		int i = 1;
		while (true) {
			if (PamController.getInstance().findControlledUnit(moduleClass, name) == null) {
				return name;
			}
			name = getDefaultName() + " " + ++i;
		}
	}

	/**
	 * @param defaultName The defaultName to set.
	 */
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	/**
	 * @return Returns the addMenuEnabler.
	 */
	public MenuItemEnabler getAddMenuEnabler() {
		return addMenuEnabler;
	}

	/**
	 * @return Returns the removeMenuEnabler.
	 */
	public MenuItemEnabler getRemoveMenuEnabler() {
		return removeMenuEnabler;
	}

	public ModulesMenuGroup getModulesMenuGroup() {
		return modulesMenuGroup;
	}

	public void setModulesMenuGroup(ModulesMenuGroup modulesMenuGroup) {
		this.modulesMenuGroup = modulesMenuGroup;
	}

	/**
	 * Hide the module. The module will still be available if already
	 * in a configuration, but will not appear in the add modules menus. 
	 * @param hidden true if hidden. 
	 */
	public void setHidden(boolean hidden) {
		this.hidden  = hidden;
	}

	/**
	 * Check whether the module is hidden. Hidden modules will still be available if already
	 * in a configuration, but will not appear in the add modules menus. 
	 * @return true if hidden. 
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Add a GUI compatibility flag. These are flags in PAMGuiManager e.g. PAMGUIManager.swing
	 * @param the GUI flag. 
	 */
	public void addGUICompatabilityFlag(int flag) {
		this.guiCombatibility.add(flag);
	}
	
	/**
	 * Get the number of GUI flags. These defined which GUI's the Module
	 * is compatible with. 
	 * @return the number of GUI flags.
	 */
	public int getNGUIFlags() {
		return guiCombatibility.size();
	}
	
	/**
	 * Get the GUI flag at the specified index. 
	 * @param the index.
	 */
	public int getGUICompatabilityFlag(int index) {
		return guiCombatibility.get(index);
	}



	
	

}
