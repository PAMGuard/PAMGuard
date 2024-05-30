package matchedTemplateClassifer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamArrayUtils;
import PamUtils.complex.ComplexArray;
import Spectrogram.WindowFunction;
import clickDetector.ClickLength;
import fftManager.FastFFT;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

/**
 * Test for the MTClassifier. Specifically testing comparisons to MATLAB to make 
 * sure all the maths is right. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class MTClassifierTest {
	
	/**
	 * Test the classifier using some imported data with default templates. 
	 */
	private static void testClassifier(double[] testWaveform, float sR) {
		testClassifier(testWaveform,  sR, null); 
	}


	/**
	 * Test a waveforms and some match and reject templates. 
	 * @param testWaveform - the test waveform
	 * @param sR - the sample rate of the test waveform
	 * @param templates - the templates to test against
	 * @return struct containing output results
	 */
	private static Struct testClassifier(double[] testWaveform, float sR, ArrayList<MatchTemplate> templates) {

		//create the classifier object 
		MTClassifier mtclassifier = new MTClassifier(); 
		//add templates if inpout 
		if (templates!=null) {
			mtclassifier.waveformMatch=templates.get(0); 
			mtclassifier.waveformReject=templates.get(1); 
		}

		FastFFT fft = new FastFFT(); 
		int fftSize =  mtclassifier.getFFTLength(288000); 

		testWaveform=PamUtils.PamArrayUtils.normalise(testWaveform);

		ComplexArray complexArray = fft.rfft(testWaveform, fftSize);
		
		System.out.println("Waveform: length: " + testWaveform.length); 
		//printWaveform(testWaveform); 
		
		System.out.println("Waveform FFT: length: " + complexArray.length()); 
		//printFFt(complexArray); 
		System.out.println("--------"); 

		System.out.println("Click waveform length: " + testWaveform.length +  "  FFT bins: " + complexArray.length() + " FFT Length: " + fftSize);

		Struct struct = mtclassifier.calcCorrelationMatchTest(complexArray, sR);
		
		struct.set("waveform",  DLMatFile.array2Matrix(testWaveform));
		
		System.out.println("Match: " + struct.getMatrix("result").getDouble(0)); 
		
		return struct; 
	}
	
	/**
	 * Test the correlation of several templates
	 * @param testWaveform - the waveform to correlate against. 
	 * @param sR - the sample rate of the waveform. 
	 * @param templates - the match templates to test. 
	 */
	public static List<MatchedTemplateResult> testCorrelation(double[] testWaveform, float sR, ArrayList<MatchTemplate> templates) {
		return testCorrelation(testWaveform,  sR,  templates, MatchedTemplateParams.NORMALIZATION_RMS); 
	}

	
	
	
	/**
	 * Test the correlation of several templates
	 * @param testWaveform - the waveform to correlate against. 
	 * @param sR - the sample rate of the waveform. 
	 * @param templates - the match templates to test. 
	 * @param normalisation - the normalisation type to use e.g.  MatchedTemplateParams.NORMALIZATION_RMS
	 * @return a list of the correlation results. 
	 */
	public static List<MatchedTemplateResult> testCorrelation(double[] testWaveform, float sR, ArrayList<MatchTemplate> templates, int normalisation) {

		
		List<MatchedTemplateResult> matchedTemplateResult = new ArrayList<MatchedTemplateResult>();
		//create the classifier object 
		for (int i=0; i<templates.size(); i++){
			MTClassifier mtclassifier = new MTClassifier(); 
			mtclassifier.normalisation = normalisation; //set the normalisation
			
			//System.out.println("Template " + i + " " + templates.get(i));
			//add templates if inpout 
			if (templates!=null) {
				mtclassifier.waveformMatch=templates.get(i); 
				mtclassifier.waveformReject=templates.get(i);; 
			}

			FastFFT fft = new FastFFT(); 

			//System.out.println("Waveform len: " +testWaveform.length + " min: " + PamArrayUtils.min(testWaveform) +  " max: " + PamArrayUtils.max(testWaveform)); 

		
			//testWaveform=PamArrayUtils.divide(testWaveform, PamUtils.PamArrayUtils.max(testWaveform));
			
			testWaveform = MTClassifier.normaliseWaveform(testWaveform, normalisation);
			
//			System.out.println("Waveform max: " + PamArrayUtils.max(testWaveform) + " len: " + testWaveform.length); 

			
			//calculate the click FFT. 
			fft = new FastFFT(); 
			//int fftSize = FastFFT.nextBinaryExp(testWaveform.length/2);

			//int fftSize = mtclassifier.getFFTLength(sR); 
					
			//fftSize = 100000;
			
			ComplexArray matchClick = fft.rfft(testWaveform, testWaveform.length);
			
			//System.out.println("FFT len: " + matchClick.length() + " waveform len: " +testWaveform.length); 

			//calculate the correlation coefficient.
			MatchedTemplateResult matchResult = mtclassifier.calcCorrelationMatch(matchClick, sR);
						
			matchedTemplateResult.add(matchResult);
			
			System.out.println(String.format("The match correlation for %d is %.5f", i, matchResult.matchCorr));
//			
//			 printFFt(matchClick);
//			 
//			 System.out.println("-----------------------");
//			
//			 ComplexArray matchTemplate = mtclassifier.getWaveformMatchFFT(sR, matchClick.length()*2); 
//
//			 printFFt(matchTemplate);


		}
		return matchedTemplateResult;
	}
	
	public static void printFFt(ComplexArray complexArray) {
		for (int i=0; i<complexArray.length(); i++ ) {
			//System.out.println(complexArray.get(i).toString(6));
			System.out.println(complexArray.get(i).real + "," + complexArray.get(i).imag);
		}
	}
	
	public static void printWaveform(double[] complexArray) {
		for (int i=0; i<complexArray.length; i++ ) {
			System.out.println(complexArray[i]); 
		}
	}

	/**
	 * Create a test click.
	 * @return the test click
	 */
	public static double[] makeTestClick() {
		double[] testWaveform=DefaultTemplates.beakedWhale1;

		//		int bins=128; 
		//		double[] paddedWaveform= new double[bins]; 
		//		for (int i=0; i<paddedWaveform.length; i++) {
		//			if (i<testWaveform.length) {
		//				paddedWaveform[i]=testWaveform[i]; 
		//			}
		//			paddedWaveform[i]=(Math.random()-0.5)*0.001;
		//		}
		return testWaveform; 
	}

	/**
	 * Import a bunch of clicks from a .mat file
	 */
	public static ArrayList<MatchTemplate> importClicks(String filePath, float sR) {

		try {
			 Mat5File mfr = Mat5.readFromFile(filePath);

			//		//get array of a name "my_array" from file
			Struct mlArrayRetrived = mfr.getStruct( "clicks" );

			int numClicks= mlArrayRetrived.getNumCols();
			ArrayList<MatchTemplate> clicks = new ArrayList<MatchTemplate>(numClicks); 

			for (int i=0; i<numClicks; i++) {
				Matrix clickWav= mlArrayRetrived.get("wave", i);
				double[][] click= PamArrayUtils.matrix2array(clickWav);
				//System.out.println("click: " + click[0].length + " num: " + numClicks);
				
				double[] waveform = new double[click.length];
				for (int j=0; j<waveform.length; j++) {
					waveform[j] = click[j][0]; 
				}
				
				Matrix clickUID= mlArrayRetrived.get("UID", i);

				clicks.add(new MatchTemplate(Long.toString(clickUID.getLong(0)), waveform, 288000)); 
			}
			return clicks; 
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; 
		}

	}
	
	
	/**
	 * Import a multiple templates. These are just waveforms and a sample rtae. 
	 */
	public static ArrayList<MatchTemplate> importTemplates(String filePath) {
		Mat5File mfr;
		try {
			mfr =  Mat5.readFromFile(filePath);
			//		//get array of a name "my_array" from file
			Struct mlArrayMatch =  mfr.getStruct( "templates" );
//			System.out.println(mlArrayMatch.getType() +  "  " + mlArrayMatch.getNumElements()); 

			ArrayList<MatchTemplate> templates = new ArrayList<MatchTemplate>(); 

			int numTemplates= mlArrayMatch.getNumElements();

			for (int i=0; i<numTemplates; i++) {
				
				Matrix waveM  = mlArrayMatch.get("wave", i);

				double[][] wave = PamArrayUtils.matrix2array(waveM);
				
				Matrix templatesr= mlArrayMatch.get("sr", i);
				double sr= templatesr.getDouble(0);
				
//				System.out.println("template wave: " + wave[0].length + " num: " + numTemplates);
				
				templates.add(new MatchTemplate(null, wave[0], (float) sr)); 
			}
			return templates; 
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; 
		}
	}
	
	
	/**
	 * Import a bunch of clicks from a .mat file
	 */
	public static ArrayList<MatchTemplate> importTemplate(String filePath) {
		Mat5File mfr;
		try {
			mfr = Mat5.readFromFile(filePath);
			//		//get array of a name "my_array" from file
			Matrix mlArrayMatch = 	mfr.getArray( "matchtemplate" );
			double[][] mlMatch= PamArrayUtils.matrix2array(mlArrayMatch); 
			Matrix mlArrayReject = 	mfr.getArray( "rejectemplate" );
			double[][] mlReject= PamArrayUtils.matrix2array(mlArrayReject);

			ArrayList<MatchTemplate> templates = new ArrayList<MatchTemplate>(2); 

			System.out.println("Importing templates with length of : " + mlMatch[0].length + " and " + mlReject[0].length); 
			templates.add(new MatchTemplate(null, mlMatch[0], 192000)); 
			templates.add(new MatchTemplate(null, mlReject[0], 192000)); 
			
			return templates; 
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null; 
		}
	}
	
	/**
	 * Test the normalisation of a wave. 
	 */
	public static void normalizeTest(double[] wave) {
		double[] normWave= PamArrayUtils.normalise(wave); 
		System.out.println("Max of the normalized wave is before: " +  PamArrayUtils.max(wave) + " after: " + PamArrayUtils.max(normWave));
	}

	/****TESTS****/
	
	public static void testMatchReject() {
		
		String testClicksPath = "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/matchedclickclassifer/DS2clks_test.mat";
		String templteFilePath= "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/matchedclickclassifer/DS2templates_test.mat";
		
		String mlExportPath="C:\\Users\\macst\\Desktop\\mtClassTest.mat"; 
				
		System.out.println("Importing clicks");
		ArrayList<MatchTemplate> clicks = importClicks(testClicksPath, 288000); 
		ArrayList<MatchTemplate> templates = importTemplate(templteFilePath); 
		
		//do some tests on normalization 
		System.out.println("Normalization test");
		for (int i=0; i<1; i++) {
			normalizeTest(clicks.get(i).waveform);
		}
		
		System.out.println("");
		System.out.println("Testing the classifier");
		
		Struct mlResults = Mat5.newStruct();
		Struct strcut; 
		int N = 3; 
//		templates.remove(1);
//		templates.add(clicks.get(0)); 
		for (int i=0; i<N; i++) {
			strcut= testClassifier(clicks.get(i).waveform, clicks.get(i).sR, templates);
			mlResults.set("results", i, strcut);
		}

		try {
			//writ ethe results to file
			Mat5.writeToFile(null, mlExportPath);
		} catch (IOException e) {
			System.err.println("The MATLAB file did not write properly");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Test how the length of the waveform affects the match correlation values
	 */
	public static void testMatchCorrLen() {
		
		String testClicksPath = "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/matchedclickclassifer/DS3clks_test.mat";
		String templteFilePath= "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/matchedclickclassifer/DS3templates_test.mat";
		
		float sR = 288000; //sample rate in samples per second. 
		ArrayList<MatchTemplate> clicks = importClicks(testClicksPath, sR); 
		ArrayList<MatchTemplate> templates = importTemplates(templteFilePath); 
		
		int index = 24; 
		//values in MATLAB are9.73577287114938	8.82782814105430	3.51936216182390
		System.out.println("Number of clicks: " + clicks.size() + " UID " + clicks.get(index).name); 
		
		System.out.println("------Standard Length--------"); 
		testCorrelation(clicks.get(index).waveform,  sR, templates); 
		
		System.out.println("------Restricted Length--------"); 

		int restrictedBins= 2048; 
		
		ClickLength clickLength = new ClickLength(); 
		int[][] lengthPoints =  clickLength.createLengthData(clicks.get(index), sR, 5.5, 3, false, null); 
		
		double[] waveformLen = MTProcess.createRestrictedLenghtWave(clicks.get(index).waveform, lengthPoints[0],
				 restrictedBins, WindowFunction.hann(restrictedBins)); 
		
		testCorrelation(waveformLen,  sR, templates); 
		
	}
	
	/**
	 * Test the match corr algorithm by cross correlating a waveform with itself. 
	 */
	public static void testMatchCorr() {
		
		String templteFilePath= "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/matchedclickclassifer/_test/DS2templates_test.mat";
		//float sR = 288000; //sample rate in samples per second. 

		ArrayList<MatchTemplate> templates = importTemplates(templteFilePath); 
		
		testCorrelation(templates.get(0).waveform,  templates.get(0).sR, templates); 
		
	}
	
	
	public static void main(String args[]) {
//		testMatchCorrLen(); 
		testMatchCorr();
	}
	

}
