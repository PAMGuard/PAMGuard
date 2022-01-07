package PamguardMVC.uid;

public interface UIDManager {

	/**
	 * This gets called just after PAMGuard has loaded all of it's modules and is 
	 * a time for the UID manager to perform checks on the UID system, either retrieving 
	 * the latest values for real time ops, or checking ordering in viewer mode. 
	 * @return true if all checks completed successfully. 
	 */
	public boolean runStartupChecks();
	
	/**
	 * Called just before PAMGurd exits, at the same time that config data are written to the psf, etc.
	 * A time to write UID's to a file  /  the database and do any other cleaning up of the system prior to 
	 * PC running again.  
	 * @return true if all checks completed successfully. 
	 */
	public boolean runShutDownOps();
	
	/**
	 * Loop through the data blocks and try to find matching database and binary file UID information.  Assign the highest UID
	 * value found to the data block.  If the onlyNewDataBlocks flag is true, only perform this operation on data blocks which
	 * have a current UID of 0 (which will happen if the module has just been added). 

	 * @param onlyNewDataBlocks true if the operation is only to be performed on new data blocks
	 */
	public void synchUIDs(boolean onlyNewDataBlocks);		
}
