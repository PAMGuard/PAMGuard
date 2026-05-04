package loggerForms.cameragrabber;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.Timer;

import com.github.sarxos.webcam.Webcam;

import PamController.PamFolders;
import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamProcess;

/**
 * Do all the grabbing in a PamProcess - we may want to log stuff and it seems consistent
 * with general PAMGuard architecture. 
 */
public class GrabberProcess extends PamProcess {

	private CameraGrabber cameraGrabber;
	
	private Timer timer;
	
	private Camera[] cameras;

	public GrabberProcess(CameraGrabber cameraGrabber) {
		super(cameraGrabber, null);
		this.cameraGrabber = cameraGrabber;
		
		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timerAction();
			}
		});
		
		
	}

	protected void timerAction() {
		GrabberParams gp = cameraGrabber.getGrabberParams();
		int allMap = PamUtils.makeChannelMap(gp.nCameras);
		grabFrames(allMap);
	}
	
	/**
	 * Grab frames from one or more cameras according to the bitmap
	 * @param cameraBitmap
	 */
	public void grabFrames(int cameraBitmap) {
		int n = PamUtils.getNumChannels(cameraBitmap);
		for (int i = 0; i < n; i++) {
			int ind = PamUtils.getNthChannel(i, cameraBitmap);
			grabFrame(ind);
		}
	}
	
	public void grabFrame(int cameraIndex) {
		long now = PamCalendar.getTimeInMillis();
		Camera camera = cameras[cameraIndex];
		BufferedImage image = camera.takePhoto();
		if (image == null) {
			return;
		}
		GrabberParams gp = cameraGrabber.getGrabberParams();
		File folder = FileFunctions.getStorageFileFolder(gp.outputFolder, now, gp.foldersByDate, true);
		String fileName = PamCalendar.createFileNameMillis(now, folder.getAbsolutePath(), camera.cameraParams.imageInitials+"_", ".png");
		try {
			ImageIO.write(image, "png", new File(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean prepareProcessOK() {
		boolean ok = super.prepareProcessOK();
		
		GrabberParams gp = cameraGrabber.getGrabberParams();
		
		stopCameras();
		
		cameras = new Camera[gp.nCameras];
		for (int i = 0; i < gp.nCameras; i++) {
			cameras[i] = new Camera(i, gp.getCameraParams(i));
		}		
		
		timer.setDelay(gp.autoGrabSeconds * 1000);
		if (gp.autoGrab) {
			timer.start();
		}
		else {
			timer.stop();
		}
		
		return ok;
	}
	
	private void stopCameras() {
		if (cameras == null) {
			return;
		}
		for (int i = 0; i < cameras.length; i++) {
			cameras[i].stop();
		}
	}

	private class Camera {
		
		private CameraParams cameraParams;
		
		private int cameraIndex;
		
		private Webcam webCam;

		/**
		 * @param cameraIndex
		 * @param cameraParams
		 */
		public Camera(int cameraIndex, CameraParams cameraParams) {
			super();
			this.cameraIndex = cameraIndex;
			this.cameraParams = cameraParams;
			try {
				webCam = Webcam.getWebcamByName(cameraParams.cameraName);
				Dimension d = cameraParams.dimension;
				if (d != null) {
					webCam.setViewSize(d);
				}
				webCam.open();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void stop() {
			webCam.close();
		}

		public BufferedImage takePhoto() {
			if (webCam == null) {
				return null;
			}
			return webCam.getImage();
		}
		
		public boolean isOk() {
			if (webCam == null) {
				return false;
			}
			
			return true;
		}
		
		
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

}
