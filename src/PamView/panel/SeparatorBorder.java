package PamView.panel;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.border.TitledBorder;

public class SeparatorBorder extends TitledBorder {

	private static final long serialVersionUID = 1L;

	public SeparatorBorder(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}
	 public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		 FontMetrics fm = g.getFontMetrics();
		 if (fm != null) {
			 height = fm.getHeight()-EDGE_SPACING*2-1;
		 }
		 else {
			 height = 12;
		 }
		super.paintBorder(c, g, x, y, width, height);   
	 }
}
