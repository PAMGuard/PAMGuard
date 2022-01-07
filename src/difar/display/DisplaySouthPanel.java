package difar.display;

import java.awt.BorderLayout;

import PamView.panel.PamPanel;
import difar.DifarControl;

public class DisplaySouthPanel extends PamPanel {

	public DisplaySouthPanel(DifarControl difarControl) {
		super();
		this.setLayout(new BorderLayout());
//		boolean isViewer = difarControl.isViewer();

		this.add(BorderLayout.NORTH, difarControl.getDemuxProgressDisplay().getComponent());
		this.add(BorderLayout.CENTER, difarControl.getDifarGram().getComponent());
	}
}
