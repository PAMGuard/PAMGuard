package matchedTemplateClassifer;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.commons.lang3.ArrayUtils;
import org.jamdev.jpamutils.wavFiles.WavInterpolator;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;

import Filters.SmoothingFilter;
import Localiser.DelayMeasurementParams;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.PamArrayUtils;
import PamUtils.PamInterp;
import PamUtils.complex.ComplexArray;
import fftManager.FastFFT;

/**
 * Parameters and useful functions for a single MT classifier. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MTClassifier implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	
	/**
	 * Threshold to classify 
	 */
	public double thresholdToAccept=0.01; 
	
	/**
	 * The normalisation
	 */
	public int normalisation = 0; 
	
	/**
	 * The current sample rates of the waveforms
	 */
	public double currentSr = -1; 	
	
	/**
	 * Copy of the current match waveform which is at the sampleRate specified by {@link currentSr}
	 */
	private double[] interpWaveformMatch;
	
	
	/**
	 * Copy of the current reject waveform which is at the sampleRate specified by {@link currentSr}
	 */
	private double[] inteprWaveformReject;
		
	
	/**
	 * The waveform match FFT
	 */
	private ComplexArray waveformMatchFFT;
	
	/**
	 * The waveform match FFT
	 */
	private ComplexArray waveformRejectFFT;
	
	/**
	 * The waveform to match
	 */
	public MatchTemplate waveformMatch =  new MatchTemplate("Beaked Whale", DefaultTemplates.beakedWhale1, 192000);
	
	/**
	 * The waveform to reject
	 */
	public MatchTemplate waveformReject=  new MatchTemplate("Dolphin", DefaultTemplates.dolphin1, 192000);
	
	/**
	 * Fast FFT 
	 */
	private transient FastFFT fft;


	public final static int TEST_FFT_LENGTH=300; 
	
	/**
	 * Decimates waveforms. 
	 */
	transient private WavInterpolator wavInterpolator = new WavInterpolator(); 
	
