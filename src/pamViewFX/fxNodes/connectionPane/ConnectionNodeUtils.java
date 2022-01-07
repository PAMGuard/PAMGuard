package pamViewFX.fxNodes.connectionPane;

import javafx.geometry.Point2D;

/**
 * Useful functions for connection nodes. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class ConnectionNodeUtils {
	
	/**
	 * Computes the closest point on a line to another point. 
	 * @param x1 - x start of line.
	 * @param y1 - y start of line.
	 * @param x2 - x end of line.
	 * @param y2 - y end of line.
	 * @param xp - x point to test.
	 * @param yp - y point to test.
	 * @return the closest point on line. 
	 */
	public static Point2D findClosestPoint(double x1, double y1, double x2, double y2, double xp, double yp){
		
		/**
		 * Let u be the percentage of the distance between p1 and p2, that is needed to find the point on the 
		 * line formed by p1 and p2, such that p1+u(p2-p1) = the point on the line that is closest to p3 
		 * (the line segment between this point and p3 also happens to be perpendicular to the line going through p1 and p2).
		 */
		double u = ((xp - x1)*(x2 - x1)+
				(yp - y1)*(y2 - y1))/(
						Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2));
		
		/**
		 * Work out closest point on line.
		 */
		double xu = x1 + u*(x2 - x1);
		double yu = y1 + u*(y2 - y1);
		
		/**
		 * The point pu is only the closest point on the line segment when 0 <= u <= 1. 
		 * Otherwise the appropriate endpoint of the line segment is the closest point to the point in question. 
		 * Thus for each pu, p1, p2, and u calculated in the above step do the following:
		 */
		Point2D pc;
		if (u<0) {pc = new Point2D(x1, y1);}
		else if (u>1) {pc = new Point2D(x2, y2);}
		else {pc = new Point2D(xu,yu);};

		return  pc;
	}
	
	/**
	 * Computes the closest point on a line to another point. 
	 * @param p1 - start of the line.
	 * @param p2 - end of the line. 
	 * @param p3 - point to measure closest point on line to. 
	 * @return the closest point on the line to p3. This will be somewhere between p1 and p2. 
	 */
	public static Point2D findClosestPoint(Point2D p1, Point2D p2, Point2D p3){
		return findClosestPoint(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
	}


//	/**
//	 * Computes the closest point on a line to another point. 
//	 * @param p1 - start of the line.
//	 * @param p2 - end of the line. 
//	 * @param p3 - point to measure closest point on line to. 
//	 * @return the closest point on the line to p3. This will be somewhere between p1 and p2. 
//	 */
//	public static Point2D findClosestPoint(Point2D p1, Point2D p2, Point2D p3){
//
//		/**
//		 * Let u be the percentage of the distance between p1 and p2, that is needed to find the point on the 
//		 * line formed by p1 and p2, such that p1+u(p2-p1) = the point on the line that is closest to p3 
//		 * (the line segment between this point and p3 also happens to be perpendicular to the line going through p1 and p2).
//		 */
//		double u = ((p3.getX() - p1.getX())*(p2.getX() - p1.getX())+
//				(p3.getY() - p1.getY())*(p2.getY() - p1.getY()))/(
//						Math.pow((p2.getX() - p1.getX()),2) + Math.pow((p2.getY() - p1.getY()),2));
//		/**
//		 * Work out closest point on line.
//		 */
//		double xu = p1.getX() + u*(p2.getX() - p1.getX());
//		double yu = p1.getY() + u*(p2.getY() - p1.getY());
//
//		//System.out.println("Closest line u "+u+ " xu "+xu+ " yu "+yu);
//
//		/**
//		 * The point pu is only the closest point on the line segment when 0 <= u <= 1. 
//		 * Otherwise the appropriate endpoint of the line segment is the closest point to the point in question. 
//		 * Thus for each pu, p1, p2, and u calculated in the above step do the following:
//		 */
//		Point2D pc;
//		if (u<0) {pc = p1;}
//		else if (u>1) {pc = p2;}
//		else {pc = new Point2D(xu,yu);};
//
//		return  pc;
//	}
	
	/**
	 * Get the closest point to the edge of a rectangle from another point
	 * @param x - rectangle layout x (top left corner)
	 * @param y - rectangle layout y (top left corner)
	 * @param width - rectangle width.
	 * @param height - rectangle height.
	 * @param xp - the x position of point to test.
	 * @param yp - the y position of point to test.
	 * @return - the closest point to (xp, yp) located on the rectangle's edge. 
	 */
	public static Point2D closestPointOnRect(double x, double y, double width, double height, double xp, double yp) {
				
		//which corner is the point closest to (no need for a sqrt here to optimise speed)
		//do this to minimise closest point line calculations. 
		double distance1 = (Math.pow((x-xp), 2)+Math.pow((y-yp), 2)); //top left corner
		double distance2= (Math.pow((x+width-xp), 2)+Math.pow((y+height-yp), 2)); //bottom right corner

		//Find the closest point on the two lines connected to the closest corner. 
		Point2D  line1;
		Point2D  line2;
		if (distance1<distance2) {
			line1 = findClosestPoint(x, y, x+width, y, xp,  yp); //top
			line2 = findClosestPoint(x, y, x, y+height, xp,  yp); //left
		}
		else {
			line1 = findClosestPoint(x+width, y+height, x+width, y, xp,  yp); //right
			line2 = findClosestPoint(x+width, y+height, x, y+height, xp,  yp); //bottom
		}
		
		//now figure out which line is closest. Again no need for sqrt here for speed. 
		distance1 = (Math.pow((line1.getX()-xp), 2)+Math.pow((line1.getY()-yp), 2)); 
		distance2 = (Math.pow((line2.getX()-xp), 2)+Math.pow((line2.getY()-yp), 2)); 
		
		if (distance1<distance2) return line1; 
		else return line2; 

	}
	


	/**
	 * Calculate the position of a point percentage along a line. 
	 * @param x1 - x co-ordinate of start of line
	 * @param x2 - x co-ordinate of end of line
	 * @param y1 - y co-ordinate of start of line
	 * @param y2 - y co-ordinate of end of line
	 * @return the position of the point if fraction % along line. 
	 */
	protected static Point2D getLinePosition(double x1,double x2, double y1,double y2, Double fraction ) {
		double vx =  x2 - x1;
		double vy =  y2 - y1;
		double mag= Math.sqrt(vx*vx + vy*vy);
		vx /= mag;
		vy /= mag;
		double px = x1 + vx * fraction*mag;
		double py = y1 + vy * fraction*mag;
		//work out unit vector
		//System.out.println(" px py "+ px + " "+py);
		return new Point2D(px,py); 
	}

	

	/**
	 * Computes the intersection between two lines. The calculated point is approximate, 
	 * since integers are used. If you need a more precise result, use doubles
	 * everywhere. 
	 * (c) 2007 Alexander Hristov. Use Freely (LGPL license). http://www.ahristov.com
	 *
	 * @param x1 Point 1 of Line 1
	 * @param y1 Point 1 of Line 1
	 * @param x2 Point 2 of Line 1
	 * @param y2 Point 2 of Line 1
	 * @param x3 Point 1 of Line 2
	 * @param y3 Point 1 of Line 2
	 * @param x4 Point 2 of Line 2
	 * @param y4 Point 2 of Line 2
	 * @return Point where the segments intersect, or null if they don't
	 */
	public static Point2D intersection(
			int x1,int y1,int x2,int y2, 
			int x3, int y3, int x4,int y4
			) {
		int d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
		if (d == 0) return null;

		int xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
		int yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;

		return new Point2D(xi,yi);
	}


}
