package PamController.command;

import Acquisition.AcquisitionControl;
import PamController.PamController;

//ST: add monitor for daq buffer. 
public class BufferCommand extends ExtCommand{

	public BufferCommand() {
		super("Buffer", true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String command) {
		if(PamController.getInstance().getPamStatus()==PamController.PAM_RUNNING && AcquisitionControl.getControllers()!=null && AcquisitionControl.getControllers().size()>0) {
			return String.valueOf(AcquisitionControl.getControllers().get(0).getAcquisitionProcess().getBufferSeconds());
		}
		return "0";
	}

}
