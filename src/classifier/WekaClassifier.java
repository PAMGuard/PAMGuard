package classifier;

import weka.classifiers.AbstractClassifier;

public class WekaClassifier extends AbstractWekaClassifier {

	private String classifierName;
	
	/**
	 * @param classifierName
	 */
	public WekaClassifier(AbstractClassifier wekaClassifier, String classifierName) {
		super();
		setWekaClassifier(wekaClassifier);
		this.classifierName = classifierName;
	}



	@Override
	public String getClassifierName() {
		return classifierName;
	}

}
