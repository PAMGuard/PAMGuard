package Azigram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.ProcessAnnotation;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import spectrogramNoiseReduction.SpecNoiseMethod;;

/**
 * Azigram process will take FFT data from a DIFAR sonobuoy source, demux, and
 * then compute Azigram (Thode et al 2019 JASA). 
 * NB: This module uses SpectrogramNoiseProcess as a template since they both
 * take FFT data as input, modify it, and spit out the modified FFT data
 * in a package that is fit for purpose for display on spectrogram.
 * @author brian_mil
 *
 */
public class AzigramProcess extends PamProcess {
	
	/**
	 * FFT data source for the noise reduction and threshold process. 
	 */
	private FFTDataBlock sourceData;
	
	/**
	 * FFT data source containing Azigram data (
	 * For now it's just an FFT datablock, but with FFT 
	 */
	private FFTDataBlock azigramData;
	
	private SpecNoiseMethod azigramMethod;
	
	private AzigramControl azigramControl;
	
	private float inputSampleRate;
	
	private int outputFftLength, outputFftHop;

	private double decimateFactor;
	
	
	public AzigramProcess(PamControlledUnit pamControlledUnit, FFTDataBlock parentDataBlock) {
		super(pamControlledUnit, parentDataBlock);
				
		this.azigramControl = (AzigramControl) pamControlledUnit;
		setSampleRate(parentDataBlock.getSampleRate(), true);
		decimateFactor = inputSampleRate / azigramControl.azigramParameters.outputSampleRate;
		azigramData = new FFTDataBlock(pamControlledUnit.getUnitName() + " Azigram FFT data", 
				this, 0, 0, 0);
		azigramData.setSampleRate(azigramControl.azigramParameters.outputSampleRate,true);
		addOutputDataBlock(azigramData);
		azigramData.setRecycle(true);
		
	}

	/**
	 * Copied from decimatorW module 
	 */
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		inputSampleRate = sampleRate;
		if (azigramControl != null) {
			super.setSampleRate(azigramControl.azigramParameters.outputSampleRate, notify);
			decimateFactor = inputSampleRate/azigramControl.azigramParameters.outputSampleRate;
		}
	}
	
	/**
	 * As per decimator module 
	 */
	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// Don't do anything. Update decimator output when data are added. 
//		super.masterClockUpdate(milliSeconds, (long) (sampleNumber/decimateFactor));
	}

	/** 
	 * An AzigramProcess has one input stream.  Return it, or null if it's
	 * not available.
	 */
	public PamDataBlock getInputDataBlock() {
		AzigramParameters p = azigramControl.azigramParameters;
		
		return (p == null || p.dataSource.getDataSource() == null) ? getParentDataBlock() :
			PamController.getInstance().getDataBlock(FFTDataUnit.class, p.dataSource.getDataSource());
	}
	
	@Override
	public void setupProcess() {
		super.setupProcess();
		
		if (getParentDataBlock() != null) 
			getParentDataBlock().deleteObserver(this);
		if (azigramControl == null) 
			return;
		
		sourceData = (FFTDataBlock) getInputDataBlock();
		setParentDataBlock(sourceData);		//in case it wasn't parent already
		if (sourceData != null)
			sourceData.addObserver(this);	//should happen in setParentDataBlock, but doesn't always
		
		AzigramParameters p = azigramControl.azigramParameters;
		azigramData.sortOutputMaps(sourceData.getChannelMap(), 
				sourceData.getSequenceMapObject(),
				p.dataSource.getChanOrSeqBitmap());
		
		prepareProcess();
	
	}
	
	@Override
	public void prepareProcess() {

		this.setSampleRate(sourceData.getSampleRate(), true);
		int channelMap = azigramControl.azigramParameters.channelBitmap;
//		PamDataBlock source = getSourceDataBlock();
		
		if (sourceData != null) {
			channelMap = sourceData.getSequenceMap(); 
			azigramControl.azigramParameters.channelBitmap = channelMap;
			
			inputSampleRate = sourceData.getSampleRate(); 
			outputFftLength = sourceData.getFftLength();
			outputFftHop = sourceData.getFftHop();
			
			// Decimation factor needs to be an integer, and divisor of fftLength
			// For now assume inputSampleRate is 48 kHz, and output is 6 kHz
			outputFftLength = (int) (sourceData.getFftLength()/decimateFactor);
			outputFftHop = (int) (sourceData.getFftHop()/decimateFactor);
			
			freqBins = new Double[outputFftLength];
			for (int i = 0; i < outputFftLength; i++) {
				freqBins[i] = (double) (sampleRate * i / outputFftLength);  
			}

			azigramData.sortOutputMaps(sourceData.getChannelMap(), sourceData.getSequenceMapObject(), channelMap);
			azigramData.setFftHop(outputFftHop);
			azigramData.setFftLength(outputFftLength);
			azigramData.setSampleRate(azigramControl.azigramParameters.outputSampleRate, true);
		
		}
	}


	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		/*
		 * First thing
		 * to do is to make a copy of the data which can then
		 * be passed in turn to demux and Azigram
		 * prior to noise output. 
		 */
		FFTDataUnit fftDataUnit = (FFTDataUnit) arg;

		AzigramDataUnit newFFTUnit = new AzigramDataUnit(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
				fftDataUnit.getStartSample(), (long) (fftDataUnit.getSampleDuration()/decimateFactor), fftDataUnit.getFftData(), fftDataUnit.getFftSlice());
		newFFTUnit.setSequenceBitmap(fftDataUnit.getSequenceBitmapObject());
		newFFTUnit.setDurationInMilliseconds(fftDataUnit.getDurationInMilliseconds());


		runDemux(newFFTUnit);
		
		runAzigram(newFFTUnit);
		
