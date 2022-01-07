package difar.display;

import java.awt.BorderLayout;

import difar.DifarControl;
import PamController.PamController;
import PamView.panel.PamPanel;


/**
 * Standard layout for the queue part of the display
 * @author doug
 *
 */
public class DisplayNorthPanel extends PamPanel {

	public DisplayNorthPanel(DifarControl difarControl) {
		super();
		this.setLayout(new BorderLayout());
		boolean isViewer = difarControl.isViewer();
		if (isViewer == false) {
			this.add(BorderLayout.NORTH, difarControl.getInternalActionsPanel().getComponent());
		}
		this.add(BorderLayout.CENTER, difarControl.getDifarQueue().getComponent());
	}
}
