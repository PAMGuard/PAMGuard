package warnings;

import java.awt.Color;

import javax.swing.Icon;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;

public class WarningLabel extends PamLabel {

	private int warningLevel = 0;
		
	public WarningLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public WarningLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public WarningLabel() {
	}

	public WarningLabel(Icon image) {
		super(image);
	}

	public WarningLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public WarningLabel(String text) {
		super(text);
	}

	/* (non-Javadoc)
	 * @see PamView.PamLabel#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
//		if (warningLevel == 0) {
			super.setBackground(bg);
//		}
	}

	/**
	 * @return the warningLevel
	 */
	public int getWarningLevel() {
		return warningLevel;
	}

	/**
	 * @param warningLevel the warningLevel to set
	 */
	public void setWarningLevel(int warningLevel) {
//		if (this.warningLevel == warningLevel) {
//			return;
//		}
		this.warningLevel = warningLevel;
		if (warningLevel >= 2) {
//			setDefaultColor(PamColor.WARNINGBORDER);
			setForeground(Color.RED);
		}
		else if (warningLevel == 1) {
			setForeground(Color.BLUE);
		}
		else {
//			setDefaultColor(PamColor.BORDER);
			setForeground(PamColors.getInstance().getForegroudColor(PamColor.AXIS));
		}
//		setBackground(PamColors.getInstance().getColor(getDefaultColor()));
	}

}
