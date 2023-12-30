package PamView.panel;

import java.io.Serializable;

public class SplitPanePositionData implements Serializable {


	public static final long serialVersionUID = 1L;
	private int position;
	private double propPosition;
	private int height;


	public SplitPanePositionData(int position, double propPosition, int height) {
		super();
		this.position = position;
		this.setHeight(height);
		this.setPropPosition(propPosition);
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the propPosition
	 */
	public double getPropPosition() {
		return propPosition;
	}

	/**
	 * @param propPosition the propPosition to set
	 */
	public void setPropPosition(double propPosition) {
		this.propPosition = propPosition;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
}
