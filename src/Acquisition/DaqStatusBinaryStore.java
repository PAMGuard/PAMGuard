package Acquisition;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import Array.Preamplifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import binaryFileStorage.PackedBinaryObject;

public class DaqStatusBinaryStore extends BinaryDataSource {

	private PamDataBlock<DaqStatusDataUnit> daqStatusDataBlock;
	private AcquisitionControl acquisitionControl;
	private DaqStatusModuleHeader moduleHeader;

	public DaqStatusBinaryStore(PamDataBlock<DaqStatusDataUnit> daqStatusDataBlock, AcquisitionControl acquisitionControl) {
		super(daqStatusDataBlock, false);
		this.daqStatusDataBlock = daqStatusDataBlock;
		this.acquisitionControl= acquisitionControl;
	}

	@Override
	public String getStreamName() {
		return "DAQStatus";
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public int getModuleVersion() {
		return 1;
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
		long adcMilliseconds = 0;
		long samples = 0; 
		Long gpsPPSMillis = null;
		short status = 0;
		short reason = 0;
		float clockError = 0;
		String systemName = null;

		try {
			adcMilliseconds = dis.readLong();
			samples = dis.readLong();
			gpsPPSMillis = dis.readLong();
			if (gpsPPSMillis == 0) {
				// zero must be intrpreted as null (no data) or it can screw up some time corrections in the
				// network receiver. 
				gpsPPSMillis = null;
			}
			status = dis.readShort();
			reason = dis.readShort();
			clockError = dis.readFloat();
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String daqSystemType = "unknown";
		float sampleRate = 0;
		int channels = 0;
		double voltsPeak2Peak = 0;
		double duration = 0;
		if (moduleHeader != null) {
			daqSystemType = moduleHeader.daqName;
			sampleRate = moduleHeader.sampleRate;
			channels = moduleHeader.channelMap;
			voltsPeak2Peak = moduleHeader.voltsPeakToPeak;
			duration = samples / sampleRate;
		}
		else {
			// in NetRX mode will need to take this from the configuration
			daqSystemType = acquisitionControl.acquisitionParameters.daqSystemType;
			sampleRate = acquisitionControl.acquisitionParameters.getSampleRate();
			channels = PamUtils.PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels);
			voltsPeak2Peak = acquisitionControl.acquisitionParameters.voltsPeak2Peak;
		}
		
		AcquisitionParameters daqParams = new AcquisitionParameters();
		daqParams.setVoltsPeak2Peak(voltsPeak2Peak);
		daqParams.setPreamplifier(new Preamplifier(0, null));
		daqParams.setSampleRate(sampleRate);
		daqParams.setDaqSystemType(daqSystemType);
		/**
		 * Only used in netrx mode and doesn't handle corrected milliseconds. 
		 */
		DaqStatusDataUnit dsdu = new DaqStatusDataUnit(binaryObjectData.getTimeMilliseconds(), 
				adcMilliseconds, adcMilliseconds, samples, gpsPPSMillis, (new Short(status)).toString(), 
				(new Short(reason)).toString(), daqParams, null, duration, clockError);
		return dsdu;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		moduleHeader = new DaqStatusModuleHeader(bh.getHeaderFormat());
		try {
			moduleHeader.daqName = dis.readUTF();
			moduleHeader.sampleRate = dis.readFloat();
			moduleHeader.channelMap = dis.readInt();
			moduleHeader.voltsPeakToPeak = dis.readFloat();
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return moduleHeader;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}


}
