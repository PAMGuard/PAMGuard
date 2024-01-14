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

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;



/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         A set of ClickTypeParams associated with a BasicClickIdentifier.
 */
public class BasicClickIdParameters implements Serializable, Cloneable, ManagedParameters {

	public ArrayList<ClickTypeParams> clickTypeParams = new ArrayList<ClickTypeParams>();

	static public final long serialVersionUID = 1;

	@Override
	public BasicClickIdParameters clone() {
		try {
			return (BasicClickIdParameters) super.clone();
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @return the first free unique intiger that can be used to identify a
	 *         click type for a specific detector.
	 */
	public int getFirstFreeClickIdentifier() {
		int id = 1;
		boolean used;
		if (clickTypeParams == null || clickTypeParams.size() == 0) {
			return 1;
		} else
			while (++id < Integer.MAX_VALUE) {
				used = false;
				for (int i = 0; i < clickTypeParams.size(); i++) {
					if (id == clickTypeParams.get(i).getSpeciesCode()) {
						used = true;
						break;
					}
					if (used == false) {
						return id;
					}
				}
			}
		return -1;
	}
	
	public ClickTypeParams createStandard(int iStandard) {
		return new ClickTypeParams(getFirstFreeClickIdentifier(), iStandard);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
