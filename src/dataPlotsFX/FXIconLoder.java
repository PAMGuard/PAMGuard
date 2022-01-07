package dataPlotsFX;

import java.net.URL;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

/**
 * Utility functions to load image files into Canvases to use as icons in 
 * displays, etc. 
 * Typical usage is icon = FXIconLoder.createIcon("Resources/BeamformIcon20.png", 20, 20);
 * @author dg50
 *
 */
public class FXIconLoder {

	/**
	 * Create an icon the same size as the original image, no scaling. 
	 * @param imageResource Image resource 
	 * @return Canvas of the same size as the image. 
	 */
	public static Canvas createIcon(String imageResource) {
		try {
			URL res = ClassLoader.getSystemResource(imageResource);//.getImage();
			Image fxImage = new Image(res.toExternalForm());
			int h = (int) fxImage.getHeight();
			int w = (int) fxImage.getWidth();
			Canvas c = new Canvas(w, h);
			c.getGraphicsContext2D().drawImage(fxImage, 0, 0);
			return c;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Create a canvas with the image scaled / squashed / stretched to a set size. 
	 * @param imageResource Image resource
	 * @param width required width after scaling
	 * @param height required height after scaling
	 * @return Canvas of the determined size. 
	 */
	public static Canvas createIcon(String imageResource, int width, int height) {
		try {
			URL res = ClassLoader.getSystemResource(imageResource);//.getImage();
			Image fxImage = new Image(res.toExternalForm());
			int h = (int) fxImage.getHeight();
			int w = (int) fxImage.getWidth();
			Canvas c = new Canvas(width, height);
			c.getGraphicsContext2D().drawImage(fxImage, 0, 0, w, h, 0, 0, width, height);
			return c;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Create a image from a resource . 
	 * @param imageResource Image resource
	 * @return Canvas of the determined size. 
	 */
	public static Image createImage(String imageResource) {
		try {
			URL res = ClassLoader.getSystemResource(imageResource);//.getImage();
			Image fxImage = new Image(res.toExternalForm());
			return fxImage;
		}
		catch (Exception e) {
			return null;
		}
	}

}
