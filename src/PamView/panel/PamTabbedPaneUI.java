package PamView.panel;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Overriding this seems to correclty set the background of PamTabPanel
 * @author dg50
 *
 */
public class PamTabbedPaneUI extends BasicTabbedPaneUI {

	public PamTabbedPaneUI() {
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		// TODO Auto-generated method stub
		super.paint(g, c);
//		g.setColor(Color.magenta);
//		g.drawLine(0, 0, 200, 200);
	}

	@Override
	protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
		// TODO Auto-generated method stub
		super.paintTabArea(g, tabPlacement, selectedIndex);
//		g.setColor(Color.RED);
//		g.drawLine(0, 0, 500, 500);
	}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
			boolean isSelected) {
		// TODO Auto-generated method stub
		super.paintTabBorder(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
			boolean isSelected) {
		// TODO Auto-generated method stub
		super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
	}

}
