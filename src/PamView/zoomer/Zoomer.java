package PamView.zoomer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JMenuItem;

/**
 * Class for handling multiple zoom and marked areas on a
 * display. <p>
 * The zoomer will generally handle both dragged rectangular
 * zoom boxes and also more complicated polygons created
 * by double clicking at a start point and then repeatedly
 * single clicking until back at the start point  
 * @author Doug Gillespie
 *
 */
public class Zoomer {

	private Zoomable zoomableThing;

	private Vector<ZoomShape> zoomShapes;

	private ZoomShape topMostShape;

	private ZoomerMouse zoomerMouse;

	private Point mousePressPoint;

	private ZoomRectangle newZoomRectangle;

	private ZoomPolygon newZoomPolygon;

	private Component zoomableComponent;

	public Zoomer(Zoomable zoomableThing, Component zoomableComponent) {
		super();
		this.zoomableThing = zoomableThing;
		this.zoomableComponent = zoomableComponent;
		zoomShapes = new Vector<ZoomShape>();
		zoomerMouse = new ZoomerMouse(this);
		zoomableComponent.addMouseListener(zoomerMouse);
		zoomableComponent.addMouseMotionListener(zoomerMouse);
	}

	/**
	 * Paint the top most shape in the zoom sequence
	 * <p>
	 * This should be called twice from the paint function of
	 * the component hosting the zoom feature, once before other 
	 * drawing and once after. 
	 * @param g graphics 
	 * @param c component to draw on
	 * @param beforeOther called before other drawing
	 * @return outer rectangle of drawing. 
	 */
	public Rectangle paintShape(Graphics g, Component c, boolean beforeOther) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(3));
		if (topMostShape != null) {
			g.setColor(topMostShape.outlineColor);
			return topMostShape.drawShape(g, c, beforeOther);
		}
		//else if (zoomShapes.size() > 0) {
		//	return zoomShapes.get(zoomShapes.size()-1).drawShape(g, c, beforeOther);
		//}
		else {
			return null;
		}
	}

	protected Point xyValToPoint(Component c, double x, double y) {
		Point p = new Point();
		p.x = (int) ((x-zoomableThing.getXStart()) * zoomableThing.getXScale());
		p.y = c.getHeight() - (int) ((y-zoomableThing.getYStart()) * zoomableThing.getYScale());
		return p;
	}

	/**
	 * Convert a point in pixels to a value for the x scale
	 * @param x x in pixels
	 * @return scaled value in units such as time, bearing, etc. 
	 */
	protected double pointXtoVal(int x) {
		return zoomableThing.getXStart() + x / zoomableThing.getXScale();
	}
	/**
	 * Convert a point in pixels to a value for the y scale
	 * @param y y  in pixels
	 * @return scaled value in units such as time, bearing, etc. 
	 */
	protected double pointYtoVal(Component c, int y) {
		return zoomableThing.getYStart() +  (c.getHeight()-y)/ zoomableThing.getYScale();
	}
	
	/**
	 * 
	 * @param pt Point on display in pixels. 
	 * @return true if there is no mark or if the click is within the marked region 
	 */
	public boolean isInMark(Component c, Point pt) {
		if (topMostShape == null) {
			return true;
		}
		return topMostShape.containsPoint(c, pt);
	}

	public void clearLatestShape() {
		topMostShape = null;
		newZoomPolygon = null;
		newZoomRectangle = null;
		zoomableThing.zoomPolygonComplete(null);
	}

	/**
	 * @return the topMostShape
	 */
	public ZoomShape getTopMostShape() {
		return topMostShape;
	}

	/**
	 * Add menu items associated with zooming into a
	 * pre-existing menu. 
	 * @param menu menu
	 * @return number of items added.
	 */
	public int appendZoomMenuItems(Container menu) {
		if (menu == null) {
			return 0;
		}
		//		if (haveZoomBox()) {
		JMenuItem menuItem;
		menuItem = new JMenuItem("Zoom in");
		menuItem.addActionListener(new ZoomIn(this));
		menu.add(menuItem);
		menuItem.setEnabled(haveZoomBox());
		menuItem = new JMenuItem("Zoom out");
		menuItem.addActionListener(new ZoomOut(this));
		menuItem.setEnabled(canZoomOut());
		menu.add(menuItem);
		menuItem = new JMenuItem("Zoom right out");
		menuItem.addActionListener(new ZoomRightOut(this));
		menuItem.setEnabled(canZoomOut());
		menu.add(menuItem);
//		menu.addSeparator();
		//		}

		return 3;
	}

	private boolean haveZoomBox() {
		if (topMostShape == null || topMostShape.isClosed() == false) {
			return false;
		}
		return true;
	}

	private boolean canZoomOut() {
		return (zoomShapes != null && zoomShapes.size() > 0);
	}
	
	/**
	 * Zoom in to the bounds of the next zoom shape
	 */
	private void zoomIn() {
		if (topMostShape == null || topMostShape.isClosed() == false) {
			return;
		}
		if (zoomShapes.size() == 0) {
			// add a rectangle representing the current state first !
			ZoomRectangle zr = new ZoomRectangle(this, zoomableThing.getCoordinateType(),
					zoomableThing.getXStart(), zoomableThing.getYStart());
			zr.newPoint(zoomableThing.getXStart() + zoomableComponent.getWidth()/zoomableThing.getXScale(), 
					zoomableThing.getYStart() + zoomableComponent.getHeight()/zoomableThing.getYScale());
			zr.closeShape();
			zoomShapes.add(zr);
		}
		zoomShapes.add(topMostShape);
		zoomableThing.zoomToShape(topMostShape);
		if (topMostShape.removeOnZoom()) {
			topMostShape = null;
		}
		
	}
	/**
	 * Zoom out to the bounds of the preceding zoom shape
	 */
	private void zoomOut() {
		if (canZoomOut() == false) {
			return;
		}
		// there should always be at least two things in the zoom list
		// if there aren't, call zoomrightOut();
		if (zoomShapes.size() <= 2) {
			zoomRightOut();
		}
		else {
			topMostShape = zoomShapes.lastElement();
			zoomShapes.remove(zoomShapes.size()-1);
			zoomableThing.zoomToShape(zoomShapes.lastElement());
		}
	}
	/**
	 * Zoom right out to position of display prior to zooming
	 */
	private void zoomRightOut() {
		if (canZoomOut() == false) {
			return;
		}
		zoomableThing.zoomToShape(zoomShapes.firstElement());
		zoomShapes.clear();
	}

	private class ZoomIn implements ActionListener {
		private Zoomer zoomer;
		public ZoomIn(Zoomer zoomer) {
			super();
			this.zoomer = zoomer;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			zoomIn();
		}
	}
	private class ZoomOut implements ActionListener {
		private Zoomer zoomer;
		public ZoomOut(Zoomer zoomer) {
			super();
			this.zoomer = zoomer;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			zoomOut();
		}
	}
	
	/**
	 * Zoom right out back to the original scale before zooming
	 * @author Doug
	 *
	 */
	private class ZoomRightOut implements ActionListener {
		private Zoomer zoomer;
		public ZoomRightOut(Zoomer zoomer) {
			super();
			this.zoomer = zoomer;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			zoomRightOut();
		}
	}

	private class ZoomerMouse extends MouseAdapter {

		private Zoomer zoomer;

		public ZoomerMouse(Zoomer zoomer) {
			this.zoomer = zoomer;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (zoomableThing.canStartZoomArea(e)) {
				mousePressPoint = e.getPoint();
			}
			else {
				mousePressPoint = null;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton()==MouseEvent.BUTTON1){
				if (e.getClickCount() == 1) {
					singleClicked(e);
				}
				else if (e.getClickCount() == 2) {
					doubleClicked(e);
				}
			}
			
		}

		private void doubleClicked(MouseEvent e) {
//			System.out.println("doubleClick zoomer");
			if (topMostShape != null) {
				if (topMostShape.isClosed() && topMostShape.containsPoint(e.getComponent(), e.getPoint())) {
					zoomIn();
					return;
				}
				else {
//					clearLatestShape();
				}
//				return;
//				
			}
			if (newZoomPolygon == null) {
				clearLatestShape();
				topMostShape = newZoomPolygon = new ZoomPolygon(zoomer, e.getPoint(), 
						zoomableThing.getCoordinateType(),
						pointXtoVal(e.getX()), pointYtoVal(e.getComponent(), e.getY()));
			}else {
				topMostShape = newZoomPolygon = null;
				zoomableThing.zoomPolygonComplete(null);
			}
		}
		
		
		private void singleClicked(MouseEvent e) {
			
			boolean canClearZoomShape = zoomableThing.canClearZoomShape(e);
//			System.out.println("singleClick zoomer. can clr: "+canClearZoomShape);
			if (newZoomPolygon != null) {//there is an polygon partially selected
				if (newZoomPolygon.getNumPoints() >= 2) {//
					Point dynamicStartPoint = xyValToPoint(e.getComponent(), newZoomPolygon.getxPoints()[0],
							newZoomPolygon.getyPoints()[0]);
					if (dynamicStartPoint.distance(e.getPoint()) < 6) {
						newZoomPolygon.closeShape();
						newZoomPolygon = null;
						zoomableThing.zoomPolygonComplete(topMostShape);
						return;
					}
				}
				newZoomPolygon.newPoint(pointXtoVal(e.getX()), pointYtoVal(e.getComponent(), e.getY()));
				zoomableThing.zoomShapeChanging(newZoomRectangle);
				
			}else if (canClearZoomShape && topMostShape!=null&& !topMostShape.containsPoint(e.getComponent(), e.getPoint())){
				
				clearLatestShape();
			}
			
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (newZoomRectangle != null) {
				Rectangle r = newZoomRectangle.getBounds(zoomableComponent);
				if (r.height <= 1 || r.width <= 1) {
					return;
				}
				if (r.height < 6 && r.width < 6) {
					return;
				}
				newZoomRectangle.newPoint(pointXtoVal(e.getX()), pointYtoVal(e.getComponent(), e.getY()));
				newZoomRectangle.closeShape();
				newZoomRectangle = null;
				zoomableThing.zoomPolygonComplete(topMostShape);

			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			
//			if ((topMostShape != null && newZoomRectangle == null) || mousePressPoint == null) {
//				System.out.println("TS:"+topMostShape+":"+newZoomRectangle+":"+mousePressPoint);
//				System.out.println("return1");
//				return;
//			}
			if (mousePressPoint == null) return;
			if (newZoomRectangle == null) {
				if (e.getPoint().distance(mousePressPoint) < 6) {
//					System.out.println("return2");
					return;
				}
				clearLatestShape();//added same time as above commented out
				topMostShape = newZoomRectangle = new ZoomRectangle(zoomer, zoomableThing.getCoordinateType(),pointXtoVal(mousePressPoint.x), pointYtoVal(e.getComponent(), mousePressPoint.y));
			}
			newZoomRectangle.newPoint(pointXtoVal(e.getX()), pointYtoVal(e.getComponent(), e.getY()));
			zoomableThing.zoomShapeChanging(newZoomRectangle);
//			System.out.println("return3");
		}


		@Override
		public void mouseMoved(MouseEvent e) {
			if (newZoomPolygon != null) {
				newZoomPolygon.setCurrentMousePoint(e.getPoint());
				zoomableThing.zoomShapeChanging(newZoomPolygon);
			}
		}

	}

	/**
	 * @return the zoomerMouse
	 */
	public ZoomerMouse getZoomerMouse() {
		return zoomerMouse;
	}

	public ZoomShape findLastZoom(int coodinateType) {
		if (zoomShapes.size() == 0) {
			return null;
		}
		ListIterator<ZoomShape> shapeIterator = zoomShapes.listIterator(zoomShapes.size()-1);
		ZoomShape aShape;
		while (shapeIterator.hasPrevious()) {
			aShape = shapeIterator.previous();
			if (aShape.getCoordinateType() == coodinateType) {
				return aShape;
			}
		}
		return null;
	}

}
