/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.PamColors;
import PamView.PamColors.PamColor;

/**
 * @author David McLaren \n Generates straight Lat/Long lines and Strings for
 *         e.g. MapRectProjector
 */

public class StraightLineGrid {

//	LatLong panelTopLeftLL;
//
//	LatLong panelTopRightLL;
//
//	LatLong panelLowerLeftLL;
//
//	LatLong panelLowerRightLL;

	double latLinesPerDegree;

	double longLinesPerDegree;

	int numLatLines;

	int numLongLines;

	private double lineGradient;

	private double b;

	private Coordinate3d intersectPoint;

	private Coordinate3d intersectPoint2;

	private MapPanel mapPanel;

	private int typStringWidth;

	private int typStringHeight;

	public StraightLineGrid(MapPanel mapPanel) {
		super();
		this.mapPanel = mapPanel;

	}

	public void drawGrid(Graphics2D g2d, MapRectProjector rectProj) {

		intersectPoint = new Coordinate3d();
		intersectPoint2 = new Coordinate3d();

		double minLat, maxLat, minLong, maxLong;
//		double longDegs;
//		double longMins;
//		double latDegs;
//		double latMins;
		double latitudeRangeFactor;
		double longitudeRangeFactor;

		String latString, longString;
		Font latLongFont = new Font("Arial", Font.PLAIN, 12);
		FontRenderContext frc = (g2d).getFontRenderContext();
		
		minLat = mapPanel.getMinCornerLatLong().getLatitude();
		minLong = mapPanel.getMinCornerLatLong().getLongitude();
		maxLat = mapPanel.getMaxCornerLatLong().getLatitude();
		maxLong = mapPanel.getMaxCornerLatLong().getLongitude();

		latitudeRangeFactor = mapPanel.getMapRangeMetres() / 60000.0;
		longitudeRangeFactor = mapPanel.getMapRangeMetres() / 60000.0
				/ Math.cos(Math.toRadians(rectProj.getMapCentreDegrees().getLatitude())); // (maxLong-minLong)*(2.0-Math.tan(mapRotation));

		// Draw longitude lines around anti-meridian for all but high-polar latitudes
		if (maxLong - minLong > 180 ) {
			double temp = maxLong;
			maxLong = 180. + 180. % minLong;
			minLong = temp;
		}

	
		if (longitudeRangeFactor <= 0.25) {
			longLinesPerDegree = 30.0;
		} else if (longitudeRangeFactor > 0.25 && longitudeRangeFactor <= 0.5) {
			longLinesPerDegree = 15.0;
		} else if (longitudeRangeFactor > 0.5 && longitudeRangeFactor <= 1.0) {
			longLinesPerDegree = 7.5;
		} else if (longitudeRangeFactor > 1.0 && longitudeRangeFactor <= 2.0) {
			longLinesPerDegree = 4;
		} else if (longitudeRangeFactor > 2.0 && longitudeRangeFactor <= 5.0) {
			longLinesPerDegree = 2;
		} else if (longitudeRangeFactor > 5.0 && longitudeRangeFactor <= 10.0) {
			longLinesPerDegree = 1;
		} else if (longitudeRangeFactor > 10.0 && longitudeRangeFactor <= 20.0) {
			longLinesPerDegree = .5;
		} else if (longitudeRangeFactor > 20.0 && longitudeRangeFactor <= 40.0) {
			longLinesPerDegree = .25;
		} else if (longitudeRangeFactor > 40.0 && longitudeRangeFactor <= 80.0) {
			longLinesPerDegree = .125;
		} else {
			longLinesPerDegree = 0.0625;
		}

		if (latitudeRangeFactor <= 0.25) {
			latLinesPerDegree = 30.0;
		} else if (latitudeRangeFactor > 0.25 && latitudeRangeFactor <= 0.5) {
			latLinesPerDegree = 15.0;
		} else if (latitudeRangeFactor > 0.5 && latitudeRangeFactor <= 1.0) {
			latLinesPerDegree = 7.5;
		} else if (latitudeRangeFactor > 1.0 && latitudeRangeFactor <= 2.0) {
			latLinesPerDegree = 4.0;
		} else if (latitudeRangeFactor > 2.0 && latitudeRangeFactor <= 5.0) {
			latLinesPerDegree = 2.0;
		} else if (latitudeRangeFactor > 5.0 && latitudeRangeFactor <= 10.0) {
			latLinesPerDegree = 1.0;
		} else if (latitudeRangeFactor > 10.0 && latitudeRangeFactor <= 20.0) {
			latLinesPerDegree = 0.5;
		} else if (latitudeRangeFactor > 20.0 && latitudeRangeFactor <= 40.0) {
			latLinesPerDegree = 0.25;
		} else if (latitudeRangeFactor > 40.0 && latitudeRangeFactor <= 80.0) {
			latLinesPerDegree = 0.125;
		} else {
			latLinesPerDegree = 0.0625;
		}

		numLatLines = (int) (Math.ceil(maxLat * latLinesPerDegree) - Math
				.floor(minLat * latLinesPerDegree));

		if (maxLong < minLong) {
			maxLong += 360;
		}
		numLongLines = (int) (Math.ceil(maxLong * longLinesPerDegree) - Math
				.floor(minLong * longLinesPerDegree));

		double minPlottedLat = Math.floor(minLat * latLinesPerDegree)
				/ latLinesPerDegree;
		double minPlottedLong = Math.floor(minLong * longLinesPerDegree)
				/ longLinesPerDegree;

		Coordinate3d coord1 = new Coordinate3d();
		Coordinate3d coord2 = new Coordinate3d();
		LatLong LL1 = new LatLong();
		LatLong LL2 = new LatLong();

		Color currentColor, latLineColor, longLineColor;
		latLineColor = PamColors.getInstance().getColor(PamColor.LATLINE);
		longLineColor =PamColors.getInstance().getColor(PamColor.LONLINE);
		currentColor = g2d.getColor();
		(g2d).setColor(latLineColor);

		DecimalFormat f = new DecimalFormat();
		f.setMaximumFractionDigits(3);
		(g2d).setFont(latLongFont);
		Rectangle2D typBounds = latLongFont.getStringBounds("45 566", frc);
		typStringWidth = (int) typBounds.getWidth();
		typStringHeight = (int) typBounds.getHeight();

		for (int i = 0; i < numLatLines; i++) {
			LL1.setLatitude(minPlottedLat + i / latLinesPerDegree);
			if (LL1.getLatitude() < minLat || LL1.getLatitude() > maxLat) {
				continue;
			}
			LL1.setLongitude(maxLong);
			LL2.setLatitude(LL1.getLatitude());
			LL2.setLongitude(minLong);

			coord1 = rectProj.LL2panel(LL1);
			coord2 = rectProj.LL2panel(LL2);

			g2d.drawLine((int) coord1.x, (int) coord1.y, (int) coord2.x,
					(int) coord2.y);

			b = coord1.y - lineGradient * coord1.x;

			lineGradient = Math.atan2(coord2.y - coord1.y, coord2.x - coord1.x);
			intersectPoint.x = coord1.x + typStringWidth*Math.cos(lineGradient);
			intersectPoint.y = coord1.y + typStringWidth*Math.sin(lineGradient);
			intersectPoint2.x = coord2.x - typStringWidth*Math.cos(lineGradient);
			intersectPoint2.y = coord2.y - typStringWidth*Math.sin(lineGradient);
			if (mapPanel.isFillSurface()) {
				intersectPoint = recoverPoint(intersectPoint, lineGradient);
				intersectPoint2 = recoverPoint(intersectPoint2, lineGradient);
			}

//			if (LL1.getLatitude() >= 0.0) {
//				latDegs = Math.floor(LL1.getLatitude());
//				latMins = (LL1.getLatitude() - latDegs) * 60.0;
//			} else {
//				latDegs = Math.ceil(LL1.getLatitude());
//				latMins = -(LL1.getLatitude() - latDegs) * 60.0;
//			}

			LL1.setLongitude(maxLong); // -(1)/longLinesPerDegree;
			// coord1=rectProj.LL2panel(LL1);
			// coord1.x = panelWidth;
//			latString = (String.valueOf(f.format(latDegs)) + (char) 176 + " "
//					+ String.valueOf(f.format(latMins)) + (char) 180 + " Lat");
			latString = LL1.shortFormatLatitude();

			(g2d).setColor(currentColor);
			(g2d).fillRect(
					(int) (intersectPoint.x - typStringWidth / 2.0),
					(int) (intersectPoint.y - typStringHeight / 2 - 3),
					(int) typStringWidth + 6, (int) typStringHeight + 6);
			(g2d).setColor(latLineColor);
			(g2d).drawString(latString,
					(int) (intersectPoint.x - typStringWidth / 2.0),
					(int) (intersectPoint.y + typStringHeight / 2));
			
			(g2d).setColor(currentColor);
			(g2d).fillRect(
					(int) (intersectPoint2.x - typStringWidth / 2.0),
					(int) (intersectPoint2.y - typStringHeight / 2 - 3),
					(int) typStringWidth + 6, (int) typStringHeight + 6);
			(g2d).setColor(latLineColor);
			(g2d).drawString(latString,
					(int) (intersectPoint2.x - typStringWidth / 2.0),
					(int) (intersectPoint2.y + typStringHeight / 2));

		}

		for (int i = 0; i < numLongLines + 1; i++) {
			double newLongitude = minPlottedLong + i / longLinesPerDegree;
			LL1.setLongitude(newLongitude);
			if (newLongitude < minLong || newLongitude > maxLong) {
				continue;
			}
			LL1.setLatitude(maxLat);
			LL2.setLongitude(LL1.getLongitude());
			LL2.setLatitude(minLat);
			// System.out.println("StraightLineGrid, LongLines..." + LL1.latDegs
			// + " " + LL1.longDegs + " " + LL2.latDegs + " " + LL2.longDegs);
			coord1 = rectProj.LL2panel(LL1);
			coord2 = rectProj.LL2panel(LL2);
			g2d.drawLine((int) coord1.x, (int) coord1.y, (int) (coord2.x),
					(int) coord2.y);

			lineGradient = Math.atan2(coord2.y - coord1.y, coord2.x - coord1.x);
			intersectPoint.x = coord1.x + typStringHeight*Math.cos(lineGradient);
			intersectPoint.y = coord1.y + typStringHeight*Math.sin(lineGradient);
			intersectPoint2.x = coord2.x - typStringHeight*Math.cos(lineGradient);
			intersectPoint2.y = coord2.y - typStringHeight*Math.sin(lineGradient);
			if (mapPanel.isFillSurface()) {
				intersectPoint = recoverPoint(intersectPoint, lineGradient);
				intersectPoint2 = recoverPoint(intersectPoint2, lineGradient);
			}

//			if (LL1.getLongitude() >= 0.0) {
//				longDegs = Math.floor(LL1.getLongitude());
//				longMins = (LL1.getLongitude() - longDegs) * 60.0;
//			} else {
//				longDegs = Math.ceil(LL1.getLongitude());
//				longMins = -(LL1.getLongitude() - longDegs) * 60.0;
//			}

			// LL1.latDegs=maxLat;
			// ////minPlottedLat+(Math.floor(numLatLines/2+1)+0.5)/latLinesPerDegree;
			// coord1=rectProj.LL2panel(LL1);
//			longString = (String.valueOf(f.format(longDegs)) + (char) 176 + " "
//					+ String.valueOf(f.format(longMins)) + (char) 180 + " Lon");
			longString = LL2.shortFormatLongitude();


			(g2d).setColor(currentColor);
			(g2d).fillRect(
					(int) (intersectPoint.x - typStringWidth / 2.0 - 3), 
					(int) (intersectPoint.y	- typStringHeight / 2.0 - 3), 
					(int) typStringWidth + 6, (int) typStringHeight + 6);
			(g2d).setColor(longLineColor);
			(g2d).drawString(longString,
					(int) (intersectPoint.x - typStringWidth / 2.0),
					(int) (intersectPoint.y + typStringHeight / 2));
			(g2d).setColor(currentColor);
			(g2d).fillRect(
					(int) (intersectPoint2.x - typStringWidth / 2.0),
					(int) (intersectPoint2.y - typStringHeight / 2 - 3),
					(int) typStringWidth + 6, (int) typStringHeight + 6);
			(g2d).setColor(longLineColor);
			(g2d).drawString(longString,
					(int) (intersectPoint2.x - typStringWidth / 2.0),
					(int) (intersectPoint2.y + typStringHeight / 2));

		}

		(g2d).setColor(currentColor);

	}

