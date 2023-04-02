package binaryFileStorage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.comparator.NameFileComparator;

import PamController.fileprocessing.StoreStatus;
import PamUtils.PamFileFilter;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;

/**
 * Set of functions used at restarts to determine the status of the binary store. 
 * @author dg50
 *
 */
public class BinaryStoreStatusFuncs {

	private BinaryStore binaryStore;

	public BinaryStoreStatusFuncs(BinaryStore binaryStore) {
		this.binaryStore = binaryStore;
	}

	public StoreStatus getStoreStatus(boolean getDetail) {
		BinaryStoreStatus binStoreStatus = new BinaryStoreStatus(binaryStore);
		binStoreStatus.setStoreStatus(checkStoreStatus());
		if (getDetail && binStoreStatus.getStoreStatus() == StoreStatus.STATUS_HASDATA) {
			binStoreStatus = getStoreDetail(binStoreStatus);
		}
		return binStoreStatus;
	}

	private BinaryStoreStatus getStoreDetail(BinaryStoreStatus binStoreStatus) {
		// go through every stream and find it's first and last data times.
		long lastTime = Long.MIN_VALUE;
		long firstTime = Long.MAX_VALUE;
		ArrayList<PamDataBlock> streams = BinaryStore.getStreamingDataBlocks(true);
		for (PamDataBlock aBlock : streams) {
			BinaryDataSource dataSource = aBlock.getBinaryDataSource();
			if (dataSource == null) {
				continue;
			}
			BinaryStoreStatus blockStatus = getStreamStartEnd(dataSource);
			binStoreStatus.considerBlockStatus(blockStatus);
		}

		return binStoreStatus;
	}


	private BinaryStoreStatus getStreamStartEnd(BinaryDataSource dataSource) {
		String filePrefix = dataSource.createFilenamePrefix();
		List<File> binFiles = binaryStore.listAllFilesWithPrefix(filePrefix);
		if (binFiles == null || binFiles.isEmpty()) {
			return null;
		}
		Collections.sort(binFiles, NameFileComparator.NAME_COMPARATOR);
		BinaryHeader firstHead = findFirstHeader(binFiles);
		BinaryFooter lastFoot = findLastFooter(binFiles);
		BinaryFooter lastData = findLastData(binFiles);
		BinaryStoreStatus storeStatus = new BinaryStoreStatus(binaryStore, firstHead, lastFoot, lastData);
		return storeStatus;
	}

	/**
	 * Get the last footer. This may be in the last file, but may not be if things 
	 * crashed and the last file didn't get completed, i nwhich case it will be in 
	 * the file before. 
	 * @param binFiles
	 * @return
	 */
	private BinaryFooter findLastFooter(List<File> binFiles) {
		for (int i = binFiles.size()-1; i>=0; i--) {
			File aFile = binFiles.get(i);
			/*
			 *  if the last file was completed correctly, it will have an index file. If there isn't 
			 *  an index file it's very unlikely there will be a footer in the main file 
			 */
			File indexFile = binaryStore.findIndexFile(aFile, true);
			if (indexFile == null) {
				continue;
			}
			BinaryHeaderAndFooter headAndFoot = binaryStore.readHeaderAndFooter(indexFile);
			if (headAndFoot != null && headAndFoot.binaryFooter != null) {
				return headAndFoot.binaryFooter;
			}
		}
		return null;
	}

	/**
	 * Get the last time of any data, whether it's from a header, footer, or actual data. 
	 * @param binFiles
	 * @return
	 */
	private BinaryFooter findLastData(List<File> binFiles) {
		for (int i = binFiles.size()-1; i>=0; i--) {
			File aFile = binFiles.get(i);
			BinaryFooter bf = findLastData(aFile);
			if (bf != null) {
				return bf;
			}
		}
		return null;		
	}

