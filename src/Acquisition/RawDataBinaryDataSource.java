package Acquisition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import binaryFileStorage.PackedBinaryObject;

/**
 * Data source for Raw acoustic data. This is set up to NEVER 
 * be written to binary storage files - WAV files are way better
 * for that. The purpose of this class is to enable sending of 
 * raw audio data over the network. Currently assumes 16 bit audio, 
 * but may make this more flexible in the future is higher res ADC's 
 * are in use within PAMGUARD. 
 * @author Doug Gillespie
 *
 */
public class RawDataBinaryDataSource extends BinaryDataSource {


	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	private long offsetSamples;
	
	public RawDataBinaryDataSource(PamRawDataBlock sisterDataBlock) {
		super(sisterDataBlock);
		setDoBinaryStore(false);
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getModuleVersion() {
		return 0;
	}

	@Override
	public String getStreamName() {
		return "Raw Data";
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

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		/**
		 * Pack a raw data unit. N.B. Raw data units in PAMGAURD only contain a single 
		 * channel of data. The channel number will need to be packed into the header. 
		 */
		RawDataUnit rawDataUnit = (RawDataUnit) pamDataUnit;
		int duration = rawDataUnit.getSampleDuration().intValue();
		int nBytes = 2;
		double scale = Math.pow(2, 8*nBytes-1)-1;
		int totalLen = duration * nBytes + 8 + 4 + 4 + 1;
		double[] rawData = rawDataUnit.getRawData();
		if (dos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream(totalLen));
		}
		else {
			bos.reset();
		}
		try {
			dos.writeLong(rawDataUnit.getStartSample());
			dos.writeInt(rawDataUnit.getChannelBitmap());
			dos.writeInt(duration);
			dos.writeByte(nBytes);
			for (int i = 0; i < duration; i++) {
				dos.writeShort((int) (rawData[i]*scale));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new BinaryObjectData(0, bos.toByteArray());
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		long startSample;
		int channelMap;
		int duration;
		byte nBytes;
		double scale;
		double[] rawData;
		try {
			startSample = dis.readLong();
			/*
			 * Muck a bit with the timing so that within this network received 
			 * data, sample counts get restarted at 0. This will ensure that 
			 * millisecond and ADC count timings end up about right for the data. 
			 */
			if (offsetSamples == 0) {
				offsetSamples = startSample;
			}
			startSample -= offsetSamples; // subtract off the offset from the sample number. 
			
			channelMap = dis.readInt();
			duration = dis.readInt();
			nBytes = dis.readByte();
			scale = Math.pow(2, 8*nBytes-1)-1;
//			System.out.println(String.format("Upack raw dataLen: %d, duration: %d, nBytes: %d",
//					binaryObjectData.getDataLength(), duration, nBytes));
			rawData = new double[duration];
			for (int i = 0; i < duration; i++) {
				rawData[i] = dis.readShort()/scale;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		RawDataUnit rawDataUnit = new RawDataUnit(binaryObjectData.getTimeMilliseconds(), channelMap, startSample, duration);
		rawDataUnit.setRawData(rawData, true);		
//		System.out.println("raw data arrived to unpack on channels " + 
//				channelMap + " sample " + startSample);
		return rawDataUnit;
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

	@Override
	public void reset() {
		super.reset();
		offsetSamples = 0L;
	}

}
