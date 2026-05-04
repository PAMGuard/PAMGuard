package loggerForms.cameragrabber.swing;

import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.panel.PamPanel;
import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.GrabberNotification;
import loggerForms.cameragrabber.GrabberObserver;

public class GrabberPanel implements GrabberObserver {

	private CameraGrabber cameraGrabber;
	
	private JPanel mainPanel;

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
		
	}

}
