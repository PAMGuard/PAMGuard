package PamView.panel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import PamView.ScrollableBufferedImage;

abstract public class JBufferedPanel extends JPanelWithPamKey {

	private ScrollableBufferedImage bufferedImage;

	//	private Rectangle bufferedClipRectangle = new Rectangle();

	private int extraWest, extraNorth, extraSouth, extraEast;

//	public final Object panelLock = new Object();

	public JBufferedPanel() {
		super();
		setDoubleBuffered(true);
	}

	public void rePaintPanel(Graphics g, Rectangle clipRectangle) {
		//		paintPanel(g, clipRectangle);
		repaint(); // cases paintComponent to get called if showing. 
	}

	abstract public void paintPanel(Graphics g, Rectangle clipRectangle);

	@Override
	protected final void paintComponent(Graphics g) {
		



		if (getBufferedImage() == null) {
			return;
		}
		Rectangle r = bufferedImage.getBufferedClipRectangle();
		Graphics bufferedImageGraphics = bufferedImage.getGraphics();
		
		///Anti Aliasing////
//		Graphics2D bufferedImageGraphics2D = (Graphics2D) bufferedImageGraphics;
//		bufferedImageGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//		          RenderingHints.VALUE_ANTIALIAS_ON);
//		bufferedImageGraphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
//		          RenderingHints.VALUE_RENDER_SPEED);
		///////////////
		
		bufferedImageGraphics.setColor(getBackground());
		bufferedImageGraphics.fillRect(r.x, r.y, r.width, r.height);

		paintPanel(bufferedImageGraphics, r);
		
	
		
		super.paintComponent(g);
		
		

		g.drawImage(bufferedImage, 0, 0, this);

	}

	private ScrollableBufferedImage getBufferedImage() {
		if (getHeight() <= 0 || getWidth() <= 0) {
			return null;
		}
		if (bufferedImage == null 
				|| bufferedImage.getWidth()!= getWidth()+extraEast 
				|| bufferedImage.getHeight() != getHeight()) {
			bufferedImage = new ScrollableBufferedImage(getWidth()+extraEast, 
					getHeight(), BufferedImage.TYPE_INT_RGB);
		}
		return bufferedImage;
	}

//	/**
//	 *
//	 * @return the graphics handle for the parent window, not the image.
//	 */
//	public Graphics getComponentGraphics() {
//		return super.getGraphics();
//	}

	public Graphics getImageGraphics() {
		getBufferedImage();
		if (bufferedImage == null) {
			return null;
		}
		return bufferedImage.getGraphics();
	}

	protected void refreshPlot() {

	}

	@Override
	public void repaint(long tm, int x, int y, int w, int h) {
		if (bufferedImage == null) return;
		bufferedImage.setBufferedClipRectangle(x,y,w,h,true);
		//		paintPanel(bufferedImage.getGraphics(), bufferedClipRectangle);
		super.repaint(tm, x, y, w, h);
	}

	@Override
	public void repaint(Rectangle arg0) {
		if (getBufferedImage() == null) {
			return;
		}
		bufferedImage.setBufferedClipRectangle(arg0, true);
		//		paintPanel(bufferedImage.getGraphics(), bufferedClipRectangle);
		super.repaint(arg0);
	}

	@Override
	public void repaint() {
		if (getBufferedImage() == null) {
			return;
		}
		bufferedImage.setBufferedClipRectangle(0,0,getWidth(),getHeight(), true);
		//		paintPanel(bufferedImage.getGraphics(), bufferedClipRectangle);
		super.repaint(100);
	}
	
	@Override
	public void repaint(long tm) {
		if (getBufferedImage() == null) {
			return;
		}
		bufferedImage.setBufferedClipRectangle(0,0,getWidth(),getHeight(), true);
		//		paintPanel(bufferedImage.getGraphics(), bufferedClipRectangle);
		super.repaint(tm);
	}

	//	int calls;
	//	public void fillClipRectangle() {
	//		
	//		if (bufferedImage == null) return;
	//		
	//		if (bufferedClipRectangle == null) return;
	//		
	//		Graphics g = bufferedImage.getGraphics();
	////		if ((++calls % 2) == 0)
	//			g.setColor(getBackground());
	////		else
	////			g.setColor(Color.cyan);
	//		g.fillRect(bufferedClipRectangle.x, bufferedClipRectangle.y, bufferedClipRectangle.width, bufferedClipRectangle.height);
	////		if ((calls % 2) == 1)
	////			g.setColor(getBackground());
	////		else
	////			g.setColor(Color.cyan);
	////		g.drawLine(bufferedClipRectangle.x, bufferedClipRectangle.y, bufferedClipRectangle.x + 20, bufferedClipRectangle.y + 20);
	//	}

