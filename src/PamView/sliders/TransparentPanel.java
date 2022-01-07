package PamView.sliders;

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;

public class TransparentPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public TransparentPanel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TransparentPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public TransparentPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public TransparentPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		Rectangle r = g.getClipBounds();
		g.fillRect(r.x, r.y, r.width, r.height);
	}
}
