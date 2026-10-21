package difar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import Filters.FilterParams;
import PamDetection.LocContents;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class DifarBinaryDataSource extends BinaryDataSource {

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
		return DifarClipPayload.CURRENT_VERSION;
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

	/**
	 * Pack a clip at the current module version: the clip's own data only.
	 * Its crossing, if any, is stored in the database as a crossing unit.
	 */
	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		DifarDataUnit ddu = (DifarDataUnit) pamDataUnit;
		DifarClipPayload payload = new DifarClipPayload();
		payload.clipStartMillis = ddu.getClipStartMillis();
		payload.displaySampleRate = ddu.getDisplaySampleRate();
		payload.demuxData = ddu.getDemuxedDecimatedData();
		payload.amplitude = (float) ddu.getAmplitudeDB();
		Double gain = ddu.getDifarGain();
		payload.gain = gain == null ? DifarClipPayload.NO_GAIN : gain.floatValue();
		payload.selectedAngle = toFloat(ddu.getSelectedAngle());
		payload.selectedFrequency = toFloat(ddu.getSelectedFrequency());
		payload.speciesCode = ddu.getSpeciesCode();
		payload.trackedGroup = ddu.getTrackedGroup();
		try {
			payload.write(dos);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new BinaryObjectData(DIFAR__DATA_ID, bos.toByteArray());
	}

	private static float toFloat(Double value) {
		return value == null ? Float.NaN : value.floatValue();
	}

	/**
	 * Unpack a clip of any module version. For versions up to 2, the crossing
	 * stored with the clip is kept on it as a plain legacy record. Nothing is
	 * looked up and no other clip is changed.
	 */
	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {

		DifarClipPayload payload;
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(binaryObjectData.getData(),
				0, binaryObjectData.getDataLength()))) {
			payload = DifarClipPayload.read(dis, moduleVersion);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		long startSample;
		int channelMap;
		double[] frequencyRange;
		if (moduleVersion < 2) {
			startSample = payload.startSample;
			channelMap = payload.channelMap;
			frequencyRange = payload.frequencyRange;
		}
		else {
			startSample = binaryObjectData.getDataUnitBaseData().getStartSample();
			channelMap = binaryObjectData.getDataUnitBaseData().getChannelBitmap();
			frequencyRange = binaryObjectData.getDataUnitBaseData().getFrequency();
		}

		/*
		 * Put in the original duration of the sound, otherwise some of the displays
		 * won't be able to correctly work out the duration in seconds. This is because the
		 * parent of the parent process of these data is connected to the original 48kHz data.
		 */
		float origSampleRate = difarControl.getDifarProcess().getSampleRate();
		int origDuration = (int) (payload.demuxedLength * origSampleRate / payload.displaySampleRate);
		if (moduleVersion >= 2) {
			origDuration = binaryObjectData.getDataUnitBaseData().getSampleDuration().intValue();
		}

		FilterParams difarFreqResponseFilterParams = difarControl.getDifarParameters().getDifarFreqResponseFilterParams();
		double[] freqs = difarFreqResponseFilterParams.getArbFreqs();
		double[] gains = difarFreqResponseFilterParams.getArbGainsdB();

		/*
		 * origDuration is in samples of the original recording, so the source
		 * sample rate has to be passed as well. Passing zero here made displays
		 * show a clip duration of Infinity, and stopped the waveform being
		 * decimated correctly.
		 */
		DifarDataUnit difarDataUnit = new DifarDataUnit(payload.clipStartMillis, binaryObjectData.getTimeMilliseconds(),
				startSample, origDuration, channelMap, null, null, null, binaryObjectData.getTimeMilliseconds(), null,
				frequencyRange, origSampleRate, payload.displaySampleRate, freqs, gains);

		difarDataUnit.setSelectedAngle((double) payload.selectedAngle);
		difarDataUnit.setSelectedFrequency((double) payload.selectedFrequency);
		difarDataUnit.setDemuxedDecimatedData(payload.demuxData);
		difarDataUnit.setMeasuredAmplitude(payload.amplitude);
		difarDataUnit.setDifarGain(payload.gain);
		difarDataUnit.setDisplaySampleRate(payload.displaySampleRate);
		difarDataUnit.setSpeciesCode(difarControl.difarParameters.getSpeciesList(difarControl), payload.speciesCode);
		difarDataUnit.setTrackedGroup(payload.trackedGroup == null ? DifarParameters.DefaultGroup : payload.trackedGroup);
		DifarLocalisation difarLocalisation = new DifarLocalisation(difarDataUnit,
				LocContents.HAS_BEARING, difarDataUnit.getChannelBitmap());
		difarLocalisation.setBearingError(difarControl.getDifarParameters().bearingError);
		difarDataUnit.setLocalisation(difarLocalisation);
		difarDataUnit.setLegacyCrossing(payload.legacyCrossing);
		difarDataUnit.setBinaryVersion(moduleVersion);
		if (!difarControl.getDifarParameters().loadViewerClips) {
			difarDataUnit.setDemuxedDecimatedData(null);
		}
		DataUnitBaseData baseData = binaryObjectData.getDataUnitBaseData();
		if (baseData != null && difarDataBlock.isSecondCopy(baseData.getUID(),
				difarDataUnit.getChannelBitmap(), difarDataUnit.getTimeMilliseconds())) {
			/*
			 * A second copy noted by a running upgrade. Core's viewer save
			 * reads back each stored object it cannot find in memory; returning
			 * nothing leaves this one out of the rewritten file.
			 */
			return null;
		}
		return difarDataUnit;
	}


	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

}
