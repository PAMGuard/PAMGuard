package loggerForms.cameragrabber;

import java.awt.image.BufferedImage;

import loggerForms.cameragrabber.data.CameraDataUnit;

public class GrabberNotification {

	public enum Type {NEWCONFIG, TESTIMAGE, GRABBEDIMAGE}
		
	private Type type;

	private CameraDataUnit cameraDataUnit;

	/**
	 * @param type
	 * @param image
	 */
	public GrabberNotification(Type type, CameraDataUnit cameraDataUnit) {
		super();
		this.type = type;
		this.cameraDataUnit = cameraDataUnit;
	}

	/**
	 * @param type
	 */
	public GrabberNotification(Type type) {
		super();
		this.type = type;
	}

	/**
	 * @return the cameraDataUnit
	 */
	public CameraDataUnit getCameraDataUnit() {
		return cameraDataUnit;
	}

	public Type getType() {
		return type;
	}
}