	/**
	 * Get the last data in a file. Hopefully this comes 
	 * from the footer, but it might have to look at all data if
	 * the footer is absent or the index file missing. 
	 * @param aFile
	 * @return
	 */
	public BinaryFooter findLastData(File aFile) {
		Long lastUID = null;
		Long lastTime = null;
		Long firstUID = null;

		File indexFile = binaryStore.findIndexFile(aFile, true);
		if (indexFile != null) {
			BinaryHeaderAndFooter headAndFoot = binaryStore.readHeaderAndFooter(indexFile);
			if (headAndFoot != null && headAndFoot.binaryFooter != null) {
				return headAndFoot.binaryFooter;
			}
		}
		/*
		 *  otherwise it would seem that we've a file without a valid end, so unpack it and
		 *  get the UID and time of the last item in the file. Can return these in the form of
		 *  a BinaryFooter since it's pretty much the same information needed.  
		 */
		BinaryInputStream inputStream = new BinaryInputStream(binaryStore, null);
		try {
			// need to work through the file now. 
			if (inputStream.openFile(aFile) == false) {
				return null;
			};
			BinaryObjectData binaryObjectData;
			BinaryHeader bh = inputStream.readHeader();
			if (bh==null) {
				inputStream.closeFile();
				return null; 
			}
			int inputFormat = bh.getHeaderFormat();
			while ((binaryObjectData = inputStream.readNextObject(inputFormat)) != null) {
				if (binaryObjectData.getTimeMilliseconds() != 0) {
					lastTime = binaryObjectData.getTimeMilliseconds();
				}
				BinaryFooter bf;
				switch (binaryObjectData.getObjectType()) {
				case BinaryTypes.FILE_FOOTER:
					// this is unlikely to happen, since we'll probably already have found an index file. 
					bf = new BinaryFooter();
					if (bf.readFooterData(binaryObjectData.getDataInputStream(), inputFormat)) {
						if (bf.getDataDate() != 0) {
							inputStream.closeFile();
							return bf;
						}
					}
					break;
				case  BinaryTypes.MODULE_HEADER:
					break;
				case  BinaryTypes.MODULE_FOOTER:
					break;
				case BinaryTypes.DATAGRAM:
					break;
				default: // should be data. 
					DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
					if (baseData != null) {
						if (baseData.getTimeMilliseconds() != 0) {
							lastTime = baseData.getTimeMilliseconds();
						}
						if (baseData.getUID() != 0) {
							lastUID = baseData.getUID();
							if (firstUID == null) {
								firstUID = lastUID;
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			System.out.printf("Corrupt data file %s: %s\n", aFile, e.getMessage());
//			return null;
		}
		try {
			if (inputStream != null) {
				inputStream.closeFile();
			}
		}
		catch (Exception e) {
			
		}
		if (lastTime != null && lastUID != null) {
			BinaryFooter bf = new BinaryFooter();
			bf.setHighestUID(lastUID);
			bf.setLowestUID(firstUID);
			bf.setDataDate(lastTime);
			bf.setFileEndReason(BinaryFooter.END_CRASHED);
			return bf;
		}
		else {
			return null;
		}
	}

	/**
	 * Get the first header. This can be read from a data file whether or not there was a 
	 * valid index file created. 
	 * @param binFiles
	 * @return
	 */
	private BinaryHeader findFirstHeader(List<File> binFiles) {
		BinaryHeader binaryHead = new BinaryHeader();
		DataInputStream dis = null;
		for (File aFile : binFiles) {
			try {
				dis = new DataInputStream(new BufferedInputStream(new FileInputStream(aFile)));
			}
			catch (IOException e) {
				binaryHead = null;
				continue;
			}
			try {
				dis.close();
			}
			catch (IOException e) {
				
			}
		}
		return binaryHead;
	}

	/**
	 *  first simple status check to see if there are any files there at all. 
	 */
	private int checkStoreStatus() {
		String currDir = binaryStore.binaryStoreSettings.getStoreLocation();
		if (currDir == null) {
			return StoreStatus.STATUS_MISSING;
		}
		File currfolder = new File(currDir);
		if (currfolder.exists() == false) {
			return StoreStatus.STATUS_MISSING;
		}
		// look for files in the folder. 
		boolean hasFiles = hasAnyFiles(currfolder);
		if (hasFiles) {
			return StoreStatus.STATUS_HASDATA;
		}
		else {
			return StoreStatus.STATUS_EMPTY;
		}

	}

	private boolean hasAnyFiles(File currFolder) {
		PamFileFilter filefilter = new PamFileFilter("data files", ".pgdf");
		File[] list = currFolder.listFiles(filefilter);
		if (list == null) {
			return false;
		}
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory()) {
				if (hasAnyFiles(list[i])) {
					return true;
				}
			}
			if (list[i].getAbsolutePath().endsWith(".pgdf")) {
				return true;
			}
		}
		return false;
	}

}
