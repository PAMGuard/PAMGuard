package spectrogramNoiseReduction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.ProcessAnnotation;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import Spectrogram.SpectrumBackgrounds;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import spectrogramNoiseReduction.averageSubtraction.AverageSubtraction;
import spectrogramNoiseReduction.kernelSmoothing.KernelSmoothing;
import spectrogramNoiseReduction.medianFilter.SpectrogramMedianFilter;
import spectrogramNoiseReduction.threshold.SpectrogramThreshold;
import spectrogramNoiseReduction.threshold.ThresholdParams;

public class SpectrogramNoiseProcess extends PamProcess {

//	private SpectrogramNoiseControl spectrogramNoiseControl;

	protected FFTDataBlock sourceData;
	
	protected FFTDataBlock outputData;

	private ArrayList<SpecNoiseMethod> methods = new ArrayList<SpecNoiseMethod>();
	
	private SpectrogramNoiseSettings noiseSettings = new SpectrogramNoiseSettings();
	
	private SpectrogramThreshold thresholdMethod;

	private SpectrumBackgrounds spectrumBackgrounds = null;
	
	private int totalDelay;
	
	private ComplexArray[] delayedInputData;
	
	public SpectrogramNoiseProcess(PamControlledUnit pamControlledUnit) {
		super(pamControlledUnit, null);
		this.setProcessName(pamControlledUnit.getUnitName() + " Noise reduction");
//		this.spectrogramNoiseControl = spectrogramNoiseControl;

		methods.add(new SpectrogramMedianFilter());
		methods.add(new AverageSubtraction());
		methods.add(new KernelSmoothing());
		methods.add(thresholdMethod = new SpectrogramThreshold());

		addOutputDataBlock(outputData = new FFTDataBlock(pamControlledUnit.getUnitName() + " Noise free FFT data", 
				this, 0, 0, 0));
		outputData.setRecycle(true);
	}
	
	@Override
	public void setupProcess() {
		super.setupProcess();
		
		PamConfiguration mainConfig = PamController.getInstance().getPamConfiguration();
		PamConfiguration localConfig = getPamControlledUnit().getPamConfiguration();
		
		sourceData = (FFTDataBlock) getPamControlledUnit().getPamConfiguration().getDataBlock(FFTDataUnit.class, 
				getNoiseSettings().dataSource);
		setParentDataBlock(sourceData);
		if (sourceData != null) {
			outputData.setLogScale(sourceData.isLogScale());
		}
		
		prepareProcess();
		
		makeAnnotations();
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		/*
		 * Each noise reduction process will work on the data array
		 * in the pamDataUnit in place, so the first thing
		 * to do is to make a copy of the data which can then
		 * be passed in turn to each active noise reduction process
		 * prior to output. 
		 * 
		 * In our attempt to also record background data, we want some kind of spectral 
		 * average out of the noise reduction data. Trouble is that the fft data may have
		 * already been through noise reduction so there may be no way any more of 
		 * getting back at the background. Bugger! Perhaps need to push harder for background
		 * to be always measured within the wsl detector so we can make this measure.  
		 */
		FFTDataUnit fftDataUnit = (FFTDataUnit) arg;
		ComplexArray fftData = fftDataUnit.getFftData();
		
//		shuffle along delayed data
		delayedInputData[totalDelay] = fftData;
		for (int i = 0; i < totalDelay; i++) {
			delayedInputData[i] = delayedInputData[i+1];
		}

		FFTDataUnit newFFTUnit;
//		newFFTUnit = outputData.getRecycledUnit();
//		if (newFFTUnit != null) {
//			newFFTUnit.setInfo(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
//					fftDataUnit.getStartSample(), fftDataUnit.getDuration(), fftDataUnit.getFftSlice());
//		}
//		else {str
			newFFTUnit = new FFTDataUnit(arg.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
					fftDataUnit.getStartSample(), fftDataUnit.getSampleDuration(), null, fftDataUnit.getFftSlice());
			newFFTUnit.setSequenceBitmap(fftDataUnit.getSequenceBitmapObject());
//		}
		/**
		 * Need to ensure a genuine copy is made and that data aren't still 
		 * pointing at the same Complex objects !
		 * But at the same time, recycle where possible
		 */
		ComplexArray newFftData = newFFTUnit.getFftData();
//		if (newFftData == null || newFftData.length() != fftData.length()) {
//			newFftData = new ComplexArray(fftData.length());//outputData.getComplexArray(fftData.length);
//			newFFTUnit.setFftData(newFftData);
//		}
//		for (int i = 0; i < newFftData.length(); i++) {
//			newFftData[i].assign(fftData[i]);
//		}
		newFftData = fftData.clone();
		newFFTUnit.setFftData(newFftData);

		for (int i = 0; i < methods.size(); i++) {
			if (noiseSettings.isRunMethod(i)) {
				methods.get(i).runNoiseReduction(newFFTUnit);
			}
		}
		
		ThresholdParams p = (ThresholdParams) thresholdMethod.getParams();
		if (p.finalOutput == SpectrogramThreshold.OUTPUT_RAW) {
			thresholdMethod.pickEarlierData(fftData, newFFTUnit.getFftData());
		}
		
		// and output the data unit. 
		outputData.addPamData(newFFTUnit);
	}
	
	
	@Override
	public void prepareProcess() {

		super.prepareProcess();
		
		int channelMap = getNoiseSettings().channelList;
//		PamDataBlock source = getSourceDataBlock();
		if (sourceData != null) {
//			channelMap &= source.getChannelMap(); // don't need to do an AND function, since noise process must always use the source channel map
			channelMap = sourceData.getSequenceMap(); 
			
			// next lines copied from setupProcess.  Depending on the order of the modules, setupProcess may not properly set channelMap, FFTHop and FFTLength
			// because they won't have been defined yet for the source data block.  At this stage, they've definitely been defined so set the values now
			noiseSettings.channelList = channelMap;
			outputData.sortOutputMaps(sourceData.getChannelMap(), sourceData.getSequenceMapObject(), channelMap);
			outputData.setFftHop(sourceData.getFftHop());
			outputData.setFftLength(sourceData.getFftLength());
		}
		
		for (int i = 0; i < methods.size(); i++) {
			methods.get(i).initialise(channelMap);
		}
	}

