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

import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import GPS.GpsData;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCoordinate;

/**
 * @author David McLaren
 *         <p>
 *         Coordinate transform utilites
 * 
 */

public class TransformUtilities {

	static final double EARTHRADIUS = 6378160.;
	
	private static double cameraFileOfView = 30;

	private static double cameraScale = Math.tan(Math.toRadians(cameraFileOfView/2))*2.;	
	
	private double rotateZDegrees = 0;

	private double rotateXDegrees = 0;
	
	/*
	 * Precalculated rotation factors. 
	 */
	private double sinZ = 0;
	private double cosZ = 1.;
	private double sinX = 0;
	private double cosX = 1;
	
	public TransformUtilities() {
	}

	/**
	 * Rotate a point about the Z axis. 
	 * @param c point to rotate
	 * @param rotateZDegrees rotation angle in degrees
	 */
	public void rotateDegreesZ(PamCoordinate c) {
//		double thetaRads = Math.toRadians(rotateZDegrees);
//		double newx = c.getCoordinate(0) * Math.cos(thetaRads) + c.getCoordinate(1) * Math.sin(thetaRads);
//		double newy = c.getCoordinate(1) * Math.cos(thetaRads) - c.getCoordinate(0) * Math.sin(thetaRads);
		double newx = c.getCoordinate(0) * cosZ + c.getCoordinate(1) * sinZ;
		double newy = c.getCoordinate(1) * cosZ - c.getCoordinate(0) * sinZ;
		c.setCoordinate(0, newx);
		c.setCoordinate(1, newy);
	}
	/**
	 * Rotate a point about the Z axis in the opposite direction. 
	 * @param c point to rotate
	 * @param rotateZDegrees rotation angle in degrees
	 */
	public void unRotateDegreesZ(PamCoordinate c) {
		double newx = c.getCoordinate(0) * cosZ - c.getCoordinate(1) * sinZ;
		double newy = c.getCoordinate(1) * cosZ + c.getCoordinate(0) * sinZ;
		c.setCoordinate(0, newx);
		c.setCoordinate(1, newy);
	}
	
	/**
	 * Rotate a point using a static method which doesn't alter the 
	 * main underlying rotation information set from the map projector. 
	 * @param c point to rotate
	 * @param degrees rotation angle in degrees
	 */
	public static void rotateDegreesZ(PamCoordinate c, double degrees) {
		double thetaRads = Math.toRadians(degrees);
		double newx = c.getCoordinate(0) * Math.cos(thetaRads) + c.getCoordinate(1) * Math.sin(thetaRads);
		double newy = c.getCoordinate(1) * Math.cos(thetaRads) - c.getCoordinate(0) * Math.sin(thetaRads);
		c.setCoordinate(0, newx);
		c.setCoordinate(1, newy);
	}
	
	/**
	 * Rotate about the X axis. i.e. x is unchanged, but y and z change. 
	 * @param c
	 * @param rotateXDegrees
	 */
	public void rotateDegreesX(PamCoordinate c) {
//		double rads = Math.toRadians(rotateXDegrees);
//		double sn = Math.sin(rads);
//		double cs = Math.cos(rads);
		double newY = c.getCoordinate(1)*cosX + c.getCoordinate(2)*sinX;
		double newZ = c.getCoordinate(2)*cosX - c.getCoordinate(1)*sinX;
		c.setCoordinate(1, newY);
		c.setCoordinate(2, newZ);
	}
	/**
	 * Rotate about the X axis in the opposite direction. i.e. x is unchanged, but y and z change. 
	 * @param c
	 * @param rotateXDegrees
	 */
	public void unRotateDegreesX(PamCoordinate c) {
		double newY = c.getCoordinate(1)*cosX - c.getCoordinate(2)*sinX;
		double newZ = c.getCoordinate(2)*cosX + c.getCoordinate(1)*sinX;
		c.setCoordinate(1, newY);
		c.setCoordinate(2, newZ);
	}

	/**
	 * Add perspective to a point. Camera automatically set at a height which
	 * will perfectly match the canvas height with a 30 degree field of view. 
	 * @param panelPos xyz position on the canvas
	 * @param panelHeight height of panel. 
	 */
	public void doPerspective(Coordinate3d panelPos, double panelHeight) {
		double cameraHeight = panelHeight/cameraScale;
		panelPos.x = Math.atan2(panelPos.x, Math.abs(cameraHeight-panelPos.z)) * cameraHeight;
		panelPos.y = Math.atan2(panelPos.y, Math.abs(cameraHeight-panelPos.z)) * cameraHeight;
	}
	
