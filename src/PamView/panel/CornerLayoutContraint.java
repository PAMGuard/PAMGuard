package PamView.panel;

import java.awt.GridBagConstraints;
import java.io.Serializable;

public class CornerLayoutContraint  implements Serializable, Cloneable {

	static public final long serialVersionUID = 0;
	

	static public final int PAGE_END = GridBagConstraints.PAGE_END;
	static public final int PAGE_START = GridBagConstraints.PAGE_START;
	static public final int LINE_END = GridBagConstraints.LINE_END;
	static public final int LINE_START = GridBagConstraints.LINE_START;
	static public final int FIRST_LINE_START = GridBagConstraints.FIRST_LINE_START;
	static public final int FIRST_LINE_END = GridBagConstraints.FIRST_LINE_END;
	static public final int LAST_LINE_END = GridBagConstraints.LAST_LINE_END;
	static public final int LAST_LINE_START = GridBagConstraints.LAST_LINE_START;
	static public final int CENTER = GridBagConstraints.CENTER;
	static public final int FILL = GridBagConstraints.REMAINDER;
	static public final int EAST = GridBagConstraints.EAST;
	static public final int NORTH = GridBagConstraints.NORTH;
	
	public int anchor = FIRST_LINE_START;

	@Override
	protected CornerLayoutContraint clone() {
		try {
			return (CornerLayoutContraint) super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * Get the horizontal positioning for a particular
	 * anchor. 
	 * @param anchor
	 * @return -1 = left, 1 = right, 0 = middle or undefined
	 */
	public static int getHorizontalAlign(int anchor) {
		switch (anchor) {
		case PAGE_START:
		case LINE_START:
		case FIRST_LINE_START:
		case LAST_LINE_START:
			return -1;
		case PAGE_END:
		case LINE_END:
		case FIRST_LINE_END:
		case LAST_LINE_END:
			return 1;
		default:
			return 0;
		}
	}
	/**
	 * Get the vertical positioning for a particular
	 * anchor. 
	 * @param anchor
	 * @return -1 = top, 1 = bottom, 0 = middle or undefined
	 */
	public static int getVerticalAlign(int anchor) {
		switch (anchor) {
		case PAGE_START:
		case FIRST_LINE_END:
		case FIRST_LINE_START:
			return -1;
		case PAGE_END:
		case LAST_LINE_END:
		case LAST_LINE_START:
			return 1;
		case LINE_START:
		case LINE_END:
			return 0;
		default:
			return 0;
		}
	}
	
}
