package PamView.dialog;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JButton;

import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamButtonAlpha extends JButton {

	private static final long serialVersionUID = 1L;

		Color background=new Color(222,222,222,220);
		
		Color highlight2=PamColors.getInstance().getColor(PamColor.HIGHLIGHT_ALPHA);

		

		public PamButtonAlpha(String string, Icon imageIcon) {
			super(string,imageIcon);
//			this.setOpaque(false);
			this.setContentAreaFilled(false);

		}
		
		public PamButtonAlpha(String string) {
			super(string);
//			this.setOpaque(false);
			this.setContentAreaFilled(false);
		}

		public PamButtonAlpha(Icon imageIcon) {
			super(imageIcon);
			this.setContentAreaFilled(false);
		}

		@Override
		public void paintComponent(Graphics g) {
				Color oldCol=g.getColor();
				if (getModel().isRollover()){
					g.setColor(highlight2);
				}
				else{
					g.setColor(background);
				}
		        Rectangle r = g.getClipBounds();
		        g.fillRect(r.x, r.y, r.width, r.height);
		        g.setColor(oldCol);
		        super.paintComponent(g);
		}
		
		public Color getHighlight() {
			return highlight2;
		}

		public void setHighlight(Color highlight) {
			this.highlight2 = highlight;
		}
		
		public Color getBackground() {
			return background;
		}

		public void setBackground(Color background) {
			this.background = background;
		}
		
		
		
	}


