package loggerForms.cameragrabber.source;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import com.github.sarxos.webcam.Webcam;

import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.GrabberProcess;

public class WebcamSource extends CameraSource {

	private Timer timer;

	private CameraParams cameraParams;

	private static final int INTERVAL = 1000;

	private Webcam webCam;

	public WebcamSource(GrabberProcess grabberProcess, int cameraIndex) {
		super(grabberProcess, cameraIndex);
		timer = new Timer(INTERVAL, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				capturePhoto();
			}
		});
	}

	protected void capturePhoto() {
		if (webCam == null) {
			return;
		}
		try {
			BufferedImage image = webCam.getImage();
			grabberProcess.takeFrame(this.cameraIndex, this, image);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public boolean prepare(CameraParams cameraParams) throws Exception {
		this.cameraParams =cameraParams;
		webCam = Webcam.getWebcamByName(cameraParams.cameraName);
		if (webCam == null) {
			throw new Exception("Camera " + cameraParams.cameraName + " cannot be opened");
		}
		try {
			Dimension d = cameraParams.dimension;
			if (d != null) {
				webCam.setViewSize(d);
			}
			webCam.open();
		}
		catch (Exception e) {
			throw e;
		}
		timer.start();
		return true;
	}

	@Override
	public boolean shutdown() {
		timer.stop();
		webCam.close();
		return true;
	}


}
