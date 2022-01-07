package Layout;

import java.awt.Graphics;

public class RepeatedAxis extends PamAxis {

	private int repeatCount = 1;
	
	public RepeatedAxis(int x1, int y1, int x2, int y2, double minVal,
			double maxVal, int tickPosition, String label, int labelPos,
			String format) {
		super(x1, y1, x2, y2, minVal, maxVal, tickPosition, label, labelPos,
				format);
		// TODO Auto-generated constructor stub
	}

	public RepeatedAxis(int x1, int y1, int x2, int y2, double minVal,
			double maxVal, boolean aboveLeft, String label, String format) {
		super(x1, y1, x2, y2, minVal, maxVal, aboveLeft, label, format);
		// TODO Auto-generated constructor stub
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = Math.max(repeatCount, 1);
	}

	@Override
	public void drawAxis(Graphics g, int x1, int y1, int x2, int y2) {
		double xStep = (x2-x1) / repeatCount;
		double yStep = (y2-y1)/ repeatCount;
		int xx1, xx2, yy1, yy2;
		for (int i = 0; i < repeatCount; i++) {
			xx1 = x1 + (int) (i * xStep);
			xx2 = xx1 + (int) xStep;
			yy1 = y1 + (int) (i * yStep);
			yy2 = yy1 + (int) yStep;
			super.drawAxis(g, xx1, yy1, xx2, yy2);
		}
	}

	
	
}
