package PamView.sliders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class PamSliderUI extends BasicSliderUI {


	private Dimension thumbSize=new Dimension(20,20);

	private Color thumbOutline=Color.GRAY; // border of the round blobby bit. 

	private Color thumbFill=new Color(200,200,200); // colour for the round blob of the slider control. 

	public PamSliderUI(JSlider slider) {
		super(slider);
	}

	/**
	 * Returns the size of a thumb.
	 */
	@Override
	protected Dimension getThumbSize() {
		return thumbSize;
	}

	/**
	 * Set the  sizes of the thumbs
	 * @param width
	 * @param height
	 */
	public void setThumbSizes(int width, int height){
		this.thumbSize=new Dimension(width,height);
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

//		Color oldColor = g.getColor();
		Rectangle clipRect = g.getClipBounds();
		if (clipRect.intersects(thumbRect)) {
			paintThumb(g, thumbRect, thumbFill, thumbOutline);
		}
//		g.setColor(oldColor);
	}

	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicSliderUI#paintThumb(java.awt.Graphics)
	 */
	@Override
	public void paintThumb(Graphics g) {
		// Do nothing. Class uses bespoke thumb drawing. 
//		super.paintThumb(g);
	}

	public void paintThumb(Graphics g, Rectangle thumbBounds, Color fillColour, Color lineColour) {  
		int w = thumbBounds.width;
		int h = thumbBounds.height;      

		// Create graphics copy.
		Graphics2D g2d = (Graphics2D) g.create();

		// Create default thumb shape.
		Shape thumbShape = createThumbShape(w - 1, h - 1);

		// Draw thumb.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(thumbBounds.x, thumbBounds.y);

		g2d.setColor(fillColour);
		g2d.fill(thumbShape);

		g2d.setColor(lineColour);
		g2d.draw(thumbShape);

		// Dispose graphics.
		g2d.dispose();
	}
	
	/**
	 * Returns a Shape representing a thumb.
	 */
	private Shape createThumbShape(int width, int height) {
		// Use circular shape.
		Ellipse2D shape = new Ellipse2D.Double(0, 0, width, height);
		return shape;
	}


}
