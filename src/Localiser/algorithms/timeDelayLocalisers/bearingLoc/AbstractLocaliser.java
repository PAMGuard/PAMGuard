package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Abstract Localiser class. 
 * 
 * An abstact localiser is primarily something which can be added to any 
 * DataBlock and will handle localisation of any data unit added or updated
 * in that datablock before those data are sent off for saving.  
 * @author Doug Gillespie
 *
 */
public abstract class AbstractLocaliser<T extends PamDataUnit> {

	/**
	 * Datablock providing data for this localistion. 
	 */
	private PamDataBlock<T> dataBlock;
	
	/**
	 * flag to say to automatically run localisation on every update. 
	 */
	private boolean runOnUpdate;
	
	/**
	 * flag to run localisation whenever data are added. 
	 */
	private boolean runOnAdd;
	
	public AbstractLocaliser(PamDataBlock<T> dataBlock) {
		this.dataBlock = dataBlock;
	}
	
	/**
	 * Localise a data unit. 
	 * <p>Localisation results will be added to the data unit itself
	 * in the AbstractLocalisation field. 
	 * @param dataUnit data unit to localise
	 * @return true if localisation was successful. 
	 */
	abstract public boolean localiseDataUnit(T dataUnit);
	
	/**
	 * @return a name for the localiser for use in dialogs, menus, etc. 
	 */
	abstract public String getLocaliserName();

	/**
	 * @return the runOnUpdate
	 */
	public boolean isRunOnUpdate() {
		return runOnUpdate;
	}

	/**
	 * @param runOnUpdate the runOnUpdate to set
	 */
	public void setRunOnUpdate(boolean runOnUpdate) {
		this.runOnUpdate = runOnUpdate;
	}

	/**
	 * @return the runOnAdd
	 */
	public boolean isRunOnAdd() {
		return runOnAdd;
	}

	/**
	 * @param runOnAdd the runOnAdd to set
	 */
	public void setRunOnAdd(boolean runOnAdd) {
		this.runOnAdd = runOnAdd;
	}

	/**
	 * @return the dataBlock
	 */
	public PamDataBlock<T> getDataBlock() {
		return dataBlock;
	}
	
}
