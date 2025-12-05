package PamView.dialog;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;
import PamView.component.PamSettingsIconButton;

/**
 * Standard settings button with the little cogwheel for use throughout Swing components. 
 * @author dg50
 *
 */
public class SettingsButton extends JButton implements ColorManaged {

	private static final long serialVersionUID = 1L;
	private boolean colourManage;

	public SettingsButton() {
		this(false);
	}
	public SettingsButton(boolean colourManage) {
		super(makeIcon());
		this.colourManage = colourManage;
	}
	
	private static Icon makeIcon() {
		return FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.SMALL_SIZE, Color.DARK_GRAY);
//		return new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
	}

	/**
	 * @param a
	 */
	public SettingsButton(Action a) {
		super(a);
		setIcon(makeIcon());
	}

	/**
	 * @param text
	 */
	public SettingsButton(String text) {
		super(text, makeIcon());
	}
	
	@Override
	public PamColor getColorId() {
		return colourManage ? PamColor.BUTTONFACE : null;
	}
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
//		super.putClientProperty("JButton.backgroundColor", Color.RED);
//		super.putClientProperty("JButton.focusedBackgroundColor", Color.GREEN);
//		super.setBackground(Color.RED);
	}

}
