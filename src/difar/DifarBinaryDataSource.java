package difar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import Filters.FilterParams;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class DifarBinaryDataSource extends BinaryDataSource {

	private static final int currentVersion = 2;
	private static final int DIFAR__DATA_ID = 0;
	private DifarControl difarControl;
	private DifarDataBlock difarDataBlock;

	public DifarBinaryDataSource(DifarControl difarControl, DifarDataBlock difarDataBlock) {
		super(difarDataBlock);
		this.difarControl = difarControl;
		this.difarDataBlock = difarDataBlock;
	}

	@Override
	public String getStreamName() {
		return difarControl.getUnitName();
	}

	@Override
	public int getStreamVersion() {
		return 0;
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
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData,
			BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData,
			BinaryHeader bh, ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	@SuppressWarnings("deprecation")
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		DifarDataUnit ddu = (DifarDataUnit) pamDataUnit;
		double[][] demuxData = ddu.getDemuxedDecimatedData();
		double[] fRange = ddu.getFrequency();
		try {
//			dos.writeLong(ddu.getStartSample()); as of version 2, start sample included in DataUnitBaseData
			dos.writeLong(ddu.getClipStartMillis());
//			dos.writeInt(ddu.getChannelBitmap()); as of version 2, channel bitmap included in DataUnitBaseData
			dos.writeFloat(ddu.getDisplaySampleRate());
			
			// if there is no demuxed data, write a 0 for the length
			if (demuxData==null) {
				dos.writeInt(0);
			} else {
				dos.writeInt(demuxData[0].length);
			}
			
//			dos.writeFloat((float) fRange[0]); as of version 2, freq range included in DataUnitBaseData
//			dos.writeFloat((float) fRange[1]); as of version 2, freq range included in DataUnitBaseData
			dos.writeFloat((float) ddu.getAmplitudeDB());
			Double gain = ddu.getDifarGain();
			if (gain == null) {
				dos.writeFloat(-9999.f);
			}
			else {
				dos.writeFloat(new Float(gain));
			}
			dos.writeFloat(new Float(ddu.getSelectedAngle())); 
			dos.writeFloat(new Float(ddu.getSelectedFrequency())); 
			dos.writeUTF(ddu.getSpeciesCode());
			dos.writeUTF(ddu.getTrackedGroup());
			
			// if there is no demuxed data, write a 0 for the max value and skip the rest
			if (demuxData==null) {
				dos.writeFloat(0);
			} else {
				double maxVal = PamUtils.getAbsMax(PamUtils.getMinAndMax(demuxData));
				dos.writeFloat((float) maxVal);
				for (int i = 0; i < demuxData.length; i++) {
					for (int j = 0; j < demuxData[i].length; j++) {
						dos.writeShort((int) (demuxData[i][j] * 32767 / maxVal)); 
					}
				}
			}
			DIFARCrossingInfo crossInfo = ddu.getDifarCrossing();
			if (crossInfo == null) {
				dos.writeShort(0);
			}
			else {
				dos.writeShort(crossInfo.getNumberOfMatchedUnits());
				dos.writeFloat((float) crossInfo.getCrossLocation().getLatitude());
				dos.writeFloat((float) crossInfo.getCrossLocation().getLongitude());
				Double errors[] = crossInfo.getErrors();
				dos.writeFloat(errors[0].floatValue());
				dos.writeFloat(errors[1].floatValue());
				for (int i = 1; i < crossInfo.getMatchedUnits().length; i++) {
					if (crossInfo.getMatchedUnits()[i] != null){ //TODO:Find out why matched units can be null 
						dos.writeShort(PamUtils.getSingleChannel(crossInfo.getMatchedUnits()[i].getChannelBitmap()));
						dos.writeLong(crossInfo.getMatchedUnits()[i].getTimeMilliseconds());
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new BinaryObjectData(DIFAR__DATA_ID, bos.toByteArray());
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		long startSample;
		long clipStart;
		int channelMap;
		float displaySampleRate;
		int demuxedLength;
		float amplitude, difarGain, selAngle, selFrequency;
		double maxVal;
		double[][] demuxData;
		String speciesCode;
		String trackedGroup = DifarParameters.DefaultGroup;
		double[] frequencyRange = new double[2];
		DifarDataUnit[] matchedUnits = null;
		LatLong latLong = null;
		Double[] errors = new Double[3];
		try {
			if (moduleVersion<2) {
				startSample = dis.readLong();
			} else {
				startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
			}
			clipStart = dis.readLong();
			if (moduleVersion<2) {
				channelMap = dis.readInt();
			} else {
				channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
			}
			displaySampleRate = dis.readFloat();
			demuxedLength = dis.readInt();
			if (moduleVersion<2) {
				frequencyRange[0] = dis.readFloat();
				frequencyRange[1] = dis.readFloat();
			} else {
				frequencyRange = binaryObjectData.getDataUnitBaseData().getFrequency();
			}
			amplitude = dis.readFloat();
			difarGain = dis.readFloat();
			selAngle = dis.readFloat();
			selFrequency = dis.readFloat();
			speciesCode = dis.readUTF();
//			if ((speciesCode.equals(DifarParameters.CalibrationClip) && 
//					!difarControl.getDifarParameters().showVesselBearings)){
//				return null;
//			}
			if (moduleVersion >= 1){
				trackedGroup = dis.readUTF();	
			} 
			
			maxVal = dis.readFloat();
			
			// check if we have any demuxed data - if not, skip this next section and just set the variable to null
			if (demuxedLength>0) {
				demuxData = new double[3][demuxedLength];

				//			if (difarControl.getDifarParameters().loadViewerClips){
				for (int i = 0; i < demuxData.length; i++) {
					for (int j = 0; j < demuxedLength; j++) {
						demuxData[i][j] = dis.readShort() * maxVal / 32767;
					}
				}
				//			}
				//			else {
				//				dis.skip(demuxData.length*demuxedLength*2);
				//			}
			} else {
				demuxData=null;
			}
			
			int nMatches = dis.readShort();
			if (nMatches > 0) {
				latLong = new LatLong(dis.readFloat(), dis.readFloat());
				if (moduleVersion >= 1) {
					errors[0] = (double) dis.readFloat();
					errors[1] = (double) dis.readFloat();
					errors[2] = 0d;
				} else {
					errors[0] = 0d;
					errors[1] = 0d;
					errors[2] = 0d;
				}
				//TODO: read the x,y,z errors of the crossLocation
				matchedUnits = new DifarDataUnit[nMatches];
				for (int i = 0; i < nMatches-1; i++) {
					short matchChan = dis.readShort();
					long matchTime = dis.readLong();
					matchedUnits[i+1] = difarDataBlock.findDataUnit(matchTime, 1<<matchChan);
				}
			}
			bis.close();			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		/*
		 * Put in the original duration of the sound, otherwise some of the displays 
		 * won't be able to correctly work out the duration in seconds. This is because the 
		 * parent of the parent process of these data is connected to the original 48kHz data.  
		 */
		float origSampleRate = difarControl.getDifarProcess().getSampleRate();
		int origDuration = (int) (demuxedLength * origSampleRate / displaySampleRate);
		if (moduleVersion>=2) {
			origDuration = binaryObjectData.getDataUnitBaseData().getSampleDuration().intValue();
		}
		
		FilterParams difarFreqResponseFilterParams = difarControl.getDifarParameters().getDifarFreqResponseFilterParams();
		
		double[] freqs = difarFreqResponseFilterParams.getArbFreqs();
		double[] gains = difarFreqResponseFilterParams.getArbGainsdB();
		
		DifarDataUnit difarDataUnit = new DifarDataUnit(clipStart, binaryObjectData.getTimeMilliseconds(), startSample, origDuration, channelMap, 
				null, null, null, binaryObjectData.getTimeMilliseconds(), null, frequencyRange, 0, displaySampleRate, freqs, gains);

		difarDataUnit.setSelectedAngle(new Double(selAngle));
		difarDataUnit.setSelectedFrequency(new Double(selFrequency));
//		difarDataUnit.setMaximumAngle(null);
//		difarDataUnit.setMaximumFrequency(null);
		difarDataUnit.setDemuxedDecimatedData(demuxData);
		difarDataUnit.setMeasuredAmplitude(amplitude);
		difarDataUnit.setDifarGain(difarGain);
		difarDataUnit.setDisplaySampleRate(displaySampleRate);
		difarDataUnit.setSpeciesCode(difarControl.difarParameters.getSpeciesList(difarControl), speciesCode);
		difarDataUnit.setTrackedGroup(trackedGroup);
		difarDataUnit.setLocalisation(new DifarLocalisation(difarDataUnit, LocContents.HAS_BEARING, difarDataUnit.getChannelBitmap()));
		if (matchedUnits != null) {
			matchedUnits[0] = difarDataUnit;
			DIFARCrossingInfo dci = new DIFARCrossingInfo(matchedUnits, latLong, errors);
			for (int i = 0; i < dci.getNumberOfMatchedUnits(); i++) {
				if (matchedUnits[i] != null) {
					matchedUnits[i].setDifarCrossing(dci);
				}
			}
		}
//		difarDataUnit.setMeasuredAmplitude(difarControl.getDifarProcess().getDifarAmplitude(difarDataUnit));
		if (!difarControl.getDifarParameters().loadViewerClips){
			difarDataUnit.setDemuxedDecimatedData(null);
		}		
//		difarControl.getTrackedGroupProcess().newData(difarDataBlock, difarDataUnit);
//		System.out.println(PamCalendar.formatDateTime(difarDataUnit.getTimeMilliseconds()));
		return difarDataUnit;
	}


	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

}
