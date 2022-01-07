package printscreen;

import java.awt.image.BufferedImage;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

public class PrintScreenDataUnit extends PamDataUnit implements PamDetection {

	private String imageFile;
	private BufferedImage screenImage;
	private int frameNumber;

	/**
	 * @param timeMilliseconds
	 */
	public PrintScreenDataUnit(long timeMilliseconds, int frameNumber, String imageFile, BufferedImage image) {
		super(timeMilliseconds);
		this.frameNumber = frameNumber;
		this.imageFile = imageFile;
		this.screenImage = image;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#getSummaryString()
	 */
	@Override
	public String getSummaryString() {
		return super.getSummaryString();
	}

	/**
	 * @return the imageFile
	 */
	public String getImageFile() {
		return imageFile;
	}

	/**
	 * @param imageFile the imageFile to set
	 */
	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * @return the screenImage
	 */
	public BufferedImage getScreenImage() {
		return screenImage;
	}

	/**
	 * @param screenImage the screenImage to set
	 */
	public void setScreenImage(BufferedImage screenImage) {
		this.screenImage = screenImage;
	}

	/**
	 * @return the frameNumber
	 */
	public int getFrameNumber() {
		return frameNumber;
	}

	

}
