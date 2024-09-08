package PamModel;

import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamguardMVC.PamDataBlock;
import javafx.scene.control.ButtonType;
import pamViewFX.PamGuiManagerFX;


public class DependencyManager {

	PamModel pamModel;

	public DependencyManager(PamModel pamModel) {

		this.pamModel = pamModel;

	}

	/**
	 * 
	 * Checks through the data model and tries to find an 
	 * appropriate PamDataBlock. Returns a reference to the 
	 * datablock if it can find one, null otherwise. 
	 * @param parentComponent
	 * @param pamDependent
	 * @return reference to a PamControlledUnit satisfying the dependency
	 */
	public PamControlledUnit checkDependency(Frame parentFrame, PamDependent pamDependent) {

		return checkDependency(parentFrame, pamDependent, false);
		
	}

	/**
	 * 
	 * Checks through the data model and tries to find an 
	 * appropriate PamDataBlock. Returns a reference to the 
	 * datablock if it can find one, null otherwise. 
	 * @param parentComponent
	 * @param pamDependent
	 * @param create create set to true if you want checkDependency to automatically
	 * create required dependencies.
	 * @return reference to a PamDataBlock satisfying the dependency
	 */
	public PamControlledUnit checkDependency(Frame parentFrame, PamDependent pamDependent, boolean create) {

//		System.out.println("HereA 1");
		PamDependency pamDependency = pamDependent.getDependency();

		PamControlledUnit dependency = findDependency(pamDependency);

//		System.out.println("HereA 2");
		if (dependency != null || !create) return dependency;

		/*
		 * No data block of the correct type / data name exists, so 
		 * find the appropriate module info for the default
		 * data provider and create one. 
		 */
		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(pamDependency.getDefaultProvider());
		if (moduleInfo == null) {
			String str = "Cannot find " + pamDependency.getDefaultProvider() + "dependent module information";
			System.out.println(str);
		}
		
//		System.out.println("HereA 3");


		/*
		 * Have found the module that's needed, check with the user that they 
		 * want to go ahead and create it.
		 * I'ts possible that a module of the required type alredy exists 
		 * in which case, can get out now. 
		 */
		String str = "The " + pamDependent.getDependentUserName() + " you are trying to create requires a \n" +
				moduleInfo.getDescription() + " before it can operate \n\n" +
				"Do you wish to go ahead and create a " + moduleInfo.getDescription() +"?";
		
		if (PamGUIManager.isSwing()) {
			if (JOptionPane.showConfirmDialog(parentFrame, str, "Module dependencey manager", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
				return null;
			}
		}
		else if (PamGUIManager.isFX()) {
			if (PamGuiManagerFX.showAlertDialog(pamDependent.getDependentUserName() + " requires a parent module.",str)!= ButtonType.OK){
				// ... user chose OK
				return null;
			}
		}
			
			
		/*
		 * Check dependencies for the module we're about to create
		 */
		if (moduleInfo.getDependency() != null)	checkDependency(parentFrame, moduleInfo, create);

		/*
		 * Then go ahead and create it.
		 */
		PamController.getInstance().addModule(parentFrame, moduleInfo);

		return findDependency(pamDependency);
	}

	/**
	 * Checks through the data model and tries to find an 
	 * appropriate PamDataBlock. Returns a reference to the 
	 * datablock if it can find one, null otherwise. <p>
	 * In some rare instances, such as Logger forms the 
	 * dependence may be on a module rather than a particular
	 * type of data, i.e. the forms need the database, but the
	 * database produces no data units to check against, so 
	 * the first test will fail.  
	 * @param pamDependency
	 * @return reference to a PamDataBlock satisfying the dependency
	 */
	public PamControlledUnit findDependency(PamDependency pamDependency)
	{
		PamControllerInterface pamController = PamController.getInstance();

		ArrayList<PamDataBlock> pamDataBlocks = pamController.getDataBlocks(pamDependency.getRequiredDataType(), true);

		PamDataBlock foundDataBlock = null;
		if (pamDataBlocks != null && pamDataBlocks.size() > 0){		
			PamDataBlock dataBlock;
			for (int i = 0; i < pamDataBlocks.size(); i++) {
				dataBlock = pamDataBlocks.get(i);
				if (dataBlock.getDataName().equals(pamDependency.getDataBlockName())) {
					foundDataBlock = dataBlock;
					break;
				}
			}
			if (foundDataBlock == null) {
				foundDataBlock = pamDataBlocks.get(0);
			}
			if (foundDataBlock != null) {
				// these conditions should always be satisfied since 
				// it found the datablock by looking in the list of units and processes. 
				if (foundDataBlock.getParentProcess() != null) {
					return foundDataBlock.getParentProcess().getPamControlledUnit();
				}
			}
		}

		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(pamDependency.getDefaultProvider());
		if (moduleInfo == null) {
			String str = "Cannot find " + pamDependency.getDefaultProvider() + "dependent module information";
			System.out.println(str);
		}
		Class modClass= moduleInfo.getModuleClass();
		PamControlledUnit existingModule = PamController.getInstance().findControlledUnit(modClass, null);

		return existingModule;

	}
}
