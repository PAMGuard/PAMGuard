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

/**
 * @author David J McLaren
 *
 */
public class CoordUtils {

	/** Given two Coordinate3d objects, return the distance between them.
	 */
	public static double Pam3dRangeCalc(Coordinate3d a, Coordinate3d b){
		double range;
		range=Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z));
		return range;
	}

	/** Given two N-element vectors representing positions, return the 
	 * distance between them. This is just the L2-norm of the two vectors.
	 * 
	 * see See also CoordUtils.norm().
	 */
	public static double dist(double[] a, double[] b) {
		double sumSq = 0.0;
		for (int i = 0; i < a.length; i++)
			sumSq += (b[i] - a[i]) * (b[i] - a[i]);
		return Math.sqrt(sumSq);
	}

	/** Given two N-element vectors representing positions, return the 
	 * distance between them. This is just the L2-norm of the two vectors.
	 * This one differs from the one above in the N is specified explicitly.
	 * 
	 * <p>see See also CoordUtils.norm().
	 * <p>author Dave Mellinger
	 */
	public static double dist(double[] a, double[] b, int n) {
		double sumSq = 0.0;
		for (int i = 0; i < n; i++)
			sumSq += (b[i] - a[i]) * (b[i] - a[i]);
		return Math.sqrt(sumSq);
	}

	
	/** Find the sum of squares of a vector.
	 * 
	 * <p>see See also CoordUtils.dist().
	 * <p>author Dave Mellinger
	 */
	public static double sumSquared(double[] x) {
		double sumSq = 0;
		for (int i = 0; i < x.length; i++)
			sumSq += x[i] * x[i];
		return sumSq;
	}
	
}
