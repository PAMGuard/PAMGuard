package rawDeepLearningClassifier.dlClassification.genericModel;


import java.io.IOException;
import org.jamdev.jdl4pam.genericmodel.GenericModel;
import ai.djl.MalformedModelException;


/**
 * The generic model. This is implemented in the JPAM library as Generic model 
 * but having it here gives a little more control without requiring constant changes 
 * the Maven dependencies.
 * 
 * @author Jamie Macaulay
 *
 */
public class PamGenericModel extends GenericModel {

	public PamGenericModel(String modelPath) throws MalformedModelException, IOException {
		super(modelPath);
		// TODO Auto-generated constructor stub
	}

}
