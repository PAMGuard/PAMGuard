package PamView.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ChannelListScroller extends JScrollPane {

	private int maxHeight = 210;
	/**
	 * @param view
	 * @param vsbPolicy
	 * @param hsbPolicy
	 */
	public ChannelListScroller(Component view) {
		super(new NorthPanel(view), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
//		setPreferredSize(new Dimension(0,210));
		setBorder(null);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.height = Math.min(dim.height, maxHeight);
		return dim;
	}
	


}
