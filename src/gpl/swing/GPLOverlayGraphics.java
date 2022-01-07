package gpl.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import gpl.GPLControlledUnit;
import gpl.GPLDetection;
import gpl.GPLDetectionBlock;
import gpl.contour.GPLContour;
import gpl.contour.GPLContourPoint;

public class GPLOverlayGraphics extends PamDetectionOverlayGraphics {
	
	private boolean drawOutline = false;

	public GPLOverlayGraphics(GPLControlledUnit gplControl, GPLDetectionBlock parentDataBlock, PamSymbol defaultSymbol) {
		super(parentDataBlock, defaultSymbol);
	}

	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		GPLDetection gplDetection = (GPLDetection) pamDataUnit;
		GPLContour contour = gplDetection.getContour();
		if (contour == null || contour.getArea() == 0) {
			 return super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
//			 return null;
		}
//		 super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		drawOutline = false;
		if (drawOutline) {
			return drawOutline(g, gplDetection, generalProjector);
		}
		else {
			return drawPatches(g, gplDetection, generalProjector);
		}
//		
//		return null;
	}
	
	private Rectangle drawOutline(Graphics g, GPLDetection gplDetection, GeneralProjector generalProjector) {
		GPLContour contour = gplDetection.getContour();
		Graphics2D g2d = (Graphics2D) g;
		int[][] outline = contour.getOutline(0);
		double dT = gplDetection.getTimeRes();
		double dF = gplDetection.getFreqRes();
//		double binLo = gplDetection.getBinLo();
		int nX = outline.length;
		/**
		 * going to draw a staired outline around the whole shape so 
		 * need 4x as many points. 
		 */
		int[] xPoints = new int[nX*4];
		int[] yPoints = new int[nX*4];
		int iX = 0;
		int x0 = outline[0][0]; // was already allowed for in timemillis. 
		double t0 = gplDetection.getTimeMilliseconds();
		Point coord1, coord2;
		double t1, f1, t2, f2;
		int meanX = 0, meanY = 0;
		for (int i = 0, i1 = 0, i2 = xPoints.length-1; i < outline.length; i++, i1 +=2, i2-=2) {
			// get bot left and top right points. 
			t1 = t0 + (outline[i][0]-x0)*dT*1000; 
			f1 = dF * (outline[i][1]);
			t2 = t0 + (outline[i][0]+1-x0)*dT*1000;
			f2 = dF * (outline[i][2] + 1);
			coord1 = generalProjector.getCoord3d(t1, f1, 0).getXYPoint();
			coord2 = generalProjector.getCoord3d(t2, f2, 0).getXYPoint();
			
			if (i1 > 0 && coord2.x < xPoints[i1-1]) {
				/*
				 *  we're wrapped. so panic a bit and don't draw more or it will look stupid on a 
				 *  wrapping spectrogram.  
				 */
				coord1.x = coord2.x = xPoints[i1-1]+1; 
			}
			
			// bottom section
			xPoints[i1] = coord1.x;
			yPoints[i1] = coord1.y; 
			xPoints[i1+1] = coord2.x;
			yPoints[i1+1] = coord1.y;

			xPoints[i2] = coord1.x;
			yPoints[i2] = coord2.y;
			xPoints[i2-1] = coord2.x;
			yPoints[i2-1] = coord2.y;
			
			meanX += coord1.x + coord2.x;
			meanY += coord1.y + coord2.y;
			
		}
		meanX /= (outline.length*2);
		meanY /= (outline.length*2);
		PamSymbol symbol = getPamSymbol(gplDetection, generalProjector);
		if (symbol.isFill()) {
			g.setColor(symbol.getFillColor());
			g.fillPolygon(xPoints, yPoints, xPoints.length);
		}
		g.setColor(symbol.getLineColor());
		g.drawPolygon(xPoints, yPoints, xPoints.length);
		
		generalProjector.addHoverData(new Coordinate3d(meanX, meanY), gplDetection);
		
		
		return null;
	}

	private Rectangle drawPatches(Graphics g, GPLDetection gplDetection, GeneralProjector generalProjector) {
		GPLContour contour = gplDetection.getContour();
		if (contour == null) {
			return null;
		}
		ArrayList<GPLContourPoint> contourPoints = contour.getContourPoints();
		if (contourPoints.size() == 0) {
			return null;
		}
		Graphics2D g2d = (Graphics2D) g;
		int[][] outline = contour.getOutline(0);
		double dT = gplDetection.getTimeRes();
		double dF = gplDetection.getFreqRes();
//		double binLo = gplDetection.getBinLo();
		int nX = outline.length;
		int xPixMin, xPixMax;
		int yPixMin, yPixMax;
		xPixMin = outline[0][0];
		xPixMax = outline[nX-1][0];
		yPixMin = contour.getMinFBin();
		yPixMax = contour.getMaxFBin();
		int imWid = xPixMax-xPixMin+1;
		int imHei = yPixMax-yPixMin+1;
		BufferedImage image = new BufferedImage(imWid, imHei, BufferedImage.TYPE_4BYTE_ABGR);
		for (int i = 0; i < imWid; i++) {
			for (int j = 0; j < imHei; j++) {
				image.setRGB(i, j, 0); // transparent
			}
		}
		PamSymbol symbol = getPamSymbol(gplDetection, generalProjector);
		Color col = symbol.getFillColor();
		int rgb = col.getRGB();
		for (Point p : contourPoints) {
			try {
				image.setRGB(p.x-xPixMin, p.y-yPixMin, rgb);
			}
			catch (Exception e) {
//				image.setRGB(p.x-xPixMin, p.y-yPixMin, rgb);
			}
		}
		// work out the corners. 
		int x0 = outline[0][0]; // was already allowed for in timemillis. 
		double t0 = gplDetection.getTimeMilliseconds() + xPixMin * dT*1000;
		Point coord1, coord2;
		double t1 = t0 + (xPixMin-x0)*dT*1000; 
		double f1 = dF * (yPixMin);
		double t2 = t0 + (xPixMax-x0)*dT*1000; 
		double f2 = dF * (yPixMax+1);
		coord1 = generalProjector.getCoord3d(t1, f1, 0).getXYPoint();
		coord2 = generalProjector.getCoord3d(t2, f2, 0).getXYPoint();
		if (coord2.x >= coord1.x) {
			g2d.drawImage(image, coord1.x, coord1.y, coord2.x, coord2.y, 0, 0, imWid, imHei, null);
		}
		else {
			int w = g2d.getClipBounds().width;
			coord2.x += w;
			g2d.drawImage(image, coord1.x, coord1.y, coord2.x, coord2.y, 0, 0, imWid, imHei, null);
			coord1.x -= w;
			coord2.x -= w;
			g2d.drawImage(image, coord1.x, coord1.y, coord2.x, coord2.y, 0, 0, imWid, imHei, null);
		}
		
		int midX = (coord1.x + coord2.x)/2;
		int midY = (coord1.y + coord2.y)/2;
		generalProjector.addHoverData(new Coordinate3d(midX, midY), gplDetection);
//		Rectangle r = new Rectangle(coord1.x, coord1.y, coord2.x-coord1.x, coord2.y-coord1.y);
//		generalProjector.addHoverData(r, gplDetection);
		
		return null;
	}

}
