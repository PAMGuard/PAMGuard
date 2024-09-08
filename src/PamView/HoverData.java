package PamView;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import PamUtils.Coordinate3d;
import PamguardMVC.PamDataUnit;

/**
 * Holds hover data. Added to a list in a GeneralProjector every time a display
 * is drawn. Was initially used to quickly obtain hover information on the 
 * map display but is now also being used to identify data units enclosed
 * within marks made on the display with the OverlayMark system. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class HoverData {
	
	public HoverData() {
		super();
	}
	
	public HoverData(Shape drawnShape, PamDataUnit pamDataUnit, int iSide, int subPlotNumber) {
		super();
		this.drawnShape = drawnShape;
		this.pamDataUnit = pamDataUnit;
		this.iSide = iSide;
		this.subPlotNumber = subPlotNumber;
	}

	public HoverData(Coordinate3d c, PamDataUnit pamDataUnit, int iSide, int subPlotNumber) {
		this.setCoordinate3D(c);
		this.pamDataUnit = pamDataUnit;
		this.iSide = iSide;
		this.subPlotNumber = subPlotNumber;
	}

	/**
	 * The position of the hover text (null if a shape instead)
	 */
//	Coordinate3d coordinate3d;
	private Shape drawnShape;
	
	/**
	 * The shape in which the hover text is located (null if a coordinate instead).  
	 */
	private TransformShape transformShape;
	
	/**
	 * Pam data unit associated with hover text
	 */
	private PamDataUnit pamDataUnit;
	
	/**
	 * The ambiguity. 
	 */
	private int iSide;
	
	/**
	 * Subplot number (used in general time displays and needed 
	 * to reidentify marked / hover data. )
	 */
	private int subPlotNumber = -1;

	/**
	 * Get the data unit associated with the hover data.
	 * @return a pma data unit. 
	 */
	public PamDataUnit getDataUnit() {
		return pamDataUnit;
	}
	
	public void setTransformShape(TransformShape shape2) {
		this.transformShape=shape2; 
	}

	public void setAmbiguity(int iSide) {
		this.iSide=iSide;
		
	}

	public void setDataUnit(PamDataUnit pamDataUnit) {
		this.pamDataUnit=pamDataUnit; 
		
	}
	
	/**
	 * Get the drawn shape
	 * @return drawn shape
	 */
	public Shape getDrawnShape() {
		return drawnShape;
	}
	
	/**
	 * Set the drawn shape
	 * @param drawnShape
	 */
	public void setDrawnShape(Shape drawnShape) {
		this.drawnShape = drawnShape;
	}
	
	public double distFromCentre(double x, double y) {
		if (drawnShape == null) {
			return Double.NaN;
		}
		if (drawnShape.contains(x, y)) {
//			return  the distance from the shapes centre ?
			return 0;
		}
		// otherwise the distance from the center. 
		Rectangle2D bounds = drawnShape.getBounds2D();
		double xc = bounds.getCenterX();
		double yc = bounds.getCenterY();
		return (Math.sqrt(Math.pow(x-xc, 2)+Math.pow(y-yc, 2))); 
	}

	/**
	 * Get the centre of the drawn shape. This is actually 
	 * the centre of the bounding rectangle, which may be different to
	 * any concept of a geometric centre for some shapes. 
	 * @return shape centre. 
	 */
	public Point2D getShapeCentre() {
		if (drawnShape == null) {
			return null;
		}
		Rectangle2D bounds = drawnShape.getBounds2D();
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}
//	/**
//	 * @return the coordinate3d
//	 */
//	public Coordinate3d getCoordinate3d() {
//		return coordinate3d;
//	}
//
	public void setCoordinate3D(Coordinate3d coordinate3d) {
//		this.coordinate3d=coordinate3d; 
		// create a zero sized rectangle. 
		drawnShape = new Rectangle2D.Double(coordinate3d.x, coordinate3d.y, 0, 0);
	}

	/**
	 * Get the ambiguity of the hover data i.e. one data unit may have multiple 
	 * shapes/co-ordinates e.g. localisations on the map
	 * @return the ambiguity (previously refferred to as iSide)
	 */
	public int getAmbiguity() {
		return iSide;
	}

	/**
	 * Get the shape in whihc hover text is located. May be null if CoOrdintate3d used instead. 
	 * @return the hover shape. Hover text is allowed inside the shape boundary. 
	 */
	public TransformShape getTransfromShape() {
		return transformShape;
	}

	/**
	 * @return the subPlotNumber
	 */
	public int getSubPlotNumber() {
		return subPlotNumber;
	}

	/**
	 * @param subPlotNumber the subPlotNumber to set
	 */
	public void setSubPlotNumber(int subPlotNumber) {
		this.subPlotNumber = subPlotNumber;
	}
	
	/**
	 * Get a point for the item. 
	 * @return a Point marking the center of this drawn object. 
	 */
//	public Point2D getPoint2D() {
//		if (coordinate3d != null) {
//			return coordinate3d.getPoint2D();
//		}
//		if (shape != null) {
//			return shape.getOrigin();
//		}
//		return null;
//	}
	
}