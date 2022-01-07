package Array.sensors.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import Array.sensors.ArrayDisplayParameters;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.debug.Debug;

public class HeadingComponent extends ArrayDimComponent {

	PamSymbol upTriangle = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLED, 8, 12, true, Color.red, Color.RED);
	private ArrayDisplayParamsProvider paramsProvider;
		
	public HeadingComponent(ArrayDisplayParamsProvider paramsProvider, int streamerMap) {
		super(streamerMap, 120, 25);
		this.paramsProvider = paramsProvider;
		setToolTipText("Streamer Heading");
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int w = getWidth();
		int h = getHeight();
		int yMid = h/2;
		int xMid = w/2;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		setFont(g2d);
		
		
		Color col = getForeground();
		
		double meanHead = getAngleMean();
		
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int fontHeight = fontMetrics.getHeight();
		
		double angRange = 35;
		double aScale = w/angRange;
		
		int nStreamers = getNStreamers();
		for (int i = 0; i < nStreamers; i++) {
			int iStreamer = getStreamerIndex(i);
			ArraySensorDataUnit sensorData = getSensorData(i);
			if (sensorData == null) {
				continue;
			}
			Double currentHead = sensorData.getField(getStreamerIndex(i), ArraySensorFieldType.HEADING);
			if (currentHead == null) {
				continue;
			}
			if (nStreamers > 1) {
				col = PamColors.getInstance().getChannelColor(iStreamer);
			}
			upTriangle.setFillColor(col);
			upTriangle.setLineColor(col);
			//		upTriangle.setHeight(12);
			int xP = getXPixforAngle(meanHead, currentHead, angRange);
			upTriangle.draw(g, new Point(xP, upTriangle.getHeight()/3));
		}
		
		g.setColor(getForeground());	
		int bigStep = 10;
		int smallStep = 2;
		int nSmallStep = bigStep/smallStep;
		/*
		 *  were always in the centre, work from near the current angle to as many
		 *  steps either side as are plottable
		 */
		BasicStroke dashed = new BasicStroke(.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1, 2}, 0);
		BasicStroke solid = new BasicStroke(1);
		int ang = (int) (meanHead - angRange);
		ang /= bigStep;
		ang *= bigStep;
		while (ang < meanHead + angRange) {
			int xPix = getXPixforAngle(meanHead, ang, angRange);
			if (ang%90 == 0) {
				g2d.setStroke(solid);
			}
			else {
				g2d.setStroke(dashed);
			}
			g.drawLine(xPix, 0, xPix, h-fontMetrics.getAscent());
			int dAng = (int) constrianAngle(ang);
			String str = String.format("%d", dAng);
			int strWid = fontMetrics.stringWidth(str);
			g.drawString(str+ LatLong.deg, xPix-strWid/2, h);
			g2d.setStroke(dashed);
			for (int i = 1; i < nSmallStep; i++) {
				ang += 2;
				xPix = getXPixforAngle(meanHead, ang, angRange);
				g.drawLine(xPix, 0, xPix, h-fontMetrics.getAscent()*3/2);
			}
			ang += 2;
		}
//		ang = (int) (currentHead - angRange);
//		ang /= 10;
//		ang *= 10;
//		while (ang < currentHead + angRange) {
//			int xPix = (int) (xMid+(ang-currentHead)*aScale);
//			g.drawLine(xPix, 0, xPix, h-fontMetrics.getAscent());
//			ang += 2;
//		}
		
	}
	
	/**
	 * Get the mean of all available headings. 
	 * @return mean heading in degrees
	 */
	private double getAngleMean() {
		int nStreamers = getNStreamers();
		double x = 0, y = 0;
		int n = 0;
		for (int i = 0; i < nStreamers; i++) {
			Double head = getSensorValue(i, ArraySensorFieldType.HEADING);
			if (head == null) {
				continue;
			}
			x += Math.cos(Math.toRadians(head));
			y += Math.sin(Math.toRadians(head));
			n++;
		}
		if (n == 0) {
			return 0;
		}
		double meanAng = Math.toDegrees(Math.atan2(y,x));
//		Debug.out.printf("Mean of %3.1f and %3.1f is %3.1f\n", currentHead[0], currentHead[1], meanAng);
		
		return meanAng;
	}
	
	
	private int getXPixforAngle(double meanAngle, double angle, double angRange) {
		double aScale = getWidth()/angRange;
		 int xPix = (int) (getWidth()/2+Math.sin(Math.toRadians((angle-meanAngle)))*aScale*40);
		return xPix;
	}

	@Override
	public Double getSensorValue(int iStreamer, ArraySensorFieldType fieldType) {
		Double head = super.getSensorValue(iStreamer, fieldType);
		if (head == null) {
			return head;
		}
		return constrianAngle(head);
	}
	/**
	 * Constrain angle to selected range
	 * @param head
	 * @return
	 */
	private double constrianAngle(double head) {
		if (paramsProvider.getDisplayParameters().getHeadRange() == ArrayDisplayParameters.HEAD_0_360) {
			head = PamUtils.constrainedAngle(head, 360);
		}
		else {
			head = PamUtils.constrainedAngle(head, 180);
		}
		return head;
	}

}
