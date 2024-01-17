package RightWhaleEdgeDetector;

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
import binaryFileStorage.PackedBinaryObject;

public class RWEBinaryDataSource extends BinaryDataSource {

	RWEDataBlock rweDataBlock;
	private RWEProcess rweProcess;
	/**
	 * <p>Module version changes</p>
	 * Version 0: original version<br>
	 * Version 1: Moved start sample and channel bitmap to DataUnitBaseData general data structure<br>
	 * Version 2: Don't read delays from main body, 
	 */
	private static final int currentVersion = 2;

	public RWEBinaryDataSource(RWEProcess rweProcess, RWEDataBlock rweDataBlock) {
		super(rweDataBlock);
		this.rweProcess = rweProcess;
		this.rweDataBlock = rweDataBlock;
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
		return "Edges";
	}

	@Override
	public int getStreamVersion() {
		return 0;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	@Override

	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit){
		RWEDataUnit rweDataUnit = (RWEDataUnit) pamDataUnit;
		RWESound aSound = rweDataUnit.rweSound;

		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}

		try {
//			dos.writeLong(rweDataUnit.getStartSample()); as of version 1, start sample included in DataUnitBaseData
//			dos.writeInt(rweDataUnit.getChannelBitmap());  as of version 1, channel bitmap included in DataUnitBaseData
			int nSlices = aSound.sliceCount;
			dos.writeShort(aSound.soundType);
			dos.writeFloat((float)aSound.signal);
			dos.writeFloat((float)aSound.noise);
			dos.writeShort(nSlices);
			for (int i = 0; i < nSlices; i++) {
				dos.writeShort(aSound.sliceList[i]);
				dos.writeShort(aSound.lowFreq[i]);
				dos.writeShort(aSound.peakFreq[i]);
				dos.writeShort(aSound.highFreq[i]);
				dos.writeFloat((float) aSound.peakAmp[i]);
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		if (getBinaryStorageStream() != null) {
//			getBinaryStorageStream().storeData(0, 
//					rweDataUnit.getTimeMilliseconds(),
//					bos.toByteArray());
//		}
		
		BinaryObjectData pbo = new BinaryObjectData(0, bos.toByteArray());

		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pbo;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		RWEDataUnit rweDataUnit;
		RWESound aSound = null;
		RWEDetectionPeak aPeak;

		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
		int fileVersion = binaryObjectData.getVersionNumber();

		long startSample;
		int channelMap;
		Integer sequenceMap=null;
		int soundType;
		double signal, noise;
		int nSlices;
		int iSlice, lowFreq, peakFreq, highFreq;
		double peakAmp;
		double[] delays = null;
		try {
//			dos.writeShort(aSound.soundType);
//			dos.writeFloat((float)aSound.signal);
//			dos.writeFloat((float)aSound.noise);
//			dos.writeShort(nSlices);
//			for (int i = 0; i < nSlices; i++) {
//				dos.writeShort(aSound.sliceList[i]);
//				dos.writeShort(aSound.lowFreq[i]);
//				dos.writeShort(aSound.peakFreq[i]);
//				dos.writeShort(aSound.highFreq[i]);
//				dos.writeFloat((float) aSound.peakAmp[i]);
//			}

			
			if (fileVersion <= 3) {
				startSample = dis.readLong();
				channelMap = dis.readInt();
				binaryObjectData.getDataUnitBaseData().setChannelBitmap(channelMap);
				binaryObjectData.getDataUnitBaseData().setStartSample(startSample);
			} else {
//				try {
					if (binaryObjectData.getDataUnitBaseData() == null) return null;
//				}
//				catch (NullPointerException e) {
//					DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
//					System.out.println(baseData);
//				}
				Long ss = binaryObjectData.getDataUnitBaseData().getStartSample();
				if (ss != null) {
					startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
				}
				else {
					startSample = 0;
				}
				channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
				sequenceMap = binaryObjectData.getDataUnitBaseData().getSequenceBitmap();
			}
			soundType = dis.readShort();
			signal = dis.readFloat();
			noise = dis.readFloat();
			nSlices = dis.readShort();
			for (int i = 0; i < nSlices; i++) {
				iSlice = dis.readShort();
				lowFreq = dis.readShort();
				peakFreq = dis.readShort();
				highFreq = dis.readShort();
				peakAmp = dis.readFloat();
				aPeak = new RWEDetectionPeak(lowFreq);
				aPeak.bin2 = highFreq;
				aPeak.peakBin = peakFreq;
				aPeak.maxAmp = peakAmp;
				if (i == 0) {
					aSound = new RWESound(binaryObjectData.getTimeMilliseconds(), aPeak, 0);
				}
				else {
					aSound.addPeak(iSlice, aPeak, 0);
				}
			}
			/**
			 * Bit complicated because when data come in from old files they 
			 * have delays here, but when they are converted with UID, they don't
			 */
			if (moduleVersion == 1 && bis.available() > Short.BYTES) {
				int nDelays = dis.readShort();
				if (nDelays > 0) {
					delays = new double[nDelays];
					for (int i = 0; i < nDelays; i++) {
						delays[i] = dis.readFloat();
					}
				}
			}
			aSound.completeSound();
			aSound.signal = signal;
			aSound.noise = noise;
			aSound.soundType = soundType;
			
		}catch (IOException e) {
//			e.printStackTrace();
			System.err.println("Error unpacking right whale binary data: " + e.getMessage());
			return null;
		}
		long duration = rweDataBlock.getFftHop() * aSound.duration;
		binaryObjectData.getDataUnitBaseData().setSampleDuration(duration);
//		rweDataUnit = new RWEDataUnit(aSound.timeMilliseconds, channelMap, 
//				startSample, duration, aSound);
		rweDataUnit = new RWEDataUnit(rweProcess, binaryObjectData.getDataUnitBaseData(), aSound);
		rweDataUnit.setSequenceBitmap(sequenceMap);
		double f[] = new double[2];
		f[0] = aSound.minFreq * rweDataBlock.getSampleRate()/rweDataBlock.getFftLength();
		f[1] = aSound.maxFreq * rweDataBlock.getSampleRate()/rweDataBlock.getFftLength();
		rweDataUnit.setFrequency(f);
		if (delays != null) {
			rweDataUnit.getBasicData().setTimeDelaysSeconds(delays);
		}
		rweProcess.calculateAngles(rweDataUnit);
//		rweDataUnit.getOriginLatLong(false);
		return rweDataUnit;
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
