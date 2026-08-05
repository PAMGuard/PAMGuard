package deepWhistle;


import java.io.IOException;

import org.jamdev.jdl4pam.utils.DLMatFile;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

/**
 * Class for saving debug info to a .mat file
 * 
 * @author Jamie Macaulay
 *
 */
public class DeepWhistleMatFile {

	//For saving debug info
	private Mat5File matFile;

	private Struct matStruct; 
	
	public int maxNumStruct = 80;


	public DeepWhistleMatFile() {
		
	}
	
	
	public void initMatFile() {
		//TEMP
		matFile = Mat5.newMatFile();
		matStruct = Mat5.newStruct(maxNumStruct, 1);
	}
	
	public void addArray(float[][] array, String arrayName, int index, int channel) {
		Matrix outputMat =  DLMatFile.array2Matrix(array);
		
		matStruct.set(arrayName, index, outputMat);
		matStruct.set("size", index, Mat5.newScalar(index));
		matStruct.set("channel", index, Mat5.newScalar(channel));

	}
	
	public void saveMatFile(String filePath) {
		try {
			matFile.addArray("transforms", matStruct);
			Mat5.writeToFile(matFile, filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public int getMaxNumStruct() {
		return maxNumStruct;
	}


	public void setMaxNumStruct(int maxNumStruct) {
		this.maxNumStruct = maxNumStruct;
	}

}
