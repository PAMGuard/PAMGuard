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



package radardisplay;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.paneloverlay.OverlayDataInfo;

/**
 * @author mo55
 *
 */
public class RadarDataInfo extends OverlayDataInfo implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	private boolean fadeDetector = false;
	
	private int detectorLifetime = 0;
	
	/**
	 * @param dataName
	 */
	public RadarDataInfo(String dataName) {
		super(dataName);
	}

	public boolean isFadeDetector() {
		return fadeDetector;
	}

	public void setFadeDetector(boolean fadeDetector) {
		this.fadeDetector = fadeDetector;
	}

	public int getDetectorLifetime() {
		return detectorLifetime;
	}

	public void setDetectorLifetime(int detectorLifetime) {
		this.detectorLifetime = detectorLifetime;
	}


	@Override
	public RadarDataInfo clone() {
		try{
		  return (RadarDataInfo) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
