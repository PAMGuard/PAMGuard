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



package PamguardMVC;

/**
 * <p>Interface to indicate that the data units contained within this datablock
 * can provide their own FFT data.</p>
 * <p><strong>Should be used when the data units
 * implement the {@link FFTDataHolder} interface</strong></p>
 * 
 * @author mo55
 *
 */
public interface FFTDataHolderBlock {
	
	/**
	 * Return the fft parameters in the following format:
	 * <ul>
	 * <li>int[0] = fft length</li>
	 * <li>int[1] = fft hop</li>
	 * </ul>
	 * 
	 * @return fft length and hop, respectively, in an int array
	 */
	public int[] getFFTparams();

}
