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
package fftManager;

import java.io.Serializable;
import java.lang.reflect.Modifier;

import PamModel.parametermanager.FieldNotFoundException;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import Spectrogram.WindowFunction;
import spectrogramNoiseReduction.SpectrogramNoiseSettings;

public class FFTParameters implements Serializable, ManagedParameters, Cloneable {

	static public final long serialVersionUID = 2;

	String name = "";

	public int fftLength = 1024;

	public int fftHop = 512;

	public int channelMap = 3;
	
//	public String rawDataSource;
	public int dataSource = 0;
	
	public String dataSourceName;
	
	public int windowFunction = WindowFunction.HANNING;
	
	// parameters for click removal
	public boolean clickRemoval = false;
	
	public double clickThreshold = 5;
	
	public int clickPower = 6;
	
	public SpectrogramNoiseSettings spectrogramNoiseSettings = new SpectrogramNoiseSettings();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static boolean isValidLength(int len) {
		return(Integer.bitCount(len) == 1);
	}

	@Override
	public FFTParameters clone() {
		FFTParameters newParams = null;
		try {
			newParams = (FFTParameters) super.clone();
			if (newParams.spectrogramNoiseSettings == null) {
				newParams.spectrogramNoiseSettings = new SpectrogramNoiseSettings();
			}
			else {
				newParams.spectrogramNoiseSettings = this.spectrogramNoiseSettings.clone();
			}
		}
		catch(CloneNotSupportedException Ex) {
			Ex.printStackTrace(); 
			return null;
		}
		return newParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet pps = PamParameterSet.autoGenerate(this, Modifier.FINAL | Modifier.STATIC);
		try {
			PamParameterData param = pps.findParameterData("fftLength");
			param.setShortName("FFT Length");
			param.setToolTip("Detector detection threshold in dB");
		}
		catch (FieldNotFoundException e) {
			e.printStackTrace();
		}
		return pps;
	}
}