	/**
	 * Remove perspective from a point. Camera automatically set at a height which
	 * will perfectly match the canvas height with a 30 degree field of view. 
	 * @param panelPos xyz position on the canvas
	 * @param panelHeight height of panel. 
	 */
	public void unDoPerspective(Coordinate3d panelPos, double panelHeight) {
		/**
		 * When this is called from a mouse event on the map it's 99.9% certain that 
		 * panelPos.z will be zero (what else can it be !). To correctly calculate xy
		 * positions we have to know what z we're after - so assume it's the sea surface, which
		 * means that we have to correct for the rotation of the surface about the x azis (tilting
		 * forwards in z). 
		 */
		double cameraHeight = panelHeight/cameraScale;
		double vertScreenAngle = Math.atan2(panelPos.y,cameraHeight);
		double tilt = Math.toRadians(rotateXDegrees);
		double r = cameraHeight * Math.sin(Math.PI/2. + tilt) / Math.sin(Math.PI/2.-vertScreenAngle-tilt);
		double surfaceZ = cameraHeight-r*Math.cos(vertScreenAngle);
//		System.out.printf("Camera height %3.1f, dist to surface %3.1f, surface Z = %3.1f\n", cameraHeight, r, surfaceZ);
		panelPos.z = surfaceZ;
		/* End of z correction calculation*/
		panelPos.x = (cameraHeight-panelPos.z) * Math.tan(panelPos.x/cameraHeight);
		panelPos.y = (cameraHeight-panelPos.z) * Math.tan(panelPos.y/cameraHeight);
	} 
	
	/**
	 * get the camera height for a given panel height. 
	 * @param panelHeight display panel height
	 * @return camera height
	 */
	public double getCameraHeight(double panelHeight) {
		return panelHeight/cameraScale;
	}
	
	/**
	 * 
	 * @return The camera field of view in degrees
	 */
	public double getCameraFieldOfView() {
		return cameraFileOfView;
	}
	
	

	/**
	 * Convert an array of 3D coordinates to arrays of x and y integers. 
	 * @param x preallocated array of x coordinates
	 * @param y preallocated array of y coordinates
	 * @param c 3d coordinates. 
	 */
	public static void Coordinate3d2XyArrays(int[] x, int[] y, Coordinate3d[] c) {
		for (int i = 0; i < c.length; i++) {
			x[i] = (int) c[i].x;
			y[i] = (int) c[i].y;
		}
	}
	
	/**
	 * Rotate a polygon about the Z axis. Used by ship. May
	 * need to replace with a more complete rotation system
	 * @param polygon polygon to rotate
	 * @param angleRadians angle in radians 
	 */
	public static void rotatePolygon(Polygon polygon, double angleRadians) {
		double cosTheta = Math.cos(angleRadians);
		double sinTheta = Math.sin(angleRadians);
		int newX, newY;
		for (int i = 0; i < polygon.npoints; i++) {
			newX = (int) (polygon.xpoints[i] * cosTheta + polygon.ypoints[i] * sinTheta);
			newY = (int) (polygon.ypoints[i] * cosTheta - polygon.xpoints[i] * sinTheta);
			polygon.xpoints[i] = newX;
			polygon.ypoints[i] = newY;
		}
	}

	/**
	 * @return the rotateZDegrees
	 */
	public double getRotateZDegrees() {
		return rotateZDegrees;
	}

	/**
	 * @param rotateZDegrees the rotateZDegrees to set
	 */
	public void setRotateZDegrees(double rotateZDegrees) {
		this.rotateZDegrees = rotateZDegrees;
		sinZ = Math.sin(Math.toRadians(rotateZDegrees));
		cosZ = Math.cos(Math.toRadians(rotateZDegrees));
	}

	/**
	 * @return the rotateXDegrees
	 */
	public double getRotateXDegrees() {
		return rotateXDegrees;
	}

	/**
	 * @param rotateXDegrees the rotateXDegrees to set
	 */
	public void setRotateXDegrees(double rotateXDegrees) {
		this.rotateXDegrees = rotateXDegrees;
		sinX = Math.sin(Math.toRadians(rotateXDegrees));
		cosX = Math.cos(Math.toRadians(rotateXDegrees));
	}

}
