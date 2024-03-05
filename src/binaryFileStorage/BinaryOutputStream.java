package binaryFileStorage;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import dataGram.Datagram;
import warnings.RepeatWarning;
import PamUtils.PamCalendar;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.debug.Debug;
import PamguardMVC.uid.DataBlockUIDHandler;
import binaryFileStorage.BinaryStore.BinaryDataMapMaker;

/**
 * Handles writing of an actual binary data file. 
 * <p>Is used during data analysis and also to rewrite
 * files if they are changed during offline analysis 
 * using the PAMGUARD viewer. 
 * @author Doug Gillespie
 * @see BinaryStore
 *
 */
public class BinaryOutputStream {

	private static RepeatWarning repeatWarning;

	private PamDataBlock parentDataBlock;

	private BinaryDataSource binaryDataSource;

	private BinaryStore binaryStore;

	private BinaryHeader header;

	private BinaryFooter footer;

	private byte[] moduleHeaderData;

	private byte[] moduleFooterData;

	private DataOutputStream dataOutputStream;
	
	private DataOutputStream noiseOutputStream;

	private int storedObjects, storedNoiseCount;

	private String mainFileName, indexFileName;

	private FileLock outputFileLock;

	private long fileStartUID;

	private BinaryOfflineDataMapPoint currentDataMapPoint;
	
	private int lastObjectType = Integer.MIN_VALUE;

