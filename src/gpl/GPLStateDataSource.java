package gpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class GPLStateDataSource extends BinaryDataSource {

	private GPLStateDataBlock gplStateDataBlock;
	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	
	public GPLStateDataSource(GPLStateDataBlock gplStateDataBlock) {
		super(gplStateDataBlock);
		this.gplStateDataBlock = gplStateDataBlock;
	}

	@Override
	public String getStreamName() {
		return "GPL State";
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(binaryObjectData.getData()));
		try {
			double baseline = dis.readFloat();
			double ceilnoise = dis.readFloat();
			double threshfloor = dis.readFloat();;
			int state = dis.readShort();
			GPLStateDataUnit stateData = new GPLStateDataUnit(binaryObjectData.getDataUnitBaseData(), baseline, ceilnoise, threshfloor, state);
			return stateData;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		return null;
	}

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		if (bos == null) {
			bos = new ByteArrayOutputStream(14);
			dos = new DataOutputStream(bos);
		}
		else {
			bos.reset();
		}
		GPLStateDataUnit stateData = (GPLStateDataUnit) pamDataUnit;
		try {
			dos.writeFloat((float) stateData.getBaseline());
			dos.writeFloat((float) stateData.getCeilnoise());
			dos.writeFloat((float) stateData.getThreshfloor());
			dos.writeShort((short) stateData.getPeakState());
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		BinaryObjectData bod = new BinaryObjectData(1, bos.toByteArray());
		
		return bod;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

}
