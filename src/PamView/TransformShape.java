package PamView;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Stores a shape and a transform, if it has one.
 * @author Jamie Macaulay
 *
 */
public class TransformShape {

	private Shape shape;
	private AffineTransform transform;
	private Point2D origin;
	private double angle;


	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public TransformShape(Shape shape, AffineTransform transform){
		this.shape=shape; 
		this.transform=transform; 
	}
	
	public TransformShape(Shape shape, AffineTransform transform, Point2D origin){
		this.origin=origin; 
		this.shape=shape; 
		this.transform=transform; 
	}
	
	public TransformShape(Shape shape, double angle, Point2D origin){
		this.origin=origin; 
		this.shape=shape; 
		this.angle=angle; 
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public AffineTransform getTransform() {
		return transform;
	}

	public void setTransform(AffineTransform transform) {
		this.transform = transform;
	}
	
	public Point2D getOrigin() {
		return origin;
	}

	public void setOrigin(Point2D origin) {
		this.origin = origin;
	}
}

