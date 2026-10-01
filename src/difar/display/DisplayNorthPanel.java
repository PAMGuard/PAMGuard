package difar.display;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;

import difar.DifarControl;
import PamController.PamController;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import javax.swing.SwingUtilities;


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
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Queue", difarControl.getDifarQueue().getComponent());
		tabs.addTab("Saved", difarControl.getSavedClips().getComponent());
		// the viewer opens on what has been saved, as it always has
		tabs.setSelectedIndex(isViewer ? 1 : 0);
		difarControl.getDifarQueue().getClipDataBlock().addObserver(new QueueWatcher(tabs), false);
		this.add(BorderLayout.CENTER, tabs);
	}

	/**
	 * Brings the queue forward when a new clip arrives on it, so a call
	 * marked while looking at saved clips is not hidden.
	 */
	private static class QueueWatcher extends PamObserverAdapter {

		private final JTabbedPane tabs;

		QueueWatcher(JTabbedPane tabs) {
			this.tabs = tabs;
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit dataUnit) {
			SwingUtilities.invokeLater(() -> tabs.setSelectedIndex(0));
		}

		@Override
		public String getObserverName() {
			return "DIFAR queue tab";
		}
	}
}
