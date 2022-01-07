package PamView.dialog;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JRadioButton;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamRadioButton extends JRadioButton implements ColorManaged {

	public PamRadioButton() {
		super();
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamRadioButton(Action a) {
		super(a);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamRadioButton(Icon icon, boolean selected) {
		super(icon, selected);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamRadioButton(Icon icon) {
		super(icon);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamRadioButton(String text, boolean selected) {
		super(text, selected);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamRadioButton(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamRadioButton(String text, Icon icon) {
		super(text, icon);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamRadioButton(String text) {
		super(text);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
	}
}
