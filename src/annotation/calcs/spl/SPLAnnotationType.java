package annotation.calcs.spl;

import generalDatabase.SQLLoggingAddon;
import noiseOneBand.OneBandDataUnit;
import noiseOneBand.OneBandProcess;
import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Filters.FastIIRFilter;
import Filters.Filter;
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
import annotation.calcs.spl.SPLAnnotation;

/**
 * SPL annotation type. Can add a single double SPL to a data unit. Can 
 * automatically calculate the SPL from the time frequency box of the 
 * data unit. 
 * @author Doug Gillespie
 *
 */
public class SPLAnnotationType extends DataAnnotationType<SPLAnnotation> {

	private SPLSqlAddon splSqlAddon;
	private SPLAnnotationPanel splAnnotationPanel;
	
	public SPLAnnotationType() {
		super();
		splSqlAddon = new SPLSqlAddon(this);
		this.splAnnotationPanel = new SPLAnnotationPanel(this);
	}

	@Override
	public String getAnnotationName() {
		return "SPL";
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return AcousticDataUnit.class.isAssignableFrom(dataUnitType);
	}

	@Override
	public String toString(SPLAnnotation dataAnnotation) {
		// TODO Auto-generated method stub
		return super.toString(dataAnnotation);
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return splSqlAddon;
	}

