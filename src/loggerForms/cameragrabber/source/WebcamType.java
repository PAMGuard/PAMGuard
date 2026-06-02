package loggerForms.cameragrabber.source;

import java.awt.Frame;

import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.GrabberProcess;

public class WebcamType extends CameraSourceType {
	
	public static final String souceType = "Webcam";

	public WebcamType(CameraGrabber cameraGrabber) {
		super(cameraGrabber);
	}

	@Override
	public CameraSource createCameraSource(GrabberProcess grabberProcess, int cameraIndex) {
		return new WebcamSource(grabberProcess, cameraIndex);
	}

	@Override
	public String getName() {
		return souceType;
	}

	@Override
	public CameraSourcePanel getDialogPanel(Frame ower) {
		return new WebcamDialogPanel(null);
	}

}
