package rawDeepLearningClassifier.dlClassification.ketos;

import java.io.File;
import java.io.IOException;

import org.jamdev.jdl4pam.ArchiveModel;
import org.jamdev.jdl4pam.genericmodel.GenericModelParams;
import org.jamdev.jdl4pam.ketos.KetosModel;
import org.jamdev.jdl4pam.ketos.KetosParams;

import ai.djl.MalformedModelException;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;

public class KetosWorker2 extends ArchiveModelWorker {
	
	/**
	 * Load a model from a file
	 * @param currentPath- the path to the model.
	 * @return- the loaded model object. 
	 * @throws MalformedModelException
	 * @throws IOException
	 */
	public ArchiveModel loadModel(String currentPath2) throws MalformedModelException, IOException {
		return new KetosModel(new File(currentPath2)); 
	}
	
	/**
	 * Create the parameters from a JSON string. 
	 * @param jsonString - the json string. 
	 * @return the paramters. 
	 */
	public GenericModelParams makeModelParams(String jsonString) {
		//ketos parameters are non standard and need a bit of extra work to get right. 
		//This also deal with legacy paramters. 
		KetosParams params =  new KetosParams(jsonString); 
//		System.out.println(params); 
		return params;
	}
}
