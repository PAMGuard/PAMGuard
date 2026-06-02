package loggerForms.cameragrabber;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

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
import loggerForms.cameragrabber.source.CameraSource;
import loggerForms.cameragrabber.source.CameraSourceType;
import loggerForms.network.LoggerNetworkManager;
import loggerForms.network.LoggerNetworkMessage;
import loggerForms.network.LoggerNetworkReceiver;

/**
 * Do all the grabbing in a PamProcess - we may want to log stuff and it seems consistent
 * with general PAMGuard architecture. 
 */
public class GrabberProcess extends PamProcess {

	private CameraGrabber cameraGrabber;
	
//	private Timer timer;
	
	private Camera[] cameras;
	
	private Random random = new Random();
		

	public GrabberProcess(CameraGrabber cameraGrabber) {
		super(cameraGrabber, null);
		this.cameraGrabber = cameraGrabber;
				
		
//		if (cameraGrabber.isViewer() == false) {
//		timer = new Timer(1000, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				timerAction();
//			}
//		});
//		}
//				unpackCameraMessage(message);
//				return true;
//			}
//		});
//		
	}


//	protected void timerAction() {
//		GrabberParams gp = cameraGrabber.getGrabberParams();
//		int allMap = PamUtils.makeChannelMap(gp.nCameras);
//		grabFrames(allMap);
//	}
	
//	/**
//	 * Grab frames from one or more cameras according to the bitmap
//	 * @param cameraBitmap
//	 */
//	public void grabFrames(int cameraBitmap) {
//		synchronized (this) {
//		int n = PamUtils.getNumChannels(cameraBitmap);
//		for (int i = 0; i < n; i++) {
//			int ind = PamUtils.getNthChannel(i, cameraBitmap);
//			grabFrame(ind);
//		}
//		}
//	}
	
	/**
	 * Call from a camera source to put a new image into the system. 
	 * @param cameraIndex
	 * @param cameraSource
	 * @param image
	 */
	public synchronized void takeFrame(int cameraIndex, CameraSource cameraSource, BufferedImage image) {
		grabFrame(cameraIndex, image);
	}
	
	public synchronized void grabFrame(int cameraIndex, BufferedImage image) {
		long now = PamCalendar.getTimeInMillis();
		if (cameraIndex >= cameras.length) {
			return;
		}
		Camera camera = cameras[cameraIndex];
		if (camera == null) {
			return;
		}
		if (image == null) {
			return;
		}

		GrabberParams gp = cameraGrabber.getGrabberParams();
		if (gp.timeStampImages) {
			stampImage(image, now);
		}
		
		/**
		 * Send out the image to the preview pane on the display (and to anything else
		 * that wants this notification
		 */
		CameraDataUnit cdu = new CameraDataUnit(now, cameraIndex, true, image, null);
		cameraGrabber.notifyObservers(new GrabberNotification(Type.TESTIMAGE, cdu));
		
		
		if (shouldStore(now, cameraIndex)) {
			storeImage(cdu);
		}
		else if (gp.bufferSeconds > 0){
			// only put it in the buffer if it's not to be stored. 
			camera.imageBuffer.add(cdu);
		}
		camera.clearEarlyBuffer(now - gp.bufferSeconds*1000);
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
		camera.setNextStoreTime(now);
		
	}

