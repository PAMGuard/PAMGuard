package cpod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import fileOfflineData.OfflineFileMapPoint;
import pamScrollSystem.ViewLoadObserver;

/**
 * Functions to handle loading of CPOD data. 
 * 
 * @author Doug Gillespie
 *
 */
@Deprecated
public class CPODLoader {

	private CPODControl cpodControl;

	public CPODLoader(CPODControl cpodControl) {
		this.cpodControl = cpodControl;
	}
	
	/**
	 * CPOD clicks
	 */
	private CPODClick prevClick;

	public int loadData(PamDataBlock dataBlock,
			ArrayList<OfflineFileMapPoint> usedMapPoints, 
			OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		
		if (usedMapPoints == null || usedMapPoints.size() == 0) {
			return 0;
		}
		File currentFile = null;
		int nLoaded = 0;
		prevClick = null;
		for (int i = 0; i < usedMapPoints.size(); i++) {
			CPODDataMapPoint mapPoint = (CPODDataMapPoint) usedMapPoints.get(i);
			if (mapPoint.getDataFile() != currentFile) {
				nLoaded += loadData(dataBlock, mapPoint, offlineDataLoadInfo,
						loadObserver);
				currentFile = mapPoint.getDataFile();
			}
		}
		
		return nLoaded;
	}

	private int loadData(PamDataBlock dataBlock, CPODDataMapPoint mapPoint,
			OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		int nClicks = 0;
		// the pamPoint should have a pointer close to the data start, so 
		// can skip straight there. 
		FileInputStream fileInputStream = null;
		BufferedInputStream bis = null;
		long totalBytes = 0;
		CPODClickDataBlock clickDataBlock = (CPODClickDataBlock) dataBlock;
		
		
		
		File dataFile = mapPoint.getDataFile();
		if (clickDataBlock.clikcType == CPODMap.FILE_CP3) {
			dataFile = CPODControl.getCP3File(dataFile);
		}
		if (!dataFile.exists()) {
			return -1;
		}
		try {
			 bis = new BufferedInputStream(fileInputStream = new FileInputStream(dataFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		int dataSize = CPODMap.getDataSize(clickDataBlock.clikcType);
		int bytesRead;
		byte[] byteData = new byte[dataSize];
		short[] shortData = new short[dataSize];
		int fileEnds = 0;
		boolean isClick;
		/*
		 * Mappoint will always point to the start of a minute marker, so 
		 * as that mark is read nMinutes will increment by 1 - so start at -1 
		 */
		int nMinutes = -1;
		try {
			fileInputStream.skip(mapPoint.getFilePos());
			while (true) {
				bytesRead = bis.read(byteData);
				for (int i = 0; i < bytesRead; i++) {
					shortData[i] = CPODMap.toUnsigned(byteData[i]);
				}
				if (CPODMap.isFileEnd(byteData)) {
					fileEnds++;
				}
				else {
					fileEnds = 0;
				}
				if (fileEnds == 2) {
					break;
				}
				isClick = byteData[dataSize-1] != -2;
				if (isClick) {
					nClicks++;
					long minuteMillis = mapPoint.getStartTime() + nMinutes * 60000L;
					CPODClick cpodClick = CPODClick.makeClick(cpodControl, minuteMillis, shortData);
					if (cpodClick.getTimeMilliseconds() > offlineDataLoadInfo.getEndMillis()) {
						break;
					}
					if (prevClick != null) {
						cpodClick.setICISamples(cpodClick.getStartSample() - prevClick.getStartSample());
					}
					prevClick = cpodClick;
					if (cpodClick.getTimeMilliseconds() > offlineDataLoadInfo.getStartMillis()) {
						dataBlock.addPamData(cpodClick);
					}
				}
				else {
					nMinutes ++;
				}
				totalBytes += dataSize;
			}
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return nClicks;
	}

}
