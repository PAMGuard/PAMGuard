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



package pamViewFX.fxStyles;

/**
 * @author mo55
 *
 */
public class PamStylesManagerFX {

	/**
	 * Singleton instance of the style manager
	 */
	private static PamStylesManagerFX singleInstance = null;
	
	/**
	 * The current style to use
	 */
	private PamDefaultStyle curStyle;
	
	/**
	 * private singleton constructor
	 */
	private PamStylesManagerFX() {
		this.setDefaultStyle();
	}

	/**
	 * Get the current style manager
	 * @return
	 */
	public static PamStylesManagerFX getPamStylesManagerFX() {
		if (singleInstance == null) {
			singleInstance = new PamStylesManagerFX();
		}
		return singleInstance;
	}
	
	/**
	 * Get the current style to use
	 * @return
	 */
	public PamDefaultStyle getCurStyle() {
		return curStyle;
	}
	
	/**
	 * Set the current style to use
	 * @param newStyle
	 */
	public void setCurStyle(PamDefaultStyle newStyle) {
		curStyle = newStyle;
	}
	
	/**
	 * Set the default style
	 */
	public void setDefaultStyle() {
		curStyle = new PamDefaultStyle();
	}



}
