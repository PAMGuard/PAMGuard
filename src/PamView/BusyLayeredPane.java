package PamView;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class BusyLayeredPane extends JPanel  {

	public BusyLayeredPane(JLayeredPane parentFrame) {
		super();
		setBackground(Color.RED);
		setOpaque(true);
//		parentFrame.setComponentZOrder(this, 0);
		setForeground(Color.GREEN);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

    /** The default dims the blocked component. */
    @Override
	public void paint(Graphics g) {
        Color bg = getBackground();
        Color c = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 128);
        Rectangle r = getBounds();
        g = g.create();
        g.setColor(c);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.dispose();
    }

}
