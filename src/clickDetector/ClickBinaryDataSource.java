package clickDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliserSelector;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.OldAngleConverter;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import clickDetector.ClickDetector.ChannelGroupDetector;
import dataMap.DataMapDrawing;

/**
 * Class for storing clicks to binary store. 
 * @author Doug Gillespie
 *
 */
public class ClickBinaryDataSource extends BinaryDataSource {

	private String streamName;

	private static final int bytesPerSamples = 1;

	/**
	 * <p>Module version changes</p>
	 * Version 2: Add int clickFlags after short clickType<br>
	 * Version 3:<br>
	 * Version 4: Moved start sample, channel bitmap, time delays and duration to DataUnitBaseData general data structure<br>
	 */
	private static final int currentVersion = 4;

	public static final int CLICK_DETECTOR_CLICK = 1000;
	
	private ClickDetector clickDetector;
	
	private ClickBinaryModuleFooter clickFooter;
	
	private AcquisitionProcess acquisitionProcess;

	public ClickBinaryDataSource(ClickDetector clickDetector, PamDataBlock sisterDataBlock, String streamName) {
		super(sisterDataBlock);
		this.clickDetector = clickDetector;
		this.streamName = streamName;
		clickMapDrawing = new ClickMapDrawing(clickDetector);
	}

	
	@Override
	public void newFileOpened(File outputFile) {
		clickFooter = new ClickBinaryModuleFooter(clickDetector);
	}

	@Override
	public int getModuleVersion() {
		return currentVersion;
	}

	@Override
	public byte[] getModuleHeaderData() {
		return null;
	}

	@Override
	public byte[] getModuleFooterData() {
		return clickFooter.getByteArray();
	}

