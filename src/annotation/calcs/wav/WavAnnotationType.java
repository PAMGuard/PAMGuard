package annotation.calcs.wav;

import generalDatabase.SQLLoggingAddon;
import wavFiles.Wav16AudioFormat;
import wavFiles.WavFileWriter;

import javax.sound.sampled.AudioFormat;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import annotation.AnnotationDialogPanel;
import annotation.DataAnnotationType;

/**
 * Wav annotation type. Can save the raw data from an annotation to a Wav file. Can 
 * automatically calculate the Wav from the time frequency box of the 
 * data unit. 
 * @author Brian Miller
 *
 */
public class WavAnnotationType extends DataAnnotationType<WavAnnotation> {

	private WavSqlAddon wavSqlAddon;
	private WavAnnotationPanel wavAnnotationPanel;
	
	public WavAnnotationType() {
		super();
		wavSqlAddon = new WavSqlAddon(this);
//		this.setWavAnnotationPanel(new WavAnnotationPanel(this));
	}

	@Override
	public String getAnnotationName() {
		return "Wav";
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return AcousticDataUnit.class.isAssignableFrom(dataUnitType);
	}

	@Override
	public String toString(WavAnnotation dataAnnotation) {
		// TODO Auto-generated method stub
		return super.toString(dataAnnotation);
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return wavSqlAddon;
	}

	@Override
	public WavAnnotation autoAnnotate(PamDataUnit pamDataUnit) {
		if (canAnnotate(pamDataUnit.getClass()) == false) return null;
		PamDataUnit acousticDataUnit = pamDataUnit;	// originally cast to AcousticDataUnit
		/**
		 * Will need to get waveform data for a sec before, a sec after and during the call 
		 * FFT it, etc. Better don't bother with the FFT, just make an 8 pole filter and add up
		 * the energy.
		 */
		PamDataBlock dataBlock = pamDataUnit.getParentDataBlock();
		if (dataBlock == null) return null;
		AcquisitionProcess daqProcess = null;
		try {
			daqProcess = (AcquisitionProcess) dataBlock.getSourceProcess();
		}
		catch (ClassCastException e) {
			/*
			 * Find any Acquisition unit !
			 */
			daqProcess = findAnyDaqProcess();
		}
		if (daqProcess == null) return null;
		float sampleRate = daqProcess.getSampleRate();
		double[] f = acousticDataUnit.getFrequency();
		FilterParams filterParams = new FilterParams();
		filterParams.filterOrder = 8;
		filterParams.filterType = FilterType.BUTTERWORTH;
		if (f == null || f.length != 2 || f[1]<=f[0] || f[0] == 0 && f[1] >= sampleRate/2.) {
			filterParams.filterType = FilterType.NONE;
		}
		else if (f[0] == 0) {
			filterParams.filterBand = FilterBand.LOWPASS;
			filterParams.lowPassFreq = (float) f[1];
		}
		else if (f[1] >= sampleRate/2) {
			filterParams.filterBand = FilterBand.HIGHPASS;
			filterParams.highPassFreq = (float) f[0];
		}
		else {
			filterParams.filterBand = FilterBand.BANDPASS;
			filterParams.highPassFreq = (float) f[0];
			filterParams.lowPassFreq = (float) f[1];
		}
		FilterMethod filterMethod = FilterMethod.createFilterMethod(sampleRate, filterParams);
		
		long dataStart = acousticDataUnit.getTimeMilliseconds()-1000;
		long dataEnd = acousticDataUnit.getEndTimeInMilliseconds()+1000;
		if (haveRawData(daqProcess.getRawDataBlock(), dataStart-1000, dataEnd+1000) == false) {
			daqProcess.getRawDataBlock().clearAll();
			loadRawData(daqProcess, dataStart-1000, dataEnd+1000);
//			System.out.println("Load Wav raw data from " + PamCalendar.formatTime(dataStart, true) + " to " +
//					PamCalendar.formatTime(dataEnd, true));
			if (haveRawData(daqProcess.getRawDataBlock(), dataStart, dataEnd) == false) {
				return null;
			}
		}
//		else {
//			System.out.println("Wav raw data present " + PamCalendar.formatTime(dataStart, true) + " to " +
//					PamCalendar.formatTime(dataEnd, true));
//		}
		double[][] rawData = null;
		/**
		 * F**** this is annoying . Raw data are loaded according to sample 
		 * number, but we only know milliseconds. This is a real pain !
		 */
		try {
			rawData = daqProcess.getRawDataBlock().getSamplesForMillis(dataStart, dataEnd-dataStart, pamDataUnit.getChannelBitmap());
		} catch (RawDataUnavailableException e) {
			System.out.println("Can find data for Wav annotation " + e.getMessage());
//			try {
//				rawData = daqProcess.getRawDataBlock().getSamplesForMillis(dataStart, dataEnd-dataStart, pamDataUnit.getChannelBitmap());
//			} catch (RawDataUnavailableException e1) {
//				// TODO Auto-generated catch block
////				e1.printStackTrace();
//			}
			return null;
		}
		if (rawData == null) return null;
		
		boolean isNew = false;
		WavAnnotation wavAnnotation = (WavAnnotation) acousticDataUnit.findDataAnnotation(WavAnnotation.class);
		if (wavAnnotation == null) {
			wavAnnotation = new WavAnnotation(this);
			isNew = true;
		}

		String fileName = PamCalendar.createFileNameMillis(acousticDataUnit.getTimeMilliseconds(),
				wavAnnotation.getWavFolderName(),  wavAnnotation.getWavPrefix() + "_", ".wav");
		
		AudioFormat af = new Wav16AudioFormat(sampleRate, rawData.length);
		WavFileWriter wavFile = new WavFileWriter(fileName, af);
		wavFile.write(rawData);
		wavFile.close();
		
		wavAnnotation.setExportedWavFileName(fileName);
		if (isNew) {
			acousticDataUnit.addDataAnnotation(wavAnnotation);
		}
		
		return wavAnnotation;
	}
	
