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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.pamguard.x3.sud.SUDClickDetectorInfo;

import Acquisition.AcquisitionControl;
import PamController.PamSensor;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamRawDataBlock;
import clickDetector.ClickBTDisplay;
import clickDetector.ClickControl;
import clickDetector.ClickDisplay;
import clickDetector.ClickDisplayManager;
import clickDetector.ClickParameters;
import soundtrap.sud.SUDParamsDialog;
import soundtrap.sud.SudFileDWVHandler;

/**
 * @author mo55
 *
 */
public class STClickControl extends ClickControl implements PamSensor {
	
	private SUDClickDetectorInfo sudClickDetectorInfo;
	
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
		rawSource = new STAcquisitionControl("Private Sound Acq for Soundtrap Click Detector");
		
		sudFileDWVHandler = new SudFileDWVHandler(this);
		sudFileDWVHandler.subscribeSUD();
		
		PamSettingManager.getInstance().registerSettings(new SUDSettings());
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
		
		JMenuItem sudItem = new JMenuItem("Sound Trap settings ...");
		sudItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSudParameters(parentFrame);
			}
		});
		newMenu.add(sudItem, 0);
		newMenu.add(new JSeparator(), 1);
		
		return newMenu;
	}

	protected void showSudParameters(Frame parentFrame) {
		SUDParamsDialog.showDialog(parentFrame, this);
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
	
	/**
	 * Called when running SUD file data to find and scroll the BT Display's
	 * @param timeMillis
	 */
	public void updateDisplayScrollers(long timeMillis) {
		ClickDisplayManager dispManager = getDisplayManager();
		if (dispManager == null) {
			// happens in -nogui operation. 
			return;
		}
		ArrayList<ClickDisplay> dispList = dispManager.getWindowList();
		for (ClickDisplay display : dispList) {
			if (display instanceof ClickBTDisplay) {
				((ClickBTDisplay) display).newScrollTimingData(timeMillis);
			}
		}
	}

	// this was a bad idea since we need to keep hold of settings for the 
	// classifier, which are in with the main set ...
//	@Override
//	public long getSettingsVersion() {
//		return SUDClickDetectorInfo.serialVersionUID;
//	}
//
//	@Override
//	public Serializable getSettingsReference() {
//		return getSudClickDetectorInfo();
//	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		// have to leave this in since I've now run data with it using the ST settings, 
		// so some configs may return these !
		Object o = pamControlledUnitSettings.getSettings();
		if (o instanceof SUDClickDetectorInfo) {
			sudClickDetectorInfo = (SUDClickDetectorInfo) o;
			return true;
		}
		if (o instanceof ClickParameters) {
			return super.restoreSettings(pamControlledUnitSettings);
		}
		return false;
	}

	/**
	 * @return the sudClickDetectorInfo
	 */
	public SUDClickDetectorInfo getSudClickDetectorInfo() {
		if (sudClickDetectorInfo == null) {
			sudClickDetectorInfo  = new SUDClickDetectorInfo();
		}
		return sudClickDetectorInfo;
	}

	/**
	 * @param sudClickDetectorInfo the sudClickDetectorInfo to set
	 */
	public void setSudClickDetectorInfo(SUDClickDetectorInfo sudClickDetectorInfo) {
		this.sudClickDetectorInfo = sudClickDetectorInfo;
	}

	@Override
	public String getSensorDescription() {
		String desc = String.format("SoundTrap Click Detector at %dHz", (int) getClickDataBlock().getSampleRate());
		return desc;
	}

	@Override
	public String getSensorId() {
		return null;
	}
	
	/**
	 * Class to handle SoundTrap click detector settings without messing up
	 * the standard click detector ones which are needed for the classifier. 
	 * @author dg50
	 *
	 */
	private class SUDSettings implements PamSettings {

		@Override
		public String getUnitName() {
			return STClickControl.this.getUnitName();
		}

		@Override
		public String getUnitType() {
			return STUNITTYPE;
		}

		@Override
		public Serializable getSettingsReference() {
			return sudClickDetectorInfo;
		}

		@Override
		public long getSettingsVersion() {
			return SUDClickDetectorInfo.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			Object o = pamControlledUnitSettings.getSettings();
			if (o instanceof SUDClickDetectorInfo) {
				sudClickDetectorInfo = (SUDClickDetectorInfo) o;
				return true;
			}
			return false;
		}
		
	}

}
