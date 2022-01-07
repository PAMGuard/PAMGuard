package PamView.symbol;

import java.awt.Color;
import java.io.Serializable;

/**
 * Simple line class. 
 * @author Jamie Macaulay
 *
 */
@Deprecated
public class LineData  implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	private Color lineColor;

	public LineData(Color lineColor) {
		this.lineColor=lineColor; 
	}

	/**
	 * @return the lineColor
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * @param lineColor the lineColor to set
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}
	
	@Override
	public LineData clone() {
		try {
			return (LineData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
 

}
