package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.junit.jupiter.api.Test;

import rawDeepLearningClassifier.dlClassification.delphinID.Whistles2Image;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;

public class DelphinIDTest { 
	
	
	@Test
	public void whistle2ImageTest() {
	
		System.out.println("Whislte2Image test started");

		/**
		 * Test whether the Whistles2Image transform works properly
		 */
		String relMatPath  =	"./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_image_example.mat";

		Path path = Paths.get(relMatPath);
	
		// Create MAT file with a scalar in a nested struct
		try {
			MatFile matFile = Mat5.readFromFile(path.toString());
			Matrix array = matFile.getArray("tfvalues");
			
			//the values for the whistle detector.
			double[][] whistleValues = DLMatFile.matrix2array(array);
			
			//the image after compression 
			array = matFile.getArray("image1compressedgrayscale");
			double[][] compressedWhistleImage = DLMatFile.matrix2array(array);

			//the whistle2Image transform image
			array = matFile.getArray("image1originalgrayscalenorm");
			double[][] whislteImage = DLMatFile.matrix2array(array);
			
			//now perform the image transform in Java 
			double[] freqLimits = new double[] {0., 20000.};
			double[] size = new double[] {680., 480.};
			
			ArrayList<double[][]> whistleImageArr = new ArrayList<double[][]>();
			whistleImageArr.add(whistleValues);
			
			BufferedImage canvas = Whistles2Image.makeScatterImage(whistleImageArr, size, new double[]{48, 48. + 4.}, freqLimits,  5.);

			double[][] imaged = new double[(int) size[0]][(int) size[1]];

			float[] color = new float[3];
			Raster raster = canvas.getData();
			for (int i=0; i<imaged.length; i++) {
				for (int j=0; j<imaged[0].length; j++) {
					color = raster.getPixel(i, j, color);
					imaged[i][j] = (255-color[0])/255.; //normalize
				}
			}
			
			ArrayList<DLTransform> transforms = new ArrayList<DLTransform>();
			transforms.add(new FreqTransform(DLTransformType.SPECRESIZE, new Number[] {Integer.valueOf(64), Integer.valueOf(48)}));
			
//			
//			//set the spec transform
//			((FreqTransform) transforms.get(0)).setSpecTransfrom(whistles2Image.getSpecTransfrom());
//
//			//process all the transforms. 
//			DLTransform transform = modelTransforms.get(0); 
//			for (int i =0; i<modelTransforms.size(); i++) {
//				transform = modelTransforms.get(i).transformData(transform); 
//			}
//			
//			transformedData2 = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
//			transformedDataStack[j] = DLUtils.toFloatArray(transformedData2); 
//			
			
			
			//now save this image to a MATFILE
			// Create MAT file with a scalar in a nested struct
			MatFile matFileWrite = Mat5.newMatFile()
			    .addArray("image1originalgrayscalenorm",DLMatFile.array2Matrix(imaged));
			// Serialize to disk using default configurations
			Mat5.writeToFile(matFileWrite, "C:\\Users\\Jamie Macaulay\\MATLAB Drive\\MATLAB\\PAMGUARD\\deep_learning\\delphinID\\whistle_image_example_java.mat");
			
			System.out.println("Whislte2Image test finished");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertEquals(false, false);
		}
		
		
		
		
	}

}
