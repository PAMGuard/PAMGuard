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

package Array;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * 
 * @author Doug Gillespie Contains information about a preamlifier in a PamArray
 * @see Array.PamArray
 * @see Array.Hydrophone
 */
public class Preamplifier implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1;

	private double gain;

	private double[] bandwidth = new double[2];

	public Preamplifier(double gain, double[] bandwidth) {
		super();
		// TODO Auto-generated constructor stub
		this.gain = gain;
		this.bandwidth = bandwidth;
	}

	public double[] getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(double[] bandwidth) {
		this.bandwidth = bandwidth;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Preamplifier clone() {
		try {
		  return (Preamplifier) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();;
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
