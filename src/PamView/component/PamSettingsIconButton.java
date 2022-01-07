package PamView.component;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class PamSettingsIconButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
	
	/**
	 * Create a simple square button using the given icon. 
	 */
	public PamSettingsIconButton() {
		super(settingsIcon);
	}
	
	/**
	 * Create a simple button containing both the icon and text. 
	 * @param title
	 */
	public PamSettingsIconButton(String title) {
		super(title, settingsIcon);
	}
}
