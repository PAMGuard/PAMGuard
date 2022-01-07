package PamView;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

public class ScrollableBufferedImage extends BufferedImage {

	private Rectangle bufferedClipRectangle;
	
	private Component parentComponent;
	
//	public ScrollableBufferedImage(ColorModel arg0, WritableRaster arg1, boolean arg2, Hashtable<?, ?> arg3) {
//		super(arg0, arg1, arg2, arg3);
//		// TODO Auto-generated constructor stub
//	}
//
//	public ScrollableBufferedImage(int arg0, int arg1, int arg2, IndexColorModel arg3) {
//		super(arg0, arg1, arg2, arg3);
//		// TODO Auto-generated constructor stub
//	}

	public ScrollableBufferedImage(int width, int height, int colorModel) {
		super(width, height, colorModel);
		setBufferedClipRectangle(0,0,width,height, true);
		// TODO Auto-generated constructor stub
	}

	public final void scrollImage(int pixsRight, int pixsUp) {
		xScrollImage(pixsRight);
		yScrollImage(pixsUp);
	}

	public final void xScrollImage(int pixsRight) {
				
		if (pixsRight == 0) return;
		int sX;
		int dX, dW;
		if (Math.abs(pixsRight) >= getWidth()) {
			setBufferedClipRectangle(0, 0, getWidth(), getHeight(), true);			
		}
		else { 
			if (pixsRight < 0) {
				// move to the left
				sX = -pixsRight;
				dX = 0;
				dW = getWidth() - sX;
				setBufferedClipRectangle(dW, 0, sX, getHeight(), true);
			}
			else {
				sX = 0;
				dX = pixsRight;
				dW = getWidth() - pixsRight;
				setBufferedClipRectangle(0, 0, dX, getHeight(), true);
			}
			BufferedImage tempBuff = null;
			try {
				tempBuff = getSubimage(sX, 0, dW, getHeight());
			}
			catch (RasterFormatException ex) {
				return;
			}
//			tempBuff.getGraphics().drawLine(0, tempBuff.getHeight(), tempBuff.getWidth()-200, 0);
			getGraphics().drawImage(tempBuff, dX, 0 ,null);
		}
//		if (repaint) {
//			rePaintPanel(bufferedImage.getGraphics(), bufferedClipRectangle);
//		}
	}
	public final void yScrollImage(int pixsUp) {
				
		if (pixsUp == 0) return;
		int sY;
		int dY, dH;
		if (Math.abs(pixsUp) >= getHeight()) {
			setBufferedClipRectangle(0, 0, getWidth(), getHeight(), true);			
		}
		else {
			if (pixsUp > 0) {
				// move to the left
				sY = pixsUp;
				dY = 0;
				dH = getHeight() - pixsUp;
				setBufferedClipRectangle(0, dH, getWidth(), sY, true);
			}
			else {
				sY = 0;
				dY = -pixsUp;
				dH = getHeight() - pixsUp;
				setBufferedClipRectangle(0, 0, getWidth(), dY, true);
			}
			BufferedImage tempBuff = null;
			try {
				tempBuff = getSubimage(0, sY, getWidth(), dH);
			}
			catch (RasterFormatException ex) {
				return;
			}
			getGraphics().drawImage(tempBuff, 0, sY ,null);
		}
//		if (repaint) {
//			rePaintPanel(bufferedImage.getGraphics(), bufferedClipRectangle);
//		}
	}
	public void setBufferedClipRectangle(int x, int y, int w, int h, boolean fill) {
		if (bufferedClipRectangle == null) {
			bufferedClipRectangle = new Rectangle(x, y, w, h);
		}
		else {
			bufferedClipRectangle.setBounds(x, y, w, h);
		}
		if (fill) {
			fillClipRectangle();
		}
	}

	public void setBufferedClipRectangle(Rectangle r, boolean fill) {
		bufferedClipRectangle = r;
		if (fill) {
			fillClipRectangle();
		}
	}

	public Rectangle getBufferedClipRectangle() {
		return bufferedClipRectangle;
	}

	int calls;
	public void fillClipRectangle() {
				
		if (bufferedClipRectangle == null) return;
		
		Graphics g = getGraphics();
//		if ((++calls % 2) == 0)
		if (parentComponent != null) {
			g.setColor(parentComponent.getBackground());
		}
//		else
//			g.setColor(Color.cyan);
		g.fillRect(bufferedClipRectangle.x, bufferedClipRectangle.y, bufferedClipRectangle.width, bufferedClipRectangle.height);
//		if ((calls % 2) == 1)
//			g.setColor(getBackground());
//		else
//			g.setColor(Color.cyan);
//		g.drawLine(bufferedClipRectangle.x, bufferedClipRectangle.y, bufferedClipRectangle.x + 20, bufferedClipRectangle.y + 20);
	}

	public Component getParentComponent() {
		return parentComponent;
	}

	public void setParentComponent(Component parentComponent) {
		this.parentComponent = parentComponent;
	}

}
