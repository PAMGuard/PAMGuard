package tethys.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;


public class TethysImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ImageIcon image;
	private int prefWidth;
	private int prefHeight;
	private int size;

	public TethysImagePanel(int size) {
		this.size = size;
		image = Images.getTethysImage();
		if (image != null) {
			prefWidth = image.getIconWidth();
			prefHeight = image.getIconHeight();
		}
//		setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image == null) {
			return;
		}
		int inset = 2;
		g.drawImage(image.getImage(), inset, inset, getWidth()-inset*2, getHeight()-inset*2, 
				0, 0, prefWidth, prefHeight, null);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(size, size);
	}
	
	
	
	

}
