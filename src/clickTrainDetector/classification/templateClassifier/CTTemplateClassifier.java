package clickTrainDetector.classification.templateClassifier;

import java.util.List;

import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamUtils.avrgwaveform.AverageWaveform;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.IDIInfo;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.simplechi2classifier.Chi2CTClassification;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdClassifier;
import clickTrainDetector.classification.templateClassifier.DefualtSpectrumTemplates.SpectrumTemplateType;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import clickTrainDetector.layout.classification.templateClassifier.CTTemplateClassifiersGraphics;
import pamMaths.PamInterp;
import PamguardMVC.debug.Debug;

/**
 * 
 * A template classifier which uses a click template to essentially cross correlate
 * the spectrums. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CTTemplateClassifier implements CTClassifier {

	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;
	
	/**
	 * Handles all graphics for the classifier. 
	 */
	private CTTemplateClassifiersGraphics ctClassifierGraphics; 
	
	/** 
	 * The classifier parameters. 
	 */
	private TemplateClassifierParams templateClassifierParams = new TemplateClassifierParams(); 
	
	/**
	 * THe Chi2 threshold classifier. 
	 */
	//public Chi2ThresholdClassifier chi2ThresholdClassifier;
	
	/**
	 * The spectrum template which has been interpolated for current sample rate and
	 * average template FFT bins. 
	 */
	private SpectrumTemplateParams interpolatedTemplate; 
	
	public CTTemplateClassifier(ClickTrainControl clickTrainControl, int defaultSpeciesID) {
		this.clickTrainControl = clickTrainControl;
		//chi2ThresholdClassifier=new Chi2ThresholdClassifier(clickTrainControl, defaultSpeciesID); 
		templateClassifierParams.speciesFlag=defaultSpeciesID; 
		//templateClassifierParams.chi2ThresholdParams.speciesFlag=defaultSpeciesID; //must make this the same 
	}
	
	public CTTemplateClassifier(int defaultSpeciesID) {
		//chi2ThresholdClassifier=new Chi2ThresholdClassifier(defaultSpeciesID); 
		templateClassifierParams.speciesFlag=defaultSpeciesID; 
		//templateClassifierParams.chi2ThresholdParams.speciesFlag=defaultSpeciesID; //must make this the same 
	}

	@Override
	public CTClassification classifyClickTrain(CTDataUnit clickTrain) {
		
		//Debug.out.println("Click train: " + clickTrain + " sub count: " + clickTrain.getSubDetectionsCount() + " UID: " + clickTrain.getUID());

		//little HACK here to ensure species falgs are the same.
		//templateClassifierParams.chi2ThresholdParams.speciesFlag=templateClassifierParams.speciesFlag; 

		//chi2ThresholdClassifier.setParams(templateClassifierParams.chi2ThresholdParams);

		if (clickTrain.getAverageSpectra()==null) {
			System.err.println("TemplateClassifier:There is no average waveform for template classification: " + clickTrain.averageWaveform);
			return new TemplateClassification(0, CTClassifier.PRECLASSIFIERFLAG);
		}
		
		//grab average waveform and calculate correlation value
		double corrValue = calcSpectrumCorrelation(clickTrain.averageWaveform); 
				
		//need to work out correlation threshold whatever
		
		//first check the chi2 value. 
		//Chi2CTClassification chi2Classification = chi2ThresholdClassifier.classifyClickTrain(clickTrain); 
		
		
//		//check chi^2 classification is passed
//		if (chi2Classification.getSpeciesID()!=templateClassifierParams.speciesFlag) {
//			//no classification
////			Debug.out.println(templateClassifierParams.classifierName + " Classifier: Failed chi2 classifier: " + chi2Classification.getSpeciesID() + 
////					" " +chi2ThresholdClassifier.getParams().speciesFlag + " " + templateClassifierParams.speciesFlag + " min chi2: " + templateClassifierParams.chi2ThresholdParams.chi2Threshold); 
//			return new TemplateClassification(CTClassifier.PRECLASSIFIERFLAG, corrValue);
//		}
		
		
		// check template correlation. 
		if (Double.isNaN(corrValue) || corrValue<templateClassifierParams.corrThreshold) {
			//no classification
//			Debug.out.println(templateClassifierParams.classifierName +" Classifier: Failed corr classifier: " + corrValue); 
			return new TemplateClassification(CTClassifier.PRECLASSIFIERFLAG, corrValue);
		}
		
//		Debug.out.println(templateClassifierParams.classifierName +" Classifier: Successfull: " + corrValue + " speciesFlag: " + templateClassifierParams.speciesFlag); 
		
		return new TemplateClassification(templateClassifierParams.speciesFlag, corrValue); 
	}
	
	
	/**
	 * Calculate the correlation value for the average waveform and spectrum template. 
	 * @param averageWaveform - the average waveform. 
	 * @return the correlation value. 
	 */
	protected double calcSpectrumCorrelation(AverageWaveform averageWaveform) {
		return calcSpectrumCorrelation(averageWaveform.getAverageSpectra(),
				averageWaveform.getSampleRate());
	}
	
	/**
	 * Calculate the correlation value for the average waveform and spectrum template. 
	 * @param averageWaveform - the average spectrum in even frequency bins. 
	 * @param sR - the samplerate of the average spectra in samples per second. 
	 * @return the correlation value. 
	 */
	protected double calcSpectrumCorrelation(double[] averageSpectra, float sR) {
		//the correlation is just the product of the real part of two normalised. 
		//Because average waveforms don't need to deal with phase and the spectra 
		//template has no phase component no complex numbers to deal. Just need to make
		//sure the template is interpolated to have the same number of points as the 
		//the average waveform. 
		
//		// the average spectra. 
//		double[] averageSpectra = averageWaveform.getAverageSpectra(); 
		
		//get the sample rate. 
//		float sR = clickTrainControl.getParentDataBlock().getSampleRate(); 
		
		double[] template = templateClassifierParams.spectrumTemplate.waveform;
		float sRtmplt = templateClassifierParams.spectrumTemplate.sR; 
		
		//interpolate the spectrum so that it is the same as the average waveform.
		if (interpolatedTemplate==null || interpolatedTemplate.template.length!=averageSpectra.length ||
				interpolatedTemplate.sampleRate!=sR || Double.isNaN(interpolatedTemplate.template[0])) {
			//know sR is not the frequency but we're just comparing so no need to divide by 2. 
			List<Double> freqBinsX = PamUtils.linspace(0, sRtmplt, template.length); 
			List<Double> freqBinsXi = PamUtils.linspace(0, sR, averageSpectra.length); 
			
			double[] freqBinsXd 	= 	PamArrayUtils.list2ArrayD(freqBinsX);
			double[] freqBinsXid 	= 	PamArrayUtils.list2ArrayD(freqBinsXi);

			double[] interpTemplate = PamInterp.interp1(freqBinsXd, template, freqBinsXid, 0); 
			

			
//			System.out.println("Is NaN 1 ? " + interpTemplate[0] + " sR " + sR);

			
			interpTemplate= PamArrayUtils.normalise(interpTemplate); 
			
//			System.out.println("Is NaN 2 ? " + interpTemplate[0]);
//			
//			if (Double.isNaN(interpTemplate[0])) {
//				System.out.println("-------------freqBinsXd: " + freqBinsXd.length + "  " + sR);
//				PamArrayUtils.printArray(freqBinsXd);
//				System.out.println("-------------freqBinsXid: " + freqBinsXid.length);
//				PamArrayUtils.printArray(freqBinsXid);
//				
//				
//				System.out.println("-------------Template Spectra: " + template.length);
//				PamArrayUtils.printArray(template);
//				System.out.println("-------------Interp Template Spectra: " + interpTemplate.length);
//				PamArrayUtils.printArray(interpTemplate);
//			}


			//create the interpolated template. May contain NaN if average waveform is out of bounds of spectrum. 
			this.interpolatedTemplate = new SpectrumTemplateParams("InterpTemplate", interpTemplate, sR); 
		}
		
		//note that normalisation distributes the ENERGY evenly, it does not make the 
		//maximum value 1. 
		double[] avrgSpectraN=PamArrayUtils.normalise(averageSpectra);
		
//		System.out.println("Interpolated Template: " + interpolatedTemplate.template.length);
//		PamArrayUtils.printArray(interpolatedTemplate.template);
//		System.out.println("Average Spectra: " + avrgSpectraN.length);
//		PamArrayUtils.printArray(avrgSpectraN);

		//find correaltion value byt multiplying both template and spectra together. If both spectra are equal the result should be one. 
		double corr=0; 
		for (int i=0; i<interpolatedTemplate.template.length; i++) {
			corr+=avrgSpectraN[i]*interpolatedTemplate.template[i];
		}
		
		if (Double.isNaN(corr)) {
			System.err.println("Click train detector: the corr. value for the average spectrum was NaN?"); 
			System.err.println("Average spectra"); 
			PamArrayUtils.printArray(avrgSpectraN);
			System.err.println("Interpolated template"); 
			PamArrayUtils.printArray(interpolatedTemplate.template);
		}
		
		return corr;
	}


	@Override
	public String getName() {
		return "Spectrum Template Classifier";
	}

	@Override
	public CTClassifierGraphics getCTClassifierGraphics() {
		if (ctClassifierGraphics==null) {
			ctClassifierGraphics = new CTTemplateClassifiersGraphics(this); 
		}
		return ctClassifierGraphics;
	}
	
	/**
	 * Get the simple classifier parameters. 
	 * @return the classifier parameters. 
	 */
	public TemplateClassifierParams getParams() {
		return templateClassifierParams;
	}

	@Override
	public void setParams(CTClassifierParams ctClassifierParams) {
		this.templateClassifierParams=(TemplateClassifierParams) ctClassifierParams;
	}

