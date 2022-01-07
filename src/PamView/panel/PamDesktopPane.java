package PamView.panel;

import javax.swing.JDesktopPane;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamDesktopPane extends JDesktopPane implements ColorManaged {

	public PamDesktopPane() {
		// TODO Auto-generated constructor stub
	}

	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
		PamColors.getInstance().setColor(this, defaultColor);
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}
}