	public SpectrogramNoiseSettings getNoiseSettings() {
		/**
		 * will need to rebuild the array list of individual modules settings
		 * first. 
		 */
		if (noiseSettings == null) {
			noiseSettings = new SpectrogramNoiseSettings();
		}
		noiseSettings.clearSettings();
		for (int i = 0; i < methods.size(); i++) {
			noiseSettings.addSettings(methods.get(i).getParams());
		}
		return noiseSettings;
	}
	
	public void setNoiseSettings(SpectrogramNoiseSettings noiseSettings) {
		this.noiseSettings = noiseSettings;
		if (noiseSettings == null) {
			noiseSettings = getNoiseSettings();
		}
		for (int i = 0; i < methods.size(); i++) {
			if (noiseSettings.getSettings(i) != null) {
				methods.get(i).setParams(noiseSettings.getSettings(i));
			}
		}
		setupProcess();
	}

	public ArrayList<SpecNoiseMethod> getMethods() {
		return methods;
	}



	public void setParentDataBlock(FFTDataBlock fftDataBlock) {
		super.setParentDataBlock(fftDataBlock);
		if (fftDataBlock != null) {
			outputData.setFftHop(fftDataBlock.getFftHop());
			outputData.setFftLength(fftDataBlock.getFftLength());
//			outputData.setChannelMap(fftDataBlock.getChannelMap());
//			outputData.setSequenceMap(fftDataBlock.getSequenceMap());
			outputData.sortOutputMaps(fftDataBlock.getChannelMap(), fftDataBlock.getSequenceMapObject(), fftDataBlock.getChannelMap());
		}
	}

	@Override
	public void pamStart() {

		// work out the total delay, so that data can be sotored for use
		// after the final threhsolding
		totalDelay = 0;
		for (int i = 0; i < methods.size(); i++) {
			totalDelay += methods.get(i).getDelay();
		}
		delayedInputData = new ComplexArray[totalDelay+1];
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public ProcessAnnotation getAnnotation(PamDataBlock pamDataBlock, int annotation) {
		// TODO Auto-generated method stub
		return fftAnnotations.get(annotation);
	}

	@Override
	public int getNumAnnotations(PamDataBlock pamDataBlock) {
		if (fftAnnotations == null) {
			return 0;
		}
		return fftAnnotations.size();
	}
	
	private Vector<ProcessAnnotation> fftAnnotations;
	
	public void makeAnnotations() {
		if (fftAnnotations == null) {
			fftAnnotations = new Vector<ProcessAnnotation>();
		}
		else {
			fftAnnotations.clear();
		}

		for (int i = 0; i < methods.size(); i++) {
			if (noiseSettings.isRunMethod(i)) {
				fftAnnotations.add(methods.get(i).getAnnotation(this));
			}
		}
		outputData.createProcessAnnotations(getSourceDataBlock(), this, true);
	}

	/**
	 * @return the outputData
	 */
	public FFTDataBlock getOutputDataBlock() {
		return outputData;
	}

	@Override
	public int getOfflineData(OfflineDataLoadInfo offlineLoadDataInfo) {
//		System.out.println("generate offline noise reduced fft data");
		prepareProcess();
		pamStart();
		return super.getOfflineData(offlineLoadDataInfo);
	}

	@Override
	public ArrayList getCompatibleDataUnits() {
		return new ArrayList<Class<? extends PamDataUnit>>(Arrays.asList(FFTDataUnit.class));
	}

	/**
	 * The original input to the noise process. May not be normal FFT data, e.g. Mel spectrogram. 
	 * @return the sourceData
	 */
	public FFTDataBlock getSourceData() {
		return sourceData;
	}

	
	
}
