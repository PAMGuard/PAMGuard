package clipgenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataUtils;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class ClipBinaryDataSource extends BinaryDataSource {

	private ClipControl clipControl;
	private ClipDisplayDataBlock clipDataBlock;
	
	private RawDataUtils rawDataUtils = new RawDataUtils();
	
	/**
	 * <p>Module version changes</p>
	 * Version 2: Moved start sample, channel bitmap, duration to DataUnitBaseData general data structure<br>
	 */
	private static final int currentVersion = 3;

	public ClipBinaryDataSource(ClipControl clipControl, ClipDisplayDataBlock clipDataBlock) {
		super(clipDataBlock);
		this.clipControl = clipControl;
		this.clipDataBlock = clipDataBlock;
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
		return "Clips";
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
		ClipDataUnit cd = (ClipDataUnit) pamDataUnit;
	
		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		int storageOption = clipControl.clipSettings.storageOption;
		if (cd.getRawData() == null) {
			storageOption = ClipSettings.STORE_WAVFILES;
		}
		try {
//			dos.writeLong(cd.getStartSample()); as of version 2, start sample included in DataUnitBaseData
//			dos.writeInt(cd.getChannelBitmap()); as of version 2, channel bitmap included in DataUnitBaseData
			dos.writeLong(cd.triggerMilliseconds);
//			dos.writeInt(cd.getSampleDuration().intValue()); as of version 2, duration included in DataUnitBaseData
			dos.writeUTF(cd.fileName);
			dos.writeUTF(cd.triggerName);
			// version 3 - also write the UId of the trigger data unit. then easy to find. 
			PamDataUnit trigUnit = cd.getTriggerDataUnit();
			dos.writeLong(trigUnit == null ? 0 : trigUnit.getUID());
			
			if (storageOption == ClipSettings.STORE_BINARY || storageOption == ClipSettings.STORE_BOTH) {
				writeWaveClip(dos, cd.getRawData());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
//		getBinaryStorageStream().storeData(1, cd.getTimeMilliseconds(), bos.toByteArray());
		return new BinaryObjectData(storageOption + 1, bos.toByteArray());

	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
		long startSample;
		int channelMap;
		long triggerMillis;
		int duration;
		String fileName;
		String triggerName;
		long trigUID = 0;
		
		int storageOption = binaryObjectData.getObjectType() - 1;
		double[][] rawData = null;
		try {
			if (moduleVersion<=1) {
				startSample = dis.readLong();
				channelMap = dis.readInt();
			} else {
				startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
				channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
			}
			triggerMillis = dis.readLong();
			if (moduleVersion<=1) {
				duration = dis.readInt();
			} else {
				duration = binaryObjectData.getDataUnitBaseData().getSampleDuration().intValue();
			}
			fileName = dis.readUTF();
			triggerName = dis.readUTF();
			if (moduleVersion >= 3) {
				trigUID = dis.readLong();
			}
			if (storageOption == ClipSettings.STORE_BINARY || storageOption == ClipSettings.STORE_BOTH) {
				rawData = readWavClip(dis);
			}
			
			bis.close();
		} catch (IOException e) {
//			e.printStackTrace();
			System.err.println(e.getMessage());
			return null;
		}
		
		ClipDataUnit newUnit = new ClipDataUnit(binaryObjectData.getTimeMilliseconds(), triggerMillis, startSample, 
				duration, channelMap, fileName, triggerName, rawData, clipDataBlock.getSampleRate());
		newUnit.setTriggerUID(trigUID);
		return newUnit;
	}

	/**
	 * Write the wave clip in scaled int8 format. 
	 * @param dos2
	 * @param rawData
	 * @throws IOException 
	 */
	private void writeWaveClip(DataOutputStream dos, double[][] rawData) throws IOException {
		rawDataUtils.writeWaveClipInt8(dos, rawData);
//		int nChan = rawData.length;
//		int nSamps = rawData[0].length;
//		double minVal = 0, maxVal = 0;
//		for (int iC = 0; iC < nChan; iC++) {
//			double[] chanData = rawData[iC];
//			for (int iS = 0; iS < nSamps; iS++) {
//				minVal = Math.min(minVal, chanData[iS]);
//				maxVal = Math.max(maxVal, chanData[iS]);		
//			}
//		}
//		maxVal = Math.max(maxVal, -minVal);
//		float scale = (float) (127./maxVal);
//		dos.writeShort(nChan);
//		dos.writeInt(nSamps);
//		dos.writeFloat(scale);
//		for (int iC = 0; iC < nChan; iC++) {
//			double[] chanData = rawData[iC];
//			for (int iS = 0; iS < nSamps; iS++) {
//				dos.writeByte((int) (chanData[iS] * scale));
//			}
//		}
	}

	private double[][] readWavClip(DataInputStream dis) throws IOException {
//		int nChan = dis.readShort();
//		int nSamps = dis.readInt();
//		double scale = 1./dis.readFloat();
//		double[][] rawData = new double[nChan][nSamps];
//		for (int iC = 0; iC < nChan; iC++) {
//			double[] chanData = rawData[iC];
//			for (int iS = 0; iS < nSamps; iS++) {
//				chanData[iS] = (double) dis.readByte() * scale;
//			}
//		}
//		return rawData;
		return rawDataUtils.readWavClipInt8(dis);
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
