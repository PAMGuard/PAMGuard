package Acquisition.rona;

import java.io.File;
import java.util.List;

import pamScrollSystem.ViewLoadObserver;
import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import Acquisition.RonaInputSystem;
import Acquisition.filedate.FileDate;
import Acquisition.offlineFuncs.OfflineWavFileServer;
import Acquisition.offlineFuncs.WavFileDataMap;
import PamController.OfflineFileDataStore;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.filemaps.FileDataMapPoint;
import dataMap.filemaps.OfflineFileServer;

public class RonaOfflineFileServer extends OfflineWavFileServer {

	private AcquisitionControl acquisitionControl;

	public RonaOfflineFileServer(AcquisitionControl acquisitionControl, FileDate fileDate) {
		super(acquisitionControl, fileDate);
		this.acquisitionControl = acquisitionControl;
	}

	/* (non-Javadoc)
	 * @see Acquisition.offlineFuncs.OfflineFileServer#addToMap(java.io.File)
	 */
	@Override
	protected void addToMap(File file) {
		// check a) that it's file 1 and b) that it's 
		// got all the other files present. 

		// now check that the last character before the final . is a 1 !
		String name = file.getName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot < 0) return;
		char ch = name.charAt(lastDot-1); 
		if ('1' != ch) {
			return;
		}
		
		// check the other files in the set exist. 
		RonaInputSystem ronaSystem = findRonaInputSystem();
		if (ronaSystem != null) {
			int nChan = ronaSystem.getChannels();
			for (int i = 1; i < nChan; i++) {
				File chanFile = ronaSystem.findChannelFile(file, i, 2);
				if (chanFile == null) {
					return;
				}
			}
		}
		
		super.addToMap(file);
	}
	
	RonaInputSystem findRonaInputSystem() {
		DaqSystem daqSystem = acquisitionControl.findDaqSystem(null);
		if (daqSystem != null && RonaInputSystem.class.isAssignableFrom(daqSystem.getClass())) {
			return (RonaInputSystem) daqSystem;
		}
		else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see Acquisition.offlineFuncs.OfflineFileServer#loadData(PamguardMVC.PamDataBlock, long, long, PamguardMVC.RequestCancellationObject, pamScrollSystem.ViewLoadObserver)
	 */
	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineLoadDataInfo,
			ViewLoadObserver loadObserver) {
		/*
		 *  need to load up data simultaneously from multiple flac files.
		 *  Should probably start up multiple threads as for acquisition.
		 *  Will need to give each thread a list of files it needs to read.  
		 */
		RonaInputSystem ronaSystem = findRonaInputSystem();
		if (ronaSystem == null) {
			return false;
		}
		WavFileDataMap dataMap = (WavFileDataMap) getDataMap();
		List<FileDataMapPoint> mapPoints = dataMap.getMapPoints(offlineLoadDataInfo.getStartMillis(), offlineLoadDataInfo.getEndMillis());
		if (mapPoints == null || mapPoints.size() == 0) {
			return false; // no data to load. 
		}
		RonaLoader ronaLoader = new RonaLoader(ronaSystem, mapPoints, (PamRawDataBlock) getRawDataBlock(), offlineLoadDataInfo);
		
		return ronaLoader.loadData(offlineLoadDataInfo.getStartMillis(), offlineLoadDataInfo.getEndMillis());
		
	}

}
