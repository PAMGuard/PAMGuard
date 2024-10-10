package rawDeepLearningClassifier.dlClassification.delphinID;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jpamutils.JamArr;
import org.jamdev.jpamutils.spectrogram.SpecTransform;
import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import ai.djl.MalformedModelException;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;
import whistlesAndMoans.AbstractWhistleDataUnit;


/**
 * A delphinID test suite. 
 * 
 * @author Jamie Macaulay
 * 
 */
public class DelphinIDTest {


	/**
	 * Main class for running the test. 
	 * @param args - the arguments
	 */
	public static void main(String args[]) {
		
		testDelphinIDArray(null);

		//
		//		String matImageSave = "C:\\Users\\Jamie Macaulay\\MATLAB Drive\\MATLAB\\PAMGUARD\\deep_learning\\delphinID\\whistleimages_4s_415.mat";
		//		matImageSave = null;
		//		testDelphinIDModel(matImageSave);

		//		//test a single image. 
		//		String imagePathOut = "C:\\Users\\Jamie Macaulay\\MATLAB Drive\\MATLAB\\PAMGUARD\\deep_learning\\delphinID\\whistle_image_python_java.mat";
		//		String imagePathOut = null;
		//		testDelphinIDImage(imagePathOut);
	}


	public static class DelphinIDWorkerTest extends DelphinIDWorker {

		private float[][][] lastModelInput;

		public float[][][] dataUnits2ModelInput(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan){

			float[][][] data = super.dataUnits2ModelInput(dataUnits, sampleRate, iChan);

			this.lastModelInput = data;

			return data;
		}

		public float[][][] getLastModelInput() {
			return lastModelInput;
		}

	}





