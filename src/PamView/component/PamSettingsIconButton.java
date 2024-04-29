package PamView.component;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

public class PamSettingsIconButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	private static final ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
	private static final FontIcon settingsIcon =  FontIcon.of(MaterialDesignC.COG, 20, Color.DARK_GRAY);

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
