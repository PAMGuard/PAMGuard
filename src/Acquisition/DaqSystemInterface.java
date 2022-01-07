package Acquisition;

import PamModel.CommonPluginInterface;
import PamModel.PamModel;
import PamModel.PamModuleInfo;

/**
 * Interface for External DAQ Systems
 * 
 * @author MO
 *
 */
public interface DaqSystemInterface extends CommonPluginInterface {
	
	/**
	 * Creates a new instance of the DAQ System controller, the class that extends
	 * DaqSystem.class.  The AcquisitionControl object that gets passed to the
	 * interface provides the DAQ System with a link to hardware information
	 * e.g. channel list, sample rate, etc. 
	 * <p>
	 * This field cannot be null.
	 * 
	 * @param the AcquisitionControl object
	 * @return the DaqSystem object
	 */
	public DaqSystem createDAQControl(AcquisitionControl acObject);
}