	private boolean shouldStore(long now, int cameraIndex) {
		GrabberParams gp = cameraGrabber.getGrabberParams();
		Camera camera = cameras[cameraIndex];
		if (camera == null) {
			return false;
		}
		if (camera.inSequence == true) {
			if (now > camera.sequenceEnd) {
				camera.inSequence = false;
			}
			else {
				return true;
			}
		}
		if (gp.autoGrab && now >= camera.nextStoreTime) {
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean prepareProcessOK() {
		boolean ok = super.prepareProcessOK();
		
		if (cameraGrabber.isViewer()) {
			return true;
		}
		
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
//		timer.setDelay(1000);
//		timer.start();
//		if (gp.autoGrab) {
//			timer.start();
//		}
//		else {
//			timer.stop();
//		}
		
		return ok;
	}
	
	private synchronized void stopCameras() {
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
		
//		private Webcam webCam;
				
		private LinkedList<CameraDataUnit> imageBuffer = new LinkedList<>();
		
		private long nextStoreTime;
		
		private boolean inSequence;
		
		private long sequenceEnd;
		
		private CameraSource cameraSource;

		/**
		 * @param cameraIndex
		 * @param cameraParams
		 */
		public Camera(int cameraIndex, CameraParams cameraParams) throws Exception {
			super();
			this.cameraIndex = cameraIndex;
			this.cameraParams = cameraParams;
			CameraSourceType ct = cameraGrabber.findSourceType(cameraParams.sourceType);
			if (ct == null) {
				System.out.println("Unknown camera source type: " + cameraParams.sourceType);
			}
			cameraSource = ct.createCameraSource(GrabberProcess.this, cameraIndex);
			if (cameraGrabber.isViewer() == false) {
				cameraSource.prepare(cameraParams);
			}
//			webCam = Webcam.getWebcamByName(cameraParams.cameraName);
//			if (webCam == null) {
//				throw new Exception("Camera " + cameraParams.cameraName + " cannot be opened");
//			}
//			try {
//				Dimension d = cameraParams.dimension;
//				if (d != null) {
//					webCam.setViewSize(d);
//				}
//				webCam.open();
//			}
//			catch (Exception e) {
//				throw e;
//			}
		}
		
		public void setNextStoreTime(long now) {
			GrabberParams gp = cameraGrabber.getGrabberParams();
			if (gp.autoGrabRandomise) {
				// pick a random number between 1 and 2xgp.autograbseconds
				int maxT = 2 * gp.autoGrabSeconds - 1;
				int t = 1;
				if (maxT > 1) {
					t = random.nextInt(maxT) + 1;
				}
				nextStoreTime = now + t * 1000;
			}
			else {
				nextStoreTime = now + gp.autoGrabSeconds * 1000;
			}
		}

		/**
		 * Remove all images from the buffer that occurred before the min time. 
		 * @param minTime
		 */
		public void clearEarlyBuffer(long minTime) {
			synchronized(imageBuffer) {
				while (imageBuffer.size() > 0) {
					CameraDataUnit cdu = imageBuffer.getFirst();
					if (cdu.getTimeMilliseconds() >= minTime) {
						break;
					}
					imageBuffer.removeFirst();
				}
			}
		}
		
		/**
		 * Store all images in the buffer and empty it at the same time 
		 * @return
		 */
		public int storeBufferedImages() {
			int n = 0;
			synchronized(imageBuffer) {
				while (imageBuffer.size() > 0) {
					CameraDataUnit cdu = imageBuffer.removeFirst();
					storeImage(cdu);
					n++;
				}
			}
			return n;
		}

		public void stop() {
			if (cameraSource != null) {
				cameraSource.shutdown();
			}
		}

//		public BufferedImage takePhoto() {
//			if (webCam == null) {
//				return null;
//			}
//			return webCam.getImage();
//		}
		
//		public boolean isOk() {
//			if (webCam == null) {
//				return false;
//			}
//			
//			return true;
//		}
		
		
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	public void grabOne(int cameraIndex) {
		Camera camera = null;
		try {
			camera = cameras[cameraIndex];
		}
		catch (Exception e) {
			System.out.println("Error grabbing camera frame: " + e.getMessage());
			return;
		}
		if (camera == null) {
			return;
		}
		camera.inSequence = true;
		camera.sequenceEnd = Long.MAX_VALUE;
		
//		grabFrame(cameraIndex);
		
		camera.inSequence = false;
	}

	public void grabSequence(int cameraIndex) {
		Camera camera = null;
		try {
			camera = cameras[cameraIndex];
		}
		catch (Exception e) {
			System.out.println("Error grabbing camera frame: " + e.getMessage());
			return;
		}
		if (camera == null) {
			return;
		}
		camera.inSequence = true;
		camera.sequenceEnd = PamCalendar.getTimeInMillis() + cameraGrabber.getGrabberParams().sequenceSeconds * 1000;
		camera.storeBufferedImages(); // get eveything in the buffer. 
		
	}

}
