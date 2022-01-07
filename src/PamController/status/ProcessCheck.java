package PamController.status;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;

public interface ProcessCheck {
	
	/**
	 * Called from a process every time new input data arrive
	 * @param obs PamObservable (always a PAMDataBlock)
	 * @param data Data unit
	 */
	public void newInput(PamObservable obs, PamDataUnit data);
	
	/**
	 * Called from a process every time new data are added to the output data block
	 * @param obs PamObservable (always a PAMDataBlock)
	 * @param data Data unit
	 */
	public void newOutput(PamObservable obs, PamDataUnit data);
	
	/**
	 * Get the process status. Note that calling this will probably 
	 * reset some counters, so don't call this multiple times in quick 
	 * succession. 
	 * @return the status of this process
	 */
	public ModuleStatus getStatus();

}
