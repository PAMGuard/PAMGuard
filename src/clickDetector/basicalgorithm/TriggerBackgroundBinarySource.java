package clickDetector.basicalgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import Acquisition.AcquisitionProcess;
import PamUtils.PamUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class TriggerBackgroundBinarySource extends BinaryDataSource {

	private TriggerBackgroundHandler triggerBackgroundHandler;
	private TriggerBackgroundDataBlock triggerBackgroundDataBlock;
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	public TriggerBackgroundBinarySource(TriggerBackgroundHandler triggerBackgroundHandler, 
			TriggerBackgroundDataBlock triggerBackgroundDataBlock) {
		super(triggerBackgroundDataBlock);
		this.triggerBackgroundHandler = triggerBackgroundHandler;
		this.triggerBackgroundDataBlock = triggerBackgroundDataBlock;
	}

	@Override
	public String getStreamName() {
		return triggerBackgroundDataBlock.getDataName();
	}

	@Override
	public int getStreamVersion() {
		return 0;
	}

	@Override
	public int getModuleVersion() {
		return 0;
	}

	@Override
	public byte[] getModuleHeaderData() {
		/*
		 * Write the channel bitmap and the db gain of each channel to 
		 * header data. This won't ever get used here, but will be useful when 
		 * unpacking in Matlab (so long as calibration values haven't changed). 
		 */
		int chanMap = triggerBackgroundHandler.getClickControl().getClickParameters().getChannelBitmap();
		int nChan = PamUtils.getNumChannels(chanMap);
		int dataSize = nChan*4+4;
		/*
		 * Find the data source and get the calibration for each channel by feeding
		 * it an amplitude of 1. 
		 */
		PamProcess sourceProcess = triggerBackgroundHandler.getTriggerBackgroundDataBlock().getSourceProcess();
		if (sourceProcess instanceof AcquisitionProcess == false) {
			return null;
		}
		AcquisitionProcess daqProcess = (AcquisitionProcess) sourceProcess; 
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(dataSize);
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(chanMap);
			for (int i = 0; i < nChan; i++) {
				int chanInd = PamUtils.getNthChannel(i, chanMap);
				dos.writeFloat((float) daqProcess.rawAmplitude2dB(1.0, chanInd, false)); 
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return bos.toByteArray();
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#getPackedData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		TriggerBackgroundDataUnit tbdu = (TriggerBackgroundDataUnit) pamDataUnit;
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		double maxVal = tbdu.getMaxValue();
		double scale;
		if (maxVal > 0) {
			scale = (float) (32767./maxVal);			
		}
		else {
			scale = 1.;
		}
		/*
		 * Pretty minimilist write since channel map will already be stored in the
		 * standard header and data.length must match the channel map. 
		 */
		try {
			dos.writeFloat((float) scale);
			double[] data = tbdu.getBackgroundData();
			for (int i = 0; i < data.length; i++) {
				dos.writeShort((short) (scale*data[i]));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		BinaryObjectData packedData = new BinaryObjectData(0, bos.toByteArray());
		return packedData;
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#sinkData(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader, int)
	 */
	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
		int channelMap = baseData.getChannelBitmap();
		int nChan = PamUtils.getNumChannels(channelMap);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(binaryObjectData.getData()));
		double[] data = new double[nChan];
		try {
			double scale = dis.readFloat();
			for (int i = 0; i < nChan; i++) {
				data[i] = (double) dis.readShort() / scale;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new TriggerBackgroundDataUnit(baseData, data);
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#sinkModuleHeader(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader)
	 */
	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#sinkModuleFooter(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader, binaryFileStorage.ModuleHeader)
	 */
	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newFileOpened(File outputFile) {
		
	}

}
