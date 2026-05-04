package loggerForms.cameragrabber;

import java.awt.image.BufferedImage;

public class GrabberNotification {

	public enum Type {NEWCONFIG, TESTIMAGE, GRABBEDIMAGE}
	
	private BufferedImage image;
	
	private Type type;

	/**
	 * @param type
	 * @param image
	 */
	public GrabberNotification(Type type, BufferedImage image) {
		super();
		this.type = type;
		this.image = image;
	}

	/**
	 * @param type
	 */
	public GrabberNotification(Type type) {
		super();
		this.type = type;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Type getType() {
		return type;
	}
}
