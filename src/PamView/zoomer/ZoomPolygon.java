package PamView.zoomer;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Arrays;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMark.OverlayMarkType;
import PamView.paneloverlay.overlaymark.OverlayMarker;

/**
 * A zoom polygon created by the zoomer. 
 * @author Doug Gillespie
 *
 */
public class ZoomPolygon extends ZoomShape {

	private double xPoints[];

	private double yPoints[];

	private Point currentMousePoint;

	private Point startPoint;

	public ZoomPolygon(Zoomer zoomer, Point startPoint, int coordinateType, double xStart, double yStart) {
		super(zoomer, coordinateType);
		xPoints = new double[1];
		yPoints = new double[1];
		this.startPoint = new Point(startPoint);
		xPoints[0] = xStart;
		yPoints[0] = yStart;
	}

	@Override
	public Rectangle drawShape(Graphics g, Component component, boolean beforeOther) {
		int nP = xPoints.length;
		if (isClosed() && beforeOther) {
			return drawShapeSolid(g, component);
		}
		else if (isClosed() == false && beforeOther == false) {
			return drawShapeOutline(g, component);
		}
		return null;
	}

	public Rectangle drawShapeOutline(Graphics g, Component component) {
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(outlineColor);
		int nP = xPoints.length;
		if (nP < 1) {
			return null;
		}
		Point lastPoint, nextPoint;
		lastPoint = getZoomer().xyValToPoint(component, xPoints[0], yPoints[0]);
		for (int i = 1; i < nP; i++) {
			nextPoint = getZoomer().xyValToPoint(component, xPoints[i], yPoints[i]);
			g.drawLine(lastPoint.x, lastPoint.y, nextPoint.x, nextPoint.y);
			lastPoint = nextPoint;
		}
		if (currentMousePoint != null) {
			float[] dashes = {2, 6};
			g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashes, 0));
			g.drawLine(lastPoint.x, lastPoint.y, currentMousePoint.x, currentMousePoint.y);
		}

		return null;
	}
	public Rectangle drawShapeSolid(Graphics g, Component component) {
		Graphics2D g2d = (Graphics2D) g;
		Polygon pol = getPolygon(component);
		if (pol == null) {
			return null;
		}

		g.setColor(outlineColor);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawPolygon(pol);
		g.setColor(fillColor);
		g2d.fillPolygon(pol);
		return pol.getBounds();
	}

	private Polygon getPolygon(Component component) {
		int nP = xPoints.length;
		if (nP < 3) {
			return null;
		}
		int[] x = new int[nP];
		int[] y = new int[nP];
		Point p;
		for (int i = 0; i < nP; i++) {
			p = getZoomer().xyValToPoint(component, xPoints[i], yPoints[i]);
			x[i] = p.x;
			y[i] = p.y;
		}
		return new Polygon(x, y, nP);
	}

	@Override
	public Rectangle getBounds(Component component) {
		Polygon pol = getPolygon(component);
		if (pol == null) {
			return null;
		}
		return pol.getBounds();
	}

	@Override
	public boolean containsPoint(Component component, Point pt) {
		Polygon pol = getPolygon(component);
		if (pol == null) {
			return false;
		}
		return pol.contains(pt);
	}

	@Override
	public void newPoint(double x, double y) {
		int currN = xPoints.length;
		xPoints = Arrays.copyOf(xPoints, currN+1);
		yPoints = Arrays.copyOf(yPoints, currN+1);
		xPoints[currN] = x;
		yPoints[currN] = y;
		currentMousePoint = null;
	}

	@Override
	public void closeShape() {
		super.closeShape();
		currentMousePoint = null;
	}

	@Override
	public boolean removeOnZoom() {
		return false;
	}

	/**
	 * @param currentMousePoint the currentMousePoint to set
	 */
	public void setCurrentMousePoint(Point currentMousePoint) {
		this.currentMousePoint = currentMousePoint;
	}

	/**
	 * @return the startPoint
	 */
	public Point getStartPoint() {
		return startPoint;
	}

	public int getNumPoints() {
		return xPoints.length;
	}

	private double getArrayMin(double[] array) {
		if (array == null || array.length == 0) {
			return 0;
		}
		double m = array[0];
		for (int i = 1; i < array.length; i++) {
			m = Math.min(m, array[i]);
		}
		return m;
	}
	private double getArrayMax(double[] array) {
		if (array == null || array.length == 0) {
			return 0;
		}
		double m = array[0];
		for (int i = 1; i < array.length; i++) {
			m = Math.max(m, array[i]);
		}
		return m;
	}
	@Override
	public double getXLength() {
		return getArrayMax(xPoints)-getArrayMin(xPoints);
	}

	@Override
	public double getXStart() {
		return getArrayMin(xPoints);
	}

	@Override
	public double getYLength() {
		return getArrayMax(yPoints)-getArrayMin(yPoints);
	}

	@Override
	public double getYStart() {
		return getArrayMin(yPoints);
	}

	/**
	 * @return the xPoints
	 */
	public double[] getxPoints() {
		return xPoints;
	}

	/**
	 * @return the yPoints
	 */
	public double[] getyPoints() {
		return yPoints;
	}

	@Override
	public OverlayMark zoomShapeToOverlayMark(OverlayMarker overlayMarker) {
		GeneralProjector proj = overlayMarker.getProjector();
		OverlayMark overlayMark = new OverlayMark(overlayMarker, null, null, 0, proj.getParameterTypes(), proj.getParameterUnits());
		overlayMark.setMarkType(OverlayMarkType.POLYGON);
		for (int i = 0; i < xPoints.length; i++) {
			overlayMark.addCoordinate(new Coordinate3d(xPoints[i], yPoints[i]));
		}
		return overlayMark;
	}


}
