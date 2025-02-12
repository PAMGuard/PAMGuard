package Map.gridbaselayer;

import java.awt.image.BufferedImage;

import PamguardMVC.PamDataUnit;

@Deprecated
public class GridDataUnit extends PamDataUnit {

	private double[] latArray;
	private double[] lonArray;
	private BufferedImage image;

	public GridDataUnit(double[] latArray, double[] lonArray, BufferedImage image) {
		super(0);
		setDurationInMilliseconds(System.currentTimeMillis());
		this.latArray = latArray;
		this.lonArray = lonArray;
		this.image = image;
		//this.data = elevation;
	}

	/**
	 * @return the latArray
	 */
	public double[] getLatArray() {
		return latArray;
	}

	/**
	 * @param latArray the latArray to set
	 */
	public void setLatArray(double[] latArray) {
		this.latArray = latArray;
	}

	/**
	 * @return the lonArray
	 */
	public double[] getLonArray() {
		return lonArray;
	}

	/**
	 * @param lonArray the lonArray to set
	 */
	public void setLonArray(double[] lonArray) {
		this.lonArray = lonArray;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}

//	/**
//	 * @return the data
//	 */
//	public MultiArray getData() {
//		return data;
//	}
//
//	/**
//	 * @param data the data to set
//	 */
//	public void setData(MultiArray data) {
//		this.data = data;
//	}


}
