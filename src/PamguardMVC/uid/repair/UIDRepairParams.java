package PamguardMVC.uid.repair;

/**
 * Params needed for the UID repair. 
 * @author dg50
 *
 */
public class UIDRepairParams {

	/**
	 * Output folder for binary data
	 */
	public String newBinaryFolder;
	
	/**
	 * Do an in place repair of files that don't already have a UID
	 */
	public boolean doPartial;

}
