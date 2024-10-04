package AIS;

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

public class AISBinaryDataSource extends BinaryDataSource {

	private AISDataBlock aisDataBlock;
	
	private AISControl aisControl;
	
	public AISBinaryDataSource(AISControl aisControl, AISDataBlock sisterDataBlock) {
		super(sisterDataBlock);
		this.aisControl = aisControl;
		this.aisDataBlock = sisterDataBlock;
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
	public String getStreamName() {
		return aisControl.getUnitName();
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
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		AISDataUnit aisDataUnit = (AISDataUnit) pamDataUnit;

		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		try {
			dos.writeInt(aisDataUnit.mmsiNumber);
			dos.writeShort(aisDataUnit.fillBits);
			dos.writeUTF(aisDataUnit.charData);
			dos.writeUTF(aisDataUnit.aisChannel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		int mmsiNumber = 0;
		int fillBits = 0;
		String charData;
		String aisChannel;
//		if (true) return null;

		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
		int writtenVersion = binaryObjectData.getVersionNumber();
		
		try {
			mmsiNumber = dis.readInt();
			fillBits = dis.readShort();
			charData = dis.readUTF();
			if (writtenVersion >= 1) {
				aisChannel = dis.readUTF();
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		AISDataUnit aisDataUnit = new AISDataUnit(binaryObjectData.getTimeMilliseconds(),
				charData, fillBits);
		if (aisDataUnit.decodeMessage()) {
			aisDataBlock.addAISData(aisDataUnit);
			return null; // stop PAMGUARD from adding the same data again
			/**
			 * A consequence of this is that the data unit will not have any file information
			 * attached to it - but OK since these data will never be resaved. 
			 */
		}
		else {
			return null;
		}
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
