package loggerForms.cameragrabber.source;

import java.awt.Frame;

import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.GrabberProcess;

/**
 * A class that can provide a CameraSource. These always exist, one for each 
 * source type, they create 0:n CameraSources. 
 */
public abstract class CameraSourceType {

	protected CameraGrabber cameraGrabber;


	public CameraSourceType(CameraGrabber cameraGrabber) {
		this.cameraGrabber = cameraGrabber;
	}
	
	/**
	 * @return the cameraGrabber
	 */
	public CameraGrabber getCameraGrabber() {
		return cameraGrabber;
	}
	
	public abstract CameraSource createCameraSource(GrabberProcess grabberProcess, int cameraIndex);

	public abstract CameraSourcePanel getDialogPanel(Frame ower);
	
	public abstract String getName();

	@Override
	public String toString() {
		return getName();
	}

}
