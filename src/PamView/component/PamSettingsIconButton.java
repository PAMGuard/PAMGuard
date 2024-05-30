package PamView.component;

import java.awt.Color;

import javax.swing.JButton;
import org.kordamp.ikonli.Ikon;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

public class PamSettingsIconButton extends JButton {
	

	private static final long serialVersionUID = 1L;

	public static final int SMALL_SIZE = 17;

	public static final int NORMAL_SIZE = 20;
	
	/**
	 * The ikon enum for the the setting button
	 */
	public static Ikon SETTINGS_IKON = MaterialDesignC.COG;

	
//	private static final ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
	private static final FontIcon settingsIcon =  FontIcon.of(SETTINGS_IKON, NORMAL_SIZE, Color.DARK_GRAY);

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
