package cpod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

/**
 * Binary storage for CPOD data. 
 * <p>
 * Seems a little silly to re create a new file format for CPOD data but by storing 
 * as binary files we can take advantage of the PAMGuard datagram, UID manager, super detections, a
 * annotations etc. without creating a very hackey module. 
 * 
 * @author Jamie Macauly
 *
 */
public class CPODBinaryStore extends BinaryDataSource {


	/**
	 * <p>Module version changes</p>
	 * Version 2: Moved start sample, channel bitmap, duration to DataUnitBaseData general data structure<br>
	 */
	private static final int currentVersion = 2;

	/**
	 * Reference to the CPOD control. 
	 */
	private CPODControl2 cpodControl;

	private CPODClickDataBlock cpodDataBlock;

	public CPODBinaryStore(CPODControl2 cpodControl2, CPODClickDataBlock clipDataBlock) {
		super(clipDataBlock);
		this.cpodControl = cpodControl2;
		this.cpodDataBlock = clipDataBlock;
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getModuleVersion() {
		return currentVersion;
	}

	@Override
	public String getStreamName() {
		return "CPOD_clicks";
	}

	@Override
	public int getStreamVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;


	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		CPODClick cd = (CPODClick) pamDataUnit;

		//System.out.println("DLDetecitonBinarySource: packed: " +  pamDataUnit.getBasicData().getMeasuredAmplitudeType()); 


		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}

		try {
			writeCPODData(dos, cd); 
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//		getBinaryStorageStream().storeData(1, cd.getTimeMilliseconds(), bos.toByteArray());
		return new BinaryObjectData(1, bos.toByteArray());

	}


	private void writeCPODData(DataOutputStream dos2, CPODClick cd) throws IOException {
		
		//CPOD has 8 bytes - FPOD has more
		dos.writeInt(cd.getRawData().length);
		for (int i=0; i<cd.getRawData().length; i++) {
			dos.writeShort(cd.getRawData()[i]);
		};
		
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);

		DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
		//This is not stored in the base data. Probably sensible because it's the same number for every data unit. 
		//baseData.setMeasuredAmplitudeType(DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD); <- now in constructor. 
		//System.out.println("DLDetecitonBinarySource: sink: " +  baseData.getMeasuredAmplitudeType()); 

		//model results are loaded as annotations. 

		try {
			short[] cpodClick = readCPODData(dis);

			if (baseData.getChannelBitmap()==0) {
				baseData.setChannelBitmap(1);
			}
			CPODClick newUnit = CPODClick.makeClick(baseData, cpodClick);
			bis.close();

			return newUnit; 
		} catch (IOException e) {
			//			e.printStackTrace();
			System.err.println(e.getMessage());
			return null;
		}

	}


	/**
	 * Read raw data from a CPOD detection. 
	 * @param dis - the data input stream. 
	 * @return
	 * @throws IOException 
	 */
	private short[] readCPODData(DataInputStream dis) throws IOException {
		short[] cpodRawData = new short[dis.readInt()];
		for (int i=0; i<cpodRawData.length; i++) {
			cpodRawData[i] =dis.readShort();

		};	
		return cpodRawData;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData,
			BinaryHeader bh, ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData,
			BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

}
