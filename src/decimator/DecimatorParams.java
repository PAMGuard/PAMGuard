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
package decimator;

import java.io.Serializable;

import Filters.FilterBand;
import Filters.FilterParams;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class DecimatorParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1;
	
	public String rawDataSource;
	
	public float newSampleRate = 2000;
	
	public int channelMap;

	public FilterParams filterParams;
	
	public int interpolation = 0;

	DecimatorParams() {
		super();
		filterParams = new FilterParams();
		filterParams.filterBand = FilterBand.LOWPASS;
		filterParams.lowPassFreq = newSampleRate / 2;
		filterParams.filterOrder = 6;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected DecimatorParams clone() {
		try {
			DecimatorParams dp = (DecimatorParams) super.clone();
			dp.filterParams = filterParams.clone();
			if (dp.channelMap == 0) {
				dp.channelMap = 0xFFFF;
			}
			return dp;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
}
