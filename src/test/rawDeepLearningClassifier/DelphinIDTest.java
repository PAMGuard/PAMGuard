package test.rawDeepLearningClassifier;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jamdev.jdl4pam.utils.DLMatFile;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDClickTest;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.ClickDetectionMAT;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.DetectionGroupMAT;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDWhistleTest;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;


public class DelphinIDTest { 

	/**
	 * The slop allowed when comparing prediction values
	 */
	public static double predSlop = 0.05; 


	@Test
	public void clickModelTest() {
		System.out.println("DelphinID: click2Spectrum test");

		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/DelphinID/clickClassifier_Jan25.zip";
		Path modelPath = Paths.get(relModelPath);


		String clicksMatPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/clicks_20200918_123234_classified.mat";
		Path clickPath = Paths.get(clicksMatPath);

		//load the clicks
		DetectionGroupMAT<ClickDetectionMAT> clicks = DelphinIDUtils.getClicksMAT(clicksMatPath);

		//run the model
		float[][] predictions = DelphinIDClickTest.testDelphinIDClickModel(modelPath.toString(), clicks, null, false); 

		String predRelPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/click_preds_PAM_20200918_123234_366.mat";
		Path predPath = Paths.get(predRelPath);


		Mat5File matFile;
		try {
			matFile = Mat5.readFromFile(predPath.toString());

			Matrix clickPredsMAT = matFile.getArray("predspython");
			double[][] clickPreds = DLMatFile.matrix2array(clickPredsMAT); 

			//now iterate through and check against true predictions.
			int classIndex = 1; //the class to use as the test.
			double calcPred;
			double truePred;
			for (int i=0; i<predictions.length; i++) {

				if (predictions[i]==null || clickPreds[i]==null) {
					continue;
				}

				System.out.print(String.format("Click segment: %d" , i));
				for (int j=0; j<predictions[i].length; j++) {
					System.out.print(String.format( " %.4f" , predictions[i][j])); 
				}
				System.out.println();

				calcPred = predictions[i][classIndex];
				
				truePred = clickPreds[i][classIndex];

				boolean passed = (calcPred - predSlop) < truePred &&  (calcPred + predSlop) > truePred;
				
				if (!passed) {
					
					System.out.print(String.format("SEGMENT FAIL: %d - true predictions" , i));
					for (int j=0; j<predictions[i].length; j++) {
						System.out.print(String.format( " %.4f" , clickPreds[i][j])); 
					}
					System.out.println();
					
				}
				assertTrue(passed);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			assertTrue(false); 
			e.printStackTrace();
		}

	}
	

//	@Test
//	public void whistle2SpectrumTest() {
//		System.out.println("DelphinID: whistle2Spectrum test");
//
//		assertEquals(false, false);
//	}
//
//
//	@Test
//	public void click2SpectrumTest() {
//		System.out.println("DelphinID: click model test");
//
//		assertEquals(false, false);
//	}

	@Test
	public void whistleModelTest() {
		System.out.println("DelphinID: whistle model test");
				
		double segLen = 4000.;
		double segHop = 1000.0;
		double startSeconds = 9.898656 + 0.1;  //seconds to start segments (so we can compare to Python) - note if this is wrong predicitons are slightly off...

		String relModelPath  =	"./src/test/resources/rawDeepLearningClassifier/DelphinID/whistleclassifier.zip";
		Path modelPath = Paths.get(relModelPath);


		String whistlesMatPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_contours_20200918_123234.mat";
		Path whisltePath = Paths.get(whistlesMatPath);

	
		float[][] predictions = DelphinIDWhistleTest.runWhistleModel(modelPath.toString(), whisltePath.toString(), null,  startSeconds,  segLen,  segHop);
	

		//the true whistle values
		String predRelPath = "./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_preds_PAM_20200918_123234_366.mat";
		Path predPath = Paths.get(predRelPath);


		Mat5File matFile;
		try {
			matFile = Mat5.readFromFile(predPath.toString());
			
			
			Matrix whistlesPredsMAT = matFile.getArray("predspython");
			double[][] whistlePreds = DLMatFile.matrix2array(whistlesPredsMAT); 
			
			Matrix startTimesMAT = matFile.getArray("starttimes");
			double[] startTimes = DelphinIDWhistleTest.matrix2array1D(startTimesMAT); 
			
			System.out.println("");

			//now iterate through and check against true predictions.
			int classIndex = 1; //the class to use as the test.
			double calcPred;
			double truePred;
			for (int i=0; i<startTimes.length; i++) {
				
				int index = (int)  startTimes[i]; 

				if (predictions[index]==null || whistlePreds[i]==null) {
					continue;
				}
				
				System.out.print(String.format("Whistle segment: %d" , index));
				for (int j=0; j<predictions[index].length; j++) {
					System.out.print(String.format( " %.4f" , predictions[index][j])); 
				}
				System.out.println();

				calcPred = predictions[index][classIndex];
				
				truePred = whistlePreds[i][classIndex];

				boolean passed = (calcPred - predSlop) < truePred &&  (calcPred + predSlop) > truePred;
				
				if (!passed) {
					
					System.out.print(String.format("SEGMENT FAIL: %.0f s index %d- true predictions" ,  startTimes[i], index));
					for (int j=0; j<predictions[index].length; j++) {
						System.out.print(String.format( " %.4f" , whistlePreds[i][j])); 
					}
					System.out.println();
					
				}
				
				assertTrue(passed);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			assertTrue(false); 
			e.printStackTrace();
		}


		assertEquals(false, false);
	}


	//	@Test
	//	public void whistle2ImageTest() {
	//	
	//		System.out.println("Whislte2Image test started");
	//
	//		/**
	//		 * Test whether the Whistles2Image transform works properly
	//		 */
	//		String relMatPath  =	"./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_image_example.mat";
	//
	//		Path path = Paths.get(relMatPath);
	//	
	//		// Create MAT file with a scalar in a nested struct
	//		try {
	//			MatFile matFile = Mat5.readFromFile(path.toString());
	//			Matrix array = matFile.getArray("tfvalues");
	//			
	//			//the values for the whistle detector.
	//			double[][] whistleValues = DLMatFile.matrix2array(array);
	//			
	////			//the image after compression 
	////			array = matFile.getArray("image1compressedgrayscale");
	////			double[][] compressedWhistleImage = DLMatFile.matrix2array(array);
	////
	////			//the whistle2Image transform image
	////			array = matFile.getArray("image1originalgrayscalenorm");
	////			double[][] whislteImage = DLMatFile.matrix2array(array);
	//			
	//			//now perform the image transform in Java 
	//			double[] freqLimits = new double[] {2000., 20000.};
	//			double[] size = new double[] {680., 480.};
	//			
	//			ArrayList<double[][]> whistleImageArr = new ArrayList<double[][]>();
	//			whistleImageArr.add(whistleValues);
	//			
	//			BufferedImage canvas = Whistles2Image.makeScatterImage(whistleImageArr, size, new double[]{50, 50. + 4.}, freqLimits,  10.);
	//
	//			double[][] imaged = new double[(int) size[0]][(int) size[1]];
	//
	//			float[] color = new float[3];
	//			Raster raster = canvas.getData();
	//			for (int i=0; i<imaged.length; i++) {
	//				for (int j=0; j<imaged[0].length; j++) {
	//					color = raster.getPixel(i,j, color);
	//					imaged[i][imaged[0].length-j-1] = (255-color[0])/255.; //normalize
	//				}
	//			}
	//			
	//			//imaged = PamArrayUtils.transposeMatrix(imaged);
	//
	//			//create the model transforms
	//			ArrayList<DLTransform> modelTransforms = new ArrayList<DLTransform>();
	////			modelTransforms.add(new FreqTransform(DLTransformType.SPECFLIP));
	////			modelTransforms.add(new FreqTransform(DLTransformType.SPECNORMALISE_MINIMAX));
	//			modelTransforms.add(new FreqTransform(DLTransformType.SPECRESIZE, new Number[] {Integer.valueOf(60), Integer.valueOf(80), SpecTransform.RESIZE_BICUBIC}));
	//			modelTransforms.add(new FreqTransform(DLTransformType.GAUSSIAN_FILTER, new Number[] {Double.valueOf(0.5)}));
	//
	//			
	//			SpecTransform specTransform = new SpecTransform(); 
	//			specTransform.setSpecData(imaged);
	//			specTransform.setSampleRate((float) (freqLimits[1]*2)); 
	//
	//			
	//			//set the spec transform
	//			((FreqTransform) modelTransforms.get(0)).setSpecTransfrom(specTransform);
	//
	//			//process all the transforms. 
	//			DLTransform transform = modelTransforms.get(0); 
	//			for (int i =0; i<modelTransforms.size(); i++) {
	//				transform = modelTransforms.get(i).transformData(transform); 
	//			}
	//			
	//			double[][] transformedData2 = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
	//			
	////			Bilinear interpolation1 = new Bilinear(JamArr.doubleToFloat(transformedData2));
	////			Bicubic interpolation2 = new Bicubic(JamArr.doubleToFloat(imaged));
	////
	////			System.out.println("Len input: " + imaged.length);
	////
	////			float[][] resizeArr = interpolation2.resize(Integer.valueOf(80), Integer.valueOf(60));
	////			
	////			System.out.println("Len resize: " + resizeArr.length);
	//
	//			System.out.println("Size Java: " + transformedData2.length + " x " + transformedData2[0].length);
	//
	//			//now save this image to a MATFILE
	//			// Create MAT file with a scalar in a nested struct
	//			MatFile matFileWrite = Mat5.newMatFile()
	//			    .addArray("image1originalgrayscalenorm",DLMatFile.array2Matrix(imaged))
	//			    .addArray("imagecompressedgrayscalenorm",DLMatFile.array2Matrix(transformedData2));
	////		    	.addArray("imagecompressedgrayscalenorm_nearest",DLMatFile.array2Matrix(JamArr.floatToDouble(resizeArr)));
	//
	//			// Serialize to disk using default configurations
	//			Mat5.writeToFile(matFileWrite, "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/whistle_image_example_java.mat");
	//			
	//			System.out.println("Whislte2Image test finished");
	//			
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//			assertEquals(false, false);
	//		}
	//
	//	}


}
