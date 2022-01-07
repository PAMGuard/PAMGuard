package PamView.component;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * A modification of JLabel which can only grow, i.e. does not shrink back and
 * change the layout if shorter text it put into it. 
 * @author dg50
 *
 */
public class FixedLabel extends JLabel {

	private static final long serialVersionUID = 1L;
	
	private int minWidth = 0;

	public FixedLabel() {
		super();
	}

	public FixedLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public FixedLabel(Icon image) {
		super(image);
	}

	public FixedLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public FixedLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public FixedLabel(String text) {
		super(text);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension sz = super.getPreferredSize();
		sz.width = minWidth = Math.max(sz.width, minWidth);
		return sz;
	}

	@Override
	public Dimension getMinimumSize() {		
		Dimension sz = super.getMinimumSize();
		sz.width = minWidth = Math.max(sz.width, minWidth);
		return sz;
	}

	public void resetWidth() {
		minWidth = 0;
	}
}
