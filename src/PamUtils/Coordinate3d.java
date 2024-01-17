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
package PamUtils;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Class definition for a x,y coordinate number type.
 * 
 * @author David McLaren
 * 
 */

public class Coordinate3d implements Serializable , Cloneable, PamCoordinate, ManagedParameters {

	/*
	 * Default for back compatibility with PAMguard 1.15.##
	 */
	private static final long serialVersionUID = -5396075323830594094L;

	public double x = 0.0;

	public double y = 0.0;

	public double z = 0.0;


	public Coordinate3d() {
	}

	public Coordinate3d(double x, double y) {
		this.x = x;
		this.y = y;
		this.z = 0.0;
	}

	public Coordinate3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Coordinate3d(Coordinate3d a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
	}

	public void assign(Coordinate3d a) {
		this.x = a.x;
		this.y = a.y;
		this.z = a.z;
	}

	public Point getXYPoint() {
		return new Point((int) x, (int) y);
	}
	
	/**
	 * Get a double precision point
	 * @return Doube precision Point2D.
	 */
	public Point2D getPoint2D() {
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Get a double precision point output as a JavaFX library Point2D object. 
	 * @return Doube precision Point2D.
	 */
	public javafx.geometry.Point2D getPoint2DFX() {
		return new javafx.geometry.Point2D(x,y); 
	}

	/**
	 * Get the geometric distance between two coordinates. 
	 * @param o other coordinate. 
	 * @return geometric distance. 
	 */
	public double distance(Coordinate3d o) {
		return Math.sqrt(Math.pow(o.x-x,2)+Math.pow(o.y-y,2)+Math.pow(o.z-z,2));
	}

	@Override
	public double getCoordinate(int iCoordinate) {
		switch (iCoordinate) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		}
		return Double.NaN;
	}

	@Override
	public void setCoordinate(int iCoordinate, double value) {
		switch (iCoordinate) {
		case 0:
			x = value;
			break;
		case 1:
			y = value;
			break;
		case 2:
			z = value;
			break;
		}
	}


	@Override
	public int getNumCoordinates() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Coordinate3d clone() {
		try {
			return (Coordinate3d) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = super.toString();
		if (x < 1e9) {
			return str + String.format(" x=%3.2f, y=%3.2f, z = %3.2f", x, y, z);
		}
		else {
			long m = (long) x;
			return str + String.format(" x=%3.2f (%s.%03d), y=%3.2f, z = %3.2f", x, PamCalendar.formatDateTime2(m), m%1000, y, z);
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
