package PamView.dialog;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;

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
	
	private static ImageIcon makeIcon() {
		return new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
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