	private AcquisitionProcess findAnyDaqProcess() {
		PamControlledUnit pcu = PamController.getInstance().findControlledUnit(AcquisitionControl.unitType);
		if (pcu == null) return null;
		if (AcquisitionControl.class.isAssignableFrom(pcu.getClass())) {
			return ((AcquisitionControl) pcu).getAcquisitionProcess();
		}
		else {
			return null;
		}
	}

	private void loadRawData(AcquisitionProcess daqProcess, long dataStart,
			long dataEnd) {
		daqProcess.getOfflineData(daqProcess.getRawDataBlock(), null, dataStart, dataEnd, 1);		
	}

	private boolean haveRawData(PamRawDataBlock rawDataBlock, long dataStart,
			long dataEnd) {
		if (rawDataBlock == null) return false;
		RawDataUnit firstUnit = rawDataBlock.getFirstUnit();
		if (firstUnit == null || firstUnit.getTimeMilliseconds() > dataStart) return false;
		RawDataUnit lastUnit = rawDataBlock.getLastUnit();
		if (lastUnit == null || lastUnit.getEndTimeInMilliseconds() < dataEnd) return false;
		return true;
	}

	class RawLoadObserver implements LoadObserver {

		@Override
		public void setLoadStatus(int loadState) {
			// TODO Auto-generated method stub
			
		}
		
	}
	class RawDataObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Wav annotator";
		}
		
	}
	
	@Override
	public boolean canAutoAnnotate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getDialogPanel()
	 */
	@Override
	public AnnotationDialogPanel getDialogPanel() {
		return getWavAnnotationPanel();
	}

	public WavAnnotationPanel getWavAnnotationPanel() {
		return wavAnnotationPanel;
	}

	public void setWavAnnotationPanel(WavAnnotationPanel wavAnnotationPanel) {
		this.wavAnnotationPanel = wavAnnotationPanel;
	}

	@Override
	public Class getAnnotationClass() {
		return WavAnnotation.class;
	}	
}
