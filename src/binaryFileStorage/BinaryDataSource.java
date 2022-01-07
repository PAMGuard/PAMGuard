package binaryFileStorage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



import dataMap.DataMapDrawing;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.background.BackgroundBinaryWriter;
import PamguardMVC.background.BackgroundDataUnit;
import PamguardMVC.background.BackgroundManager;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;

/**
 * Not just a source, but also a sink for binary data from the
 * binary store. Every BinaryDataSource is tied
 * to a single PamDataBlock
 * @author Doug Gillespie
 * @see PamDataBlock
 *
 */
public abstract class BinaryDataSource {

	private BinaryOutputStream binaryStorageStream;
	
	private PamDataBlock sisterDataBlock;
	
	private boolean doBinaryStore = true;
	
	private boolean storeData = true;
	
	private boolean saveUpdates = false;
	
	private BackgroundBinaryWriter backgroundBinaryWriter;

	/**
	 * Create a binary data source. These are used both to store data in binary 
	 * files, and possibly also to send data to other PAMguard instances over the network. 
	 * @param sisterDataBlock dataBlock for data to store / send
	 */
	public BinaryDataSource(PamDataBlock sisterDataBlock) {
		super();
		this.sisterDataBlock = sisterDataBlock;
	}

	/**
	 * Create a binary data source. These are used both to store data in binary 
	 * files, and possibly also to send data to other PAMguard instances over the network. 
	 * @param sisterDataBlock dataBlock for data to store / send
	 * @param doBinaryStore true if data to be stored by default, false otherwise. 
	 */
	public BinaryDataSource(PamDataBlock sisterDataBlock, boolean doBinaryStore) {
		super();
		this.sisterDataBlock = sisterDataBlock;
		setDoBinaryStore(doBinaryStore);
	}
	
	/**
	 *
	 * @return Module type to be stored in the file header
	 */
	public String getModuleType() {
		return sisterDataBlock.getParentProcess().getPamControlledUnit().getUnitType();
	}
	
	/**
	 * 
	 * @return Module name to be stored in the file header
	 */
	public String getModuleName() {
		return sisterDataBlock.getParentProcess().getPamControlledUnit().getUnitName();
	}
	
	/**
	 * 
	 * @return Stream name to be stored in the file header
	 */
	public abstract String getStreamName();
	
	/**
	 * 
	 * @return Stream version name to be stored in the 
	 * Module Specific Control structure
	 */
	public abstract int getStreamVersion();

	/**
	 * Get a version number for the module. 
	 * <p>This is different to the version number in the main
	 * file header and allows individual modules to update their 
	 * format and maintain backwards compatibility with old data
	 * @return integer module version number
	 */
	public abstract int getModuleVersion();
	/**
	 * 
	 * @return Additional information (e.g. a control structure
	 * for a detector) to be stored in the 
	 * Module Specific Control structure
	 */
	public abstract byte[] getModuleHeaderData();
	
	/**
	 * @return data for the binary footer, or null. 
	 */
	public byte[] getModuleFooterData() {
		return null;
	}

	/**
	 * Convert data read back in in viewer mode into the correct
	 * type of PamDataUnit.
	 * <p><strong>DO NOT</strong> add this unit directly to the datablock, but pass
	 * it back to the calling process which will add it to the datablock
	 * if necessary. 
	 * @param binaryObjectData Binary data read back from a file. 
	 * @param bh binary header from start of file.
	 * @param moduleVersion 
	 * @return the PamDataUnit created from these data 
	 */
	public abstract PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion);

	/**
	 * Do something with module header information
	 * @param binaryObjectData data for the module header. 
	 * @param bh Binary header information
	 */
	public abstract ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh);
	
	/**
	 * Do something with module footer information
	 * @param binaryObjectData data for the module header. 
	 * @param bh Binary header information
	 */
	public abstract ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, 
			BinaryHeader bh, ModuleHeader moduleHeader);
	
	/**
	 * @param binaryStorageStream the binaryStorageStream to set
	 */
	public void setBinaryStorageStream(BinaryOutputStream binaryStorageStream) {
		this.binaryStorageStream = binaryStorageStream;
	}

	/**
	 * @return the binaryStorageStream
	 */
	public BinaryOutputStream getBinaryStorageStream() {
		return binaryStorageStream;
	}

	
	/**
	 * Creates the prefix for the binary store file names.  Moved from the BinaryOutputStream
	 * class and changed to a public method so that it could be called by the
	 * binaryUIDFunctions object when searching for specific files
	 * 
	 * @return
	 */
	public String createFilenamePrefix() {
		String filePrefix = String.format("%s_%s_%s_", 
				this.getModuleType(), this.getModuleName(),
				this.getStreamName());
		return (this.fillSpaces(filePrefix));
	}

	/**
	 * Fill blank spaces in a string.
	 * @param str
	 * @return string with spaces replaced with the underscore character
	 */
	private String fillSpaces(String str) {
		return str.replaceAll(" ", "_");
	}

