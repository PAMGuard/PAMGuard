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



package beamformer.algorithms.nullalgo;

import beamformer.BeamAlgorithmParams;

/**
 * @author mo55
 *
 */
public class NullBeamParams extends BeamAlgorithmParams {

	public static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public NullBeamParams(String algorithmName, int groupNumber, int channelMap) {
		super(algorithmName, groupNumber, channelMap);
	}

	/* (non-Javadoc)
	 * @see beamformer.BeamAlgorithmParams#clone()
	 */
	@Override
	public BeamAlgorithmParams clone() {
		NullBeamParams newOne = new NullBeamParams(this.getAlgorithmName(), this.getGroupNumber(), this.getChannelMap());
		return newOne;
	}

}