//		for (int i = 0; i < methods.size(); i++) {
//			if (noiseSettings.isRunMethod(i)) {
//				methods.get(i).runNoiseReduction(newFFTUnit);
//			}
//		}
		
//		ThresholdParams p = (ThresholdParams) thresholdMethod.getParams();
//		if (p.finalOutput == SpectrogramThreshold.OUTPUT_RAW) {
//			thresholdMethod.pickEarlierData(fftData, newFFTUnit.getFftData());
//		}
		
		// and output the data unit. 
		azigramData.addPamData(newFFTUnit);

	}
	
	
	private void runDemux(AzigramDataUnit newFFTUnit) {

		/*
		 * Locate the pilot tones of the multiplexed DIFAR signals
		 * These should be at or very close to 7.500 and 15.000 kHz, and they
		 * should have higher amplitude than any signal from the acoustic 
		 * sensors
		 */
		double deltaF =  inputSampleRate / sourceData.getFftLength();
		double Bwidth = 25; // Bandwidth above and below pilots in Hz;
		ComplexArray fftData = newFFTUnit.getFftData();

		int loIndex = (int) Math.floor((7500-Bwidth) / deltaF);
		int hiIndex = (int) Math.ceil((7500+Bwidth) / deltaF);
	
		int i75 = 0;
		double max = 0;
		for (int i = loIndex; i < hiIndex; i++) {
			double fftMag = fftData.mag(i); 
			if (fftMag > max) {
				max = fftMag;
				i75 = i;
			}
		}

		int i15 = 0;
		loIndex = (int) Math.floor((2*7500-Bwidth) / deltaF);
		hiIndex = (int) Math.ceil((2*7500+Bwidth) /  deltaF);
		
		max = 0;
		for (int i = loIndex; i < hiIndex; i++) {
			double fftMag = fftData.mag(i);
			if (fftMag > max) {
				max = fftMag;
				i15 = i;
			}
		}
//		System.out.println("Freq pilot: " + i75 + "; " + freqBins[i75] + 
//						   " Phase pilot: "  + i15 + "; " + freqBins[i15]);		
		Complex pilotPhase = new Complex(0,fftData.ang(i15)*-1).exp();


		/*
		 * These directional signals are double-sideband modulated around the 
		 * 15 KHz pilot tone. Here we use a bit of complex-number math and array
		 * book-keeping to demux the North-South and East-West directional 
		 * signals, and put them in their own arrays. 
		 * 
		 * This follows Thode et al 2019 J. Acoust. Soc. Am. Vol 146(1) pp95-102
		 * (doi: 10.1121/1.5114810).
		 */
		double[] fftReal = fftData.getReal();
		double[] fftImag = fftData.getImag();
		
		

		// P is omnidirectional signal in freq domain from [0-maxFreq) kHz
		// Frequency domain downsampling -- maxFreq is the new sample rate 
		float maxFreq = azigramControl.azigramParameters.getOutputSampleRate()/2;
		int lastIndex = (int) Math.floor(maxFreq / deltaF);

		//Arrays.copyOfRange(double[] original, int from, int to)
		ComplexArray P = new ComplexArray(Arrays.copyOfRange(fftReal, 1, lastIndex+1), 
				Arrays.copyOfRange(fftImag,  1,  lastIndex+1));

		int N = P.length();
		
		int iStart = i15-N;
		int iStop = iStart + N;
		ComplexArray SmFlip = new ComplexArray(
				Arrays.copyOfRange(fftReal, iStart, iStop),
				Arrays.copyOfRange(fftImag, iStart, iStop));
		
		ComplexArray Sm = new ComplexArray(SmFlip.length());
		for (int i=0; i<SmFlip.length(); i++) {
			Sm.set(Sm.length()-i-1, SmFlip.getReal(i), SmFlip.getImag(i));
		}
		
		iStart = i15+1;
		iStop = iStart + N;
		ComplexArray Sp = new ComplexArray(
				Arrays.copyOfRange(fftReal, iStart, iStop),
				Arrays.copyOfRange(fftImag, iStart, iStop));

		// Correct the amplitude by the amount that the FFT length changed
		P.internalTimes(1/decimateFactor);
		Sm.internalTimes(1/decimateFactor);
		Sp.internalTimes(1/decimateFactor);
		
//		double[] F = new double[N]; 
//		for (int i = 0; i<N; i++) {
//			F[i] = i * deltaF;
//		}
		
		Sm = Sm.times(pilotPhase);
		Sp = Sp.times(pilotPhase);

		double[] ReMi = Sp.plus(Sm).getReal();
		double[] ReMq = Sp.plus(Sm).getImag();
		double[] ImMi = Sp.minus(Sm).getImag();
		double[] ImMq = Sp.minus(Sm).getReal();
		for (int i = 0; i<ImMq.length; i++)
			ImMq[i] = -1.0 * ImMq[i];

		ComplexArray Mi = new ComplexArray(ReMi, ImMi);
		ComplexArray Mq = new ComplexArray(ReMq, ImMq);
		
		double[] F = new double[N];
		double[] vx = P.conjTimes(Mi).getReal();
		for (int i = 0; i < vx.length; i++) {
			vx[i] = -1.0 * vx[i];
			F[i] = i * deltaF;
		}
		
		double[] vy = P.conjTimes(Mq).getReal();
		
		newFFTUnit.setP(P);
		newFFTUnit.setFftData(P);
		newFFTUnit.setF(F);
		newFFTUnit.setVx(vx);
		newFFTUnit.setVy(vy);
	}


	private void runAzigram(AzigramDataUnit du) {
		int len = du.getVx().length;
		double[] mu = new double[len];
		double[] mag = new double[len];
		double[] vx = du.getVx();
		double[] vy = du.getVy();
		double angle;
		for (int i = 0; i < len; i++) {
			angle = Math.atan2(vx[i], vy[i])*180/Math.PI;
			mu[i] = PamUtils.constrainedAngle(angle, 360);
			mag[i] = 20* Math.log10(Math.sqrt(vx[i]*vx[i] + vy[i]*vy[i])); 
		}
		
		du.setDirectionalAngle(mu);
		du.setDirectionalMagnitude(mag);
	}

	
	
	@Override
	public void pamStart() {
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

	private Double[] freqBins;
	
	public void makeAnnotations() {
		if (fftAnnotations == null) {
			fftAnnotations = new Vector<ProcessAnnotation>();
		}
		else {
			fftAnnotations.clear();
		}

		fftAnnotations.add(new ProcessAnnotation(this, this, "Azigram", "Demux"));
			
		azigramData.createProcessAnnotations(getSourceDataBlock(), this, true);
	}

	/**
	 * @return the outputData
	 */
	public FFTDataBlock getOutputDataBlock() {
		return azigramData;
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

}
