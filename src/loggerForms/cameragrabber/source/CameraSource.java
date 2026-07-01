package loggerForms.cameragrabber.source;

import java.awt.Frame;

import PamView.dialog.PamDialogPanel;
import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.GrabberProcess;

/**
 * interface for different camera sources, e.g. webcams, network devices, etc. 
 */
public abstract class CameraSource {

	protected GrabberProcess grabberProcess;
	
	protected int cameraIndex;

	/**
	 * @param grabberProcess
	 * @param cameraParams
	 */
	public CameraSource(GrabberProcess grabberProcess, int cameraIndex) {
		super();
		this.grabberProcess = grabberProcess;
		this.cameraIndex = cameraIndex;
	}
	
	/**
	 * Prepare and start process. 
	 * @param cameraParams
	 * @return
	 */
	public abstract boolean prepare(CameraParams cameraParams) throws Exception;
	
	/**
	 * Stop acquiring
	 * @return
	 */
	public abstract boolean shutdown();
	
//	/**
//	 * Get the params. Ideally never null. 
//	 * @return
//	 */
//	public CameraParams getParams() {
//		return cameraParams;
//	}
}
