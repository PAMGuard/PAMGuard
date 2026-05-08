package loggerForms.cameragrabber.swing;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.panel.PamPanel;
import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.GrabberNotification;
import loggerForms.cameragrabber.GrabberObserver;
import loggerForms.cameragrabber.GrabberParams;
import loggerForms.cameragrabber.data.CameraDataUnit;

/**
 * Main panel for tab control. May decide to include in a user panel instead
 * at some point, depending on how things go so keep main functionality out of TabPanel
 */
public class GrabberPanel implements GrabberObserver {

	private CameraGrabber cameraGrabber;
	
	private JPanel mainPanel;
	
	private ImagePanel[] previewImages;
	
	private ImagePanel[] caughtImages;

	public GrabberPanel(CameraGrabber cameraGrabber) {
		super();
		this.cameraGrabber = cameraGrabber;
		mainPanel = new PamPanel();
		cameraGrabber.addObserver(this);
	}

	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void notify(GrabberNotification grabberNotification) {
		switch (grabberNotification.getType()) {
		case NEWCONFIG:
			layoutComponents();
			break;
		case GRABBEDIMAGE:
			displayImage(grabberNotification.getCameraDataUnit());
			break;
		case TESTIMAGE:
			displayImage(grabberNotification.getCameraDataUnit());
			break;
		default:
			break;
		
		}
	}

	private void displayImage(CameraDataUnit cameraDataUnit) {
		int camInd = cameraDataUnit.getCameraIndex();
		if (previewImages == null || camInd >= previewImages.length) {
			return;
		}
		if (cameraDataUnit.isPreview()) {
			previewImages[camInd].newImage(cameraDataUnit);
		}
		else {
			caughtImages[camInd].newImage(cameraDataUnit);
		}
	}

	private void layoutComponents() {
		mainPanel.removeAll();
		GrabberParams gp = cameraGrabber.getGrabberParams();
		int nCam = gp.nCameras;
		mainPanel.setLayout(new GridLayout(nCam, 2));
		previewImages = new ImagePanel[nCam];
		caughtImages = new ImagePanel[nCam];
		for (int i = 0; i < nCam; i++) {
			previewImages[i] = new ImagePanel(cameraGrabber, this, i, true);
			caughtImages[i] = new ImagePanel(cameraGrabber, this, i, false);
			mainPanel.add(previewImages[i].getPanel());
			mainPanel.add(caughtImages[i].getPanel());
		}
		mainPanel.invalidate();
	}

}
