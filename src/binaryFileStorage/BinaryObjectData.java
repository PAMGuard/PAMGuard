package binaryFileStorage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import PamUtils.PamCalendar;
import PamguardMVC.DataUnitBaseData;

/**
 * Class to temporarily hold an objected data that has just been read from 
 * a binary file. Not all types of object use all the fields and data don't
 * tend to stay in this class for long - it's just a convenient way of getting
 * data from a to b. Note that the format of data within this object is only concerned with 
 * the file. Individual modules will have put an additional version number in the module header
 * which will get passed around by whatever is unpacking the data. 
 * @author Doug Gillespie
 *
 */
public class BinaryObjectData {

	/**
	 * Object type (negative numbers for main headers and footers, > 0
	 * for proprietary data formats for data objects)
	 */
	private int objectType;
	
	/**
	 * Binary data - in whatever format
	 */
	private byte[] data;
	
	/**
	 * Length of data (often data.length)
	 */
	private int dataLength;

	/**
	 * Number of the object within the file
	 */
	private int objectNumber;
	
	/**
	 * Version number of the FILE format, not the module
	 * format.
	 */
	private int versionNumber;

	/**
	 * Basic data for all data units. Will be null
	 * for header and footer information, but get's populated for 
	 * other data types. This will eventually get passed stright in 
	 * to new PamDataUnits. 
	 */
	private DataUnitBaseData dataUnitBaseData;
	
	/**
	 * Byte array of all annotation data, starting with the int16 giving the number of annotations
	 */
	private byte[] annotationData;
	
	/**
	 * Length of annotationData. The total length actually written to the file
	 * will be this + 2 for the two bytes giving the length. 
	 */
	private int annotationDataLength;
	

	@Override
	public String toString() {
		String t = "Not set";
		if (dataUnitBaseData != null) {
			t = PamCalendar.formatDate(dataUnitBaseData.getTimeMilliseconds());
		}
		return String.format("Obj %d, Type %d, Time %s, DataLen %d, arrayLen %d",
				objectNumber, objectType, t,
				dataLength, data.length);
	}


	/**
	 * Constructor used when header / footer or other non-data objects are read from a stream. 
	 * @param objectType Object type
	 * @param data binary data
	 * @param dataLength binary data length. 
	 */
	public BinaryObjectData(int fileVersion, int objectType, byte[] data,
			int dataLength) {
		this.versionNumber = fileVersion;
		this.objectType = objectType;
		this.data = data;
		this.dataLength = dataLength;
	}


	/**
	 * Constructor used when data units are being read from a data stream. 
	 * @param objectType object type 
	 * @param baseData base data object, which should have already been read and populated. 
	 * @param objectNumber object count in stream
	 * @param data binary data
	 * @param dataLength binary data length
	 */
	public BinaryObjectData(int fileVersion, int objectType, DataUnitBaseData baseData, int objectNumber,  byte[] data,
			int dataLength) {
//		super(timeMillis, 0, null);
		this.versionNumber = fileVersion;
		this.objectType = objectType;
		this.dataUnitBaseData = baseData;
		this.objectNumber = objectNumber;
		this.data = data;
		this.dataLength = dataLength;
	}


	/**
	 * Version of constructor to replace previous calls to PackedbinaryObject which is 
	 * now deprecated and removed. 
	 * @param objectType Object type (+ve for data type, -ve for headers and footers)
	 * @param moduleVersion (will have been read back from file, not really used when writing). 
	 * @param byteArray array of data for the data unit 
	 */
	public BinaryObjectData(int objectType, byte[] byteArray) {
		this.versionNumber = -1;
		this.objectType = objectType;
		this.data = byteArray;
		this.dataLength = byteArray.length;
	}


	/**
	 * @return the objectType
	 */
	public int getObjectType() {
		return objectType;
	}

	/**
	 * @return the data. Note that the data array may be longer than the
	 * actual data within it. Use getDataLength() to check.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Get a data input stream to read actual data from if needed. 
	 * @return null if no data or a data input stream. 
	 */
	public DataInputStream getDataInputStream() {
		if (data == null) {
			return null;
		}
		return new DataInputStream(new ByteArrayInputStream(data, 0, dataLength));
	}

	/**
	 * @return the dataLength
	 */
	public int getDataLength() {
		return dataLength;
	}


	/**
	 * @return the objectNumber
	 */
	public int getObjectNumber() {
		return objectNumber;
	}


	/**
	 * This is the FILE or NETWORK version number, not the module specific version number. For file
	 * data, this will be read out of the file header, for network data, it is the second word in 
	 * every TCP packet. The module specific version number is read for data units separately
	 * and is NOT included in this object. 
	 * @return the versionNumber
	 */
	public int getVersionNumber() {
		return versionNumber;
	}


	/**
	 * This is the FILE or NETWORK version number, not the module specific version number. For file
	 * data, this will be read out of the file header, for network data, it is the second word in 
	 * every TCP packet. The module specific version number is read for data units separately
	 * and is NOT included in this object. 
	 * @param versionNumber the versionNumber to set
	 */
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}


	public long getTimeMilliseconds() {
		if (dataUnitBaseData == null) {
			return 0;
		}
		else {
			return dataUnitBaseData.getTimeMilliseconds();
		}
	}


	/**
	 * @return the dataUnitBaseData
	 */
	public DataUnitBaseData getDataUnitBaseData() {
		if (dataUnitBaseData == null) {
			dataUnitBaseData = new DataUnitBaseData(0, 0);
		}
		return dataUnitBaseData;
	}


	/**
	 * Will return null if there is no annotation data
	 * @return the annotationData
	 */
	public byte[] getAnnotationData() {
		return annotationData;
	}


	/**
	 * @param annotationData the annotationData to set
	 */
	public void setAnnotationData(byte[] annotationData) {
		this.annotationData = annotationData;
		if (annotationData != null) {
			this.annotationDataLength = annotationData.length;
		}
		else {
			this.annotationDataLength = 0;
		}
	}

	/**
	 * @param annotationData the annotationData to set
	 * @param annotationDataLength length of annotation data (may not be using the full byte array annotationData)
	 */
	public void setAnnotationData(byte[] annotationData, int annotationDataLength) {
		this.annotationData = annotationData;
		this.annotationDataLength = annotationDataLength;
	}


	/**
	 * @return the annotationDataLength
	 */
	public int getAnnotationDataLength() {
		return annotationDataLength;
	}
	
	/**
	 * Shift the channels in the base data. Positive will 
	 * increase channel numbers, -ve reduce them. 
	 * @param shift channel shift
	 * @return new channel map
	 */
	public int shiftChannels(int shift) {
		if (dataUnitBaseData == null) {
			return 0;
		}
		int newChans = dataUnitBaseData.getChannelBitmap() << shift;
		dataUnitBaseData.setChannelBitmap(newChans);
		return newChans;
	}
	
	
}