//	/**
//	 * Get the simple chi^2 classifier which forms one of the classification tests for 
//	 * the template classifier. 
//	 * @return the chi2 classifier. 
//	 */
//	public Chi2ThresholdClassifier getSimpleCTClassifier() {
//		return this.chi2ThresholdClassifier;
//	}

	@Override
	public int getSpeciesID() {
		return this.templateClassifierParams.speciesFlag;
	}
	
	public static void main(String[] args) {
		System.out.println("Template Spectrum Test"); 
		
		CTTemplateClassifier templateClassification = new CTTemplateClassifier(null, 1); 
		
		templateClassification.getParams().spectrumTemplate = DefualtSpectrumTemplates.getTemplate(SpectrumTemplateType.DOLPHIN); 
		
		double corr = templateClassification.calcSpectrumCorrelation(templateClassification.getParams().spectrumTemplate.waveform, templateClassification.getParams().spectrumTemplate.sR); 
		
		System.out.println("The correlation value for dolphin to dolphin template " + corr); 

		corr = templateClassification.calcSpectrumCorrelation(DefualtSpectrumTemplates.getTemplate(SpectrumTemplateType.PORPOISE).waveform, DefualtSpectrumTemplates.getTemplate(SpectrumTemplateType.PORPOISE).sR); 
		
		System.out.println("The correlation value for dolphin to harbour porpoise template " + corr); 

	}


}
