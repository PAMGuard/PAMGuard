package clickDetector.background;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import PamUtils.PamUtils;
import PamguardMVC.background.BackgroundBinaryWriter;
import PamguardMVC.background.BackgroundDataUnit;
import PamguardMVC.background.BackgroundManager;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryTypes;

public class ClickBackgroundBinaryWriter extends BackgroundBinaryWriter<ClickBackgroundDataUnit> {

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	
	public ClickBackgroundBinaryWriter(BackgroundManager backgroundManager) {
		super(backgroundManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BinaryObjectData packBackgroundData(ClickBackgroundDataUnit backgroundUnit) {
		if (bos == null) {
			bos = new ByteArrayOutputStream(2048);
			dos = new DataOutputStream(bos);
		}
		else {
			bos.reset();
		}
		try {
			double[] bgData = backgroundUnit.getLevels();
			dos.writeShort(bgData.length);
			for (int i = 0; i < bgData.length; i++) {
				dos.writeFloat((float) bgData[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BinaryObjectData(BinaryTypes.BACKGROUND_DATA, bos.toByteArray());
	}

	@Override
	public ClickBackgroundDataUnit unpackBackgroundData(BinaryObjectData binaryObjectData, BinaryHeader bh,
			int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData());
		int chanMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
		int nChan = PamUtils.getNumChannels(chanMap);
		int totSize = Short.BYTES + Float.BYTES*nChan;
		if (totSize != binaryObjectData.getDataLength()) {
			System.out.printf("Background data size warning. Have %d bytes, expecting %d for %d channels\n", 
					binaryObjectData.getDataLength(), totSize, nChan);
			return null;
		}
		DataInputStream dis = new DataInputStream(bis);
		double[] data = new double[nChan];

		try {
			int n = dis.readShort();
			for (int i = 0; i < n; i++) {
				data[i] = dis.readFloat();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new ClickBackgroundDataUnit(binaryObjectData.getDataUnitBaseData(), data);
	}

}
