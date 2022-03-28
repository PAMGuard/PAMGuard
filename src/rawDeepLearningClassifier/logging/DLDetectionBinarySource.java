package rawDeepLearningClassifier.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import rawDeepLearningClassifier.dlClassification.DLDetectionDataBlock;
import rawDeepLearningClassifier.dlClassification.PredictionResult;

/**
 * Saves and loads binary data for classified Deep Learning data units. 
 * @author Jamie Macaulay 
 *
 */
public class DLDetectionBinarySource extends BinaryDataSource {


	/**
	 * <p>Module version changes</p>
	 * Version 2: Moved start sample, channel bitmap, duration to DataUnitBaseData general data structure<br>
	 */
	private static final int currentVersion = 2;

	private DLControl clipControl;

	private DLDetectionDataBlock clipDataBlock;

	public DLDetectionBinarySource(DLControl dlControl, DLDetectionDataBlock clipDataBlock) {
		super(clipDataBlock);
		this.clipControl = dlControl;
		this.clipDataBlock = clipDataBlock;
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
		return "DL_detection";
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

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;


	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		DLDetection cd = (DLDetection) pamDataUnit;
		
		//System.out.println("DLDetecitonBinarySource: packed: " +  pamDataUnit.getBasicData().getMeasuredAmplitudeType()); 
		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}

		try {
			writeProbData(dos, cd.getModelResults()); 
			writeWaveClip(dos, cd.getWaveData());
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//		getBinaryStorageStream().storeData(1, cd.getTimeMilliseconds(), bos.toByteArray());
		return new BinaryObjectData(1, bos.toByteArray());

	}


	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);

		DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
		//This is not stored in the base data. Probably sensible because it's the same number for every data unit. 
		//baseData.setMeasuredAmplitudeType(DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD); <- now in constructor. 
		//System.out.println("DLDetecitonBinarySource: sink: " +  baseData.getMeasuredAmplitudeType()); 
		
		//model results are loaded as annotations. 

		double[][] rawData = null;
		
		try {
			rawData = readWavClip(dis);

			bis.close();
		} catch (IOException e) {
			//			e.printStackTrace();
			System.err.println(e.getMessage());
			return null;
		}

		
		if (baseData.getChannelBitmap()==0) {
			baseData.setChannelBitmap(1);
		}
		DLDetection newUnit = new DLDetection(baseData, rawData);
		
		return newUnit;
	}

	/**
	 * Write the wave clip in scaled int8 format. 
	 * @param dos2
	 * @param rawData
	 * @throws IOException 
	 */
	private void writeWaveClip(DataOutputStream dos2, double[][] rawData) throws IOException {
		int nChan = rawData.length;
		int nSamps = rawData[0].length;
		double minVal = 0, maxVal = 0;
		for (int iC = 0; iC < nChan; iC++) {
			double[] chanData = rawData[iC];
			for (int iS = 0; iS < nSamps; iS++) {
				minVal = Math.min(minVal, chanData[iS]);
				maxVal = Math.max(maxVal, chanData[iS]);		
			}
		}
		maxVal = Math.max(maxVal, -minVal);
		float scale = (float) (127./maxVal);
		dos.writeShort(nChan);
		dos.writeInt(nSamps);
		dos.writeFloat(scale);
		for (int iC = 0; iC < nChan; iC++) {
			double[] chanData = rawData[iC];
			for (int iS = 0; iS < nSamps; iS++) {
				dos.writeByte((int) (chanData[iS] * scale));
			}
		}
	}

	/**
	 * Write the model result data. 
	 * @param dos2 - the model result data 
	 * @param arrayList - the model result data
	 */
	private void writeProbData(DataOutputStream dos2, ArrayList<PredictionResult> arrayList) {
		// TODO Auto-generated method stub

	}

	private double[][] readWavClip(DataInputStream dis) throws IOException {
		int nChan = dis.readShort();
		int nSamps = dis.readInt();
		double scale = 1./dis.readFloat();
		double[][] rawData = new double[nChan][nSamps];
		for (int iC = 0; iC < nChan; iC++) {
			double[] chanData = rawData[iC];
			for (int iS = 0; iS < nSamps; iS++) {
				chanData[iS] = (double) dis.readByte() * scale;
			}
		}
		return rawData;
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
