package PamView.dialog;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

import PamView.component.PamSettingsIconButton;

/**
 * Standard settings button with the little cogwheel for use throughout Swing components. 
 * @author dg50
 *
 */
public class SettingsButton extends JButton {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SettingsButton() {
		super(makeIcon());
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

}
