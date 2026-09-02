package PamView.component;

import javax.swing.JButton;
import org.kordamp.ikonli.Ikon;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

public class PamSettingsIconButton extends JButton {
	

	private static final long serialVersionUID = 1L;

	public static final int SMALL_SIZE = 17;

	public static final int NORMAL_SIZE = 20;
	
	/**
	 * The ikon enum for the the setting button
	 */
	public static Ikon SETTINGS_IKON = MaterialDesignC.COG;

	
//	private static final ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
	private static final PamFontIcon settingsIcon = PamFontIcon.of(SETTINGS_IKON, NORMAL_SIZE);

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
