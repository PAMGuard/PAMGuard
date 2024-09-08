package test.matchedTemplateClassifier;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import PamUtils.PamArrayUtils;
import PamUtils.complex.ComplexArray;
import Spectrogram.WindowFunction;
import clickDetector.ClickLength;
import fftManager.FastFFT;
import matchedTemplateClassifer.MTClassifier;
import matchedTemplateClassifer.MTClassifierTest;
import matchedTemplateClassifer.MTProcess;
import matchedTemplateClassifer.MatchTemplate;
import matchedTemplateClassifer.MatchedTemplateParams;
import matchedTemplateClassifer.MatchedTemplateResult;

/**
 * Tests for the matched click classifier. 
 */
public class MatchedTemplateClassifierTest {
	
	
	/**
	 * Test the match correlation algorithm by cross correlating a waveform with itself. Results
	 * are tested against the matlab xcorr funtion
	 */
	@Test
	public void testMatchCorr() {
		
		System.out.println("Matched template classifier test: match corr");

		
		/*
		 * 
		 * Test against MATLAB Xcorr function 
		 * 
		 * load('/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/matchedclickclassifer/
		 * _test/DS2templates_test.mat')
		 * 
		 * wave = templates(1).wave; %test correlating waves with themselves using xcorr
		 * 
		 * r = xcorr( wave , wave); disp(['Max correlation no-norm: ' num2str(max(r))])
		 * 
		 * r = xcorr( normalize(wave) , normalize(wave)); disp(['Max correlation rms: '
		 * num2str(max(r))])
		 * 
		 * r = xcorr( wave/max(wave) , wave/max(wave)); disp(['Max correlation peak: '
		 * num2str(max(r))])
		 */

		//Note that the return value of two identical waveforms depends on normalisation. 
		
		String templteFilePath= "./src/test/resources/matchedTemplateClassifier/DS2templates_test.mat";
		//float sR = 288000; //sample rate in samples per second. 
		Path path = Paths.get(templteFilePath);
		String templteFilePathR  = path.toAbsolutePath().normalize().toString();

		ArrayList<MatchTemplate> templates = MTClassifierTest.importTemplates(templteFilePathR); 
		
		List<MatchedTemplateResult> matchedClickResult ;
		/**
		 * Correlate a template waveform with itself and check the result is 1.
		 */
		matchedClickResult = MTClassifierTest.testCorrelation(templates.get(0).waveform,  templates.get(0).sR, templates,  MatchedTemplateParams.NORMALIZATION_RMS); 
		assertEquals(matchedClickResult.get(0).matchCorr, 1.0, 0.01);
	
		
		matchedClickResult = MTClassifierTest.testCorrelation(templates.get(0).waveform,  templates.get(0).sR, templates,  MatchedTemplateParams.NORMALIZATION_PEAK); 
		assertEquals(matchedClickResult.get(0).matchCorr, 134.5961, 0.01);
	

		matchedClickResult = MTClassifierTest.testCorrelation(templates.get(0).waveform,  templates.get(0).sR, templates,  MatchedTemplateParams.NORMALIZATION_NONE); 
		assertEquals(matchedClickResult.get(0).matchCorr, 7.8457, 0.01);
		
	}
	
	/**
	 * Test the match correlation algorithm combined with click length algorithm. Here we want to test that 
	 * a long waveform can be processed properly - i.e the peak of the click can be found and cross correlation performed
	 * on a shortened section. 
	 */
	@Test
	public void testMatchCorrLen() {
		
		System.out.println("Matched template classifier test: match corr len");

		String testClicksPath = "./src/test/resources/matchedTemplateClassifier/DS3clks_test.mat";
		Path path = Paths.get(testClicksPath);
		testClicksPath=path.toAbsolutePath().normalize().toString();

		String templteFilePath= "./src/test/resources/matchedTemplateClassifier/DS3templates_test.mat";
		path = Paths.get(templteFilePath);
		templteFilePath=path.toAbsolutePath().normalize().toString();
		
		//import some example clicks
		float sR = 288000; //sample rate in samples per second. 
		ArrayList<MatchTemplate> clicks = MTClassifierTest.importClicks(testClicksPath, sR); 
		
		//import some templates
		ArrayList<MatchTemplate> templates = MTClassifierTest.importTemplates(templteFilePath); 
		
		int index = 24; //the index of the test clck to use.
		//values in MATLAB are9.73577287114938	8.82782814105430	3.51936216182390
//		System.out.println("Number of clicks: " + clicks.size() + " UID " + clicks.get(index).name); 
		
		System.out.println("------Standard Length--------"); 
		List<MatchedTemplateResult> matchedClickResultLen1 = MTClassifierTest.testCorrelation(clicks.get(index).waveform,  sR, templates); 
		
		System.out.println("------Restricted Length--------"); 

		int restrictedBins= 2048; 
		
		ClickLength clickLength = new ClickLength(); 
		int[][] lengthPoints =  clickLength.createLengthData(clicks.get(index), sR, 5.5, 3, false, null); 
		
		double[] waveformLen = MTProcess.createRestrictedLenghtWave(clicks.get(index).waveform, lengthPoints[0],
				 restrictedBins, WindowFunction.hann(restrictedBins)); 
		
		List<MatchedTemplateResult> matchedClickResultLen2 = MTClassifierTest.testCorrelation(waveformLen,  sR, templates); 
		
		assertEquals(matchedClickResultLen1.get(0).matchCorr, matchedClickResultLen2.get(0).matchCorr, 0.05);

		
	}
	
	
	/**
	 * Test the FFT method of correlating two identical waveforms. 
	 */
	@Test
	public void testFFTCorrelation() {
		
		System.out.println("Matched template classifier test: xcorr");
		
		String templteFilePath= "./src/test/resources/matchedTemplateClassifier/DS2templates_test.mat";
		//float sR = 288000; //sample rate in samples per second. 
		Path path = Paths.get(templteFilePath);
		String templteFilePathR  = path.toAbsolutePath().normalize().toString();

		ArrayList<MatchTemplate> templates = MTClassifierTest.importTemplates(templteFilePathR); 
		
		double[] waveform = templates.get(0).getWaveData()[0];
		waveform = MTClassifier.normaliseWaveform(waveform, MatchedTemplateParams.NORMALIZATION_RMS);

		FastFFT fft = new FastFFT(); 
		ComplexArray testClick = fft.rfft(waveform, waveform.length);
		ComplexArray matchTemplate = fft.rfft(waveform, waveform.length).conj();
		
		int fftLength = testClick.length()*2;
		ComplexArray matchResult= new ComplexArray(fftLength); 
		
		for (int i=0; i<Math.min(testClick.length(), matchTemplate.length()); i++) {
			matchResult.set(i, testClick.get(i).times(matchTemplate.get(i)));
		}
		
		fft.ifft(matchResult, fftLength, true);

		double[] matchReal = new double[matchResult.length()]; 
		for (int i=0; i<matchResult.length(); i++) {
			matchReal[i]=2*matchResult.getReal(i); 
		}
		
		System.out.println("Max correlation result: " + PamArrayUtils.max(matchReal));
		
		assertEquals(PamArrayUtils.max(matchReal), 1.0, 0.01);

		
	}
	
	

}
