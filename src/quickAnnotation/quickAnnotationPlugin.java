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



package quickAnnotation;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;

/**
 * @author SCANS
 *
 */
public class quickAnnotationPlugin implements PamPluginInterface {

	/**
	 * The name of the jarfile this package is contained in
	 */
	String jarFile;

	@Override
	public String getDefaultName() {
		return "Quick Spectrogram Annotation";
	}

	@Override
	public String getHelpSetName() {
		return "quickAnnotation/quickAnnotation.hs";
	}


	@Override
	public String getClassName() {
		return "quickAnnotation.QuickAnnotationModule";
	}

	@Override
	public String getDescription() {
		return "Quick Spectrogram Annotation";
	}

	@Override
	public String getMenuGroup() {
		return "Utilities";
	}

	@Override
	public String getToolTip() {
		return "Manual marking on the spectrogram display using user-defined 'quick' annotations";
	}

	@Override
	public PamDependency getDependency() {
		return null;
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 1;
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
		return "10.0.2";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "1.15.05";
	}

	@Override
	public String getPamVerTestedOn() {
		return "1.15.05";
	}

	@Override
	public String getAboutText() {
		String desc = "Quick Spectrogram Annotations are similar to Spectrogram Annotations "+
	"and allow the user to make marks on the spectrogram display and add a small amount of "+
	"text to go with each mark.  They can be used either in real time or offline using the "+
	"PAMGuard viewer.  Data from the marks are stored in the PAMGuard Database";
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
