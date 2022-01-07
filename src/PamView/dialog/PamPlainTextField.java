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



package PamView.dialog;

import java.awt.Dimension;

import javax.swing.JTextField;

/**
 * This is a class created to deal with the problem of text fields having a zero length.  Originally seen in the
 * LatLongDialogStrip displayed in the SimulatedObjectDialog, but may be present in other places.<br>
 * 
 * Seems to be an issue with Java12+ JTextField not having a reasonable minimum size when using a GridBagLayout.  Note this isn't a problem
 * with Java 8.
 * 
 * @author mo55
 *
 */
public class PamPlainTextField  extends JTextField  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param i
	 */
	public PamPlainTextField(int i) {
		super(i);
	}

	@Override
	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}
	
	

}
