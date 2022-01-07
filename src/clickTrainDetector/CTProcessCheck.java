package clickTrainDetector;

import PamController.status.ModuleStatus;
import PamController.status.ProcessCheck;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;

/**
 * Process check for the click train detector. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CTProcessCheck implements ProcessCheck {
	
	private ClickTrainProcess ctProcess;
	
	private ModuleStatus currentStatus = new ModuleStatus(0); 
	
	long count =0; 
	
	/**
	 * The number of clicks to skip before a new check is performed. 
	 */
	private static final int COUNT_CHECK =100; 

	/**
	 * Constructor for the process check,
	 * @param process
	 */
	public CTProcessCheck(ClickTrainProcess ctProcess) {
		this.ctProcess=ctProcess; 
	}

	@Override
	public void newInput(PamObservable obs, PamDataUnit data) {
		//input is click detection.
		count++;
		if (count%100 == 0) {
			//general check 
			completeCheck(); 
		}
	}

	@Override
	public void newOutput(PamObservable obs, PamDataUnit data) {
		// TODO Auto-generated method stub
	}
	
	private void completeCheck() {
		boolean allOK = true; 
		if (!checkSourceChannels()) {
			currentStatus = new ModuleStatus(ModuleStatus.STATUS_ERROR); 
			currentStatus.setMessage("The click train detector has no channels set");
			return; 
		}
	
		currentStatus = new ModuleStatus(ModuleStatus.STATUS_OK); 
		
	}
	
	/**
	 * Check that the input has some source channels. 
	 */
	public boolean checkSourceChannels() {
		int channNumber = PamUtils.getNumChannels(ctProcess.getClickTrainControl().getClickTrainParams().getChannelMap()); 
		
		if (channNumber>=0) return true;
		else return false;
	}

	@Override
	public ModuleStatus getStatus() {
		return currentStatus;
	}



}
