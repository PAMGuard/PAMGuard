package PamView.dialog;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JLabel;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

/**
 * Extension of JLabel to use standard PAMGUARD colours
 * <p> generally not used in dialogs which use the default
 * colours, but should be used in 
 * preference to JLabel in on screen information. 
 * 
 * @author Doug Gillespie
 *
 */
public class PamLabel extends JLabel implements ColorManaged {

	public PamLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public PamLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public PamLabel() {
		super();
	}

	public PamLabel(Icon image) {
		super(image);
	}

	public PamLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public PamLabel(String text) {
		super(text);
	}

	public static final PamColor defaultColor = PamColor.BORDER;
	
	private PamColor labelColor = defaultColor;
	
	public PamColor getDefaultColor() {
		return labelColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.labelColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return labelColor;
	}
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
	}
 
	
}
