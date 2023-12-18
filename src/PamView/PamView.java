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

import javax.swing.JFrame;

import PamController.PamControlledUnit;
import PamController.PamControllerInterface;
import PamModel.PamModelInterface;
import javafx.application.Platform;

/**
 * @author Doug Gillespie
 *         <p>
 *         Makes a simple display with a main GUI and a list of data objects.
 */
abstract public class PamView implements PamViewInterface {

	protected PamControllerInterface pamControllerInterface;

	protected PamModelInterface pamModelInterface;

	/** 
	 * Frame for main window associated with this view (i.e a PamGUI).
	 */
	protected JFrame frame;
	
	private int frameNumber;
		

	public PamView(PamControllerInterface pamControllerInterface,
			PamModelInterface pamModelInterface, int frameNumber) {
		this.pamControllerInterface = pamControllerInterface;
		this.pamModelInterface = pamModelInterface;
		this.frameNumber = frameNumber;
		
		// Start the JavaFX thread and tell it to stay open, even if the JavaFX window is closed.  If this parameter
		// is not set, the JavaFX thread will stop if the fx display is closed, and it won't restart again
		// even when another fx display is created - which causes Pamguard to lock up.  This problem and
		// solution are detailed here: https://stackoverflow.com/questions/25193198/prevent-javafx-thread-from-dying-with-jfxpanel-swing-interop
		// Note that for this to work, Platform.exit() needs to be called when Pamguard exits, to force the JavaFX thread to close.  That
		// was added into the shutDownPamguard() method below.
		FXInitialiser.initialise();
		Platform.setImplicitExit(false);
	}

	/**
	 * tells the view to show the main display panel of a pamControlledUnit
	 * @param pamControlledUnit
	 */
	abstract public void showControlledUnit(PamControlledUnit pamControlledUnit);
	
	abstract public void renameControlledUnit(PamControlledUnit pamControlledUnit);
	
	abstract public String getViewName();


	public int getFrameNumber() {
		return frameNumber;
	}

	@Override
	public JFrame getGuiFrame() {
		return frame;
	}

	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}
	

}
