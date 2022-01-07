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

import Acquisition.AcquisitionControl;

/**
 * @author SCANS
 *
 */
public class STAcquisitionControl extends AcquisitionControl {

	public static final String STUNITTYPE = "SoundTrap Data Acquisition";
	private STAcquisitionProcess stAcquisitionProcess;

	/**
	 * @param name
	 */
	public STAcquisitionControl(String name) {
		super(name);
		
//		super(STUNITTYPE, name);
//
//		acquisitionControl = this;
//		
//		fileDate = new StandardFileDate(this);
//
//		pamController = PamController.getInstance();
//		
//		registerDaqSystem(new FileInputSystem(this));
//		registerDaqSystem(folderSystem = new FolderInputSystem(this));
//
//		daqChannelListManager = new DAQChannelListManager(this);
//
//		PamSettingManager.getInstance().registerSettings(this);
//
//		addPamProcess(acquisitionProcess = new STAcquisitionProcess(this));
//
//		daqMenuEnabler = new MenuItemEnabler();
//
//		statusBarComponent = getStatusBarComponent();
//
//		if (isViewer) {
//				offlineFileServer = new OfflineFileServer(this, fileDate);
//		}
//		else {
//			PamStatusBar statusBar = PamStatusBar.getStatusBar();
//
//			if (statusBar != null) {
//				statusBar.getToolBar().add(statusBarComponent);
//				statusBar.getToolBar().addSeparator();
//				setupStatusBar();
//			}
//		}
//		setSelectedSystem();
//		
//		TDDataProviderRegisterFX.getInstance().registerDataInfo(new RawSoundProviderFX(this));

		// add a second PAM process, for the click data
		addPamProcess(stAcquisitionProcess = new STAcquisitionProcess(this));

	}

	@Override
	/**
	 * Note that since this constructor calls the AcquisitionControl constructor directly,
	 * the actual unit type for this controlled unit is "Data Acquisition".
	 * Override here to return the Soundtrap unit type instead
	 */
	public String getUnitType() {
		return STUNITTYPE;
	}

	/**
	 * Returns the Soundtrap Acquisition Process, which handles the click data
	 * @return
	 */
	public STAcquisitionProcess getStAcquisitionProcess() {
		return stAcquisitionProcess;
	}

	

}