	@Override
	public String getStreamName() {
		return streamName;
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;

	private ClickMapDrawing clickMapDrawing;

	/**
	 * Save a click to the binary data store
	 * @param cd click detection
	 */
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		ClickDetection cd = (ClickDetection) pamDataUnit;
		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
//		System.out.println("Packing data for click " + cd.clickNumber);
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		try {
//			dos.writeLong(cd.getStartSample()); as of version 4, start sample included in DataUnitBaseData
//			dos.writeInt(cd.getChannelBitmap()); as of version 4, channel bitmap included in DataUnitBaseData
			dos.writeInt(cd.triggerList);
			//if (cd.getAmplitudeDB()>150) System.out.println("ClickBinaryDataSource: ClickType: "+cd.getClickType()+" channel: "+PamUtils.getSingleChannel(cd.getChannelBitmap())+ " Amplitude: "+  cd.getAmplitudeDB());
			dos.writeShort(cd.getClickType());
			dos.writeInt(cd.getClickFlags());
//			double[] delays = cd.getDelays(); as of version 4, time delays are included in DataUnitBaseData
//			if (delays != null) {
//				dos.writeShort(delays.length);
//				for (int i = 0; i < delays.length; i++) {
//					dos.writeFloat((float) delays[i]);
//				}
//			}
//			else {
//				dos.writeShort(0);
//			}
			double[] angles = null;
			double[] angleErrors = null;
			if (cd.getLocalisation() != null) {
				angles = cd.getLocalisation().getAngles();
				angleErrors = cd.getLocalisation().getAngleErrors();
			}
			if (angles != null) {
				dos.writeShort(angles.length);
				for (int i = 0; i < angles.length; i++) {
					dos.writeFloat((float)angles[i]);
				}
			}
			else {
				dos.writeShort(0);
			}
			if (angleErrors != null) {
				dos.writeShort(angleErrors.length);
				for (int i = 0; i < angleErrors.length; i++) {
					dos.writeFloat((float)angleErrors[i]);
				}
			}
			else {
				dos.writeShort(0);
			}
//			int duration = cd.getSampleDuration().intValue(); as of version 4, duration included in DataUnitBaseData
//			dos.writeShort(duration);
			byte[][] waveData = cd.getCompressedWaveData();
			double maxVal = cd.getWaveAmplitude();
			// write the scale factor. 
			dos.writeFloat((float) maxVal);
			for (int i = 0; i < cd.getNChan(); i++) {
				dos.write(waveData[i]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		BinaryObjectData packedData = new BinaryObjectData(CLICK_DETECTOR_CLICK, bos.toByteArray());

		if (clickFooter != null) {
			clickFooter.newClick(cd);
		}
		
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return packedData;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		ClickBinaryModuleHeader mh = new ClickBinaryModuleHeader(binaryObjectData.getVersionNumber());
		if (!mh.createHeader(binaryObjectData, bh)) {
			return null;
		}
		return mh;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData,
			BinaryHeader bh, ModuleHeader mh) {
		ClickBinaryModuleFooter mf = new ClickBinaryModuleFooter(clickDetector);
		if (!mf.createFooter(binaryObjectData, bh, mh)) {
			return null;
		}
		return mf;
	}

//	long lastUID = 0;
	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		// Turn the binary data back into a click. 
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);

//		long uid = binaryObjectData.getDataUnitBaseData().getUID();
//		System.out.printf("Loading click with UID %d at %s\n", uid, 
//				PamCalendar.formatDateTime(binaryObjectData.getTimeMilliseconds()));
//		if (uid == 110006089) {
//			System.out.println("Click  UID: " + 110006089);
//		}
//		else {
//			lastUID = binaryObjectData.getDataUnitBaseData().getUID();
//		}
		/**
		 * Can't work out what on earth this line was doing !
		 */
//		long fileOffsetSamples = (long)((bh.getDataDate()-binaryObjectData.getTimeMillis()) * 
//				(double)clickDetector.getSampleRate() / 1000.) -
//				bh.getFileStartSample();

//		int intLength;
		long startSample;
		int channelMap;
		int triggerList;
		short clickType;
		short nDelays;
		double[] delays = null;
		short nAngles;
		double[] angles = null;
		short nAngleErrors;
		double[] angleErrors = null;
//		short duration; changed to long to match info in DataUnitBaseData
		long duration;
		double waveMax;
		double waveScale;
		int clickFlags = 0;
		int nChan;
		int maxDelays;
		byte[][] waveData;
		double channelMax, aVal, allMax = 0;
		ClickDetection newClick = null;
		ClickLocalisation clickLocalisation;
		ChannelGroupDetector channelGroupDetector = null;
		BearingLocaliser bearingLocaliser = null;
		try {
//			intLength = dis.readInt(); // should always be dataLength-4 !
//			System.out.println("Read click at " + PamCalendar.formatDateTime(millis));
			if (moduleVersion<4) {
				startSample = dis.readLong();
			} else {
				startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
			}
//			startSample += fileOffsetSamples;
			/*
			 * Should be able to do some check here whereby the millis time
			 * is the same as dataStart + startSample.sampleRate*1000
			 */
//			long millis2 = dataStart + (long) (startSample / clickDetector.getSampleRate() * 1000.);
//			millis2 = clickDetector.absSamplesToMilliseconds(startSample);
//			if (Math.abs(millis2-timeMillis) > 1000) {
//				System.out.println(String.format("Time offset in read %d", + millis2 - timeMillis));
//			}
			
			
			
			if (moduleVersion<4) {
				channelMap = dis.readInt();
			} else {
				channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
			}
			
			nChan = PamUtils.getNumChannels(channelMap);
			channelGroupDetector = clickDetector.findChannelGroupDetector(channelMap);
			maxDelays = (nChan*(nChan-1))/2;
			triggerList = dis.readInt();
			clickType = dis.readShort();
			if (moduleVersion >= 2) {
				clickFlags = dis.readInt();
			}
			
			if (moduleVersion<4) {
				nDelays = dis.readShort();
				if (nDelays > maxDelays) {
					System.out.println("Too many delays in click: " + nDelays);
				}
				if (nDelays > 0) {
					delays = new double[nDelays];
					for (int i = 0; i < nDelays; i++) {
						delays[i] = dis.readFloat();
					}
				}
			} else {
				if (binaryObjectData.getDataUnitBaseData().getTimeDelaysSeconds()==null) {
					nDelays = 0;
				} else {
					 delays = binaryObjectData.getDataUnitBaseData().getTimeDelaysSeconds();
					 nDelays = (short) delays.length;
					 // compare nDelays to the max number permitted and display a
					 // warning if exceeded.  This was added to match what used to happen
					 // prior to version 4
						if (nDelays > maxDelays) {
							System.out.println("Too many delays in click: " + nDelays);
							delays = null;
						} else {
							// convert the delays in seconds to delays in samples, which
							// is how the Click Detector code expects it
							for (int i=0; i<nDelays; i++) {
								delays[i] *= clickDetector.getSampleRate();
							}
						}
				}
			}
			
			nAngles = dis.readShort();
			if (nAngles > 0) {
				angles = new double[nAngles];
				for (int i = 0; i < nAngles; i++) {
					angles[i] = dis.readFloat();
				}
				if (bh != null && bh.getHeaderFormat() <= 4) {
					angles = OldAngleConverter.convertOldAnglePair(angles);
				}
			}
			
			if (moduleVersion >= 3) {
				nAngleErrors = dis.readShort();
				if (nAngleErrors > 0) {
					angleErrors = new double[nAngleErrors];
					for (int i = 0; i < nAngleErrors; i++) {
						angleErrors[i] = dis.readFloat();
					}
				}
			}

			if (moduleVersion<4) {
				duration = dis.readUnsignedShort();
				if (duration < 0) {
					System.err.println("Negative click duration: " + duration);
				}
			} else {
				duration = binaryObjectData.getDataUnitBaseData().getSampleDuration();
			}
			
			newClick = new ClickDetection(channelMap, startSample, duration, clickDetector, null, triggerList);
			newClick.setClickType((byte) clickType);
			newClick.setClickFlags(clickFlags);
			newClick.setChannelGroupDetector(channelGroupDetector);
			newClick.clickNumber = binaryObjectData.getObjectNumber();
			for (int i = 0; i < nDelays; i++) {
				newClick.setDelayInSamples(i, delays[i]);
			}
			
			waveMax = dis.readFloat();
			waveScale = waveMax / 127.;
			if (nChan <= 0 || duration <= 0) {
				System.out.println("Invalid click in file");
				return null;
			}
			waveData = new byte[nChan][(int) duration];
			newClick.setCompressedData(waveData, waveMax);
			for (int iChan = 0; iChan < nChan; iChan++) {
				dis.read(waveData[iChan]);
//				channelMax = 0;
//				for (int iSamp = 0; iSamp < duration; iSamp++) {
////					waveData[iChan][iSamp] = (aVal = dis.readByte() * waveScale);
//					channelMax = Math.max(channelMax, waveData[iChan][iSamp]);
//				}
//				newClick.setAmplitude(iChan, channelMax*waveScale);
			}
			// put this in a separate loop since calling cal'am' inflated all channels 
			// on the first call before some channels are loaded. 
			for (int iChan = 0; iChan < nChan; iChan++) {
				newClick.calculateAmplitude(iChan);
			}
			/*
			 * calculating the amplitude will have made it inflate the compressed data to double
			 * which takes a load of memory. Therefore set the double wavedata back to null in the hpe
			 * that it doesn't get loaded up again. 
			 */
			newClick.setWaveData(null);
			
			int firstChan = PamUtils.getLowestChannel(newClick.getChannelBitmap());
//			newClick.setMeasuredAmplitude(allMax/nChan);
//			newClick.setMeasuredAmplitudeType(AcousticDataUnit.AMPLITUDE_SCALE_LINREFSD);
			if (acquisitionProcess == null) {
				PamProcess sourceProcess = clickDetector.getSourceProcess();
				if (sourceProcess instanceof AcquisitionProcess) {
					acquisitionProcess = (AcquisitionProcess) clickDetector.getSourceProcess();
				}
			}
			if (acquisitionProcess != null) {
				newClick.setCalculatedAmlitudeDB(acquisitionProcess.
						rawAmplitude2dB(newClick.getMeanAmplitude(), firstChan, false));
			}
//			if (newClick.getChannelBitmap() > 3000) {
//				System.out.println("Click channel map =  " + newClick.getChannelBitmap());
//			}
			clickLocalisation = newClick.getClickLocalisation();
			if (clickLocalisation != null && angles != null) {
				clickLocalisation.setAngles(angles);
//				 needs another call into the localisation to set the correct array axis. 
				clickLocalisation.setSubArrayType(getArrayType(channelMap, nAngles));
				if (channelGroupDetector != null) {
					bearingLocaliser = channelGroupDetector.getBearingLocaliser();
				}
				if (bearingLocaliser == null) {
					// this should be called with hydrophone map, not channel map. 
					// this should never happen, since handles in 
					int[] simpleMap = PamUtils.getChannelArray(channelMap);
					bearingLocaliser = BearingLocaliserSelector.createBearingLocaliser(simpleMap, 0);
				}
				clickLocalisation.setSubArrayType(bearingLocaliser.getArrayType());
				clickLocalisation.setArrayAxis(bearingLocaliser.getArrayAxis());


			}
//			if (clickLocalisation.getSubArrayType() != 4) {
//				System.out.printf("Invalid sub array type in click channeld %d array type %d\n", newClick.getChannelBitmap(), clickLocalisation.getSubArrayType());
//			}
			if (clickLocalisation != null && angleErrors != null) {
					clickLocalisation.setAngleErrors(angleErrors);
			}
			newClick.setTimeMilliseconds(binaryObjectData.getTimeMilliseconds());
//			clickDetector.getClickDataBlock().addPamData(newClick);
			
		} catch (IOException e1) {
			System.out.println("IOException in Click binary file: " + e1.getMessage());
			return null;
		} catch (Exception e) {
			System.out.println("Error in ClickBinaryDataSource: " + e.getMessage());
//			e.printStackTrace();
//			return null;
		}
		

		try {
			bis.close();
		} catch (IOException e) {
			System.out.println("Error in file: " + e.getMessage());
			return null;
		}
		
		//if (newClick.getAmplitudeDB()>150) System.out.println("SinkData: ClickType: "+newClick.getClickType()+" channel: "+PamUtils.getSingleChannel(newClick.getChannelBitmap())+ " Amplitude: "+  newClick.getAmplitudeDB());

		
		return newClick;
	}
	
	/**
	 * Try to work out from the click detector what on earth the array type is for this
	 * localisation. 
	 * For now, cheat and just base the decision on the number of channels !
	 * @param channelMap
	 * @return
	 */
	private int getArrayType(int channelMap, int nAngles) {
		int nChan = PamUtils.getNumChannels(channelMap);
		if (nChan <= 1) {
			return ArrayManager.ARRAY_TYPE_POINT;
		}
		else if (nChan == 2)  {
			return ArrayManager.ARRAY_TYPE_LINE;
		}
		else if (nAngles == 1) {
			return ArrayManager.ARRAY_TYPE_LINE;
		}
		else if (nChan == 3) {
			return ArrayManager.ARRAY_TYPE_PLANE;
		}
		else if (nChan >= 4) {
			return ArrayManager.ARRAY_TYPE_VOLUME;
		}
		return ArrayManager.ARRAY_TYPE_POINT;
	}



	@Override
	public DataMapDrawing getSpecialDrawing() {
		return clickMapDrawing;
	}

	/*
	 * version 0 format. 
	public void saveData(PamDataUnit pamDataUnit) {
		if (getBinaryStorageStream() == null) {
			return;
		}
		ClickDetection cd = (ClickDetection) pamDataUnit;
		// make a byte array output stream and write the data to that, 
		// then dump that down to the main storage stream
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		try {
			dos.writeLong(cd.getTimeMilliseconds());
			dos.writeLong(cd.getStartSample());
			dos.writeInt(cd.getChannelBitmap());
			dos.writeInt(cd.triggerList);
			dos.writeShort(cd.getClickType());
			double[] delays = cd.getDelays();
			if (delays != null) {
				dos.writeShort(delays.length);
				for (int i = 0; i < delays.length; i++) {
					dos.writeFloat((float) delays[i]);
				}
			}
			else {
				dos.writeShort(0);
			}
			double[] angles = cd.getLocalisation().getAngles();
			if (angles != null) {
				dos.writeShort(angles.length);
				for (int i = 0; i < angles.length; i++) {
					dos.writeFloat((float)angles[i]);
				}
			}
			else {
				dos.writeShort(0);
			}
			int duration = (int)cd.getDuration();
			dos.writeShort(duration);
			double[][] waveData = cd.getWaveData();
			for (int i = 0; i < cd.getNChan(); i++) {
				for (int j = 0; j < duration; j++) {
					dos.writeShort((int)(waveData[i][j]*32768.));
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		getBinaryStorageStream().storeData(BinaryTypes.CLICK_DETECTOR_CLICK, bos.toByteArray());
	}
	 */

}
