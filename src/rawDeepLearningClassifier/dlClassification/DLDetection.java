package rawDeepLearningClassifier.dlClassification;

import java.util.ArrayList;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;
import annotation.DataAnnotation;
import bearinglocaliser.annotation.BearingAnnotation;
import clipgenerator.ClipSpectrogram;
import rawDeepLearningClassifier.logging.DLAnnotation;

/**
 * A detected DL data unit. These data units are only ever generated from raw 
 * sound data from the segmenter. Otherwise DL results are saved as annotations on 
 * other data units. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DLDetection extends PamDataUnit implements PamDetection, RawDataHolder {
	
	/**
	 * The abstract localisation 
	 */
	private DLLocalisation localisation; 
	
	/**
	 * The raw data unit. 
	 */
	private double[][] waveData;
	
	/**
	 * Holds the raw data transforms for the  wave data. 
	 */
	private RawDataTransforms rawDataTransforms; 


	/**
	 * Create a data unit for DL which has passed binary classification.  
	 * @param timeMilliseconds - the time in milliseconds. 
	 * @param channelBitmap - the channel bit map. 
	 * @param startSample - the start sample. 
	 * @param durationSamples - the duration in samples. 
	 * @param modelResults - the model results that were used to construct the data unit. 
	 */
	@Deprecated
	public DLDetection(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples,
			ArrayList<PredictionResult> modelResults, double[][] waveData) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
		DLAnnotation annotation = new DLAnnotation(null, modelResults); 
		this.addDataAnnotation(annotation);
		this.waveData=waveData; 
		rawDataTransforms= new RawDataTransforms(this); 
		setAmplitude(); //must set amplitude as this is not stored in binary files. 
	}
	
	
	/**
	 * Create a data unit for DL which has passed binary classification. Usually used for 
	 * loading data units from binary files. 
	 * @param baseData - the base binary data. 
	 * @param probdata - the probability data. 
	 * @param waveData - the wave data. 
	 */
	public DLDetection(DataUnitBaseData baseData, double[][] waveData) {
		super(baseData);
		//System.out.println("Load DL deteciton: " + this.getChannelBitmap());
		this.waveData=waveData; 
		rawDataTransforms= new RawDataTransforms(this); 
		setAmplitude();//must set amplitude as this is not stored in binary files. 
	}
	
	private void setAmplitude() {
		//calcuate the amplitude. 
		this.getBasicData().setMeasuredAmplitudeType(DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD);

		double[] amp = new double[waveData.length]; 
		int n = 0; 
		for (double[] waveform:waveData) {
			amp[n] = PamArrayUtils.minmaxdiff(waveform); 
			n++; 
		}
		//the maximum amplitude within the channel group is used. 
		this.getBasicData().setMeasuredAmplitude(PamArrayUtils.max(amp));
	}

	
	
	@Override
	public void addDataAnnotation(DataAnnotation dataAnnotation) {
		super.addDataAnnotation(dataAnnotation); 
		/**
		 * This is a total hack to add bearing info from the bearing module 
		 * so that everything displays nicely on the map.
		 */
		if (dataAnnotation instanceof BearingAnnotation) {
			
			BearingAnnotation bearingAnnotation = (BearingAnnotation) dataAnnotation; 
			
			localisation = new DLLocalisation(this, LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY, 
					PamUtils.getSingleChannel(this.getChannelBitmap())); 

			localisation.setBearing(bearingAnnotation); 
		}
	}
	

	@Override
	public AbstractLocalisation getLocalisation() {
		return this.localisation; 
	}


	@Override
	public double[][] getWaveData() {
		return waveData;
	}
	
	/**
	 * Get the model results that were used to construct the data unit. 
	 * The number of results will generally be the raw data length divided by hop size. 
	 * @return the  model results. 
	 */
	public ArrayList<PredictionResult> getModelResults() {
		DLAnnotation annotation = (DLAnnotation) this. findDataAnnotation(DLAnnotation.class) ;
		if (annotation!=null) return annotation.getModelResults(); 
		else return null; 
	}
	
	
	/**
	 * Get the wave data for a specified channel. 
	 * @param channel
	 * @return
	 */
	private double[] getWaveData(int channel) {
		return this.getWaveData()[channel];
	}


	
	/**
	 * Get a spectrogram image of the wave clip. The clip is null until called. It is recalculated if the 
	 * FFT length and/or hop size are different. 
	 * @param fftSize - the FFT size in samples
	 * @param fftHop - the FFT hop in samples
	 * @return a spectrogram clip (dB/Hz ).
	 */
	public ClipSpectrogram getSpectrogram(int fftSize, int fftHop) {
		return rawDataTransforms.getSpectrogram(fftSize, fftHop);
	}

//	/**
//	 * Get the RawDataTransforms which handles data transforms such as FFT calculations, spectrgorams, cepstrums's etc.  
//	 * @return the raw transforms associated with the data unit. 
//	 */
//	public RawDataTransforms getRawDataTransforms() {
//		return rawDataTransforms;
//	}

	/***
	 * Get the power spectrum for all channels. 
	 * @param fftLength - the fft length to use. 
	 * @return the power sepctrums for all channels. 
	 */
	public double[][] getPowerSpectrum(int fftLength) {
		return getDataTransforms().getPowerSpectrum(fftLength);
	}


	@Override
	public RawDataTransforms getDataTransforms() {
		return this.rawDataTransforms;
	}



}
