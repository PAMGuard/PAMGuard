package PamView.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JScrollPane;

import PamController.PamController;

/**
 * Scroll pane for which we can set a maximum height in characters for the standard font. 
 * @author Dougl
 *
 */
public class DialogScrollPane extends JScrollPane {

	private static final long serialVersionUID = 1L;

	private int maxHeight;

	public DialogScrollPane(Component view, int maxHeightChars) {
		super(view, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Font font = getFont();
		Graphics g = PamController.getMainFrame().getGraphics();
		this.maxHeight = maxHeightChars*12;
		try {
			if (font != null && g != null) {
				this.maxHeight = g.getFontMetrics().getHeight()*maxHeightChars;
			}
		}
		catch (Exception e) {
			
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.height = Math.min(dim.height, maxHeight);
		return dim;
	}


}
