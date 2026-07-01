package loggerForms.cameragrabber.data;

import java.awt.image.BufferedImage;

import PamguardMVC.PamDataUnit;

public class CameraDataUnit extends PamDataUnit {

	private boolean preview;
	private BufferedImage image;
	private int cameraIndex;
	private String fileName;

	public CameraDataUnit(long timeMilliseconds, int cameraIndex, boolean preview, BufferedImage image, String fileName) {
		super(timeMilliseconds);
		setChannelBitmap(1<<cameraIndex);
		this.cameraIndex = cameraIndex;
		this.preview = preview;
		this.image = image;
		this.fileName = fileName;
	}

	public boolean isPreview() {
		return preview;
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getCameraIndex() {
		return cameraIndex;
	}

	public String getFileName() {
		return fileName;
	}


}
