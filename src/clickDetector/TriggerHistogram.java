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

import pamMaths.PamHistogram;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Creates a histogram of data for the click detector trigger display
 */
public class TriggerHistogram extends PamHistogram{


	private double decaySeconds;


	private long lastDecaySample;

	TriggerHistogram(double minVal, double maxVal, int nBins,
			double decaySeconds) {
		super(minVal, maxVal, nBins);
		this.decaySeconds = decaySeconds;
	}


	public void decay(double CurrentSample) {
		double decayConst = 1. - 1. / decaySeconds;
		for (int i = 0; i < getNBins(); i++) {
			data[i] *= decayConst;
		}
	}

}
