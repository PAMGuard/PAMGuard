package PamView.zoomer;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamView.paneloverlay.overlaymark.OverlayMark.OverlayMarkType;

public class ZoomRectangle extends ZoomShape {
	
	double x1, x2, y1, y2; 
	
	public ZoomRectangle(Zoomer zoomer, int coordindateType, double xStart, double yStart) {
		super(zoomer, coordindateType);
		x1 = x2 = xStart;
		y1 = y2 = yStart;
	}

	@Override
	public Rectangle drawShape(Graphics g, Component component, boolean beforeOther) {
//		if (beforeOther == isClosed()) {
//			return null;
//		}
		if (beforeOther) return null;
		Rectangle r = getBounds(component);
		if (isClosed()) r=drawShapeSolid(g,  component,r);
		else r=drawShapeOutline( g,  component,r);
	
		return r;
	}
	
	public Rectangle drawShapeOutline(Graphics g, Component component, Rectangle r) {
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(outlineColor);
		g2d.setStroke(new BasicStroke(3));
		g.drawRect(r.x, r.y, r.width, r.height);
		return r; 
	}
	
	public Rectangle drawShapeSolid(Graphics g, Component component, Rectangle r) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		g.setColor(outlineColor);
		g.drawRect(r.x, r.y, r.width, r.height);
		g.setColor(fillColor);
		g.fillRect(r.x, r.y, r.width, r.height);
		return r; 
	}

	@Override
	public boolean containsPoint(Component component, Point pt) {
		Rectangle r = getBounds(component);
		return r.contains(pt);
	}

	@Override
	public void closeShape() {
		super.closeShape();
	}

	@Override
	public boolean removeOnZoom() {
		return true;
	}

	@Override
	public void newPoint(double x, double y) {
		x2 = x;
		y2 = y;
	}
	
	

	@Override
	public Rectangle getBounds(Component component) {
		Point p1 = getZoomer().xyValToPoint(component, x1, y1);
		Point p2 = getZoomer().xyValToPoint(component, x2, y2);
		Rectangle r = new Rectangle();
		r.x = Math.min(p1.x, p2.x);
		r.y = Math.min(p1.y, p2.y);
		r.width = Math.abs(p2.x-p1.x);
		r.height = Math.abs(p2.y-p1.y);
		return r;
	}

	@Override
	public double getXLength() {
		return Math.abs(x2-x1);
	}

	@Override
	public double getXStart() {
		return Math.min(x2,x1);
	}

	@Override
	public double getYLength() {
		return Math.abs(y2-y1);
	}

	@Override
	public double getYStart() {
		return Math.min(y2,y1);
	}

	@Override
	public OverlayMark zoomShapeToOverlayMark(OverlayMarker overlayMarker) {
		GeneralProjector proj = overlayMarker.getProjector();
		OverlayMark overlayMark = new OverlayMark(overlayMarker, null, null, 0, proj.getParameterTypes(), proj.getParameterUnits());
		overlayMark.setMarkType(OverlayMarkType.RECTANGLE);
		overlayMark.addCoordinate(new Coordinate3d(x1, y1));
		overlayMark.addCoordinate(new Coordinate3d(x2, y2));
		return overlayMark;
	}

}
