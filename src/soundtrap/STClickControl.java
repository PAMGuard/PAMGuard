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



package soundtrap;

import java.awt.Frame;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import Acquisition.AcquisitionControl;
import Acquisition.sud.SUDNotificationManager;
import PamController.PamController;
import PamguardMVC.PamRawDataBlock;
import clickDetector.ClickControl;
import soundtrap.sud.SudFileDWVHandler;

/**
 * @author mo55
 *
 */
public class STClickControl extends ClickControl {
	
	public static final String STUNITTYPE = "SoundTrap Click Detector";

	/**
	 * The private sound acquisition module linked to the Soundtrap click data
	 */
	private AcquisitionControl rawSource;
	
	private SudFileDWVHandler sudFileDWVHandler;


	/**
	 * @param name
	 */
	public STClickControl(String name) {
		super(name);
		
		// create a private acquisition control that only this module can see
		rawSource = new AcquisitionControl("Private Sound Acq for Soundtrap Click Detector");
		
		sudFileDWVHandler = new SudFileDWVHandler(this);
		sudFileDWVHandler.subscribeSUD();
	}

	@Override
	public PamRawDataBlock findRawDataBlock() {
		return rawSource.getAcquisitionProcess().getRawDataBlock();
	}
	
	@Override
	public String getUnitType() {
		return STUNITTYPE;
	}
	
	public AcquisitionControl getSTAcquisition() {
		return rawSource;
	}

	@Override
	public JMenu createDetectionMenu(Frame parentFrame) {
		JMenu newMenu = super.createDetectionMenu(parentFrame);
		
		// now get rid of the menu items not applicable to SoundTrap data.
		// Start from the end, so that as we remove them we don't mess up
		// the index positions of the remaining items
		int numItems = newMenu.getItemCount();
		for (int i=numItems-1; i>=0; i--) {
			JMenuItem thisItem = newMenu.getItem(i);
			if (thisItem==null) continue;
			
			String thisName = thisItem.getText();
			
			if (thisName == "Detection Parameters ...") {
				newMenu.remove(i);
			}
			else if (thisName == "Digital pre filter ...") {
				newMenu.remove(i);
			}
			else if (thisName == "Digital trigger filter ...") {
				newMenu.remove(i);
			}
			else if (thisName == "Angle Vetoes ...") {
				newMenu.remove(i);
			}
			else if (thisName == "Audible Alarm ...") {
				newMenu.remove(i);
			}
			else if (thisName == "Import RainbowClick Data ...") {
				newMenu.remove(i);
			}
		}
		
		return newMenu;
	}

	@Override
	public void pamStart() {
		sudFileDWVHandler.pamStart();
		super.pamStart();
	}

	@Override
	public void pamStop() {
		sudFileDWVHandler.pamStop();
		super.pamStop();
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			sudFileDWVHandler.subscribeSUD();
		}
	}
	

}
