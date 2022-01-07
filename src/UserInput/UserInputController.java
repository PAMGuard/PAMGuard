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
package UserInput;

import java.awt.Frame;

import javax.swing.JMenuBar;

import PamController.PamControlledUnit;
import PamController.PamControllerInterface;
import PamView.PamGui;
import PamguardMVC.PamDataBlock;

public class UserInputController extends PamControlledUnit {
	
	UserInputProcess userInputProcess;
	
	public static final int maxCommentLength = 2550000;
	UserInputSidePanel userInputSidePanel;
	UserInputPanel userInputPanel;

	public UserInputController(String name) {
		super("User Input Controller", name);
		addPamProcess(userInputProcess = new UserInputProcess(this,
				(PamDataBlock) null));

		setTabPanel(userInputPanel = new UserInputPanel(this));
		setSidePanel(userInputSidePanel = new UserInputSidePanel(this));
	}

	public UserInputProcess getUserInputProcess() {
		return userInputProcess;
	}
	JMenuBar userInputTabMenu = null;
	@Override
	public JMenuBar getTabSpecificMenuBar(Frame parentFrame, JMenuBar standardMenu, PamGui pamGui) {

		// start bymaking a completely new copy.
		if (userInputTabMenu == null) {
			userInputTabMenu = pamGui.makeGuiMenu();
			for (int i = 0; i < userInputTabMenu.getMenuCount(); i++) {
				if (userInputTabMenu.getMenu(i).getText().equals("Display")) {
					
					//userInputTabMenu.remove(userInputTabMenu.getMenu(i));
					
					
					break;
				}
			}
		}
		return userInputTabMenu;
	}

	public UserInputPanel getUserInputPanel() {
		return userInputPanel;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			userInputPanel.refillHistory();
		}
	}
	
}
