package tethys.swing;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Images {

	private static final String imageRes = "tethys/images/Tethys-200.png";
	public static ImageIcon getTethysImage() {
		ImageIcon tethysImage = null;
		try {
			tethysImage = new ImageIcon(ClassLoader.getSystemResource(imageRes));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
//			System.out.println("Unable to load file "  + file.getAbsolutePath());
		}
		return tethysImage;
	}
}
