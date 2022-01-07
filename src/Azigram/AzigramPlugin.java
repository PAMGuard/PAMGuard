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



package Azigram;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import fftManager.FFTDataUnit;

/**
 * Module that implements the Azigram algorithm from Thode et al 2019 J. Acoust.
 * Soc. Am. Vol 146(1) pp 95-102 (doi: 10.1121/1.5114810).
 * This module also includes the methods described in that paper for 
 * frequency domain demultiplexing of directional signals from DIFAR sonobuoys. 
 * 
 * This module is just a prototype, and has not been designed for efficiency. 
 * For quick prototyping it has been based on SpectrogramNoise and FFTDataUnit
 * super-classes, and should plot on the User Display Spectrogram (Swing). 
 *  
 * @author Brian Miller (Pamguard port) & Aaron Thode (original Matlab implementation)
 *
 */
public class AzigramPlugin implements PamPluginInterface {

	/**
	 * The name of the jarfile this package is contained in
	 */
	String jarFile;

	@Override
	public String getDefaultName() {
		return "Azigram engine";
	}

	@Override
	public String getHelpSetName() {
		return "Azigram/trunk/help/Azigram.hs";
	}

	@Override
	public String getClassName() {
		return "Azigram.AzigramControl";
	}

	@Override
	public String getDescription() {
		return "DIFAR Azigram Engine";
	}

	@Override
	public String getMenuGroup() {
		return "Sound Processing";
	}

	@Override
	public String getToolTip() {
		return "Azigram engine for multiplexed DIFAR data";
	}

	@Override
	public PamDependency getDependency() {
		return new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl");
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 0;
	}

	@Override
	public int getNInstances() {
		return 1;
	}

	@Override
	public boolean isItHidden() {
		return false;
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Brian Miller";
	}

	@Override
	public String getContactEmail() {
		return "Brian.Miller@aad.gov.au";
	}

	@Override
	public String getVersion() {
		return "0.0.1";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.00.18";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.01.03";
	}

	@Override
	public String getAboutText() {
		String desc = 
"This module that implements the Azigram algorithm from Thode et al 2019 " + 
"J. Acoust. Soc. Am. Vol 146(1) pp 95-102 (doi: 10.1121/1.5114810). " + 
"This module also includes the methods described in that paper for " + 
"frequency domain demultiplexing of directional signals from DIFAR sonobuoys. " + 
"\n\n" + 
"This module is just a prototype, and has not been designed for efficiency. " + 
"\n\n" + 
"Output from this module can be viewed on the User Display Spectrogram (Swing)."; 
 		return desc;
	}

	/* (non-Javadoc)
	 * @see PamModel.PamPluginInterface#allowedModes()
	 */
	@Override
	public int allowedModes() {
		return PamPluginInterface.ALLMODES;
	}
}
