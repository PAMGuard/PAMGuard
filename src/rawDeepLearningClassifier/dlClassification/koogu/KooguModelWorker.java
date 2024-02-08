package rawDeepLearningClassifier.dlClassification.koogu;

import java.io.File;
import java.io.IOException;

import org.jamdev.jdl4pam.ArchiveModel;
import org.jamdev.jdl4pam.koogu.KooguModel;

import ai.djl.MalformedModelException;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;

public class KooguModelWorker extends ArchiveModelWorker {

	
	/**
	 * Load a model from a file
	 * @param currentPath- the path to the model.
	 * @return- the loaded model object. 
	 * @throws MalformedModelException
	 * @throws IOException
	 */
	public ArchiveModel loadModel(String currentPath2) throws MalformedModelException, IOException {
		return new KooguModel(new File(currentPath2)); 
	}
}