	@Override
	public SPLAnnotation autoAnnotate(PamDataUnit pamDataUnit) {
		if (canAnnotate(pamDataUnit.getClass()) == false) return null;
		PamDataUnit acousticDataUnit = pamDataUnit;	// originally cast to AcousticDataUnit
		/**
		 * Will need to get waveform data for a sec before, a sec after and during the call 
		 * FFT it, etc. Better don't bother with the FFT, just make an 8 pole filter and add up
		 * the energy.
		 */
		
		AcquisitionProcess daqProcess = findDaqProcess(pamDataUnit);
		if (daqProcess == null) return null;
		
		// we need an actual channel in order to process this, but the pamDataUnit may actually have a sequence.  If that's
		// the case, just use the lowest channel in the channel map
//		int channel = pamDataUnit.getChannelBitmap(); // TODO: Make sure we can handle single channels or multiple channels
		int channel = pamDataUnit.getParentDataBlock().getARealChannel(pamDataUnit.getChannelBitmap());
		
		
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
		
		long dataStart = acousticDataUnit.getTimeMilliseconds();
		long dataEnd = acousticDataUnit.getEndTimeInMilliseconds();
		if (haveRawData(daqProcess.getRawDataBlock(), dataStart, dataEnd) == false) {
			daqProcess.getRawDataBlock().clearAll();
			loadRawData(daqProcess, dataStart, dataEnd);
//			System.out.println("Load SPL raw data from " + PamCalendar.formatTime(dataStart, true) + " to " +
//					PamCalendar.formatTime(dataEnd, true));
			if (haveRawData(daqProcess.getRawDataBlock(), dataStart, dataEnd) == false) {
				return null;
			}
		}
//		else {
//			System.out.println("SPL raw data present " + PamCalendar.formatTime(dataStart, true) + " to " +
//					PamCalendar.formatTime(dataEnd, true));
//		}
		double[][] rawData = null;
		/**
		 * F**** this is annoying . Raw data are loaded according to sample 
		 * number, but we only know milliseconds. This is a real pain !
		 */
		try {
			rawData = daqProcess.getRawDataBlock().getSamplesForMillis(dataStart, dataEnd-dataStart, channel);
		} catch (RawDataUnavailableException e) {
			System.out.println("Can find data for SPL annotation " + e.getMessage());
//			try {
//				rawData = daqProcess.getRawDataBlock().getSamplesForMillis(dataStart, dataEnd-dataStart, pamDataUnit.getChannelBitmap());
//			} catch (RawDataUnavailableException e1) {
//				// TODO Auto-generated catch block
////				e1.printStackTrace();
//			}
			return null;
		}
		if (rawData == null) return null;
		long s1 = (long) 0;
		long s2 = (long) (s1 + acousticDataUnit.getDurationInMilliseconds() * sampleRate / 1000);
		double [][] filteredData = new double[rawData.length][rawData[0].length];
		for (int c = 0; c<rawData.length; c++){
			System.arraycopy(rawData[c], 0, filteredData[c], 0, rawData[0].length);
		}
		int nChan = rawData.length;
		if (nChan == 0) return null;
		if (nChan != 1) System.out.println("Warning, multiple channels not yet supported properly for SPL annotations");
		int nSamps = rawData[0].length;
		double signal = 0, noise = 0;
		int nSig = 0, nNoise = 0;
		double dataSum = 0, dataSum2 = 0;
		double maxVal = 0, minVal = 0, lastMax = 0, lastMin = 0; 
		double maxPP = 0, minPP = 0, maxZP = 0, minZP = 0;
		int n = filteredData[0].length;
		boolean isMin, isMax;
		double val;
		for (int c = 0; c < nChan; c++) {
			Filter filter = filterMethod.createFilter(0);
			for (int i = 0; i < nSamps; i++) {
				val = filter.runFilter(rawData[c][i]);
				if (i < s1) { // Noise measurement before the signal
					noise += val*val;
					nNoise++;
				}
				else if (i >= s1 && i < s2) { // Signal measurement
					filteredData[c][(int) (i-s1)] = val;
					signal += val*val;
					nSig++;
					dataSum += filteredData[c][i];
					dataSum2 += (filteredData[c][i]*filteredData[c][i]);
					maxVal = Math.max(maxVal, filteredData[c][i]);
					minVal = Math.min(minVal, filteredData[c][i]);
					if (i > 0 && i < n-1) {
						isMax = (filteredData[c][i] > filteredData[c][i-1]);
						isMin = (filteredData[c][i] < filteredData[c][i-1]);
						if (isMax) {
							lastMax = filteredData[c][i];
							maxPP = Math.max(maxPP, lastMax-lastMin);
							maxZP = Math.max(maxZP, lastMax);
						}
						if (isMin) {
							lastMin = filteredData[c][i];
							maxPP = Math.max(maxPP, lastMax-lastMin);
							maxZP = Math.max(maxZP, -lastMin);
						}
					}
				}
			}
		}
//		if (nSig == 0) || nNoise == 0) {
//			return null;
//		}
//		signal /= nSig;
//		noise /= nNoise;
//		double spl = 10.*Math.log10(signal/noise);

		SPLAnnotation splAnnotation = (SPLAnnotation) acousticDataUnit.findDataAnnotation(SPLAnnotation.class);
		double rms = Math.sqrt((dataSum2-(dataSum/n))/(n-1));
		double seconds = (dataEnd - dataStart)/1000.; 
		
		boolean isNew = false;
		if (splAnnotation == null) {
			splAnnotation = new SPLAnnotation(this);
			isNew = true;
		}
		splAnnotation.setRms(daqProcess.rawAmplitude2dB(rms, channel, false));
		splAnnotation.setZeroPeak(daqProcess.rawAmplitude2dB(maxZP, channel, false));
		splAnnotation.setPeakPeak(daqProcess.rawAmplitude2dB(maxPP, channel, false));
		double selPa = Math.pow(10., splAnnotation.getRms()/10.) * seconds; // Sound exposure level in Pascals (NOT dB!)
		splAnnotation.setSEL(10*Math.log10(selPa));
		
		if (isNew) {
			acousticDataUnit.addDataAnnotation(splAnnotation);
		}
	
		return splAnnotation;
	}
	
	
	private AcquisitionProcess findDaqProcess(PamDataUnit pamDataUnit) {
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
		return daqProcess;
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
			return "SPL annotator";
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
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
		return splAnnotationPanel;
	}	

	@Override
	public Class getAnnotationClass() {
		return SPLAnnotation.class;
	}	


	
}
