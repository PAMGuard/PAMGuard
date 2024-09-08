package noiseOneBand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class OneBandDataSource extends BinaryDataSource {

	private OneBandDataBlock oneBandDataBlock;
	private OneBandControl oneBandControl;
	private String streamName;
	
	/**
	 * <p>Module version changes</p>
	 * Version 1: <br>
	 * Version 2: Added SEL and SEL Integration Time<br>
	 * Version 3: Moved start sample and channel bitmap to DataUnitBaseData general data structure<br>
	 */
	private static final int MODULEVERSION = 3;
	
	public OneBandDataSource(OneBandControl oneBandControl, OneBandDataBlock oneBandDataBlock, String streamName) {
		super(oneBandDataBlock);
		this.oneBandControl = oneBandControl;
		this.oneBandDataBlock = oneBandDataBlock;
		this.streamName = streamName;
	}

	@Override
	public byte[] getModuleHeaderData() {
		return null;
	}

	@Override
	public int getModuleVersion() {
		return MODULEVERSION;
	}

	@Override
	public String getStreamName() {
		return streamName;
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public void newFileOpened(File outputFile) {

	}

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	
	@Override

	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		OneBandDataUnit du = (OneBandDataUnit) pamDataUnit;

		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		/**
		 * dB levels should never be more than 100 or so. 
		 * To save space for PAMBUOY, will write dB values as int16
		 * data, scaled up by 100, so max dB scale is between -327.68 and + 327.67 dB
		 */
		try {
//			dos.writeLong(du.getStartSample()); as of version 3, start sample included in DataUnitBaseData
//			dos.writeInt(du.getChannelBitmap()); as of version 3, channel bitmap included in DataUnitBaseData
			dos.writeShort((int) Math.round(du.getRms()*100.));
			dos.writeShort((int) Math.round(du.getZeroPeak()*100.));
			dos.writeShort((int) Math.round(du.getPeakPeak()*100.));
			dos.writeShort((int) Math.round(du.getIntegratedSEL()*100.));
			dos.writeShort((int) Math.round(du.getSelIntegationTime()));
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

//		getBinaryStorageStream().storeData(1, du.getTimeMilliseconds(), bos.toByteArray());
		return new BinaryObjectData(1, bos.toByteArray());
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
		int channelMap;
		long startSample;
		double rms, zp, pp, sel=0;
		int selSecs=0;
		try {
			if (moduleVersion<3) {
				startSample = dis.readLong();
				channelMap = dis.readInt();
			} else {
				startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
				channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
			}
			rms = dis.readShort();
			zp = dis.readShort();
			pp = dis.readShort();
			if (moduleVersion >= 2) {
				sel = dis.readShort();
				selSecs = dis.readShort();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		
		long duration = (long) (oneBandControl.oneBandParameters.measurementInterval * oneBandControl.getOneBandProcess().getSampleRate());
		
		OneBandDataUnit du = new OneBandDataUnit(binaryObjectData.getTimeMilliseconds(), channelMap, startSample, duration);
		du.setRms(rms/100.);
		du.setZeroPeak(zp/100.);
		du.setPeakPeak(pp/100.);
		if (moduleVersion >= 2) {
			du.setSEL(sel/100., selSecs);
		}
		
		return du;
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
