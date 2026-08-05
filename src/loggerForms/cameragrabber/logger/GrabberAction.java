package loggerForms.cameragrabber.logger;

import loggerForms.LoggerForm;
import loggerForms.actions.LoggerAction;
import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.GrabberParams;
import loggerForms.controls.LoggerControl;

public class GrabberAction extends LoggerAction {

	private CameraGrabber cameraGrabber;

	public enum GRABTYPE {FRAME, SEQUENCE};

	private GRABTYPE grabType;
	private int cameraIndex;

	/**
	 * Create a camera grab action - there may be several options for different cameras and numbers of frames
	 * <br> Better off using static createAction function
	 * @param cameraGrabber
	 * @param cameraIndex
	 * @param actionName
	 * @param grabType
	 */
	public GrabberAction(CameraGrabber cameraGrabber, int cameraIndex, String actionName, GRABTYPE grabType) {
		super(cameraGrabber, actionName, "Grab still frames from camera");
		this.cameraGrabber = cameraGrabber;
		this.cameraIndex = cameraIndex;
		this.grabType = grabType;
	}

	/**
	 * Create a camera grab action
	 * @param cameraGrabber 
	 * @param cameraIndex
	 * @param grabType
	 * @return
	 */
	public static GrabberAction createAction(CameraGrabber cameraGrabber, int cameraIndex, GRABTYPE grabType) {
		String name;
		GrabberParams gp = cameraGrabber.getGrabberParams();
		CameraParams cs = gp.getCameraParams(cameraIndex);
		if (grabType == GRABTYPE.FRAME) {
			name = String.format("Grab a single frame from camera %d - %s", cameraIndex, cs.imageInitials);
		}
		else {
			name = String.format("Grab frame sequence from camera %d - %s", cameraIndex, cs.imageInitials);
		}
		return new GrabberAction(cameraGrabber, cameraIndex, name, grabType);
	}

	@Override
	public boolean runAction(LoggerForm loggerForm, LoggerControl loggerControl) {
		try {
			if (grabType == GRABTYPE.FRAME) {
				cameraGrabber.getGrabberProcess().grabOne(cameraIndex);
			}
			else {
				cameraGrabber.getGrabberProcess().grabSequence(cameraIndex);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}

}
