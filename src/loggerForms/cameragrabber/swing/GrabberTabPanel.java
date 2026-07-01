package loggerForms.cameragrabber.swing;

import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import PamView.PamTabPanel;
import loggerForms.cameragrabber.CameraGrabber;

/**
 * Tab panel. Does nothing apart from display camera view and grabbed frames. All work 
 * is done in GrabberProcess so that the grabber can run headless if required. 
 */
public class GrabberTabPanel implements PamTabPanel {

	private CameraGrabber cameraGrabber;

	private GrabberPanel grabberPanel;
	
	public GrabberTabPanel(CameraGrabber cameraGrabber) {
		super();
		this.cameraGrabber = cameraGrabber;
		this.grabberPanel = new GrabberPanel(cameraGrabber);
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	@Override
	public JComponent getPanel() {
		// TODO Auto-generated method stub
		return grabberPanel.getPanel();
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}

}