	/**
	 * get a point back onto the screen to ensure visibility. 
	 * Never recover in more than one dimension or it wil be jogging back and forth !
	 * @param intersectPoint
	 * @param lineGradient
	 * @return modified point. 
	 */
	private Coordinate3d recoverPoint(Coordinate3d point, double lineGradient) {
		Coordinate3d pt = point.clone();
//		System.out.println("Line gradient: " + lineGradient);
		if (pt.x < typStringWidth) {
			pt.x = typStringWidth;
			pt.y += (pt.x-point.x) * Math.tan(lineGradient);
		}
		if (pt.x > mapPanel.getWidth()-typStringWidth) {
			pt.x = mapPanel.getWidth() - typStringWidth;
			pt.y += (pt.x-point.x) * Math.tan(lineGradient);
		}
		if (pt.y < typStringHeight) {
			pt.y = typStringHeight;
			pt.x += (pt.y-point.y) / Math.tan(lineGradient);
		}
		if (pt.y > mapPanel.getHeight()-typStringHeight) {
			pt.y = mapPanel.getHeight() - typStringHeight;
			pt.x += (pt.y-point.y) / Math.tan(lineGradient);
		}
		return pt;
	}

	public void setLatLinesPerDegree(double latLinesPerDegree) {
		this.latLinesPerDegree = latLinesPerDegree;
	}

	public void setLongLinesPerDegree(double longLinesPerDegree) {
		this.longLinesPerDegree = longLinesPerDegree;
	}

}
