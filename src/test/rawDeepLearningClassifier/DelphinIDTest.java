package test.rawDeepLearningClassifier;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.JamArr;
import org.jamdev.jpamutils.interpolation.Bicubic;
import org.jamdev.jpamutils.interpolation.Bilinear;
import org.jamdev.jpamutils.interpolation.NearestNeighbor;
import org.jamdev.jpamutils.spectrogram.SpecTransform;
import org.junit.jupiter.api.Test;

import PamUtils.PamArrayUtils;
import ai.djl.MalformedModelException;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;
import rawDeepLearningClassifier.dlClassification.delphinID.Whistles2Image;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;

public class DelphinIDTest { 
	
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
	
	@Test
	public static void modelInputTest() {
		
		//test whether a single segment gives the correct answer. 
//		boolean result = rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDTest.testDelphinIDImage(null);
		
//		assertTrue(result); 
	}

}