	public BinaryOutputStream(BinaryStore binaryStore,
			PamDataBlock parentDataBlock) {
		super();
		this.binaryStore = binaryStore;
		this.parentDataBlock = parentDataBlock;
		binaryDataSource = parentDataBlock.getBinaryDataSource();
		binaryDataSource.setBinaryStorageStream(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		closeFile();
		super.finalize();
	}

	protected synchronized void reOpen(long dataTime, long analTime, int endReason) {
		// finish off this file. 
		writeModuleFooter();
		writeFooter(dataTime, analTime, endReason);
		closeFile();
		// create the matching index file
		createIndexFile();
		
		// open the next file
		openOutputFiles(dataTime);
		writeHeader(dataTime, analTime);
		writeModuleHeader();
	}

	/**
	 * Open an output file. 
	 * <p> this call is used in real time ops to create a new file
	 * name based on the time and information from the datablock name.
	 * @param dataTime time in Java milliseconds 
	 * @return true if successful, false otherwise. 
	 */
	public synchronized boolean openOutputFiles(long dataTime) {

		createFileNames(dataTime);
		
		
		if (mainFileName == null) {
			System.out.println("Binary files not created - invalid folder ? " + binaryStore.binaryStoreSettings.getStoreLocation());
			return false;
		}
		
		DataBlockUIDHandler uidHandler = parentDataBlock.getUidHandler();
		uidHandler.roundUpUID(DataBlockUIDHandler.ROUNDINGFACTOR);		
		fileStartUID = parentDataBlock.getUidHandler().getCurrentUID();
		
		File outputFile = new File(mainFileName);

		boolean open = openPGDFFile(outputFile);
		
//		System.out.println("Open outout file " + outputFile.getAbsolutePath());

		if (open) {
			addToDataMap(outputFile);
		}		
		
		if (wantNoiseOutputFile()) {
			openNoiseFile(outputFile);
		}
		else {
			noiseOutputStream = null;
		}
		
		
		
		return open;
	}

	/**
	 * See if we're going to want a separate noise output file
	 * @return true if we'll want a separate noise output file
	 */
	private boolean wantNoiseOutputFile() {
		if (parentDataBlock.getBackgroundManager() == null) {
			return false; // nope !
		}
		if (parentDataBlock.getBackgroundManager().getBackgroundBinaryWriter() == null) {
			return false; // nope !
		}
		if (binaryStore.getNoiseStore() == NoiseStoreType.PGDF) {
			return false; // may have noise, but will write to pgdf file
		}
		return true;
	}
	/**
	 * Now gets called online. this should never get called in viewer mode. 
	 * @param outputFile
	 */
	private void addToDataMap(File outputFile) {
		BinaryOfflineDataMap dataMap = (BinaryOfflineDataMap) parentDataBlock.getOfflineDataMap(binaryStore);
		if (dataMap == null) {
			return;
		}
		currentDataMapPoint = new BinaryOfflineDataMapPoint(binaryStore, outputFile, header, null, null, null, null);
		dataMap.addDataPoint(currentDataMapPoint);
	}

	/**
	 * Open an output file.
	 * <p>
	 * This version is also used when rewriting files when data have 
	 * been changed offline. Generally the file will be a .tmp file 
	 * @param outputFile output file
	 * @return true if successful
	 */
	public boolean openPGDFFile(File outputFile) {
		header = new BinaryHeader(binaryDataSource.getModuleType(), binaryDataSource.getModuleName(),
				binaryDataSource.getStreamName(), BinaryStore.getCurrentFileFormat());
		//		header.setExtraInfo(binaryDataSource.getModuleHeader());
		storedObjects = 0;
		FileOutputStream fileOutputStream;
		try {
			dataOutputStream = new DataOutputStream(new BufferedOutputStream(fileOutputStream = new 
					FileOutputStream(outputFile)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		/*
		 * Attempt to lock the file so that nothing else
		 * can modify it. 
		 */
//		FileChannel outputFileChannel = fileOutputStream.getChannel();
//		try {
//			outputFileLock = outputFileChannel.tryLock();
////			outputFileLock.i
//		} catch (IOException e) {
////			e.printStackTrace();
//		}
//		Debug.out.printf("Opened binary storage file %s\n", outputFile.getName());
		
		binaryDataSource.newFileOpened(outputFile);

		return true;
	}
	
	public boolean openNoiseFile(File anyOldFile) {
		if (binaryStore.getNoiseStore() == NoiseStoreType.PGNF) {
			File noiseFile = binaryStore.swapFileType(anyOldFile, BinaryStore.noiseFileType);
			try {
				noiseOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(noiseFile)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				noiseOutputStream = null;
				return false;
			}
		}
		else {
			noiseOutputStream = null;
		}
		storedNoiseCount = 0;
		return true;
	}

	/**
	 * Create names for the main storage file and also for the 
	 * corresponding index file. 
	 * @param dataTime time in milliseconds. 
	 * @return true if folder is OK and file names have been created. 
	 */
	private boolean createFileNames(long dataTime) {

		String folderName = binaryStore.getFolderName(dataTime, true);

		if (folderName == null) {
			return false;
		}
		/**
		 * Don't check for spaces in the directory name since that will 
		 * have been set from the browser - so will be correct. 
		 * do however remove spaces from the rest of the file name. 
		 */
//		String filePrefix = String.format("%s_%s_%s_", 
//				binaryDataSource.getModuleType(), binaryDataSource.getModuleName(),
//				binaryDataSource.getStreamName());
		String filePrefix = binaryDataSource.createFilenamePrefix();
		mainFileName = folderName + fillSpaces(PamCalendar.createFileName(dataTime, 
				filePrefix, BinaryStore.fileType));
		indexFileName = folderName + fillSpaces(PamCalendar.createFileName(dataTime, 
				filePrefix, BinaryStore.indexFileType));
		return true;
	}
	
	/**
	 * Fill blank spaces in a string.
	 * @param str
	 * @return string with spaces replaced with the underscore character
	 */
	private String fillSpaces(String str) {
		return str.replaceAll(" ", "_");
	}

	public synchronized boolean closeFile() {
		boolean ok = true;
//		System.out.println("Close output file " + mainFileName);
		if (dataOutputStream != null) {
			if (currentDataMapPoint != null) {
				currentDataMapPoint.setBinaryFooter(footer);
			}
			try {
				dataOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				ok = false;
			}
			dataOutputStream = null;
		}
		if (noiseOutputStream != null) {
			try {
				noiseOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				ok = false;
			}
		}
//		Debug.out.printf("Closed binary storage file %s\n", mainFileName);
		return ok;
	}

	private long getSamplesFromMilliseconds(long timeMillis) {
		if (parentDataBlock == null) {
			return 0;
		}
		if (parentDataBlock.getParentProcess() == null) {
			return 0;
		}
		return parentDataBlock.getParentProcess().absMillisecondsToSamples(timeMillis);
	}

	public synchronized boolean writeHeader(long dataTime, long analTime) {
		if (dataOutputStream == null) {
			return false;
		}
		long sampleNumber = 0;
		if (parentDataBlock != null)
			header.setFileStartSample(getSamplesFromMilliseconds(dataTime));
		header.setDataDate(dataTime);
		header.setAnalysisDate(analTime);
		boolean ok = header.writeHeader(dataOutputStream);
		if (noiseOutputStream != null) {
			ok &= header.writeHeader(noiseOutputStream);
		}
		lastObjectType = BinaryTypes.FILE_HEADER;
		return ok;
	}

	/**
	 * Called just after datafiles are opened to put a header in the main output pgdf file. 
	 * @return true if OK. 
	 */
	public synchronized boolean writeModuleHeader() {
		if (dataOutputStream == null) {
			return false;
		}
		byte[] moduleHeaderData = binaryDataSource.getModuleHeaderData();
		boolean ok = writeModuleHeader(dataOutputStream, moduleHeaderData);
		if (noiseOutputStream != null ) {
			ok &= writeModuleHeader(noiseOutputStream, moduleHeaderData);
		}
		lastObjectType = BinaryTypes.MODULE_HEADER;
		return ok;
	}

	/**
	 * Only writes module header to main pgdf file.
	 * @param headerData
	 * @return
	 */
	public synchronized boolean writeModuleHeader(byte[] headerData) { 
		return writeModuleHeader(dataOutputStream, headerData);
	}
	
	public synchronized boolean writeModuleHeader(DataOutputStream outputStream, byte[] headerData) { 
		moduleHeaderData = headerData;
		if (outputStream == null) {
			return false;
		}
		int objectLength = 16;
		int dataLength = 0;
		int moduleVersion = binaryDataSource.getModuleVersion();
		if (headerData != null) {
			dataLength = headerData.length;
			objectLength += dataLength;
		}

		try {
			outputStream.writeInt(objectLength);
			outputStream.writeInt(BinaryTypes.MODULE_HEADER);
			outputStream.writeInt(moduleVersion);
			outputStream.writeInt(dataLength);
			if (moduleHeaderData != null) {
				outputStream.write(headerData);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	public synchronized boolean writeModuleFooter() {
		if (dataOutputStream == null) {
			return false;
		}
		moduleFooterData = binaryDataSource.getModuleFooterData();
		return writeModuleFooter(dataOutputStream, moduleFooterData);
	}
	
	/**
	 * Write a module footer to the output stream. 
	 * @param moduleFooter module footer
	 * @return true if successful
	 */
	public synchronized boolean writeModuleFooter(ModuleFooter moduleFooter) {
		byte[] footerData = moduleFooter.getByteArray();
		return writeModuleFooter(footerData);
	}

	/**
	 * Write module footer data to the output stream. Note that 
	 * this is the data alone, without the first 12 bytes of identifier and 
	 * length information. 
	 * @param footerData footer data as a binary array
	 * @return true if successful
	 */
	public synchronized boolean writeModuleFooter(byte[] footerData) {
		lastObjectType = BinaryTypes.MODULE_FOOTER;
		return writeModuleFooter(dataOutputStream, footerData);
	}
	
	/**
	 * Write module footer data to a specific output stream. 
	 * @param outputStream output stream
	 * @param footerData footer data in a binary array
	 * @return true if sucessful. 
	 */
	public synchronized boolean writeModuleFooter(DataOutputStream outputStream, byte[] footerData) {
		moduleFooterData = footerData; // will need this when index file is written !
		if (outputStream == null) {
			return false;
		}
		int objectLength = 12;
		int dataLength = 0;
		if (footerData != null) {
			dataLength = footerData.length;
			objectLength += dataLength;
		}

		try {
			outputStream.writeInt(objectLength);
			outputStream.writeInt(BinaryTypes.MODULE_FOOTER);
			outputStream.writeInt(dataLength);
			if (footerData != null) {
				outputStream.write(footerData);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		lastObjectType = BinaryTypes.MODULE_FOOTER;
		return true;
	}

	public synchronized boolean writeFooter(long dataTime, long analTime, int endReason) {
		long fileLen = 0;
		if (dataOutputStream == null) {
			return false;
		}
		if (currentDataMapPoint != null) {
			currentDataMapPoint.setEndTime(dataTime);
		}
		fileLen = dataOutputStream.size();
		footer = new BinaryFooter(dataTime, analTime,
				storedObjects, fileLen);
		footer.setFileEndSample(getSamplesFromMilliseconds(dataTime));
		footer.setFileEndReason(endReason);
		footer.setLowestUID(this.fileStartUID);
		footer.setHighestUID(parentDataBlock.getUidHandler().getCurrentUID());
		boolean ok = footer.writeFooter(dataOutputStream, BinaryStore.getCurrentFileFormat());
		if (noiseOutputStream != null) {
//			footer.setnObjects(storedNoiseCount);
			ok &= footer.writeFooter(noiseOutputStream, BinaryStore.getCurrentFileFormat());
		}
		lastObjectType = BinaryTypes.FILE_FOOTER;
		return ok;
	}
	
	public synchronized long getFileSize() {
		if (dataOutputStream == null) {
			return -1;
		}
		return dataOutputStream.size();
	}

//	/**
//	 * Write data to a file
//	 * @param objectId unique object identifier.
//	 * @param timeMillis time of object in java milliseconds
//	 * @param data byte array of binary data to store. 
//	 * @return true if written successfully. 
//	 */
//	@Deprecated
//	public synchronized boolean storeData(int objectId, DataUnitBaseData basicData, byte[] data) {
//		return storeData(objectId, basicData, data, data.length);
//	}

//	/**
//	 * Write data to a file.  
//	 * @param binaryObjectData data to store.  
//	 * @return true if written successfully
//	 */
//	@Deprecated
//	public boolean storeData(BinaryObjectData binaryObjectData) {
//		return storeData(binaryObjectData.getObjectType(), binaryObjectData.getDataUnitBaseData(),
//				binaryObjectData.getData(), binaryObjectData.getDataLength());
//	}
	
	public synchronized boolean storeData(int objectId, DataUnitBaseData baseData, BinaryObjectData binaryObjectData) {
		boolean ok;
		if (objectId == BinaryTypes.BACKGROUND_DATA & noiseOutputStream != null) {
			ok = storeData(noiseOutputStream, objectId, baseData, binaryObjectData);
			if (ok) {
				storedNoiseCount++;
			}
		}
		else {
			ok = storeData(dataOutputStream, objectId, baseData, binaryObjectData);
			if (ok) {
				storedObjects++;
			}
		}
		return ok;
	}
	/**
	 * Writes data to a file. Note that the length of data may be greater than
	 * the actual length of useful data which is held in onjectLength 
	 * @param objectId unique object identifier.
	 * @param timeMillis time of object in java milliseconds
	 * @param data byte array of binary data to store. 
	 * @param objectLength length of useful data in data (often = data.length) 
	 * @return true if written successfully.
	 */
	public synchronized boolean storeData(DataOutputStream outputStream, int objectId, DataUnitBaseData baseData, BinaryObjectData binaryObjectData) {
		if (lastObjectType == BinaryTypes.MODULE_FOOTER) {
			System.out.printf("Storing binary object type %d in file %s with no module header\n", objectId, outputStream == null ? null : outputStream.toString());
		}
		byte[] data = binaryObjectData.getData();
		int objectLength = binaryObjectData.getDataLength();
		byte[] annotationData = binaryObjectData.getAnnotationData();
		int annotationDataLength = binaryObjectData.getAnnotationDataLength();
		int lengthInFile = objectLength + baseData.getBaseDataBinaryLength() + 12;
		if (annotationDataLength > 0) {
			lengthInFile += annotationDataLength + 2;
		}
		/*
		 *  extra space for length in file       (4)
		 *                  object identifier    (4)
		 *                  object binary length (4)
		 *                  timestamp            (8) total = 20 bytes.   
		 */
//		int dataLength = objectLength + 8; // don't add the additional timestamp length
		// above line was in there and WRONG right up to V1.15. See corresponding bodge
		// in BinaryinputStream.readNextObject();
		int dataLength = objectLength; // this is correct !

		if (outputStream == null) {
			return false;
		}

		int newLength = outputStream.size() + lengthInFile + BinaryFooter.getStandardLength();

		if (binaryStore.binaryStoreSettings.limitFileSize && binaryStore.isViewer() == false &&
				newLength > binaryStore.binaryStoreSettings.getMaxSizeMegas()) {
			reOpen(PamCalendar.getTimeInMillis(), System.currentTimeMillis(), BinaryFooter.END_FILETOOBIG);
		}

		try {
			outputStream.writeInt(lengthInFile);
			outputStream.writeInt(objectId);
//			if (BinaryStore.CURRENT_FORMAT <= 2) {
//				dataOutputStream.writeLong(baseData.getTimeMilliseconds());
//			}
//			if (BinaryStore.CURRENT_FORMAT == 2) {
//				Long timeNanoseconds = baseData.getTimeNanoseconds();
//				dataOutputStream.writeLong(timeNanoseconds == null ? 0 : timeNanoseconds);
//				dataOutputStream.writeInt(baseData.getChannelBitmap());
//			}
//			else {
			baseData.writeBaseData(outputStream, binaryStore.getCurrentFileFormat());
//			}
			outputStream.writeInt(dataLength);
			outputStream.write(data, 0, objectLength);
			if (annotationDataLength > 0) {
				outputStream.writeShort(annotationDataLength+2);
				outputStream.write(annotationData, 0, annotationDataLength);
			}
		} catch (IOException e) {
			reportStreamError(e);
			return false;
		}



		return true;
	}

	/**
	 * Report stream error counts. 
	 * @param e
	 */
	private static synchronized void reportStreamError(IOException e) {
		// TODO Auto-generated method stub
		if (repeatWarning == null) {
			repeatWarning = new RepeatWarning("Binary Output Stream");
		}
		repeatWarning.showWarning(e, 2);
	}

	public boolean createIndexFile() {
		if (indexFileName != null) {
			return createIndexFile(new File(indexFileName), currentDataMapPoint);
		}
		return false;
	}

	public boolean createIndexFile(File indexFile, BinaryOfflineDataMapPoint mapPoint){
		DataOutputStream opStream;
		try {
			opStream = new DataOutputStream(new BufferedOutputStream(new 
					FileOutputStream(indexFile)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		header.writeHeader(opStream);
		writeModuleHeader(opStream, moduleHeaderData);
		if (mapPoint != null) {
			Datagram dataGram = mapPoint.getDatagram();
			if (dataGram != null) {
				dataGram.writeDatagram(opStream);
			}
		}

		writeModuleFooter(opStream, moduleFooterData);
		footer.writeFooter(opStream, binaryStore.getCurrentFileFormat());

		try {
			opStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Write a Datagram to an output stream
	 * @param datagram
	 * @return true if successful. 
	 * @see Datagram
	 */
	public boolean writeDatagram(Datagram datagram) {
		if (dataOutputStream == null || datagram == null) {
			return false;
		}
		return datagram.writeDatagram(dataOutputStream);
	}

	/**
	 * Write a file footer to the binary output stream. 
	 * @param footer file footer. 
	 * @return true if successful.
	 */
	public boolean writeFileFooter(BinaryFooter footer) {
		if (dataOutputStream == null || footer == null) {
			return false;
		}
		return footer.writeFooter(dataOutputStream, binaryStore.getCurrentFileFormat());
	}

	/**
	 * @return the mainFileName
	 */
	public String getMainFileName() {
		if (mainFileName == null) {
			return null;
		}
		File aFile = new File(mainFileName);
		return aFile.getName();
	}

	/**
	 * Get the number of objects stored in the current file. 
	 * @return the number of objects stored in the current file. 
	 */
	public int getStoredObjects() {
		return storedObjects;
	}
}
