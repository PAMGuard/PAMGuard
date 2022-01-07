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

/**
 * Class definition for a x,y coordinate number type.
 * 
 * @author David McLaren
 * 
 */

public class CoordsXY {

	public double x = 0.0;

	public double y = 0.0;

	public CoordsXY() {
	}

	public CoordsXY(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public CoordsXY(CoordsXY a) {
		this.x = a.x;
		this.y = a.y;
	}

}
