package difar.beamforming;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import Acquisition.filedate.FileDate;
import Acquisition.filedate.StandardFileDate;
import Acquisition.offlineFuncs.OfflineWavFileServer;
import PamController.OfflineFileDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.fileprocessing.StoreStatus;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import difar.DifarParameters;
import difar.dialogs.DifarDisplayParamsDialog;
import pamScrollSystem.ViewLoadObserver;


/**
 * This module requires audio from a DIFAR sonobuoy and will output 
 * directional audio by using simple beamforming algorithms. This module 
 * makes use of the AMMC DIFAR demodulator, and beamforming follows equation 1 
 * from Thode et al (2016). This module will eventually be able to use a GPS 
 * and/or AIS data streams to set the direction of noise cancellation also
 * described by Thode et al (2016). 
 * 
 * References
 * Thode et al 2016. Acoustic vector sensor beamforming reduces masking from 
 * 		underwater industrial noise during passive monitoring. JASA-EL 139(4)
 * 		EL105-EL111. DOI: http://dx.doi.org/10.1121/1.4946011
 * @author brian_mil
 *
 */
public class BeamformControl extends PamControlledUnit implements PamSettings, OfflineFileDataStore {

	private BeamformProcess beamformProcess;
	
	private BeamformParameters beamformParameters = new BeamformParameters();
	
	private BeamformSidePanel beamformSidePanel;
	
	private OfflineFileServer offlineFileServer;
	
	private FileDate fileDate;

	public BeamformControl(String unitName) {
		
		super("DIFAR Directional Audio", unitName);

		PamSettingManager.getInstance().registerSettings(this);		
		
		addPamProcess(setBeamformProcess(new BeamformProcess(this)));
		
		beamformSidePanel = new BeamformSidePanel(this);
		setSidePanel(beamformSidePanel);
		
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		
		if (isViewer) {
			fileDate = new StandardFileDate(this);
			offlineFileServer = new OfflineWavFileServer(this, fileDate);
		}
}

	@Override
	public Serializable getSettingsReference() {
		return beamformParameters;
	}

	@Override
	public long getSettingsVersion() {
		return DifarParameters.serialVersionUID;
	}

	@Override
	public String getDataLocation() {
		if (offlineFileServer != null) {
			return offlineFileServer.getDataLocation();
		}
		else {
			return null;
		}
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		beamformParameters = ((BeamformParameters) pamControlledUnitSettings.getSettings()).clone();
		return (beamformParameters != null);
	}
	
	public BeamformParameters getBeamformParameters(){
		return beamformParameters;
	}

	public BeamformProcess getBeamformProcess() {
		return beamformProcess;
	}

	public BeamformProcess setBeamformProcess(BeamformProcess difarBeamformProcess) {
		this.beamformProcess = difarBeamformProcess;
		return difarBeamformProcess;
	}
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDisplayMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " options...");
		menuItem.addActionListener(new DisplayMenu(parentFrame));
		return menuItem;
	}
	
	class DisplayMenu implements ActionListener {

		private Frame parentFrame;

		public DisplayMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			displayMenu(parentFrame);
		}
	}
	
	public boolean displayMenu(Frame parentFrame) {
		if (parentFrame == null) {
			parentFrame = this.getGuiFrame();
		}
		BeamformParameters newParams = BeamformParamsDialog.showDialog(parentFrame, this, beamformParameters);
		if (newParams != null) {
			beamformParameters = newParams.clone();
			beamformProcess.setupProcess();
			return true;
		}
		else {
			return false;
		}

	}

	public void updateSidePanel() {
		BeamformSidePanel sidePanel = (BeamformSidePanel) getSidePanel();
		sidePanel.updateAngles();
	}
	
	@Override
	public void createOfflineDataMap(Window parentFrame) {
		offlineFileServer.createOfflineDataMap(parentFrame);
	}
	@Override
	public String getDataSourceName() {
		return offlineFileServer.getDataSourceName();
	}
	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		return offlineFileServer.loadData(dataBlock, offlineDataLoadInfo, loadObserver);
	}
	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		return offlineFileServer.saveData(dataBlock);
	}

	@Override
	public OfflineFileServer getOfflineFileServer() {
		return offlineFileServer;
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint dmp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatagramManager getDatagramManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamRawDataBlock getRawDataBlock() {
		// TODO Auto-generated method stub
		return beamformProcess.getOutputDataBlock();
	}

	@Override
	public PamProcess getParentProcess() {
		// TODO Auto-generated method stub
		return beamformProcess;
	}

}
	