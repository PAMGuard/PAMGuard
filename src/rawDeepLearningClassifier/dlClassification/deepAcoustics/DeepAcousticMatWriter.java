package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import java.io.IOException;

import org.jamdev.jdl4pam.deepAcoustics.DeepAcousticResultArray;
import org.jamdev.jdl4pam.utils.DLMatFile;

import PamguardMVC.PamDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Struct;

/**
 * Write .mat files for deepAcoustic model results. Used primarily for
 * debugging.
 */
public class DeepAcousticMatWriter {

	public String getMatFileOut() {
		return matFileOut;
	}

	public void setMatFileOut(String matFileOut) {
		this.matFileOut = matFileOut;
	}

	private String matFileOut = null; // if not null, then write the results to a mat file with this name.


	private Mat5File matFile;

	/**
	 * Struct to write results to the .mat file
	 */
	private Struct matResults;


	/**
	 * Add a new image to the .mat file.
	 * 
	 * @param image          The image data to add, as a 2D float array.
	 * @param timeMillisStart The start time in milliseconds for the image.
	 * @param sampleNumber   The sample number associated with the image.
	 */
	public void addMatImage(float[][] image, long timeMillisStart, long sampleNumber, int matCount) {

		if (matFile == null) {
			// Create a new Mat5File if it doesn't exist
			matFile = Mat5.newMatFile();
			matResults = Mat5.newStruct(100, 1);
		}

		//		/**** Create a mat file with the results. ***/
		//		float[][] im = new float[transformedDataStack.length][transformedDataStack[0].length];
		//
		//		for (int i = 0; i < transformedDataStack.length; i++) {
		//			for (int j = 0; j < transformedDataStack[0].length; j++) {
		//				im[i][j] = transformedDataStack[i][j][0];
		//			}
		//		}

		matResults.set("inputimage", matCount, DLMatFile.array2Matrix(image));
		matResults.set("timeMillis", matCount, Mat5.newScalar(timeMillisStart));
		matResults.set("samplenumber", matCount, Mat5.newScalar(sampleNumber));
		
	}

	public void addMatDLResult(DeepAcousticResultArray deepAcousticResultArray, long timeMillisStart, long sampleNumber, int matCount) {
		if (matFile == null) {
			// Create a new Mat5File if it doesn't exist
			matFile = Mat5.newMatFile();
			matResults = Mat5.newStruct(100, 1);
		}

		if (deepAcousticResultArray == null || deepAcousticResultArray.size() == 0) {
			matResults.set("boxes", matCount, Mat5.EMPTY_MATRIX);
			return;
		}
		else {
			double[][] boundingboxes = new double[deepAcousticResultArray.size()][];
			for (int i = 0; i < deepAcousticResultArray.size(); i++) {
				boundingboxes[i] = deepAcousticResultArray.get(i).getBoundingBox();
			}
			matResults.set("boxes", matCount, DLMatFile.array2Matrix(boundingboxes));
		}
		
		matResults.set("timeMillis", matCount, Mat5.newScalar(timeMillisStart));
		matResults.set("samplenumber", matCount, Mat5.newScalar(sampleNumber));


	}

	/**
	 * Write the results to a .mat file if matFileOut is set.
	 */
	public void writeMatFile() {
		if (matFileOut == null) {
			System.err.println("DeepAcousticMatWriter: No mat file name set. Not writing results.");
			return;
		}

		try {
			matFile.addArray("input_images", matResults); 
			Mat5.writeToFile(matFile, matFileOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Reset the writer state, clearing any existing data.
	 */
	public void reset() {
		matFile = null;
		matResults = null;
		matFileOut = null;
		System.out.println("DeepAcousticMatWriter: Resetting writer state.");
	}



}
