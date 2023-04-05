package binaryFileStorage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.comparator.NameFileComparator;

import PamUtils.PamCalendar;
import PamUtils.PamFileFilter;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class BinaryStoreDeleter {

	private static final int FILE_DELETE_ERROR = 1;
	private static final int FILE_TOO_EARLY = 2;
	private static final int FILE_DELETED = 3;
	private static final int FILE_PARTIAL_DELETE = 4;
	

	private BinaryStore binaryStore;
	
	private FileFilter directoryFilter;
	
	private BinaryStoreStatusFuncs binaryStoreStatusFuncs;

	public BinaryStoreDeleter(BinaryStore binaryStore) {
		this.binaryStore = binaryStore;
		directoryFilter = new DirectoryFilter();
		binaryStoreStatusFuncs = new BinaryStoreStatusFuncs(binaryStore);
	}

	public boolean deleteDataFrom(long timeMillis) {
		if (timeMillis == 0) {
			return deleteEverything();
		}
		else {
			return deleteFrom(timeMillis);
		}
	}
	
	private class DirectoryFilter implements java.io.FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
		
	}

	private boolean deleteEverything() {
		ArrayList<File> fileList = new ArrayList<File>();
		String root = binaryStore.binaryStoreSettings.getStoreLocation();
		if (root == null) {
			return false;
		}
		File rootFolder = new File(root);
		PamFileFilter binaryDataFilter = new PamFileFilter("Binary Data Files", BinaryStore.fileType);
		binaryDataFilter.addFileType(BinaryStore.indexFileType);
		binaryDataFilter.addFileType(BinaryStore.noiseFileType);
		binaryDataFilter.setAcceptFolders(true);

		binaryStore.listDataFiles(fileList, rootFolder, binaryDataFilter);
		int errors = 0;
		for (File aFile : fileList) {
			try {
				aFile.delete();
			}
			catch (Exception e) {
				errors++;
			}
		}
		deleteEmptyFolders();
		return errors == 0;
	}

	private boolean deleteFrom(long timeMillis) {
		/*
		 *  need to go through the data one stream at a time so that 
		 *  we can pick files off from the end of the list. 
		 */
		ArrayList<PamDataBlock> streams = BinaryStore.getStreamingDataBlocks(true);
		int errors = 0;
		for (PamDataBlock aBlock : streams) {
			boolean ok = deleteFrom(aBlock, timeMillis);
			if (!ok) {
				errors++;
			}
		}
		
		deleteEmptyFolders();
		return false;
	}
	
	private boolean deleteFrom(PamDataBlock aBlock, long timeMillis) {
		System.out.printf("Deleting binary data for %s from %s\n", aBlock.getDataName(), PamCalendar.formatDBDateTime(timeMillis));
		BinaryDataSource dataSource = aBlock.getBinaryDataSource();
		if (dataSource == null) {
			return true; //  don't see how this can happen. 
		}
		// first deal with pgdf and pgdx files, then noise. 
		String filePrefix = dataSource.createFilenamePrefix();
		List<File> binFiles = binaryStore.listAllFilesWithPrefix(filePrefix);
		if (binFiles == null || binFiles.isEmpty()) {
			return true; // nothing to delete. 
		}
		Collections.sort(binFiles, NameFileComparator.NAME_COMPARATOR);
		for (int i = binFiles.size()-1; i >= 0; i--) {
			int ans = deleteFileFrom(aBlock, binFiles.get(i), timeMillis);
			if (ans == FILE_TOO_EARLY) {
				break;
			}
		}
		
		return true;
	}

	/**
	 * Delete a specific file from a specific time. If the start of the file 
	 * is after timeMillis, delete the entire file, otherwise it will have 
	 * to be a partial delete. 
	 * @param aBlock 
	 * @param file
	 * @param timeMillis
	 * @return
	 */
	private int deleteFileFrom(PamDataBlock aBlock, File dataFile, long timeMillis) {
		File indexFile = binaryStore.findIndexFile(dataFile, true);
		if (indexFile == null) {
			indexFile = dataFile;
		}
		File noiseFile = binaryStore.findNoiseFile(dataFile, true);
		// get the header. 
		boolean headOk = false;
		BinaryHeader binaryHead = new BinaryHeader();
		try {
			FileInputStream fis = new FileInputStream(indexFile);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
			headOk = binaryHead.readHeader(dis);
			fis.close();
		}
		catch (IOException e) {
			headOk = false;
		}
		if (headOk == false || binaryHead.getDataDate() >= timeMillis) {
			boolean deleteOk = deleteFileSet(dataFile);
			return deleteOk ? FILE_DELETED : FILE_DELETE_ERROR;
		}
		/**
		 * Now need to see if the file is earlier than we want, in which case we return 
		 * immediately and won't look at any more files. 
		 */
		BinaryFooter fileEnd = binaryStoreStatusFuncs.findLastData(dataFile);
		if (fileEnd == null) {
			// the file has no footer and no data, so must be corrupt, so delete it.
			boolean deleteOk = deleteFileSet(dataFile);
			return deleteOk ? FILE_DELETED : FILE_DELETE_ERROR;
		}
		if (fileEnd.getDataDate() <= timeMillis) {
			/*
			 *  this file is earlier than our delete time, so we don't want to delete it
			 *  and need to send a message saying not to delete anything else either. 
			 */
			return FILE_TOO_EARLY;
		}
		/**
		 * If we land here, it looks like we're in the realm of needing to partially delete
		 * a file / set of data and noise files. What a pain ! Will need to do 
		 * the deleting and update the index file. f** knows what to do about a 
		 * serialized datamap. 
		 */
		partialCopyFile(aBlock, dataFile, timeMillis);
		if (indexFile != null) {
			partialCopyFile(aBlock, indexFile, timeMillis);
		}
		if (noiseFile != null) {
			partialCopyFile(aBlock, noiseFile, timeMillis);
		}
		
		return FILE_PARTIAL_DELETE;
	}
	
	private boolean partialCopyFile(PamDataBlock aBlock, File dataFile, long timeMillis) {
		System.out.printf("Partial delete of file %s from %s\n", dataFile.getAbsoluteFile(), PamCalendar.formatDBDateTime(timeMillis));
		try {
			BinaryInputStream inputStream = new BinaryInputStream(binaryStore, aBlock);
			if (inputStream.openFile(dataFile) == false) {
				return false;
			}
			
			BinaryDataSource dataSource = aBlock.getBinaryDataSource();

			File tempFile = new File(dataFile.getAbsolutePath() + ".tmp");
			BinaryOutputStream outputStream = new BinaryOutputStream(binaryStore, aBlock);
			dataSource.setBinaryStorageStream(outputStream);
			
			BinaryObjectData binaryObjectData;
			BinaryHeader bh = inputStream.readHeader();
			if (bh==null) {
				return false; 
			}
			outputStream.writeHeader(bh.getDataDate(), bh.getAnalysisDate());
			ModuleHeader mh = null;
			
			BinaryFooter bf = null;
			int inputFormat = bh.getHeaderFormat();
			while ((binaryObjectData = inputStream.readNextObject(inputFormat)) != null) {

				switch (binaryObjectData.getObjectType()) {
				case BinaryTypes.FILE_FOOTER:
					// this is unlikely to happen, since we'll probably already have found an index file. 
					bf = new BinaryFooter();
					bf.readFooterData(binaryObjectData.getDataInputStream(), inputFormat);
					bf.setDataDate(timeMillis);
					outputStream.writeFileFooter(bf);
					break;
				case  BinaryTypes.MODULE_HEADER:
					mh = dataSource.sinkModuleHeader(binaryObjectData, bh);
					outputStream.writeModuleHeader();
					break;
				case  BinaryTypes.MODULE_FOOTER:
					ModuleFooter mf = dataSource.sinkModuleFooter(binaryObjectData, bh, mh);
					outputStream.writeModuleFooter();
					break;
				case BinaryTypes.DATAGRAM:
//					dataSource.
					break;
				default: // should be data. 
					DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
					if (baseData == null) {
						continue;
					}
					if (baseData.getTimeMilliseconds() > timeMillis) {
						continue;
					}
					/*
					 *  otherwise we need to store this data unit. I think we can just copy in the
					 *  existing binary data to the new file non ? Might mess the datagram slightly, 
					 *  but that is only in the index file and can sort itself out.  
					 *  better to make a data unit and then rewrite it I think. 
					 */
					PamDataUnit dataUnit = dataSource.sinkData(binaryObjectData, bh, inputFormat);
					if (dataUnit != null) {
						dataUnit.getBasicData().mergeBaseData(binaryObjectData.getDataUnitBaseData());
						binaryStore.unpackAnnotationData(bh.getHeaderFormat(), dataUnit, binaryObjectData, null);
						dataSource.saveData(dataUnit);
					}
					
				}
			}

			outputStream.closeFile();
			inputStream.closeFile();

			/*
			 * Now file final stage - copy the temp file in place of the 
			 * original file. 
			 */
			boolean deletedOld = false;
			try {
				deletedOld = dataFile.delete();
			}
			catch (SecurityException e) {
				System.out.println("Error deleting old pgdf file: " + dataFile.getAbsolutePath());
				e.printStackTrace();
			}

			boolean renamedNew = false;
			try {
				renamedNew = tempFile.renameTo(dataFile);
			}
			catch (SecurityException e) {
				System.out.println("Error renaming new pgdf file: " + tempFile.getAbsolutePath() + 
						" to " + dataFile.getAbsolutePath());
				e.printStackTrace();
			}
			if (renamedNew == false) {
				if (deletedOld == false) {
					binaryStore.reportError("Unable to delete " + dataFile.getAbsolutePath());
				}
				return binaryStore.reportError(String.format("Unable to rename %s to %s", 
						tempFile.getAbsolutePath(), dataFile.getAbsolutePath()));
			}
			
			return true;
					
		}
		catch (Exception ex) {
			return false;
		}
		
	}

	/**
	 * Delete a set of files, including main data file, index file and noise file. 
	 * @param dataFile
	 * @return
	 */
	private boolean deleteFileSet(File dataFile) {
		System.out.printf("Deleting full file set for %s\n", dataFile.getAbsoluteFile());
		boolean deleteOk = true;
		try {
			File indexFile = binaryStore.findIndexFile(dataFile, true);
			File noiseFile = binaryStore.findNoiseFile(dataFile, true);
			deleteOk &= dataFile.delete();
			if (indexFile != null) {
				deleteOk &= indexFile.delete();
			}
			if (noiseFile != null) {
				deleteOk &= noiseFile.delete();
			}
		}
		catch (Exception e) {
			deleteOk = false;
		}

		System.out.printf("Deleting full file set %s for %s\n", deleteOk?"OK":"Error", dataFile.getAbsoluteFile());
		
		return deleteOk;
		
	}

	private void deleteEmptyFolders() {
		String root = binaryStore.binaryStoreSettings.getStoreLocation();
		if (root == null) {
			return;
		}
		/**
		 * Iterate through the root folder first and then call a 
		 * recursive function to delete sub folders. this will stop the 
		 * root folder from being deleted, but sub folders will get deleted if 
		 * they have no files (of any type) in them. 
		 */
		File rootFolder = new File(root);
		File[] subFolders = rootFolder.listFiles(directoryFilter);
		if (subFolders == null) {
			return;
		}
		for (int i = 0; i < subFolders.length; i++) {
			deleteEmptyFolders(subFolders[i]);
		}
	}

	private void deleteEmptyFolders(File file) {
		File[] subFolders = file.listFiles(directoryFilter);
		for (int i = 0; i < subFolders.length; i++) {
			deleteEmptyFolders(subFolders[i]);
		}
		// see if there is anything at all in this folder
		File[] remaining = file.listFiles();
		if (remaining.length == 0) {
			try {
				file.delete();
			}
			catch (Exception e) {
				System.out.printf("Binary folder %s cannot be deleted: %s\n", file.getAbsolutePath(), e.getMessage());
			}
		}
	}

}
