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

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import PamModel.PamModel;

/**
 * @author dgillespie
 * 
 * Creates some standard Pam menus and appends the Display and Detection menus
 * with PamProcess specific menus from the model.
 * <p>
 * This is now pretty redundant - will remove after I've removed it from teh
 * ObjectList
 * 
 */
public class PamMenu {
	static public JMenuBar createBasicMenu(PamModel pamModelInterface,
			ActionListener actionListener) {

		JMenuBar menuBar = new JMenuBar();

		menuBar.add(fileMenu(pamModelInterface, actionListener));
		menuBar.add(detectionMenu(pamModelInterface, actionListener));
		menuBar.add(displayMenu(pamModelInterface, actionListener));
		menuBar.add(loggingMenu(pamModelInterface, actionListener));

		return menuBar;
	}

	static public JMenu fileMenu(PamModel pamModelInterface,
			ActionListener actionListener) {
		JMenuItem menuItem;
		JMenu menu = new JMenu("File");

		menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);

		return menu;
	}

	static public JMenu loggingMenu(PamModel pamModelInterface,
			ActionListener actionListener) {
		JMenuItem menuItem;
		JMenu menu = new JMenu("Logging");

		menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);

		return menu;
	}

	static public JMenu detectionMenu(PamModel pamModelInterface,
			ActionListener actionListener) {
		JMenu menu = new JMenu("Detection");
		JMenuItem menuItem;

		menuItem = new JMenuItem("Start PAM");
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);

		menuItem = new JMenuItem("Stop PAM");
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);

		/*
		 * now go through the model and see which PamProcesses have menu's
		 */
		// if (pamModelInterface != null){
		// ArrayList<PamProcess> pamProcesses =
		// pamModelInterface.GetModelProcessList();
		//			
		// if (pamProcesses.size() > 0) menu.addSeparator();
		//			
		// for (int i = 0; i < pamProcesses.size(); i++){
		// if (pamProcesses.get(i).GetDetectionMenu() != null){
		// // menu.
		// menu.add(pamProcesses.get(i).GetDetectionMenu());
		// }
		// }
		// }
		return menu;
	}

	static public JMenu displayMenu(PamModel pamModelInterface,
			ActionListener actionListener) {
		JMenu menu = new JMenu("Display");
		JMenuItem menuItem;

		menuItem = new JMenuItem("Map Options ...");
		menuItem.addActionListener(actionListener);
		menu.add(menuItem);

		/*
		 * now go through the model and see which PamProcesses have menu's
		 */
		// if (pamModelInterface != null){
		// ArrayList<PamProcess> pamProcesses =
		// pamModelInterface.GetModelProcessList();
		//			
		// if (pamProcesses.size() > 0) menu.addSeparator();
		//			
		// for (int i = 0; i < pamProcesses.size(); i++){
		// if (pamProcesses.get(i).GetDisplayMenu() != null){
		// menu.add(pamProcesses.get(i).GetDisplayMenu());
		// }
		// }
		// }
		return menu;
	}

}
