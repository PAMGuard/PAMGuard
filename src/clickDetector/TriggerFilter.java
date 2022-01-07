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

package clickDetector;

/**
 * @author Doug Gillespie
 *         <p>
 *         Really simple filter for creating decaying averages of the rectified
 *         signals in the click triggers.
 * 
 */
public class TriggerFilter {

	double[] alpha = new double[2];

	double[] alpha_1 = new double[2];

	double memory;

	/**
	 * 
	 * @param alpha
	 *            decay constant
	 * @param initialValue
	 *            Initidalisation value
	 */
	public TriggerFilter(double alpha, double initialValue) {
		setupFilter(alpha, alpha, initialValue);
	}

	/**
	 * 
	 * @param alpha1
	 *            decay constant
	 * @param alpha2
	 *            decay constant for use when trigger function is above
	 *            threshold
	 * @param initialValue
	 *            initialisation value
	 */
	public TriggerFilter(double alpha1, double alpha2, double initialValue) {
		setupFilter(alpha1, alpha2, initialValue);
	}

	/**
	 * Initialises the filter
	 * 
	 * @param alpha1
	 *            decay constant
	 * @param alpha2
	 *            decay constant for use when trigger function is above
	 *            threshold
	 * @param initialValue
	 *            initialisation value
	 */
	private void setupFilter(double alpha1, double alpha2, double initialValue) {
		this.alpha[0] = alpha1;
		alpha_1[0] = 1.0 - alpha1;
		this.alpha[1] = alpha2;
		alpha_1[1] = 1.0 - alpha2;
		memory = initialValue;
	}

	/**
	 * Runs the filter ona single sample
	 * 
	 * @param newValue
	 *            filter input
	 * @param overThreshold
	 *            over threshold flag (controls which alpha value to use)
	 * @return filter output
	 */
	public double runFilter(double newValue, boolean overThreshold) {
		if (overThreshold)
			return (memory = memory * alpha_1[1] + Math.abs(newValue)
					* alpha[1]);
		else
			return (memory = memory * alpha_1[0] + Math.abs(newValue)
					* alpha[0]);
	}

	public void setMemory(double memory) {
		this.memory = memory;
	}
}
