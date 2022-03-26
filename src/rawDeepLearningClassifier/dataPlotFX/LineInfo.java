package rawDeepLearningClassifier.dataPlotFX;

import java.io.Serializable;

import javafx.scene.paint.Color;

/**
 * The line info. 
 * 
 * @author Jamie Macaulay
 *
 */
public class LineInfo implements Serializable, Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LineInfo(boolean enabled, Color color) {
		this.enabled = enabled;
		this.color = color; 
	}

	/**
	 * The colour of the line
	 */
	public Color color = Color.DODGERBLUE;
	
	/**
	 * True if the line is enabled (shown on the plot)
	 */
	public boolean enabled = true;
	
	@Override
	protected LineInfo clone() {
		try {
			return (LineInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}