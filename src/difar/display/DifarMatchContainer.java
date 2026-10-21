package difar.display;

import java.awt.Component;

import difar.DifarControl;
import userDisplay.UserDisplayComponentAdapter;

/**
 * Wraps the match candidate table so that it can be placed in any user display,
 * or moved into a window of its own.
 */
public class DifarMatchContainer extends UserDisplayComponentAdapter {

	private final DifarControl difarControl;

	private final DifarMatchPanel panel;

	public DifarMatchContainer(DifarControl difarControl) {
		this.difarControl = difarControl;
		panel = new DifarMatchPanel(difarControl);
	}

	@Override
	public Component getComponent() {
		return panel;
	}

	/** @return the panel itself, which is also a DIFAR display unit. */
	public DifarMatchPanel getMatchPanel() {
		return panel;
	}

	@Override
	public void openComponent() {
	}

	@Override
	public void closeComponent() {
	}

	@Override
	public void notifyModelChanged(int changeType) {
	}

	@Override
	public String getFrameTitle() {
		return difarControl.getUnitName() + " match candidates";
	}
}
