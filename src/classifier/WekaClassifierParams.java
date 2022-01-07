package classifier;

import java.io.Serializable;

public class WekaClassifierParams extends ClassifierParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 2L;

	private weka.classifiers.AbstractClassifier classifier;
		
	public WekaClassifierParams() {
		super(WekaClassifierParams.class);
		
	}

	public void setClassifier(weka.classifiers.AbstractClassifier classifier) {
		this.classifier = classifier;
	}

	public weka.classifiers.AbstractClassifier getClassifier() {
		return classifier;
	}


}
