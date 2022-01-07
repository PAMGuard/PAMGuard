package whistleClassifier;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import Layout.PamAxis;

public class ClassifierHistoryTimeAxis extends PamAxis {

	public ClassifierHistoryTimeAxis(int x1, int y1, int x2, int y2, double minVal, double maxVal, boolean aboveLeft, String label, String format) {
		super(x1, y1, x2, y2, minVal, maxVal, aboveLeft, label, format);
	}

	public ClassifierHistoryTimeAxis(int x1, int y1, int x2, int y2, double minVal, double maxVal, int tickPosition, String label, int labelPos, String format) {
		super(x1, y1, x2, y2, minVal, maxVal, tickPosition, label, labelPos, format);
	}

	/**
	 * Very specific case, where val is in munutes - want to format values < 1
	 * in seconds. 
	 */
	@Override
	protected String formatValue(double val) {
		if (val < 1) {
			double seconds = val * 60;
			if (val < .01) {
				return String.format("%.2f s", seconds);
			}
			else if (val < 0.1) {
				return String.format("%.1f s", seconds);
			}
			else {
				return String.format("%d s", (int) seconds);
			}
		}
		if (val == 1) {
			return String.format("%d minute", (int) val);
		}
		return String.format("%d minutes", (int) val);
	}

	@Override
	protected void drawLogAxis(Graphics2D g2d) {
		// TODO Auto-generated method stub
//		super.drawLogAxis(g2d);
		ArrayList<Point> axisPoints = getLogPoints(false);
		// axis values are set in the same function and held 
	    
		double val = 0;
		for (int i = 0; i < axisPoints.size(); i++) {
			drawTickAndLabel(g2d, axisPoints.get(i), val = getAxisValues().get(i));
		}
		if (val < this.getMaxVal() / 2) {
			Point xy = new Point(this.getX2(), this.getY2());
			drawTickAndLabel(g2d, xy, this.getMaxVal());
		}
	}
	
	

}
