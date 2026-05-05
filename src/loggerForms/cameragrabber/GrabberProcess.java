package loggerForms.cameragrabber;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
import loggerForms.cameragrabber.GrabberNotification.Type;
import loggerForms.cameragrabber.data.CameraDataUnit;

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
		if (camera == null) {
			return;
		}
		BufferedImage image = camera.takePhoto();
		if (image == null) {
			return;
		}

		GrabberParams gp = cameraGrabber.getGrabberParams();
		if (gp.timeStampImages) {
			stampImage(image, now);
		}
		
		CameraDataUnit cdu = new CameraDataUnit(now, cameraIndex, true, image, null);
		cameraGrabber.notifyObservers(new GrabberNotification(Type.TESTIMAGE, cdu));
		
		if (shouldStore(now, cameraIndex)) {
			storeImage(cdu);
		}
	}

	/**
	 * Write a timestamp into the corner of the image. 
	 * @param image
	 * @param now
	 */
	private void stampImage(BufferedImage image, long now) {
		String stamp = PamCalendar.formatDBDateTime(now, true);
		Graphics g = image.getGraphics();
		FontMetrics fm = g.getFontMetrics();
		int border = 2;
		int w = fm.stringWidth(stamp);
		g.setColor(Color.white);
		g.fillRect(0,  0, w+border*2, fm.getHeight() + border*2);

		g.setColor(Color.black);
		g.drawString(stamp, border, border + fm.getHeight());
		
	}

	private void storeImage(CameraDataUnit cdu) {
		GrabberParams gp = cameraGrabber.getGrabberParams();
		long now = cdu.getTimeMilliseconds();
		Camera camera = cameras[cdu.getCameraIndex()];
		BufferedImage image = cdu.getImage();
		File folder = FileFunctions.getStorageFileFolder(gp.outputFolder, now, gp.foldersByDate, true);
		String fileName = PamCalendar.createFileNameMillis(now, folder.getAbsolutePath(), camera.cameraParams.imageInitials+"_", ".png");
		try {
			ImageIO.write(image, "png", new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		CameraDataUnit scdu = new CameraDataUnit(now, cdu.getCameraIndex(), false, image, fileName);
		cameraGrabber.notifyObservers(new GrabberNotification(Type.GRABBEDIMAGE, scdu));
		camera.lastStoreTime = now;
		
	}

	private boolean shouldStore(long now, int cameraIndex) {
		GrabberParams gp = cameraGrabber.getGrabberParams();
		Camera camera = cameras[cameraIndex];
		if (camera == null) {
			return false;
		}
		if (gp.autoGrab && now-camera.lastStoreTime > gp.autoGrabSeconds*1000) {
			return true;
		}
		return false;
	}

	@Override
	public boolean prepareProcessOK() {
		boolean ok = super.prepareProcessOK();
		
		GrabberParams gp = cameraGrabber.getGrabberParams();
		
		stopCameras();
		
		cameras = new Camera[gp.nCameras];
		for (int i = 0; i < gp.nCameras; i++) {
			try {
				cameras[i] = new Camera(i, gp.getCameraParams(i));
			}
			catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}		
		
		/**
		 * Always take data at one per second for preview and storage
		 * in image buffer, then decide separately which ones to store. 
		 */
		timer.setDelay(1000);
		timer.start();
//		if (gp.autoGrab) {
//			timer.start();
//		}
//		else {
//			timer.stop();
//		}
		
		return ok;
	}
	
	private void stopCameras() {
		if (cameras == null) {
			return;
		}
		for (int i = 0; i < cameras.length; i++) {
			if (cameras[i] != null) {
				cameras[i].stop();
			}
		}
	}

	private class Camera {
		
		private CameraParams cameraParams;
		
		private int cameraIndex;
		
		private Webcam webCam;
		
		private long lastStoreTime = 0;

		/**
		 * @param cameraIndex
		 * @param cameraParams
		 */
		public Camera(int cameraIndex, CameraParams cameraParams) throws Exception {
			super();
			this.cameraIndex = cameraIndex;
			this.cameraParams = cameraParams;
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
