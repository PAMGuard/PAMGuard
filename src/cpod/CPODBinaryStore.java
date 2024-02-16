package cpod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataTransforms;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import cpod.CPODClick.CPODWaveTransforms;

/**
 * Binary storage for CPOD data. 
 * <p>
 * Seems a little silly to re create a new file format for CPOD data but by storing 
 * as binary files we can take advantage of the PAMGuard datagram, UID manager, super detections, a
 * annotations etc. without creating a very hacky module. 
 * 
 * @author Jamie Macauly
 *
 */
public class CPODBinaryStore extends BinaryDataSource {




	/**
	 * <p>Module version changes</p>
	 * Version 2: Moved start sample, channel bitmap, duration to DataUnitBaseData general data structure<br>
	 * Version 3: Implemented FPOD data which includes waveforms<br>
	 */
	private static final int currentVersion = 3;

	/**
	 * Reference to the CPOD control. 
	 */
	private CPODControl2 cpodControl;

	private CPODClickDataBlock cpodDataBlock;

	public CPODBinaryStore(CPODControl2 cpodControl2, CPODClickDataBlock clipDataBlock) {
		super(clipDataBlock);
		this.cpodControl = cpodControl2;
		this.cpodDataBlock = clipDataBlock;
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
		return "CPOD_clicks";
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
		CPODClick cd = (CPODClick) pamDataUnit;

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
			writeCPODdata3(dos, cd); 
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//		getBinaryStorageStream().storeData(1, cd.getTimeMilliseconds(), bos.toByteArray());
		return new BinaryObjectData(1, bos.toByteArray());

	}
	
	
	private void writeCPODdata3(DataOutputStream dos2, CPODClick cd) throws IOException {

//		data[3]=nCyc;
//		data[4]=bw;
//		data[5]=kHz;
//		data[6]=endF;
//		data[7]=spl;
//		data[8]=slope;
		
		dos.writeShort(cd.getnCyc());
		dos.writeShort(cd.getBw());
		dos.writeShort(cd.getkHz());
		dos.writeShort(cd.getEndF());
		dos.writeShort(cd.getSpl());
		dos.writeShort(cd.getSlope());
		
		boolean hasWav = cd.getWaveData()!=null;
		dos.writeBoolean(cd.getWaveData()!=null);
		
		if (hasWav) {
			//the number of samples - note FPODs only have one sample
			dos.writeShort(cd.getWaveData()[0].length);
			
			//convert back to shorts using scale factor
			for (int i=0; i<cd.getWaveData()[0].length; i++) {
				dos.writeShort((short) (cd.getWaveData()[0][i]*FPODReader.WAV_SCALE_FACTOR));
			}
		}

	}


	@Deprecated
	private void writeCPODData2(DataOutputStream dos2, CPODClick cd) throws IOException {

		//CPOD has 8 bytes - FPOD has more
		dos.writeInt(cd.getRawData().length);
		for (int i=0; i<cd.getRawData().length; i++) {
			dos.writeShort(cd.getRawData()[i]);
		};

	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);

		DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
				
		if (moduleVersion==2)
		{
			//This is not stored in the base data. Probably sensible because it's the same number for every data unit. 
			//baseData.setMeasuredAmplitudeType(DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD); <- now in constructor. 
			//System.out.println("DLDetecitonBinarySource: sink: " +  baseData.getMeasuredAmplitudeType()); 

			//model results are loaded as annotations. 
			try {
				
				short[] cpodClick = readCPODData(dis);

				if (baseData.getChannelBitmap()==0) {
					baseData.setChannelBitmap(1);
				}
				CPODClick newUnit = CPODClick.makeClick(baseData, cpodClick);
				bis.close();

				return newUnit; 
			} catch (IOException e) {
				//			e.printStackTrace();
				System.err.println(e.getMessage());
				return null;
			}
		}
		
		else {
			try {
				
				//TODO - maybe include original time data?| 
				
				//Convenient way to make a click
				short[] data = new short[9]; 
				data[3]=dis.readShort();
				data[4]=dis.readShort();
				data[5]=dis.readShort();
				data[6]=dis.readShort();
				data[7]=dis.readShort();
				data[8]=dis.readShort();
				
				//make a basic click
				CPODClick click = CPODClick.makeClick(baseData, data);
				
				//now add more info if needed. 
				
				boolean wav = dis.readBoolean();
				
				if (wav) {
					int len = dis.readShort();
					int[] arr = new int[len];
					for (int i=0; i<arr.length ; i++) {
						arr[i]=dis.readShort();
					}
					
					double[] wavData = FPODReader.scaleWavData(arr);
					
					click.setWavData(new double[][] {wavData});
					click.setRawDataTransfroms(new CPODWaveTransforms(click)); 
				}
				
			
				bis.close();
				
				return click;

			} catch (IOException e) {
				//			e.printStackTrace();
				System.err.println(e.getMessage());
				return null;
			}
			
			
		}

	}


	/**
	 * Read raw data from a CPOD detection from version 2 of the binary file format. 
	 * @param dis - the data input stream. 
	 * @return
	 * @throws IOException 
	 */
	private short[] readCPODData(DataInputStream dis) throws IOException {
		short[] cpodRawData = new short[dis.readInt()];
		for (int i=0; i<cpodRawData.length; i++) {
			cpodRawData[i] =dis.readShort();
		};	
		return cpodRawData;
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
