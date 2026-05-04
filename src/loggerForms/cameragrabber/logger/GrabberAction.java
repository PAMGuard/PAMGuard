package loggerForms.cameragrabber.logger;

import loggerForms.LoggerForm;
import loggerForms.actions.LoggerAction;
import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.controls.LoggerControl;

public class GrabberAction extends LoggerAction {

	private CameraGrabber cameraGrabber;

	public GrabberAction(CameraGrabber cameraGrabber) {
		super(cameraGrabber.getUnitName(), "Grab still frames from camera");
		this.cameraGrabber = cameraGrabber;
	}

	@Override
	public boolean runAction(LoggerForm loggerForm, LoggerControl loggerControl) {
		return false;
	}

}