	public final void scrollImage(int pixsRight, int pixsUp, boolean repaint) {
		xScrollImage(pixsRight, repaint);
		yScrollImage(pixsUp, repaint);
	}

	public final void xScrollImage(int pixsRight, boolean repaint) {

		if (bufferedImage == null) return;

		bufferedImage.xScrollImage(pixsRight);
		//		
		//		if (pixsRight == 0) return;
		//		int sX;
		//		int dX, dW;
		//		if (Math.abs(pixsRight) >= bufferedImage.getWidth()) {
		//			setBufferedClipRectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), true);			
		//		}
		//		else { 
		//			if (pixsRight < 0) {
		//				// move to the left
		//				sX = -pixsRight;
		//				dX = 0;
		//				dW = bufferedImage.getWidth() - sX;
		//				setBufferedClipRectangle(dW, 0, sX, bufferedImage.getHeight(), true);
		//			}
		//			else {
		//				sX = 0;
		//				dX = pixsRight;
		//				dW = bufferedImage.getWidth() - pixsRight;
		//				setBufferedClipRectangle(0, 0, dX, bufferedImage.getHeight(), true);
		//			}
		//			BufferedImage tempBuff = null;
		//			try {
		//				tempBuff = bufferedImage.getSubimage(sX, 0, dW, getHeight());
		//			}
		//			catch (RasterFormatException ex) {
		//				return;
		//			}
		////			tempBuff.getGraphics().drawLine(0, tempBuff.getHeight(), tempBuff.getWidth()-200, 0);
		//			bufferedImage.getGraphics().drawImage(tempBuff, dX, 0 ,this);
		//		}
		if (repaint) {
			rePaintPanel(bufferedImage.getGraphics(), bufferedImage.getBufferedClipRectangle());
		}
	}
	public final void yScrollImage(int pixsUp, boolean repaint) {

		if (bufferedImage == null) return;

		bufferedImage.yScrollImage(pixsUp);
		//		if (pixsUp == 0) return;
		//		int sY;
		//		int dY, dH;
		//		if (Math.abs(pixsUp) >= bufferedImage.getHeight()) {
		//			setBufferedClipRectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), true);			
		//		}
		//		else {
		//			if (pixsUp > 0) {
		//				// move to the left
		//				sY = pixsUp;
		//				dY = 0;
		//				dH = bufferedImage.getHeight() - pixsUp;
		//				setBufferedClipRectangle(0, dH, getWidth(), sY, true);
		//			}
		//			else {
		//				sY = 0;
		//				dY = -pixsUp;
		//				dH = bufferedImage.getHeight() - pixsUp;
		//				setBufferedClipRectangle(0, 0, getWidth(), dY, true);
		//			}
		//			BufferedImage tempBuff = null;
		//			try {
		//				tempBuff = bufferedImage.getSubimage(0, sY, getWidth(), dH);
		//			}
		//			catch (RasterFormatException ex) {
		//				return;
		//			}
		//			bufferedImage.getGraphics().drawImage(tempBuff, 0, sY ,this);
		//		}
		if (repaint) {
			rePaintPanel(bufferedImage.getGraphics(), bufferedImage.getBufferedClipRectangle());
		}
	}
	//
	//	private void setBufferedClipRectangle(int x, int y, int w, int h, boolean fill) {
	//		if (bufferedClipRectangle == null) {
	//			bufferedClipRectangle = new Rectangle(x, y, w, h);
	//		}
	//		else {
	//			bufferedClipRectangle.setBounds(x, y, w, h);
	//		}
	//		if (fill) {
	//			fillClipRectangle();
	//		}
	//	}
	//
	//	private void setBufferedClipRectangle(Rectangle r, boolean fill) {
	//		bufferedClipRectangle = r;
	//		if (fill) {
	//			fillClipRectangle();
	//		}
	//	}
	//
	//	public Rectangle getBufferedClipRectangle() {
	//		return bufferedClipRectangle;
	//	}

	public int getExtraEast() {
		return extraEast;
	}

	public void setExtraEast(int extraEast) {
		this.extraEast = extraEast;
	}

	public int getExtraNorth() {
		return extraNorth;
	}

	public void setExtraNorth(int extraNorth) {
		this.extraNorth = extraNorth;
	}

	public int getExtraSouth() {
		return extraSouth;
	}

	public void setExtraSouth(int extraSouth) {
		this.extraSouth = extraSouth;
	}

	public int getExtraWest() {
		return extraWest;
	}

	public void setExtraWest(int extraWest) {
		this.extraWest = extraWest;
	}


}