//	/**
//	 * The delay measurment parameters. 
//	 */
//	private transient DelayMeasurementParams delayMeasurementParams = defualtDelayParams(); 
//
//	/**
//	 * Runs the cross correlation algorithm. 
//	 */
//	private transient Correlations correlations = new Correlations(); 
	
	/**
	 * Default MT classifier 
	 */
	public MTClassifier(){
		fft= new FastFFT();
	}

	private DelayMeasurementParams defualtDelayParams() {
		DelayMeasurementParams delayMeasurementParams = new DelayMeasurementParams(); 
		//delayMeasurementParams.setUpSample(4);
		delayMeasurementParams.setFftFilterParams(null);

		return delayMeasurementParams;
	}

	/**
	 * Get the match waveform FFT for the sampleRate.
	 * @param fftLength - the FFTlength to use. Bins around waveform peak are used. 
	 * @param sR - the sample rate in samples per second
	 */
	public ComplexArray getWaveformMatchFFT(float sR, int length) {
		if (waveformMatchFFT==null || currentSr!=sR) {
			if (fft==null) fft=new FastFFT();
			
			
//			System.out.println("interpWaveform: " + waveformMatch.waveform.length + " sR " + waveformMatch.sR);

			//re-sample the waveform if the sample rate is different
			this.interpWaveformMatch=interpWaveform(this.waveformMatch, sR); 
			
			//System.out.println("interpWaveformMatch: " + interpWaveformMatch.length + " sR " + sR);
			
			//normalise
			//this.interpWaveformMatch=PamArrayUtils.normalise(interpWaveformMatch);
			
			//this.inteprWaveformReject=PamArrayUtils.divide(interpWaveformMatch, PamArrayUtils.max(interpWaveformMatch));
			this.interpWaveformMatch = normaliseWaveform(interpWaveformMatch, this.normalisation);
			
//			System.out.println("MatchNorm: MATCH");
//			MTClassifierTest.normalizeTest(interpWaveformMatch);
			
			/**
			 * There is an issue here because, if we have a long template waveform, then it
			 * will become truncated and the actual waveform may be missed. This means we
			 * have to use the peak of the template
			 */
			waveformMatchFFT = calcTemplateFFT(interpWaveformMatch,  length); 
			
			//need to calculate the complex conjugate - note that originally I was flipping the array but this means 
			//the max value does not equal one with identical waveforms...doh. 
			waveformMatchFFT = waveformMatchFFT.conj();
//			System.out.println("waveformMatch: " + waveformMatch.waveform.length + 
//					" interpWaveformMatch: " + interpWaveformMatch.length + " for " +sR + " sr "); 

		}
		return waveformMatchFFT; 
	}
	
	
	/**
	 * Calculate the FFT of an interpolate match template. 
	 * @param interpTemplateWaveform - the waveform interpolated to the correct sample rate. 
	 * @param length - the length of the FFT. 
	 * @return the FFT of the waveform as a complex array. 
	 */
	private ComplexArray calcTemplateFFT(double[] interpTemplateWaveform, int length) {
		
		ComplexArray fftTemplate;
		/**
		 * There is an issue here because, if we have a long template waveform, then it
		 * will become truncated and the actual waveform may be missed. This means we
		 * have to use the peak of the template
		 */
		if (interpTemplateWaveform.length>length) {
			//If the template is long then need to find the peak, otherwise we will end up cross correlating with noise at the
			//start of the template. 
			//because this is a template and not a random click we don't need to be so clever with how we find peaks. Find 
			//the maximum and use around that. 
			int pos = PamArrayUtils.maxPos(interpTemplateWaveform); 
			
			int startind = Math.max(0, pos-length/2);
			int endind = startind+length-1;

			double[] peakTemplate = ArrayUtils.subarray(interpTemplateWaveform, startind, endind); 
			fftTemplate = fft.rfft(peakTemplate, length);
		}
		else {
			//template waveform is padded by fft function
			fftTemplate = fft.rfft(interpTemplateWaveform, length); 
		}
		
		return fftTemplate; 
	}
	

	
	/**
	 * Get the match waveform for the sample rate 
	 * @param sR - the sample rate in samples per second
	 */
	public ComplexArray getWaveformRejectFFT(float sR, int length) {
		if (waveformRejectFFT==null || currentSr!=sR) {
			if (fft==null) fft=new FastFFT();
			
			//re-sample the waveform
			this.inteprWaveformReject=interpWaveform(this.waveformReject, sR);
			
//			System.out.println("MatchNorm: REJECT BEFORE INTERP");
//			MTClassifierTest.printWaveform(waveformReject.waveform);
//			System.out.println("MatchNorm: REJECT AFTER INTERP");
//			MTClassifierTest.printWaveform(inteprWaveformReject);

			//normalise
//			this.inteprWaveformReject=PamArrayUtils.normalise(inteprWaveformReject);
			
//			this.inteprWaveformReject=PamArrayUtils.divide(inteprWaveformReject, PamArrayUtils.max(inteprWaveformReject));
			this.inteprWaveformReject = normaliseWaveform(inteprWaveformReject, this.normalisation);

//			System.out.println("MatchNorm: REJECT ");
//			MTClassifierTest.normalizeTest(inteprWaveformReject);
//			MTClassifierTest.printWaveform(inteprWaveformReject);
			
			/**
			 * There is an issue here because, if we have a long template waveform, then it
			 * will become truncated and the actual waveform may be missed. This means we
			 * have to use the peak of the template
			 */
			waveformRejectFFT = calcTemplateFFT(inteprWaveformReject,  length); 

			
			//need to calculate the complex conjugate - note that originally I was flipping the array but this means 
			//the max value does not equal one with identical waveforms...doh. 
			waveformRejectFFT = waveformRejectFFT.conj(); 
		}
		return waveformRejectFFT; 
	}
	
	/**
	 * Normalise the waveform. 
	 * @param waveform
	 * @param normeType
	 * @return
	 */
	public static double[] normaliseWaveform(double[] waveform, int normeType) {
		double[] newWaveform = null;
		switch(normeType) {
		case MatchedTemplateParams.NORMALIZATION_NONE:
			newWaveform = waveform;
			break; 
		case MatchedTemplateParams.NORMALIZATION_PEAK:
			newWaveform =PamUtils.PamArrayUtils.divide(waveform, PamUtils.PamArrayUtils.max(waveform));
			break;
		case MatchedTemplateParams.NORMALIZATION_RMS:
			newWaveform =PamUtils.PamArrayUtils.normalise(waveform);
			break;
		}
		return newWaveform;
	}
	
	
	/**
	 * Get the FFT length which should be used for the current sample rate.
	 * @param length 
	 * @param the sample rate. 
	 * @return the FFT length to be used.This is the maximum length of the two templates. 
	 */
	@Deprecated
	public int getFFTLength(float sR) {
//		return TEST_FFT_LENGTH; 
		
		if (inteprWaveformReject==null || interpWaveformMatch==null || currentSr!=sR) {
			interpTamplateWaveforms(sR);
		}
		return Math.max(inteprWaveformReject.length,interpWaveformMatch.length);
	}
	
	/**
	 * Reset all interpolated waveforms etc. 
	 */
	public void reset() {
		waveformMatchFFT=null; 
		waveformRejectFFT=null;
		inteprWaveformReject=null; 
		interpWaveformMatch=null; 
	}
	
	/**
	 * Interpolate both reject and match waveforms so that they match the sample rate
	 * @param sR - the sample rate.  
	 */
	private void interpTamplateWaveforms(float sR) {
		this.inteprWaveformReject=interpWaveform(this.waveformReject, sR); 
		this.interpWaveformMatch=interpWaveform(this.waveformMatch, sR); 
	}
	
	/**
	 * Single double value in MATLAB
	 * @param value - the value  
	 * @return
	 */
	private MLDouble mlDouble(double value) {
		return new MLDouble(null, new double[]{value}, 1);
	} 
	
	
	/**
	 * Tests the correlation function by outputting data to .mat file. Calculate a
	 * value for the click reject and match template ration correlation.
	 * 
	 * @param click
	 *            - the input click to perform correlation match on. It should be
	 *            the same length and sample rate as the templates.
	 * @param sR-
	 *            the sample rate in samples per second

	 * @return an ML Structure with results of all stuff. 
	 */
	public MLStructure calcCorrelationMatchTest(ComplexArray click, float sR) {
	
		MLStructure mlStruct = new MLStructure("matchdata", new int[] {1,1});
		mlStruct.setField("waveform_fft",  complexArray2MLArray(click));
	
		
		//set the stored sR
		currentSr=sR;
		
		int fftLength = TEST_FFT_LENGTH; 
				
//				getFFTLength(sR);

		ComplexArray matchResult= new ComplexArray(fftLength); 
		
		ComplexArray matchTemplate = getWaveformMatchFFT(sR, matchResult.length()); 
		
//		System.out.println("Match template length: " + matchTemplate.length() + "Click : " + click.length()); 
		
		for (int i=0; i<matchTemplate.length(); i++) {
			matchResult.set(i, click.get(i).times(matchTemplate.get(i)));
		}
		
		
		ComplexArray rejectResult= new ComplexArray(fftLength); 
		ComplexArray rejectTemplate = getWaveformRejectFFT(sR, matchResult.length()); 
		
		for (int i=0; i<rejectTemplate.length(); i++) {
			rejectResult.set(i, click.get(i).times(rejectTemplate.get(i)));
		}
		
		//add data to struct her ebecause some arrays get overwritten
		mlStruct.setField("match_template_waveform",  new MLDouble(null, new double[][] {this.interpWaveformMatch}));
		mlStruct.setField("reject_template_waveform", new MLDouble(null, new double[][] {this.inteprWaveformReject}));

		//add data to struct her ebecause some arrays get overwritten
		mlStruct.setField("match_template_fft",  complexArray2MLArray(matchTemplate));
		mlStruct.setField("reject_template_fft",  complexArray2MLArray(rejectTemplate));
		mlStruct.setField("match_result_corr",  complexArray2MLArray(matchResult));
		mlStruct.setField("reject_result_corr",  complexArray2MLArray(rejectResult));
		
		//must use scaling to get the same result as MATLAB 
		if (fft==null) fft= new FastFFT();
		fft.ifft(matchResult,  fftLength, true);
		fft.ifft(rejectResult, fftLength, true);
	
		//need to take the real part of the result and multiply by 2 to get same as 
		//ifft function in MATLAB
		double[] matchReal = new double[matchResult.length()]; 
		double[] rejectReal = new double[rejectResult.length()]; 
		
		for (int i=0; i<matchResult.length(); i++) {
			matchReal[i]=matchResult.getReal(i); 
			rejectReal[i]=rejectResult.getReal(i); 
		}

		double result = 2*(PamArrayUtils.max(matchReal)-PamArrayUtils.max(rejectReal)); 
		
		mlStruct.setField("match_result_ifft",  complexArray2MLArray(matchResult));
		mlStruct.setField("reject_result_ifft",  complexArray2MLArray(rejectResult));
		mlStruct.setField("result", mlDouble(result));
		mlStruct.setField("sR",   mlDouble(sR));


		return mlStruct;
	}
	
	
	/**
	 * Convert a PG ComplexArray to an ML complex array for export. 
	 * @param complexArray the complex array to export. 
	 * @return the ML array. 
	 */
	private MLDouble complexArray2MLArray(ComplexArray complexArray) {
		MLDouble matchTemplateML = new MLDouble(null, new int[]{complexArray.length(),1}, MLArray.mxDOUBLE_CLASS, MLArray.mtFLAG_COMPLEX ); 
		
		for (int i=0; i<complexArray.length(); i++) {
			matchTemplateML.setReal(complexArray.getReal(i), i);
			matchTemplateML.setImaginary(complexArray.getImag(i), i);

		}
		return matchTemplateML;
	}
	
	
	/**
	 * Calculate a value for the click reject and match template ration correlation
	 * 
	 * @param click
	 *            - the input click FFT to perform correlation match on. It should be
	 *            the same length and sample rate as the templates.
	 * @param sR
	 *            - the sample rate in samples per second
	 * @return the correlation reject match template.
	 */
	public MatchedTemplateResult calcCorrelationMatch(ComplexArray click, float sR) {
		
		//set the stored sR
		currentSr=sR;
		
		System.out.println("Waveform click: len: " + click.length()); 

		//System.out.println("Waveform Reject max: " + PamArrayUtils.max(this.inteprWaveformReject)+ " len " + interpWaveformMatch.length); 

		//int fftLength = getFFTLength(sR);
		//int fftLength = this.getFFTLength(sR); 
		
		//System.out.println("click: Waveform click: " + click.length()); 

	
		/**
		 * 10/11/2021 An FFT length based on sample rate was being use for some reason but this will never 
		 * work with long clicks from, for example, a SoundTrap click detector. The FFT length is now based on
		 * input waveform and not the sample rate...
		 */
		int fftLength = click.length()*2;
		
		ComplexArray matchResult= new ComplexArray(fftLength); 
		ComplexArray matchTemplate = getWaveformMatchFFT(sR,fftLength); //remember this is the  complex conjugate
		
//		System.out.println("matchTemplate interp: len: " + interpWaveformMatch.length+ " max: " +  PamArrayUtils.max(interpWaveformMatch)); 
//		System.out.println("matchTemplate: len: " + waveformMatch.waveform.length+ " max: " +  PamArrayUtils.max(waveformMatch.waveform)); 
//		System.out.println("matchTemplate interp: len: " + interpWaveformMatch.length+ " max: " +  PamArrayUtils.max(interpWaveformMatch)); 

		System.out.println("matchTemplate len: " + matchTemplate.length() + " click.length(): "  +click.length()); 
		for (int i=0; i<Math.min(matchTemplate.length(), click.length()); i++) {
			matchResult.set(i, click.get(i).times(matchTemplate.get(i)));
		}
		
		ComplexArray rejectResult= new ComplexArray(fftLength); 
		ComplexArray rejectTemplate = getWaveformRejectFFT(sR, fftLength); //remember this is the  complex conjugate
		for (int i=0; i<Math.min(rejectTemplate.length(), click.length()); i++) {
			rejectResult.set(i, click.get(i).times(rejectTemplate.get(i)));
		}
		
		System.out.println("Waveform Match max: " + PamArrayUtils.max(this.interpWaveformMatch)); 
		System.out.println("Waveform Reject max: " + PamArrayUtils.max(this.inteprWaveformReject)); 
		
		//System.out.println("Click input: " + click.length() + " click template: " + matchTemplate.length());

		//must use scaling to get the same result as MATLAB 
		if (fft==null) fft= new FastFFT();
		fft.ifft(matchResult,  fftLength, true);
		fft.ifft(rejectResult, fftLength, true);

		//System.out.println("Inverse MATCH RESULTS");
	
		//need to take the real part of the result and multiply by 2 to get same as 
		//ifft function in MATLAB - dunno why this is...
		double[] matchReal = new double[matchResult.length()]; 
		double[] rejectReal = new double[rejectResult.length()]; 
		
		for (int i=0; i<matchResult.length(); i++) {
			matchReal[i]=2*matchResult.getReal(i); 
			rejectReal[i]=2*rejectResult.getReal(i); 
			//System.out.println("iFFt match result: " + matchResult.get(i) + " iFFT rejectResult: " + rejectResult.get(i) );
		}
		
//		System.out.println("iFFT Real MATCH");
//		MTClassifierTest.printWaveform(matchReal);
//		System.out.println("-------------------------------------");
//		System.out.println("iFFT Real REJECT");
//		MTClassifierTest.printWaveform(rejectReal);
		

		double maxmatch=PamArrayUtils.max(matchReal); 
		double maxreject=PamArrayUtils.max(rejectReal); 
		
		
//		//TEST
//		if (correlations==null) correlations=new Correlations(); 
//		TimeDelayData matchResultTD = correlations.getDelay(click,  matchTemplate.conj(), null, sR,  fftLength); 
//		System.out.println("Old xcorr method: " + maxmatch + " new PG method: " + matchResultTD.getDelayScore()); 
		
//		//TEST
		
		//if the reject template is set to "none" then reject template will return a NaN 
		//TODO bit messy and inefficient. 
		double result; 
//		double maxReject = PamArrayUtils.max(rejectReal); 
		if (Double.isNaN(maxreject)) {
			result = PamArrayUtils.max(matchReal);
		}
		else {
			result = PamArrayUtils.max(matchReal)-maxreject; 
		}

		System.out.println("Match corr " + maxmatch + " Reject Corr: " + maxreject);

		MatchedTemplateResult matchTmpResult = new MatchedTemplateResult(); 
		matchTmpResult.threshold=result; 
		matchTmpResult.matchCorr=maxmatch; 
		matchTmpResult.rejectCorr=maxreject;
		
		return matchTmpResult;
	}
	
	
	/**
	 * Get the match waveform for the sample rate 
	 * @param waveformMatch - the template to interpolate or decimate. 
	 * @param sR - the target sample rate in samples per second
	 */
	private double[] interpWaveform(MatchTemplate waveformMatch, double sR) {
//		System.out.println("Interp waveform: " + " old: " + waveformMatch.sR + " new: " +  sR);
		
		if (waveformMatch.sR<sR) {
			//up sample
			double[] interpWaveformMatch=reSampleWaveform(waveformMatch.waveform, waveformMatch.sR, sR);
			return interpWaveformMatch; 
		}
		else if (waveformMatch.sR>sR){
			//decimate
//			//TODO - make a better decimator?
//			double[] interpWaveformMatch=reSampleWaveform(waveformMatch.waveform, waveformMatch.sR, sR);
//			return interpWaveformMatch; 
			if (wavInterpolator == null) wavInterpolator = new WavInterpolator(); 
			return wavInterpolator.decimate(waveformMatch.waveform, waveformMatch.sR, (float) sR); 
		}
		else {
			//nothing needed
			return waveformMatch.waveform;
		}		
	}
	
	
	public double[] binArray(int start, int stop, double step) {
		int nBins=(int) Math.floor((stop-start)/step); 
		double[] newBins= new double[nBins]; 
		for (int i=0; i<nBins; i++) {
			newBins[i]=start+i*step; 
		}
		return newBins; 
	}
	
	/**
	 * Resample the waveform. 
	 * @param waveform
	 * @param oldSampleRate - the old sample rate
	 * @param newSampleRate - the new sample rate
	 * @return
	 */
	private double[] reSampleWaveform(double[] waveform, double oldSampleRate, double newSampleRate) {
		//figure out the number of new bins
		double binSize = oldSampleRate/newSampleRate;// the new bin size. 

//		//create x arrays. 
//		double[] x= binArray(0,waveform.length,1); 
//		double[] xi= binArray(0,waveform.length,binSize); 
//		
//		// TODO Auto-generated method stub
//		return PamInterp.interpLinear(x, waveform, xi);
		
//		System.out.println("Interp waveform: " + binSize); 
		return PamInterp.interpWaveform(waveform, 1./binSize); 
		
	}
	
	/***
	 * The click needs to be the same or shorter than the FFT length.
	 * If the click is longer need to find the click in the waveform snipper
	 * and extract a subsection. 
	 *
	 */
	public static double[] conditionClickLength(double[] click, int fftLength, int smoothing) {
		double[] aWave = SmoothingFilter.smoothData(click, smoothing);
		int waveLen = aWave.length;
		double maxVal = aWave[0];
		double maxIndex = 0;
		for (int s = 1; s < waveLen; s++) {
			if (aWave[s] > maxVal) {
				maxVal = aWave[s];
				maxIndex = s;
			}
		}
		//now extract the waveform from the snippet
		//if peak is near front. 
		int start=(int) Math.max(maxIndex-(fftLength/2), 0);
		//if peak is near end
		start=Math.min(start, click.length-fftLength);
		
		return ArrayUtils.subarray(click, start, fftLength+start); 
	}
	
	@Override
	protected MTClassifier clone() {
		MTClassifier newParams = null;
		try {
			newParams = (MTClassifier) super.clone();
		}
		catch(CloneNotSupportedException Ex) {
			Ex.printStackTrace(); 
			return null;
		}
		return newParams;
	}
	
	
	/**
	 * Get the auto-generated parameter set, and then add in the fields that are not included
	 * because they are not public and do not have getters.
	 * Note: for each field, we search the current class and (if that fails) the superclass.  It's
	 * done this way because MTClassifier might be used directly (and thus the field would
	 * be found in the class) and it also might be used as a superclass to something else
	 * (e.g. DefaultTemplates) in which case the field would only be found in the superclass.
	 */
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("inteprWaveformReject");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return inteprWaveformReject;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("inteprWaveformReject");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return inteprWaveformReject;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		try {
			Field field = this.getClass().getDeclaredField("interpWaveformMatch");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return interpWaveformMatch;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("interpWaveformMatch");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return interpWaveformMatch;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		try {
			Field field = this.getClass().getDeclaredField("waveformMatchFFT");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					if (waveformMatchFFT==null) return null;
					return waveformMatchFFT.getData();
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("waveformMatchFFT");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						if (waveformMatchFFT==null) return null;
						return waveformMatchFFT.getData();
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		try {
			Field field = this.getClass().getDeclaredField("waveformRejectFFT");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					if (waveformRejectFFT==null) return null;
					return waveformRejectFFT.getData();
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("waveformRejectFFT");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						if (waveformRejectFFT==null) return null;
						return waveformRejectFFT.getData();
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		return ps;
	}
			

	
	
//	/**
//	 * Calculate a value for the click reject and match template ration correlation
//	 * 
//	 * @param click
//	 *            - the input click to perform correlation match on. It should be
//	 *            the same length and sample rate as the templates.
//	 * @param sR-
//	 *            the sample rate in samples per second
//	 * @return the correlation reject match template.
//	 */
//	public MatchedTemplateResult calcCorrelationMatch(ComplexArray click, float sR) {
//		
//		//set the stored sR
//		currentSr=sR;
//		
//		//int fftLength = getFFTLength(sR);
//		int fftLength = this.getFFTLength(sR); 
//		
//		ComplexArray matchResult= new ComplexArray(fftLength); 
//		ComplexArray matchTemplate = getWaveformMatchFFT(sR); 
//		//System.out.println("Match template length: " + matchTemplate.length()); 
//		for (int i=0; i<matchTemplate.length(); i++) {
//			matchResult.set(i, click.get(i).times(matchTemplate.get(i)));
//		}
//		
//		
//		ComplexArray rejectResult= new ComplexArray(fftLength); 
//		ComplexArray rejectTemplate = getWaveformRejectFFT(sR); 
//		for (int i=0; i<rejectTemplate.length(); i++) {
//			rejectResult.set(i, click.get(i).times(rejectTemplate.get(i)));
//		}
//
//		//must use scaling to get the same result as MATLAB 
//		if (fft==null) fft= new FastFFT();
//		fft.ifft(matchResult,  fftLength, true);
//		fft.ifft(rejectResult, fftLength, true);
//	
//		//need to take the real part of the result and multiply by 2 to get same as 
//		//ifft function in MATLAB
//		double[] matchReal = new double[matchResult.length()]; 
//		double[] rejectReal = new double[rejectResult.length()]; 
//		
//		for (int i=0; i<matchResult.length(); i++) {
//			matchReal[i]=matchResult.getReal(i); 
//			rejectReal[i]=rejectResult.getReal(i); 
//		}
//
//		double maxmatch=PamArrayUtils.max(matchReal); 
//		double maxreject=PamArrayUtils.max(rejectReal); 
//		
//		
//		double result = 2*(PamArrayUtils.max(matchReal)-PamArrayUtils.max(rejectReal)); 
//		
//		//create results structure. 
//		MatchedTemplateResult matchTmpResult = new MatchedTemplateResult(); 
//		matchTmpResult.threshold=result; 
//		matchTmpResult.matchCorr=maxmatch; 
//		matchTmpResult.threshold=maxreject;
//
////		System.out.println("iFFT Real MATCH");
////		MTClassifierTest.printWaveform(matchReal);
////		System.out.println("-------------------------------------");
////		System.out.println("iFFT Real REJECT");
////		MTClassifierTest.printWaveform(rejectReal);
//		
//		return matchTmpResult;
//	}


	

}
