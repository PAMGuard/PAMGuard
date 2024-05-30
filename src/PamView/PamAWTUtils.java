package PamView;

import java.awt.Component;
import java.awt.Container;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Useful functions for swing. 
 * @author Jamie Macaulay
 *
 */
public class PamAWTUtils {
	
	/**
	 * Disable an entire swing panel including all child components. 
	 * @param jComponent - the panel to disable
	 * @param isEnabled true if enabled. 
	 */
	public static void setPanelEnabled(JComponent jComponent, Boolean isEnabled) {
		setPanelEnabled(jComponent, isEnabled? 1 :-1);
	}
	
//    private static final Map<Component, Integer> componentAvailability = new WeakHashMap<Component, Integer>();
    
    public static void setMoreEnabled(Component component) {
    	setPanelEnabled(component, +1);
    }

    public static void setMoreDisabled(Component component) {
    	setPanelEnabled(component, -1);
    }
	
    // val = 1 for enabling, val = -1 for disabling
    private static void setPanelEnabled(Component component, int val) {
        if (component != null) {
        	
//            final Integer oldValObj = componentAvailability.get(component);
//            
//            final int oldVal = (oldValObj == null)
//                    ? 0
//                    : oldValObj;
//            
//            final int newVal = oldVal + val;
//            componentAvailability.put(component, newVal);
            
            int newVal = val;

            if (newVal >= 0) {
                component.setEnabled(true);
            } else if (newVal < 0) {
                component.setEnabled(false);
            }
            if (component instanceof Container) {
                Container componentAsContainer = (Container) component;
                for (Component c : componentAsContainer.getComponents()) {
                	setPanelEnabled(c,val);
                }
            }
        }
    }

	/**
	 * Find the closest boundary of a shape to a point. 	
	 * http://stackoverflow.com/questions/8103451/point-outside-of-area-which-is-closest-to-point-inside
	 * @param area - the shape 
	 * @param point - the point from which to find the closest the boundary point
	 * @return the closest boundary point. 
	 */
	public static Point2D findClosestBoundry(Shape area, Point2D point){

		ArrayList<Line2D.Double> areaSegments = new ArrayList<Line2D.Double>();
		Point2D.Double insidePoint = new Point2D.Double(point.getX(), point.getY());
		Point2D.Double closestPoint = new Point2D.Double(-1, -1);
		Point2D.Double bestPoint = new Point2D.Double(-1, -1);
		ArrayList<Point2D.Double> closestPointList = new ArrayList<Point2D.Double>();

		// Note: we're storing double[] and not Point2D.Double
		ArrayList<double[]> areaPoints = new ArrayList<double[]>();
		double[] coords = new double[6];

		for (PathIterator pi = area.getPathIterator(null); !pi.isDone(); pi.next()) {

			// Because the Area is composed of straight lines
			int type = pi.currentSegment(coords);
			// We record a double array of {segment type, x coord, y coord}
			double[] pathIteratorCoords = {type, coords[0], coords[1]};
			areaPoints.add(pathIteratorCoords);
		}

		double[] start = new double[3]; // To record where each polygon starts
		for (int i = 0; i < areaPoints.size(); i++) {
			// If we're not on the last point, return a line from this point to the next
			double[] currentElement = areaPoints.get(i);

			// We need a default value in case we've reached the end of the ArrayList
			double[] nextElement = {-1, -1, -1};
			if (i < areaPoints.size() - 1) {
				nextElement = areaPoints.get(i + 1);
			}

			// Make the lines
			if (currentElement[0] == PathIterator.SEG_MOVETO) {
				start = currentElement; // Record where the polygon started to close it later
			} 

			if (nextElement[0] == PathIterator.SEG_LINETO) {
				areaSegments.add(
						new Line2D.Double(
								currentElement[1], currentElement[2],
								nextElement[1], nextElement[2]
								)
						);
			} else if (nextElement[0] == PathIterator.SEG_CLOSE) {
				areaSegments.add(
						new Line2D.Double(
								currentElement[1], currentElement[2],
								start[1], start[2]
								)
						);
			}
		}

		// Calculate the nearest point on the edge
		for (Line2D.Double line : areaSegments) {

			// From: http://stackoverflow.com/questions/6176227
			double u = 
					((insidePoint.getX() - line.x1) * (line.x2 - line.x1) + (insidePoint.getY() - line.y1) * (line.y2 - line.y1))
					/ ((line.x2 - line.x1) * (line.x2 - line.x1) + (line.y2 - line.y1) * (line.y2 - line.y1));

			double xu = line.x1 + u * (line.x2 - line.x1);
			double yu = line.y1 + u * (line.y2 - line.y1);

			if (u < 0) {
				closestPoint.setLocation(line.getP1());
			} else if (u > 1) {
				closestPoint.setLocation(line.getP2());
			} else {
				closestPoint.setLocation(xu, yu);
			}

			closestPointList.add((Point2D.Double) closestPoint.clone());

			if (closestPoint.distance(insidePoint) < bestPoint.distance(insidePoint)) {
				bestPoint.setLocation(closestPoint);
			}
		}

		return bestPoint; 

	}
}
