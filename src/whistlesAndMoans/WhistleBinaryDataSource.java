package whistlesAndMoans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import whistlesAndMoans.WhistleToneConnectProcess.ShapeConnector;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import binaryFileStorage.PackedBinaryObject;

public class WhistleBinaryDataSource extends BinaryDataSource {

	private String streamName;
	
	public static final int WHISTLE_MOAN_DETECTION = 2000;
	
	private static final int currentVersion = 2;
	
	private WhistleToneConnectProcess wmDetector;
	
	private int runMode;

	public WhistleBinaryDataSource(WhistleToneConnectProcess wmDetector, PamDataBlock sisterDataBlock, String streamName) {
		super(sisterDataBlock);
		this.wmDetector = wmDetector;
		this.streamName = streamName;
		runMode = PamController.getInstance().getRunMode();
	}

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	private DataOutputStream headerOutputStream;

	private ByteArrayOutputStream headerBytes;

	private int delayScale = 0;
	
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		ConnectedRegionDataUnit crdu = (ConnectedRegionDataUnit) pamDataUnit;
		ConnectedRegion cr = crdu.getConnectedRegion();

		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		if (delayScale == 0) {
			delayScale = wmDetector.getDelayScale();
		}
		List<SliceData> sliceDataList;
//		SliceData sliceData;
		int[][] peakInfo;
		try {
//			dos.writeLong(crdu.getStartSample()); as of version 2, start sample included in DataUnitBaseData
//			dos.writeInt(crdu.getChannelBitmap()); as of version 2, channel bitmap included in DataUnitBaseData
			int nSlices = cr.getNumSlices();
			dos.writeShort(nSlices);
			dos.writeShort((int) (crdu.getAmplitudeDB() * 100));
			sliceDataList = cr.getSliceData();
			for (SliceData sliceData : sliceDataList) {
				dos.writeInt(sliceData.sliceNumber);
				dos.writeByte(sliceData.nPeaks);
				peakInfo = sliceData.peakInfo;
				for (int j = 0; j < sliceData.nPeaks; j++) {
					for (int k = 0; k < 4; k++) {
						dos.writeShort(peakInfo[j][k]);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		BinaryObjectData pbo = new BinaryObjectData(WHISTLE_MOAN_DETECTION, bos.toByteArray());
		
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pbo;
	}

	@Override
	public String getStreamName() {
		return streamName;
	}

	@Override
	public int getStreamVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public synchronized PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {

		ConnectedRegionDataUnit crdu = null;
		ConnectedRegion cr = null;
		SliceData sliceData;
		ConnectedRegionDataBlock wmDataBlock = wmDetector.getOutputData();
		int fftHop = wmDataBlock.getFftHop();
		int fftLength = wmDataBlock.getFftLength();
		float sampleRate = wmDataBlock.getSampleRate();
		int fileVersion = binaryObjectData.getVersionNumber();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		
//		long intLength;
		if (delayScale == 0) {
			delayScale = wmDetector.getDelayScale();
		}
		long startSample;
		int channelMap; 
		int singleChannel;
		short nSlices;
		int sliceNum;
		int nPeaks;
		double amplitude = 0;
		double[] delays = null;
		int nDelays;
		long firstSliceSample;
		int[][] peakInfo;
		int firstSliceNum = 0;
		int[] timeBins;
		int[] peakFreqsBins;
		try {
//			intLength = dis.readInt(); // should always be dataLength-4 !
//			firstSliceSample = (long) ((double)(binaryObjectData.getTimeMillis() - bh.getDataDate()) * sampleRate / 1000.);
//			firstSliceSample = binary

			/**
			 * Bit of mess sorted out on 15/5/2020. Was working because module version went from 1 to 2 at same time 
			 * as file version went from 3 to 4. May have been some middly stuff where file version and module 
			 * There is some FV 3 with MV 1, in which case data were probably duplicated. 
			 */
			if (fileVersion > 3) { // basic data now in standard format. 
				firstSliceSample = startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
				
				// if the DataUnitBaseData contains a sequence map, use it in place of the channel map
				if (binaryObjectData.getDataUnitBaseData().getSequenceBitmap()!=null) {
					channelMap = binaryObjectData.getDataUnitBaseData().getSequenceBitmap();
				} else {
					channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
				}
			}
			else { // old stuff which should only be 
				firstSliceSample = startSample = dis.readLong();
				channelMap = dis.readInt();
				binaryObjectData.getDataUnitBaseData().setChannelBitmap(channelMap);
			}

//			if (channelMap != 1) {
//				System.out.println("Channel map = " + channelMap);
//			}
			singleChannel = PamUtils.getLowestChannel(channelMap);
			nSlices = dis.readShort();
			if (moduleVersion >= 1) {
				amplitude = (double) dis.readShort() / 100.;
			}
			if (fileVersion >= 4) {
				// As of FILE version 4, the time delays are now stored in the DataUnitBaseData object.
				// If there are no time delays yet, this method would return null.  In previous versions
				// however, if there were no time delays an empty array would be created.  Therefore,
				// to maintain compatibility with the rest of the code, create an empty array if the
				// method returns a null value
				// PAMGuard file that have FV4 and MV2 will end up in this part of the conditional statement
				// leaving the conditional reading of floats for Network data which is still in the older format. 
				delays = binaryObjectData.getDataUnitBaseData().getTimeDelaysSeconds();
				if (delays==null) {
					delays=new double[0];
				}
			}
			else if (moduleVersion <2)  { // old files with FV<4, delays in a messed up integer format. 
				nDelays = dis.readByte();
				//					if (nDelays > 1) {
				//						System.out.println("Bad number of delays : " + nDelays);
				//					}

				delays = new double[nDelays];
				//					if (moduleVersion == 1) {
				for (int i = 0; i < nDelays; i++) {
					delays[i] = (double) dis.readShort() / delayScale / sampleRate;
				}
			} 
			else if (moduleVersion == 2) { // still only for FV<4, so network data only. 
				nDelays = dis.readByte();
				delays = new double[nDelays];
				for (int i = 0; i < nDelays; i++) {
					delays[i] = (double) dis.readFloat() / sampleRate;
				}
			}
		
			/*
			 * sliceDataList = cr.getSliceData();
			for (int i = 0; i < nSlices; i++) {
				sliceData = sliceDataList.get(i);
				dos.writeInt(sliceData.sliceNumber);
				dos.writeByte(sliceData.nPeaks);
				peakInfo = sliceData.peakInfo;
				for (int j = 0; j < sliceData.nPeaks; j++) {
					for (int k = 0; k < 4; k++) {
						dos.writeShort(peakInfo[j][k]);
					}
				}
			}
			 */
			
			timeBins = new int[nSlices];
			peakFreqsBins = new int[nSlices];
			for (int i = 0; i < nSlices; i++) {
				sliceNum = dis.readInt();
				nPeaks = dis.readByte();
				if (nPeaks < 0) {
					System.out.println("Negative number of peaks: " + nPeaks);
				}
				if (i == 0) {
					firstSliceNum = sliceNum;
					cr = new ConnectedRegion(singleChannel, sliceNum, 0, wmDetector.getFFTLen());
				}
				peakInfo = new int[nPeaks][4];
				for (int j = 0; j < nPeaks; j++) {
					for (int k = 0; k < 4; k++) {
						peakInfo[j][k] = dis.readShort();
					}
				}
				sliceData = new SliceData(sliceNum, firstSliceSample + 
						fftHop * (sliceNum-firstSliceNum), peakInfo);
				cr.addOfflineSlice(sliceData);
				timeBins[i] = sliceData.sliceNumber;
				peakFreqsBins[i] = sliceData.getPeakBin();
			}			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}
		cr.cleanFragmentedFragment();
//		cr.sett
//		cr.addOfflineSlice(sliceData);
//		cr.condenseInfo();
		crdu = new ConnectedRegionDataUnit(binaryObjectData.getDataUnitBaseData(), cr, wmDetector);
		if(fileVersion<2) {
			crdu.setTimeMilliseconds(binaryObjectData.getTimeMilliseconds());
			crdu.setTimeDelaysSeconds(delays);
			crdu.setCalculatedAmlitudeDB(amplitude);
			crdu.setSampleDuration((long) ((nSlices+1) * fftHop));
			crdu.setChannelBitmap(channelMap);
			crdu.setStartSample(startSample);
		}
		
		/*
		 *  now also need to recalculate bearings using the appropriate bearing localiser.
		 *  These are hidden away in the sub processes and may be different for different 
		 *  hydrophone groups. 
		 *  Only do this here if we're in viewer mode, not network receive mode. 
		 *  If we're n network receive mode, we can't do this until 
		 *  channel numbers have been reassigned.   
		 */
		if ((runMode == PamController.RUN_PAMVIEW || runMode == PamController.RUN_NOTHING) && delays != null) {
			ShapeConnector shapeConnector = wmDetector.findShapeConnector(channelMap);
			if (shapeConnector != null) {
				BearingLocaliser bl = shapeConnector.getBearingLocaliser();
				if (bl != null) {
					double[][] angles = bl.localise(delays, crdu.getTimeMilliseconds());
					WhistleBearingInfo newLoc = new WhistleBearingInfo(crdu, bl, 
							shapeConnector.getGroupChannels(), angles);
					newLoc.setArrayAxis(bl.getArrayAxis());
					newLoc.setSubArrayType(bl.getArrayType());
					crdu.setTimeDelaysSeconds(delays);
					crdu.setLocalisation(newLoc);
				}
			}
		}
		
		try {
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return crdu;
	}

	@Override
	public int getModuleVersion() {
		return currentVersion;
	}

	@Override
	public byte[] getModuleHeaderData() {
		if (headerOutputStream == null) {
			headerOutputStream = new DataOutputStream(headerBytes = new ByteArrayOutputStream(4));
		}
		headerBytes.reset();
		try {
			headerOutputStream.writeInt(delayScale = wmDetector.getDelayScale());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return headerBytes.toByteArray();
	}


	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData,
			BinaryHeader bh, ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.BinaryDataSource#sinkModuleHeader(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader)
	 */
	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData,
			BinaryHeader bh) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData());
		DataInputStream dis = new DataInputStream(bis);
		try {
			delayScale = dis.readInt();
		} catch (IOException e) {
//			e.printStackTrace();
		}
		WhistleBinaryModuleHeader mh = new WhistleBinaryModuleHeader(binaryObjectData.getVersionNumber());
		mh.delayScale = delayScale;
		return mh;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub
		
	}

	
}
