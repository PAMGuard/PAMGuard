/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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



package qa.generator.location;

import qa.QAControl;
import qa.generator.clusters.QACluster;

/**
 * @author mo55
 *
 */
public class SmartLocationGenerator2 extends RandomLocationGenerator {

	/**
	 * @param qaControl
	 * @param qaCluster
	 * @param totalSequences
	 */
	public SmartLocationGenerator2(QAControl qaControl, QACluster qaCluster, int totalSequences, double[] rangeLimits) {
		super(qaControl, qaCluster, totalSequences, rangeLimits);
	}

	@Override
	public double getNominalRange() {
		Double smartRange = getSmartRange();
		if (smartRange == null) {
			return super.getNominalRange();
		}
		else {
			return smartRange;
		}
	}

	/**
	 * @return
	 */
	private Double getSmartRange() {
		// TODO Auto-generated method stub
		return null;
	}


}
