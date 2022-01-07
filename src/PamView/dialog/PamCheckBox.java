package PamView.dialog;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamCheckBox extends JCheckBox implements ColorManaged {

	private PamColor defaultColor = PamColor.BORDER;
	
	public PamCheckBox() {
		super();
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}

	public PamCheckBox(Action a) {
		super(a);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamCheckBox(Icon icon) {
		super(icon);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamCheckBox(String text, boolean selected) {
		super(text, selected);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamCheckBox(String text, Icon icon) {
		super(text, icon);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamCheckBox(String text) {
		super(text);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
	}

	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}

}
