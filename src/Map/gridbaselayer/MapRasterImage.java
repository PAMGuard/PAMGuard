package Map.gridbaselayer;

import java.awt.image.BufferedImage;

public class MapRasterImage {

	private double[] latRange;
	private double[] lonRange;
	private BufferedImage image;

	public MapRasterImage(double[] latRange, double[] lonRange, BufferedImage image) {
		this.latRange = latRange;
		this.lonRange = lonRange;
		this.image = image;
	}

	/**
	 * @return the latRange
	 */
	protected double[] getLatRange() {
		return latRange;
	}

	/**
	 * @return the lonRange
	 */
	protected double[] getLonRange() {
		return lonRange;
	}

	/**
	 * @return the image
	 */
	protected BufferedImage getImage() {
		return image;
	}

}
