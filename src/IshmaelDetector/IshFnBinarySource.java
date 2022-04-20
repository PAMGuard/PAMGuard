package IshmaelDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;


/**
 * Binary data for peak binary data source. 
 * 
 * @author Jamie Macaulay
 *
 */
public class IshFnBinarySource extends BinaryDataSource {

	
	/**
	 * Stream name
	 */
	private String streamName;

	/**
	 * Reference to the peak process. 
	 */
	private IshDetFnProcess ishDetFnProcess;
	
	private static final int currentVersion = 1;
	
	public IshFnBinarySource(IshDetFnProcess ishDetFnProcess, PamDataBlock sisterDataBlock, String streamName) {
		super(sisterDataBlock);
		this.streamName=streamName; 
		this.ishDetFnProcess = ishDetFnProcess;
	}

	@Override
	public String getStreamName() {
		return "Ishmael Peak Data";
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public int getModuleVersion() {
		return currentVersion;
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
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

		try {
			int nDet = dis.readInt(); 

			double[][] detData = new double[nDet][]; 
			for (int i=0; i<nDet; i++) {
				//the first double is peak, the second is noise. 
				detData[i] = new double[] {dis.readDouble(), dis.readDouble()};
			}

			if (baseData.getChannelBitmap()==0) {
				baseData.setChannelBitmap(1);
			}

			IshDetFnDataUnit newUnit = new IshDetFnDataUnit(baseData, detData);

			bis.close();

			return newUnit;

		} catch (IOException e) {
			//			e.printStackTrace();
			System.err.println(e.getMessage());
			return null;
		}
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}
	

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		IshDetFnDataUnit ishDet = (IshDetFnDataUnit) pamDataUnit;

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
			dos.writeInt(ishDet.getDetData().length);
			for (int i=0; i<ishDet.getDetData().length; i++) {
				dos.writeDouble(ishDet.getDetData()[i][0]);
				dos.writeDouble(ishDet.getDetData()[i][1]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//		getBinaryStorageStream().storeData(1, cd.getTimeMilliseconds(), bos.toByteArray());
		return new BinaryObjectData(1, bos.toByteArray());
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

}
