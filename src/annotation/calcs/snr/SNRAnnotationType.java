package annotation.calcs.snr;

import generalDatabase.SQLLoggingAddon;
import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import annotation.AnnotationDialogPanel;
import annotation.AnnotationSettingsPanel;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationOptions;

/**
 * SNR annotation type. Can add a single double SNR to a data unit. Can 
 * automatically calculate the SNR from the time frequency box of the 
 * data unit. 
 * @author Doug Gillespie
 *
 */
public class SNRAnnotationType extends DataAnnotationType<SNRAnnotation> {

	private SnrSqlAddon snrSqlAddon;
	private SNRAnnotationPanel snrAnnotationPanel;
	private SNRSettingsPanel snrSettingsPanel;
	
	private SNRAnnotationOptions snrAnnotationOptions = new SNRAnnotationOptions("SNR Annotation Options");
	
	public SNRAnnotationType() {
		super();
		snrSqlAddon = new SnrSqlAddon(this);
		this.snrAnnotationPanel = new SNRAnnotationPanel(this);
	}

	@Override
	public String getAnnotationName() {
		return "SNR";
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return AcousticDataUnit.class.isAssignableFrom(dataUnitType);
	}

	@Override
	public String toString(SNRAnnotation dataAnnotation) {
		// TODO Auto-generated method stub
		return super.toString(dataAnnotation);
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return snrSqlAddon;
	}

	@Override
	public SNRAnnotation autoAnnotate(PamDataUnit pamDataUnit) {
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
		
		SNRAnnotationParameters snrAnnotationParameters = snrAnnotationOptions.getSnrAnnotationParameters();
		
		long dataStart = acousticDataUnit.getTimeMilliseconds()-snrAnnotationParameters.bufferMillis-snrAnnotationParameters.noiseMillis;
		long dataEnd = acousticDataUnit.getEndTimeInMilliseconds() + snrAnnotationParameters.bufferMillis+snrAnnotationParameters.noiseMillis;

		RawDataUnit[] channelData = null;
		try {
			channelData = daqProcess.getRawDataBlock().getAvailableSamples(dataStart, dataEnd-dataStart, pamDataUnit.getChannelBitmap(), true);
		} catch (RawDataUnavailableException e1) {
			System.out.println("SNR Measurement: " + e1.getLocalizedMessage());
			return null;
		} 
		if (channelData == null) {
			System.out.println("SNR Measurement: No raw data available for measurement");
			return null;
		}
		
		int nChan = channelData.length;
		if (nChan == 0) return null;
		double signal = 0, noise = 0;
		int nSig = 0, nNoise = 0;
		double val;
		for (int c = 0; c < nChan; c++) {
			/*
			 * Work out three different regions of the sounds, first noise period (<n1), second noise period (>n2)
			 * sound period of interest (between s1 and s2) 
			 */
			long n1 = acousticDataUnit.getTimeMilliseconds()-snrAnnotationParameters.bufferMillis-channelData[0].getTimeMilliseconds();
			long n2 = acousticDataUnit.getEndTimeInMilliseconds() + snrAnnotationParameters.bufferMillis - channelData[0].getTimeMilliseconds();
			long s1 = acousticDataUnit.getTimeMilliseconds()-channelData[0].getTimeMilliseconds();
			long s2 = acousticDataUnit.getEndTimeInMilliseconds() - channelData[0].getTimeMilliseconds();
			n1 *= sampleRate / 1000.;
			n2 *= sampleRate / 1000.;
			s1 *= sampleRate / 1000.;
			s2 *= sampleRate / 1000.;			
			
			double[] rawData = channelData[c].getRawData();
			long nSamps = rawData.length;
			Filter filter = filterMethod.createFilter(0);
			for (int i = 0; i < nSamps; i++) {
				val = filter.runFilter(rawData[i]);
				if (i < n1 || i > n2) {
					noise += val*val;
					nNoise++;
				}
				else if (i >= s1 && i < s2) {
					signal += val*val;
					nSig++;
				}
			}
		}
		if (nSig == 0 || nNoise == 0) {
			return null;
		}
		signal /= nSig;
		noise /= nNoise;
		double snr = 10.*Math.log10(signal/noise);
		boolean isNew = false;
		SNRAnnotation snrAnnotation = (SNRAnnotation) acousticDataUnit.findDataAnnotation(SNRAnnotation.class);
		if (snrAnnotation == null) {
			snrAnnotation = new SNRAnnotation(this);
			isNew = true;
		}
//		System.out.printf("SNR: %3.2fdB\n", snr);
		snrAnnotation.setSnr(snr);
		if (isNew) {
			acousticDataUnit.addDataAnnotation(snrAnnotation);
		}
		
		return snrAnnotation;
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
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		@Override
		public String getObserverName() {
			return "SNR annotator";
		}
		
	}
	
	@Override
	public boolean canAutoAnnotate() {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getDialogPanel()
	 */
	@Override
	public AnnotationDialogPanel getDialogPanel() {
		return snrAnnotationPanel;
	}

	@Override
	public Class getAnnotationClass() {
		return SNRAnnotation.class;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getSettingsPanel()
	 */
	@Override
	public AnnotationSettingsPanel getSettingsPanel() {
//		if (snrSettingsPanel == null) {
			snrSettingsPanel = new SNRSettingsPanel(this);
//		}
		return snrSettingsPanel;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#hasSettingsPanel()
	 */
	@Override
	public boolean hasSettingsPanel() {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getAnnotationOptions()
	 */
	@Override
	public AnnotationOptions getAnnotationOptions() {
		return snrAnnotationOptions;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#setAnnotationOptions(annotation.handler.AnnotationOptions)
	 */
	@Override
	public void setAnnotationOptions(AnnotationOptions annotationOptions) {
		if (annotationOptions instanceof SNRAnnotationOptions) {
			snrAnnotationOptions = (SNRAnnotationOptions) annotationOptions;
		}
	}

}