	/**
	 * The 
	 * @param matImageSave - the MATLAB image
	 * @return true of the test is passed
	 */
	private static boolean testDelphinID2DModel(String matImageSave) {
		double segLen = 4000.;
		double segHop = 1000.0;
		float sampleRate =96000;
		double startSeconds = 9.565; //seconds to start segments (so we can compare to Python)
		//unix time from sound file
		long dataStartMillis = 1340212413000L;

		//path to the .mat containing whistle contours. 
		String whistleContourPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/SI20120620_171333_whistle_contours.mat";

		//the path to the model
		//String modelPath = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_model_2/whistle_4s_415.zip";
		String modelPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Dde415/whistle_4s_415_model.zip";
		//		String modelPath =  "./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_4s_415_model.zip";

		//create MatFile for saving the image data to. 
		MatFile matFile = Mat5.newMatFile();

		//get the whislte contours form a .mat file. 
		ArrayList<AbstractWhistleDataUnit> whistleContours = DelphinIDUtils.getWhistleContoursMAT(whistleContourPath);

		//segment the whistle detections
		//Note, delphinID starts from the first whistle and NOT the first file. 
		ArrayList<SegmenterDetectionGroup> segments =  DelphinIDUtils.segmentWhsitleData(whistleContours,  (long) (dataStartMillis+(startSeconds*1000.)), 
				segLen,  segHop);

		for (int i=0; i<segments.size(); i++) {
			System.out.println("Segment " + i + " contains " + segments.get(i).getSubDetectionsCount() + " whistles"); 
		}

		//prepare the model - this loads the zip file and loads the correct transforms. 
		Path path = Paths.get(modelPath);
		DelphinIDWorkerTest model = DelphinIDUtils.prepDelphinIDModel(path.toAbsolutePath().toString());
		model.setEnableSoftMax(false);


		//initialise strcuture for image data
		Struct imageStruct = Mat5.newStruct(segments.size(), 1);

		for (int i=0; i<segments.size(); i++) {

			//remember that the input is a stack of detections to be run by thge model at once - Here we want to do each one individually. 
			ArrayList<SegmenterDetectionGroup> aSegment = new  ArrayList<SegmenterDetectionGroup>();
			aSegment.add(segments.get(i)); 

			//the prediction. 
			ArrayList<StandardPrediction> predicition = model.runModel(aSegment, sampleRate, 1);		

			float[] output =  predicition.get(0).getPrediction();

			System.out.println();
			System.out.print(String.format("Segment: %d %.2f s" , i ,((aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000. - startSeconds)));
			for (int j=0; j<output.length; j++) {
				System.out.print(String.format( " %.3f" , output[j])); 
			}



			Matrix image = DLMatFile.array2Matrix(PamArrayUtils.float2Double(model.getLastModelInput()[0]));
			imageStruct.set("image", i, image);
			imageStruct.set("startmillis", i, Mat5.newScalar(aSegment.get(0).getSegmentStartMillis()));
			imageStruct.set("startseconds", i, Mat5.newScalar((aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000.));
			imageStruct.set("prediction", i, DLMatFile.array2Matrix(PamArrayUtils.float2Double(output)));

		}

		matFile.addArray("whistle_images", imageStruct);

		if (matImageSave!=null) {
			// Serialize to disk using default configurations
			try {
				Mat5.writeToFile(matFile,matImageSave);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//		for (int i=0; i<whistleContours.size(); i++) {
		//			System.out.println("Whislte: " + i);
		//			PamArrayUtils.printArray(whistleContours.get(i).getFreqsHz());
		//		}
		return true;
	}

	public static double[][] whistleScatter2Image(double[][] whistleValues, double startseg, double seglen) {

		//now perform the image transform in Java 
		double[] freqLimits = new double[] {2000., 20000.};
		double[] size = new double[] {496., 369.};

		ArrayList<double[][]> whistleImageArr = new ArrayList<double[][]>();
		whistleImageArr.add(whistleValues);

		BufferedImage canvas = Whistles2Image.makeScatterImage(whistleImageArr, size, new double[]{startseg, startseg + seglen}, freqLimits,  6.);

		double[][] imaged = new double[(int) size[0]][(int) size[1]];

		float[] color = new float[3];
		Raster raster = canvas.getData();
		for (int i=0; i<imaged.length; i++) {
			for (int j=0; j<imaged[0].length; j++) {
				color = raster.getPixel(i,j, color);
				imaged[i][j] = (255-color[0])/255.; //normalize
			}
		}

		//this is useful because it allows us to play around with transforms required to make this all work. 
		//create the model transforms
		ArrayList<DLTransform> modelTransforms = new ArrayList<DLTransform>();
		modelTransforms.add(new FreqTransform(DLTransformType.SPECFLIP));
		//		modelTransforms.add(new FreqTransform(DLTransformType.SPECNORMALISE_MINIMAX));
		modelTransforms.add(new FreqTransform(DLTransformType.SPECRESIZE, new Number[] {Integer.valueOf(48), Integer.valueOf(62), SpecTransform.RESIZE_BICUBIC}));
		modelTransforms.add(new FreqTransform(DLTransformType.GAUSSIAN_FILTER, new Number[] {Double.valueOf(0.5)}));

		SpecTransform specTransform = new SpecTransform(); 
		specTransform.setSpecData(imaged);
		specTransform.setSampleRate((float) (freqLimits[1]*2)); 

		//set the spec transform
		((FreqTransform) modelTransforms.get(0)).setSpecTransfrom(specTransform);

		//process all the transforms. 
		DLTransform transform = modelTransforms.get(0); 
		for (int i =0; i<modelTransforms.size(); i++) {
			transform = modelTransforms.get(i).transformData(transform); 
		}

		double[][] transformedData2 = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 

		return transformedData2;
	}
	

	/**
	 * This test runs delphinID on a single 4 second window from whistle contours saved
	 * in a mat file. The mat file also contains an image from Python. The test
	 * compares the Python image to to the image generated by exporting both images
	 * to a .mat file. The model is run on both images and results are compared
	 * 
	 * @return 
	 */
	public static boolean testDelphin2DImage(String imagePathOut) {

		System.out.println("------DelphinID image comparison test---------");

		double seglen = 4;

		//test the model
		//String modelPath =  "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Dde415/whistle_4s_415_model.zip";
		String modelPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Dde415/whistle_4s_415_model.zip";
		//		String modelPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_4s_415_model.zip";

		File file = new File(modelPath);
		System.out.println("File exists: ? " + file.exists()); 

		//the image to test
		String relMatPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/Dde_415_s10_SI20120620_171333_d042_50.mat";

		//Dde_415_s10_SI20120620_171333_d042_50
		//		double[] expectedOutput = new double[]{0.998737633, 0.998737633,	0.000146952,	1.49E-10,	0.001111862, 1.64E-10,	1.66E-08,	3.53E-06};

		//Dde_415_s10_SI20120620_171333_d042_27
		//		double[] expectedOutput = new double[]{0.8434083	3.48E-05	8.71E-05	0.14855734	9.86E-07	0.002373327	0.005538126};
		try {

			Path path = Paths.get(modelPath);

			//load the model
			SimpleArchiveModel model = new SimpleArchiveModel(new File(path.toString()));

			path = Paths.get(relMatPath);

			// Create MAT file with a scalar in a nested struct
			MatFile matFile = Mat5.readFromFile(path.toString());
			Matrix array = matFile.getArray("tfvalues");

			//the values for the whistle detector.
			double[][] whistleValues = DLMatFile.matrix2array(array);

			//the image after compression 
			array = matFile.getArray("whistle_image_gray_python");
			double[][] compressedWhistleImage = DLMatFile.matrix2array(array);

			array = matFile.getArray("timeseg");

			//the values for the whistle detector.
			double[][] pamguardWhistleImage = 	whistleScatter2Image(whistleValues, array.getDouble(0), seglen);

			//IMPORTANT - WE MUST TRANPOSE THE MATRIC HERE. 
			pamguardWhistleImage=PamArrayUtils.transposeMatrix(pamguardWhistleImage);

			//System.out.println("Size python: " + compressedWhistleImage.length + " x " + compressedWhistleImage[0].length);
			System.out.println("----Model outputs---");

			float[][][] input = new float[1][][];
			input[0] =  JamArr.doubleToFloat(compressedWhistleImage);

			System.out.println("Size Python: " + 	input[0].length + " x " + 	input[0][0].length);

			float[] outputPython = model.runModel(input);

			input[0] =   JamArr.doubleToFloat(pamguardWhistleImage);

			System.out.println("Size Java: " + 	input[0].length + " x " + 	input[0][0].length);

			//a bit ugly but works.
			//				transformedData2 = JamArr.transposeMatrix(transformedData2);


			float[] outputJava = model.runModel(input);

			boolean outputOk = false;
			for (int i=0; i<outputPython.length; i++) {
				System.out.println(String.format("Output Python: %.6f Java: %.6f",outputPython[i],outputJava[i] )); 
				if (Math.abs(outputPython[i] - outputJava[i])>0.05) outputOk=false;
			}

			if (imagePathOut!=null){
				MatFile matFileWrite = Mat5.newMatFile()
						.addArray("imagePython",DLMatFile.array2Matrix(compressedWhistleImage))
						.addArray("imageJava",DLMatFile.array2Matrix(pamguardWhistleImage));



				Mat5.writeToFile(matFileWrite, imagePathOut);
			}

			return outputOk; 

			//			JamArr.printArray(output);

		} 
		catch (MalformedModelException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		} 
	}
	
	
	/****---------------------1D Tests---------------------****/
	/*
	/*
	/*
	/****--------------------------------------------------****/

	/**
	 * This test runs delphinID on one 4 second window from whistle contours saved
	 * in a mat file. 
	 * 
	 * @return true if the test is passed. 
	 */
	public static boolean testDelphinIDArray(String arrayPathOut) {

		System.out.println("------DelphinID 1D array comparison test---------");

		double seglen = 4;
		//test the model
		//String modelPath =  "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Dde415/whistle_4s_415_model.zip";
		String modelPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/whistleclassifier.zip";
		//		String modelPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_4s_415_model.zip";

		File file = new File(modelPath);
		System.out.println("File exists: ? " + file.exists()); 

		//the image to test
		String relMatPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/Dde_415_s10_SI20120620_171333_d042_50.mat";
	
		try {

			Path path = Paths.get(modelPath);

			//load the model
			SimpleArchiveModel model = new SimpleArchiveModel(new File(path.toString()));
			
			path = Paths.get(relMatPath);

			// Create MAT file with a scalar in a nested struct
			MatFile matFile = Mat5.readFromFile(path.toString());
			Matrix array = matFile.getArray("tfvalues");

			//the values for the whistle detector.
			double[][] whistleValues = DLMatFile.matrix2array(array);
			
			double[] freqLimits = new double[] {2000., 20000.};
			
			//Create spectrum
			double[] whistleArray = whistle2AverageArray(whistleValues, array.getDouble(0), seglen, freqLimits);
			
			System.out.println("Whistle spectrum size intial: " + whistleArray.length); 
			
			//normalise the array 
			whistleArray = PamArrayUtils.divide(whistleArray, PamArrayUtils.sum(whistleArray)); 
			
			//down sample by a factor of 2
			whistleArray =  downSampleMean(whistleArray, 2);
			
			float[] whistleSpectrumF = PamArrayUtils.double2Float(whistleArray);
			
			System.out.println("Whistle spectrum size after transforms: " + whistleArray.length); 

			//generate model input
			float[][] input = new float[1][];
			input[0] = whistleSpectrumF;
			
			float[] outputJava = model.runModel(input);
			
			System.out.println("Whistle spectrum output: "); 
			PamArrayUtils.printArray(outputJava);


		} 
		catch (MalformedModelException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}
		
		
		return false;
	}
	
	/**
	 * Down sample a spectrum array. 
	 * @param spectrum - down sample a spectrum arra by a factor of 2 
	 * @return the down sampled array. 
	 */
	private static double[] downSampleMean(double[] spectrum, int factor) {
		
		int newlen = (int) Math.floor(((double) spectrum.length)/factor); 
		
		double[] downSample = new double[newlen]; 
		
		int n=0; 
		double mean =0; 
		for (int i=0; i<spectrum.length; i+=factor) {
			
			mean=0;
			for (int j=0; j<factor; j++) {
				mean += spectrum[i+j]; 
			}
			mean =mean/factor; 
			
			downSample[n]=mean;
			n++; 
		}
		
		return downSample; 
	}
	


	private static double[] whistle2AverageArray(double[][] whistleValues, double startseg, double seglen, double[] freqLimits) {

		//now perform the image transform in Java 
//		double[] freqLimits = new double[] {2000., 20000.};

		double freqBin = 100.;

		int nbins = (int) ((freqLimits[1] -freqLimits[0])/freqBin);

		System.out.println("Number of bins: " + nbins);

		double[] peakBins = new double[nbins];
		double minFreq, maxFreq;
		int n;
		int ntot = 0;
		for (int i=0; i<nbins; i++) {
			
			minFreq = i*freqBin+freqLimits[0];
			maxFreq = (i+1)*freqBin+freqLimits[0];

			n=0;
			for (int j=0; j<whistleValues.length ; j++) {
				if (whistleValues[j][1]>= minFreq && whistleValues[j][1]< maxFreq && whistleValues[j][0]>=startseg && whistleValues[j][0]<(startseg+seglen)) {
					n++;
				}
			}
			
			ntot=ntot+n;

			System.out.println("Bin: " + minFreq + " Hz   " + n); 
			peakBins[i]=n;
		}
		
		for (int i=0; i<nbins; i++) {
			peakBins[i]=peakBins[i]/ntot;
		}
		
		return peakBins;
	}



}
