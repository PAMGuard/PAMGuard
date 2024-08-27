package binaryFileStorage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;

/**
 * Used to read data back from a binary data file and either send
 * it off to the associated PamDataBlock or hand it back to the 
 * BinaryStore controller, e.g. for writing into a new file if 
 * data are being updated. 
 * @author Doug Gillespie. 
 *
 */
public class BinaryInputStream {

	private PamDataBlock pamDataBlock;

	private BinaryStore binaryStore;

	private DataInputStream inputStream;

	//	private byte[] byteBuffer = null;

	private BinaryFooter binaryFooter = null;

	private int unitsRead = 0;

	private long lastObjectTime;

	private File currentFile;

	private FileInputStream fileInputStream;

	private BufferedInputStream bufferedInputStream;

	private CountingInputStream countingInputStream;



	public BinaryInputStream(BinaryStore binaryStore, PamDataBlock pamDataBlock) {
		super();
		this.binaryStore = binaryStore;
		this.pamDataBlock = pamDataBlock;
	}


	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		closeFile();
	}

	/**
	 * Open an input file for reading. 
	 * @param inputFile file to open. 
	 * @return true is successful. 
	 */
	protected boolean openFile(File inputFile) {
		currentFile = inputFile;
		try {
			fileInputStream = new FileInputStream(inputFile);
			countingInputStream = new CountingInputStream(new BufferedInputStream(fileInputStream));
			inputStream = new DataInputStream(countingInputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		unitsRead = 0;

		return true;
	}

	protected void closeFile() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			inputStream = null;
		}		
	}

	/**
	 * Reads the binary header from the file. 
	 * @return a binary header or null if the header could not be read
	 */
	protected BinaryHeader readHeader() {
		BinaryHeader bh = new BinaryHeader();
		if (bh.readHeader(inputStream)) {
			if (bh != null) {
				lastObjectTime = bh.getDataDate();
			}
			return bh;
		}
		return null;
	}

	long prevPos = 0;
	/**
	 * Read the next object from the file. 
	 * <p>The object data will be packed up in a 
	 * BinaryObjectData which will have various fields
	 * filled in depending on the type of object.
	 * <p> null will be returned when there is an eof or 
	 * when the file footer has been identified. 
	 * <p>
	 * The calling function can check the file was complete by testing
	 * whether or not the file footer is null.  
	 * @param fileFormat Over-arching File format read from the file header for files and incorporated 
	 * into additional header structures for network and other data.  
	 * @return data object. 
	 */
	protected BinaryObjectData readNextObject(int fileFormat) {
		int objectLength;
		int objectType;
		int binaryDataLength;
		byte[] byteBuffer = null;
		int buffLen = 0;
//		long objectTime;
//		Long nanoTime = null;
//		int channelMap = 0;
		int bytesRead;
		int moduleVersion;
		BinaryObjectData boData;

		try {
			long pos = countingInputStream.getPos();
			objectLength = inputStream.readInt();
			objectType = inputStream.readInt();
//			System.out.printf("File position = %d, step %d, next object length %d type %d ", pos, pos-prevPos, objectLength, objectType);
			prevPos = pos;
			if (objectLength <= 0) {
				System.out.println("Error in file - objectlength < 0 !");
				return null;
			}
//			System.out.println(String.format("Read object type %d, length %d", objectType, objectLength));
			if (objectType == BinaryTypes.FILE_FOOTER) {
				if (objectLength == 36) {
					objectLength = 48;
				}
				buffLen = objectLength-8;
				byteBuffer = new byte[buffLen];
				bytesRead = inputStream.read(byteBuffer);
				if (bytesRead != buffLen) {
					return null;
				}
				boData = new BinaryObjectData(fileFormat, objectType, byteBuffer, objectLength);
				binaryFooter = new BinaryFooter();
				binaryFooter.readFooterData(boData.getDataInputStream(), fileFormat);
				return boData;
			}
			else if (objectType == BinaryTypes.MODULE_HEADER) {
				moduleVersion = inputStream.readInt();
				if (objectLength >= 16) {
					buffLen = inputStream.readInt();
					byteBuffer = new byte[buffLen];
					bytesRead = inputStream.read(byteBuffer, 0, buffLen);
					if (bytesRead != buffLen) {
						return null;
					}
				}
				boData = new BinaryObjectData(fileFormat, objectType, byteBuffer, buffLen);
				boData.setVersionNumber(moduleVersion);
				return boData;
			}
			else if (objectType == BinaryTypes.MODULE_FOOTER) {
				buffLen = inputStream.readInt();
				byteBuffer = new byte[buffLen];
				bytesRead = inputStream.read(byteBuffer, 0, buffLen);
				if (bytesRead != buffLen) {
					return null;
				}
				boData = new BinaryObjectData(fileFormat, objectType, byteBuffer, buffLen);
				return boData;
			}
			else if (objectType == BinaryTypes.DATAGRAM) {
				buffLen = objectLength - 8;
				byteBuffer = new byte[buffLen];
				bytesRead = inputStream.read(byteBuffer, 0, buffLen);
				if (bytesRead != buffLen) {
					return null;
				}
				boData = new BinaryObjectData(fileFormat, objectType, byteBuffer, buffLen);
				return boData;
			}
			else{ // it's data - so get the extra timestamp
				long baseStart = countingInputStream.getPos();
				DataUnitBaseData baseData = new DataUnitBaseData();
				baseData.readBaseData(inputStream, fileFormat);
				long baseEnd = countingInputStream.getPos();
//				objectTime = inputStream.readLong();
//				if (fileFormat >= 2) {
//					nanoTime = inputStream.readLong();
//					channelMap = inputStream.readInt();
//				}
				binaryDataLength = inputStream.readInt();
//				System.out.printf(" base data %d bytes, other %d bytes (total %d)\n", baseEnd-baseStart, binaryDataLength, binaryDataLength + baseEnd-baseStart);
				// this should be 20, but in some modules it's incorrectly set at 
				//12 !!!!!
				if (binaryDataLength + 12 == objectLength && fileFormat < 2) {
//					System.out.println("Incorrect object data length in BinaryInputStream.readNextObject() " +
//							pamDataBlock.getDataName());
					// basically the total data length was correct and the object data length may not have been !
					// this is in PAMGuard rght up to V1.15
					binaryDataLength -= 8; // stoopid mess up with whether the time is included !
				}
				lastObjectTime = baseData.getTimeMilliseconds();
				//					objectTime += bh.getDataDate();
				//				buffLen = objectLength-16;
				//				if (byteBuffer == null || byteBuffer.length < buffLen) {
				if (binaryDataLength < 0) {
					System.out.println("Negative array size in BinaryInputStream.readNextObject() " +
							pamDataBlock.getDataName() + " filename: " + currentFile.getName());
				}
				byteBuffer = new byte[binaryDataLength];
				bytesRead = inputStream.read(byteBuffer, 0, binaryDataLength);
				if (bytesRead != binaryDataLength) {
					return null;
				}
				BinaryObjectData bod = new BinaryObjectData(fileFormat, objectType, baseData, unitsRead++, byteBuffer, binaryDataLength);
				if (baseData.isHasBinaryAnnotation()) {
					int totalAnnotationLength = inputStream.readShort();
					/*
					 *  now do another check on length.... not possible since
					 *  we don't know the binary record length.  
					 */
					int combinedDataLength = totalAnnotationLength + binaryDataLength;
//					if (combinedDataLength != objectLength-8) {
//						System.err.printf("Mess up in binary data total len = %d, main binary = ");
//					}
					byte[] allAnnotationData = new byte[totalAnnotationLength-2];
					int anBytesRead = inputStream.read(allAnnotationData);
					bod.setAnnotationData(allAnnotationData);
				}
//				DataUnitBaseData baseData = bod.getDataUnitBaseData();
//				baseData.setTimeNanoseconds(nanoTime);
//				baseData.setChannelBitmap(channelMap);
				return bod;
				
			}
		} 
		catch (EOFException eof) {
			return null;
		}
		catch (IOException e) {
			System.out.println(String.format("Read error in file %s: %s", currentFile.getName(), e.getMessage()));
		}
		return null;
	}


	/**
	 * @return the binaryFooter
	 */
	public BinaryFooter getBinaryFooter() {
		return binaryFooter;
	}

	/**
	 * @return the unitsRead
	 */
	public int getUnitsRead() {
		return unitsRead;
	}


	/**
	 * @return the lastObjectTime
	 */
	public long getLastObjectTime() {
		return lastObjectTime;
	}


	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}


	/**
	 * @return the currentFile
	 */
	public File getCurrentFile() {
		return currentFile;
	}
}
