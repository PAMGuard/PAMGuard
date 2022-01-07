package noiseMonitor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamController.PamControlledUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryInputStream;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import binaryFileStorage.PackedBinaryObject;

public class NoiseBinaryDataSource extends BinaryDataSource {

	private NoiseDataBlock noiseDataBlock;

	private PamControlledUnit pamControlledUnit;

	public NoiseBinaryDataSource(PamControlledUnit pamControlledUnit, NoiseDataBlock noiseDataBlock) {
		super(noiseDataBlock);
		this.pamControlledUnit = pamControlledUnit;
		this.noiseDataBlock = noiseDataBlock;
	}

	@Override
	public int getModuleVersion() {
		/**
		 * Version 0 - four measures as standard, number of measures NOT written to file.
		 * Version 1 - six measures as standard, number of measures written to file.  
		 * Also deals with data from the new filter based noise band monitor which only
		 * has two measures. 
		 * Version 2 - save space by writing data as (short) data*100 which will give 0.01dB
		 * resolution and a range of up to 327.67db, which should be plenty !
		 */
		return 2;
	}

	@Override
	public byte[] getModuleHeaderData() {
		/*
		 * Write out a module header which contains the following information
		 * (for version 1)
		 * <p>int16 number of bands
		 * <p>int16 bitmap of which bands are used
		 * <p>float[] list of low frequency edges
		 * <p>float[] list of high frequency edges 
		 * 
		 */
		ByteArrayOutputStream baos;
		DataOutputStream ds = new DataOutputStream(baos = new ByteArrayOutputStream());
		double[] loEdges = noiseDataBlock.bandLoEdges;
		double[] hiEdges = noiseDataBlock.bandHiEdges;
		try {
			ds.writeShort(hiEdges.length);
			ds.writeShort(noiseDataBlock.getStatisticTypes());
			for (int i = 0; i < loEdges.length; i++) {
				ds.writeFloat((float) loEdges[i]);
			}
			for (int i = 0; i < hiEdges.length; i++) {
				ds.writeFloat((float) hiEdges[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] retArray = baos.toByteArray();
		try {
			ds.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retArray;
	}


	@Override
	public String getStreamName() {
		return pamControlledUnit.getUnitName();
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	@Override

	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {

		NoiseDataUnit ndu = (NoiseDataUnit) pamDataUnit;
		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		double[][] noiseData = ndu.getNoiseBandData();
		if (noiseData == null || noiseData.length == 0) {
			return null;
		}
		int iChan = PamUtils.getSingleChannel(ndu.getChannelBitmap());
		int nBands = noiseData.length;
		int nMeas = noiseData[0].length;
		try {
			dos.writeShort(iChan);
			dos.writeShort(nBands);
			dos.writeShort(nMeas);
			for (int iB = 0; iB < nBands; iB++) {
				for (int iM = 0; iM < nMeas; iM++) {
//					dos.writeFloat((float) noiseData[iB][iM]); // version 0 or 1
					dos.writeShort((short) (noiseData[iB][iM]*100.)); // version 2
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		getBinaryStorageStream().storeData(1, ndu.getTimeMilliseconds(), bos.toByteArray());
		BinaryObjectData pbo = new BinaryObjectData(1, bos.toByteArray());
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
		
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
		int iChan = 0;
		int nBands;
		int nMeas = 4;
		double[][] noiseData;
		try {
			iChan = dis.readShort();
			nBands = dis.readShort();
			if (moduleVersion >= 1) {
				nMeas = dis.readShort();
			}
			noiseData = new double[nBands][nMeas];
			if (moduleVersion <= 1) {
				for (int iB = 0; iB < nBands; iB++) {
					for (int iM = 0; iM < nMeas; iM++) {
						noiseData[iB][iM] = dis.readFloat();
					}
				}
			}
			else if (moduleVersion == 2) {
				for (int iB = 0; iB < nBands; iB++) {
					for (int iM = 0; iM < nMeas; iM++) {
						noiseData[iB][iM] = (double) dis.readShort() / 100.;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		NoiseDataUnit ndu = new NoiseDataUnit(binaryObjectData.getTimeMilliseconds(),
				1<<iChan, 0, 0);
		ndu.setNoiseBandData(noiseData);
		return ndu;
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
		if (binaryObjectData.getDataLength() == 0) {
			return null;
		}
		ByteArrayInputStream bas = new ByteArrayInputStream(binaryObjectData.getData());
		DataInputStream dis = new DataInputStream(bas);
		int nBands;
		int selectedStats;
		double[] loEdges;
		double[] hiEdges;
		try {
			nBands = dis.readShort();
			selectedStats = dis.readShort();
			loEdges = new double[nBands];
			hiEdges = new double[nBands];
			for (int i = 0; i < nBands; i++) {
				loEdges[i] = dis.readFloat();
			}
			for (int i = 0; i < nBands; i++) {
				hiEdges[i] = dis.readFloat();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
