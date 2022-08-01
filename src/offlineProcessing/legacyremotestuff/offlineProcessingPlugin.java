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



package offlineProcessing.legacyremotestuff;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;

/**
 * @author SCANS
 *
 */
public class offlineProcessingPlugin implements PamPluginInterface {

	String jarFile;

	@Override
	public String getClassName() {
		return "offlineProcessing.OfflineProcessingControlledUnit";
	}

	@Override
	public String getDefaultName() {
		return "Offline Processing";
	}

	@Override
	public String getDescription() {
		return "Offline Processing";
	}

	@Override
	public String getMenuGroup() {
		return "Utilities";
	}

	@Override
	public String getToolTip() {
		return "Used to control offline data tasks";
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
	public String getHelpSetName() {
		return null;
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
		return "Graham Weatherup";
	}

	@Override
	public String getContactEmail() {
		return "gw@sa-instrumentation.com";
	}

	@Override
	public String getVersion() {
		return "1.0.2";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "1.11.02";
	}

	@Override
	public String getPamVerTestedOn() {
		return "1.15.05";
	}

	@Override
	public String getAboutText() {
		String desc = "Used to control offline data tasks";
		return desc;
	}

	/* (non-Javadoc)
	 * @see PamModel.PamPluginInterface#allowedModes()
	 */
	@Override
	public int allowedModes() {
		return PamPluginInterface.VIEWERONLY;
	}

}
