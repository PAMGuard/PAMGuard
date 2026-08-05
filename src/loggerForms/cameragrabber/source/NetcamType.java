package loggerForms.cameragrabber.source;

import java.awt.Frame;

import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.GrabberProcess;

public class NetcamType extends CameraSourceType {



	public NetcamType(CameraGrabber cameraGrabber) {
		super(cameraGrabber);
	}

	@Override
	public CameraSource createCameraSource(GrabberProcess grabberProcess, int cameraIndex) {
		return new NetcamSouce(grabberProcess, cameraIndex);
	}

	@Override
	public String getName() {
		return "Network camera (scansapp)";
	}

	@Override
	public CameraSourcePanel getDialogPanel(Frame ower) {
		return new NetcamDialogPanel();
	}

}
