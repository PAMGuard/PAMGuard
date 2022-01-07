package PamguardMVC.background;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryTypes;

public class SpecBackgroundWriter extends BackgroundBinaryWriter<SpecBackgroundDataUnit> {

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	public SpecBackgroundWriter(BackgroundManager<SpecBackgroundDataUnit> backgroundManager) {
		super(backgroundManager);
	}

	@Override
	public BinaryObjectData packBackgroundData(SpecBackgroundDataUnit backgroundUnit) {
		if (bos == null) {
			bos = new ByteArrayOutputStream(2048);
			dos = new DataOutputStream(bos);
		}
		else {
			bos.reset();
		}
		try {
			dos.writeInt(backgroundUnit.getLoBin());
			double[] data = backgroundUnit.getData();
			dos.writeInt(data.length);
			for (int i = 0; i < data.length; i++) {
				dos.writeFloat((float) data[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BinaryObjectData(BinaryTypes.BACKGROUND_DATA, bos.toByteArray());
	}

	@Override
	public SpecBackgroundDataUnit unpackBackgroundData(BinaryObjectData binaryObjectData, BinaryHeader bh,
			int moduleVersion) {

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(binaryObjectData.getData()));
		
		try {
			int loBin = dis.readInt();
			int dataLen = dis.readInt();
			double[] data = new double[dataLen];
			for (int i = 0; i < dataLen; i++) {
				data[i] = dis.readFloat();
			}
			return new SpecBackgroundDataUnit(binaryObjectData.getDataUnitBaseData(), loBin, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
