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
package decimator;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import pamScrollSystem.ViewLoadObserver;
import Acquisition.filedate.FileDate;
import Acquisition.filedate.StandardFileDate;
import Acquisition.offlineFuncs.OfflineWavFileServer;
import PamController.OfflineFileDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * @author Doug Gillespie
 * 
 * Quite a simple control unit that filters and decimates raw data producing a
 * new data stream
 * <p>
 * Needs a control dialog
 * 
 */
public class DecimatorControl extends PamControlledUnit implements PamSettings, OfflineFileDataStore {

	DecimatorParams decimatorParams = new DecimatorParams();
	
	DecimatorProcessW decimatorProcess;
	
	private OfflineFileServer offlineFileServer;
	
	private FileDate fileDate;
	
	public DecimatorControl(String name) {
		
		super("Decimator", name);
		
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		
//		
//
//		PamRawDataBlock rawDataBlock = null;
//		if (inputControl.GetPamProcess(0).GetOutputDataBlock(0).GetDataType() == DataType.RAW) {
//			rawDataBlock = (PamRawDataBlock) inputControl.GetPamProcess(0).GetOutputDataBlock(0);
//		}
		
		addPamProcess(decimatorProcess = new DecimatorProcessW(this));

		PamSettingManager.getInstance().registerSettings(this);
		

		if (isViewer) {
			fileDate = new StandardFileDate(this);
			offlineFileServer = new OfflineWavFileServer(this, fileDate);
		}
		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#CreateDetectionMenu(boolean)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + "...");
		menuItem.addActionListener(new DetectionMenu(parentFrame));
		return menuItem;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	class DetectionMenu implements ActionListener {
		Frame parentFrame;
		
		public DetectionMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		
		public void actionPerformed(ActionEvent e) {
			DecimatorParams newParams = DecimatorParamsDialog.showDialog(parentFrame, DecimatorControl.this, decimatorParams);
			if (newParams != null) {
				decimatorParams = newParams.clone();
				decimatorProcess.newSettings();
				if (isViewer) {
					offlineFileServer.createOfflineDataMap(parentFrame);
				}
			}
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch(changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			if (isViewer) {
				offlineFileServer.createOfflineDataMap(PamController.getMainFrame());
			}
			decimatorProcess.newSettings();
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#GetSettingsReference()
	 */
	public Serializable getSettingsReference() {
		return decimatorParams;
	}


	/* (non-Javadoc)
	 * @see PamController.PamSettings#GetSettingsVersion()
	 */
	public long getSettingsVersion() {
		return DecimatorParams.serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#RestoreSettings(PamController.PamControlledUnitSettings)
	 */
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		decimatorParams = ((DecimatorParams) pamControlledUnitSettings.getSettings()).clone();
				
		return true;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	@Override
	public void setupControlledUnit() {
		// if there is no data block in the system, setup on the first one available. 
		PamDataBlock rawBlock = PamController.getInstance().getRawDataBlock(decimatorParams.rawDataSource);
		if (rawBlock == null) {
			rawBlock = PamController.getInstance().getRawDataBlock(0);
			if (rawBlock != null) decimatorParams.rawDataSource = rawBlock.toString();
		}
		super.setupControlledUnit();
		if (decimatorProcess != null) decimatorProcess.newSettings();
	}

	@Override
	public void createOfflineDataMap(Window parentFrame) {
		if (offlineFileServer == null) {
			return;
		}
		offlineFileServer.createOfflineDataMap(parentFrame);
	}
	@Override
	public String getDataSourceName() {
		if (offlineFileServer == null) {
			return getUnitName();
		}
		return offlineFileServer.getDataSourceName();
	}
	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		if (offlineFileServer == null) {
			return false;
		}
		return offlineFileServer.loadData(dataBlock, offlineDataLoadInfo, loadObserver);
	}
	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		if (offlineFileServer == null) {
			return false;
		}
		return offlineFileServer.saveData(dataBlock);
	}

	@Override
	public OfflineFileServer getOfflineFileServer() {
		return offlineFileServer;
	}

	@Override
	public PamProcess getParentProcess() {
		return decimatorProcess;
	}

	@Override
	public PamRawDataBlock getRawDataBlock() {
		return decimatorProcess.getOutputDataBlock();
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint dmp) {
		return false;
	}

	@Override
	public DatagramManager getDatagramManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public DecimatorParams getDecimatorParams() {
		return decimatorParams;
	}
	
	/**
	 * See if the decimator is integer, whether going up or down in 
	 * frequency 
	 * @return
	 */
	public boolean isIntegerDecimation() {
		return isIntegerDecimation(decimatorParams.newSampleRate);
	}

	public boolean isIntegerDecimation(float newSampleRate) {
		PamDataBlock<PamDataUnit> source = decimatorProcess.getSourceDataBlock();
		if (source == null) {
			return false;
		}
		float sourceFS = source.getSampleRate();
		return newSampleRate % sourceFS == 0.;
	}

	public boolean isIntegerDecimation(float sourceFS, float newSampleRate) {
		double fbig = Math.max(sourceFS, newSampleRate);
		double fsmall = Math.min(sourceFS, newSampleRate);
		double m = fbig % fsmall;
		return m == 0;
	}
	
	
}
