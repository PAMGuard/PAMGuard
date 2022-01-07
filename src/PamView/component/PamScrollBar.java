package PamView.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

import PamView.ColorManaged;
import PamView.ColourScheme;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamScrollBar extends JScrollBar implements ColorManaged {

	private PamScrollBarUI pamScrollBarUI = new PamScrollBarUI();
	public PamScrollBar() {
		setUI(pamScrollBarUI);
	}

	public PamScrollBar(int orientation) {
		super(orientation);
		setUI(pamScrollBarUI);
	}

	public PamScrollBar(int orientation, int value, int extent, int min, int max) {
		super(orientation, value, extent, min, max);
		setUI(pamScrollBarUI);
	}

	/* (non-Javadoc)
	 * @see PamView.ColorManaged#getColorId()
	 */
	@Override
	public PamColor getColorId() {
		return PamColor.BORDER;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		if (pamScrollBarUI != null) {
			pamScrollBarUI.setColours(bg);
		}
		if (bg != null) {
			//			super.setBackground(bg);
		}
	}

	class PamScrollBarUI extends BasicScrollBarUI{

		public void setColours(Color bg) {	
			PamColors pamColors = PamColors.getInstance();
			trackColor = pamColors.getColor(PamColor.BORDER);
			thumbColor = pamColors.getColor(PamColor.BUTTONFACE);
		}

		@Override
		protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
			super.paintTrack(g, c, trackBounds);
		}

		@Override
		protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
			super.paintThumb(g, c, thumbBounds);
		}
	}

}