/**
	 * Save new data into the binary stream
	 * @param pamDataUnit
	 */
	public final boolean saveData(PamDataUnit pamDataUnit) {
		
		//System.out.println("Save data: " + pamDataUnit);

		if (getBinaryStorageStream() == null) {
			return false;
		}
		
		if (pamDataUnit==null){
			System.out.println("Warning: PAMGUARD attempted to write null data unit to binary file.");
			return false;
		}

		if (storeData == false) {
			return false;
		}
		
		int channelShift = 0;
		BinaryStore binaryStore = null;
		DataUnitFileInformation fileInfo = pamDataUnit.getDataUnitFileInformation();
		if (fileInfo != null && fileInfo.getBinaryStore() != null) {
			binaryStore = fileInfo.getBinaryStore();
			channelShift = fileInfo.getBinaryStore().getBinaryStoreSettings().channelShift;
		}
		/* 
		 * would have been shifted when it was read in, remember it !
		 */
		int shiftedBitmap = pamDataUnit.getChannelBitmap();
//		shiftedBitmap = -268435456;
//		if (shiftedBitmap == -268435456) {
//			int newBM = shiftedBitmap >>> channelShift;
//			int newBMS = shiftedBitmap >> channelShift;
//		}
		pamDataUnit.setChannelBitmap(shiftedBitmap >>> channelShift);
		/*
		 * Then pack the data
		 */
		BinaryObjectData data;
		if (pamDataUnit instanceof BackgroundDataUnit) {
			data = getBackgroundBinaryWriter().packBackgroundData((BackgroundDataUnit) pamDataUnit);
		}
		else {
			data = getPackedData(pamDataUnit);
		}
		/*
		 * Then put it back to how it was a moment ago.
		 */
		pamDataUnit.setChannelBitmap(shiftedBitmap);
		
		if (data == null) {
			return false;
		}
		
		// now get any annotation data that goes with this binary object. 
		getPackedAnnotationData(pamDataUnit, data);
		pamDataUnit.getBasicData().setHasBinaryAnnotations(data.getAnnotationDataLength() != 0);
		
		/*
		 * Add the file information to ALL data units (needed in the click detector
		 * but I see no reason not to do it generically - except perhaps memory usage). 
		 */
		if (pamDataUnit.getDataUnitFileInformation() == null && binaryStorageStream != null) {
			String fileName = binaryStorageStream.getMainFileName();
			int indexInFile = binaryStorageStream.getStoredObjects();
			if (fileName != null) {
				DataUnitFileInformation dataUnitFileInformation = new DataUnitFileInformation(binaryStore, new File(fileName), indexInFile);
				pamDataUnit.setDataUnitFileInformation(dataUnitFileInformation);
			}
		}
				
		return getBinaryStorageStream().storeData(data.getObjectType(), pamDataUnit.getBasicData(), data);
	}

	/**
	 * Get packed annotation data from the data unit. 
	 * @param pamDataUnit data unit which might contain data annotations
	 * @param data object data to hold the packed annotation data. 
	 */
	private void getPackedAnnotationData(PamDataUnit pamDataUnit, BinaryObjectData data) {
		int nAnnotation = pamDataUnit.getNumDataAnnotations();
		ArrayList<AnnotationBinaryData> dataList = null;
		int totalLength = 0;
		//System.out.println("N annotation: " +  nAnnotation);
		for (int i = 0; i < nAnnotation; i++) {
			DataAnnotation annotation = pamDataUnit.getDataAnnotation(i);
			DataAnnotationType<?> annotationType = annotation.getDataAnnotationType(); 
			AnnotationBinaryHandler<?> binaryHandler = annotationType.getBinaryHandler();
			
			//System.out.println("Binary handler: " +  binaryHandler);

			if (binaryHandler == null) {
				continue;
			}
			AnnotationBinaryData annotationData = binaryHandler.getAnnotationBinaryData(pamDataUnit, annotation);
			
			//System.out.println("Annotation data: " +  annotationData);

			if (annotationData == null) {
				continue;
			}
			if (dataList == null) {
				dataList = new ArrayList<>();
			}
			dataList.add(annotationData);
			totalLength += annotationData.data.length + 4 + annotationData.shortIdCode.length() + 2 + 2;
		}
		if (dataList == null || dataList.size() == 0) {
			data.setAnnotationData(null);
			return;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
//			dos.writeShort(totalLength);
			dos.writeShort(dataList.size());
			for (AnnotationBinaryData abd:dataList) {
				/*
				 * length of individual annotations is data.length + 6 + strlen(idCode)
				 * that's 2 bytes for total length, 2 for version, 2 for UTF header and chars from code name. 
				 */
				/*
				 * Looks like there is an error here, but we probably shoulnd't correct it
				 * for some reason we're writing +8 in the length when it should have been +6
				 * but won't change since it would mess old data. DMG June 2019. 
				 */
				dos.writeShort(abd.data.length + abd.shortIdCode.length() + 2 + 4 + 2);
				dos.writeUTF(abd.shortIdCode);
				dos.writeShort(abd.annotationVersion);
				dos.write(abd.data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		data.setAnnotationData(bos.toByteArray());
	}

	/**
	 * Get packed binary data for either sending to file or over the network
	 * @param pamDataUnit data unit to pack
	 * @return packed binary data object
	 */
	abstract public BinaryObjectData getPackedData(PamDataUnit pamDataUnit);
	
//	/**
//	 * 
//	 * @param objectId
//	 * @param timeMillis
//	 * @param data
//	 * @return
//	 */
//	@Deprecated
//	public boolean storeData(int objectId, DataUnitBaseData baseData, byte[] data) {
//		if (binaryStorageStream == null) {
//			return false;
//		}
//		return binaryStorageStream.storeData(objectId, baseData, data);
//	}

	public PamDataBlock getSisterDataBlock() {
		return sisterDataBlock;
	}

	/**
	 * Called from the BinaryOutputStream whenever a new output
	 * file is opened.  
	 * @param outputFile file information.
	 */
	public abstract void newFileOpened(File outputFile);
	

	/**
	 * REturn a class capable of overriding the normal drawing on 
	 * the data map 
	 * @return null if nothign exists. 
	 */
	public DataMapDrawing getSpecialDrawing() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param doBinaryStore the doBinaryStore to set
	 * This flag tells the binary store that this unit is available for binary storage. It's
	 * used mostly in raw data blocks which use this same class to write to a network socket
	 * but have the ability to wrote to the binary store disabled. 
	 */
	public void setDoBinaryStore(boolean doBinaryStore) {
		this.doBinaryStore = doBinaryStore;
	}

	/**
	 * @return the doBinaryStore
	 */
	public boolean isDoBinaryStore() {
		return doBinaryStore;
	}

	/**
	 * Reset anything needing resetting in the binary data source. 
	 * This get's called just before PamStart(). 
	 */
	public void reset() {
	}

	/**
	 * Flag to say we want to actually store the data. 
	 * @return the storeData
	 */
	public boolean isStoreData() {
		return storeData;
	}

	/**
	 * Flag to say we want to actually store the data. 
	 * @param storeData the storeData to set
	 */
	public void setStoreData(boolean storeData) {
		this.storeData = storeData;
	}

	/**
	 * Flag to say that updates should also be stored. 
	 * this will cause a second record to be written to the binary file, so 
	 * default response is false. 
	 * @return the saveUpdates
	 */
	public boolean isSaveUpdates() {
		return saveUpdates;
	}

	/**
	 * Flag to say that updates should also be stored. 
	 * this will cause a second record to be written to the binary file, so 
	 * default response is false. 
	 * @param saveUpdates the saveUpdates to set
	 */
	public void setSaveUpdates(boolean saveUpdates) {
		this.saveUpdates = saveUpdates;
	}

	/**
	 * @return the backgroundBinaryWriter
	 */
	public BackgroundBinaryWriter getBackgroundBinaryWriter() {
		if (backgroundBinaryWriter == null) {
			BackgroundManager bgm = sisterDataBlock.getBackgroundManager();
			if (bgm == null) {
				return null;
			}
			backgroundBinaryWriter = bgm.getBackgroundBinaryWriter();
		}
		return backgroundBinaryWriter;
	}
	
}
