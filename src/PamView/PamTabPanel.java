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
package PamView;

import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

/**
 * @author Doug Gillespie
 * 
 * Interface used to build coherent units for the main GUI display which contain
 * a JPanel to add to the main Tab control and potentially a menu to go with
 * that tab.
 */
public interface PamTabPanel {

	/**
	 * @return a JMeny for the panel
	 */
	JMenu createMenu(Frame parentFrame);

	/**
	 * @return Reference to a graphics component that can be added to the view.
	 *         This will typically be a JPanel or a JInternalFrame;
	 */
	JComponent getPanel();
	
	/**
	 * 
	 * @return a JToolbas associated with this tab. This will only be displayed when 
	 * the current tab is activated
	 */
	JToolBar getToolBar();

}
