package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.io.File;
import java.io.IOException;

import org.jamdev.jdl4pam.ArchiveModel;

import ai.djl.MalformedModelException;

/**
 * A Tensorflow model packaged with a jar file. 
 * @author Jamie Macaulay
 *
 */
public class SimpleArchiveModel extends ArchiveModel {

	public SimpleArchiveModel(File file) throws MalformedModelException, IOException {
		super(file);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getAudioReprRelPath(String zipFolder) {
		// settings are in parent directory
		return "audio_repr_pg.json";
	}

	@Override
	public String getModelRelPath(String zipFolder) {
		// model is in parent directory
		return "model/saved_model.pb";
	}

	@Override
	public String getModelFolderName() {
		return "zip_model";
	}

}
