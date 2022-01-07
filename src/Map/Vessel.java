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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.reflect.Field;

import GPS.GpsData;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PamColors.PamColor;

public class Vessel extends PamSymbol implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6610207880007794175L;

	double vesselLength, vesselWidth;
	
	double dimA = 20; // lngth ahead of GPS
	double dimB = 20; // length astern of GPS
	double dimC = 5; // width to port of GPS
	double dimD = 5; // width to starboard of GPS
	
	int predictionArrow;

	double vesselLengthWidthRatio;

	public Color vesselColor;

	public Coordinate3d[] shipOutline;

	public Coordinate3d[] shipPolygon;

	public LatLong[] shipPolygonLatLog;

	static int vesselPolySize = 6;

	private GpsData shipGps;

	Coordinate3d centreOfRotation;

	Coordinate3d gpsReceiverPosition;

	private LatLong shipLocation;

	Coordinate3d shipPosition;

	double shipIconLength;

	double pixelsPerMetre;
	
//	private Color shipColor = Color.RED;

	public Vessel(Color vesselColor) {
		super();
//		shipColor = vesselColor;
		setShipColor(Color.RED);
		centreOfRotation = new Coordinate3d();
		gpsReceiverPosition = new Coordinate3d();
		shipLocation = new LatLong();
		shipPosition = new Coordinate3d();
//		shipGps = new GpsData();
		setSymbol(PamSymbolType.SYMBOL_CUSTOMPOLYGON);
	}
	
	public void setPixelsPerMetre(double pixelsPerMetre) {
		this.pixelsPerMetre = pixelsPerMetre;
	}


	public void setVesselDimension(double dimA, double dimB, double dimC, double dimD) {
		this.dimA = dimA;
		this.dimB = dimB;
		this.dimC = dimC;
		this.dimD = dimD;
	}
	public void setPredictionArrow(int predictionArrow) {
		this.predictionArrow = predictionArrow;
	}
	
	public Color getShipColor() {
		return this.getFillColor();
	}

	public void setShipColor(Color shipColor) {
		this.setFillColor(shipColor);
		this.setLineColor(shipColor);
	}

	public void setShipOutline() {
		// vesselLengthWidthRatio = vesselLength/vesselWidth;
		/*if (vesselLength < 10) {
			vesselLength = 10;
			// vesselWidth = vesselLength/vesselLengthWidthRatio;
		}*/

		// set the ouline of the ship (in metres)
//		shipOutline[0].x = -centreOfRotation.x;
//		shipOutline[0].y = -centreOfRotation.y;
//		shipOutline[5].x = shipOutline[0].x;
//		shipOutline[5].y = shipOutline[0].y;
//		shipOutline[1].x = shipOutline[0].x;
//		shipOutline[1].y = 0.7 * vesselLength - centreOfRotation.y;
//		shipOutline[2].x = 0.0;
//		shipOutline[2].y = vesselLength - centreOfRotation.y;
//		shipOutline[3].x = -shipOutline[0].x;
//		shipOutline[3].y = shipOutline[1].y;
//		shipOutline[4].x = -shipOutline[0].x;
//		shipOutline[4].y = shipOutline[0].y;
		
		shipOutline[0].x = -centreOfRotation.x;
		shipOutline[0].y = -centreOfRotation.y;
		shipOutline[1].x = shipOutline[0].x;
		shipOutline[1].y = 0.7 * vesselLength - centreOfRotation.y;
		shipOutline[2].x = shipOutline[0].x + vesselWidth/2;
		shipOutline[2].y = vesselLength - centreOfRotation.y;
		shipOutline[3].x = shipOutline[0].x + vesselWidth;
		shipOutline[3].y = shipOutline[1].y;
		shipOutline[4].x = shipOutline[3].x;
		shipOutline[4].y = shipOutline[0].y;
		shipOutline[5].x = shipOutline[0].x;
		shipOutline[5].y = shipOutline[0].y;

		for (int i = 0; i < shipOutline.length; i++) {
			Coordinate3d tempCoord = new Coordinate3d();
			tempCoord.x = shipOutline[i].x;
			tempCoord.y = shipOutline[i].y;
			TransformUtilities.rotateDegreesZ(tempCoord,
					shipGps.getHeading());
			shipOutline[i].x = tempCoord.x;
			shipOutline[i].y = tempCoord.y;
			// System.out.println("Vessel, shipOutline: " + shipOutline[i].x + "
			// " + shipOutline[i].y + " pixPerMetre: " + pixelsPerMetre) ;
		}
	}

	/**
	 * Draw the ship
	 * @param g2d Graphics context
	 * @param rectProj graphics projector
	 * @return 3D plot coordinate. 
	 */
	public Coordinate3d drawShip(Graphics2D g2d, MapRectProjector rectProj) {
		
		setPixelsPerMetre(rectProj.getPixelsPerMetre());
		
		vesselLength = (dimA + dimB) * pixelsPerMetre; // newVesselLength*pixelsPerMetre;
		vesselWidth = (dimC + dimD) * pixelsPerMetre; // newVesselWidth*pixelsPerMetre;

		if (vesselLength < 10.0) {
			vesselLength = 10.0;
			vesselWidth = 4.5;
			// vesselWidth = vesselLength/vesselLengthWidthRatio;
		}

		// vesselColor = newVesselColor;

		centreOfRotation.x = vesselWidth * dimC / Math.max(dimC + dimD, 1);
		centreOfRotation.y = vesselLength * dimB / Math.max(dimA + dimB,1);
		gpsReceiverPosition.x = vesselWidth * dimC / Math.max(dimC + dimD, 1);
		gpsReceiverPosition.y = vesselLength * dimB / Math.max(dimA + dimB, 1);

		shipOutline = new Coordinate3d[vesselPolySize];
		shipPolygon = new Coordinate3d[vesselPolySize];
		shipPolygonLatLog = new LatLong[vesselPolySize];
		for (int i = 0; i < vesselPolySize; i++) {
			shipOutline[i] = new Coordinate3d();
			shipPolygon[i] = new Coordinate3d();
		}

		setShipOutline();
		Coordinate3d tempOutline = new Coordinate3d();

		GpsData plotGpsPoint = getShipGps(true);
		if (plotGpsPoint == null) {
			System.out.println("shipGps is null");
		}
		shipPosition = rectProj.getCoord3d(plotGpsPoint.getLatitude(), plotGpsPoint
				.getLongitude(), 0.0);
//		g2d.drawLine(0, 0, (int)shipPosition.x, (int)shipPosition.y);
		
		for (int i = 0; i < shipOutline.length; i++) {
			tempOutline.x = shipOutline[i].x;
			tempOutline.y = shipOutline[i].y;
			LatLong polyLL = new LatLong();
			polyLL = rectProj.image2LL(tempOutline);
			polyLL.setLatitude(polyLL.getLatitude() + plotGpsPoint.getLatitude());
			polyLL.setLongitude(polyLL.getLongitude() + plotGpsPoint.getLongitude());
			tempOutline = rectProj.getCoord3d(polyLL.getLatitude(), polyLL.getLongitude(),
					0.0);
			shipPolygon[i].x = tempOutline.x;
			shipPolygon[i].y = tempOutline.y;
		}

		int[] shipPolyX = new int[6];
		int[] shipPolyY = new int[6];

		TransformUtilities.Coordinate3d2XyArrays(shipPolyX, shipPolyY,
				shipPolygon);

		Polygon shipPoly = new Polygon(shipPolyX, shipPolyY, 6);
		/*
		 * for (int i = 0; i<shipPoly.npoints; i++){
		 * //System.out.println("Vessel, polyScreen: " + shipPoly.xpoints[i] + " " +
		 * shipPoly.ypoints[i]); }
		 */
		Color currentColor;
		currentColor = g2d.getColor();
		(g2d).setColor(getShipColor());
		(g2d).fillPolygon(shipPoly);
		(g2d).setColor(getLineColor());
		(g2d).drawPolygon(shipPoly);
		g2d.setColor(Color.WHITE);
		g2d.drawOval((int)shipPosition.x-2, (int) shipPosition.y-2, 4, 4); 
		if (predictionArrow > 0) {
			// draw a line from the front of the ship. Course over ground first !
			g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
			double lineLength = predictionArrow * shipGps.getSpeed() * GpsData.METERSPERMILE / 3600;
			lineLength = Math.max(lineLength, 1);
			LatLong endGps;
			Coordinate3d p1, p2;
			int headLength = 8;
			if (shipGps.getSpeed() > 0) {
				endGps = plotGpsPoint.travelDistanceMeters(plotGpsPoint.getCourseOverGround(), lineLength);
				p1 = rectProj.getCoord3d(plotGpsPoint.getLatitude(), plotGpsPoint.getLongitude(), 0);
				p2 = rectProj.getCoord3d(endGps.getLatitude(), endGps.getLongitude(), 0);
				//			g2d.drawLine((int) p1.x, (int) p1.y,(int) p2.x, (int) p2.y);
				double xoff = shipPolyX[2] -  p1.x;
				double yoff = shipPolyY[2] -  p1.y;
				PamSymbol.drawArrow(g2d, xoff + p1.x, yoff + p1.y, xoff+ p2.x, yoff+ p2.y, headLength, 30, false);
			}
			Double trueHead = shipGps.getHeading(true);
			if (trueHead != null && !Double.isNaN(trueHead)) {
				lineLength = Math.max(lineLength, 4);
				endGps = plotGpsPoint.travelDistanceMeters(trueHead, lineLength);
				p1 = rectProj.getCoord3d(plotGpsPoint.getLatitude(), plotGpsPoint.getLongitude(), 0);
				p2 = rectProj.getCoord3d(endGps.getLatitude(), endGps.getLongitude(), 0);
				//			g2d.drawLine((int) p1.x, (int) p1.y,(int) p2.x, (int) p2.y);
				double xoff = shipPolyX[2] -  p1.x;
				double yoff = shipPolyY[2] -  p1.y;
				p1.x += xoff;
				p1.y += yoff;
				p2.x += xoff;
				p2.y += yoff;
				PamSymbol.drawArrow(g2d, p1.x, p1.y, p2.x, p2.y, headLength, 30, false);
				// want a double array head, so work out it's direction and add 2 or 3 pixels. 
				double dx = p2.x-p1.x;
				double dy = p2.y-p1.y;
				double mag = Math.sqrt(dx*dx + dy*dy);
				dx /= mag;
				dy /= mag;
				double extraLength = 5;
				dx *= extraLength;
				dy *= extraLength;
				Coordinate3d p3 = new Coordinate3d();
				p3.x = p2.x + dx;
				p3.y = p2.y + dy;
				PamSymbol.drawArrow(g2d, p2.x, p2.y, p3.x, p3.y, headLength, 30, false);
			}
		}
		(g2d).setColor(currentColor);
		return shipPosition;
	}
	
	public Rectangle drawShip(Graphics g, Point xy, double pixelsPerMetre, double heading) {
		
		Graphics2D g2d = (Graphics2D) g;
		Polygon shipGon = getShipPolygon(xy, pixelsPerMetre, heading);

		(g2d).setColor(getShipColor());
		(g2d).fillPolygon(shipGon);
		(g2d).setColor(getLineColor());
		(g2d).drawPolygon(shipGon);
//		g2d.setColor(Color.WHITE);
//		g2d.drawOval(xy.x-2, xy.y-2, 4, 4); 
		
		return shipGon.getBounds();
	}
	
	private Polygon getShipPolygon(Point centre, double pixelsPerMetre, double heading) {
		int x[] = new int[6];
		int y[] = new int[6];
		double drawLength = (int) Math.max((dimA + dimB) * pixelsPerMetre, 15);
		double drawScale = drawLength / (dimA + dimB);
		double A, B, C, D;
		A = dimA * drawScale;
		B = dimB * drawScale;
		C = Math.max(dimC * drawScale, 5);
		D = Math.max(dimD * drawScale, 5);
		x[0] = (int) C;
		x[1] = (int) C;
		x[2] = 0;
		x[3] = (int) -D;
		x[4] = (int) -D;
		x[5] = x[0];
		y[0] = (int) B;
		y[1] = (int) (y[0] - (A+B) * 0.7);
		y[2] = (int) -A;
		y[3] = y[1];
		y[4] = y[0];
		y[5] = y[0];
		Polygon shipGon = new Polygon(x, y, 6);
		TransformUtilities.rotatePolygon(shipGon, (-heading) * Math.PI / 180.);
		shipGon.translate(centre.x, centre.y);
		return shipGon;
	}

	public GpsData getShipGps(boolean predict) {
		if (shipGps == null) {
			return null;
		}
		else if (predict == false) {
			return shipGps;
		}
		else {
			return shipGps.getPredictedGPSData(PamCalendar.getTimeInMillis(), 600000L);
		}
	}

	public void setShipGps(GpsData shipGps) {
//		if (shipGps == null) shipGps = new GpsData();
		this.shipGps = shipGps;
	}

	public LatLong getShipLLD() {
		if (shipGps == null) return null;
		return (new LatLong(shipGps.getLatitude(), shipGps
				.getLongitude()));
	}

	public Coordinate3d getShipPosition() {
		return shipPosition;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// TODO Auto-generated method stub
		super.paintIcon(c, g, x, y);
	}

	/*
	 * (non-Javadoc)
	 * @see PamView.CustomSymbol#getXPoints()
	 */
	// these are to keep CustomSymbol happy - so return a normalised version
	// of the coordinates. 
	@Override
	public double[] getXPoints() {

		double C, D;
		double x[] = new double[6];
//		C = dimC;
//		D = dimD;
//		if ((C+D) < 1) {
//			D = C = 0.5;
//		}
//		else {
//			C = dimC / (dimD+dimC);
//			D = dimD / (dimD+dimC);
//		}
		C = D = 0.3;
		x[0] = C;
		x[1] = C;
		x[2] = 0;
		x[3] = -D;
		x[4] = -D;
		x[5] = x[0];
		return x;
	}
	
	@Override
	public double[] getYPoints() {
		double A, B;
//		A = dimA;
//		B = dimB;
//		if ((A + B) < 2) {
//			A = B = 1;
//		}
//		else {
//			A = 2 * dimA / (dimA+dimB);
//			B = 2 * dimD / (dimA+dimB);
//		}
		A = B = 1;
		double y[] = new double[6];
		y[0] = B;
		y[1] = (y[0] - (A+B) * 0.7);
		y[2] = -A;
		y[3] = y[1];
		y[4] = y[0];
		y[5] = y[0];
		return y;
	}

	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("centreOfRotation");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return centreOfRotation;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("dimA");
			ps.put(new PrivatePamParameterData(this, field, "Length Ahead of GPS", null) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dimA;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("dimB");
			ps.put(new PrivatePamParameterData(this, field, "Length Astern of GPS", null) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dimB;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("dimC");
			ps.put(new PrivatePamParameterData(this, field, "Width to Port of GPS", null) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dimC;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("dimD");
			ps.put(new PrivatePamParameterData(this, field, "Width to Starboard of GPS", null) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dimD;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("gpsReceiverPosition");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return gpsReceiverPosition;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("pixelsPerMetre");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return pixelsPerMetre;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("predictionArrow");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return predictionArrow;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("shipIconLength");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return shipIconLength;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("shipLocation");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return shipLocation;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("shipPosition");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return shipPosition;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("vesselLength");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return vesselLength;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("vesselLengthWidthRatio");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return vesselLengthWidthRatio;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("vesselWidth");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return vesselWidth;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
